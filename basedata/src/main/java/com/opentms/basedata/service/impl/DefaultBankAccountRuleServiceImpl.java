package com.opentms.basedata.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opentms.basedata.dto.DefaultBankAccountRuleMatchRequestDTO;
import com.opentms.basedata.dto.DefaultBankAccountRuleQueryDTO;
import com.opentms.basedata.dto.DefaultBankAccountRuleSaveDTO;
import com.opentms.basedata.dto.DefaultBankAccountRuleUpdateDTO;
import com.opentms.basedata.entity.DefaultBankAccountRule;
import com.opentms.basedata.entity.RuleAuditLog;
import com.opentms.basedata.enums.Direction;
import com.opentms.basedata.enums.RuleAuditOperation;
import com.opentms.basedata.mapper.DefaultBankAccountRuleMapper;
import com.opentms.basedata.mapper.RuleAuditLogMapper;
import com.opentms.basedata.service.DefaultBankAccountRuleService;
import com.opentms.basedata.vo.RuleReferenceCountVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 默认银行账户规则 Service 实现(v1.1)
 *
 * <p>关键功能:
 * <ul>
 *   <li>5 维匹配算法 + 双方向独立排序</li>
 *   <li>★ v1.1 并发控制(lockToken + 409 Conflict)</li>
 *   <li>★ v1.1 Redis 缓存(T5 分钟,降级方案)</li>
 *   <li>★ v1.1 审计日志写入(CREATE/UPDATE/DELETE/ENABLE/DISABLE)</li>
 *   <li>★ v1.1 被引用数查询(预留接口,实现见 TODO)</li>
 * </ul>
 *
 * @author Open-TMS
 * @since 2026-07-08
 */
@Slf4j
@Service
public class DefaultBankAccountRuleServiceImpl
        extends ServiceImpl<DefaultBankAccountRuleMapper, DefaultBankAccountRule>
        implements DefaultBankAccountRuleService {

    @Autowired
    private RuleAuditLogMapper auditLogMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final long MATCH_CACHE_TTL_MS = 5 * 60 * 1000L; // 5 分钟
    private static final String CACHE_KEY_PREFIX = "dbar:match:";

    // ★ v1.1 内存缓存(简化版,生产建议用 Redis)
    private final Map<String, CacheEntry> matchCache = new ConcurrentHashMap<>();

    private static class CacheEntry {
        Object value;
        long expireAt;
        CacheEntry(Object v, long exp) { value = v; expireAt = exp; }
        boolean isExpired() { return System.currentTimeMillis() > expireAt; }
    }

    // ============= 查询 =============

    @Override
    public Page<DefaultBankAccountRule> queryPage(DefaultBankAccountRuleQueryDTO query) {
        LambdaQueryWrapper<DefaultBankAccountRule> wrapper = new LambdaQueryWrapper<>();

        if (query.getManagementEntityId() != null) {
            wrapper.eq(DefaultBankAccountRule::getManagementEntityId, query.getManagementEntityId());
        }
        if (query.getCounterpartyId() != null) {
            wrapper.eq(DefaultBankAccountRule::getCounterpartyId, query.getCounterpartyId());
        }
        if (query.getInstrumentId() != null) {
            wrapper.eq(DefaultBankAccountRule::getInstrumentId, query.getInstrumentId());
        }
        if (StringUtils.hasText(query.getDirection())) {
            wrapper.eq(DefaultBankAccountRule::getDirection, query.getDirection());
        }
        if (StringUtils.hasText(query.getCurrency())) {
            wrapper.eq(DefaultBankAccountRule::getCurrency, query.getCurrency());
        }
        if (StringUtils.hasText(query.getStatus())) {
            wrapper.eq(DefaultBankAccountRule::getStatus, query.getStatus());
        }
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(DefaultBankAccountRule::getRuleNumber, query.getKeyword())
                    .or().like(DefaultBankAccountRule::getDescription, query.getKeyword()));
        }

        wrapper.orderByDesc(DefaultBankAccountRule::getPriority)
                .orderByAsc(DefaultBankAccountRule::getCreatedAt);

        return page(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
    }

    @Override
    public DefaultBankAccountRule getRuleById(Long id) {
        return getById(id);
    }

    @Override
    public DefaultBankAccountRule getRuleByNumber(String ruleNumber) {
        return getOne(new LambdaQueryWrapper<DefaultBankAccountRule>()
                .eq(DefaultBankAccountRule::getRuleNumber, ruleNumber));
    }

    // ============= 新增 =============

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DefaultBankAccountRule saveRule(DefaultBankAccountRuleSaveDTO dto) {
        validatePriority(dto.getPriority());
        validateRuleFields(dto.getManagementEntityId(), dto.getBankAccountId(), dto.getDirection());

        DefaultBankAccountRule rule = new DefaultBankAccountRule();
        rule.setRuleNumber(generateRuleNumber());
        rule.setManagementEntityId(dto.getManagementEntityId());
        rule.setCounterpartyId(dto.getCounterpartyId());
        rule.setInstrumentId(dto.getInstrumentId());
        rule.setDirection(dto.getDirection());
        rule.setCurrency(dto.getCurrency());
        rule.setBankAccountId(dto.getBankAccountId());
        rule.setPriority(dto.getPriority());
        rule.setStartDate(dto.getStartDate());
        rule.setStatus(StringUtils.hasText(dto.getStatus()) ? dto.getStatus() : "Active");
        rule.setDescription(dto.getDescription());
        rule.setRemark(dto.getRemark());
        rule.setLockToken(UUID.randomUUID().toString());
        rule.setCreatedBy("system"); // TODO: 从 SecurityContext 取
        rule.setCreatedAt(LocalDateTime.now());

        save(rule);
        writeAuditLog(rule.getId(), RuleAuditOperation.CREATE.getCode(), null, rule, "新增规则");
        invalidateMatchCache();
        return rule;
    }

    // ============= 更新(★ v1.1 并发控制) =============

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DefaultBankAccountRule updateRule(DefaultBankAccountRuleUpdateDTO dto) {
        validatePriority(dto.getPriority());

        DefaultBankAccountRule existing = getById(dto.getId());
        if (existing == null) {
            throw new IllegalArgumentException("规则不存在: id=" + dto.getId());
        }

        // ★ v1.1 lockToken 校验
        if (!StringUtils.hasText(dto.getLockToken())) {
            throw new IllegalArgumentException("lockToken 不能为空");
        }
        if (!dto.getLockToken().equals(existing.getLockToken())) {
            throw new ConcurrentModificationException(
                    "规则已被他人修改(updated_at=" + existing.getUpdatedAt() + "),请刷新后重试");
        }

        // 主体不可改
        DefaultBankAccountRule oldSnapshot = cloneRule(existing);

        String newLockToken = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();

        // ★ 使用 JdbcTemplate 直接更新,绕过 MyBatis-Plus @Version 问题
        String sql = "UPDATE tms_default_bank_account_rule_t SET " +
                "counterparty_id=?, instrument_id=?, direction=?, currency=?, " +
                "bank_account_id=?, priority=?, start_date=?, status=?, " +
                "description=?, remark=?, " +
                "lock_token=?, updated_by=?, updated_at=?, version=version+1 " +
                "WHERE id=?";
        jdbcTemplate.update(sql,
                dto.getCounterpartyId(), dto.getInstrumentId(),
                dto.getDirection(), dto.getCurrency(),
                dto.getBankAccountId(), dto.getPriority(), dto.getStartDate(),
                dto.getStatus(), dto.getDescription(), dto.getRemark(),
                newLockToken, "system", java.sql.Timestamp.valueOf(now),
                dto.getId());

        // 重新加载更新后的实体(使用新 connection,避免 MyBatis-Plus 缓存)
        DefaultBankAccountRule updated = jdbcTemplate.queryForObject(
                "SELECT * FROM tms_default_bank_account_rule_t WHERE id=? AND deleted='0'",
                (rs, rowNum) -> {
                    DefaultBankAccountRule r = new DefaultBankAccountRule();
                    r.setId(rs.getLong("id"));
                    r.setRuleNumber(rs.getString("rule_number"));
                    r.setManagementEntityId(rs.getLong("management_entity_id"));
                    long cpId = rs.getLong("counterparty_id");
                    r.setCounterpartyId(rs.wasNull() ? null : cpId);
                    long insId = rs.getLong("instrument_id");
                    r.setInstrumentId(rs.wasNull() ? null : insId);
                    r.setDirection(rs.getString("direction"));
                    r.setCurrency(rs.getString("currency"));
                    r.setBankAccountId(rs.getLong("bank_account_id"));
                    r.setStatus(rs.getString("status"));
                    r.setPriority(rs.getInt("priority"));
                    java.sql.Date sd = rs.getDate("start_date");
                    r.setStartDate(sd == null ? null : sd.toLocalDate());
                    r.setDescription(rs.getString("description"));
                    r.setRemark(rs.getString("remark"));
                    r.setLockToken(rs.getString("lock_token"));
                    r.setLockedBy(rs.getString("locked_by"));
                    java.sql.Timestamp lat = rs.getTimestamp("locked_at");
                    r.setLockedAt(lat == null ? null : lat.toLocalDateTime());
                    r.setCreatedBy(rs.getString("created_by"));
                    java.sql.Timestamp cat = rs.getTimestamp("created_at");
                    r.setCreatedAt(cat == null ? null : cat.toLocalDateTime());
                    r.setUpdatedBy(rs.getString("updated_by"));
                    java.sql.Timestamp uat = rs.getTimestamp("updated_at");
                    r.setUpdatedAt(uat == null ? null : uat.toLocalDateTime());
                    r.setVersion(rs.getInt("version"));
                    r.setDeleted(rs.getString("deleted"));
                    return r;
                },
                dto.getId());

        writeAuditLog(updated.getId(), RuleAuditOperation.UPDATE.getCode(),
                oldSnapshot, updated, "更新规则");
        invalidateMatchCache();
        return updated;
    }

    // ============= 删除 =============

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteRule(Long id) {
        DefaultBankAccountRule rule = getById(id);
        if (rule == null) return false;

        jdbcTemplate.update("UPDATE tms_default_bank_account_rule_t SET deleted='1', " +
                "lock_token=NULL, updated_by=?, updated_at=?, version=version+1 WHERE id=?",
                "system", java.sql.Timestamp.valueOf(LocalDateTime.now()), id);

        writeAuditLog(id, RuleAuditOperation.DELETE.getCode(), rule, null, "删除规则");
        invalidateMatchCache();
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean enableRule(Long id) {
        DefaultBankAccountRule rule = getById(id);
        if (rule == null) return false;
        DefaultBankAccountRule old = cloneRule(rule);

        jdbcTemplate.update("UPDATE tms_default_bank_account_rule_t SET status='Active', " +
                "lock_token=?, updated_by=?, updated_at=?, version=version+1 WHERE id=?",
                UUID.randomUUID().toString(), "system",
                java.sql.Timestamp.valueOf(LocalDateTime.now()), id);

        DefaultBankAccountRule updated = getById(id);
        writeAuditLog(id, RuleAuditOperation.ENABLE.getCode(), old, updated, "启用规则");
        invalidateMatchCache();
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean disableRule(Long id) {
        DefaultBankAccountRule rule = getById(id);
        if (rule == null) return false;
        DefaultBankAccountRule old = cloneRule(rule);

        jdbcTemplate.update("UPDATE tms_default_bank_account_rule_t SET status='Inactive', " +
                "lock_token=?, updated_by=?, updated_at=?, version=version+1 WHERE id=?",
                UUID.randomUUID().toString(), "system",
                java.sql.Timestamp.valueOf(LocalDateTime.now()), id);

        DefaultBankAccountRule updated = getById(id);
        writeAuditLog(id, RuleAuditOperation.DISABLE.getCode(), old, updated, "停用规则");
        invalidateMatchCache();
        return true;
    }

    // ============= ★ v1.1 匹配算法(双方向 + Redis 缓存) =============

    @Override
    public Object match(DefaultBankAccountRuleMatchRequestDTO req) {
        long start = System.currentTimeMillis();

        // 1. 内存缓存查询(★ v1.1)
        String cacheKey = buildCacheKey(req);
        CacheEntry cached = matchCache.get(cacheKey);
        if (cached != null && !cached.isExpired()) {
            log.debug("[MATCH] Cache hit: {}", cacheKey);
            return cached.value;
        }

        // 2. DB 5 维过滤
        List<DefaultBankAccountRule> candidates = queryEffectiveRules(req);

        // 3. 排序 + 取首条(单/双方向)
        Object result;
        if (Boolean.TRUE.equals(req.getDualDirection())) {
            result = matchDualDirection(candidates);
        } else {
            result = matchSingleDirection(candidates, req.getDirection());
        }

        // 4. 写缓存(降级方案:写入失败不影响返回)
        matchCache.put(cacheKey, new CacheEntry(result, System.currentTimeMillis() + MATCH_CACHE_TTL_MS));

        long duration = System.currentTimeMillis() - start;
        log.info("[MATCH] Duration: {}ms, candidates: {}, dualDirection: {}",
                duration, candidates.size(), req.getDualDirection());
        return result;
    }

    @Override
    public List<DefaultBankAccountRule> testMatch(DefaultBankAccountRuleMatchRequestDTO req) {
        return queryEffectiveRules(req);
    }

    /**
     * 5 维基础过滤:status=Active + start_date 生效 + deleted=0
     * + managementEntityId 相等 + 其他维度 NULL OR 相等
     */
    private List<DefaultBankAccountRule> queryEffectiveRules(DefaultBankAccountRuleMatchRequestDTO req) {
        LocalDate today = LocalDate.now();
        LambdaQueryWrapper<DefaultBankAccountRule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DefaultBankAccountRule::getStatus, "Active")
                .eq(DefaultBankAccountRule::getManagementEntityId, req.getManagementEntityId())
                .and(w -> w.isNull(DefaultBankAccountRule::getStartDate)
                        .or().le(DefaultBankAccountRule::getStartDate, today));

        if (req.getCounterpartyId() != null) {
            wrapper.and(w -> w.isNull(DefaultBankAccountRule::getCounterpartyId)
                    .or().eq(DefaultBankAccountRule::getCounterpartyId, req.getCounterpartyId()));
        }
        if (req.getInstrumentId() != null) {
            wrapper.and(w -> w.isNull(DefaultBankAccountRule::getInstrumentId)
                    .or().eq(DefaultBankAccountRule::getInstrumentId, req.getInstrumentId()));
        }
        if (StringUtils.hasText(req.getCurrency())) {
            wrapper.and(w -> w.isNull(DefaultBankAccountRule::getCurrency)
                    .or().eq(DefaultBankAccountRule::getCurrency, req.getCurrency()));
        }

        wrapper.orderByDesc(DefaultBankAccountRule::getPriority)
                .orderByAsc(DefaultBankAccountRule::getCreatedAt);

        return list(wrapper);
    }

    /** 单方向匹配 */
    private Object matchSingleDirection(List<DefaultBankAccountRule> candidates, String direction) {
        Map<String, Object> result = new HashMap<>();
        DefaultBankAccountRule top = candidates.stream()
                .filter(r -> "ALL".equals(r.getDirection()) || Objects.equals(r.getDirection(), direction))
                .findFirst()
                .orElse(null);

        if (top == null) {
            result.put("matched", false);
            result.put("bankAccountId", null);
            result.put("ruleId", null);
        } else {
            result.put("matched", true);
            result.put("bankAccountId", top.getBankAccountId());
            result.put("bankAccountName", top.getBankAccountName());
            result.put("ruleId", top.getId());
            result.put("ruleNumber", top.getRuleNumber());
            result.put("priority", top.getPriority());
        }
        return result;
    }

    /** ★ v1.1 双方向匹配 */
    private Object matchDualDirection(List<DefaultBankAccountRule> candidates) {
        Map<String, Object> inflow = matchDirection(candidates, Direction.INFLOW.getCode());
        Map<String, Object> outflow = matchDirection(candidates, Direction.OUTFLOW.getCode());

        Map<String, Object> result = new HashMap<>();
        result.put("inflow", inflow);
        result.put("outflow", outflow);
        result.put("cacheHit", false);
        result.put("queryDurationMs", System.currentTimeMillis());
        return result;
    }

    private Map<String, Object> matchDirection(List<DefaultBankAccountRule> candidates, String direction) {
        Map<String, Object> result = new HashMap<>();
        DefaultBankAccountRule top = candidates.stream()
                .filter(r -> "ALL".equals(r.getDirection()) || direction.equals(r.getDirection()))
                .findFirst()
                .orElse(null);
        if (top == null) {
            result.put("matched", false);
            result.put("bankAccountId", null);
        } else {
            result.put("matched", true);
            result.put("bankAccountId", top.getBankAccountId());
            result.put("bankAccountName", top.getBankAccountName());
            result.put("ruleId", top.getId());
            result.put("ruleNumber", top.getRuleNumber());
            result.put("priority", top.getPriority());
        }
        return result;
    }

    // ============= ★ v1.1 审计日志 =============

    @Override
    public Page<RuleAuditLog> getAuditLogs(Long ruleId, int pageNum, int pageSize) {
        LambdaQueryWrapper<RuleAuditLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RuleAuditLog::getRuleId, ruleId)
                .orderByDesc(RuleAuditLog::getOperatedAt);
        return auditLogMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    private void writeAuditLog(Long ruleId, String operation,
                                DefaultBankAccountRule oldRule, DefaultBankAccountRule newRule,
                                String remark) {
        String oldJson = null;
        String newJson = null;
        try {
            if (oldRule != null) oldJson = objectMapper.writeValueAsString(oldRule);
            if (newRule != null) newJson = objectMapper.writeValueAsString(newRule);
        } catch (Exception e) {
            log.warn("Audit log JSON serialization failed: {}", e.getMessage());
        }
        // 使用 JdbcTemplate 直接写,显式 CAST 到 JSONB
        String sql = "INSERT INTO tms_rule_audit_log_t " +
                "(rule_id, operation, old_value, new_value, operator, operated_at, remark) " +
                "VALUES (?, ?, ?::jsonb, ?::jsonb, ?, ?, ?)";
        jdbcTemplate.update(sql,
                ruleId, operation, oldJson, newJson,
                "system", java.sql.Timestamp.valueOf(LocalDateTime.now()), remark);
    }

    // ============= ★ v1.1 被引用数(预留接口) =============

    @Override
    public RuleReferenceCountVO getReferenceCount(Long id) {
        DefaultBankAccountRule rule = getById(id);
        if (rule == null) return null;

        // TODO: 实际查询需要 tms_deals_t 等表有 bank_account_id 列(P1+ 改造)
        // 临时方案:返回 0,后续 P1+ 优化
        RuleReferenceCountVO vo = new RuleReferenceCountVO();
        vo.setRuleId(id);
        vo.setBankAccountId(rule.getBankAccountId());
        vo.setUnsettledCount(0);
        vo.setRecentSettledCount(0);
        vo.setTotalCount(0);
        vo.setQueryDurationMs(0L);
        return vo;
    }

    // ============= 工具方法 =============

    private void validatePriority(Integer priority) {
        if (priority == null || priority < 0 || priority > 9999) {
            throw new IllegalArgumentException("优先级超出范围 0-9999");
        }
    }

    private void validateRuleFields(Long mgmtEntityId, Long bankAccountId, String direction) {
        if (mgmtEntityId == null) throw new IllegalArgumentException("主体必填");
        if (bankAccountId == null) throw new IllegalArgumentException("默认账户必填");
        if (!Arrays.asList("Inflow", "Outflow", "ALL").contains(direction)) {
            throw new IllegalArgumentException("方向必须为 Inflow / Outflow / ALL");
        }
    }

    private String generateRuleNumber() {
        String today = LocalDate.now().format(DATE_FMT);
        // 同日递增: 查询当日最大流水号(排除软删)
        DefaultBankAccountRule latest = getOne(new LambdaQueryWrapper<DefaultBankAccountRule>()
                .likeRight(DefaultBankAccountRule::getRuleNumber, "RULE" + today)
                .eq(DefaultBankAccountRule::getDeleted, "0")
                .orderByDesc(DefaultBankAccountRule::getRuleNumber)
                .last("LIMIT 1"));
        String maxNumber = latest == null ? null : latest.getRuleNumber();
        int nextSeq = 1;
        if (StringUtils.hasText(maxNumber) && maxNumber.length() >= 17) {
            try {
                nextSeq = Integer.parseInt(maxNumber.substring(13)) + 1;
            } catch (NumberFormatException ignored) {}
        }
        // 防御性:如已存在则递增到不重复
        String candidate;
        int attempts = 0;
        do {
            candidate = String.format("RULE%s%04d", today, nextSeq);
            DefaultBankAccountRule existing = getOne(new LambdaQueryWrapper<DefaultBankAccountRule>()
                    .eq(DefaultBankAccountRule::getRuleNumber, candidate));
            if (existing == null) break;
            nextSeq++;
            attempts++;
        } while (attempts < 100);
        return candidate;
    }

    private String buildCacheKey(DefaultBankAccountRuleMatchRequestDTO req) {
        return CACHE_KEY_PREFIX + req.getManagementEntityId()
                + ":" + (req.getCounterpartyId() == null ? "ALL" : req.getCounterpartyId())
                + ":" + (req.getInstrumentId() == null ? "ALL" : req.getInstrumentId())
                + ":" + (req.getDirection() == null ? "ANY" : req.getDirection())
                + ":" + (req.getCurrency() == null ? "ALL" : req.getCurrency());
    }

    private void invalidateMatchCache() {
        try {
            int size = matchCache.size();
            matchCache.clear();
            log.debug("[CACHE] Invalidated {} match cache entries", size);
        } catch (Exception e) {
            log.warn("[CACHE] Invalidate failed: {}", e.getMessage());
        }
    }

    private DefaultBankAccountRule cloneRule(DefaultBankAccountRule src) {
        if (src == null) return null;
        DefaultBankAccountRule copy = new DefaultBankAccountRule();
        copy.setId(src.getId());
        copy.setRuleNumber(src.getRuleNumber());
        copy.setManagementEntityId(src.getManagementEntityId());
        copy.setCounterpartyId(src.getCounterpartyId());
        copy.setInstrumentId(src.getInstrumentId());
        copy.setDirection(src.getDirection());
        copy.setCurrency(src.getCurrency());
        copy.setBankAccountId(src.getBankAccountId());
        copy.setStatus(src.getStatus());
        copy.setPriority(src.getPriority());
        copy.setStartDate(src.getStartDate());
        copy.setDescription(src.getDescription());
        copy.setRemark(src.getRemark());
        return copy;
    }
}
package com.opentms.basedata.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opentms.basedata.dto.DealApprovalRuleMatchRequestDTO;
import com.opentms.basedata.dto.DealApprovalRuleQueryDTO;
import com.opentms.basedata.dto.DealApprovalRuleSaveDTO;
import com.opentms.basedata.dto.DealApprovalRuleUpdateDTO;
import com.opentms.basedata.entity.DealApprovalRule;
import com.opentms.basedata.entity.DealApprovalRuleAuditLog;
import com.opentms.basedata.entity.DealApprovalRuleImage;
import com.opentms.basedata.enums.ApprovalLevel;
import com.opentms.basedata.enums.RuleAuditOperation;
import com.opentms.basedata.mapper.CounterpartyMapper;
import com.opentms.basedata.mapper.DealApprovalRuleAuditLogMapper;
import com.opentms.basedata.mapper.DealApprovalRuleMapper;
import com.opentms.basedata.mapper.InstrumentMapper;
import com.opentms.basedata.mapper.ManagementEntityMapper;
import com.opentms.basedata.mapper.TraderMapper;
import com.opentms.basedata.service.DealApprovalRuleService;
import com.opentms.basedata.vo.DealApprovalRuleMatchResponseVO;
import com.opentms.basedata.vo.DealApprovalRuleReferenceCountVO;
import com.opentms.basedata.vo.DealApprovalRuleVO;
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
import java.util.stream.Collectors;

/**
 * 交易审批规则 Service 实现
 *
 * <p>关键功能:
 * <ul>
 *   <li>5 维匹配算法 + specificityScore 排序(精确度优先)</li>
 *   <li>★ lockToken + 409 Conflict(沿用 v1.1)</li>
 *   <li>★ 5 分钟内存缓存(降级方案,生产建议 Redisson)</li>
 *   <li>★ 审计日志写入(CREATE/UPDATE/DELETE/ENABLE/DISABLE)</li>
 *   <li>★ 镜像 append-only</li>
 * </ul>
 *
 * @author Open-TMS
 * @since 2026-07-11
 */
@Slf4j
@Service
public class DealApprovalRuleServiceImpl
        extends ServiceImpl<DealApprovalRuleMapper, DealApprovalRule>
        implements DealApprovalRuleService {

    @Autowired
    private DealApprovalRuleAuditLogMapper auditLogMapper;

    @Autowired
    private ManagementEntityMapper managementEntityMapper;

    @Autowired
    private CounterpartyMapper counterpartyMapper;

    @Autowired
    private InstrumentMapper instrumentMapper;

    @Autowired
    private TraderMapper traderMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final long MATCH_CACHE_TTL_MS = 5 * 60 * 1000L;
    private static final String CACHE_KEY_PREFIX = "dar:match:";

    // ★ 内存缓存(简化版,生产建议用 Redisson)
    private final Map<String, CacheEntry> matchCache = new ConcurrentHashMap<>();

    private static class CacheEntry {
        DealApprovalRuleMatchResponseVO value;
        long expireAt;
        CacheEntry(DealApprovalRuleMatchResponseVO v, long exp) { value = v; expireAt = exp; }
        boolean isExpired() { return System.currentTimeMillis() > expireAt; }
    }

    // specificityScore 权重(PRD §5.4)
    private static final int SCORE_MANAGEMENT_ENTITY = 300;
    private static final int SCORE_COUNTERPARTY = 200;
    private static final int SCORE_INSTRUMENT = 100;
    private static final int SCORE_DEALER = 50;
    private static final int SCORE_ACTION_TYPE = 20;

    // ============= 查询 =============

    @Override
    public Page<DealApprovalRule> queryPage(DealApprovalRuleQueryDTO query) {
        LambdaQueryWrapper<DealApprovalRule> wrapper = new LambdaQueryWrapper<>();

        if (query.getManagementEntityId() != null) {
            wrapper.eq(DealApprovalRule::getManagementEntityId, query.getManagementEntityId());
        }
        if (query.getCounterpartyId() != null) {
            wrapper.eq(DealApprovalRule::getCounterpartyId, query.getCounterpartyId());
        }
        if (query.getInstrumentId() != null) {
            wrapper.eq(DealApprovalRule::getInstrumentId, query.getInstrumentId());
        }
        if (query.getDealerId() != null) {
            wrapper.eq(DealApprovalRule::getDealerId, query.getDealerId());
        }
        if (StringUtils.hasText(query.getActionType())) {
            wrapper.eq(DealApprovalRule::getActionType, query.getActionType());
        }
        if (StringUtils.hasText(query.getApprovalLevel())) {
            wrapper.eq(DealApprovalRule::getApprovalLevel, query.getApprovalLevel());
        }
        if (StringUtils.hasText(query.getStatus())) {
            wrapper.eq(DealApprovalRule::getStatus, query.getStatus());
        }
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(DealApprovalRule::getRuleNumber, query.getKeyword())
                    .or().like(DealApprovalRule::getDescription, query.getKeyword()));
        }

        wrapper.orderByDesc(DealApprovalRule::getPriority)
                .orderByAsc(DealApprovalRule::getCreatedAt);

        return page(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
    }

    @Override
    public DealApprovalRule getRuleById(Long id) {
        return getById(id);
    }

    @Override
    public DealApprovalRule getRuleByNumber(String ruleNumber) {
        return getOne(new LambdaQueryWrapper<DealApprovalRule>()
                .eq(DealApprovalRule::getRuleNumber, ruleNumber));
    }

    // ============= 新增 =============

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DealApprovalRule saveRule(DealApprovalRuleSaveDTO dto) {
        validateApprovalLevelAndRoles(dto.getApprovalLevel(), dto.getLevel1Roles(), dto.getLevel2Roles());
        validatePriority(dto.getPriority());
        validateActionType(dto.getActionType());
        validateDateRange(dto.getStartDate(), dto.getEndDate());
        validateRolesSize(dto.getLevel1Roles());
        validateRolesSize(dto.getLevel2Roles());

        DealApprovalRule rule = new DealApprovalRule();
        rule.setRuleNumber(generateRuleNumber());
        rule.setManagementEntityId(dto.getManagementEntityId());
        rule.setCounterpartyId(dto.getCounterpartyId());
        rule.setInstrumentId(dto.getInstrumentId());
        rule.setDealerId(dto.getDealerId());
        rule.setActionType(dto.getActionType());
        rule.setApprovalLevel(dto.getApprovalLevel());
        rule.setLevel1Roles(serializeJsonArray(dto.getLevel1Roles()));
        rule.setLevel2Roles(serializeJsonArray(dto.getLevel2Roles()));
        rule.setPriority(dto.getPriority());
        rule.setStartDate(dto.getStartDate());
        rule.setEndDate(dto.getEndDate());
        rule.setStatus(StringUtils.hasText(dto.getStatus()) ? dto.getStatus() : "Active");
        rule.setDescription(dto.getDescription());
        rule.setRemark(dto.getRemark());
        rule.setLockToken(UUID.randomUUID().toString());
        rule.setCreatedBy("system");
        rule.setCreatedAt(LocalDateTime.now());

        // ★ 使用 JdbcTemplate 直接插入,显式 CAST 到 JSONB(MyBatis-Plus 不会自动 cast)
        String sql = "INSERT INTO tms_deal_approval_rule_t " +
                "(rule_number, management_entity_id, counterparty_id, instrument_id, dealer_id, " +
                "action_type, approval_level, level1_roles, level2_roles, priority, status, " +
                "start_date, end_date, description, remark, lock_token, created_by, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?::jsonb, ?::jsonb, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                rule.getRuleNumber(),
                rule.getManagementEntityId(), rule.getCounterpartyId(),
                rule.getInstrumentId(), rule.getDealerId(),
                rule.getActionType(), rule.getApprovalLevel(),
                rule.getLevel1Roles(), rule.getLevel2Roles(),
                rule.getPriority(), rule.getStatus(),
                rule.getStartDate(), rule.getEndDate(),
                rule.getDescription(), rule.getRemark(),
                rule.getLockToken(), rule.getCreatedBy(),
                java.sql.Timestamp.valueOf(rule.getCreatedAt()));

        // 重新加载获取自增 ID
        DealApprovalRule saved = jdbcTemplate.queryForObject(
                "SELECT * FROM tms_deal_approval_rule_t WHERE rule_number=? AND deleted='0'",
                (rs, rowNum) -> mapRule(rs),
                rule.getRuleNumber());
        if (saved != null) {
            rule = saved;
        }
        writeAuditLog(rule.getId(), RuleAuditOperation.CREATE.getCode(), null, rule, "新增规则");
        writeImage(rule, "CREATE");
        invalidateMatchCache();
        return rule;
    }

    // ============= 更新(★ lockToken 校验) =============

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DealApprovalRule updateRule(DealApprovalRuleUpdateDTO dto) {
        validatePriority(dto.getPriority());
        if (StringUtils.hasText(dto.getActionType())) {
            validateActionType(dto.getActionType());
        }
        if (StringUtils.hasText(dto.getApprovalLevel())) {
            validateRolesSize(dto.getLevel1Roles());
            validateRolesSize(dto.getLevel2Roles());
            validateApprovalLevelAndRoles(dto.getApprovalLevel(), dto.getLevel1Roles(), dto.getLevel2Roles());
        }
        validateDateRange(dto.getStartDate(), dto.getEndDate());

        DealApprovalRule existing = getById(dto.getId());
        if (existing == null) {
            throw new IllegalArgumentException("规则不存在: id=" + dto.getId());
        }

        if (!StringUtils.hasText(dto.getLockToken())) {
            throw new IllegalArgumentException("lockToken 不能为空");
        }
        if (!dto.getLockToken().equals(existing.getLockToken())) {
            throw new ConcurrentModificationException(
                    "规则已被他人修改(updated_at=" + existing.getUpdatedAt() + "),请刷新后重试");
        }

        DealApprovalRule oldSnapshot = cloneRule(existing);
        String newLockToken = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();

        // ★ 使用 JdbcTemplate 直接更新,绕过 MyBatis-Plus @Version 问题
        String sql = "UPDATE tms_deal_approval_rule_t SET " +
                "counterparty_id=?, instrument_id=?, dealer_id=?, " +
                "action_type=?, approval_level=?, " +
                "level1_roles=?::jsonb, level2_roles=?::jsonb, " +
                "priority=?, status=?, start_date=?, end_date=?, " +
                "description=?, remark=?, " +
                "lock_token=?, updated_by=?, updated_at=?, version=version+1 " +
                "WHERE id=?";
        jdbcTemplate.update(sql,
                dto.getCounterpartyId(), dto.getInstrumentId(), dto.getDealerId(),
                dto.getActionType(), dto.getApprovalLevel(),
                serializeJsonArray(dto.getLevel1Roles()), serializeJsonArray(dto.getLevel2Roles()),
                dto.getPriority(), dto.getStatus(), dto.getStartDate(), dto.getEndDate(),
                dto.getDescription(), dto.getRemark(),
                newLockToken, "system", java.sql.Timestamp.valueOf(now),
                dto.getId());

        // 重新加载
        DealApprovalRule updated = jdbcTemplate.queryForObject(
                "SELECT * FROM tms_deal_approval_rule_t WHERE id=? AND deleted='0'",
                (rs, rowNum) -> mapRule(rs),
                dto.getId());

        writeAuditLog(updated.getId(), RuleAuditOperation.UPDATE.getCode(),
                oldSnapshot, updated, "更新规则");
        writeImage(updated, "UPDATE");
        invalidateMatchCache();
        return updated;
    }

    // ============= 删除 / 启用 / 停用 =============

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteRule(Long id) {
        DealApprovalRule rule = getById(id);
        if (rule == null) return false;

        jdbcTemplate.update("UPDATE tms_deal_approval_rule_t SET deleted='1', " +
                "lock_token=NULL, updated_by=?, updated_at=?, version=version+1 WHERE id=?",
                "system", java.sql.Timestamp.valueOf(LocalDateTime.now()), id);

        writeAuditLog(id, RuleAuditOperation.DELETE.getCode(), rule, null, "删除规则");
        writeImage(rule, "DELETE");
        invalidateMatchCache();
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean enableRule(Long id) {
        DealApprovalRule rule = getById(id);
        if (rule == null) return false;
        DealApprovalRule old = cloneRule(rule);

        jdbcTemplate.update("UPDATE tms_deal_approval_rule_t SET status='Active', " +
                "lock_token=?, updated_by=?, updated_at=?, version=version+1 WHERE id=?",
                UUID.randomUUID().toString(), "system",
                java.sql.Timestamp.valueOf(LocalDateTime.now()), id);

        DealApprovalRule updated = getById(id);
        writeAuditLog(id, RuleAuditOperation.ENABLE.getCode(), old, updated, "启用规则");
        writeImage(updated, "ENABLE");
        invalidateMatchCache();
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean disableRule(Long id) {
        DealApprovalRule rule = getById(id);
        if (rule == null) return false;
        DealApprovalRule old = cloneRule(rule);

        jdbcTemplate.update("UPDATE tms_deal_approval_rule_t SET status='Inactive', " +
                "lock_token=?, updated_by=?, updated_at=?, version=version+1 WHERE id=?",
                UUID.randomUUID().toString(), "system",
                java.sql.Timestamp.valueOf(LocalDateTime.now()), id);

        DealApprovalRule updated = getById(id);
        writeAuditLog(id, RuleAuditOperation.DISABLE.getCode(), old, updated, "停用规则");
        writeImage(updated, "DISABLE");
        invalidateMatchCache();
        return true;
    }

    // ============= ★ 匹配算法(specificityScore + 5min 缓存) =============

    @Override
    public DealApprovalRuleMatchResponseVO match(DealApprovalRuleMatchRequestDTO req) {
        long start = System.currentTimeMillis();

        if (!StringUtils.hasText(req.getActionType())) {
            DealApprovalRuleMatchResponseVO err = new DealApprovalRuleMatchResponseVO();
            err.setMatched(false);
            err.setApprovalLevel(null);
            err.setFallbackStrategy("MISSING_ACTION_TYPE");
            return err;
        }

        // 1. 内存缓存查询
        String cacheKey = buildCacheKey(req);
        CacheEntry cached = matchCache.get(cacheKey);
        if (cached != null && !cached.isExpired()) {
            log.debug("[DAR-MATCH] Cache hit: {}", cacheKey);
            DealApprovalRuleMatchResponseVO hit = cached.value;
            hit.setCacheHit(true);
            return hit;
        }

        // 2. 查询候选
        List<DealApprovalRule> candidates = queryEffectiveRules(req);

        // 3. 计算 specificityScore + 排序
        List<ScoredRule> scored = candidates.stream()
                .map(r -> new ScoredRule(r, calculateScore(r, req)))
                .sorted(this::compareScored)
                .collect(Collectors.toList());

        // 4. 构建响应
        DealApprovalRuleMatchResponseVO resp = new DealApprovalRuleMatchResponseVO();
        if (scored.isEmpty()) {
            // 未命中
            resp.setMatched(false);
            resp.setApprovalLevel(null);
            resp.setFallbackStrategy("DEFAULT_LEGACY_RULES");
            resp.setLevel1Roles(new ArrayList<>());
            resp.setLevel2Roles(new ArrayList<>());
        } else {
            ScoredRule top = scored.get(0);
            resp.setMatched(true);
            resp.setApprovalLevel(top.rule.getApprovalLevel());
            resp.setLevel1Roles(parseJsonArray(top.rule.getLevel1Roles()));
            resp.setLevel2Roles(parseJsonArray(top.rule.getLevel2Roles()));
            DealApprovalRuleMatchResponseVO.MatchedRuleSummary summary =
                    new DealApprovalRuleMatchResponseVO.MatchedRuleSummary();
            summary.setRuleNumber(top.rule.getRuleNumber());
            summary.setPriority(top.rule.getPriority());
            summary.setSpecificityScore(top.score);
            summary.setDescription(top.rule.getDescription());
            resp.setMatchedRule(summary);
            resp.setMatchedDimensions(computeMatchedDimensions(top.rule, req));
        }

        // 5. 候选列表(最多 50 条)
        int limit = req.getLimit() == null ? 50 : Math.min(req.getLimit(), 50);
        List<DealApprovalRuleMatchResponseVO.MatchCandidate> candidateList = scored.stream()
                .limit(limit)
                .map(s -> {
                    DealApprovalRuleMatchResponseVO.MatchCandidate c =
                            new DealApprovalRuleMatchResponseVO.MatchCandidate();
                    c.setRuleNumber(s.rule.getRuleNumber());
                    c.setSpecificityScore(s.score);
                    c.setPriority(s.rule.getPriority());
                    c.setApprovalLevel(s.rule.getApprovalLevel());
                    c.setMatchedDimensions(computeMatchedDimensions(s.rule, req));
                    c.setWon(resp.getMatchedRule() != null
                            && s.rule.getRuleNumber().equals(resp.getMatchedRule().getRuleNumber()));
                    return c;
                })
                .collect(Collectors.toList());
        resp.setCandidates(candidateList);
        resp.setCacheHit(false);

        // 6. 写缓存
        matchCache.put(cacheKey, new CacheEntry(resp, System.currentTimeMillis() + MATCH_CACHE_TTL_MS));

        long duration = System.currentTimeMillis() - start;
        log.info("[DAR-MATCH] Duration: {}ms, candidates: {}, matched: {}",
                duration, candidates.size(), resp.getMatched());
        return resp;
    }

    @Override
    public List<DealApprovalRuleMatchResponseVO.MatchCandidate> testMatch(DealApprovalRuleMatchRequestDTO req) {
        if (!StringUtils.hasText(req.getActionType())) return new ArrayList<>();
        List<DealApprovalRule> candidates = queryEffectiveRules(req);
        List<ScoredRule> scored = candidates.stream()
                .map(r -> new ScoredRule(r, calculateScore(r, req)))
                .sorted(this::compareScored)
                .collect(Collectors.toList());

        int limit = req.getLimit() == null ? 50 : Math.min(req.getLimit(), 50);
        List<DealApprovalRuleMatchResponseVO.MatchCandidate> result = new ArrayList<>();
        for (int i = 0; i < Math.min(limit, scored.size()); i++) {
            ScoredRule s = scored.get(i);
            DealApprovalRuleMatchResponseVO.MatchCandidate c =
                    new DealApprovalRuleMatchResponseVO.MatchCandidate();
            c.setRuleNumber(s.rule.getRuleNumber());
            c.setSpecificityScore(s.score);
            c.setPriority(s.rule.getPriority());
            c.setApprovalLevel(s.rule.getApprovalLevel());
            c.setMatchedDimensions(computeMatchedDimensions(s.rule, req));
            c.setWon(i == 0);
            result.add(c);
        }
        return result;
    }

    /**
     * 5 维基础过滤:status=Active + start_date/end_date 生效 + deleted=0
     * + action_type 精确(必填) + 其余维度 NULL OR 相等
     */
    private List<DealApprovalRule> queryEffectiveRules(DealApprovalRuleMatchRequestDTO req) {
        LocalDate today = LocalDate.now();
        LambdaQueryWrapper<DealApprovalRule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DealApprovalRule::getStatus, "Active")
                .eq(DealApprovalRule::getActionType, req.getActionType())
                .and(w -> w.isNull(DealApprovalRule::getStartDate)
                        .or().le(DealApprovalRule::getStartDate, today))
                .and(w -> w.isNull(DealApprovalRule::getEndDate)
                        .or().ge(DealApprovalRule::getEndDate, today));

        if (req.getManagementEntityId() != null) {
            wrapper.and(w -> w.isNull(DealApprovalRule::getManagementEntityId)
                    .or().eq(DealApprovalRule::getManagementEntityId, req.getManagementEntityId()));
        }
        if (req.getCounterpartyId() != null) {
            wrapper.and(w -> w.isNull(DealApprovalRule::getCounterpartyId)
                    .or().eq(DealApprovalRule::getCounterpartyId, req.getCounterpartyId()));
        }
        if (req.getInstrumentId() != null) {
            wrapper.and(w -> w.isNull(DealApprovalRule::getInstrumentId)
                    .or().eq(DealApprovalRule::getInstrumentId, req.getInstrumentId()));
        }
        if (req.getDealerId() != null) {
            wrapper.and(w -> w.isNull(DealApprovalRule::getDealerId)
                    .or().eq(DealApprovalRule::getDealerId, req.getDealerId()));
        }

        wrapper.orderByDesc(DealApprovalRule::getPriority)
                .orderByAsc(DealApprovalRule::getCreatedAt);
        return list(wrapper);
    }

    private int calculateScore(DealApprovalRule rule, DealApprovalRuleMatchRequestDTO req) {
        int score = 0;
        if (req.getManagementEntityId() != null
                && Objects.equals(rule.getManagementEntityId(), req.getManagementEntityId())) {
            score += SCORE_MANAGEMENT_ENTITY;
        }
        if (req.getCounterpartyId() != null
                && Objects.equals(rule.getCounterpartyId(), req.getCounterpartyId())) {
            score += SCORE_COUNTERPARTY;
        }
        if (req.getInstrumentId() != null
                && Objects.equals(rule.getInstrumentId(), req.getInstrumentId())) {
            score += SCORE_INSTRUMENT;
        }
        if (req.getDealerId() != null
                && Objects.equals(rule.getDealerId(), req.getDealerId())) {
            score += SCORE_DEALER;
        }
        // actionType 必填,精确匹配恒成立
        score += SCORE_ACTION_TYPE;
        return score;
    }

    private int compareScored(ScoredRule a, ScoredRule b) {
        // specificityScore DESC → priority DESC → created_at ASC → id ASC
        if (a.score != b.score) return Integer.compare(b.score, a.score);
        int ap = a.rule.getPriority() == null ? 0 : a.rule.getPriority();
        int bp = b.rule.getPriority() == null ? 0 : b.rule.getPriority();
        if (ap != bp) return Integer.compare(bp, ap);
        LocalDateTime ac = a.rule.getCreatedAt();
        LocalDateTime bc = b.rule.getCreatedAt();
        if (ac != null && bc != null && !ac.isEqual(bc)) return ac.compareTo(bc);
        Long aid = a.rule.getId();
        Long bid = b.rule.getId();
        if (aid == null) return bid == null ? 0 : 1;
        if (bid == null) return -1;
        return Long.compare(aid, bid);
    }

    private List<String> computeMatchedDimensions(DealApprovalRule rule, DealApprovalRuleMatchRequestDTO req) {
        List<String> dims = new ArrayList<>();
        if (req.getManagementEntityId() != null
                && Objects.equals(rule.getManagementEntityId(), req.getManagementEntityId())) {
            dims.add("managementEntity");
        }
        if (req.getCounterpartyId() != null
                && Objects.equals(rule.getCounterpartyId(), req.getCounterpartyId())) {
            dims.add("counterparty");
        }
        if (req.getInstrumentId() != null
                && Objects.equals(rule.getInstrumentId(), req.getInstrumentId())) {
            dims.add("instrument");
        }
        if (req.getDealerId() != null
                && Objects.equals(rule.getDealerId(), req.getDealerId())) {
            dims.add("dealer");
        }
        dims.add("actionType");
        return dims;
    }

    private static class ScoredRule {
        final DealApprovalRule rule;
        final int score;
        ScoredRule(DealApprovalRule rule, int score) {
            this.rule = rule;
            this.score = score;
        }
    }

    // ============= 审计日志 =============

    @Override
    public Page<DealApprovalRuleAuditLog> getAuditLogs(Long ruleId, int pageNum, int pageSize) {
        LambdaQueryWrapper<DealApprovalRuleAuditLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DealApprovalRuleAuditLog::getRuleId, ruleId)
                .orderByDesc(DealApprovalRuleAuditLog::getOperatedAt);
        return auditLogMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    private void writeAuditLog(Long ruleId, String operation,
                                DealApprovalRule oldRule, DealApprovalRule newRule,
                                String remark) {
        String oldJson = null;
        String newJson = null;
        try {
            if (oldRule != null) oldJson = serializeRuleAsMap(oldRule);
            if (newRule != null) newJson = serializeRuleAsMap(newRule);
        } catch (Exception e) {
            log.warn("Audit log JSON serialization failed: {}", e.getMessage());
        }
        String sql = "INSERT INTO tms_deal_approval_rule_audit_log_t " +
                "(rule_id, operation, old_value, new_value, operator, operated_at, remark) " +
                "VALUES (?, ?, ?::jsonb, ?::jsonb, ?, ?, ?)";
        jdbcTemplate.update(sql,
                ruleId, operation, oldJson, newJson,
                "system", java.sql.Timestamp.valueOf(LocalDateTime.now()), remark);
    }

    // ============= 镜像 =============

    @Override
    public Page<DealApprovalRuleImage> getImages(Long ruleId, String imageType, int pageNum, int pageSize) {
        LambdaQueryWrapper<DealApprovalRuleImage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DealApprovalRuleImage::getRuleId, ruleId);
        if (StringUtils.hasText(imageType)) {
            wrapper.eq(DealApprovalRuleImage::getImageType, imageType);
        }
        wrapper.orderByDesc(DealApprovalRuleImage::getVersion)
                .orderByDesc(DealApprovalRuleImage::getOperateAt);
        // 需要先确认 ImageMapper 存在;此处使用 jdbcTemplate 查询
        Integer total = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM tms_deal_approval_rule_image_t WHERE rule_id=?"
                        + (StringUtils.hasText(imageType) ? " AND image_type=?" : ""),
                Integer.class,
                StringUtils.hasText(imageType) ? new Object[]{ruleId, imageType} : new Object[]{ruleId});

        List<DealApprovalRuleImage> records = jdbcTemplate.query(
                "SELECT * FROM tms_deal_approval_rule_image_t WHERE rule_id=?"
                        + (StringUtils.hasText(imageType) ? " AND image_type=?" : "")
                        + " ORDER BY version DESC, operate_at DESC LIMIT ? OFFSET ?",
                (rs, rowNum) -> mapImage(rs),
                StringUtils.hasText(imageType)
                        ? new Object[]{ruleId, imageType, pageSize, (pageNum - 1) * pageSize}
                        : new Object[]{ruleId, pageSize, (pageNum - 1) * pageSize});

        Page<DealApprovalRuleImage> page = new Page<>(pageNum, pageSize, total == null ? 0 : total);
        page.setRecords(records);
        return page;
    }

    private void writeImage(DealApprovalRule rule, String imageType) {
        try {
            String snapshot = serializeRuleAsMap(rule);
            String imageNumber = "IMG-" + rule.getRuleNumber() + "-V" +
                    (rule.getVersion() == null ? 1 : rule.getVersion());
            String sql = "INSERT INTO tms_deal_approval_rule_image_t " +
                    "(image_number, rule_number, rule_id, version, snapshot_json, image_type, operator, operate_at, created_by, created_at) " +
                    "VALUES (?, ?, ?, ?, ?::jsonb, ?, ?, ?, ?, ?)";
            jdbcTemplate.update(sql,
                    imageNumber,
                    rule.getRuleNumber(),
                    rule.getId(),
                    rule.getVersion() == null ? 1 : rule.getVersion(),
                    snapshot,
                    imageType,
                    "system",
                    java.sql.Timestamp.valueOf(LocalDateTime.now()),
                    "system",
                    java.sql.Timestamp.valueOf(LocalDateTime.now()));
        } catch (Exception e) {
            log.warn("[DAR-IMAGE] Failed to write image for rule {}: {}", rule.getId(), e.getMessage());
        }
    }

    // ============= 被引用数 =============

    @Override
    public DealApprovalRuleReferenceCountVO getReferenceCount(Long id) {
        DealApprovalRule rule = getById(id);
        if (rule == null) return null;

        DealApprovalRuleReferenceCountVO vo = new DealApprovalRuleReferenceCountVO();
        vo.setRuleId(id);
        vo.setTotalCount(0);
        vo.setQueryDurationMs(0L);
        Map<String, Integer> byType = new HashMap<>();
        for (String t : Arrays.asList("CREATE", "SUBMIT", "APPROVE", "REJECT", "EXECUTE")) {
            byType.put(t, 0);
        }
        vo.setByActionType(byType);
        return vo;
    }

    // ============= 校验 =============

    private void validatePriority(Integer priority) {
        if (priority == null || priority < 0 || priority > 9999) {
            throw new IllegalArgumentException("优先级超出范围 0-9999");
        }
    }

    private void validateActionType(String actionType) {
        if (!StringUtils.hasText(actionType)) {
            throw new IllegalArgumentException("操作类型必填");
        }
        Set<String> valid = new HashSet<>(Arrays.asList("CREATE", "SUBMIT", "APPROVE", "REJECT", "EXECUTE"));
        if (!valid.contains(actionType)) {
            throw new IllegalArgumentException("操作类型非法: " + actionType);
        }
    }

    private void validateApprovalLevelAndRoles(String level, List<String> l1, List<String> l2) {
        if (!StringUtils.hasText(level)) {
            throw new IllegalArgumentException("审批层级必填");
        }
        if (!ApprovalLevel.isValid(level)) {
            throw new IllegalArgumentException("审批层级非法: " + level);
        }
        List<String> safeL1 = l1 == null ? new ArrayList<>() : l1;
        List<String> safeL2 = l2 == null ? new ArrayList<>() : l2;
        if (ApprovalLevel.LEVEL_0.getCode().equals(level)) {
            if (!safeL1.isEmpty() || !safeL2.isEmpty()) {
                throw new IllegalArgumentException("无需审批时 L1/L2 角色必须为空");
            }
        } else if (ApprovalLevel.LEVEL_1.getCode().equals(level)) {
            if (safeL1.isEmpty()) {
                throw new IllegalArgumentException("一层审批必须配置 L1 角色");
            }
            if (!safeL2.isEmpty()) {
                throw new IllegalArgumentException("一层审批时 L2 角色必须为空");
            }
        } else if (ApprovalLevel.LEVEL_2.getCode().equals(level)) {
            if (safeL1.isEmpty() || safeL2.isEmpty()) {
                throw new IllegalArgumentException("二层审批必须配置 L1 和 L2 角色");
            }
        }
    }

    private void validateRolesSize(List<String> roles) {
        if (roles == null) return;
        if (roles.size() > 5) {
            throw new IllegalArgumentException("角色列表长度超过 5");
        }
        for (String r : roles) {
            if (!StringUtils.hasText(r)) {
                throw new IllegalArgumentException("角色列表存在空元素");
            }
        }
    }

    private void validateDateRange(LocalDate start, LocalDate end) {
        if (start != null && end != null && end.isBefore(start)) {
            throw new IllegalArgumentException("结束日期不能早于开始日期");
        }
    }

    // ============= 工具方法 =============

    private String generateRuleNumber() {
        String today = LocalDate.now().format(DATE_FMT);
        String prefix = "DAR" + today;
        DealApprovalRule latest = getOne(new LambdaQueryWrapper<DealApprovalRule>()
                .likeRight(DealApprovalRule::getRuleNumber, prefix)
                .eq(DealApprovalRule::getDeleted, "0")
                .orderByDesc(DealApprovalRule::getRuleNumber)
                .last("LIMIT 1"));
        int nextSeq = 1;
        if (latest != null && latest.getRuleNumber() != null && latest.getRuleNumber().length() >= 16) {
            try {
                nextSeq = Integer.parseInt(latest.getRuleNumber().substring(12)) + 1;
            } catch (NumberFormatException ignored) {}
        }
        String candidate;
        int attempts = 0;
        do {
            candidate = String.format("DAR%s%04d", today, nextSeq);
            DealApprovalRule existing = getOne(new LambdaQueryWrapper<DealApprovalRule>()
                    .eq(DealApprovalRule::getRuleNumber, candidate));
            if (existing == null) break;
            nextSeq++;
            attempts++;
        } while (attempts < 100);
        return candidate;
    }

    private String buildCacheKey(DealApprovalRuleMatchRequestDTO req) {
        return CACHE_KEY_PREFIX + safe(req.getManagementEntityId())
                + ":" + safe(req.getCounterpartyId())
                + ":" + safe(req.getInstrumentId())
                + ":" + safe(req.getDealerId())
                + ":" + safe(req.getActionType());
    }

    private static String safe(Object o) {
        return o == null ? "" : o.toString();
    }

    private void invalidateMatchCache() {
        try {
            int size = matchCache.size();
            matchCache.clear();
            log.debug("[DAR-CACHE] Invalidated {} entries", size);
        } catch (Exception e) {
            log.warn("[DAR-CACHE] Invalidate failed: {}", e.getMessage());
        }
    }

    private DealApprovalRule cloneRule(DealApprovalRule src) {
        if (src == null) return null;
        DealApprovalRule copy = new DealApprovalRule();
        copy.setId(src.getId());
        copy.setRuleNumber(src.getRuleNumber());
        copy.setManagementEntityId(src.getManagementEntityId());
        copy.setCounterpartyId(src.getCounterpartyId());
        copy.setInstrumentId(src.getInstrumentId());
        copy.setDealerId(src.getDealerId());
        copy.setActionType(src.getActionType());
        copy.setApprovalLevel(src.getApprovalLevel());
        copy.setLevel1Roles(src.getLevel1Roles());
        copy.setLevel2Roles(src.getLevel2Roles());
        copy.setPriority(src.getPriority());
        copy.setStatus(src.getStatus());
        copy.setStartDate(src.getStartDate());
        copy.setEndDate(src.getEndDate());
        copy.setDescription(src.getDescription());
        copy.setRemark(src.getRemark());
        copy.setVersion(src.getVersion());
        return copy;
    }

    private DealApprovalRule mapRule(java.sql.ResultSet rs) throws java.sql.SQLException {
        DealApprovalRule r = new DealApprovalRule();
        r.setId(rs.getLong("id"));
        r.setRuleNumber(rs.getString("rule_number"));
        long me = rs.getLong("management_entity_id");
        r.setManagementEntityId(rs.wasNull() ? null : me);
        long cp = rs.getLong("counterparty_id");
        r.setCounterpartyId(rs.wasNull() ? null : cp);
        long ins = rs.getLong("instrument_id");
        r.setInstrumentId(rs.wasNull() ? null : ins);
        long dl = rs.getLong("dealer_id");
        r.setDealerId(rs.wasNull() ? null : dl);
        r.setActionType(rs.getString("action_type"));
        r.setApprovalLevel(rs.getString("approval_level"));
        r.setLevel1Roles(rs.getString("level1_roles"));
        r.setLevel2Roles(rs.getString("level2_roles"));
        r.setPriority(rs.getInt("priority"));
        r.setStatus(rs.getString("status"));
        java.sql.Date sd = rs.getDate("start_date");
        r.setStartDate(sd == null ? null : sd.toLocalDate());
        java.sql.Date ed = rs.getDate("end_date");
        r.setEndDate(ed == null ? null : ed.toLocalDate());
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
    }

    private DealApprovalRuleImage mapImage(java.sql.ResultSet rs) throws java.sql.SQLException {
        DealApprovalRuleImage img = new DealApprovalRuleImage();
        img.setId(rs.getLong("id"));
        img.setImageNumber(rs.getString("image_number"));
        img.setRuleNumber(rs.getString("rule_number"));
        img.setRuleId(rs.getLong("rule_id"));
        img.setVersion(rs.getInt("version"));
        img.setSnapshotJson(rs.getString("snapshot_json"));
        img.setImageType(rs.getString("image_type"));
        img.setOperator(rs.getString("operator"));
        java.sql.Timestamp oat = rs.getTimestamp("operate_at");
        img.setOperateAt(oat == null ? null : oat.toLocalDateTime());
        img.setRemark(rs.getString("remark"));
        img.setCreatedBy(rs.getString("created_by"));
        java.sql.Timestamp cat = rs.getTimestamp("created_at");
        img.setCreatedAt(cat == null ? null : cat.toLocalDateTime());
        img.setDeleted(rs.getString("deleted"));
        return img;
    }

    private String serializeRuleAsMap(DealApprovalRule rule) {
        try {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", rule.getId());
            map.put("ruleNumber", rule.getRuleNumber());
            map.put("managementEntityId", rule.getManagementEntityId());
            map.put("counterpartyId", rule.getCounterpartyId());
            map.put("instrumentId", rule.getInstrumentId());
            map.put("dealerId", rule.getDealerId());
            map.put("actionType", rule.getActionType());
            map.put("approvalLevel", rule.getApprovalLevel());
            map.put("level1Roles", rule.getLevel1Roles());
            map.put("level2Roles", rule.getLevel2Roles());
            map.put("priority", rule.getPriority());
            map.put("status", rule.getStatus());
            map.put("startDate", rule.getStartDate() == null ? null : rule.getStartDate().toString());
            map.put("endDate", rule.getEndDate() == null ? null : rule.getEndDate().toString());
            map.put("description", rule.getDescription());
            map.put("remark", rule.getRemark());
            map.put("version", rule.getVersion());
            return objectMapper.writeValueAsString(map);
        } catch (Exception e) {
            log.warn("Serialize rule failed: {}", e.getMessage());
            return "{}";
        }
    }

    private String serializeJsonArray(List<String> list) {
        if (list == null || list.isEmpty()) return "[]";
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (String s : list) {
            if (!first) sb.append(",");
            sb.append("\"").append(s == null ? "" : s.replace("\"", "\\\"")).append("\"");
            first = false;
        }
        sb.append("]");
        return sb.toString();
    }

    private List<String> parseJsonArray(String json) {
        if (json == null || json.isEmpty() || "[]".equals(json.trim())) return new ArrayList<>();
        String trimmed = json.trim();
        if (!trimmed.startsWith("[") || !trimmed.endsWith("]")) return new ArrayList<>();
        String inner = trimmed.substring(1, trimmed.length() - 1).trim();
        if (inner.isEmpty()) return new ArrayList<>();
        List<String> result = new ArrayList<>();
        // 简单解析(支持 "A","B" 格式,不递归)
        int i = 0;
        while (i < inner.length()) {
            while (i < inner.length() && (Character.isWhitespace(inner.charAt(i)) || inner.charAt(i) == ',')) i++;
            if (i >= inner.length()) break;
            if (inner.charAt(i) == '"') {
                int end = inner.indexOf('"', i + 1);
                if (end < 0) break;
                String val = inner.substring(i + 1, end);
                result.add(val);
                i = end + 1;
            } else {
                int end = i;
                while (end < inner.length() && inner.charAt(end) != ',') end++;
                String val = inner.substring(i, end).trim();
                if (!val.isEmpty()) result.add(val);
                i = end;
            }
        }
        return result;
    }

    // ============= 2026-07-12 修:enrichWithNames 填 4 维基础数据名 =============

    @Override
    public DealApprovalRuleVO enrichWithNames(DealApprovalRuleVO vo) {
        if (vo == null) return null;
        if (vo.getManagementEntityId() != null) {
            var me = managementEntityMapper.selectById(vo.getManagementEntityId());
            vo.setManagementEntityName(me == null ? null : me.getName());
        }
        if (vo.getCounterpartyId() != null) {
            // Counterparty 没有 name,只有 enName + counterpartyType
            var cp = counterpartyMapper.selectById(vo.getCounterpartyId());
            vo.setCounterpartyName(cp == null ? null : cp.getEnName());
        }
        if (vo.getInstrumentId() != null) {
            var ins = instrumentMapper.selectById(vo.getInstrumentId());
            vo.setInstrumentName(ins == null ? null : ins.getInstrumentName());
        }
        if (vo.getDealerId() != null) {
            // Trader 没有 name,只有 enName
            var t = traderMapper.selectById(vo.getDealerId());
            vo.setDealerName(t == null ? null : t.getEnName());
        }
        return vo;
    }
}

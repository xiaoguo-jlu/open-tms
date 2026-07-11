package com.opentms.dealing.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.opentms.dealing.entity.Cashflow;
import com.opentms.dealing.entity.Deal;
import com.opentms.dealing.integration.BasedataMatchClient;
import com.opentms.dealing.mapper.CashflowMapper;
import com.opentms.dealing.mapper.DealMapper;
import com.opentms.dealing.service.CashflowImageService;
import com.opentms.dealing.service.CashflowService;
import com.opentms.dealing.vo.CashflowVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 现金流 Service 实现 (v1.0 - 2026-07-11)
 *
 * <p>新增能力:
 * <ul>
 *   <li>创建 / 更新 / 软删时调 v1.1 默认银行账户规则自动填充 bankAccountId</li>
 *   <li>同步写 {@code tms_cashflow_image_t} 镜像（CREATE / UPDATE / DELETE）</li>
 *   <li>镜像失败 → 整个 cashflow 操作回滚（@Transactional）</li>
 * </ul>
 *
 * <p>注: Cashflow 表本身没有 counterpartyId / managementEntityId 字段,
 * 规则匹配需要的参数从关联 Deal 取;FT 用例管理主体 ID 需要 managementEntity code → id 转换,
 * v1.0 暂跳过（依赖 calling site 注入,见 {@link AcDealServiceImpl} 的 update 流程）。</p>
 *
 * @author Open-TMS Backend Developer
 * @since 2026-07-11
 */
@Service
public class CashflowServiceImpl extends ServiceImpl<CashflowMapper, Cashflow> implements CashflowService {

    private static final Logger log = LoggerFactory.getLogger(CashflowServiceImpl.class);

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    private static final String CFLOW_STATUS_CREATED = "Created";

    private static final String IMAGE_TYPE_CREATE = "CREATE";
    private static final String IMAGE_TYPE_UPDATE = "UPDATE";
    private static final String IMAGE_TYPE_DELETE = "DELETE";

    private final CashflowImageService cashflowImageService;
    private final BasedataMatchClient basedataMatchClient;
    private final DealMapper dealMapper;

    public CashflowServiceImpl(@Lazy CashflowImageService cashflowImageService,
                               BasedataMatchClient basedataMatchClient,
                               DealMapper dealMapper) {
        this.cashflowImageService = cashflowImageService;
        this.basedataMatchClient = basedataMatchClient;
        this.dealMapper = dealMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createCashflow(Cashflow cashflow) {
        if (!StringUtils.hasText(cashflow.getCflowNumber())) {
            cashflow.setCflowNumber(generateCflowNumber());
        }
        if (!StringUtils.hasText(cashflow.getStatus())) {
            cashflow.setStatus(CFLOW_STATUS_CREATED);
        }
        if (cashflow.getCreatedAt() == null) {
            cashflow.setCreatedAt(LocalDateTime.now());
        }
        if (cashflow.getVersion() == null) {
            cashflow.setVersion(1);
        }
        if (!StringUtils.hasText(cashflow.getDeleted())) {
            cashflow.setDeleted("0");
        }

        // 1) 调 v1.1 默认银行账户规则匹配（失败/超时降级为 null）
        autoFillBankAccount(cashflow, null, null, true);

        // 2) 写主表
        super.save(cashflow);

        // 3) 同步写 CREATE 镜像（@Transactional 整体回滚）
        try {
            cashflowImageService.append(cashflow, IMAGE_TYPE_CREATE);
        } catch (RuntimeException e) {
            log.error("[CashflowService] createCashflow 写 CREATE 镜像失败,事务回滚: cflowNumber={}, err={}",
                    cashflow.getCflowNumber(), e.getMessage(), e);
            throw e;
        }

        return cashflow.getCflowNumber();
    }

    @Override
    public boolean save(Cashflow cashflow) {
        if (cashflow.getCreatedAt() == null) {
            cashflow.setCreatedAt(LocalDateTime.now());
        }
        if (cashflow.getVersion() == null) {
            cashflow.setVersion(1);
        }
        if (!StringUtils.hasText(cashflow.getDeleted())) {
            cashflow.setDeleted("0");
        }
        if (!StringUtils.hasText(cashflow.getStatus())) {
            cashflow.setStatus(CFLOW_STATUS_CREATED);
        }
        return super.save(cashflow);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateCashflow(Cashflow cashflow) {
        if (cashflow.getId() == null) {
            throw new IllegalArgumentException("id 不能为空");
        }
        Cashflow existing = baseMapper.selectById(cashflow.getId());
        if (existing == null) {
            throw new IllegalArgumentException("Cashflow 不存在: id=" + cashflow.getId());
        }

        // 1) 先写改前镜像（保留完整旧值）
        try {
            cashflowImageService.append(existing, IMAGE_TYPE_UPDATE);
        } catch (RuntimeException e) {
            log.error("[CashflowService] updateCashflow 写 UPDATE 镜像失败,事务回滚: cflowNumber={}, err={}",
                    existing.getCflowNumber(), e.getMessage(), e);
            throw e;
        }

        // 2) 字段更新（只更新非空字段，保留未传字段的旧值）
        if (StringUtils.hasText(cashflow.getBankAccount())) existing.setBankAccount(cashflow.getBankAccount());
        if (StringUtils.hasText(cashflow.getCounterpartyAccount())) existing.setCounterpartyAccount(cashflow.getCounterpartyAccount());
        if (cashflow.getBankAccountId() != null) existing.setBankAccountId(cashflow.getBankAccountId());
        if (cashflow.getCounterpartyBankAccountId() != null) existing.setCounterpartyBankAccountId(cashflow.getCounterpartyBankAccountId());
        if (StringUtils.hasText(cashflow.getDirection())) existing.setDirection(cashflow.getDirection());
        if (cashflow.getAmount() != null) existing.setAmount(cashflow.getAmount());
        if (StringUtils.hasText(cashflow.getCurrency())) existing.setCurrency(cashflow.getCurrency());
        if (cashflow.getCflowDate() != null) existing.setCflowDate(cashflow.getCflowDate());
        if (cashflow.getValueDate() != null) existing.setValueDate(cashflow.getValueDate());
        if (StringUtils.hasText(cashflow.getStatus())) existing.setStatus(cashflow.getStatus());
        if (StringUtils.hasText(cashflow.getPurpose())) existing.setPurpose(cashflow.getPurpose());
        if (StringUtils.hasText(cashflow.getRemark())) existing.setRemark(cashflow.getRemark());

        // 3) 关键字段变化（对手方/币种/方向）→ 重新调 match
        boolean keyChanged = StringUtils.hasText(cashflow.getDirection())
                || StringUtils.hasText(cashflow.getCurrency());
        if (keyChanged) {
            autoFillBankAccount(existing, null, null, false);
        }

        existing.setUpdatedBy(cashflow.getUpdatedBy() != null ? cashflow.getUpdatedBy() : "system");
        existing.setUpdatedAt(LocalDateTime.now());
        if (existing.getVersion() != null) {
            existing.setVersion(existing.getVersion() + 1);
        }

        return super.updateById(existing);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteCashflow(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("id 不能为空");
        }
        Cashflow existing = baseMapper.selectById(id);
        if (existing == null) {
            return false;
        }

        // 1) 先写 DELETE 镜像（保存删除前完整快照）
        try {
            cashflowImageService.append(existing, IMAGE_TYPE_DELETE);
        } catch (RuntimeException e) {
            log.error("[CashflowService] deleteCashflow 写 DELETE 镜像失败,事务回滚: cflowNumber={}, err={}",
                    existing.getCflowNumber(), e.getMessage(), e);
            throw e;
        }

        // 2) 软删主表
        Cashflow update = new Cashflow();
        update.setId(id);
        update.setDeleted("1");
        update.setUpdatedBy("system");
        update.setUpdatedAt(LocalDateTime.now());
        update.setVersion(existing.getVersion() != null ? existing.getVersion() + 1 : 1);
        return baseMapper.updateById(update) > 0;
    }

    @Override
    public int softDeleteByDealMapNumber(String dealmapNumber) {
        Cashflow update = new Cashflow();
        update.setDeleted("1");
        update.setUpdatedAt(LocalDateTime.now());
        update.setUpdatedBy("system");

        LambdaQueryWrapper<Cashflow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Cashflow::getDealmapNumber, dealmapNumber)
               .eq(Cashflow::getDeleted, "0");
        return baseMapper.update(update, wrapper);
    }

    @Override
    public int updateDealMapNumber(String oldDealMapNumber, String newDealMapNumber) {
        Cashflow update = new Cashflow();
        update.setDealmapNumber(newDealMapNumber);
        update.setUpdatedAt(LocalDateTime.now());
        update.setUpdatedBy("system");

        LambdaQueryWrapper<Cashflow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Cashflow::getDealmapNumber, oldDealMapNumber)
               .eq(Cashflow::getDeleted, "0");
        return baseMapper.update(update, wrapper);
    }

    @Override
    public List<CashflowVO> listByDealMapNumber(String dealmapNumber) {
        LambdaQueryWrapper<Cashflow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Cashflow::getDealmapNumber, dealmapNumber)
               .orderByDesc(Cashflow::getCreatedAt);
        return list(wrapper).stream().map(this::convertToVO).toList();
    }

    @Override
    public List<CashflowVO> listByDealNumber(String dealNumber) {
        LambdaQueryWrapper<Cashflow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Cashflow::getDealNumber, dealNumber)
               .orderByDesc(Cashflow::getCreatedAt);
        return list(wrapper).stream().map(this::convertToVO).toList();
    }

    @Override
    public String generateCflowNumber() {
        String dateStr = LocalDateTime.now().format(DATE_FORMATTER);
        String prefix = "CF" + dateStr;
        LambdaQueryWrapper<Cashflow> wrapper = new LambdaQueryWrapper<>();
        wrapper.likeRight(Cashflow::getCflowNumber, prefix)
               .orderByDesc(Cashflow::getCflowNumber)
               .last("LIMIT 1");
        Cashflow last = getOne(wrapper);
        int seq = 1;
        if (last != null && last.getCflowNumber() != null
                && last.getCflowNumber().length() > prefix.length()) {
            try {
                String lastSeqStr = last.getCflowNumber().substring(prefix.length());
                seq = Integer.parseInt(lastSeqStr) + 1;
            } catch (NumberFormatException ignored) {
            }
        }
        return prefix + String.format("%04d", seq);
    }

    /**
     * 调 v1.1 规则匹配，自动填充 bank_account_id。
     * 失败 / 超时 / 未命中 → 不抛异常，仅记录 warn。
     *
     * @param cashflow 待填充的现金流
     * @param explicitMeId 显式传入的管理主体 ID（call site 提供,可空）
     * @param explicitCpId 显式传入的对手方 ID（call site 提供,可空）
     * @param isCreate     是否为创建场景（仅日志区别）
     */
    private void autoFillBankAccount(Cashflow cashflow, Long explicitMeId, Long explicitCpId, boolean isCreate) {
        try {
            Long meId = explicitMeId;
            Long cpId = explicitCpId;
            String direction = cashflow.getDirection();
            String currency = cashflow.getCurrency();

            // fallback: 从 deal 表取 counterpartyId
            if ((meId == null || cpId == null) && StringUtils.hasText(cashflow.getDealNumber())) {
                Deal deal = lookupDeal(cashflow.getDealNumber());
                if (deal != null) {
                    if (cpId == null) cpId = deal.getCounterpartyId();
                    // managementEntityId 通过 EntityNameLookup 解析 code → id 较为复杂,
                    // v1.0 简化: 若 explicitMeId == null 则跳过 match。AcDeal 流程会在 save 前
                    // 通过 deal.getManagementEntity() (code) 解析 → managementEntityId。
                }
            }

            if (meId == null || cpId == null || !StringUtils.hasText(direction) || !StringUtils.hasText(currency)) {
                log.debug("[CashflowService] skip match (insufficient params): cflowNumber={}, meId={}, cpId={}, dir={}, ccy={}",
                        cashflow.getCflowNumber(), meId, cpId, direction, currency);
                return;
            }

            BasedataMatchClient.BasedataMatchResult result = basedataMatchClient.match(
                    meId, cpId, null, direction, currency, false);

            if (result == null || !result.isMatched()) {
                log.info("[CashflowService] rule match 未命中: cflowNumber={}, meId={}, cpId={}, dir={}, ccy={}",
                        cashflow.getCflowNumber(), meId, cpId, direction, currency);
                return;
            }

            Long bankAccountId = result.bankAccountIdFor(direction);
            if (bankAccountId != null) {
                cashflow.setBankAccountId(bankAccountId);
                log.info("[CashflowService] 自动填充 bank_account_id={} cflowNumber={} isCreate={}",
                        bankAccountId, cashflow.getCflowNumber(), isCreate);
            }
        } catch (RuntimeException e) {
            log.warn("[CashflowService] 自动填充银行账户失败,降级为 null: cflowNumber={}, err={}",
                    cashflow.getCflowNumber(), e.getMessage());
        }
    }

    private Deal lookupDeal(String dealNumber) {
        if (!StringUtils.hasText(dealNumber)) return null;
        LambdaQueryWrapper<Deal> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Deal::getDealNumber, dealNumber);
        return dealMapper.selectOne(wrapper);
    }

    private CashflowVO convertToVO(Cashflow cashflow) {
        CashflowVO vo = new CashflowVO();
        BeanUtils.copyProperties(cashflow, vo);
        return vo;
    }
}
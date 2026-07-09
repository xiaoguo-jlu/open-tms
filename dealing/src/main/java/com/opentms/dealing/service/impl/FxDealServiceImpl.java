package com.opentms.dealing.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.opentms.dealing.dto.FxCalculateRequest;
import com.opentms.dealing.dto.FxCalculateResponse;
import com.opentms.dealing.dto.FxDealDTO;
import com.opentms.dealing.dto.RateFixRequest;
import com.opentms.dealing.entity.Action;
import com.opentms.dealing.entity.Cashflow;
import com.opentms.dealing.entity.Deal;
import com.opentms.dealing.entity.DealMap;
import com.opentms.dealing.entity.FxDeal;
import com.opentms.dealing.mapper.ActionMapper;
import com.opentms.dealing.mapper.CashflowMapper;
import com.opentms.dealing.mapper.DealMapMapper;
import com.opentms.dealing.mapper.DealMapper;
import com.opentms.dealing.mapper.FxDealMapper;
import com.opentms.dealing.service.EntityNameLookup;
import com.opentms.dealing.service.FxDealService;
import com.opentms.dealing.vo.ActionVO;
import com.opentms.dealing.vo.CashflowVO;
import com.opentms.dealing.vo.DealMapVO;
import com.opentms.dealing.vo.FxDealDetailVO;
import com.opentms.dealing.vo.FxDealVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * FX 交易 Service 实现（v3.2 - 后端 calculate + 共享主键 + 4 Action）
 *
 * 核心规则：
 * 1) DEAL:    Deal + FxDeal(共享主键) + Action(DEAL) + 3 DealMap(BUY/SELL/RATE) + 0/2 Cashflow
 *             - SPOT/FWD 立即生成 2 Cashflow
 *             - NDF 不生成 Cashflow，等 RATE_FIX
 * 2) UPDATE:  Action(UPDATE) + UPDATE Deal/FxDeal（不重新生成 DealMap/Cashflow）
 * 3) DELETE:  Action(DELETE) + 软删 Deal/FxDeal + 级联软删 DealMap/Cashflow
 * 4) RATE_FIX (NDF only): Action(RATE_FIX) + 1 DealMap(FX_FIX) + 1 Cashflow(差额) + UPDATE fixing_rate/settlement_amount
 */
@Service
public class FxDealServiceImpl implements FxDealService {

    private static final Logger log = LoggerFactory.getLogger(FxDealServiceImpl.class);

    private final DealMapper dealMapper;
    private final FxDealMapper fxDealMapper;
    private final ActionMapper actionMapper;
    private final DealMapMapper dealMapMapper;
    private final CashflowMapper cashflowMapper;
    /**
     * 跨模块关联实体名称查询 (用于 copy 端点补全名称字段, 2026-07-05)
     */
    private final EntityNameLookup entityNameLookup;

    // ====== 常量 ======
    private static final String DEAL_TYPE = "FX";
    private static final String DEAL_STATUS_NEW = "New";
    private static final String DEAL_STATUS_ACTIVE = "Active";
    private static final String DEAL_STATUS_CANCELED = "Canceled";

    private static final String ACTION_TYPE_DEAL = "DEAL";
    private static final String ACTION_TYPE_UPDATE = "UPDATE";
    private static final String ACTION_TYPE_DELETE = "DELETE";
    private static final String ACTION_TYPE_RATE_FIX = "RATE_FIX";

    private static final String ACTION_STATUS_APPROVED = "Approved";
    private static final String APPROVAL_STATUS_APPROVED = "Approved";
    private static final String APPROVAL_STATUS_REJECTED = "Rejected";

    private static final String DEALMAP_TYPE_FX_BUY_AMOUNT = "FX_BUY_AMOUNT";
    private static final String DEALMAP_TYPE_FX_SELL_AMOUNT = "FX_SELL_AMOUNT";
    private static final String DEALMAP_TYPE_FX_RATE = "FX_RATE";
    private static final String DEALMAP_TYPE_FX_FIX = "FX_FIX";

    private static final String DIRECTION_OUTFLOW = "Outflow";
    private static final String DIRECTION_INFLOW = "Inflow";

    private static final String CFLOW_STATUS_CREATED = "Created";
    private static final String CFLOW_SOURCE_TYPE = "FX_DEAL";

    private static final String EVENT_STATUS_ACTIVE = "Active";
    private static final String EVENT_TYPE_FX = "FX";

    private static final String NOT_DELETED = "0";
    private static final String DELETED = "1";

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    // 8 位小数（汇率） / 18 位小数（金额）
    private static final int RATE_SCALE = 8;
    private static final int AMOUNT_SCALE = 18;
    // 计算允许的浮点误差
    private static final BigDecimal EPSILON = new BigDecimal("0.00000001");

    public FxDealServiceImpl(DealMapper dealMapper,
                             FxDealMapper fxDealMapper,
                             ActionMapper actionMapper,
                             DealMapMapper dealMapMapper,
                             CashflowMapper cashflowMapper,
                             EntityNameLookup entityNameLookup) {
        this.dealMapper = dealMapper;
        this.fxDealMapper = fxDealMapper;
        this.actionMapper = actionMapper;
        this.dealMapMapper = dealMapMapper;
        this.cashflowMapper = cashflowMapper;
        this.entityNameLookup = entityNameLookup;
    }

    // ======================== Calculate ========================

    @Override
    public FxCalculateResponse calculate(FxCalculateRequest req) {
        FxCalculateResponse resp = new FxCalculateResponse();
        // 拷贝原始值
        resp.setSellAmount(req.getSellAmount());
        resp.setBuyAmount(req.getBuyAmount());
        resp.setExchangeRate(req.getExchangeRate());
        resp.setMarketRate(req.getMarketRate());
        resp.setSpreadBp(req.getSpreadBp());
        resp.setTradeDate(req.getTradeDate());
        resp.setValueDate(req.getValueDate());

        // 校验：至少 2 个金额/汇率字段
        int filled = countFilledAmountRate(req);
        if (filled < 2) {
            throw new IllegalArgumentException("至少需要填 2 个金额/汇率字段(INPUT_INSUFFICIENT)");
        }

        // 校验日期：tradeDate <= valueDate
        if (resp.getTradeDate() != null && resp.getValueDate() != null
                && resp.getValueDate().isBefore(resp.getTradeDate())) {
            throw new IllegalArgumentException("交割日不能早于交易日(DATE_INVALID)");
        }

        // 联动 1：buyAmount = sellAmount × exchangeRate
        if (resp.getBuyAmount() == null && resp.getSellAmount() != null && resp.getExchangeRate() != null) {
            resp.setBuyAmount(resp.getSellAmount().multiply(resp.getExchangeRate()).setScale(AMOUNT_SCALE, RoundingMode.HALF_UP));
        } else if (resp.getSellAmount() == null && resp.getBuyAmount() != null && resp.getExchangeRate() != null
                && resp.getExchangeRate().compareTo(BigDecimal.ZERO) != 0) {
            resp.setSellAmount(resp.getBuyAmount().divide(resp.getExchangeRate(), AMOUNT_SCALE, RoundingMode.HALF_UP));
        } else if (resp.getExchangeRate() == null && resp.getSellAmount() != null && resp.getBuyAmount() != null
                && resp.getSellAmount().compareTo(BigDecimal.ZERO) != 0) {
            resp.setExchangeRate(resp.getBuyAmount().divide(resp.getSellAmount(), RATE_SCALE, RoundingMode.HALF_UP));
        }

        // 联动 2：exchangeRate = marketRate + spreadBp / 10000
        if (resp.getExchangeRate() == null && resp.getMarketRate() != null && resp.getSpreadBp() != null) {
            resp.setExchangeRate(resp.getMarketRate()
                    .add(resp.getSpreadBp().divide(new BigDecimal("10000"), RATE_SCALE, RoundingMode.HALF_UP))
                    .setScale(RATE_SCALE, RoundingMode.HALF_UP));
        } else if (resp.getMarketRate() == null && resp.getExchangeRate() != null && resp.getSpreadBp() != null) {
            resp.setMarketRate(resp.getExchangeRate()
                    .subtract(resp.getSpreadBp().divide(new BigDecimal("10000"), RATE_SCALE, RoundingMode.HALF_UP))
                    .setScale(RATE_SCALE, RoundingMode.HALF_UP));
        } else if (resp.getSpreadBp() == null && resp.getExchangeRate() != null && resp.getMarketRate() != null) {
            resp.setSpreadBp(resp.getExchangeRate().subtract(resp.getMarketRate())
                    .multiply(new BigDecimal("10000"))
                    .setScale(4, RoundingMode.HALF_UP));
        }

        // 联动 3：termDays = valueDate - tradeDate
        if (resp.getTradeDate() != null && resp.getValueDate() != null) {
            long days = ChronoUnit.DAYS.between(resp.getTradeDate(), resp.getValueDate());
            resp.setTermDays((int) days);
        }

        // 联动 4：maturityDate = valueDate
        if (resp.getValueDate() != null) {
            resp.setMaturityDate(resp.getValueDate());
        }

        // 一致性校验（仅当所有 3 个都已填时校验）
        if (resp.getSellAmount() != null && resp.getBuyAmount() != null && resp.getExchangeRate() != null
                && resp.getExchangeRate().compareTo(BigDecimal.ZERO) != 0) {
            BigDecimal expectedBuy = resp.getSellAmount().multiply(resp.getExchangeRate());
            BigDecimal diff = expectedBuy.subtract(resp.getBuyAmount()).abs();
            if (diff.compareTo(EPSILON.multiply(resp.getSellAmount().abs().max(BigDecimal.ONE))) > 0) {
                throw new IllegalArgumentException(
                        "字段不一致: sellAmount × exchangeRate ≠ buyAmount (VALUE_INCONSISTENT)");
            }
        }

        return resp;
    }

    private int countFilledAmountRate(FxCalculateRequest req) {
        int n = 0;
        if (req.getSellAmount() != null) n++;
        if (req.getBuyAmount() != null) n++;
        if (req.getExchangeRate() != null) n++;
        if (req.getMarketRate() != null) n++;
        if (req.getSpreadBp() != null) n++;
        return n;
    }

    // ======================== Page / Query ========================

    @Override
    public Page<FxDealVO> queryPage(Long managementEntityId, Long counterpartyId,
                                    String productType, String status,
                                    LocalDate startDate, LocalDate endDate,
                                    int pageNum, int pageSize) {
        LambdaQueryWrapper<Deal> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Deal::getDealType, DEAL_TYPE);

        if (StringUtils.hasText(status)) {
            wrapper.eq(Deal::getStatus, status);
        }
        if (counterpartyId != null) {
            wrapper.eq(Deal::getCounterpartyId, counterpartyId);
        }
        if (startDate != null) {
            wrapper.ge(Deal::getDealDate, startDate);
        }
        if (endDate != null) {
            wrapper.le(Deal::getDealDate, endDate);
        }
        // 管理主体和 productType 在 fx_deals 中,先过滤 deal 集合
        wrapper.orderByDesc(Deal::getCreatedAt);

        Page<Deal> page = dealMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        Page<FxDealVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());

        List<FxDealVO> voList = new ArrayList<>();
        for (Deal deal : page.getRecords()) {
            FxDealVO vo = convertDealToListVO(deal);
            // 管理主体过滤
            if (managementEntityId != null && vo.getManagementEntityId() != null
                    && !managementEntityId.equals(vo.getManagementEntityId())) {
                continue;
            }
            // 产品类型过滤（占位：FX 通用 productType 由 instrument 决定，本期实现用简化判断）
            if (StringUtils.hasText(productType) && !productType.equalsIgnoreCase(vo.getProductType())) {
                continue;
            }
            voList.add(vo);
        }
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public FxDealDetailVO getDetailByDealNumber(String dealNumber) {
        Deal deal = getDealByNumber(dealNumber);
        if (deal == null) {
            return null;
        }
        FxDeal fxDeal = getFxDealByNumber(dealNumber);

        FxDealDetailVO vo = new FxDealDetailVO();
        BeanUtils.copyProperties(deal, vo);
        vo.setDealType(deal.getDealType());

        if (fxDeal != null) {
            vo.setManagementEntityId(fxDeal.getManagementEntityId());
            vo.setCurrencyPairId(fxDeal.getCurrencyPairId());
            vo.setSellCurrency(fxDeal.getSellCurrency());
            vo.setSellAmount(fxDeal.getSellAmount());
            vo.setBuyCurrency(fxDeal.getBuyCurrency());
            vo.setBuyAmount(fxDeal.getBuyAmount());
            vo.setExchangeRate(fxDeal.getExchangeRate());
            vo.setMarketRate(fxDeal.getMarketRate());
            vo.setSpreadBp(fxDeal.getSpreadBp());
            vo.setNotional(fxDeal.getNotional());
            vo.setFixingSource(fxDeal.getFixingSource());
            vo.setFixingRate(fxDeal.getFixingRate());
            vo.setSettlementAmount(fxDeal.getSettlementAmount());
            vo.setFixDate(fxDeal.getFixDate());
            vo.setFixCurrency(fxDeal.getFixCurrency());
            vo.setFixMarketRate(fxDeal.getFixMarketRate());
            vo.setVerifierBy(fxDeal.getVerifierBy());
            vo.setFixRemark(fxDeal.getFixRemark());
        }

        // 日期字段从 Deal 取（v3.2 移到公共表）
        vo.setTradeDate(deal.getDealDate());
        vo.setValueDate(deal.getValueDate());
        // maturity_date 字段在 tms_deals_t(public)，v3.2 强制 = value_date（应用层）
        // 此处简化：maturityDate = valueDate
        vo.setMaturityDate(deal.getValueDate());

        if (deal.getDealDate() != null && deal.getValueDate() != null) {
            vo.setTermDays((int) ChronoUnit.DAYS.between(deal.getDealDate(), deal.getValueDate()));
        }

        // 子列表
        vo.setDealMapList(listDealMapsByDeal(dealNumber));
        vo.setCashflowList(listCashflowsByDeal(dealNumber));
        vo.setActionList(listActionsByDeal(dealNumber));

        // ===== 2026-07-05: 补全关联实体名称 (前端 BaseDataPicker preloadRow 需求) =====
        populateEntityNames(vo, deal, fxDeal);

        return vo;
    }

    /**
     * FX 详情响应补全 *Name 字段 (失败容错)
     */
    private void populateEntityNames(FxDealDetailVO vo, Deal deal, FxDeal fxDeal) {
        Long mgmtEntityId = fxDeal != null ? fxDeal.getManagementEntityId() : null;
        try {
            Map<String, Object> me = entityNameLookup.findManagementEntity(mgmtEntityId);
            vo.setManagementEntityName(formatCodeName(me, "code", "name"));
        } catch (Exception ignore) {
        }
        try {
            Map<String, Object> cp = entityNameLookup.findCounterparty(deal.getCounterpartyId());
            vo.setCounterpartyName(formatCodeName(cp, "code", "name"));
        } catch (Exception ignore) {
        }
        try {
            Map<String, Object> tr = entityNameLookup.findTrader(deal.getTraderId());
            vo.setTraderName(formatCodeName(tr, "code", "name"));
        } catch (Exception ignore) {
        }
        try {
            Map<String, Object> inst = entityNameLookup.findInstrument(deal.getInstrumentId());
            vo.setInstrumentName(formatCodeName(inst, "instrumentCode", "instrumentName"));
        } catch (Exception ignore) {
        }
        try {
            Map<String, Object> pair = entityNameLookup.findCurrencyPair(fxDeal != null ? fxDeal.getCurrencyPairId() : null);
            if (pair != null) {
                Object pc = pair.get("pairCode");
                Object c1 = pair.get("baseCurrency");
                Object c2 = pair.get("quoteCurrency");
                StringBuilder sb = new StringBuilder();
                if (pc != null) sb.append(pc);
                if (c1 != null && c2 != null) {
                    if (sb.length() > 0) sb.append(" ");
                    sb.append(c1).append("/").append(c2);
                }
                vo.setCurrencyPairName(sb.length() == 0 ? null : sb.toString());
            }
        } catch (Exception ignore) {
        }
    }

    @Override
    public FxDealDTO getCopyData(String dealNumber) {
        Deal deal = getDealByNumber(dealNumber);
        if (deal == null) return null;
        FxDeal fxDeal = getFxDealByNumber(dealNumber);

        FxDealDTO dto = new FxDealDTO();
        Long mgmtEntityId = fxDeal != null ? fxDeal.getManagementEntityId() : null;
        dto.setManagementEntityId(mgmtEntityId);
        dto.setCounterpartyId(deal.getCounterpartyId());
        dto.setTraderId(deal.getTraderId());
        dto.setInstrumentId(deal.getInstrumentId());
        dto.setCurrencyPairId(fxDeal != null ? fxDeal.getCurrencyPairId() : null);
        dto.setSellCurrency(fxDeal != null ? fxDeal.getSellCurrency() : null);
        dto.setSellAmount(fxDeal != null ? fxDeal.getSellAmount() : null);
        dto.setBuyCurrency(fxDeal != null ? fxDeal.getBuyCurrency() : null);
        dto.setBuyAmount(fxDeal != null ? fxDeal.getBuyAmount() : null);
        dto.setExchangeRate(fxDeal != null ? fxDeal.getExchangeRate() : null);
        dto.setMarketRate(fxDeal != null ? fxDeal.getMarketRate() : null);
        dto.setSpreadBp(fxDeal != null ? fxDeal.getSpreadBp() : null);
        dto.setTradeDate(deal.getDealDate());
        dto.setValueDate(deal.getValueDate());
        dto.setNotional(fxDeal != null ? fxDeal.getNotional() : null);
        dto.setFixingSource(fxDeal != null ? fxDeal.getFixingSource() : null);
        dto.setDescription(deal.getDescription());
        dto.setRemark(deal.getRemark());
        // 系统字段清空
        dto.setId(null);
        dto.setDealNumber(null);
        dto.setOperator("");

        // ===== v3.3: 补全关联实体名称 (跨模块 JdbcTemplate) =====
        Map<String, Object> me = entityNameLookup.findManagementEntity(mgmtEntityId);
        if (me != null) dto.setManagementEntityName(formatCodeName(me, "code", "name"));

        Map<String, Object> cp = entityNameLookup.findCounterparty(deal.getCounterpartyId());
        if (cp != null) dto.setCounterpartyName(formatCodeName(cp, "code", "name"));

        Map<String, Object> inst = entityNameLookup.findInstrument(deal.getInstrumentId());
        if (inst != null) dto.setInstrumentName(formatCodeName(inst, "instrumentCode", "instrumentName"));

        Map<String, Object> tr = entityNameLookup.findTrader(deal.getTraderId());
        if (tr != null) dto.setTraderName(formatCodeName(tr, "code", "name"));

        Map<String, Object> pair = entityNameLookup.findCurrencyPair(fxDeal != null ? fxDeal.getCurrencyPairId() : null);
        if (pair != null) {
            // pairCode (baseCurrency/quoteCurrency) - 与 picker displayFormat 一致
            Object pc = pair.get("pairCode");
            Object c1 = pair.get("baseCurrency");
            Object c2 = pair.get("quoteCurrency");
            StringBuilder sb = new StringBuilder();
            if (pc != null) sb.append(pc);
            if (c1 != null && c2 != null) {
                if (sb.length() > 0) sb.append(" ");
                sb.append(c1).append("/").append(c2);
            }
            dto.setCurrencyPairName(sb.toString());
        }

        return dto;
    }

    /** "code (name)" 形式展示，与 BaseDataPicker displayFormat 风格对齐 */
    private static String formatCodeName(Map<String, Object> row, String codeKey, String nameKey) {
        if (row == null) return null;
        Object code = row.get(codeKey);
        Object name = row.get(nameKey);
        StringBuilder sb = new StringBuilder();
        if (code != null && !code.toString().isEmpty()) sb.append(code);
        if (name != null && !name.toString().isEmpty()) {
            if (sb.length() > 0) sb.append(" ");
            sb.append("(").append(name).append(")");
        }
        return sb.length() == 0 ? null : sb.toString();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean approveAction(String actionNumber, String approver, String remark) {
        Action action = getActionByNumber(actionNumber);
        if (action == null) {
            throw new RuntimeException("Action 不存在: " + actionNumber);
        }
        LocalDateTime now = LocalDateTime.now();

        // 关键：仅更新 Action.approval_status1，不改变 DealMap / Cashflow 任何状态
        if (action.getApprover1() == null) {
            action.setApprover1(approver);
        }
        action.setApprovalStatus1(APPROVAL_STATUS_APPROVED);
        if (StringUtils.hasText(remark)) {
            action.setApprovalRemark(remark);
        }
        action.setOperator(approver);
        action.setOperateAt(now);
        action.setUpdatedBy(approver);
        action.setUpdatedAt(now);
        action.setVersion((action.getVersion() == null ? 0 : action.getVersion()) + 1);
        actionMapper.updateById(action);

        // 若 Action 对应 FX 业务，同时更新 Deal.status → Active
        if (DEAL_TYPE.equals(action.getDealType())) {
            Deal deal = getDealByNumber(action.getDealNumber());
            if (deal != null) {
                deal.setStatus(DEAL_STATUS_ACTIVE);
                deal.setUpdatedBy(approver);
                deal.setUpdatedAt(now);
                deal.setVersion((deal.getVersion() == null ? 0 : deal.getVersion()) + 1);
                dealMapper.updateById(deal);
            }
        }
        log.info("[FX] Action {} approved by {}", actionNumber, approver);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean rejectAction(String actionNumber, String approver, String remark) {
        Action action = getActionByNumber(actionNumber);
        if (action == null) {
            throw new RuntimeException("Action 不存在: " + actionNumber);
        }
        LocalDateTime now = LocalDateTime.now();

        if (action.getApprover1() == null) {
            action.setApprover1(approver);
        }
        action.setApprovalStatus1(APPROVAL_STATUS_REJECTED);
        if (StringUtils.hasText(remark)) {
            action.setApprovalRemark(remark);
        }
        action.setOperator(approver);
        action.setOperateAt(now);
        action.setUpdatedBy(approver);
        action.setUpdatedAt(now);
        action.setVersion((action.getVersion() == null ? 0 : action.getVersion()) + 1);
        actionMapper.updateById(action);

        if (DEAL_TYPE.equals(action.getDealType())) {
            Deal deal = getDealByNumber(action.getDealNumber());
            if (deal != null) {
                deal.setStatus(APPROVAL_STATUS_REJECTED);
                deal.setUpdatedBy(approver);
                deal.setUpdatedAt(now);
                deal.setVersion((deal.getVersion() == null ? 0 : deal.getVersion()) + 1);
                dealMapper.updateById(deal);
            }
        }
        log.info("[FX] Action {} rejected by {}", actionNumber, approver);
        return true;
    }

    // ======================== Create ========================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createFxDeal(FxDealDTO dto) {
        validateFxDealDTO(dto);

        LocalDateTime now = LocalDateTime.now();
        String operator = dto.getOperator();
        String dealNumber = generateFxDealNumber();

        // 1. INSERT Deal（公共主表）
        Deal deal = new Deal();
        deal.setDealNumber(dealNumber);
        deal.setDealType(DEAL_TYPE);
        deal.setManagementEntity(String.valueOf(dto.getManagementEntityId()));
        deal.setCounterpartyId(dto.getCounterpartyId());
        deal.setInstrumentId(dto.getInstrumentId());
        deal.setTraderId(dto.getTraderId());
        deal.setDirection("FX");
        deal.setAmount(dto.getSellAmount());
        deal.setCurrency(dto.getSellCurrency());
        deal.setDealDate(dto.getTradeDate());
        deal.setValueDate(dto.getValueDate());
        deal.setStatus(DEAL_STATUS_NEW);
        deal.setDescription(dto.getDescription());
        deal.setRemark(dto.getRemark());
        deal.setCreatedBy(operator);
        deal.setCreatedAt(now);
        deal.setUpdatedBy(operator);
        deal.setUpdatedAt(now);
        deal.setVersion(0);
        deal.setDeleted(NOT_DELETED);
        dealMapper.insert(deal);

        Long dealId = deal.getId();

        // 2. INSERT FxDeal（共享主键）
        FxDeal fxDeal = new FxDeal();
        fxDeal.setId(dealId);
        fxDeal.setDealNumber(dealNumber);
        fxDeal.setManagementEntityId(dto.getManagementEntityId());
        fxDeal.setCurrencyPairId(dto.getCurrencyPairId());
        fxDeal.setSellCurrency(dto.getSellCurrency());
        fxDeal.setSellAmount(dto.getSellAmount());
        fxDeal.setBuyCurrency(dto.getBuyCurrency());
        fxDeal.setBuyAmount(dto.getBuyAmount());
        fxDeal.setExchangeRate(dto.getExchangeRate());
        fxDeal.setMarketRate(dto.getMarketRate());
        fxDeal.setSpreadBp(dto.getSpreadBp());
        fxDeal.setNotional(dto.getNotional() != null ? dto.getNotional() : dto.getSellAmount());
        fxDeal.setFixingSource(dto.getFixingSource());
        fxDeal.setFixingRate(dto.getFixingRate());
        fxDeal.setSettlementAmount(dto.getSettlementAmount());
        fxDeal.setDescription(dto.getDescription());
        fxDeal.setRemark(dto.getRemark());
        fxDeal.setCreatedBy(operator);
        fxDeal.setCreatedAt(now);
        fxDeal.setUpdatedBy(operator);
        fxDeal.setUpdatedAt(now);
        fxDeal.setVersion(0);
        fxDeal.setDeleted(NOT_DELETED);
        fxDealMapper.insert(fxDeal);

        // 3. INSERT Action(DEAL)
        String actionNumber = generateActionNumber();
        Action action = new Action();
        action.setActionNumber(actionNumber);
        action.setDealNumber(dealNumber);
        action.setDealType(DEAL_TYPE);
        action.setActionType(ACTION_TYPE_DEAL);
        action.setActionStatus(ACTION_STATUS_APPROVED); // FX 无审批流,直接 Approved
        action.setOperator(operator);
        action.setOperateAt(now);
        action.setRemark(dto.getRemark());
        action.setApprovalStatus1(ACTION_STATUS_APPROVED);
        action.setApprovalStatus2(ACTION_STATUS_APPROVED);
        action.setCreatedBy(operator);
        action.setCreatedAt(now);
        action.setVersion(0);
        action.setDeleted(NOT_DELETED);
        actionMapper.insert(action);

        // 4. INSERT 3 DealMap (BUY/SELL/RATE)
        // DMP#1: FX_SELL_AMOUNT (Outflow)
        String dmpSellNo = insertSingleDealMap(dealNumber, actionNumber, dto, operator, now,
                DEALMAP_TYPE_FX_SELL_AMOUNT, DIRECTION_OUTFLOW,
                dto.getSellAmount(), dto.getSellCurrency(),
                "FX Deal - SELL leg (amount snapshot)");

        // DMP#2: FX_BUY_AMOUNT (Inflow)
        String dmpBuyNo = insertSingleDealMap(dealNumber, actionNumber, dto, operator, now,
                DEALMAP_TYPE_FX_BUY_AMOUNT, DIRECTION_INFLOW,
                dto.getBuyAmount(), dto.getBuyCurrency(),
                "FX Deal - BUY leg (amount snapshot)");

        // DMP#3: FX_RATE (snapshot only, no CF)
        insertSingleDealMap(dealNumber, actionNumber, dto, operator, now,
                DEALMAP_TYPE_FX_RATE, null,
                dto.getExchangeRate(), null,
                "FX Deal - rate snapshot");

        // 5. INSERT Cashflow (SPOT/FWD: 2 条；NDF: 0 条)
        // v3.2: NDF 由 fixingSource 是否非空决定（PRD 要求）
        boolean isNdf = StringUtils.hasText(dto.getFixingSource());
        if (!isNdf) {
            // CF#1: SELL leg (Outflow)
            insertSingleCashflow(dealNumber, dmpSellNo, dto, operator, now,
                    DIRECTION_OUTFLOW, dto.getSellAmount(), dto.getSellCurrency(),
                    dto.getSellCurrency());
            // CF#2: BUY leg (Inflow)
            insertSingleCashflow(dealNumber, dmpBuyNo, dto, operator, now,
                    DIRECTION_INFLOW, dto.getBuyAmount(), dto.getBuyCurrency(),
                    dto.getBuyCurrency());
        } else {
            log.info("[FX][NDF] {} NDF Deal 创建,不生成 Cashflow,等 RATE_FIX", dealNumber);
        }

        // 6. UPDATE Deal.latest_action_number
        deal.setLatestActionNumber(actionNumber);
        deal.setUpdatedBy(operator);
        deal.setUpdatedAt(LocalDateTime.now());
        dealMapper.updateById(deal);

        log.info("[FX] DEAL 创建完成: dealNumber={}, instrumentId={}, isNdf={}, dmCount={}, cfCount={}",
                dealNumber, dto.getInstrumentId(), isNdf, 3, isNdf ? 0 : 2);

        return dealNumber;
    }

    // ======================== Update ========================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateFxDeal(FxDealDTO dto) {
        if (!StringUtils.hasText(dto.getDealNumber())) {
            throw new IllegalArgumentException("dealNumber 不能为空");
        }

        LocalDateTime now = LocalDateTime.now();
        String operator = dto.getOperator();
        String dealNumber = dto.getDealNumber();

        Deal existingDeal = getDealByNumber(dealNumber);
        if (existingDeal == null) {
            throw new RuntimeException("Deal not found: " + dealNumber);
        }
        FxDeal existingFx = getFxDealByNumber(dealNumber);
        if (existingFx == null) {
            throw new RuntimeException("FxDeal not found: " + dealNumber);
        }

        int newVersion = (existingDeal.getVersion() == null ? 0 : existingDeal.getVersion()) + 1;

        // 1. INSERT Action(UPDATE)
        String actionNumber = generateActionNumber();
        Action action = new Action();
        action.setActionNumber(actionNumber);
        action.setDealNumber(dealNumber);
        action.setDealType(DEAL_TYPE);
        action.setActionType(ACTION_TYPE_UPDATE);
        action.setActionStatus(ACTION_STATUS_APPROVED);
        action.setOperator(operator);
        action.setOperateAt(now);
        action.setRemark(dto.getRemark());
        action.setApprovalStatus1(ACTION_STATUS_APPROVED);
        action.setApprovalStatus2(ACTION_STATUS_APPROVED);
        action.setCreatedBy(operator);
        action.setCreatedAt(now);
        action.setVersion(0);
        action.setDeleted(NOT_DELETED);
        actionMapper.insert(action);

        // 2. UPDATE Deal
        existingDeal.setCounterpartyId(dto.getCounterpartyId());
        existingDeal.setInstrumentId(dto.getInstrumentId());
        existingDeal.setTraderId(dto.getTraderId());
        existingDeal.setAmount(dto.getSellAmount());
        existingDeal.setCurrency(dto.getSellCurrency());
        existingDeal.setDealDate(dto.getTradeDate());
        existingDeal.setValueDate(dto.getValueDate());
        existingDeal.setDescription(dto.getDescription());
        existingDeal.setRemark(dto.getRemark());
        existingDeal.setLatestActionNumber(actionNumber);
        existingDeal.setUpdatedBy(operator);
        existingDeal.setUpdatedAt(now);
        existingDeal.setVersion(newVersion);
        dealMapper.updateById(existingDeal);

        // 3. UPDATE FxDeal
        existingFx.setManagementEntityId(dto.getManagementEntityId());
        existingFx.setCurrencyPairId(dto.getCurrencyPairId());
        existingFx.setSellCurrency(dto.getSellCurrency());
        existingFx.setSellAmount(dto.getSellAmount());
        existingFx.setBuyCurrency(dto.getBuyCurrency());
        existingFx.setBuyAmount(dto.getBuyAmount());
        existingFx.setExchangeRate(dto.getExchangeRate());
        existingFx.setMarketRate(dto.getMarketRate());
        existingFx.setSpreadBp(dto.getSpreadBp());
        if (dto.getNotional() != null) existingFx.setNotional(dto.getNotional());
        existingFx.setFixingSource(dto.getFixingSource());
        existingFx.setUpdatedBy(operator);
        existingFx.setUpdatedAt(now);
        existingFx.setVersion(newVersion);
        fxDealMapper.updateById(existingFx);

        return true;
    }

    // ======================== Delete ========================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteFxDeal(Long id) {
        Deal deal = dealMapper.selectById(id);
        if (deal == null) {
            throw new RuntimeException("Deal not found: " + id);
        }

        LocalDateTime now = LocalDateTime.now();
        String operator = "system";
        String dealNumber = deal.getDealNumber();
        int newVersion = (deal.getVersion() == null ? 0 : deal.getVersion()) + 1;

        // 1. INSERT Action(DELETE)
        String actionNumber = generateActionNumber();
        Action action = new Action();
        action.setActionNumber(actionNumber);
        action.setDealNumber(dealNumber);
        action.setDealType(DEAL_TYPE);
        action.setActionType(ACTION_TYPE_DELETE);
        action.setActionStatus(ACTION_STATUS_APPROVED);
        action.setOperator(operator);
        action.setOperateAt(now);
        action.setApprovalStatus1(ACTION_STATUS_APPROVED);
        action.setApprovalStatus2(ACTION_STATUS_APPROVED);
        action.setCreatedBy(operator);
        action.setCreatedAt(now);
        action.setVersion(0);
        action.setDeleted(NOT_DELETED);
        actionMapper.insert(action);

        // 2. 软删 Deal
        deal.setStatus(DEAL_STATUS_CANCELED);
        deal.setLatestActionNumber(actionNumber);
        deal.setUpdatedBy(operator);
        deal.setUpdatedAt(now);
        deal.setVersion(newVersion);
        deal.setDeleted(DELETED);
        dealMapper.updateById(deal);

        // 3. 软删 FxDeal
        FxDeal fxDeal = getFxDealByNumber(dealNumber);
        if (fxDeal != null) {
            fxDeal.setUpdatedBy(operator);
            fxDeal.setUpdatedAt(now);
            fxDeal.setVersion(newVersion);
            fxDeal.setDeleted(DELETED);
            fxDealMapper.updateById(fxDeal);
        }

        // 4. 级联软删 DealMap
        LambdaUpdateWrapper<DealMap> dmDel = new LambdaUpdateWrapper<>();
        dmDel.eq(DealMap::getDealNumber, dealNumber)
                .eq(DealMap::getDeleted, NOT_DELETED)
                .set(DealMap::getDeleted, DELETED)
                .set(DealMap::getUpdatedBy, operator)
                .set(DealMap::getUpdatedAt, now);
        dealMapMapper.update(null, dmDel);

        // 5. 级联软删 Cashflow（通过 dealmap_number 关联）
        List<DealMap> allDms = dealMapMapper.selectList(new LambdaQueryWrapper<DealMap>()
                .eq(DealMap::getDealNumber, dealNumber));
        if (!allDms.isEmpty()) {
            List<String> dmNumbers = allDms.stream().map(DealMap::getDealmapNumber).toList();
            LambdaUpdateWrapper<Cashflow> cfDel = new LambdaUpdateWrapper<>();
            cfDel.in(Cashflow::getDealmapNumber, dmNumbers)
                    .eq(Cashflow::getDeleted, NOT_DELETED)
                    .set(Cashflow::getDeleted, DELETED)
                    .set(Cashflow::getUpdatedBy, operator)
                    .set(Cashflow::getUpdatedAt, now);
            cashflowMapper.update(null, cfDel);
        }

        return true;
    }

    // ======================== RATE_FIX (NDF only) ========================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> rateFix(Long id, RateFixRequest req) {
        if (req.getFixingRate() == null || req.getFixingRate().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("fixingRate 必须 > 0");
        }
        if (!StringUtils.hasText(req.getOperator())) {
            throw new IllegalArgumentException("operator 不能为空");
        }

        Deal deal = dealMapper.selectById(id);
        if (deal == null) {
            throw new RuntimeException("Deal not found: " + id);
        }
        FxDeal fxDeal = getFxDealByNumber(deal.getDealNumber());
        if (fxDeal == null) {
            throw new RuntimeException("FxDeal not found: " + deal.getDealNumber());
        }

        // RATE_FIX 只能一次
        if (fxDeal.getFixingRate() != null) {
            throw new IllegalArgumentException("该 FX Deal 已执行过 RATE_FIX,不能重复执行");
        }

        // 验证是 NDF（有 fixingSource）
        if (!StringUtils.hasText(fxDeal.getFixingSource())) {
            throw new IllegalArgumentException("RATE_FIX 仅适用于 NDF Deal(需 fixingSource)");
        }

        // Phase 1: 解析 fixDate/fixCurrency 默认值
        LocalDate fixDate = req.getFixDate() != null ? req.getFixDate() : deal.getValueDate();
        String fixCurrency = StringUtils.hasText(req.getFixCurrency()) ? req.getFixCurrency() : fxDeal.getBuyCurrency();

        // 校验 fixCurrency 必须是 buyCurrency 或 sellCurrency 之一
        if (!fixCurrency.equals(fxDeal.getBuyCurrency()) && !fixCurrency.equals(fxDeal.getSellCurrency())) {
            throw new IllegalArgumentException("fixCurrency 必须是 " + fxDeal.getBuyCurrency() + " 或 " + fxDeal.getSellCurrency());
        }

        LocalDateTime now = LocalDateTime.now();
        String dealNumber = deal.getDealNumber();
        String operator = req.getOperator();
        int newVersion = (fxDeal.getVersion() == null ? 0 : fxDeal.getVersion()) + 1;
        BigDecimal fixingRate = req.getFixingRate();

        // 1. INSERT Action(RATE_FIX) — 直接 Approved（FX 无审批流）
        String actionNumber = generateActionNumber();
        Action action = new Action();
        action.setActionNumber(actionNumber);
        action.setDealNumber(dealNumber);
        action.setDealType(DEAL_TYPE);
        action.setActionType(ACTION_TYPE_RATE_FIX);
        action.setActionStatus(ACTION_STATUS_APPROVED);
        action.setOperator(operator);
        action.setOperateAt(now);
        action.setRemark(req.getFixRemark());
        action.setApprovalStatus1(ACTION_STATUS_APPROVED);
        action.setApprovalStatus2(ACTION_STATUS_APPROVED);
        action.setCreatedBy(operator);
        action.setCreatedAt(now);
        action.setVersion(0);
        action.setDeleted(NOT_DELETED);
        actionMapper.insert(action);

        // 2. INSERT DealMap(FX_FIX)
        String dealmapNumber = generateDealMapNumber();
        DealMap fixDm = new DealMap();
        fixDm.setDealmapNumber(dealmapNumber);
        fixDm.setDealNumber(dealNumber);
        fixDm.setActionNumber(actionNumber);
        fixDm.setEventType(EVENT_TYPE_FX);
        fixDm.setEventStatus(EVENT_STATUS_ACTIVE);
        fixDm.setEventDate(fixDate);
        fixDm.setValueDate(fixDate);
        fixDm.setIsReversal(NOT_DELETED);
        fixDm.setDealmapType(DEALMAP_TYPE_FX_FIX);
        fixDm.setAmountOrRate(fixingRate);
        fixDm.setDescription("FX NDF RATE_FIX - fixing rate snapshot (" + fixingRate + ")");
        fixDm.setCreatedBy(operator);
        fixDm.setCreatedAt(now);
        fixDm.setVersion(0);
        fixDm.setDeleted(NOT_DELETED);
        dealMapMapper.insert(fixDm);

        // 3. 计算 settlementAmount = notional × (fixingRate - exchangeRate)
        BigDecimal notional = fxDeal.getNotional() != null ? fxDeal.getNotional() : fxDeal.getSellAmount();
        BigDecimal settlementAmount = notional.multiply(fixingRate.subtract(fxDeal.getExchangeRate()))
                .setScale(AMOUNT_SCALE, RoundingMode.HALF_UP);

        String direction = settlementAmount.compareTo(BigDecimal.ZERO) >= 0 ? DIRECTION_INFLOW : DIRECTION_OUTFLOW;

        // 4. INSERT Cashflow(差额) — currency 使用 fixCurrency（而非写死 buyCurrency）
        String cflowNumber = generateCflowNumber();
        Cashflow cf = new Cashflow();
        cf.setCflowNumber(cflowNumber);
        cf.setDealNumber(dealNumber);
        cf.setDealmapNumber(dealmapNumber);
        cf.setManagementEntity(String.valueOf(fxDeal.getManagementEntityId()));
        cf.setDirection(direction);
        cf.setAmount(settlementAmount.abs());
        cf.setCurrency(fixCurrency);
        cf.setCflowDate(fixDate);
        cf.setValueDate(fixDate);
        cf.setSourceType(CFLOW_SOURCE_TYPE);
        cf.setSourceRef(dealNumber);
        cf.setStatus(CFLOW_STATUS_CREATED);
        cf.setPurpose("FX NDF RATE_FIX settlement");
        cf.setCreatedBy(operator);
        cf.setCreatedAt(now);
        cf.setVersion(0);
        cf.setDeleted(NOT_DELETED);
        cashflowMapper.insert(cf);

        // 5. UPDATE FxDeal（fixingRate + settlementAmount + 新增 Phase 1 字段）
        fxDeal.setFixingRate(fixingRate);
        fxDeal.setSettlementAmount(settlementAmount);
        fxDeal.setFixDate(fixDate);
        fxDeal.setFixCurrency(fixCurrency);
        if (req.getFixMarketRate() != null) {
            fxDeal.setFixMarketRate(req.getFixMarketRate());
        }
        if (StringUtils.hasText(req.getVerifierBy())) {
            fxDeal.setVerifierBy(req.getVerifierBy());
        }
        if (StringUtils.hasText(req.getFixRemark())) {
            fxDeal.setFixRemark(req.getFixRemark());
        }
        fxDeal.setUpdatedBy(operator);
        fxDeal.setUpdatedAt(now);
        fxDeal.setVersion(newVersion);
        fxDealMapper.updateById(fxDeal);

        // 6. UPDATE Deal（status = Active）
        deal.setStatus(DEAL_STATUS_ACTIVE);
        deal.setLatestActionNumber(actionNumber);
        deal.setUpdatedBy(operator);
        deal.setUpdatedAt(now);
        deal.setVersion(newVersion);
        dealMapper.updateById(deal);

        Map<String, Object> result = new HashMap<>();
        result.put("dealNumber", dealNumber);
        result.put("status", DEAL_STATUS_ACTIVE);
        result.put("settlementAmount", settlementAmount);
        result.put("dealmapNumber", dealmapNumber);
        result.put("currency", fixCurrency);
        result.put("direction", direction);
        return result;
    }

    @Override
    @Deprecated
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> rateFix(Long id, BigDecimal fixingRate, String operator) {
        RateFixRequest req = new RateFixRequest();
        req.setFixingRate(fixingRate);
        req.setOperator(operator);
        return rateFix(id, req);
    }

    // ======================== Helper: DealMap / Cashflow ========================

    private String insertSingleDealMap(String dealNumber, String actionNumber,
                                       FxDealDTO dto, String operator, LocalDateTime now,
                                       String dealmapType, String direction,
                                       BigDecimal amountOrRate, String currency,
                                       String description) {
        String dealmapNumber = generateDealMapNumber();
        DealMap dm = new DealMap();
        dm.setDealmapNumber(dealmapNumber);
        dm.setDealNumber(dealNumber);
        dm.setActionNumber(actionNumber);
        dm.setEventType(EVENT_TYPE_FX);
        dm.setEventStatus(EVENT_STATUS_ACTIVE);
        dm.setDirection(direction);
        dm.setAmount(amountOrRate); // 兼容旧 amount 字段
        dm.setCurrency(currency);
        dm.setEventDate(dto.getValueDate());
        dm.setValueDate(dto.getValueDate());
        dm.setIsReversal(NOT_DELETED);
        dm.setDealmapType(dealmapType);
        dm.setAmountOrRate(amountOrRate);
        dm.setDescription(description);
        dm.setCreatedBy(operator);
        dm.setCreatedAt(now);
        dm.setUpdatedBy(operator);
        dm.setUpdatedAt(now);
        dm.setVersion(0);
        dm.setDeleted(NOT_DELETED);
        dealMapMapper.insert(dm);
        return dealmapNumber;
    }

    private void insertSingleCashflow(String dealNumber, String dealmapNumber,
                                     FxDealDTO dto, String operator, LocalDateTime now,
                                     String direction, BigDecimal amount, String currency,
                                     String displayCurrency) {
        String cflowNumber = generateCflowNumber();
        Cashflow cf = new Cashflow();
        cf.setCflowNumber(cflowNumber);
        cf.setDealNumber(dealNumber);
        cf.setDealmapNumber(dealmapNumber);
        cf.setManagementEntity(String.valueOf(dto.getManagementEntityId()));
        cf.setDirection(direction);
        cf.setAmount(amount);
        cf.setCurrency(currency);
        cf.setCflowDate(dto.getValueDate());
        cf.setValueDate(dto.getValueDate());
        cf.setSourceType(CFLOW_SOURCE_TYPE);
        cf.setSourceRef(dealNumber);
        cf.setStatus(CFLOW_STATUS_CREATED);
        cf.setPurpose(dto.getDescription());
        cf.setCreatedBy(operator);
        cf.setCreatedAt(now);
        cf.setUpdatedBy(operator);
        cf.setUpdatedAt(now);
        cf.setVersion(0);
        cf.setDeleted(NOT_DELETED);
        cashflowMapper.insert(cf);
    }

    private boolean isNdfInstrument(Long instrumentId) {
        if (instrumentId == null) return false;
        // v3.2: NDF 由 fixingSource 决定（PRD 要求），但在此方法中通过 instrumentId 查询
        // 实际判断放 createFxDeal 中直接检查 dto.getFixingSource()
        // 此处保留兼容，默认返回 false（调用方通过 dto 自己判断）
        return false;
    }

    // ======================== Validation ========================

    private void validateFxDealDTO(FxDealDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("DTO 不能为空");
        }
        if (dto.getManagementEntityId() == null) {
            throw new IllegalArgumentException("管理主体不能为空");
        }
        if (dto.getCounterpartyId() == null) {
            throw new IllegalArgumentException("交易对手不能为空");
        }
        if (dto.getTraderId() == null) {
            throw new IllegalArgumentException("交易员不能为空");
        }
        if (dto.getInstrumentId() == null) {
            throw new IllegalArgumentException("金融工具不能为空");
        }
        if (dto.getCurrencyPairId() == null) {
            throw new IllegalArgumentException("币种对不能为空");
        }
        if (!StringUtils.hasText(dto.getSellCurrency())) {
            throw new IllegalArgumentException("卖出币种不能为空");
        }
        if (!StringUtils.hasText(dto.getBuyCurrency())) {
            throw new IllegalArgumentException("买入币种不能为空");
        }
        if (dto.getSellCurrency().equals(dto.getBuyCurrency())) {
            throw new IllegalArgumentException("卖出币种与买入币种不能相同");
        }
        if (dto.getSellAmount() == null || dto.getSellAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("卖出金额必须 > 0");
        }
        if (dto.getBuyAmount() == null || dto.getBuyAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("买入金额必须 > 0");
        }
        if (dto.getExchangeRate() == null || dto.getExchangeRate().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("成交汇率必须 > 0");
        }
        if (dto.getMarketRate() == null || dto.getMarketRate().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("市场汇率必须 > 0");
        }
        if (dto.getSpreadBp() == null) {
            throw new IllegalArgumentException("点差不能为空");
        }
        if (dto.getTradeDate() == null) {
            throw new IllegalArgumentException("交易日不能为空");
        }
        if (dto.getValueDate() == null) {
            throw new IllegalArgumentException("交割日不能为空");
        }
        if (dto.getValueDate().isBefore(dto.getTradeDate())) {
            throw new IllegalArgumentException("交割日不能早于交易日");
        }
        if (!StringUtils.hasText(dto.getOperator())) {
            throw new IllegalArgumentException("操作人不能为空");
        }
    }

    // ======================== Helper: queries ========================

    private Deal getDealByNumber(String dealNumber) {
        LambdaQueryWrapper<Deal> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Deal::getDealNumber, dealNumber);
        return dealMapper.selectOne(wrapper);
    }

    private Action getActionByNumber(String actionNumber) {
        LambdaQueryWrapper<Action> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Action::getActionNumber, actionNumber);
        return actionMapper.selectOne(wrapper);
    }

    private FxDeal getFxDealByNumber(String dealNumber) {
        LambdaQueryWrapper<FxDeal> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FxDeal::getDealNumber, dealNumber);
        return fxDealMapper.selectOne(wrapper);
    }

    private List<DealMapVO> listDealMapsByDeal(String dealNumber) {
        LambdaQueryWrapper<DealMap> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DealMap::getDealNumber, dealNumber)
                .eq(DealMap::getDeleted, NOT_DELETED)
                .orderByAsc(DealMap::getId);
        return dealMapMapper.selectList(wrapper).stream().map(this::convertDealMapToVO).toList();
    }

    private List<CashflowVO> listCashflowsByDeal(String dealNumber) {
        LambdaQueryWrapper<Cashflow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Cashflow::getDealNumber, dealNumber)
                .eq(Cashflow::getDeleted, NOT_DELETED)
                .orderByAsc(Cashflow::getId);
        return cashflowMapper.selectList(wrapper).stream().map(this::convertCashflowToVO).toList();
    }

    private List<ActionVO> listActionsByDeal(String dealNumber) {
        LambdaQueryWrapper<Action> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Action::getDealNumber, dealNumber)
                .eq(Action::getDeleted, NOT_DELETED)
                .orderByAsc(Action::getCreatedAt);
        return actionMapper.selectList(wrapper).stream().map(this::convertActionToVO).toList();
    }

    // ======================== Helper: number generators ========================

    private String generateFxDealNumber() {
        String dateStr = LocalDateTime.now().format(DATE_FORMATTER);
        String prefix = "FX" + dateStr;
        LambdaQueryWrapper<Deal> wrapper = new LambdaQueryWrapper<>();
        wrapper.likeRight(Deal::getDealNumber, prefix)
                .orderByDesc(Deal::getDealNumber)
                .last("LIMIT 1");
        Deal last = dealMapper.selectOne(wrapper);
        int seq = 1;
        if (last != null && last.getDealNumber() != null
                && last.getDealNumber().length() > prefix.length()) {
            try {
                String lastSeq = last.getDealNumber().substring(prefix.length());
                seq = Integer.parseInt(lastSeq) + 1;
            } catch (NumberFormatException ignore) {
                seq = 1;
            }
        }
        return prefix + String.format("%04d", seq);
    }

    private String generateActionNumber() {
        String dateStr = LocalDateTime.now().format(DATE_FORMATTER);
        String prefix = "ACT" + dateStr;
        LambdaQueryWrapper<Action> wrapper = new LambdaQueryWrapper<>();
        wrapper.likeRight(Action::getActionNumber, prefix)
                .orderByDesc(Action::getActionNumber)
                .last("LIMIT 1");
        Action last = actionMapper.selectOne(wrapper);
        int seq = 1;
        if (last != null && last.getActionNumber() != null
                && last.getActionNumber().length() > prefix.length()) {
            try {
                String lastSeq = last.getActionNumber().substring(prefix.length());
                seq = Integer.parseInt(lastSeq) + 1;
            } catch (NumberFormatException ignore) {
                seq = 1;
            }
        }
        return prefix + String.format("%04d", seq);
    }

    private String generateDealMapNumber() {
        String dateStr = LocalDateTime.now().format(DATE_FORMATTER);
        String prefix = "DMP" + dateStr;
        LambdaQueryWrapper<DealMap> wrapper = new LambdaQueryWrapper<>();
        wrapper.likeRight(DealMap::getDealmapNumber, prefix)
                .orderByDesc(DealMap::getDealmapNumber)
                .last("LIMIT 1");
        DealMap last = dealMapMapper.selectOne(wrapper);
        int seq = 1;
        if (last != null && last.getDealmapNumber() != null
                && last.getDealmapNumber().length() > prefix.length()) {
            try {
                String lastSeq = last.getDealmapNumber().substring(prefix.length());
                seq = Integer.parseInt(lastSeq) + 1;
            } catch (NumberFormatException ignore) {
                seq = 1;
            }
        }
        return prefix + String.format("%04d", seq);
    }

    private String generateCflowNumber() {
        String dateStr = LocalDateTime.now().format(DATE_FORMATTER);
        String prefix = "CF" + dateStr;
        LambdaQueryWrapper<Cashflow> wrapper = new LambdaQueryWrapper<>();
        wrapper.likeRight(Cashflow::getCflowNumber, prefix)
                .orderByDesc(Cashflow::getCflowNumber)
                .last("LIMIT 1");
        Cashflow last = cashflowMapper.selectOne(wrapper);
        int seq = 1;
        if (last != null && last.getCflowNumber() != null
                && last.getCflowNumber().length() > prefix.length()) {
            try {
                String lastSeq = last.getCflowNumber().substring(prefix.length());
                seq = Integer.parseInt(lastSeq) + 1;
            } catch (NumberFormatException ignore) {
                seq = 1;
            }
        }
        return prefix + String.format("%04d", seq);
    }

    // ======================== Converters ========================

    private FxDealVO convertDealToListVO(Deal deal) {
        FxDealVO vo = new FxDealVO();
        BeanUtils.copyProperties(deal, vo);
        FxDeal fx = getFxDealByNumber(deal.getDealNumber());
        if (fx != null) {
            vo.setManagementEntityId(fx.getManagementEntityId());
            vo.setCurrencyPairId(fx.getCurrencyPairId());
            vo.setSellCurrency(fx.getSellCurrency());
            vo.setSellAmount(fx.getSellAmount());
            vo.setBuyCurrency(fx.getBuyCurrency());
            vo.setBuyAmount(fx.getBuyAmount());
            vo.setExchangeRate(fx.getExchangeRate());
            vo.setMarketRate(fx.getMarketRate());
            vo.setSpreadBp(fx.getSpreadBp());
            // productType 简化推断:NDF 有 fixingSource
            if (StringUtils.hasText(fx.getFixingSource())) {
                vo.setProductType("NDF");
            } else if (deal.getValueDate() != null && deal.getDealDate() != null
                    && !deal.getValueDate().isEqual(deal.getDealDate())) {
                vo.setProductType("FWD");
            } else {
                vo.setProductType("SPOT");
            }
        }
        vo.setTradeDate(deal.getDealDate());
        vo.setValueDate(deal.getValueDate());
        return vo;
    }

    private DealMapVO convertDealMapToVO(DealMap dm) {
        DealMapVO vo = new DealMapVO();
        BeanUtils.copyProperties(dm, vo);
        return vo;
    }

    private CashflowVO convertCashflowToVO(Cashflow cf) {
        CashflowVO vo = new CashflowVO();
        BeanUtils.copyProperties(cf, vo);
        return vo;
    }

    private ActionVO convertActionToVO(Action a) {
        ActionVO vo = new ActionVO();
        BeanUtils.copyProperties(a, vo);
        return vo;
    }
}
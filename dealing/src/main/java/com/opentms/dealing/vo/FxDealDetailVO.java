package com.opentms.dealing.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * FX 交易详情聚合 VO（v3.2）
 * <p>聚合：基本信息 + DealMap 列表 + Cashflow 列表 + Action 列表</p>
 */
@Data
public class FxDealDetailVO {

    private Long id;

    private String dealNumber;

    private String dealType;

    // 公共字段
    private Long managementEntityId;

    private String managementEntityName;

    private Long counterpartyId;

    private String counterpartyName;

    private Long traderId;

    private String traderName;

    private Long instrumentId;

    private String instrumentName;

    /** 产品类型 SPOT/FWD/NDF（由 instrument 决定） */
    private String productType;

    // FX 特性字段
    private Long currencyPairId;

    private String currencyPairName;

    private String sellCurrency;

    private BigDecimal sellAmount;

    private String buyCurrency;

    private BigDecimal buyAmount;

    private BigDecimal exchangeRate;

    private BigDecimal marketRate;

    private BigDecimal spreadBp;

    // NDF 字段
    private BigDecimal notional;

    private String fixingSource;

    private BigDecimal fixingRate;

    private BigDecimal settlementAmount;

    // 日期字段（v3.2 移到公共表）
    private LocalDate tradeDate;

    private LocalDate valueDate;

    private LocalDate maturityDate;

    /** 期限（天数） = valueDate - tradeDate */
    private Integer termDays;

    private String status;

    private String description;

    private String remark;

    private String latestActionNumber;

    // 审计
    private String createdBy;

    private LocalDateTime createdAt;

    private String updatedBy;

    private LocalDateTime updatedAt;

    private Integer version;

    // 子列表
    /** DealMap 列表（v3.2: 3-4 行：FX_BUY_AMOUNT/FX_SELL_AMOUNT/FX_RATE[/FX_FIX]） */
    private List<DealMapVO> dealMapList;

    /** Cashflow 列表（SPOT/FWD: 2 条；NDF RATE_FIX 后: 1 条） */
    private List<CashflowVO> cashflowList;

    /** Action 列表（DEAL/UPDATE/DELETE/RATE_FIX） */
    private List<ActionVO> actionList;
}
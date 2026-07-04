package com.opentms.dealing.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * FX 交易请求 DTO（v3.2 - 后端 calculate + 共享主键 + 4 Action）
 * <p>用于创建/更新 FX 交易（SPOT/FWD/NDF）</p>
 */
@Data
public class FxDealDTO {

    /** 主键（更新时需要） */
    private Long id;

    /** Deal 编号（更新时需要） */
    private String dealNumber;

    // ==================== 通用字段 ====================

    /** 管理主体 ID（必填，FK → tms_management_entity_t.id） */
    @NotNull(message = "管理主体不能为空")
    private Long managementEntityId;

    /** 交易对手 ID（必填，FK → tms_counterparty_t.id） */
    @NotNull(message = "交易对手不能为空")
    private Long counterpartyId;

    /** 交易员 ID（必填，FK → tms_trader_t.id） */
    @NotNull(message = "交易员不能为空")
    private Long traderId;

    /** 金融工具 ID（必填，FX 产品类型内嵌） */
    @NotNull(message = "金融工具不能为空")
    private Long instrumentId;

    /** 币种对 ID（必填，FK → tms_currency_pair_t.id） */
    @NotNull(message = "币种对不能为空")
    private Long currencyPairId;

    // ==================== 价值字段 ====================

    @NotBlank(message = "卖出币种不能为空")
    private String sellCurrency;

    @NotNull(message = "卖出金额不能为空")
    private BigDecimal sellAmount;

    @NotBlank(message = "买入币种不能为空")
    private String buyCurrency;

    @NotNull(message = "买入金额不能为空")
    private BigDecimal buyAmount;

    @NotNull(message = "成交汇率不能为空")
    private BigDecimal exchangeRate;

    @NotNull(message = "市场汇率不能为空")
    private BigDecimal marketRate;

    @NotNull(message = "点差不能为空")
    private BigDecimal spreadBp;

    // ==================== 日期字段（v3.2 移到公共表） ====================

    /** 交易日（必填） */
    @NotNull(message = "交易日不能为空")
    private LocalDate tradeDate;

    /** 交割日（必填，DB 字段 value_date） */
    @NotNull(message = "交割日不能为空")
    private LocalDate valueDate;

    // ==================== NDF 特有字段 ====================

    /** NDF 名义本金（可选，= sellAmount） */
    private BigDecimal notional;

    /** NDF fixing 汇率来源（NDF 必填，例："BLOOMBERG BFIX"） */
    private String fixingSource;

    /** NDF 结算汇率（RATE_FIX 时由系统填入） */
    private BigDecimal fixingRate;

    /** NDF 结算金额（RATE_FIX 时计算 = notional × (fixingRate - exchangeRate)） */
    private BigDecimal settlementAmount;

    // ==================== 描述 ====================

    private String description;

    private String remark;

    // ==================== 操作 ====================

    /** 操作人（必填） */
    @NotBlank(message = "操作人不能为空")
    private String operator;
}
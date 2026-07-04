package com.opentms.dealing.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * FX 交易个性化实体（v3.2 - 共享主键 + 后端 calculate + NDF 专用字段）
 * <p>对应表：tms_fx_deals_t（与 tms_deals_t.id 共享主键）</p>
 * <p>核心特点：</p>
 * <ul>
 *     <li>共享主键：id 值 = tms_deals_t.id，无独立 PK 增长</li>
 *     <li>管理主体 FK 改为 id 强类型（managementEntityId BIGINT FK）</li>
 *     <li>日期字段统一在公共表 tms_deals_t（trade_date / value_date / maturity_date）</li>
 *     <li>NDF 特有：notional / fixingSource / fixingRate / settlementAmount</li>
 * </ul>
 */
@Data
@TableName("tms_fx_deals_t")
public class FxDeal {

    /** 共享主键：值 = tms_deals_t.id */
    @TableId(type = IdType.INPUT)
    private Long id;

    @TableField("deal_number")
    private String dealNumber;

    /** 管理主体 ID（强类型 FK → tms_management_entity_t.id） */
    @TableField("management_entity_id")
    private Long managementEntityId;

    /** 币种对 ID（FK → tms_currency_pair_t.id） */
    @TableField("currency_pair_id")
    private Long currencyPairId;

    /** 卖出币种（冗余自 currency_pair.base_currency） */
    @TableField("sell_currency")
    private String sellCurrency;

    /** 卖出金额（DECIMAL(38,18)） */
    @TableField("sell_amount")
    private BigDecimal sellAmount;

    /** 买入币种（冗余自 currency_pair.quote_currency） */
    @TableField("buy_currency")
    private String buyCurrency;

    /** 买入金额（DECIMAL(38,18)，后端 calculate 算） */
    @TableField("buy_amount")
    private BigDecimal buyAmount;

    /** 成交汇率（DECIMAL(18,8)） */
    @TableField("exchange_rate")
    private BigDecimal exchangeRate;

    /** 市场汇率（DECIMAL(18,8)） */
    @TableField("market_rate")
    private BigDecimal marketRate;

    /** 点差（基点，DECIMAL(10,4)） */
    @TableField("spread_bp")
    private BigDecimal spreadBp;

    /** NDF 名义本金（DECIMAL(38,18)） */
    private BigDecimal notional;

    /** NDF fixing 汇率来源（如 "BLOOMBERG BFIX"） */
    @TableField("fixing_source")
    private String fixingSource;

    /** NDF 结算汇率（RATE_FIX 时由系统填入） */
    @TableField("fixing_rate")
    private BigDecimal fixingRate;

    /** NDF 结算金额 = notional × (fixingRate - exchangeRate) */
    @TableField("settlement_amount")
    private BigDecimal settlementAmount;

    private String description;

    private String remark;

    @TableField("created_by")
    private String createdBy;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_by")
    private String updatedBy;

    @TableField("updated_at")
    private LocalDateTime updatedAt;

    private Integer version;

    private String deleted;
}
package com.opentms.dealing.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DealMap 业务事件表（v2.0 精简版）
 * <p>
 * 记录交易生命周期中的业务事件（ActualCashflow、AccountTransfer、Unwind 等）。
 * 每个 DealMap 由一个 Action 触发，并通过 dealmap_number 字符串关联到 Cashflow。
 * </p>
 */
@Data
@TableName("tms_deal_map_t")
public class DealMap {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("dealmap_number")
    private String dealmapNumber;

    @TableField("deal_number")
    private String dealNumber;

    @TableField("action_number")
    private String actionNumber;

    @TableField("event_type")
    private String eventType;

    @TableField("event_status")
    private String eventStatus;

    /** v3.2 新增:DealMap 类型（FX_BUY_AMOUNT/FX_SELL_AMOUNT/FX_RATE/FX_FIX/AC/AT） */
    @TableField("dealmap_type")
    private String dealmapType;

    /** v3.2 新增:DealMap 单字段（替代 buy_amount/sell_amount/rate 3 字段） */
    @TableField("amount_or_rate")
    private BigDecimal amountOrRate;

    private BigDecimal amount;

    private String currency;

    private String direction;

    @TableField("event_date")
    private LocalDate eventDate;

    @TableField("value_date")
    private LocalDate valueDate;

    @TableField("is_reversal")
    private String isReversal;

    @TableField("reverses_event_id")
    private Long reversesEventId;

    @TableField("reversed_by_event_id")
    private Long reversedByEventId;

    private String description;

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

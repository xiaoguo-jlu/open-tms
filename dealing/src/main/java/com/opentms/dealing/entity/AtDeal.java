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
 * AT 交易个性化实体（Account Transfer）
 * 对应表：tms_at_deals_t
 * 核心特点：双腿设计（source_account → dest_account）+ 跨币种汇率
 */
@Data
@TableName("tms_at_deals_t")
public class AtDeal {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("deal_number")
    private String dealNumber;

    /** 转账类型：SAME_COMPANY / CROSS_COMPANY / CROSS_BORDER */
    @TableField("transfer_type")
    private String transferType;

    /** 付出方银行账户 ID */
    @TableField("source_account_id")
    private Long sourceAccountId;

    /** 收入方银行账户 ID */
    @TableField("dest_account_id")
    private Long destAccountId;

    @TableField("source_amount")
    private BigDecimal sourceAmount;

    @TableField("dest_amount")
    private BigDecimal destAmount;

    @TableField("source_currency")
    private String sourceCurrency;

    @TableField("dest_currency")
    private String destCurrency;

    @TableField("exchange_rate")
    private BigDecimal exchangeRate;

    @TableField("management_entity")
    private String managementEntity;

    @TableField("value_date")
    private LocalDate valueDate;

    @TableField("payment_method")
    private String paymentMethod;

    private String purpose;

    private String status;

    @TableField("latest_action_number")
    private String latestActionNumber;

    private String createdBy;

    private LocalDateTime createdAt;

    private String updatedBy;

    private LocalDateTime updatedAt;

    private Integer version;

    private String deleted;
}

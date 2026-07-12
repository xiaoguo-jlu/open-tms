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
 * 现金流表（v2.0 增加 dealmap_number 字段反向关联 DealMap）
 */
@Data
@TableName("tms_cashflow_t")
public class Cashflow {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("cflow_number")
    private String cflowNumber;

    @TableField("deal_number")
    private String dealNumber;

    @TableField("dealmap_number")
    private String dealmapNumber;

    @TableField("management_entity_id")
    private String managementEntity;

    @TableField("bank_account")
    private String bankAccount;

    @TableField("counterparty_account")
    private String counterpartyAccount;

    /** 我方银行账户 ID（v1.0 新增 — 默认银行账户规则自动填充或人工选） */
    @TableField("bank_account_id")
    private Long bankAccountId;

    /** 对手方银行账户 ID（v1.0 新增） */
    @TableField("counterparty_bank_account_id")
    private Long counterpartyBankAccountId;

    private String direction;

    private BigDecimal amount;

    private String currency;

    @TableField("cflow_date")
    private LocalDate cflowDate;

    @TableField("value_date")
    private LocalDate valueDate;

    @TableField("source_type")
    private String sourceType;

    @TableField("source_ref")
    private String sourceRef;

    private String status;

    @TableField("counterparty_name")
    private String counterpartyName;

    private String purpose;

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

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

    @TableField("business_unit")
    private String businessUnit;

    @TableField("bank_account")
    private String bankAccount;

    @TableField("counterparty_account")
    private String counterpartyAccount;

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

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
 * 现金流镜像表 (v1.0 - 2026-07-11)
 *
 * <p>记录 Cashflow 在 CREATE / UPDATE / DELETE / STATUS_CHANGE / RATE_FIX 事件时的字段快照。
 * 监管要求 ≥ 7 年保留，永不过期。DDL: db/schema/29-cashflow-enhance.sql。</p>
 *
 * <p>镜像失败 → 整个 cashflow 操作回滚（@Transactional）。</p>
 *
 * @author Open-TMS Backend Developer
 * @since 2026-07-11
 */
@Data
@TableName("tms_cashflow_image_t")
public class CashflowImage {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 镜像编号（全局唯一，IMG + yyyyMMdd + 4 位流水） */
    @TableField("image_number")
    private String imageNumber;

    /** 原现金流编号 */
    @TableField("cflow_number")
    private String cflowNumber;

    /** 关联交易编号（冗余便于审计查询） */
    @TableField("deal_number")
    private String dealNumber;

    /** 版本号（与 tms_deals_t.version 同步递增） */
    private Integer version;

    // ===== 业务字段（与 tms_cashflow_t 同步快照） =====

    @TableField("dealmap_number")
    private String dealmapNumber;

    @TableField("management_entity_id")
    private String managementEntity;

    @TableField("bank_account")
    private String bankAccount;

    @TableField("counterparty_account")
    private String counterpartyAccount;

    /** 我方银行账户 ID（v1.0 新增） */
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

    // ===== 镜像元信息 =====

    /** CREATE / UPDATE / DELETE / STATUS_CHANGE / RATE_FIX */
    @TableField("image_type")
    private String imageType;

    private String operator;

    @TableField("operate_at")
    private LocalDateTime operateAt;

    // ===== 审计字段 =====

    @TableField("created_by")
    private String createdBy;

    @TableField("created_at")
    private LocalDateTime createdAt;

    private String deleted;
}
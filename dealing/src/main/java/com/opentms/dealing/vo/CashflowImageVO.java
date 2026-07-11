package com.opentms.dealing.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 现金流镜像 VO（与 tms_cashflow_image_t 字段对齐）
 *
 * @author Open-TMS Backend Developer
 * @since 2026-07-11
 */
@Data
public class CashflowImageVO {

    private Long id;

    private String imageNumber;

    private String cflowNumber;

    private String dealNumber;

    private Integer version;

    private String dealmapNumber;

    private String businessUnit;

    private String bankAccount;

    private String counterpartyAccount;

    private Long bankAccountId;

    private Long counterpartyBankAccountId;

    private String direction;

    private BigDecimal amount;

    private String currency;

    private LocalDate cflowDate;

    private LocalDate valueDate;

    private String sourceType;

    private String sourceRef;

    private String status;

    private String counterpartyName;

    private String purpose;

    private String remark;

    private String imageType;

    private String operator;

    private LocalDateTime operateAt;

    private String createdBy;

    private LocalDateTime createdAt;
}
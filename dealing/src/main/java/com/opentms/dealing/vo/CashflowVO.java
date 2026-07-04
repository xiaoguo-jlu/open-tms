package com.opentms.dealing.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class CashflowVO {

    private Long id;

    private String cflowNumber;

    private String dealNumber;

    private String dealmapNumber;

    private String managementEntity;

    private String bankAccount;

    private String counterpartyAccount;

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

    private String createdBy;

    private LocalDateTime createdAt;

    private String updatedBy;

    private LocalDateTime updatedAt;

    private Integer version;

    private String deleted;
}

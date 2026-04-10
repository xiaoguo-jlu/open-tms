package com.opentms.dealing.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class DealVO {

    private Long id;

    private String code;

    private String name;

    private String status;

    private String createdBy;

    private LocalDateTime createdAt;

    private String updatedBy;

    private LocalDateTime updatedAt;

    private String dealNo;

    private String dealType;

    private String dealSubtype;

    private Long instrumentId;

    private Long counterpartyId;

    private Long businessUnitId;

    private BigDecimal amount;

    private String currency;

    private LocalDate valueDate;

    private LocalDate maturityDate;

    private BigDecimal interestRate;
}
package com.opentms.dealing.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class DealDTO {

    private Long id;

    private String code;

    private String name;

    private String status;

    private String keyword;

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
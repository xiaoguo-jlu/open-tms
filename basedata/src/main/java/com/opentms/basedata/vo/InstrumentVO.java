package com.opentms.basedata.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
public class InstrumentVO extends BasedataVO {

    private String instrumentCode;

    private String instrumentName;

    private String instrumentType;

    private String instrumentSubtype;

    private String enName;

    private String underlying;

    private String exchange;

    private String currency;

    private BigDecimal faceValue;

    private LocalDate issueDate;

    private LocalDate maturityDate;

    private BigDecimal interestRate;

    private Long counterpartyId;
}
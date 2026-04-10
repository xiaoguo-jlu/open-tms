package com.opentms.irs.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class IrsDealVO {

    private String dealCode;

    private String dealType;

    private BigDecimal notionalAmount;

    private String currency;

    private LocalDate startDate;

    private LocalDate endDate;

    private BigDecimal fixedRate;

    private String floatingRateType;

    private BigDecimal floatingRateSpread;

    private String paymentFrequency;

    private Long counterpartyId;

    private String counterpartyName;

    private Long accountId;

    private String accountName;

    private String status;
}
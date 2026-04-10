package com.opentms.valuation.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ValuationDTO {

    private Long instrumentId;

    private LocalDate valuationDate;

    private BigDecimal marketValue;

    private BigDecimal costValue;

    private BigDecimal unrealizedPnl;

    private String valuationMethod;
}
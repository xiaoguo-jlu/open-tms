package com.opentms.valuation.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ValuationVO {

    private Long instrumentId;

    private LocalDate valuationDate;

    private BigDecimal marketValue;

    private BigDecimal costValue;

    private BigDecimal unrealizedPnl;

    private String valuationMethod;
}
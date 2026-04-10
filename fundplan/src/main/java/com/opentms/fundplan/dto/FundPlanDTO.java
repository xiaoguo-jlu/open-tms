package com.opentms.fundplan.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class FundPlanDTO {

    private Long id;

    private Integer planYear;

    private Integer planMonth;

    private Long businessUnitId;

    private BigDecimal totalIncome;

    private BigDecimal totalExpense;

    private BigDecimal netCashFlow;

    private String planType;

    private String status;
}
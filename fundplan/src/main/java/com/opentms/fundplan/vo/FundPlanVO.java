package com.opentms.fundplan.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class FundPlanVO {

    private Long id;

    private Integer planYear;

    private Integer planMonth;

    private Long businessUnitId;

    private BigDecimal totalIncome;

    private BigDecimal totalExpense;

    private BigDecimal netCashFlow;

    private String planType;

    private String status;

    private String createdBy;

    private LocalDateTime createdAt;

    private String updatedBy;

    private LocalDateTime updatedAt;
}
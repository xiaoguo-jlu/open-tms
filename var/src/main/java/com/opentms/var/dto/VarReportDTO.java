package com.opentms.var.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class VarReportDTO {

    private Long id;

    private LocalDate reportDate;

    private String varType;

    private BigDecimal confidenceLevel;

    private Integer holdingPeriod;

    private BigDecimal totalVar;

    private BigDecimal fxVar;

    private BigDecimal irVar;

    private BigDecimal creditVar;

    private String status;
}
package com.opentms.var.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class VarReportVO {

    private Long id;

    private String code;

    private String name;

    private LocalDate reportDate;

    private String varType;

    private BigDecimal confidenceLevel;

    private Integer holdingPeriod;

    private BigDecimal totalVar;

    private BigDecimal fxVar;

    private BigDecimal irVar;

    private BigDecimal creditVar;

    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
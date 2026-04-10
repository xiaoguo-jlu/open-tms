package com.opentms.impairment.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ImpairmentVO {

    private Long id;

    private LocalDate assessmentDate;

    private Long businessUnitId;

    private String businessUnitName;

    private String assessmentType;

    private BigDecimal totalExposure;

    private BigDecimal expectedLoss;

    private BigDecimal provisionRate;

    private String status;

    private String createdBy;

    private LocalDateTime createdAt;

    private String updatedBy;

    private LocalDateTime updatedAt;
}
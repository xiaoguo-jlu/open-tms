package com.opentms.impairment.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ImpairmentDTO {

    private Long id;

    private LocalDate assessmentDate;

    private Long businessUnitId;

    private String assessmentType;

    private BigDecimal totalExposure;

    private BigDecimal expectedLoss;

    private BigDecimal provisionRate;

    private String status;
}
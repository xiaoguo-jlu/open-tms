package com.opentms.hedge.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class HedgeRelationDTO {

    private Long id;

    private String code;

    private String name;

    private String hedgeName;

    private String hedgeType;

    private Long hedgeInstrumentId;

    private Long hedgedItemId;

    private BigDecimal hedgeRatio;

    private LocalDate startDate;

    private LocalDate endDate;

    private String status;
}
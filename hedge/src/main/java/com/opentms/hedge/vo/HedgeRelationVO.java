package com.opentms.hedge.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class HedgeRelationVO extends com.opentms.common.model.BaseVO {

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

    private String createdBy;

    private LocalDateTime createdAt;

    private String updatedBy;

    private LocalDateTime updatedAt;
}
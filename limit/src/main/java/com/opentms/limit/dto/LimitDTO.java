package com.opentms.limit.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
public class LimitDTO extends com.opentms.basedata.dto.BaseDTO {

    private String limitName;

    private String limitType;

    private Long businessUnitId;

    private BigDecimal limitAmount;

    private String currency;

    private BigDecimal warningThreshold;

    private BigDecimal controlThreshold;

    private LocalDate startDate;

    private LocalDate endDate;
}
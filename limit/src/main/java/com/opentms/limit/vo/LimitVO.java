package com.opentms.limit.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
public class LimitVO extends com.opentms.basedata.vo.BaseVO {

    private String limitName;

    private String limitType;

    private Long businessUnitId;

    private String businessUnitName;

    private BigDecimal limitAmount;

    private String currency;

    private BigDecimal warningThreshold;

    private BigDecimal controlThreshold;

    private LocalDate startDate;

    private LocalDate endDate;
}
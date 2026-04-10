package com.opentms.limit.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.opentms.common.model.BaseCodeEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("trm_limit_t")
public class Limit extends BaseCodeEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

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
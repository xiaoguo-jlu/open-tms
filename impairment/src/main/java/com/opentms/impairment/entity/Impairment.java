package com.opentms.impairment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.opentms.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("trm_impairment_t")
public class Impairment extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private LocalDate assessmentDate;

    private Long businessUnitId;

    private String assessmentType;

    private BigDecimal totalExposure;

    private BigDecimal expectedLoss;

    private BigDecimal provisionRate;

    private String status;
}
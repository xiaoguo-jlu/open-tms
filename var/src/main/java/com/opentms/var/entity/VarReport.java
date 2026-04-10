package com.opentms.var.entity;

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
@TableName("trm_var_report_t")
public class VarReport extends BaseCodeEntity {

    @TableId(type = IdType.AUTO)
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
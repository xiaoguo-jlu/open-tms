package com.opentms.var.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@TableName("tms_var_report_t")
public class VarReport {

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

    private String remark;

    private String createdBy;

    private java.time.LocalDateTime createdAt;

    private String updatedBy;

    private java.time.LocalDateTime updatedAt;

    private Integer version;

    private String deleted;
}
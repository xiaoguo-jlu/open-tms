package com.opentms.fundplan.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@TableName("tms_fund_plan_t")
public class FundPlan {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String planNo;

    private String planName;

    private String planType;

    private Long businessUnitId;

    private String currency;

    private BigDecimal planAmount;

    private LocalDate startDate;

    private LocalDate endDate;

    private String periodType;

    private Integer versionNo;

    private String status;

    private String remark;

    private String createdBy;

    private java.time.LocalDateTime createdAt;

    private String updatedBy;

    private java.time.LocalDateTime updatedAt;

    private Integer version;

    private String deleted;
}
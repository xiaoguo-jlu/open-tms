package com.opentms.limit.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("tms_limit_t")
public class Limit {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String limitNo;

    private String limitName;

    private String limitType;

    private Long businessUnitId;

    private String currency;

    private BigDecimal limitAmount;

    private BigDecimal usedAmount;

    private BigDecimal warningPercent;

    private String status;

    private String remark;

    private String createdBy;

    private java.time.LocalDateTime createdAt;

    private String updatedBy;

    private java.time.LocalDateTime updatedAt;

    private Integer version;

    private String deleted;
}
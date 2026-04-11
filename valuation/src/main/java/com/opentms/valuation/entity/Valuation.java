package com.opentms.valuation.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@TableName("tms_valuation_t")
public class Valuation {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long instId;

    private LocalDate valuationDate;

    private BigDecimal marketValue;

    private BigDecimal costValue;

    private BigDecimal unrealizedPl;

    private String valuationMethod;

    private BigDecimal discountRate;

    private String status;

    private String remark;

    private String createdBy;

    private java.time.LocalDateTime createdAt;

    private String updatedBy;

    private java.time.LocalDateTime updatedAt;

    private Integer version;

    private String deleted;
}
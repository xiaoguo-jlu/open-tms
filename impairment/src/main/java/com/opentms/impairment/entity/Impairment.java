package com.opentms.impairment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@TableName("tms_impairment_t")
public class Impairment {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long instId;

    private LocalDate impairmentDate;

    private BigDecimal ead;

    private BigDecimal probabilityOfDefault;

    private BigDecimal lossGivenDefault;

    private BigDecimal expectedLoss;

    private Integer stage;

    private String modelUsed;

    private String status;

    private String remark;

    private String createdBy;

    private java.time.LocalDateTime createdAt;

    private String updatedBy;

    private java.time.LocalDateTime updatedAt;

    private Integer version;

    private String deleted;
}
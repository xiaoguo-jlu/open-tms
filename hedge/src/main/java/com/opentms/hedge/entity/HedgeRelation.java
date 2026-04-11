package com.opentms.hedge.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("tms_hedge_relation_t")
public class HedgeRelation {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String hedgeNo;

    private String hedgeType;

    private Long exposureId;

    private Long instrumentId;

    private BigDecimal hedgeRatio;

    private BigDecimal hedgeAmount;

    private BigDecimal hedgeEffectiveness;

    private String status;

    private String remark;

    private String createdBy;

    private java.time.LocalDateTime createdAt;

    private String updatedBy;

    private java.time.LocalDateTime updatedAt;

    private Integer version;

    private String deleted;
}
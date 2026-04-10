package com.opentms.hedge.entity;

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
@TableName("trm_hedge_relation_t")
public class HedgeRelation extends BaseCodeEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String hedgeName;

    private String hedgeType;

    private Long hedgeInstrumentId;

    private Long hedgedItemId;

    private BigDecimal hedgeRatio;

    private LocalDate startDate;

    private LocalDate endDate;

    private String status;
}
package com.opentms.valuation.entity;

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
@TableName("trm_valuation_t")
public class Valuation extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long instrumentId;

    private LocalDate valuationDate;

    private BigDecimal marketValue;

    private BigDecimal costValue;

    private BigDecimal unrealizedPnl;

    private String valuationMethod;
}
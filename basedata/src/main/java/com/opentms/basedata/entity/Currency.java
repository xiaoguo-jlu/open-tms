package com.opentms.basedata.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.opentms.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("trm_currency_t")
public class Currency extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String currencyCode;

    private String currencyName;

    private String currencySymbol;

    private Integer decimalPlaces;

    private String status;
}

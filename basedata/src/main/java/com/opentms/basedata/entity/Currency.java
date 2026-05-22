package com.opentms.basedata.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tms_currency_t")
public class Currency extends BasedataEntity {

    private String symbol;

    private Integer decimalPlaces;
}
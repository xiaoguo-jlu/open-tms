package com.opentms.basedata.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CurrencyVO extends BasedataVO {

    private String symbol;

    private Integer decimalPlaces;
}
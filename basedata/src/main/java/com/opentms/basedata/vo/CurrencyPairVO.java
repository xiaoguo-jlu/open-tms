package com.opentms.basedata.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CurrencyPairVO extends BasedataVO {

    private String pairCode;

    private String currency1;

    private String currency2;

    private String strongerCurrency;

    private Integer bidDecimal;

    private Integer askDecimal;
}

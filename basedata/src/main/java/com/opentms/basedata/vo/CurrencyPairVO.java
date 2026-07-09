package com.opentms.basedata.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CurrencyPairVO extends BasedataVO {

    private String pairCode;

    private String baseCurrency;

    private String quoteCurrency;

    private Integer bidDecimal;

    private Integer askDecimal;
}

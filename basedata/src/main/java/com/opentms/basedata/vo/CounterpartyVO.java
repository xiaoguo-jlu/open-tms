package com.opentms.basedata.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CounterpartyVO extends BasedataVO {

    private String enName;

    private String counterpartyType;

    private String countryCode;

    private String swiftCode;

    private String internalRating;

    private String externalRating;

    private String phone;

    private String address;
}

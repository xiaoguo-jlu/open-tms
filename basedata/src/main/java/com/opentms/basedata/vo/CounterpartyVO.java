package com.opentms.basedata.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CounterpartyVO extends BaseVO {

    private String enName;

    private String type;

    private String countryCode;

    private String creditRating;

    private String extRating;

    private String address;

    private String phone;
}

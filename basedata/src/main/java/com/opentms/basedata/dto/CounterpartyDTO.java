package com.opentms.basedata.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CounterpartyDTO extends BaseDTO {

    private String enName;

    private String type;

    private String countryCode;

    private String creditRating;

    private String extRating;

    private String address;

    private String phone;
}

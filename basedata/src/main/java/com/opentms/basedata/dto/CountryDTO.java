package com.opentms.basedata.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CountryDTO extends BaseDTO {

    private String enName;

    private String timezone;

    private String areaCode;
}

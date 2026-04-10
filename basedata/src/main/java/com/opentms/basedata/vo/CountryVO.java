package com.opentms.basedata.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CountryVO extends BaseVO {

    private String enName;

    private String timezone;

    private String areaCode;
}

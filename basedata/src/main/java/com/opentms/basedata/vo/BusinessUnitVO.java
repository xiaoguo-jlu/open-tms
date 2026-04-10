package com.opentms.basedata.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class BusinessUnitVO extends BaseVO {

    private String enName;

    private String legalPerson;

    private String address;

    private String taxNo;
}

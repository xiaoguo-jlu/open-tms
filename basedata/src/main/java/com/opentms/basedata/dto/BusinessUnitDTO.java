package com.opentms.basedata.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class BusinessUnitDTO extends BaseDTO {

    private String enName;

    private String legalPerson;

    private String address;

    private String taxNo;
}

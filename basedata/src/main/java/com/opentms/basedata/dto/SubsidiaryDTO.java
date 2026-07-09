package com.opentms.basedata.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 子公司DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SubsidiaryDTO extends BasedataDTO {

    private String enName;

    private String parentCode;

    private String businessUnitCode;

    private String legalPerson;

    private String registrationNo;

    private String taxNo;

    private String address;

    private String phone;

    private String email;
}
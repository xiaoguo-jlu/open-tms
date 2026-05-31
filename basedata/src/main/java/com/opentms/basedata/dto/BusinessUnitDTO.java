package com.opentms.basedata.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class BusinessUnitDTO extends BaseDTO {

    private String enName;

    private String entityType;

    private String parentCode;

    private Integer levelDepth;

    private String hierarchyPath;

    private String legalPerson;

    private String registeredAddress;

    private String officeAddress;

    private String unifiedSocialCreditCode;

    private String businessLicenseNo;

    private java.time.LocalDate establishmentDate;

    private String taxNo;
}

package com.opentms.basedata.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class BusinessUnitVO extends BaseVO {

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

    private List<BusinessUnitVO> children;
}

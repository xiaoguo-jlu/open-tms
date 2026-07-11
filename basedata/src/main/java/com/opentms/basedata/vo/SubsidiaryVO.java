package com.opentms.basedata.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 子公司VO
 */
@Data
public class SubsidiaryVO {

    private Long id;

    private String code;

    private String name;

    private String enName;

    private String parentCode;

    private String managementEntityCode;

    private String legalPerson;

    private String registrationNo;

    private String taxNo;

    private String address;

    private String phone;

    private String email;

    private String status;

    private String remark;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
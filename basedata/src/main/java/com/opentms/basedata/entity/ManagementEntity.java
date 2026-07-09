package com.opentms.basedata.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDate;

@Data
@TableName("tms_business_unit_t")
public class ManagementEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String code;

    private String name;

    @TableField("en_name")
    private String enName;

    @TableField("entity_type")
    private String entityType;

    @TableField("parent_code")
    private String parentCode;

    @TableField("level_depth")
    private Integer levelDepth;

    @TableField("hierarchy_path")
    private String hierarchyPath;

    private String legalPerson;

    @TableField("registered_address")
    private String registeredAddress;

    @TableField("office_address")
    private String officeAddress;

    @TableField("unified_social_credit_code")
    private String unifiedSocialCreditCode;

    @TableField("business_license_no")
    private String businessLicenseNo;

    @TableField("establishment_date")
    private LocalDate establishmentDate;

    private String taxNo;

    private String status;

    private String createdBy;

    private java.time.LocalDateTime createdAt;

    private String updatedBy;

    private java.time.LocalDateTime updatedAt;

    private Integer version;

    private String deleted;
}
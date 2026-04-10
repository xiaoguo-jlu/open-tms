package com.opentms.basedata.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.opentms.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("trm_business_unit_t")
public class BusinessUnit extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String unitCode;

    private String unitName;

    private String unitNameEn;

    private String legalRepresentative;

    private String registeredAddress;

    private String taxNumber;

    private String status;
}
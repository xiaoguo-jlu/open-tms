package com.opentms.basedata.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.opentms.common.model.BaseCodeEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("trm_business_unit_t")
public class BusinessUnit extends BaseCodeEntity {

    private String enName;

    private String legalPerson;

    private String address;

    private String taxNo;
}

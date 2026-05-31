package com.opentms.basedata.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 子公司表实体
 * 表名: tms_subsidiary_t
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tms_subsidiary_t")
public class Subsidiary extends BasedataEntity {

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
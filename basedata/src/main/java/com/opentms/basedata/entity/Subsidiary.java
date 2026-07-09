package com.opentms.basedata.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 子公司表实体
 * 表名: tms_subsidiary_t
 * 字段对齐:tms_subsidiary_t.business_unit_code(2026-07-09 修复)
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tms_subsidiary_t")
public class Subsidiary extends BasedataEntity {

    private String enName;

    private String parentCode;

    @TableField("business_unit_code")
    private String businessUnitCode;

    private String legalPerson;

    private String registrationNo;

    private String taxNo;

    private String address;

    private String phone;

    private String email;
}
package com.opentms.basedata.entity;

import com.baomidou.mybatisplus.annotation.TableField;
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

    /**
     * 归属管理主体 ID(tms_subsidiary_t.management_entity_id)
     */
    @TableField("management_entity_id")
    private String managementEntityCode;

    private String legalPerson;

    private String registrationNo;

    private String taxNo;

    private String address;

    private String phone;

    private String email;
}
package com.opentms.basedata.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.opentms.common.model.BaseCodeEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("trm_bank_t")
public class Bank extends BaseCodeEntity {

    @TableField("en_name")
    private String enName;

    @TableField("swift_code")
    private String swiftCode;

    @TableField("bank_no")
    private String bankNo;

    @TableField("country_code")
    private String countryCode;

    @TableField("bank_type")
    private String bankType;
}

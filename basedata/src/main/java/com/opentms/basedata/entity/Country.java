package com.opentms.basedata.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tms_country_t")
public class Country extends BasedataEntity {

    private String enName;

    private String timezone;

    @TableField("country_no")
    private String areaCode;
}

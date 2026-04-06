package com.opentms.basedata.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.opentms.common.model.BaseCodeEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("trm_country_t")
public class Country extends BaseCodeEntity {

    private String enName;

    private String timezone;

    private String areaCode;
}

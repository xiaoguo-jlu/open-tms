package com.opentms.basedata.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.opentms.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("trm_country_t")
public class Country extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String countryCode;

    private String countryName;

    private String countryNameEn;

    private String timezone;

    private String countryCodePhone;

    private String status;
}
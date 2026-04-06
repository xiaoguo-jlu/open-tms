package com.opentms.basedata.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.opentms.common.model.BaseCodeEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("trm_counterparty_t")
public class Counterparty extends BaseCodeEntity {

    private String enName;

    private String type;

    private String countryCode;

    private String creditRating;

    private String extRating;

    private String address;

    private String phone;
}

package com.opentms.basedata.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.opentms.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("trm_counterparty_t")
public class Counterparty extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String counterpartyCode;

    private String counterpartyName;

    private String counterpartyNameEn;

    private String counterpartyType;

    private String countryCode;

    private String creditRating;

    private String externalRating;

    private String address;

    private String phone;

    private String status;
}
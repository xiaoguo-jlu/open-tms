package com.opentms.basedata.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.opentms.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("trm_bank_t")
public class Bank extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String bankCode;

    private String bankName;

    private String bankNameEn;

    private String swiftCode;

    private String bankLineCode;

    private String countryCode;

    private String bankType;

    private String status;
}
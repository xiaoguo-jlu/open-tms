package com.opentms.basedata.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.opentms.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("trm_trader_t")
public class Trader extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String traderCode;

    private String traderName;

    private String traderNameEn;

    private String department;

    private String phone;

    private String email;

    private String status;
}
package com.opentms.basedata.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.opentms.common.model.BaseCodeEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("trm_trader_t")
public class Trader extends BaseCodeEntity {

    private String enName;

    private String department;

    private String phone;

    private String email;
}

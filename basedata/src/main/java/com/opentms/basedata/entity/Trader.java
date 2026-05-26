package com.opentms.basedata.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tms_trader_t")
public class Trader extends BasedataEntity {

    private String enName;

    private String department;

    private String phone;

    private String email;
}

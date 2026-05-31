package com.opentms.basedata.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tms_counterparty_t")
public class Counterparty extends BasedataEntity {

    private String enName;

    private String counterpartyType;

    private String countryCode;

    private String swiftCode;

    private String internalRating;

    private String externalRating;

    private String phone;

    private String address;
}

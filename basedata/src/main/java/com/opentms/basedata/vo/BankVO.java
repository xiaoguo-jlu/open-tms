package com.opentms.basedata.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class BankVO extends BaseVO {

    private String enName;

    private String swiftCode;

    private String bankNo;

    private String countryCode;

    private String bankType;
}

package com.opentms.basedata.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class BankDTO extends BaseDTO {

    private String enName;

    private String swiftCode;

    private String bankNo;

    private String countryCode;

    private String bankType;
}

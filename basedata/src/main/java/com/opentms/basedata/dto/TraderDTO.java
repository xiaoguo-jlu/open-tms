package com.opentms.basedata.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TraderDTO extends BaseDTO {

    private String enName;

    private String department;

    private String phone;

    private String email;
}

package com.opentms.basedata.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TraderVO extends BasedataVO {

    private String enName;

    private String department;

    private String phone;

    private String email;
}

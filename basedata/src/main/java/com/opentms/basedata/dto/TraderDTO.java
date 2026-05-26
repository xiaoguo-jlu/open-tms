package com.opentms.basedata.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TraderDTO extends BasedataDTO {

    @Size(max = 50, message = "英文名长度不能超过50位")
    private String enName;

    @Size(max = 100, message = "部门长度不能超过100位")
    private String department;

    @Size(max = 30, message = "联系电话长度不能超过30位")
    private String phone;

    @Size(max = 100, message = "邮箱长度不能超过100位")
    private String email;
}

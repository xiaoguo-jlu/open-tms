package com.opentms.basedata.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CounterpartyDTO extends BasedataDTO {

    @Size(max = 200, message = "英文名长度不能超过200位")
    private String enName;

    @Size(max = 20, message = "对手类型长度不能超过20位")
    private String counterpartyType;

    @Size(max = 10, message = "国家代码长度不能超过10位")
    private String countryCode;

    @Size(max = 20, message = "Swift代码长度不能超过20位")
    private String swiftCode;
}

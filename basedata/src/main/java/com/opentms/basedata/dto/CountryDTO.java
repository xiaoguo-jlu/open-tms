package com.opentms.basedata.dto;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CountryDTO extends BasedataDTO {
    @Size(max = 100, message = "英文名长度不能超过100位")
    private String enName;
    @Size(max = 50, message = "时区长度不能超过50位")
    private String timezone;
    @Size(max = 10, message = "国家编号长度不能超过10位")
    private String countryNo;
}

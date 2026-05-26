package com.opentms.basedata.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDate;
@Data
public class HolidayDTO {
    private Long id;
    @NotNull(message = "日期不能为空")
    private LocalDate holidayDate;
    @NotBlank(message = "名称不能为空")
    @Size(max = 100, message = "名称长度不能超过100位")
    private String name;
    @NotBlank(message = "国家代码不能为空")
    @Size(max = 10, message = "国家代码长度不能超过10位")
    private String countryCode;
    private String isAdjacent;
    @Size(max = 500, message = "备注长度不能超过500位")
    private String remark;
}

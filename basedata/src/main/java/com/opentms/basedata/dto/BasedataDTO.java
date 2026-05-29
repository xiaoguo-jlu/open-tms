package com.opentms.basedata.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class BasedataDTO {

    private Long id;

    @NotBlank(message = "代码不能为空")
    @Size(max = 50, message = "代码长度不能超过50位")
    private String code;

    @NotBlank(message = "名称不能为空")
    @Size(max = 200, message = "名称长度不能超过200位")
    private String name;

    @Pattern(regexp = "^[01]$", message = "状态只能是0或1")
    private String status;

    @Size(max = 500, message = "备注长度不能超过500位")
    private String remark;

    private String keyword;
}
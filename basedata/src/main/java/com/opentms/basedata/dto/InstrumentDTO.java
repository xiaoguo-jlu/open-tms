package com.opentms.basedata.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class InstrumentDTO extends BasedataDTO {

    private Long id;

    @NotBlank(message = "工具编码不能为空")
    @Size(max = 50, message = "工具编码长度不能超过50位")
    private String instrumentCode;

    @NotBlank(message = "工具名称不能为空")
    @Size(max = 200, message = "工具名称长度不能超过200位")
    private String instrumentName;

    @NotBlank(message = "工具类型不能为空")
    @Size(max = 20, message = "工具类型长度不能超过20位")
    private String instrumentType;

    @Size(max = 20, message = "子类型长度不能超过20位")
    private String instrumentSubtype;

    @Size(max = 200, message = "英文名称长度不能超过200位")
    private String enName;

    @Size(max = 50, message = "标的资产长度不能超过50位")
    private String underlying;

    @Size(max = 50, message = "交易所长度不能超过50位")
    private String exchange;

    @Size(max = 10, message = "币种长度不能超过10位")
    private String currency;

    private java.math.BigDecimal faceValue;

    private java.time.LocalDate issueDate;

    private java.time.LocalDate maturityDate;

    private java.math.BigDecimal interestRate;

    private Long counterpartyId;
}
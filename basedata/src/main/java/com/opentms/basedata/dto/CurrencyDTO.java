package com.opentms.basedata.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CurrencyDTO extends BasedataDTO {

    @Size(max = 10, message = "货币符号长度不能超过10位")
    private String symbol;

    @Min(value = 0, message = "小数位数最小值为0")
    @Max(value = 6, message = "小数位数最大值为6")
    private Integer decimalPlaces;
}
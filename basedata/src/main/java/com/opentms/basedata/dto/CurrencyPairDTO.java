package com.opentms.basedata.dto;

import jakarta.validation.constraints.Min;
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
public class CurrencyPairDTO extends BasedataDTO {

    private Long id;

    @NotBlank(message = "货币对编码不能为空")
    @Size(max = 20, message = "货币对编码长度不能超过20位")
    private String pairCode;

    @NotBlank(message = "基础货币不能为空")
    @Size(max = 10, message = "基础货币长度不能超过10位")
    private String baseCurrency;

    @NotBlank(message = "报价货币不能为空")
    @Size(max = 10, message = "报价货币长度不能超过10位")
    private String quoteCurrency;

    @Min(value = 0, message = "买方小数位最小值为0")
    private Integer bidDecimal;

    @Min(value = 0, message = "卖方小数位最小值为0")
    private Integer askDecimal;
}

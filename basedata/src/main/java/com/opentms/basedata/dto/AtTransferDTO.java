package com.opentms.basedata.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class AtTransferDTO {

    private Long id;

    private String transferNo;

    @NotNull(message = "转账日期不能为空")
    private LocalDate transferDate;

    @NotBlank(message = "业务单元不能为空")
    @Size(max = 50, message = "业务单元长度不能超过50位")
    private String businessUnit;

    @NotBlank(message = "付款账户不能为空")
    @Size(max = 50, message = "付款账户长度不能超过50位")
    private String fromAccount;

    @NotBlank(message = "收款账户不能为空")
    @Size(max = 50, message = "收款账户长度不能超过50位")
    private String toAccount;

    @NotNull(message = "转账金额不能为空")
    private BigDecimal amount;

    @NotBlank(message = "币种不能为空")
    @Size(max = 10, message = "币种长度不能超过10位")
    private String currency;

    @NotNull(message = "预计到账日期不能为空")
    private LocalDate expectedDate;

    @NotBlank(message = "支付方式不能为空")
    @Size(max = 20, message = "支付方式长度不能超过20位")
    private String paymentMethod;

    @Size(max = 200, message = "转账原因长度不能超过200位")
    private String transferReason;

    @NotBlank(message = "转账类型不能为空")
    @Size(max = 20, message = "转账类型长度不能超过20位")
    private String transferType;

    @NotBlank(message = "是否需要授权不能为空")
    @Pattern(regexp = "^[01]$", message = "是否需要授权只能是0或1")
    private String needAuthorization;

    @NotBlank(message = "申请人不能为空")
    @Size(max = 50, message = "申请人长度不能超过50位")
    private String applicant;

    @Size(max = 500, message = "备注长度不能超过500位")
    private String remark;

    private String keyword;
}

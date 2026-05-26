package com.opentms.basedata.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class AcCashflowDTO {

    private Long id;

    @NotBlank(message = "业务单元不能为空")
    @Size(max = 50, message = "业务单元长度不能超过50位")
    private String businessUnit;

    @NotBlank(message = "银行账号不能为空")
    @Size(max = 50, message = "银行账号长度不能超过50位")
    private String bankAccount;

    @Size(max = 50, message = "对手账户长度不能超过50位")
    private String counterpartyAccount;

    @NotBlank(message = "方向不能为空")
    @Size(max = 10, message = "方向长度不能超过10位")
    private String direction;

    @Positive(message = "金额必须大于0")
    private BigDecimal amount;

    @NotBlank(message = "币种不能为空")
    @Size(max = 10, message = "币种长度不能超过10位")
    private String currency;

    private LocalDate cashflowDate;

    private LocalDate valueDate;

    @NotBlank(message = "来源类型不能为空")
    @Size(max = 20, message = "来源类型长度不能超过20位")
    private String sourceType;

    @NotBlank(message = "来源编号不能为空")
    @Size(max = 50, message = "来源编号长度不能超过50位")
    private String sourceRef;

    @Size(max = 20, message = "子类型长度不能超过20位")
    private String subType;

    @Size(max = 50, message = "银行参考号长度不能超过50位")
    private String bankRef;

    @Size(max = 50, message = "对账单号长度不能超过50位")
    private String statementNo;

    private String status;

    @Size(max = 200, message = "对手方名称长度不能超过200位")
    private String counterpartyName;

    @Size(max = 500, message = "摘要长度不能超过500位")
    private String purpose;

    private String keyword;
}

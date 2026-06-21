package com.opentms.dealing.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * AT 交易前端请求 DTO
 * 用于接收 AT 交易创建/更新的请求数据
 */
@Data
public class AtDealDTO {

    /** 更新时需要（用于定位交易） */
    private Long id;

    /** 关联交易编号（更新时需要） */
    private String dealNumber;

    /** 转账类型：SAME_COMPANY / CROSS_COMPANY / CROSS_BORDER */
    private String transferType;

    private String businessUnit;

    /** 付出方银行账户 ID */
    private Long sourceAccountId;

    /** 收入方银行账户 ID */
    private Long destAccountId;

    private BigDecimal sourceAmount;
    private BigDecimal destAmount;

    private String sourceCurrency;
    private String destCurrency;

    /** 跨币种时记录汇率；同币种为 1 */
    private BigDecimal exchangeRate;

    private LocalDate valueDate;

    /** INTERNAL / SWIFT / RTGS */
    private String paymentMethod;

    private String purpose;

    /** 操作人（用于审计） */
    private String operator;

    /** 操作备注 */
    private String remark;
}

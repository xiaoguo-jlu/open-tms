package com.opentms.dealing.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * AT 交易镜像 VO
 * 用于展示 UPDATE / DELETE 时的字段快照
 */
@Data
public class AtDealImageVO {

    private Long id;

    private String imageNumber;

    private String dealNumber;

    private Integer version;

    private String transferType;

    private Long sourceAccountId;
    private Long destAccountId;

    private BigDecimal sourceAmount;
    private BigDecimal destAmount;

    private String sourceCurrency;
    private String destCurrency;

    private BigDecimal exchangeRate;

    private String businessUnit;

    private LocalDate valueDate;

    private String paymentMethod;

    private String purpose;

    private String status;

    private String latestActionNumber;

    /** UPDATE / DELETE（AT 中不会生成 CREATE 类型） */
    private String imageType;

    private String operator;

    private LocalDateTime operateAt;

    private LocalDateTime createdAt;
}

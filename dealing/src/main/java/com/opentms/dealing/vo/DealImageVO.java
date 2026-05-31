package com.opentms.dealing.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class DealImageVO {

    private Long id;

    private String imageNumber;

    private String dealNumber;

    private String dealType;

    private Integer version;

    private String businessUnit;

    private Long counterpartyId;

    private Long instrumentId;

    private Long traderId;

    private String direction;

    private BigDecimal amount;

    private String currency;

    private LocalDate dealDate;

    private LocalDate valueDate;

    private String status;

    private String description;

    private String remark;

    private String latestActionNumber;

    private String imageType;

    private String operator;

    private LocalDateTime operateAt;

    private String createdBy;

    private LocalDateTime createdAt;

    // AC交易个性化镜像字段
    private Long bankAccountId;

    private Long counterpartyAccountId;

    private String paymentMethod;
}
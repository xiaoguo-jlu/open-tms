package com.opentms.dealing.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class DealVO {

    private Long id;

    private String dealNumber;

    private String dealType;

    private String managementEntity;

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

    private String createdBy;

    private LocalDateTime createdAt;

    private String updatedBy;

    private LocalDateTime updatedAt;

    private Integer version;

    // AC交易个性化字段
    private Long bankAccountId;

    private Long counterpartyAccountId;

    private String paymentMethod;
}
package com.opentms.dealing.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class DealDTO {

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

    // AC交易个性化字段
    private Long bankAccountId;

    private Long counterpartyAccountId;

    private String paymentMethod;

    private String operator;

    private String remark2;
}
package com.opentms.fx.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class FxDealVO {

    private Long id;

    private String code;

    private String name;

    private String fxType;

    private String buyCurrency;

    private String sellCurrency;

    private BigDecimal amount;

    private BigDecimal rate;

    private Long counterpartyId;

    private String counterpartyName;

    private Long accountId;

    private String accountName;

    private LocalDate valueDate;

    private String status;

    private String remark;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
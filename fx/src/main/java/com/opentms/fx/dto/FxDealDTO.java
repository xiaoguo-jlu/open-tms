package com.opentms.fx.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class FxDealDTO {

    private Long id;

    private String code;

    private String name;

    private String fxType;

    private String buyCurrency;

    private String sellCurrency;

    private BigDecimal amount;

    private BigDecimal rate;

    private Long counterpartyId;

    private Long accountId;

    private LocalDate valueDate;

    private String status;

    private String remark;
}
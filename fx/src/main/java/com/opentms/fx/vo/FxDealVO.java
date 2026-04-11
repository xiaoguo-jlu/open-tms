package com.opentms.fx.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class FxDealVO {

    private Long id;

    private String dealNo;

    private String dealType;

    private String buyCurrency;

    private String sellCurrency;

    private BigDecimal buyAmount;

    private BigDecimal sellAmount;

    private BigDecimal rate;

    private Long counterpartyId;

    private LocalDate valueDate;

    private LocalDate maturityDate;

    private String status;

    private String remark;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
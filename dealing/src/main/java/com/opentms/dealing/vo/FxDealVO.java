package com.opentms.dealing.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * FX 交易列表 VO（v3.2 - 精简字段）
 */
@Data
public class FxDealVO {

    private Long id;

    private String dealNumber;

    private String dealType;

    private Long managementEntityId;

    private Long currencyPairId;

    private String sellCurrency;

    private BigDecimal sellAmount;

    private String buyCurrency;

    private BigDecimal buyAmount;

    private BigDecimal exchangeRate;

    private BigDecimal marketRate;

    private BigDecimal spreadBp;

    /** 产品类型（从 instrument 推算 SPOT/FWD/NDF），列表查询时填充 */
    private String productType;

    private LocalDate tradeDate;

    private LocalDate valueDate;

    private LocalDate maturityDate;

    private String status;

    private String description;

    private String remark;

    private LocalDateTime createdAt;
}
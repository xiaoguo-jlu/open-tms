package com.opentms.dealing.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class DealMapVO {

    private Long id;

    private String dealmapNumber;

    private String dealNumber;

    private String actionNumber;

    private String eventType;

    private String eventStatus;

    private BigDecimal amount;

    private String currency;

    private String direction;

    private LocalDate eventDate;

    private LocalDate valueDate;

    private String isReversal;

    private Long reversesEventId;

    private Long reversedByEventId;

    private String description;

    private String createdBy;

    private LocalDateTime createdAt;

    private String updatedBy;

    private LocalDateTime updatedAt;

    private Integer version;

    private String deleted;

    /** v3.2: DealMap 类型（FX_BUY_AMOUNT/FX_SELL_AMOUNT/FX_RATE/FX_FIX/AC/AT） */
    private String dealmapType;

    /** v3.2: DealMap 单字段值（金额或汇率） */
    private BigDecimal amountOrRate;
}

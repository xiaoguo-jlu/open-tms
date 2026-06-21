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
}

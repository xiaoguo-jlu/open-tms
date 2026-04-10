package com.opentms.settlement.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class SettlementVO {

    private Long id;

    private String settlementType;

    private Long accountId;

    private Long payeeId;

    private String payeeName;

    private String payeeBank;

    private String payeeAccountNo;

    private BigDecimal amount;

    private String currency;

    private String purpose;

    private LocalDate executeDate;

    private String status;

    private String createdBy;

    private LocalDateTime createdAt;

    private String updatedBy;

    private LocalDateTime updatedAt;
}
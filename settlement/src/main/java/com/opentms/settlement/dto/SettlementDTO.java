package com.opentms.settlement.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class SettlementDTO {

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
}
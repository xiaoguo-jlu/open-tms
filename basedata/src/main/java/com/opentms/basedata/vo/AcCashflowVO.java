package com.opentms.basedata.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class AcCashflowVO {

    private Long id;

    private String cashflowNo;

    private String businessUnit;

    private String bankAccount;

    private String counterpartyAccount;

    private String direction;

    private BigDecimal amount;

    private String currency;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate cashflowDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate valueDate;

    private String sourceType;

    private String sourceRef;

    private String subType;

    private String bankRef;

    private String statementNo;

    private String status;

    private String counterpartyName;

    private String purpose;

    private String createdBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    private String updatedBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    private Integer version;
}

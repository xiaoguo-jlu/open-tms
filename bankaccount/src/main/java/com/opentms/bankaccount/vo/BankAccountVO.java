package com.opentms.bankaccount.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class BankAccountVO {

    private Long id;

    private String code;

    private String name;

    private String status;

    private String accountNo;

    private String accountName;

    private Long bankId;

    private String bankName;

    private String accountType;

    private String accountTypeName;

    private String currency;

    private BigDecimal balance;

    private BigDecimal availableBalance;

    private BigDecimal frozenBalance;

    private String isMain;

    private String createdBy;

    private LocalDateTime createdAt;

    private String updatedBy;

    private LocalDateTime updatedAt;
}
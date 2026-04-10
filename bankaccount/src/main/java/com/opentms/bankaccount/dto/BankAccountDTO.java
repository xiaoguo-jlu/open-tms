package com.opentms.bankaccount.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class BankAccountDTO {

    private Long id;

    private String code;

    private String name;

    private String status;

    private String accountNo;

    private String accountName;

    private Long bankId;

    private String accountType;

    private String currency;

    private BigDecimal balance;

    private BigDecimal availableBalance;

    private BigDecimal frozenBalance;

    private String isMain;
}
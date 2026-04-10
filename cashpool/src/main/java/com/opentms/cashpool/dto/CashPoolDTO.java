package com.opentms.cashpool.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CashPoolDTO {

    private Long id;

    private String code;

    private String name;

    private String status;

    private String poolName;

    private String poolType;

    private Long businessUnitId;

    private Boolean autoTransfer;

    private BigDecimal thresholdAmount;

    private BigDecimal totalBalance;
}
package com.opentms.cashpool.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CashPoolVO {

    private Long id;

    private String code;

    private String name;

    private String status;

    private String poolName;

    private String poolType;

    private Long businessUnitId;

    private String businessUnitName;

    private Boolean autoTransfer;

    private BigDecimal thresholdAmount;

    private BigDecimal totalBalance;

    private String createdBy;

    private LocalDateTime createdAt;

    private String updatedBy;

    private LocalDateTime updatedAt;
}
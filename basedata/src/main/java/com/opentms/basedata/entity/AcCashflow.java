package com.opentms.basedata.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("tms_cashflow_t")
public class AcCashflow {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String cashflowNo;

    private String businessUnit;

    private String bankAccount;

    private String counterpartyAccount;

    private String direction;

    private BigDecimal amount;

    private String currency;

    private LocalDate cashflowDate;

    private LocalDate valueDate;

    private String sourceType;

    private String sourceRef;

    private String subType;

    private String bankRef;

    private String statementNo;

    private String status;

    private String counterpartyName;

    private String purpose;

    @TableField(fill = FieldFill.INSERT)
    private String createdBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updatedBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @Version
    private Integer version;

    @TableLogic
    private String deleted;
}

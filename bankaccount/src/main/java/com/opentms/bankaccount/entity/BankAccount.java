package com.opentms.bankaccount.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("tms_bank_account_t")
public class BankAccount {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String accountNo;

    private String accountName;

    private Long bankId;

    private String accountType;

    private String currency;

    private BigDecimal balance;

    private BigDecimal availableBalance;

    private BigDecimal frozenBalance;

    private String isMain;

    private Long businessUnitId;

    private String status;

    private String remark;

    private String createdBy;

    private java.time.LocalDateTime createdAt;

    private String updatedBy;

    private java.time.LocalDateTime updatedAt;

    private Integer version;

    private String deleted;
}
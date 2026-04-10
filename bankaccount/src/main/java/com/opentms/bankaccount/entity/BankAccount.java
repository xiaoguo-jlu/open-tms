package com.opentms.bankaccount.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.opentms.common.model.BaseCodeEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("trm_bank_account_t")
public class BankAccount extends BaseCodeEntity {

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
}
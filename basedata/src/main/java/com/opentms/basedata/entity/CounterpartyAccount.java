package com.opentms.basedata.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.opentms.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("trm_counterparty_account_t")
public class CounterpartyAccount extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String accountCode;

    private Long counterpartyId;

    private Long bankId;

    private String accountName;

    private String accountNumber;

    private String currencyCode;

    private String accountType;

    private String status;
}
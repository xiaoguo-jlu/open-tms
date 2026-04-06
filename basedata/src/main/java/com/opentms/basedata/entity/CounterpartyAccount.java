package com.opentms.basedata.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.opentms.common.model.BaseCodeEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("trm_counterparty_account_t")
public class CounterpartyAccount extends BaseCodeEntity {

    private Long counterpartyId;

    private Long bankId;

    private String accountName;

    private String accountNo;

    private String currency;

    private String accountType;
}

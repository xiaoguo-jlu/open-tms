package com.opentms.basedata.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CounterpartyAccountVO extends BaseVO {

    private Long counterpartyId;

    private String counterpartyName;

    private Long bankId;

    private String bankName;

    private String accountName;

    private String accountNo;

    private String currency;

    private String accountType;
}

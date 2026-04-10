package com.opentms.basedata.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CounterpartyAccountDTO extends BaseDTO {

    private Long counterpartyId;

    private Long bankId;

    private String accountName;

    private String accountNo;

    private String currency;

    private String accountType;
}

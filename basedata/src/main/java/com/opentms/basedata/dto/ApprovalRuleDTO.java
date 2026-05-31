package com.opentms.basedata.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ApprovalRuleDTO extends BasedataDTO {

    private String bizType;

    private java.math.BigDecimal amountLimit;

    private String currency;

    private Integer approvalLevel;

    private String approverType;

    private String approverExpr;
}
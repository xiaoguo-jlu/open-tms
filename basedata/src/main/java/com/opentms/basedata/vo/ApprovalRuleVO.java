package com.opentms.basedata.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ApprovalRuleVO extends BasedataVO {

    private String bizType;

    private java.math.BigDecimal amountLimit;

    private String currency;

    private Integer approvalLevel;

    private String approverType;

    private String approverExpr;
}
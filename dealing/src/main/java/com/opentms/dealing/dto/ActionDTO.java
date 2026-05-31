package com.opentms.dealing.dto;

import lombok.Data;

@Data
public class ActionDTO {

    private Long id;

    private String actionNumber;

    private String dealNumber;

    private String dealType;

    private String actionType;

    private String actionStatus;

    private String operator;

    private String remark;

    private String approver1;

    private String approver2;

    private String approvalStatus1;

    private String approvalStatus2;

    private String approvalRemark;
}
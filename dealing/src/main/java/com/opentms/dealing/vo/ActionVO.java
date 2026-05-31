package com.opentms.dealing.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ActionVO {

    private Long id;

    private String actionNumber;

    private String dealNumber;

    private String dealType;

    private String actionType;

    private String actionStatus;

    private String operator;

    private LocalDateTime operateAt;

    private String remark;

    private String approver1;

    private String approver2;

    private String approvalStatus1;

    private String approvalStatus2;

    private String approvalRemark;

    private String createdBy;

    private LocalDateTime createdAt;

    private String updatedBy;

    private LocalDateTime updatedAt;

    private Integer version;
}
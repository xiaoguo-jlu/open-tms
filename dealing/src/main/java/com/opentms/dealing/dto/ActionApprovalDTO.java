package com.opentms.dealing.dto;

import lombok.Data;

/**
 * Action 审批 / 驳回请求 DTO（v2.0 - 审批仅作用于 Action）
 */
@Data
public class ActionApprovalDTO {

    /** 审批人 */
    private String approver;

    /** 审批备注（驳回时必填） */
    private String approvalRemark;
}

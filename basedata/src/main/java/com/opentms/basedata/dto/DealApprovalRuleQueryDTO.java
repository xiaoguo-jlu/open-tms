package com.opentms.basedata.dto;

import lombok.Data;

/**
 * 交易审批规则分页查询 DTO
 *
 * @author Open-TMS
 * @since 2026-07-11
 */
@Data
public class DealApprovalRuleQueryDTO {

    private Long managementEntityId;
    private Long counterpartyId;
    private Long instrumentId;
    private Long dealerId;
    private String actionType;
    private String approvalLevel;
    private String status;
    private String keyword;

    private Integer pageNum = 1;
    private Integer pageSize = 20;
}
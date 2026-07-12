package com.opentms.basedata.dto;

import lombok.Data;

/**
 * match / test-match 接口入参 DTO
 *
 * <p>actionType 必填,其余维度 NULL=通配。</p>
 *
 * @author Open-TMS
 * @since 2026-07-11
 */
@Data
public class DealApprovalRuleMatchRequestDTO {

    private Long managementEntityId;
    private Long counterpartyId;
    private Long instrumentId;
    private Long dealerId;

    /** 必填:CREATE / SUBMIT / APPROVE / REJECT / EXECUTE */
    private String actionType;

    /** test-match 限定候选数(默认 50) */
    private Integer limit = 50;
}
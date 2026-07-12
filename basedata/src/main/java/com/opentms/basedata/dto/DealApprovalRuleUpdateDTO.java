package com.opentms.basedata.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 更新交易审批规则 DTO(★ 必填 lockToken)
 *
 * <p>5 维要素(主体除外,主体不可改) + 角色 + 优先级 + 状态 + 生效期。lockToken 不匹配 → 409 Conflict。</p>
 *
 * @author Open-TMS
 * @since 2026-07-11
 */
@Data
public class DealApprovalRuleUpdateDTO {

    private Long id;

    /** ★ 必填,从 detail 获取 */
    private String lockToken;

    private Long counterpartyId;

    private Long instrumentId;

    private Long dealerId;

    private String actionType;

    private String approvalLevel;

    private List<String> level1Roles;

    private List<String> level2Roles;

    private Integer priority;

    private LocalDate startDate;

    private LocalDate endDate;

    private String status;

    private String description;

    private String remark;

    private Integer version;
}
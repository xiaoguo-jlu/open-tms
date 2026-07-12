package com.opentms.basedata.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 新增交易审批规则 DTO
 *
 * <p>5 维要素 + approvalLevel + 角色列表 + 优先级 + 状态 + 生效期。</p>
 *
 * @author Open-TMS
 * @since 2026-07-11
 */
@Data
public class DealApprovalRuleSaveDTO {

    /** 交易主体 ID(NULL=通配) */
    private Long managementEntityId;

    /** 交易对手 ID(NULL=通配) */
    private Long counterpartyId;

    /** 金融工具 ID(NULL=通配) */
    private Long instrumentId;

    /** 交易员 ID(NULL=通配) */
    private Long dealerId;

    /** 操作类型:CREATE / SUBMIT / APPROVE / REJECT / EXECUTE */
    private String actionType;

    /** 审批层级:LEVEL_0 / LEVEL_1 / LEVEL_2 */
    private String approvalLevel;

    /** L1 角色列表 */
    private List<String> level1Roles;

    /** L2 角色列表 */
    private List<String> level2Roles;

    /** 优先级 0-9999 */
    private Integer priority;

    /** 状态:Active / Inactive */
    private String status;

    /** 生效开始日(NULL=立即) */
    private LocalDate startDate;

    /** 生效结束日(NULL=长期) */
    private LocalDate endDate;

    /** 业务说明 */
    private String description;

    /** 内部备注 */
    private String remark;
}
package com.opentms.basedata.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 交易审批规则主表 (Deal Approval Rule, DAR) — 基于 5 维要素 + actionType 灵活匹配
 *
 * <p>关键设计:
 * <ul>
 *   <li>5 维匹配:managementEntityId / counterpartyId / instrumentId / dealerId (可空=通配) + actionType (必填精确)</li>
 *   <li>approvalLevel: LEVEL_0(无需) / LEVEL_1(一层) / LEVEL_2(二层)</li>
 *   <li>JSONB 角色列表: level1Roles / level2Roles</li>
 *   <li>并发控制: lockToken / lockedBy / lockedAt (沿用 v1.1)</li>
 *   <li>Active 唯一约束 (5 维 + actionType + approvalLevel,NULLS NOT DISTINCT)</li>
 * </ul>
 *
 * @author Open-TMS
 * @since 2026-07-11
 */
@Data
@TableName("tms_deal_approval_rule_t")
public class DealApprovalRule {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 规则编号,如 DAR202607110001 */
    @TableField("rule_number")
    private String ruleNumber;

    /** 交易主体 ID(NULL=通配) */
    @TableField("management_entity_id")
    private Long managementEntityId;

    /** 交易对手 ID(NULL=通配) */
    @TableField("counterparty_id")
    private Long counterpartyId;

    /** 金融工具 ID(NULL=通配) */
    @TableField("instrument_id")
    private Long instrumentId;

    /** 交易员 ID(NULL=通配) */
    @TableField("dealer_id")
    private Long dealerId;

    /** 操作类型:CREATE / SUBMIT / APPROVE / REJECT / EXECUTE (沿用 GlobalConstants.ActionType) */
    @TableField("action_type")
    private String actionType;

    /** 审批层级:LEVEL_0 / LEVEL_1 / LEVEL_2 */
    @TableField("approval_level")
    private String approvalLevel;

    /** L1 角色列表(JSONB 数组) — 在 mapper 层使用 JSONB → List<String> 转换 */
    @TableField("level1_roles")
    private String level1Roles;

    /** L2 角色列表(JSONB 数组) */
    @TableField("level2_roles")
    private String level2Roles;

    /** 优先级 0-9999,数字越大越优先 */
    private Integer priority;

    /** 状态:Active / Inactive */
    private String status;

    /** 生效开始日(NULL=立即) */
    @TableField("start_date")
    private LocalDate startDate;

    /** 生效结束日(NULL=长期) */
    @TableField("end_date")
    private LocalDate endDate;

    /** 业务说明 */
    private String description;

    /** 内部备注 */
    private String remark;

    /** 乐观锁 token(UUID) */
    @TableField("lock_token")
    private String lockToken;

    /** 锁定人 */
    @TableField("locked_by")
    private String lockedBy;

    /** 锁定时间 */
    @TableField("locked_at")
    private LocalDateTime lockedAt;

    // ============= 审计字段 =============

    @TableField("created_by")
    private String createdBy;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_by")
    private String updatedBy;

    @TableField("updated_at")
    private LocalDateTime updatedAt;

    @Version
    private Integer version;

    @TableLogic
    private String deleted;

    // ============= 关联展示字段(exist=false) =============

    @TableField(exist = false)
    private String managementEntityName;

    @TableField(exist = false)
    private String counterpartyName;

    @TableField(exist = false)
    private String instrumentName;

    @TableField(exist = false)
    private String dealerName;

    @TableField(exist = false)
    private String actionTypeLabel;
}
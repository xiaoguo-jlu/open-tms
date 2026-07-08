package com.opentms.basedata.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 规则变更审计日志(v1.1 新增)
 *
 * <p>记录规则的 CREATE/UPDATE/DELETE/ENABLE/DISABLE 操作,old_value/new_value 为 JSONB 快照
 *
 * @author Open-TMS
 * @since 2026-07-08
 */
@Data
@TableName("tms_rule_audit_log_t")
public class RuleAuditLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联规则 ID */
    @TableField("rule_id")
    private Long ruleId;

    /** 操作类型:CREATE/UPDATE/DELETE/ENABLE/DISABLE */
    private String operation;

    /** 变更前完整字段 JSONB 快照 */
    @TableField(value = "old_value", insertStrategy = FieldStrategy.NOT_NULL, updateStrategy = FieldStrategy.NOT_NULL)
    private String oldValue;

    /** 变更后完整字段 JSONB 快照 */
    @TableField(value = "new_value", insertStrategy = FieldStrategy.NOT_NULL, updateStrategy = FieldStrategy.NOT_NULL)
    private String newValue;

    /** 操作人 */
    private String operator;

    /** 操作时间 */
    @TableField("operated_at")
    private LocalDateTime operatedAt;

    /** 备注 */
    private String remark;

    /** 关联展示字段(exist=false) */
    @TableField(exist = false)
    private String operatorName;
}
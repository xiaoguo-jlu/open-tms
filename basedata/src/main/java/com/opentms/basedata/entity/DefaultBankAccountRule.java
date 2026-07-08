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
 * 主体默认银行账户规则(v1.1)
 *
 * <p>关键设计:
 * <ul>
 *   <li>5 维匹配:managementEntityId / counterpartyId(可空=ALL) / instrumentId(可空=ALL) / direction / currency(可空=ALL)</li>
 *   <li>并发控制:lockToken / lockedBy / lockedAt(v1.1 新增)</li>
 *   <li>priority 范围 0-9999(v1.1)</li>
 *   <li>Active 唯一约束(由 DB 部分索引 uniq_dbar_active_dims 保证)</li>
 * </ul>
 *
 * @author Open-TMS
 * @since 2026-07-08
 */
@Data
@TableName("tms_default_bank_account_rule_t")
public class DefaultBankAccountRule {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 规则编号,如 RULE202607080001 */
    @TableField("rule_number")
    private String ruleNumber;

    /** 主体(必填,不能 ALL) */
    @TableField("management_entity_id")
    private Long managementEntityId;

    /** 对手方(可空=ALL 通配) */
    @TableField("counterparty_id")
    private Long counterpartyId;

    /** 金融产品(可空=ALL 通配) */
    @TableField("instrument_id")
    private Long instrumentId;

    /** 方向:Inflow/Outflow/ALL */
    private String direction;

    /** 币种(可空=ALL 通配,v1.1 允许 NULL) */
    private String currency;

    /** 默认银行账户 */
    @TableField("bank_account_id")
    private Long bankAccountId;

    /** 状态:Active/Inactive */
    private String status;

    /** 优先级,数字越大越优先,范围 0-9999 */
    private Integer priority;

    /** 开始生效日(可空=立即生效) */
    @TableField("start_date")
    private LocalDate startDate;

    /** 业务说明 */
    private String description;

    /** 备注 */
    private String remark;

    /** ★ v1.1 乐观锁 token(UUID),编辑时生成,提交时校验 */
    @TableField("lock_token")
    private String lockToken;

    /** ★ v1.1 锁定人 */
    @TableField("locked_by")
    private String lockedBy;

    /** ★ v1.1 锁定时间 */
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

    // ============= 关联展示字段(exist=false,用于前端 VO) =============

    @TableField(exist = false)
    private String managementEntityName;

    @TableField(exist = false)
    private String counterpartyName;

    @TableField(exist = false)
    private String instrumentName;

    @TableField(exist = false)
    private String bankAccountName;
}
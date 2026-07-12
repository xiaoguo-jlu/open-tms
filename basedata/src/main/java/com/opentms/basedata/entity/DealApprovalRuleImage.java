package com.opentms.basedata.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 交易审批规则镜像表 (append-only)
 *
 * <p>记录规则的 CREATE / UPDATE / DELETE / ENABLE / DISABLE 操作的完整字段快照。
 * 用于审计历史回溯 (Audit History 视图数据源)。</p>
 *
 * @author Open-TMS
 * @since 2026-07-11
 */
@Data
@TableName("tms_deal_approval_rule_image_t")
public class DealApprovalRuleImage {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 镜像编号(全局唯一,如 IMG-DAR202607110001-V3) */
    @TableField("image_number")
    private String imageNumber;

    /** 原规则编号 */
    @TableField("rule_number")
    private String ruleNumber;

    /** 原规则 ID */
    @TableField("rule_id")
    private Long ruleId;

    /** 镜像版本号 */
    private Integer version;

    /** 全字段 JSONB 快照 */
    @TableField("snapshot_json")
    private String snapshotJson;

    /** 镜像类型:CREATE / UPDATE / DELETE / ENABLE / DISABLE */
    @TableField("image_type")
    private String imageType;

    /** 操作人 */
    private String operator;

    /** 操作时间 */
    @TableField("operate_at")
    private LocalDateTime operateAt;

    /** 备注 */
    private String remark;

    @TableField("created_by")
    private String createdBy;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableLogic
    private String deleted;
}
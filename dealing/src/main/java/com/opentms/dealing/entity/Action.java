package com.opentms.dealing.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tms_actions_t")
public class Action {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("action_number")
    private String actionNumber;

    @TableField("deal_number")
    private String dealNumber;

    @TableField("deal_type")
    private String dealType;

    @TableField("action_type")
    private String actionType;

    @TableField("action_status")
    private String actionStatus;

    private String operator;

    @TableField("operate_at")
    private LocalDateTime operateAt;

    private String remark;

    private String approver1;

    private String approver2;

    @TableField("approval_status1")
    private String approvalStatus1;

    @TableField("approval_status2")
    private String approvalStatus2;

    @TableField("approval_remark")
    private String approvalRemark;

    private String createdBy;

    private LocalDateTime createdAt;

    private String updatedBy;

    private LocalDateTime updatedAt;

    private Integer version;

    private String deleted;
}
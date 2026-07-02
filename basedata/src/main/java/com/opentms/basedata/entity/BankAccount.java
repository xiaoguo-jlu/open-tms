package com.opentms.basedata.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("tms_bank_account_t")
public class BankAccount {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("account_no")
    private String accountNo;

    @TableField("account_name")
    private String accountName;

    @TableField("bank_id")
    private Long bankId;

    private String currency;

    @TableField("account_type")
    private String accountType;

    @TableField("business_unit_id")
    private Long businessUnitId;

    private String status;

    private String remark;

    // ===== 2026-06-29 扩展:银行账户业务字段(原 bankaccount 模块) =====

    /**
     * 账户别名
     */
    private String account;

    /**
     * 账户性质:Internal(内部)/ External(外部)
     */
    @TableField("account_nature")
    private String accountNature;

    /**
     * 是否归集:0=否 1=是
     */
    @TableField("is_collected")
    private String isCollected;

    /**
     * 归集方向:Up(上拨)/ Down(下拨)
     */
    @TableField("collect_direction")
    private String collectDirection;

    /**
     * 主账户ID(归集时关联)
     */
    @TableField("main_account_id")
    private Long mainAccountId;

    /**
     * 日累计限额
     */
    @TableField("day_limit")
    private BigDecimal dayLimit;

    /**
     * 夜间限额
     */
    @TableField("night_limit")
    private BigDecimal nightLimit;

    /**
     * 当前余额
     */
    private BigDecimal balance;

    /**
     * 可用余额
     */
    @TableField("available_balance")
    private BigDecimal availableBalance;

    /**
     * 冻结余额
     */
    @TableField("frozen_balance")
    private BigDecimal frozenBalance;

    // ===== 审计字段 =====

    private String createdBy;

    private LocalDateTime createdAt;

    private String updatedBy;

    private LocalDateTime updatedAt;

    private Integer version;

    private String deleted;
}

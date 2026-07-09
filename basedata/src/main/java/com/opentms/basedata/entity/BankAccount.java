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

    /**
     * 归属管理主体(基于业务单元)
     * 字段对齐:tms_bank_account_t.business_unit_id(2026-07-09 修复)
     */
    @TableField("business_unit_id")
    private Long businessUnitId;

    private String status;

    private String remark;

    // ===== 2026-06-29 扩展:银行账户业务字段(原 bankaccount 模块) =====
    // 这些字段只在 dealing 侧 BankAccountList/AtDealForm 上展示或暂未落库,
    // 当前 tms_bank_account_t 表不包含这些列;若未来要落库则同步建表并移除 exist=false。
    // 此处统一加 @TableField(exist = false) 防止 MyBatis Plus 自动 SELECT 这些不存在的列导致 SQL 错误(字段不存在)。

    /**
     * 账户别名
     */
    @TableField(exist = false)
    private String account;

    /**
     * 账户性质:Internal(内部)/ External(外部)
     */
    @TableField(exist = false)
    private String accountNature;

    /**
     * 是否归集:0=否 1=是
     */
    @TableField(exist = false)
    private String isCollected;

    /**
     * 归集方向:Up(上拨)/ Down(下拨)
     */
    @TableField(exist = false)
    private String collectDirection;

    /**
     * 主账户ID(归集时关联)
     */
    @TableField(exist = false)
    private Long mainAccountId;

    /**
     * 日累计限额
     */
    @TableField(exist = false)
    private BigDecimal dayLimit;

    /**
     * 夜间限额
     */
    @TableField(exist = false)
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

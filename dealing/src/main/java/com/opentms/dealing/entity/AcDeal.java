package com.opentms.dealing.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tms_ac_deals_t")
public class AcDeal {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("deal_number")
    private String dealNumber;

    @TableField("bank_account_id")
    private Long bankAccountId;

    @TableField("counterparty_account_id")
    private Long counterpartyAccountId;

    @TableField("payment_method")
    private String paymentMethod;

    private String createdBy;

    private LocalDateTime createdAt;

    private String updatedBy;

    private LocalDateTime updatedAt;

    private Integer version;

    private String deleted;
}
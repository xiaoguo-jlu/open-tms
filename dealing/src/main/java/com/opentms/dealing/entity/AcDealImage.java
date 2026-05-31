package com.opentms.dealing.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tms_ac_deals_image_t")
public class AcDealImage {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("image_number")
    private String imageNumber;

    @TableField("deal_number")
    private String dealNumber;

    private Integer version;

    @TableField("bank_account_id")
    private Long bankAccountId;

    @TableField("counterparty_account_id")
    private Long counterpartyAccountId;

    @TableField("payment_method")
    private String paymentMethod;

    @TableField("image_type")
    private String imageType;

    private String operator;

    @TableField("operate_at")
    private LocalDateTime operateAt;

    private String createdBy;

    private LocalDateTime createdAt;

    private String deleted;
}
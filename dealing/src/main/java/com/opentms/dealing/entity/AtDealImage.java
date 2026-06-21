package com.opentms.dealing.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * AT 交易镜像表（用于 UPDATE / DELETE 时记录字段快照；CREATE 不生成）
 * 对应表：tms_at_deals_image_t
 */
@Data
@TableName("tms_at_deals_image_t")
public class AtDealImage {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("image_number")
    private String imageNumber;

    @TableField("deal_number")
    private String dealNumber;

    private Integer version;

    @TableField("transfer_type")
    private String transferType;

    @TableField("source_account_id")
    private Long sourceAccountId;

    @TableField("dest_account_id")
    private Long destAccountId;

    @TableField("source_amount")
    private BigDecimal sourceAmount;

    @TableField("dest_amount")
    private BigDecimal destAmount;

    @TableField("source_currency")
    private String sourceCurrency;

    @TableField("dest_currency")
    private String destCurrency;

    @TableField("exchange_rate")
    private BigDecimal exchangeRate;

    @TableField("business_unit")
    private String businessUnit;

    @TableField("value_date")
    private LocalDate valueDate;

    @TableField("payment_method")
    private String paymentMethod;

    private String purpose;

    private String status;

    @TableField("latest_action_number")
    private String latestActionNumber;

    @TableField("image_type")
    private String imageType;

    private String operator;

    @TableField("operate_at")
    private LocalDateTime operateAt;

    private String createdBy;

    private LocalDateTime createdAt;

    private String deleted;
}

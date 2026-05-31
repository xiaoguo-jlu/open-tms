package com.opentms.dealing.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("tms_deals_image_t")
public class DealImage {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("image_number")
    private String imageNumber;

    @TableField("deal_number")
    private String dealNumber;

    @TableField("deal_type")
    private String dealType;

    private Integer version;

    @TableField("business_unit")
    private String businessUnit;

    @TableField("counterparty_id")
    private Long counterpartyId;

    @TableField("instrument_id")
    private Long instrumentId;

    @TableField("trader_id")
    private Long traderId;

    private String direction;

    private BigDecimal amount;

    private String currency;

    @TableField("deal_date")
    private LocalDate dealDate;

    @TableField("value_date")
    private LocalDate valueDate;

    private String status;

    private String description;

    private String remark;

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
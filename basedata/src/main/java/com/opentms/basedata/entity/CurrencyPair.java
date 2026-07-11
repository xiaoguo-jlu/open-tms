package com.opentms.basedata.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("tms_currency_pair_t")
public class CurrencyPair {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("pair_code")
    private String pairCode;

    @TableField("currency1")
    private String currency1;

    @TableField("currency2")
    private String currency2;

    @TableField("stronger_currency")
    private String strongerCurrency;

    @TableField("bid_decimal")
    private Integer bidDecimal;

    @TableField("ask_decimal")
    private Integer askDecimal;

    private String status;

    private String remark;

    private String createdBy;

    private java.time.LocalDateTime createdAt;

    private String updatedBy;

    private java.time.LocalDateTime updatedAt;

    private Integer version;

    private String deleted;
}

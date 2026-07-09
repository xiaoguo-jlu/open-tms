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

    /**
     * 基础货币(原 currency1)
     * 字段对齐:tms_currency_pair_t.base_currency(2026-07-09 修复)
     */
    @TableField("base_currency")
    private String baseCurrency;

    /**
     * 报价货币(原 currency2)
     * 字段对齐:tms_currency_pair_t.quote_currency(2026-07-09 修复)
     */
    @TableField("quote_currency")
    private String quoteCurrency;

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

package com.opentms.fx.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@TableName("tms_fx_deal_t")
public class FxDeal {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String dealNo;

    private String dealType;

    private String buyCurrency;

    private String sellCurrency;

    private BigDecimal buyAmount;

    private BigDecimal sellAmount;

    private BigDecimal rate;

    private Long counterpartyId;

    private LocalDate valueDate;

    private LocalDate maturityDate;

    private String status;

    private String remark;

    private String createdBy;

    private java.time.LocalDateTime createdAt;

    private String updatedBy;

    private java.time.LocalDateTime updatedAt;

    private Integer version;

    private String deleted;
}
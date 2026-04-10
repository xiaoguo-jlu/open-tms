package com.opentms.fx.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.opentms.common.model.BaseCodeEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("fx_deal_t")
public class FxDeal extends BaseCodeEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String fxType;

    private String buyCurrency;

    private String sellCurrency;

    private BigDecimal amount;

    private BigDecimal rate;

    private Long counterpartyId;

    private Long accountId;

    private LocalDate valueDate;

    private String status;

    private String remark;
}
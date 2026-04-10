package com.opentms.irs.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.opentms.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("trm_irs_deal_t")
public class IrsDeal extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String dealCode;

    private String dealType;

    private BigDecimal notionalAmount;

    private String currency;

    private LocalDate startDate;

    private LocalDate endDate;

    private BigDecimal fixedRate;

    private String floatingRateType;

    private BigDecimal floatingRateSpread;

    private String paymentFrequency;

    private Long counterpartyId;

    private Long accountId;

    private String status;
}
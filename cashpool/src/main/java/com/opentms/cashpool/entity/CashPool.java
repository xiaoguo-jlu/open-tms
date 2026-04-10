package com.opentms.cashpool.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.opentms.common.model.BaseCodeEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("trm_cash_pool_t")
public class CashPool extends BaseCodeEntity {

    private String poolName;

    private String poolType;

    private Long businessUnitId;

    private Boolean autoTransfer;

    private BigDecimal thresholdAmount;

    private BigDecimal totalBalance;
}
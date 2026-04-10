package com.opentms.fundplan.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.opentms.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("trm_fund_plan_t")
public class FundPlan extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Integer planYear;

    private Integer planMonth;

    private Long businessUnitId;

    private BigDecimal totalIncome;

    private BigDecimal totalExpense;

    private BigDecimal netCashFlow;

    private String planType;

    private String status;
}
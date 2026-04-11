package com.opentms.cashpool.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("tms_cash_pool_t")
public class CashPool {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String poolNo;

    private String poolName;

    private String poolType;

    private Long businessUnitId;

    private String currency;

    private BigDecimal balance;

    private BigDecimal thresholdAmount;

    private String autoTransfer;

    private String status;

    private String remark;

    private String createdBy;

    private java.time.LocalDateTime createdAt;

    private String updatedBy;

    private java.time.LocalDateTime updatedAt;

    private Integer version;

    private String deleted;
}
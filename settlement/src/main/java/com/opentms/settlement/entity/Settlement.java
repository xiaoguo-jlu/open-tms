package com.opentms.settlement.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@TableName("tms_settlement_t")
public class Settlement {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String settleNo;

    private String settleType;

    private Long dealId;

    private Long fromAccountId;

    private Long toAccountId;

    private BigDecimal amount;

    private String currency;

    private LocalDate valueDate;

    private String settleStatus;

    private String remark;

    private String createdBy;

    private java.time.LocalDateTime createdAt;

    private String updatedBy;

    private java.time.LocalDateTime updatedAt;

    private Integer version;

    private String deleted;
}
package com.opentms.settlement.entity;

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
@TableName("trm_settlement_t")
public class Settlement extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String settlementType;

    private Long accountId;

    private Long payeeId;

    private String payeeName;

    private String payeeBank;

    private String payeeAccountNo;

    private BigDecimal amount;

    private String currency;

    private String purpose;

    private LocalDate executeDate;

    private String status;
}
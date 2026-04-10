package com.opentms.dealing.entity;

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
@TableName("trm_deal_t")
public class Deal extends BaseCodeEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String dealNo;

    private String dealType;

    private String dealSubtype;

    private Long instrumentId;

    private Long counterpartyId;

    private Long businessUnitId;

    private BigDecimal amount;

    private String currency;

    private LocalDate valueDate;

    private LocalDate maturityDate;

    private BigDecimal interestRate;

    private String status;
}
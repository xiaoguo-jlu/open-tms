package com.opentms.dealing.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@TableName("tms_deal_t")
public class Deal {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String idempotencyKey;

    private String dealNo;

    private String dealType;

    private String dealSubtype;

    private Long instrumentId;

    private Long counterpartyId;

    private Long businessUnitId;

    private Long traderId;

    private BigDecimal amount;

    private String currency;

    private LocalDate valueDate;

    private LocalDate maturityDate;

    private BigDecimal interestRate;

    private String status;

    private String remark;

    private String createdBy;

    private java.time.LocalDateTime createdAt;

    private String updatedBy;

    private java.time.LocalDateTime updatedAt;

    private Integer version;

    private String deleted;
}
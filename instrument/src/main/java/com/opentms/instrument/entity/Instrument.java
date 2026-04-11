package com.opentms.instrument.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@TableName("tms_instrument_t")
public class Instrument {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String instrumentCode;

    private String instrumentName;

    private String instrumentType;

    private String instrumentSubtype;

    private String currency;

    private BigDecimal faceValue;

    private LocalDate issueDate;

    private LocalDate maturityDate;

    private BigDecimal interestRate;

    private Long counterpartyId;

    private String status;

    private String remark;

    private String createdBy;

    private java.time.LocalDateTime createdAt;

    private String updatedBy;

    private java.time.LocalDateTime updatedAt;

    private Integer version;

    private String deleted;
}
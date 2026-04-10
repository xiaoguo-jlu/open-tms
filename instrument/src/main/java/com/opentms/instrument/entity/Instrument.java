package com.opentms.instrument.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.opentms.common.model.BaseCodeEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("trm_instrument_t")
public class Instrument extends BaseCodeEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String instrumentCode;

    private String instrumentName;

    private String instrumentType;

    private String underlyingType;

    private String currency;

    private LocalDate maturityDate;
}
package com.opentms.instrument.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class InstrumentVO {

    private Long id;

    private String code;

    private String name;

    private String enName;

    private String status;

    private String instrumentCode;

    private String instrumentName;

    private String instrumentType;

    private String underlyingType;

    private String underlying;

    private String exchange;

    private String currency;

    private LocalDate maturityDate;

    private String createdBy;

    private LocalDateTime createdAt;

    private String updatedBy;

    private LocalDateTime updatedAt;
}
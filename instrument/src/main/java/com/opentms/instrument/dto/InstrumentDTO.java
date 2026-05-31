package com.opentms.instrument.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class InstrumentDTO {

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
}
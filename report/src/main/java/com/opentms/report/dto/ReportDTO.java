package com.opentms.report.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ReportDTO {

    private Long id;

    private String reportName;

    private Long templateId;

    private String templateName;

    private LocalDate reportDate;

    private Long businessUnitId;

    private LocalDate startDate;

    private LocalDate endDate;

    private String fileUrl;

    private String status;
}
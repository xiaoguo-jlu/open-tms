package com.opentms.report.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ReportVO {

    private Long id;

    private String reportName;

    private Long templateId;

    private String templateName;

    private LocalDate reportDate;

    private Long businessUnitId;

    private String businessUnitName;

    private LocalDate startDate;

    private LocalDate endDate;

    private String fileUrl;

    private String status;

    private String dataJson;

    private LocalDateTime generatedAt;

    private String createdBy;

    private LocalDateTime createdAt;
}
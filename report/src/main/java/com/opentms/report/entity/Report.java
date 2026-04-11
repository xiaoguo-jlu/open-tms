package com.opentms.report.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;

@Data
@TableName("tms_report_t")
public class Report {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String reportNo;

    private String reportName;

    private String reportType;

    private String reportTemplate;

    private Long businessUnitId;

    private String currency;

    private LocalDate startDate;

    private LocalDate endDate;

    private String reportData;

    private String status;

    private String createdBy;

    private java.time.LocalDateTime createdAt;

    private String updatedBy;

    private java.time.LocalDateTime updatedAt;

    private Integer version;

    private String deleted;
}
package com.opentms.report.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.opentms.common.model.BaseCodeEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("trm_report_t")
public class Report extends BaseCodeEntity {

    @TableId(type = IdType.AUTO)
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

    private String dataJson;

    private LocalDateTime generatedAt;
}
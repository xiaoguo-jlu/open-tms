package com.opentms.report.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.opentms.report.entity.Report;

public interface ReportService {

    Page<Report> queryPage(String keyword, Long templateId, Long businessUnitId, 
                          String status, int pageNum, int pageSize);

    Report getReportById(Long id);

    boolean saveReport(Report report);

    boolean updateReport(Report report);

    boolean deleteReport(Long id);

    Report generateReport(Report report);
}
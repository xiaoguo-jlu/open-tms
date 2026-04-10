package com.opentms.report.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.opentms.report.entity.Report;
import com.opentms.report.mapper.ReportMapper;
import com.opentms.report.service.ReportService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
public class ReportServiceImpl extends ServiceImpl<ReportMapper, Report> implements ReportService {

    @Override
    public Page<Report> queryPage(String keyword, Long templateId, Long businessUnitId,
                                 String status, int pageNum, int pageSize) {
        LambdaQueryWrapper<Report> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(keyword)) {
            wrapper.like(Report::getReportName, keyword);
        }

        if (templateId != null) {
            wrapper.eq(Report::getTemplateId, templateId);
        }

        if (businessUnitId != null) {
            wrapper.eq(Report::getBusinessUnitId, businessUnitId);
        }

        if (StringUtils.hasText(status)) {
            wrapper.eq(Report::getStatus, status);
        }

        wrapper.orderByDesc(Report::getCreatedAt);

        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public Report getReportById(Long id) {
        return getById(id);
    }

    @Override
    public boolean saveReport(Report report) {
        return save(report);
    }

    @Override
    public boolean updateReport(Report report) {
        return updateById(report);
    }

    @Override
    public boolean deleteReport(Long id) {
        return removeById(id);
    }

    @Override
    public Report generateReport(Report report) {
        report.setStatus("GENERATED");
        report.setGeneratedAt(LocalDateTime.now());
        save(report);
        return report;
    }
}
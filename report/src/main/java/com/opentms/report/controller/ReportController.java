package com.opentms.report.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.opentms.common.model.Result;
import com.opentms.report.entity.Report;
import com.opentms.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/page")
    public Result<Page<Report>> page(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long templateId,
            @RequestParam(required = false) Long businessUnitId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(reportService.queryPage(keyword, templateId, businessUnitId, status, pageNum, pageSize));
    }

    @GetMapping("/templates")
    public Result<List<Map<String, Object>>> templates() {
        return Result.success(List.of(
                Map.of("id", 1L, "name", "Cash Flow Report", "type", "CASH_FLOW"),
                Map.of("id", 2L, "name", "Position Report", "type", "POSITION"),
                Map.of("id", 3L, "name", "P&L Report", "type", "PROFIT_LOSS"),
                Map.of("id", 4L, "name", "FX Exposure Report", "type", "FX_EXPOSURE")
        ));
    }

    @PostMapping("/generate")
    public Result<Report> generate(@RequestBody Report report) {
        Report generated = reportService.generateReport(report);
        return Result.success(generated);
    }

    @GetMapping("/{id}")
    public Result<Report> getById(@PathVariable Long id) {
        Report report = reportService.getReportById(id);
        if (report == null) {
            return Result.notFound("Report not found");
        }
        return Result.success(report);
    }

    @GetMapping("/{id}/export")
    public Result<Map<String, String>> export(@PathVariable Long id) {
        Report report = reportService.getReportById(id);
        if (report == null) {
            return Result.notFound("Report not found");
        }
        return Result.success(Map.of(
                "fileUrl", report.getFileUrl() != null ? report.getFileUrl() : "",
                "fileName", report.getReportName() + ".xlsx"
        ));
    }

    @GetMapping("/schedules")
    public Result<List<Map<String, Object>>> schedules() {
        return Result.success(List.of());
    }

    @PostMapping("/schedules")
    public Result<Void> createSchedule(@RequestBody Map<String, Object> schedule) {
        return Result.success();
    }
}
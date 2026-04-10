package com.opentms.var.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.opentms.common.model.Result;
import com.opentms.var.entity.VarReport;
import com.opentms.var.service.VarReportService;
import com.opentms.var.vo.VarReportVO;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/var-reports")
@RequiredArgsConstructor
public class VarReportController {

    private final VarReportService varReportService;

    @GetMapping("/page")
    public Result<Page<VarReport>> page(
            @RequestParam(required = false) String varType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(varReportService.queryPage(varType, startDate, endDate, pageNum, pageSize));
    }

    @GetMapping("/{id}")
    public Result<VarReportVO> getById(@PathVariable Long id) {
        VarReportVO vo = varReportService.getById(id);
        if (vo == null) {
            return Result.notFound("VaR Report not found");
        }
        return Result.success(vo);
    }

    @PostMapping("/calculate")
    public Result<VarReport> calculate() {
        VarReport varReport = varReportService.calculate();
        return Result.success(varReport);
    }

    @GetMapping("/trend")
    public Result<List<Map<String, Object>>> trend(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return Result.success(varReportService.getTrend(startDate, endDate));
    }

    @PostMapping("/stress-test")
    public Result<Map<String, Object>> stressTest(@RequestBody Map<String, Object> params) {
        return Result.success(varReportService.stressTest(params));
    }
}
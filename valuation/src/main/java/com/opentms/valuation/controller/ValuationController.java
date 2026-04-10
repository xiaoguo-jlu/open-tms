package com.opentms.valuation.controller;

import com.opentms.common.model.Result;
import com.opentms.valuation.entity.Valuation;
import com.opentms.valuation.service.ValuationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/valuations")
@RequiredArgsConstructor
public class ValuationController {

    private final ValuationService valuationService;

    @GetMapping("/page")
    public Result<com.baomidou.mybatisplus.extension.plugins.pagination.Page<Valuation>> page(
            @RequestParam(required = false) Long instrumentId,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) String valuationMethod,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(valuationService.queryPage(instrumentId, startDate, endDate, valuationMethod, pageNum, pageSize));
    }

    @GetMapping("/{id}")
    public Result<Valuation> getById(@PathVariable Long id) {
        Valuation valuation = valuationService.getValuationById(id);
        if (valuation == null) {
            return Result.notFound("Valuation not found");
        }
        return Result.success(valuation);
    }

    @PostMapping("/execute")
    public Result<Map<String, Object>> execute(
            @RequestParam Long instrumentId,
            @RequestParam(required = false) LocalDate valuationDate) {
        if (valuationDate == null) {
            valuationDate = LocalDate.now();
        }
        return Result.success(valuationService.executeValuation(instrumentId, valuationDate));
    }

    @GetMapping("/history")
    public Result<List<Valuation>> history(
            @RequestParam Long instrumentId,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {
        return Result.success(valuationService.getHistory(instrumentId, startDate, endDate));
    }

    @GetMapping("/parameters")
    public Result<Map<String, Object>> parameters(@RequestParam Long instrumentId) {
        return Result.success(valuationService.getParameters(instrumentId));
    }

    @PutMapping("/parameters")
    public Result<Void> updateParameters(
            @RequestParam Long instrumentId,
            @RequestBody Map<String, Object> parameters) {
        valuationService.updateParameters(instrumentId, parameters);
        return Result.success();
    }
}
package com.opentms.fundplan.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.opentms.fundplan.entity.FundPlan;
import com.opentms.fundplan.service.FundPlanService;
import com.opentms.common.model.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/fund-plans")
@RequiredArgsConstructor
public class FundPlanController {

    private final FundPlanService fundPlanService;

    @GetMapping("/page")
    public Result<Page<FundPlan>> page(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer planYear,
            @RequestParam(required = false) String planType,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(fundPlanService.queryPage(keyword, planYear, planType, status, pageNum, pageSize));
    }

    @GetMapping("/annual")
    public Result<List<FundPlan>> annual(
            @RequestParam(required = false) Integer planYear,
            @RequestParam(required = false) Long businessUnitId) {
        return Result.success(fundPlanService.getAnnualPlans(planYear, businessUnitId));
    }

    @GetMapping("/monthly")
    public Result<List<FundPlan>> monthly(
            @RequestParam(required = false) Integer planYear,
            @RequestParam(required = false) Integer planMonth,
            @RequestParam(required = false) Long businessUnitId) {
        return Result.success(fundPlanService.getMonthlyPlans(planYear, planMonth, businessUnitId));
    }

    @GetMapping("/{id}")
    public Result<FundPlan> getById(@PathVariable Long id) {
        FundPlan fundPlan = fundPlanService.getFundPlanById(id);
        if (fundPlan == null) {
            return Result.notFound("Fund plan not found");
        }
        return Result.success(fundPlan);
    }

    @PostMapping
    public Result<Void> save(@RequestBody FundPlan fundPlan) {
        fundPlanService.saveFundPlan(fundPlan);
        return Result.success();
    }

    @PutMapping
    public Result<Void> update(@RequestBody FundPlan fundPlan) {
        fundPlanService.updateFundPlan(fundPlan);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        fundPlanService.deleteFundPlan(id);
        return Result.success();
    }

    @PostMapping("/{id}/submit")
    public Result<Void> submit(@PathVariable Long id) {
        fundPlanService.submitFundPlan(id);
        return Result.success();
    }

    @PostMapping("/{id}/approve")
    public Result<Void> approve(@PathVariable Long id) {
        fundPlanService.approveFundPlan(id);
        return Result.success();
    }

    @PostMapping("/{id}/lock")
    public Result<Void> lock(@PathVariable Long id) {
        fundPlanService.lockFundPlan(id);
        return Result.success();
    }
}
package com.opentms.irs.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.opentms.common.model.Result;
import com.opentms.irs.entity.IrsDeal;
import com.opentms.irs.service.IrsDealService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/irs-deals")
@RequiredArgsConstructor
public class IrsDealController {

    private final IrsDealService irsDealService;

    @GetMapping("/page")
    public Result<Page<IrsDeal>> page(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String dealType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String counterpartyId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(irsDealService.queryPage(keyword, dealType, status, counterpartyId, pageNum, pageSize));
    }

    @GetMapping("/{id}")
    public Result<IrsDeal> getById(@PathVariable Long id) {
        IrsDeal irsDeal = irsDealService.getIrsDealById(id);
        if (irsDeal == null) {
            return Result.notFound("IRS Deal not found");
        }
        return Result.success(irsDeal);
    }

    @GetMapping("/{id}/valuation")
    public Result<Map<String, Object>> getValuation(@PathVariable Long id) {
        IrsDeal deal = irsDealService.getIrsDealById(id);
        if (deal == null) {
            return Result.notFound("IRS Deal not found");
        }
        return Result.success(Map.of(
            "dealId", id,
            "dealCode", deal.getDealCode(),
            "valuation", BigDecimal.ZERO,
            "currency", deal.getCurrency(),
            "valuationDate", java.time.LocalDate.now()
        ));
    }

    @GetMapping("/{id}/cashflows")
    public Result<List<Map<String, Object>>> getCashflows(@PathVariable Long id) {
        IrsDeal deal = irsDealService.getIrsDealById(id);
        if (deal == null) {
            return Result.notFound("IRS Deal not found");
        }
        return Result.success(List.of());
    }

    @PostMapping
    public Result<Void> save(@RequestBody IrsDeal irsDeal) {
        irsDealService.saveIrsDeal(irsDeal);
        return Result.success();
    }

    @PutMapping
    public Result<Void> update(@RequestBody IrsDeal irsDeal) {
        irsDealService.updateIrsDeal(irsDeal);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        irsDealService.deleteIrsDeal(id);
        return Result.success();
    }

    @PostMapping("/{id}/submit")
    public Result<Void> submit(@PathVariable Long id) {
        irsDealService.submit(id);
        return Result.success();
    }

    @PostMapping("/{id}/approve")
    public Result<Void> approve(@PathVariable Long id) {
        irsDealService.approve(id);
        return Result.success();
    }
}
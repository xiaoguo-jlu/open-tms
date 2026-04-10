package com.opentms.dealing.controller;

import com.opentms.dealing.entity.Deal;
import com.opentms.dealing.service.DealService;
import com.opentms.common.model.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/deals")
@RequiredArgsConstructor
public class DealController {

    private final DealService dealService;

    @GetMapping("/page")
    public Result<com.baomidou.mybatisplus.extension.plugins.pagination.Page<Deal>> page(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String dealType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String counterpartyId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(dealService.queryPage(keyword, dealType, status, counterpartyId, pageNum, pageSize));
    }

    @GetMapping("/{id}")
    public Result<Deal> getById(@PathVariable Long id) {
        Deal deal = dealService.getDealById(id);
        if (deal == null) {
            return Result.notFound("Deal not found");
        }
        return Result.success(deal);
    }

    @PostMapping
    public Result<Void> save(@RequestBody Deal deal) {
        dealService.saveDeal(deal);
        return Result.success();
    }

    @PutMapping
    public Result<Void> update(@RequestBody Deal deal) {
        dealService.updateDeal(deal);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        dealService.deleteDeal(id);
        return Result.success();
    }

    @PostMapping("/{id}/submit")
    public Result<Void> submit(@PathVariable Long id) {
        dealService.submitDeal(id);
        return Result.success();
    }

    @PostMapping("/{id}/approve")
    public Result<Void> approve(@PathVariable Long id) {
        dealService.approveDeal(id);
        return Result.success();
    }

    @PostMapping("/{id}/reject")
    public Result<Void> reject(@PathVariable Long id) {
        dealService.rejectDeal(id);
        return Result.success();
    }

    @PostMapping("/batch-delete")
    public Result<Void> batchDelete(@RequestBody java.util.Map<String, String> request) {
        String ids = request.get("ids");
        dealService.batchDelete(ids);
        return Result.success();
    }
}
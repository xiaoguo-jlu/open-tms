package com.opentms.dealing.controller;

import com.opentms.dealing.dto.DealDTO;
import com.opentms.dealing.service.DealService;
import com.opentms.dealing.vo.DealVO;
import com.opentms.common.model.Result;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/dealing/deals")
@RequiredArgsConstructor
public class DealController {

    private final DealService dealService;

    @GetMapping("/page")
    public Result<Page<DealVO>> page(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String dealType,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(dealService.queryPage(keyword, dealType, status, pageNum, pageSize));
    }

    @GetMapping("/{id}")
    public Result<DealVO> getById(@PathVariable Long id) {
        DealVO deal = dealService.getDealById(id);
        if (deal == null) {
            return Result.notFound("Deal not found");
        }
        return Result.success(deal);
    }

    @GetMapping("/number/{dealNumber}")
    public Result<DealVO> getByDealNumber(@PathVariable String dealNumber) {
        DealVO deal = dealService.getDealByDealNumber(dealNumber);
        if (deal == null) {
            return Result.notFound("Deal not found");
        }
        return Result.success(deal);
    }

    @PostMapping
    public Result<Void> save(@RequestBody DealDTO dealDTO) {
        dealService.saveDeal(dealDTO);
        return Result.success();
    }

    @PostMapping("/update")
    public Result<Void> update(@RequestBody DealDTO dealDTO) {
        dealService.updateDeal(dealDTO);
        return Result.success();
    }

    @PostMapping("/{id}/submit")
    public Result<Void> submit(@PathVariable Long id, @RequestBody java.util.Map<String, String> request) {
        String operator = request.getOrDefault("operator", "system");
        dealService.submitDeal(id, operator);
        return Result.success();
    }

    @PostMapping("/{id}/approve")
    public Result<Void> approve(@PathVariable Long id, @RequestBody java.util.Map<String, String> request) {
        String approver = request.getOrDefault("approver", "system");
        String approvalRemark = request.get("approvalRemark");
        dealService.approveDeal(id, approver, approvalRemark);
        return Result.success();
    }

    @PostMapping("/{id}/reject")
    public Result<Void> reject(@PathVariable Long id, @RequestBody java.util.Map<String, String> request) {
        String approver = request.getOrDefault("approver", "system");
        String approvalRemark = request.get("approvalRemark");
        dealService.rejectDeal(id, approver, approvalRemark);
        return Result.success();
    }

    @PostMapping("/{id}/execute")
    public Result<Void> execute(@PathVariable Long id, @RequestBody java.util.Map<String, String> request) {
        String operator = request.getOrDefault("operator", "system");
        dealService.executeDeal(id, operator);
        return Result.success();
    }
}
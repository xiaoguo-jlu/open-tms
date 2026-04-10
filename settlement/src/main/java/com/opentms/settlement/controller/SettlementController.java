package com.opentms.settlement.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.opentms.common.model.Result;
import com.opentms.settlement.dto.SettlementDTO;
import com.opentms.settlement.entity.Settlement;
import com.opentms.settlement.service.SettlementService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/settlements")
@RequiredArgsConstructor
public class SettlementController {

    private final SettlementService settlementService;

    @GetMapping("/page")
    public Result<Page<Settlement>> page(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String settlementType,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(settlementService.queryPage(keyword, status, settlementType, pageNum, pageSize));
    }

    @GetMapping("/{id}")
    public Result<Settlement> getById(@PathVariable Long id) {
        Settlement settlement = settlementService.getSettlementById(id);
        if (settlement == null) {
            return Result.notFound("Settlement not found");
        }
        return Result.success(settlement);
    }

    @PostMapping
    public Result<Void> save(@RequestBody SettlementDTO dto) {
        settlementService.saveSettlement(dto);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody SettlementDTO dto) {
        settlementService.updateSettlement(dto, id);
        return Result.success();
    }

    @PostMapping("/{id}/submit")
    public Result<Void> submit(@PathVariable Long id) {
        settlementService.submit(id);
        return Result.success();
    }

    @PostMapping("/{id}/approve")
    public Result<Void> approve(@PathVariable Long id) {
        settlementService.approve(id);
        return Result.success();
    }

    @PostMapping("/{id}/execute")
    public Result<Void> execute(@PathVariable Long id) {
        settlementService.execute(id);
        return Result.success();
    }

    @GetMapping("/{id}/result")
    public Result<String> getResult(@PathVariable Long id) {
        String result = settlementService.getResult(id);
        if (result == null) {
            return Result.notFound("Settlement not found");
        }
        return Result.success(result);
    }
}
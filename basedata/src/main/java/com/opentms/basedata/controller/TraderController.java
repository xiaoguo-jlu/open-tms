package com.opentms.basedata.controller;

import com.opentms.basedata.entity.Trader;
import com.opentms.basedata.service.TraderService;
import com.opentms.common.model.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/trader")
@RequiredArgsConstructor
public class TraderController {

    private final TraderService traderService;

    @GetMapping("/page")
    public Result<com.baomidou.mybatisplus.extension.plugins.pagination.Page<Trader>> page(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(traderService.queryPage(keyword, status, pageNum, pageSize));
    }

    @GetMapping("/{id}")
    public Result<Trader> getById(@PathVariable Long id) {
        Trader trader = traderService.getTraderById(id);
        if (trader == null) {
            return Result.notFound("Trader not found");
        }
        return Result.success(trader);
    }

    @PostMapping
    public Result<Void> save(@RequestBody Trader trader) {
        traderService.saveTrader(trader);
        return Result.success();
    }

    @PutMapping
    public Result<Void> update(@RequestBody Trader trader) {
        traderService.updateTrader(trader);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        traderService.deleteTrader(id);
        return Result.success();
    }
}
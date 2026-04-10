package com.opentms.basedata.controller;

import com.opentms.basedata.entity.Counterparty;
import com.opentms.basedata.service.CounterpartyService;
import com.opentms.common.model.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/counterparty")
@RequiredArgsConstructor
public class CounterpartyController {

    private final CounterpartyService counterpartyService;

    @GetMapping("/page")
    public Result<com.baomidou.mybatisplus.extension.plugins.pagination.Page<Counterparty>> page(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String counterpartyType,
            @RequestParam(required = false) String countryCode,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(counterpartyService.queryPage(keyword, counterpartyType, countryCode, status, pageNum, pageSize));
    }

    @GetMapping("/{id}")
    public Result<Counterparty> getById(@PathVariable Long id) {
        Counterparty counterparty = counterpartyService.getCounterpartyById(id);
        if (counterparty == null) {
            return Result.notFound("Counterparty not found");
        }
        return Result.success(counterparty);
    }

    @PostMapping
    public Result<Void> save(@RequestBody Counterparty counterparty) {
        counterpartyService.saveCounterparty(counterparty);
        return Result.success();
    }

    @PutMapping
    public Result<Void> update(@RequestBody Counterparty counterparty) {
        counterpartyService.updateCounterparty(counterparty);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        counterpartyService.deleteCounterparty(id);
        return Result.success();
    }
}
package com.opentms.basedata.controller;

import com.opentms.basedata.entity.Currency;
import com.opentms.basedata.service.CurrencyService;
import com.opentms.common.model.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/currency")
@RequiredArgsConstructor
public class CurrencyController {

    private final CurrencyService currencyService;

    @GetMapping("/list")
    public Result<List<Currency>> list(@RequestParam(required = false) String keyword,
                                        @RequestParam(required = false) String status) {
        return Result.success(currencyService.queryPage(keyword, status, 1, 100).getRecords());
    }

    @GetMapping("/page")
    public Result<com.baomidou.mybatisplus.extension.plugins.pagination.Page<Currency>> page(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(currencyService.queryPage(keyword, status, pageNum, pageSize));
    }

    @GetMapping("/{id}")
    public Result<Currency> getById(@PathVariable Long id) {
        Currency currency = currencyService.getCurrencyById(id);
        if (currency == null) {
            return Result.notFound("Currency not found");
        }
        return Result.success(currency);
    }

    @PostMapping
    public Result<Void> save(@RequestBody Currency currency) {
        boolean success = currencyService.saveCurrency(currency);
        return success ? Result.success() : Result.error("Save failed");
    }

    @PutMapping
    public Result<Void> update(@RequestBody Currency currency) {
        boolean success = currencyService.updateCurrency(currency);
        return success ? Result.success() : Result.error("Update failed");
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        boolean success = currencyService.deleteCurrency(id);
        return success ? Result.success() : Result.error("Delete failed");
    }
}

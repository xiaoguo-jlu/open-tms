package com.opentms.basedata.controller;

import com.opentms.basedata.entity.Bank;
import com.opentms.basedata.service.BankService;
import com.opentms.common.model.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/banks")
@RequiredArgsConstructor
public class BankController {

    private final BankService bankService;

    @GetMapping("/page")
    public Result<com.baomidou.mybatisplus.extension.plugins.pagination.Page<Bank>> page(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String countryCode,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(bankService.queryPage(keyword, countryCode, status, pageNum, pageSize));
    }

    @GetMapping("/{id}")
    public Result<Bank> getById(@PathVariable Long id) {
        Bank bank = bankService.getBankById(id);
        if (bank == null) {
            return Result.notFound("Bank not found");
        }
        return Result.success(bank);
    }

    @PostMapping
    public Result<Void> save(@RequestBody Bank bank) {
        bankService.saveBank(bank);
        return Result.success();
    }

    @PutMapping
    public Result<Void> update(@RequestBody Bank bank) {
        bankService.updateBank(bank);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        bankService.deleteBank(id);
        return Result.success();
    }
}
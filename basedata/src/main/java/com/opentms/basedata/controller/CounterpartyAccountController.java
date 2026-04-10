package com.opentms.basedata.controller;

import com.opentms.basedata.entity.CounterpartyAccount;
import com.opentms.basedata.service.CounterpartyAccountService;
import com.opentms.common.model.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/counterparty-account")
@RequiredArgsConstructor
public class CounterpartyAccountController {

    private final CounterpartyAccountService counterpartyAccountService;

    @GetMapping("/page")
    public Result<com.baomidou.mybatisplus.extension.plugins.pagination.Page<CounterpartyAccount>> page(
            @RequestParam(required = false) Long counterpartyId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(counterpartyAccountService.queryPage(counterpartyId, keyword, status, pageNum, pageSize));
    }

    @GetMapping("/{id}")
    public Result<CounterpartyAccount> getById(@PathVariable Long id) {
        CounterpartyAccount account = counterpartyAccountService.getCounterpartyAccountById(id);
        if (account == null) {
            return Result.notFound("Counterparty account not found");
        }
        return Result.success(account);
    }

    @PostMapping
    public Result<Void> save(@RequestBody CounterpartyAccount account) {
        counterpartyAccountService.saveCounterpartyAccount(account);
        return Result.success();
    }

    @PutMapping
    public Result<Void> update(@RequestBody CounterpartyAccount account) {
        counterpartyAccountService.updateCounterpartyAccount(account);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        counterpartyAccountService.deleteCounterpartyAccount(id);
        return Result.success();
    }
}
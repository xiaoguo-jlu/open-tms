package com.opentms.bankaccount.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.opentms.bankaccount.entity.BankAccount;
import com.opentms.bankaccount.service.BankAccountService;
import com.opentms.common.model.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/bank-accounts")
@RequiredArgsConstructor
public class BankAccountController {

    private final BankAccountService bankAccountService;

    @GetMapping("/page")
    public Result<Page<BankAccount>> page(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long bankId,
            @RequestParam(required = false) String accountType,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(bankAccountService.queryPage(keyword, bankId, accountType, status, pageNum, pageSize));
    }

    @GetMapping("/{id}")
    public Result<BankAccount> getById(@PathVariable Long id) {
        BankAccount bankAccount = bankAccountService.getBankAccountById(id);
        if (bankAccount == null) {
            return Result.notFound("Bank account not found");
        }
        return Result.success(bankAccount);
    }

    @GetMapping("/{id}/balance")
    public Result<Map<String, BigDecimal>> getBalance(@PathVariable Long id) {
        BigDecimal balance = bankAccountService.getBalance(id);
        BankAccount bankAccount = bankAccountService.getBankAccountById(id);
        return Result.success(Map.of(
            "balance", balance,
            "availableBalance", bankAccount.getAvailableBalance(),
            "frozenBalance", bankAccount.getFrozenBalance()
        ));
    }

    @PostMapping
    public Result<Void> save(@RequestBody BankAccount bankAccount) {
        bankAccountService.saveBankAccount(bankAccount);
        return Result.success();
    }

    @PutMapping
    public Result<Void> update(@RequestBody BankAccount bankAccount) {
        bankAccountService.updateBankAccount(bankAccount);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        bankAccountService.deleteBankAccount(id);
        return Result.success();
    }

    @PostMapping("/batch-delete")
    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
        bankAccountService.batchDelete(ids);
        return Result.success();
    }
}
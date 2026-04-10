package com.opentms.cashpool.controller;

import com.opentms.cashpool.entity.CashPool;
import com.opentms.cashpool.service.CashPoolService;
import com.opentms.common.model.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/cash-pools")
@RequiredArgsConstructor
public class CashPoolController {

    private final CashPoolService cashPoolService;

    @GetMapping("/page")
    public Result<com.baomidou.mybatisplus.extension.plugins.pagination.Page<CashPool>> page(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String poolType,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(cashPoolService.queryPage(keyword, poolType, status, pageNum, pageSize));
    }

    @GetMapping("/{id}")
    public Result<CashPool> getById(@PathVariable Long id) {
        CashPool cashPool = cashPoolService.getCashPoolById(id);
        if (cashPool == null) {
            return Result.notFound("Cash pool not found");
        }
        return Result.success(cashPool);
    }

    @GetMapping("/{id}/balance")
    public Result<BigDecimal> getBalance(@PathVariable Long id) {
        try {
            BigDecimal balance = cashPoolService.getPoolBalance(id);
            return Result.success(balance);
        } catch (RuntimeException e) {
            return Result.notFound(e.getMessage());
        }
    }

    @PostMapping("/{id}/transfer")
    public Result<Void> transfer(@PathVariable Long id, @RequestBody TransferRequest request) {
        try {
            cashPoolService.manualTransfer(id, request.getToPoolId(), request.getAmount());
            return Result.success();
        } catch (RuntimeException e) {
            return Result.badRequest(e.getMessage());
        }
    }

    @PostMapping
    public Result<Void> save(@RequestBody CashPool cashPool) {
        cashPoolService.saveCashPool(cashPool);
        return Result.success();
    }

    @PutMapping
    public Result<Void> update(@RequestBody CashPool cashPool) {
        cashPoolService.updateCashPool(cashPool);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        cashPoolService.deleteCashPool(id);
        return Result.success();
    }

    public static class TransferRequest {
        private Long toPoolId;
        private BigDecimal amount;

        public Long getToPoolId() {
            return toPoolId;
        }

        public void setToPoolId(Long toPoolId) {
            this.toPoolId = toPoolId;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }
    }
}
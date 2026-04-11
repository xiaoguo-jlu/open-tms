package com.opentms.cashpool.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.opentms.cashpool.entity.CashPool;
import com.opentms.cashpool.mapper.CashPoolMapper;
import com.opentms.cashpool.service.CashPoolService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;

@Service
public class CashPoolServiceImpl extends ServiceImpl<CashPoolMapper, CashPool> implements CashPoolService {

    @Override
    public Page<CashPool> queryPage(String keyword, String poolType, String status, int pageNum, int pageSize) {
        LambdaQueryWrapper<CashPool> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(keyword)) {
            wrapper.like(CashPool::getPoolNo, keyword)
                   .or()
                   .like(CashPool::getPoolName, keyword);
        }

        if (StringUtils.hasText(poolType)) {
            wrapper.eq(CashPool::getPoolType, poolType);
        }

        if (StringUtils.hasText(status)) {
            wrapper.eq(CashPool::getStatus, status);
        }

        wrapper.orderByDesc(CashPool::getCreatedAt);

        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public CashPool getCashPoolById(Long id) {
        return getById(id);
    }

    @Override
    public BigDecimal getPoolBalance(Long id) {
        CashPool cashPool = getById(id);
        if (cashPool == null) {
            throw new RuntimeException("Cash pool not found");
        }
        return cashPool.getBalance() != null ? cashPool.getBalance() : BigDecimal.ZERO;
    }

    @Override
    public boolean saveCashPool(CashPool cashPool) {
        return save(cashPool);
    }

    @Override
    public boolean updateCashPool(CashPool cashPool) {
        return updateById(cashPool);
    }

    @Override
    public boolean deleteCashPool(Long id) {
        return removeById(id);
    }

    @Override
    public boolean manualTransfer(Long fromPoolId, Long toPoolId, BigDecimal amount) {
        if (fromPoolId.equals(toPoolId)) {
            throw new RuntimeException("Source and target pool cannot be the same");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Transfer amount must be greater than zero");
        }

        CashPool fromPool = getById(fromPoolId);
        if (fromPool == null) {
            throw new RuntimeException("Source cash pool not found");
        }

        CashPool toPool = getById(toPoolId);
        if (toPool == null) {
            throw new RuntimeException("Target cash pool not found");
        }

        BigDecimal fromBalance = fromPool.getBalance() != null ? fromPool.getBalance() : BigDecimal.ZERO;
        if (fromBalance.compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient balance in source pool");
        }

        fromPool.setBalance(fromBalance.subtract(amount));
        toPool.setBalance(toPool.getBalance() != null ? toPool.getBalance().add(amount) : amount);

        return updateById(fromPool) && updateById(toPool);
    }
}
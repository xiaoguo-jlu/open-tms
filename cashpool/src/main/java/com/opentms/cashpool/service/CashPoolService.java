package com.opentms.cashpool.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.opentms.cashpool.entity.CashPool;

import java.math.BigDecimal;

public interface CashPoolService {

    Page<CashPool> queryPage(String keyword, String poolType, String status, int pageNum, int pageSize);

    CashPool getCashPoolById(Long id);

    BigDecimal getPoolBalance(Long id);

    boolean saveCashPool(CashPool cashPool);

    boolean updateCashPool(CashPool cashPool);

    boolean deleteCashPool(Long id);

    boolean manualTransfer(Long fromPoolId, Long toPoolId, BigDecimal amount);
}
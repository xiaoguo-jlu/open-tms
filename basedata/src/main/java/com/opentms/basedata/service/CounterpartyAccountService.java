package com.opentms.basedata.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.opentms.basedata.entity.CounterpartyAccount;

public interface CounterpartyAccountService {

    Page<CounterpartyAccount> queryPage(Long counterpartyId, String keyword, String status, int pageNum, int pageSize);

    CounterpartyAccount getCounterpartyAccountById(Long id);

    boolean saveCounterpartyAccount(CounterpartyAccount account);

    boolean updateCounterpartyAccount(CounterpartyAccount account);

    boolean deleteCounterpartyAccount(Long id);

    boolean checkCodeExists(String code, Long excludeId);
}
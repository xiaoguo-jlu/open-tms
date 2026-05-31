package com.opentms.basedata.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.opentms.basedata.entity.BankAccount;

public interface BankAccountService {

    Page<BankAccount> queryPage(String keyword, Long bankId, String currency, String accountType, Long businessUnitId, String status, int pageNum, int pageSize);

    BankAccount getBankAccountById(Long id);

    boolean saveBankAccount(BankAccount account);

    boolean updateBankAccount(BankAccount account);

    boolean deleteBankAccount(Long id);

    boolean checkAccountNoExists(String accountNo, Long excludeId);
}

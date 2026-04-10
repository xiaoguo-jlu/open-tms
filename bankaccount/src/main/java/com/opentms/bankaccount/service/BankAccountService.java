package com.opentms.bankaccount.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.opentms.bankaccount.entity.BankAccount;
import java.math.BigDecimal;
import java.util.List;

public interface BankAccountService {

    Page<BankAccount> queryPage(String keyword, Long bankId, String accountType, String status, int pageNum, int pageSize);

    BankAccount getBankAccountById(Long id);

    boolean saveBankAccount(BankAccount bankAccount);

    boolean updateBankAccount(BankAccount bankAccount);

    boolean deleteBankAccount(Long id);

    boolean batchDelete(List<Long> ids);

    boolean checkCodeExists(String code, Long excludeId);

    BigDecimal getBalance(Long id);
}
package com.opentms.basedata.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.opentms.basedata.entity.BankAccount;
import com.opentms.basedata.mapper.BankAccountMapper;
import com.opentms.basedata.service.BankAccountService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
public class BankAccountServiceImpl extends ServiceImpl<BankAccountMapper, BankAccount> implements BankAccountService {

    @Override
    public Page<BankAccount> queryPage(String keyword, Long bankId, String currency, String accountType, Long managementEntityId, String status, int pageNum, int pageSize) {
        LambdaQueryWrapper<BankAccount> wrapper = new LambdaQueryWrapper<>();

        if (bankId != null) {
            wrapper.eq(BankAccount::getBankId, bankId);
        }

        if (StringUtils.hasText(currency)) {
            wrapper.eq(BankAccount::getCurrency, currency);
        }

        if (StringUtils.hasText(accountType)) {
            wrapper.eq(BankAccount::getAccountType, accountType);
        }

        if (managementEntityId != null) {
            wrapper.eq(BankAccount::getManagementEntityId, managementEntityId);
        }

        if (StringUtils.hasText(status)) {
            wrapper.eq(BankAccount::getStatus, status);
        }

        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(BankAccount::getAccountNo, keyword)
                    .or()
                    .like(BankAccount::getAccountName, keyword));
        }

        wrapper.orderByDesc(BankAccount::getCreatedAt);

        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public BankAccount getBankAccountById(Long id) {
        return getById(id);
    }

    @Override
    public boolean saveBankAccount(BankAccount account) {
        if (checkAccountNoExists(account.getAccountNo(), null)) {
            throw new RuntimeException("Account number already exists");
        }
        account.setCreatedBy("system");
        account.setCreatedAt(LocalDateTime.now());
        account.setStatus("1");
        return save(account);
    }

    @Override
    public boolean updateBankAccount(BankAccount account) {
        if (account.getId() == null) {
            throw new RuntimeException("Account ID cannot be null");
        }
        BankAccount existing = getById(account.getId());
        if (existing == null) {
            throw new RuntimeException("Account not found");
        }
        if (checkAccountNoExists(account.getAccountNo(), account.getId())) {
            throw new RuntimeException("Account number already exists");
        }
        // Preserve created audit fields
        account.setCreatedBy(existing.getCreatedBy());
        account.setCreatedAt(existing.getCreatedAt());
        account.setUpdatedBy("system");
        account.setUpdatedAt(LocalDateTime.now());
        return updateById(account);
    }

    @Override
    public boolean deleteBankAccount(Long id) {
        BankAccount existing = getById(id);
        if (existing == null) {
            throw new RuntimeException("Account not found");
        }
        return removeById(id);
    }

    @Override
    public boolean checkAccountNoExists(String accountNo, Long excludeId) {
        if (!StringUtils.hasText(accountNo)) {
            return false;
        }
        LambdaQueryWrapper<BankAccount> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BankAccount::getAccountNo, accountNo);
        if (excludeId != null) {
            wrapper.ne(BankAccount::getId, excludeId);
        }
        return count(wrapper) > 0;
    }
}

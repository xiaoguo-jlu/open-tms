package com.opentms.bankaccount.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.opentms.bankaccount.entity.BankAccount;
import com.opentms.bankaccount.mapper.BankAccountMapper;
import com.opentms.bankaccount.service.BankAccountService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.math.BigDecimal;
import java.util.List;

@Service
public class BankAccountServiceImpl extends ServiceImpl<BankAccountMapper, BankAccount> implements BankAccountService {

    @Override
    public Page<BankAccount> queryPage(String keyword, Long bankId, String accountType, String status, int pageNum, int pageSize) {
        LambdaQueryWrapper<BankAccount> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(keyword)) {
            wrapper.like(BankAccount::getCode, keyword)
                   .or()
                   .like(BankAccount::getName, keyword)
                   .or()
                   .like(BankAccount::getAccountNo, keyword);
        }

        if (bankId != null) {
            wrapper.eq(BankAccount::getBankId, bankId);
        }

        if (StringUtils.hasText(accountType)) {
            wrapper.eq(BankAccount::getAccountType, accountType);
        }

        if (StringUtils.hasText(status)) {
            wrapper.eq(BankAccount::getStatus, status);
        }

        wrapper.orderByDesc(BankAccount::getCreatedAt);

        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public BankAccount getBankAccountById(Long id) {
        return getById(id);
    }

    @Override
    public boolean saveBankAccount(BankAccount bankAccount) {
        if (checkCodeExists(bankAccount.getCode(), null)) {
            throw new RuntimeException("Bank account code already exists");
        }
        if (bankAccount.getBalance() == null) {
            bankAccount.setBalance(BigDecimal.ZERO);
        }
        if (bankAccount.getAvailableBalance() == null) {
            bankAccount.setAvailableBalance(BigDecimal.ZERO);
        }
        if (bankAccount.getFrozenBalance() == null) {
            bankAccount.setFrozenBalance(BigDecimal.ZERO);
        }
        return save(bankAccount);
    }

    @Override
    public boolean updateBankAccount(BankAccount bankAccount) {
        if (checkCodeExists(bankAccount.getCode(), bankAccount.getId())) {
            throw new RuntimeException("Bank account code already exists");
        }
        return updateById(bankAccount);
    }

    @Override
    public boolean deleteBankAccount(Long id) {
        return removeById(id);
    }

    @Override
    public boolean batchDelete(List<Long> ids) {
        return removeByIds(ids);
    }

    @Override
    public boolean checkCodeExists(String code, Long excludeId) {
        if (!StringUtils.hasText(code)) {
            return false;
        }
        LambdaQueryWrapper<BankAccount> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BankAccount::getCode, code);
        if (excludeId != null) {
            wrapper.ne(BankAccount::getId, excludeId);
        }
        return count(wrapper) > 0;
    }

    @Override
    public BigDecimal getBalance(Long id) {
        BankAccount bankAccount = getById(id);
        if (bankAccount == null) {
            throw new RuntimeException("Bank account not found");
        }
        return bankAccount.getBalance();
    }
}
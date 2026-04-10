package com.opentms.basedata.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.opentms.basedata.entity.CounterpartyAccount;
import com.opentms.basedata.mapper.CounterpartyAccountMapper;
import com.opentms.basedata.service.CounterpartyAccountService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class CounterpartyAccountServiceImpl extends ServiceImpl<CounterpartyAccountMapper, CounterpartyAccount> implements CounterpartyAccountService {

    @Override
    public Page<CounterpartyAccount> queryPage(Long counterpartyId, String keyword, String status, int pageNum, int pageSize) {
        LambdaQueryWrapper<CounterpartyAccount> wrapper = new LambdaQueryWrapper<>();

        if (counterpartyId != null) {
            wrapper.eq(CounterpartyAccount::getCounterpartyId, counterpartyId);
        }

        if (StringUtils.hasText(keyword)) {
            wrapper.like(CounterpartyAccount::getAccountCode, keyword)
                   .or()
                   .like(CounterpartyAccount::getAccountName, keyword);
        }

        if (StringUtils.hasText(status)) {
            wrapper.eq(CounterpartyAccount::getStatus, status);
        }

        wrapper.orderByDesc(CounterpartyAccount::getCreatedAt);

        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public CounterpartyAccount getCounterpartyAccountById(Long id) {
        return getById(id);
    }

    @Override
    public boolean saveCounterpartyAccount(CounterpartyAccount account) {
        if (checkCodeExists(account.getAccountCode(), null)) {
            throw new RuntimeException("Account code already exists");
        }
        return save(account);
    }

    @Override
    public boolean updateCounterpartyAccount(CounterpartyAccount account) {
        if (checkCodeExists(account.getAccountCode(), account.getId())) {
            throw new RuntimeException("Account code already exists");
        }
        return updateById(account);
    }

    @Override
    public boolean deleteCounterpartyAccount(Long id) {
        return removeById(id);
    }

    @Override
    public boolean checkCodeExists(String code, Long excludeId) {
        if (!StringUtils.hasText(code)) {
            return false;
        }
        LambdaQueryWrapper<CounterpartyAccount> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CounterpartyAccount::getAccountCode, code);
        if (excludeId != null) {
            wrapper.ne(CounterpartyAccount::getId, excludeId);
        }
        return count(wrapper) > 0;
    }
}
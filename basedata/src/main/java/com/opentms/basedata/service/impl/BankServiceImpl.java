package com.opentms.basedata.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.opentms.basedata.entity.Bank;
import com.opentms.basedata.mapper.BankMapper;
import com.opentms.basedata.service.BankService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class BankServiceImpl extends ServiceImpl<BankMapper, Bank> implements BankService {

    @Override
    public Page<Bank> queryPage(String keyword, String countryCode, String status, int pageNum, int pageSize) {
        LambdaQueryWrapper<Bank> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(keyword)) {
            wrapper.like(Bank::getBankCode, keyword)
                   .or()
                   .like(Bank::getBankName, keyword);
        }

        if (StringUtils.hasText(countryCode)) {
            wrapper.eq(Bank::getCountryCode, countryCode);
        }

        if (StringUtils.hasText(status)) {
            wrapper.eq(Bank::getStatus, status);
        }

        wrapper.orderByDesc(Bank::getCreatedAt);

        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public Bank getBankById(Long id) {
        return getById(id);
    }

    @Override
    public boolean saveBank(Bank bank) {
        if (checkCodeExists(bank.getBankCode(), null)) {
            throw new RuntimeException("Bank code already exists");
        }
        return save(bank);
    }

    @Override
    public boolean updateBank(Bank bank) {
        if (checkCodeExists(bank.getBankCode(), bank.getId())) {
            throw new RuntimeException("Bank code already exists");
        }
        return updateById(bank);
    }

    @Override
    public boolean deleteBank(Long id) {
        return removeById(id);
    }

    @Override
    public boolean checkCodeExists(String code, Long excludeId) {
        if (!StringUtils.hasText(code)) {
            return false;
        }
        LambdaQueryWrapper<Bank> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Bank::getBankCode, code);
        if (excludeId != null) {
            wrapper.ne(Bank::getId, excludeId);
        }
        return count(wrapper) > 0;
    }
}
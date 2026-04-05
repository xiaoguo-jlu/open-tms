package com.opentms.basedata.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.opentms.basedata.entity.Currency;
import com.opentms.basedata.mapper.CurrencyMapper;
import com.opentms.basedata.service.CurrencyService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class CurrencyServiceImpl extends ServiceImpl<CurrencyMapper, Currency> implements CurrencyService {

    @Override
    public Page<Currency> queryPage(String keyword, String status, int pageNum, int pageSize) {
        LambdaQueryWrapper<Currency> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Currency::getCurrencyCode, keyword)
                   .or()
                   .like(Currency::getCurrencyName, keyword);
        }
        
        if (StringUtils.hasText(status)) {
            wrapper.eq(Currency::getStatus, status);
        }
        
        wrapper.orderByDesc(Currency::getCreatedAt);
        
        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public boolean saveCurrency(Currency currency) {
        return save(currency);
    }

    @Override
    public boolean updateCurrency(Currency currency) {
        return updateById(currency);
    }

    @Override
    public boolean deleteCurrency(Long id) {
        return removeById(id);
    }

    @Override
    public Currency getCurrencyById(Long id) {
        return getById(id);
    }
}

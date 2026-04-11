package com.opentms.basedata.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.opentms.basedata.entity.Counterparty;
import com.opentms.basedata.mapper.CounterpartyMapper;
import com.opentms.basedata.service.CounterpartyService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class CounterpartyServiceImpl extends ServiceImpl<CounterpartyMapper, Counterparty> implements CounterpartyService {

    @Override
    public Page<Counterparty> queryPage(String keyword, String counterpartyType, String countryCode, String status, int pageNum, int pageSize) {
        LambdaQueryWrapper<Counterparty> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(keyword)) {
            wrapper.like(Counterparty::getCode, keyword)
                   .or()
                   .like(Counterparty::getName, keyword);
        }

        if (StringUtils.hasText(counterpartyType)) {
            wrapper.eq(Counterparty::getCounterpartyType, counterpartyType);
        }

        if (StringUtils.hasText(countryCode)) {
            wrapper.eq(Counterparty::getCountryCode, countryCode);
        }

        if (StringUtils.hasText(status)) {
            wrapper.eq(Counterparty::getStatus, status);
        }

        wrapper.orderByDesc(Counterparty::getCreatedAt);

        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public Counterparty getCounterpartyById(Long id) {
        return getById(id);
    }

    @Override
    public boolean saveCounterparty(Counterparty counterparty) {
        if (checkCodeExists(counterparty.getCode(), null)) {
            throw new RuntimeException("Counterparty code already exists");
        }
        return save(counterparty);
    }

    @Override
    public boolean updateCounterparty(Counterparty counterparty) {
        if (checkCodeExists(counterparty.getCode(), counterparty.getId())) {
            throw new RuntimeException("Counterparty code already exists");
        }
        return updateById(counterparty);
    }

    @Override
    public boolean deleteCounterparty(Long id) {
        return removeById(id);
    }

    @Override
    public boolean checkCodeExists(String code, Long excludeId) {
        if (!StringUtils.hasText(code)) {
            return false;
        }
        LambdaQueryWrapper<Counterparty> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Counterparty::getCode, code);
        if (excludeId != null) {
            wrapper.ne(Counterparty::getId, excludeId);
        }
        return count(wrapper) > 0;
    }
}
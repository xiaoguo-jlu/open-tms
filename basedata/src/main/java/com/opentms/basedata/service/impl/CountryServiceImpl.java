package com.opentms.basedata.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.opentms.basedata.entity.Country;
import com.opentms.basedata.mapper.CountryMapper;
import com.opentms.basedata.service.CountryService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class CountryServiceImpl extends ServiceImpl<CountryMapper, Country> implements CountryService {

    @Override
    public Page<Country> queryPage(String keyword, String status, int pageNum, int pageSize) {
        LambdaQueryWrapper<Country> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(keyword)) {
            wrapper.like(Country::getCountryCode, keyword)
                   .or()
                   .like(Country::getCountryName, keyword);
        }

        if (StringUtils.hasText(status)) {
            wrapper.eq(Country::getStatus, status);
        }

        wrapper.orderByDesc(Country::getCreatedAt);

        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public Country getCountryById(Long id) {
        return getById(id);
    }

    @Override
    public boolean saveCountry(Country country) {
        if (checkCodeExists(country.getCountryCode(), null)) {
            throw new RuntimeException("Country code already exists");
        }
        return save(country);
    }

    @Override
    public boolean updateCountry(Country country) {
        if (checkCodeExists(country.getCountryCode(), country.getId())) {
            throw new RuntimeException("Country code already exists");
        }
        return updateById(country);
    }

    @Override
    public boolean deleteCountry(Long id) {
        return removeById(id);
    }

    @Override
    public boolean checkCodeExists(String code, Long excludeId) {
        if (!StringUtils.hasText(code)) {
            return false;
        }
        LambdaQueryWrapper<Country> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Country::getCountryCode, code);
        if (excludeId != null) {
            wrapper.ne(Country::getId, excludeId);
        }
        return count(wrapper) > 0;
    }
}
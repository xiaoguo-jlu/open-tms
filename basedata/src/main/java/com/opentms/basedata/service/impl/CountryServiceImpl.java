package com.opentms.basedata.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.opentms.basedata.entity.Country;
import com.opentms.basedata.mapper.CountryMapper;
import com.opentms.basedata.service.CountryService;
import com.opentms.basedata.vo.CountryVO;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class CountryServiceImpl extends ServiceImpl<CountryMapper, Country> implements CountryService {

    @Override
    public List<CountryVO> listAll() {
        LambdaQueryWrapper<Country> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Country::getStatus, "1");
        wrapper.orderByDesc(Country::getCreatedAt);
        List<Country> countries = list(wrapper);
        return countries.stream().map(this::convertToVO).toList();
    }

    @Override
    public Page<CountryVO> queryPage(String keyword, String status, int pageNum, int pageSize) {
        LambdaQueryWrapper<Country> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(keyword)) {
            wrapper.like(Country::getCode, keyword)
                   .or()
                   .like(Country::getName, keyword);
        }

        if (StringUtils.hasText(status)) {
            wrapper.eq(Country::getStatus, status);
        }

        wrapper.orderByDesc(Country::getCreatedAt);

        Page<Country> page = page(new Page<>(pageNum, pageSize), wrapper);
        Page<CountryVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(page.getRecords().stream().map(this::convertToVO).toList());
        return voPage;
    }

    @Override
    public CountryVO getCountryById(Long id) {
        Country country = getById(id);
        return country != null ? convertToVO(country) : null;
    }

    @Override
    public boolean saveCountry(Country country) {
        if (checkCodeExists(country.getCode(), null)) {
            throw new RuntimeException("Country code already exists");
        }
        // Set audit fields explicitly - MetaObjectHandler not working properly
        if (country.getCreatedBy() == null) {
            country.setCreatedBy("system");
        }
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        if (country.getCreatedAt() == null) {
            country.setCreatedAt(now);
        }
        if (country.getUpdatedAt() == null) {
            country.setUpdatedAt(now);
        }
        return save(country);
    }

    @Override
    public boolean updateCountry(Country country) {
        if (country.getId() == null) {
            throw new RuntimeException("Country ID cannot be null");
        }
        Country existing = getById(country.getId());
        if (existing == null || "1".equals(existing.getDeleted())) {
            throw new RuntimeException("Country not found");
        }
        if (checkCodeExists(country.getCode(), country.getId())) {
            throw new RuntimeException("Country code already exists");
        }
        // Set audit field explicitly
        country.setUpdatedAt(java.time.LocalDateTime.now());
        country.setUpdatedBy("system");
        return updateById(country);
    }

    @Override
    public boolean deleteCountry(Long id) {
        Country existing = getById(id);
        if (existing == null || "1".equals(existing.getDeleted())) {
            throw new RuntimeException("Country not found");
        }
        // Let @TableLogic handle soft delete - removeById with @TableLogic converts to UPDATE SET deleted='1'
        return removeById(id);
    }

    @Override
    public boolean checkCodeExists(String code, Long excludeId) {
        if (!StringUtils.hasText(code)) {
            return false;
        }
        LambdaQueryWrapper<Country> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Country::getCode, code);
        if (excludeId != null) {
            wrapper.ne(Country::getId, excludeId);
        }
        return count(wrapper) > 0;
    }

    private CountryVO convertToVO(Country country) {
        CountryVO vo = new CountryVO();
        vo.setId(country.getId());
        vo.setCode(country.getCode());
        vo.setName(country.getName());
        vo.setEnName(country.getEnName());
        vo.setTimezone(country.getTimezone());
        vo.setAreaCode(country.getAreaCode());
        vo.setStatus(country.getStatus());
        vo.setRemark(country.getRemark());
        vo.setCreatedBy(country.getCreatedBy());
        vo.setCreatedAt(country.getCreatedAt());
        vo.setUpdatedBy(country.getUpdatedBy());
        vo.setUpdatedAt(country.getUpdatedAt());
        return vo;
    }
}
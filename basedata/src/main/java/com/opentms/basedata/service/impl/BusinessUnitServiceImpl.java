package com.opentms.basedata.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.opentms.basedata.entity.BusinessUnit;
import com.opentms.basedata.mapper.BusinessUnitMapper;
import com.opentms.basedata.service.BusinessUnitService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class BusinessUnitServiceImpl extends ServiceImpl<BusinessUnitMapper, BusinessUnit> implements BusinessUnitService {

    @Override
    public Page<BusinessUnit> queryPage(String keyword, String status, int pageNum, int pageSize) {
        LambdaQueryWrapper<BusinessUnit> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(keyword)) {
            wrapper.like(BusinessUnit::getUnitCode, keyword)
                   .or()
                   .like(BusinessUnit::getUnitName, keyword);
        }

        if (StringUtils.hasText(status)) {
            wrapper.eq(BusinessUnit::getStatus, status);
        }

        wrapper.orderByDesc(BusinessUnit::getCreatedAt);

        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public BusinessUnit getBusinessUnitById(Long id) {
        return getById(id);
    }

    @Override
    public boolean saveBusinessUnit(BusinessUnit businessUnit) {
        if (checkCodeExists(businessUnit.getUnitCode(), null)) {
            throw new RuntimeException("Business unit code already exists");
        }
        return save(businessUnit);
    }

    @Override
    public boolean updateBusinessUnit(BusinessUnit businessUnit) {
        if (checkCodeExists(businessUnit.getUnitCode(), businessUnit.getId())) {
            throw new RuntimeException("Business unit code already exists");
        }
        return updateById(businessUnit);
    }

    @Override
    public boolean deleteBusinessUnit(Long id) {
        return removeById(id);
    }

    @Override
    public boolean checkCodeExists(String code, Long excludeId) {
        if (!StringUtils.hasText(code)) {
            return false;
        }
        LambdaQueryWrapper<BusinessUnit> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BusinessUnit::getUnitCode, code);
        if (excludeId != null) {
            wrapper.ne(BusinessUnit::getId, excludeId);
        }
        return count(wrapper) > 0;
    }
}
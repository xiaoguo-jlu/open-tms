package com.opentms.limit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.opentms.limit.entity.Limit;
import com.opentms.limit.mapper.LimitMapper;
import com.opentms.limit.service.LimitService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class LimitServiceImpl extends ServiceImpl<LimitMapper, Limit> implements LimitService {

    @Override
    public Page<Limit> queryPage(String keyword, String limitType, Long businessUnitId, int pageNum, int pageSize) {
        LambdaQueryWrapper<Limit> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(keyword)) {
            wrapper.like(Limit::getCode, keyword)
                   .or()
                   .like(Limit::getName, keyword)
                   .or()
                   .like(Limit::getLimitName, keyword);
        }

        if (StringUtils.hasText(limitType)) {
            wrapper.eq(Limit::getLimitType, limitType);
        }

        if (businessUnitId != null) {
            wrapper.eq(Limit::getBusinessUnitId, businessUnitId);
        }

        wrapper.orderByDesc(Limit::getCreatedAt);

        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public Limit getLimitById(Long id) {
        return getById(id);
    }

    @Override
    public boolean saveLimit(Limit limit) {
        if (checkCodeExists(limit.getCode(), null)) {
            throw new RuntimeException("Limit code already exists");
        }
        return save(limit);
    }

    @Override
    public boolean updateLimit(Limit limit) {
        if (checkCodeExists(limit.getCode(), limit.getId())) {
            throw new RuntimeException("Limit code already exists");
        }
        return updateById(limit);
    }

    @Override
    public boolean deleteLimit(Long id) {
        return removeById(id);
    }

    @Override
    public boolean checkCodeExists(String code, Long excludeId) {
        if (!StringUtils.hasText(code)) {
            return false;
        }
        LambdaQueryWrapper<Limit> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Limit::getCode, code);
        if (excludeId != null) {
            wrapper.ne(Limit::getId, excludeId);
        }
        return count(wrapper) > 0;
    }

    @Override
    public Limit monitor(Long id) {
        Limit limit = getById(id);
        if (limit == null) {
            return null;
        }
        return limit;
    }

    @Override
    public List<Limit> getAlerts() {
        LambdaQueryWrapper<Limit> wrapper = new LambdaQueryWrapper<>();
        wrapper.isNotNull(Limit::getWarningThreshold);
        wrapper.orderByDesc(Limit::getCreatedAt);
        return list(wrapper);
    }
}
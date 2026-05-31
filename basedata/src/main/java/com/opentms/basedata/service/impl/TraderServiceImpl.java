package com.opentms.basedata.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.opentms.basedata.entity.Trader;
import com.opentms.basedata.mapper.TraderMapper;
import com.opentms.basedata.service.TraderService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class TraderServiceImpl extends ServiceImpl<TraderMapper, Trader> implements TraderService {

    @Override
    public List<Trader> listAll() {
        LambdaQueryWrapper<Trader> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Trader::getDeleted, "0").eq(Trader::getStatus, "1");
        wrapper.orderByDesc(Trader::getId);
        return list(wrapper);
    }

    @Override
    public Page<Trader> queryPage(String keyword, String status, int pageNum, int pageSize) {
        LambdaQueryWrapper<Trader> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Trader::getDeleted, "0");

        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Trader::getCode, keyword).or().like(Trader::getName, keyword));
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(Trader::getStatus, status);
        }
        wrapper.orderByDesc(Trader::getCreatedAt);
        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public Trader getTraderById(Long id) {
        Trader trader = getById(id);
        if (trader != null && "0".equals(trader.getDeleted())) {
            return trader;
        }
        return null;
    }

    @Override
    public boolean saveTrader(Trader trader) {
        if (checkCodeExists(trader.getCode(), null)) {
            throw new RuntimeException("Trader code already exists");
        }
        trader.setCreatedBy("system");
        trader.setCreatedAt(java.time.LocalDateTime.now());
        trader.setStatus("1");
        return save(trader);
    }

    @Override
    public boolean updateTrader(Trader trader) {
        if (trader.getId() == null) {
            throw new RuntimeException("Trader ID cannot be null");
        }
        Trader existing = getById(trader.getId());
        if (existing == null) {
            throw new RuntimeException("Trader not found");
        }
        if (checkCodeExists(trader.getCode(), trader.getId())) {
            throw new RuntimeException("Trader code already exists");
        }
        // Preserve created audit fields and copy them to the input trader
        trader.setCreatedBy(existing.getCreatedBy());
        trader.setCreatedAt(existing.getCreatedAt());
        trader.setUpdatedBy("system");
        trader.setUpdatedAt(java.time.LocalDateTime.now());
        return updateById(trader);
    }

    @Override
    public boolean deleteTrader(Long id) {
        Trader existing = getById(id);
        if (existing == null) {
            throw new RuntimeException("Trader not found");
        }
        return removeById(id);
    }

    @Override
    public boolean checkCodeExists(String code, Long excludeId) {
        if (!StringUtils.hasText(code)) {
            return false;
        }
        LambdaQueryWrapper<Trader> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Trader::getCode, code).eq(Trader::getDeleted, "0");
        if (excludeId != null) {
            wrapper.ne(Trader::getId, excludeId);
        }
        return count(wrapper) > 0;
    }
}
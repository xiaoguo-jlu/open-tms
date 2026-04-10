package com.opentms.basedata.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.opentms.basedata.entity.Trader;
import com.opentms.basedata.mapper.TraderMapper;
import com.opentms.basedata.service.TraderService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class TraderServiceImpl extends ServiceImpl<TraderMapper, Trader> implements TraderService {

    @Override
    public Page<Trader> queryPage(String keyword, String status, int pageNum, int pageSize) {
        LambdaQueryWrapper<Trader> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(keyword)) {
            wrapper.like(Trader::getTraderCode, keyword)
                   .or()
                   .like(Trader::getTraderName, keyword);
        }

        if (StringUtils.hasText(status)) {
            wrapper.eq(Trader::getStatus, status);
        }

        wrapper.orderByDesc(Trader::getCreatedAt);

        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public Trader getTraderById(Long id) {
        return getById(id);
    }

    @Override
    public boolean saveTrader(Trader trader) {
        if (checkCodeExists(trader.getTraderCode(), null)) {
            throw new RuntimeException("Trader code already exists");
        }
        return save(trader);
    }

    @Override
    public boolean updateTrader(Trader trader) {
        if (checkCodeExists(trader.getTraderCode(), trader.getId())) {
            throw new RuntimeException("Trader code already exists");
        }
        return updateById(trader);
    }

    @Override
    public boolean deleteTrader(Long id) {
        return removeById(id);
    }

    @Override
    public boolean checkCodeExists(String code, Long excludeId) {
        if (!StringUtils.hasText(code)) {
            return false;
        }
        LambdaQueryWrapper<Trader> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Trader::getTraderCode, code);
        if (excludeId != null) {
            wrapper.ne(Trader::getId, excludeId);
        }
        return count(wrapper) > 0;
    }
}
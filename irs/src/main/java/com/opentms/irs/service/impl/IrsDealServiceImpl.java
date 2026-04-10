package com.opentms.irs.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.opentms.irs.entity.IrsDeal;
import com.opentms.irs.mapper.IrsDealMapper;
import com.opentms.irs.service.IrsDealService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class IrsDealServiceImpl extends ServiceImpl<IrsDealMapper, IrsDeal> implements IrsDealService {

    @Override
    public Page<IrsDeal> queryPage(String keyword, String dealType, String status, String counterpartyId, int pageNum, int pageSize) {
        LambdaQueryWrapper<IrsDeal> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(keyword)) {
            wrapper.like(IrsDeal::getDealCode, keyword);
        }

        if (StringUtils.hasText(dealType)) {
            wrapper.eq(IrsDeal::getDealType, dealType);
        }

        if (StringUtils.hasText(status)) {
            wrapper.eq(IrsDeal::getStatus, status);
        }

        if (StringUtils.hasText(counterpartyId)) {
            wrapper.eq(IrsDeal::getCounterpartyId, Long.parseLong(counterpartyId));
        }

        wrapper.orderByDesc(IrsDeal::getCreatedAt);

        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public IrsDeal getIrsDealById(Long id) {
        return getById(id);
    }

    @Override
    public boolean saveIrsDeal(IrsDeal irsDeal) {
        if (StringUtils.hasText(irsDeal.getDealCode()) && checkCodeExists(irsDeal.getDealCode(), null)) {
            throw new RuntimeException("Deal code already exists");
        }
        if (irsDeal.getStatus() == null) {
            irsDeal.setStatus("DRAFT");
        }
        return save(irsDeal);
    }

    @Override
    public boolean updateIrsDeal(IrsDeal irsDeal) {
        if (StringUtils.hasText(irsDeal.getDealCode()) && checkCodeExists(irsDeal.getDealCode(), irsDeal.getId())) {
            throw new RuntimeException("Deal code already exists");
        }
        return updateById(irsDeal);
    }

    @Override
    public boolean deleteIrsDeal(Long id) {
        return removeById(id);
    }

    @Override
    public boolean submit(Long id) {
        IrsDeal deal = getById(id);
        if (deal == null) {
            throw new RuntimeException("Deal not found");
        }
        if (!"DRAFT".equals(deal.getStatus())) {
            throw new RuntimeException("Only DRAFT status can be submitted");
        }
        deal.setStatus("PENDING");
        return updateById(deal);
    }

    @Override
    public boolean approve(Long id) {
        IrsDeal deal = getById(id);
        if (deal == null) {
            throw new RuntimeException("Deal not found");
        }
        if (!"PENDING".equals(deal.getStatus())) {
            throw new RuntimeException("Only PENDING status can be approved");
        }
        deal.setStatus("APPROVED");
        return updateById(deal);
    }

    private boolean checkCodeExists(String code, Long excludeId) {
        if (!StringUtils.hasText(code)) {
            return false;
        }
        LambdaQueryWrapper<IrsDeal> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(IrsDeal::getDealCode, code);
        if (excludeId != null) {
            wrapper.ne(IrsDeal::getId, excludeId);
        }
        return count(wrapper) > 0;
    }
}
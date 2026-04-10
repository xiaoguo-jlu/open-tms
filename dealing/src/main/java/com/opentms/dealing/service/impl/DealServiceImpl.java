package com.opentms.dealing.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.opentms.dealing.entity.Deal;
import com.opentms.dealing.mapper.DealMapper;
import com.opentms.dealing.service.DealService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;

@Service
public class DealServiceImpl extends ServiceImpl<DealMapper, Deal> implements DealService {

    private static final String STATUS_DRAFT = "DRAFT";
    private static final String STATUS_PENDING_APPROVE = "PENDING_APPROVE";
    private static final String STATUS_APPROVED = "APPROVED";
    private static final String STATUS_REJECTED = "REJECTED";
    private static final String STATUS_SETTLED = "SETTLED";

    @Override
    public Page<Deal> queryPage(String keyword, String dealType, String status, String counterpartyId, int pageNum, int pageSize) {
        LambdaQueryWrapper<Deal> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(keyword)) {
            wrapper.like(Deal::getDealNo, keyword)
                   .or()
                   .like(Deal::getName, keyword);
        }

        if (StringUtils.hasText(dealType)) {
            wrapper.eq(Deal::getDealType, dealType);
        }

        if (StringUtils.hasText(status)) {
            wrapper.eq(Deal::getStatus, status);
        }

        if (StringUtils.hasText(counterpartyId)) {
            wrapper.eq(Deal::getCounterpartyId, Long.parseLong(counterpartyId));
        }

        wrapper.orderByDesc(Deal::getCreatedAt);

        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public Deal getDealById(Long id) {
        return getById(id);
    }

    @Override
    public boolean saveDeal(Deal deal) {
        deal.setStatus(STATUS_DRAFT);
        return save(deal);
    }

    @Override
    public boolean updateDeal(Deal deal) {
        Deal existing = getById(deal.getId());
        if (existing == null) {
            throw new RuntimeException("Deal not found");
        }
        if (STATUS_APPROVED.equals(existing.getStatus()) || STATUS_SETTLED.equals(existing.getStatus())) {
            throw new RuntimeException("Cannot update approved or settled deal");
        }
        return updateById(deal);
    }

    @Override
    public boolean deleteDeal(Long id) {
        Deal deal = getById(id);
        if (deal == null) {
            throw new RuntimeException("Deal not found");
        }
        if (STATUS_APPROVED.equals(deal.getStatus()) || STATUS_SETTLED.equals(deal.getStatus())) {
            throw new RuntimeException("Cannot delete approved or settled deal");
        }
        return removeById(id);
    }

    @Override
    public boolean submitDeal(Long id) {
        Deal deal = getById(id);
        if (deal == null) {
            throw new RuntimeException("Deal not found");
        }
        if (!STATUS_DRAFT.equals(deal.getStatus())) {
            throw new RuntimeException("Only draft deal can be submitted");
        }
        deal.setStatus(STATUS_PENDING_APPROVE);
        return updateById(deal);
    }

    @Override
    public boolean approveDeal(Long id) {
        Deal deal = getById(id);
        if (deal == null) {
            throw new RuntimeException("Deal not found");
        }
        if (!STATUS_PENDING_APPROVE.equals(deal.getStatus())) {
            throw new RuntimeException("Only pending approve deal can be approved");
        }
        deal.setStatus(STATUS_APPROVED);
        return updateById(deal);
    }

    @Override
    public boolean rejectDeal(Long id) {
        Deal deal = getById(id);
        if (deal == null) {
            throw new RuntimeException("Deal not found");
        }
        if (!STATUS_PENDING_APPROVE.equals(deal.getStatus())) {
            throw new RuntimeException("Only pending approve deal can be rejected");
        }
        deal.setStatus(STATUS_REJECTED);
        return updateById(deal);
    }

    @Override
    public boolean batchDelete(String ids) {
        if (!StringUtils.hasText(ids)) {
            throw new RuntimeException("IDs cannot be empty");
        }
        List<Long> idList = Arrays.stream(ids.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .map(Long::parseLong)
                .toList();
        
        if (idList.isEmpty()) {
            throw new RuntimeException("No valid IDs provided");
        }
        
        return removeByIds(idList);
    }
}
package com.opentms.fx.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.opentms.fx.entity.FxDeal;
import com.opentms.fx.mapper.FxDealMapper;
import com.opentms.fx.service.FxDealService;
import com.opentms.fx.vo.FxDealVO;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

@Service
public class FxDealServiceImpl extends ServiceImpl<FxDealMapper, FxDeal> implements FxDealService {

    private static final String STATUS_DRAFT = "DRAFT";
    private static final String STATUS_SUBMITTED = "SUBMITTED";
    private static final String STATUS_APPROVED = "APPROVED";
    private static final String STATUS_EXECUTED = "EXECUTED";
    private static final String STATUS_CANCELLED = "CANCELLED";

    private static final Map<String, String> STATUS_TRANSITIONS = new HashMap<>();

    static {
        STATUS_TRANSITIONS.put(STATUS_DRAFT, STATUS_SUBMITTED);
        STATUS_TRANSITIONS.put(STATUS_SUBMITTED, STATUS_APPROVED);
        STATUS_TRANSITIONS.put(STATUS_APPROVED, STATUS_EXECUTED);
    }

    @Override
    public IPage<FxDealVO> queryPage(String keyword, String fxType, String status, int pageNum, int pageSize) {
        LambdaQueryWrapper<FxDeal> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(keyword)) {
            wrapper.like(FxDeal::getCode, keyword)
                   .or()
                   .like(FxDeal::getName, keyword);
        }

        if (StringUtils.hasText(fxType)) {
            wrapper.eq(FxDeal::getFxType, fxType);
        }

        if (StringUtils.hasText(status)) {
            wrapper.eq(FxDeal::getStatus, status);
        }

        wrapper.orderByDesc(FxDeal::getCreatedAt);

        Page<FxDeal> page = new Page<>(pageNum, pageSize);
        IPage<FxDeal> result = this.page(page, wrapper);
        return result.convert(this::convertToVO);
    }

    @Override
    public FxDealVO getFxDealById(Long id) {
        FxDeal fxDeal = getById(id);
        if (fxDeal == null) {
            return null;
        }
        return convertToVO(fxDeal);
    }

    @Override
    public boolean saveFxDeal(FxDeal fxDeal) {
        fxDeal.setStatus(STATUS_DRAFT);
        return save(fxDeal);
    }

    @Override
    public boolean updateFxDeal(FxDeal fxDeal) {
        FxDeal existing = getById(fxDeal.getId());
        if (existing == null) {
            throw new RuntimeException("FX Deal not found");
        }
        if (!STATUS_DRAFT.equals(existing.getStatus())) {
            throw new RuntimeException("Only draft status can be updated");
        }
        return updateById(fxDeal);
    }

    @Override
    public boolean deleteFxDeal(Long id) {
        FxDeal existing = getById(id);
        if (existing == null) {
            throw new RuntimeException("FX Deal not found");
        }
        if (!STATUS_DRAFT.equals(existing.getStatus())) {
            throw new RuntimeException("Only draft status can be deleted");
        }
        return removeById(id);
    }

    @Override
    public boolean submitFxDeal(Long id) {
        return transitionStatus(id, STATUS_SUBMITTED);
    }

    @Override
    public boolean approveFxDeal(Long id) {
        return transitionStatus(id, STATUS_APPROVED);
    }

    @Override
    public boolean executeFxDeal(Long id) {
        return transitionStatus(id, STATUS_EXECUTED);
    }

    private boolean transitionStatus(Long id, String targetStatus) {
        FxDeal fxDeal = getById(id);
        if (fxDeal == null) {
            throw new RuntimeException("FX Deal not found");
        }
        String currentStatus = fxDeal.getStatus();
        String expectedStatus = getExpectedStatus(targetStatus);
        if (!expectedStatus.equals(currentStatus)) {
            throw new RuntimeException("Invalid status transition. Current status: " + currentStatus + ", expected: " + expectedStatus);
        }
        fxDeal.setStatus(targetStatus);
        return updateById(fxDeal);
    }

    private String getExpectedStatus(String targetStatus) {
        for (Map.Entry<String, String> entry : STATUS_TRANSITIONS.entrySet()) {
            if (entry.getValue().equals(targetStatus)) {
                return entry.getKey();
            }
        }
        return null;
    }

    private FxDealVO convertToVO(FxDeal fxDeal) {
        FxDealVO vo = new FxDealVO();
        vo.setId(fxDeal.getId());
        vo.setCode(fxDeal.getCode());
        vo.setName(fxDeal.getName());
        vo.setFxType(fxDeal.getFxType());
        vo.setBuyCurrency(fxDeal.getBuyCurrency());
        vo.setSellCurrency(fxDeal.getSellCurrency());
        vo.setAmount(fxDeal.getAmount());
        vo.setRate(fxDeal.getRate());
        vo.setCounterpartyId(fxDeal.getCounterpartyId());
        vo.setAccountId(fxDeal.getAccountId());
        vo.setValueDate(fxDeal.getValueDate());
        vo.setStatus(fxDeal.getStatus());
        vo.setRemark(fxDeal.getRemark());
        vo.setCreatedAt(fxDeal.getCreatedAt());
        vo.setUpdatedAt(fxDeal.getUpdatedAt());
        return vo;
    }
}
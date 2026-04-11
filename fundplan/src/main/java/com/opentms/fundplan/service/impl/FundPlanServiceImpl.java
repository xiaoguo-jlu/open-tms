package com.opentms.fundplan.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.opentms.fundplan.entity.FundPlan;
import com.opentms.fundplan.mapper.FundPlanMapper;
import com.opentms.fundplan.service.FundPlanService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class FundPlanServiceImpl extends ServiceImpl<FundPlanMapper, FundPlan> implements FundPlanService {

    @Override
    public Page<FundPlan> queryPage(String keyword, Integer planYear, String planType, String status, int pageNum, int pageSize) {
        LambdaQueryWrapper<FundPlan> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(keyword)) {
            wrapper.like(FundPlan::getPlanNo, keyword);
        }

        if (planYear != null) {
            wrapper.eq(FundPlan::getPlanNo, planYear);
        }

        if (StringUtils.hasText(planType)) {
            wrapper.eq(FundPlan::getPlanType, planType);
        }

        if (StringUtils.hasText(status)) {
            wrapper.eq(FundPlan::getStatus, status);
        }

        wrapper.orderByDesc(FundPlan::getCreatedAt);

        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public List<FundPlan> getAnnualPlans(Integer planYear, Long businessUnitId) {
        LambdaQueryWrapper<FundPlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FundPlan::getPlanType, "ANNUAL");
        if (planYear != null) {
            wrapper.eq(FundPlan::getPlanNo, planYear);
        }
        if (businessUnitId != null) {
            wrapper.eq(FundPlan::getBusinessUnitId, businessUnitId);
        }
        wrapper.orderByDesc(FundPlan::getPlanNo);
        return list(wrapper);
    }

    @Override
    public List<FundPlan> getMonthlyPlans(Integer planYear, Integer planMonth, Long businessUnitId) {
        LambdaQueryWrapper<FundPlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FundPlan::getPlanType, "MONTHLY");
        if (planYear != null) {
            wrapper.eq(FundPlan::getPlanNo, planYear);
        }
        if (planMonth != null) {
            wrapper.eq(FundPlan::getPlanName, planMonth);
        }
        if (businessUnitId != null) {
            wrapper.eq(FundPlan::getBusinessUnitId, businessUnitId);
        }
        wrapper.orderByDesc(FundPlan::getPlanNo, FundPlan::getPlanName);
        return list(wrapper);
    }

    @Override
    public FundPlan getFundPlanById(Long id) {
        return getById(id);
    }

    @Override
    public boolean saveFundPlan(FundPlan fundPlan) {
        fundPlan.setStatus("DRAFT");
        return save(fundPlan);
    }

    @Override
    public boolean updateFundPlan(FundPlan fundPlan) {
        return updateById(fundPlan);
    }

    @Override
    public boolean deleteFundPlan(Long id) {
        FundPlan fundPlan = getById(id);
        if (fundPlan == null) {
            return false;
        }
        if (!"DRAFT".equals(fundPlan.getStatus())) {
            throw new RuntimeException("Only DRAFT status fund plan can be deleted");
        }
        return removeById(id);
    }

    @Override
    public boolean submitFundPlan(Long id) {
        FundPlan fundPlan = getById(id);
        if (fundPlan == null) {
            throw new RuntimeException("Fund plan not found");
        }
        if (!"DRAFT".equals(fundPlan.getStatus())) {
            throw new RuntimeException("Only DRAFT status fund plan can be submitted");
        }
        fundPlan.setStatus("PENDING_APPROVE");
        return updateById(fundPlan);
    }

    @Override
    public boolean approveFundPlan(Long id) {
        FundPlan fundPlan = getById(id);
        if (fundPlan == null) {
            throw new RuntimeException("Fund plan not found");
        }
        if (!"PENDING_APPROVE".equals(fundPlan.getStatus())) {
            throw new RuntimeException("Only PENDING_APPROVE status fund plan can be approved");
        }
        fundPlan.setStatus("APPROVED");
        return updateById(fundPlan);
    }

    @Override
    public boolean lockFundPlan(Long id) {
        FundPlan fundPlan = getById(id);
        if (fundPlan == null) {
            throw new RuntimeException("Fund plan not found");
        }
        if (!"APPROVED".equals(fundPlan.getStatus())) {
            throw new RuntimeException("Only APPROVED status fund plan can be locked");
        }
        fundPlan.setStatus("LOCKED");
        return updateById(fundPlan);
    }
}
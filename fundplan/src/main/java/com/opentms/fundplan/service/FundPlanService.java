package com.opentms.fundplan.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.opentms.fundplan.entity.FundPlan;

import java.util.List;

public interface FundPlanService {

    Page<FundPlan> queryPage(String keyword, Integer planYear, String planType, String status, int pageNum, int pageSize);

    List<FundPlan> getAnnualPlans(Integer planYear, Long businessUnitId);

    List<FundPlan> getMonthlyPlans(Integer planYear, Integer planMonth, Long businessUnitId);

    FundPlan getFundPlanById(Long id);

    boolean saveFundPlan(FundPlan fundPlan);

    boolean updateFundPlan(FundPlan fundPlan);

    boolean deleteFundPlan(Long id);

    boolean submitFundPlan(Long id);

    boolean approveFundPlan(Long id);

    boolean lockFundPlan(Long id);
}
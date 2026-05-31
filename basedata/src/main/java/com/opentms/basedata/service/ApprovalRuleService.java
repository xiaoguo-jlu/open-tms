package com.opentms.basedata.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.opentms.basedata.entity.ApprovalRule;

public interface ApprovalRuleService {

    Page<ApprovalRule> queryPage(String keyword, String bizType, String status, int pageNum, int pageSize);

    ApprovalRule getApprovalRuleById(Long id);

    boolean saveApprovalRule(ApprovalRule approvalRule);

    boolean updateApprovalRule(ApprovalRule approvalRule);

    boolean deleteApprovalRule(Long id);

    boolean checkCodeExists(String ruleCode, Long excludeId);
}
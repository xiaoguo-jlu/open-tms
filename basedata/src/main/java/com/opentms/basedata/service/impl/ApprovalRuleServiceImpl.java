package com.opentms.basedata.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.opentms.basedata.entity.ApprovalRule;
import com.opentms.basedata.mapper.ApprovalRuleMapper;
import com.opentms.basedata.service.ApprovalRuleService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class ApprovalRuleServiceImpl extends ServiceImpl<ApprovalRuleMapper, ApprovalRule> implements ApprovalRuleService {

    @Override
    public Page<ApprovalRule> queryPage(String keyword, String bizType, String status, int pageNum, int pageSize) {
        LambdaQueryWrapper<ApprovalRule> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(keyword)) {
            wrapper.like(ApprovalRule::getRuleCode, keyword)
                   .or()
                   .like(ApprovalRule::getRuleName, keyword);
        }

        if (StringUtils.hasText(bizType)) {
            wrapper.eq(ApprovalRule::getBizType, bizType);
        }

        if (StringUtils.hasText(status)) {
            wrapper.eq(ApprovalRule::getStatus, status);
        }

        wrapper.orderByDesc(ApprovalRule::getCreatedAt);

        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public ApprovalRule getApprovalRuleById(Long id) {
        return getById(id);
    }

    @Override
    public boolean saveApprovalRule(ApprovalRule approvalRule) {
        if (checkCodeExists(approvalRule.getRuleCode(), null)) {
            throw new RuntimeException("Rule code already exists");
        }
        return save(approvalRule);
    }

    @Override
    public boolean updateApprovalRule(ApprovalRule approvalRule) {
        if (approvalRule.getId() == null) {
            throw new RuntimeException("Approval rule ID cannot be null");
        }
        ApprovalRule existing = getById(approvalRule.getId());
        if (existing == null) {
            throw new RuntimeException("Approval rule not found");
        }
        if (checkCodeExists(approvalRule.getRuleCode(), approvalRule.getId())) {
            throw new RuntimeException("Rule code already exists");
        }
        return updateById(approvalRule);
    }

    @Override
    public boolean deleteApprovalRule(Long id) {
        ApprovalRule existing = getById(id);
        if (existing == null) {
            throw new RuntimeException("Approval rule not found");
        }
        return removeById(id);
    }

    @Override
    public boolean checkCodeExists(String ruleCode, Long excludeId) {
        if (!StringUtils.hasText(ruleCode)) {
            return false;
        }
        LambdaQueryWrapper<ApprovalRule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApprovalRule::getRuleCode, ruleCode);
        if (excludeId != null) {
            wrapper.ne(ApprovalRule::getId, excludeId);
        }
        return count(wrapper) > 0;
    }
}
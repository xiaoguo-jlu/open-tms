package com.opentms.basedata.vo;

import com.opentms.basedata.entity.DealApprovalRule;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 交易审批规则 VO(API 返回对象,含展示字段 + JSONB 角色数组)
 *
 * @author Open-TMS
 * @since 2026-07-11
 */
@Data
public class DealApprovalRuleVO {

    private Long id;
    private String ruleNumber;
    private Long managementEntityId;
    private String managementEntityName;
    private Long counterpartyId;
    private String counterpartyName;
    private Long instrumentId;
    private String instrumentName;
    private Long dealerId;
    private String dealerName;
    private String actionType;
    private String actionTypeLabel;
    private String approvalLevel;
    private List<String> level1Roles;
    private List<String> level2Roles;
    private Integer priority;
    private String status;
    private LocalDate startDate;
    private LocalDate endDate;
    private String description;
    private String remark;
    private String lockToken;
    private String lockedBy;
    private LocalDateTime lockedAt;
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;
    private Integer version;
    private String deleted;

    public static DealApprovalRuleVO from(DealApprovalRule rule) {
        if (rule == null) return null;
        DealApprovalRuleVO vo = new DealApprovalRuleVO();
        BeanUtils.copyProperties(rule, vo);
        // JSONB 字符串 → List<String>(主表 level1Roles/level2Roles 字段为 JSONB 序列化字符串)
        vo.setLevel1Roles(parseJsonArray(rule.getLevel1Roles()));
        vo.setLevel2Roles(parseJsonArray(rule.getLevel2Roles()));
        // actionType 中文标签
        vo.setActionTypeLabel(actionLabel(rule.getActionType()));
        return vo;
    }

    private static List<String> parseJsonArray(String json) {
        if (json == null || json.isEmpty() || "[]".equals(json)) return new ArrayList<>();
        // 简化的 JSON 数组解析(支持 ["A","B"] 格式,避免引入 Jackson 依赖)
        String trimmed = json.trim();
        if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
            String inner = trimmed.substring(1, trimmed.length() - 1).trim();
            if (inner.isEmpty()) return new ArrayList<>();
            List<String> result = new ArrayList<>();
            for (String s : inner.split(",")) {
                String v = s.trim();
                if (v.startsWith("\"") && v.endsWith("\"") && v.length() >= 2) {
                    v = v.substring(1, v.length() - 1);
                }
                if (!v.isEmpty()) result.add(v);
            }
            return result;
        }
        return new ArrayList<>();
    }

    private static String actionLabel(String actionType) {
        if (actionType == null) return null;
        return switch (actionType) {
            case "CREATE" -> "创建";
            case "SUBMIT" -> "提交审批";
            case "APPROVE" -> "审批通过";
            case "REJECT" -> "驳回";
            case "EXECUTE" -> "执行";
            default -> actionType;
        };
    }
}
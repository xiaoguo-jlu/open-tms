package com.opentms.basedata.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * match 端点响应 VO
 *
 * <p>★ 本特性关键:返回命中的规则 + 实际需要审批层级 + 角色列表 + 全部候选(可解释)。</p>
 *
 * @author Open-TMS
 * @since 2026-07-11
 */
@Data
public class DealApprovalRuleMatchResponseVO {

    /** 是否命中规则 */
    private Boolean matched;

    /** 审批层级:LEVEL_0 / LEVEL_1 / LEVEL_2(未命中时为 null) */
    private String approvalLevel;

    /** L1 角色列表 */
    private List<String> level1Roles = new ArrayList<>();

    /** L2 角色列表 */
    private List<String> level2Roles = new ArrayList<>();

    /** 命中的规则摘要 */
    private MatchedRuleSummary matchedRule;

    /** 全部候选(按 specificityScore DESC 排序) */
    private List<MatchCandidate> candidates = new ArrayList<>();

    /** 命中维度列表(便于前端展示) */
    private List<String> matchedDimensions = new ArrayList<>();

    /** 降级策略提示(null 表示无降级,新规则直接命中;非 null 表示降级到旧逻辑) */
    private String fallbackStrategy;

    /** 缓存命中标记(true = 命中内存缓存,省去 DB 查询) */
    private Boolean cacheHit = false;

    @Data
    public static class MatchedRuleSummary {
        private String ruleNumber;
        private Integer priority;
        private Integer specificityScore;
        private String description;
    }

    @Data
    public static class MatchCandidate {
        private String ruleNumber;
        private Integer specificityScore;
        private Integer priority;
        private String approvalLevel;
        private List<String> matchedDimensions;
        /** 是否最终胜出 */
        private Boolean won;
    }
}
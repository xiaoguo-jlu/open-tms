package com.opentms.basedata.vo;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * 交易审批规则被引用数 VO
 *
 * <p>Phase 1 暂返回全 0(参考 v1.1 RuleReferenceCountVO 风格,后续 P1+ 优化)。</p>
 *
 * @author Open-TMS
 * @since 2026-07-11
 */
@Data
public class DealApprovalRuleReferenceCountVO {

    private Long ruleId;

    /** 总引用次数 */
    private Integer totalCount;

    /** 按 actionType 分组的引用数 */
    private Map<String, Integer> byActionType = new HashMap<>();

    /** 查询耗时(ms) */
    private Long queryDurationMs;
}
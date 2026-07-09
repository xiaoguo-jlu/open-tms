package com.opentms.basedata.vo;

import lombok.Data;

/**
 * ★ v1.1 双方向匹配结果(返回 inflow + outflow 两个账户)
 *
 * @author Open-TMS
 * @since 2026-07-08
 */
@Data
public class RuleDualMatchResultVO {

    private RuleMatchResultVO inflow;

    private RuleMatchResultVO outflow;

    private Boolean cacheHit;

    private Long queryDurationMs;

    public static RuleDualMatchResultVO empty() {
        RuleDualMatchResultVO vo = new RuleDualMatchResultVO();
        RuleMatchResultVO empty = new RuleMatchResultVO();
        empty.setMatched(false);
        empty.setBankAccountId(null);
        vo.setInflow(empty);
        vo.setOutflow(clone(empty));
        return vo;
    }

    private static RuleMatchResultVO clone(RuleMatchResultVO src) {
        RuleMatchResultVO copy = new RuleMatchResultVO();
        copy.setMatched(false);
        copy.setBankAccountId(null);
        return copy;
    }
}
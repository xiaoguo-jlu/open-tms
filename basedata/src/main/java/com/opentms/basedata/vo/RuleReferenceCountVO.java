package com.opentms.basedata.vo;

import lombok.Data;

/**
 * 被引用数响应(★ v1.1 新增)
 *
 * @author Open-TMS
 * @since 2026-07-08
 */
@Data
public class RuleReferenceCountVO {

    private Long ruleId;

    private Long bankAccountId;

    private Integer unsettledCount;

    private Integer recentSettledCount;

    private Integer totalCount;

    private Long queryDurationMs;
}
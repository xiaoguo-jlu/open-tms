package com.opentms.basedata.vo;

import lombok.Data;

/**
 * 交易审批规则摘要 VO(供 dealing 端使用,避免循环依赖)
 *
 * <p>轻量级 DTO,只含 match 决策所需核心字段,dealing 模块通过 BasedataMatchClient
 * 跨服务调用后可直接 cast / 反序列化为本类型。</p>
 *
 * @author Open-TMS
 * @since 2026-07-11
 */
@Data
public class DealApprovalRuleSummaryVO {

    private Boolean matched;
    private String approvalLevel;
    private java.util.List<String> level1Roles = new java.util.ArrayList<>();
    private java.util.List<String> level2Roles = new java.util.ArrayList<>();
    /** 命中的 ruleNumber */
    private String ruleNumber;
    /** specificityScore */
    private Integer specificityScore;
}
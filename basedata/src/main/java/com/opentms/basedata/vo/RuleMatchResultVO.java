package com.opentms.basedata.vo;

import lombok.Data;

/**
 * 单方向匹配结果
 *
 * @author Open-TMS
 * @since 2026-07-08
 */
@Data
public class RuleMatchResultVO {
    private Boolean matched;
    private Long bankAccountId;
    private String bankAccountName;
    private Long ruleId;
    private String ruleNumber;
    private Integer priority;
}
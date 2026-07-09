package com.opentms.basedata.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * 更新规则 DTO(★ v1.1 带 lockToken)
 *
 * <p>调用方必须传入 lockToken(从前次查询的 detail 接口获取),后端校验失败返回 409 Conflict
 *
 * @author Open-TMS
 * @since 2026-07-08
 */
@Data
public class DefaultBankAccountRuleUpdateDTO {

    private Long id;

    /** ★ v1.1 必填,从 detail 获取 */
    private String lockToken;

    private Long counterpartyId;

    private Long instrumentId;

    private String direction;

    private String currency;

    private Long bankAccountId;

    private Integer priority;

    private LocalDate startDate;

    private String status;

    private String description;

    private String remark;

    private Integer version;
}
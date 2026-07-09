package com.opentms.basedata.dto;

import lombok.Data;

/**
 * 默认银行账户规则分页查询 DTO
 *
 * @author Open-TMS
 * @since 2026-07-08
 */
@Data
public class DefaultBankAccountRuleQueryDTO {

    private Long managementEntityId;

    private Long counterpartyId;

    private Long instrumentId;

    private String direction;

    private String currency;

    private String status;

    private String keyword;

    private Integer pageNum = 1;

    private Integer pageSize = 20;
}
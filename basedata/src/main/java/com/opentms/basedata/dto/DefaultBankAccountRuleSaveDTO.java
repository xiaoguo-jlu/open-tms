package com.opentms.basedata.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * 新增规则 DTO
 *
 * @author Open-TMS
 * @since 2026-07-08
 */
@Data
public class DefaultBankAccountRuleSaveDTO {

    private Long managementEntityId;

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
}
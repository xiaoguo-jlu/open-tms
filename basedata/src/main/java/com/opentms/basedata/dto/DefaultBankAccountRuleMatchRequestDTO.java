package com.opentms.basedata.dto;

import lombok.Data;

/**
 * match 接口入参 DTO(★ v1.1 双方向)
 *
 * @author Open-TMS
 * @since 2026-07-08
 */
@Data
public class DefaultBankAccountRuleMatchRequestDTO {

    private Long managementEntityId;

    private Long counterpartyId;

    private Long instrumentId;

    private String direction;

    private String currency;

    /** ★ v1.1 是否双方向匹配,FX 录入必须传 true */
    private Boolean dualDirection = false;
}
package com.opentms.dealing.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * FX 后端统一计算请求（v3.2 - 单一计算源）
 * <p>所有字段均可选，至少填 2 个金额/汇率字段才会计算</p>
 * <p>联动规则：</p>
 * <ul>
 *     <li>buyAmount = sellAmount × exchangeRate</li>
 *     <li>exchangeRate = marketRate + spreadBp / 10000</li>
 *     <li>termDays = valueDate - tradeDate</li>
 *     <li>maturityDate = valueDate</li>
 * </ul>
 */
@Data
public class FxCalculateRequest {

    private BigDecimal sellAmount;

    private BigDecimal buyAmount;

    private BigDecimal exchangeRate;

    private BigDecimal marketRate;

    private BigDecimal spreadBp;

    private LocalDate tradeDate;

    private LocalDate valueDate;
}
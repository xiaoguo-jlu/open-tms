package com.opentms.dealing.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * FX 后端统一计算响应（v3.2）
 * <p>返回后端补全后的完整字段集</p>
 */
@Data
public class FxCalculateResponse {

    private BigDecimal sellAmount;

    private BigDecimal buyAmount;

    private BigDecimal exchangeRate;

    private BigDecimal marketRate;

    private BigDecimal spreadBp;

    private LocalDate tradeDate;

    private LocalDate valueDate;

    /** 期限（天数） = valueDate - tradeDate */
    private Integer termDays;

    /** 到期日 = valueDate（冗余，便于前端显示） */
    private LocalDate maturityDate;
}
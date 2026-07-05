package com.opentms.dealing.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * NDF RATE_FIX 请求 DTO（Phase 1）
 * <p>用于 NDF 交易的 Rate Fix 操作，支持 fixDate / fixCurrency / fixMarketRate / verifierBy / fixRemark</p>
 */
@Data
public class RateFixRequest {

    /** Fixing 汇率（必填） */
    @NotNull(message = "fixingRate 不能为空")
    private BigDecimal fixingRate;

    /** Fixing 执行日期（可选，默认 = deal.valueDate） */
    private LocalDate fixDate;

    /** Fixing 结算币种（可选，默认 = deal.buyCurrency，下拉单选从 buy/sell 二选一） */
    private String fixCurrency;

    /** Fixing 时的市场参考汇率（可选） */
    private BigDecimal fixMarketRate;

    /** 操作人（必填） */
    @NotNull(message = "operator 不能为空")
    private String operator;

    /** 复核人（可选） */
    private String verifierBy;

    /** RATE_FIX 备注（可选） */
    private String fixRemark;
}

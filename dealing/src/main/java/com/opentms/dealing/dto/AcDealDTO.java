package com.opentms.dealing.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * AC 交易请求 DTO（v2.0 - Action 多对一）
 */
@Data
public class AcDealDTO {

    /** 主键（更新时必填） */
    private Long id;

    /** Deal 编号（更新时必填） */
    private String dealNumber;

    /** 交易类型：固定 "AC" */
    private String dealType = "AC";

    /** 管理主体编码（必填） */
    private String managementEntity;

    /** 交易对手 ID */
    private Long counterpartyId;

    /** 金融工具 ID */
    private Long instrumentId;

    /** 交易员 ID（必填） */
    private Long traderId;

    /** 方向：Inflow / Outflow */
    private String direction;

    /** 金额（必填，> 0，DECIMAL(38,18)） */
    private BigDecimal amount;

    /** 币种（必填） */
    private String currency;

    /** 交易日期（必填） */
    private LocalDate dealDate;

    /** 起息日（必填，>= dealDate） */
    private LocalDate valueDate;

    /** 描述 */
    private String description;

    /** 备注 */
    private String remark;

    // ============ AC 交易个性化字段 ============

    /** 本方银行账户 ID（必填） */
    private Long bankAccountId;

    /** 对手方账户 ID */
    private Long counterpartyAccountId;

    /** 支付方式：TRANSFER / CHECK / OTHER */
    private String paymentMethod;

    // ============ 操作人 ============

    /** 操作人（必填） */
    private String operator;

    // ============ 复制增强字段 (v2.1 - 2026-07-05) ============
    // 仅由 /copy 端点填充,不影响 create/update 入参格式
    // 前端 BaseDataPicker 利用 preloadRow 显示实体名称而非 ID

    /** 管理主体名称（optional, copy 端点填充） */
    private String managementEntityName;

    /** 交易对手名称 */
    private String counterpartyName;

    /** 金融工具名称 */
    private String instrumentName;

    /** 交易员名称 */
    private String traderName;

    /** 银行账户名称 */
    private String bankAccountName;

    /** 对手方账户名称 */
    private String counterpartyAccountName;
}

package com.opentms.dealing.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * AT 交易返回 VO
 * 包含 AtDeal 全部字段 + Deal 公共字段 + 双腿 DealMap 列表
 */
@Data
public class AtDealVO {

    private Long id;

    private String dealNumber;

    private String dealType;

    private String transferType;

    private String managementEntity;

    /** 2026-07-05: 详情响应补全的管理主体可读名称 (前端 BaseDataPicker preloadRow) */
    private String managementEntityName;

    private Long sourceAccountId;
    private Long destAccountId;

    /** 2026-07-05: 详情响应补全的源/目标账户可读名称 */
    private String sourceAccountName;
    private String destAccountName;

    private BigDecimal sourceAmount;
    private BigDecimal destAmount;

    private String sourceCurrency;
    private String destCurrency;

    private BigDecimal exchangeRate;

    private LocalDate valueDate;

    private String paymentMethod;

    private String purpose;

    private String status;

    private String description;
    private String remark;

    private String latestActionNumber;

    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;
    private Integer version;

    /** 双腿 DealMap 列表（4 条：2×AccountTransfer + 2×ActualCashflow） */
    private List<DealMapVO> legs;
}

package com.opentms.dealing.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * AC 交易详情聚合 VO（基本信息 + DealMap 时间线 + Cashflow + Action 列表）
 */
@Data
public class AcDealDetailVO {

    private Long id;

    private String dealNumber;

    private String dealType;

    private String businessUnit;

    private Long counterpartyId;

    private String counterpartyName;

    private Long instrumentId;

    private String instrumentName;

    private Long traderId;

    private String traderName;

    private String direction;

    private BigDecimal amount;

    private String currency;

    private LocalDate dealDate;

    private LocalDate valueDate;

    private String status;

    private String description;

    private String remark;

    private String latestActionNumber;

    private Long bankAccountId;

    private String bankAccountName;

    private Long counterpartyAccountId;

    private String counterpartyAccountName;

    private String paymentMethod;

    private String createdBy;

    private LocalDateTime createdAt;

    private String updatedBy;

    private LocalDateTime updatedAt;

    private Integer version;

    /** DealMap 时间线（按 event_date 排序） */
    private List<com.opentms.dealing.vo.DealMapVO> dealMapList;

    /** 关联 Cashflow 列表（按 dealmap_number 关联） */
    private List<com.opentms.dealing.vo.CashflowVO> cashflowList;

    /** Action 列表（多 Action/Deal：CREATE/UPDATE/DELETE/APPROVE/REJECT） */
    private List<com.opentms.dealing.vo.ActionVO> actionList;
}

package com.opentms.dealing.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 审计历史版本摘要 VO（列表端点用）
 *
 * <p>对应表：tms_deals_image_t，按 (deal_number, version) 一行。</p>
 *
 * @author Open-TMS Backend Developer
 * @since 2026-07-11
 */
@Data
public class AuditHistoryVersionSummaryVO {

    private Long id;

    private Integer version;

    private String dealType;

    private String imageNumber;

    /** CREATE / UPDATE / DELETE / RATE_FIX / STATUS_CHANGE */
    private String imageType;

    private String operator;

    private LocalDateTime operateAt;

    private String status;

    private String actionNumber;

    /** 2026-07-11 BUG-001 修复:变更摘要(从 cashflow_image 补全的版本会带) */
    private String changeSummary;
}
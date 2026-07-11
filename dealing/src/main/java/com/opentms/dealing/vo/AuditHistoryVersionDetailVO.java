package com.opentms.dealing.vo;

import lombok.Data;

import java.util.List;

/**
 * 审计历史版本详情 VO（3 段式 LEFT JOIN）
 *
 * <ul>
 *   <li>dealImage — 必有（主表镜像）</li>
 *   <li>specificDealImage — 可空（AC/AT/FX 镜像）</li>
 *   <li>cashflowImages — 可空列表（v3.0 之前的 cashflow 无镜像）</li>
 * </ul>
 *
 * @author Open-TMS Backend Developer
 * @since 2026-07-11
 */
@Data
public class AuditHistoryVersionDetailVO {

    private DealImageVO dealImage;

    /** 通用字段：AC/AT/FX 任一镜像（按 dealType 分支） */
    private DealImageVO specificDealImage;

    /** 现金流镜像列表（v3.0 之前的 cashflow 无镜像时返回空列表） */
    private List<CashflowImageVO> cashflowImages;

    /** 触发的 Action 编号 */
    private String actionNumber;

    /** 镜像编号 */
    private String imageNumber;
}
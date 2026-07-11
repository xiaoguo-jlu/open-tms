package com.opentms.dealing.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.opentms.dealing.entity.Cashflow;
import com.opentms.dealing.entity.CashflowImage;
import com.opentms.dealing.vo.CashflowImageVO;

import java.util.List;

/**
 * 现金流镜像 Service（v1.0 - 2026-07-11）
 *
 * <p>提供 Cashflow 增删改留痕、版本查询、版本详情拼装能力。</p>
 *
 * @author Open-TMS Backend Developer
 * @since 2026-07-11
 */
public interface CashflowImageService {

    /**
     * 追加一条镜像。imageType 必填（CREATE/UPDATE/DELETE/RATE_FIX/STATUS_CHANGE）。
     *
     * <p>调用方需保证在 @Transactional 中，镜像失败整体回滚。</p>
     *
     * @param cf        镜像源（cashflow 主表对象）
     * @param imageType CREATE/UPDATE/DELETE/RATE_FIX/STATUS_CHANGE
     * @return imageNumber
     */
    String append(Cashflow cf, String imageType);

    /**
     * 按 dealNumber + imageType 分页查询镜像版本摘要。
     *
     * @param dealNumber 交易编号
     * @param imageType  可选过滤（CREATE/UPDATE/DELETE/RATE_FIX/STATUS_CHANGE）
     * @param pageNum    页码（从 1 开始）
     * @param pageSize   每页大小
     * @return 分页对象（records 元素为 CashflowImageVO）
     */
    Page<CashflowImageVO> listByDealNumber(String dealNumber, String imageType, int pageNum, int pageSize);

    /**
     * 按 dealNumber + version 查询该版本所有 cashflow 镜像。
     */
    List<CashflowImageVO> listByDealNumberAndVersion(String dealNumber, Integer version);
}
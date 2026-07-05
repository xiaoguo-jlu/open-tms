package com.opentms.dealing.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.opentms.dealing.dto.FxCalculateRequest;
import com.opentms.dealing.dto.FxCalculateResponse;
import com.opentms.dealing.dto.FxDealDTO;
import com.opentms.dealing.dto.RateFixRequest;
import com.opentms.dealing.vo.FxDealDetailVO;
import com.opentms.dealing.vo.FxDealVO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

public interface FxDealService {

    /**
     * 后端统一计算接口（v3.2 - 单一可信源）
     * @return 补全后的所有字段
     */
    FxCalculateResponse calculate(FxCalculateRequest req);

    /**
     * 分页查询 FX 交易列表
     */
    Page<FxDealVO> queryPage(Long managementEntityId, Long counterpartyId,
                             String productType, String status,
                             LocalDate startDate, LocalDate endDate,
                             int pageNum, int pageSize);

    /**
     * 获取 FX 交易详情（聚合 Deal + FxDeal + DealMap + Cashflow + Action）
     */
    FxDealDetailVO getDetailByDealNumber(String dealNumber);

    /**
     * 创建 FX 交易（DEAL Action + 共享主键插入 + 3 DealMap + 0/2 Cashflow）
     * @return 生成的 dealNumber
     */
    String createFxDeal(FxDealDTO dto);

    /**
     * 更新 FX 交易（UPDATE Action）
     */
    boolean updateFxDeal(FxDealDTO dto);

    /**
     * 删除 FX 交易（DELETE Action + 级联软删）
     */
    boolean deleteFxDeal(Long id);

    /**
     * NDF RATE_FIX（生成 1 DealMap(FX_FIX) + 1 Cashflow(差额) + UPDATE fixing_rate/settlement_amount）
     * Phase 1: 支持 fixDate / fixCurrency / fixMarketRate / verifierBy / fixRemark
     * @return 包含 dealNumber / settlementAmount / dealmapNumber / currency / direction
     */
    Map<String, Object> rateFix(Long id, RateFixRequest req);

    /**
     * NDF RATE_FIX（旧签名，向后兼容）
     * @deprecated 使用 {@link #rateFix(Long, RateFixRequest)} 替代
     */
    @Deprecated
    Map<String, Object> rateFix(Long id, BigDecimal fixingRate, String operator);

    /**
     * 获取可复制字段（id/dealNumber 置 null，operator 置空）
     */
    FxDealDTO getCopyData(String dealNumber);

    /**
     * 审批通过 Action（仅更新 Action.approval_status1，Deal.status → Approved）
     */
    boolean approveAction(String actionNumber, String approver, String remark);

    /**
     * 驳回 Action（Deal.status → Rejected）
     */
    boolean rejectAction(String actionNumber, String approver, String remark);
}
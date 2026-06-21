package com.opentms.dealing.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.opentms.dealing.dto.AcDealDTO;
import com.opentms.dealing.dto.AcDealDetailVO;
import com.opentms.dealing.vo.DealVO;

public interface AcDealService {

    /**
     * 分页查询 AC 交易列表
     */
    Page<DealVO> queryPage(String keyword, String status, String direction,
                          String businessUnit, int pageNum, int pageSize);

    /**
     * 获取 AC 交易详情（聚合 Deal + AcDeal + DealMap 时间线 + Cashflow + Action）
     */
    AcDealDetailVO getDetail(Long id);

    AcDealDetailVO getDetailByDealNumber(String dealNumber);

    /**
     * 创建 AC 交易（v2.0 - 事务内自动生成 Action + Deal + AcDeal + DealMap + Cashflow，不生成 DealImage）
     */
    boolean createAcDeal(AcDealDTO dto);

    /**
     * 更新 AC 交易（v2.0 - 事务内：INSERT 新 Action + UPDATE Deal/AcDeal + 软删旧 DealMap + 新建 DealMap + UPDATE Cashflow.dealmap_number + INSERT DealImage v+1）
     */
    boolean updateAcDeal(AcDealDTO dto);

    /**
     * 删除 AC 交易（v2.0 - 事务内：INSERT Action(DELETE) + 软删 Deal/AcDeal/DealMap/Cashflow 级联 + INSERT DealImage v+1）
     */
    boolean deleteAcDeal(Long id);

    /**
     * 审批通过 Action（v2.0 - 仅更新 Action.approval_status1，DealMap/Cashflow 状态不变）
     */
    boolean approveAction(String actionNumber, String approver, String approvalRemark);

    /**
     * 驳回 Action（v2.0 - 仅更新 Action.approval_status1=Rejected，DealMap/Cashflow 状态不变）
     */
    boolean rejectAction(String actionNumber, String approver, String approvalRemark);
}

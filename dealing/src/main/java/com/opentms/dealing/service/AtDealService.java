package com.opentms.dealing.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.opentms.dealing.dto.AtDealDTO;
import com.opentms.dealing.vo.ActionVO;
import com.opentms.dealing.vo.AtDealImageVO;
import com.opentms.dealing.vo.AtDealVO;
import com.opentms.dealing.vo.CashflowVO;
import com.opentms.dealing.vo.DealMapVO;

import java.util.List;

/**
 * AT 交易服务接口
 */
public interface AtDealService {

    /**
     * 分页查询 AT 交易
     */
    Page<AtDealVO> queryPage(String keyword, String transferType, String status,
                             int pageNum, int pageSize);

    /**
     * 按主键 ID 获取 AT 交易详情（含 DealMap 双腿）
     */
    AtDealVO getById(Long id);

    /**
     * 按 dealNumber 获取 AT 交易详情
     */
    AtDealVO getByDealNumber(String dealNumber);

    /**
     * 创建 AT 交易（v2.0 CREATE 流程）
     * 自动生成：Deal + AtDeal + Action(CREATE) + 4 DealMap + 2 Cashflow
     * 不生成 AtDealImage
     */
    boolean saveAtDeal(AtDealDTO dto);

    /**
     * 更新 AT 交易（v2.0 UPDATE 流程）
     * 1) INSERT Action(UPDATE)
     * 2) UPDATE Deal + AtDeal
     * 3) 软删旧 DealMap × 4 + 旧 Cashflow × 2
     * 4) INSERT 新 DealMap × 4 + 新 Cashflow × 2
     * 5) INSERT AtDealImage(v+1)
     */
    boolean updateAtDeal(AtDealDTO dto);

    /**
     * 删除 AT 交易（v2.0 DELETE 流程 - 级联软删）
     * 1) INSERT Action(DELETE)
     * 2) 软删 Deal + AtDeal
     * 3) 级联软删 DealMap × 4 + Cashflow × 2
     * 4) INSERT AtDealImage(v+1)
     */
    boolean deleteAtDeal(Long id);

    /**
     * 查询某 AT 交易的所有 DealMap（双腿）
     */
    List<DealMapVO> listDealMapsByDeal(String dealNumber);

    /**
     * 查询某 AT 交易的所有 Cashflow
     */
    List<CashflowVO> listCashflowsByDeal(String dealNumber);

    /**
     * 查询某 AT 交易的所有 Action（含 CREATE/UPDATE/DELETE/APPROVE/REJECT）
     */
    List<ActionVO> listActionsByDeal(String dealNumber);

    /**
     * 查询某 AT 交易的所有 Image 快照
     */
    List<AtDealImageVO> listImagesByDeal(String dealNumber);

    /**
     * 审批通过 Action
     * 关键：仅更新 Action.approval_status1，不改变 DealMap / Cashflow 任何状态
     */
    boolean approveAction(String actionNumber, String approver, String remark);

    /**
     * 驳回 Action
     * 关键：仅更新 Action.approval_status1=Rejected，不改变 DealMap / Cashflow 状态
     */
    boolean rejectAction(String actionNumber, String approver, String remark);
}

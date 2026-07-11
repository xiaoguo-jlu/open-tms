package com.opentms.dealing.service;

import com.opentms.dealing.entity.Cashflow;
import com.opentms.dealing.vo.CashflowVO;

import java.util.List;

public interface CashflowService {

    /**
     * 创建 Cashflow（v2.0 - 由 CREATE Action 触发，关联 dealmap_number）
     */
    String createCashflow(Cashflow cashflow);

    /**
     * 直接保存 Cashflow
     */
    boolean save(Cashflow cashflow);

    /**
     * 软删除指定 dealmap_number 关联的所有 Cashflow
     */
    int softDeleteByDealMapNumber(String dealmapNumber);

    /**
     * 更新 Cashflow 关联的 dealmap_number（v2.0 - UPDATE 时指向新 DealMap）
     */
    int updateDealMapNumber(String oldDealMapNumber, String newDealMapNumber);

    /**
     * 根据 dealmap_number 查询
     */
    List<CashflowVO> listByDealMapNumber(String dealmapNumber);

    /**
     * 根据 deal_number 查询
     */
    List<CashflowVO> listByDealNumber(String dealNumber);

    /**
     * 更新现金流（v1.0 - 透明下沉银行账户ID + 写镜像）
     *
     * <p>写入 UPDATE 镜像 → 重新调默认银行账户规则（若参数变化）→ 更新主表 → 失败回滚。</p>
     */
    boolean updateCashflow(Cashflow cashflow);

    /**
     * 软删除现金流（v1.0 - 写 DELETE 镜像）
     *
     * <p>写入 DELETE 镜像 → 软删主表 → 失败回滚。</p>
     */
    boolean deleteCashflow(Long id);

    /**
     * 生成 cflow_number 编号
     */
    String generateCflowNumber();
}

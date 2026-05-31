package com.opentms.dealing.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.opentms.dealing.dto.DealDTO;
import com.opentms.dealing.vo.DealVO;

public interface DealService {

    Page<DealVO> queryPage(String keyword, String dealType, String status, int pageNum, int pageSize);

    DealVO getDealById(Long id);

    DealVO getDealByDealNumber(String dealNumber);

    boolean saveDeal(DealDTO dealDTO);

    boolean updateDeal(DealDTO dealDTO);

    boolean deleteDeal(Long id);

    boolean submitDeal(Long id, String operator);

    boolean approveDeal(Long id, String approver, String approvalRemark);

    boolean rejectDeal(Long id, String approver, String approvalRemark);

    boolean executeDeal(Long id, String operator);
}
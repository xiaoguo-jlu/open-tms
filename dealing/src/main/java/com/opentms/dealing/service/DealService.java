package com.opentms.dealing.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.opentms.dealing.entity.Deal;

public interface DealService {

    Page<Deal> queryPage(String keyword, String dealType, String status, String counterpartyId, int pageNum, int pageSize);

    Deal getDealById(Long id);

    boolean saveDeal(Deal deal);

    boolean updateDeal(Deal deal);

    boolean deleteDeal(Long id);

    boolean submitDeal(Long id);

    boolean approveDeal(Long id);

    boolean rejectDeal(Long id);

    boolean batchDelete(String ids);
}
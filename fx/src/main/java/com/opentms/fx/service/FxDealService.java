package com.opentms.fx.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.opentms.fx.entity.FxDeal;
import com.opentms.fx.vo.FxDealVO;

public interface FxDealService {

    IPage<FxDealVO> queryPage(String keyword, String fxType, String status, int pageNum, int pageSize);

    FxDealVO getFxDealById(Long id);

    boolean saveFxDeal(FxDeal fxDeal);

    boolean updateFxDeal(FxDeal fxDeal);

    boolean deleteFxDeal(Long id);

    boolean submitFxDeal(Long id);

    boolean approveFxDeal(Long id);

    boolean executeFxDeal(Long id);
}
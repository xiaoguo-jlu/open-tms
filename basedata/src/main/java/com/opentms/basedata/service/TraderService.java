package com.opentms.basedata.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.opentms.basedata.entity.Trader;

public interface TraderService {

    Page<Trader> queryPage(String keyword, String status, int pageNum, int pageSize);

    Trader getTraderById(Long id);

    boolean saveTrader(Trader trader);

    boolean updateTrader(Trader trader);

    boolean deleteTrader(Long id);

    boolean checkCodeExists(String code, Long excludeId);
}
package com.opentms.irs.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.opentms.irs.entity.IrsDeal;

public interface IrsDealService {

    Page<IrsDeal> queryPage(String keyword, String dealType, String status, String counterpartyId, int pageNum, int pageSize);

    IrsDeal getIrsDealById(Long id);

    boolean saveIrsDeal(IrsDeal irsDeal);

    boolean updateIrsDeal(IrsDeal irsDeal);

    boolean deleteIrsDeal(Long id);

    boolean submit(Long id);

    boolean approve(Long id);
}
package com.opentms.basedata.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.opentms.basedata.dto.AcCashflowDTO;
import com.opentms.basedata.vo.AcCashflowVO;

public interface AcCashflowService {

    Page<AcCashflowVO> queryPage(AcCashflowDTO dto, int pageNum, int pageSize);

    AcCashflowVO getById(Long id);

    AcCashflowVO save(AcCashflowDTO dto);

    AcCashflowVO updateById(AcCashflowDTO dto);

    void removeById(Long id);

    AcCashflowVO confirm(Long id);

    AcCashflowVO generateFromDeal(Long dealId);
}

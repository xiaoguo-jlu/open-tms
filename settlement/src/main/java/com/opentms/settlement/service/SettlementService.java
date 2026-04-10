package com.opentms.settlement.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.opentms.settlement.dto.SettlementDTO;
import com.opentms.settlement.entity.Settlement;
import com.opentms.settlement.vo.SettlementVO;

public interface SettlementService {

    Page<Settlement> queryPage(String keyword, String status, String settlementType, int pageNum, int pageSize);

    Settlement getSettlementById(Long id);

    boolean saveSettlement(SettlementDTO dto);

    boolean updateSettlement(SettlementDTO dto, Long id);

    boolean submit(Long id);

    boolean approve(Long id);

    boolean execute(Long id);

    String getResult(Long id);
}
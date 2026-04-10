package com.opentms.basedata.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.opentms.basedata.entity.Counterparty;

public interface CounterpartyService {

    Page<Counterparty> queryPage(String keyword, String counterpartyType, String countryCode, String status, int pageNum, int pageSize);

    Counterparty getCounterpartyById(Long id);

    boolean saveCounterparty(Counterparty counterparty);

    boolean updateCounterparty(Counterparty counterparty);

    boolean deleteCounterparty(Long id);

    boolean checkCodeExists(String code, Long excludeId);
}
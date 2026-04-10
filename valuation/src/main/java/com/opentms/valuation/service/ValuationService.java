package com.opentms.valuation.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.opentms.valuation.entity.Valuation;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface ValuationService {

    Page<Valuation> queryPage(Long instrumentId, LocalDate startDate, LocalDate endDate, 
                              String valuationMethod, int pageNum, int pageSize);

    Valuation getValuationById(Long id);

    boolean saveValuation(Valuation valuation);

    boolean updateValuation(Valuation valuation);

    boolean deleteValuation(Long id);

    Map<String, Object> executeValuation(Long instrumentId, LocalDate valuationDate);

    List<Valuation> getHistory(Long instrumentId, LocalDate startDate, LocalDate endDate);

    Map<String, Object> getParameters(Long instrumentId);

    boolean updateParameters(Long instrumentId, Map<String, Object> parameters);
}
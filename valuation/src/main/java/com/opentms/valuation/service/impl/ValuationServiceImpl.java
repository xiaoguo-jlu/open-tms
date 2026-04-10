package com.opentms.valuation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.opentms.valuation.entity.Valuation;
import com.opentms.valuation.mapper.ValuationMapper;
import com.opentms.valuation.service.ValuationService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ValuationServiceImpl extends ServiceImpl<ValuationMapper, Valuation> implements ValuationService {

    @Override
    public Page<Valuation> queryPage(Long instrumentId, LocalDate startDate, LocalDate endDate,
                                     String valuationMethod, int pageNum, int pageSize) {
        LambdaQueryWrapper<Valuation> wrapper = new LambdaQueryWrapper<>();

        if (instrumentId != null) {
            wrapper.eq(Valuation::getInstrumentId, instrumentId);
        }

        if (startDate != null) {
            wrapper.ge(Valuation::getValuationDate, startDate);
        }

        if (endDate != null) {
            wrapper.le(Valuation::getValuationDate, endDate);
        }

        if (StringUtils.hasText(valuationMethod)) {
            wrapper.eq(Valuation::getValuationMethod, valuationMethod);
        }

        wrapper.orderByDesc(Valuation::getValuationDate);

        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public Valuation getValuationById(Long id) {
        return getById(id);
    }

    @Override
    public boolean saveValuation(Valuation valuation) {
        return save(valuation);
    }

    @Override
    public boolean updateValuation(Valuation valuation) {
        return updateById(valuation);
    }

    @Override
    public boolean deleteValuation(Long id) {
        return removeById(id);
    }

    @Override
    public Map<String, Object> executeValuation(Long instrumentId, LocalDate valuationDate) {
        Map<String, Object> result = new HashMap<>();
        
        Valuation valuation = new Valuation();
        valuation.setInstrumentId(instrumentId);
        valuation.setValuationDate(valuationDate);
        valuation.setValuationMethod("MARK_TO_MARKET");
        
        BigDecimal marketValue = new BigDecimal("100000.00");
        BigDecimal costValue = new BigDecimal("95000.00");
        
        valuation.setMarketValue(marketValue);
        valuation.setCostValue(costValue);
        valuation.setUnrealizedPnl(marketValue.subtract(costValue));
        
        save(valuation);
        
        result.put("valuationId", valuation.getId());
        result.put("marketValue", marketValue);
        result.put("costValue", costValue);
        result.put("unrealizedPnl", valuation.getUnrealizedPnl());
        result.put("valuationDate", valuationDate);
        
        return result;
    }

    @Override
    public List<Valuation> getHistory(Long instrumentId, LocalDate startDate, LocalDate endDate) {
        LambdaQueryWrapper<Valuation> wrapper = new LambdaQueryWrapper<>();
        
        if (instrumentId != null) {
            wrapper.eq(Valuation::getInstrumentId, instrumentId);
        }
        
        if (startDate != null) {
            wrapper.ge(Valuation::getValuationDate, startDate);
        }
        
        if (endDate != null) {
            wrapper.le(Valuation::getValuationDate, endDate);
        }
        
        wrapper.orderByDesc(Valuation::getValuationDate);
        
        return list(wrapper);
    }

    @Override
    public Map<String, Object> getParameters(Long instrumentId) {
        Map<String, Object> params = new HashMap<>();
        params.put("instrumentId", instrumentId);
        params.put("valuationMethod", "MARK_TO_MARKET");
        params.put("discountRate", new BigDecimal("0.05"));
        params.put("volatility", new BigDecimal("0.15"));
        return params;
    }

    @Override
    public boolean updateParameters(Long instrumentId, Map<String, Object> parameters) {
        return true;
    }
}
package com.opentms.basedata.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.opentms.basedata.dto.CurrencyDTO;
import com.opentms.basedata.entity.Currency;
import com.opentms.basedata.mapper.CurrencyMapper;
import com.opentms.basedata.service.CurrencyService;
import com.opentms.basedata.vo.CurrencyVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@Slf4j
public class CurrencyServiceImpl extends BasedataServiceImpl<CurrencyMapper, Currency, CurrencyDTO, CurrencyVO> implements CurrencyService {

    @Override
    public List<CurrencyVO> listAll() {
        LambdaQueryWrapper<Currency> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Currency::getStatus, "1");
        return baseMapper.selectList(wrapper).stream()
                .map(this::convertToVO)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    protected CurrencyVO convertToVO(Currency entity) {
        CurrencyVO vo = new CurrencyVO();
        vo.setId(entity.getId());
        vo.setCode(entity.getCode());
        vo.setName(entity.getName());
        vo.setSymbol(entity.getSymbol());
        vo.setDecimalPlaces(entity.getDecimalPlaces());
        vo.setStatus(entity.getStatus());
        vo.setCreatedAt(entity.getCreatedAt());
        vo.setUpdatedAt(entity.getUpdatedAt());
        return vo;
    }

    @Override
    protected Currency convertToEntity(CurrencyDTO dto) {
        Currency entity = new Currency();
        entity.setId(dto.getId());
        entity.setCode(dto.getCode());
        entity.setName(dto.getName());
        entity.setSymbol(dto.getSymbol());
        entity.setDecimalPlaces(dto.getDecimalPlaces());
        entity.setStatus(dto.getStatus());
        entity.setRemark(dto.getRemark());
        return entity;
    }

    @Override
    protected void updateEntityFromDTO(Currency entity, CurrencyDTO dto) {
        entity.setName(dto.getName());
        entity.setSymbol(dto.getSymbol());
        entity.setDecimalPlaces(dto.getDecimalPlaces());
        entity.setStatus(dto.getStatus());
        entity.setRemark(dto.getRemark());
    }

    @Override
    protected String getEntityName() {
        return "币种";
    }
}
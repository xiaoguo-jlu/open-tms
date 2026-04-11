package com.opentms.basedata.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.opentms.basedata.dto.CurrencyDTO;
import com.opentms.basedata.entity.Currency;
import com.opentms.basedata.mapper.CurrencyMapper;
import com.opentms.basedata.service.CurrencyService;
import com.opentms.basedata.vo.CurrencyVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class CurrencyServiceImpl implements CurrencyService {

    private final CurrencyMapper currencyMapper;

    @Override
    public List<CurrencyVO> listAll() {
        return currencyMapper.selectList(null).stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public Page<CurrencyVO> queryPage(String keyword, String status, int pageNo, int pageSize) {
        LambdaQueryWrapper<Currency> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(keyword)) {
            wrapper.like(Currency::getCode, keyword)
                   .or()
                   .like(Currency::getName, keyword);
        }

        if (StringUtils.hasText(status)) {
            wrapper.eq(Currency::getStatus, status);
        }

        wrapper.orderByDesc(Currency::getCreatedAt);

        Page<Currency> page = currencyMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
        
        Page<CurrencyVO> result = new Page<>();
        result.setCurrent(page.getCurrent());
        result.setSize(page.getSize());
        result.setTotal(page.getTotal());
        result.setRecords(page.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList()));
        
        return result;
    }

    @Override
    public CurrencyVO getById(Long id) {
        Currency entity = currencyMapper.selectById(id);
        return entity != null ? convertToVO(entity) : null;
    }

    @Override
    public CurrencyVO getByCode(String code) {
        LambdaQueryWrapper<Currency> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Currency::getCode, code);
        Currency entity = currencyMapper.selectOne(wrapper);
        return entity != null ? convertToVO(entity) : null;
    }

    @Override
    public CurrencyVO save(CurrencyDTO dto) {
        log.info("[新增币种] code={}", dto.getCode());
        
        Currency entity = convertToEntity(dto);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setStatus("1");
        
        currencyMapper.insert(entity);
        
        log.info("[新增币种] success id={}", entity.getId());
        return convertToVO(entity);
    }

    @Override
    public CurrencyVO updateById(CurrencyDTO dto) {
        log.info("[更新币种] id={}", dto.getId());
        
        Currency entity = currencyMapper.selectById(dto.getId());
        if (entity == null) {
            throw new RuntimeException("币种不存在");
        }
        
        entity.setName(dto.getName());
        entity.setSymbol(dto.getSymbol());
        entity.setDecimalPlaces(dto.getDecimalPlaces());
        entity.setStatus(dto.getStatus());
        entity.setRemark(dto.getRemark());
        entity.setUpdatedAt(LocalDateTime.now());
        
        currencyMapper.updateById(entity);
        
        log.info("[更新币种] success id={}", entity.getId());
        return convertToVO(entity);
    }

    @Override
    public void removeById(Long id) {
        log.info("[删除币种] id={}", id);
        currencyMapper.deleteById(id);
    }

    private CurrencyVO convertToVO(Currency entity) {
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

    private Currency convertToEntity(CurrencyDTO dto) {
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
}
package com.opentms.basedata.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.opentms.basedata.entity.Counterparty;
import com.opentms.basedata.mapper.CounterpartyMapper;
import com.opentms.basedata.service.CounterpartyService;
import com.opentms.basedata.vo.CounterpartyVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.stream.Collectors;

@Slf4j
@Service
public class CounterpartyServiceImpl extends ServiceImpl<CounterpartyMapper, Counterparty> implements CounterpartyService {

    @Override
    public Page<CounterpartyVO> queryPage(String keyword, String counterpartyType, String countryCode, String status, int pageNum, int pageSize) {
        LambdaQueryWrapper<Counterparty> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(keyword)) {
            wrapper.like(Counterparty::getCode, keyword)
                   .or()
                   .like(Counterparty::getName, keyword);
        }

        if (StringUtils.hasText(counterpartyType)) {
            wrapper.eq(Counterparty::getCounterpartyType, counterpartyType);
        }

        if (StringUtils.hasText(countryCode)) {
            wrapper.eq(Counterparty::getCountryCode, countryCode);
        }

        if (StringUtils.hasText(status)) {
            wrapper.eq(Counterparty::getStatus, status);
        }

        wrapper.orderByDesc(Counterparty::getCreatedAt);

        Page<Counterparty> page = baseMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);

        Page<CounterpartyVO> result = new Page<>();
        result.setCurrent(page.getCurrent());
        result.setSize(page.getSize());
        result.setTotal(page.getTotal());
        result.setRecords(page.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList()));

        return result;
    }

    @Override
    public CounterpartyVO getCounterpartyById(Long id) {
        Counterparty counterparty = getById(id);
        return counterparty != null ? convertToVO(counterparty) : null;
    }

    @Override
    public boolean saveCounterparty(Counterparty counterparty) {
        if (checkCodeExists(counterparty.getCode(), null)) {
            throw new RuntimeException("Counterparty code already exists");
        }
        counterparty.setCreatedBy("system");
        counterparty.setCreatedAt(java.time.LocalDateTime.now());
        counterparty.setStatus("1");
        return save(counterparty);
    }

    @Override
    public boolean updateCounterparty(Counterparty counterparty) {
        if (counterparty.getId() == null) {
            throw new RuntimeException("Counterparty ID cannot be null");
        }
        Counterparty existing = getById(counterparty.getId());
        if (existing == null) {
            throw new RuntimeException("Counterparty not found");
        }
        if (checkCodeExists(counterparty.getCode(), counterparty.getId())) {
            throw new RuntimeException("Counterparty code already exists");
        }
        // Preserve created audit fields
        counterparty.setCreatedBy(existing.getCreatedBy());
        counterparty.setCreatedAt(existing.getCreatedAt());
        counterparty.setUpdatedBy("system");
        counterparty.setUpdatedAt(java.time.LocalDateTime.now());
        return updateById(counterparty);
    }

    @Override
    public boolean deleteCounterparty(Long id) {
        Counterparty existing = getById(id);
        if (existing == null) {
            throw new RuntimeException("Counterparty not found");
        }
        return removeById(id);
    }

    @Override
    public boolean checkCodeExists(String code, Long excludeId) {
        if (!StringUtils.hasText(code)) {
            return false;
        }
        LambdaQueryWrapper<Counterparty> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Counterparty::getCode, code);
        if (excludeId != null) {
            wrapper.ne(Counterparty::getId, excludeId);
        }
        return count(wrapper) > 0;
    }

    private CounterpartyVO convertToVO(Counterparty entity) {
        CounterpartyVO vo = new CounterpartyVO();
        vo.setId(entity.getId());
        vo.setCode(entity.getCode());
        vo.setName(entity.getName());
        vo.setEnName(entity.getEnName());
        vo.setCounterpartyType(entity.getCounterpartyType());
        vo.setCountryCode(entity.getCountryCode());
        vo.setSwiftCode(entity.getSwiftCode());
        vo.setInternalRating(entity.getInternalRating());
        vo.setExternalRating(entity.getExternalRating());
        vo.setPhone(entity.getPhone());
        vo.setAddress(entity.getAddress());
        vo.setStatus(entity.getStatus());
        vo.setRemark(entity.getRemark());
        vo.setCreatedAt(entity.getCreatedAt());
        vo.setUpdatedAt(entity.getUpdatedAt());
        return vo;
    }
}
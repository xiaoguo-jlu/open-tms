package com.opentms.basedata.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.opentms.basedata.dto.CounterpartyDTO;
import com.opentms.basedata.entity.Counterparty;
import com.opentms.basedata.mapper.CounterpartyMapper;
import com.opentms.basedata.service.CounterpartyService;
import com.opentms.basedata.vo.CounterpartyVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class CounterpartyServiceImpl extends ServiceImpl<CounterpartyMapper, Counterparty> implements CounterpartyService {

    @Override
    public IPage<CounterpartyVO> queryPage(String keyword, String type, String countryCode, String status, int pageNo, int pageSize) {
        LambdaQueryWrapper<Counterparty> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Counterparty::getCode, keyword)
                   .or()
                   .like(Counterparty::getName, keyword);
        }
        
        if (StringUtils.hasText(type)) {
            wrapper.eq(Counterparty::getType, type);
        }
        
        if (StringUtils.hasText(countryCode)) {
            wrapper.eq(Counterparty::getCountryCode, countryCode);
        }
        
        if (StringUtils.hasText(status)) {
            wrapper.eq(Counterparty::getStatus, status);
        }
        
        wrapper.orderByDesc(Counterparty::getCreatedAt);
        
        Page<Counterparty> page = new Page<>(pageNo, pageSize);
        IPage<Counterparty> result = this.page(page, wrapper);
        
        return result.convert(this::convertToVO);
    }

    @Override
    public CounterpartyVO getById(Long id) {
        Counterparty entity = super.getById(id);
        return entity == null ? null : convertToVO(entity);
    }

    @Override
    public boolean save(CounterpartyDTO dto) {
        Counterparty entity = new Counterparty();
        BeanUtils.copyProperties(dto, entity);
        return this.save(entity);
    }

    @Override
    public boolean update(CounterpartyDTO dto) {
        Counterparty entity = new Counterparty();
        BeanUtils.copyProperties(dto, entity);
        return this.updateById(entity);
    }

    @Override
    public boolean delete(Long id) {
        return this.removeById(id);
    }

    @Override
    public boolean batchDelete(List<Long> ids) {
        return this.removeByIds(ids);
    }

    @Override
    public List<CounterpartyVO> listAll() {
        List<Counterparty> list = this.list();
        List<CounterpartyVO> result = new ArrayList<>();
        for (Counterparty entity : list) {
            result.add(convertToVO(entity));
        }
        return result;
    }

    private CounterpartyVO convertToVO(Counterparty entity) {
        CounterpartyVO vo = new CounterpartyVO();
        vo.setId(entity.getId());
        vo.setCode(entity.getCode());
        vo.setName(entity.getName());
        vo.setEnName(entity.getEnName());
        vo.setType(entity.getType());
        vo.setCountryCode(entity.getCountryCode());
        vo.setCreditRating(entity.getCreditRating());
        vo.setExtRating(entity.getExtRating());
        vo.setAddress(entity.getAddress());
        vo.setPhone(entity.getPhone());
        vo.setStatus(entity.getStatus());
        vo.setCreatedBy(entity.getCreatedBy());
        vo.setCreatedAt(entity.getCreatedAt());
        vo.setUpdatedBy(entity.getUpdatedBy());
        vo.setUpdatedAt(entity.getUpdatedAt());
        return vo;
    }
}

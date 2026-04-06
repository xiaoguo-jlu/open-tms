package com.opentms.basedata.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.opentms.basedata.dto.CountryDTO;
import com.opentms.basedata.entity.Country;
import com.opentms.basedata.mapper.CountryMapper;
import com.opentms.basedata.service.CountryService;
import com.opentms.basedata.vo.CountryVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class CountryServiceImpl extends ServiceImpl<CountryMapper, Country> implements CountryService {

    @Override
    public IPage<CountryVO> queryPage(String keyword, String status, int pageNo, int pageSize) {
        LambdaQueryWrapper<Country> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Country::getCode, keyword)
                   .or()
                   .like(Country::getName, keyword);
        }
        
        if (StringUtils.hasText(status)) {
            wrapper.eq(Country::getStatus, status);
        }
        
        wrapper.orderByDesc(Country::getCreatedAt);
        
        Page<Country> page = new Page<>(pageNo, pageSize);
        IPage<Country> result = this.page(page, wrapper);
        
        return result.convert(this::convertToVO);
    }

    @Override
    public CountryVO getById(Long id) {
        Country entity = super.getById(id);
        return entity == null ? null : convertToVO(entity);
    }

    @Override
    public boolean save(CountryDTO dto) {
        Country entity = new Country();
        BeanUtils.copyProperties(dto, entity);
        return this.save(entity);
    }

    @Override
    public boolean update(CountryDTO dto) {
        Country entity = new Country();
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
    public List<CountryVO> listAll() {
        List<Country> list = this.list();
        List<CountryVO> result = new ArrayList<>();
        for (Country entity : list) {
            result.add(convertToVO(entity));
        }
        return result;
    }

    private CountryVO convertToVO(Country entity) {
        CountryVO vo = new CountryVO();
        vo.setId(entity.getId());
        vo.setCode(entity.getCode());
        vo.setName(entity.getName());
        vo.setEnName(entity.getEnName());
        vo.setTimezone(entity.getTimezone());
        vo.setAreaCode(entity.getAreaCode());
        vo.setStatus(entity.getStatus());
        vo.setCreatedBy(entity.getCreatedBy());
        vo.setCreatedAt(entity.getCreatedAt());
        vo.setUpdatedBy(entity.getUpdatedBy());
        vo.setUpdatedAt(entity.getUpdatedAt());
        return vo;
    }
}

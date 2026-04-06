package com.opentms.basedata.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.opentms.basedata.dto.BusinessUnitDTO;
import com.opentms.basedata.entity.BusinessUnit;
import com.opentms.basedata.mapper.BusinessUnitMapper;
import com.opentms.basedata.service.BusinessUnitService;
import com.opentms.basedata.vo.BusinessUnitVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class BusinessUnitServiceImpl extends ServiceImpl<BusinessUnitMapper, BusinessUnit> implements BusinessUnitService {

    @Override
    public IPage<BusinessUnitVO> queryPage(String keyword, String status, int pageNo, int pageSize) {
        LambdaQueryWrapper<BusinessUnit> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(keyword)) {
            wrapper.like(BusinessUnit::getCode, keyword)
                   .or()
                   .like(BusinessUnit::getName, keyword);
        }
        
        if (StringUtils.hasText(status)) {
            wrapper.eq(BusinessUnit::getStatus, status);
        }
        
        wrapper.orderByDesc(BusinessUnit::getCreatedAt);
        
        Page<BusinessUnit> page = new Page<>(pageNo, pageSize);
        IPage<BusinessUnit> result = this.page(page, wrapper);
        
        return result.convert(this::convertToVO);
    }

    @Override
    public BusinessUnitVO getById(Long id) {
        BusinessUnit entity = super.getById(id);
        return entity == null ? null : convertToVO(entity);
    }

    @Override
    public boolean save(BusinessUnitDTO dto) {
        BusinessUnit entity = new BusinessUnit();
        BeanUtils.copyProperties(dto, entity);
        return this.save(entity);
    }

    @Override
    public boolean update(BusinessUnitDTO dto) {
        BusinessUnit entity = new BusinessUnit();
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
    public List<BusinessUnitVO> listAll() {
        List<BusinessUnit> list = this.list();
        List<BusinessUnitVO> result = new ArrayList<>();
        for (BusinessUnit entity : list) {
            result.add(convertToVO(entity));
        }
        return result;
    }

    private BusinessUnitVO convertToVO(BusinessUnit entity) {
        BusinessUnitVO vo = new BusinessUnitVO();
        vo.setId(entity.getId());
        vo.setCode(entity.getCode());
        vo.setName(entity.getName());
        vo.setEnName(entity.getEnName());
        vo.setLegalPerson(entity.getLegalPerson());
        vo.setAddress(entity.getAddress());
        vo.setTaxNo(entity.getTaxNo());
        vo.setStatus(entity.getStatus());
        vo.setCreatedBy(entity.getCreatedBy());
        vo.setCreatedAt(entity.getCreatedAt());
        vo.setUpdatedBy(entity.getUpdatedBy());
        vo.setUpdatedAt(entity.getUpdatedAt());
        return vo;
    }
}

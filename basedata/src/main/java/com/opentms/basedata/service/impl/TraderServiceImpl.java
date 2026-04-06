package com.opentms.basedata.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.opentms.basedata.dto.TraderDTO;
import com.opentms.basedata.entity.Trader;
import com.opentms.basedata.mapper.TraderMapper;
import com.opentms.basedata.service.TraderService;
import com.opentms.basedata.vo.TraderVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class TraderServiceImpl extends ServiceImpl<TraderMapper, Trader> implements TraderService {

    @Override
    public IPage<TraderVO> queryPage(String keyword, String status, int pageNo, int pageSize) {
        LambdaQueryWrapper<Trader> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Trader::getCode, keyword)
                   .or()
                   .like(Trader::getName, keyword);
        }
        
        if (StringUtils.hasText(status)) {
            wrapper.eq(Trader::getStatus, status);
        }
        
        wrapper.orderByDesc(Trader::getCreatedAt);
        
        Page<Trader> page = new Page<>(pageNo, pageSize);
        IPage<Trader> result = this.page(page, wrapper);
        
        return result.convert(this::convertToVO);
    }

    @Override
    public TraderVO getById(Long id) {
        Trader entity = super.getById(id);
        return entity == null ? null : convertToVO(entity);
    }

    @Override
    public boolean save(TraderDTO dto) {
        Trader entity = new Trader();
        BeanUtils.copyProperties(dto, entity);
        return this.save(entity);
    }

    @Override
    public boolean update(TraderDTO dto) {
        Trader entity = new Trader();
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
    public List<TraderVO> listAll() {
        List<Trader> list = this.list();
        List<TraderVO> result = new ArrayList<>();
        for (Trader entity : list) {
            result.add(convertToVO(entity));
        }
        return result;
    }

    private TraderVO convertToVO(Trader entity) {
        TraderVO vo = new TraderVO();
        vo.setId(entity.getId());
        vo.setCode(entity.getCode());
        vo.setName(entity.getName());
        vo.setEnName(entity.getEnName());
        vo.setDepartment(entity.getDepartment());
        vo.setPhone(entity.getPhone());
        vo.setEmail(entity.getEmail());
        vo.setStatus(entity.getStatus());
        vo.setCreatedBy(entity.getCreatedBy());
        vo.setCreatedAt(entity.getCreatedAt());
        vo.setUpdatedBy(entity.getUpdatedBy());
        vo.setUpdatedAt(entity.getUpdatedAt());
        return vo;
    }
}

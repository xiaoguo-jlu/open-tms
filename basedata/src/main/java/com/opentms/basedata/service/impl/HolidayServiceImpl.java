package com.opentms.basedata.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.opentms.basedata.dto.HolidayDTO;
import com.opentms.basedata.entity.Holiday;
import com.opentms.basedata.mapper.HolidayMapper;
import com.opentms.basedata.service.HolidayService;
import com.opentms.basedata.vo.HolidayVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class HolidayServiceImpl extends ServiceImpl<HolidayMapper, Holiday> implements HolidayService {

    @Override
    public IPage<HolidayVO> queryPage(Integer year, String countryCode, int pageNo, int pageSize) {
        LambdaQueryWrapper<Holiday> wrapper = new LambdaQueryWrapper<>();
        
        if (year != null) {
            LocalDate start = LocalDate.of(year, 1, 1);
            LocalDate end = LocalDate.of(year, 12, 31);
            wrapper.ge(Holiday::getHolidayDate, start).le(Holiday::getHolidayDate, end);
        }
        
        if (countryCode != null && !countryCode.isEmpty()) {
            wrapper.eq(Holiday::getCountryCode, countryCode);
        }
        
        wrapper.orderByAsc(Holiday::getHolidayDate);
        
        Page<Holiday> page = new Page<>(pageNo, pageSize);
        IPage<Holiday> result = this.page(page, wrapper);
        
        return result.convert(this::convertToVO);
    }

    @Override
    public HolidayVO getById(Long id) {
        Holiday entity = super.getById(id);
        return entity == null ? null : convertToVO(entity);
    }

    @Override
    public boolean save(HolidayDTO dto) {
        Holiday entity = new Holiday();
        BeanUtils.copyProperties(dto, entity);
        return this.save(entity);
    }

    @Override
    public boolean delete(Long id) {
        return this.removeById(id);
    }

    @Override
    public boolean batchDelete(List<Long> ids) {
        return this.removeByIds(ids);
    }

    private HolidayVO convertToVO(Holiday entity) {
        HolidayVO vo = new HolidayVO();
        vo.setId(entity.getId());
        vo.setHolidayDate(entity.getHolidayDate());
        vo.setName(entity.getName());
        vo.setCountryCode(entity.getCountryCode());
        vo.setIsAdjust(entity.getIsAdjust());
        vo.setRemark(entity.getRemark());
        vo.setCreatedBy(entity.getCreatedBy());
        vo.setCreatedAt(entity.getCreatedAt());
        vo.setUpdatedBy(entity.getUpdatedBy());
        vo.setUpdatedAt(entity.getUpdatedAt());
        return vo;
    }
}

package com.opentms.basedata.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.opentms.basedata.entity.Holiday;
import com.opentms.basedata.mapper.HolidayMapper;
import com.opentms.basedata.service.HolidayService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;

@Service
public class HolidayServiceImpl extends ServiceImpl<HolidayMapper, Holiday> implements HolidayService {

    @Override
    public Page<Holiday> queryPage(String countryCode, Integer year, int pageNum, int pageSize) {
        LambdaQueryWrapper<Holiday> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(countryCode)) {
            wrapper.eq(Holiday::getCountryCode, countryCode);
        }

        if (year != null) {
            wrapper.apply("YEAR(holiday_date) = {0}", year);
        }

        wrapper.orderByAsc(Holiday::getHolidayDate);

        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public Holiday getHolidayById(Long id) {
        return getById(id);
    }

    @Override
    public boolean saveHoliday(Holiday holiday) {
        if (checkDateExists(holiday.getHolidayDate(), holiday.getCountryCode(), null)) {
            throw new RuntimeException("Holiday date already exists for this country");
        }
        return save(holiday);
    }

    @Override
    public boolean updateHoliday(Holiday holiday) {
        if (checkDateExists(holiday.getHolidayDate(), holiday.getCountryCode(), holiday.getId())) {
            throw new RuntimeException("Holiday date already exists for this country");
        }
        return updateById(holiday);
    }

    @Override
    public boolean deleteHoliday(Long id) {
        return removeById(id);
    }

    @Override
    public boolean checkDateExists(LocalDate date, String countryCode, Long excludeId) {
        if (date == null || !StringUtils.hasText(countryCode)) {
            return false;
        }
        LambdaQueryWrapper<Holiday> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Holiday::getHolidayDate, date)
               .eq(Holiday::getCountryCode, countryCode);
        if (excludeId != null) {
            wrapper.ne(Holiday::getId, excludeId);
        }
        return count(wrapper) > 0;
    }

    @Override
    public boolean isHoliday(LocalDate date, String countryCode) {
        if (date == null || !StringUtils.hasText(countryCode)) {
            return false;
        }
        LambdaQueryWrapper<Holiday> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Holiday::getHolidayDate, date)
               .eq(Holiday::getCountryCode, countryCode);
        return count(wrapper) > 0;
    }
}
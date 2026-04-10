package com.opentms.basedata.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.opentms.basedata.entity.Holiday;

import java.time.LocalDate;

public interface HolidayService {

    Page<Holiday> queryPage(String countryCode, Integer year, int pageNum, int pageSize);

    Holiday getHolidayById(Long id);

    boolean saveHoliday(Holiday holiday);

    boolean updateHoliday(Holiday holiday);

    boolean deleteHoliday(Long id);

    boolean checkDateExists(LocalDate date, String countryCode, Long excludeId);

    boolean isHoliday(LocalDate date, String countryCode);
}
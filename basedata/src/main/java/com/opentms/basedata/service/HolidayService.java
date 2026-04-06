package com.opentms.basedata.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.opentms.basedata.dto.HolidayDTO;
import com.opentms.basedata.entity.Holiday;
import com.opentms.basedata.vo.HolidayVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface HolidayService extends IService<Holiday> {

    IPage<HolidayVO> queryPage(Integer year, String countryCode, int pageNo, int pageSize);

    HolidayVO getById(Long id);

    boolean save(HolidayDTO dto);

    boolean delete(Long id);

    boolean batchDelete(List<Long> ids);
}

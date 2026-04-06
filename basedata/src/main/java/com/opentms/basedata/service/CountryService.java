package com.opentms.basedata.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.opentms.basedata.dto.CountryDTO;
import com.opentms.basedata.entity.Country;
import com.opentms.basedata.vo.CountryVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface CountryService extends IService<Country> {

    IPage<CountryVO> queryPage(String keyword, String status, int pageNo, int pageSize);

    CountryVO getById(Long id);

    boolean save(CountryDTO dto);

    boolean update(CountryDTO dto);

    boolean delete(Long id);

    boolean batchDelete(List<Long> ids);

    List<CountryVO> listAll();
}

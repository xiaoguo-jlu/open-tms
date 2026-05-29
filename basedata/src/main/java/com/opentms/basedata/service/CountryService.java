package com.opentms.basedata.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.opentms.basedata.entity.Country;
import com.opentms.basedata.vo.CountryVO;

import java.util.List;

public interface CountryService {

    List<CountryVO> listAll();

    IPage<CountryVO> queryPage(String keyword, String status, int pageNum, int pageSize);

    CountryVO getCountryById(Long id);

    boolean saveCountry(Country country);

    boolean updateCountry(Country country);

    boolean deleteCountry(Long id);

    boolean checkCodeExists(String code, Long excludeId);
}
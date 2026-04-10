package com.opentms.basedata.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.opentms.basedata.entity.Country;

public interface CountryService {

    Page<Country> queryPage(String keyword, String status, int pageNum, int pageSize);

    Country getCountryById(Long id);

    boolean saveCountry(Country country);

    boolean updateCountry(Country country);

    boolean deleteCountry(Long id);

    boolean checkCodeExists(String code, Long excludeId);
}
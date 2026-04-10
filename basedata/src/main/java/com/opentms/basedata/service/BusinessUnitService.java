package com.opentms.basedata.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.opentms.basedata.entity.BusinessUnit;

public interface BusinessUnitService {

    Page<BusinessUnit> queryPage(String keyword, String status, int pageNum, int pageSize);

    BusinessUnit getBusinessUnitById(Long id);

    boolean saveBusinessUnit(BusinessUnit businessUnit);

    boolean updateBusinessUnit(BusinessUnit businessUnit);

    boolean deleteBusinessUnit(Long id);

    boolean checkCodeExists(String code, Long excludeId);
}
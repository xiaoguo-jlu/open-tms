package com.opentms.basedata.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.opentms.basedata.entity.BusinessUnit;
import com.opentms.basedata.vo.BusinessUnitVO;

import java.util.List;

public interface BusinessUnitService {

    Page<BusinessUnit> queryPage(String keyword, String status, String entityType, int pageNum, int pageSize);

    BusinessUnit getBusinessUnitById(Long id);

    boolean saveBusinessUnit(BusinessUnit businessUnit);

    boolean updateBusinessUnit(BusinessUnit businessUnit);

    boolean deleteBusinessUnit(Long id);

    boolean checkCodeExists(String code, Long excludeId);

    List<BusinessUnitVO> getHierarchyTree();
}
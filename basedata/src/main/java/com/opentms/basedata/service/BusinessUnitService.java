package com.opentms.basedata.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.opentms.basedata.dto.BusinessUnitDTO;
import com.opentms.basedata.entity.BusinessUnit;
import com.opentms.basedata.vo.BusinessUnitVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface BusinessUnitService extends IService<BusinessUnit> {

    IPage<BusinessUnitVO> queryPage(String keyword, String status, int pageNo, int pageSize);

    BusinessUnitVO getById(Long id);

    boolean save(BusinessUnitDTO dto);

    boolean update(BusinessUnitDTO dto);

    boolean delete(Long id);

    boolean batchDelete(List<Long> ids);

    List<BusinessUnitVO> listAll();
}

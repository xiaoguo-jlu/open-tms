package com.opentms.basedata.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.opentms.basedata.entity.ManagementEntity;
import com.opentms.basedata.vo.ManagementEntityVO;

import java.util.List;

public interface ManagementEntityService {

    Page<ManagementEntity> queryPage(String keyword, String status, String entityType, int pageNum, int pageSize);

    ManagementEntity getManagementEntityById(Long id);

    boolean saveManagementEntity(ManagementEntity managementEntity);

    boolean updateManagementEntity(ManagementEntity managementEntity);

    boolean deleteManagementEntity(Long id);

    boolean checkCodeExists(String code, Long excludeId);

    List<ManagementEntityVO> getHierarchyTree();
}
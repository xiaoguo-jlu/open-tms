package com.opentms.basedata.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.opentms.basedata.entity.ManagementEntity;
import com.opentms.basedata.mapper.ManagementEntityMapper;
import com.opentms.basedata.service.ManagementEntityService;
import com.opentms.basedata.vo.ManagementEntityVO;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ManagementEntityServiceImpl extends ServiceImpl<ManagementEntityMapper, ManagementEntity> implements ManagementEntityService {

    @Override
    public Page<ManagementEntity> queryPage(String keyword, String status, String entityType, int pageNum, int pageSize) {
        LambdaQueryWrapper<ManagementEntity> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(keyword)) {
            wrapper.like(ManagementEntity::getCode, keyword)
                   .or()
                   .like(ManagementEntity::getName, keyword);
        }

        if (StringUtils.hasText(status)) {
            wrapper.eq(ManagementEntity::getStatus, status);
        }

        if (StringUtils.hasText(entityType)) {
            wrapper.eq(ManagementEntity::getEntityType, entityType);
        }

        wrapper.orderByDesc(ManagementEntity::getCreatedAt);

        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public ManagementEntity getManagementEntityById(Long id) {
        return getById(id);
    }

    @Override
    public boolean saveManagementEntity(ManagementEntity managementEntity) {
        if (checkCodeExists(managementEntity.getCode(), null)) {
            throw new RuntimeException("管理主体编码已存在");
        }
        calculateHierarchyInfo(managementEntity);
        return save(managementEntity);
    }

    @Override
    public boolean updateManagementEntity(ManagementEntity managementEntity) {
        if (managementEntity.getId() == null) {
            throw new RuntimeException("ManagementEntity ID cannot be null");
        }
        ManagementEntity existing = getById(managementEntity.getId());
        if (existing == null) {
            throw new RuntimeException("ManagementEntity not found");
        }
        if (checkCodeExists(managementEntity.getCode(), managementEntity.getId())) {
            throw new RuntimeException("管理主体编码已存在");
        }
        calculateHierarchyInfo(managementEntity);
        return updateById(managementEntity);
    }

    @Override
    public boolean deleteManagementEntity(Long id) {
        ManagementEntity existing = getById(id);
        if (existing == null) {
            throw new RuntimeException("ManagementEntity not found");
        }
        return removeById(id);
    }

    @Override
    public boolean checkCodeExists(String code, Long excludeId) {
        if (!StringUtils.hasText(code)) {
            return false;
        }
        LambdaQueryWrapper<ManagementEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ManagementEntity::getCode, code);
        if (excludeId != null) {
            wrapper.ne(ManagementEntity::getId, excludeId);
        }
        return count(wrapper) > 0;
    }

    @Override
    public List<ManagementEntityVO> getHierarchyTree() {
        LambdaQueryWrapper<ManagementEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ManagementEntity::getStatus, "1");
        wrapper.orderByAsc(ManagementEntity::getLevelDepth, ManagementEntity::getCode);
        List<ManagementEntity> allUnits = list(wrapper);

        Map<String, List<ManagementEntity>> parentChildrenMap = allUnits.stream()
                .filter(u -> u.getParentCode() != null)
                .collect(Collectors.groupingBy(ManagementEntity::getParentCode));

        List<ManagementEntityVO> result = new ArrayList<>();
        for (ManagementEntity unit : allUnits) {
            ManagementEntityVO vo = convertToVO(unit);
            if (unit.getParentCode() == null || unit.getParentCode().isEmpty()) {
                result.add(vo);
            }
            List<ManagementEntity> children = parentChildrenMap.get(unit.getCode());
            if (children != null) {
                List<ManagementEntityVO> childVOs = children.stream()
                        .map(this::convertToVO)
                        .collect(Collectors.toList());
                vo.setChildren(childVOs);
            }
        }
        return result;
    }

    private void calculateHierarchyInfo(ManagementEntity managementEntity) {
        String parentCode = managementEntity.getParentCode();
        if (!StringUtils.hasText(parentCode)) {
            managementEntity.setLevelDepth(1);
            managementEntity.setHierarchyPath("/" + managementEntity.getCode() + "/");
        } else {
            ManagementEntity parent = getOne(new LambdaQueryWrapper<ManagementEntity>()
                    .eq(ManagementEntity::getCode, parentCode));
            if (parent == null) {
                throw new RuntimeException("上级管理主体不存在: " + parentCode);
            }
            int newLevelDepth = parent.getLevelDepth() + 1;
            if (newLevelDepth > 6) {
                throw new RuntimeException("管理层级深度超出最大限制(6)");
            }
            managementEntity.setLevelDepth(newLevelDepth);
            managementEntity.setHierarchyPath(parent.getHierarchyPath() + managementEntity.getCode() + "/");
        }
    }

    private ManagementEntityVO convertToVO(ManagementEntity unit) {
        ManagementEntityVO vo = new ManagementEntityVO();
        vo.setId(unit.getId());
        vo.setCode(unit.getCode());
        vo.setName(unit.getName());
        vo.setEnName(unit.getEnName());
        vo.setEntityType(unit.getEntityType());
        vo.setParentCode(unit.getParentCode());
        vo.setLevelDepth(unit.getLevelDepth());
        vo.setHierarchyPath(unit.getHierarchyPath());
        vo.setLegalPerson(unit.getLegalPerson());
        vo.setRegisteredAddress(unit.getRegisteredAddress());
        vo.setOfficeAddress(unit.getOfficeAddress());
        vo.setUnifiedSocialCreditCode(unit.getUnifiedSocialCreditCode());
        vo.setBusinessLicenseNo(unit.getBusinessLicenseNo());
        vo.setEstablishmentDate(unit.getEstablishmentDate());
        vo.setTaxNo(unit.getTaxNo());
        vo.setStatus(unit.getStatus());
        vo.setCreatedBy(unit.getCreatedBy());
        vo.setCreatedAt(unit.getCreatedAt());
        vo.setUpdatedBy(unit.getUpdatedBy());
        vo.setUpdatedAt(unit.getUpdatedAt());
        return vo;
    }
}
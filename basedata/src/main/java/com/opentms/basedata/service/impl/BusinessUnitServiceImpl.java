package com.opentms.basedata.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.opentms.basedata.entity.BusinessUnit;
import com.opentms.basedata.mapper.BusinessUnitMapper;
import com.opentms.basedata.service.BusinessUnitService;
import com.opentms.basedata.vo.BusinessUnitVO;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BusinessUnitServiceImpl extends ServiceImpl<BusinessUnitMapper, BusinessUnit> implements BusinessUnitService {

    @Override
    public Page<BusinessUnit> queryPage(String keyword, String status, String entityType, int pageNum, int pageSize) {
        LambdaQueryWrapper<BusinessUnit> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(keyword)) {
            wrapper.like(BusinessUnit::getCode, keyword)
                   .or()
                   .like(BusinessUnit::getName, keyword);
        }

        if (StringUtils.hasText(status)) {
            wrapper.eq(BusinessUnit::getStatus, status);
        }

        if (StringUtils.hasText(entityType)) {
            wrapper.eq(BusinessUnit::getEntityType, entityType);
        }

        wrapper.orderByDesc(BusinessUnit::getCreatedAt);

        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public BusinessUnit getBusinessUnitById(Long id) {
        return getById(id);
    }

    @Override
    public boolean saveBusinessUnit(BusinessUnit businessUnit) {
        if (checkCodeExists(businessUnit.getCode(), null)) {
            throw new RuntimeException("Business unit code already exists");
        }
        calculateHierarchyInfo(businessUnit);
        return save(businessUnit);
    }

    @Override
    public boolean updateBusinessUnit(BusinessUnit businessUnit) {
        if (businessUnit.getId() == null) {
            throw new RuntimeException("BusinessUnit ID cannot be null");
        }
        BusinessUnit existing = getById(businessUnit.getId());
        if (existing == null) {
            throw new RuntimeException("BusinessUnit not found");
        }
        if (checkCodeExists(businessUnit.getCode(), businessUnit.getId())) {
            throw new RuntimeException("Business unit code already exists");
        }
        calculateHierarchyInfo(businessUnit);
        return updateById(businessUnit);
    }

    @Override
    public boolean deleteBusinessUnit(Long id) {
        BusinessUnit existing = getById(id);
        if (existing == null) {
            throw new RuntimeException("BusinessUnit not found");
        }
        return removeById(id);
    }

    @Override
    public boolean checkCodeExists(String code, Long excludeId) {
        if (!StringUtils.hasText(code)) {
            return false;
        }
        LambdaQueryWrapper<BusinessUnit> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BusinessUnit::getCode, code);
        if (excludeId != null) {
            wrapper.ne(BusinessUnit::getId, excludeId);
        }
        return count(wrapper) > 0;
    }

    @Override
    public List<BusinessUnitVO> getHierarchyTree() {
        LambdaQueryWrapper<BusinessUnit> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BusinessUnit::getStatus, "1");
        wrapper.orderByAsc(BusinessUnit::getLevelDepth, BusinessUnit::getCode);
        List<BusinessUnit> allUnits = list(wrapper);

        Map<String, List<BusinessUnit>> parentChildrenMap = allUnits.stream()
                .filter(u -> u.getParentCode() != null)
                .collect(Collectors.groupingBy(BusinessUnit::getParentCode));

        List<BusinessUnitVO> result = new ArrayList<>();
        for (BusinessUnit unit : allUnits) {
            BusinessUnitVO vo = convertToVO(unit);
            if (unit.getParentCode() == null || unit.getParentCode().isEmpty()) {
                result.add(vo);
            }
            List<BusinessUnit> children = parentChildrenMap.get(unit.getCode());
            if (children != null) {
                List<BusinessUnitVO> childVOs = children.stream()
                        .map(this::convertToVO)
                        .collect(Collectors.toList());
                vo.setChildren(childVOs);
            }
        }
        return result;
    }

    private void calculateHierarchyInfo(BusinessUnit businessUnit) {
        String parentCode = businessUnit.getParentCode();
        if (!StringUtils.hasText(parentCode)) {
            businessUnit.setLevelDepth(1);
            businessUnit.setHierarchyPath("/" + businessUnit.getCode() + "/");
        } else {
            BusinessUnit parent = getOne(new LambdaQueryWrapper<BusinessUnit>()
                    .eq(BusinessUnit::getCode, parentCode));
            if (parent == null) {
                throw new RuntimeException("Parent business unit not found: " + parentCode);
            }
            int newLevelDepth = parent.getLevelDepth() + 1;
            if (newLevelDepth > 6) {
                throw new RuntimeException("Maximum hierarchy depth (6) exceeded");
            }
            businessUnit.setLevelDepth(newLevelDepth);
            businessUnit.setHierarchyPath(parent.getHierarchyPath() + businessUnit.getCode() + "/");
        }
    }

    private BusinessUnitVO convertToVO(BusinessUnit unit) {
        BusinessUnitVO vo = new BusinessUnitVO();
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
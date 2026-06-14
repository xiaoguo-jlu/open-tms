package com.opentms.basedata.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.opentms.basedata.dto.SubsidiaryDTO;
import com.opentms.basedata.entity.Subsidiary;
import com.opentms.basedata.mapper.SubsidiaryMapper;
import com.opentms.basedata.service.SubsidiaryService;
import com.opentms.basedata.vo.SubsidiaryVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * 子公司Service实现
 */
@Slf4j
@Service
public class SubsidiaryServiceImpl extends ServiceImpl<SubsidiaryMapper, Subsidiary> implements SubsidiaryService {

    @Override
    public Page<SubsidiaryVO> queryPage(String keyword, String status, int pageNum, int pageSize) {
        LambdaQueryWrapper<Subsidiary> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(keyword)) {
            wrapper.like(Subsidiary::getCode, keyword)
                   .or()
                   .like(Subsidiary::getName, keyword);
        }

        if (StringUtils.hasText(status)) {
            wrapper.eq(Subsidiary::getStatus, status);
        }

        wrapper.orderByDesc(Subsidiary::getCreatedAt);

        Page<Subsidiary> page = baseMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);

        Page<SubsidiaryVO> result = new Page<>();
        result.setCurrent(page.getCurrent());
        result.setSize(page.getSize());
        result.setTotal(page.getTotal());
        result.setRecords(page.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList()));

        return result;
    }

    @Override
    public SubsidiaryVO getSubsidiaryById(Long id) {
        Subsidiary subsidiary = getById(id);
        return subsidiary != null ? convertToVO(subsidiary) : null;
    }

    @Override
    public SubsidiaryVO saveSubsidiary(SubsidiaryDTO dto) {
        log.info("[新增子公司] code={}", dto.getCode());

        if (checkCodeExists(dto.getCode(), null)) {
            throw new RuntimeException("子公司编码已存在: " + dto.getCode());
        }

        Subsidiary entity = convertToEntity(dto);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setCreatedBy("system");
        entity.setStatus("1");

        save(entity);

        log.info("[新增子公司] success id={}", entity.getId());
        return convertToVO(entity);
    }

    @Override
    public SubsidiaryVO updateSubsidiary(SubsidiaryDTO dto) {
        log.info("[更新子公司] id={}", dto.getId());

        if (dto.getId() == null) {
            throw new RuntimeException("ID不能为空");
        }

        Subsidiary existing = getById(dto.getId());
        if (existing == null) {
            throw new RuntimeException("子公司不存在");
        }

        if (checkCodeExists(dto.getCode(), dto.getId())) {
            throw new RuntimeException("子公司编码已存在: " + dto.getCode());
        }

        // Use LambdaUpdateWrapper to avoid optimistic lock issues with @Version
        LambdaUpdateWrapper<Subsidiary> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Subsidiary::getId, dto.getId());
        wrapper.set(Subsidiary::getName, dto.getName());
        wrapper.set(Subsidiary::getEnName, dto.getEnName());
        wrapper.set(Subsidiary::getParentCode, dto.getParentCode());
        wrapper.set(Subsidiary::getBusinessUnitCode, dto.getBusinessUnitCode());
        wrapper.set(Subsidiary::getLegalPerson, dto.getLegalPerson());
        wrapper.set(Subsidiary::getRegistrationNo, dto.getRegistrationNo());
        wrapper.set(Subsidiary::getTaxNo, dto.getTaxNo());
        wrapper.set(Subsidiary::getAddress, dto.getAddress());
        wrapper.set(Subsidiary::getPhone, dto.getPhone());
        wrapper.set(Subsidiary::getEmail, dto.getEmail());
        wrapper.set(Subsidiary::getStatus, dto.getStatus());
        wrapper.set(Subsidiary::getRemark, dto.getRemark());
        wrapper.set(Subsidiary::getUpdatedAt, LocalDateTime.now());
        wrapper.set(Subsidiary::getUpdatedBy, "system");
        this.update(wrapper);

        log.info("[更新子公司] success id={}", dto.getId());
        return convertToVO(getById(dto.getId()));
    }

    @Override
    public void deleteSubsidiary(Long id) {
        log.info("[删除子公司] id={}", id);

        Subsidiary existing = getById(id);
        if (existing == null) {
            throw new RuntimeException("子公司不存在");
        }

        baseMapper.deleteById(id);

        log.info("[删除子公司] success id={}", id);
    }

    @Override
    public boolean checkCodeExists(String code, Long excludeId) {
        if (!StringUtils.hasText(code)) {
            return false;
        }
        LambdaQueryWrapper<Subsidiary> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Subsidiary::getCode, code);
        if (excludeId != null) {
            wrapper.ne(Subsidiary::getId, excludeId);
        }
        return count(wrapper) > 0;
    }

    private SubsidiaryVO convertToVO(Subsidiary entity) {
        SubsidiaryVO vo = new SubsidiaryVO();
        vo.setId(entity.getId());
        vo.setCode(entity.getCode());
        vo.setName(entity.getName());
        vo.setEnName(entity.getEnName());
        vo.setParentCode(entity.getParentCode());
        vo.setBusinessUnitCode(entity.getBusinessUnitCode());
        vo.setLegalPerson(entity.getLegalPerson());
        vo.setRegistrationNo(entity.getRegistrationNo());
        vo.setTaxNo(entity.getTaxNo());
        vo.setAddress(entity.getAddress());
        vo.setPhone(entity.getPhone());
        vo.setEmail(entity.getEmail());
        vo.setStatus(entity.getStatus());
        vo.setRemark(entity.getRemark());
        vo.setCreatedAt(entity.getCreatedAt());
        vo.setUpdatedAt(entity.getUpdatedAt());
        return vo;
    }

    private Subsidiary convertToEntity(SubsidiaryDTO dto) {
        Subsidiary entity = new Subsidiary();
        entity.setId(dto.getId());
        entity.setCode(dto.getCode());
        entity.setName(dto.getName());
        entity.setEnName(dto.getEnName());
        entity.setParentCode(dto.getParentCode());
        entity.setBusinessUnitCode(dto.getBusinessUnitCode());
        entity.setLegalPerson(dto.getLegalPerson());
        entity.setRegistrationNo(dto.getRegistrationNo());
        entity.setTaxNo(dto.getTaxNo());
        entity.setAddress(dto.getAddress());
        entity.setPhone(dto.getPhone());
        entity.setEmail(dto.getEmail());
        entity.setStatus(dto.getStatus());
        entity.setRemark(dto.getRemark());
        return entity;
    }
}
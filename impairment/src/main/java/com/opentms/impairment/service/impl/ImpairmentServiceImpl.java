package com.opentms.impairment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.opentms.impairment.entity.Impairment;
import com.opentms.impairment.mapper.ImpairmentMapper;
import com.opentms.impairment.service.ImpairmentService;
import com.opentms.impairment.vo.ImpairmentVO;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;

@Service
public class ImpairmentServiceImpl extends ServiceImpl<ImpairmentMapper, Impairment> implements ImpairmentService {

    @Override
    public Page<Impairment> queryPage(String keyword, String status, Long businessUnitId, String assessmentType, int pageNum, int pageSize) {
        LambdaQueryWrapper<Impairment> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(keyword)) {
            wrapper.like(Impairment::getAssessmentType, keyword);
        }

        if (StringUtils.hasText(status)) {
            wrapper.eq(Impairment::getStatus, status);
        }

        if (businessUnitId != null) {
            wrapper.eq(Impairment::getBusinessUnitId, businessUnitId);
        }

        if (StringUtils.hasText(assessmentType)) {
            wrapper.eq(Impairment::getAssessmentType, assessmentType);
        }

        wrapper.orderByDesc(Impairment::getCreatedAt);
        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public Impairment getById(Long id) {
        return getById(id);
    }

    @Override
    public ImpairmentVO getDetailById(Long id) {
        Impairment impairment = super.getById(id);
        if (impairment == null) {
            return null;
        }
        return convertToVO(impairment);
    }

    @Override
    public ImpairmentVO getResult(Long id) {
        Impairment impairment = super.getById(id);
        if (impairment == null) {
            return null;
        }
        ImpairmentVO vo = convertToVO(impairment);
        if (impairment.getTotalExposure() != null && impairment.getProvisionRate() != null) {
            vo.setExpectedLoss(impairment.getTotalExposure().multiply(impairment.getProvisionRate()));
        }
        return vo;
    }

    @Override
    public ImpairmentVO getDetails(Long id) {
        return getResult(id);
    }

    @Override
    public boolean save(Impairment impairment) {
        return save(impairment);
    }

    @Override
    public Impairment calculate(Long id) {
        Impairment impairment = super.getById(id);
        if (impairment == null) {
            throw new RuntimeException("Impairment not found");
        }

        if (impairment.getTotalExposure() != null && impairment.getProvisionRate() != null) {
            BigDecimal expectedLoss = impairment.getTotalExposure().multiply(impairment.getProvisionRate());
            impairment.setExpectedLoss(expectedLoss);
            impairment.setStatus("CALCULATED");
            updateById(impairment);
        }

        return impairment;
    }

    private ImpairmentVO convertToVO(Impairment impairment) {
        ImpairmentVO vo = new ImpairmentVO();
        vo.setId(impairment.getId());
        vo.setAssessmentDate(impairment.getAssessmentDate());
        vo.setBusinessUnitId(impairment.getBusinessUnitId());
        vo.setAssessmentType(impairment.getAssessmentType());
        vo.setTotalExposure(impairment.getTotalExposure());
        vo.setExpectedLoss(impairment.getExpectedLoss());
        vo.setProvisionRate(impairment.getProvisionRate());
        vo.setStatus(impairment.getStatus());
        vo.setCreatedBy(impairment.getCreatedBy());
        vo.setCreatedAt(impairment.getCreatedAt());
        vo.setUpdatedBy(impairment.getUpdatedBy());
        vo.setUpdatedAt(impairment.getUpdatedAt());
        return vo;
    }
}
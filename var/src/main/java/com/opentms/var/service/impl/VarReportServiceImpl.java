package com.opentms.var.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.opentms.var.entity.VarReport;
import com.opentms.var.mapper.VarReportMapper;
import com.opentms.var.service.VarReportService;
import com.opentms.var.vo.VarReportVO;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class VarReportServiceImpl extends ServiceImpl<VarReportMapper, VarReport> implements VarReportService {

    @Override
    public Page<VarReport> queryPage(String varType, LocalDate startDate, LocalDate endDate, 
                                      Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<VarReport> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(varType)) {
            wrapper.eq(VarReport::getVarType, varType);
        }

        if (startDate != null) {
            wrapper.ge(VarReport::getReportDate, startDate);
        }

        if (endDate != null) {
            wrapper.le(VarReport::getReportDate, endDate);
        }

        wrapper.orderByDesc(VarReport::getReportDate);

        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public VarReportVO getById(Long id) {
        VarReport varReport = super.getById(id);
        if (varReport == null) {
            return null;
        }
        return convertToVO(varReport);
    }

    @Override
    public VarReport calculate() {
        VarReport varReport = new VarReport();
        varReport.setReportDate(LocalDate.now());
        varReport.setVarType("历史模拟法");
        varReport.setConfidenceLevel(new BigDecimal("0.99"));
        varReport.setHoldingPeriod(1);
        varReport.setTotalVar(new BigDecimal("1000000"));
        varReport.setFxVar(new BigDecimal("500000"));
        varReport.setIrVar(new BigDecimal("300000"));
        varReport.setCreditVar(new BigDecimal("200000"));
        varReport.setStatus("ACTIVE");
        save(varReport);
        return varReport;
    }

    @Override
    public List<Map<String, Object>> getTrend(LocalDate startDate, LocalDate endDate) {
        LambdaQueryWrapper<VarReport> wrapper = new LambdaQueryWrapper<>();
        
        if (startDate != null) {
            wrapper.ge(VarReport::getReportDate, startDate);
        }
        if (endDate != null) {
            wrapper.le(VarReport::getReportDate, endDate);
        }
        
        wrapper.orderByAsc(VarReport::getReportDate);
        List<VarReport> reports = list(wrapper);
        
        List<Map<String, Object>> trend = new ArrayList<>();
        for (VarReport report : reports) {
            Map<String, Object> item = new HashMap<>();
            item.put("reportDate", report.getReportDate());
            item.put("totalVar", report.getTotalVar());
            item.put("fxVar", report.getFxVar());
            item.put("irVar", report.getIrVar());
            item.put("creditVar", report.getCreditVar());
            trend.add(item);
        }
        
        return trend;
    }

    @Override
    public Map<String, Object> stressTest(Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        result.put("scenario", params.get("scenario"));
        result.put("totalVar", new BigDecimal("1500000"));
        result.put("fxVar", new BigDecimal("750000"));
        result.put("irVar", new BigDecimal("450000"));
        result.put("creditVar", new BigDecimal("300000"));
        result.put("impact", "15%");
        return result;
    }

    @Override
    public boolean save(VarReport varReport) {
        return VarReportServiceImpl.super.save(varReport);
    }

    @Override
    public boolean update(VarReport varReport) {
        return updateById(varReport);
    }

    @Override
    public boolean delete(Long id) {
        return removeById(id);
    }

    private VarReportVO convertToVO(VarReport varReport) {
        VarReportVO vo = new VarReportVO();
        vo.setId(varReport.getId());
        vo.setReportDate(varReport.getReportDate());
        vo.setVarType(varReport.getVarType());
        vo.setConfidenceLevel(varReport.getConfidenceLevel());
        vo.setHoldingPeriod(varReport.getHoldingPeriod());
        vo.setTotalVar(varReport.getTotalVar());
        vo.setFxVar(varReport.getFxVar());
        vo.setIrVar(varReport.getIrVar());
        vo.setCreditVar(varReport.getCreditVar());
        vo.setStatus(varReport.getStatus());
        vo.setCreatedAt(varReport.getCreatedAt());
        vo.setUpdatedAt(varReport.getUpdatedAt());
        return vo;
    }
}
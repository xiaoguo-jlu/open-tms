package com.opentms.var.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.opentms.var.entity.VarReport;
import com.opentms.var.vo.VarReportVO;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface VarReportService {

    Page<VarReport> queryPage(String varType, LocalDate startDate, LocalDate endDate, 
                               Integer pageNum, Integer pageSize);

    VarReportVO getById(Long id);

    VarReport calculate();

    List<Map<String, Object>> getTrend(LocalDate startDate, LocalDate endDate);

    Map<String, Object> stressTest(Map<String, Object> params);

    boolean save(VarReport varReport);

    boolean update(VarReport varReport);

    boolean delete(Long id);
}
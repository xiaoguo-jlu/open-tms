package com.opentms.impairment.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.opentms.impairment.entity.Impairment;
import com.opentms.impairment.vo.ImpairmentVO;

public interface ImpairmentService {

    Page<Impairment> queryPage(String keyword, String status, Long businessUnitId, String assessmentType, int pageNum, int pageSize);

    Impairment getById(Long id);

    ImpairmentVO getDetailById(Long id);

    ImpairmentVO getResult(Long id);

    ImpairmentVO getDetails(Long id);

    boolean save(Impairment impairment);

    Impairment calculate(Long id);
}
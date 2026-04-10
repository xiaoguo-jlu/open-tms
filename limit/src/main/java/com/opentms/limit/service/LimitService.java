package com.opentms.limit.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.opentms.limit.entity.Limit;

import java.util.List;

public interface LimitService {

    Page<Limit> queryPage(String keyword, String limitType, Long businessUnitId, int pageNum, int pageSize);

    Limit getLimitById(Long id);

    boolean saveLimit(Limit limit);

    boolean updateLimit(Limit limit);

    boolean deleteLimit(Long id);

    boolean checkCodeExists(String code, Long excludeId);

    Limit monitor(Long id);

    List<Limit> getAlerts();
}
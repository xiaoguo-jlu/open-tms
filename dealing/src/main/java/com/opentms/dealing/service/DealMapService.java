package com.opentms.dealing.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.opentms.dealing.entity.DealMap;
import com.opentms.dealing.vo.DealMapVO;

import java.util.List;

public interface DealMapService {

    Page<DealMapVO> queryPage(String dealNumber, String eventType, String eventStatus,
                              int pageNum, int pageSize);

    DealMapVO getById(Long id);

    List<DealMapVO> listByDealNumber(String dealNumber);

    /**
     * 直接保存 DealMap（事务内调用）
     */
    boolean save(DealMap dealMap);

    /**
     * 创建 DealMap（v2.0 - 由 CREATE/UPDATE Action 触发）
     * @return 生成的 dealmapNumber
     */
    String createDealMap(DealMap dealMap);

    /**
     * 软删除指定 Deal 的所有 Active DealMap
     * @return 影响的行数
     */
    int softDeleteByDealNumber(String dealNumber);

    /**
     * 按条件查询单个 DealMap
     */
    DealMap getOne(LambdaQueryWrapper<DealMap> wrapper);

    /**
     * 冲销 DealMap
     */
    DealMap reverseDealMap(Long id, String operator, String remark);

    /**
     * 生成 DealMap 编号（DMP + yyyyMMdd + 4位序号）
     */
    String generateDealMapNumber();
}

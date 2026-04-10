package com.opentms.hedge.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.opentms.hedge.entity.HedgeRelation;
import com.opentms.hedge.vo.HedgeRelationVO;

public interface HedgeRelationService extends IService<HedgeRelation> {

    Page<HedgeRelationVO> queryPage(String keyword, String status, int pageNum, int pageSize);

    HedgeRelationVO getHedgeRelationById(Long id);

    HedgeRelationVO getEffectiveness(Long id);

    HedgeRelationVO getPnL(Long id);

    boolean saveHedgeRelation(HedgeRelation hedgeRelation);

    boolean updateHedgeRelation(HedgeRelation hedgeRelation);

    boolean deleteHedgeRelation(Long id);

    boolean terminate(Long id);
}
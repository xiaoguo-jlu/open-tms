package com.opentms.hedge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.opentms.hedge.entity.HedgeRelation;
import com.opentms.hedge.mapper.HedgeRelationMapper;
import com.opentms.hedge.service.HedgeRelationService;
import com.opentms.hedge.vo.HedgeRelationVO;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service
public class HedgeRelationServiceImpl extends ServiceImpl<HedgeRelationMapper, HedgeRelation> implements HedgeRelationService {

    @Override
    public Page<HedgeRelationVO> queryPage(String keyword, String status, int pageNum, int pageSize) {
        LambdaQueryWrapper<HedgeRelation> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(keyword)) {
            wrapper.like(HedgeRelation::getHedgeName, keyword)
                   .or()
                   .like(HedgeRelation::getCode, keyword)
                   .or()
                   .like(HedgeRelation::getName, keyword);
        }
        
        if (StringUtils.hasText(status)) {
            wrapper.eq(HedgeRelation::getStatus, status);
        }
        
        wrapper.orderByDesc(HedgeRelation::getCreatedAt);
        
        Page<HedgeRelation> page = page(new Page<>(pageNum, pageSize), wrapper);
        
        return convertToVO(page);
    }

    @Override
    public HedgeRelationVO getHedgeRelationById(Long id) {
        HedgeRelation entity = getById(id);
        return convertToVO(entity);
    }

    @Override
    public HedgeRelationVO getEffectiveness(Long id) {
        HedgeRelation entity = getById(id);
        HedgeRelationVO vo = convertToVO(entity);
        
        Map<String, Object> effectiveness = new HashMap<>();
        effectiveness.put("hedgeRatio", entity.getHedgeRatio());
        effectiveness.put("assessmentDate", LocalDate.now());
        effectiveness.put("assessmentResult", "PROSPECTIVE");
        effectiveness.put("effectiveness", "HIGHLY_EFFECTIVE");
        
        return vo;
    }

    @Override
    public HedgeRelationVO getPnL(Long id) {
        HedgeRelation entity = getById(id);
        HedgeRelationVO vo = convertToVO(entity);
        
        Map<String, Object> pnl = new HashMap<>();
        pnl.put("realizedPnL", 0.00);
        pnl.put("unrealizedPnL", 0.00);
        pnl.put("totalPnL", 0.00);
        pnl.put("calculationDate", LocalDate.now());
        
        return vo;
    }

    @Override
    public boolean saveHedgeRelation(HedgeRelation hedgeRelation) {
        return save(hedgeRelation);
    }

    @Override
    public boolean updateHedgeRelation(HedgeRelation hedgeRelation) {
        return updateById(hedgeRelation);
    }

    @Override
    public boolean deleteHedgeRelation(Long id) {
        return removeById(id);
    }

    @Override
    public boolean terminate(Long id) {
        HedgeRelation entity = new HedgeRelation();
        entity.setId(id);
        entity.setStatus("TERMINATED");
        return updateById(entity);
    }

    private Page<HedgeRelationVO> convertToVO(Page<HedgeRelation> page) {
        Page<HedgeRelationVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(page.getRecords().stream().map(this::convertToVO).toList());
        return voPage;
    }

    private HedgeRelationVO convertToVO(HedgeRelation entity) {
        if (entity == null) return null;
        HedgeRelationVO vo = new HedgeRelationVO();
        vo.setId(entity.getId());
        vo.setCode(entity.getCode());
        vo.setName(entity.getName());
        vo.setHedgeName(entity.getHedgeName());
        vo.setHedgeType(entity.getHedgeType());
        vo.setHedgeInstrumentId(entity.getHedgeInstrumentId());
        vo.setHedgedItemId(entity.getHedgedItemId());
        vo.setHedgeRatio(entity.getHedgeRatio());
        vo.setStartDate(entity.getStartDate());
        vo.setEndDate(entity.getEndDate());
        vo.setStatus(entity.getStatus());
        vo.setCreatedAt(entity.getCreatedAt());
        vo.setCreatedBy(entity.getCreatedBy());
        vo.setUpdatedAt(entity.getUpdatedAt());
        vo.setUpdatedBy(entity.getUpdatedBy());
        return vo;
    }
}
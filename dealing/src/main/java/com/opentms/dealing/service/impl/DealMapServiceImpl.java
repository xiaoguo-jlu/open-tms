package com.opentms.dealing.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.opentms.dealing.entity.DealMap;
import com.opentms.dealing.mapper.DealMapMapper;
import com.opentms.dealing.service.DealMapService;
import com.opentms.dealing.vo.DealMapVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class DealMapServiceImpl extends ServiceImpl<DealMapMapper, DealMap> implements DealMapService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    private static final String DEALMAP_STATUS_ACTIVE = "Active";
    private static final String DEALMAP_STATUS_INACTIVE = "Inactive";

    @Override
    public Page<DealMapVO> queryPage(String dealNumber, String eventType, String eventStatus,
                                     int pageNum, int pageSize) {
        LambdaQueryWrapper<DealMap> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(dealNumber)) {
            wrapper.eq(DealMap::getDealNumber, dealNumber);
        }
        if (StringUtils.hasText(eventType)) {
            wrapper.eq(DealMap::getEventType, eventType);
        }
        if (StringUtils.hasText(eventStatus)) {
            wrapper.eq(DealMap::getEventStatus, eventStatus);
        }

        wrapper.orderByAsc(DealMap::getEventDate).orderByAsc(DealMap::getCreatedAt);

        Page<DealMap> page = page(new Page<>(pageNum, pageSize), wrapper);
        Page<DealMapVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        List<DealMapVO> voList = page.getRecords().stream().map(this::convertToVO).toList();
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public DealMapVO getById(Long id) {
        DealMap dm = super.getById(id);
        return dm != null ? convertToVO(dm) : null;
    }

    @Override
    public List<DealMapVO> listByDealNumber(String dealNumber) {
        LambdaQueryWrapper<DealMap> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DealMap::getDealNumber, dealNumber)
               .orderByAsc(DealMap::getEventDate)
               .orderByAsc(DealMap::getCreatedAt);
        return super.list(wrapper).stream().map(this::convertToVO).toList();
    }

    @Override
    public boolean save(DealMap dealMap) {
        if (dealMap.getCreatedAt() == null) {
            dealMap.setCreatedAt(LocalDateTime.now());
        }
        if (dealMap.getVersion() == null) {
            dealMap.setVersion(1);
        }
        if (!StringUtils.hasText(dealMap.getDeleted())) {
            dealMap.setDeleted("0");
        }
        if (!StringUtils.hasText(dealMap.getIsReversal())) {
            dealMap.setIsReversal("0");
        }
        if (!StringUtils.hasText(dealMap.getEventStatus())) {
            dealMap.setEventStatus(DEALMAP_STATUS_ACTIVE);
        }
        return super.save(dealMap);
    }

    @Override
    public DealMap getOne(LambdaQueryWrapper<DealMap> wrapper) {
        return super.getOne(wrapper);
    }

    @Override
    public String createDealMap(DealMap dealMap) {
        if (!StringUtils.hasText(dealMap.getDealmapNumber())) {
            dealMap.setDealmapNumber(generateDealMapNumber());
        }
        save(dealMap);
        return dealMap.getDealmapNumber();
    }

    @Override
    public int softDeleteByDealNumber(String dealNumber) {
        DealMap update = new DealMap();
        update.setDeleted("1");
        update.setUpdatedAt(LocalDateTime.now());
        update.setUpdatedBy("system");

        LambdaQueryWrapper<DealMap> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DealMap::getDealNumber, dealNumber)
               .eq(DealMap::getDeleted, "0");
        return baseMapper.update(update, wrapper);
    }

    @Override
    public DealMap reverseDealMap(Long id, String operator, String remark) {
        DealMap original = super.getById(id);
        if (original == null) {
            throw new RuntimeException("DealMap not found: " + id);
        }

        // 1. 创建冲销 DealMap
        DealMap reversal = new DealMap();
        BeanUtils.copyProperties(original, reversal);
        reversal.setId(null);
        reversal.setDealmapNumber(generateDealMapNumber());
        reversal.setIsReversal("1");
        reversal.setReversesEventId(original.getId());
        reversal.setReversedByEventId(null);
        reversal.setEventStatus(DEALMAP_STATUS_ACTIVE);
        reversal.setCreatedBy(operator);
        reversal.setCreatedAt(LocalDateTime.now());
        reversal.setVersion(1);
        reversal.setUpdatedAt(null);
        reversal.setDescription(remark != null ? remark : "Reversal of " + original.getDealmapNumber());
        super.save(reversal);

        // 2. 更新原 DealMap 状态为 Inactive
        original.setEventStatus(DEALMAP_STATUS_INACTIVE);
        original.setReversedByEventId(reversal.getId());
        original.setUpdatedBy(operator);
        original.setUpdatedAt(LocalDateTime.now());
        updateById(original);

        return reversal;
    }

    @Override
    public String generateDealMapNumber() {
        String dateStr = LocalDateTime.now().format(DATE_FORMATTER);
        String prefix = "DMP" + dateStr;
        LambdaQueryWrapper<DealMap> wrapper = new LambdaQueryWrapper<>();
        wrapper.likeRight(DealMap::getDealmapNumber, prefix)
               .orderByDesc(DealMap::getDealmapNumber)
               .last("LIMIT 1");
        DealMap last = super.getOne(wrapper);
        int seq = 1;
        if (last != null && last.getDealmapNumber() != null
                && last.getDealmapNumber().length() > prefix.length()) {
            try {
                String lastSeqStr = last.getDealmapNumber().substring(prefix.length());
                seq = Integer.parseInt(lastSeqStr) + 1;
            } catch (NumberFormatException ignored) {
            }
        }
        return prefix + String.format("%04d", seq);
    }

    private DealMapVO convertToVO(DealMap dealMap) {
        DealMapVO vo = new DealMapVO();
        BeanUtils.copyProperties(dealMap, vo);
        return vo;
    }
}

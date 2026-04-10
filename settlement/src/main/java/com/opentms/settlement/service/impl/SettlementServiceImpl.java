package com.opentms.settlement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.opentms.settlement.dto.SettlementDTO;
import com.opentms.settlement.entity.Settlement;
import com.opentms.settlement.mapper.SettlementMapper;
import com.opentms.settlement.service.SettlementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class SettlementServiceImpl implements SettlementService {

    private final SettlementMapper settlementMapper;

    @Override
    public Page<Settlement> queryPage(String keyword, String status, String settlementType, int pageNum, int pageSize) {
        LambdaQueryWrapper<Settlement> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Settlement::getPayeeName, keyword)
                   .or()
                   .like(Settlement::getPayeeAccountNo, keyword);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(Settlement::getStatus, status);
        }
        if (StringUtils.hasText(settlementType)) {
            wrapper.eq(Settlement::getSettlementType, settlementType);
        }
        wrapper.orderByDesc(Settlement::getCreatedAt);
        return settlementMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public Settlement getSettlementById(Long id) {
        return settlementMapper.selectById(id);
    }

    @Override
    public boolean saveSettlement(SettlementDTO dto) {
        Settlement settlement = new Settlement();
        settlement.setSettlementType(dto.getSettlementType());
        settlement.setAccountId(dto.getAccountId());
        settlement.setPayeeId(dto.getPayeeId());
        settlement.setPayeeName(dto.getPayeeName());
        settlement.setPayeeBank(dto.getPayeeBank());
        settlement.setPayeeAccountNo(dto.getPayeeAccountNo());
        settlement.setAmount(dto.getAmount());
        settlement.setCurrency(dto.getCurrency());
        settlement.setPurpose(dto.getPurpose());
        settlement.setExecuteDate(dto.getExecuteDate());
        settlement.setStatus("PENDING");
        return settlementMapper.insert(settlement) > 0;
    }

    @Override
    public boolean updateSettlement(SettlementDTO dto, Long id) {
        Settlement settlement = settlementMapper.selectById(id);
        if (settlement == null) {
            return false;
        }
        if (!"PENDING".equals(settlement.getStatus())) {
            throw new RuntimeException("Only PENDING status can be updated");
        }
        settlement.setSettlementType(dto.getSettlementType());
        settlement.setAccountId(dto.getAccountId());
        settlement.setPayeeId(dto.getPayeeId());
        settlement.setPayeeName(dto.getPayeeName());
        settlement.setPayeeBank(dto.getPayeeBank());
        settlement.setPayeeAccountNo(dto.getPayeeAccountNo());
        settlement.setAmount(dto.getAmount());
        settlement.setCurrency(dto.getCurrency());
        settlement.setPurpose(dto.getPurpose());
        settlement.setExecuteDate(dto.getExecuteDate());
        return settlementMapper.updateById(settlement) > 0;
    }

    @Override
    public boolean submit(Long id) {
        Settlement settlement = settlementMapper.selectById(id);
        if (settlement == null) {
            return false;
        }
        if (!"PENDING".equals(settlement.getStatus())) {
            throw new RuntimeException("Only PENDING status can be submitted");
        }
        settlement.setStatus("APPROVED");
        return settlementMapper.updateById(settlement) > 0;
    }

    @Override
    public boolean approve(Long id) {
        Settlement settlement = settlementMapper.selectById(id);
        if (settlement == null) {
            return false;
        }
        if (!"APPROVED".equals(settlement.getStatus())) {
            throw new RuntimeException("Only APPROVED status can be approved");
        }
        settlement.setStatus("EXECUTING");
        return settlementMapper.updateById(settlement) > 0;
    }

    @Override
    public boolean execute(Long id) {
        Settlement settlement = settlementMapper.selectById(id);
        if (settlement == null) {
            return false;
        }
        if (!"EXECUTING".equals(settlement.getStatus())) {
            throw new RuntimeException("Only EXECUTING status can be executed");
        }
        settlement.setStatus("SUCCESS");
        return settlementMapper.updateById(settlement) > 0;
    }

    @Override
    public String getResult(Long id) {
        Settlement settlement = settlementMapper.selectById(id);
        if (settlement == null) {
            return null;
        }
        return settlement.getStatus();
    }
}
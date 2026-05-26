package com.opentms.basedata.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.opentms.basedata.dto.AcCashflowDTO;
import com.opentms.basedata.entity.AcCashflow;
import com.opentms.basedata.mapper.AcCashflowMapper;
import com.opentms.basedata.service.AcCashflowService;
import com.opentms.basedata.vo.AcCashflowVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class AcCashflowServiceImpl implements AcCashflowService {

    private final AcCashflowMapper acCashflowMapper;

    @Override
    public Page<AcCashflowVO> queryPage(AcCashflowDTO dto, int pageNum, int pageSize) {
        LambdaQueryWrapper<AcCashflow> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(dto.getKeyword())) {
            wrapper.like(AcCashflow::getCashflowNo, dto.getKeyword())
                   .or()
                   .like(AcCashflow::getBankAccount, dto.getKeyword())
                   .or()
                   .like(AcCashflow::getCounterpartyName, dto.getKeyword())
                   .or()
                   .like(AcCashflow::getCounterpartyAccount, dto.getKeyword());
        }

        if (StringUtils.hasText(dto.getStatus())) {
            wrapper.eq(AcCashflow::getStatus, dto.getStatus());
        }

        if (StringUtils.hasText(dto.getBankAccount())) {
            wrapper.eq(AcCashflow::getBankAccount, dto.getBankAccount());
        }

        if (StringUtils.hasText(dto.getDirection())) {
            wrapper.eq(AcCashflow::getDirection, dto.getDirection());
        }

        wrapper.orderByDesc(AcCashflow::getCreatedAt);

        Page<AcCashflow> page = acCashflowMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);

        Page<AcCashflowVO> result = new Page<>();
        result.setCurrent(page.getCurrent());
        result.setSize(page.getSize());
        result.setTotal(page.getTotal());
        result.setRecords(page.getRecords().stream()
                .map(this::convertToVO)
                .toList());

        return result;
    }

    @Override
    public AcCashflowVO getById(Long id) {
        AcCashflow entity = acCashflowMapper.selectById(id);
        return entity != null ? convertToVO(entity) : null;
    }

    @Override
    public AcCashflowVO save(AcCashflowDTO dto) {
        AcCashflow entity = convertToEntity(dto);
        entity.setCashflowNo(generateCashflowNo());
        entity.setStatus("Created");
        entity.setCreatedBy(getCurrentUser());
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedBy(getCurrentUser());
        entity.setUpdatedAt(LocalDateTime.now());

        acCashflowMapper.insert(entity);

        log.info("[新增现金流水] id={}, cashflowNo={}", entity.getId(), entity.getCashflowNo());
        return convertToVO(entity);
    }

    @Override
    public AcCashflowVO updateById(AcCashflowDTO dto) {
        AcCashflow entity = acCashflowMapper.selectById(dto.getId());
        if (entity == null) {
            throw new RuntimeException("现金流水不存在");
        }

        entity.setBusinessUnit(dto.getBusinessUnit());
        entity.setBankAccount(dto.getBankAccount());
        entity.setCounterpartyAccount(dto.getCounterpartyAccount());
        entity.setDirection(dto.getDirection());
        entity.setAmount(dto.getAmount());
        entity.setCurrency(dto.getCurrency());
        entity.setCashflowDate(dto.getCashflowDate());
        entity.setValueDate(dto.getValueDate());
        entity.setSourceType(dto.getSourceType());
        entity.setSourceRef(dto.getSourceRef());
        entity.setSubType(dto.getSubType());
        entity.setBankRef(dto.getBankRef());
        entity.setStatementNo(dto.getStatementNo());
        entity.setCounterpartyName(dto.getCounterpartyName());
        entity.setPurpose(dto.getPurpose());
        entity.setUpdatedBy(getCurrentUser());
        entity.setUpdatedAt(LocalDateTime.now());

        acCashflowMapper.updateById(entity);

        log.info("[更新现金流水] id={}", entity.getId());
        return convertToVO(entity);
    }

    @Override
    public void removeById(Long id) {
        AcCashflow entity = acCashflowMapper.selectById(id);
        if (entity == null) {
            throw new RuntimeException("现金流水不存在");
        }

        entity.setDeleted("1");
        entity.setUpdatedBy(getCurrentUser());
        entity.setUpdatedAt(LocalDateTime.now());
        acCashflowMapper.updateById(entity);

        log.info("[删除现金流水] id={}", id);
    }

    @Override
    public AcCashflowVO confirm(Long id) {
        AcCashflow entity = acCashflowMapper.selectById(id);
        if (entity == null) {
            throw new RuntimeException("现金流水不存在");
        }

        entity.setStatus("Cleared");
        entity.setUpdatedBy(getCurrentUser());
        entity.setUpdatedAt(LocalDateTime.now());
        acCashflowMapper.updateById(entity);

        log.info("[确认现金流水] id={}, status=Cleared", id);
        return convertToVO(entity);
    }

    @Override
    public AcCashflowVO generateFromDeal(Long dealId) {
        log.info("[从交易生成现金流水] dealId={}", dealId);

        AcCashflow entity = new AcCashflow();
        entity.setCashflowNo(generateCashflowNo());
        entity.setBusinessUnit("BU001");
        entity.setBankAccount("ACCT001");
        entity.setDirection("Inflow");
        entity.setAmount(BigDecimal.ZERO);
        entity.setCurrency("CNY");
        entity.setCashflowDate(LocalDate.now());
        entity.setValueDate(LocalDate.now());
        entity.setSourceType("Deal");
        entity.setSourceRef(String.valueOf(dealId));
        entity.setStatus("Created");
        entity.setCreatedBy(getCurrentUser());
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedBy(getCurrentUser());
        entity.setUpdatedAt(LocalDateTime.now());

        acCashflowMapper.insert(entity);

        log.info("[从交易生成现金流水] success id={}, dealId={}", entity.getId(), dealId);
        return convertToVO(entity);
    }

    private String generateCashflowNo() {
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        LambdaQueryWrapper<AcCashflow> wrapper = new LambdaQueryWrapper<>();
        wrapper.likeRight(AcCashflow::getCashflowNo, "CF" + datePart);
        wrapper.orderByDesc(AcCashflow::getId);
        wrapper.last("LIMIT 1");
        AcCashflow last = acCashflowMapper.selectOne(wrapper);

        int seq = 1;
        if (last != null && last.getCashflowNo() != null) {
            String seqStr = last.getCashflowNo().substring(last.getCashflowNo().length() - 4);
            seq = Integer.parseInt(seqStr) + 1;
        }

        return "CF" + datePart + String.format("%04d", seq);
    }

    private AcCashflowVO convertToVO(AcCashflow entity) {
        AcCashflowVO vo = new AcCashflowVO();
        vo.setId(entity.getId());
        vo.setCashflowNo(entity.getCashflowNo());
        vo.setBusinessUnit(entity.getBusinessUnit());
        vo.setBankAccount(entity.getBankAccount());
        vo.setCounterpartyAccount(entity.getCounterpartyAccount());
        vo.setDirection(entity.getDirection());
        vo.setAmount(entity.getAmount());
        vo.setCurrency(entity.getCurrency());
        vo.setCashflowDate(entity.getCashflowDate());
        vo.setValueDate(entity.getValueDate());
        vo.setSourceType(entity.getSourceType());
        vo.setSourceRef(entity.getSourceRef());
        vo.setSubType(entity.getSubType());
        vo.setBankRef(entity.getBankRef());
        vo.setStatementNo(entity.getStatementNo());
        vo.setStatus(entity.getStatus());
        vo.setCounterpartyName(entity.getCounterpartyName());
        vo.setPurpose(entity.getPurpose());
        vo.setCreatedBy(entity.getCreatedBy());
        vo.setCreatedAt(entity.getCreatedAt());
        vo.setUpdatedBy(entity.getUpdatedBy());
        vo.setUpdatedAt(entity.getUpdatedAt());
        vo.setVersion(entity.getVersion());
        return vo;
    }

    private AcCashflow convertToEntity(AcCashflowDTO dto) {
        AcCashflow entity = new AcCashflow();
        entity.setId(dto.getId());
        entity.setBusinessUnit(dto.getBusinessUnit());
        entity.setBankAccount(dto.getBankAccount());
        entity.setCounterpartyAccount(dto.getCounterpartyAccount());
        entity.setDirection(dto.getDirection());
        entity.setAmount(dto.getAmount());
        entity.setCurrency(dto.getCurrency());
        entity.setCashflowDate(dto.getCashflowDate());
        entity.setValueDate(dto.getValueDate());
        entity.setSourceType(dto.getSourceType());
        entity.setSourceRef(dto.getSourceRef());
        entity.setSubType(dto.getSubType());
        entity.setBankRef(dto.getBankRef());
        entity.setStatementNo(dto.getStatementNo());
        entity.setStatus(dto.getStatus());
        entity.setCounterpartyName(dto.getCounterpartyName());
        entity.setPurpose(dto.getPurpose());
        return entity;
    }

    private String getCurrentUser() {
        return "system";
    }
}

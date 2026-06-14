package com.opentms.basedata.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.opentms.basedata.dto.InstrumentDTO;
import com.opentms.basedata.entity.Instrument;
import com.opentms.basedata.mapper.InstrumentMapper;
import com.opentms.basedata.service.InstrumentService;
import com.opentms.basedata.vo.InstrumentVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class InstrumentServiceImpl extends ServiceImpl<InstrumentMapper, Instrument> implements InstrumentService {

    @Override
    public Page<InstrumentVO> queryPage(String keyword, String instrumentType, String status, int pageNum, int pageSize) {
        LambdaQueryWrapper<Instrument> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(keyword)) {
            wrapper.like(Instrument::getInstrumentCode, keyword)
                   .or()
                   .like(Instrument::getInstrumentName, keyword)
                   .or()
                   .like(Instrument::getEnName, keyword);
        }

        if (StringUtils.hasText(instrumentType)) {
            wrapper.eq(Instrument::getInstrumentType, instrumentType);
        }

        if (StringUtils.hasText(status)) {
            wrapper.eq(Instrument::getStatus, status);
        }

        wrapper.orderByDesc(Instrument::getCreatedAt);

        Page<Instrument> page = baseMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);

        Page<InstrumentVO> result = new Page<>();
        result.setCurrent(page.getCurrent());
        result.setSize(page.getSize());
        result.setTotal(page.getTotal());
        result.setRecords(page.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList()));

        return result;
    }

    @Override
    public List<InstrumentVO> listAll() {
        LambdaQueryWrapper<Instrument> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Instrument::getStatus, "1");
        wrapper.orderByAsc(Instrument::getInstrumentCode);
        return baseMapper.selectList(wrapper).stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public InstrumentVO getById(Long id) {
        Instrument entity = baseMapper.selectById(id);
        return entity != null ? convertToVO(entity) : null;
    }

    @Override
    public InstrumentVO save(InstrumentDTO dto) {
        log.info("[新增金融工具] instrumentCode={}", dto.getInstrumentCode());

        if (checkInstrumentCodeExists(dto.getInstrumentCode(), null)) {
            throw new BusinessException("金融工具编码已存在: " + dto.getInstrumentCode());
        }

        Instrument entity = convertToEntity(dto);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setCreatedBy(getCurrentUser());
        entity.setStatus("1");
        entity.setDeleted("0");

        baseMapper.insert(entity);

        log.info("[新增金融工具] success id={}", entity.getId());
        return convertToVO(entity);
    }

    @Override
    public InstrumentVO updateById(InstrumentDTO dto) {
        Instrument entity = baseMapper.selectById(dto.getId());
        if (entity == null) {
            throw new BusinessException("金融工具不存在");
        }

        entity.setInstrumentName(dto.getInstrumentName());
        entity.setEnName(dto.getEnName());
        entity.setInstrumentType(dto.getInstrumentType());
        entity.setInstrumentSubtype(dto.getInstrumentSubtype());
        entity.setUnderlying(dto.getUnderlying());
        entity.setExchange(dto.getExchange());
        entity.setCurrency(dto.getCurrency());
        entity.setFaceValue(dto.getFaceValue());
        entity.setIssueDate(dto.getIssueDate());
        entity.setMaturityDate(dto.getMaturityDate());
        entity.setInterestRate(dto.getInterestRate());
        entity.setCounterpartyId(dto.getCounterpartyId());
        entity.setStatus(dto.getStatus());
        entity.setRemark(dto.getRemark());
        entity.setUpdatedAt(LocalDateTime.now());
        entity.setUpdatedBy(getCurrentUser());
        entity.setVersion(entity.getVersion() + 1);

        baseMapper.updateById(entity);

        log.info("[更新金融工具] success id={}", entity.getId());
        return convertToVO(baseMapper.selectById(dto.getId()));
    }

    @Override
    public void removeById(Long id) {
        log.info("[删除金融工具] id={}", id);

        Instrument entity = baseMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("金融工具不存在");
        }

        entity.setDeleted("1");
        entity.setUpdatedAt(LocalDateTime.now());
        entity.setUpdatedBy(getCurrentUser());
        entity.setVersion(entity.getVersion() + 1);

        baseMapper.updateById(entity);

        log.info("[删除金融工具] success id={}", id);
    }

    @Override
    public void batchDelete(List<Long> ids) {
        log.info("[批量删除金融工具] ids={}", ids);
        for (Long id : ids) {
            removeById(id);
        }
    }

    private InstrumentVO convertToVO(Instrument entity) {
        InstrumentVO vo = new InstrumentVO();
        vo.setId(entity.getId());
        vo.setInstrumentCode(entity.getInstrumentCode());
        vo.setInstrumentName(entity.getInstrumentName());
        vo.setInstrumentType(entity.getInstrumentType());
        vo.setInstrumentSubtype(entity.getInstrumentSubtype());
        vo.setEnName(entity.getEnName());
        vo.setUnderlying(entity.getUnderlying());
        vo.setExchange(entity.getExchange());
        vo.setCurrency(entity.getCurrency());
        vo.setFaceValue(entity.getFaceValue());
        vo.setIssueDate(entity.getIssueDate());
        vo.setMaturityDate(entity.getMaturityDate());
        vo.setInterestRate(entity.getInterestRate());
        vo.setCounterpartyId(entity.getCounterpartyId());
        vo.setStatus(entity.getStatus());
        vo.setRemark(entity.getRemark());
        vo.setCreatedAt(entity.getCreatedAt());
        vo.setUpdatedAt(entity.getUpdatedAt());
        return vo;
    }

    private Instrument convertToEntity(InstrumentDTO dto) {
        Instrument entity = new Instrument();
        entity.setId(dto.getId());
        entity.setInstrumentCode(dto.getInstrumentCode());
        entity.setInstrumentName(dto.getInstrumentName());
        entity.setInstrumentType(dto.getInstrumentType());
        entity.setInstrumentSubtype(dto.getInstrumentSubtype());
        entity.setEnName(dto.getEnName());
        entity.setUnderlying(dto.getUnderlying());
        entity.setExchange(dto.getExchange());
        entity.setCurrency(dto.getCurrency());
        entity.setFaceValue(dto.getFaceValue());
        entity.setIssueDate(dto.getIssueDate());
        entity.setMaturityDate(dto.getMaturityDate());
        entity.setInterestRate(dto.getInterestRate());
        entity.setCounterpartyId(dto.getCounterpartyId());
        entity.setStatus(dto.getStatus());
        entity.setRemark(dto.getRemark());
        return entity;
    }

    private String getCurrentUser() {
        return "system";
    }

    private boolean checkInstrumentCodeExists(String instrumentCode, Long excludeId) {
        if (!StringUtils.hasText(instrumentCode)) {
            return false;
        }
        LambdaQueryWrapper<Instrument> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Instrument::getInstrumentCode, instrumentCode);
        if (excludeId != null) {
            wrapper.ne(Instrument::getId, excludeId);
        }
        return count(wrapper) > 0;
    }

    public static class BusinessException extends RuntimeException {
        public BusinessException(String message) {
            super(message);
        }
    }
}
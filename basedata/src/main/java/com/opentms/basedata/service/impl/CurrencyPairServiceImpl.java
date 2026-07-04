package com.opentms.basedata.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.opentms.basedata.dto.CurrencyPairDTO;
import com.opentms.basedata.entity.CurrencyPair;
import com.opentms.basedata.mapper.CurrencyPairMapper;
import com.opentms.basedata.service.CurrencyPairService;
import com.opentms.basedata.vo.CurrencyPairVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CurrencyPairServiceImpl extends ServiceImpl<CurrencyPairMapper, CurrencyPair> implements CurrencyPairService {

    @Override
    public Page<CurrencyPairVO> queryPage(String keyword, String status, int pageNum, int pageSize) {
        LambdaQueryWrapper<CurrencyPair> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(keyword)) {
            wrapper.like(CurrencyPair::getPairCode, keyword)
                   .or()
                   .like(CurrencyPair::getCurrency1, keyword)
                   .or()
                   .like(CurrencyPair::getCurrency2, keyword);
        }

        if (StringUtils.hasText(status)) {
            wrapper.eq(CurrencyPair::getStatus, status);
        }

        wrapper.orderByDesc(CurrencyPair::getCreatedAt);

        Page<CurrencyPair> page = baseMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);

        Page<CurrencyPairVO> result = new Page<>();
        result.setCurrent(page.getCurrent());
        result.setSize(page.getSize());
        result.setTotal(page.getTotal());
        result.setRecords(page.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList()));

        return result;
    }

    @Override
    public List<CurrencyPairVO> listAll() {
        LambdaQueryWrapper<CurrencyPair> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CurrencyPair::getStatus, "1");
        wrapper.orderByAsc(CurrencyPair::getPairCode);
        return baseMapper.selectList(wrapper).stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public CurrencyPairVO getById(Long id) {
        CurrencyPair entity = baseMapper.selectById(id);
        return entity != null ? convertToVO(entity) : null;
    }

    @Override
    public CurrencyPairVO getByPairCode(String pairCode) {
        LambdaQueryWrapper<CurrencyPair> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CurrencyPair::getPairCode, pairCode);
        CurrencyPair entity = baseMapper.selectOne(wrapper);
        return entity != null ? convertToVO(entity) : null;
    }

    @Override
    public CurrencyPairVO save(CurrencyPairDTO dto) {
        log.info("[新增币种对] pairCode={}", dto.getPairCode());

        if (checkPairCodeExists(dto.getPairCode(), null)) {
            throw new BusinessException("货币对编码已存在: " + dto.getPairCode());
        }

        CurrencyPair entity = convertToEntity(dto);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setCreatedBy(getCurrentUser());
        entity.setStatus("1");

        baseMapper.insert(entity);

        log.info("[新增币种对] success id={}", entity.getId());
        return convertToVO(entity);
    }

    @Override
    public CurrencyPairVO updateById(CurrencyPairDTO dto) {
        CurrencyPair entity = baseMapper.selectById(dto.getId());
        if (entity == null) {
            throw new BusinessException("币种对不存在");
        }

        entity.setCurrency1(dto.getCurrency1());
        entity.setCurrency2(dto.getCurrency2());
        entity.setStrongerCurrency(dto.getStrongerCurrency());
        entity.setBidDecimal(dto.getBidDecimal());
        entity.setAskDecimal(dto.getAskDecimal());
        entity.setStatus(dto.getStatus());
        entity.setRemark(dto.getRemark());
        entity.setUpdatedAt(LocalDateTime.now());
        entity.setUpdatedBy(getCurrentUser());
        entity.setVersion(entity.getVersion() + 1);

        baseMapper.updateById(entity);

        log.info("[更新币种对] success id={}", entity.getId());
        return convertToVO(baseMapper.selectById(dto.getId()));
    }

    @Override
    public void removeById(Long id) {
        log.info("[删除币种对] id={}", id);

        CurrencyPair entity = baseMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("币种对不存在");
        }

        entity.setDeleted("1");
        entity.setUpdatedAt(LocalDateTime.now());
        entity.setUpdatedBy(getCurrentUser());
        entity.setVersion(entity.getVersion() + 1);

        baseMapper.updateById(entity);

        log.info("[删除币种对] success id={}", id);
    }

    private CurrencyPairVO convertToVO(CurrencyPair entity) {
        CurrencyPairVO vo = new CurrencyPairVO();
        vo.setId(entity.getId());
        vo.setPairCode(entity.getPairCode());
        vo.setCurrency1(entity.getCurrency1());
        vo.setCurrency2(entity.getCurrency2());
        vo.setStrongerCurrency(entity.getStrongerCurrency());
        vo.setBidDecimal(entity.getBidDecimal());
        vo.setAskDecimal(entity.getAskDecimal());
        vo.setStatus(entity.getStatus());
        vo.setRemark(entity.getRemark());
        vo.setCreatedAt(entity.getCreatedAt());
        vo.setUpdatedAt(entity.getUpdatedAt());
        return vo;
    }

    private CurrencyPair convertToEntity(CurrencyPairDTO dto) {
        CurrencyPair entity = new CurrencyPair();
        entity.setId(dto.getId());
        entity.setPairCode(dto.getPairCode());
        entity.setCurrency1(dto.getCurrency1());
        entity.setCurrency2(dto.getCurrency2());
        entity.setStrongerCurrency(dto.getStrongerCurrency());
        entity.setBidDecimal(dto.getBidDecimal());
        entity.setAskDecimal(dto.getAskDecimal());
        entity.setStatus(dto.getStatus());
        entity.setRemark(dto.getRemark());
        return entity;
    }

    private String getCurrentUser() {
        return "system";
    }

    private boolean checkPairCodeExists(String pairCode, Long excludeId) {
        if (!StringUtils.hasText(pairCode)) {
            return false;
        }
        LambdaQueryWrapper<CurrencyPair> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CurrencyPair::getPairCode, pairCode);
        if (excludeId != null) {
            wrapper.ne(CurrencyPair::getId, excludeId);
        }
        return count(wrapper) > 0;
    }

    public static class BusinessException extends RuntimeException {
        public BusinessException(String message) {
            super(message);
        }
    }
}
package com.opentms.basedata.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.opentms.basedata.dto.CurrencyDTO;
import com.opentms.basedata.entity.Currency;
import com.opentms.basedata.mapper.CurrencyMapper;
import com.opentms.basedata.service.CurrencyService;
import com.opentms.basedata.vo.CurrencyVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CurrencyServiceImpl extends ServiceImpl<CurrencyMapper, Currency> implements CurrencyService {

    @Override
    public Page<CurrencyVO> queryPage(String keyword, String status, int pageNum, int pageSize) {
        LambdaQueryWrapper<Currency> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(status)) {
            wrapper.eq(Currency::getStatus, status);
        }

        wrapper.orderByDesc(Currency::getCreatedAt);

        Page<Currency> page = baseMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);

        Page<CurrencyVO> result = new Page<>();
        result.setCurrent(page.getCurrent());
        result.setSize(page.getSize());
        result.setTotal(page.getTotal());
        result.setRecords(page.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList()));

        return result;
    }

    @Override
    public List<CurrencyVO> listAll() {
        LambdaQueryWrapper<Currency> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Currency::getStatus, "1");
        wrapper.orderByAsc(Currency::getCode);
        return baseMapper.selectList(wrapper).stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public CurrencyVO getById(Long id) {
        Currency entity = baseMapper.selectById(id);
        return entity != null ? convertToVO(entity) : null;
    }

    @Override
    public CurrencyVO getByCode(String code) {
        LambdaQueryWrapper<Currency> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Currency::getCode, code);
        Currency entity = baseMapper.selectOne(wrapper);
        return entity != null ? convertToVO(entity) : null;
    }

    @Override
    public CurrencyVO save(CurrencyDTO dto) {
        log.info("[新增币种] code={}", dto.getCode());

        if (checkCodeExists(dto.getCode(), null)) {
            throw new BusinessException("币种代码已存在: " + dto.getCode());
        }

        Currency entity = convertToEntity(dto);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setCreatedBy(getCurrentUser());
        entity.setStatus("1");

        baseMapper.insert(entity);

        log.info("[新增币种] success id={}", entity.getId());
        return convertToVO(entity);
    }

    @Override
    public CurrencyVO updateById(CurrencyDTO dto) {
        Currency entity = baseMapper.selectById(dto.getId());
        if (entity == null) {
            throw new BusinessException("币种不存在");
        }

        // Use direct JDBC to update
        try {
            java.sql.Connection conn = java.sql.DriverManager.getConnection(
                "jdbc:postgresql://localhost:5432/opentms", "opentms", "opentms123");
            java.sql.PreparedStatement ps = conn.prepareStatement(
                "UPDATE tms_currency_t SET name=?, symbol=?, decimal_places=?, status=?, remark=?, updated_by=?, updated_at=? WHERE id=?");
            ps.setString(1, dto.getName());
            ps.setString(2, dto.getSymbol());
            ps.setObject(3, dto.getDecimalPlaces());
            ps.setString(4, dto.getStatus());
            ps.setString(5, dto.getRemark());
            ps.setString(6, getCurrentUser());
            ps.setObject(7, java.time.LocalDateTime.now());
            ps.setLong(8, dto.getId());
            int rows = ps.executeUpdate();
            ps.close();
            conn.close();
            if (rows == 0) {
                throw new BusinessException("更新失败");
            }
        } catch (java.sql.SQLException e) {
            throw new RuntimeException("数据库更新失败: " + e.getMessage());
        }

        // Re-fetch the updated entity
        return convertToVO(baseMapper.selectById(dto.getId()));
    }

    @Override
    public void removeById(Long id) {
        log.info("[删除币种] id={}", id);

        Currency entity = baseMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("币种不存在");
        }

        try {
            java.sql.Connection conn = java.sql.DriverManager.getConnection(
                "jdbc:postgresql://localhost:5432/opentms", "opentms", "opentms123");
            java.sql.PreparedStatement ps = conn.prepareStatement(
                "UPDATE tms_currency_t SET deleted='1', updated_by=?, updated_at=? WHERE id=?");
            ps.setString(1, getCurrentUser());
            ps.setObject(2, java.time.LocalDateTime.now());
            ps.setLong(3, id);
            int rows = ps.executeUpdate();
            ps.close();
            conn.close();
            if (rows == 0) {
                throw new BusinessException("删除失败");
            }
        } catch (java.sql.SQLException e) {
            throw new RuntimeException("数据库删除失败: " + e.getMessage());
        }

        log.info("[删除币种] success id={}", id);
    }

    private CurrencyVO convertToVO(Currency entity) {
        CurrencyVO vo = new CurrencyVO();
        vo.setId(entity.getId());
        vo.setCode(entity.getCode());
        vo.setName(entity.getName());
        vo.setSymbol(entity.getSymbol());
        vo.setDecimalPlaces(entity.getDecimalPlaces());
        vo.setStatus(entity.getStatus());
        vo.setCreatedAt(entity.getCreatedAt());
        vo.setUpdatedAt(entity.getUpdatedAt());
        return vo;
    }

    private Currency convertToEntity(CurrencyDTO dto) {
        Currency entity = new Currency();
        entity.setId(dto.getId());
        entity.setCode(dto.getCode());
        entity.setName(dto.getName());
        entity.setSymbol(dto.getSymbol());
        entity.setDecimalPlaces(dto.getDecimalPlaces());
        entity.setStatus(dto.getStatus());
        entity.setRemark(dto.getRemark());
        return entity;
    }

    private String getCurrentUser() {
        return "system";
    }

    private boolean checkCodeExists(String code, Long excludeId) {
        if (!StringUtils.hasText(code)) {
            return false;
        }
        LambdaQueryWrapper<Currency> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Currency::getCode, code);
        if (excludeId != null) {
            wrapper.ne(Currency::getId, excludeId);
        }
        return count(wrapper) > 0;
    }

    public static class BusinessException extends RuntimeException {
        public BusinessException(String message) {
            super(message);
        }
    }
}
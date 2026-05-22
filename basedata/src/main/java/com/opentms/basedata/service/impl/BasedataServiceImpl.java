package com.opentms.basedata.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.opentms.basedata.dto.BasedataDTO;
import com.opentms.basedata.entity.BasedataEntity;
import com.opentms.basedata.mapper.BasedataMapper;
import com.opentms.basedata.service.BasedataService;
import com.opentms.basedata.vo.BasedataVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public abstract class BasedataServiceImpl<
        M extends BasedataMapper<T>,
        T extends BasedataEntity,
        D extends BasedataDTO,
        V extends BasedataVO> extends ServiceImpl<M, T> implements BasedataService<T, D, V> {

    protected abstract V convertToVO(T entity);

    protected abstract T convertToEntity(D dto);

    protected String getEntityName() {
        return this.getClass().getSimpleName().replace("ServiceImpl", "");
    }

    @Override
    public Page<V> queryPage(D dto, int pageNum, int pageSize) {
        LambdaQueryWrapper<T> wrapper = buildQueryWrapper(dto);
        wrapper.orderByDesc(T::getCreatedAt);

        Page<T> page = baseMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);

        Page<V> result = new Page<>();
        result.setCurrent(page.getCurrent());
        result.setSize(page.getSize());
        result.setTotal(page.getTotal());
        result.setRecords(page.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList()));

        return result;
    }

    @Override
    public List<V> listAll() {
        LambdaQueryWrapper<T> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(T::getStatus, "1");
        return baseMapper.selectList(wrapper).stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public V getById(Long id) {
        T entity = super.getById(id);
        return entity != null ? convertToVO(entity) : null;
    }

    @Override
    public V getByCode(String code) {
        LambdaQueryWrapper<T> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(T::getCode, code);
        T entity = baseMapper.selectOne(wrapper);
        return entity != null ? convertToVO(entity) : null;
    }

    @Override
    public V save(D dto) {
        log.info("[新增{}] code={}", getEntityName(), dto.getCode());

        V exist = getByCode(dto.getCode());
        if (exist != null) {
            throw new BusinessException(getEntityName() + "代码已存在: " + dto.getCode());
        }

        T entity = convertToEntity(dto);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setCreatedBy(getCurrentUser());
        entity.setStatus("1");

        baseMapper.insert(entity);

        log.info("[新增{}] success id={}", getEntityName(), entity.getId());
        return convertToVO(entity);
    }

    @Override
    public V updateById(D dto) {
        log.info("[更新{}] id={}", getEntityName(), dto.getId());

        T entity = super.getById(dto.getId());
        if (entity == null) {
            throw new BusinessException(getEntityName() + "不存在");
        }

        updateEntityFromDTO(entity, dto);
        entity.setUpdatedAt(LocalDateTime.now());
        entity.setUpdatedBy(getCurrentUser());

        baseMapper.updateById(entity);

        log.info("[更新{}] success id={}", getEntityName(), entity.getId());
        return convertToVO(entity);
    }

    @Override
    public void removeById(Long id) {
        log.info("[删除{}] id={}", getEntityName(), id);

        T entity = super.getById(id);
        if (entity == null) {
            throw new BusinessException(getEntityName() + "不存在");
        }

        entity.setDeleted("1");
        entity.setUpdatedAt(LocalDateTime.now());
        baseMapper.updateById(entity);

        log.info("[删除{}] success id={}", getEntityName(), id);
    }

    protected LambdaQueryWrapper<T> buildQueryWrapper(D dto) {
        LambdaQueryWrapper<T> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(dto.getKeyword())) {
            wrapper.like(T::getCode, dto.getKeyword())
                   .or()
                   .like(T::getName, dto.getKeyword());
        }

        if (StringUtils.hasText(dto.getStatus())) {
            wrapper.eq(T::getStatus, dto.getStatus());
        }

        return wrapper;
    }

    protected void updateEntityFromDTO(T entity, D dto) {
        entity.setName(dto.getName());
        entity.setStatus(dto.getStatus());
        entity.setRemark(dto.getRemark());
    }

    protected String getCurrentUser() {
        return "system";
    }

    public static class BusinessException extends RuntimeException {
        public BusinessException(String message) {
            super(message);
        }
    }
}
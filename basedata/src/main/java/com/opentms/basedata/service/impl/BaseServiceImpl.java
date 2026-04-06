package com.opentms.basedata.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.opentms.basedata.dto.BaseDTO;
import com.opentms.basedata.vo.BaseVO;
import com.opentms.common.model.BaseCodeEntity;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseServiceImpl<M extends com.baomidou.mybatisplus.core.mapper.BaseMapper<T>, T extends BaseCodeEntity, D extends BaseDTO, V extends BaseVO> 
        extends ServiceImpl<M, T> implements com.opentms.basedata.service.BaseService<T, D, V> {

    @Override
    public IPage<V> queryPage(D dto, int pageNum, int pageSize) {
        Page<T> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<T> wrapper = buildQueryWrapper(dto);
        IPage<T> result = this.page(page, wrapper);
        return convertPage(result);
    }

    @Override
    public V getById(Long id) {
        T entity = super.getById(id);
        return entity == null ? null : convertToVO(entity);
    }

    @Override
    public boolean save(D dto) {
        T entity = convertToEntity(dto);
        return super.save(entity);
    }

    @Override
    public boolean update(D dto) {
        T entity = convertToEntity(dto);
        return super.updateById(entity);
    }

    @Override
    public boolean delete(Long id) {
        return super.removeById(id);
    }

    @Override
    public boolean batchDelete(List<Long> ids) {
        return super.removeByIds(ids);
    }

    @Override
    public List<V> listAll() {
        List<T> list = super.list();
        return convertList(list);
    }

    protected LambdaQueryWrapper<T> buildQueryWrapper(D dto) {
        LambdaQueryWrapper<T> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(T::getCreatedAt);
        return wrapper;
    }

    protected IPage<V> convertPage(IPage<T> page) {
        List<V> records = convertList(page.getRecords());
        Page<V> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(records);
        return voPage;
    }

    protected List<V> convertList(List<T> list) {
        List<V> result = new ArrayList<>();
        if (list != null) {
            for (T entity : list) {
                result.add(convertToVO(entity));
            }
        }
        return result;
    }

    protected V convertToVO(T entity) {
        return null;
    }

    protected T convertToEntity(D dto) {
        return null;
    }
}

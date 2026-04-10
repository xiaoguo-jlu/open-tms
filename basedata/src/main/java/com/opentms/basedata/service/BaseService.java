package com.opentms.basedata.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.opentms.basedata.dto.BaseDTO;
import com.opentms.basedata.vo.BaseVO;

import java.util.List;

public interface BaseService<T extends com.opentms.common.model.BaseCodeEntity, D extends BaseDTO, V extends BaseVO> extends IService<T> {

    IPage<V> queryPage(D dto, int pageNum, int pageSize);

    V getById(Long id);

    boolean save(D dto);

    boolean update(D dto);

    boolean delete(Long id);

    boolean batchDelete(List<Long> ids);

    List<V> listAll();
}

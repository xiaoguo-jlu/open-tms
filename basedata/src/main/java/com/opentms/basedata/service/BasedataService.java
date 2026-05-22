package com.opentms.basedata.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.opentms.basedata.dto.BasedataDTO;
import com.opentms.basedata.entity.BasedataEntity;
import com.opentms.basedata.vo.BasedataVO;

import java.util.List;

public interface BasedataService<
        T extends BasedataEntity,
        D extends BasedataDTO,
        V extends BasedataVO> extends IService<T> {

    IPage<V> queryPage(D dto, int pageNum, int pageSize);

    List<V> listAll();

    V getById(Long id);

    V getByCode(String code);

    V save(D dto);

    V updateById(D dto);

    void removeById(Long id);
}
package com.opentms.basedata.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.opentms.basedata.dto.InstrumentDTO;
import com.opentms.basedata.vo.InstrumentVO;

import java.util.List;

public interface InstrumentService {

    Page<InstrumentVO> queryPage(String keyword, String instrumentType, String status, int pageNum, int pageSize);

    List<InstrumentVO> listAll();

    InstrumentVO getById(Long id);

    InstrumentVO save(InstrumentDTO dto);

    InstrumentVO updateById(InstrumentDTO dto);

    void removeById(Long id);

    void batchDelete(List<Long> ids);
}
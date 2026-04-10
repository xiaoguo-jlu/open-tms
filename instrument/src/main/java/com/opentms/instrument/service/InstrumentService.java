package com.opentms.instrument.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.opentms.instrument.entity.Instrument;

public interface InstrumentService extends IService<Instrument> {

    Page<Instrument> queryPage(String keyword, String instrumentType, String status, int pageNum, int pageSize);

    boolean saveInstrument(Instrument instrument);

    boolean updateInstrument(Instrument instrument);

    boolean deleteInstrument(Long id);

    Instrument getInstrumentById(Long id);

    boolean batchDelete(java.util.List<Long> ids);
}
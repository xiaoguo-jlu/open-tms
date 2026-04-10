package com.opentms.instrument.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.opentms.instrument.entity.Instrument;
import com.opentms.instrument.mapper.InstrumentMapper;
import com.opentms.instrument.service.InstrumentService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class InstrumentServiceImpl extends ServiceImpl<InstrumentMapper, Instrument> implements InstrumentService {

    @Override
    public Page<Instrument> queryPage(String keyword, String instrumentType, String status, int pageNum, int pageSize) {
        LambdaQueryWrapper<Instrument> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(keyword)) {
            wrapper.like(Instrument::getInstrumentCode, keyword)
                   .or()
                   .like(Instrument::getInstrumentName, keyword);
        }

        if (StringUtils.hasText(instrumentType)) {
            wrapper.eq(Instrument::getInstrumentType, instrumentType);
        }

        if (StringUtils.hasText(status)) {
            wrapper.eq(Instrument::getStatus, status);
        }

        wrapper.orderByDesc(Instrument::getCreatedAt);

        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public boolean saveInstrument(Instrument instrument) {
        return save(instrument);
    }

    @Override
    public boolean updateInstrument(Instrument instrument) {
        return updateById(instrument);
    }

    @Override
    public boolean deleteInstrument(Long id) {
        return removeById(id);
    }

    @Override
    public Instrument getInstrumentById(Long id) {
        return getById(id);
    }

    @Override
    public boolean batchDelete(List<Long> ids) {
        return removeByIds(ids);
    }
}
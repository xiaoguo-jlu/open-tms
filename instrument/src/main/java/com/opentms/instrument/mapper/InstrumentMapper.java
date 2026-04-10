package com.opentms.instrument.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.opentms.instrument.entity.Instrument;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface InstrumentMapper extends BaseMapper<Instrument> {
}
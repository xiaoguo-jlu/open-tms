package com.opentms.limit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.opentms.limit.entity.Limit;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LimitMapper extends BaseMapper<Limit> {
}
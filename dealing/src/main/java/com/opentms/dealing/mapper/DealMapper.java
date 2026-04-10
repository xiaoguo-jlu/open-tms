package com.opentms.dealing.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.opentms.dealing.entity.Deal;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DealMapper extends BaseMapper<Deal> {
}
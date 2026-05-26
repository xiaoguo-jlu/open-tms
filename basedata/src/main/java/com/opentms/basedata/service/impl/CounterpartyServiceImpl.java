package com.opentms.basedata.service.impl;

import com.opentms.basedata.dto.CounterpartyDTO;
import com.opentms.basedata.entity.Counterparty;
import com.opentms.basedata.mapper.CounterpartyMapper;
import com.opentms.basedata.service.CounterpartyService;
import com.opentms.basedata.vo.CounterpartyVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CounterpartyServiceImpl extends BasedataServiceImpl<CounterpartyMapper, Counterparty, CounterpartyDTO, CounterpartyVO> implements CounterpartyService {

    @Override
    protected CounterpartyVO convertToVO(Counterparty entity) {
        CounterpartyVO vo = new CounterpartyVO();
        vo.setId(entity.getId());
        vo.setCode(entity.getCode());
        vo.setName(entity.getName());
        vo.setEnName(entity.getEnName());
        vo.setCounterpartyType(entity.getCounterpartyType());
        vo.setCountryCode(entity.getCountryCode());
        vo.setSwiftCode(entity.getSwiftCode());
        vo.setStatus(entity.getStatus());
        vo.setRemark(entity.getRemark());
        vo.setCreatedAt(entity.getCreatedAt());
        vo.setUpdatedAt(entity.getUpdatedAt());
        return vo;
    }

    @Override
    protected Counterparty convertToEntity(CounterpartyDTO dto) {
        Counterparty entity = new Counterparty();
        entity.setId(dto.getId());
        entity.setCode(dto.getCode());
        entity.setName(dto.getName());
        entity.setEnName(dto.getEnName());
        entity.setCounterpartyType(dto.getCounterpartyType());
        entity.setCountryCode(dto.getCountryCode());
        entity.setSwiftCode(dto.getSwiftCode());
        entity.setStatus(dto.getStatus());
        entity.setRemark(dto.getRemark());
        return entity;
    }

    @Override
    protected void updateEntityFromDTO(Counterparty entity, CounterpartyDTO dto) {
        entity.setName(dto.getName());
        entity.setEnName(dto.getEnName());
        entity.setCounterpartyType(dto.getCounterpartyType());
        entity.setCountryCode(dto.getCountryCode());
        entity.setSwiftCode(dto.getSwiftCode());
        entity.setStatus(dto.getStatus());
        entity.setRemark(dto.getRemark());
    }

    @Override
    protected String getEntityName() {
        return "交易对手";
    }
}

package com.opentms.basedata.service.impl;
import com.opentms.basedata.dto.CountryDTO;
import com.opentms.basedata.entity.Country;
import com.opentms.basedata.mapper.CountryMapper;
import com.opentms.basedata.service.CountryService;
import com.opentms.basedata.vo.CountryVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CountryServiceImpl extends BasedataServiceImpl<CountryMapper, Country, CountryDTO, CountryVO> implements CountryService {
    @Override
    protected CountryVO convertToVO(Country entity) {
        CountryVO vo = new CountryVO();
        vo.setId(entity.getId()); vo.setCode(entity.getCode()); vo.setName(entity.getName());
        vo.setEnName(entity.getEnName()); vo.setTimezone(entity.getTimezone());
        vo.setCountryNo(entity.getCountryNo());
        vo.setStatus(entity.getStatus()); vo.setRemark(entity.getRemark());
        vo.setCreatedAt(entity.getCreatedAt()); vo.setUpdatedAt(entity.getUpdatedAt());
        return vo;
    }
    @Override
    protected Country convertToEntity(CountryDTO dto) {
        Country entity = new Country();
        entity.setId(dto.getId()); entity.setCode(dto.getCode()); entity.setName(dto.getName());
        entity.setEnName(dto.getEnName()); entity.setTimezone(dto.getTimezone());
        entity.setCountryNo(dto.getCountryNo());
        entity.setStatus(dto.getStatus()); entity.setRemark(dto.getRemark());
        return entity;
    }
    @Override
    protected void updateEntityFromDTO(Country entity, CountryDTO dto) {
        entity.setName(dto.getName()); entity.setEnName(dto.getEnName());
        entity.setTimezone(dto.getTimezone()); entity.setCountryNo(dto.getCountryNo());
        entity.setStatus(dto.getStatus()); entity.setRemark(dto.getRemark());
    }
    @Override
    protected String getEntityName() { return "国家"; }
}

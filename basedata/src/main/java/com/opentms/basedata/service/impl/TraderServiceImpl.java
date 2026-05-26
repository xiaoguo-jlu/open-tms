package com.opentms.basedata.service.impl;

import com.opentms.basedata.dto.TraderDTO;
import com.opentms.basedata.entity.Trader;
import com.opentms.basedata.mapper.TraderMapper;
import com.opentms.basedata.service.TraderService;
import com.opentms.basedata.vo.TraderVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TraderServiceImpl extends BasedataServiceImpl<TraderMapper, Trader, TraderDTO, TraderVO> implements TraderService {

    @Override
    protected TraderVO convertToVO(Trader entity) {
        TraderVO vo = new TraderVO();
        vo.setId(entity.getId());
        vo.setCode(entity.getCode());
        vo.setName(entity.getName());
        vo.setEnName(entity.getEnName());
        vo.setDepartment(entity.getDepartment());
        vo.setPhone(entity.getPhone());
        vo.setEmail(entity.getEmail());
        vo.setStatus(entity.getStatus());
        vo.setRemark(entity.getRemark());
        vo.setCreatedAt(entity.getCreatedAt());
        vo.setUpdatedAt(entity.getUpdatedAt());
        return vo;
    }

    @Override
    protected Trader convertToEntity(TraderDTO dto) {
        Trader entity = new Trader();
        entity.setId(dto.getId());
        entity.setCode(dto.getCode());
        entity.setName(dto.getName());
        entity.setEnName(dto.getEnName());
        entity.setDepartment(dto.getDepartment());
        entity.setPhone(dto.getPhone());
        entity.setEmail(dto.getEmail());
        entity.setStatus(dto.getStatus());
        entity.setRemark(dto.getRemark());
        return entity;
    }

    @Override
    protected void updateEntityFromDTO(Trader entity, TraderDTO dto) {
        entity.setName(dto.getName());
        entity.setEnName(dto.getEnName());
        entity.setDepartment(dto.getDepartment());
        entity.setPhone(dto.getPhone());
        entity.setEmail(dto.getEmail());
        entity.setStatus(dto.getStatus());
        entity.setRemark(dto.getRemark());
    }

    @Override
    protected String getEntityName() {
        return "交易员";
    }
}

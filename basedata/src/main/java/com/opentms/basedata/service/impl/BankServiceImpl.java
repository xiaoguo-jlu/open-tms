package com.opentms.basedata.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.opentms.basedata.dto.BankDTO;
import com.opentms.basedata.entity.Bank;
import com.opentms.basedata.mapper.BankMapper;
import com.opentms.basedata.service.BankService;
import com.opentms.basedata.vo.BankVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class BankServiceImpl extends ServiceImpl<BankMapper, Bank> implements BankService {

    @Override
    public IPage<BankVO> queryPage(String keyword, String countryCode, String bankType, String status, int pageNo, int pageSize) {
        LambdaQueryWrapper<Bank> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Bank::getCode, keyword)
                   .or()
                   .like(Bank::getName, keyword);
        }
        
        if (StringUtils.hasText(countryCode)) {
            wrapper.eq(Bank::getCountryCode, countryCode);
        }
        
        if (StringUtils.hasText(bankType)) {
            wrapper.eq(Bank::getBankType, bankType);
        }
        
        if (StringUtils.hasText(status)) {
            wrapper.eq(Bank::getStatus, status);
        }
        
        wrapper.orderByDesc(Bank::getCreatedAt);
        
        Page<Bank> page = new Page<>(pageNo, pageSize);
        IPage<Bank> result = this.page(page, wrapper);
        
        return result.convert(this::convertToVO);
    }

    @Override
    public BankVO getById(Long id) {
        Bank entity = super.getById(id);
        return entity == null ? null : convertToVO(entity);
    }

    @Override
    public boolean save(BankDTO dto) {
        Bank entity = new Bank();
        BeanUtils.copyProperties(dto, entity);
        return this.save(entity);
    }

    @Override
    public boolean update(BankDTO dto) {
        Bank entity = new Bank();
        BeanUtils.copyProperties(dto, entity);
        return this.updateById(entity);
    }

    @Override
    public boolean delete(Long id) {
        return this.removeById(id);
    }

    @Override
    public boolean batchDelete(List<Long> ids) {
        return this.removeByIds(ids);
    }

    @Override
    public List<BankVO> listAll() {
        List<Bank> list = this.list();
        List<BankVO> result = new ArrayList<>();
        for (Bank entity : list) {
            result.add(convertToVO(entity));
        }
        return result;
    }

    private BankVO convertToVO(Bank entity) {
        BankVO vo = new BankVO();
        vo.setId(entity.getId());
        vo.setCode(entity.getCode());
        vo.setName(entity.getName());
        vo.setEnName(entity.getEnName());
        vo.setSwiftCode(entity.getSwiftCode());
        vo.setBankNo(entity.getBankNo());
        vo.setCountryCode(entity.getCountryCode());
        vo.setBankType(entity.getBankType());
        vo.setStatus(entity.getStatus());
        vo.setCreatedBy(entity.getCreatedBy());
        vo.setCreatedAt(entity.getCreatedAt());
        vo.setUpdatedBy(entity.getUpdatedBy());
        vo.setUpdatedAt(entity.getUpdatedAt());
        return vo;
    }
}

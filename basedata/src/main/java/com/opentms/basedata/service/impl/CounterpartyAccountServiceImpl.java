package com.opentms.basedata.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.opentms.basedata.dto.CounterpartyAccountDTO;
import com.opentms.basedata.entity.CounterpartyAccount;
import com.opentms.basedata.entity.Bank;
import com.opentms.basedata.entity.Counterparty;
import com.opentms.basedata.mapper.BankMapper;
import com.opentms.basedata.mapper.CounterpartyMapper;
import com.opentms.basedata.mapper.CounterpartyAccountMapper;
import com.opentms.basedata.service.BankService;
import com.opentms.basedata.service.CounterpartyAccountService;
import com.opentms.basedata.service.CounterpartyService;
import com.opentms.basedata.vo.CounterpartyAccountVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CounterpartyAccountServiceImpl extends ServiceImpl<CounterpartyAccountMapper, CounterpartyAccount> implements CounterpartyAccountService {

    private final CounterpartyMapper counterpartyMapper;
    private final BankMapper bankMapper;

    public CounterpartyAccountServiceImpl(CounterpartyMapper counterpartyMapper, BankMapper bankMapper) {
        this.counterpartyMapper = counterpartyMapper;
        this.bankMapper = bankMapper;
    }

    @Override
    public IPage<CounterpartyAccountVO> queryPage(Long counterpartyId, String currency, String accountType, String status, int pageNo, int pageSize) {
        LambdaQueryWrapper<CounterpartyAccount> wrapper = new LambdaQueryWrapper<>();
        
        if (counterpartyId != null) {
            wrapper.eq(CounterpartyAccount::getCounterpartyId, counterpartyId);
        }
        
        if (currency != null && !currency.isEmpty()) {
            wrapper.eq(CounterpartyAccount::getCurrency, currency);
        }
        
        if (accountType != null && !accountType.isEmpty()) {
            wrapper.eq(CounterpartyAccount::getAccountType, accountType);
        }
        
        if (status != null && !status.isEmpty()) {
            wrapper.eq(CounterpartyAccount::getStatus, status);
        }
        
        wrapper.orderByDesc(CounterpartyAccount::getCreatedAt);
        
        Page<CounterpartyAccount> page = new Page<>(pageNo, pageSize);
        IPage<CounterpartyAccount> result = this.page(page, wrapper);
        
        return result.convert(this::convertToVO);
    }

    @Override
    public CounterpartyAccountVO getById(Long id) {
        CounterpartyAccount entity = super.getById(id);
        return entity == null ? null : convertToVO(entity);
    }

    @Override
    public boolean save(CounterpartyAccountDTO dto) {
        CounterpartyAccount entity = new CounterpartyAccount();
        BeanUtils.copyProperties(dto, entity);
        return this.save(entity);
    }

    @Override
    public boolean update(CounterpartyAccountDTO dto) {
        CounterpartyAccount entity = new CounterpartyAccount();
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
    public List<CounterpartyAccountVO> listAll() {
        List<CounterpartyAccount> list = this.list();
        List<CounterpartyAccountVO> result = new ArrayList<>();
        for (CounterpartyAccount entity : list) {
            result.add(convertToVO(entity));
        }
        return result;
    }

    private CounterpartyAccountVO convertToVO(CounterpartyAccount entity) {
        CounterpartyAccountVO vo = new CounterpartyAccountVO();
        vo.setId(entity.getId());
        vo.setCode(entity.getCode());
        vo.setName(entity.getName());
        vo.setCounterpartyId(entity.getCounterpartyId());
        vo.setBankId(entity.getBankId());
        vo.setAccountName(entity.getAccountName());
        vo.setAccountNo(entity.getAccountNo());
        vo.setCurrency(entity.getCurrency());
        vo.setAccountType(entity.getAccountType());
        vo.setStatus(entity.getStatus());
        vo.setCreatedBy(entity.getCreatedBy());
        vo.setCreatedAt(entity.getCreatedAt());
        vo.setUpdatedBy(entity.getUpdatedBy());
        vo.setUpdatedAt(entity.getUpdatedAt());
        
        if (entity.getCounterpartyId() != null) {
            Counterparty cp = counterpartyMapper.selectById(entity.getCounterpartyId());
            if (cp != null) {
                vo.setCounterpartyName(cp.getName());
            }
        }
        if (entity.getBankId() != null) {
            Bank bank = bankMapper.selectById(entity.getBankId());
            if (bank != null) {
                vo.setBankName(bank.getName());
            }
        }
        return vo;
    }
}

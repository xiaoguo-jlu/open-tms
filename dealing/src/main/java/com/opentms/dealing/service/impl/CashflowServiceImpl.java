package com.opentms.dealing.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.opentms.dealing.entity.Cashflow;
import com.opentms.dealing.mapper.CashflowMapper;
import com.opentms.dealing.service.CashflowService;
import com.opentms.dealing.vo.CashflowVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class CashflowServiceImpl extends ServiceImpl<CashflowMapper, Cashflow> implements CashflowService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    private static final String CFLOW_STATUS_CREATED = "Created";

    @Override
    public String createCashflow(Cashflow cashflow) {
        if (!StringUtils.hasText(cashflow.getCflowNumber())) {
            cashflow.setCflowNumber(generateCflowNumber());
        }
        if (!StringUtils.hasText(cashflow.getStatus())) {
            cashflow.setStatus(CFLOW_STATUS_CREATED);
        }
        if (cashflow.getCreatedAt() == null) {
            cashflow.setCreatedAt(LocalDateTime.now());
        }
        if (cashflow.getVersion() == null) {
            cashflow.setVersion(1);
        }
        if (!StringUtils.hasText(cashflow.getDeleted())) {
            cashflow.setDeleted("0");
        }
        super.save(cashflow);
        return cashflow.getCflowNumber();
    }

    @Override
    public boolean save(Cashflow cashflow) {
        if (cashflow.getCreatedAt() == null) {
            cashflow.setCreatedAt(LocalDateTime.now());
        }
        if (cashflow.getVersion() == null) {
            cashflow.setVersion(1);
        }
        if (!StringUtils.hasText(cashflow.getDeleted())) {
            cashflow.setDeleted("0");
        }
        if (!StringUtils.hasText(cashflow.getStatus())) {
            cashflow.setStatus(CFLOW_STATUS_CREATED);
        }
        return super.save(cashflow);
    }

    @Override
    public int softDeleteByDealMapNumber(String dealmapNumber) {
        Cashflow update = new Cashflow();
        update.setDeleted("1");
        update.setUpdatedAt(LocalDateTime.now());
        update.setUpdatedBy("system");

        LambdaQueryWrapper<Cashflow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Cashflow::getDealmapNumber, dealmapNumber)
               .eq(Cashflow::getDeleted, "0");
        return baseMapper.update(update, wrapper);
    }

    @Override
    public int updateDealMapNumber(String oldDealMapNumber, String newDealMapNumber) {
        Cashflow update = new Cashflow();
        update.setDealmapNumber(newDealMapNumber);
        update.setUpdatedAt(LocalDateTime.now());
        update.setUpdatedBy("system");

        LambdaQueryWrapper<Cashflow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Cashflow::getDealmapNumber, oldDealMapNumber)
               .eq(Cashflow::getDeleted, "0");
        return baseMapper.update(update, wrapper);
    }

    @Override
    public List<CashflowVO> listByDealMapNumber(String dealmapNumber) {
        LambdaQueryWrapper<Cashflow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Cashflow::getDealmapNumber, dealmapNumber)
               .orderByDesc(Cashflow::getCreatedAt);
        return list(wrapper).stream().map(this::convertToVO).toList();
    }

    @Override
    public List<CashflowVO> listByDealNumber(String dealNumber) {
        LambdaQueryWrapper<Cashflow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Cashflow::getDealNumber, dealNumber)
               .orderByDesc(Cashflow::getCreatedAt);
        return list(wrapper).stream().map(this::convertToVO).toList();
    }

    @Override
    public String generateCflowNumber() {
        String dateStr = LocalDateTime.now().format(DATE_FORMATTER);
        String prefix = "CF" + dateStr;
        LambdaQueryWrapper<Cashflow> wrapper = new LambdaQueryWrapper<>();
        wrapper.likeRight(Cashflow::getCflowNumber, prefix)
               .orderByDesc(Cashflow::getCflowNumber)
               .last("LIMIT 1");
        Cashflow last = getOne(wrapper);
        int seq = 1;
        if (last != null && last.getCflowNumber() != null
                && last.getCflowNumber().length() > prefix.length()) {
            try {
                String lastSeqStr = last.getCflowNumber().substring(prefix.length());
                seq = Integer.parseInt(lastSeqStr) + 1;
            } catch (NumberFormatException ignored) {
            }
        }
        return prefix + String.format("%04d", seq);
    }

    private CashflowVO convertToVO(Cashflow cashflow) {
        CashflowVO vo = new CashflowVO();
        BeanUtils.copyProperties(cashflow, vo);
        return vo;
    }
}

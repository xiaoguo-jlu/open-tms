package com.opentms.basedata.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.opentms.basedata.dto.BankDTO;
import com.opentms.basedata.entity.Bank;
import com.opentms.basedata.vo.BankVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface BankService extends IService<Bank> {

    IPage<BankVO> queryPage(String keyword, String countryCode, String bankType, String status, int pageNo, int pageSize);

    BankVO getById(Long id);

    boolean save(BankDTO dto);

    boolean update(BankDTO dto);

    boolean delete(Long id);

    boolean batchDelete(List<Long> ids);

    List<BankVO> listAll();
}

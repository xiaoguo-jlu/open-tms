package com.opentms.basedata.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.opentms.basedata.dto.CounterpartyAccountDTO;
import com.opentms.basedata.entity.CounterpartyAccount;
import com.opentms.basedata.vo.CounterpartyAccountVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface CounterpartyAccountService extends IService<CounterpartyAccount> {

    IPage<CounterpartyAccountVO> queryPage(Long counterpartyId, String currency, String accountType, String status, int pageNo, int pageSize);

    CounterpartyAccountVO getById(Long id);

    boolean save(CounterpartyAccountDTO dto);

    boolean update(CounterpartyAccountDTO dto);

    boolean delete(Long id);

    boolean batchDelete(List<Long> ids);

    List<CounterpartyAccountVO> listAll();
}

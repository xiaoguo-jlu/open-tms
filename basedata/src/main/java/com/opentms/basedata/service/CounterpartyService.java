package com.opentms.basedata.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.opentms.basedata.dto.CounterpartyDTO;
import com.opentms.basedata.entity.Counterparty;
import com.opentms.basedata.vo.CounterpartyVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface CounterpartyService extends IService<Counterparty> {

    IPage<CounterpartyVO> queryPage(String keyword, String type, String countryCode, String status, int pageNo, int pageSize);

    CounterpartyVO getById(Long id);

    boolean save(CounterpartyDTO dto);

    boolean update(CounterpartyDTO dto);

    boolean delete(Long id);

    boolean batchDelete(List<Long> ids);

    List<CounterpartyVO> listAll();
}

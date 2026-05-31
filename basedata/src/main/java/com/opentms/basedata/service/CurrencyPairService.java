package com.opentms.basedata.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.opentms.basedata.dto.CurrencyPairDTO;
import com.opentms.basedata.vo.CurrencyPairVO;

import java.util.List;

public interface CurrencyPairService {

    Page<CurrencyPairVO> queryPage(String keyword, String status, int pageNum, int pageSize);

    List<CurrencyPairVO> listAll();

    CurrencyPairVO getById(Long id);

    CurrencyPairVO getByPairCode(String pairCode);

    CurrencyPairVO save(CurrencyPairDTO dto);

    CurrencyPairVO updateById(CurrencyPairDTO dto);

    void removeById(Long id);
}
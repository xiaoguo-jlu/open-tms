package com.opentms.basedata.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.opentms.basedata.dto.CurrencyDTO;
import com.opentms.basedata.vo.CurrencyVO;

import java.util.List;

public interface CurrencyService {

    List<CurrencyVO> listAll();

    Page<CurrencyVO> queryPage(String keyword, String status, int pageNo, int pageSize);

    CurrencyVO getById(Long id);

    CurrencyVO getByCode(String code);

    CurrencyVO save(CurrencyDTO dto);

    CurrencyVO updateById(CurrencyDTO dto);

    void removeById(Long id);
}
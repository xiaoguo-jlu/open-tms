package com.opentms.basedata.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.opentms.basedata.dto.CurrencyDTO;
import com.opentms.basedata.vo.CurrencyVO;

import java.util.List;

public interface CurrencyService {

    Page<CurrencyVO> queryPage(String keyword, String status, int pageNum, int pageSize);

    List<CurrencyVO> listAll();

    CurrencyVO getById(Long id);

    CurrencyVO getByCode(String code);

    CurrencyVO save(CurrencyDTO dto);

    CurrencyVO updateById(CurrencyDTO dto);

    void removeById(Long id);
}
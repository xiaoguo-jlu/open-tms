package com.opentms.basedata.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.opentms.basedata.entity.Currency;

public interface CurrencyService extends IService<Currency> {

    Page<Currency> queryPage(String keyword, String status, int pageNum, int pageSize);

    boolean saveCurrency(Currency currency);

    boolean updateCurrency(Currency currency);

    boolean deleteCurrency(Long id);

    Currency getCurrencyById(Long id);
}

package com.opentms.basedata.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.opentms.basedata.entity.Bank;

public interface BankService {

    Page<Bank> queryPage(String keyword, String countryCode, String status, int pageNum, int pageSize);

    Bank getBankById(Long id);

    boolean saveBank(Bank bank);

    boolean updateBank(Bank bank);

    boolean deleteBank(Long id);

    boolean checkCodeExists(String code, Long excludeId);
}
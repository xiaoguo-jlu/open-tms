package com.opentms.bankaccount.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.opentms.bankaccount.entity.BankAccount;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BankAccountMapper extends BaseMapper<BankAccount> {
}
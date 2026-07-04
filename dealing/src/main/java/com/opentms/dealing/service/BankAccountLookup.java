package com.opentms.dealing.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 银行账户查询服务 (跨模块)
 *
 * 由于 dealing 模块不依赖 basedata, 而 AT 校验需要读取源/目标账户的
 * 币种和管理主体, 故通过 JdbcTemplate 直接查询共享的 PG 库 (opentms).
 */
@Service
public class BankAccountLookup {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public BankAccountLookup(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 查询账户的币种和管理主体ID
     * @return Map{currency, management_entity_id} 或 null (账户不存在)
     */
    public Map<String, Object> findAccountSnapshot(Long accountId) {
        if (accountId == null) {
            return null;
        }
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT currency, management_entity_id FROM tms_bank_account_t WHERE id = ? AND deleted = '0'",
                accountId);
        return rows.isEmpty() ? null : rows.get(0);
    }

    /**
     * 查询账户完整信息 (含 accountNo/accountName) — 用于复制/展示
     */
    public Map<String, Object> findAccountFull(Long accountId) {
        if (accountId == null) {
            return null;
        }
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT id, account_no, account_name, currency, management_entity_id, account_type, bank_id " +
                        "FROM tms_bank_account_t WHERE id = ? AND deleted = '0'",
                accountId);
        return rows.isEmpty() ? null : rows.get(0);
    }
}

package com.opentms.dealing.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 基于数据的实体名称查询服务 (跨模块 v2.0)
 *
 * <p>解决问题: AC/FX 详情页 / 复制场景下, 服务端需返回关联实体的可读名称,
 * 前端 BaseDataPicker 才能直接展示 "名称 (编码)" 而非 ID 数字。</p>
 *
 * <p>为什么不直接依赖 basedata 模块?
 * dealing → basedata 跨模块依赖会破坏 Maven 模块边界(单向依赖、避免循环),
 * 因此沿用 BankAccountLookup 的 JdbcTemplate 直连共享 PG 库(opentms)模式。</p>
 *
 * <p>使用场景:
 * <ul>
 *   <li>AcDealServiceImpl.getCopyData()</li>
 *   <li>FxDealServiceImpl.getCopyData()</li>
 *   <li>FxDealServiceImpl.getDetailByDealNumber() 等</li>
 * </ul>
 * </p>
 *
 * <p>所有方法都加 deleted='0' 过滤(软删除不可见)。</p>
 */
@Service
public class EntityNameLookup {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public EntityNameLookup(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 管理主体 - 复制场景 (按 ID 查询)
     * @return Map{id, code, name} 或 null
     */
    public Map<String, Object> findManagementEntity(Long id) {
        if (id == null) return null;
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT id, code, name FROM tms_management_entity_t WHERE id = ? AND deleted = '0'",
                id);
        return rows.isEmpty() ? null : rows.get(0);
    }

    /**
     * 管理主体 - 复制场景 (按 code 查询)
     * <p>Deal.managementEntity 字段存储的是 code 字符串，不是 ID，
     * 所以 AC Deal 复制时按 code 反查对应主体。</p>
     * @return Map{id, code, name} 或 null
     */
    public Map<String, Object> findManagementEntityByCode(String code) {
        if (code == null || code.isEmpty()) return null;
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT id, code, name FROM tms_management_entity_t WHERE code = ? AND deleted = '0'",
                code);
        return rows.isEmpty() ? null : rows.get(0);
    }

    /**
     * 交易对手 - 复制场景
     * @return Map{id, code, name} 或 null
     */
    public Map<String, Object> findCounterparty(Long id) {
        if (id == null) return null;
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT id, code, name FROM tms_counterparty_t WHERE id = ? AND deleted = '0'",
                id);
        return rows.isEmpty() ? null : rows.get(0);
    }

    /**
     * 交易员 - 复制场景
     * @return Map{id, code, name} 或 null
     */
    public Map<String, Object> findTrader(Long id) {
        if (id == null) return null;
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT id, code, name FROM tms_trader_t WHERE id = ? AND deleted = '0'",
                id);
        return rows.isEmpty() ? null : rows.get(0);
    }

    /**
     * 金融工具 - 复制场景
     * @return Map{id, instrumentCode, instrumentName} 或 null
     *         (PG snake_case 列已转为 camelCase, 与 Picker/前端一致)
     */
    public Map<String, Object> findInstrument(Long id) {
        if (id == null) return null;
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT id, instrument_code, instrument_name FROM tms_instrument_t WHERE id = ? AND deleted = '0'",
                id);
        if (rows.isEmpty()) return null;
        Map<String, Object> src = rows.get(0);
        Map<String, Object> out = new HashMap<>();
        out.put("id", src.get("id"));
        out.put("instrumentCode", src.get("instrument_code"));
        out.put("instrumentName", src.get("instrument_name"));
        return out;
    }

    /**
     * 币种对 - 复制场景
     * @return Map{id, pairCode, currency1, currency2} 或 null
     */
    public Map<String, Object> findCurrencyPair(Long id) {
        if (id == null) return null;
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT id, pair_code, currency1, currency2 FROM tms_currency_pair_t WHERE id = ? AND deleted = '0'",
                id);
        return rows.isEmpty() ? null : convertCurrencyPairKeys(rows.get(0));
    }

    /**
     * 银行账户 - 复制场景
     * @return Map{id, accountNo, accountName, currency} 或 null
     */
    public Map<String, Object> findBankAccount(Long id) {
        if (id == null) return null;
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT id, account_no, account_name, currency FROM tms_bank_account_t WHERE id = ? AND deleted = '0'",
                id);
        if (rows.isEmpty()) return null;
        Map<String, Object> src = rows.get(0);
        Map<String, Object> out = new HashMap<>();
        out.put("id", src.get("id"));
        out.put("accountNo", src.get("account_no"));
        out.put("accountName", src.get("account_name"));
        out.put("currency", src.get("currency"));
        return out;
    }

    /**
     * 对手方账户 - 复制场景
     * @return Map{id, accountNo, accountName, currency, counterpartyId} 或 null
     */
    public Map<String, Object> findCounterpartyAccount(Long id) {
        if (id == null) return null;
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT id, account_no, account_name, currency, counterparty_id FROM tms_counterparty_account_t WHERE id = ? AND deleted = '0'",
                id);
        if (rows.isEmpty()) return null;
        Map<String, Object> src = rows.get(0);
        Map<String, Object> out = new HashMap<>();
        out.put("id", src.get("id"));
        out.put("accountNo", src.get("account_no"));
        out.put("accountName", src.get("account_name"));
        out.put("currency", src.get("currency"));
        out.put("counterpartyId", src.get("counterparty_id"));
        return out;
    }

    /**
     * PG 默认返回 snake_case 列名,BaseDataPicker 期望 camelCase 字段
     * (pairCode/currency1/currency2)。统一转译。
     */
    private Map<String, Object> convertCurrencyPairKeys(Map<String, Object> src) {
        Map<String, Object> out = new HashMap<>();
        out.put("id", src.get("id"));
        out.put("pairCode", src.get("pair_code"));
        out.put("currency1", src.get("currency1"));
        out.put("currency2", src.get("currency2"));
        return out;
    }
}

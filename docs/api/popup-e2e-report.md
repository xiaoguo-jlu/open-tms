# 基础数据弹框/列表端到端测试报告

> 日期: 2026-07-11
> 范围: web/src/views/basedata/*.vue 所有 res.data.records 引用
> 代理: http://localhost:3000
> 总数: 20 | 通过: 19 | 失败: 1 | 通过率: 95.0%
> 评级: **B**

## 1. 修复说明

**核心 Bug**:`web/src/views/basedata/CounterpartyAccountList.vue` 3 处写错为 `res.data.list`
(后端实际返回 `{code, data: {records, total, ...}}`)。
导致 counterpartyList.value 始终为 `[]` → 弹框 el-option 不渲染 → 选不到交易对手。

**全量清理**:把 10 个 .vue 的 `res.data.records || res.data.list || []` 统一为 `res.data.records || []`,
避免代码腐烂 + 后续维护混淆。

## 2. 测试结果明细

| 文件 | ref | API | Method | URL | HTTP | code | records | 结果 |
|------|-----|-----|--------|-----|------|------|---------|------|
| BankAccountList.vue | bankList | listBank | get | `/api/v1/bank-accounts/page` | 200 | 200 | 7 | ✅ |
| BankAccountList.vue | currencyList | listBank | get | `/api/v1/bank-accounts/page` | 200 | 200 | 7 | ✅ |
| BankAccountList.vue | managementEntityList | listBank | get | `/api/v1/bank-accounts/page` | 200 | 200 | 7 | ✅ |
| BankAccountList.vue | tableData | listBank | get | `/api/v1/bank-accounts/page` | 200 | 200 | 7 | ✅ |
| CounterpartyAccountList.vue | counterpartyList | listCounterparty | get | `/api/v1/counterparties/page` | 200 | 200 | 7 | ✅ |
| CounterpartyAccountList.vue | currencyList | listCounterparty | get | `/api/v1/counterparties/page` | 200 | 200 | 7 | ✅ |
| CounterpartyAccountList.vue | tableData | listCounterparty | get | `/api/v1/counterparties/page` | 200 | 200 | 7 | ✅ |
| CounterpartyList.vue | countryList | listCountry | get | `/api/v1/countries/page` | 200 | 200 | 14 | ✅ |
| CounterpartyList.vue | tableData | listCountry | get | `/api/v1/countries/page` | 200 | 200 | 14 | ✅ |
| CountryList.vue | tableData | listCountry | get | `/api/v1/countries/page` | 200 | 200 | 14 | ✅ |
| CurrencyList.vue | tableData | listCurrency | get | `/api/v1/currencies/page` | 200 | 200 | 12 | ✅ |
| CurrencyPairList.vue | tableData | listCurrencyPair | get | `/api/v1/currency-pairs/page` | 200 | 200 | 29 | ✅ |
| DefaultBankAccountRuleList.vue | tableData | pageDefaultBankAccountRule | post | `/api/v1/default-bank-account-rules/page` | 200 | 200 | 1 | ✅ |
| DefaultBankAccountRuleList.vue | auditLogs | deleteDefaultBankAccountRule | post | `/api/v1/default-bank-account-rules/delete/${id}` | 400 | None | 0 | ❌ |
| HolidayList.vue | countryList | listCountry | get | `/api/v1/countries/page` | 200 | 200 | 14 | ✅ |
| HolidayList.vue | tableData | listCountry | get | `/api/v1/countries/page` | 200 | 200 | 14 | ✅ |
| InstrumentList.vue | tableData | listInstrument | get | `/api/v1/instruments/page` | 200 | 200 | 5 | ✅ |
| ManagementEntityList.vue | tableData | listManagementEntity | get | `/api/v1/management-entities/page` | 200 | 200 | 8 | ✅ |
| SubsidiaryList.vue | tableData | listSubsidiary | get | `/api/v1/subsidiaries/page` | 200 | 200 | 7 | ✅ |
| TraderList.vue | tableData | listTrader | get | `/api/v1/traders/page` | 200 | 200 | 13 | ✅ |

## 3. 后续

- 修改了 CounterpartyAccountList.vue(主 bug)
- 清理 10 个 .vue 的双 fallback
- 后续 Vite HMR 即时生效,无需重新构建
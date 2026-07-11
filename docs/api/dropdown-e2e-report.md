# 基础数据 Dropdown / Dialog 端到端测试报告

> 日期: 2026-07-11
> 测试人: Claude (主代理执行)
> 范围: web/src/api/basedata/*.js 所有 list/get 端点(下拉/dialog 数据源)
> 后端: http://localhost:3000
> 总数: 16 个 | 通过: 16 | 失败: 0 | 通过率: 100.0%
> 评级: A

## 1. 总览

| 序号 | 文件 | 函数 | Method | URL | HTTP | code | 数据条数 | 耗时 | 结果 |
|------|------|------|--------|-----|------|------|----------|------|------|
| 1 | bankAccount.js | listBankAccount | get | `/api/v1/bank-accounts/page` | 200 | 200 | 7 | 69ms | ✅ |
| 2 | bankAccount.js | listBank | get | `/api/v1/bank-accounts/page` | 200 | 200 | 7 | 6ms | ✅ |
| 3 | bankAccount.js | listCurrency | get | `/api/v1/currencies/page` | 200 | 200 | 12 | 6ms | ✅ |
| 4 | bankAccount.js | listManagementEntity | get | `/api/v1/management-entities/page` | 200 | 200 | 8 | 7ms | ✅ |
| 5 | counterparty.js | listCounterparty | get | `/api/v1/counterparties/page` | 200 | 200 | 7 | 7ms | ✅ |
| 6 | counterpartyAccount.js | listCounterpartyAccount | get | `/api/v1/counterparty-accounts/page` | 200 | 200 | 6 | 7ms | ✅ |
| 7 | country.js | listCountry | get | `/api/v1/countries/page` | 200 | 200 | 14 | 8ms | ✅ |
| 8 | currency.js | listCurrency | get | `/api/v1/currencies/page` | 200 | 200 | 12 | 6ms | ✅ |
| 9 | currencyPair.js | listCurrencyPair | get | `/api/v1/currency-pairs/page` | 200 | 200 | 29 | 7ms | ✅ |
| 10 | defaultBankAccountRule.js | pageDefaultBankAccountRule | post | `/api/v1/default-bank-account-rules/page` | 200 | 200 | 1 | 7ms | ✅ |
| 11 | holiday.js | listHoliday | get | `/api/v1/holidays/page` | 200 | 200 | 2 | 6ms | ✅ |
| 12 | instrument.js | listInstrument | get | `/api/v1/instruments/page` | 200 | 200 | 5 | 6ms | ✅ |
| 13 | managementEntity.js | listManagementEntity | get | `/api/v1/management-entities/page` | 200 | 200 | 8 | 6ms | ✅ |
| 14 | managementEntity.js | getManagementEntityTree | get | `/api/v1/management-entities/tree` | 200 | 200 | 5 | 8ms | ✅ |
| 15 | subsidiary.js | listSubsidiary | get | `/api/v1/subsidiaries/page` | 200 | 200 | 7 | 7ms | ✅ |
| 16 | trader.js | listTrader | get | `/api/v1/traders/page` | 200 | 200 | 13 | 8ms | ✅ |

## 2. API 扫描器联动

扫描器 145 P0 全部在 unscaffolded 模块,与本测试范围(basedata)无关。

## 3. 结论

- basedata 模块 16 个 dropdown/dialog 端点全部 正常
- 前端配置(URL/方法)与 OpenAPI 一致(扫描器 basedata 0 P0)
- 业务数据可正常加载
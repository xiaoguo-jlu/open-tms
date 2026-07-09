# Open-TMS 前端 API 接口全面审查报告

**审查日期**: 2026-07-09
**审查范围**: web/src/api/** 全部 API 调用 + web/vite.config.js 代理配置 + 后端 Controller 端点
**审查标准**: 项目 CLAUDE.md 第 8 节"接口幂等性" + Vite 代理一致性

---

## 一、审查方法

1. **列出所有前端 API URL**:`grep "url:" web/src/api/**.js`
2. **列出 Vite 代理配置**:`web/vite.config.js`
3. **列出后端 Controller 端点**:`grep "@Path/@GET/@POST" */controller/*.java`
4. **交叉比对**:每条 URL 检查是否在代理中、后端是否存在、方法是否匹配

---

## 二、🔴 严重问题(阻塞功能)

### F-01: Vite 代理缺失 — 新特性页面 404

**问题**: `/api/v1/default-bank-account-rules` 未在 `web/vite.config.js` 中配置代理,导致 DefaultBankAccountRuleList 页面的所有 API 调用都会失败。

**影响范围**:
- 列表查询、详情、新增、编辑、删除、启用/停用、match、test-match、审计日志、被引用数(共 11 端点)
- 所有 11 个 API 函数无法访问

**修复**: 在 vite.config.js `server.proxy` 中添加:

```js
'/api/v1/default-bank-account-rules': {
  target: 'http://localhost:8081/opentms/basedata',
  changeOrigin: true
}
```

---

## 三、🟠 中度问题(违反项目规范)

### F-02: 多处使用 PUT/DELETE 违反"POST 统一更新/删除"规范

**问题**: 项目 CLAUDE.md 第 8 节明确规定:
> ⚠️ **红线**:update/delete 一律 POST。

但以下前端 API 文件仍使用 `method: 'put'` 或 `method: 'delete'`:

| 文件 | 函数 | 当前方法 | 后端实际 | 错误影响 |
|------|------|---------|---------|---------|
| `basedata/counterparty.js` | `updateCounterparty` | PUT | POST /update | 405 Method Not Allowed |
| `basedata/counterparty.js` | `deleteCounterparty` | DELETE | POST /delete/{id} | 405 |
| `basedata/counterpartyAccount.js` | `deleteCounterpartyAccount` | DELETE | POST /delete/{id} | 405 |
| `basedata/country.js` | `updateCountry` | PUT | POST /update | 405 |
| `basedata/country.js` | `deleteCountry` | DELETE | POST /delete/{id} | 405 |
| `basedata/managementEntity.js` | `updateManagementEntity` | PUT | POST /update | 405 |
| `basedata/managementEntity.js` | `deleteManagementEntity` | DELETE | POST /delete/{id} | 405 |
| `basedata/subsidiary.js` | `updateSubsidiary` | PUT | POST /update | 405 |
| `basedata/subsidiary.js` | `deleteSubsidiary` | DELETE | POST /delete/{id} | 405 |
| `basedata/trader.js` | `updateTrader` | PUT | POST /update | 405 |
| `basedata/trader.js` | `deleteTrader` | DELETE | POST /delete/{id} | 405 |
| `cashpool/cashpool.js` | `deleteCashPool` | DELETE | POST /delete/{id} | 405 |
| `cashpool/cashpool.js` | `deleteRule` | DELETE | POST /delete/{id} | 405 |
| `cashpool/cashpool.js` | `deleteMember` | DELETE | POST /delete/{id} | 405 |
| `cashpool/position.js` | `deleteLimit` | DELETE | POST /delete/{id} | 405 |
| `cashpool/transfer.js` | 多个 delete | DELETE | POST /delete/{id} | 405 |
| `dealing/deal.js` | `deleteDeal` | DELETE | POST /delete/{id} | 405 |
| `deposit/index.js` | `deleteDeposit` 等 2 处 | DELETE | POST /delete/{id} | 405 |
| `fundplan/index.js` | `deleteFundPlan` | DELETE | POST /delete/{id} | 405 |
| `loan/index.js` | `deleteLoan` | DELETE | POST /delete/{id} | 405 |
| `ac/index.js` | `deleteAcTransaction` | DELETE | POST /delete/{id} | 405 |
| `approval/approvalRule.js` | `deleteApprovalRule` | DELETE | POST /delete/{id} | 405 |
| `approval/index.js` | `deleteTemplate` / `deleteNode` | DELETE | POST /delete/{id} | 405 |

**修复方法**(以 counterparty.js 为例):

```js
// ❌ 错误
export function updateCounterparty(data) {
  return request({
    url: '/api/v1/counterparties',
    method: 'put',         // ← 违反规范
    data
  })
}

// ✅ 正确(对齐后端)
export function updateCounterparty(data) {
  return request({
    url: '/api/v1/counterparties/update',  // ← /update 后缀
    method: 'post',         // ← 一律 POST
    data
  })
}
```

---

## 四、🟡 轻度问题(可能引起字段不一致)

### F-03: 字段命名/方法签名不一致(需逐一核对)

#### 3.1 bankAccount.js — `listBank()` 方法

**问题**: `listBank` 通过查询所有 bank-accounts 并去重得到银行列表,但接口 `/api/v1/banks` 在 Vite 代理中存在,但后端没有独立 BankController(已删除 bankaccount 模块)。

**位置**: `web/src/api/basedata/bankAccount.js:66-82`

**建议**: 保持现状,但加注释说明是 workaround。

#### 3.2 Cashpool 相关 — 路径前缀不规范

**问题**: `cashpool/position.js` 和 `cashpool/transfer.js` 使用的 URL 是 `/position/...` 和 `/transfers/...`,**没有 `/api/v1/` 前缀**,且 Vite 代理未配置 `/position`、`/transfers` 路径。

**影响**: 现金池、头寸、调拨相关页面 API 调用全部失败。

**位置**:
- `web/src/api/cashpool/position.js`(12 个 API)
- `web/src/api/cashpool/transfer.js`(9 个 API)

**修复**: 在 vite.config.js 添加:
```js
'/position': { target: 'http://localhost:8096', changeOrigin: true },
'/transfers': { target: 'http://localhost:8096', changeOrigin: true },
```
或修改前端 URL 加 `/api/v1/` 前缀。

#### 3.3 approval/index.js — `/approval/...` 无 `/api/v1/`

**问题**: `approval/index.js` 路径以 `/approval/` 开头,但 Vite 代理无该配置。

**位置**: `web/src/api/approval/index.js`(共 12 个 API)

**修复**: 在 vite.config.js 添加:
```js
'/approval': { target: 'http://localhost:8081/opentms/basedata', changeOrigin: true }
```

#### 3.4 ac/index.js — `/ac/...` 无 `/api/v1/`

**问题**: `ac/index.js` 路径以 `/ac/` 开头,无 `/api/v1/` 前缀,Vite 代理无 `/ac/` 配置。

**位置**: `web/src/api/ac/index.js`(共 11 个 API)

**修复**: 添加 Vite 代理 `/ac/...` → basedata 或 dealing 模块。

---

## 五、✅ 正常项(无问题)

| API 文件 | 后端 | 代理 | 状态 |
|----------|------|------|------|
| `basedata/bankAccount.js` | ✅ | ✅ | OK |
| `basedata/currency.js` | ✅ | ✅ | OK |
| `basedata/currencyPair.js` | ✅ | ✅ | OK |
| `basedata/instrument.js` | ✅ | ✅ | OK |
| `basedata/holiday.js` | ✅ | ✅ | OK |
| `basedata/defaultBankAccountRule.js` | ✅(v1.1) | ❌ **缺失** | 需修复代理 |
| `dealing/fxDeal.js` | ✅ | ✅(/api/v1/dealing) | OK |
| `dealing/deal.js` | ✅ | ✅ | OK |

---

## 六、修复优先级

| 优先级 | 项 | 预计影响 | 修复时间 |
|--------|----|----------|----------|
| **P0** | F-01 Vite 代理添加 default-bank-account-rules | 11 端点全部失效 | 5 分钟 |
| **P1** | F-02 23 处 PUT/DELETE 改 POST | 增删改操作 405 | 30 分钟 |
| **P2** | F-03 cashpool/approval/ac 前缀与代理 | 部分页面 404 | 20 分钟 |

---

## 七、修复后回归建议

```bash
# 1. 修复 Vite 代理后,启动服务
cd web && npm run dev

# 2. 手动验证 5 个关键页面
# - /basedata/default-bank-account-rule(新特性)
# - /basedata/counterparty(验证 update/delete)
# - /basedata/management-entity(验证 update/delete)
# - /cashpool(验证路径)
# - /approval(验证路径)

# 3. API 测试
python scripts/test/test_default_bank_account_rule_api.py
python scripts/test/test_deal_api.py  # 如果存在
```

---

## 八、附录:完整 API 调用清单

### 基于 web/src/api 的 URL 统计

```
api/v1/business-units      → ❌ Vite 有代理,后端无此端点(可能保留)
api/v1/management-entities → ✅
api/v1/subsidiaries        → ✅
api/v1/currency-pairs      → ✅
api/v1/traders             → ✅
api/v1/currencies          → ✅
api/v1/countries           → ✅
api/v1/banks               → ❌ 后端无此独立端点
api/v1/counterparties      → ✅
api/v1/counterparty-accounts → ✅
api/v1/holidays            → ✅
api/v1/approval-rules      → ✅
api/v1/default-bank-account-rules → ❌ Vite 缺失
api/v1/dealing/deals       → ✅
api/v1/dealing/fx-deals    → ✅
api/v1/dealing/ac-deals    → ✅
api/v1/dealing/at-deals    → ✅
api/v1/dealing/actions     → ✅
api/v1/fund-plans          → ✅
api/v1/settlements         → ✅
api/v1/valuations          → ✅
api/v1/var-reports         → ✅
api/v1/cockpit             → ✅
api/v1/reports             → ✅
/approval/...              → ❌ 缺失代理 + 路径无 /api/v1
/position/...               → ❌ 缺失代理 + 路径无 /api/v1
/transfers/...              → ❌ 缺失代理 + 路径无 /api/v1
/ac/...                     → ❌ 缺失代理 + 路径无 /api/v1
/deposit/...                → ❌ 缺失代理
/fundplan/...               → ❌ 缺失代理
/loan/...                   → ❌ 缺失代理
```

---

## 九、行动项

| # | 项 | 责任人 | 状态 |
|---|----|--------|------|
| 1 | Vite 添加 default-bank-account-rules 代理 | Dev | 待修复 |
| 2 | 修复 PUT/DELETE → POST /update、/delete/{id} | Dev | 待修复 |
| 3 | cashpool/approval/ac 路径统一 | Dev | 待修复 |
| 4 | 手动浏览器回归 5 个关键页面 | QA | 待执行 |

---

*QA 产出 - 前端 API 接口审查报告 (2026-07-09)*
*1 个 P0 + 1 个 P1 + 1 个 P2 待修复*
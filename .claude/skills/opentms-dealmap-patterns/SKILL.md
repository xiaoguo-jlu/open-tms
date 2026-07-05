---
name: opentms-dealmap-patterns
description: |
  Open-TMS DealMap v3.2 设计模式与前端实现规范。覆盖单字段多行模式、
  4 种 DealMap 类型(FX_BUY_AMOUNT/FX_SELL_AMOUNT/FX_RATE/FX_FIX)、
  1 DealMap → 0/1 Cashflow 约束、BaseDataPicker 使用模式、
  4 模式详情页(new/copy/edit/readonly)统一布局、跨模块数据查找模式。

  Trigger: "DealMap"、"交易字段映射"、"DealMap 模式"、"FX DealMap"、"详情页模式"
---

# opentms-dealmap-patterns

DealMap v3.2 设计模式 — 定义交易字段映射的标准模式、前端组件使用规范、跨模块数据查找约定。

> 本 skill 不执行具体实现,而是为 `opentms-business-architect` / `opentms-db-design` /
> `opentms-frontend-dev` / `opentms-backend-dev` 提供统一的设计模式参考。

---

## 1. DealMap v3.2 核心概念

**单字段多行模式**: 一张 DealMap 表存储所有交易类型的字段定义,通过 `deal_type` + `field_key` 区分。

```sql
-- DealMap 核心结构
tms_deal_map_t (
    id              BIGSERIAL PRIMARY KEY,
    deal_type       VARCHAR(10) NOT NULL,  -- AC/AT/FX/IRS/...
    field_key       VARCHAR(50) NOT NULL,  -- buy_amount/sell_amount/fx_rate/...
    field_label     VARCHAR(100),          -- 前端显示标签
    field_type      VARCHAR(20),           -- DECIMAL/DATE/VARCHAR/CCY_PAIR
    source_table    VARCHAR(100),          -- 数据来源表(tms_ac_deal_t)
    source_column   VARCHAR(50),           -- 数据来源列(buy_amount)
    is_required     CHAR(1) DEFAULT '1',   -- 是否必填
    display_order   INT DEFAULT 0,         -- 显示顺序
    UNIQUE(deal_type, field_key)
);
```

**设计原则**: 不按交易类型建 N 张 Map 表,所有类型共用一个表定义。

---

## 2. 四种 DealMap 类型

### 2.1 FX_BUY_AMOUNT — 买入金额字段

```yaml
deal_type: FX
field_key: buy_amount
特性:
  - 精度: DECIMAL(38,18)
  - 必填: Y
  - 联动: 选择币种对后,自动填入买入币种
  - 校验: buy_amount > 0
```

### 2.2 FX_SELL_AMOUNT — 卖出金额字段

```yaml
deal_type: FX
field_key: sell_amount
特性:
  - 精度: DECIMAL(38,18)
  - 必填: 根据交易方向(FIX_IN/FIX_OUT)决定
  - 联动: 填写 buy_amount + fx_rate → 自动计算 sell_amount
  - 校验: sell_amount > 0
```

### 2.3 FX_RATE — 汇率字段

```yaml
deal_type: FX
field_key: fx_rate
特性:
  - 精度: DECIMAL(18,8)
  - 必填: Y
  - 联动: 修改 fx_rate 自动重算另一方向金额
  - 校验: fx_rate > 0
  - 来源: 市场数据 / 手动录入 / 央行牌价
```

### 2.4 FX_FIX — 定价方式

```yaml
deal_type: FX
field_key: fix_type
特性:
  - 类型: VARCHAR(10)
  - 取值: FIX_IN(固定买入金额) / FIX_OUT(固定卖出金额) / FIX_RATE(固定汇率)
  - 联动: FIX_IN → 锁定 buy_amount,计算 sell_amount
         FIX_OUT → 锁定 sell_amount,计算 buy_amount
         FIX_RATE → 锁定 fx_rate,用户填金额
```

---

## 3. 1 DealMap → 0/1 Cashflow 约束

**核心规则**: 一条 DealMap 定义最多产生 0 或 1 条 Cashflow 记录,不产生多条。

```yaml
映射关系:
  FX_BUY_AMOUNT:  → 1 cashflow (buy_leg)
  FX_SELL_AMOUNT: → 1 cashflow (sell_leg)
  FX_RATE:        → 0 cashflow (汇率不产生现金流)
  FX_FIX:         → 0 cashflow (定价方式不产生现金流)
  AC_AMOUNT:      → 1 cashflow
  AT_FROM_AMOUNT: → 1 cashflow (from_leg)
  AT_TO_AMOUNT:   → 1 cashflow (to_leg)
```

**校验规则**:
- 一个 Deal 的所有 DealMap 产生的 Cashflow 数量 ≤ DealMap 中标记为产生 Cashflow 的字段数
- 禁止一个 DealMap 字段产生 2+ 条 Cashflow
- Deal 执行(execute)时触发 Cashflow 生成

---

## 4. BaseDataPicker 使用模式

### 4.1 基本使用

```vue
<BaseDataPicker
  ref="pickerRef"
  v-model="form.bankAccountId"
  module="basedata"
  resource="bank-account"
  :auto-filter="{ status: '1' }"
  :return-field="['id', 'bankAccountCode', 'bankAccountName', 'currency']"
  @change="handleBankAccountChange"
/>
```

### 4.2 preloadRow — 编辑回填

```vue
// 编辑模式下回填已选数据
onMounted(async () => {
  if (props.mode === 'edit') {
    await pickerRef.preloadRow(form.bankAccountId);
  }
});
```

### 4.3 autoFilter — 筛选联动

```vue
// 根据已选管理主体过滤账户列表
<BaseDataPicker
  :auto-filter="{ managementEntityId: form.managementEntityId }"
/>
```

### 4.4 @change 事件 — 联动更新

```typescript
const handleBankAccountChange = (row: any) => {
  form.currency = row.currency;
  form.bankAccountName = row.bankAccountName;
  form.bankId = row.bankId;
};
```

---

## 5. 4 模式详情页统一布局

**1 个 Vue 组件,4 种模式** — 新建(new) / 复制(copy) / 编辑(edit) / 只读(readonly)

### 5.1 模式状态管理

```typescript
const props = defineProps<{ mode: 'new' | 'copy' | 'edit' | 'readonly'; id?: number }>();

const isDisabled = computed(() => props.mode === 'readonly');
const isEdit = computed(() => props.mode === 'edit' || props.mode === 'copy');
const modeLabel = computed(() => {
  const map = { new: '新建', copy: '复制', edit: '编辑', readonly: '查看' };
  return map[props.mode];
});
```

### 5.2 统一布局模板

```
┌────────────────────────────────────────────┐
│  ModeBadge :mode="mode"                    │
├────────────────────────────────────────────┤
│  [基本信息]                                │
│  交易编号: [input]  交易日期: [datepicker] │
│  币种对:   [picker] 交易方向: [select]     │
├────────────────────────────────────────────┤
│  [交易明细]                                │
│  买入金额: [input]  卖出金额: [input]      │
│  汇率:     [input]  定价方式: [select]     │
├────────────────────────────────────────────┤
│  [操作按钮]                                │
│  new:     [保存] [取消]                    │
│  copy:    [保存] [取消]                    │
│  edit:    [保存] [提交审批] [取消]         │
│  readonly:[编辑] [复制]                    │
└────────────────────────────────────────────┘
```

### 5.3 模式差异处理

| 差异点 | new | copy | edit | readonly |
|--------|-----|------|------|----------|
| 表单禁用 | false | false | false | true |
| 初始数据 | 空 | 复制源数据 | 加载已有数据 | 加载已有数据 |
| 保存按钮 | 显示 | 显示 | 显示 | 隐藏 |
| 审批按钮 | 隐藏 | 隐藏 | 显示(状态允许时) | 隐藏 |
| DealMap 加载 | 按 deal_type 加载模板 | 复制源 DealMap | 加载已保存 DealMap | 加载已保存 DealMap |

---

## 6. 跨模块数据查找 (BankAccountLookup 模式)

### 6.1 查找模式定义

```typescript
// BankAccountLookup — 跨模块查找银行账户
interface BankAccountLookup {
  id: number;
  bankAccountCode: string;
  bankAccountName: string;
  bankId: number;
  bankName: string;
  currency: string;
  managementEntityId: number;
  status: string;
}
```

### 6.2 API 调用规范

```typescript
// web/src/api/basedata/bankAccount.js
export function lookupBankAccount(params: {
  managementEntityId?: number;
  currency?: string;
  keyword?: string;
}) {
  return request.get('/api/v1/bank-accounts/lookup', { params });
}
```

### 6.3 跨模块引用约定

| 查找场景 | 数据来源模块 | API 路径 | 返回字段 |
|----------|-------------|----------|----------|
| 银行账户 | basedata | GET /api/v1/bank-accounts/lookup | id, code, name, bankId, currency |
| 交易对手方 | basedata | GET /api/v1/counterparties/lookup | id, code, name, creditRating |
| 管理主体 | basedata | GET /api/v1/management-entities/lookup | id, code, name, country |
| 金融工具 | basedata | GET /api/v1/instruments/lookup | id, code, name, instrumentType |
| 币种对 | basedata | GET /api/v1/currency-pairs/lookup | id, pairCode, baseCcy, quoteCcy |

---

## 7. 与存量模块的关系

- **basedata**: 提供 BankAccount / Counterparty / Instrument / CurrencyPair 数据源
- **dealing**: 消费 DealMap 实现 AC/AT/FX 交易录入
- **fx**: 消费 DealMap 实现 FX 交易特有的 4 种类型
- **cashflow**: 消费 DealMap → Cashflow 映射规则

---

## 8. 相关 Skills

- `opentms-business-architect` — 业务架构与 DealMap 建模
- `opentms-db-design` — DealMap 表结构设计
- `opentms-frontend-dev` — BaseDataPicker / 4 模式详情页实现
- `opentms-backend-dev` — DealMap 解析与 Cashflow 生成

---

## 版本历史

| 版本 | 日期 | 变更内容 |
|------|------|---------|
| v1.0 | 2026-07-05 | 初始版本 — DealMap v3.2 + BaseDataPicker + 4 模式 + 跨模块查找 |

# Open-TMS M3-交易对手默认银行账户规则 PRD

**版本**: v1.0 Phase 1 PM 草稿  
**角色**: 产品经理(PM)  
**日期**: 2026-07-10  
**模块归属**: basedata 基础数据 / dealing 交易录入联动  
**特性名称**: 交易对手默认银行账户规则(Counterparty Default Bank Account Rule, CDBAR)  
**参考实现**: v1.1 default-bank-account-rule(管理主体锚点规则)  
**状态**: Phase 1 PRD 草稿,待需求评审

---
## 0. 修订记录
| 版本 | 日期 | 作者 | 变更内容 |
|------|------|------|----------|
| v1.0 | 2026-07-10 | PM | 首版 PRD,定义交易对手锚点默认银行账户规则,参考 v1.1 default-bank-account-rule 的 11 端点、lockToken、审计、被引用数、双方向匹配能力 |

---
## 1. 背景与动机
### 1.1 现状
Open-TMS 已实现 v1.1 默认银行账户规则,定位是**以管理主体为锚点**。

现有规则根据:

- `managementEntityId` 必填;
- `counterpartyId` / `instrumentId` / `currency` / `direction` 可选;
- 命中后返回一个默认 `bankAccountId`。

它解决的是:"某个我方管理主体在某类收付交易中默认使用哪个我方银行账户"。

### 1.2 新问题
实际资金交易中,默认结算账户也经常由**外部交易对手方关系**决定。

典型业务场景:

1. **HSBC 全球场景**  
   集团与 HSBC 多个分支做 FX / 存款 / 贷款交易。财资政策要求:对 HSBC 集团的 USD 收付默认走我方 HSBC HK USD 账户,降低跨行手续费和失败率。

2. **中国银行 CNY 场景**  
   对中国银行所有 CNY 收付默认走我方境内中行账户,便于银企直连、回单下载和自动对账。

3. **对手方具体账户场景**  
   同一交易对手可能有多个对手方账户或 SSI/CSI 指令。若交易指定了某个具体 `counterpartyAccountId`,系统应优先使用账户级规则。

4. **操作风险控制场景**  
   交易员手工选择银行账户容易选错实体、币种或银行。系统自动推荐默认账户可降低录入错误。

5. **结算成本优化场景**  
   企业希望优先选择同银行、同地区、同币种账户,减少 SWIFT / 跨境 / 跨行费用。

### 1.3 目标
新增一套**以交易对手为锚点**的默认银行账户规则:

- `counterparty_id` 必填;
- `counterparty_account_id` 可选,用于对手方具体账户级规则;
- 支持 `instrument_id` / `currency` / `direction` / `dual_direction`;
- 结果仍为我方默认 `bank_account_id`;
- 与 v1.1 default-bank-account-rule 同时存在;
- 交易发生时两条规则链路都跑;
- 按 **specificity(精确度) > priority(优先级)** 决胜;
- 命中结果只作为默认推荐值,交易员仍可手动修改。

### 1.4 设计原则
| 原则 | 说明 |
|------|------|
| 兼容 v1.1 | API 风格、lockToken、审计、被引用数、状态值参考 v1.1 default-bank-account-rule |
| 对手方锚点 | 本特性主语义是 counterparty,不是 managementEntity |
| 精确度优先 | `counterparty_account_id` 精确规则优先于对手方泛化规则 |
| 可解释 | test-match 必须返回候选规则、排序与命中原因 |
| 可回退 | 未命中不阻断交易保存,交易员可手动选择账户 |
| 不强制执行 | 默认账户是推荐值,不是支付指令锁定值 |

---

## 2. 范围(In / Out of Scope)

### 2.1 In Scope

| 编号 | 功能 | 优先级 | 说明 |
|------|------|--------|------|
| IN-1 | 规则 CRUD | P0 | 新增、分页、详情、更新、软删除 |
| IN-2 | 启用/停用 | P0 | Active / Inactive 状态,参考 v1.1 default-bank-account-rule 的 `status` 字段 |
| IN-3 | 运行时 match | P0 | 基于 `counterpartyId` 维度返回默认账户 |
| IN-4 | test-match | P0 | 返回所有候选规则、specificityScore、priority、命中原因 |
| IN-5 | 审计日志 | P0 | CREATE / UPDATE / DELETE / ENABLE / DISABLE 全量记录 |
| IN-6 | 被引用数 | P0 | 删除前展示该规则已被 N 笔交易引用 |
| IN-7 | 双方向 | P0 | `dual_direction=true` 一条规则覆盖 Inflow 与 Outflow |
| IN-8 | 并发编辑 | P0 | 参考 v1.1 default-bank-account-rule 的 `lockToken` 字段,冲突返回 409 |
| IN-9 | 唯一约束 | P0 | Active 状态同维度组合不可重复 |
| IN-10 | 与 v1.1 联动 | P0 | 管理主体规则链路和交易对手规则链路共同决胜 |
| IN-11 | 前端配置页 | P1 | 列表、筛选、编辑弹窗、审计弹窗、删除提示 |

### 2.2 Out of Scope

| 编号 | 不做项 | 说明 |
|------|--------|------|
| OUT-1 | 不强制执行命中结果 | 交易员仍可手动修改银行账户 |
| OUT-2 | 不生成支付指令 | 本特性不负责 SWIFT、银企直连、支付路由 |
| OUT-3 | 不维护对手方账户主数据 | `counterparty_account_id` 依赖既有对手方账户模块 |
| OUT-4 | 不做复杂规则引擎 UI | Phase 1 使用表单配置,不做拖拽式规则编排 |
| OUT-5 | 不替代 v1.1 规则 | 两套规则并存,不是迁移或废弃 |
| OUT-6 | 不做审批流 | 规则维护只做操作审计,暂不引入审批 |
| OUT-7 | 不做外部 CSI 自动导入 | 不从银行或 ERP 自动导入结算指令 |

---

## 3. 用户故事(Given-When-Then)

### US-1: 对手方专员为某外部对手方配置默认收/付账户

**作为** 对手方专员  
**我希望** 为某外部对手方配置默认收款/付款账户  
**以便** 交易员录入交易时自动带出我方推荐银行账户。

Given 系统中已存在交易对手 HSBC Bank、我方 HSBC HK USD 银行账户  
When 对手方专员新增规则:counterparty=HSBC Bank,currency=USD,dualDirection=true,bankAccount=HSBC HK USD  
Then 系统保存规则并生成规则编号  
And 状态默认为 Active  
And 后续与 HSBC Bank 的 USD Inflow/Outflow 交易均可命中该规则。

### US-2: 多币种对手方按币种分别配置默认账户

**作为** 财资经理  
**我希望** 对 HSBC 全球按币种配置不同默认账户  
**以便** USD、CNY、EUR 收付分别走最合适的我方账户。

Given HSBC Bank 已存在,且我方存在 USD、CNY、EUR 三个银行账户  
When 财资经理分别新增 USD/CNY/EUR 三条 Active 规则  
Then USD 交易命中 USD 账户  
And CNY 交易命中 CNY 账户  
And EUR 交易命中 EUR 账户  
And 三条规则因 currency 不同,不触发唯一约束冲突。

### US-3: 匹配测试

**作为** 财资经理  
**我希望** 模拟一笔交易查看会命中哪条规则  
**以便** 上线前验证规则精确度和优先级。

Given 系统存在多条交易对手默认银行账户规则  
When 财资经理输入 counterpartyId、counterpartyAccountId、instrumentId、currency、direction 进行测试匹配  
Then 系统返回所有候选规则列表  
And 每条候选规则展示 specificityScore、priority、命中维度、是否最终胜出  
And 最终胜出的规则排在第一位。

### US-4: 并发编辑冲突提示

**作为** 对手方专员  
**我希望** 多人同时编辑同一规则时能得到冲突提示  
**以便** 避免覆盖他人的最新修改。

Given 用户 A 与用户 B 同时打开同一条规则详情,两人获得相同 lockToken  
When 用户 A 先保存并刷新 lockToken  
And 用户 B 使用旧 lockToken 提交保存  
Then 后端返回 409 Conflict  
And 前端提示"规则已被他人修改,请刷新后重试"  
And 用户 B 的修改不会覆盖用户 A 的结果。

### US-5: 审计与被引用数

**作为** 审计人员  
**我希望** 查看规则变更历史与规则被交易引用的数量  
**以便** 删除或停用规则前评估影响。

Given 某规则已被 12 笔交易引用  
When 用户点击删除该规则  
Then 系统先调用 reference-count 返回 totalCount=12  
And 前端提示"该规则已被 12 笔交易引用,确认删除?"  
And 用户确认后系统执行软删除并写入 DELETE 审计日志。

### US-6: 对手方具体账户优先于对手方泛化规则

**作为** 交易员  
**我希望** 当交易指定了对手方具体账户时,系统优先使用账户级规则  
**以便** 同一对手方不同 SSI/CSI 指令能带出不同我方账户。

Given HSBC Bank 有一条 counterparty 级 USD 规则,又有一条 counterpartyAccount 级 USD 规则  
When 交易录入选择了该 counterpartyAccountId  
Then 系统优先命中 counterpartyAccount 级规则  
And 即使 counterparty 级规则 priority 更高,也不能覆盖账户级规则。

---

## 4. 字段定义

### 4.1 核心表

建议新增主表: `tms_counterparty_default_bank_account_rule_t`。

命名理由:

- `tms_` 前缀符合 Open-TMS 规范;
- `counterparty_default_bank_account_rule` 表达交易对手锚点规则;
- `_t` 表示主表;
- 不复用 `tms_default_bank_account_rule_t`,避免管理主体锚点与交易对手锚点语义混淆。

### 4.2 主表字段清单(24 个存储字段)

| # | 字段名 | 类型 | 必填 | 默认值 | 说明 |
|---|--------|------|------|--------|------|
| 1 | `id` | BIGSERIAL | Y | 自增 | 主键 |
| 2 | `rule_number` | VARCHAR(50) | Y | 系统生成 | 规则编号,建议 `CDBARyyyyMMdd0001`;参考 v1.1 default-bank-account-rule 的 `rule_number` 字段 |
| 3 | `counterparty_id` | BIGINT | Y | - | 交易对手 ID,FK → `tms_counterparty_t.id`;主锚点 |
| 4 | `counterparty_account_id` | BIGINT | N | NULL | 对手方具体账户;有值时精确度高于仅指定 counterparty |
| 5 | `instrument_id` | BIGINT | N | NULL | 金融工具 ID;NULL 表示 ALL;参考 v1.1 default-bank-account-rule 的 `instrument_id` 字段 |
| 6 | `currency` | VARCHAR(10) | N | NULL | ISO 4217 币种;NULL 表示 ALL;参考 v1.1 default-bank-account-rule 的 `currency` 字段 |
| 7 | `direction` | VARCHAR(20) | Y | `ALL` | Inflow / Outflow / ALL;参考 v1.1 default-bank-account-rule 的 `direction` 字段 |
| 8 | `dual_direction` | BOOLEAN | Y | false | true 表示一条规则同时覆盖 Inflow 与 Outflow |
| 9 | `bank_account_id` | BIGINT | Y | - | 我方默认银行账户 ID;参考 v1.1 default-bank-account-rule 的 `bank_account_id` 字段 |
| 10 | `priority` | INT | Y | 0 | 优先级 0-9999,数值越大越优先;参考 v1.1 default-bank-account-rule 的 `priority` 字段 |
| 11 | `status` | VARCHAR(20) | Y | `Active` | Active / Inactive;参考 v1.1 default-bank-account-rule 的 `status` 字段 |
| 12 | `start_date` | DATE | N | NULL | 开始生效日;NULL 表示立即生效;参考 v1.1 default-bank-account-rule 的 `start_date` 字段 |
| 13 | `end_date` | DATE | N | NULL | 结束生效日;NULL 表示长期有效 |
| 14 | `description` | VARCHAR(500) | N | NULL | 业务说明;参考 v1.1 default-bank-account-rule 的 `description` 字段 |
| 15 | `remark` | VARCHAR(500) | N | NULL | 内部备注;参考 v1.1 default-bank-account-rule 的 `remark` 字段 |
| 16 | `lock_token` | VARCHAR(64) | N | UUID | 并发控制 token;参考 v1.1 default-bank-account-rule 的 `lock_token` 字段 |
| 17 | `locked_by` | VARCHAR(50) | N | NULL | 锁定人;参考 v1.1 default-bank-account-rule 的 `locked_by` 字段 |
| 18 | `locked_at` | TIMESTAMP | N | NULL | 锁定时间;参考 v1.1 default-bank-account-rule 的 `locked_at` 字段 |
| 19 | `created_by` | VARCHAR(50) | Y | `system` | 创建人,审计字段必备 |
| 20 | `created_at` | TIMESTAMP | Y | CURRENT_TIMESTAMP | 创建时间,审计字段必备 |
| 21 | `updated_by` | VARCHAR(50) | N | NULL | 更新人,审计字段必备 |
| 22 | `updated_at` | TIMESTAMP | N | NULL | 更新时间,审计字段必备 |
| 23 | `version` | INT | Y | 0 | 乐观锁版本;参考 v1.1 default-bank-account-rule 的 `version` 字段 |
| 24 | `deleted` | CHAR(1) | Y | `0` | 逻辑删除;参考 v1.1 default-bank-account-rule 的 `deleted` 字段 |

### 4.3 VO 展示字段

| 字段 | 类型 | 说明 |
|------|------|------|
| `counterpartyName` | String | 对手方名称 |
| `counterpartyAccountName` | String | 对手方账户名称或账号摘要 |
| `instrumentName` | String | 金融工具名称 |
| `bankAccountName` | String | 我方银行账户名称 |
| `specificityScore` | Integer | test-match 返回,表示精确度得分 |
| `matchedDimensions` | Array/String | test-match 返回,说明命中的维度 |
| `ruleSource` | String | `COUNTERPARTY_DEFAULT_RULE`,用于与 v1.1 结果区分 |

### 4.4 DTO 建议

参考 v1.1 default-bank-account-rule 的 4 个 DTO:

| DTO | 用途 | 关键字段 |
|-----|------|----------|
| `CounterpartyDefaultBankAccountRuleQueryDTO` | 分页查询 | counterpartyId / counterpartyAccountId / instrumentId / direction / currency / status / keyword / pageNum / pageSize |
| `CounterpartyDefaultBankAccountRuleSaveDTO` | 新增 | 业务字段,不含 id 和审计字段 |
| `CounterpartyDefaultBankAccountRuleUpdateDTO` | 更新 | id / lockToken / 业务字段 / version |
| `CounterpartyDefaultBankAccountRuleMatchRequestDTO` | match/test-match | counterpartyId / counterpartyAccountId / instrumentId / direction / currency / dualDirection / managementEntityId(可选) |

### 4.5 唯一约束

Active 状态下,同一维度组合只能存在一条未删除规则。

建议唯一约束逻辑:

```sql
UNIQUE (
  counterparty_id,
  counterparty_account_id,
  instrument_id,
  currency,
  direction,
  status
)
NULLS NOT DISTINCT
WHERE deleted = '0' AND status = 'Active'
```

说明:

- `counterparty_account_id` / `instrument_id` / `currency` 为 NULL 时表示 ALL;
- PostgreSQL 使用 `NULLS NOT DISTINCT`,确保 NULL 与 NULL 视为相同通配维度;
- `dual_direction=true` 时建议将 `direction` 归一为 `ALL`;
- 软删除和 Inactive 不参与 Active 唯一约束。

---

## 5. 业务规则

### 5.1 状态与生效期

规则参与匹配必须同时满足:

- `status='Active'`;
- `deleted='0'`;
- `start_date IS NULL OR start_date <= today`;
- `end_date IS NULL OR end_date >= today`。

Phase 1 不新增审批状态。

### 5.2 字段校验

| 字段 | 校验规则 | 错误信息 |
|------|----------|----------|
| `counterparty_id` | 必填 | 交易对手必填 |
| `bank_account_id` | 必填 | 默认账户必填 |
| `direction` | Inflow / Outflow / ALL | 方向必须为 Inflow / Outflow / ALL |
| `dual_direction` | true 时 direction 归一为 ALL | 双方向规则方向必须为 ALL |
| `priority` | 0-9999 | 优先级超出范围 0-9999 |
| `status` | Active / Inactive | 状态非法 |
| `start_date/end_date` | end_date >= start_date | 结束日期不能早于开始日期 |
| `currency` | 非空时为有效币种 | 币种不存在或已停用 |
| `counterparty_account_id` | 必须属于 counterparty_id | 对手方账户不属于该交易对手 |

### 5.3 单链匹配过滤

交易对手规则链路候选过滤:

1. `counterparty_id = req.counterpartyId`;
2. 若请求有 `counterpartyAccountId`,候选可以是:
   - `counterparty_account_id = req.counterpartyAccountId`;
   - 或 `counterparty_account_id IS NULL` 泛化规则;
3. 若请求有 `instrumentId`,候选可以是:
   - `instrument_id = req.instrumentId`;
   - 或 `instrument_id IS NULL` 泛化规则;
4. 若请求有 `currency`,候选可以是:
   - `currency = req.currency`;
   - 或 `currency IS NULL` 泛化规则;
5. 方向过滤:
   - `direction=req.direction` 精确匹配;
   - `direction='ALL'` 匹配 Inflow 和 Outflow;
   - `dual_direction=true` 同时匹配 Inflow 和 Outflow。

### 5.4 匹配优先级

排序规则:

**specificityScore DESC → priority DESC → start_date DESC → created_at ASC → id ASC**。

specificityScore 建议:

| 维度 | 得分 | 说明 |
|------|------|------|
| counterparty_account_id 精确 | +1000 | 最高优先级 |
| instrument_id 精确 | +100 | 产品精确优先于产品通配 |
| currency 精确 | +50 | 币种精确优先于币种通配 |
| direction 精确 | +20 | 单方向精确优先于 ALL/dualDirection |
| dual_direction / ALL | +5 | 可命中,但精确度低于单方向 |

关键规则:

- 指定 `counterparty_account_id` 的规则一定优先于只指定 `counterparty_id` 的规则;
- 精确度高的规则优先于 priority 高但更泛化的规则;
- priority 只在同一精确度层级内决胜;
- 若仍相同,按 created_at / id 稳定排序,避免结果抖动。

### 5.5 双方向规则

当 `dual_direction=true`:

- 一条规则同时覆盖 Inflow 与 Outflow;
- 存储层建议将 `direction` 归一为 `ALL`;
- 单方向 match 请求可命中该规则;
- `dualDirection=true` 的 match 请求返回 `inflow` 和 `outflow` 两个结果;
- 两个方向可能命中同一条规则,也可能因存在更精确单方向规则而不同。

示例:

- 规则 A: HSBC + USD + dualDirection=true + priority=100 → 默认账户 1;
- 规则 B: HSBC + USD + direction=Outflow + priority=10 → 默认账户 2;
- Inflow 命中 A;
- Outflow 因 direction 更精确,命中 B。

### 5.6 与现有 v1.1 default-bank-account-rule 联动

交易录入或交易保存前,系统可运行两条链路:

1. **管理主体锚点链路**  
   调用 v1.1 default-bank-account-rule,以 `managementEntityId` 为必填锚点。

2. **交易对手锚点链路**  
   调用本特性,以 `counterpartyId` 为必填锚点。

统一决胜逻辑:

| 步骤 | 规则 |
|------|------|
| 1 | 两条链路分别返回本链路最优候选和候选列表 |
| 2 | 统一包装为 `DefaultBankAccountCandidate` |
| 3 | 计算统一 specificityScore |
| 4 | 按 specificityScore DESC 排序 |
| 5 | specificityScore 相同再按 priority DESC 排序 |
| 6 | priority 相同再按 ruleSourceWeight、createdAt、id 稳定排序 |

统一 specificity 建议:

| 来源 | 维度 | 得分建议 |
|------|------|----------|
| 本特性 | counterparty_account_id 精确 | +1000 |
| v1.1 | managementEntityId 锚点 | +300 |
| 两者 | counterparty_id 精确 | +200 |
| 两者 | instrument_id 精确 | +100 |
| 两者 | currency 精确 | +50 |
| 两者 | direction 精确 | +20 |
| 两者 | ALL / dualDirection | +5 |

说明:

- v1.1 规则天然有 `managementEntityId`,但主体级泛化规则不应压过本特性的对手方账户级规则;
- 本特性的 `counterparty_account_id` 是更细粒度维度,优先级最高;
- 若两条链路精确度相同,可通过 priority 控制业务偏好;
- 返回结果必须包含 `ruleSource`。

### 5.7 软删除与唯一约束冲突

软删除:

- 删除操作只设置 `deleted='1'`;
- 清空或刷新 `lock_token`;
- 写入 DELETE 审计日志;
- 已引用该规则的历史交易不回写、不清空规则引用。

唯一约束冲突错误信息:

- 新增冲突: `已存在相同维度的启用规则: counterparty=HSBC, account=ALL, instrument=ALL, currency=USD, direction=ALL`;
- 启用冲突: `启用失败:存在相同维度的 Active 规则,请先停用或调整原规则`;
- 更新冲突: `保存失败:Active 规则维度重复,请调整币种/方向/产品或停用重复规则`。

### 5.8 审计与被引用数

审计操作参考 v1.1 default-bank-account-rule 的 RuleAuditLog:

| 操作 | 触发时机 | 审计内容 |
|------|----------|----------|
| CREATE | 新增规则 | new_value JSONB |
| UPDATE | 更新规则 | old_value + new_value JSONB |
| DELETE | 软删除规则 | old_value JSONB |
| ENABLE | 启用规则 | old_value + new_value JSONB |
| DISABLE | 停用规则 | old_value + new_value JSONB |

审计表建议:

- 优先新增专用表 `tms_counterparty_rule_audit_log_t`;
- 若复用 `tms_rule_audit_log_t`,必须新增 `rule_type`,避免不同规则表的 `rule_id` 冲突;
- Phase 1 推荐专用表,降低对 v1.1 的影响。

被引用数定义:

- 交易录入时系统命中本规则;
- 前端自动带出 `bankAccountId`;
- 用户保存交易时未手动覆盖该账户;
- 交易或现金流记录保存 `default_rule_source='COUNTERPARTY'` 与 `default_rule_id=<ruleId>`。

---

## 6. API 端点清单

基础路径: `/api/v1/counterparty-default-bank-account-rules`。

响应结构统一为 `Result<T> = {code, message, data, timestamp}`。

写操作沿用 Open-TMS 规范:update/delete 使用 POST。

### 6.1 11 个端点

| # | 方法 | Path | 说明 | 参考 v1.1 |
|---|------|------|------|----------|
| 1 | POST | `/api/v1/counterparty-default-bank-account-rules/page` | 分页查询 | 参考 v1.1 default-bank-account-rule 的 `/page` |
| 2 | GET | `/api/v1/counterparty-default-bank-account-rules/{id}` | 详情,id 或 ruleNumber | 参考 v1.1 default-bank-account-rule 的 `getById(String id)` |
| 3 | POST | `/api/v1/counterparty-default-bank-account-rules` | 新增规则 | 参考 v1.1 default-bank-account-rule 的 save |
| 4 | POST | `/api/v1/counterparty-default-bank-account-rules/update` | 更新规则,校验 lockToken | 参考 v1.1 default-bank-account-rule 的 update |
| 5 | POST | `/api/v1/counterparty-default-bank-account-rules/delete/{id}` | 软删除 | 参考 v1.1 default-bank-account-rule 的 delete |
| 6 | POST | `/api/v1/counterparty-default-bank-account-rules/{id}/enable` | 启用 | 参考 v1.1 default-bank-account-rule 的 enable |
| 7 | POST | `/api/v1/counterparty-default-bank-account-rules/{id}/disable` | 停用 | 参考 v1.1 default-bank-account-rule 的 disable |
| 8 | GET | `/api/v1/counterparty-default-bank-account-rules/match` | 运行时匹配,基于 counterpartyId | 参考 v1.1 default-bank-account-rule 的 match |
| 9 | GET | `/api/v1/counterparty-default-bank-account-rules/test-match` | 测试匹配,返回所有候选 | 参考 v1.1 default-bank-account-rule 的 test-match |
| 10 | GET | `/api/v1/counterparty-default-bank-account-rules/{id}/audit-logs` | 审计日志 | 参考 v1.1 default-bank-account-rule 的 audit-logs |
| 11 | GET | `/api/v1/counterparty-default-bank-account-rules/{id}/reference-count` | 被引用数 | 参考 v1.1 default-bank-account-rule 的 reference-count |

### 6.2 match 请求参数

```http
GET /api/v1/counterparty-default-bank-account-rules/match
  ?counterpartyId=1001
  &counterpartyAccountId=2001
  &instrumentId=301
  &direction=Inflow
  &currency=USD
  &dualDirection=false
  &managementEntityId=10
```

| 参数 | 必填 | 说明 |
|------|------|------|
| `counterpartyId` | Y | 对手方锚点 |
| `counterpartyAccountId` | N | 对手方具体账户,有值时精确度最高 |
| `instrumentId` | N | 金融工具 |
| `direction` | N | Inflow / Outflow;dualDirection=false 时建议必传 |
| `currency` | N | 交易币种 |
| `dualDirection` | N | 是否同时返回收/付两个方向 |
| `managementEntityId` | N | 不作为本规则表维度,但可用于账户归属校验和联动解释 |

### 6.3 match 响应示例

```json
{
  "matched": true,
  "bankAccountId": 9001,
  "bankAccountName": "HSBC HK USD Operating Account",
  "ruleId": 101,
  "ruleNumber": "CDBAR202607100001",
  "ruleSource": "COUNTERPARTY_DEFAULT_RULE",
  "priority": 500,
  "specificityScore": 1070,
  "matchedDimensions": ["counterparty", "counterpartyAccount", "currency", "direction"],
  "overridable": true
}
```

### 6.4 dualDirection=true 响应示例

```json
{
  "inflow": {
    "matched": true,
    "bankAccountId": 9001,
    "ruleNumber": "CDBAR202607100001",
    "specificityScore": 1055
  },
  "outflow": {
    "matched": true,
    "bankAccountId": 9002,
    "ruleNumber": "CDBAR202607100002",
    "specificityScore": 1070
  },
  "cacheHit": false,
  "queryDurationMs": 18
}
```

### 6.5 错误码

| code | 场景 | message 示例 |
|------|------|--------------|
| 200 | 成功 | success |
| 400 | 参数错误 | 交易对手必填 |
| 400 | 唯一冲突 | 已存在相同维度的启用规则 |
| 404 | 资源不存在 | 规则不存在: CDBAR202607100001 |
| 409 | 并发冲突 | 规则已被他人修改,请刷新后重试 |
| 500 | 系统异常 | 系统异常,请稍后重试 |

---


## 7. 对标业界

| 业界系统 | 对标能力 | 对本特性的启示 |
|----------|----------|----------------|
| FIS Quantum | Counterparty Settlement Instructions(CSI) | 按 counterparty / currency / product / direction 管理结算指令,Open-TMS 以规则表实现轻量 CSI |
| Murex MX.3 | Settlement Instruction Matrix | 使用矩阵匹配交易要素并自动带出结算账户,Open-TMS 采用 specificityScore + priority 决胜 |
| SAP TRM | Bank Account Determination by Business Partner | Business Partner 维度可决定本方银行账户,Open-TMS 对应 counterparty 锚点规则 |
| Kyriba | Counterparty Bank Account Mapping | 通过交易对手与银行账户映射降低支付失败率,Open-TMS 保留人工覆盖能力 |

行业共性:

- 默认结算账户通常由主体、对手方、产品、币种、方向共同决定;
- 账户级 SSI/CSI 优先于对手方级泛化规则;
- 规则命中必须可解释;
- 自动默认不等于强制执行;
- 规则变更需要完整审计。

---

## 8. 验收标准

### 8.1 功能验收

| # | 功能 | 验收标准 |
|---|------|----------|
| AC-1 | 新增规则 | counterpartyId、bankAccountId、priority、status 合法时保存成功,返回 ruleNumber 与 lockToken |
| AC-2 | 分页查询 | 支持 counterpartyId / counterpartyAccountId / instrumentId / currency / direction / status / keyword 查询 |
| AC-3 | 更新规则 | 携带正确 lockToken 更新成功,返回新 lockToken |
| AC-4 | 并发冲突 | 旧 lockToken 更新返回 code=409,数据不被覆盖 |
| AC-5 | 软删除 | 删除后 `deleted='1'`,列表默认不可见,写 DELETE 审计日志 |
| AC-6 | 启用/停用 | 状态在 Active / Inactive 切换,写 ENABLE / DISABLE 审计日志 |
| AC-7 | 唯一约束 | 相同维度 Active 规则重复新增/启用失败,错误信息可读 |
| AC-8 | 单方向匹配 | Inflow/Outflow 请求能命中对应方向或 dualDirection 规则 |
| AC-9 | 双方向匹配 | `dualDirection=true` 返回 inflow 与 outflow 两个结果 |
| AC-10 | 精确度优先 | counterpartyAccount 级规则优先于 counterparty 级规则,即使后者 priority 更高 |
| AC-11 | v1.1 联动 | 与管理主体规则同时存在时,最终账户按统一 specificity + priority 决胜 |
| AC-12 | test-match | 返回所有候选、specificityScore、priority、排序、最终胜出标记 |
| AC-13 | 审计日志 | CREATE/UPDATE/DELETE/ENABLE/DISABLE 均可查询 JSON 快照 |
| AC-14 | 被引用数 | 删除前能返回 totalCount,前端能展示被 N 笔交易引用提示 |
| AC-15 | 人工覆盖 | 交易页自动带出账户后,交易员可手动改账户并保存 |

### 8.2 性能验收

| # | 指标 | 目标 |
|---|------|------|
| PERF-1 | 规则数量 | 支持 50,000 条总规则、10,000 条 Active 规则 |
| PERF-2 | match 延迟 | 单次 match P95 <= 50ms(命中索引,不含网络) |
| PERF-3 | 缓存命中 | 缓存命中时 P95 <= 10ms |
| PERF-4 | test-match 延迟 | 返回 Top 50 候选 P95 <= 100ms |
| PERF-5 | 分页查询 | 20 条/页 P95 <= 200ms |
| PERF-6 | 审计查询 | 单规则 1,000 条审计内分页 P95 <= 200ms |
| PERF-7 | 并发编辑 | 20 个并发更新同一规则时只允许 1 个成功,其他返回 409 或重试失败 |

### 8.3 数据与异常验收

| # | 数据/异常 | 验收标准 |
|---|-----------|----------|
| DATA-1 | 审计字段 | 主表包含 created_by / created_at / updated_by / updated_at / version / deleted |
| DATA-2 | 软删除 | deleted='1' 数据不参与 match 与唯一约束 |
| DATA-3 | 生效期 | 未到 start_date 或超过 end_date 不参与 match |
| DATA-4 | 对手方账户归属 | counterpartyAccountId 必须属于 counterpartyId |
| ERR-1 | counterpartyId 为空 | code=400,message=交易对手必填 |
| ERR-2 | bankAccountId 为空 | code=400,message=默认账户必填 |
| ERR-3 | priority 越界 | code=400,message=优先级超出范围 0-9999 |
| ERR-4 | direction 非法 | code=400,message=方向必须为 Inflow / Outflow / ALL |
| ERR-5 | match 未命中 | code=200,matched=false,bankAccountId=null,不阻断交易 |
| ERR-6 | lockToken 过期 | code=409,message=规则已被他人修改 |

---

## 9. 风险与依赖

### 9.1 枚举依赖

Phase 1 不强制新增枚举。

建议复用:

- status: `Active` / `Inactive`,参考 v1.1 default-bank-account-rule 的 `status` 字段;
- direction: `Inflow` / `Outflow` / `ALL`,参考 v1.1 default-bank-account-rule 的 `direction` 字段;
- audit operation: `CREATE` / `UPDATE` / `DELETE` / `ENABLE` / `DISABLE`,参考 v1.1 RuleAuditOperation。

如后续需要集中治理,可新增:

- `DefaultAccountRuleSource` = `MANAGEMENT_ENTITY` / `COUNTERPARTY`;
- `CDBARStatus` 不建议新增,除非决定不复用 Active/Inactive。

### 9.2 主要依赖与风险

| 依赖 | 风险 | 缓解 |
|------|------|------|
| CounterpartyAccount 主数据 | 数据不完整影响账户级规则 | counterparty_account_id 可选,先支持 counterparty 级规则 |
| BankAccount 主数据 | 账户币种或状态不准确 | 保存规则时校验账户币种与状态 |
| v1.1 default-bank-account-rule | 联动排序需要统一候选模型 | 定义 `DefaultBankAccountCandidate` 或在 dealing 层统一计算 |
| 交易表引用字段 | 被引用数需要规则来源 | Phase 5+6 补充 defaultRuleSource/defaultRuleId 或操作日志 |
| 缓存一致性 | 规则更新后旧缓存可能误命中 | 写操作后清理匹配缓存 |
| PostgreSQL NULL 语义 | NULL 默认不相等 | 使用 `NULLS NOT DISTINCT` 部分唯一索引 |
| 多规则冲突 | 用户难以理解命中结果 | test-match 展示候选排序和命中原因 |

### 9.3 待确认事项

| # | 事项 | 建议 |
|---|------|------|
| Q1 | bankAccountId 是否必须属于交易 managementEntity | match 时可用 managementEntityId 校验,规则表本身不以 managementEntity 为锚点 |
| Q2 | 被引用规则删除是否硬阻断 | Phase 1 仅强提示,不硬阻断 |
| Q3 | 审计表复用还是新建 | 建议新建专用审计表,避免 rule_id 冲突 |
| Q4 | priority 与 specificity 顺序 | 本 PRD 明确 specificity 优先,priority 次之 |
| Q5 | 是否需要规则审批 | Phase 1 不需要,仅操作审计;如监管要求可 P2 接入审批 |

---

## 10. 与 v1.1 default-bank-account-rule 的主要差异

| # | 维度 | v1.1 管理主体默认规则 | 本特性交易对手默认规则 |
|---|------|------------------------|--------------------------|
| 1 | 主锚点 | `management_entity_id` 必填 | `counterparty_id` 必填 |
| 2 | 细分维度 | 无 `counterparty_account_id` | 新增 `counterparty_account_id`,账户级精确度最高 |
| 3 | 生效期 | 参考 v1.1 default-bank-account-rule 的 `start_date` 字段,无 end_date | 新增 `end_date`,支持临时结算安排 |
| 4 | 排序重点 | 现实现主要按 priority 排序 | 明确 specificityScore 优先,priority 次之 |
| 5 | 联动角色 | 给定管理主体后推荐账户 | 给定交易对手/对手方账户后推荐账户,并与 v1.1 链路共同决胜 |
| 6 | 业务对标 | 更像内部主体结算偏好 | 更像 CSI / SSI / Settlement Instruction Matrix |
| 7 | 审计表建议 | 已有 `tms_rule_audit_log_t` | 建议专用审计表或补 `rule_type`,避免 rule_id 冲突 |

---

## 11. 后续 Phase 计划

| Phase | 目标 | 一行计划 |
|-------|------|----------|
| Phase 2 UX | 页面与交互设计 | 输出列表页、编辑弹窗、test-match 抽屉、交易录入带出提示的 UX 原型 |
| Phase 3+4 设计 | DB + API 设计 | 输出 DDL、Entity/DTO/VO/API 文档,并完成 DB/API 审核 |
| Phase 5+6 开发 | 后端 + 前端开发 | 实现 11 端点、匹配算法、前端列表页、交易页联动与引用记录 |
| Phase 7+8 测试 | 用例 + 执行 | 设计并执行 API/UI/并发/性能/回归测试,覆盖 v1.1 联动场景 |
| Phase 9 复审 | 6 维复审 | 需求/UX/DB/API/后端/前端/测试联合复审,无 P0 后交付 |

---

## 12. 相关文档

- `basedata/src/main/java/com/opentms/basedata/controller/DefaultBankAccountRuleResource.java` - v1.1 11 端点参考
- `basedata/src/main/java/com/opentms/basedata/service/impl/DefaultBankAccountRuleServiceImpl.java` - v1.1 match、lockToken、审计、缓存参考
- `basedata/src/main/java/com/opentms/basedata/entity/DefaultBankAccountRule.java` - v1.1 主表字段参考
- `basedata/src/main/java/com/opentms/basedata/entity/RuleAuditLog.java` - v1.1 审计日志参考
- `db/schema/28-default-bank-account-rule-v1.1.sql` - v1.1 DDL 参考
- `web/src/views/basedata/DefaultBankAccountRuleList.vue` - v1.1 前端页面参考
- `web/src/api/basedata/defaultBankAccountRule.js` - v1.1 前端 API 参考
- `docs/prd/M3/M3-外汇交易PRD.md` - M3 PRD 格式与交易上下文参考
- `docs/prd/M3/M3-NDF-Rate-Fix设计评审.md` - 最近设计评审格式参考

---

*PM Phase 1 产出 - 2026-07-10*  
*本 PRD 仅定义交易对手默认银行账户规则需求,不修改源码/数据库/配置。*

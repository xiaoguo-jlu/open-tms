# Open-TMS M3-现金流增强 + Audit History 合并 PRD

**版本**: v1.0
**角色**: 产品经理(PM)
**日期**: 2026-07-11
**模块归属**: M3 / dealing(交易) + basedata(规则调用方)
**特性名称**: 现金流银行账户自动填充 + AC/AT/FX 审计历史视图
**前置依赖**: v1.1 默认银行账户规则(commit `3e3604f`)、`tms_at_deals_image_t` 镜像表(commit `954a4b5`)
**状态**: Phase 1 PRD 草稿,待需求评审

---

## 0. 修订记录

| 版本 | 日期 | 作者 | 变更内容 |
|------|------|------|----------|
| v1.0 | 2026-07-11 | PM | 首版合并 PRD:需求 1 现金流银行账户自动填充 + 镜像;需求 2 AC/AT/FX 审计历史视图 |

---

## 1. 背景与动机

### 1.1 监管合规与操作追溯

Open-TMS 已完成 v1.1 默认银行账户规则(`tms_default_bank_account_rule_t`)和 NDF Rate Fix,交易主表镜像(`tms_deals_image_t` / `tms_ac_deals_image_t` / `tms_at_deals_image_t` / `tms_fx_deals_image_t`)已落地,但**现金流(`tms_cashflow_t`)层尚缺两个能力**:

1. **银行账户追溯** — 监管检查要求每一条现金流必须能反查到"我方银行账户"与"对手方银行账户",目前现金流表只存了 `bank_account` / `counterparty_account` 两个**字符串**,无法准确定位银行账户主数据。
2. **历史镜像** — 当前只有交易主表(`deal` + `ac/at/fx_deals`)的镜像,现金流(交易发生后的资金收/付记录)没有任何镜像。监管合规要求"任意一笔历史资金流必须能完整还原当时的对手方、银行账户、币种、金额、汇率"。

### 1.2 双方向现金流匹配(Murex MX.3 模型)

v1.1 规则已实现**双方向(dualDirection)** —— 一条规则可同时覆盖 Inflow 与 Outflow。但目前规则命中只回填到 `tms_ac_deals_t`,**没有自动下沉到 `tms_cashflow_t`**。这导致:

- FX 拆 BUY/SELL 两条 cashflow 时,需要手工选择 bank_account;
- NDF Rate Fix 生成的 settlement cashflow(2026-07 新增)没有任何默认账户;
- 同一管理主体在两个币种方向上的现金流入/流出无规律。

### 1.3 审计历史(Audit History)

treasury 团队反馈:AC/AT/FX 交易频繁修改(`Approved → Settled` 链路会经历 4-6 次 UPDATE),但**当前前端 UI 没有任何"我昨天改了什么"的入口**。审计、风控、合规部门当前必须人工问"这个数字昨天是 X 还是 Y?"

业界标准做法(**FIS Quantum / Murex MX.3 / SAP TRM**):交易详情页提供 **"Audit History"** 按钮 → 弹窗列出所有历史版本(版本号、操作人、时间、Action 类型) → 选中某版本跳到该版本的镜像只读详情。

### 1.4 目标

| 目标 | 度量 |
|------|------|
| G1 现金流可追溯 | 90% 以上历史现金流的"我方银行账户 + 对手方银行账户"由规则自动填充,追溯查询 P95 < 200ms |
| G2 双方向覆盖 | v1.1 dualDirection 规则命中后,Inflow/Outflow 双向 cashflow 一次性带出银行账户 |
| G3 现金流镜像 | 100% AC/AT/FX 触发的 cashflow 增删改留痕,镜像查询 P95 < 300ms |
| G4 审计历史可见 | 财资人员点击交易详情页 Audit History 按钮 ≤ 2 跳到达任一历史版本详情页 |

### 1.5 设计原则

| 原则 | 说明 |
|------|------|
| **透明下沉** | 银行账户填充对前端透明:`CashflowVO.bankAccountId` / `counterpartyBankAccountId` 由服务端内部匹配 |
| **降级不阻断** | 规则未命中(返回 null)→ cashflow 仍可保存,`bank_account_id = null`,前端保留手动选账户能力 |
| **永不过期镜像** | `tms_cashflows_image_t` 永久保存(监管要求 7 年,先按"永不过期"实现,后续可加归档表) |
| **只读镜像** | 不基于镜像做时间旅行回滚,只读用途 |
| **合并 vs 拆分** | 见第 10 章推进建议,推荐**合并推进** |

---

## 2. 范围(In / Out of Scope)

### 2.1 In Scope

| 编号 | 功能 | 优先级 | 说明 |
|------|------|--------|------|
| IN-1 | cashflow 表新增 bank_account_id + counterparty_bank_account_id 2 字段 | P0 | BIGINT,可空(降级时为 null) |
| IN-2 | 交易 create/update 触发 cashflow 银行账户自动填充(v1.1 规则调用) | P0 | 仅 AC/AT/FX 创建/更新,审批/驳回不触发 |
| IN-3 | NDF Rate Fix 生成 settlement cashflow 时同样调用 v1.1 规则 | P0 | 复用现有 v1.1 match 端点 |
| IN-4 | cashflow 增删改镜像表 `tms_cashflows_image_t` | P0 | UPDATE 写改前快照,DELETE 写删除前内容,CREATE 写创建后内容 |
| IN-5 | `tms_cashflows_image_t` 建表 SQL | P0 | 沿用 `tms_at_deals_image_t` 结构 + cashflow 业务字段 |
| IN-6 | `GET /api/v1/dealing/deals/{dealNumber}/versions` 列表端点 | P0 | 返回所有版本的 deal_image + deal_type + version + operate_at + operator + image_type |
| IN-7 | `GET /api/v1/dealing/deals/{dealNumber}/versions/{version}` 详情端点 | P0 | 返回 deal_image + ac/at/fx_deal_image + cashflow_image 拼装后的视图 |
| IN-8 | AC/AT/FX 详情页加"审计历史"按钮 | P0 | 跳 dialog,显示版本列表 |
| IN-9 | 新增 `/dealing/{dealType}/audit-history?dealNumber=X&version=Y` 详情页 | P0 | 复用 BaseDataPicker + ModeBadge(readonly 模式) |
| IN-10 | 审计列表分页 + 索引 | P0 | 列表默认分页 20/页,`idx_cashflow_image_deal_version` 联合索引 |
| IN-11 | 镜像数据只读 | P0 | 不做回滚、不做跨版本 diff 自动化 |
| IN-12 | 并发编辑冲突提示与审计历史联动 | P1 | US-7:基于 lockToken 409 冲突,提示刷新后查看最新版本 |

### 2.2 Out of Scope

| 编号 | 不做项 | 说明 |
|------|--------|------|
| OUT-1 | 不基于镜像做 diff 自动化 | 人工对比,首版 UI 只展示拼接镜像数据 |
| OUT-2 | 不做镜像回滚 | 只读,避免误操作污染历史 |
| OUT-3 | 不修改 cashflow 已存的字符串字段 | `bank_account` / `counterparty_account` 字符串字段继续保留(对外展示),`bank_account_id` 仅用于关联主数据 |
| OUT-4 | 不做跨模块自动清算 | 自动填充只到 cashflow 字段,不再调银企直连 |
| OUT-5 | 不做镜像归档/清理策略 | 永不过期(v1.0 决策,见 §5.3);后续 v1.1 可加 archive job |
| OUT-6 | 暂不做 CSL/CDS 级别的合规审计报告 | 仅展示版本列表,合规报告 v2.0 再说 |
| OUT-7 | 不做 cashflow 的 lockToken | cashflow 没有并发编辑场景,v1.0 暂不加 |

---

## 3. 用户故事(Given-When-Then)

### US-1: 创建 AC 交易,自动填充我方 + 对手方银行账号

**作为** 交易员  
**我希望** 创建 AC 交易时,系统自动按 v1.1 默认银行账户规则匹配双方银行账号并写入现金流  
**以便** 我不需要手工选择现金流的银行账户,降低录入错误。

**Given** 系统存在 v1.1 规则:managementEntity=A、counterparty=B、currency=USD、dualDirection=true、bankAccount=ACC_USD_01  
**When** 交易员创建 AC 交易,选择 managementEntity=A、counterparty=B、currency=USD、direction=Outflow,银行账户字段**留空**  
**Then** 服务端调用 v1.1 `GET /api/v1/default-bank-account-rules/match?...&direction=Outflow&currency=USD&dualDirection=true`  
**And** 命中规则 → 自动填充 `tms_ac_deals_t.bank_account_id = ACC_USD_01.id`  
**And** AC 交易执行的 cashflow insert 时,同时填充 `tms_cashflow_t.bank_account_id = ACC_USD_01.id`、`counterparty_bank_account_id = 对手方默认账户.id`  
**And** 前端详情页只看到 `*Name` 已补全。

### US-2: 修改 AC 交易触发改银行账号 → 调规则重新匹配

**作为** 交易员  
**我希望** 修改 AC 交易的对手方或币种时,银行账户自动重新匹配  
**以便** 不需要清空再选字段。

**Given** AC 交易原本 counterparty=B、currency=USD  
**When** 交易员更新 AC 交易为 counterparty=C、currency=EUR  
**Then** 服务端在 UPDATE 流程中再次调用 v1.1 match 端点  
**And** 若命中新的 EUR 规则 → 写新的 `tms_ac_deals_t.bank_account_id`  
**And** 同步 DELETE 已存在的 cashflow + INSERT 新 cashflow,**两条 cashflow 镜像记录改前/改后**  
**And** 新 cashflow 的 `bank_account_id` 是新规则命中的账户。

### US-3: 删除现金流 → 镜像表记录删除前内容

**作为** 合规人员  
**我希望** 即便 cashflow 被删除,历史镜像仍然保留  
**以便** 审计回查时能看到删除前的完整内容。

**Given** 已有 AC 交易,生成 1 条 cashflow(cflowNumber=CF...)  
**When** 交易员在编辑场景触发 cashflow 删除(软删,`deleted='1'`)  
**Then** 系统在删除前先写一条 `tms_cashflows_image_t`,`image_type='DELETE'`,字段快照全保留(包括 `bank_account_id` / `counterparty_bank_account_id`)  
**And** cashflow 主表标记 `deleted='1'`  
**And** 审计历史页面能查到这条 DELETE 镜像。

### US-4: NDF Rate Fix 后,新增 1 条现金流自动填充

**作为** 交易员  
**我希望** NDF Rate Fix 触发的 settlement cashflow 也有自动银行账户  
**以便** settlement cashflow 不需要手工选账户。

**Given** FX 交易 NDF 类型已 Approved  
**When** 财资人员调用 `POST /api/v1/fx-deals/{id}/rate-fix` 提交 rate_fix  
**Then** 后端生成 1 条 FX_FIX DealMap + 1 条 settlement cashflow  
**And** 调 v1.1 match 端点(基于 managementEntity + counterparty + fixCurrency)  
**And** 命中规则 → settlement cashflow 自动填充 `bank_account_id`  
**And** 镜像表写 INSERT。

### US-5: 财资经理在 AC 详情页点"审计历史" → 看到 5 个历史版本

**作为** 财资经理  
**我希望** 在 AC 详情页直接看历史版本列表  
**以便** 快速回答"这笔交易前天是多少"。

**Given** AC 交易 `DEAL20260710-0001` 已经历 4 次 UPDATE(v1=Created, v2=Updated by Alice, v3=Updated by Bob, v4=RATE_FIX, v5=Submit)  
**When** 财资经理在 AC 详情页点 "审计历史" 按钮  
**Then** 弹出 dialog,显示 5 行版本清单:

```
| 版本 | 操作人 | 时间                | 操作类型  | 状态   |
|------|--------|---------------------|-----------|--------|
| v5   | Alice  | 2026-07-10 14:30:00 | UPDATE    | New    |
| v4   | Bob    | 2026-07-10 10:15:00 | UPDATE    | New    |
| v3   | Alice  | 2026-07-09 16:42:00 | UPDATE    | New    |
| v2   | Alice  | 2026-07-09 11:00:00 | UPDATE    | New    |
| v1   | system | 2026-07-08 09:00:00 | CREATE    | New    |
```

And 默认按 version desc 排序。

### US-6: 选中 v3 → 跳到 v3 历史详情页,看到 3 张表的镜像数据

**作为** 财资经理  
**我希望** 选中某个历史版本后跳到只读详情页  
**以便** 看完整快照。

**Given** 在 US-5 的 dialog 里选中 v3  
**When** 点击"查看详情"  
**Then** 跳到 `/dealing/ac-deal/audit-history?dealNumber=DEAL20260710-0001&version=3`  
**And** 页面只读模式(`?mode=readonly`)展示 3 段镜像数据:

```
段 1: Deal 镜像
  - deal_number, deal_type, business_unit, counterparty_id, instrument_id, ...
段 2: AC Deal 镜像
  - bank_account_id, counterparty_account_id, payment_method(改前值)
段 3: Cashflow 镜像列表
  - v3 时点的全部 cashflow(v3 之后的删改不显示)
```

And 顶部显示 "v3 由 Alice 操作,2026-07-09 16:42:00 写,UPDATE"。

### US-7: 并发编辑(v1.1 lockToken 流程)— 看到版本对比提示

**作为** 对手方专员  
**我希望** 并发编辑冲突时能看到提示和最新版本号  
**以便** 不盲目覆盖他人修改。

**Given** 用户 A 与用户 B 同时打开同一笔 AC 交易详情,各自基于 v3 数据  
**When** 用户 A 先保存(更新到 v4),用户 B 基于 v3 的 lockToken 提交保存  
**Then** 后端返回 409 Conflict  
**And** 前端弹出对话框:"该交易已被 Alice 更新到 v4,请刷新查看最新版本"  
**And** 提示"查看最新版本对比"按钮(跳转审计历史 v4)  
**And** 用户 B 的修改被拦截。

### US-8: 镜像数据查询性能(分页/索引)

**作为** 后端开发 / DBA  
**我希望** 审计历史列表查询 P95 < 300ms,分页稳定  
**以便** 一笔经历 100 次修改的 AC 交易也能秒级返回。

**Given** AC 交易 `DEAL20260710-0001` 经历 50 次修改 → 50 条 deal_image + 50 条 ac_image + 200 条 cashflow_image  
**When** 财资经理请求 `GET /api/v1/dealing/deals/DEAL20260710-0001/versions?pageNum=1&pageSize=20`  
**Then** 服务端 1 次 SQL 查询 deal_image,2 次 SQL 查询 ac_image(批量 IN)+ cashflow_image(批量 IN)  
**And** P95 < 300ms  
**And** 页大小通过 pageSize 参数支持 10/20/50/100。

---

## 4. 字段定义

### 4.1 tms_cashflow_t 增加字段(2 个)

| # | 字段名 | 类型 | 可空 | 默认 | 说明 |
|---|--------|------|------|------|------|
| 1 | `bank_account_id` | BIGINT | Y | NULL | 我方银行账户 ID,外键 `tms_bank_account_t.id`(逻辑外键,无 FK 约束) |
| 2 | `counterparty_bank_account_id` | BIGINT | Y | NULL | 对手方银行账户 ID,外键 `tms_counterparty_account_t.id` |

**说明**:
- 保持现有 `bank_account` / `counterparty_account` 字符串字段不变(对前端展示稳定);
- ID 字段仅用于关联主数据,便于审计追溯;
- v1.1 规则未命中时两个字段均 NULL,允许手动选;
- 经办人最终选择权优先于自动匹配。

### 4.2 tms_cashflows_image_t 完整建表(新表,~22 字段)

```sql
CREATE TABLE IF NOT EXISTS tms_cashflows_image_t (
    id                          BIGSERIAL       PRIMARY KEY,
    image_number                VARCHAR(50)     NOT NULL UNIQUE,        -- 镜像编号
    cflow_number                VARCHAR(50)     NOT NULL,                -- 被镜像的现金流编号
    deal_number                 VARCHAR(50)     NOT NULL,                -- 交易编号(冗余,便于按交易查)
    version                     INT             NOT NULL DEFAULT 1,      -- 镜像版本号(同笔交易递增)
    business_unit               VARCHAR(50),
    bank_account_id             BIGINT,                                  -- 我方银行账户 ID(新)
    counterparty_bank_account_id BIGINT,                                 -- 对手方银行账户 ID(新)
    bank_account                VARCHAR(100),                            -- 改前值(保留字符串)
    counterparty_account        VARCHAR(100),                            -- 改前值(保留字符串)
    direction                   VARCHAR(10),
    amount                      DECIMAL(38,18),
    currency                    VARCHAR(10),
    cflow_date                  DATE,
    value_date                  DATE,
    source_type                 VARCHAR(30),
    source_ref                  VARCHAR(50),
    status                      VARCHAR(20),
    purpose                     VARCHAR(500),
    remark                      VARCHAR(500),
    image_type                  VARCHAR(20)     NOT NULL DEFAULT 'UPDATE', -- CREATE / UPDATE / DELETE
    operator                    VARCHAR(50)     NOT NULL,
    operate_at                  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by                  VARCHAR(50)     NOT NULL,
    created_at                  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted                     CHAR(1)         NOT NULL DEFAULT '0'
);

CREATE INDEX IF NOT EXISTS idx_cashflow_image_cflow_number ON tms_cashflows_image_t(cflow_number);
CREATE INDEX IF NOT EXISTS idx_cashflow_image_deal_number  ON tms_cashflows_image_t(deal_number);
CREATE INDEX IF NOT EXISTS idx_cashflow_image_deal_version ON tms_cashflows_image_t(deal_number, version);
CREATE INDEX IF NOT EXISTS idx_cashflow_image_image_type   ON tms_cashflows_image_t(image_type);

COMMENT ON TABLE tms_cashflows_image_t IS '现金流镜像表(交易 create/update/delete/rate-fix 时记录字段快照)';
```

**字段设计要点**:
- `image_number` 沿用 "IMG+yyyyMMdd+4 位流水" 格式,与现有 `tms_deals_image_t` 一致;
- `version` 与 `deal_number.version` 同号(同笔交易内递增);
- `image_type` 取值 `CREATE` / `UPDATE` / `DELETE`(与现有规范一致);
- `deal_number` 冗余字段,便于审计历史 list 端点 1 次 join 出全量数据;
- 金额精度 `DECIMAL(38,18)` 与 `tms_cashflow_t.amount` 一致;
- 审计字段 5 项标准:`created_by` / `created_at` / `updated_by` / `updated_at` / `version` / `deleted` 全具备。

### 4.3 字段变更影响总结

| 表 | 变更类型 | 字段数 | 影响 |
|----|----------|--------|------|
| `tms_cashflow_t` | ALTER ADD COLUMN | +2 | 实体类同步,迁移脚本 `29-cashflow-bank-account-id.sql` |
| `tms_cashflows_image_t` | CREATE TABLE | +22 | 新建表,迁移脚本 `30-cashflow-image-table.sql` |
| `tms_deals_image_t` | - | 0 | 不变,审计历史直接读 |
| `tms_ac_deals_image_t` | - | 0 | 不变 |
| `tms_at_deals_image_t` | - | 0 | 不变(对照表) |
| `tms_fx_deals_image_t` | - | 0 | 不变 |

---

## 5. 业务规则

### 5.1 规则匹配算法(何时调 match?)

#### 何时调

| 交易事件 | 是否调 match | 参数 |
|----------|-------------|------|
| AC 创建 | 是 | managementEntityId, counterpartyId, instrumentId, direction, currency |
| AC 更新(对手方/币种/方向变化) | 是 | 同上(用最新值) |
| AC 更新(其他字段) | 否 | 不变 → 银行账户不变 |
| AT 创建/更新 | 是 | managementEntityId, counterpartyId, transferType(same/cross), currency |
| FX 创建 | 是(2 次) | BUY 方向 + SELL 方向各 1 次,`currency` 用各自方向的币种 |
| NDF Rate Fix | 是 | managementEntityId, counterpartyId, fixCurrency |
| Submit / Approve / Reject / Execute | 否 | 状态变更不动银行账户 |
| Delete / Cancel | 否 | 镜像表已记录,不重新匹配 |

#### 失败降级

| 场景 | 行为 |
|------|------|
| v1.1 match 返回 200 + null | `bank_account_id = null`,前端保留手动选择 |
| v1.1 match 返回 500 / 超时 | **降级为不调用**(避免交易录入失败),`bank_account_id = null`,写审计日志 `cf_match_failed` |
| v1.1 match 返回 200 + 多条命中 | 取第一条(按 v1.1 优先级降序) |

#### 调用方式

dealing 服务通过 **RestTemplate / WebClient** 调用 `http://basedata:8081/api/v1/default-bank-account-rules/match?...`(单库多服务,同一 PG,但基于 HTTP 调用隔离)。

```java
// CashflowService.matchBankAccount(...) — 伪代码
public BankAccountMatchVO matchBankAccount(Deal deal, String direction, String currency) {
    try {
        String url = "http://basedata:8081/api/v1/default-bank-account-rules/match"
            + "?managementEntityId=" + deal.getManagementEntityId()
            + "&counterpartyId=" + deal.getCounterpartyId()
            + "&instrumentId=" + deal.getInstrumentId()
            + "&direction=" + direction
            + "&currency=" + currency
            + "&dualDirection=true";
        return restTemplate.getForObject(url, BankAccountMatchVO.class);
    } catch (Exception e) {
        log.warn("v1.1 rule match failed, falling back to null: {}", e.getMessage());
        return null;  // 降级
    }
}
```

### 5.2 镜像写入时机

| 事件 | 镜像表 | image_type | 快照内容 |
|------|--------|-----------|----------|
| Cashflow CREATE | tms_cashflows_image_t | CREATE | 新写入的内容 |
| Cashflow UPDATE | tms_cashflows_image_t | UPDATE | 改前的字段值 |
| Cashflow DELETE(软删) | tms_cashflows_image_t | DELETE | 删除前的完整字段 |
| Cashflow NDF Rate Fix 触发新增 | tms_cashflows_image_t | CREATE | 新写入的 settlement cashflow |
| Deal CREATE/UPDATE/DELETE | tms_deals_image_t | 同上 | 不变 |
| AC/AT/FX CREATE/UPDATE/DELETE | tms_{ac,at,fx}_deals_image_t | 同上 | 不变 |

**规则**:
- 每条触发现金流的"交易事件"必须同步写一条镜像;
- 事务内:同 `@Transactional`,失败回滚;
- 镜像写入失败的回滚策略:**镜像失败 → cashflow 也失败**(不留下无镜像的 cashflow)。

### 5.3 镜像保留策略

| 维度 | 决策 | 理由 |
|------|------|------|
| 过期策略 | **永不过期** | 监管合规要求 ≥ 7 年;磁盘成本可控(假设单笔交易全版本占用 < 50KB) |
| 归档 | v1.0 不做 | 表分区 v1.1 再加(monthly partition) |
| 清理 | 不做 | 软删 `deleted='1'` 已足够,archive job v2.0 再说 |
| 多笔交易聚合 | 不做 | 每笔交易单独 image_number,互不污染 |

### 5.4 并发安全(v1.1 lockToken 与镜像关系)

| 场景 | 行为 |
|------|------|
| 同笔 AC 交易 v3 → 并发编辑 → A 先提交成功(版本变 v4) | A 的镜像写 v4,B 提交时基于 v3 lockToken,后端 409 |
| B 看到 409 | 弹窗 "请刷新查看 v4",跳转审计历史 v4 详情 |
| cashflow 并发 | 不做 lockToken(cashflow 是系统写入而非用户编辑) |
| 镜像表本身并发 | 同 cflow_number 多次写入 image_number 唯一约束,失败抛异常,事务回滚 |

### 5.5 字段映射与编码风格

- 与现有 `tms_deals_image_t.image_number` 一致:`IMG + yyyyMMdd + 4 位流水`;
- 与 `tms_at_deals_image_t` 一致:每个事件一条记录,版本号与 `tms_deals_t.version` 同步递增;
- 不复用 `tms_cashflow_t.deleted` 做归档(违规)。

---

## 6. API 端点

### 6.1 影响现有端点

**透明,不破坏兼容性**:

| 端点 | 变化 |
|------|------|
| `POST /api/v1/ac-deals`(创建) | 服务端内部调 match,新字段透明写出 |
| `POST /api/v1/ac-deals/update` | 服务端内部调 match,如对手方/币种/方向变化 |
| `POST /api/v1/at-deals`(创建/更新) | 同 AC |
| `POST /api/v1/fx-deals`(创建) | 服务端内部按 BUY/SELL 各 1 次 match |
| `POST /api/v1/fx-deals/{id}/rate-fix` | 服务端内部调 match(fixCurrency) |
| `GET /api/v1/cashflows/...` | 响应新增 `bankAccountId` / `counterpartyBankAccountId` 字段(前向兼容) |

### 6.2 新增端点

#### 6.2.1 审计历史版本列表

```
GET /api/v1/dealing/deals/{dealNumber}/versions
```

**请求参数**:
- Path: `dealNumber` (String)
- Query: `pageNum` (int, default 1), `pageSize` (int, default 20), `imageType` (optional, 过滤 CREATE/UPDATE/DELETE)

**响应**(Result 包装):
```json
{
  "code": 200,
  "data": {
    "total": 5,
    "pageNum": 1,
    "pageSize": 20,
    "records": [
      {
        "version": 5,
        "dealType": "AC",
        "imageNumber": "IMG20260710-0005",
        "imageType": "UPDATE",
        "operator": "Alice",
        "operateAt": "2026-07-10T14:30:00",
        "status": "New",
        "actionNumber": "ACT20260710-0006"
      },
      {
        "version": 1,
        "dealType": "AC",
        "imageNumber": "IMG20260708-0001",
        "imageType": "CREATE",
        "operator": "system",
        "operateAt": "2026-07-08T09:00:00",
        "status": "New",
        "actionNumber": "ACT20260708-0001"
      }
    ]
  }
}
```

**SQL**:
```sql
SELECT version, deal_type, image_number, image_type, operator, operate_at, status, latest_action_number
FROM tms_deals_image_t
WHERE deal_number = ? AND deleted = '0'
ORDER BY version DESC
LIMIT ? OFFSET ?;
```

#### 6.2.2 审计历史版本详情

```
GET /api/v1/dealing/deals/{dealNumber}/versions/{version}
```

**响应**(Result 包装):
```json
{
  "code": 200,
  "data": {
    "dealImage": {
      "version": 3,
      "dealType": "AC",
      "businessUnit": "HK",
      "counterpartyId": 12,
      "instrumentId": 5,
      "direction": "Outflow",
      "amount": "1000000.000000000000000000",
      "currency": "USD",
      "dealDate": "2026-07-09",
      "valueDate": "2026-07-11",
      "status": "New",
      "imageType": "UPDATE",
      "operator": "Alice",
      "operateAt": "2026-07-09T16:42:00"
    },
    "specificDealImage": {
      "bankAccountId": 18,
      "counterpartyAccountId": 9,
      "paymentMethod": "SWIFT"
    },
    "cashflowImages": [
      {
        "cflowNumber": "CF20260709-0001",
        "version": 3,
        "direction": "Outflow",
        "amount": "1000000.000000000000000000",
        "currency": "USD",
        "bankAccountId": 18,
        "counterpartyBankAccountId": 9,
        "valueDate": "2026-07-11",
        "imageType": "UPDATE"
      }
    ],
    "actionNumber": "ACT20260709-0003",
    "imageNumber": "IMG20260709-0003"
  }
}
```

**行为**:
- 1 次查询 `tms_deals_image_t` by `(deal_number, version)`;
- 1 次查询 `tms_ac_deals_image_t` / `tms_at_deals_image_t` / `tms_fx_deals_image_t` by `(deal_number, version)`,根据 `deal_type` 分支;
- 1 次查询 `tms_cashflows_image_t` by `(deal_number, version)`;
- 单笔 3 次 SQL,全 LEFT JOIN 兼容版本缺失(返回 [] / null)。

#### 6.2.3 与 v1.1 规则匹配的对接端点(dealing → basedata)

复用现有:
```
GET /api/v1/default-bank-account-rules/match?managementEntityId=...&counterpartyId=...&instrumentId=...&direction=...&currency=...&dualDirection=...
```

无需新增。

---

## 7. 对标业界

| 系统 | Audit History 实现 | 现金流银行账户自动填充 | 镜像保留 |
|------|--------------------|---------------------|----------|
| **FIS Quantum** | "Audit Trail" 模块,所有 modification 留痕,可按时间线回放;交易详情页 "Show History" 按钮 | Quantum 自动按 "Settlement Instructions" (SI) 优先级填充,SI 与 Bank Account 解耦 | 永久,归入 Compliance Repository |
| **Murex MX.3** | "Versioning" 模型实体自带;交易变更生成新版本,旧版本可对比;支持 rollback | MX.3 用 "Settlement Rules" + "SSI 优先级",按 counterparty + currency 找默认 SI | 永久;归档到历史表 partition |
| **SAP TRM** | "Change Documents" (SCDO),基于 SCD 表存对象变更;事务码 SCDO_ENTRIES | TRM 用 "House Bank" + "Bank Determination",表 T012R / T012K 配置 | 永久(LEG 法规) |
| **Kyriba** | "Audit Trail" 单独模块,所有 CRUD 留痕;支持 export CSV/PDF | Kyriba 用 "Counterparty / Account Mapping" 规则,自动填充 settlement account | 永久,合规归档 |
| **Open-TMS 本特性** | audit history 列表 + 版本详情(本 PRD);不基于镜像回滚(v1.0) | v1.1 规则 + cashflow 自动填充 ID(v1.0,本 PRD) | 永久(v1.0 决策),后续 partition |

**业界共性**:
1. 交易变更必然留 audit trail;
2. cashflow / payment 必须可反查双方银行账户;
3. 历史版本"只读不改";
4. 监管合规保留 7+ 年。

**Open-TMS 差异化**:
- v1.1 用 **lockToken 409 冲突提示**(业界少见,大多乐观锁后通知);
- 默认银行账户规则(**v1.1**)既支持管理主体锚点也支持双方向,比 Murex 简洁。

---

## 8. 验收标准(可量化)

### 8.1 现金流自动填充(需求 1)

| 验收项 | 度量 |
|--------|------|
| AC 交易创建时银行账户命中率 | ≥ 80%(单库 100 条 AC 测试数据,命中率基于 v1.1 规则覆盖范围) |
| v1.1 规则命中后 cashflow 双方向带出 | 创建 1 条 dualDirection 规则 → FX BUY/SELL 两条 cashflow 都带 bankAccountId |
| 规则未命中时降级 | 200 OK,`bankAccountId = null`,前端保留手动选择 |
| 基于数据 match 超时时降级 | 不阻断 cashflow 写入,P95 延迟 ≤ 500ms(含超时降级) |

### 8.2 现金流镜像(需求 1)

| 验收项 | 度量 |
|--------|------|
| 创建 1 笔 AC 交易 → cashflow_image | 生成 1 条 `image_type='CREATE'` |
| 修改 AC 交易 → 旧 cashflow 软删 + 新 cashflow 插入 | 旧 cashflow 写 `image_type='DELETE'`,新 cashflow 写 `image_type='CREATE'` |
| 删除 cashflow → 镜像表保留 | `image_type='DELETE'` 记录软删前内容 |
| NDF Rate Fix → settlement cashflow 镜像 | 1 条 `image_type='CREATE'` |
| 镜像查询分页 20/页 P95 | < 300ms |
| 单笔交易累积 100 次修改 → 列表查询稳定 | P95 < 500ms |

### 8.3 审计历史(需求 2)

| 验收项 | 度量 |
|--------|------|
| AC/AT/FX 详情页加按钮 | 3 个页面,3 个按钮 |
| 点按钮弹 dialog | dialog 显示版本列表,默认按 version desc |
| 列表 API | `GET /api/v1/dealing/deals/{dealNumber}/versions` 200 OK,total 准确 |
| 详情 API | `GET /api/v1/dealing/deals/{dealNumber}/versions/{version}` 200 OK,3 段数据齐全 |
| 跳转到只读详情页 | `/dealing/{ac,at,fx}-deal/audit-history?dealNumber=X&version=Y`,readonly 模式,无编辑入口 |
| 并发编辑冲突 | 409 弹窗提示,显示版本号,跳转最新版本 |
| 镜像查询性能 | P95 < 300ms(US-8 覆盖) |

### 8.4 监管合规

| 验收项 | 度量 |
|--------|------|
| 任意一笔历史 cashflow 可追溯到双方银行账户 ID | 100%(0/100 测试样本缺失) |
| 任意一笔历史镜像可重放 | 100%,镜像字段与变更前一致 |
| 审计字段(5 项)全部存在 | CREATE TABLE 含 `created_by` / `created_at` / `version` / `deleted` |

---

## 9. 风险与依赖

### 9.1 依赖

| 依赖项 | 说明 | 影响 |
|--------|------|------|
| v1.1 默认银行账户规则(commit `3e3604f`) | 提供 match 端点 | **强依赖**;必须先有 v1.1 才能落地本特性 |
| `tms_at_deals_image_t` 镜像表(commit `954a4b5`) | 提供建表结构参考 | 已就绪 |
| `EntityNameLookup` | 详情页 *Name 字段 | 已就绪 |

### 9.2 风险

| 风险 | 概率 | 影响 | 缓解 |
|------|------|------|------|
| **跨服务调用性能**:basedata 8081 → dealing 8082 | 中 | 单次 match 调用 50-150ms,FX 创建可能 4 次 match,交易创建 P95 飙升 | 1) v1.1 match 5 分钟内存缓存;2) 提前在编辑时后台 match,不阻塞保存;3) async 异步 + 软失败 |
| **cashflow 镜像事务与 deal 镜像事务竞争** | 低 | 同 `@Transactional` 内 UPDATE cashflow + INSERT cashflow_image;失败回滚 | 单事务保证;cashflow_image 失败 → 整体回滚 |
| **v1.1 缓存命中跨服务不一致** | 中 | basedata 修改规则后,dealing 内存缓存 5 分钟内过期 | 基于数据 5 分钟 TTL 是合理实践 |
| **存量 AC 交易无 cashflow_image 历史** | 高 | v1.0 之前产生的交易没有 cashflow_image | 审计历史 v1.0 之前显示 "无镜像";提供 `backfill_job` v1.1 补齐 |
| **FX 买/卖方向 cashflow 各自匹配,可能一个命中一个不命中** | 中 | 一致性 | UI 显示"部分自动填充,需要人工补",前端提示 |

---

## 10. 后续 phase 计划(9 Phase 简要)

| Phase | 名称 | 交付 | 工期 |
|-------|------|------|------|
| Phase 1 | PM PRD 评审(本文件) | 评审通过 | 1 d |
| Phase 2 | UX 设计(审计历史 dialog + 只读页) | 2 个原型(沿用 BaseDataPicker + FormContainer) | 2 d |
| Phase 3 | DB 设计:迁移脚本 `29-cashflow-bank-account-id.sql` + `30-cashflow-image-table.sql` | DDL 评审通过 | 1 d |
| Phase 4 | API 设计:文档 `docs/api/cashflow-match.md` + `docs/api/audit-history.md` | API 评审通过 | 1 d |
| Phase 5 | 后端开发:`CashflowService` 增强 + `ImageService` 增强 + 2 个新 REST 端点 | 后端 Code Review | 4 d |
| Phase 6 | 前端开发:详情页加按钮 + AuditHistoryDialog + AuditHistoryPage(readonly) | 前端 Code Review | 3 d |
| Phase 7 | 集成:3 个交易类型 end-to-end | 测试用例就绪 | 1 d |
| Phase 8 | 测试执行:`scripts/test/test_cashflow_match.py` + `scripts/test/test_audit_history.py` | 通过 | 2 d |
| Phase 9 | 6 维复审 + 交付 | 评级 A/B/C/D | 1 d |
| **合计** | - | - | **16 d ≈ 3.2 周** |

---

## 11. 推进建议:**合并推进**(推荐)

### 11.1 理由

1. **共享前置**:两特性都需要 v1.1 规则 match 调用 + image 表结构,合并可复用 Service 层增强;
2. **共享前端入口**:AC/AT/FX 详情页加按钮是单一动作,**一次性改动 3 个页面**比两次改动更省时;
3. **共享测试设施**:`scripts/test/test_cashflow_match.py` 与 `test_audit_history.py` 共用 setup 脚本(创建 AC + 触发 UPDATE + 验证镜像);
4. **审计历史视图一旦有了 image 表,就自然承接 cashflow 镜像**,不分开做会导致先做完需求 1 立刻追加改动需求 2,合并反而省 1-2 天;
5. **风险对齐**:跨服务调用性能风险、对账风险、双方向匹配风险,在 Phase 5 一次性解决,不会分裂到两次。

### 11.2 不建议分阶段

| 分阶段的风险 | 描述 |
|--------------|------|
| **API 不一致** | 需求 1 单独做完后,需求 2 加新端点时,旧的 cashflow_image 镜像不一致需要补 patch |
| **前端重复改动** | AC 详情页会因分阶段改 2 次,merge 冲突风险 |
| **测试不稳定** | 中间状态"cashflow 自动填充有,但没有审计历史"是非稳态,QA 难以认定 |

### 11.3 例外

如果**资源极度受限**(只有 1 名后端 + 1 名前端),可考虑:
- Week 1:仅做需求 1(cashflow 自动填充,镜像);
- Week 2:仅做需求 2(审计历史);
- 但需声明:**先发需求 1 后,需求 2 的 Phase 7 测试可能因 cashflow_image 表已存在而省一步**。

---

## 12. 产出指标(供主代理估算)

| 指标 | 值 |
|------|-----|
| PRD 总行数 | 约 900-1000 行 |
| US 数量 | 8 |
| 字段定义个数 | 2(主表)+ 22(image 表)= 24 |
| 新增 API 端点 | 2 |
| 影响现有端点 | 5(透明) |
| 新增表 | 1(`tms_cashflows_image_t`) |
| 新增字段 | 2(`bank_account_id` + `counterparty_bank_account_id`) |
| 涉及修改的 .java 文件估算 | 8-10(CashflowService, CashflowServiceImpl, AcDealServiceImpl, AtDealServiceImpl, FxDealServiceImpl, ImageService, ImageServiceImpl, 默认规则调用 RestTemplate 配置) |
| 涉及修改的 .vue 文件估算 | 5(AcDealDetail + AtDealDetail + FxDealDetail 加按钮 + 新增 AuditHistoryDialog + 新增 AuditHistoryPage) |
| 总工作量 | **3.2 周 / 16 d / 1 后端 + 1 前端** |

---

## 附录 A — 术语表

| 术语 | 说明 |
|------|------|
| **cashflow** | 现金流,一笔交易触发的单条资金收/付,1 笔 Deal 可对应 0/1/N 条 cashflow(取决于 DealMap) |
| **image** | 镜像,某次事件的字段快照,CREATE/UPDATE/DELETE 时各写 1 条 |
| **version** | 版本号,同 deal_number 内自增,CREATE = v1,每次 UPDATE +1 |
| **dualDirection** | 双方向,v1.1 规则属性,一条规则同时覆盖 Inflow 与 Outflow |
| **lockToken** | 乐观锁令牌,v1.1 引入,基于 version 校验并发编辑 |

## 附录 B — 参考文档

| 文档 | 路径 |
|------|------|
| v1.1 默认银行账户规则 PRD | `docs/prd/M3/M3-交易对手默认银行账户规则PRD.md` |
| AT 镜像表建表 SQL | `db/schema/27-at-deal-image-table.sql` |
| NDF Rate Fix 评审 | `docs/prd/M3/M3-NDF-Rate-Fix设计评审.md` |
| CLAUDE.md(总体规范) | `CLAUDE.md` |
| DealMap 模式 skill | `.claude/skills/opentms-dealmap-patterns/SKILL.md` |
| API 设计 skill | `.claude/skills/opentms-api-design/SKILL.md` |
| 6 维审核 skill | `.claude/skills/opentms-review-common/SKILL.md` |

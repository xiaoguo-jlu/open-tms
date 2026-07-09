# 默认银行账户规则 PRD — 优化建议

**评估对象**:`docs/prd/M1/M1-主体默认银行账户规则PRD.md` v1.0
**评估日期**: 2026-07-08
**评估角色**: PM Lead + 业务架构师
**评估结论**: v1.0 总体设计清晰(5 维匹配 + ALL 通配 + 优先级排序),**8 个维度** 提出优化建议

---

## 概览(按优先级)

| 优先级 | 数量 | 影响 |
|--------|------|------|
| ⭐⭐⭐ P0 | 5 项 | 必须修,影响发布质量 |
| ⭐⭐ P1 | 11 项 | 强烈建议修,影响体验/可维护性 |
| ⭐ P2 | 9 项 | 后续迭代 |
| **合计** | **25 项** | — |

---

## ⭐⭐⭐ P0 - 必须修(5 项)

### P0-1:数据库字段命名混乱 — `counterparty`/`instrument` VARCHAR 冗余无定义

**问题**(PRD §4.1 line 161-164):
```sql
counterparty        VARCHAR(20)    NOT NULL DEFAULT 'ALL',    -- 对手方,ALL 表示通配
counterparty_id     BIGINT,        -- 对手方 FK(可空=ALL)
instrument         VARCHAR(20)    NOT NULL DEFAULT 'ALL',
instrument_id      BIGINT,
```

**矛盾**:字段同时定义了 VARCHAR(20) 和 BIGINT,但正文 §4.1 又说"用 `_id BIGINT` 强类型 FK,不用 VARCHAR code"。两套机制并存,**后续维护混乱**。

**风险**:
- 哪个是 Source of Truth?
- VARCHAR 字段从哪填?何时同步?
- 表数据冗余容易不一致

**建议**:删除 VARCHAR 冗余字段,只用 `_id BIGINT`。如果前端需要展示"ALL"文字,在 VO 层映射即可。

---

### P0-2:match 接口 `direction` 是必填,但 FX 录入顺序未明确

**问题**(PRD §6.2):
- Step 1 选主体(无 direction)— match 应返回空
- Step 2 选金融产品 + direction=Inflow(默认)— match 触发
- Step 3 选币种对 — 重新 match

**矛盾**:PRD §6.2 Step 1 没 direction,§6.2 Step 2 默认 direction=Inflow,但实际 FX 是双币种(买入币种 + 卖出币种),方向不确定。

**风险**:
- 交易员选完金融产品后,FX 自动取"买入币种的方向"(Inflow)?
- 还是同时取两条规则(买入 + 卖出)?
- 一笔 FX 交易需要**两个账户**(买入账户 + 卖出账户),但 match 只返回一个

**建议**:match 应支持 `dualDirection`(返回 2 个账户:Inflow 账户 + Outflow 账户),或拆成两个 match 调用:
```
GET /match?mgmt=1&instrument=401&direction=Inflow&currency=USD
GET /match?mgmt=1&instrument=401&direction=Outflow&currency=CNY
```

---

### P0-3:match 接口无并发控制 — 多个交易员同时编辑规则会产生脏数据

**问题**(PRD §6.3 算法 + §8.2.4 更新):没有提到并发控制。

**风险**:
- 交易员 A 正在编辑规则 R1(priority 100 → 200)
- 交易员 B 同时把 R1 的 priority 从 100 → 150
- 后保存的覆盖先保存的,**version 乐观锁只能防覆盖,不能防误操作**

**建议**:
1. 引入**规则锁**机制:编辑时获取 `lock_token`,提交时验证
2. 或:**审批流**(规则变更需主管审批),但 PRD 已说本期不做(Z4)
3. **最低要求**:编辑页面加 "Optimistic Lock" 提示,显示 `updated_at`,变化需刷新

---

### P0-4:规则"已被 N 笔交易引用"的 N 怎么算?

**问题**(PRD §6.1 line 340-342):
> 系统提示"该规则已被 N 笔交易引用"

**模糊点**:
- N = 历史交易数?当前未结算?近 30 天?
- 查的是 `tms_deals_t.bank_account_id` 还是 `tms_cashflow_t.bank_account`?
- N 实时算还是定时预聚合?(百万级交易实时算性能差)

**风险**:用户删除规则后,如果有 N=1000 笔历史交易引用了此规则,会出现"幽灵账户"(账户还在,但规则没了,无法追溯)。

**建议**:
- N 定义明确:**当前未完成结算的交易** + **近 90 天已完成交易**
- 实时 SQL 查询时加索引:`tms_deals_t(bank_account_id, status)`
- 或预聚合到 `tms_rule_usage_t` 表,定时刷新

---

### P0-5:FX 录入 match 调用频次 — 没有防抖策略

**问题**(PRD §6.4 时序图):
- 用户选管理主体 → 触发 match
- 用户选金融产品 → 触发 match
- 用户选币种对 → 触发 match
- 用户改汇率 → 也可能触发 match(?)
- 用户选账户 → 也可能触发 match(?)

**风险**:
- 用户快速切换金融产品下拉,**短时间内触发 10+ 次 match**
- 数据库压力 + 用户体验差(loading 闪烁)

**建议**:
- 前端 match 调用**加防抖**(300ms debounce)
- match 调用**只在前端真正变化时触发**(维度完全相同不调)
- 服务端加 Redis 缓存(match 结果按 5 维 hash 缓存,TTL 5 分钟)

---

## ⭐⭐ P1 - 强烈建议修(11 项)

### P1-1:`start_date` 只有单边,无 `end_date`,长期会痛

**问题**(PRD §4.1):只有 start_date,无 end_date。

**场景**:
- 2026-01-01 创建规则 R1(start_date=2026-01-01),适用全年
- 2027-01-01 想让 R1 失效,只能"停用(status=Inactive)"或"删除"
- 但**用户希望保留审计痕迹**:R1 在 2026 年生效,2027 年起新规则 R2 生效

**建议**:加 `end_date DATE`(可空,NULL=长期有效)。同步加 CHECK 约束:`end_date IS NULL OR end_date > start_date`。

---

### P1-2:`priority` 没说取值范围,可能极端值

**问题**(PRD §4.1):priority INT DEFAULT 0,**没说范围**。

**风险**:
- 用户填 priority=99999999,排序性能?
- 负数是否允许?Q4 说"仅 ≥ 0",但代码层没强约束

**建议**:
- DB 加 CHECK:`priority BETWEEN 0 AND 9999`
- 前端 InputNumber 加 `min=0, max=9999`

---

### P1-3:同一维度组合可以重复(R18) — 容易混乱

**问题**(PRD §5.5 R18):"同一维度组合允许重复,留给优先级解决"。

**风险**:
- 资金主管误建 2 条相同规则,系统不告警
- 3 个月后审计时,发现"咦这 2 条规则为什么一样?"

**建议**:
- 同一维度组合 + 同 status=Active,**唯一约束**(UNIQUE INDEX)
- 或者:**允许重复但加警告**(前端红色提示"已存在 N 条相同规则")

---

### P1-4:match 接口无性能要求

**问题**(PRD §七 验收标准):全是功能验收,**无性能 SLA**。

**风险**:
- 规则表到 10 万条时,5 维过滤慢
- 高并发 FX 录入时,match 接口是瓶颈

**建议**:加性能验收:
- 单条 match 响应 < 50ms(p99)
- 并发 100 QPS 下,数据库 CPU < 60%
- 规则表超过 1000 条时启用 Redis 缓存

---

### P1-5:规则变更无"审计日志" — 只能查到当前值,查不到历史

**问题**(PRD §4.1):有 `version`(乐观锁),但**没存历史值**。

**场景**:
- 2026-01-01 创建规则 R1(priority=100)
- 2026-06-01 改为 priority=200
- 2026-12-01 审计查询"2026-03-15 时这条规则的 priority 是多少?"**查不到**

**建议**:新增 `tms_rule_audit_log_t`(规则审计表):
```sql
CREATE TABLE tms_rule_audit_log_t (
    id           BIGSERIAL PRIMARY KEY,
    rule_id      BIGINT NOT NULL,
    operation     VARCHAR(20),  -- UPDATE/DELETE/ENABLE/DISABLE
    old_value    JSONB,         -- 旧值
    new_value    JSONB,         -- 新值
    operator     VARCHAR(50),
    operated_at  TIMESTAMP
);
```
规则变更时,在事务中写日志。

---

### P1-6:`match` 接口无"匹配测试"工具,运营调试困难

**场景**:资金主管想"我的规则有没有匹配这个场景?",但 match 接口是给前端用的(只返回 bankAccountId)。

**建议**:加运营端点:
```
GET /api/v1/default-bank-account-rules/test-match
  ?managementEntityId=1&counterpartyId=5001&...
→ 返回:
{
  matchedRules: [
    {ruleId: 12, priority: 100, bankAccountId: 1001, ruleNumber: "RULE202607050001"},
    {ruleId: 15, priority: 80, bankAccountId: 1002, ruleNumber: "RULE202607050002"}
  ],
  matchedCount: 2,
  selected: {ruleId: 12, ...}
}
```
资金主管可在运营平台预览"为什么 FX 录入会自动填这个账户"。

---

### P1-7:FX 录入联动 — match 接口调用参数未明确

**问题**(PRD §6.2):
- Step 2 触发:`mgmt=1&instrument=401&direction=Inflow&currency=USD`
- 但 PRD 没说 **direction 是 Inflow 还是 Outflow 由谁决定**

**冲突点**:
- FX 是双币种,买入币种用 Inflow?卖出币种用 Outflow?
- 但有的 FX 视角(交易员视角)可能相反

**建议**:在 PRD §6.2 明确:"买入币种账户 direction=Inflow,卖出币种账户 direction=Outflow",并在 match 接口支持 `dualDirection=true` 一次返回两个账户。

---

### P1-8:8.1 列了 8 个端点,8.2 只详述 4 个 — 文档不一致

**问题**(PRD §8.1 vs §8.2):
- §8.1 列 8 个端点(包括 `/enable` `/disable` `/delete/{id}` 等)
- §8.2 只详述 4 个(match/page/update/POST)

**风险**:开发时不知道完整端点列表

**建议**:在 §8.2 补全所有端点的请求/响应结构,或**指向** `docs/api/basedata/02-default-bank-account-rules.md`(独立 API 文档)。

---

### P1-9:跨主体规则继承预留不充分

**问题**(PRD §9.2):
> 主体 `tms_management_entity_t` 已有 `parent_code` / `level_depth` 字段,继承数据基础就绪

**风险**:
- 集团下属主体的账户体系差异大,继承容易引入误配(PRD 已承认)
- 但**预留不充分**:即使 P2+ 要做,数据库结构可能还要大改

**建议**:在 `tms_default_bank_account_rule_t` 加一列:
```sql
inherit_parent  CHAR(1) DEFAULT '0'  -- '1' 表示继承父主体规则
```
P1 先加列 + 索引,P2+ 再实现继承逻辑。

---

### P1-10:match 接口无版本控制,规则改了交易自动变账户

**问题**:
- 2026-03-15 交易员录入 FX,match 命中 R1,自动填账户 A
- 2026-04-01 资金主管改了 R1 的 bankAccountId(账户 A → 账户 B)
- **历史交易没变**(FX 表仍记账户 A),但**业务逻辑变了** — 后续清算按 A 还是 B?

**风险**:监管/合规审计时,无法说明"当时的规则是什么"。

**建议**:**规则快照机制**
- FX 交易表加 `rule_snapshot` 字段(JSONB),录入时存 match 时点的规则副本
- 或:新增 `tms_fx_deal_rule_snapshot_t` 表,1:1 关联 FX 交易

---

### P1-11:章节 §6.3 算法代码注释说 `r.getCurrency() == null`,但 `currency` DB 是 NOT NULL DEFAULT 'ALL' 缺位

**问题**(PRD §4.1 line 166):
```sql
currency            VARCHAR(10)    NOT NULL DEFAULT 'ALL',
```

**矛盾**:
- SQL 说 NOT NULL
- 但算法 line 433:`r.getCurrency() == null`(假设 NULL=ALL)
- §5.1 R6 也说"ALL 维度 DB 存储为 NULL"

**结论**:应该让 `currency` 字段**可空(NULL)**,而不是 NOT NULL DEFAULT 'ALL'。需修改 DDL。

---

## ⭐ P2 - 后续迭代(9 项)

### P2-1:`direction` 枚举来源不明确

PRD §4.4 说"方向枚举遵循 `GlobalConstants.Direction`(需新增)",但没说 ALL 这个值。

**建议**:在 `GlobalConstants` 加:
```java
public static final String DIRECTION_INFLOW = "Inflow";
public static final String DIRECTION_OUTFLOW = "Outflow";
public static final String DIRECTION_ALL = "ALL";
```

### P2-2:导入/导出(Z2)应该有 CSV/Excel 模板

虽然 Z2 是 P1+,但当前 P0 阶段应该**预留字段映射**(`rule_number` 作为外部 ID,方便导入时去重)。

### P2-3:规则模板功能

资金主管经常需要"为某主体下所有对手方创建规则",应支持:
- 复制现有规则
- 模板套用(预置常用模式)

### P2-4:批量启用/停用(P0 范围 3.3)

PRD §3.3 提到"一键启用/停用",但只有"行内切换"。如果某主体下有 100 条规则,行内切换效率低。需加批量操作端点。

### P2-5:多候选返回(Q1)

Q1 决定只返回首条,但用户场景:同一主体下 USD SPOT 有 2 条规则(中行对手方优先,否则工商),交易员可能想看候选。P2+ 支持。

### P2-6:`RULE + yyyyMMddxxxx` 编号在多实例下会重复

单进程 OK,多实例部署(主备/集群)时会重复。建议改用雪花算法或 UUID 片段。

### P2-7:规则变更通知(Z4)

规则被改了,正在使用此规则的交易员应该收到通知(站内信/WebSocket)。本期不做。

### P2-8:FX 录入"无匹配"时的兜底体验

PRD §3.2:"账户字段留空,提示'无默认账户,请手动选择'"。建议进一步:
- 显示"建议账户"(基于历史交易统计)
- 提供"一键新建规则"快捷入口

### P2-9:基于历史交易的"AI 推荐账户"(Z5)

Q5/P3+ 暂不规划,但应在 PRD 标注为"未来增强"。

---

## 📋 综合建议清单(按 PM 实施优先级排序)

### P0(必做,阻塞发布)
1. 修字段命名(去 VARCHAR 冗余)
2. match 接口支持双方向 / dualDirection
3. 编辑/启用/停用加乐观锁提示
4. 明确"被引用 N"的查询逻辑
5. FX 录入防抖策略

### P1(强烈建议,影响可维护性)
1. 加 end_date 字段
2. priority 取值范围约束
3. 唯一约束 + 重复警告
4. match 性能 SLA 定义
5. 规则审计日志表
6. 匹配测试运营端点
7. FX 录入 match 参数语义
8. 补全 §8.2 端点文档
9. inherit_parent 预留列
10. 规则快照机制(交易侧存 rule_snapshot)
11. 修 `currency` 字段 NOT NULL 矛盾

### P2(后续)
- GlobalConstants 枚举补全
- 导入/导出字段映射
- 规则模板/批量操作
- 多候选返回
- 编号生成器改分布式
- 变更通知
- 无匹配兜底 UX
- AI 推荐(预)

---

## 🎯 给 PM 的建议

1. **本版本 v1.0 建议增加 1 个版本号到 v1.1**:补 P0 五项
2. **API 文档单独抽出**:`docs/api/basedata/02-default-bank-account-rules.md`,不再放在 PRD 内
3. **架构评审前**:补 match 接口双方向讨论(让业务确认)
4. **联调前**:做技术评审,看 match 接口 QPS 性能要求
5. **上线前**:补 P1-4 性能 SLA + P1-5 审计日志(否则无法对账)

---

## 相关文档

- `docs/prd/M1/M1-主体默认银行账户规则PRD.md` v1.0
- `docs/api/basedata/01-bank-accounts.md` v1.2
- `docs/优化需求/AC交易列表-优化需求.md`
- `docs/优化需求/通用交易体验-优化需求.md`
- `CLAUDE.md` 第七节 验收标准
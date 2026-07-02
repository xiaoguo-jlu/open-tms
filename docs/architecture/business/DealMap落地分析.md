# Open-TMS DealMap 落地分析

**版本**: v1.0
**角色**: 业务架构师 (BA)
**日期**: 2026-06-05
**状态**: 设计方案
**基线文档**:
- `docs/architecture/business/AC交易与现金流分离架构设计.md` (v1.2)
- `docs/architecture/business/交易现金流与会计事件架构.md` (v1.0)
- `docs/architecture/business/AC交易与现金流分离架构设计.md`

---

## 一、目标与背景

### 1.1 用户诉求

> "交易创建后,生成全生命周期的事件(即 TMS 的 dealmap),然后驱动生成现金流和会计事件。"

### 1.2 业界对标

| 系统 | 模型 | 关键能力 |
|------|------|----------|
| **FIS Quantum** | Trade-Centric,执行即触发 | 交易→现金流→分录 同步生成 |
| **Murex MX.3** | Event Sourcing,异步消费者 | 事件可回放,SAGA 协调 |
| **SAP TRM** | 业务事件总线 | 业务事件/会计事件分离 |
| **Kyriba** | 规则引擎驱动 | 配置化核算规则 |

### 1.3 当前 Open-TMS 痛点

| 痛点 | 影响 |
|------|------|
| `executeDeal()` 只更新状态,无事件 | 账实分离,无法对账 |
| 无 Deal→Cashflow→Accounting 链路 | 财务手工记账 |
| 业务事件/会计事件未分离 | 灵活性差,反核算困难 |
| 无核算规则引擎 | 新产品需硬编码 |

---

## 二、DealMap 架构总览

### 2.1 核心思想:**事件溯源 + 规则驱动**

```
Deal(交易)                    ← 业务意图(用户行为)
   │
   │ execute()
   ▼
DealEvent(交易事件)            ← 业务事件,Deal 生命周期快照
   │
   │ on(DealExecuted)
   ▼
CashflowEvent(现金流事件)      ← 资金侧事件
   │
   │ on(CashflowGenerated)
   ▼
AccountingEvent(会计事件)      ← 财务侧事件
   │
   │ rule-based split
   ▼
AccountingEntry × N(分录)      ← 复式记账
```

### 2.2 与现有 Action/Image 的关系

| 现有对象 | DealMap 中定位 | 改造方向 |
|----------|---------------|----------|
| `Action` (操作记录) | **保留**,作为 DealMap 的操作日志 | 不变,继续做操作审计 |
| `DealImage` (镜像) | **保留**,作为事件快照的实现 | 强化为 DealEvent 实体 |
| `AcDealImage` | **升级** → CashflowEvent | 事件化,带状态机 |
| ❌ 缺失:AccountingEvent | **新增** | 新建表 + 服务 |
| ❌ 缺失:AccountingEntry | **新增** | 新建表 + 服务 |
| ❌ 缺失:AccountingRule | **新增** | 新建表 + 规则引擎 |

---

## 三、领域模型设计

### 3.1 新增/调整实体

#### 3.1.1 `tms_deal_events_t` (新增) — 交易事件

```sql
CREATE TABLE tms_deal_events_t (
    id BIGSERIAL PRIMARY KEY,
    event_number VARCHAR(50) NOT NULL UNIQUE,  -- DLEvt+yyyyMMdd+seq
    deal_id BIGINT NOT NULL,
    deal_number VARCHAR(50) NOT NULL,
    deal_type VARCHAR(10) NOT NULL,           -- AC/AT/FX/ST
    event_type VARCHAR(30) NOT NULL,          -- Executed/Updated/Submitted/Approved/Rejected/Canceled
    event_status VARCHAR(20) NOT NULL,        -- Generated/Processed/Failed
    payload JSONB,                            -- 事件载荷(Deal 当时的完整快照)
    occurred_at TIMESTAMP NOT NULL,
    processed_at TIMESTAMP,
    created_by VARCHAR(50),
    created_at TIMESTAMP,
    deleted CHAR(1) DEFAULT '0'
);
```

**职责**: Deal 生命周期中每个状态变化产生一个事件,作为后续现金流的"根因"。

#### 3.1.2 `tms_cashflow_events_t` (新增) — 现金流事件

复用现有 `tms_cashflow_events_t` 设计(已存在于 v1.0 文档)。

**与 AcDeal 关系**:
- AC 交易:1 Deal → 1~N Cashflow(本金/利息/费用分离时)
- AT 交易:1 Deal → 1 Cashflow

#### 3.1.3 `tms_acct_events_t` + `tms_acct_entries_t` + `tms_acct_rules_t` (新增) — 会计三件套

直接采用 v1.0 文档中的设计。

### 3.2 实体关系图

```
┌────────────────────────────────────────────────────────────┐
│                  DealMap 完整数据流                         │
├────────────────────────────────────────────────────────────┤
│                                                             │
│  Deal (1) ─────execute()────▶ (1) DealEvent               │
│   │                                  │                      │
│   │ 1:N                              │ 1:N (按规则展开)    │
│   ▼                                  ▼                      │
│  Action (操作审计)            CashflowEvent (1)            │
│   │                                  │                      │
│   │                                  │ 1:1                  │
│   │                                  ▼                      │
│   │                          AccountingEvent (1)            │
│   │                                  │                      │
│   │                                  │ 1:N                  │
│   │                                  ▼                      │
│   │                          AccountingEntry × N            │
│   │                                  │                      │
│   │                                  │ 1:N                  │
│   │                                  ▼                      │
│   └──▶ AccountingRule ◀───lookup─────┘                      │
│                                                             │
└────────────────────────────────────────────────────────────┘
```

---

## 四、事件流转引擎设计

### 4.1 三种引擎选型对比

| 方案 | 复杂度 | 一致性 | 适用场景 | **建议** |
|------|--------|--------|----------|----------|
| **A. 同步链式调用** | ⭐ | 强一致 | 单体,低并发 | ✅ **M1 起步** |
| **B. Spring Event 异步** | ⭐⭐ | 最终一致 | 中等规模 | ⭐ M2 升级 |
| **C. MQ (Kafka/RocketMQ)** | ⭐⭐⭐ | 最终一致 | 分布式,高吞吐 | M3 远期 |

**M1 阶段推荐方案 A**,通过 `ApplicationEventPublisher` + `@TransactionalEventListener` 实现,既保证强一致,又为 M2 升级到异步预留接口。

### 4.2 M1 同步事件流转实现

```java
// 1. 定义领域事件
public class DealExecutedEvent extends ApplicationEvent {
    private final Deal deal;
    private final String operator;
    // ...
}

// 2. DealService.executeDeal() 发布事件
@Transactional
public boolean executeDeal(Long id, String operator) {
    // ... 现有逻辑,设置状态为 Settled ...
    
    // 发布事件(事务提交后由监听器消费)
    eventPublisher.publishEvent(new DealExecutedEvent(this, deal, operator));
    return true;
}

// 3. CashflowEventListener 监听
@Component
public class DealEventListener {
    
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onDealExecuted(DealExecutedEvent event) {
        cashflowEventService.generateFromDeal(event.getDeal());
    }
}

// 4. CashflowEventService 内部再发布 CashflowGeneratedEvent
@Service
public class CashflowEventService {
    
    @Transactional
    public void generateFromDeal(Deal deal) {
        // 1. 查询核算规则
        AccountingRule rule = ruleService.findRule(deal.getDealType(), deal.getDirection());
        
        // 2. 创建 CashflowEvent
        CashflowEvent cflow = new CashflowEvent(...);
        cflowMapper.insert(cflow);
        
        // 3. 创建 AccountingEvent
        eventPublisher.publishEvent(new CashflowGeneratedEvent(this, cflow));
    }
}
```

### 4.3 状态机保障

```java
// DealServiceImpl.approveDeal() - 状态机检查
if (!DEAL_STATUS_SUBMITTED.equals(deal.getStatus())) {
    throw new RuntimeException("Only submitted deal can be approved");
}
```

✅ 现有实现已包含,无需修改。

---

## 五、核算规则引擎设计

### 5.1 为什么需要规则引擎?

| 方案 | 适用 | 优势 | 劣势 |
|------|------|------|------|
| **硬编码 if-else** | 产品少(≤5 种) | 简单直接 | 每加新产品改代码 |
| **规则表驱动** | 产品多(>5 种) | 灵活扩展 | 需规则维护 UI |

**Open-TMS 目标产品数**:AC/AT/FX/IR/ST/Derivative/... >10 种,**必须用规则引擎**。

### 5.2 规则定义

```java
@Data
public class AccountingRule {
    private String ruleCode;        // AC_DEPOSIT_INFLOW
    private String dealType;        // AC
    private String cflowDirection;  // Inflow
    private String postTiming;      // Immediate
    private List<RuleEntry> entries;
}

@Data
public class RuleEntry {
    private String accountCode;     // 1001
    private String dc;              // D (Debit)
    private String amountType;      // Principal
    private String amountFormula;   // AMOUNT (或 AMOUNT*0.05 表示 5% 手续费)
}
```

### 5.3 规则示例:AC 存款 Inflow

| 借/贷 | 科目 | 金额 | 说明 |
|--------|------|------|------|
| D | 1001 银行存款 | 1,000,000 | 本金 |
| C | 6001 主营业务收入 | 1,000,000 | 实际收到的现金 |

### 5.4 规则匹配流程

```
Deal(dealType=AC, direction=Inflow, amount=1000000)
   │
   ▼
RuleService.findRule(dealType=AC, direction=Inflow)
   │
   ▼
Rule(ruleCode=AC_INFLOW_DEFAULT, entries=[...])
   │
   ▼
生成 AccountingEntry × N
```

---

## 六、落地实施路线图

### 6.1 分阶段目标

| 阶段 | 目标 | 周期 | 关键交付 |
|------|------|------|----------|
| **M1.1** | DealMap 骨架 | 1 周 | DealEvent 表+服务+埋点 |
| **M1.2** | Cashflow 自动化 | 1 周 | executeDeal 触发 Cashflow |
| **M1.3** | Accounting 自动化 | 1 周 | 规则引擎 + Entry 生成 |
| **M1.4** | 异常/冲销 | 0.5 周 | 反向事件 + 红字分录 |
| **M2** | 异步化 | 2 周 | Spring Event + MQ |
| **M3** | 分布式事务 | 2 周 | SAGA / Outbox |

### 6.2 M1.1:DealMap 骨架(本周)

**交付物**:
- [ ] `tms_deal_events_t` 表 + 索引
- [ ] `DealEvent` 实体/Mapper/Service
- [ ] 在 `saveDeal / updateDeal / submitDeal / approveDeal / rejectDeal / executeDeal / deleteDeal` 8 个方法中插入 `recordEvent()` 调用
- [ ] `DealEvent` 查询 API(供前端 DealMap 时间线展示)

**代码模板**:
```java
private void recordEvent(Deal deal, String eventType, String operator) {
    DealEvent event = new DealEvent();
    event.setEventNumber(generateEventNumber());
    event.setDealId(deal.getId());
    event.setDealNumber(deal.getDealNumber());
    event.setDealType(deal.getDealType());
    event.setEventType(eventType);
    event.setEventStatus("Processed");
    event.setPayload(JsonUtils.toJson(deal));  // 快照
    event.setOccurredAt(LocalDateTime.now());
    event.setCreatedBy(operator);
    event.setCreatedAt(LocalDateTime.now());
    dealEventMapper.insert(event);
}
```

### 6.3 M1.2:Cashflow 自动化(下周)

**交付物**:
- [ ] `tms_cashflow_events_t` 表(已设计)
- [ ] `CashflowEventService.generateFromDeal(Deal)` 方法
- [ ] `executeDeal()` 改造:状态更新为 `Settled` → 触发 `DealEventType.EXECUTED` → 监听器调用 CashflowService
- [ ] 单元测试:AC 存款/取款 2 个场景

**关键点**:
- 事务边界:`executeDeal()` 与 Cashflow 生成必须在**同一事务**(M1 阶段),M2 再拆异步
- 失败处理:任一环节失败,整个事务回滚

### 6.4 M1.3:Accounting 自动化(第三周)

**交付物**:
- [ ] `tms_acct_rules_t` + `tms_acct_rule_entries_t` 表
- [ ] `tms_acct_events_t` + `tms_acct_entries_t` 表
- [ ] `AccountingRuleService` (基础 CRUD + 按 dealType/direction 查找)
- [ ] `AccountingEngine` (规则匹配 + 分录展开)
- [ ] 预置 3 条规则:AC Inflow、AC Outflow、AT Transfer
- [ ] 单元测试:借贷平衡验证

### 6.5 M1.4:异常/冲销(第四周)

**交付物**:
- [ ] `cancelCashflow()` 方法:生成反向 AccountingEvent(eventType=Reverse)
- [ ] `redLetterEntry()` 工具方法:借贷方互换
- [ ] 集成测试:Cancel Deal → 反向 Cashflow → 红字分录

---

## 七、关键设计决策

### 7.1 决策 1:事件同步 vs 异步?

| 选项 | 选 | 理由 |
|------|----|------|
| **同步(M1)** | ✅ | 简单,强一致,便于调试;为异步预留接口 |
| 异步(M2) | ⏰ | 解耦,高吞吐;但需处理最终一致性 |

### 7.2 决策 2:Cashflow 与 AcDealImage 关系?

| 选项 | 选 | 理由 |
|------|----|------|
| **替代 Image** | ❌ | 现有 Image 设计简单,强行替换风险大 |
| **并存(Image 做快照,Cashflow 做事件)** | ✅ | 各司其职,Image 留作操作审计,Cashflow 专注资金流 |

### 7.3 决策 3:核算规则存 DB 还是配置?

| 选项 | 选 | 理由 |
|------|----|------|
| **DB 表** | ✅ | 支持热更新,适配产品迭代 |
| YAML/JSON 配置 | ❌ | 改完需重启,运维成本高 |

### 7.4 决策 4:多币种/汇率处理?

**M1.3 阶段**:
- 交易币种 = 入账币种时,`exchange_rate=1.0`,`amount_lc = amount`
- 交易币种 ≠ 入账币种时,**M1 不支持**,M2 引入汇率服务

---

## 八、对现有代码的影响评估

### 8.1 需要修改的文件

| 文件 | 修改类型 | 影响范围 |
|------|----------|----------|
| `DealServiceImpl.java` | 重构 | 8 个方法,每个增加 `recordEvent` 调用 |
| `AtDealServiceImpl.java` | 重构 | 同上 |
| `pom.xml` (dealing) | 依赖 | 不需新增,复用现有 |
| `db/schema/*.sql` | 新增 | 3-4 张新表 |

### 8.2 新增的文件

| 类型 | 数量 | 命名规范 |
|------|------|----------|
| Entity | 4 | `DealEvent/CashflowEvent/AccountingEvent/AccountingEntry/AccountingRule` |
| Mapper | 5 | `*Mapper.java` |
| Service | 4 | `*Service.java` + `*ServiceImpl.java` |
| DTO/VO | 8 | 入参/出参/查询条件 |
| Controller | 3 | `EventController/RuleController/AccountingController` |
| Listener | 2 | `DealEventListener/CashflowEventListener` |
| Test | 6 | Service 层单测 + 集成测试 |

**预估代码量**: 约 3000-4000 行(含测试)

### 8.3 数据库变更

| 表 | 操作 | 字段数 |
|----|------|--------|
| `tms_deal_events_t` | 新建 | 14 |
| `tms_cashflow_events_t` | 新建 | 22 |
| `tms_acct_events_t` | 新建 | 17 |
| `tms_acct_entries_t` | 新建 | 19 |
| `tms_acct_rules_t` | 新建 | 13 |
| `tms_acct_rule_entries_t` | 新建 | 10 |

---

## 九、风险与缓解

| 风险 | 等级 | 缓解措施 |
|------|------|----------|
| 现有 `executeDeal` 重构破坏现有功能 | 🟠 | 先跑现有测试,再重构;保留灰度开关 |
| 借贷不平衡导致账务错误 | 🔴 | 单元测试覆盖所有规则;过账前 `SUM(D)=SUM(C)` 校验 |
| 高并发下事件重复生成 | 🟠 | 事务边界保证;事件表 `event_number` 唯一约束 |
| 跨模块依赖循环(Dealing → Accounting) | 🟠 | Accounting 放 `common-accounting` 公共模块 |
| 性能:Cashflow 生成阻塞主交易 | 🟡 | M1 单事务内接受;M2 异步化 |

---

## 十、验证标准

### 10.1 功能验证

- [ ] 提交 1 笔 AC 存款 → 自动产生 1 个 DealEvent(EXECUTED)+ 1 个 CashflowEvent + 1 个 AccountingEvent + 2 个 AccountingEntry(借+贷)
- [ ] 取消已执行的交易 → 产生反向 CashflowEvent(eventType=Reverse)+ 红字分录
- [ ] 借贷合计 = 0(分录平衡)

### 10.2 性能验证

- [ ] executeDeal 端到端耗时 < 200ms(M1 同步)
- [ ] 1000 笔交易批量执行,事件生成无丢失、无重复

### 10.3 审计验证

- [ ] 任意 CashflowEvent 可追溯到 Deal
- [ ] 任意 AccountingEntry 可追溯到 CashflowEvent → Deal
- [ ] 任意反向事件可追溯到原事件(双向链接)

---

## 十一、立即可执行的下一步

按优先级排序:

| 序号 | 任务 | 负责人 | 预计耗时 |
|------|------|--------|----------|
| 1 | 创建 `tms_deal_events_t` 表 | DB | 0.5h |
| 2 | 编写 `DealEvent` 实体+Mapper+Service | BE | 2h |
| 3 | 在 DealServiceImpl 8 个方法中插入 `recordEvent` | BE | 3h |
| 4 | 单元测试:每个状态变化产生 1 个事件 | BE | 2h |
| 5 | 编写 DealEvent 查询 API + 前端 DealMap 时间线 | BE+FE | 4h |
| 6 | 设计 tms_cashflow_events_t 表结构(评审) | BA+DB | 1h |

**总计**:M1.1 阶段约 1.5 个工作日完成骨架。

---

## 十二、相关文档

- `docs/architecture/business/AC交易与现金流分离架构设计.md` — Deal/Cashflow 分离设计
- `docs/architecture/business/交易现金流与会计事件架构.md` — 完整四件套领域模型
- `docs/prd/M1/M1-交易录入与管理PRD.md` — 交易模块 PRD
- `docs/规范/Open-TMS开发规范文档.md` — 编码规范

---

*分析人: Claude Code (业务架构师技能辅助)*
*创建日期: 2026-06-05*

# Checklist 03 — 跨模块影响与依赖

## 目的

验证新 PRD 是否会破坏 Open-TMS 既有 M1 模块:
- basedata (14 Resource, 端口 8081)
- dealing (AC/AT 全流程, 端口 8082)
- 以及未来 M2 的 fundplan / valuation / settlement 等

## M1 已贯通模块(基于 CLAUDE.md)

### basedata 模块

| Resource | 资源名 | 端口 | 状态 |
|----------|--------|------|------|
| Country | 国家 | 8081 | 已贯通 |
| Currency | 币种 | 8081 | 已贯通 |
| Bank | 银行 | 8081 | 已贯通 |
| BankAccount | 银行账户 | 8081 | 已贯通 |
| Counterparty | 对手方 | 8081 | 已贯通 |
| Trader | 交易员 | 8081 | 已贯通 |
| BusinessUnit | 业务单元 | 8081 | 已贯通 |
| CurrencyPair | 币种对 | 8081 | 已贯通 |
| ExchangeRate | 汇率 | 8081 | 已贯通 |
| ManagementEntity | 管理主体 | 8081 | 已贯通 |
| Instrument | 金融工具 | 8081 | 已贯通 |
| Calendar | 日历 | 8081 | 已贯通 |
| Holiday | 节假日 | 8081 | 已贯通 |
| SettlementAccount | 结算账户 | 8081 | 已贯通 |

### dealing 模块

| Resource | 资源名 | 端口 | 状态 |
|----------|--------|------|------|
| Deal | 交易单据 | 8082 | AC/AT 全流程已贯通 |
| DealAction | 交易动作 | 8082 | 已贯通 |
| DealImage | 交易影像 | 8082 | 已贯通 |
| ApprovalTask | 审批任务 | 8082 | 已贯通 |
| Cashflow | 现金流 | 8082 | 已贯通 |

## 跨模块依赖检查

```yaml
dependencies:
  - id: REQ-CROSS-001
    name: basedata 资源引用合规性
    severity: P0
    check_method: |
      1. 列出新 PRD 引用的 basedata 资源
      2. 验证引用方式(通过 ID / Code, 非冗余字段)
      3. 验证无循环依赖
    pass_criteria: 仅通过 ID 引用,无冗余字段

  - id: REQ-CROSS-002
    name: dealing 交易引用合规性
    severity: P0
    check_method: |
      1. 验证新特性是否需要调用 Deal / Action / Cashflow
      2. 验证调用方式(Fegin / OpenFeign, 不得直接 mapper 跨模块)
    pass_criteria: 通过服务调用,不跨 mapper 访问

  - id: REQ-CROSS-003
    name: 共享类放置
    severity: P0
    check_method: |
      1. 验证 DTO / VO / Util 类的归属
      2. 跨模块共享必须放 common
    pass_criteria: 0 业务类直接放错模块

  - id: REQ-CROSS-004
    name: 数据库表命名一致
    severity: P0
    check_method: |
      1. 验证新表遵循 tms_{module}_{type} 命名
      2. type ∈ {t/d/log/rel/his}
    pass_criteria: 100% 命名一致
```

## 存量影响检查

```yaml
impact_analysis:
  - id: REQ-IMPACT-001
    name: schema 兼容性
    severity: P0
    check_method: |
      1. PRD 是否新增既有表字段
      2. 验证 NOT NULL 字段是否有默认值
      3. 验证字段长度扩展不破坏数据
    pass_criteria: 0 破坏性 schema 变更,或附带迁移脚本

  - id: REQ-IMPACT-002
    name: API 兼容性
    severity: P0
    check_method: |
      1. PRD 是否修改既有 API 响应字段
      2. 验证前端是否依赖被删字段
    pass_criteria: API 向后兼容,或前端同步升级

  - id: REQ-IMPACT-003
    name: 数据迁移
    severity: P1
    check_method: |
      1. 是否需要回填历史数据
      2. 是否提供迁移脚本
    pass_criteria: 迁移脚本齐全 + 回滚方案

  - id: REQ-IMPACT-004
    name: 前端影响范围
    severity: P1
    check_method: |
      1. 列出受影响的前端页面
      2. 验证是否需要新增视图
    pass_criteria: 影响范围明确
```

## 检查方法

```bash
# 列出既有资源
Glob: basedata/src/main/java/com/opentms/basedata/controller/*.java
Glob: dealing/src/main/java/com/opentms/dealing/controller/*.java

# 验证 common 类
Glob: common/src/main/java/com/opentms/common/**/*.java

# grep 新 PRD 引用的存量资源
Grep:
  pattern: counterparty|bank|instrument|deal|action
  path: docs/prd/{module}/{feature}.md
  output: content
```
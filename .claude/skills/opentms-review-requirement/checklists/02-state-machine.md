# Checklist 02 — 状态机完整性

## 目的

验证业务对象的状态定义是否完整,涵盖初始 / 中间 / 终态 / 异常状态,
符合 Murex MX.3 平均每个对象 8-15 个状态的成熟标准。

## Deal 状态机(Open-TMS Murex 对标)

```yaml
DealStatus:
  - name: New
    label: 新建
    initial: true
    transitions_to: [Submitted, Canceled]

  - name: Submitted
    label: 已提交
    transitions_to: [Approved, Rejected]

  - name: Approved
    label: 已审批
    transitions_to: [Settled, Executed, Canceled]

  - name: Rejected
    label: 已驳回
    terminal: true
    transitions_to: []

  - name: Executed
    label: 已执行
    transitions_to: [Settled]

  - name: Settled
    label: 已结算
    terminal: true
    transitions_to: []

  - name: Canceled
    label: 已撤销
    terminal: true
    transitions_to: []
```

## Action 状态机

```yaml
ActionStatus:
  - Pending -> Approved/Rejected -> Executed
  - 异常: ExecutionFailed(执行失败,可重试)
```

## Approval 状态机

```yaml
ApprovalStatus:
  - Pending -> Approved/Rejected
  - 异常: Withdrawn(撤回)
```

## 检查项

- [ ] 每个业务对象都有初始状态(Initial)
- [ ] 每个业务对象都有终态(Terminal: Settled/Canceled)
- [ ] 异常状态明确(Rejected/Canceled/Failed/Expired)
- [ ] 状态转移图无死锁(可达性分析)
- [ ] 状态转移无环路(Closed loop 检测)
- [ ] 状态枚举使用 GlobalConstants,无魔术字符串
- [ ] 状态变更触发业务事件(deal.submitted / deal.approved)

## 检查方法

```bash
# grep PRD 中的状态定义
Grep:
  pattern: status|state|状态
  path: docs/prd/{module}/{feature}.md
  output: content

# 验证状态机是否引用 GlobalConstants
Grep:
  pattern: DealStatus|ActionStatus|ApprovalStatus
  path: common/src/main/java/com/opentms/common/constant/GlobalConstants.java
  output: content
```

## 反例(必须退回)

- 仅定义 New / Active / Inactive 三状态(无异常路径)
- 状态转移未明确(允许 New 直接到 Settled)
- 状态名称混用中英文("Active" 和 "启用" 混用)
- 状态名称使用魔术字符串而非 GlobalConstants
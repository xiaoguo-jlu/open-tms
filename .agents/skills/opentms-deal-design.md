# Open-TMS 交易产品设计 Skill

**版本**: v1.0
**角色**: 产品经理 (BA)
**日期**: 2026-06-01
**状态**: 新建 - 交易产品设计方法论沉淀

---

## 一、交易产品设计核心理念

### 1.1 核心理念

| 理念 | 说明 | 重要性 |
|------|------|--------|
| **Action是审批的作用对象** | 审批人审批的是Action，不是交易本身 | ⭐⭐⭐⭐⭐ |
| **Action记录操作历史** | 每次数据变化（创建/修改/删除）生成一条Action记录 | ⭐⭐⭐⭐⭐ |
| **镜像表结构化存储** | 记录每个字段的旧值，支持交易回溯，而非存储JSON | ⭐⭐⭐⭐⭐ |
| **公共表+个性化表** | 所有交易共享公共表，各交易类型独立个性化表 | ⭐⭐⭐⭐ |
| **交易表新增字段时镜像表同步** | 保持镜像表与交易表字段一致 | ⭐⭐⭐⭐ |

### 1.2 交易设计的行业参考

**FIS Quantum TMS**的交易架构核心：
```
交易(Trade/Deal)
    │
    ├── 生命周期管理（状态机）
    ├── 审批工作流（多级审批）
    ├── 操作审计（Action历史）
    └── 快照回溯（Image）
```

**Murex MX.3**的交易架构核心：
```
Transaction Aggregate
    ├── Deal（交易单据）
    │   └── 审批流程
    ├── Cashflow[]（现金流数组）
    └── Image（历史快照）
```

---

## 二、交易架构设计

### 2.1 标准表结构

每种交易类型由以下几类表组成：

```
┌─────────────────────────────────────────────────────────┐
│                   交易公共表 (tms_deals_t)              │
│  所有交易类型共享，存储交易公共信息                        │
├─────────────────────────────────────────────────────────┤
│                   交易个性化表 (tms_xx_deals_t)         │
│  特定交易类型的个性化信息                                │
├─────────────────────────────────────────────────────────┤
│                   Action表 (tms_actions_t)              │
│ 审批作用对象，每次数据变化生成一条Action记录 │
├─────────────────────────────────────────────────────────┤
│                   公共镜像表 (tms_deals_image_t)         │
│  交易公共信息的版本历史 │
├─────────────────────────────────────────────────────────┤
│                   个性化镜像表 (tms_xx_deals_image_t)    │
│  交易个性化信息的版本历史                               │
└─────────────────────────────────────────────────────────┘
```

### 2.2 交易公共表字段模板

| 字段类型 | 字段名 | 类型 | 说明 |
|----------|--------|------|------|
| **主键** | dealId | BIGINT | 系统生成 |
| **编号** | dealNumber | VARCHAR(50) | 自动生成，格式: 类型 + yyyyMMdd + 序号 |
| **类型** | dealType | VARCHAR(20) | 交易类型代码：AC/AT/FX/ST等 |
| **业务字段** | businessUnit | VARCHAR(50) | 资金管理主体 |
| | counterpartyId | BIGINT | 交易对手 |
| | instrumentId | BIGINT | 金融工具 |
| | traderId | BIGINT | 交易员 |
| | direction | VARCHAR(10) | Inflow/Outflow |
| | amount | DECIMAL(38,18) | 交易金额，精度38,18 |
| | currency | VARCHAR(10) | 币种代码 |
| | dealDate | DATE | 交易日期 |
| | valueDate | DATE | 起息日/结算日 |
| **状态** | status | VARCHAR(20) | New/Submitted/Approved/Settled/Canceled |
| **扩展** | description | VARCHAR(500) | 交易描述 |
| | remark | VARCHAR(500) | 备注 |
| **Action关联** | latestActionNumber | VARCHAR(50) | 最新Action编号 |
| **审计字段** | createdBy/createdAt | - | 创建人/时间 |
| | updatedBy/updatedAt | - | 更新人/时间 |
| | version | INT | 版本号 |
| | deleted | CHAR(1) | 逻辑删除标记 |

### 2.3 Action表字段模板

| 字段类型 | 字段名 | 类型 | 说明 |
|----------|--------|------|------|
| **主键** | actionId | BIGINT | 系统生成 |
| **编号** | actionNumber | VARCHAR(50) | 自动生成，格式: ACT + yyyyMMdd + 序号 |
| **关联** | dealNumber | VARCHAR(50) | 关联交易编号（非UNIQUE，每次数据变化生成新Action） |
| | dealType | VARCHAR(20) | 交易类型 |
| **操作信息** | actionType | VARCHAR(20) | CREATE/UPDATE/DELETE/SUBMIT/APPROVE/REJECT/EXECUTE |
| | actionStatus | VARCHAR(20) | Pending/Approved/Rejected/Executed |
| | operator | VARCHAR(50) | 操作人 |
| | operateAt | DATETIME | 操作时间 |
| | remark | VARCHAR(500) | 动作备注 |
| **审批信息** | approver1 | VARCHAR(50) | 一级审批人 |
| | approver2 | VARCHAR(50) | 二级审批人 |
| | approvalStatus1 | VARCHAR(20) | 一级审批状态 |
| | approvalStatus2 | VARCHAR(20) | 二级审批状态 |
| | approvalRemark | VARCHAR(500) | 审批备注 |
| **审计字段** | createdBy/createdAt | - | 创建人/时间 |
| | updatedBy/updatedAt | - | 更新人/时间 |
| | version | INT | 版本号 |
| | deleted | CHAR(1) | 逻辑删除标记 |

### 2.4 镜像表字段模板

**公共镜像表**字段与交易公共表一一对应，新增字段：
| 字段 | 类型 | 说明 |
|------|------|------|
| imageId | BIGINT | 主键 |
| imageNumber | VARCHAR(50) | 镜像编号，格式: IMG + yyyyMMdd + 序号 |
| dealNumber | VARCHAR(50) | 关联交易编号 |
| dealType | VARCHAR(20) | 交易类型 |
| version | INT | 版本号，从1开始 |
| [交易公共表字段...] | - | 每个字段存储**变化前的旧值** |
| imageType | VARCHAR(20) | CREATE/UPDATE/DELETE |
| operator | VARCHAR(50) | 操作人 |
| operateAt | DATETIME | 操作时间 |
| createdBy/createdAt | - | 创建人/时间 |

**个性化镜像表**字段与交易个性化表一一对应。

---

## 三、Action机制设计

### 3.1 Action的核心原则

| 原则 | 说明 | 违反后果 |
|------|------|----------|
| **Action是审批的作用对象** | 审批人审批的是Action，不是交易 | 审批流程设计错误 |
| **数据变化创建Action** | CREATE/UPDATE/DELETE时创建新Action记录 | 审计记录不完整 |
| **状态流转更新Action** | SUBMIT/APPROVE/REJECT/EXECUTE只更新Action状态，不创建新Action | 数据冗余，性能浪费 |

### 3.2 Action与操作类型

| 操作类型 | actionType | 创建新Action | 生成镜像 | 说明 |
|----------|-----------|-------------|----------|------|
| **数据变化** | CREATE | ✅ | ✅ | 交易创建，version=1 |
| | UPDATE | ✅ | ✅ | 交易修改，version=n+1 |
| | DELETE | ✅ | ✅ | 交易删除，version=n+1 |
| **状态流转** | SUBMIT | ❌ | ❌ | 提交审批 |
| | APPROVE | ❌ | ❌ | 审批通过 |
| | REJECT | ❌ | ❌ | 审批拒绝 |
| | EXECUTE | ❌ | ❌ | 执行交易 |

### 3.3 Action状态机

```
                   ┌─────────┐
                    │ Pending │ ← 初始状态（交易创建时）
                    └────┬────┘
                         │
            ┌────────────┴────────────┐
            ▼                         ▼
     ┌──────────┐              ┌──────────┐
     │Approved │              │Rejected  │
     └────┬─────┘              └──────────┘
          │
          ▼
   ┌────────────┐
   │ Executed   │ ← 执行完成后
  └────────────┘
```

### 3.4 多级审批设计

| 层级 | 审批人字段 | 审批状态字段 | 说明 |
|------|-----------|--------------|------|
| 一级 | approver1 | approvalStatus1 | 必选 |
| 二级 | approver2 | approvalStatus2 | 可选（根据业务规则） |

**审批状态流转**：
```
Pending → Approved（审批通过）
Pending → Rejected（审批拒绝）
```

---

## 四、镜像机制设计

### 4.1 镜像的核心原则

| 原则 | 说明 | 重要性 |
|------|------|--------|
| **结构化存储** | 每个字段独立列，不存储JSON | ⭐⭐⭐⭐⭐ |
| **version变化条件** | 交易表任何变化（信息变化+状态变化）都要version+1 | ⭐⭐⭐⭐⭐ |
| **创建image条件** | 仅交易信息变化时创建新image，状态变化不创建image | ⭐⭐⭐⭐⭐ |
| **存储旧值** | 镜像记录的是变化前的值，而非新值 | ⭐⭐⭐⭐⭐ |
| **字段同步** | 交易表新增字段时，镜像表必须同步新增 | ⭐⭐⭐⭐⭐ |

### 4.2 version与Image的关系

**version变化条件**：交易表任何变化（信息变化+状态变化）都要version+1

**创建Image条件**：仅交易信息变化时才创建新Image，状态变化不创建Image

| 操作 | version变化 | 创建Image | 说明 |
|------|------------|-----------|------|
| CREATE | ✅ version=1 | ✅ | 交易创建，version+1，创建image |
| UPDATE | ✅ version=n+1 | ✅ | 交易修改，version+1，创建image（存储旧值） |
| DELETE | ✅ version=n+1 | ✅ | 交易删除，version+1，创建image（存储旧值） |
| SUBMIT | ✅ version=n+1 | ❌ | 状态变化，version+1，但不创建image |
| APPROVE | ✅ version=n+1 | ❌ | 状态变化，version+1，但不创建image |
| REJECT | ✅ version=n+1 | ❌ | 状态变化，version+1，但不创建image |
| EXECUTE | ✅ version=n+1 | ❌ | 状态变化，version+1，但不创建image |

### 4.3 镜像使用场景

**场景1：交易回溯**
```sql
-- 查询某笔交易的所有版本
SELECT * FROM tms_deals_image_t
WHERE dealNumber = 'DEAL202606010001'
ORDER BY version;

-- 查询某笔交易某个版本的具体信息
SELECT * FROM tms_deals_image_t
WHERE dealNumber = 'DEAL202606010001' AND version = 3;
```

**场景2：字段变更历史**
```sql
-- 查询某字段的历史变更
SELECT imageNumber, version, amount, operateAt
FROM tms_deals_image_t
WHERE dealNumber = 'DEAL202606010001' AND amount IS NOT NULL
ORDER BY version;
```

**场景3：版本对比**
```sql
-- 对比两个版本的差异
SELECT 'v' || v1.version as ver, v1.amount as old_value, v2.amount as new_value
FROM tms_deals_image_t v1
JOIN tms_deals_image_t v2 ON v1.dealNumber = v2.dealNumber AND v2.version = v1.version + 1
WHERE v1.dealNumber = 'DEAL202606010001';
```

---

## 五、审批流程设计

### 5.1 审批流程标准

```
┌─────────────────────────────────────────────────────────────┐
│                     审批流程标准 │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  交易创建 ──▶ Action(CREATE)创建，actionStatus='Pending'    │
│       │                                                     │
│       ▼                                                     │
│  提交审批 ──▶ 更新Action.actionType='SUBMIT'                │
│       │交易.status='Submitted'                      │
│       │                                                     │
│       ▼                                                     │
│  一级审批人审批Action │
│       │                                                     │
│       ├── 拒绝 ──▶ Action.approvalStatus1='Rejected'        │
│       │ actionStatus='Rejected'                   │
│       │          交易.status='Rejected'                   │
│       │                                                     │
│       └── 通过 ──▶ Action.approvalStatus1='Approved'        │
│                   │                                        │
│                   ├── 需要二级 ──▶ 二级审批                  │
│                   │                │                        │
│                   │                ├── 通过 ──▶ 交易.status='Approved'
│                   │                │                        │
│                   │                └── 拒绝 ──▶ 交易.status='Rejected'
│                   │                                        │
│                   └── 不需要二级 ──▶ 交易.status='Approved' │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### 5.2 审批规则判断

```java
// 提交审批时调用ApprovalRule判断审批层级
ApprovalRule rule = approvalRuleService.getRule(dealType, amount);
if (rule != null && rule.getApprovalLevels() > 0) {
    // 需要审批
    deal.setStatus("Submitted");
} else {
    // 不需要审批，自动通过
    deal.setStatus("Approved");
}
```

### 5.3 审批字段设计

**交易表**（冗余设计，用于查询展示）：
```sql
-- 交易表中可以冗余存储审批信息
ALTER TABLE tms_deals_t ADD COLUMN approver1 VARCHAR(50);
ALTER TABLE tms_deals_t ADD COLUMN approval_status1 VARCHAR(20);
```

**实际审批信息存储在Action表**：
- Action表是审批信息的权威来源
- 交易表审批字段是冗余，方便前端查询展示

---

## 六、新交易类型设计流程

### 6.1 设计检查清单

```
┌─────────────────────────────────────────────────────────────┐
│              新交易类型设计检查清单                           │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  1. 交易公共表设计 │
│     □ 确认dealType代码（如FX、IRS） │
│     □ 交易公共字段是否完整（见2.2模板）                        │
│     □ latestActionNumber字段                                │
│                                                             │
│  2. 交易个性化表设计                                         │
│     □ 个性化表名称（tms_xx_deals_t）                         │
│     □ 个性化字段清单 │
│     □ 与公共表的关联字段（dealNumber）                        │
│                                                             │
│  3. Action表设计                                             │
│     □ deal_number 非UNIQUE（每次数据变化生成新Action）        │
│     □ actionType枚举值完整 │
│     □ actionStatus枚举值完整                                 │
│     □ 审批字段（approver1/2, approvalStatus1/2）             │
│                                                             │
│  4. 镜像表设计                                               │
│     □ 公共镜像表字段与交易公共表一致 │
│     □ 个性化镜像表字段与交易个性化表一致                      │
│     □ imageNumber、version字段                              │
│                                                             │
│  5. 审批流程设计                                             │
│     □ 审批层级（是否需要多级审批）                            │
│     □ 审批规则（基于金额/类型等）                             │
│     □ 审批状态机 │
│                                                             │
│  6. 状态机设计                                               │
│     □ 状态列表（New/Submitted/Approved/Settled/Rejected/Canceled）│
│     □ 状态转换图 │
│     □ 各状态可执行操作                                       │
│                                                             │
│  7. API接口设计                                              │
│     □ CRUD接口 │
│     □ 状态流转接口（submit/approve/reject/execute/cancel）  │
│     □ Action查询接口                                         │
│     □ 镜像查询接口 │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### 6.2 设计文档模板

```markdown
# Open-TMS M1-{交易类型名称}交易 PRD

## 一、模块概述
- 交易类型代码：
- 功能定位：
- 用户角色：

## 二、交易公共字段
| 字段名 | 类型 | 必填 | 说明 |

## 三、交易个性化字段
| 字段名 | 类型 | 必填 | 说明 |

## 四、Action设计
| 操作 | actionType | 说明 |

## 五、镜像表设计
（字段与交易表一致）

## 六、审批流程
（多级审批规则）

## 七、状态机
（状态转换图）

## 八、API接口
（接口清单）

## 九、数据库设计
（DDL脚本）
```

---

## 七、常见问题与解决方案

### 7.1 Action设计常见问题

| 问题 | 原因 | 解决方案 |
|------|------|----------|
| 混淆version与Image | 未区分两者变化条件 | version任何变化都要+1，Image仅信息变化时创建 |
| SUBMIT创建新Action | 状态流转操作不应创建Action | SUBMIT只更新现有Action的actionType，不创建新Action |
| 审批信息存储在交易表 | 未理解Action是审批对象 | 将审批信息迁移到Action表，交易表只做冗余 |

### 7.2 镜像设计常见问题

| 问题 | 原因 | 解决方案 |
|------|------|----------|
| 镜像存储JSON | 结构化设计不完整 | 重构镜像表，按字段结构化存储 |
| version与Image混淆 | 状态变化也创建Image | 状态变化只更新version，不创建Image |
| 新增字段未同步 | 缺少同步机制 | 建立规范：交易表新增字段必须同步镜像表 |

### 7.3 审批设计常见问题

| 问题 | 原因 | 解决方案 |
|------|------|----------|
| 审批流程复杂 | 未抽象标准审批流程 | 采用标准三级审批（Pending/Approved/Rejected） |
| 审批规则分散 | 审批规则未统一管理 | 使用ApprovalRule服务统一管理 |

---

## 八、设计原则总结

### 8.1核心原则（必须遵守）

1. **Action是审批作用对象**：审批人审批的是Action，不是交易
2. **Action记录操作历史**：每次数据变化（创建/修改/删除）生成一条Action记录
3. **状态流转只更新Action**：SUBMIT/APPROVE/REJECT/EXECUTE只更新Action状态
4. **version变化条件**：交易表任何变化（信息变化+状态变化）都要version+1
5. **Image创建条件**：仅交易信息变化时创建新Image，状态变化不创建Image
6. **镜像结构化存储**：每个字段独立列，不存储JSON
7. **字段同步**：交易表新增字段，镜像表必须同步

### 8.2 设计原则（推荐遵守）

1. **公共表+个性化表分离**：交易公共信息与个性化信息分离
2. **审批字段冗余**：交易表冗余存储审批信息，方便查询
3. **版本号连续**：version从1开始，每次数据变化+1
4. **审计字段完整**：createdBy/createdAt/updatedBy/updatedAt/version/deleted

### 8.3 架构原则（高级设计）

1. **交易类型可扩展**：新交易类型只需新增个性化表
2. **审批规则可配置**：通过ApprovalRule服务配置审批层级
3. **状态机可配置**：通过规则引擎配置状态转换

---

## 九、附录

### 9.1 交易类型代码参考

| 交易类型 | 代码 | 说明 |
|----------|------|------|
| AC交易 | AC | Actual Cashflow - 纯粹资金收付 |
| AT交易 | AT | Account Transfer - 账户转账 |
| FX交易 | FX | Foreign Exchange - 外汇交易 |
| ST交易 | ST | Securities - 证券交易 |
| IRS | IRS | Interest Rate Swap - 利率互换 |
| CCS | CCS | Cross Currency Swap - 交叉货币互换 |
| 存款 | DEP | Deposit - 存款产品 |
| 贷款 | LNA | Loan - 贷款产品 |
| 保函 | LG | Letter of Guarantee - 保函 |
| 信用证 | LC | Letter of Credit - 信用证 |

### 9.2 编号格式参考

| 编号类型 | 格式 | 示例 |
|----------|------|------|
| 交易编号 | {类型} + yyyyMMdd + 序号(4位) | AC202606010001 |
| Action编号 | ACT + yyyyMMdd + 序号(4位) | ACT202606010001 |
| 镜像编号 | IMG + yyyyMMdd + 序号(4位) | IMG202606010001 |

### 9.3 状态码参考

| 状态 | 代码 | 说明 |
|------|------|------|
| 新建 | New | - |
| 已提交 | Submitted | - |
| 已审批 | Approved | - |
| 已结算 | Settled | 终态 |
| 已拒绝 | Rejected | - |
| 已取消 | Canceled | - |

### 9.4 操作类型参考

| 操作类型 | actionType | 说明 |
|----------|-----------|------|
| 创建 | CREATE | 数据变化操作 |
| 修改 | UPDATE | 数据变化操作 |
| 删除 | DELETE | 数据变化操作 |
| 提交 | SUBMIT | 状态流转操作 |
| 审批通过 | APPROVE | 状态流转操作 |
| 审批拒绝 | REJECT | 状态流转操作 |
| 执行 | EXECUTE | 状态流转操作 |

---

*Skill产出 - v1.0 (2026-06-01)*
*交易产品设计方法论沉淀，用于指导AC/AT/FX/IRS等各类交易的设计*
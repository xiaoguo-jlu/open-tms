# Open-TMS M1-交易录入与管理 PRD

**版本**: v1.2  
**角色**: 产品经理 (PM)  
**日期**: 2026-04-06

---

## 一、模块概述

**模块名称**: dealing - 交易录入与管理  
**功能定位**: 支持资金交易的手工录入、暂存、提交、查询、变更、撤销等全生命周期管理  
**用户角色**: 交易员、交易主管、资金经理  
**国际化**: 支持中英文界面，所有界面文字需支持多语言切换

---

## 1.1 国际化规范

### 1.1.1 界面语言支持

| 特性 | 要求 | 优先级 |
|------|------|--------|
| 中英文切换 | 界面所有文字支持中英文切换 | P0 |
| 语言切换入口 | 顶部导航栏提供语言切换入口 | P0 |
| 语言记忆 | 用户选择的语言偏好需持久化保存 | P0 |
| 实时切换 | 切换语言无需刷新页面 | P0 |
| 数字格式 | 按 locale 格式化数字(千分位/小数点) | P0 |
| 日期格式 | 按 locale 格式化日期(CN:yyyy-MM-dd/US:MM/dd/yyyy) | P0 |
| 货币格式 | 按 locale 格式化货币符号 | P0 |

### 1.1.2 多语言字段

所有用户可见的字段均需支持多语言：

| 字段类型 | 示例 | 处理方式 |
|----------|------|----------|
| 页面标题 | 交易录入 / Deal Entry | i18n key |
| 按钮文字 | 新建 / New | i18n key |
| 下拉选项 | 存款 / Deposit | i18n key |
| 状态文字 | 已审批 / Approved | i18n key |
| 提示信息 | 提交成功 / Submit Success | i18n key |
| 错误信息 | 金额必须大于0 / Amount must be greater than 0 | i18n key |

### 1.1.3 语言切换示例

```
交易列表 / Deal List
├─ 搜索条件 / Search Criteria
│   ├─ 交易对手 / Counterparty
│   ├─ 交易类型 / Deal Type
│   └─ 交易状态 / Status
├─ 操作列 / Actions
│   ├─ 新建 / New
│   ├─ 编辑 / Edit
│   └─ 删除 / Delete
└─ 状态 / Status
    ├─ 草稿 / Draft
    ├─ 待审批 / Pending Approval
    ├─ 已审批 / Approved
    └─ 已结算 / Settled
```  
**国际化**: 支持中英文界面，所有界面文字需支持多语言切换

---

## 二、交易类型定义

| 类型 | 子类型 | 说明 | 优先级 |
|------|--------|------|--------|
| **存款** | 定期存款、活期存款、大额存单、通知存款 | 资金存放银行 | P0 |
| **贷款** | 短期贷款、中长期贷款、信用贷款、抵押贷款 | 从银行借入资金 | P0 |
| **外汇** | 即期外汇、远期外汇、NDF、外汇掉期 | 外汇交易 | P0 |
| **同业** | 同业存放、存放同业、同业拆借 | 同业资金往来 | P0 |

---

## 三、功能清单

### 3.1 交易录入

| 功能 | 说明 | 优先级 |
|------|------|--------|
| 新建交易 | 选择交易类型，填写交易要素 | P0 |
| 交易模板 | 预设常用交易模板，快速创建 | P0 |
| 暂存 | 临时保存，不提交审批 | P0 |
| 提交审批 | 提交到审批流程 | P0 |
| 交易复制 | 复制已有交易创建新交易 | P1 |
| 批量导入 | Excel批量导入交易 | P1 |

### 3.2 交易公共字段

所有交易类型共用的核心字段：

| 字段 | 类型 | 必填 | 说明 | 选择方式 |
|------|------|------|------|----------|
| Deal No.(交易编号) | VARCHAR(50) | 系统 | 自动生成 | - |
| Deal Type(交易类型) | VARCHAR(20) | Y | 交易大类 | 下拉选择 |
| Deal Subtype(交易子类型) | VARCHAR(20) | Y | 交易小类 | 下拉选择 |
| Instrument(金融工具) | VARCHAR(50) | Y | 金融工具 | 弹出框选择 |
| Entity(资金管理主体) | VARCHAR(50) | Y | 业务单元 | 弹出框选择 |
| Dealer(交易员) | VARCHAR(50) | Y | 交易员 | 弹出框选择 |
| Counterparty(交易对手) | VARCHAR(50) | Y | 交易对手 | 弹出框选择 |
| Counterparty Account(对手账户) | VARCHAR(50) | Y | 对手方银行账户 | 弹出框选择 |
| Value Date(交割日) | DATE | Y | 价值日期 | 日期选择 |
| Maturity Date(到期日) | DATE | Y | 到期日期 | 日期选择 |
| Settlement Currency(结算币种) | VARCHAR(10) | Y | 结算币种 | 弹出框选择 |
| Settlement Amount(结算金额) | DECIMAL(18,2) | Y | 结算金额 | - |
| Status(状态) | VARCHAR(20) | 系统 | 交易状态 | - |
| Description(描述) | VARCHAR(500) | N | 交易描述 | - |
| Remarks(备注) | VARCHAR(1000) | N | 备注信息 | - |

### 3.3 交易要素字段

#### 3.3.1 存款交易

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| Deal No. | VARCHAR(50) | 系统 | 自动生成 |
| Deal Type | VARCHAR(20) | Y | 存款 |
| Deal Subtype | VARCHAR(20) | Y | 定期/活期/大额存单/通知 |
| Instrument | VARCHAR(50) | Y | 弹出框选择金融工具 |
| Entity | VARCHAR(50) | Y | 弹出框选择业务单元 |
| Dealer | VARCHAR(50) | Y | 弹出框选择交易员 |
| Counterparty | VARCHAR(50) | Y | 弹出框选择交易对手 |
| Counterparty Account | VARCHAR(50) | Y | 弹出框选择对手账户 |
| Settlement Currency | VARCHAR(10) | Y | 弹出框选择币种 |
| Settlement Amount | DECIMAL(18,2) | Y | 存款金额 |
| Value Date | DATE | Y | 起息日期 |
| Maturity Date | DATE | Y | 到期日期 |
| Interest Rate | DECIMAL(10,4) | Y | 年利率 |
| Interest Type | VARCHAR(20) | Y | 计息方式(定期/活期/按月付息) |
| Description | VARCHAR(500) | N | 描述 |
| Remarks | VARCHAR(1000) | N | 备注 |

#### 3.3.2 贷款交易

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| Deal No. | VARCHAR(50) | 系统 | 自动生成 |
| Deal Type | VARCHAR(20) | Y | 贷款 |
| Deal Subtype | VARCHAR(20) | Y | 短期/中长期/信用/抵押 |
| Instrument | VARCHAR(50) | Y | 弹出框选择金融工具 |
| Entity | VARCHAR(50) | Y | 弹出框选择业务单元 |
| Dealer | VARCHAR(50) | Y | 弹出框选择交易员 |
| Counterparty | VARCHAR(50) | Y | 弹出框选择贷款银行 |
| Settlement Currency | VARCHAR(10) | Y | 弹出框选择币种 |
| Settlement Amount | DECIMAL(18,2) | Y | 贷款本金 |
| Value Date | DATE | Y | 放款日期 |
| Maturity Date | DATE | Y | 到期日期 |
| Interest Rate | DECIMAL(10,4) | Y | 年利率 |
| Repayment Method | VARCHAR(20) | Y | 还款方式 |
| Guarantee Type | VARCHAR(20) | N | 担保方式 |

#### 3.3.3 外汇交易(即期)

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| Deal No. | VARCHAR(50) | 系统 | 自动生成 |
| Deal Type | VARCHAR(20) | Y | 外汇 |
| Deal Subtype | VARCHAR(20) | Y | 即期 |
| Instrument | VARCHAR(50) | Y | 弹出框选择金融工具 |
| Entity | VARCHAR(50) | Y | 弹出框选择业务单元 |
| Dealer | VARCHAR(50) | Y | 弹出框选择交易员 |
| Counterparty | VARCHAR(50) | Y | 弹出框选择交易对手 |
| Buy Currency | VARCHAR(10) | Y | 买入币种 |
| Sell Currency | VARCHAR(10) | Y | 卖出币种 |
| Amount | DECIMAL(18,2) | Y | 源币种金额 |
| Exchange Rate | DECIMAL(18,8) | Y | 交易汇率 |
| Value Date | DATE | Y | 交割日期 |

#### 3.3.4 外汇交易(远期)

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| Deal No. | VARCHAR(50) | 系统 | 自动生成 |
| Deal Type | VARCHAR(20) | Y | 外汇 |
| Deal Subtype | VARCHAR(20) | Y | 远期/NDF |
| Instrument | VARCHAR(50) | Y | 弹出框选择金融工具 |
| Entity | VARCHAR(50) | Y | 弹出框选择业务单元 |
| Dealer | VARCHAR(50) | Y | 弹出框选择交易员 |
| Counterparty | VARCHAR(50) | Y | 弹出框选择交易对手 |
| Buy Currency | VARCHAR(10) | Y | 买入币种 |
| Sell Currency | VARCHAR(10) | Y | 卖出币种 |
| Amount | DECIMAL(18,2) | Y | 源币种金额 |
| Forward Rate | DECIMAL(18,8) | Y | 远期汇率 |
| Value Date | DATE | Y | 起息日期 |
| Maturity Date | DATE | Y | 到期日期 |

#### 3.3.5 同业存放

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| Deal No. | VARCHAR(50) | 系统 | 自动生成 |
| Deal Type | VARCHAR(20) | Y | 同业 |
| Deal Subtype | VARCHAR(20) | Y | 同业存放/存放同业/同业拆借 |
| Instrument | VARCHAR(50) | Y | 弹出框选择金融工具 |
| Entity | VARCHAR(50) | Y | 弹出框选择业务单元 |
| Dealer | VARCHAR(50) | Y | 弹出框选择交易员 |
| Counterparty | VARCHAR(50) | Y | 弹出框选择交易对手(银行) |
| Settlement Currency | VARCHAR(10) | Y | 弹出框选择币种 |
| Settlement Amount | DECIMAL(18,2) | Y | 交易金额 |
| Value Date | DATE | Y | 起息日期 |
| Maturity Date | DATE | Y | 到期日期 |
| Interest Rate | DECIMAL(10,4) | Y | 年利率 |
| Interest Type | VARCHAR(20) | Y | 按月付息/到期付息 |

### 3.4 字段选择器交互

#### 3.4.1 选择器通用规则

| 字段 | 选择器类型 | 数据来源 |
|------|------------|----------|
| Entity | 弹出框 | 基础数据-业务单元列表 |
| Dealer | 弹出框 | 基础数据-交易员列表 |
| Counterparty | 弹出框 | 基础数据-交易对手列表 |
| Counterparty Account | 弹出框 | 基础数据-对手方银行账户(关联对手) |
| Instrument | 弹出框 | 金融工具-产品类型列表 |
| Settlement Currency | 下拉/弹出框 | 基础数据-币种列表 |

#### 3.4.2 弹出框交互规范

1. **弹出框内容**: 分页展示可选数据列表，支持搜索过滤
2. **展示字段**: 至少展示编号、名称两个关键字段
3. **支持模糊搜索**: 输入关键词可匹配编号或名称
4. **选中回填**: 选中后自动回填到对应字段
5. **关联过滤**: 如对手账户根据已选对手自动过滤

### 3.5 交易页面布局

交易详情页面包含以下页签：

| 页签 | 说明 | 优先级 |
|------|------|--------|
| 基本信息 | 交易核心要素字段 | P0 |
| 现金流(Cashflow) | 交易产生的现金流明细 | P0 |
|  Deal Map | 交易与现金流映射关系 | P0 |
| 审批历史 | 审批流程轨迹 | P0 |
| 变更记录 | 交易变更历史 | P1 |

### 3.6 交易状态

| 状态 | 说明 | 可操作 |
|------|------|--------|
| 草稿 | 暂存状态，未提交 | 编辑/提交/删除 |
| 待审批 | 已提交，等待审批 | 撤回 |
| 审批中 | 审批中(多级审批) | - |
| 已审批 | 审批通过 | 执行/变更/撤销 |
| 已驳回 | 审批驳回 | 编辑/重新提交 |
| 已执行 | 已执行待结算 | - |
| 已结算 | 交易已完成 | - |
| 已撤销 | 交易已撤销 | - |

### 3.7 交易变更

| 功能 | 说明 | 优先级 |
|------|------|--------|
| 要素变更 | 修改交易要素(审批中/已审批) | P0 |
| 变更记录 | 记录所有字段变更历史 | P0 |
| 变更审批 | 变更需重新审批 | P0 |

### 3.8 交易撤销

| 功能 | 说明 | 优先级 |
|------|------|--------|
| 交易撤销 | 撤销未结算交易 | P0 |
| 撤销原因 | 记录撤销原因 | P0 |
| 冲销处理 | 已结算交易冲销处理 | P1 |

### 3.9 交易查询

| 功能 | 说明 | 优先级 |
|------|------|--------|
| 交易列表 | 分页展示交易 | P0 |
| 条件查询 | 按Entity/Dealer/Counterparty/状态/日期等查询 | P0 |
| 交易详情 | 查看交易完整信息(含各页签) | P0 |
| 交易导出 | Excel导出交易数据 | P0 |
| 交易筛选器 | 保存常用查询条件 | P1 |

### 3.10 交易复核

| 功能 | 说明 | 优先级 |
|------|------|--------|
| 复核机制 | 交易员录入，复核员复核 | P0 |
| 复核通过 | 复核通过后可提交审批 | P0 |
| 复核驳回 | 复核驳回可修改重新提交 | P0 |

---

## 四、业务规则

1. **编号规则**: Deal No. = Entity编码 + 日期 + 序号，如 `BJ202604050001`
2. **字段选择**: Entity/Dealer/Counterparty/Counterparty Account/Instrument必须通过弹出框从基础数据选择
3. **金额校验**: 交易金额需大于0，精度匹配币种小数位数
4. **日期校验**: 
   - Value Date需大于等于当前日期
   - Maturity Date需大于Value Date
   - 交割日需跳过节假日
5. **限额检查**: 交易金额不能超过对手方授信额度
6. **汇率校验**: 外汇交易汇率需在合理范围内
7. **利息计算**: 定期存款按约定利率计算
8. **数据权限**: 用户只能查看/操作有权限的Entity数据

---

## 五、验收标准

| 功能 | 验收条件 |
|------|----------|
| 新建交易 | 通过弹出框选择Entity/Dealer/Counterparty/Instrument，所有必填字段校验，提交成功 |
| 暂存 | 暂存后可在列表中找到，状态为草稿 |
| 交易复制 | 复制后生成新交易编号，相关字段复制 |
| 审批流转 | 多级审批正确流转，审批历史完整 |
| 状态变更 | 状态变更正确，消息通知到位 |
| 查询导出 | 多条件组合查询，导出Excel正常 |
| 交易变更 | 变更后生成变更记录，需审批 |
| 交易撤销 | 撤销后状态正确，记录撤销原因 |
| 权限控制 | 无权限数据不可见 |
| 页签功能 | Cashflow/Deal Map页签正确展示数据 |

---

## 六、接口清单

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/dealing/create | POST | 创建交易 |
| /api/dealing/update | PUT | 修改交易 |
| /api/dealing/submit | POST | 提交审批 |
| /api/dealing/approve | POST | 审批通过 |
| /api/dealing/reject | POST | 审批驳回 |
| /api/dealing/query | GET | 查询交易列表 |
| /api/dealing/{id} | GET | 交易详情(含Cashflow/DealMap) |
| /api/dealing/export | GET | 导出交易 |
| /api/dealing/change | POST | 交易变更 |
| /api/dealing/cancel | POST | 交易撤销 |
| /api/dealing/copy | POST | 交易复制 |
| /api/dealing/cashflow/{dealId} | GET | 获取交易关联现金流 |
| /api/dealing/dealmap/{dealId} | GET | 获取交易DealMap |

---

## 七、页面原型(UX待设计)

(待UX输出页面原型)

---

## 八、与其他模块的关系

| 模块 | 关系 |
|------|------|
| 金融工具 | Instrument从金融工具模块选择 |
| 业务单元 | Entity从组织架构模块选择 |
| 交易员 | Dealer从基础数据模块选择 |
| 交易对手 | Counterparty从基础数据模块选择 |
| 对手方账户 | Counterparty Account从对手方银行账户选择 |
| AC交易 | 交易执行后自动生成Cashflow |

---

*PM产出 - M1 v1.1*

# Open-TMS M1-AC交易基础数据 PRD

**版本**: v1.1  
**角色**: 产品经理 (PM)  
**日期**: 2026-04-06

---

## 一、模块概述

**模块名称**: cashflow-base - AC交易基础数据管理  
**功能定位**: 管理AC(实际现金流)交易涉及的基础数据，包括现金流类型、来源类型、映射规则等  
**用户角色**: IT运维人员、资金管理人员  
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

### 1.1.2 多语言字段

所有用户可见的字段均需支持多语言：

| 字段类型 | 中文示例 | 英文示例 |
|----------|----------|----------|
| 页面标题 | 现金流类型 | Cashflow Type |
| 功能按钮 | 新增 / 编辑 / 删除 | Add / Edit / Delete |
| 列标题 | 类型编号 / 类型名称 | Type Code / Type Name |
| 下拉选项 | 流入 / 流出 | Inflow / Outflow |
| 状态 | 启用 / 停用 | Enabled / Disabled |
| 提示信息 | 操作成功 | Operation Success |
| 错误信息 | 编号已存在 | Code Already Exists |

---

## 二、基础数据类型

### 2.1 现金流类型(Cashflow Type)

| 功能 | 说明 / Function | 优先级 |
|------|------------------|--------|
| 列表查询 / List Query | 分页展示现金流类型 | P0 |
| 新增 / Create | 创建现金流类型 | P0 |
| 编辑 / Edit | 修改现金流类型 | P0 |
| 删除 / Delete | 逻辑删除 | P0 |
| 导入 / Import | Excel批量导入 | P1 |
| 导出 / Export | Excel导出 | P0 |

**字段定义 / Field Definition**:
| 字段 / Field | 类型 / Type | 必填 / Required | 说明 / Description |
|--------------|-------------|----------------|---------------------|
| 类型编号 / Type Code | VARCHAR(50) | Y | 唯一编码，如CF_INFLOW/CF_OUTFLOW |
| 类型名称(中) / Name CN | VARCHAR(100) | Y | 中文名称，如流入/流出 |
| 类型名称(英) / Name EN | VARCHAR(100) | Y | 英文名称，如Inflow/Outflow |
| 类型值 / Type Value | VARCHAR(10) | Y | Inflow/Outflow |
| 说明 / Description | VARCHAR(500) | N | 描述 |
| 状态 / Status | CHAR(1) | Y | 0-停用 Disabled / 1-启用 Enabled |
| 创建人 / Created By | VARCHAR(50) | 系统 | - |
| 创建时间 / Creation Date | DATETIME | 系统 | - |
| 修改人 / Last Updated By | VARCHAR(50) | 系统 | - |
| 修改时间 / Last Update Date | DATETIME | 系统 | - |

### 2.2 交易来源类型(Source Type)

| 功能 | 说明 / Function | 优先级 |
|------|------------------|--------|
| 列表查询 / List Query | 分页展示来源类型 | P0 |
| 新增 / Create | 创建来源类型 | P0 |
| 编辑 / Edit | 修改来源类型 | P0 |
| 删除 / Delete | 逻辑删除 | P0 |
| 导入 / Import | Excel批量导入 | P1 |
| 导出 / Export | Excel导出 | P0 |

**字段定义 / Field Definition**:
| 字段 / Field | 类型 / Type | 必填 / Required | 说明 / Description |
|--------------|-------------|----------------|---------------------|
| 来源编号 / Source Code | VARCHAR(50) | Y | 唯一编码 |
| 来源名称(中) / Name CN | VARCHAR(100) | Y | 中文名称 |
| 来源名称(英) / Name EN | VARCHAR(100) | Y | 英文名称 |
| 来源值 / Source Value | VARCHAR(20) | Y | Bank Transfer/Statement/Sweep/Cash Leveling |
| 说明 / Description | VARCHAR(500) | N | 描述 |
| 状态 / Status | CHAR(1) | Y | 0-停用 Disabled / 1-启用 Enabled |

### 2.3 现金流子类型(Cashflow Subtype)

| 功能 | 说明 / Function | 优先级 |
|------|------------------|--------|
| 列表查询 / List Query | 分页展示子类型 | P0 |
| 新增 / Create | 创建子类型 | P0 |
| 编辑 / Edit | 修改子类型 | P0 |
| 删除 / Delete | 逻辑删除 | P0 |

**字段定义 / Field Definition**:
| 字段 / Field | 类型 / Type | 必填 / Required | 说明 / Description |
|--------------|-------------|----------------|---------------------|
| 子类型编号 / Subtype Code | VARCHAR(50) | Y | 唯一编码 |
| 子类型名称(中) / Name CN | VARCHAR(100) | Y | 中文名称 |
| 子类型名称(英) / Name EN | VARCHAR(100) | Y | 英文名称 |
| 现金流类型 / Cashflow Type | VARCHAR(10) | Y | 关联Inflow/Outflow |
| 缺省流向 / Default Direction | VARCHAR(10) | Y | 默认流向 |
| 状态 / Status | CHAR(1) | Y | 0-停用 Disabled / 1-启用 Enabled |

### 2.4 银行交易码映射规则(Bank Code Mapping)

| 功能 | 说明 / Function | 优先级 |
|------|------------------|--------|
| 列表查询 / List Query | 分页展示映射规则 | P0 |
| 新增 / Create | 创建映射规则 | P0 |
| 编辑 / Edit | 修改映射规则 | P0 |
| 删除 / Delete | 删除映射规则 | P0 |
| 导入 / Import | Excel批量导入 | P1 |
| 导出 / Export | Excel导出 | P0 |

**字段定义 / Field Definition**:
| 字段 / Field | 类型 / Type | 必填 / Required | 说明 / Description |
|--------------|-------------|----------------|---------------------|
| 规则编号 / Rule Code | VARCHAR(50) | Y | 唯一编码 |
| 银行账户 / Bank Account | VARCHAR(50) | Y | 关联银行账户(弹出框选择) |
| 银行交易码 / Bank Transaction Code | VARCHAR(20) | Y | 银行对账单交易码 |
| 搜索字符串 / Search String | VARCHAR(100) | N | 描述匹配字符串 |
| 现金流类型 / Cashflow Type | VARCHAR(10) | Y | 流入/流出(弹出框选择) |
| 现金流子类型 / Cashflow Subtype | VARCHAR(50) | Y | 弹出框选择子类型 |
| 对手方账户 / Counterparty Account | VARCHAR(50) | N | 缺省对手方账户(弹出框选择) |
| 对方名称(中) / Counterparty Name CN | VARCHAR(200) | N | 缺省对手方名称(中文) |
| 对方名称(英) / Counterparty Name EN | VARCHAR(200) | N | 缺省对手方名称(英文) |
| 用途(中) / Purpose CN | VARCHAR(500) | N | 缺省用途(中文) |
| 用途(英) / Purpose EN | VARCHAR(500) | N | 缺省用途(英文) |
| 状态 / Status | CHAR(1) | Y | 0-停用 Disabled / 1-启用 Enabled |

### 2.5 清分规则(Clear Rule)

| 功能 | 说明 / Function | 优先级 |
|------|------------------|--------|
| 列表查询 / List Query | 分页展示清分规则 | P0 |
| 新增 / Create | 创建清分规则 | P0 |
| 编辑 / Edit | 修改清分规则 | P0 |
| 删除 / Delete | 删除清分规则 | P0 |

**字段定义 / Field Definition**:
| 字段 / Field | 类型 / Type | 必填 / Required | 说明 / Description |
|--------------|-------------|----------------|---------------------|
| 规则编号 / Rule Code | VARCHAR(50) | Y | 唯一编码 |
| 规则名称(中) / Rule Name CN | VARCHAR(100) | Y | 规则名称(中文) |
| 规则名称(英) / Rule Name EN | VARCHAR(100) | Y | 规则名称(英文) |
| 银行账户 / Bank Account | VARCHAR(50) | Y | 关联银行账户(弹出框选择) |
| 匹配字段 / Match Field | VARCHAR(20) | Y | 银行参考号/金额/对手方 / Bank Ref/Amount/Counterparty |
| 匹配方式 / Match Type | VARCHAR(20) | Y | 精确匹配 Exact / 模糊匹配 Fuzzy |
| 优先级 / Priority | INT | Y | 优先级(数字越小越优先) |
| 自动清分 / Auto Clear | CHAR(1) | Y | 0-手工 Manual / 1-自动 Auto |
| 状态 / Status | CHAR(1) | Y | 0-停用 Disabled / 1-启用 Enabled |

### 2.6 对账规则(Reconcile Rule)

| 功能 | 说明 / Function | 优先级 |
|------|------------------|--------|
| 列表查询 / List Query | 分页展示对账规则 | P0 |
| 新增 / Create | 创建对账规则 | P0 |
| 编辑 / Edit | 修改对账规则 | P0 |
| 删除 / Delete | 删除对账规则 | P0 |

**字段定义 / Field Definition**:
| 字段 / Field | 类型 / Type | 必填 / Required | 说明 / Description |
|--------------|-------------|----------------|---------------------|
| 规则编号 / Rule Code | VARCHAR(50) | Y | 唯一编码 |
| 规则名称(中) / Rule Name CN | VARCHAR(100) | Y | 规则名称(中文) |
| 规则名称(英) / Rule Name EN | VARCHAR(100) | Y | 规则名称(英文) |
| 银行账户 / Bank Account | VARCHAR(50) | Y | 关联银行账户(弹出框选择) |
| 对账维度 / Reconcile Dimension | VARCHAR(20) | Y | 按金额 Amount / 按笔数 Count / 全额 Full |
| 差异容忍度 / Tolerance Amount | DECIMAL(18,2) | N | 允许的差异金额 |
| 自动对账 / Auto Reconcile | CHAR(1) | Y | 0-手工 Manual / 1-自动 Auto |
| 状态 / Status | CHAR(1) | Y | 0-停用 Disabled / 1-启用 Enabled |

---

## 三、选择器交互规范 / Selector Interaction

### 3.1 银行账户选择器 / Bank Account Selector

- **触发方式 / Trigger**: 点击输入框弹出选择框 / Click input to open selector
- **数据来源 / Data Source**: 银行账户管理模块 / Bank Account Management
- **展示字段 / Display Fields**: 账户编号/账户名称/开户银行 / Account No./Account Name/Bank Name
- **搜索支持 / Search**: 支持按账户编号/名称模糊搜索 / Support fuzzy search by code/name

### 3.2 现金流类型选择器 / Cashflow Type Selector

- **触发方式 / Trigger**: 下拉选择 / Dropdown
- **数据来源 / Data Source**: 2.1 现金流类型 / Cashflow Type
- **选项值 / Options**: Inflow / Outflow

### 3.3 现金流子类型选择器 / Cashflow Subtype Selector

- **触发方式 / Trigger**: 点击输入框弹出选择框 / Click input to open selector
- **数据来源 / Data Source**: 2.3 现金流子类型 / Cashflow Subtype
- **级联过滤 / Cascade Filter**: 根据已选现金流类型过滤 / Filter by selected cashflow type

### 3.4 对手方账户选择器 / Counterparty Account Selector

- **触发方式 / Trigger**: 点击输入框弹出选择框 / Click input to open selector
- **数据来源 / Data Source**: 基础数据-对手方银行账户 / Base Data - Counterparty Account
- **级联过滤 / Cascade Filter**: 根据已选银行账户过滤 / Filter by selected bank account

---

## 四、通用功能定义 / Common Functions

### 4.1 分页查询 / Pagination Query

| 功能 / Function | 说明 / Description |
|-----------------|---------------------|
| 分页大小 / Page Size | 默认20条，可选10/20/50/100 |
| 排序 / Sorting | 支持按创建时间/名称等字段排序 |
| 条件筛选 / Filter | 支持多字段组合查询 |

### 4.2 明细查询 / Detail Query

- 点击列表行查看详情 / Click row to view detail
- 详情页展示所有字段 / Detail page shows all fields

### 4.3 新增 / Create

- 弹出表单填写 / Popup form
- 必填字段校验 / Required field validation
- 编码唯一性校验 / Code uniqueness validation

### 4.4 修改 / Update

- 点击编辑按钮 / Click edit button
- 修改后保存 / Save after modification
- 修改记录日志 / Modification log

### 4.5 删除 / Delete

- 逻辑删除(状态置为停用) / Logical delete (set status to disabled)
- 关联数据检查 / Associated data check

### 4.6 导入 / Import

- Excel文件上传 / Excel file upload
- 模板下载 / Template download
- 数据校验 / Data validation
- 错误报告 / Error report

### 4.7 导出 / Export

- Excel导出 / Excel export
- 支持全量/选中导出 / Support full/selected export

---

## 五、业务规则 / Business Rules

1. **编码唯一性 / Code Uniqueness**: 所有基础数据编号均需唯一
2. **状态控制 / Status Control**: 停用状态的数据不可引用
3. **级联删除 / Cascade Delete**: 删除银行账户时检查关联映射规则
4. **优先级 / Priority**: 清分规则按优先级顺序匹配
5. **默认值 / Default Value**: 新增时状态默认为启用 Enabled
6. **多语言 / Multi-language**: 名称字段需同时维护中英文

---

## 六、验收标准 / Acceptance Criteria

| 功能 / Function | 验收条件 / Acceptance Criteria |
|-----------------|--------------------------------|
| 分页查询 / Pagination | 分页正常，排序筛选正确 |
| 明细查询 / Detail Query | 点击行展示详情 |
| 新增 / Create | 字段校验，编码唯一性，中英文名称必填 |
| 修改 / Update | 修改保存，变更记录 |
| 删除 / Delete | 逻辑删除，关联检查 |
| 导入 / Import | Excel导入成功，格式校验 |
| 导出 / Export | Excel导出正常 |
| 选择器 / Selector | 弹出框选择，数据回填 |
| 国际化 / i18n | 界面所有文字支持中英文切换 |

---

## 七、接口清单 / API List

### 7.1 现金流类型 / Cashflow Type

| 接口 / API | 方法 / Method | 说明 / Description |
|------------|---------------|---------------------|
| /api/cashflow/type/query | GET | 分页查询 / Pagination Query |
| /api/cashflow/type/{id} | GET | 明细查询 / Detail Query |
| /api/cashflow/type/create | POST | 新增 / Create |
| /api/cashflow/type/update | PUT | 修改 / Update |
| /api/cashflow/type/delete | DELETE | 删除 / Delete |
| /api/cashflow/type/export | GET | 导出 / Export |
| /api/cashflow/type/import | POST | 导入 / Import |

### 7.2 来源类型 / Source Type

| 接口 / API | 方法 / Method | 说明 / Description |
|------------|---------------|---------------------|
| /api/cashflow/source/query | GET | 分页查询 |
| /api/cashflow/source/create | POST | 新增 |
| /api/cashflow/source/update | PUT | 修改 |
| /api/cashflow/source/delete | DELETE | 删除 |
| /api/cashflow/source/export | GET | 导出 |

### 7.3 映射规则 / Mapping Rule

| 接口 / API | 方法 / Method | 说明 / Description |
|------------|---------------|---------------------|
| /api/cashflow/mapping/query | GET | 分页查询 |
| /api/cashflow/mapping/create | POST | 新增 |
| /api/cashflow/mapping/update | PUT | 修改 |
| /api/cashflow/mapping/delete | DELETE | 删除 |
| /api/cashflow/mapping/export | GET | 导出 |
| /api/cashflow/mapping/import | POST | 导入 |

### 7.4 清分规则 / Clear Rule

| 接口 / API | 方法 / Method | 说明 / Description |
|------------|---------------|---------------------|
| /api/cashflow/clear/rule/query | GET | 分页查询 |
| /api/cashflow/clear/rule/create | POST | 新增 |
| /api/cashflow/clear/rule/update | PUT | 修改 |
| /api/cashflow/clear/rule/delete | DELETE | 删除 |

### 7.5 对账规则 / Reconcile Rule

| 接口 / API | 方法 / Method | 说明 / Description |
|------------|---------------|---------------------|
| /api/cashflow/reconcile/rule/query | GET | 分页查询 |
| /api/cashflow/reconcile/rule/create | POST | 新增 |
| /api/cashflow/reconcile/rule/update | PUT | 修改 |
| /api/cashflow/reconcile/rule/delete | DELETE | 删除 |

---

## 八、与其他模块的关系 / Relationship with Other Modules

| 模块 / Module | 关系 / Relationship |
|---------------|---------------------|
| 银行账户管理 / Bank Account | 映射规则/清分规则/对账规则关联银行账户 |
| AC交易 / AC Transaction | 基础数据用于AC交易的分类和处理 |

---

## 九、页面原型(UX待设计) / Wireframe (To be designed)

(待UX输出页面原型 / To be provided by UX)

---

*PM产出 - M1 v1.1*

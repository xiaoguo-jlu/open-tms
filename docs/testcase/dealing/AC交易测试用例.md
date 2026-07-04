# Open-TMS AC 交易测试用例（v2.0 - DealMap 自动生成版）

**模块**: dealing
**版本**: v2.0
**日期**: 2026-06-21
**测试工程师**: QA
**基于**: DealMap PRD v2.0

---

## 一、测试范围与目标

### 1.1 范围

| 模块 | 功能 | 接口 |
|------|------|------|
| AC 交易 | 列表/详情/创建/更新/删除 | `/api/v1/dealing/ac-deals/*` |
| Action | 列表/审批/驳回 | `/api/v1/dealing/actions/*` |
| DealMap | 时间线查询/冲销 | `/api/v1/dealing/dealmap/*` |
| UI | 列表/详情/审批 | `AcDealList.vue` / `AcDealDetail.vue` |

### 1.2 v2.0 关键验收点

| # | 验收点 | 验证方式 |
|---|--------|----------|
| AP-1 | CREATE 后自动生成 DealMap(ActualCashflow) | API + DB 验证 |
| AP-2 | CREATE 后自动生成 Cashflow（关联 dealmap_number） | API + DB 验证 |
| AP-3 | CREATE 不生成 DealImage | DB 验证 |
| AP-4 | UPDATE 软删旧 DealMap + 新建 | API + DB 验证 |
| AP-5 | UPDATE UPDATE Cashflow.dealmap_number 指向新 DealMap | DB 验证 |
| AP-6 | UPDATE 生成 DealImage v+1 | DB 验证 |
| AP-7 | DELETE 级联软删 Deal/AcDeal/DealMap/Cashflow | DB 验证 |
| AP-8 | DELETE 生成 DealImage v+1 | DB 验证 |
| AP-9 | 审批不改变 DealMap/Cashflow 状态 | API + DB 验证 |
| AP-10 | Action 多对一（一笔 Deal 多个 Action） | DB 验证 |
| AP-11 | AC/AT 操作只有 save/delete/approve/reject | UI 验证 |

### 1.3 优先级

- P0: 核心 CRUD + 业务正确性
- P1: 边界/异常/审批
- P2: UI 交互/性能

---

## 二、API 测试用例汇总

| 用例编号 | 用例名称 | 类型 | 优先级 |
|----------|----------|------|--------|
| TC_AC_API_001 | 分页查询 AC 交易-正常 | API | P0 |
| TC_AC_API_002 | 分页查询-关键字搜索 | API | P1 |
| TC_AC_API_003 | 分页查询-状态筛选 | API | P1 |
| TC_AC_API_004 | 获取详情-按 ID | API | P0 |
| TC_AC_API_005 | 获取详情-按 dealNumber | API | P0 |
| TC_AC_API_006 | 获取详情-不存在 | API | P1 |
| TC_AC_API_007 | **创建 AC 交易-正常** | API | P0 |
| TC_AC_API_008 | **创建后 DealMap 自动生成** | API | P0 |
| TC_AC_API_009 | **创建后 Cashflow 自动生成** | API | P0 |
| TC_AC_API_010 | **创建不生成 DealImage** | API | P0 |
| TC_AC_API_011 | 创建-必填项缺失 | API | P0 |
| TC_AC_API_012 | 创建-金额=0 | API | P0 |
| TC_AC_API_013 | 创建-金额<0 | API | P0 |
| TC_AC_API_014 | 创建-起息日早于交易日期 | API | P0 |
| TC_AC_API_015 | 创建-方向非法 | API | P0 |
| TC_AC_API_016 | **更新 AC 交易-软删旧 DealMap + 新建** | API | P0 |
| TC_AC_API_017 | **更新-Cashflow.dealmap_number 更新到新 DealMap** | API | P0 |
| TC_AC_API_018 | **更新-生成 DealImage v+1** | API | P0 |
| TC_AC_API_019 | 更新-未传 dealNumber | API | P0 |
| TC_AC_API_020 | **删除 AC 交易-级联软删 Deal/AcDeal/DealMap/Cashflow** | API | P0 |
| TC_AC_API_021 | **删除-生成 DealImage v+1** | API | P0 |
| TC_AC_API_022 | 删除-不存在 ID | API | P1 |
| TC_AC_API_023 | 查询 Action 列表-按 dealNumber | API | P0 |
| TC_AC_API_024 | **Action 多对一（一笔 Deal 多个 Action）** | API | P0 |
| TC_AC_API_025 | 查询待审批 Action 列表 | API | P1 |
| TC_AC_API_026 | **审批通过 Action** | API | P0 |
| TC_AC_API_027 | **审批不改变 DealMap/Cashflow 状态** | API | P0 |
| TC_AC_API_028 | 审批驳回 Action-正常 | API | P0 |
| TC_AC_API_029 | 审批驳回-审批意见为空 | API | P0 |
| TC_AC_API_030 | 审批-不存在的 actionNumber | API | P1 |
| TC_AC_API_031 | DealMap 时间线查询 | API | P0 |
| TC_AC_API_032 | DealMap 分页查询 | API | P1 |
| TC_AC_API_033 | DealMap 冲销 | API | P1 |

---

## 三、UI 测试用例汇总

| 用例编号 | 用例名称 | 优先级 |
|----------|----------|--------|
| TC_AC_UI_001 | 列表页加载 | P0 |
| TC_AC_UI_002 | 列表页查询筛选 | P1 |
| TC_AC_UI_003 | 新建 AC 交易-抽屉打开 | P0 |
| TC_AC_UI_004 | 新建-表单实时校验 | P0 |
| TC_AC_UI_005 | 新建-成功提示展示 v2.0 自动生成摘要 | P1 |
| TC_AC_UI_006 | 编辑 AC 交易-抽屉带回显 | P0 |
| TC_AC_UI_007 | 删除 AC 交易-二次确认 | P0 |
| TC_AC_UI_008 | 详情页-基本信息 Tab | P0 |
| TC_AC_UI_009 | 详情页-DealMap 时间线 Tab | P0 |
| TC_AC_UI_010 | 详情页-现金流 Tab | P0 |
| TC_AC_UI_011 | 详情页-操作历史 Tab | P0 |
| TC_AC_UI_012 | 审批弹窗-Action 列表 | P0 |
| TC_AC_UI_013 | 审批弹窗-多选审批 | P0 |
| TC_AC_UI_014 | 审批弹窗-驳回审批意见必填 | P1 |

---

## 四、详细测试用例

### 4.1 API - 列表查询

#### TC_AC_API_001 分页查询 AC 交易-正常

| 项目 | 内容 |
|------|------|
| 接口 | `GET /api/v1/dealing/ac-deals/page?pageNum=1&pageSize=10` |
| 优先级 | P0 |

**前置条件**：DB 中至少有 1 条 deal_type='AC' 的 Deal 记录

**预期结果**：
- code = 200
- data.records 为 AC 交易列表
- data.total > 0

#### TC_AC_API_002 分页查询-关键字搜索

| 项目 | 内容 |
|------|------|
| 接口 | `GET /api/v1/dealing/ac-deals/page?keyword=AC202606210001` |
| 优先级 | P1 |

**预期结果**：
- data.records 中所有 dealNumber 包含关键字
- data.total ≥ 1

#### TC_AC_API_003 分页查询-状态筛选

| 项目 | 内容 |
|------|------|
| 接口 | `GET /api/v1/dealing/ac-deals/page?status=New` |
| 优先级 | P1 |

**预期结果**：
- data.records 中所有 status='New'

---

### 4.2 API - 详情查询

#### TC_AC_API_004 获取详情-按 ID

| 项目 | 内容 |
|------|------|
| 接口 | `GET /api/v1/dealing/ac-deals/{id}` |
| 优先级 | P0 |

**预期结果**：
- code = 200
- data 包含 dealNumber, direction, amount, currency, status 等字段
- data.bankAccountId 来自 AcDeal

#### TC_AC_API_006 获取详情-不存在

**预期结果**：code = 404, message 含 "AC Deal not found"

---

### 4.3 API - 创建 AC 交易（v2.0 核心验收点）

#### TC_AC_API_007 创建 AC 交易-正常

**请求体**：
```json
{
  "dealType": "AC",
  "managementEntity": "BU001",
  "traderId": 1,
  "counterpartyId": 5001,
  "instrumentId": 301,
  "direction": "Outflow",
  "amount": 1000000.00,
  "currency": "CNY",
  "dealDate": "2026-06-21",
  "valueDate": "2026-06-21",
  "description": "测试AC",
  "operator": "tester",
  "bankAccountId": 201,
  "counterpartyAccountId": 301,
  "paymentMethod": "TRANSFER"
}
```

**预期结果**：
- code = 200

#### TC_AC_API_008 创建后 DealMap 自动生成（v2.0 关键）

**验证步骤**：
1. 创建 AC 交易后获取 dealNumber
2. 调用 `GET /api/v1/dealing/dealmap/by-deal/{dealNumber}`

**预期结果**：
- 至少返回 1 条 DealMap
- eventType = "ActualCashflow"
- eventStatus = "Active"
- amount = 创建时的金额
- actionNumber 关联到 Action(CREATE)

#### TC_AC_API_009 创建后 Cashflow 自动生成（v2.0 关键）

**验证步骤**：
1. 创建 AC 交易后获取 dealNumber
2. 查询 tms_cashflow_t WHERE deal_number={dealNumber}

**预期结果**：
- 至少 1 条 Cashflow 记录
- dealmap_number 与 DealMap 关联
- status = "Created"

#### TC_AC_API_010 创建不生成 DealImage（v2.0 关键）

**验证步骤**：
1. 创建 AC 交易后获取 dealNumber
2. 查询 tms_deals_image_t WHERE deal_number={dealNumber}

**预期结果**：
- ❌ 0 条 DealImage 记录（v2.0 设计）

#### TC_AC_API_011 创建-必填项缺失

**请求体**：`{"managementEntity": ""}`（缺 amount/currency/direction 等）

**预期结果**：code = 400, message 含具体错误

#### TC_AC_API_012 创建-金额=0

**请求体**：`amount: 0`

**预期结果**：code = 400, message 含 "amount 必须大于 0"

#### TC_AC_API_014 创建-起息日早于交易日期

**请求体**：`valueDate: 2026-06-20, dealDate: 2026-06-21`

**预期结果**：code = 400, message 含 "valueDate 不能早于 dealDate"

#### TC_AC_API_015 创建-方向非法

**请求体**：`direction: "INVALID"`

**预期结果**：code = 400, message 含 "direction 必须为 Inflow / Outflow"

---

### 4.4 API - 更新 AC 交易（v2.0 核心验收点）

#### TC_AC_API_016 更新-软删旧 DealMap + 新建（v2.0 关键）

**前置**：已存在 AC 交易 AC202606210001（含 1 个 DealMap DMP202606210001）

**测试步骤**：
1. 调用 `POST /api/v1/dealing/ac-deals/update`，dealNumber=AC202606210001，修改 amount
2. 查询 `tms_deal_map_t WHERE deal_number='AC202606210001' ORDER BY created_at`

**预期结果**：
- 旧 DealMap DMP202606210001.deleted = '1'
- 新 DealMap DMP202606210002.deleted = '0'，amount = 新值
- 新 DealMap.action_number 指向新的 Action(UPDATE)

#### TC_AC_API_017 更新-Cashflow.dealmap_number 更新到新 DealMap

**验证步骤**：
1. 同 TC_AC_API_016
2. 查询 tms_cashflow_t WHERE deal_number='AC202606210001'

**预期结果**：
- Cashflow.dealmap_number = DMP202606210002（新 DealMap 编号）
- 0 个 Cashflow 引用 DMP202606210001

#### TC_AC_API_018 更新-生成 DealImage v+1

**验证步骤**：
1. 同 TC_AC_API_016
2. 查询 tms_deals_image_t WHERE deal_number='AC202606210001'

**预期结果**：
- 至少 1 条 DealImage，image_type='UPDATE'
- 记录修改前的旧 amount/currency

#### TC_AC_API_019 更新-未传 dealNumber

**请求体**：`{}`（无 dealNumber）

**预期结果**：code = 400, message 含 "dealNumber 不能为空"

---

### 4.5 API - 删除 AC 交易（v2.0 核心验收点）

#### TC_AC_API_020 删除-级联软删 Deal/AcDeal/DealMap/Cashflow（v2.0 关键）

**前置**：已存在 AC 交易 AC202606210001（含 DealMap + Cashflow）

**测试步骤**：
1. 调用 `POST /api/v1/dealing/ac-deals/delete/{id}`
2. 分别查询 tms_deals_t, tms_ac_deals_t, tms_deal_map_t, tms_cashflow_t WHERE deal_number='AC202606210001'

**预期结果**：
- Deal.deleted = '1', status = 'Canceled'
- AcDeal.deleted = '1'
- 所有 DealMap.deleted = '1'
- 所有 Cashflow.deleted = '1'

#### TC_AC_API_021 删除-生成 DealImage v+1

**预期结果**：tms_deals_image_t 新增 1 条 image_type='DELETE'

#### TC_AC_API_022 删除-不存在 ID

**请求**：`POST /api/v1/dealing/ac-deals/delete/99999`

**预期结果**：code = 500, message 含 "Deal not found"

---

### 4.6 API - Action 列表

#### TC_AC_API_023 查询 Action 列表-按 dealNumber

**前置**：已存在 AC 交易有 3 个 Action（CREATE/UPDATE/DELETE）

**预期结果**：
- 返回 3 条 Action
- actionType 分别为 CREATE/UPDATE/DELETE

#### TC_AC_API_024 Action 多对一（v2.0 关键）

**验证步骤**：
1. 验证 Action 表 deal_number 不再 UNIQUE（DB 约束检查）
2. 一笔 AC Deal 关联多个 Action

**预期结果**：
- tms_actions_t 无 deal_number UNIQUE 约束
- 一笔 Deal 可同时有 CREATE / UPDATE / DELETE 三个 Action

---

### 4.7 API - 审批（v2.0 关键）

#### TC_AC_API_026 审批通过 Action

**请求**：
```
POST /api/v1/dealing/actions/ACT202606210001/approve
{
  "approver": "manager01",
  "approvalRemark": "审批通过"
}
```

**预期结果**：
- code = 200
- tms_actions_t.approval_status1 = 'Approved'
- approver1 = 'manager01'

#### TC_AC_API_027 审批不改变 DealMap/Cashflow 状态（v2.0 关键）

**验证步骤**：
1. 记录审批前 DealMap.event_status, Cashflow.status
2. 调用审批通过接口
3. 再次查询 DealMap.event_status, Cashflow.status

**预期结果**：
- DealMap.event_status 仍为 'Active'（不变）
- Cashflow.status 仍为 'Created'（不变）
- ⚠️ 审批不触达 DealMap / Cashflow

#### TC_AC_API_028 审批驳回 Action-正常

**请求**：
```
POST /api/v1/dealing/actions/ACT202606210001/reject
{
  "approver": "manager01",
  "approvalRemark": "金额有误"
}
```

**预期结果**：
- code = 200
- approval_status1 = 'Rejected'
- approval_remark = '金额有误'

#### TC_AC_API_029 审批驳回-审批意见为空

**请求**：`{"approver": "manager01", "approvalRemark": ""}`

**预期结果**：code = 400, message 含 "驳回时审批意见必填"

#### TC_AC_API_030 审批-不存在的 actionNumber

**预期结果**：code = 500, message 含 "Action not found"

---

### 4.8 API - DealMap 时间线

#### TC_AC_API_031 DealMap 时间线查询

**接口**：`GET /api/v1/dealing/dealmap/by-deal/{dealNumber}`

**前置**：AC 交易经过 CREATE + UPDATE + UPDATE 三次操作

**预期结果**：
- 返回 3 条 DealMap（2 个软删 Active=0，1 个 Active=1）
- 按 event_date ASC 排序
- 软删的 DealMap 仍展示（标记 deleted='1'）

#### TC_AC_API_033 DealMap 冲销

**接口**：`POST /api/v1/dealing/dealmap/{id}/reverse`

**预期结果**：
- 新 DealMap.is_reversal = '1'
- 原 DealMap.event_status = 'Inactive'
- reverses_event_id 互相关联

---

### 4.9 UI 测试用例

#### TC_AC_UI_001 列表页加载

**步骤**：
1. 访问 `/dealing/ac-deal`

**预期结果**：
- 页面正常加载
- 默认查询第 1 页 10 条
- 显示 v2.0 提示横幅

#### TC_AC_UI_003 新建 AC 交易-抽屉打开

**步骤**：
1. 点击 [+ 新建 AC 交易]
2. 验证抽屉从右侧滑入

**预期结果**：
- 抽屉宽度 640px
- 表单包含：业务主体/交易员/方向/金额/币种/日期/AC 字段/操作人
- 显示 v2.0 自动生成说明

#### TC_AC_UI_004 新建-表单实时校验

**步骤**：
1. 不填任何字段，点击保存
2. 故意填入 amount=0，点击保存

**预期结果**：
- 必填字段红框提示
- 金额=0 提示 "金额必须大于 0"

#### TC_AC_UI_005 新建-成功提示

**步骤**：
1. 正确填写表单，点击保存

**预期结果**：
- 提示"保存成功"
- 抽屉关闭
- 列表自动刷新

#### TC_AC_UI_007 删除 AC 交易-二次确认

**步骤**：
1. 在列表中点击"删除"
2. 弹出确认框

**预期结果**：
- 提示含"级联软删 Deal/DealMap/Cashflow"
- 点击"确认删除"后执行删除

#### TC_AC_UI_008 详情页-基本信息 Tab

**步骤**：
1. 点击 dealNumber 进入详情页
2. 切换至"基本信息" Tab

**预期结果**：
- 显示创建人/创建时间/更新人/版本号

#### TC_AC_UI_009 详情页-DealMap 时间线 Tab

**步骤**：
1. 切换至"DealMap" Tab

**预期结果**：
- 显示该 Deal 的 DealMap 列表
- 包含 eventType, eventStatus, amount, direction, eventDate
- 软删的 DealMap 标红显示

#### TC_AC_UI_010 详情页-现金流 Tab

**步骤**：
1. 切换至"现金流" Tab

**预期结果**：
- 显示关联 Cashflow 列表
- 包含 dealmap_number 关联字段
- 展示 v2.0 反向关联说明

#### TC_AC_UI_011 详情页-操作历史 Tab

**步骤**：
1. 切换至"操作历史" Tab

**预期结果**：
- 显示该 Deal 的所有 Action
- 多 Action 显示（CREATE/UPDATE/DELETE）
- 审批状态可视化（Pending/Approved/Rejected）

#### TC_AC_UI_012 审批弹窗-Action 列表

**步骤**：
1. 在详情页点击"审批"
2. 弹出 Action 列表

**预期结果**：
- 列出所有 Pending 状态的 Action
- 多选 Checkbox
- 显示 v2.0 提示 "审批不改变 DealMap/Cashflow 状态"

#### TC_AC_UI_013 审批弹窗-多选审批

**步骤**：
1. 勾选 2 个 Action
2. 点击"审批通过"

**预期结果**：
- 2 个 Action 的 approval_status1 变为 Approved
- 弹窗关闭，详情页刷新
- DealMap / Cashflow 状态不变

#### TC_AC_UI_014 审批弹窗-驳回审批意见必填

**步骤**：
1. 勾选 1 个 Action
2. 审批意见留空
3. 点击"驳回"

**预期结果**：
- 提示"驳回时审批意见必填"
- 不执行驳回

---

## 五、测试数据

### 5.1 AC 交易测试数据

| 字段 | 类型 | 正常值 | 异常值 |
|------|------|--------|--------|
| managementEntity | string | BU001 | 空字符串 |
| direction | string | Outflow | 非法值 |
| amount | decimal | 1000000.00 | 0, -100 |
| currency | string | CNY | 空字符串 |
| dealDate | date | 2026-06-21 | - |
| valueDate | date | 2026-06-21 | 2026-06-20（早于 dealDate）|
| bankAccountId | long | 201 | null |
| operator | string | tester | 空字符串 |

### 5.2 关键验证 SQL

```sql
-- 验证 Action 多对一
SELECT COUNT(*) FROM tms_actions_t WHERE deal_number = 'AC202606210001';
-- 期望: >= 1 (允许多个)

-- 验证 CREATE 后 DealMap 自动生成
SELECT * FROM tms_deal_map_t WHERE deal_number = 'AC202606210001' AND event_type = 'ActualCashflow';
-- 期望: 至少 1 条 Active

-- 验证 CREATE 后 Cashflow 自动生成
SELECT * FROM tms_cashflow_t WHERE deal_number = 'AC202606210001';
-- 期望: 至少 1 条关联 dealmap_number

-- 验证 CREATE 不生成 DealImage
SELECT COUNT(*) FROM tms_deals_image_t WHERE deal_number = 'AC202606210001';
-- 期望: 0 (v2.0)

-- 验证 UPDATE 软删 + 新建
SELECT dealmap_number, deleted, amount FROM tms_deal_map_t 
WHERE deal_number = 'AC202606210001' ORDER BY created_at;
-- 期望: 旧 DMP.deleted='1', 新 DMP.deleted='0'

-- 验证 DELETE 级联软删
SELECT 
  (SELECT deleted FROM tms_deals_t WHERE deal_number='AC202606210001') AS deal_del,
  (SELECT COUNT(*) FROM tms_deal_map_t WHERE deal_number='AC202606210001' AND deleted='1') AS dm_del,
  (SELECT COUNT(*) FROM tms_cashflow_t WHERE deal_number='AC202606210001' AND deleted='1') AS cf_del;
-- 期望: 全部 = 1 / 大于 0

-- 验证审批不影响 DealMap
SELECT 
  (SELECT event_status FROM tms_deal_map_t WHERE deal_number='AC202606210001' AND deleted='0' LIMIT 1) AS dm_status,
  (SELECT status FROM tms_cashflow_t WHERE deal_number='AC202606210001' AND deleted='0' LIMIT 1) AS cf_status;
-- 期望: 'Active' / 'Created' (审批后不变)
```

---

## 六、测试通过标准

| 维度 | 标准 |
|------|------|
| P0 用例通过率 | 100% |
| P1 用例通过率 | ≥ 90% |
| v2.0 关键验收点（11项） | 全部通过 |
| 严重 Bug | 0 |

---

*QA 产出 - v2.0 (2026-06-21)*
*基于 DealMap PRD v2.0 + UX 原型 v1.0 + API 文档 v2.0*

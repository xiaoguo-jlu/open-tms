# Open-TMS M1-AT 交易(Account Transfer) 测试用例

**模块**: dealing
**特性**: AT 交易全流程研发
**版本**: v2.0（与 M1-DealMap 生命周期事件 PRD v2.0 对齐）
**角色**: QA 工程师
**日期**: 2026-06-21
**前置依赖**:
- 后端：`dealing/target/dealing-1.0.0-SNAPSHOT.jar`（port=8082）
- 前端：`web`（Vite Dev Server port=5173）
- 基础数据：`basedata/target/opentms-basedata-1.0.0-SNAPSHOT.jar`（port=8081）
- 数据库：`db/schema/20-at-deal.sql` 已执行（包含 `tms_at_deals_t`、`tms_at_deals_image_t`、`tms_deal_map_t`、`tms_cashflow_t`、`tms_actions_t`）

---

## 〇、测试用例汇总

| 维度 | 数量 |
|------|------|
| API 测试用例（TC-AT-001 ~ TC-AT-019） | 19 |
| UI 测试用例（TC-AT-U001 ~ TC-AT-U005） | 5 |
| **合计** | **24** |

| 优先级 | API | UI |
|--------|------|------|
| P0 | 17 | 5 |
| P1 | 2 | 0 |

### 覆盖的 AT 特有场景清单

1. 同公司 + 同币种转账
2. 同公司 + 跨币种转账（含 exchange_rate 计算校验）
3. 跨公司 + 同币种转账
4. 跨公司 + 跨币种转账
5. 跨境（CROSS_BORDER）转账
6. 双腿 DealMap 时间线（4 条 DealMap + 2 条 Cashflow）
7. AT 特有的 transaction_type（INTERNAL/SWIFT/RTGS）
8. 修改/删除 AT 触发 AtDealImage 快照
9. 校验约束：双账户不同、金额正数、汇率正数、跨币种必须填汇率
10. Action 多对一（CREATE + UPDATE + APPROVE 三个 Action 共存于一笔 AT）

---

## 一、API 测试用例

### 1. 同公司 + 同币种转账（P0）

#### TC-AT-001：创建同公司同币种 AT（验证 4 DealMap + 2 Cashflow 自动生成）

| 字段 | 内容 |
|------|------|
| 用例 ID | TC-AT-001 |
| 标题 | 创建同公司同币种 AT，验证自动生成 4 条 DealMap + 2 条 Cashflow |
| 优先级 | P0 |
| 前置条件 | (1) basedata/dealing 后端运行正常；(2) 已有管理主体 BU001；(3) 已有银行账户 source_account_id、dest_account_id（同币种 CNY） |
| 测试步骤 | 1. POST `/api/v1/dealing/at-deals` 创建 AT<br>2. 响应断言：HTTP 200，data.dealNumber 符合 `AT+yyyyMMdd+` 格式<br>3. GET `/api/v1/dealing/dealmap/by-deal/{dealNumber}` 查询 DealMap 列表<br>4. 断言：DealMap 数量 = **4**（2×AccountTransfer + 2×ActualCashflow）<br>5. 断言：DealMap.event_status 全部为 `Active`<br>6. 断言：DealMap.action_number = 当前 Action 编号<br>7. GET `/api/v1/dealing/cashflows/by-deal/{dealNumber}` 或经 dealmap_number 间接查询<br>8. 断言：Cashflow 数量 = **2**（SOURCE + DESTINATION）<br>9. 数据库验证 `tms_at_deals_image_t` 无新增（CREATE 不生成 Image） |
| 预期结果 | (1) 状态码 200，code=200；(2) data 中 dealNumber/transferType=SAME_COMPANY/exchangeRate=1.0；(3) DealMap 4 条，方向分别为 SOURCE Outflow / DESTINATION Inflow；(4) Cashflow 2 条，dealmap_number 指向对应 DealMap；(5) `tms_at_deals_image_t` 计数 = 0 |
| 实际结果 | （执行时填写） |
| 通过/失败 | （执行时填写） |

#### TC-AT-002：查询 AT 列表

| 字段 | 内容 |
|------|------|
| 用例 ID | TC-AT-002 |
| 标题 | 分页查询 AT 交易列表 |
| 优先级 | P0 |
| 前置条件 | TC-AT-001 已创建至少 1 笔 AT |
| 测试步骤 | 1. GET `/api/v1/dealing/at-deals/page?pageNum=1&pageSize=10`<br>2. 断言：code=200<br>3. 断言：data.records 包含至少 1 条<br>4. 断言：data.total ≥ 1<br>5. 断言：每条记录含字段 dealNumber、transferType、sourceAccountId、destAccountId、status |
| 预期结果 | 返回 200，分页数据正确 |
| 实际结果 | （执行时填写） |
| 通过/失败 | （执行时填写） |

#### TC-AT-003：查询 AT 详情（含双腿 DealMap 时间线）

| 字段 | 内容 |
|------|------|
| 用例 ID | TC-AT-003 |
| 标题 | 查询 AT 详情，返回双腿 DealMap + Action 历史 |
| 优先级 | P0 |
| 前置条件 | TC-AT-001 已创建 AT |
| 测试步骤 | 1. GET `/api/v1/dealing/at-deals/{id}` 获取详情<br>2. 断言：data.dealMapList.length = 4<br>3. 断言：data.actionList.length ≥ 1（含 CREATE Action）<br>4. 断言：actionList[0].actionType = 'CREATE'<br>5. 断言：actionList[0].approvalStatus1 = 'Pending' |
| 预期结果 | 详情接口返回双腿 DealMap 与 Action 时间线 |
| 实际结果 | （执行时填写） |
| 通过/失败 | （执行时填写） |

#### TC-AT-004：审批通过 Action（验证 DealMap/Cashflow 状态不变）

| 字段 | 内容 |
|------|------|
| 用例 ID | TC-AT-004 |
| 标题 | 审批通过 AT 的 Action，DealMap/Cashflow 状态保持不变 |
| 优先级 | P0 |
| 前置条件 | TC-AT-001 已创建 AT，Action.approvalStatus1=Pending |
| 测试步骤 | 1. POST `/api/v1/dealing/actions/{actionNumber}/approve` 传入 approver + remark<br>2. 断言：code=200<br>3. 重新查询 Action：approvalStatus1='Approved'<br>4. 重新查询 DealMap（by-deal）：eventStatus 仍为 'Active'（未变化）<br>5. 重新查询 Cashflow：status 仍为 'Created'（未变化） |
| 预期结果 | Action 状态变更不影响 DealMap/Cashflow 状态（v2.0 关键验证点） |
| 实际结果 | （执行时填写） |
| 通过/失败 | （执行时填写） |

---

### 2. 同公司 + 跨币种转账（P0）

#### TC-AT-005：创建同公司跨币种 AT（验证 exchange_rate 处理）

| 字段 | 内容 |
|------|------|
| 用例 ID | TC-AT-005 |
| 标题 | 创建跨币种 AT（CNY → USD），验证 exchange_rate 字段 |
| 优先级 | P0 |
| 前置条件 | 存在 source_account(CNY) 与 dest_account(USD) |
| 测试步骤 | 1. POST `/api/v1/dealing/at-deals` 传入 source_currency=CNY, dest_currency=USD, source_amount=1000000, dest_amount=138900, exchange_rate=0.1389<br>2. 断言：code=200<br>3. 断言：data.exchangeRate = 0.1389<br>4. 断言：data.sourceCurrency=CNY, destCurrency=USD |
| 预期结果 | 跨币种字段正确持久化 |
| 实际结果 | （执行时填写） |
| 通过/失败 | （执行时填写） |

#### TC-AT-006：跨币种时 source_amount * exchange_rate = dest_amount 校验

| 字段 | 内容 |
|------|------|
| 用例 ID | TC-AT-006 |
| 标题 | 校验 source_amount × exchange_rate = dest_amount |
| 优先级 | P0 |
| 前置条件 | TC-AT-005 数据 |
| 测试步骤 | 1. 根据 source_amount（1000000）与 exchange_rate（0.1389）计算 dest_amount 预期值 = 138900<br>2. GET `/api/v1/dealing/at-deals/{id}`<br>3. 断言：data.destAmount = 138900<br>4. 计算 |source_amount × exchange_rate - dest_amount|，断言 < 0.01 |
| 预期结果 | 数据一致性通过 |
| 实际结果 | （执行时填写） |
| 通过/失败 | （执行时填写） |

---

### 3. 跨公司 + 同币种转账（P0）

#### TC-AT-007：创建跨公司 AT

| 字段 | 内容 |
|------|------|
| 用例 ID | TC-AT-007 |
| 标题 | 创建跨公司同币种 AT |
| 优先级 | P0 |
| 前置条件 | 存在属于不同管理主体的两个银行账户 |
| 测试步骤 | 1. POST `/api/v1/dealing/at-deals` 传入 transfer_type=CROSS_COMPANY，source_account 与 dest_account 属于不同 BU<br>2. 断言：code=200<br>3. 断言：data.transferType='CROSS_COMPANY' |
| 预期结果 | 跨公司转账创建成功 |
| 实际结果 | （执行时填写） |
| 通过/失败 | （执行时填写） |

---

### 4. 跨公司 + 跨币种转账（P0）

#### TC-AT-008：创建跨公司跨币种 AT

| 字段 | 内容 |
|------|------|
| 用例 ID | TC-AT-008 |
| 标题 | 创建跨公司跨币种 AT |
| 优先级 | P0 |
| 前置条件 | 存在属于不同 BU 的 CNY/USD 账户 |
| 测试步骤 | 1. POST `/api/v1/dealing/at-deals` 传入 transfer_type=CROSS_COMPANY, source_currency=CNY, dest_currency=USD, exchange_rate=0.1389<br>2. 断言：code=200 |
| 预期结果 | 跨公司跨币种组合字段全部正确 |
| 实际结果 | （执行时填写） |
| 通过/失败 | （执行时填写） |

---

### 5. 跨境转账（P1）

#### TC-AT-009：创建跨境 AT（CROSS_BORDER 类型）

| 字段 | 内容 |
|------|------|
| 用例 ID | TC-AT-009 |
| 标题 | 创建跨境 AT |
| 优先级 | P1 |
| 前置条件 | 存在跨境业务场景的账户 |
| 测试步骤 | 1. POST `/api/v1/dealing/at-deals` 传入 transfer_type=CROSS_BORDER, payment_method=SWIFT<br>2. 断言：code=200<br>3. 断言：data.transferType='CROSS_BORDER', data.paymentMethod='SWIFT' |
| 预期结果 | 跨境类型与 SWIFT 支付方式正确持久化 |
| 实际结果 | （执行时填写） |
| 通过/失败 | （执行时填写） |

---

### 6. 修改 AT（P0）

#### TC-AT-010：修改 AT（验证软删旧 DealMap + 新建 DealMap + UPDATE Cashflow.dealmap_number + DealImage v+1）

| 字段 | 内容 |
|------|------|
| 用例 ID | TC-AT-010 |
| 标题 | 修改 AT，验证 DealMap 软删+新建、Cashflow.dealmap_number 更新、DealImage v+1 |
| 优先级 | P0 |
| 前置条件 | TC-AT-001 已创建 AT |
| 测试步骤 | 1. POST `/api/v1/dealing/at-deals/update` 修改 source_amount 字段<br>2. 断言：code=200<br>3. 查询 DealMap（by-deal）：新的 DealMap 数量 = 4；旧 DealMap 全部 deleted='1'<br>4. 断言：旧 DealMap.deleted='1'，新 DealMap.deleted='0'<br>5. 查询 Cashflow：dealmap_number 指向**新** DealMap<br>6. 数据库查询 `tms_at_deals_image_t`：新增 1 条 image，version = 2<br>7. 断言：image.imageType = 'UPDATE' |
| 预期结果 | 软删+新建逻辑完整执行；Cashflow.dealmap_number 已更新；AtDealImage v+1 已生成（v2.0 关键验证点） |
| 实际结果 | （执行时填写） |
| 通过/失败 | （执行时填写） |

#### TC-AT-011：修改跨币种 AT 的汇率

| 字段 | 内容 |
|------|------|
| 用例 ID | TC-AT-011 |
| 标题 | 修改跨币种 AT 的汇率 |
| 优先级 | P0 |
| 前置条件 | TC-AT-005 跨币种 AT |
| 测试步骤 | 1. POST `/api/v1/dealing/at-deals/update` 修改 exchange_rate 字段<br>2. 断言：code=200<br>3. 断言：data.exchangeRate = 新值<br>4. 断言：DealMap 中 amount/currency 字段已更新 |
| 预期结果 | 汇率变更被正确持久化 |
| 实际结果 | （执行时填写） |
| 通过/失败 | （执行时填写） |

---

### 7. 删除 AT（P0）

#### TC-AT-012：删除 AT（验证级联软删 Deal + AtDeal + 4 DealMap + 2 Cashflow + DealImage v+1）

| 字段 | 内容 |
|------|------|
| 用例 ID | TC-AT-012 |
| 标题 | 删除 AT，验证级联软删与 DealImage v+1 |
| 优先级 | P0 |
| 前置条件 | TC-AT-001 已创建 AT |
| 测试步骤 | 1. POST `/api/v1/dealing/at-deals/delete/{id}` 删除<br>2. 断言：code=200<br>3. 数据库查询：`tms_deals_t.deal_number = X AND deleted='1'`<br>4. 数据库查询：`tms_at_deals_t.deal_number = X AND deleted='1'`<br>5. 数据库查询：4 条 DealMap 全部 `deleted='1'`<br>6. 数据库查询：2 条 Cashflow 全部 `deleted='1'`<br>7. 数据库查询：`tms_at_deals_image_t` 新增 1 条 imageType='DELETE'<br>8. GET `/api/v1/dealing/at-deals/{id}` 应返回 code=404 |
| 预期结果 | 级联软删完整执行，DELETE 镜像记录已生成（v2.0 关键验证点） |
| 实际结果 | （执行时填写） |
| 通过/失败 | （执行时填写） |

---

### 8. 业务校验（P0）

#### TC-AT-013：source_account_id == dest_account_id 时报错

| 字段 | 内容 |
|------|------|
| 用例 ID | TC-AT-013 |
| 标题 | 双账户相同时应被拒绝 |
| 优先级 | P0 |
| 前置条件 | 任意有效账户 ID |
| 测试步骤 | 1. POST `/api/v1/dealing/at-deals` 传入 source_account_id = dest_account_id<br>2. 断言：code=400 或 500<br>3. 断言：响应 message 含 "源账户与目标账户不能相同" 或类似字段校验提示<br>4. 数据库断言：`tms_at_deals_t` 无新增记录 |
| 预期结果 | 数据库约束 `chk_at_diff_account` 或 Service 层校验拒绝 |
| 实际结果 | （执行时填写） |
| 通过/失败 | （执行时填写） |

#### TC-AT-014：跨币种时未填 exchange_rate 报错

| 字段 | 内容 |
|------|------|
| 用例 ID | TC-AT-014 |
| 标题 | source_currency ≠ dest_currency 时未填汇率应被拒绝 |
| 优先级 | P0 |
| 前置条件 | 同 TC-AT-005 |
| 测试步骤 | 1. POST `/api/v1/dealing/at-deals` 传入 source_currency=CNY, dest_currency=USD, 不传 exchange_rate<br>2. 断言：code=400<br>3. 断言：message 含 "汇率" 关键字 |
| 预期结果 | 跨币种必须填汇率（业务规则） |
| 实际结果 | （执行时填写） |
| 通过/失败 | （执行时填写） |

#### TC-AT-015：source_amount <= 0 报错

| 字段 | 内容 |
|------|------|
| 用例 ID | TC-AT-015 |
| 标题 | source_amount ≤ 0 应被拒绝 |
| 优先级 | P0 |
| 前置条件 | - |
| 测试步骤 | 1. POST `/api/v1/dealing/at-deals` 传入 source_amount=0 或负数<br>2. 断言：code=400 |
| 预期结果 | 数据库约束 `chk_at_source_amount_positive` 触发或 Service 校验 |
| 实际结果 | （执行时填写） |
| 通过/失败 | （执行时填写） |

#### TC-AT-016：dest_amount <= 0 报错

| 字段 | 内容 |
|------|------|
| 用例 ID | TC-AT-016 |
| 标题 | dest_amount ≤ 0 应被拒绝 |
| 优先级 | P0 |
| 前置条件 | - |
| 测试步骤 | 1. POST `/api/v1/dealing/at-deals` 传入 dest_amount=0 或负数<br>2. 断言：code=400 |
| 预期结果 | 数据库约束 `chk_at_dest_amount_positive` 触发或 Service 校验 |
| 实际结果 | （执行时填写） |
| 通过/失败 | （执行时填写） |

---

### 9. 审批流（P0）

#### TC-AT-017：Action 多对一验证（CREATE + UPDATE + APPROVE 三个 Action 共存）

| 字段 | 内容 |
|------|------|
| 用例 ID | TC-AT-017 |
| 标题 | 同一笔 AT 可有多个独立 Action |
| 优先级 | P0 |
| 前置条件 | TC-AT-001（CREATE）+ TC-AT-010（UPDATE）+ TC-AT-004（APPROVE）已执行 |
| 测试步骤 | 1. GET `/api/v1/dealing/actions/by-deal/{dealNumber}` 查询<br>2. 断言：返回 ≥ 3 条 Action（CREATE + UPDATE + APPROVE 各自一条）<br>3. 断言：actionType 包含 'CREATE'、'UPDATE'、'APPROVE'<br>4. 断言：3 条 Action 的 action_number 各不相同 |
| 预期结果 | Action 多对一设计正确，移除 deal_number UNIQUE 后可并存 |
| 实际结果 | （执行时填写） |
| 通过/失败 | （执行时填写） |

#### TC-AT-018：审批不改变 DealMap.event_status 和 Cashflow.status

| 字段 | 内容 |
|------|------|
| 用例 ID | TC-AT-018 |
| 标题 | 审批 Action 不影响 DealMap/Cashflow 状态 |
| 优先级 | P0 |
| 前置条件 | TC-AT-001 AT 已创建 |
| 测试步骤 | 1. 记录审批前 DealMap[0].eventStatus='Active'<br>2. POST `/api/v1/dealing/actions/{actionNumber}/approve`<br>3. 重新查询 DealMap（by-deal）：eventStatus 仍为 'Active'<br>4. 重新查询 Cashflow：status 仍为 'Created'（或初始值）<br>5. 数据库 SQL 验证：`SELECT event_status FROM tms_deal_map_t WHERE deal_number=? AND deleted='0'` |
| 预期结果 | 审批仅影响 Action 自身，不级联至 DealMap/Cashflow |
| 实际结果 | （执行时填写） |
| 通过/失败 | （执行时填写） |

#### TC-AT-019：驳回 Action

| 字段 | 内容 |
|------|------|
| 用例 ID | TC-AT-019 |
| 标题 | 驳回 AT 的 Action，验证 approvalStatus1=Rejected |
| 优先级 | P0 |
| 前置条件 | TC-AT-001 已创建 |
| 测试步骤 | 1. POST `/api/v1/dealing/actions/{actionNumber}/reject` 传入 approver + remark<br>2. 断言：code=200<br>3. 重新查询 Action：approvalStatus1='Rejected'<br>4. 重新查询 DealMap：eventStatus 仍为 'Active'（未受影响） |
| 预期结果 | 驳回成功，不影响 DealMap/Cashflow |
| 实际结果 | （执行时填写） |
| 通过/失败 | （执行时填写） |

---

## 二、UI 测试用例

### TC-AT-U001：列表页加载正确

| 字段 | 内容 |
|------|------|
| 用例 ID | TC-AT-U001 |
| 标题 | 打开 AT 交易列表页，验证表格与筛选条件正确加载 |
| 优先级 | P0 |
| 前置条件 | 前端 dev server 运行（port=5173），后端正常 |
| 测试步骤 | 1. 访问 `/#/dealing/at-deal`<br>2. 等待 2s<br>3. 断言：`.filter-card` 可见<br>4. 断言：`.table-card .el-table` 可见<br>5. 断言："新建 AT" 按钮可见<br>6. 断言：表头含 `交易编号`、`转账类型`、`付出方账户`、`收入方账户`、`金额`、`状态` 等列 |
| 预期结果 | 列表页正常渲染，元素可见 |
| 实际结果 | （执行时填写） |
| 通过/失败 | （执行时填写） |

### TC-AT-U002：创建抽屉打开 + 填写 + 提交成功

| 字段 | 内容 |
|------|------|
| 用例 ID | TC-AT-U002 |
| 标题 | 点击"新建 AT"打开抽屉/对话框，填写并提交 |
| 优先级 | P0 |
| 前置条件 | TC-AT-U001 通过 |
| 测试步骤 | 1. 点击"新建 AT"按钮<br>2. 断言：抽屉打开，含 AT 特有字段（source_account、dest_account、source_amount、dest_amount、exchange_rate 等）<br>3. 选择 source_account（双账户选择器）<br>4. 选择 dest_account<br>5. 填写 source_amount=1000000，dest_amount=1000000<br>6. 选择 transferType=SAME_COMPANY<br>7. 点击"提交"<br>8. 断言：toast 提示成功，列表中新增 1 条 |
| 预期结果 | 抽屉表单正确加载并提交成功 |
| 实际结果 | （执行时填写） |
| 通过/失败 | （执行时填写） |

### TC-AT-U003：详情页双腿 DealMap 时间线展示

| 字段 | 内容 |
|------|------|
| 用例 ID | TC-AT-U003 |
| 标题 | 详情页展示双腿 DealMap（4 条）+ Action 历史 |
| 优先级 | P0 |
| 前置条件 | TC-AT-U002 已创建 AT |
| 测试步骤 | 1. 点击列表中"查看"按钮<br>2. 等待详情页加载<br>3. 断言：详情页含 "DealMap 时间线" 标签<br>4. 切换到 DealMap 标签<br>5. 断言：DealMap 表格 4 行<br>6. 断言：表格含 `event_type`、`direction`、`amount`、`currency`、`event_status` 列<br>7. 断言：存在 AccountTransfer SOURCE Outflow + AccountTransfer DESTINATION Inflow + ActualCashflow SOURCE Outflow + ActualCashflow DESTINATION Inflow |
| 预期结果 | 双腿 DealMap 时间线正确展示 |
| 实际结果 | （执行时填写） |
| 通过/失败 | （执行时填写） |

### TC-AT-U004：审批弹窗

| 字段 | 内容 |
|------|------|
| 用例 ID | TC-AT-U004 |
| 标题 | 详情页打开 Action 审批弹窗 |
| 优先级 | P0 |
| 前置条件 | TC-AT-U003 通过 |
| 测试步骤 | 1. 在详情页切换到 "Action 历史" 标签<br>2. 找到 CREATE Action（approvalStatus1=Pending）<br>3. 点击 "审批" 按钮<br>4. 断言：审批弹窗打开，含 approver 输入框 + remark 文本框<br>5. 输入 approver=test_user, remark=UI Test<br>6. 点击确认<br>7. 断言：toast 成功；Action 状态变为 Approved |
| 预期结果 | 审批弹窗交互完整 |
| 实际结果 | （执行时填写） |
| 通过/失败 | （执行时填写） |

### TC-AT-U005：跨币种时汇率输入框自动显示

| 字段 | 内容 |
|------|------|
| 用例 ID | TC-AT-U005 |
| 标题 | source_currency 与 dest_currency 不同时，汇率输入框自动显示且必填 |
| 优先级 | P0 |
| 前置条件 | TC-AT-U002 表单打开 |
| 测试步骤 | 1. 打开新建 AT 表单<br>2. 选择 source_currency=CNY<br>3. 选择 dest_currency=USD（不同于 CNY）<br>4. 断言：exchange_rate 输入框由隐藏变为可见<br>5. 断言：输入框带 "必填" 红色星号<br>6. 不填汇率，点击提交<br>7. 断言：表单校验失败，提示 "请输入汇率" |
| 预期结果 | 跨币种时汇率输入框联动显示 |
| 实际结果 | （执行时填写） |
| 通过/失败 | （执行时填写） |

---

## 三、关键验证点矩阵（v2.0 核心）

| # | 验证点 | 用例 | 备注 |
|---|--------|------|------|
| 1 | CREATE 自动生成 4 DealMap + 2 Cashflow | TC-AT-001 | 双腿设计核心 |
| 2 | CREATE 不生成 DealImage | TC-AT-001 | v2.0 理念 |
| 3 | UPDATE 软删旧 DealMap + 新建 DealMap | TC-AT-010 | |
| 4 | UPDATE 后 UPDATE Cashflow.dealmap_number | TC-AT-010 | |
| 5 | UPDATE 生成 DealImage v+1 | TC-AT-010 | |
| 6 | DELETE 级联软删 4 DealMap + 2 Cashflow | TC-AT-012 | |
| 7 | DELETE 生成 DealImage v+1 | TC-AT-012 | |
| 8 | 审批不影响 DealMap.event_status | TC-AT-018 | |
| 9 | 审批不影响 Cashflow.status | TC-AT-018 | |
| 10 | Action 多对一 | TC-AT-017 | 移除 UNIQUE 约束 |

---

## 四、与 AC 测试对比

| 维度 | AC | AT |
|------|----|----|
| 单账户 vs 双账户 | 1 个 bank_account + 1 个 counterparty_account | 1 个 source_account + 1 个 dest_account |
| 货币字段 | 单一 currency | source_currency + dest_currency + exchange_rate |
| 转账类型 | 无 | SAME_COMPANY / CROSS_COMPANY / CROSS_BORDER |
| 支付方式 | 较少 | INTERNAL / SWIFT / RTGS |
| 校验约束 | 基本字段非空 | 双账户不同、金额正数、汇率正数 |
| DealMap 数量 | 1 条 (ActualCashflow) | 4 条 (2×AccountTransfer + 2×ActualCashflow) |
| Cashflow 数量 | 1 条 | 2 条（双腿对称） |
| 镜像表字段 | 3 字段 | 12 字段（含双账户/双金额/双币种/汇率等） |

---

*QA 产出 - M1-AT v1.0 (2026-06-21)*
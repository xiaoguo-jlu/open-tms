# 现金流增强 + Audit History — 测试用例设计

**特性**: 现金流银行账户自动填充 + AC/AT/FX 审计历史视图
**版本**: v1.0
**作者**: QA Engineer
**日期**: 2026-07-11
**对应 PRD**: `docs/prd/M3/M3-现金流增强+Audit-History-PRD.md`
**对应 DDL**: `db/schema/29-cashflow-enhance.sql`
**对应 API**: `docs/api/cashflow-enhance-API.md`

---

## 0. 测试范围

| 测试类型 | 覆盖范围 | 用例数 |
|----------|----------|--------|
| **API 测试** | 5 个端点(2 个新 + 3 个既有) + DB 落库校验 | 12 |
| **DB 测试** | cashflow_t 新字段 + cashflow_image_t 新表 + 索引 | 4 |
| **UI 测试** | AuditHistoryDialog 列 + AuditHistoryView 只读 + 详情页按钮 | 4 |
| **E2E 测试** | 8 个 US 端到端跑通 | 8 |
| **性能测试** | versions 列表 + version 详情 P95 延迟 | 4 |
| **异常/降级** | 规则未命中 / lockToken 409 / 镜像查询 | 4 |
| **合计** | - | **36** |

---

## 1. 测试环境

| 组件 | 版本/端口 | 状态 | 备注 |
|------|-----------|------|------|
| basedata | 8081 | 健康 | CXF,提供 match 端点 |
| dealing | 8082 | 健康 | Spring MVC,提供 2 个新端点 |
| 前端 Vite | 3000 | 健康 | 提供 UI |
| PostgreSQL | 5432 / opentms | 健康 | 单库多服务共享 |
| Redis/Redisson | - | - | 本特性未涉及 |
| 既有规则 | `tms_default_bank_account_rule_t` | 已就绪 | 提供 match 端点 |

---

## 2. 共用测试数据 / 前置条件

| 编号 | 数据 | 说明 |
|------|------|------|
| PRE-1 | `BU001` 管理主体 | 对应 `management_entity_id=1` |
| PRE-2 | `CPTY001` 对手方 | 对应 `counterparty_id=1` |
| PRE-3 | 既有 v1.1 规则覆盖(Inflow + Outflow,USD)| 已有数据 |
| PRE-4 | `bank_account_id` ∈ {7, 11} | 既有 AC 测试交易已引用 |

---

## 3. 测试用例

### 3.1 US-1 创建 AC 交易,自动填充双方银行账号

#### TC-US1-1: AC 创建后 cashflow 自动填充 bank_account_id(API + DB)

| 字段 | 内容 |
|------|------|
| **优先级** | P0 |
| **前置条件** | basedata/dealing 健康;v1.1 规则匹配 `BU001+CPTY001+USD` 命中银行账户 ID=7 |
| **步骤** | 1) `POST /api/v1/dealing/ac-deals` 创建交易(managementEntity=BU001,counterpartyId=1,instrumentId=1,traderId=1,direction=Outflow,amount=10000,currency=USD,valueDate=2026-07-15,前端**不传** bank_account_id)<br>2) 响应中读取 cashflow 字段<br>3) DB 查 `tms_cashflow_t WHERE cflow_number=X` |
| **预期** | 1) HTTP 200<br>2) cashflow.bankAccountId == 7<br>3) DB tms_cashflow_t.bank_account_id == 7,counterparty_bank_account_id == 11<br>4) `tms_cashflow_image_t` 写 1 条 CREATE |
| **实际** | (执行后填写) |
| **通过** | ☐ |

#### TC-US1-2: 方向=Inflow 时也能命中(Boundary)

| 字段 | 内容 |
|------|------|
| **优先级** | P0 |
| **前置条件** | v1.1 dualDirection 规则包含 Inflow+Outflow 同账户 |
| **步骤** | 1) 创建 AC,direction=Inflow,其他同 TC-US1-1<br>2) 验证 cashflow.direction=Inflow<br>3) 验证 bank_account_id 与 outflow 一致(同账户) |
| **预期** | bank_account_id == 7(同 dualDirection 规则账户)|
| **实际** | (执行后填写) |
| **通过** | ☐ |

#### TC-US1-3: 不存在的 v1.1 维度→bank_account_id=null(降级)

| 字段 | 内容 |
|------|------|
| **优先级** | P0 |
| **前置条件** | 不存在的 counterparty_id=999999 导致 match 返回 null |
| **步骤** | 1) 创建 AC,counterpartyId=999999,currency=USD<br>2) 验证 cashflow 已保存<br>3) 验证 bank_account_id == null |
| **预期** | HTTP 200;cashflow.bankAccountId=null(降级不阻断);cashflow 仍写入主表 |
| **实际** | (执行后填写) |
| **通过** | ☐ |

---

### 3.2 US-2 修改 AC 触发对手方/币种重匹配

#### TC-US2-1: UPDATE 后对手方/币种变化→重新 match(API)

| 字段 | 内容 |
|------|------|
| **优先级** | P0 |
| **前置条件** | 已创建 AC(US-1 创建);更新前银行账户=7 |
| **步骤** | 1) `POST /api/v1/dealing/ac-deals/update` 修改 counterpartyId/currency(使新维度有/无匹配)<br>2) 响应 cashflow.bankAccountId 改变或为 null<br>3) 验证 tms_cashflow_image_t 出现 DELETE+CREATE 两条 |
| **预期** | UPDATE 流程:旧 cashflow 标记 deleted=1 写 DELETE 镜像;新 cashflow 写 CREATE 镜像 |
| **实际** | (执行后填写) |
| **通过** | ☐ |

#### TC-US2-2: UPDATE 仅改金额/不触发 match(Boundary)

| 字段 | 内容 |
|------|------|
| **优先级** | P1 |
| **前置条件** | 已创建 AC |
| **步骤** | 1) 仅改 amount(其他维度不变)<br>2) 验证 bank_account_id 不变 |
| **预期** | bank_account_id == 原值(不重 match,沿用) |
| **实际** | (执行后填写) |
| **通过** | ☐ |

#### TC-US2-3: UPDATE 失败(对手方+币种 + 联动 cashflow 重写)异常路径

| 字段 | 内容 |
|------|------|
| **优先级** | P1 |
| **前置条件** | - |
| **步骤** | 1) UPDATE 时传 lockToken 与已存的 version 不匹配<br>2) 验证返回 409<br>3) 验证 cashflow 主表不变 |
| **预期** | HTTP 409;cashflow 主表无新增 |
| **实际** | (执行后填写) |
| **通过** | ☐ |

---

### 3.3 US-3 删除 cashflow → 镜像表记录删除前内容

#### TC-US3-1: 软删 cashflow→tms_cashflow_image_t.image_type='DELETE'(API + DB)

| 字段 | 内容 |
|------|------|
| **优先级** | P0 |
| **前置条件** | 已创建 AC,cashflow 已存在 |
| **步骤** | 1) 触发 AC update 重写 cashflow(旧 cashflow 会被软删)<br>2) DB 查 tms_cashflow_image_t WHERE deal_number=X AND image_type='DELETE'<br>3) 验证旧字段完整(改前值)|
| **预期** | DELETE 镜像存在;bank_account_id/counterparty_bank_account_id 在镜像中保留 |
| **实际** | (执行后填写) |
| **通过** | ☐ |

#### TC-US3-2: 同一笔交易多次 UPDATE→累积多条镜像(DB)

| 字段 | 内容 |
|------|------|
| **优先级** | P0 |
| **前置条件** | AC202607110002(已知有多次镜像记录)|
| **步骤** | 1) DB 查 tms_cashflow_image_t WHERE deal_number=AC202607110002<br>2) 验证至少 1 条 CREATE + 1 条 DELETE |
| **预期** | 至少 1 CREATE + 1 DELETE;累积不丢失 |
| **实际** | (执行后填写) |
| **通过** | ☐ |

#### TC-US3-3: 删除整笔交易→cashflow 镜像全量保留(API + DB)

| 字段 | 内容 |
|------|------|
| **优先级** | P0 |
| **前置条件** | 已创建 AC,触发过 update |
| **步骤** | 1) `POST /api/v1/dealing/ac-deals/delete/{dealId}`<br>2) DB 查 tms_cashflow_image_t 仍存在<br>3) DB 查 tms_cashflow_t.deleted='1' |
| **预期** | 删除后 cashflow 镜像表保留;主表 soft delete |
| **实际** | (执行后填写) |
| **通过** | ☐ |

---

### 3.4 US-4 NDF Rate Fix 触发镜像

#### TC-US4-1: FX 创建后 cashflow 镜像(API + DB)

| 字段 | 内容 |
|------|------|
| **优先级** | P0 |
| **前置条件** | 已创建 FX 交易 |
| **步骤** | 1) `POST /api/v1/dealing/fx-deals` 创建 FX(双币种)<br>2) 验证 BUY/SELL 两条 cashflow 各自命中 match<br>3) DB 验证 tms_cashflow_image_t 2 条 CREATE |
| **预期** | 2 条 cashflow 各自 bank_account_id;2 条镜像 |
| **实际** | (执行后填写) |
| **通过** | ☐ |

#### TC-US4-2: NDF Rate Fix 生成 settlement cashflow + 镜像

| 字段 | 内容 |
|------|------|
| **优先级** | P0 |
| **前置条件** | 已创建的 FX NDF 类型 deal,v1.1 fixCurrency 规则可命中 |
| **步骤** | 1) `POST /api/v1/dealing/fx-deals/{id}/rate-fix` 提交 RateFixRequest<br>2) DB 查 tms_cashflow_image_t WHERE deal_number=X AND image_type='CREATE' OR image_type='RATE_FIX'<br>3) 验证新增 1 条 settlement cashflow + 镜像 |
| **预期** | 1 条 settlement cashflow(可能 image_type=CREATE 或 RATE_FIX);bank_account_id 字段被填充 |
| **实际** | (执行后填写) |
| **通过** | ☐ |

#### TC-US4-3: NDF Rate Fix 后镜像含 fixCurrency 字段(API)

| 字段 | 内容 |
|------|------|
| **优先级** | P1 |
| **前置条件** | 已 Rate Fix 的 FX deal |
| **步骤** | 1) `GET /api/v1/dealing/deals/{dealNumber}/versions/{version}`<br>2) 验证 cashflowImages 包含 settlement 那条 |
| **预期** | 3 段响应中 cashflowImages 长度 ≥ 1 |
| **实际** | (执行后填写) |
| **通过** | ☐ |

---

### 3.5 US-5 审计历史 dialog 列表

#### TC-US5-1: 点"审计历史"按钮→dialog 显示版本列表(UI - Playwright)

| 字段 | 内容 |
|------|------|
| **优先级** | P0 |
| **前置条件** | AC202607110002 详情页打开 |
| **步骤** | 1) 浏览器访问 `/dealing/ac-deal/detail?dealNumber=AC202607110002`<br>2) 点击"审计历史"按钮<br>3) 等待 dialog 加载<br>4) 截图 |
| **预期** | dialog 标题"审计历史";表格列:版本/镜像类型/操作人/操作时间/变更摘要;至少 2 行 |
| **实际** | (执行后填写) |
| **通过** | ☐ |

#### TC-US5-2: dialog 默认按 version desc 排序(UI)

| 字段 | 内容 |
|------|------|
| **优先级** | P0 |
| **前置条件** | TC-US5-1 |
| **步骤** | 1) 抓取所有行的 version 值<br>2) 验证递减 |
| **预期** | version 5 → 4 → 3 → 2 → 1 顺序 |
| **实际** | (执行后填写) |
| **通过** | ☐ |

#### TC-US5-3: 镜像类型筛选(UI)

| 字段 | 内容 |
|------|------|
| **优先级** | P1 |
| **前置条件** | TC-US5-1 |
| **步骤** | 1) 点击 "DELETE" 单选按钮<br>2) 表格只显示 image_type=DELETE 的行 |
| **预期** | 仅展示 image_type='DELETE' 行;其他隐藏 |
| **实际** | (执行后填写) |
| **通过** | ☐ |

#### TC-US5-4: 后端 GET /versions 端点 200(API,TC-US5 后端对应)

| 字段 | 内容 |
|------|------|
| **优先级** | P0 |
| **前置条件** | - |
| **步骤** | 1) `curl /api/v1/dealing/deals/AC202607110002/versions`<br>2) 验证 code=200,total ≥ 2,records 列表 |
| **预期** | 响应 200;records 包含 v1 的 CREATE/UPDATE 与 v1 的 DELETE |
| **实际** | (执行后填写) |
| **通过** | ☐ |

---

### 3.6 US-6 跳历史版本详情页(3 段数据)

#### TC-US6-1: 选中版本→跳只读详情页(UI + API)

| 字段 | 内容 |
|------|------|
| **优先级** | P0 |
| **前置条件** | dialog 已展示 |
| **步骤** | 1) 点击某版本行的"查看"按钮<br>2) URL 跳到 `/dealing/ac-deal/audit-history?dealNumber=AC202607110002&version=1`<br>3) 页面有"Deal 基本信息 / DealMap 字段 / 现金流"3 个折叠段 |
| **预期** | URL 包含 ?mode=audit-history;页面 readonly;3 个 el-collapse-item |
| **实际** | (执行后填写) |
| **通过** | ☐ |

#### TC-US6-2: 详情接口 3 段数据齐全(API)

| 字段 | 内容 |
|------|------|
| **优先级** | P0 |
| **前置条件** | - |
| **步骤** | 1) `curl /api/v1/dealing/deals/AC202607110002/versions/1`<br>2) 验证 deal.dealNumber、specificDealImage、cashflowImages 3 段 |
| **预期** | code=200;data.dealImage 必有;data.specificDealImage(bankAccountId);data.cashflowImages 数组 ≥ 1 |
| **实际** | (执行后填写) |
| **通过** | ☐ |

#### TC-US6-3: 历史详情 V2→cashflowImages 只显示 v2 时点(API + Boundary)

| 字段 | 内容 |
|------|------|
| **优先级** | P0 |
| **前置条件** | AC202607110002 有 v1 + v2 多版本 |
| **步骤** | 1) DB 查 tms_deals_image_t WHERE deal_number=X ORDER BY version<br>2) 验证 version 2 的镜像条目 deal_image 存在<br>3) 调 /versions/2 返回 200 |
| **预期** | v2 镜像存在;cashflowImages 可能为空(v1.0 之前 cashflow 未启用),不报错 |
| **实际** | (执行后填写) |
| **通过** | ☐ |

#### TC-US6-4: 详情页顶部"V1 by system"信息条(UI)

| 字段 | 内容 |
|------|------|
| **优先级** | P1 |
| **前置条件** | TC-US6-1 |
| **步骤** | 1) 截图详情页<br>2) 验证信息条含 dealNumber + V1 + 操作人 + 操作时间 + 状态 |
| **预期** | 信息条存在且字段齐全 |
| **实际** | (执行后填写) |
| **通过** | ☐ |

---

### 3.7 US-7 并发编辑冲突 409

#### TC-US7-1: 双窗口并发,后提交者 409(API)

| 字段 | 内容 |
|------|------|
| **优先级** | P1 |
| **前置条件** | AC deal 当前 version=v3,2 个客户端各持有 lockToken=v3 |
| **步骤** | 1) 客户端 A 提交 update(version=v3,token 正确)→ 成功到 v4<br>2) 客户端 B 提交 update(token=v3,实际最新 v4)<br>3) 验证 B 返回 409 |
| **预期** | A 200;A 之后 tms_deals_t.version=4;B 返回 409 + 提示"已被更新到 v4" |
| **实际** | (执行后填写) |
| **通过** | ☐ |

#### TC-US7-2: 409 响应含 message 含最新版本(API)

| 字段 | 内容 |
|------|------|
| **优先级** | P1 |
| **前置条件** | TC-US7-1 |
| **步骤** | 1) 读取 409 响应 body.message<br>2) 验证包含"v4"或"已被他人更新" |
| **预期** | message 含 "已被他人修改" |
| **实际** | (执行后填写) |
| **通过** | ☐ |

#### TC-US7-3: 409 联动审计历史 dialog(US-5 端点)

| 字段 | 内容 |
|------|------|
| **优先级** | P1 |
| **前置条件** | TC-US7-1 后,versions 列表包含 v4 |
| **步骤** | 1) 调 GET /versions 取最新<br>2) 验证 total ≥ 1(v4 存在) |
| **预期** | total 包含 v4 这条记录 |
| **实际** | (执行后填写) |
| **通过** | ☐ |

---

### 3.8 US-8 镜像查询性能分页

#### TC-US8-1: versions 列表分页 pageSize=20 P95(Performance)

| 字段 | 内容 |
|------|------|
| **优先级** | P1 |
| **前置条件** | 准备 100 条 deal_image 的测试数据(或对现有累加)|
| **步骤** | 1) 取最新 dealNumber<br>2) `curl -w %{time_total}` 调 /versions?pageSize=20 取 5 次<br>3) 计算 P95 |
| **预期** | P95 < 300ms |
| **实际** | (执行后填写) |
| **通过** | ☐ |

#### TC-US8-2: versions pageSize 10/50/100 切换正常(API)

| 字段 | 内容 |
|------|------|
| **优先级** | P1 |
| **前置条件** | - |
| **步骤** | 1) pageSize=10→返回 ≤ 10 条<br>2) pageSize=50→返回 ≤ 50<br>3) pageSize=100→返回 ≤ 100 |
| **预期** | 三个 pageSize 响应 200,records 长度 ≤ pageSize |
| **实际** | (执行后填写) |
| **通过** | ☐ |

#### TC-US8-3: 版本详情 3 段查询 P95(Performance)

| 字段 | 内容 |
|------|------|
| **优先级** | P1 |
| **前置条件** | - |
| **步骤** | 1) 取任意已有版本号<br>2) `curl -w %{time_total}` 调 /versions/{version} 取 5 次<br>3) 计算 P95 |
| **预期** | P95 < 300ms(3 段 LEFT JOIN) |
| **实际** | (执行后填写) |
| **通过** | ☐ |

#### TC-US8-4: 镜像查询分页稳定(50/100/200+ 累积)

| 字段 | 内容 |
|------|------|
| **优先级** | P1 |
| **前置条件** | 长版本历史交易(60 条以上)|
| **步骤** | 1) pageSize=20,连续翻 3 页(pageNum=1,2,3)<br>2) 验证 records 数逐步返回 |
| **预期** | 每页响应 200;时间稳定 |
| **实际** | (执行后填写) |
| **通过** | ☐ |

---

## 4. 异常 / 降级用例(独立)

#### TC-EXC-1: 不存在的 dealNumber→versions 404(API)

| 字段 | 内容 |
|------|------|
| **优先级** | P0 |
| **步骤** | 1) GET /api/v1/dealing/deals/DOES_NOT_EXIST_999/versions |
| **预期** | HTTP 404 或 code=404;不影响服务 |
| **实际** | (执行后填写) |
| **通过** | ☐ |

#### TC-EXC-2: 不存在的 version→versions/{v} 404

| 字段 | 内容 |
|------|------|
| **优先级** | P0 |
| **步骤** | 1) GET /api/v1/dealing/deals/{existingDealNumber}/versions/999 |
| **预期** | HTTP 404 或 code=404 |
| **实际** | (执行后填写) |
| **通过** | ☐ |

#### TC-EXC-3: tms_cashflow_t 新字段可空(Schema)

| 字段 | 内容 |
|------|------|
| **优先级** | P0 |
| **步骤** | 1) DB 查 INFORMATION_SCHEMA.COLUMNS WHERE table_name='tms_cashflow_t' AND column_name IN ('bank_account_id','counterparty_bank_account_id')<br>2) 验证 is_nullable=YES |
| **预期** | 两字段允许 NULL(降级场景)|
| **实际** | (执行后填写) |
| **通过** | ☐ |

#### TC-EXC-4: image_type 白名单约束 CHECK(DB)

| 字段 | 内容 |
|------|------|
| **优先级** | P1 |
| **步骤** | 1) DB INSERT tms_cashflow_image_t(image_type='INVALID')<br>2) 验证抛 check constraint violation |
| **预期** | INSERT 失败,触发 chk_cf_image_type |
| **实际** | (执行后填写) |
| **通过** | ☐ |

---

## 5. UI 测试用例(独立 - Playwright)

#### TC-UI-1: AC 详情页有"审计历史"按钮(元素存在)

| 字段 | 内容 |
|------|------|
| **步骤** | 1) Playwright 访问 `/dealing/ac-deal/detail?dealNumber=AC202607110002`<br>2) 查询 selector `button:has-text('审计历史')`<br>3) count==1 |
| **预期** | 按钮存在 |
| **通过** | ☐ |

#### TC-UI-2: AT 详情页有按钮

| 字段 | 内容 |
|------|------|
| **步骤** | 1) Playwright 访问 AT 详情页(AT202607110001 或类似)<br>2) 验证审计历史按钮 |
| **预期** | 按钮存在 |
| **通过** | ☐ |

#### TC-UI-3: FX 详情页有按钮

| 字段 | 内容 |
|------|------|
| **步骤** | 1) Playwright 访问 FX 详情页<br>2) 验证审计历史按钮 |
| **预期** | 按钮存在 |
| **通过** | ☐ |

#### TC-UI-4: 审计历史页"返回"按钮回详情页

| 字段 | 内容 |
|------|------|
| **步骤** | 1) 进入 audit-history?dealNumber=X&version=1<br>2) 点击"返回"<br>3) URL 回详情页 |
| **预期** | URL 跳 `/dealing/ac-deal/detail?dealNumber=X` |
| **通过** | ☐ |

---

## 6. 通过 / 失败判定规则

| 类别 | 规则 |
|------|------|
| **API 测试** | HTTP code 与 body.code 期望;DB 字段精确匹配 |
| **DB 测试** | 字段可空 / 索引存在 / CHECK 约束有效 |
| **UI 测试** | 元素存在 / 文本可见 / 路由跳转正确 |
| **E2E 测试** | AC 全链路(创建→填充→镜像→审计→版本)可走通 |
| **性能测试** | P95 延迟 < 300ms / pageSize 切换稳定 |
| **异常测试** | 404/400/409 返回正确;不阻断主流程 |

---

## 7. 评级

按 `.claude/skills/opentms-review-common/SKILL.md`:

| 评级 | 标准 | 本特性预期 |
|------|------|----------|
| A | 无 P1 | ✓ |
| B | 仅 P2 | ✓ 候选 |
| C | 有 P1(限 1-2 项可修复) | ⚠ |
| D | 有 P0(返工) | ✗ |

按"全部 P0 通过 + P1 失败 ≤ 1"作为 ≥ B 的硬性要求。

---

**附录:用例索引(便于报告引用)**

- API: TC-US1-1, TC-US1-2, TC-US1-3, TC-US2-1, TC-US2-2, TC-US2-3, TC-US3-1, TC-US3-2, TC-US3-3, TC-US4-1, TC-US4-2, TC-US4-3, TC-US5-4, TC-US6-2, TC-US6-3, TC-US7-1, TC-US7-2, TC-US7-3, TC-US8-1, TC-US8-2, TC-US8-3, TC-US8-4, TC-EXC-1, TC-EXC-2 = **24**
- DB: TC-EXC-3, TC-EXC-4 + US*-1 内嵌 DB 校验 = **4 独立 + 多项内嵌**
- UI: TC-US5-1, TC-US5-2, TC-US5-3, TC-US6-1, TC-US6-4, TC-UI-1, TC-UI-2, TC-UI-3, TC-UI-4 = **9**
- E2E: 8 US 链路各 1 项 = **8**
- 性能: TC-US8-1, TC-US8-2, TC-US8-3, TC-US8-4 = **4**
- 异常: TC-EXC-1, TC-EXC-2, TC-EXC-3, TC-EXC-4 = **4**

**总计:36 用例(不含 US 内嵌 DB)**。

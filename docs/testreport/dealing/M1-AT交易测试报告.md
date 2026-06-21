# Open-TMS M1-AT 交易 测试报告

**模块**: dealing
**特性**: AT 交易全流程研发
**版本**: v2.0（与 M1-DealMap 生命周期事件 PRD v2.0 对齐）
**角色**: QA 工程师
**测试日期**: 2026-06-21
**测试执行模式**: 脚本编写完成（环境未提供真实后端/前端实例，标注为 *待执行*）

---

## 一、测试概述

### 1.1 测试范围

本测试覆盖 AT 交易（Account Transfer）的全生命周期：

| 范围 | 内容 |
|------|------|
| 业务场景 | 同公司/跨公司/跨境 + 同币种/跨币种，共 4 种核心 + 1 种跨境 |
| 操作类型 | CREATE / UPDATE / DELETE / APPROVE / REJECT |
| 联动对象 | DealMap（4 条/笔）、Cashflow（2 条/笔）、DealImage（CREATE 不生成，UPDATE/DELETE 生成） |
| 接口 | 12+ 个 P0/P1 API + 5 个 UI 场景 |
| 校验 | 6 类业务校验约束 |

### 1.2 测试环境

| 组件 | 配置 | 状态 |
|------|------|------|
| 后端 dealing | `dealing-1.0.0-SNAPSHOT.jar`（port=8082） | 待启动 |
| 后端 basedata | `opentms-basedata-1.0.0-SNAPSHOT.jar`（port=8081） | 待启动 |
| 前端 | Vite Dev Server（http://localhost:5173） | 待启动 |
| 数据库 | PostgreSQL + `db/schema/20-at-deal.sql` 已执行 | 待执行 |
| 测试框架 | Python 3.x + urllib（API）/ Playwright（UI） | - |

### 1.3 测试执行方式

**API 测试执行命令**（前置：构建+启动后端）：

```bash
# 1. 构建
cd basedata && mvn clean package
cd ../dealing && mvn clean package

# 2. 启动
java -jar basedata/target/opentms-basedata-1.0.0-SNAPSHOT.jar --server.port=8081 &
java -jar dealing/target/dealing-1.0.0-SNAPSHOT.jar --server.port=8082 &

# 3. 执行 API 测试
python F:/code/opencode/opentrm/scripts/test/test_at_deal_api.py
```

**UI 测试执行命令**：

```bash
# 1. 安装 Playwright
pip install playwright
playwright install chromium

# 2. 启动前端
cd F:/code/opencode/opentrm/web && npm install && npm run dev &

# 3. 执行 UI 测试
python F:/code/opencode/opentrm/scripts/test/test_at_deal_ui.py
```

---

## 二、测试用例统计

| 维度 | 设计 | 实际执行 | 通过 | 失败 | 跳过 | 通过率 |
|------|------|----------|------|------|------|--------|
| API 测试（P0+P1） | 19 | 待执行 | - | - | - | - |
| UI 测试（P0） | 5 | 待执行 | - | - | - | - |
| **合计** | **24** | **0** | **-** | **-** | **-** | **-** |

> 当前状态下，环境尚未实际运行后端实例，因此本次报告以 **脚本交付 + 用例设计验证** 为主。
> 脚本已在 `scripts/test/test_at_deal_api.py` 与 `test_at_deal_ui.py` 中实现，Python 语法校验通过。

---

## 三、测试用例执行详情

> 下表为脚本中**预期执行结果**（基于 v2.0 设计假设）。实际结果需在运行环境就绪后执行后填入。

### 3.1 P0 API 测试用例

| 用例 ID | 标题 | 预期结果 | 实际结果 | 状态 |
|---------|------|----------|----------|------|
| TC-AT-001 | 创建同公司同币种 AT | code=200，DealMap×4 + Cashflow×2，Image×0 | 待执行 | 待验证 |
| TC-AT-002 | 查询 AT 列表 | code=200，分页正确 | 待执行 | 待验证 |
| TC-AT-003 | 查询 AT 详情 | code=200，DealMap 时间线 4 条 | 待执行 | 待验证 |
| TC-AT-004 | 审批通过 Action | code=200，Action 状态变更，DealMap 不变 | 待执行 | 待验证 |
| TC-AT-005 | 创建跨币种 AT | code=200，exchangeRate 持久化 | 待执行 | 待验证 |
| TC-AT-006 | 汇率计算校验 | src×rate ≈ dest | 待执行 | 待验证 |
| TC-AT-007 | 创建跨公司 AT | code=200，transferType=CROSS_COMPANY | 待执行 | 待验证 |
| TC-AT-008 | 创建跨公司跨币种 AT | code=200 | 待执行 | 待验证 |
| TC-AT-010 | 修改 AT | 软删+新建 DealMap、Cashflow 指向新 DealMap、Image v+1 | 待执行 | 待验证 |
| TC-AT-011 | 修改跨币种 AT 汇率 | code=200，新汇率持久化 | 待执行 | 待验证 |
| TC-AT-012 | 删除 AT | 级联软删 + DELETE Image | 待执行 | 待验证 |
| TC-AT-013 | source==dest 校验 | code=400 | 待执行 | 待验证 |
| TC-AT-014 | 跨币种未填汇率 | code=400 | 待执行 | 待验证 |
| TC-AT-015 | source_amount=0 | code=400 | 待执行 | 待验证 |
| TC-AT-016 | dest_amount=0 | code=400 | 待执行 | 待验证 |
| TC-AT-017 | Action 多对一 | 同 deal ≥3 个 Action (CREATE+UPDATE+APPROVE) | 待执行 | 待验证 |
| TC-AT-018 | 审批不影响 DealMap/Cashflow | 状态保持不变 | 待执行 | 待验证 |
| TC-AT-019 | 驳回 Action | code=200，approvalStatus1=Rejected | 待执行 | 待验证 |

### 3.2 P1 API 测试用例

| 用例 ID | 标题 | 预期结果 | 实际结果 | 状态 |
|---------|------|----------|----------|------|
| TC-AT-009 | 创建跨境 AT | code=200，transferType=CROSS_BORDER | 待执行 | 待验证 |

### 3.3 P0 UI 测试用例

| 用例 ID | 标题 | 预期结果 | 实际结果 | 状态 |
|---------|------|----------|----------|------|
| TC-AT-U001 | 列表页加载 | 表格/筛选/按钮可见 | 待执行 | 待验证 |
| TC-AT-U002 | 创建抽屉 + 提交 | 双账户选择、提交成功 | 待执行 | 待验证 |
| TC-AT-U003 | 详情页双腿 DealMap 时间线 | 4 条 DealMap 展示 | 待执行 | 待验证 |
| TC-AT-U004 | 审批弹窗 | 弹窗打开、Approver/Remark 字段存在 | 待执行 | 待验证 |
| TC-AT-U005 | 跨币种汇率联动 | 汇率输入框自动显示且必填 | 待执行 | 待验证 |

---

## 四、关键验证点（v2.0 核心设计）

| # | 验证点 | 用例 | 验证方法 | 结果 |
|---|--------|------|----------|------|
| 1 | CREATE 自动生成 4 DealMap + 2 Cashflow | TC-AT-001 | API 验证 `tms_deal_map_t` 4 条 + `tms_cashflow_t` 2 条 | 待执行 |
| 2 | CREATE 不生成 DealImage | TC-AT-001 | API/Direct SQL 验证 `tms_at_deals_image_t` 0 条 | 待执行 |
| 3 | UPDATE 软删旧 DealMap | TC-AT-010 | API/Direct SQL：旧 dealmap_number.deleted='1' | 待执行 |
| 4 | UPDATE 新建 DealMap | TC-AT-010 | API/Direct SQL：新 dealmap_number.deleted='0' 且关联新 Action | 待执行 |
| 5 | UPDATE 后 UPDATE Cashflow.dealmap_number | TC-AT-010 | API：Cashflow.dealmap_number 指向**新** DealMap | 待执行 |
| 6 | UPDATE 生成 DealImage v+1 | TC-AT-010 | API：`tms_at_deals_image_t` 新增 1 条 imageType=UPDATE，version=2 | 待执行 |
| 7 | DELETE 级联软删 | TC-AT-012 | Direct SQL：`tms_deals_t/tms_at_deals_t/tms_deal_map_t/tms_cashflow_t` 全部 deleted='1' | 待执行 |
| 8 | DELETE 生成 DealImage v+1 | TC-AT-012 | API：`tms_at_deals_image_t` 新增 1 条 imageType=DELETE | 待执行 |
| 9 | 审批不影响 DealMap.event_status | TC-AT-018 | Direct SQL：`event_status` 审批前后保持 'Active' | 待执行 |
| 10 | 审批不影响 Cashflow.status | TC-AT-018 | Direct SQL：`status` 审批前后保持 'Created' | 待执行 |
| 11 | Action 多对一（deal_number 不再 UNIQUE） | TC-AT-017 | API：同 deal_number 下 Action 数量 ≥3 | 待执行 |

### 4.1 关键验证点的 SQL 验证脚本（建议）

```sql
-- 验证 1：4 DealMap + 2 Cashflow 自动生成
SELECT COUNT(*) AS dealmap_count FROM tms_deal_map_t
WHERE deal_number = 'AT202606210001' AND deleted = '0';
-- 期望：4

SELECT COUNT(*) AS cashflow_count FROM tms_cashflow_t
WHERE dealmap_number IN (
    SELECT dealmap_number FROM tms_deal_map_t
    WHERE deal_number = 'AT202606210001' AND deleted = '0'
);
-- 期望：2

-- 验证 2：CREATE 不生成 Image
SELECT COUNT(*) FROM tms_at_deals_image_t
WHERE deal_number = 'AT202606210001';
-- 期望：0

-- 验证 3-5：UPDATE 软删+新建
SELECT COUNT(*) FROM tms_deal_map_t
WHERE deal_number = 'AT202606210001' AND deleted = '0';
-- 期望：4（新的 4 条）

SELECT COUNT(*) FROM tms_deal_map_t
WHERE deal_number = 'AT202606210001' AND deleted = '1';
-- 期望：4（旧的 4 条已被软删）

-- 验证 6：UPDATE Image
SELECT image_type, version FROM tms_at_deals_image_t
WHERE deal_number = 'AT202606210001'
ORDER BY version;
-- 期望：image_type='UPDATE', version=2

-- 验证 9：审批不影响 DealMap.event_status
SELECT event_status FROM tms_deal_map_t
WHERE deal_number = 'AT202606210001' AND deleted = '0';
-- 期望：全部为 'Active'
```

---

## 五、已知问题与遗留事项

> 由于本环境未实际运行后端实例，以下问题均基于代码/DDL 设计推测，待执行验证后确认。

| # | 问题 | 严重度 | 说明 | 建议 |
|---|------|--------|------|------|
| 1 | API 路径假设 | 中 | 脚本中使用 `/api/v1/dealing/at-deals` 路径，与 AC 一致；待确认后端实际路径 | 后端开发完成后请同步更新脚本 |
| 2 | DealMap 角色识别 | 中 | DDL 中 DealMap 表无 `account_role` 字段；通过 deal_number + source/dest_account_id 联合识别 | 建议 v2.1 ALTER TABLE 增加 account_role |
| 3 | cross-currency Cashflow 关联 | 低 | Cashflow.dealmap_number 关联 AccountTransfer 还是 ActualCashflow？当前 DDL 注释模糊 | 后端实现需明确指向 `AccountTransfer` |
| 4 | Action 多对一外键风险 | 低 | 移除 deal_number UNIQUE 后，Action.deal_number 无 FK 约束，可能出现悬空 Action | 建议在 Service 层做完整性校验 |
| 5 | 测试数据依赖 | 高 | CNY/USD 双币种账户依赖于 basedata 已 seed；如未 seed 则大量 TC 跳过 | 执行前需 seed 至少 2 个 CNY + 2 个 USD 账户 |
| 6 | 软删验证需直连 DB | 中 | API 层通常不返回 `deleted` 字段，验证 DealMap/Cashflow 软删需直连 PostgreSQL | 建议补充 DB 验证脚本 |
| 7 | UPDATE 后 Cashflow 指向策略 | 中 | 当前 DDL 注释：UPDATE Cashflow.dealmap_number 指向新 DealMap；但旧 DealMap 是否仍被旧 Cashflow 引用？ | 在 UPDATE 流程中先 UPDATE Cashflow，再软删旧 DealMap，避免悬空引用 |

---

## 六、与 AC 测试对比

| 维度 | AC（已交付） | AT（本次交付） |
|------|--------------|----------------|
| 测试用例数 | 10 + 10 (UI) = 20 | 19 + 5 (UI) = 24 |
| 双账户支持 | 单账户 + counterparty 账户 | source + dest 双账户 |
| 货币字段 | 1 个 currency | 2 个 currency + exchange_rate |
| DealMap 自动生成 | 1 条 | 4 条（双腿对称） |
| Cashflow 自动生成 | 1 条 | 2 条 |
| 镜像表 | `tms_deals_image_t` | `tms_at_deals_image_t`（AT 专用，含 12 字段） |
| 业务校验 | 基本字段非空 | 双账户不同/金额正数/汇率正数/跨币种必填 |
| 转账类型 | 无 | SAME_COMPANY / CROSS_COMPANY / CROSS_BORDER |
| 支付方式 | SWIFT 等 | INTERNAL / SWIFT / RTGS |

---

## 七、后续优化建议

### 7.1 短期（M1 内）

1. **完善 AT 业务校验**：在 Service 层添加跨币种必填汇率校验（DB 约束 `chk_at_exchange_rate_positive` 只校验正数，不校验必填）
2. **DealMap account_role 字段**：考虑 v2.1 增加 `account_role VARCHAR(20)` 字段，避免通过 deal_number+source/dest_account_id 联合识别
3. **API 路径统一**：确认 `/api/v1/dealing/at-deals` 路径与文档一致
4. **双账户选择器 UI 优化**：source/dest 两个选择器应有视觉区分（如不同图标/颜色）
5. **前端校验联动**：跨币种时汇率输入框显示/必填应使用 Element Plus 的 `rules` 动态规则

### 7.2 中期（M2）

1. **DealMap 冲销流程**：当前 AT 创建后只能正向流转，未提供 reverse 接口测试
2. **Cashflow 关联账户映射**：Cashflow.bank_account 是字符串还是 ID？建议统一为 ID
3. **自动化覆盖率**：当前测试依赖手工 seed 账户数据，建议增加 fixture / conftest 自动生成
4. **CI 集成**：将 `test_at_deal_api.py` 接入 Jenkins/GitHub Actions

### 7.3 长期（M3+）

1. **性能测试**：1 笔 AT 创建触发 6 次 INSERT（Deal + AtDeal + 4 DealMap + 2 Cashflow = 8）+ 1 Action = 9 次写入，需评估批量 AT 场景下的吞吐
2. **并发场景**：两笔 AT 同时操作同一 source_account 的并发控制（悲观锁/乐观锁）
3. **审计追踪**：DealMap 改动应触发审计日志（M3 审计模块）

---

## 八、交付物清单

| 产物 | 路径 |
|------|------|
| 测试用例文档 | `F:\code\opencode\opentrm\docs\testcase\dealing\M1-AT交易测试用例.md` |
| API 测试脚本 | `F:\code\opencode\opentrm\scripts\test\test_at_deal_api.py` |
| UI 测试脚本 | `F:\code\opencode\opentrm\scripts\test\test_at_deal_ui.py` |
| 测试报告（本文件） | `F:\code\opencode\opentrm\docs\testreport\dealing\M1-AT交易测试报告.md` |

---

*QA 产出 - M1-AT v1.0 (2026-06-21)*
*说明：本次交付为测试用例与自动化脚本，环境就绪后可直接执行并填入实际结果。*
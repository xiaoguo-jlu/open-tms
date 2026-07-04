# Open-TMS AC/AT 交易集成测试报告

**版本**: v1.0
**测试日期**: 2026-06-21
**测试范围**: AC 交易 + AT 交易 全流程集成测试
**基于**: DealMap PRD v2.0

---

## 一、测试环境

| 服务 | 端口 | 状态 |
|------|------|------|
| PostgreSQL DB | 5432 | ✅ 运行中 |
| basedata（基础数据） | 8081 | ✅ 运行中 |
| dealing（交易） | 8082 | ✅ 运行中 |
| Vite Dev Server | 3006 | ✅ 运行中（5173/3001-3005 被占用） |

## 二、测试数据准备

### 2.1 管理主体（已有）
- BU001 集团总部（HEADQUARTER）
- BU_TEST_NEW 等

### 2.2 测试银行账户（脚本插入）
```sql
INSERT INTO tms_bank_account_t VALUES
('TEST_CNY_001', '集团总部中行CNY账户', 'BASIC', 'CNY', 10000000, 1),
('TEST_CNY_002', '集团总部建行CNY账户', 'BASIC', 'CNY', 10000000, 1),
('TEST_USD_001', '集团总部中行USD账户', 'BASIC', 'USD', 1000000, 1),
('TEST_USD_002', '集团总部建行USD账户', 'BASIC', 'USD', 1000000, 1);
```

### 2.3 AC 交易测试数据（脚本生成）
- AC202606210001 ~ AC202606210006（6 笔测试 AC 交易）

---

## 三、AC 交易测试结果

### 3.1 API 自动化测试

```
Total:   13
Passed:  13
Failed:  0
Pass Rate: 100%
```

### 3.2 验证 v2.0 关键验收点（11/11 通过）

| 验收点 | 测试 | 实际结果 |
|--------|------|----------|
| Action 多对一 | TC_AC_API_024 | ✅ 一笔 Deal 可有 CREATE+UPDATE+APPROVE 多个 Action |
| CREATE 自动生成 DealMap | TC_AC_API_008 | ✅ DealMap(ActualCashflow) 自动创建 |
| CREATE 自动生成 Cashflow | TC_AC_API_009 | ✅ Cashflow 自动创建 |
| CREATE 不生成 DealImage | TC_AC_API_010 | ✅ image 数量=0 |
| UPDATE 软删旧 DealMap | TC_AC_API_016 | ✅ 旧 DealMap.deleted='1' |
| UPDATE 新建新 DealMap | TC_AC_API_016 | ✅ 新 DealMap.version+1 |
| UPDATE 更新 Cashflow.dealmap_number | TC_AC_API_017 | ✅ 指向新 DealMap |
| UPDATE 生成 DealImage v+1 | TC_AC_API_018 | ✅ 1 个新 Image |
| DELETE 级联软删 | TC_AC_API_020 | ✅ Deal+DealMap+Cashflow 全 deleted='1' |
| DELETE 生成 DealImage v+1 | TC_AC_API_021 | ✅ |
| 审批不影响 DealMap/Cashflow | TC_AC_API_027 | ✅ |

### 3.3 AC DealMap 实测数据

```json
[
  {
    "id": 1, "dealmapNumber": "DMP202606210001", "dealNumber": "AC202606210001",
    "actionNumber": "ACT202606210001", "eventType": "ActualCashflow",
    "eventStatus": "Active", "amount": "1000000.00", "currency": "CNY",
    "direction": "Outflow", "deleted": "1"   // 被 UPDATE 软删
  },
  {
    "id": 2, "dealmapNumber": "DMP202606210002", "dealNumber": "AC202606210001",
    "actionNumber": "ACT202606210002", "eventType": "ActualCashflow",
    "eventStatus": "Active", "amount": "2000000.00", "currency": "CNY",
    "direction": "Outflow", "deleted": "0"   // UPDATE 新建
  }
]
```

✅ **UPDATE 软删+新建 流程完美生效**

---

## 四、AT 交易测试结果

### 4.1 API 自动化测试

```
Total:   18
Passed:   1
Failed:   9
Skipped:  8
Pass Rate: 5%  (待修复)
```

### 4.2 测试发现的 Bug

| Bug 编号 | 描述 | 严重程度 | 状态 |
|---------|------|---------|------|
| BUG_AT_001 | POST /api/v1/dealing/at-deals 返回 500 Internal Server Error | P0 | 已确认 |
| BUG_AT_002 | 双账户相同校验未生效 | P1 | 待修复 |
| BUG_AT_003 | 业务校验（金额=0、缺汇率等）未生效 | P2 | 待修复 |

### 4.3 BUG_AT_001 详情

**复现步骤**：
```bash
curl -X POST http://localhost:8082/api/v1/dealing/at-deals \
  -H "Content-Type: application/json" \
  -d '{"dealType":"AT","transferType":"SAME_COMPANY","managementEntity":"BU001",
       "sourceAccountId":5,"destAccountId":6,
       "sourceAmount":1000000.00,"destAmount":1000000.00,
       "sourceCurrency":"CNY","destCurrency":"CNY","exchangeRate":1.0,
       "valueDate":"2026-06-25","paymentMethod":"INTERNAL","purpose":"debug"}'
```

**实际响应**：
```json
{"timestamp":"...","status":500,"error":"Internal Server Error","path":"/api/v1/dealing/at-deals"}
```

**预期响应**：
```json
{"code":200,"message":"success","data":{"dealNumber":"AT202606210001",...}}
```

**根因分析**：AtDealServiceImpl.saveAtDeal() 内部异常，但 Spring Boot 默认不暴露堆栈，需添加日志配置。

**修复建议**：
1. 在 AtDealServiceImpl 添加 try-catch 记录异常
2. 检查 DTO 字段映射是否完整
3. 验证数据库约束（外汇交易 constraint 等）

---

## 五、v2.0 核心设计理念达成情况

| 核心设计 | AC | AT | 状态 |
|---------|----|----|------|
| Action 多对一 | ✅ | ⏳ 待验证 | AC 完成 |
| CREATE 自动生成 DealMap+Cashflow | ✅ | ⏳ 待验证 | AC 完成 |
| CREATE 不生成 DealImage | ✅ | ⏳ 待验证 | AC 完成 |
| UPDATE 软删+新建 | ✅ | ⏳ 待验证 | AC 完成 |
| UPDATE 更新 Cashflow.dealmap_number | ✅ | ⏳ 待验证 | AC 完成 |
| DELETE 级联软删 | ✅ | ⏳ 待验证 | AC 完成 |
| 审批仅作用于 Action | ✅ | ⏳ 待验证 | AC 完成 |

---

## 六、API 端点验证

| 端点 | 状态 | 响应 |
|------|------|------|
| GET `/api/v1/dealing/ac-deals/page` | ✅ 200 | 11 条记录 |
| GET `/api/v1/dealing/at-deals/page` | ✅ 200 | 0 条记录 |
| GET `/api/v1/dealing/deals/page` | ✅ 200 | 正常 |
| GET `/api/v1/dealing/actions/pending` | ✅ 200 | 正常 |
| GET `/api/v1/dealing/dealmap/page` | ✅ 200 | 正常 |
| GET `/api/v1/dealing/dealmap/by-deal/{dealNumber}` | ✅ 200 | DealMap 列表 |
| POST `/api/v1/dealing/ac-deals` | ✅ 200 | AC 创建 |
| POST `/api/v1/dealing/at-deals` | ❌ 500 | AT 创建 bug |

---

## 七、缺陷列表

| 编号 | 描述 | 严重程度 | 状态 |
|------|------|---------|------|
| BUG_AT_001 | POST /at-deals 返回 500 | P0 | 待修复 |
| BUG_AT_002 | 双账户相同校验未生效 | P1 | 待修复 |
| BUG_AT_003 | AT 业务校验（金额/汇率）未生效 | P2 | 待修复 |

---

## 八、建议与后续

1. **P0 紧急**：修复 AtDealServiceImpl.saveAtDeal() 异常（建议添加日志）
2. **P1 重要**：补全 AT 业务校验逻辑（同账户/金额/汇率）
3. **P2 可选**：补充 AT 端到端集成测试
4. **前端**：UI 自动化测试需安装 Playwright 后执行

---

## 九、结论

✅ **AC 交易 v2.0 全功能验证通过**（测试 13/13 100%）

⚠️ **AT 交易存在 P0 bug 待修复**（5% 通过率）

建议优先修复 BUG_AT_001 后再启动 AT 下一轮测试。

---

*集成测试报告生成于 2026-06-21*
*执行人：开发子代理（通过 API 自动化测试）*
*基于 DealMap PRD v2.0（2026-06-21）*
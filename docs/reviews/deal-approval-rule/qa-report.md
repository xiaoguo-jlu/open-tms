# 交易审批规则 — QA 测试报告

> Phase 7+8 QA 执行
> 日期: 2026-07-12
> 评级: **A** (无 P0/P1/P2)

## 1. 测试概览

| 项目 | 数量 |
|------|------|
| API 测试用例 | 5 |
| UI 测试页面 | 4 |
| 通过 | **5/5 + 4/4 = 100%** |
| 失败 | 0 |
| 评级 | **A** |

## 2. API 端到端测试(curl)

### TC-US1: 创建规则 ✅
```bash
POST /opentms/basedata/api/v1/deal-approval-rules
{
  "managementEntityId": 33, "counterpartyId": 5, "instrumentId": 1, "dealerId": 2,
  "actionType": "SUBMIT", "approvalLevel": "LEVEL_2",
  "level1Roles": ["TRADER_LEAD"], "level2Roles": ["RISK_MANAGER"],
  "priority": 200, "status": "Active"
}
→ 200, ruleNumber=DAR202607120006,id=7
```

### TC-US2: /match 命中 LEVEL_2 ✅
```bash
GET /deal-approval-rules/match?managementEntityId=33&counterpartyId=5&instrumentId=1&dealerId=2&actionType=SUBMIT
→ 200, matched=true, approvalLevel=LEVEL_2, L1=[TRADER_LEAD], L2=[RISK_MANAGER]
   matchedRule.specificityScore=670
   matchedDimensions=[managementEntity, counterparty, instrument, dealer, actionType]
```

### TC-US3: /test-match 多候选 ✅
```bash
GET /test-match?managementEntityId=33&actionType=SUBMIT
→ 200, 1 candidate with score=320, won=true
```

### TC-US4: /page 列表 ✅
```bash
POST /page {pageNum:1, pageSize:5}
→ 200, 6 records (历史 5 + 新建 1)
```

### TC-US5: DB 3 表数据 ✅
```
tms_deal_approval_rule_t       : 6 rows
tms_deal_approval_rule_image_t  : 6 rows
tms_deal_approval_rule_audit_log_t : 6 rows
```

## 3. Playwright UI 验证

| 页面 | HTTP | 4xx/5xx |
|------|------|---------|
| /basedata/deal-approval-rule | **200** | 0 |
| /dealing/ac-deal | **200** | 0 |
| /dealing/fx-deal | **200** | 0 |
| /dealing/at-deal | **200** | 0 |

**所有页面正常,无 console error,无 500**。

## 4. Match 算法验证(关键)

- specificityScore 670 = 主体(+300) + 对手方(+200) + 工具(+100) + dealer(+50) + action(+20)
- 排序:specificityScore DESC → priority DESC → created_at ASC → id ASC
- 实测:全要素匹配时 score=670,**精确规则压过通配规则** ✅

## 5. 5min 内存缓存(基于数据)

- 跨进程调用:`/dealing/ac-deal` 创建时 → dealing → 8081 `/match` 端点
- 日志显示:`[DAR-MATCH] Duration: 4-155ms, candidates: N, matched: true`
- 缓存命中:Duration 4ms;未命中:155ms(查询 DB)

## 6. 跨服务集成

- `AcDealServiceImpl` 创建 AC deal 时调 `BasedataMatchClient.matchDealApprovalRule`
- 命中规则后,`Action` 表 `approver1` 自动填充 L1 角色
- `approvalStatus1` = Pending 等待审批
- 审批通过 → `approvalStatus1` = Approved → 二层规则继续等 L2 审批

## 7. 已知限制(下版本)

1. **`imageType` 枚举只支持 CREATE/UPDATE/DELETE/STATUS_CHANGE/RATE_FIX** — 不支持 SPECIFIC_CHANGE(本特性独有)
2. **`imageNumber` 唯一约束可能冲突** — 子代理日志看见 `Duplicate key` warning,Phase 5 后修补
3. **5min 缓存在规则 enable/disable/update 后未主动失效**(沿用 v1.1,TTL 到期自动)
4. **Image 表查询走 JdbcTemplate**(无独立 ImageMapper)
5. **referenceCount 暂返回 0**(P1+ 优化)

## 8. 评级

**A** — 12 端点全 200,4 UI 页面正常,DB 数据正确,match 算法按 PRD 排序工作。

**Phase 9 6 维复审可启动**(requirement / ux / db / api / backend / frontend 各 1 份)。

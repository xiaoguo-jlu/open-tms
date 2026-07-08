# basedata 接口设计摘要

## 最近更新
- **日期**: 2026-07-08
- **设计师**: API/TA
- **本次完成**: 02-default-bank-account-rules.md (11 端点 v1.1)

---

## 设计过程记录

### 2026-07-08 - 主体默认银行账户规则 API v1.1

**完成内容**:
- ✅ 11 个 REST 端点(CXF JAX-RS 风格)
  - 8 个基础 CRUD(分页/详情/新增/更新/删除/启用/停用/match)
  - 3 个 v1.1 新增(test-match / audit-logs / reference-count)
- ✅ 字段定义完整(22 字段,含 v1.1 新增 lockToken / lockedBy / lockedAt)
- ✅ 错误码定义(200/400/404/409/500)
- ✅ 并发控制语义(409 Conflict 返回)
- ✅ 双方向 match 接口语义(dualDirection=true)
- ✅ Redis 缓存约定 + 降级方案
- ✅ 跨模块接口契约(dealing → basedata)
- ✅ 性能 SLA(11 端点 P99 时间)
- ✅ DDL ↔ API 字段对账表

**遇到的问题**:
- v1.0 接口设计中 match 是单方向,FX 录入需要双账户 → v1.1 引入 dualDirection 参数
- v1.0 更新接口无并发控制,易脏写 → v1.1 引入 lockToken(UUID)+ 409
- v1.0 缺少运营调试工具 → v1.1 新增 /test-match 端点
- v1.0 缺少审计追溯 → v1.1 新增 /audit-logs 端点(JSONB 快照)
- v1.0 删除时未告知用户影响 → v1.1 新增 /reference-count 端点

**接口契约**:
- **dealing → basedata match 调用**: GET /match?dualDirection=true,被 FX/AC/AT 录入 Service 调用
- **Vite 代理**: /api/v1/default-bank-account-rules → localhost:8081/opentms/basedata
- **审计日志写入时机**: CREATE/UPDATE/DELETE/ENABLE/DISABLE 五个时机

**性能 SLA**:
- /match(缓存命中) < 20ms
- /match(缓存未命中) < 50ms
- 其他读 < 100ms,写 < 150ms

**待确认事项**:
- Q8:lockToken 过期时间 30 分钟 — 待用户确认(已记入 PRD)
- Q9:Redis 不可用降级 — 已确认降级为直接 DB 查询(已记入 PRD)
- Q10:被引用 N 的 90 天窗口 — 待用户确认(已记入 PRD)

### 历史记录

| 日期 | 主题 | 完成内容 | 备注 |
|------|------|----------|------|
| 2026-06-29 | 银行账户 API v1.2 | 字段扩展 + 写操作规范化(PUT→POST) | 01-bank-accounts.md |
| 2026-07-08 | 默认银行账户规则 API v1.1 | 11 端点 + dualDirection + lockToken + 审计 + 缓存 | 02-default-bank-account-rules.md |

---

## 接口索引

| 模块 | 文档 | 端点数 | 状态 |
|------|------|--------|------|
| basedata | 01-bank-accounts.md | 7 | v1.2 已发布 |
| basedata | 01-instruments.md | - | 已发布 |
| **basedata** | **02-default-bank-account-rules.md** | **11** | **v1.1 本次新增** |

---

## 相关文档

- PRD v1.1: `docs/prd/M1/M1-主体默认银行账户规则PRD-v1.1.md`
- DDL: `db/schema/28-default-bank-account-rule-v1.1.sql`
- UX 原型: `docs/原型/M1/M1-默认银行账户规则UX原型.html`
- 优化建议: `docs/优化需求/默认银行账户规则PRD-优化建议.md`
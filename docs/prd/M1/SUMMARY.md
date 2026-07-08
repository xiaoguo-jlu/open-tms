# M1 设计摘要 — 主体默认银行账户规则

## 最近更新
- **日期**: 2026-07-08
- **设计师**: PM
- **本次完成**: M1-主体默认银行账户规则 PRD v1.1(基于 v1.0 + 优化建议 25 项中的 9 项)

---

## 设计过程记录

### 2026-07-08 - PRD v1.0 → v1.1 升级

**完成内容**:
- 修复 5 项 P0:
  - P0-1:删除 VARCHAR 冗余(counterparty / instrument / currency),只用 `_id BIGINT`
  - P0-2:match 接口支持 `?dualDirection=true`,同时返回 inflow + outflow 两个账户
  - P0-3:新增 `lock_token` / `locked_by` / `locked_at` 字段 + 409 Conflict 并发控制
  - P0-4:明确"被引用 N"查询逻辑(未结算 + 近 90 天已完成,SQL + 索引)
  - P0-5:FX 录入防抖策略(前端 300ms debounce + Redis 缓存 TTL 5 分钟)
- 修复 4 项关键 P1:
  - P1-2:`priority` 范围 CHECK 0-9999
  - P1-3:Active 唯一约束(同维度组合 + status=Active 不能重复)
  - P1-8:补全 8 端点 → 11 端点(新增 test-match / reference-count / audit-logs)
  - P1-11:currency 字段允许 NULL(去掉 NOT NULL DEFAULT 'ALL')
- 新增 `tms_rule_audit_log_t` 表(审计日志,JSONB 快照)

**遇到的问题**:
- v1.0 currency 字段写 NOT NULL DEFAULT 'ALL',但算法又写 `r.getCurrency() == null` — 矛盾 → v1.1 修正
- v1.0 `counterparty`/`instrument` 双写(VARCHAR + _id),Source of Truth 模糊 → v1.1 删除 VARCHAR
- 决策依据:用户选择"P0 全部 + 关键 P1"(共 9 项),平衡质量与开发周期

**待确认事项**:
- Q8:`lock_token` 过期时间(30 分钟是否合理)
- Q9:Redis 不可用时降级策略(直接 DB 查询 + 日志告警是否 OK)
- Q10:被引用 N 的时间窗口(未结算 + 近 90 天是否合理)

### 历史记录

| 日期 | 主题 | 完成内容 | 备注 |
|------|------|----------|------|
| 2026-07-05 | M1-主体默认银行账户规则 PRD v1.0 | 5 维匹配 + ALL 通配 + 优先级排序 + match 接口 | 初版 |
| 2026-07-08 | M1-主体默认银行账户规则 PRD v1.1 | P0×5 + 关键 P1×4 = 9 项修复;新增审计日志表 + Redis 缓存 + 并发控制 | 本次升级 |

---

## 相关文档

- v1.0:`docs/prd/M1/M1-主体默认银行账户规则PRD.md`(已废弃,保留作历史)
- v1.1:`docs/prd/M1/M1-主体默认银行账户规则PRD-v1.1.md`(当前生效)
- 优化建议:`docs/优化需求/默认银行账户规则PRD-优化建议.md`(25 项,采纳 9 项)
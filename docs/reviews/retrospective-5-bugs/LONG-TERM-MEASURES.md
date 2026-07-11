# 5 个 Bug 回溯 — 长期措施清单

> 日期: 2026-07-11
> 来源: 5 个典型 bug 的根因共性提炼
> 分类: 7 大类(规范/流程/审核/测试/工具/公共抽象/基础设施)

---

## P0 — 本周必做(5 项)

| # | 措施 | 类别 | 涉及 Bug | 落地位置 | 工作量 |
|---|------|------|---------|----------|--------|
| 1 | 写 `docs/规范/Open-TMS-REST-规范.md`(约定 Page 响应 5 字段) | 规范 | #1, #2, #3 | docs/规范/ | 0.5h |
| 2 | 写 `web/src/composables/useApiResult.js` composable | 公共抽象 | #1 | web/src/composables/ | 1h |
| 3 | `.husky/pre-commit` 加 grep 规则禁 `res.data.list` | 工具 | #1, #4 | .husky/ | 0.5h |
| 4 | 写 `scripts/db/verify-schema.sh` 比对 schema vs 实际 DB | 工具 | #5 | scripts/db/ | 1h |
| 5 | 修 9 个未修复的 `.list` 残留(approval/dealing/deposit/fundplan/loan) | 代码 | #1 | web/src/views/ | 1h |

**P0 总工作量**: 4h

---

## P1 — 本月建议(8 项)

| # | 措施 | 类别 | 涉及 Bug | 落地位置 | 工作量 |
|---|------|------|---------|----------|--------|
| 1 | 加 `opentms-review-frontend` **FE-033:Response 字段消费一致** checklist | 审核 | #1, #3 | .claude/skills/ | 0.5h |
| 2 | 加 `opentms-review-backend` "controller SQL 跨表覆盖度"checklist | 审核 | #2 | .claude/skills/ | 0.5h |
| 3 | 加 `opentms-review-db` "DDL vs 实际 DB 一致性"checklist | 审核 | #5 | .claude/skills/ | 0.5h |
| 4 | Phase 5 BE 交付物加 `*-response-schema.md` | 流程 | #1, #2, #3 | docs/api/ | 流程 |
| 5 | Phase 5 BE 强制"DB migration 必须在测试环境验证" | 流程 | #5 | opentms-feature-dev | 流程 |
| 6 | 写 `scripts/test/check_required_fields.py`(DTO @NotNull vs el-form rules) | 测试 | #3 | scripts/test/ | 1d |
| 7 | 修 9 个 `.list` 后,跑 dropdown e2e 全量验证 | 测试 | #1 | scripts/test/ | 0.5h |
| 8 | `webapp-testing` skill 加"Vue CDN 全局检测"check | 工具 | #4 | .claude/skills/ | 0.5h |

**P1 总工作量**: 3.5 天

---

## P2 — 下季度锦上添花(5 项)

| # | 措施 | 类别 | 涉及 Bug | 落地位置 | 工作量 |
|---|------|------|---------|----------|--------|
| 1 | 后端统一 `PageResult<T>` 包装类 | 公共抽象 | #1, #2 | common 模块 | 1d |
| 2 | 引入 Flyway/Liquibase 自动化 schema migration | 基础设施 | #5 | 引入 + 配置 | 1w |
| 3 | 用 Bean Validation 自动生成 OpenAPI schema | 工具 | #1, #3 | basedata + dealing | 1w |
| 4 | Vitest + MSW 自动化契约测试 | 测试 | #1, #2, #3 | web/tests/ | 1w |
| 5 | 写 `scripts/build-html-prototype.py` 统一 HTML 模板 | 工具 | #4 | scripts/ | 0.5d |

**P2 总工作量**: 4.5 周

---

## 跨主题改进(战略级)

### 主题 1:字段命名/契约一致性
- P0 #1,#2: 写 Open-TMS-REST-规范
- P1 #1: FE-033 checklist
- P2 #1: PageResult 包装类
- P2 #3: Bean Validation 自动生成 OpenAPI
- **目标**: 消灭"前端 res.data.X 字段名猜测"类 bug

### 主题 2:DB Schema 部署自动化
- P0 #4: verify-schema.sh
- P1 #5: 测试环境验证
- P2 #2: Flyway
- **目标**: 消灭"DDL 写好但生产没建"类 bug

### 主题 3:Phase 5/6 边界门禁强化
- P1 #4: response-schema.md
- P1 #5: DB migration 验证
- P1 #6: required fields check
- **目标**: 把"必填字段" / "跨表查询" / "DB 一致性"在 Phase 边界堵住

### 主题 4:HTML 原型 + 工具一致性
- P0 #3: pre-commit hook
- P1 #8: Vue CDN 检测
- P2 #5: HTML 模板生成器
- **目标**: 消灭"前端框架升级带来的全局暴露"类 bug

### 主题 5:Skill 体系建设
- 5 个 review skill 都加"覆盖率"checklist
- 新建 `opentms-bug-fix`(已完成)
- 新建 `opentms-review-contract`(P1 后续)

---

## 总结

5 个 bug 揭示的**核心问题**:
- **60%** 是规范缺失(CLAUDE.md 没写)
- **60%** 是流程漏洞(Phase 5/6 门禁没拦)
- **60%** 是测试盲区(无自动化拦截)
- **20-40%** 是历史债务 + 代码腐烂

**P0 措施 4h 内可落地**;P1 累计 3.5 天可让 Open-TMS 健壮性提升一个量级;P2 是季度工程。

**核心结论**:Open-TMS 进入"快速扩张"阶段,**测试工具化 + 流程门禁 + 规范文档**是下一阶段研发效率的关键。

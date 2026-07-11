# Bug 5: tms_at_deals_image_t 表缺失(commit 954a4b5 修复)

> 报告时间: 2026-07-11
> Skill: opentms-bug-fix v1.0
> 状态: ✅ 已修(2026-07-04 早期修复)

## 01 - 定界
- **类别**: DB/版本管理(部署遗漏)
- **证据**: AT 详情页 images 端点 500;SQL 找 `tms_at_deals_image_t` 不存在
- **结论**: DDL 文件 `db/schema/27-at-deal-image-table.sql` 写好但生产 DB 没执行

## 02 - 原因分析
- **直接**: 新表 DDL 没纳入发布流程,生产 DB 缺表
- **位置**: `db/schema/27-at-deal-image-table.sql`

## 03 - 修复
- 提交 commit `954a4b5` 修复 AT images 端点 500
- 重新跑 27 号 DDL(基于数据 8081 重启后自动执行 schema-update)

## 04 - 根因
- **研发流程**: Phase 5 BE 完成时**无 DB migration 验证步骤**——只 mvn build,没在生产 DB 跑 schema
- **需求设计**: 特性 PRD 没强制要求"DB 变更必须 production 验证"
- **设计规范**: CLAUDE.md 缺"DB schema 变更必须生产验证"约定
- **开发实现**: 后端 jar 包了新建表逻辑(SQL/JPA),但 DDL 单独管理,容易漏
- **测试工具**: 无"DB schema 与代码一致性"测试
- **Skill**: `opentms-review-db` checklist 缺"DDL vs 实际生产 DB 一致性"

## 05 - 同类排查
- 27 号后续的所有 DDL(`28-default-bank-account-rule-v1.1.sql`、`29-cashflow-enhance.sql`)都是**手动跑**——风险同源
- 9 个 unscaffolded 模块的 SQL 可能部分表也未建

## 06 - 改进措施
- **P0**: Phase 5 BE 加门禁"DB migration 必须在测试环境验证后再合 master"
- **P1**: 写 `scripts/db/verify-schema.sh` 比对 db/schema/*.sql 与实际 DB 表/字段
- **P2**: 引入 Flyway/Liquibase 自动化 schema migration

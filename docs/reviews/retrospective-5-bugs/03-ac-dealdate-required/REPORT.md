# Bug 3: AC `dealDate` 必填但前端表单不传

> 报告时间: 2026-07-11
> Skill: opentms-bug-fix v1.0
> 状态: ⏳ 待修(主代理发现但未做完整修)

## 01 - 定界
- **类别**: 前端问题(契约对齐缺失)
- **证据**: 后端 400 `dealDate 不能为空`;前端测试脚本尝试创建 AC 时未传 `dealDate` 字段
- **结论**: 后端校验符合 CLAUDE.md(必填字段),前端表单/测试用例漏传

## 02 - 原因分析
- **直接**: AC 创建表单/测试数据没填 `dealDate`
- **位置**: `web/src/views/dealing/AcDealDetail.vue` 表单 fields;`scripts/test/test_ac_deal_api.py` 测试 payload

## 03 - 修复(临时)
- 测试脚本 + `dealDate: '2026-07-11'` 后通过
- 前端表单已自动填 `dealDate = today`(2026-07-08 之前 feature 改)

## 04 - 根因
- **研发流程**: Phase 6 FE 完成后无"必填字段覆盖率"自动化测试
- **需求设计**: PRD 有列必填字段,但**未在 PRD §验收标准**强制要求
- **设计规范**: CLAUDE.md 没约定"必填字段必须前端表单有显式 form-item + rules 校验"
- **开发实现**: 前端 el-form rules 用 `required: true` 缺校验(后端 400 兜底)
- **测试工具**: 无"必填字段全覆盖"扫描脚本
- **Skill**: Phase 6 FE checklist 缺"el-form rules 与后端 DTO @NotNull 一致性"

## 05 - 同类排查
- 同样问题在 AT/FX 测试 payload(已含 dealDate,但 review 应该强制)
- 其他 9 个 unscaffolded 模块可能有同样问题

## 06 - 改进措施
- **P0**: 写 `scripts/test/check_required_fields.py`,对比后端 DTO @NotNull 与前端 el-form rules
- **P1**: Phase 6 FE checklist 加"el-form rules 100% 覆盖后端必填字段"
- **P2**: 用 Bean Validation 注解自动生成 OpenAPI schema,前端自动生成表单

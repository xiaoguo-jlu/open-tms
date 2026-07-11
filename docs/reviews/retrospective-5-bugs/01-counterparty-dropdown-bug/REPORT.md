# Bug 1: 交易对手方银行账户弹框选不到对手方

> 报告时间: 2026-07-11
> Skill: opentms-bug-fix v1.0
> 状态: ✅ 已修

## 01 - 定界
- **类别**: 前端问题
- **证据**: `curl /api/v1/counterparties/page` 返回 200 + 7 条 records;前端 `counterpartyList.value = res.data.list || []` 永远空
- **结论**: 后端正确,前端解析字段名错

## 02 - 原因分析
- **直接**: `res.data.list` 应为 `res.data.records`(MyBatis Plus Page 默认字段)
- **位置**: `web/src/views/basedata/CounterpartyAccountList.vue:165, 175, 191`

## 03 - 修复
```diff
- counterpartyList.value = res.data.list || []
+ counterpartyList.value = res.data.records || []
```
- 同步清理 10 个 .vue 的双 fallback `records || list` → 只 `records`
- Vite HMR 即时生效

## 04 - 根因(6 维度)
- **研发流程**: Phase 6 FE 没有"response 字段消费一致性"门禁
- **需求设计**: PRD 没约定 `data` 内部结构(只约定外层 4 字段)
- **设计规范**: CLAUDE.md 缺 Page 响应结构说明
- **开发实现**: 11 个 .vue 复制 CounterpartyAccountList 模板,`.list` 沿用扩散
- **测试工具**: 当时无 `popup_e2e_test.py`,本可以前置拦截
- **Skill**: `opentms-review-frontend` FE-002 不查"前端消费字段"只查"label/路径"

## 05 - 同类排查
- `scripts/test/find_similar_bugs.py --bug-type "res.data.list"` → 9 个文件 10 处(approval / dealing / deposit / fundplan / loan)
- 10 个 .vue 用了双 fallback `records || list`,腐化扩散

## 06 - 改进措施

### P0(本周)
- 加 CLAUDE.md "API 响应规范"小节(records/total/size/current/pages 5 字段)
- 写 `web/src/composables/useApiResult.js` composable
- `.husky/pre-commit` 加 grep 规则禁 `res.data.list`
- 修 9 个 .vue 残留 `.list`

### P1(本月)
- `opentms-review-frontend` 加 **FE-033:Response 字段消费一致**
- Phase 5 BE 完成时产出 `*-response-schema.md`

### P2(下季度)
- 后端统一 `PageResult<T>` 包装类

# Bug 根因分析 + 改进建议

> 触发事件: 2026-07-11 — 交易对手方银行账户新增弹框选不到交易对手
> 主代理分析,适用: Open-TMS 后续特性研发

---

## 1. 直接根因(技术层)

`web/src/views/basedata/CounterpartyAccountList.vue` 3 处访问 `res.data.list`:
- 165 行 `counterpartyList.value = res.data.list || []` ← 主 bug
- 175 行 `currencyList.value = res.data.list || []`
- 191 行 `tableData.value = res.data.list || []`

后端 MyBatis Plus `Page<T>` 实际返回:
```json
{ "code": 200, "data": { "records": [...], "total": N, "size": 10, "current": 1, "pages": 1 } }
```

`.list` 永远 undefined → ref 为 `[]` → 弹框 el-option 不渲染 → 用户看不到任何选项。

---

## 2. 系统根因(为什么 bug 能溜进生产)

### 2.1 规范缺失(规范层)

| 缺失项 | 现状 | 影响 |
|------|------|------|
| 后端 `Result<T>` 响应字段规范 | CLAUDE.md 只说"code, message, data, timestamp"4 个外层字段 | `data` 内部结构(records/total/size)无文档 |
| 前端访问响应字段的约定 | 无 | 开发者猜,猜错就成 bug |
| MyBatis Plus `Page` 返回结构 | 无项目级封装类(只用了 `com.baomidou.mybatisplus.extension.plugins.pagination.Page`) | 字段名随 MP 默认,前端必须知道 |
| 端到端字段名映射矩阵 | 无 | 前后端各自猜,容易错配 |

### 2.2 复审 checklist 漏掉(质量门禁层)

`opentms-review-frontend` 的 FE-002 checklist:
> 接口地址 / 方法 / 字段名 / 绑定标签正确
> standard: API 路径 / 方法 / 字段名 / el-form-item label 100% 与 API 文档一致
> check_method: 1. Read web/src/api/{module}/{entity}.js,记录 API 路径与字段...

**只查 "API 路径/方法/el-form-item label"**,**没查 `res.data.records` vs `res.data.list` 这种"前端消费 response 字段"的正确性**——这是 FE-002 的盲区。

### 2.3 工具缺位(测试工具层)

| 工具 | 现状 | 缺什么 |
|------|------|------|
| `scripts/api_scanner.py` | 静态分析 `url/method/params` 与 OpenAPI 契约 | **不查前端 `res.data.records/.list` 是否匹配后端实际返回** |
| `scripts/test/dropdown_e2e_test.py` | 验证 endpoint HTTP 200 + records 字段 | 不知道哪个 .vue 用了 `.list` |
| `scripts/test/popup_e2e_test.py`(本次) | 跑出来了能查 .list 误用 | 本次写的,本可以前置 |

### 2.4 研发流程缺位(流程层)

| 环节 | 缺什么 |
|------|------|
| Phase 5 BE 完成时 | 没强制要求"后端响应字段命名"给到前端 |
| Phase 6 FE 开发时 | 没强制要求"前端访问 response 字段"用公共 hook(`useApiResult`) |
| Phase 6 完成后 | `scripts/api_scanner.py` 跑后只检查 OpenAPI 契约,不查前端消费是否一致 |
| Phase 9 6 维复审 | frontend review 没有"response 字段消费"专项 checklist |

### 2.5 自动化 hook 缺位(基础设施层)

| 缺失项 | 现状 |
|------|------|
| 前端 response 解析公共方法 | 无 — 每个 .vue 自己写 `res.data.list` 或 `res.data.records` |
| 后端统一响应包装 | 有 `Result.success()` 但 Page 内部结构散在各 controller |
| 字段名漂移检测(lint 阶段) | 无 ESLint 规则禁 `res.data.list` |

### 2.6 历史债务(时间维度)

- **2026-04-06**(`5e8e2c1`): 早期开发者手写基础数据页面,猜测字段名为 `list`——**可能是从老项目搬过来没核对**
- **2026-06-14**(`eb28602`): instrument 回归基础数据 — **复制 CounterpartyAccountList 模板,`.list` 沿用**
- **2026-07-08** 默认银行账户规则特性: 写 `records` 是因为参考了 MyBatis-Plus 文档,**但其他基础数据页面没同步**
- **累计 11 个 .vue 用 `.list`,1 个孤例 `.list-only`**(CounterpartyAccountList)→ 弹框彻底空

---

## 3. 改进建议(优先级排序)

### P0(必修 — 本周)

#### 3.1 统一规范:`docs/规范/Open-TMS-REST-规范.md`(新文件)

**约定**:
```json
// 分页列表响应(基于 MyBatis Plus Page)
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [...],  // 必填
    "total": 100,      // 必填
    "size": 10,        // 必填
    "current": 1,      // 必填
    "pages": 10        // 必填
  },
  "timestamp": 1783700000000
}
```

#### 3.2 增强 API 扫描器(`scripts/api_scanner.py`)

加 P2 检查项:**前端访问的 response 字段名 vs 后端实际返回的字段名**:
```python
# 扫描 .vue 中 res.data.XXX 的引用
# 对比 OpenAPI response schema
# 字段名不匹配 → P2
```

#### 3.3 加 `useApiResult` 公共 composable(`web/src/composables/useApiResult.js`)

```js
// 用法: const { data, loading, error } = useApiResult(() => listBank({ pageSize: 10 }))
// 自动解 res.data.records / res.data.total
// 不再让每个 .vue 自己写 res.data.list
```

11 个 .vue 重构使用该 composable(预计 2-3 天)。

#### 3.4 lint 规则:`eslint-plugin-` 或简单 grep pre-commit hook

```bash
# .husky/pre-commit
git diff --cached --name-only | grep '\.vue$' | xargs grep -l "res.data.list" && {
  echo "❌ 发现 res.data.list(应改为 res.data.records)"
  exit 1
}
```

---

### P1(建议 — 本月)

#### 3.5 增强 review-frontend skill

新增 checklist 项 **FE-033:Response 字段消费一致**:
```yaml
- id: FE-033
  name: Response 字段消费正确
  severity: P1
  standard: 前端访问的 res.data.XXX 字段名与后端 OpenAPI schema 100% 一致
  check_method: |
    1. Grep `res.data\.\w+` 收集所有前端消费字段;
    2. 对照 OpenAPI response schema 字段名;
    3. 不一致项 = P1。
  pass_criteria: 100% 一致
  failure_action: 改为 schema 实际返回的字段名
```

#### 3.6 增强扫描器(降级为 1.1)

新增 `--check-response` 模式:专门查前端 `res.data.X` vs OpenAPI response schema。

#### 3.7 Phase 5 BE 完成时新增交付物

要求 Phase 5 BE 完成后产出 `docs/api/{feature}-response-schema.md`,列所有新增/修改端点的 response schema。前端必须**先看 schema 再写代码**。

---

### P2(锦上添花 — 下季度)

#### 3.8 后端统一 `PageResult<T>` 包装类

```java
// common 模块新建
public class PageResult<T> {
    private List<T> records;
    private long total;
    private long size;
    private long current;
    private long pages;
    // getter / setter / static of(IPage<T>) 工厂
}
```

所有 controller 返回 `Result.success(PageResult.of(page))` —— 字段名固化,后端字段名修改影响降到 0。

#### 3.9 自动化契约测试(Vitest + MSW)

前端集成测试时 mock 后端 response,**强制断言返回结构与 OpenAPI 一致**——任何字段名漂移自动报警。

#### 3.10 Phase 9 6 维复审新增"后端-前端契约"维度

`opentms-review-contract` 新 skill:对每个新特性,跑一遍 `gen-openapi.sh` + `api_scanner.py --check-response`,出契约一致性报告。

---

## 4. 这次 bug 暴露的更深层问题

### 4.1 "代码腐烂"扩散

防御性写法 `res.data.records || res.data.list || []` 让 bug **看上去能跑**(其实是空数组 fallback),没人发现,代码腐烂扩散到 11 个 .vue。

**教训**:**fallback 模式掩盖 bug**。应**显式报错** `if (!res.data.records) throw new Error('API contract changed')`。

### 4.2 测试工具写得太晚

`scripts/test/popup_e2e_test.py` 是 bug 出现后**才**写的,而不是预防性的——**测试工具应在 Phase 6 FE 完成后立即写**,作为 smoke test 跑过再进 QA。

**教训**:`gen-openapi.sh` + `api_scanner.py` + `dropdown_e2e_test.py` + `popup_e2e_test.py` 应**作为 Phase 7 入口必跑**的 4 件套。

### 4.3 Skill 体系"产出规范"≠"消费规范"

CLAUDE.md 定义了 `Result<T>` 4 个外层字段,但**没定义 data 内部结构**——开发者消费时只能猜。这是规范文档的常见盲区。

**教训**:**每个公共 API 规范,既要约定"返回什么"也要约定"消费者怎么读"**。

---

## 5. 立即行动项(主代理)

| # | 行动 | 优先级 | 工作量 |
|---|------|------|------|
| 1 | 写 `docs/规范/Open-TMS-REST-规范.md` | P0 | 0.5h |
| 2 | 写 `web/src/composables/useApiResult.js` | P0 | 1h |
| 3 | 改 `scripts/api_scanner.py` 加 `--check-response` | P0 | 2h |
| 4 | 加 `.husky/pre-commit` grep 规则 | P0 | 0.5h |
| 5 | 重构 11 个 .vue 用 useApiResult | P0 | 2-3h |
| 6 | 加 `FE-033` checklist 到 opentms-review-frontend | P1 | 0.5h |
| 7 | Phase 5 BE 交付物加 `*-response-schema.md` | P1 | 流程 |
| 8 | 写 `PageResult<T>` 包装类 | P2 | 1d |
| 9 | 自动化契约测试 | P2 | 1w |

**P0 总工作量约 1 个工作日**;P1+P2 累计 1.5 周。

---

## 6. 类似 Bug 风险预防(横向扫描)

**其他可能存在的"字段名猜测"风险点**:
- `tms_deal_map_t` / `tms_actions_t` 等表的字段名
- dealing 模块 CashflowVO / ActionVO / DealMapVO 字段消费
- 新增 AuditHistoryVO 字段(刚完成)
- API scanner 145 P0 中 unscaffolded 模块的字段

**建议**:在 `useApiResult` 推广后,要求所有响应字段消费都走它,避免下一波腐烂。

# Open-TMS 前端 API 一致性扫描工具使用指南

> 状态:**已上线** (frontend-api-scanner v1.0 · 2026-07-10)  
> 维护者:Open-TMS Tech Lead  
> 配套:`scripts/api_scanner.py` + `scripts/api_scanner_test.py`

---

## 1. 为什么需要这个工具

Open-TMS 后端按 CLAUDE.md 规范提供两类契约:
- **基于数据 (CXF)** — 由自写 `OpenApiCxfScanner` 反射扫描 `@Path` 资源
- **交易 (Spring MVC)** — 由 springdoc-openapi 自动扫描 `@RestController`

合并后产物:`docs/api/openapi.json` (131 paths,47 schemas,2026-07-10 快照)。

但前端 API 调用方常在以下场景出问题:
1. 写错 url — `/api/v1/bank-accounts/{id}` vs `/api/v1/banks/{bankId}`
2. 漏必传参数 — 后端 DTO 标了 `@NotNull`,前端 form 没绑字段
3. 多传 schema 不接受的字段 — 后端严格反序列化 400
4. 改字段名忘记同步 schema — 前端 `fixRemark` vs 后端 `fix_remark`(snake_case 漂移)
5. 重命名 controller 但前端没跟 — path 错

**手工逐个对 path 太慢**;`frontend-api-scanner` 把这件事**静态**自动化 — 不跑后端,只读源码 + 读 OpenAPI,CI 跑一次就能立刻定位。

---

## 2. 工具组成

| 文件 | 用途 |
|------|------|
| `scripts/api_scanner.py` | 主程序,扫描 + 对比 + 报告生成 |
| `scripts/api_scanner_test.py` | 自测(用 5 个故意写错的 fixture 验证检测能力) |
| `docs/api/frontend-api-consistency.html` | 默认 HTML 报告产物 |
| `docs/api/openapi.json` | 输入(契约源) |

**依赖**:仅 Python 3.8+ 标准库 (`json` / `re` / `argparse` / `urllib` / `dataclasses` / `html` / `pathlib`),无第三方依赖。

---

## 3. 快速开始

### 3.1 默认运行

```bash
cd F:/code/opencode/opentrm
python scripts/api_scanner.py
```

输出:
- 控制台摘要:扫描 API 数 / P0/P1/P2 / 评级
- `docs/api/frontend-api-consistency.html` (默认 < 200KB)

### 3.2 自定义参数

```bash
# 1. 扫描不同目录
python scripts/api_scanner.py --api-dir web/src/api

# 2. 显式指定 OpenAPI(避免加载默认)
python scripts/api_scanner.py --openapi docs/api/openapi.json

# 3. 输出到不同路径
python scripts/api_scanner.py --report-html docs/reviews/api-scan-2026-07-10.html

# 4. 同时输出 JSON 给 CI
python scripts/api_scanner.py --json docs/api/scan.json

# 5. 一次性:先 bash gen-openapi.sh,再扫描
python scripts/api_scanner.py --gen-openapi

# 6. CI 模式(P0 存在时 exit 1)
python scripts/api_scanner.py --ci
```

### 3.3 完整 CLI

```
usage: api_scanner.py [-h] [--api-dir API_DIR] [--openapi OPENAPI]
                      [--report-html REPORT_HTML] [--json JSON]
                      [--gen-openapi] [--ci] [--quiet]
```

---

## 4. 评级体系(沿用 opentms-review-common)

| 评级 | 含义 | 后续动作 |
|------|------|----------|
| **A** | 无 P0/P1/P2 | 通过,直接进入集成阶段 |
| **B** | 仅 P2(可优化不阻塞) | 通过,P2 记入待优化 |
| **C** | 有 P1(必须修复) | 修复 P1 → 复扫 |
| **D** | 有 P0(必须返工) | 阻塞,**不通过** |

CI 模式:`--ci` 时只要存在 P0 就 `exit 1`,确保带病合入。

---

## 5. 检测能力矩阵

| 类别 | 严重度 | 示例 | 触发条件 |
|------|--------|------|----------|
| `path` | P0 | url 不在 OpenAPI 中 | 模板字符串替换后未精确匹配,且位置不同 |
| `path-param` | P1 | `${accountId}` vs `{id}` | 模板变量名不在 OpenAPI path params 集合中 |
| `query-extra` | P1 | `params: { foo }` 但 OpenAPI 不收 | 字段不在 OpenAPI query 列表 |
| `query-missing` | P0 | OpenAPI 标记 required 但前端没传 | 必传 query 字段缺失 |
| `body-extra` | P0 | `data: { unknownField }` | schema properties 没这个字段 |
| `body-missing` | P0 | `data: { remark }` 但 DTO 必传 15 个 | schema required 字段缺失 |
| `static-analyze` | P2 | `params: opts` 变量传递 | 非字面量对象,跳过对比 |

---

## 6. 报告 HTML 结构

报告复用 `opentms-review-frontend/templates/report.html` 视觉风格:
- 顶栏 `#1f2d3d` + Open-TMS 品牌
- 4 KPI 卡片:扫描 API / 已匹配 / 路径错 / 参数错
- P0/P1/P2 三段独立表格
- 来源标识改为 `frontend-api-scanner`(区别于人工 review 报告)

每行问题含 6 列:
- 严重度 + 位置 (`文件:行号`) + 问题 + 标准 + 现状 + 修复建议

---

## 7. CI 集成

### 7.1 GitHub Actions 示例

```yaml
name: api-consistency
on: [pull_request]
jobs:
  scan:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-python@v5
        with: { python-version: '3.11' }
      - name: 拉取最新 OpenAPI
        run: |
          # 1) 启动 basedata + dealing
          # 2) bash scripts/gen-openapi.sh
          # (省略)
      - name: 扫描
        run: python scripts/api_scanner.py --ci --json scan.json
      - uses: actions/upload-artifact@v4
        with:
          name: api-consistency-report
          path: |
            docs/api/frontend-api-consistency.html
            scan.json
```

### 7.2 本地 pre-commit 钩子

```bash
# .git/hooks/pre-commit
python scripts/api_scanner.py --ci --quiet
```

---

## 8. 自测

```bash
python scripts/api_scanner_test.py
```

自测逻辑:
1. 创建 `web/src/api/_test_fixture.js`(5 个 export function,故意制造 path/query/body/static-analyze 5 类问题)
2. 跑默认扫描 + 验证 5 类 category 全部命中
3. 跑 `--ci` 模式,验证 exit code = 1
4. 验证 HTML 报告 < 200KB
5. 删除 fixture,清理

退出码 0 = 全部通过,1 = 至少 1 项失败。

---

## 9. 已知限制

1. **非字面量 params/data**:`params: opts` 这种变量传递的,工具跳过并标记 P2 (static-analyze)。建议前端工程师写 JSDoc 标注字段名,或改为内联对象。
2. **TypeScript 类型**:仅扫 `.js` / `.ts` 文件的 `export function`,不解析 TS interface;`opts.field` 的类型推断靠 JS 字面量,复杂场景会跳过。
3. **requestBody schema 嵌套**:OpenAPI 中 `$ref: #/components/schemas/FxDealDTO` 已支持,但超过 3 层嵌套的字段不展开(只比对顶层 properties 集合)。
4. **路径变量别名**:当前仅匹配 `${x}` 的字面变量名;若 JS 端拼字符串 (`url + '/delete/' + id`),工具无法识别为 path param。
5. **多服务路径重叠**:`dealing` 与 `basedata` 都有 `/api/v1/xxx/page` 端点时,`dealing` 优先(合并策略来自 gen-openapi.sh)。

---

## 10. 后续可优化

| 优化项 | 价值 | 优先级 |
|--------|------|--------|
| ESLint 插件:`@opentms/api-consistency` | 实时提示,开发态拦截 | P1 |
| 增量扫描:只扫 `git diff` 变更的 api 文件 | 减少大项目扫描时间 | P2 |
| 支持 `axios.create` 自定义 instance | 覆盖部分项目的封装变种 | P2 |
| 接入 husky pre-commit | 提交前阻断 P0 | P1 |
| 报告订阅:飞书/Slack webhook | 评审失败自动通知 | P2 |
| `--baseline` 选项:忽略历史已知问题 | 灰度引入,避免一次性爆量 | P1 |

---

## 11. 验证清单(2026-07-10 实测)

- [x] `python scripts/api_scanner.py` 跑成功(无需任何参数)
- [x] 报告 HTML 浏览器双击能打开,布局正常(< 90KB)
- [x] 扫到 285 个 API 调用
- [x] 路径错触发 145 个示例(`/ac/transactions` 等)
- [x] `--ci` 模式有 P0 时 exit 1
- [x] `--json` 模式输出 JSON(`docs/api/_diag.json` 已生成可参考)
- [x] 无第三方依赖(纯 Python 标准库)
- [x] `python scripts/api_scanner_test.py` 全部通过(5/5)

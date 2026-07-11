---
name: opentms-bug-fix
description: |
  Open-TMS Bug 修复标准流程 Skill。由主代理(Claude)在用户报 bug 或 QA/自测发现 bug 时调用。
  严格按 6 步走:1)Bug 定界 2)原因分析 3)修复 4)根因 5)同类排查 6)改进措施。
  每个 bug 修复都必须产出 docs/reviews/{bug-name}/ 报告 + 配套改进。

  Trigger: "修 bug"、"bug 修复"、"bug 报告"、"fix"、"修复这个 bug"
---

# opentms-bug-fix

Open-TMS **Bug 修复标准流程** —— 当主代理/QA/PM 收到 bug 报告时,严格按 6 步走完,
避免"修了就行"的快修模式,**确保根因被识别 + 改进措施落地 + 防止同类问题扩散**。

> **本 skill 不替代具体角色**(后端修代码/前端修代码),而是为整个 bug 修复过程提供 SOP。
> 触发后,主代理将作为**协调者**调度相关 skill(backend-dev / frontend-dev / review-* / test-execution)。

---

## 一、Skill 适用范围

| 触发场景 | 说明 |
|---------|------|
| 用户报告 bug(UI 异常/数据错/性能差/崩溃) | 用户原话触发 |
| QA 测试发现 bug | `opentms-test-execution` skill 引用 |
| Playwright/curl 自测发现 P0 | 主代理自调用 |
| Code review 6 维审核发现 | `opentms-review-*` skill 引用 |
| 日志告警 / 监控发现 | 自动触发(预留) |

**不适用范围**:
- 需求变更 → 用 `opentms-product-design`
- 设计方案错误 → 用 `opentms-business-architect`
- 新特性开发 → 用 `opentms-feature-dev`

---

## 二、Bug 修复 6 步标准流程

```
Step 1: Bug 定界
   ↓
Step 2: 原因分析
   ↓
Step 3: 修复
   ↓
Step 4: 根因分析
   ↓
Step 5: 同类排查
   ↓
Step 6: 改进措施
```

**强制要求**:6 步缺一不可。PM-Lead 审核特性时,如果 bug 修复跳过任何一步,
视为不通过,需重新走完整流程。

---

## Step 1: Bug 定界

**目标**:确定 bug 的真实归属,**避免"修了不该修的代码"**。

### 1.1 四类归属

| 类别 | 含义 | 处理 |
|------|------|------|
| **非问题(需求现状)** | 行为符合 PRD/设计,只是用户期望不同 | 不用修,告知用户这是 by-design 行为 |
| **前端问题** | 后端返回正确,前端解析/渲染错 | 修前端 |
| **后端问题** | 前端调用正确,后端逻辑/数据/响应错 | 修后端 |
| **方案设计问题** | 前后端都没错,但整体方案/PRD 本身有问题 | **不直接修,汇报用户确认** |

### 1.2 定界方法

| 排查方法 | 工具 | 关键判断 |
|---------|------|---------|
| **接口直接调用** | `curl` / `python urllib` / Postman | 不走前端,直接调后端端点,看响应是否正确 |
| **浏览器 Network 面板** | DevTools F12 | 看实际请求 URL/headers/body/response |
| **OpenAPI 对比** | `docs/api/openapi.json` vs 实际 | 后端端点是否符合 schema |
| **代码静态分析** | `Grep` / `Read` 工具 | 前端消费字段名 vs 后端实际返回 |

### 1.3 定界决策树

```
bug 报告
  ↓
调后端 API(curl 拿原始响应)
  ↓
响应正确? ─── 否 ──→ 后端问题
  ↓ 是
OpenAPI schema 一致? ─── 否 ──→ 后端问题(scheme 错)
  ↓ 是
前端调 API 方式正确? ─── 否 ──→ 前端问题
  ↓ 是
PRD/设计文档描述? ── 不一致 ──→ 方案问题
  ↓ 一致
                        → 非问题(需求现状,需与用户对齐)
```

### 1.4 定界报告

写到 `docs/reviews/{bug-name}/01-boundary.md`:
```markdown
# Bug {name} 定界报告

## 用户报告
{原始用户消息/QA 发现}

## 排查过程
### 接口直调
```bash
curl -X POST .../api/v1/...
# 响应
```
### 浏览器 Network
...

## 结论
- [ ] **非问题**(需求现状)
- [ ] **前端问题**
- [ ] **后端问题**
- [ ] **方案问题**

修复路径: 修 {前端/后端},或汇报用户确认
```

---

## Step 2: 原因分析

**目标**:找到 bug 的直接技术原因(不是根因,根因在 Step 4)。

### 2.1 排查清单

| 项 | 工具 | 关键点 |
|----|------|--------|
| **需求文档** | `docs/prd/{module}/` 或 `docs/规范/` | PRD 是否明确描述此场景? |
| **API 契约** | `docs/api/*.md` + `openapi.json` | 接口定义是否清楚? |
| **后端日志** | `/tmp/{basedata,dealing}.log` | 是否有异常堆栈? |
| **前端 console** | 浏览器 DevTools Console | 是否有 JS error? |
| **数据库** | `python scripts/db/db_tool.py -d {table}` | 数据是否符合预期? |
| **代码 diff** | `git log -p -- {file}` | 何时引入? 谁改的? |

### 2.2 常见 bug 模式

| 模式 | 检查点 | 修复方向 |
|------|--------|---------|
| 字段名错(`.list` vs `.records`) | grep `res.data.XXX` vs OpenAPI schema | 修前端 |
| 类型错(返回 string 而非 number) | Network response | 修后端或前端类型 |
| 时区错 | 数据库 timestamp vs JS Date | 修后端(统一 UTC) |
| 缓存陈旧 | 重启服务后是否仍错 | 加缓存失效 |
| 路由冲突(CXF vs Spring MVC) | 直接 8081 vs Vite 代理 | 修路由配置 |
| 编码错(GBK vs UTF-8) | curl 中文响应 | 修请求/响应 encoding |
| 空指针/NPE | 堆栈含 NullPointerException | 加空值检查 |

### 2.3 原因分析报告

写到 `docs/reviews/{bug-name}/02-cause.md`:
```markdown
# Bug {name} 原因分析

## 直接原因
{技术层面: 代码哪一行/哪个字段错了}

## 触发场景
- 用户操作: ...
- 数据: ...
- 时间: ...

## 关键证据
- 日志片段
- curl 输出
- 截图
- 代码 diff

## 涉及文件
- path/to/file.java:123
- path/to/file.vue:45
```

---

## Step 3: 修复

**目标**:精准修复,最小变更。

### 3.1 修复原则

- **最小变更**: 只改直接相关的代码,不做"顺手优化"
- **不破坏存量**: 不影响其他端点/页面
- **保留测试用例**: 修 bug 时,补一个回归测试用例
- **Commit 规范**: `fix(module): 修复 {bug-name} - {一句话根因}`

### 3.2 修复流程

```
1. 写代码(Edit 工具,精确替换)
2. 编译/构建确认: mvn / npm run dev
3. 端到端验证: 复现用户场景
4. 跑回归测试: 相关 Playwright/curl 脚本
5. 更新 PRD/CLAUDE.md(若规范缺失)
6. 提交 commit
```

### 3.3 修复报告

写到 `docs/reviews/{bug-name}/03-fix.md`:
```markdown
# Bug {name} 修复记录

## 修复 diff
{Before/After 代码片段}

## 验证步骤
1. 复现用户场景
2. 端到端测试
3. 回归测试

## Commit
```bash
git add -A
git commit -m "fix(basedata): 修复交易对手弹框选不到对手方 - res.data.list → res.data.records"
```
```

---

## Step 4: 根因分析

**目标**:穿透直接原因,找到引入这个 bug 的**流程/规范/工具漏洞**。

### 4.1 6 个角度根因分析

| 角度 | 问题 | 排查方法 |
|------|------|---------|
| **研发流程** | 哪一阶段没拦截?(PM 门禁/UX 门禁/TA 门禁/Dev 门禁/QA 门禁/Code Review) | 对照 opentms-feature-dev 9 Phase,看哪一阶段没检测出 |
| **需求设计** | PRD 是否有歧义?字段定义是否完整? | `docs/prd/` grep 关键字段 |
| **设计规范** | CLAUDE.md / docs/规范/ 是否有相关约定? | 查规范文档 |
| **开发实现** | 是否有公共抽象/可复用?代码风格? | 看 .vue / .java 一致性 |
| **测试工具** | 是否有自动化测试能拦截?为何没拦? | `scripts/test/` 是否有相关测试 |
| **Skill** | 是否有 skill 应该有此检查项? | 查 `opentms-review-*` |

### 4.2 根因模板(必填)

写到 `docs/reviews/{bug-name}/04-root-cause.md`:
```markdown
# Bug {name} 根因分析

## 6 维度分析

### 4.1 研发流程
- 哪一阶段应该拦截?:
- 实际是否拦截?:
- 改进: ...

### 4.2 需求设计
- PRD 是否定义清楚?:
- 改进: ...

### 4.3 设计规范
- 是否有相关规范?:
- 改进: ...

### 4.4 开发实现
- 是否有公共抽象?:
- 改进: ...

### 4.5 测试工具
- 是否有相关测试?:
- 改进: ...

### 4.6 Skill
- 哪个 skill 应该加强?:
- 改进: ...

## 根因结论
{1-2 句话总结 — 哪个环节出错了}

## 时间维度(可选)
- 何时引入?:
- 引入者?: (git blame)
- 为何没人发现?:
```

### 4.3 根因分类(常见 5 类)

| 类型 | 描述 | 修复方向 |
|------|------|---------|
| **规范缺失** | CLAUDE.md 没写,开发者猜 | 补规范 + 加 review checklist |
| **流程漏洞** | Phase 5/6 门禁没拦截 | 改 skill 流程 + 加门禁 |
| **测试盲区** | 没自动化测试 | 写测试脚本 + 集成到 CI |
| **代码腐烂** | 防御性 fallback 掩盖 bug | 移除 fallback + 显式报错 |
| **历史债务** | 早期代码错,后人不改 | 重构 + 标技术债务 |

---

## Step 5: 同类排查

**目标**:一个 bug 往往是冰山一角,**主动排查同类问题**。

### 5.1 排查方法

| 方法 | 工具 | 命令 |
|------|------|------|
| **grep 同类代码** | Grep | `grep "res.data.list" web/src/views/**/*.vue` |
| **跑扫描器** | `scripts/api_scanner.py` | 检查所有 API 契约 |
| **跑 e2e 测试** | `scripts/test/dropdown_e2e_test.py` | 检查所有 dropdown |
| **横向对照** | `git grep {pattern}` | 跨模块同模式 |

### 5.2 同类排查脚本

主代理可直接调用 `scripts/test/find_similar_bugs.py`(本 skill 自带工具):
```bash
python scripts/test/find_similar_bugs.py --bug-type "res.data.list"
```

### 5.3 同类排查报告

写到 `docs/reviews/{bug-name}/05-similar.md`:
```markdown
# Bug {name} 同类排查

## 排查范围
{哪些文件/模块/特性}

## 排查方法
- 工具:
- 扫描命令:

## 发现
| 文件 | 行号 | 问题描述 | 严重度 |
|------|------|---------|--------|
| {file} | {line} | {desc} | P0/P1/P2 |

## 修复
- 已修:{n} 个
- 待修:{m} 个
- 不修(遗留技术债务):{k} 个
```

---

## Step 6: 改进措施

**目标**:把根因 → 改进措施 → 落地,避免后续再发现。

### 6.1 改进措施分类(7 大类)

| 类别 | 措施类型 | 落地方式 |
|------|---------|---------|
| **研发流程** | 加门禁/阶段/审核 | 改 `opentms-feature-dev/SKILL.md` |
| **开发规范** | 命名/字段/结构约定 | 改 `CLAUDE.md` 或 `docs/规范/*.md` |
| **测试工具** | 写脚本/集成 CI | 新建/改 `scripts/test/*.py` |
| **公共抽象** | 提取 composable/工具类 | 新建 `web/src/composables/*.js` |
| **审核 Skill** | 加 checklist/规则 | 改 `opentms-review-*/SKILL.md` |
| **Lint/Hook** | pre-commit / lint | 新建 `.husky/*` 或 `.eslintrc` |
| **文档** | 补充规范/FAQ | 改 CLAUDE.md 或 docs/ |

### 6.2 改进措施模板

写到 `docs/reviews/{bug-name}/06-improvements.md`:
```markdown
# Bug {name} 改进措施

## P0 改进(本周必做)
| # | 改进项 | 类别 | 落地位置 | 工作量 |
|---|--------|------|----------|--------|
| 1 | ... | 规范/工具/审核/... | file | Xh |

## P1 改进(本月建议)
| # | 改进项 | 类别 | 落地位置 | 工作量 |
|---|--------|------|----------|--------|
| 1 | ... | ... | ... | ... |

## P2 改进(下季度)
| # | 改进项 | 类别 | 落地位置 | 工作量 |
|---|--------|------|----------|--------|

## 总工作量
P0 + P1 + P2 = X 人天
```

### 6.3 改进措施落地原则

| 原则 | 说明 |
|------|------|
| **P0 必做** | 防止同类 bug 再次出现 |
| **P1 建议** | 提升效率/可维护性 |
| **P2 锦上添花** | 远期优化,技术债务清单 |
| **必须写代码** | 改进措施不能只写文档,必须有 commit |
| **回归测试** | 每个改进措施都要有对应测试 |

---

## 三、报告模板总览

每个 bug 修复必须产出 6 份报告(在 `docs/reviews/{bug-name}/` ):

| 编号 | 文件名 | 内容 | 时机 |
|------|--------|------|------|
| 01 | `01-boundary.md` | 定界: 非问题/前端/后端/方案 | Step 1 完成后 |
| 02 | `02-cause.md` | 原因分析(直接技术原因) | Step 2 完成后 |
| 03 | `03-fix.md` | 修复记录(diff + 验证 + commit) | Step 3 完成后 |
| 04 | `04-root-cause.md` | 6 维度根因分析 | Step 4 完成后 |
| 05 | `05-similar.md` | 同类问题排查清单 | Step 5 完成后 |
| 06 | `06-improvements.md` | 改进措施清单(P0/P1/P2) | Step 6 完成后 |

**总报告**:`docs/reviews/{bug-name}/REPORT.md`(6 份合一的精简版,1 页内可读)。

---

## 四、配套工具

### 4.1 `scripts/test/find_similar_bugs.py`

**用途**:根据 bug 类型,在代码库 grep 同类问题。

**用法**:
```bash
python scripts/test/find_similar_bugs.py --bug-type "res.data.list"
python scripts/test/find_similar_bugs.py --bug-type "res.data.total"
python scripts/test/find_similar_bugs.py --pattern "res\\.data\\.\\w+" --file-glob "web/src/views/**/*.vue"
```

**预置 bug 模式**:
- `res.data.list`(应该用 `res.data.records`)
- `res.data.total`(后端 `total` 字段为 0 bug)
- 字段名拼写错(根据配置)

### 4.2 `scripts/test/bug_regression_test.py`

**用途**:跑 bug 修复后的回归测试,确保问题真修复且无回归。

---

## 五、与 Open-TMS 其他 Skill 的关系

| Skill | 关系 |
|------|------|
| `opentms-pm-lead` | PM-Lead 创建 bug 报告,触发本 skill |
| `opentms-test-execution` | QA 测试发现 bug,触发本 skill |
| `opentms-review-*` | 6 维审核发现 P0,触发本 skill |
| `opentms-frontend-dev` / `opentms-backend-dev` | Step 3 修复阶段调用 |
| `opentms-feature-dev` | Step 6 改进措施可能回流到特性流程 |
| `opentms-api-scanner` | Step 2 原因分析 + Step 5 同类排查的工具 |

---

## 六、参考案例

参考已修复的真实 bug 案例:
- `docs/reviews/ROOT-CAUSE-ANALYSIS.md` — 交易对手方弹框 bug 根因分析(2026-07-11)

---

## 七、版本历史

| 版本 | 日期 | 变更 |
|------|------|------|
| v1.0 | 2026-07-11 | 初版,6 步流程 + 6 份报告模板 + 配套工具 |

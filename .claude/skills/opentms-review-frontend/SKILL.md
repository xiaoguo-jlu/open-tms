---
name: opentms-review-frontend
description: |
  Open-TMS 前端代码审核 Skill。由 Frontend Lead / Frontend Reviewer 调用,用于审核
  Vue 3 + Element Plus + Vite 前端代码(页面 / 组件 / API / Store),
  确保符合 Open-TMS 既有规范(BaseDataPicker / ModeBadge / FormContainer)、
  CLAUDE.md 前端规范、FIS Quantum / Murex MX.3 / Bloomberg AIM
  企业级 UX 对标,以及与存量已合并页面(AcDealList / CountryList /
  InstrumentList)的一致性。

  Trigger: "前端审核"、"前端评审"、"Frontend 审核"、"Vue 审核"、"前端代码 review"
---

# opentms-review-frontend

前端代码审核 — 对 Vue 3 页面 / 组件 / API / Store 进行结构化审核,确保符合
Open-TMS 既有规范与成熟资金管理系统 (FIS Quantum / Murex MX.3) 前端架构标准。

> **本 skill 遵循** `opentms-review-common` 公共规范 — 统一评级体系、报告格式、调用方式、归档路径。

---

## 输入

- 待审核的 `.vue` / `.js` / `.ts` 文件路径(必填,可多个)
- 所属模块名(必填,如 `dealing` / `basedata`)
- 关联的 UX 设计稿 / PRD / API 文档路径(可选)
- 是否新增 / 修改 / 重构(必填)

## 输出

- 审核报告: `docs/reviews/{feature-name}/frontend-review.html`
- 按 `templates/report.html` 填充(Vue 3 + Element Plus CDN,双击浏览器即可查看)
- 公共样式规范:`../opentms-review-common/templates/report.html`
- **历史 .md 文件保留作为归档,不再作为主交付物**(2026-07-10 PM-Lead 决定)

## 工作流程

1. **加载公共规范** — 读取 `opentms-review-common/SKILL.md`
2. **读取 Vue 文件** — 用 `Read` 工具读取页面 / 组件 / API 源码
3. **静态检查** — 用 `Grep` 搜索关键模式(公共组件 / API 路径 / 命名 / 校验)
4. **对标检查** — 对比 Open-TMS 既有页面(AcDealList / FxDealList / CountryList)
5. **加载 checklist** — 按 `checklists/01-component-pattern.md` / `02-api-binding.md` / `03-error-ux.md` 逐项打勾
6. **逐项审核** — 按下方 YAML checklist 逐项判定 PASS/FAIL
6.5 **运行 API 一致性扫描**(2026-07-11):`python scripts/api_scanner.py`,把扫描评级纳入报告评级
7. **输出报告** — 评级 A/B/C/D + P0/P1/P2 问题清单 + 整改建议

---

## 审核项结构化清单 (YAML 数组)

```yaml
frontend_review_items:

  # ============= 用户列出的 3 点 =============

  - id: FE-001
    name: 符合前端开发规范,公共组件复用
    severity: P0
    standard: 符合 Open-TMS 前端开发规范(CLAUDE.md) + BaseDataPicker / ModeBadge / FormContainer 强制复用
    check_method: |
      1. Read *.vue,识别 el-table / el-form / el-dialog 实现;
      2. Grep `BaseDataPicker|ModeBadge|FormContainer|ActionApprovalDialog|StatusTag` import;
      3. 验证同类场景(下拉 / 模式标识 / 表单容器)复用公共组件;
      4. 验证未自造通用组件。
    pass_criteria: 公共组件复用率 100%;无自造同类组件
    failure_action: 替换为公共组件

  - id: FE-002
    name: 接口地址 / 方法 / 字段名 / 绑定标签正确
    severity: P0
    standard: API 路径 / 方法 / 字段名 / el-form-item label 100% 与 API 文档一致
    check_method: |
      1. Read web/src/api/{module}/{entity}.js,记录 API 路径与字段;
      2. Read *.vue,验证调用路径正确;
      3. Grep `v-model=` 验证字段绑定;
      4. Read *.vue,验证 el-form-item :label 与字段名一致;
      5. 对照 API 文档验证响应字段映射。
    pass_criteria: API 路径 / 字段 / 标签 100% 对齐 API 文档
    failure_action: 退回修正

  - id: FE-003
    name: 代码文件存放路径
    severity: P0
    standard: 路径严格遵循 CLAUDE.md 约定 — web/src/views/{module}/{page}.vue + web/src/api/{module}/{entity}.js
    check_method: |
      1. Glob 文件确认路径;
      2. 验证 views/{module}/ 子目录结构;
      3. 验证 api/{module}/ 子目录结构。
    pass_criteria: 路径 100% 符合 CLAUDE.md 规范
    failure_action: 修正路径

  # ============= 业界补充审核项 (Vue 3 / Element Plus / FIS Quantum / Murex MX.3) =============

  - id: FE-004
    name: Vue 3 composition API (script setup)
    severity: P1
    standard: Vue 3 官方推荐 — <script setup> + Composition API
    check_method: |
      Grep `<script setup>` 在 *.vue;
      验证未使用 Options API(`data() / methods:`)。
    pass_criteria: 100% Vue 3 Composition API
    failure_action: 重构为 script setup

  - id: FE-005
    name: 状态管理 (Pinia)
    severity: P1
    standard: 全局状态用 Pinia(替代 Vuex)
    check_method: |
      Grep `defineStore|useStore` 在 web/src/store/;
      验证未使用 Vuex。
    pass_criteria: 100% Pinia
    failure_action: 迁移至 Pinia

  - id: FE-006
    name: 路由懒加载
    severity: P1
    standard: Vue Router 懒加载 — () => import('@/views/...')
    check_method: |
      Grep `component: \(\) => import` 在 router;
      验证路由全部懒加载。
    pass_criteria: 100% 路由懒加载
    failure_action: 改为动态 import

  - id: FE-007
    name: API 模块化 (/api/{module}/{entity}.js)
    severity: P0
    standard: CLAUDE.md 强制 — API 调用统一放 web/src/api/{module}/{entity}.js
    check_method: |
      1. Glob web/src/api/{module}/*.js;
      2. Grep `axios|request\.(post|get)` 在 *.vue 中是否直接调用(违规);
      3. 验证统一通过 api 模块调用。
    pass_criteria: 0 *.vue 直接 axios/request 调用;100% 通过 api 模块
    failure_action: 抽取到 api 模块

  - id: FE-008
    name: 错误处理统一 (request.js 拦截 ElMessage)
    severity: P0
    standard: 统一拦截器处理错误 — request.js response interceptor → ElMessage
    check_method: |
      Read web/src/utils/request.js;
      验证 401 / 403 / 500 等错误码自动 ElMessage。
    pass_criteria: request.js 拦截器覆盖所有错误码
    failure_action: 补充拦截器

  - id: FE-009
    name: 401 跳转登录页
    severity: P0
    standard: 401 状态码自动清除 token + 跳转 /login
    check_method: |
      Read request.js 拦截器;
      验证 401 → removeToken + router.push('/login')。
    pass_criteria: 401 跳转登录 100% 实现
    failure_action: 补充 401 处理

  - id: FE-010
    name: 友好加载状态
    severity: P1
    standard: 所有异步操作有 v-loading 或 el-button :loading
    check_method: |
      Grep `v-loading|:loading|loading=` 在 *.vue;
      验证关键操作有 loading。
    pass_criteria: 100% 异步操作有 loading
    failure_action: 补充 loading

  - id: FE-011
    name: 友好空状态 (el-empty)
    severity: P1
    standard: 列表 / 表格 / 搜索结果用 el-empty 显示空状态
    check_method: |
      Grep `el-empty` 在 *.vue;
      验证空数据时显示 el-empty。
    pass_criteria: 100% 空数据场景用 el-empty
    failure_action: 补充 el-empty

  - id: FE-012
    name: 联动逻辑完整 (币种对 / 管理主体 / 账户)
    severity: P1
    standard: Murex MX.3 录入联动 — 币种对切换自动填币种;管理主体切换默认账户
    check_method: |
      Grep `@change|@watch` 在 *.vue;
      验证 3+ 处典型联动场景实现。
    pass_criteria: ≥3 处联动场景
    failure_action: 补充联动逻辑

  - id: FE-013
    name: 表单校验 (async-validator)
    severity: P0
    standard: Element Plus el-form + el-form-item :rules 完整校验
    check_method: |
      Grep `:rules|el-form-item` 在 *.vue;
      验证必填项 / 格式校验。
    pass_criteria: 100% 必填项有校验;格式校验完整
    failure_action: 补充校验规则

  - id: FE-014
    name: 表单保存后状态 (按钮 loading)
    severity: P0
    standard: 保存按钮 :loading 防重复提交;成功后 ElMessage.success
    check_method: |
      Read *.vue,验证:
        - 保存按钮 :loading="submitting"
        - 成功后 ElMessage.success + 跳转列表
        - 失败后 ElMessage.error + 保留表单
    pass_criteria: 100% 表单有 loading + 反馈
    failure_action: 补充 loading + 反馈

  - id: FE-015
    name: 按钮 / 标签 / 输入组件风格统一
    severity: P1
    standard: 统一使用 Element Plus 组件,不自造风格
    check_method: |
      Grep `<el-button|<el-input|<el-select|<el-tag`;
      验证未使用原生 HTML 按钮 / 输入。
    pass_criteria: 100% Element Plus 组件
    failure_action: 替换为 Element Plus

  - id: FE-016
    name: 列表分页
    severity: P0
    standard: CLAUDE.md 强制 — 列表页四件套(搜索 + 工具栏 + 表格 + 分页)
    check_method: |
      Grep `el-pagination` 在列表页 *.vue;
      验证 :total / :page-size / :current-page 绑定。
    pass_criteria: 100% 列表有 el-pagination
    failure_action: 补充分页

  - id: FE-017
    name: 搜索防抖 (debounce 300ms)
    severity: P1
    standard: 搜索输入 300ms 防抖,避免频繁请求
    check_method: |
      Grep `debounce|@input` 在搜索区;
      验证有防抖处理。
    pass_criteria: 搜索框 100% 有防抖
    failure_action: 加防抖

  - id: FE-018
    name: 高亮显示关键数据 (金额大字体)
    severity: P2
    standard: 详情页关键金额用 AmountDisplay 组件(大字号 + 等宽)
    check_method: |
      Grep `AmountDisplay` 在详情页;
      验证关键金额使用。
    pass_criteria: 详情页金额字段用 AmountDisplay
    failure_action: 替换 AmountDisplay

  - id: FE-019
    name: 时间格式统一 (YYYY-MM-DD HH:mm:ss)
    severity: P1
    standard: 全局时间格式 YYYY-MM-DD HH:mm:ss(基于 CLAUDE.md)
    check_method: |
      Grep `formatDate|moment\(|dayjs\(` 在 *.vue;
      验证统一格式化。
    pass_criteria: 100% 时间字段有格式化
    failure_action: 统一格式化

  - id: FE-020
    name: 大数字格式化 (千分位)
    severity: P1
    standard: 金额 / 数量字段千分位显示(基于 Open-TMS UX 标准)
    check_method: |
      Grep `toLocaleString|formatAmount` 在 *.vue;
      验证金额显示千分位。
    pass_criteria: 100% 金额字段千分位
    failure_action: 加千分位格式化

  - id: FE-021
    name: 货币符号显示
    severity: P2
    standard: 金额字段显示对应币种符号(¥ / $ / €)
    check_method: |
      Grep `currencySymbol|ccy` 在金额显示处;
      验证币种符号。
    pass_criteria: 关键金额显示币种符号
    failure_action: 加币种符号

  - id: FE-022
    name: 多语言预留 (i18n)
    severity: P2
    standard: 关键文案走 i18n,预留多语言扩展
    check_method: |
      Grep `t\(|useI18n|vue-i18n` 在 *.vue;
      验证关键文案 i18n 化。
    pass_criteria: ≥50% 关键文案 i18n 化
    failure_action: 抽取 i18n

  - id: FE-023
    name: 响应式设计
    severity: P2
    standard: 1920 / 1440 / 1280 三种分辨率下不出现横向滚动条
    check_method: |
      Grep `@media|flex-wrap|el-row` 在 *.vue;
      验证响应式布局。
    pass_criteria: 主流分辨率无横向滚动条
    failure_action: 加响应式

  - id: FE-024
    name: 浏览器兼容
    severity: P2
    standard: Chrome / Edge / Firefox 最新 2 个大版本
    check_method: |
      Grep `babel.config|polyfill` 在 vite.config;
      验证无 ES2020+ 未兼容语法。
    pass_criteria: 主流浏览器兼容
    failure_action: 加 polyfill

  - id: FE-025
    name: ESLint 通过
    severity: P1
    standard: ESLint 0 error,允许 ≤5 warning
    check_method: |
      npm run lint;
      验证 0 error。
    pass_criteria: ESLint 0 error
    failure_action: 修正 ESLint

  - id: FE-026
    name: TypeScript 类型
    severity: P2
    standard: 关键组件用 <script setup lang="ts"> 强类型
    check_method: |
      Grep `lang="ts"` 在 *.vue;
      验证 TS 覆盖率。
    pass_criteria: 关键组件 100% TS
    failure_action: 迁移 TS

  - id: FE-027
    name: 组件命名 (PascalCase)
    severity: P1
    standard: Vue 3 官方规范 — 组件名 PascalCase(AcDealList.vue / BaseDataPicker.vue)
    check_method: |
      Grep `components:|` 命名;
      验证 PascalCase。
    pass_criteria: 100% PascalCase
    failure_action: 改名 PascalCase

  - id: FE-028
    name: 变量命名 (camelCase)
    severity: P2
    standard: JS / TS 变量 / 函数 camelCase;常量 UPPER_SNAKE
    check_method: |
      Grep `const [a-z]+ =|function [a-z]+`;
      验证命名规范。
    pass_criteria: 100% camelCase
    failure_action: 改名 camelCase

  - id: FE-029
    name: 避免大文件 (>500 行)
    severity: P2
    standard: 单文件 ≤500 行;超 500 行需拆分
    check_method: |
      Bash: wc -l *.vue;
      验证文件大小。
    pass_criteria: 100% 文件 ≤500 行
    failure_action: 拆分组件

  - id: FE-030
    name: 与 Open-TMS 现有页面风格一致
    severity: P1
    standard: 与 AcDealList / CountryList / InstrumentList 等既有页面视觉 / 交互一致
    check_method: |
      1. 对比同模块页面布局 / 按钮位置 / 状态色;
      2. 验证列定义 / 搜索区 / 工具栏结构一致。
    pass_criteria: 与既有页面差异点 ≤3 处
    failure_action: 对齐既有页面

  - id: FE-031
    name: BaseDataPicker @change/preloadRow 使用完整性
    severity: P1
    standard: 使用 BaseDataPicker 时必须正确处理 @change 事件做联动,preloadRow 用于编辑回填,autoFilter 用于筛选联动。缺任何一项即不完整
    check_method: |
      1. Grep `BaseDataPicker` 在所有 *.vue 中
      2. 验证每个实例: @change 处理联动 / preloadRow 用于编辑回填
      3. 验证 autoFilter 用于筛选联动场景
      4. 验证无 BaseDataPicker 实例缺失事件处理
    pass_criteria: 100% BaseDataPicker 实例有 @change 处理;编辑场景 100% 有 preloadRow
    failure_action: 补充缺失的事件处理

  # ============= 2026-07-11 新增:API 一致性 =============
  - id: FE-032
    name: 前端 API 一致性扫描
    severity: P0
    standard: 跑 scripts/api_scanner.py,验证前端 API 调用 (web/src/api/**/*.js) 与后端 OpenAPI 契约一致 — 路径 / 方法 / 参数 / body 字段 100% 对齐
    check_method: |
      1. 运行 python scripts/api_scanner.py(必要时先 bash scripts/gen-openapi.sh 拉最新契约);
      2. 读取 docs/api/frontend-api-consistency.html,确认评级 (A/B/C/D);
      3. CI 模式可用 python scripts/api_scanner.py --ci(P0 存在 exit 1)。
    pass_criteria: 扫描评级 A(无问题) 或 B(仅 P2);无 P0/P1
    failure_p1: 评级 C(有 P1) — 退回修复后复审
    failure_p0: 评级 D(有 P0) — 强制返工,修改 API 封装代码后重新提交
    failure_action: 退回 FE 开发者按 P0/P1 清单修改 web/src/api/ 下的 API 封装
    references:
      - docs/api/FRONTEND-API-SCANNER.md(详细使用指南)
      - CLAUDE.md "API 一致性扫描" 小节
```

---

## 审核流程 (Agent 可执行)

### Step 1: 范围确认
```bash
# 通过 Glob 定位待审核文件
web/src/views/{module}/{name}.vue
web/src/api/{module}/{entity}.js
```

### Step 2: 静态检查 (Read / Grep)

| 模式 | 用途 |
|------|------|
| `Read *.vue` | 读取页面 / 组件源码 |
| `Grep "BaseDataPicker\|ModeBadge"` | 检测公共组件复用 |
| `Grep ":rules=\""` | 检测表单校验 |
| `Grep "axios\|request\.(post\|get)"` | 检测 API 调用规范 |
| `Grep "console.error"` | 检测错误处理 |
| `Grep "v-loading\|:loading"` | 检测 loading 状态 |
| `Grep "el-empty"` | 检测空状态 |

### Step 3: 对标检查
对比 `web/src/views/dealing/ac/AcDealList.vue` 或 `web/src/views/basedata/CountryList.vue`。

### Step 4: 评级与输出
- 按 3 级 (P0/P1/P2) 打标问题;
- 按 `templates/report.md` 输出报告;
- 按 `opentms-review-common` 评级:
  - 含 P0 → D (返工)
  - 含 P1 → C (修复后复审)
  - 仅 P2 → B (通过,记录待优化)
  - 无任何问题 → A

### Step 5: 整改建议
- 每个问题提供具体代码片段或修改路径;
- 标注预计工时;
- 归档到 `docs/reviews/{feature-name}/frontend-review.md`。

---

## 一票否决 (P0 直判 D)

- **FE-001**: 未复用公共组件,自造通用组件 → D
- **FE-002**: API 路径 / 字段 / 标签错误 → D
- **FE-003**: 文件路径违规 → D
- **FE-007**: *.vue 直接 axios/request 调用 → D
- **FE-008**: request.js 拦截器缺失 → D
- **FE-009**: 401 未跳转登录 → D
- **FE-013**: 表单无校验 → D
- **FE-014**: 保存无 loading / 反馈 → D
- **FE-016**: 列表无分页 → D

---

## 协作关系

```
opentms-ux-design (UX 设计)
   └─→ opentms-frontend-dev (前端实现)
        └─→ opentms-review-ux (UX 审核)
             └─→ opentms-review-frontend (本次) ★ 前端代码审核
                  └─→ opentms-test-execution (QA 测试)
```

**与 opentms-review-ux 区别**:
- `opentms-review-ux`: 用户交互 / 视觉一致性 / 操作体验
- `opentms-review-frontend`: 代码风格 / 组件命名 / API 绑定 / 校验

---

## 相关文件

- `checklists/01-component-pattern.md` — 组件模式清单
- `checklists/02-api-binding.md` — API 绑定清单
- `checklists/03-error-ux.md` — 错误与体验清单
- `references/standards.md` — 前端标准映射
- `templates/report.md` — 审核报告模板
- `../../opentms-review-common/SKILL.md` — 公共规范
- `../../../CLAUDE.md` — Open-TMS 项目规范
- `../../../web/src/views/dealing/` — 既有参考页面

---

## 版本历史

| 版本 | 日期 | 变更内容 |
|------|------|---------|
| v1.0 | 2026-07-05 | 初始版本 — 30 项前端审核项 (11 P0 / 13 P1 / 6 P2) |
| v1.1 | 2026-07-05 | 新增 FE-031: BaseDataPicker @change/preloadRow 完整性检查 |
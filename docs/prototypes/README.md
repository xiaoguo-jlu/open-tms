# Open-TMS UX P0 改进原型

> **版本**: v1.0
> **日期**: 2026-07-10
> **作者**: UX P0 原型子代理
> **目的**: 可视化呈现 UX 评审报告中的 9 项 P0 改进,支持评审会议演示与开发对齐

---

## 文件清单

| 文件 | 大小 | 用途 |
|------|------|------|
| `ux-p0-improvements.html` | ~107 KB | **单 HTML 多页**交互原型,9 个 Tab |
| `README.md` | (本文档) | 原型说明、Tab 索引、源报告对应 |

---

## 如何打开

**最简单(零配置)**:
```
双击 docs/prototypes/ux-p0-improvements.html
```

需要联网(首次加载 CDN 依赖): Element Plus 2.4.6 + Vue 3.4.21 + @element-plus/icons-vue 2.1.0

**推荐的本地静态服务器**(避免 file:// 协议限制):
```bash
cd docs/prototypes
python -m http.server 8088
# 浏览器打开 http://localhost:8088/ux-p0-improvements.html
```

**支持的视口**:
- 1440px(优先,主设计宽度)
- 1280px(完整可用)
- 1024px(可用,Tab 8 表格需横向滚动)

---

## 9 个 Tab 简述

| Tab | 标题 | 核心交互 | 涉及模块 |
|-----|------|---------|---------|
| **1** | 折叠筛选器 | 「高级筛选」按钮展开/收起 + 「已选 N 项」徽标;BankAccount 6 筛选项折叠后首屏表格可见 | 基础数据 / BankAccount |
| **2** | 主体预填 | 顶部用户切换器(onChange 自动重填默认主体)→ 表格自动过滤;切换用户看不同子集 | 规则 / DefaultBankAccountRule |
| **3** | 余额列 | 表格新增「余额/IBAN/SWIFT/最后同步」4 列;顶栏 KPI 总余额(折 USD)/活跃/跨境账户数 | 基础数据 / BankAccount |
| **4** | AT 审批按钮 | 详情页右上角「审批通过/驳回/撤回」3 按钮;弹窗必填意见/原因;生命周期 Timeline | 交易 / AT 详情 |
| **5** | FX 列表 Picker | 管理主体/对手方 `el-select` filterable + remote 搜索;顶栏快速日期按钮组(今日/本周/本月/本季/全部) | 交易 / FX 列表 |
| **6** | RATE_FIX Toast | 4 个 Tab 切换;触发 3 种状态 toast(正向/反向/异常);右上角弹出 + 4 秒自动消失 + 「查看 cashflow/DealMap →」链接 | 交易 / FX NDF |
| **7** | FX NDF 复制清空 | 「模拟复制」按钮触发后,展示「复制前/复制后」对比卡,4 个 fixing 字段已清空 | 交易 / FX NDF |
| **8** | 规则详情 4 Tabs | 1 屏布局 + 4 Tabs:基本信息 / 命中历史(6 条) / 审计 Timeline(6 节点) / 影响范围(3 笔下游交易) | 规则 / DefaultBankAccountRule 详情 |
| **9** | 规则 KPI Banner | 4 张渐变 KPI 卡(总规则/启用/停用/本月命中);点击下钻展示分组/排行 | 规则 / DefaultBankAccountRule 列表 |

---

## 与 UX 评审报告的对应关系

每 Tab 底部小字注明「引用: docs/ux-review/{file}.md §X.X」,以下为完整索引:

### `docs/ux-review/basedata-pages-ux-improvements.md`(基础数据)

| Tab | 对应条目 | 章节 |
|-----|---------|------|
| Tab 1 | 2.1 [P0] 跨页面筛选器分组 + 默认折叠 | 适用于 BankAccountList |
| Tab 3 | 2.3 [P0] BankAccount 列表缺失「币对/IBAN/SWIFT/余额」关键列 | 余额/IBAN/SWIFT 列补全 |
| Tab 3 | 2.12 [P2] 列表首屏缺 Summary Row (汇总行) | 顶部 KPI Banner |

### `docs/ux-review/dealing-pages-ux-improvements.md`(交易 AC/AT/FX)

| Tab | 对应条目 | 章节 |
|-----|---------|------|
| Tab 4 | 2.1 [P0] AT 详情页补「审批」按钮(与 AC/FX 不一致) | 复审 + 实施细节 |
| Tab 5 | 2.5 [P1] FX 列表「管理主体/对手方」用 el-input-number + ID 过滤是反模式 | 改 BaseDataPicker |
| Tab 5 | 2.6 [P1] FX 列表无「日期分桶」快捷面板(今日/本周/本月/上月) | 快速日期按钮组 |
| Tab 6 | 2.3 [P0] RATE_FIX 后无「软刷新」反馈,体验割裂 | 改 toast 不打断 Tab |
| Tab 7 | 2.17 [P0]「复制模式」后表单脏数据清除不彻底 | FX NDF 复制清空 4 个 fixing 字段 |

### `docs/ux-review/rules-pages-ux-improvements.md`(规则)

| Tab | 对应条目 | 章节 |
|-----|---------|------|
| Tab 2 | 2.1 [P0] DefaultBankAccountRule 「管理主体必填」硬阻拦导致首屏空白 | 自动预填当前用户主体 |
| Tab 8 | 2.2 [P0] 缺少规则「详情页」 — 用户无法查看命中示例/影响范围/历史 | 4 Tabs 详情页 |
| Tab 9 | 2.3 [P0] 11 端点缓存策略用户不可见 — 监控/统计缺位 | KPI Banner 总览 |

---

## 技术栈

- **Vue 3.4.21**(CDN, `vue.global.prod.js`)
- **Element Plus 2.4.6**(CDN, `index.css` + `index.full.min.js`)
- **@element-plus/icons-vue 2.1.0**(CDN, `index.iife.min.js`,全图标自动注册)
- **零构建**:双击 HTML 即可运行,所有 JS/CSS 通过 CDN 加载

## 视觉规范

参考 `docs/原型/Open-TMS界面原型与设计规范.md`:
- 顶栏:深色 `#1f2d3d` + 白色 logo + 渐变 logo-mark
- 背景:`#f5f7fa`(主区域) / `#fff`(卡片) / 12px 圆角
- KPI 渐变:蓝(`#409EFF → #1890ff`)/ 绿(`#67c23a → #36CFC9`)/ 橙(`#E6A23C → #F56C6C`)/ 紫(`#8e44ad → #6c5ce7`)/ 青(`#36CFC9 → #1890ff`)/ 红(`#f56c6c → #c0392b`)
- 字体:`-apple-system, "PingFang SC", "Microsoft YaHei"` / `JetBrains Mono`(数字金额)
- 关键信息条:浅蓝渐变 `linear-gradient(90deg, #f8fbff 0%, #f0f5ff 100%)` + 蓝色描边

---

## 数据说明

- 全部为模拟数据,与生产无关(每 Tab 底部已注明「数据为模拟,与生产无关」)
- 银行/币种/主体/对手方 等基础数据复用同一组 mock,跨 Tab 一致
- FX 交易 8 条 / 银行账户 8 条 / 规则 8 条 / 命中历史 6 条 / 影响交易 3 条

## 已知限制

1. **RATE_FIX Toast 双弹**:`t6Trigger` 同时调用 Vue 响应式 mock 卡片(右下角可见)+ Element Plus `ElNotification`(右上角),两套 toast 共存,便于演示但不重叠
2. **远程搜索 Picker 模拟**:Tab 5 的 BaseDataPicker 用本地数据 + setTimeout(200ms)模拟远程,真实场景会调用后端
3. **拖拽排序未实现**:Tab 9 KPI 卡片 click 下钻为模拟,Murex 同款拖拽排序优先级需后续单独原型
4. **响应式**:1024px 视口下 Tab 8 详情页表格内允许横向滚动(已加 `overflow-x: auto`)
5. **未实现的功能**:规则详情页中的「编辑/复制/停用」按钮仅占位,无具体弹窗逻辑;FX 列表「RATE_FIX」点击跳 Tab 6

---

*UX P0 改进原型 - v1.0 - 2026-07-10*
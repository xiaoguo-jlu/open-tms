# 真实页面 UX 改进分析 — 索引

> **版本**: v1.0
> **日期**: 2026-07-11
> **审查范围**: 9 张生产环境真实页面截图(基于 `_recon.json` DOM 数据)
> **对标**: FIS Quantum / Murex MX.3 / SAP TRM / Bloomberg AIM / Kyriba

---

## 文件清单

| 文件 | 大小 | 说明 |
|------|------|------|
| `REAL-PAGES-UX-ANALYSIS.md` | ~12KB | 10 条 P0/P1/P2 改进建议(基于真实截图观察) |
| `real-pages-ux-prototype.html` | ~30KB | 单文件交互原型,10 个 Tab(现状 → 改进后对比) |
| `screenshots/real/*.png` | ~880KB | 9 张真实页面截图 |
| `screenshots/real/_recon.json` | ~10KB | DOM 采集数据(列标题、按钮、行数等) |

---

## 浏览器查看

**双击 `real-pages-ux-prototype.html` 即可在浏览器打开**:

```
F:\code\opencode\opentrm\docs\ux-review\real-pages-ux-prototype.html
```

或浏览器地址栏输入:
```
file:///F:/code/opencode/opentrm/docs/ux-review/real-pages-ux-prototype.html
```

---

## 原型结构

**10 个 Tab**(单击切换),每个 Tab 展示:
- 顶部 `痛点:... → 改法:...` alert
- 左卡片:现状(基于真实截图,标 ❌ 痛点)
- 右卡片:改进后(Element Plus 真实交互 demo)
- 底部:优先级 / 工作量 / 对标业界 / 影响页面

| # | Tab 标题 | 优先级 |
|---|---------|--------|
| 1 | DefaultBankAccountRule 首屏预填 | P0 |
| 2 | FX 列表顶部 v3.2 alert 清理 | P0 |
| 3 | 筛选器 6 项平铺折叠 | P0 |
| 4 | BankAccount 余额 / IBAN 列 | P0 |
| 5 | FX 列表 BaseDataPicker | P0 |
| 6 | 批量操作栏 | P0 |
| 7 | 列表空态 / 错误态 | P1 |
| 8 | 详情页一屏布局 | P1 |
| 9 | 全局面包屑 | P1 |
| 10 | 改进总览 | P2 |

**统计**: P0 = 6 条 · P1 = 3 条 · P2 = 1 条 · 总工作量 ~9 天

---

## 9 个真实页面覆盖

| # | 页面 | 路径 | 关键问题 |
|---|------|------|---------|
| 1 | BankAccountList | `/basedata/bank-account` | 6 筛选平铺、缺余额列、无批量 |
| 2 | CurrencyPairList | `/basedata/currency-pair` | 66 按钮(每行 2 个) |
| 3 | SubsidiaryList | `/basedata/subsidiary` | 同 BankAccount |
| 4 | DefaultBankAccountRuleList | `/basedata/default-bank-account-rule` | **首屏空表!** |
| 5 | AcDealList | `/dealing/ac-deal` | 顶部 v2.0 开发日志 |
| 6 | AtDealList | `/dealing/at-deal` | 缺审批按钮入口 |
| 7 | FxDealList | `/dealing/fx-deal` | **顶部 v3.2 开发日志**(P0) |
| 8 | AcDealDetail | `/dealing/ac-deal/detail` | 4 模式详情页 |
| 9 | FxDealDetail | `/dealing/fx-deal/detail` | 4 模式详情页 |

---

## 关联文档(已读,不重复)

- `basedata-pages-ux-improvements.md` — 基于代码的 17 条改进(架构补完)
- `dealing-pages-ux-improvements.md` — 交易页 20 条改进(含主代理 8 条 + 新增 12 条)
- `rules-pages-ux-improvements.md` — 规则页 15 条改进
- `screenshots/ux-review/` — 之前的旧截图(本报告未引用)

**本报告聚焦**: 基于真实截图观察到的"首页级"缺陷,跨页面共性,必须解决的 P0 阻塞。

---

## 技术栈

- Vue 3.4.21(prod,unpkg)
- Element Plus 2.5.6(cdnjs,UMD)
- 零依赖,双击 HTML 即可打开
- 自包含 CSS,无外部样式表
- 截图用相对路径(`../screenshots/real/*.png`)

---

## 硬约束确认

- [x] **未修改** 任何源码 / 路由 / 后端
- [x] **未删除** 任何已有文件
- [x] **仅新增** 3 个文件:分析文档 / HTML 原型 / README 索引
- [x] HTML 双击在浏览器打开,0 console error
- [x] 8-10 个 Tab 全部渲染(10 个)
- [x] 真实截图在原型里可显示(相对路径)
- [x] 改进后的 demo 用 Element Plus 真实组件(可点击)
- [x] 10 条改进含 P0/P1/P2 优先级 + 对标 + 工作量

---

*索引 v1.0 - 2026-07-11*
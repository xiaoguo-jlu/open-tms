# Bug 4: Vue 3.4 vue.global.prod.js 不挂 window 导致 createApp 失败

> 报告时间: 2026-07-11
> Skill: opentms-bug-fix v1.0
> 状态: ✅ 已修(模板改 CDN + window.Vue 兜底)

## 01 - 定界
- **类别**: 工具/基础设施(非前后端业务 bug)
- **证据**: Playwright 跑原型报 `Vue is not defined`;`unpkg.com/vue@3.4.21/dist/vue.global.prod.js` 文件里 `var Vue=function(){}` 不挂 window
- **结论**: Vue 3.4+ 设计改变,vue.global.prod.js 走 IIFE 闭包,无 UMD 全局

## 02 - 原因分析
- **直接**: `<script src="vue@3.4.21">` 加载后,Vue 变量在另一 script 作用域,不可见
- **位置**: `docs/ux-review/real-pages-ux-prototype.html`、`docs/prototypes/ux-p0-improvements.html` 等多个 HTML 模板

## 03 - 修复
```html
<script src="...vue@3.4.21/dist/vue.global.prod.js"></script>
<!-- Vue 3.4+ 走 IIFE 闭包不挂 window,这里手动挂载 -->
<script>if (typeof Vue !== "undefined") window.Vue = Vue;</script>
<script src="...element-plus@2.5.6/index.full.min.js"></script>
```
- 顺序: Vue → window.Vue 兜底 → Element Plus
- Element Plus 2.5.6 cdnjs 是 UMD 挂全局,内部 import Vue 时能拿到 window.Vue

## 04 - 根因
- **研发流程**: 没有"HTML 模板模板"项目,各原型独立写
- **需求设计**: N/A(非业务)
- **设计规范**: opentms-ux-design skill 没规定"HTML 原型模板基线"
- **开发实现**: 子代理写 HTML 时未考虑 Vue 3 升级带来的 UMD 变化
- **测试工具**: `webapp-testing` Playwright 跑后没第一时间看 console error 就调代码
- **Skill**: 没有"HTML 模板/原型"专门的 review skill

## 05 - 同类排查
- 所有 `docs/prototypes/*.html`、`docs/ux-review/*.html` 都用 unpkg/cdnjs 加载 Vue,都可能踩同一坑
- 建议统一改成 cdnjs 显式版本(已验证可挂全局)+ 兜底脚本

## 06 - 改进措施
- **P0**: 在 `opentms-ux-design` skill 新增 "HTML 模板"小节,提供 boilerplate(含 Vue + Element Plus + 兜底)
- **P1**: `webapp-testing` skill 加"Vue CDN 全局检测"check
- **P2**: 写一个 build 脚本 `scripts/build-html-prototype.py`,所有 HTML 模板统一从一份 template 生成

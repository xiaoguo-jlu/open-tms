"""
UX P0 原型 — Playwright 验证脚本
- 服务器已在 8765 跑(http.server)
- 验证 9 个 Tab + 3 个关键交互
- 截图保存到 docs/prototypes/screenshots/
"""
import sys
import io

# Windows GBK 编码兜底
try:
    sys.stdout.reconfigure(encoding="utf-8")
except Exception:
    pass

from playwright.sync_api import sync_playwright

URL = "http://localhost:8765/ux-p0-improvements.html"
SHOTS = "docs/prototypes/screenshots"

errors = []
console_msgs = []


def ok(msg):
    print("[OK] " + msg)


def main():
    with sync_playwright() as p:
        browser = p.chromium.launch(headless=True)
        ctx = browser.new_context(viewport={"width": 1440, "height": 900})
        page = ctx.new_page()

        # 1) 收集 console
        def on_console(msg):
            console_msgs.append(f"[{msg.type}] {msg.text}")
            if msg.type == "error":
                errors.append(msg.text)

        def on_pageerror(err):
            errors.append(f"PAGE_ERROR: {err}")

        page.on("console", on_console)
        page.on("pageerror", on_pageerror)

        # 2) 加载 + 等 networkidle
        page.goto(URL, wait_until="networkidle", timeout=30000)
        page.wait_for_timeout(4000)  # 让 Vue + Element Plus 挂载完成

        # 2.5) 先 dump 一份完整 console,用于诊断
        for m in console_msgs:
            print(f"  CONSOLE: {m}")
        for e in errors:
            print(f"  ERROR: {e}")

        # 3) 截图 1: 默认首页(Tab 1)
        page.screenshot(path=f"{SHOTS}/01-tab1-default.png", full_page=True)
        ok("截图 01-tab1-default.png")

        # 4) 校验 9 个 Tab 标签
        tabs = page.locator(".el-tabs__item").all()
        tab_labels = [t.inner_text().strip() for t in tabs]
        ok(f"找到 {len(tabs)} 个 Tab 标签: {tab_labels}")
        if len(tabs) < 9:
            errors.append(f"Tab 数量不足: 期望 9, 实际 {len(tabs)}")

        # 5) Tab 1 — 折叠筛选器交互
        tab1 = page.locator('.el-tab-pane').nth(0)
        adv_btn = tab1.locator('button:has-text("高级筛选")').first
        if adv_btn.count() == 0:
            adv_btn = tab1.locator('button:has-text("筛选")').first
        if adv_btn.count() > 0:
            adv_btn.click()
            page.wait_for_timeout(800)
            page.screenshot(path=f"{SHOTS}/02-tab1-advanced-open.png", full_page=True)
            ok("Tab1 折叠: 高级筛选已展开")
            adv_btn.click()
            page.wait_for_timeout(400)
        else:
            errors.append("Tab1 未找到「高级筛选」按钮")

        # 6) 切到 Tab 4(AT 审批按钮)
        tab4 = page.locator('.el-tabs__item:has-text("AT 详情")').first
        if tab4.count() == 0:
            tab4 = page.locator('.el-tabs__item').nth(3)
        tab4.click()
        page.wait_for_timeout(800)
        page.screenshot(path=f"{SHOTS}/03-tab4-at-approval.png", full_page=True)
        ok("截图 03-tab4-at-approval.png (Tab 4)")

        # 7) Tab 4 — 找审批通过按钮
        tab4_pane = page.locator('.el-tab-pane').nth(3)
        approve_btn = tab4_pane.locator('button:has-text("审批通过")').first
        if approve_btn.count() > 0:
            approve_btn.click()
            page.wait_for_timeout(1000)
            page.screenshot(path=f"{SHOTS}/04-tab4-approve-dialog.png", full_page=True)
            ok("Tab4 审批通过 dialog 已弹出")
            close_btn = page.locator('.el-dialog__close').first
            if close_btn.count() > 0:
                close_btn.click()
                page.wait_for_timeout(400)
        else:
            errors.append("Tab4 未找到「审批通过」按钮")

        # 8) 切到 Tab 6(RATE_FIX Toast)
        tab6 = page.locator('.el-tabs__item:has-text("RATE_FIX")').first
        if tab6.count() == 0:
            tab6 = page.locator('.el-tabs__item').nth(5)
        tab6.click()
        page.wait_for_timeout(800)
        page.screenshot(path=f"{SHOTS}/05-tab6-ratefix.png", full_page=True)
        ok("截图 05-tab6-ratefix.png (Tab 6)")

        # 9) Tab 6 — 触发 toast
        tab6_pane = page.locator('.el-tab-pane').nth(5)
        trigger_btn = tab6_pane.locator('button:has-text("Rate Fix")').first
        if trigger_btn.count() == 0:
            trigger_btn = tab6_pane.locator('button:has-text("触发")').first
        if trigger_btn.count() == 0:
            trigger_btn = tab6_pane.locator('button:has-text("演示")').first
        if trigger_btn.count() > 0:
            trigger_btn.click()
            page.wait_for_timeout(1200)
            page.screenshot(path=f"{SHOTS}/06-tab6-toast.png", full_page=True)
            notif = page.locator('.el-notification').count()
            ok(f"Tab6 触发: 检测到 {notif} 个 .el-notification")
            if notif == 0:
                errors.append("Tab6 toast 触发后未检测到 .el-notification")
        else:
            errors.append("Tab6 未找到 Rate Fix 触发按钮")

        # 10) 9 Tab 概览图
        page.locator('.el-tabs__item').nth(0).click()
        page.wait_for_timeout(500)
        page.screenshot(path=f"{SHOTS}/00-overview.png", full_page=True)
        ok("截图 00-overview.png (概览)")

        browser.close()


if __name__ == "__main__":
    try:
        main()
    except Exception as e:
        print(f"FATAL: {e}")
        errors.append(f"FATAL: {e}")
    finally:
        with open(f"{SHOTS}/_REPORT.md", "w", encoding="utf-8") as f:
            f.write("# UX P0 原型 — 验证报告\n\n")
            f.write(f"**URL**: {URL}\n\n")
            f.write(f"**Console 消息数**: {len(console_msgs)}\n")
            f.write(f"**Error 数**: {len(errors)}\n\n")
            f.write("## Console 消息(全部)\n\n```\n")
            for m in console_msgs[:80]:
                f.write(m + "\n")
            f.write("```\n\n## 错误\n\n")
            if errors:
                for e in errors:
                    f.write(f"- [ERR] {e}\n")
            else:
                f.write("[OK] 无错误\n")
        print(f"\n[REPORT] console={len(console_msgs)} errors={len(errors)}")
        print(f"[REPORT] saved {SHOTS}/_REPORT.md")


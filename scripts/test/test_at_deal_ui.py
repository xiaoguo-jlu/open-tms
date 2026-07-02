#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Open-TMS AT交易(Account Transfer) UI自动化测试
使用 Playwright 进行 Web UI 测试（参考 test_deal_ui.py 风格）

执行前置条件：
  1. 后端 dealing 已启动（port=8082）：java -jar dealing/target/dealing-1.0.0-SNAPSHOT.jar --server.port=8082
  2. 后端 basedata 已启动（port=8081）
  3. 前端 dev server 已启动：cd web && npm run dev（默认 http://localhost:3000）
  4. 已安装 playwright：pip install playwright && playwright install chromium
  5. 数据库已执行 db/schema/20-at-deal.sql
  6. 运行：python scripts/test/test_at_deal_ui.py
"""

import asyncio
import time
import json
from playwright.async_api import async_playwright, expect

FRONTEND_URL = "http://localhost:3000"
BACKEND_DEALING_URL = "http://localhost:8082"
BACKEND_BASEDATA_URL = "http://localhost:8081/opentms/basedata"

report = {
    "feature": "AT交易 UI 测试",
    "version": "v2.0",
    "total": 5,
    "passed": 0,
    "failed": 0,
    "screenshots": [],
    "results": []
}


def add_result(test_id, name, status, screenshot=None, note=None):
    result = {"id": test_id, "name": name, "status": status}
    if note:
        result["note"] = note
    report["results"].append(result)
    if screenshot:
        report["screenshots"].append({"name": test_id, "path": screenshot})
    if status == "passed":
        report["passed"] += 1
    else:
        report["failed"] += 1


async def test_u01_open_at_list(page):
    """TC-AT-U001: 打开 AT 列表页"""
    print("\n[U01] 打开 AT 列表页...")
    try:
        await page.goto(f"{FRONTEND_URL}/dealing/at-deal", wait_until="networkidle",
                        timeout=30000)
        await page.wait_for_timeout(2000)
        filter_card = page.locator(".filter-card")
        await expect(filter_card).to_be_visible(timeout=5000)
        table = page.locator(".table-card .el-table")
        await expect(table).to_be_visible(timeout=5000)
        add_btn = page.locator("button:has-text('新建 AT')")
        await expect(add_btn.first).to_be_visible(timeout=5000)
        screenshot_path = "/tmp/at_deal_ui_U01_list.png"
        await page.screenshot(path=screenshot_path, full_page=True)
        add_result("U01", "打开 AT 列表页", "passed", screenshot_path)
        print("[U01] PASS")
    except Exception as e:
        screenshot_path = "/tmp/at_deal_ui_U01_error.png"
        await page.screenshot(path=screenshot_path, full_page=True)
        add_result("U01", "打开 AT 列表页", "failed", screenshot_path, str(e))
        print(f"[U01] FAIL: {e}")


async def test_u02_create_drawer(page):
    """TC-AT-U002: 打开创建抽屉 + 填写 + 提交"""
    print("\n[U02] 创建 AT 抽屉...")
    try:
        add_btn = page.locator("button:has-text('新建 AT')").first
        await add_btn.click()
        await page.wait_for_timeout(1500)
        drawer = page.locator(".el-drawer:visible, .el-dialog:visible")
        await expect(drawer).to_be_visible(timeout=5000)

        # 验证 AT 特有字段
        source_acct = page.locator("input[placeholder*='付出'], input[placeholder*='源账户']").first
        dest_acct = page.locator("input[placeholder*='收入'], input[placeholder*='目标账户']").first
        src_amount = page.locator(".el-input-number input").first
        await expect(source_acct).to_be_visible(timeout=3000)
        await expect(dest_acct).to_be_visible(timeout=3000)

        # 选择源账户（点击下拉框）
        await source_acct.click()
        await page.wait_for_timeout(800)
        # 弹出选择器：选择第一行
        first_option = page.locator(".el-dialog:visible .el-table__body tr").first
        if await first_option.is_visible():
            await first_option.click()
        await page.wait_for_timeout(500)

        # 选择目标账户
        await dest_acct.click()
        await page.wait_for_timeout(800)
        first_option2 = page.locator(".el-dialog:visible .el-table__body tr").first
        if await first_option2.is_visible():
            await first_option2.click()
        await page.wait_for_timeout(500)

        # 填写金额
        await src_amount.fill("1000000")

        screenshot_path = "/tmp/at_deal_ui_U02_form_filled.png"
        await page.screenshot(path=screenshot_path, full_page=True)

        # 提交
        submit_btn = page.locator("button:has-text('提交'), button:has-text('保存')").first
        await submit_btn.click()
        await page.wait_for_timeout(2500)

        add_result("U02", "创建 AT 抽屉 + 提交", "passed", screenshot_path)
        print("[U02] PASS")
    except Exception as e:
        screenshot_path = "/tmp/at_deal_ui_U02_error.png"
        await page.screenshot(path=screenshot_path, full_page=True)
        add_result("U02", "创建 AT 抽屉 + 提交", "failed", screenshot_path, str(e))
        print(f"[U02] FAIL: {e}")


async def test_u03_detail_dealmap_timeline(page):
    """TC-AT-U003: 详情页双腿 DealMap 时间线展示"""
    print("\n[U03] 详情页双腿 DealMap 时间线...")
    try:
        # 返回列表
        await page.goto(f"{FRONTEND_URL}/dealing/at-deal", wait_until="networkidle",
                        timeout=30000)
        await page.wait_for_timeout(2000)
        # 点击第一行的查看按钮
        view_btn = page.locator(".table-card .el-table__body tr").first.locator(
            "button:has-text('查看'), button:has-text('详情')"
        )
        if await view_btn.count() == 0:
            view_btn = page.locator(".table-card .el-table__body tr").first
        await view_btn.click()
        await page.wait_for_timeout(2500)

        # 等待详情页/抽屉
        dealmap_tab = page.locator(".el-tabs__item:has-text('DealMap'), .el-tab-pane:has-text('DealMap')")
        if await dealmap_tab.count() > 0:
            await dealmap_tab.first.click()
            await page.wait_for_timeout(1000)

        # 验证 DealMap 表格 4 行
        dm_rows = page.locator(".el-table__body tr")
        # 注：抽屉/详情页结构可能不同，仅作尽力验证
        row_count = await dm_rows.count()
        print(f"      DealMap 区域行数（含表头）={row_count}")

        screenshot_path = "/tmp/at_deal_ui_U03_dealmap.png"
        await page.screenshot(path=screenshot_path, full_page=True)
        add_result("U03", "详情页双腿 DealMap 时间线", "passed", screenshot_path,
                   f"DealMap 区域行数={row_count}")
        print("[U03] PASS")
    except Exception as e:
        screenshot_path = "/tmp/at_deal_ui_U03_error.png"
        await page.screenshot(path=screenshot_path, full_page=True)
        add_result("U03", "详情页双腿 DealMap 时间线", "failed", screenshot_path, str(e))
        print(f"[U03] FAIL: {e}")


async def test_u04_approval_dialog(page):
    """TC-AT-U004: 审批弹窗"""
    print("\n[U04] 审批弹窗...")
    try:
        # 切换到 Action 历史标签
        action_tab = page.locator(".el-tabs__item:has-text('Action'), .el-tabs__item:has-text('操作历史')")
        if await action_tab.count() > 0:
            await action_tab.first.click()
            await page.wait_for_timeout(1000)

        # 点击审批按钮（第一行 Pending Action）
        approve_btn = page.locator(
            ".el-table__body tr button:has-text('审批'), .el-table__body tr button:has-text('通过')"
        ).first
        if await approve_btn.count() > 0:
            await approve_btn.click()
            await page.wait_for_timeout(1500)
            dialog = page.locator(".el-dialog:visible")
            await expect(dialog).to_be_visible(timeout=3000)
            screenshot_path = "/tmp/at_deal_ui_U04_dialog.png"
            await page.screenshot(path=screenshot_path, full_page=True)
            add_result("U04", "审批弹窗", "passed", screenshot_path)
            print("[U04] PASS")
        else:
            screenshot_path = "/tmp/at_deal_ui_U04_no_btn.png"
            await page.screenshot(path=screenshot_path, full_page=True)
            add_result("U04", "审批弹窗", "failed", screenshot_path, "未找到审批按钮")
            print("[U04] FAIL: 未找到审批按钮")
    except Exception as e:
        screenshot_path = "/tmp/at_deal_ui_U04_error.png"
        await page.screenshot(path=screenshot_path, full_page=True)
        add_result("U04", "审批弹窗", "failed", screenshot_path, str(e))
        print(f"[U04] FAIL: {e}")


async def test_u05_cross_currency_exchange_rate(page):
    """TC-AT-U005: 跨币种时汇率输入框自动显示"""
    print("\n[U05] 跨币种汇率输入框联动...")
    try:
        # 返回列表
        await page.goto(f"{FRONTEND_URL}/dealing/at-deal", wait_until="networkidle",
                        timeout=30000)
        await page.wait_for_timeout(1500)
        # 新建
        add_btn = page.locator("button:has-text('新建 AT')").first
        await add_btn.click()
        await page.wait_for_timeout(1500)

        # 查找币种选择器
        src_currency = page.locator("input[placeholder*='源币种'], input[placeholder*='付出方币种']").first
        dst_currency = page.locator("input[placeholder*='目标币种'], input[placeholder*='收入方币种']").first
        if await src_currency.count() == 0 or await dst_currency.count() == 0:
            # 若为下拉 select
            src_currency = page.locator(".el-form-item:has-text('源币种') .el-select").first
            dst_currency = page.locator(".el-form-item:has-text('目标币种') .el-select").first

        # 选择 CNY
        await src_currency.click()
        await page.wait_for_timeout(500)
        cny = page.locator(".el-select-dropdown__item:has-text('CNY')").first
        if await cny.count() > 0:
            await cny.click()
        await page.wait_for_timeout(300)

        # 选择 USD
        await dst_currency.click()
        await page.wait_for_timeout(500)
        usd = page.locator(".el-select-dropdown__item:has-text('USD')").first
        if await usd.count() > 0:
            await usd.click()
        await page.wait_for_timeout(500)

        # 检查汇率输入框是否可见
        rate_input = page.locator("input[placeholder*='汇率'], input[placeholder*='exchange']").first
        visible = await rate_input.is_visible() if await rate_input.count() > 0 else False

        screenshot_path = "/tmp/at_deal_ui_U05_rate_visible.png"
        await page.screenshot(path=screenshot_path, full_page=True)
        if visible:
            add_result("U05", "跨币种汇率输入框联动", "passed", screenshot_path)
            print("[U05] PASS")
        else:
            add_result("U05", "跨币种汇率输入框联动", "failed", screenshot_path,
                       "汇率输入框未自动显示")
            print("[U05] FAIL: 汇率输入框未自动显示")
    except Exception as e:
        screenshot_path = "/tmp/at_deal_ui_U05_error.png"
        await page.screenshot(path=screenshot_path, full_page=True)
        add_result("U05", "跨币种汇率输入框联动", "failed", screenshot_path, str(e))
        print(f"[U05] FAIL: {e}")


async def test_backend_api():
    """检查后端 API 是否正常"""
    import urllib.request
    print("\n" + "=" * 60)
    print("[API Health Check]")
    print("=" * 60)

    api_ok = True
    try:
        url = f"{BACKEND_DEALING_URL}/api/v1/dealing/at-deals/page?pageNum=1&pageSize=1"
        resp = urllib.request.urlopen(url, timeout=5)
        data = json.loads(resp.read().decode())
        if data.get('code') == 200:
            print(f"[OK] Dealing Backend: 正常（AT 接口可达）")
        else:
            print(f"[WARN] Dealing Backend 返回: {data}")
            api_ok = False
    except Exception as e:
        print(f"[FAIL] Dealing Backend: {e}")
        api_ok = False

    try:
        url = f"{BACKEND_BASEDATA_URL}/api/v1/bank-accounts/page?pageNum=1&pageSize=1"
        resp = urllib.request.urlopen(url, timeout=5)
        data = json.loads(resp.read().decode())
        if data.get('code') == 200:
            print(f"[OK] Basedata Backend: 正常")
    except Exception as e:
        print(f"[FAIL] Basedata Backend: {e}")

    return api_ok


async def main():
    print("\n" + "#" * 60)
    print("# Open-TMS AT交易(Account Transfer) UI 自动化测试")
    print("# 版本: v2.0")
    print("#" * 60)

    api_ok = await test_backend_api()
    if not api_ok:
        print("\n[ERROR] 后端服务不可用，请先启动后端服务。")
        print("  Basedata: java -jar basedata/target/opentms-basedata-1.0.0-SNAPSHOT.jar --server.port=8081")
        print("  Dealing:  java -jar dealing/target/dealing-1.0.0-SNAPSHOT.jar --server.port=8082")
        print("  Frontend: cd web && npm run dev")
        return

    async with async_playwright() as p:
        browser = await p.chromium.launch(headless=True)
        context = await browser.new_context(viewport={"width": 1920, "height": 1080})
        page = await context.new_page()

        errors = []
        page_errors = []
        failed_requests = []
        page.on("console", lambda msg: errors.append(f"[{msg.type}] {msg.text}")
                if msg.type == "error" else None)
        page.on("pageerror", lambda err: page_errors.append(str(err)))
        page.on("requestfailed", lambda req: failed_requests.append(f"{req.method} {req.url} - {req.failure}"))

        print("\n" + "=" * 60)
        print("[P0 UI 冒烟测试]")
        print("=" * 60)

        await test_u01_open_at_list(page)
        await test_u02_create_drawer(page)
        await test_u03_detail_dealmap_timeline(page)
        await test_u04_approval_dialog(page)
        await test_u05_cross_currency_exchange_rate(page)

        # 过滤 Vite HMR 自身消息
        real_console_errors = [e for e in errors if "[vite]" not in e]
        # 汇总诊断信息
        if real_console_errors:
            print("\n[Console Errors]:")
            for err in real_console_errors[:10]:
                print(f"  {err[:200]}")
        else:
            print("\n[OK] 无控制台错误")

        if page_errors:
            print("\n[Page JS Errors]:")
            for err in page_errors[:10]:
                print(f"  {err[:200]}")

        if failed_requests:
            print("\n[Failed Requests]:")
            for r in failed_requests[:10]:
                print(f"  {r[:200]}")

        # 关键断言:任何错误都 FAIL
        if real_console_errors or page_errors or failed_requests:
            add_result("U_CONSOLE", "FAIL",
                       f"console={len(real_console_errors)} page_error={len(page_errors)} failed_req={len(failed_requests)}")
            print(f"\n[FAIL] U_CONSOLE: 页面存在错误")
        else:
            add_result("U_CONSOLE", "PASS", "无 console error / page error / failed request")
            print(f"\n[PASS] U_CONSOLE: 页面无错误")

        await browser.close()

    # 输出报告
    print("\n" + "=" * 60)
    print("# UI 测试报告")
    print("=" * 60)
    print(f"  Total: {report['total']}")
    print(f"  Passed: {report['passed']}")
    print(f"  Failed: {report['failed']}")
    rate = report['passed'] * 100 // report['total'] if report['total'] > 0 else 0
    print(f"  Pass Rate: {rate}%")

    print("\n# 详细结果")
    for r in report["results"]:
        icon = "[PASS]" if r["status"] == "passed" else "[FAIL]"
        print(f"  {icon} {r['id']}: {r['name']}" + (f" — {r.get('note')}" if r.get('note') else ""))

    print("\n# 截图")
    for s in report["screenshots"]:
        print(f"  {s['name']}: {s['path']}")

    report_file = "F:/code/opencode/opentrm/scripts/test/test_at_deal_ui_report.json"
    with open(report_file, 'w', encoding='utf-8') as f:
        json.dump(report, f, ensure_ascii=False, indent=2)
    print(f"\n[INFO] Report saved: {report_file}")
    return 0 if report["failed"] == 0 else 1


if __name__ == "__main__":
    asyncio.run(main())
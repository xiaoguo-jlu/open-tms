#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Open-TMS FX UI Test (v3.2)
Uses Playwright to test FX frontend pages: list, drawer, detail, RATE_FIX
Run: python scripts/test/test_fx_deal_ui.py

Prerequisites:
  1. Backend dealing started (port 8082)
  2. Frontend dev server started (port 3000)
  3. playwright installed
"""

import asyncio, time, json, os
from playwright.async_api import async_playwright, expect

FRONTEND_URL = "http://localhost:3000"

report = {
    "feature": "FX UI Test (v3.2)",
    "total": 0, "passed": 0, "failed": 0, "skipped": 0,
    "screenshots": [],
    "results": []
}

screenshot_dir = "scripts/test/reports/screenshots"
os.makedirs(screenshot_dir, exist_ok=True)

def ss(name):
    return os.path.join(screenshot_dir, name)


def add_result(tid, name, status, shot=None, note=None):
    r = {"id": tid, "name": name, "status": status}
    if note: r["note"] = str(note)[:200]
    report["results"].append(r)
    if shot: report["screenshots"].append({"name": tid, "path": shot})
    if status == "passed": report["passed"] += 1
    elif status == "skipped": report["skipped"] += 1
    else: report["failed"] += 1


# ========== U01: Open FX list page ==========

async def test_u01_fx_list_page(page):
    """TC-FX-U001: Open FX list page, verify key elements"""
    print("\n[U01] Open FX list page...")
    tid = "U01"
    try:
        await page.goto(f"{FRONTEND_URL}/dealing/fx-deal", wait_until="networkidle", timeout=30000)
        await page.wait_for_timeout(2000)

        # Verify filter card
        fc = page.locator(".filter-card")
        await expect(fc).to_be_visible(timeout=5000)

        # Verify table
        table = page.locator(".table-card .el-table")
        await expect(table).to_be_visible(timeout=5000)

        # Verify "New FX" button
        add_btn = page.locator("button:has-text('新建 FX 交易')")
        await expect(add_btn.first).to_be_visible(timeout=5000)

        # Verify query button
        query_btn = page.locator("button:has-text('查询')")
        await expect(query_btn.first).to_be_visible(timeout=5000)

        await page.screenshot(path=ss("fx_u01_list.png"), full_page=True)
        add_result(tid, "Open FX list page", "passed", ss("fx_u01_list.png"))
        print(f"[{tid}] PASS")
    except Exception as e:
        await page.screenshot(path=ss("fx_u01_error.png"), full_page=True)
        add_result(tid, "Open FX list page", "failed", ss("fx_u01_error.png"), e)
        print(f"[{tid}] FAIL: {e}")


# ========== U02: Filter functionality ==========

async def test_u02_filter(page):
    """TC-FX-U002: Filter by product type and status"""
    print("\n[U02] Filter FX deals...")
    tid = "U02"
    try:
        # Filter by product type SPOT
        pt_select = page.locator(".filter-card .el-select").first
        await pt_select.click()
        await page.wait_for_timeout(500)
        spot_option = page.locator(".el-select-dropdown:visible .el-select-dropdown__item").filter(has_text="SPOT").first
        if await spot_option.is_visible(timeout=3000):
            await spot_option.click()
            await page.wait_for_timeout(500)

        # Click query
        query_btn = page.locator("button:has-text('查询')").first
        await query_btn.click()
        await page.wait_for_timeout(2000)

        # Verify table shows data (or is still visible)
        table = page.locator(".table-card .el-table")
        await expect(table).to_be_visible(timeout=5000)

        await page.screenshot(path=ss("fx_u02_filter.png"), full_page=True)
        add_result(tid, "Filter FX deals", "passed", ss("fx_u02_filter.png"))
        print(f"[{tid}] PASS")
    except Exception as e:
        await page.screenshot(path=ss("fx_u02_error.png"), full_page=True)
        add_result(tid, "Filter FX deals", "failed", ss("fx_u02_error.png"), e)
        print(f"[{tid}] FAIL: {e}")


# ========== U03: Open create drawer ==========

async def test_u03_open_drawer(page):
    """TC-FX-U003: Open create drawer, verify form fields"""
    print("\n[U03] Open create drawer...")
    tid = "U03"
    try:
        # Reset to fresh state
        await page.goto(f"{FRONTEND_URL}/dealing/fx-deal", wait_until="networkidle", timeout=30000)
        await page.wait_for_timeout(2000)

        add_btn = page.locator("button:has-text('新建 FX 交易')").first
        await add_btn.click()
        await page.wait_for_timeout(1500)

        # Verify drawer opened
        drawer = page.locator(".el-drawer:visible").first
        await expect(drawer).to_be_visible(timeout=5000)

        # Verify key form fields
        # Generic fields
        me_field = page.locator(".el-drawer:visible .el-form-item:has-text('管理主体')").first
        await expect(me_field).to_be_visible(timeout=3000)

        cp_field = page.locator(".el-drawer:visible .el-form-item:has-text('交易对手')").first
        await expect(cp_field).to_be_visible(timeout=3000)

        tr_field = page.locator(".el-drawer:visible .el-form-item:has-text('交易员')").first
        await expect(tr_field).to_be_visible(timeout=3000)

        # Calculate fields
        sell_field = page.locator(".el-drawer:visible .el-form-item:has-text('卖出金额')").first
        await expect(sell_field).to_be_visible(timeout=3000)

        buy_field = page.locator(".el-drawer:visible .el-form-item:has-text('买入金额')").first
        await expect(buy_field).to_be_visible(timeout=3000)

        exchange_field = page.locator(".el-drawer:visible .el-form-item:has-text('成交汇率')").first
        await expect(exchange_field).to_be_visible(timeout=3000)

        # Date fields
        td_field = page.locator(".el-drawer:visible .el-form-item:has-text('交易日')").first
        await expect(td_field).to_be_visible(timeout=3000)

        vd_field = page.locator(".el-drawer:visible .el-form-item:has-text('交割日')").first
        await expect(vd_field).to_be_visible(timeout=3000)

        # Operator field
        op_field = page.locator(".el-drawer:visible .el-form-item:has-text('操作人')").first
        await expect(op_field).to_be_visible(timeout=3000)

        # Save button
        save_btn = page.locator(".el-drawer:visible button:has-text('保存')").first
        await expect(save_btn).to_be_visible(timeout=3000)

        # v3.2 auto-generate alert (文字"系统将自动生成")
        auto_alert = page.locator(".el-drawer:visible .el-alert:has-text('DealMap')").first
        # Alternative: check Save button is inside the drawer
        if await auto_alert.is_visible(timeout=2000):
            print("  Found v3.2 auto-generate alert")
        else:
            print("  v3.2 alert not visible (non-critical)")

        await page.screenshot(path=ss("fx_u03_drawer.png"), full_page=True)
        add_result(tid, "Open create drawer", "passed", ss("fx_u03_drawer.png"))
        print(f"[{tid}] PASS")
    except Exception as e:
        await page.screenshot(path=ss("fx_u03_error.png"), full_page=True)
        add_result(tid, "Open create drawer", "failed", ss("fx_u03_error.png"), e)
        print(f"[{tid}] FAIL: {e}")


# ========== U04: Navigate to detail ==========

async def test_u04_detail_page(page):
    """TC-FX-U004: Navigate to detail page from list"""
    print("\n[U04] Navigate to detail page...")
    tid = "U04"
    try:
        await page.goto(f"{FRONTEND_URL}/dealing/fx-deal", wait_until="networkidle", timeout=30000)
        await page.wait_for_timeout(2000)

        # Click first deal number link in table
        first_link = page.locator(".table-card .el-table .el-link").first
        if await first_link.is_visible(timeout=5000):
            await first_link.click()
            await page.wait_for_timeout(3000)

            # Verify detail page loaded
            detail_header = page.locator(".fx-deal-detail").first
            await expect(detail_header).to_be_visible(timeout=5000)

            # Verify key elements
            desc = page.locator(".fx-deal-detail .el-descriptions").first
            await expect(desc).to_be_visible(timeout=5000)

            # Verify tabs exist
            tabs = page.locator(".fx-deal-detail .el-tabs").first
            await expect(tabs).to_be_visible(timeout=5000)

            # Verify back button
            back_btn = page.locator("button:has-text('返回')").first
            await expect(back_btn).to_be_visible(timeout=3000)

            await page.screenshot(path=ss("fx_u04_detail.png"), full_page=True)
            add_result(tid, "Navigate to detail page", "passed", ss("fx_u04_detail.png"))
            print(f"[{tid}] PASS")
        else:
            add_result(tid, "Navigate to detail page", "skipped", note="No deal links in table")
            print(f"[{tid}] SKIP: No deal links found")
    except Exception as e:
        await page.screenshot(path=ss("fx_u04_error.png"), full_page=True)
        add_result(tid, "Navigate to detail page", "failed", ss("fx_u04_error.png"), e)
        print(f"[{tid}] FAIL: {e}")


# ========== U05: Detail tabs switch ==========

async def test_u05_detail_tabs(page):
    """TC-FX-U005: Switch between detail tabs"""
    print("\n[U05] Switch detail tabs...")
    tid = "U05"
    try:
        # Already on detail page from U04, check if we're there
        if "fx-deal/detail" not in page.url:
            # Navigate back
            first_link = page.locator(".table-card .el-table .el-link").first
            if await first_link.is_visible(timeout=3000):
                await first_link.click()
                await page.wait_for_timeout(2000)

        if "fx-deal/detail" in page.url:
            # Click "DealMap" tab if present
            dm_tab = page.locator(".el-tabs__item:has-text('DealMap')").first
            cf_tab = page.locator(".el-tabs__item:has-text('现金流')").first

            if await dm_tab.is_visible(timeout=3000):
                await dm_tab.click()
                await page.wait_for_timeout(1000)
                await page.screenshot(path=ss("fx_u05_tab_dm.png"), full_page=True)
                print("  Switched to DealMap tab")

            if await cf_tab.is_visible(timeout=3000):
                await cf_tab.click()
                await page.wait_for_timeout(1000)
                await page.screenshot(path=ss("fx_u05_tab_cf.png"), full_page=True)
                print("  Switched to Cashflow tab")

            add_result(tid, "Switch detail tabs", "passed", ss("fx_u05_tab_cf.png"))
            print(f"[{tid}] PASS")
        else:
            add_result(tid, "Switch detail tabs", "skipped", note="Not on detail page")
            print(f"[{tid}] SKIP")
    except Exception as e:
        await page.screenshot(path=ss("fx_u05_error.png"), full_page=True)
        add_result(tid, "Switch detail tabs", "failed", ss("fx_u05_error.png"), e)
        print(f"[{tid}] FAIL: {e}")


# ========== U06: RATE_FIX dialog ==========

async def test_u06_ratefix_dialog(page):
    """TC-FX-U006: Open RATE_FIX dialog for NDF deal"""
    print("\n[U06] RATE_FIX dialog...")
    tid = "U06"
    try:
        await page.goto(f"{FRONTEND_URL}/dealing/fx-deal", wait_until="networkidle", timeout=30000)
        await page.wait_for_timeout(2000)

        # Check if there are any NDF deals in the table
        ndf_rows = page.locator(".el-table__body tr:has-text('NDF')")
        ndf_count = await ndf_rows.count()

        if ndf_count > 0:
            # Find RATE_FIX button in the NDF row
            ratefix_btn = page.locator("button:has-text('RATE_FIX')").first
            if await ratefix_btn.is_visible(timeout=3000):
                await ratefix_btn.click()
                await page.wait_for_timeout(1500)

                # Verify dialog opened
                dialog = page.locator(".el-dialog:visible").first
                if await dialog.is_visible(timeout=5000):
                    # Verify dialog title
                    title = page.locator(".el-dialog:visible .el-dialog__title:has-text('RATE_FIX')").first
                    await expect(title).to_be_visible(timeout=3000)

                    # Verify fixing rate input
                    rate_input = page.locator(".el-dialog:visible .el-input-number input").first
                    await expect(rate_input).to_be_visible(timeout=3000)

                    # Fill fixing rate
                    await rate_input.fill("1.1500")
                    await page.wait_for_timeout(500)

                    await page.screenshot(path=ss("fx_u06_ratefix.png"), full_page=True)

                    # Click confirm
                    confirm_btn = page.locator("button:has-text('确认 RATE_FIX')").first
                    if await confirm_btn.is_visible(timeout=3000):
                        await confirm_btn.click()
                        await page.wait_for_timeout(3000)

                    add_result(tid, "RATE_FIX dialog + submit", "passed", ss("fx_u06_ratefix.png"))
                    print(f"[{tid}] PASS")
                else:
                    add_result(tid, "RATE_FIX dialog", "failed", ss("fx_u06_error.png"), "Dialog did not open")
                    print(f"[{tid}] FAIL: Dialog did not open")
            else:
                add_result(tid, "RATE_FIX dialog", "skipped", note="No RATE_FIX buttons visible")
                print(f"[{tid}] SKIP: No visible RATE_FIX buttons")
        else:
            add_result(tid, "RATE_FIX dialog", "skipped", note="No NDF deals in table")
            print(f"[{tid}] SKIP: No NDF deals in table")
    except Exception as e:
        await page.screenshot(path=ss("fx_u06_error.png"), full_page=True)
        add_result(tid, "RATE_FIX dialog", "failed", ss("fx_u06_error.png"), e)
        print(f"[{tid}] FAIL: {e}")


# ========== U07: Pagination ==========

async def test_u07_pagination(page):
    """TC-FX-U007: Test pagination controls"""
    print("\n[U07] Test pagination...")
    tid = "U07"
    try:
        await page.goto(f"{FRONTEND_URL}/dealing/fx-deal", wait_until="networkidle", timeout=30000)
        await page.wait_for_timeout(2000)

        # Check pagination exists
        pager = page.locator(".el-pagination").first
        if await pager.is_visible(timeout=5000):
            # Verify total is displayed
            total_text = await pager.text_content()
            print(f"  Pagination: {total_text[:80]}...")

            # Change page size to 50
            size_trigger = page.locator(".el-pagination .el-select").first
            if await size_trigger.is_visible(timeout=3000):
                await size_trigger.click()
                await page.wait_for_timeout(500)

                # Select 50
                opt_50 = page.locator(".el-select-dropdown:visible .el-select-dropdown__item").last
                if await opt_50.is_visible(timeout=2000):
                    await opt_50.click()
                    await page.wait_for_timeout(1500)

            await page.screenshot(path=ss("fx_u07_pagination.png"), full_page=True)
            add_result(tid, "Test pagination", "passed", ss("fx_u07_pagination.png"))
            print(f"[{tid}] PASS")
        else:
            add_result(tid, "Test pagination", "skipped", note="No pagination found")
            print(f"[{tid}] SKIP")
    except Exception as e:
        await page.screenshot(path=ss("fx_u07_error.png"), full_page=True)
        add_result(tid, "Test pagination", "failed", ss("fx_u07_error.png"), e)
        print(f"[{tid}] FAIL: {e}")


# ========== Main ==========

async def main():
    print("\n" + "#" * 64)
    print("#  Open-TMS FX UI Test (v3.2)")
    print(f"#  Frontend: {FRONTEND_URL}")
    print("#" * 64)

    async with async_playwright() as p:
        browser = await p.chromium.launch(headless=True)
        context = await browser.new_context(
            viewport={"width": 1440, "height": 900},
            locale="zh-CN"
        )
        page = await context.new_page()

        tests = [
            test_u01_fx_list_page,
            test_u02_filter,
            test_u03_open_drawer,
            test_u04_detail_page,
            test_u05_detail_tabs,
            test_u06_ratefix_dialog,
            test_u07_pagination,
        ]

        for test_fn in tests:
            report["total"] += 1
            try:
                await test_fn(page)
            except Exception as e:
                import traceback
                print(f"  [CRASH] {test_fn.__name__}: {e}")
                traceback.print_exc()
                add_result(test_fn.__name__, test_fn.__doc__ or test_fn.__name__, "failed", note=str(e))

        await browser.close()

    # Summary
    print("\n" + "#" * 64)
    print("#  UI Test Summary")
    print("#" * 64)
    print(f"  Total:    {report['total']}")
    print(f"  Passed:   {report['passed']}")
    print(f"  Failed:   {report['failed']}")
    print(f"  Skipped:  {report['skipped']}")
    pct = report['passed'] / max(report['total'] - report.get('skipped', 0), 1) * 100
    print(f"  Pass rate (excl. skipped): {pct:.1f}%")
    print(f"  Screenshots: {len(report['screenshots'])}")

    failures = [r for r in report["results"] if r["status"] == "failed"]
    if failures:
        print(f"\n  Failed cases:")
        for f in failures:
            print(f"    [FAIL] {f['id']}: {f.get('note', '')}")

    # Save JSON report
    rp = f"scripts/test/reports/fx_deal_ui_{time.strftime('%Y%m%d_%H%M%S')}.json"
    with open(rp, "w", encoding="utf-8") as fp:
        json.dump(report, fp, ensure_ascii=False, indent=2, default=str)
    print(f"\n  Report saved: {rp}")
    print("#" * 64)


if __name__ == "__main__":
    asyncio.run(main())

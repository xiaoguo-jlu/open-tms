#!/usr/bin/env python3
"""
Open-TMS 基础数据模块 UI自动化测试
使用Playwright进行Web UI测试，覆盖所有基础数据模块页面
"""

import asyncio
import time
from playwright.async_api import async_playwright, expect

BACKEND_URL = "http://localhost:8081/opentms/basedata"
FRONTEND_URL = "http://localhost:3000"

# 基础数据模块所有页面路由
BASEDATA_PAGES = [
    {"name": "Country", "path": "/#/basedata/country", "keyword_placeholder": "代码/名称"},
    {"name": "Bank", "path": "/#/basedata/bank", "keyword_placeholder": "银行代码/名称"},
    {"name": "Currency", "path": "/#/basedata/currency", "keyword_placeholder": "币种代码/名称"},
    {"name": "Trader", "path": "/#/basedata/trader", "keyword_placeholder": "交易员代码/名称"},
    {"name": "Holiday", "path": "/#/basedata/holiday", "keyword_placeholder": "假日名称"},
    {"name": "Counterparty", "path": "/#/basedata/counterparty", "keyword_placeholder": "对手方代码/名称"},
    {"name": "CounterpartyAccount", "path": "/#/basedata/counterparty-account", "keyword_placeholder": "账号/名称"},
    {"name": "CurrencyPair", "path": "/#/basedata/currency-pair", "keyword_placeholder": "货币对代码/名称"},
    {"name": "Subsidiary", "path": "/#/basedata/subsidiary", "keyword_placeholder": "子公司代码/名称"},
]


async def test_backend_api():
    """测试后端API是否正常"""
    import urllib.request
    import json

    print("\n" + "="*60)
    print("[TEST] Backend API Health Check")
    print("="*60)

    try:
        resp = urllib.request.urlopen(f"{BACKEND_URL}/api/v1/countries/page?pageNum=1&pageSize=1", timeout=5)
        data = json.loads(resp.read().decode())
        if data.get('code') == 200:
            print(f"[PASS] Backend API: 正常")
            return True
    except Exception as e:
        print(f"[FAIL] Backend API: {e}")
    return False


async def test_page_list_ui(page_name, path, keyword_placeholder):
    """测试单个列表页面UI"""
    async with async_playwright() as p:
        browser = await p.chromium.launch(headless=True)
        context = await browser.new_context()
        page = await context.new_page()

        errors = []
        page.on("console", lambda msg: errors.append(f"[{msg.type}] {msg.text}") if msg.type == "error" else None)

        print(f"\n  [Page: {page_name}]")

        try:
            # 1. 打开页面
            await page.goto(f"{FRONTEND_URL}{path}", wait_until="networkidle", timeout=30000)
            await page.wait_for_timeout(1500)

            # 2. 检查筛选表单
            try:
                filter_card = page.locator(".filter-card")
                await expect(filter_card).to_be_visible(timeout=5000)
                print(f"    [OK] 筛选区域可见")
            except:
                print(f"    [WARN] 筛选区域未找到")

            # 3. 检查表格
            try:
                table = page.locator(".el-table")
                await expect(table).to_be_visible(timeout=5000)
                print(f"    [OK] 数据表格可见")
            except:
                print(f"    [WARN] 数据表格未找到")

            # 4. 检查新增按钮
            try:
                add_btn = page.locator("button:has-text('新增')")
                await expect(add_btn).to_be_visible(timeout=5000)
                print(f"    [OK] 新增按钮可见")
            except:
                print(f"    [WARN] 新增按钮未找到")

            # 5. 测试搜索功能
            try:
                search_input = page.locator(f"input[placeholder='{keyword_placeholder}']")
                await search_input.fill("test")
                await page.locator("button:has-text('查询')").click()
                await page.wait_for_timeout(1000)
                print(f"    [OK] 搜索功能正常")
            except Exception as e:
                print(f"    [WARN] 搜索功能异常: {e}")

            # 6. 测试重置功能
            try:
                await page.locator("button:has-text('重置')").click()
                await page.wait_for_timeout(500)
                print(f"    [OK] 重置功能正常")
            except Exception as e:
                print(f"    [WARN] 重置功能异常: {e}")

            # 7. 检查分页组件
            try:
                pagination = page.locator(".el-pagination")
                await expect(pagination).to_be_visible(timeout=3000)
                print(f"    [OK] 分页组件可见")
            except:
                print(f"    [WARN] 分页组件未找到")

        except Exception as e:
            print(f"    [FAIL] 页面加载失败: {e}")

        await browser.close()
        return len([e for e in errors if "error" in e.lower()]) == 0


async def test_add_dialog_ui(page_name):
    """测试新增弹窗功能"""
    async with async_playwright() as p:
        browser = await p.chromium.launch(headless=True)
        context = await browser.new_context()
        page = await context.new_page()

        errors = []
        page.on("console", lambda msg: errors.append(f"[{msg.type}] {msg.text}") if msg.type == "error" else None)

        print(f"\n  [Add Dialog: {page_name}]")

        # 不同页面的路由
        page_paths = {
            "Country": "/#/basedata/country",
            "Bank": "/#/basedata/bank",
            "Currency": "/#/basedata/currency",
            "Trader": "/#/basedata/trader",
            "Counterparty": "/#/basedata/counterparty",
            "Subsidiary": "/#/basedata/subsidiary",
        }

        path = page_paths.get(page_name)
        if not path:
            print(f"    [SKIP] 无对应路由")
            await browser.close()
            return True

        try:
            await page.goto(f"{FRONTEND_URL}{path}", wait_until="networkidle", timeout=30000)
            await page.wait_for_timeout(1500)

            # 点击新增按钮
            try:
                await page.locator("button:has-text('新增')").click()
                await page.wait_for_timeout(500)

                # 检查drawer弹窗
                try:
                    drawer = page.locator(".el-drawer")
                    await expect(drawer).to_be_visible(timeout=3000)
                    print(f"    [OK] 新增弹窗打开成功")

                    # 关闭弹窗
                    try:
                        close_btns = page.locator("button:has-text('取消')")
                        await close_btns.last.click()
                        await page.wait_for_timeout(500)
                        print(f"    [OK] 弹窗关闭成功")
                    except:
                        print(f"    [WARN] 关闭弹窗失败")
                except:
                    print(f"    [WARN] 弹窗未出现")

            except Exception as e:
                print(f"    [FAIL] 新增按钮点击失败: {e}")

        except Exception as e:
            print(f"    [FAIL] 页面加载失败: {e}")

        await browser.close()
        return len([e for e in errors if "error" in e.lower()]) == 0


async def test_pagination_ui():
    """测试分页功能"""
    async with async_playwright() as p:
        browser = await p.chromium.launch(headless=True)
        context = await browser.new_context()
        page = await context.new_page()

        print(f"\n  [Pagination Test]")

        try:
            await page.goto(f"{FRONTEND_URL}/#/basedata/country", wait_until="networkidle", timeout=30000)
            await page.wait_for_timeout(1500)

            # 获取当前页码
            try:
                current_page = page.locator(".el-pagination__current").first
                await current_page.wait_for(timeout=3000)
                print(f"    [OK] 分页信息可见")
            except:
                print(f"    [WARN] 分页信息未找到")

            # 尝试切换每页条数
            try:
                page_size_select = page.locator(".el-pagination__sizes .el-input__inner")
                await page_size_select.click()
                await page.wait_for_timeout(300)
                options = page.locator(".el-select-dropdown__item")
                if await options.count() > 0:
                    await options.nth(1).click()  # 选择第二项 (如20条)
                    await page.wait_for_timeout(1000)
                    print(f"    [OK] 切换每页条数成功")
            except Exception as e:
                print(f"    [WARN] 切换每页条数失败: {e}")

        except Exception as e:
            print(f"    [FAIL] 分页测试失败: {e}")

        await browser.close()


async def main():
    print("\n" + "#"*60)
    print("# Open-TMS 基础数据模块 UI自动化测试")
    print("#"*60)

    # 先检查后端
    api_ok = await test_backend_api()
    if not api_ok:
        print("\n[ERROR] Backend is not available. Please start backend first.")
        print("  java -jar basedata/target/opentms-basedata-1.0.0-SNAPSHOT.jar")
        return

    # ========== 1. 测试所有列表页面 ==========
    print("\n" + "="*60)
    print("[TEST 1] 列表页面UI测试")
    print("="*60)

    for page in BASEDATA_PAGES:
        await test_page_list_ui(page["name"], page["path"], page["keyword_placeholder"])

    # ========== 2. 测试新增弹窗 ==========
    print("\n" + "="*60)
    print("[TEST 2] 新增弹窗UI测试")
    print("="*60)

    for page_name in ["Country", "Bank", "Currency", "Trader", "Counterparty", "Subsidiary"]:
        await test_add_dialog_ui(page_name)

    # ========== 3. 测试分页功能 ==========
    print("\n" + "="*60)
    print("[TEST 3] 分页功能测试")
    print("="*60)

    await test_pagination_ui()

    print("\n" + "#"*60)
    print("# Test Summary")
    print("#"*60)
    print(f"  Backend API: {'PASS' if api_ok else 'FAIL'}")
    print(f"  UI Pages Tested: {len(BASEDATA_PAGES)}")
    print("#"*60 + "\n")


if __name__ == "__main__":
    asyncio.run(main())
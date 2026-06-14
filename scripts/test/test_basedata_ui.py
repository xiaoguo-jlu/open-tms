#!/usr/bin/env python3
"""
Open-TMS 基础数据模块 UI自动化测试
使用Playwright进行Web UI测试，覆盖所有基础数据模块页面
增强功能：功能验证而非仅检查元素存在
"""

import asyncio
import time
import urllib.request
import json
from playwright.async_api import async_playwright, expect

BACKEND_URL = "http://localhost:8081/opentms/basedata"
FRONTEND_URL = "http://localhost:3000"

# 基础数据模块所有页面路由（使用history模式）
BASEDATA_PAGES = [
    {"name": "Country", "path": "/basedata/country", "api": "countries"},
    {"name": "Currency", "path": "/basedata/currency", "api": "currencies"},
    {"name": "Trader", "path": "/basedata/trader", "api": "traders"},
    {"name": "Holiday", "path": "/basedata/holiday", "api": "holidays"},
    {"name": "Counterparty", "path": "/basedata/counterparty", "api": "counterparties"},
    {"name": "CounterpartyAccount", "path": "/basedata/counterparty-account", "api": "counterparty-accounts"},
    {"name": "CurrencyPair", "path": "/basedata/currency-pair", "api": "currency-pairs"},
    {"name": "Subsidiary", "path": "/basedata/subsidiary", "api": "subsidiaries"},
]

# 测试结果统计
TEST_RESULTS = {
    "passed": 0,
    "failed": 0,
    "warnings": 0,
}


async def check_api_health(api_path):
    """检查API端点是否可达"""
    try:
        url = f"{BACKEND_URL}/api/v1/{api_path}/page?pageNum=1&pageSize=1"
        req = urllib.request.Request(url, headers={"User-Agent": "Playwright-Test"})
        resp = urllib.request.urlopen(req, timeout=5)
        data = json.loads(resp.read().decode())
        # 支持两种响应格式: Result包装器 或 直接返回Page
        is_success = data.get('code') == 200 or (isinstance(data, dict) and 'records' in data)
        return is_success, data
    except Exception as e:
        return False, str(e)


async def test_backend_api():
    """测试后端API是否正常"""
    print("\n" + "="*60)
    print("[TEST] Backend API Health Check")
    print("="*60)

    all_api_ok = True
    for page in BASEDATA_PAGES:
        api_ok, result = await check_api_health(page["api"])
        status = "[PASS]" if api_ok else "[FAIL]"
        print(f"  {status} {page['name']} API ({page['api']}): {'正常' if api_ok else '不可达'}")
        if not api_ok:
            all_api_ok = False
            TEST_RESULTS["failed"] += 1
        else:
            TEST_RESULTS["passed"] += 1

    if all_api_ok:
        print(f"\n[PASS] Backend API: 全部正常")
    else:
        print(f"\n[FAIL] Backend API: 部分不可用")
    return all_api_ok


async def print_error_toast(page, operation):
    """检查并打印错误提示"""
    error_toast = page.locator(".el-message--error")
    if await error_toast.count() > 0:
        try:
            error_text = await error_toast.first.text_content(timeout=2000)
            print(f"    [FAIL] {operation} - 错误提示: {error_text}")
            return True
        except:
            print(f"    [FAIL] {operation} - 检测到错误提示")
            return True
    return False


def print_console_errors(errors):
    """打印控制台错误"""
    error_list = [e for e in errors if "error" in e.lower()]
    if error_list:
        print(f"    [ERROR] 控制台错误:")
        for err in error_list[:5]:  # 最多显示5条
            print(f"      - {err[:200]}")  # 截断过长错误


async def test_page_list_ui(page_name, path, api_name):
    """测试单个列表页面UI - 增强功能验证"""
    async with async_playwright() as p:
        browser = await p.chromium.launch(headless=True)
        context = await browser.new_context()
        page = await context.new_page()

        console_errors = []
        network_errors = []

        # 捕获控制台错误
        page.on("console", lambda msg: console_errors.append(f"[{msg.type}] {msg.text}") if msg.type == "error" else None)
        # 捕获网络请求失败
        page.on("response", lambda resp: network_errors.append(f"{resp.status} {resp.url}") if resp.status >= 400 else None)

        print(f"\n  [Page: {page_name}]")
        test_passed = True

        try:
            # 1. API前置检查
            api_ok, result = await check_api_health(api_name)
            if not api_ok:
                print(f"    [FAIL] API不可访问: /api/v1/{api_name}/page")
                print(f"          错误: {result}")
                TEST_RESULTS["failed"] += 1
                await browser.close()
                return False

            # 2. 打开页面
            await page.goto(f"{FRONTEND_URL}{path}", wait_until="networkidle", timeout=30000)
            await page.wait_for_timeout(1000)

            # 3. 检查筛选表单
            try:
                filter_card = page.locator(".filter-card")
                await expect(filter_card).to_be_visible(timeout=5000)
                print(f"    [OK] 筛选区域可见")
            except:
                print(f"    [WARN] 筛选区域未找到")
                TEST_RESULTS["warnings"] += 1

            # 4. 检查表格
            try:
                table = page.locator(".el-table")
                await expect(table).to_be_visible(timeout=5000)
                print(f"    [OK] 数据表格可见")
            except:
                print(f"    [WARN] 数据表格未找到")
                TEST_RESULTS["warnings"] += 1

            # 5. 检查新增按钮
            try:
                add_btn = page.locator("button:has-text('新增')")
                await expect(add_btn).to_be_visible(timeout=5000)
                print(f"    [OK] 新增按钮可见")
            except:
                print(f"    [WARN] 新增按钮未找到")
                TEST_RESULTS["warnings"] += 1

            # 6. 测试查询功能 - 核心功能验证
            try:
                search_input = page.locator(".filter-card input").first
                await search_input.fill("test")
                await page.locator("button:has-text('查询')").click()
                await page.wait_for_timeout(2000)  # 等待API响应

                # 检查是否有错误提示
                if await print_error_toast(page, "查询操作"):
                    test_passed = False
                    TEST_RESULTS["failed"] += 1
                else:
                    print(f"    [OK] 查询功能正常")
            except Exception as e:
                print(f"    [FAIL] 查询功能异常: {e}")
                test_passed = False
                TEST_RESULTS["failed"] += 1

            # 7. 验证表格数据加载
            try:
                table = page.locator(".el-table")
                await table.wait_for(timeout=5000)

                # 检查是否有数据行
                rows = table.locator(".el-table__row")
                row_count = await rows.count()

                # 检查是否有"无数据"提示
                empty_text = page.locator(".el-table__empty-text")
                if await empty_text.is_visible(timeout=2000):
                    print(f"    [OK] 查询成功，表格显示无数据（符合预期）")
                elif row_count > 0:
                    print(f"    [OK] 查询成功，数据已加载 ({row_count} 行)")
                else:
                    print(f"    [WARN] 表格状态不明确")
                    TEST_RESULTS["warnings"] += 1
            except Exception as e:
                print(f"    [FAIL] 表格数据验证失败: {e}")
                test_passed = False
                TEST_RESULTS["failed"] += 1

            # 8. 测试重置功能
            try:
                await page.locator("button:has-text('重置')").click()
                await page.wait_for_timeout(1000)

                if await print_error_toast(page, "重置操作"):
                    test_passed = False
                    TEST_RESULTS["failed"] += 1
                else:
                    print(f"    [OK] 重置功能正常")
            except Exception as e:
                print(f"    [FAIL] 重置功能异常: {e}")
                test_passed = False
                TEST_RESULTS["failed"] += 1

            # 9. 检查分页组件
            try:
                pagination = page.locator(".el-pagination")
                await expect(pagination).to_be_visible(timeout=3000)
                print(f"    [OK] 分页组件可见")
            except:
                print(f"    [WARN] 分页组件未找到")
                TEST_RESULTS["warnings"] += 1

        except Exception as e:
            print(f"    [FAIL] 页面加载失败: {e}")
            test_passed = False
            TEST_RESULTS["failed"] += 1

        # 打印控制台错误
        print_console_errors(console_errors)

        # 打印网络错误
        if network_errors:
            print(f"    [ERROR] 网络请求失败:")
            for err in network_errors[:5]:
                print(f"      - {err[:100]}")

        if test_passed:
            TEST_RESULTS["passed"] += 1
        await browser.close()
        return test_passed


async def test_add_dialog_ui(page_name, path, api_name):
    """测试新增弹窗功能 - 增强功能验证"""
    async with async_playwright() as p:
        browser = await p.chromium.launch(headless=True)
        context = await browser.new_context()
        page = await context.new_page()

        console_errors = []
        page.on("console", lambda msg: console_errors.append(f"[{msg.type}] {msg.text}") if msg.type == "error" else None)

        print(f"\n  [Add Dialog: {page_name}]")
        test_passed = True

        # 不同页面的路由（history模式）
        page_paths = {
            "Country": "/basedata/country",
            "Currency": "/basedata/currency",
            "Trader": "/basedata/trader",
            "Counterparty": "/basedata/counterparty",
            "Subsidiary": "/basedata/subsidiary",
        }

        path = page_paths.get(page_name)
        if not path:
            print(f"    [SKIP] 无对应路由")
            await browser.close()
            return True

        try:
            # API前置检查
            api_ok, result = await check_api_health(api_name)
            if not api_ok:
                print(f"    [FAIL] API不可访问: /api/v1/{api_name}/page")
                TEST_RESULTS["failed"] += 1
                await browser.close()
                return False

            await page.goto(f"{FRONTEND_URL}{path}", wait_until="networkidle", timeout=30000)
            await page.wait_for_timeout(1000)

            # 点击新增按钮
            try:
                await page.locator("button:has-text('新增')").click()
                await page.wait_for_timeout(1000)

                # 检查drawer弹窗
                try:
                    drawer = page.locator(".el-drawer")
                    await expect(drawer).to_be_visible(timeout=3000)
                    print(f"    [OK] 新增弹窗打开成功")

                    # 关闭弹窗
                    try:
                        close_btns = page.locator("button:has-text('取消')")
                        await close_btns.last.click()
                        await page.wait_for_timeout(1000)

                        # 检查关闭时是否有错误
                        if await print_error_toast(page, "关闭弹窗"):
                            test_passed = False
                            TEST_RESULTS["failed"] += 1
                        else:
                            print(f"    [OK] 弹窗关闭成功")
                    except Exception as e:
                        print(f"    [FAIL] 关闭弹窗失败: {e}")
                        test_passed = False
                        TEST_RESULTS["failed"] += 1
                except Exception as e:
                    print(f"    [FAIL] 弹窗未出现: {e}")
                    test_passed = False
                    TEST_RESULTS["failed"] += 1

            except Exception as e:
                print(f"    [FAIL] 新增按钮点击失败: {e}")
                test_passed = False
                TEST_RESULTS["failed"] += 1

        except Exception as e:
            print(f"    [FAIL] 页面加载失败: {e}")
            test_passed = False
            TEST_RESULTS["failed"] += 1

        print_console_errors(console_errors)

        if test_passed:
            TEST_RESULTS["passed"] += 1
        await browser.close()
        return test_passed


async def test_pagination_ui():
    """测试分页功能 - 增强功能验证"""
    async with async_playwright() as p:
        browser = await p.chromium.launch(headless=True)
        context = await browser.new_context()
        page = await context.new_page()

        console_errors = []
        page.on("console", lambda msg: console_errors.append(f"[{msg.type}] {msg.text}") if msg.type == "error" else None)

        print(f"\n  [Pagination Test]")
        test_passed = True

        try:
            # API前置检查
            api_ok, result = await check_api_health("countries")
            if not api_ok:
                print(f"    [FAIL] API不可访问: countries")
                TEST_RESULTS["failed"] += 1
                await browser.close()
                return False

            await page.goto(f"{FRONTEND_URL}/basedata/country", wait_until="networkidle", timeout=30000)
            await page.wait_for_timeout(1000)

            # 获取当前页码
            try:
                current_page = page.locator(".el-pagination__current").first
                await current_page.wait_for(timeout=3000)
                print(f"    [OK] 分页信息可见")
            except:
                print(f"    [WARN] 分页信息未找到")
                TEST_RESULTS["warnings"] += 1

            # 尝试切换每页条数
            try:
                page_size_select = page.locator(".el-pagination__sizes .el-input__inner")
                await page_size_select.click()
                await page.wait_for_timeout(300)
                options = page.locator(".el-select-dropdown__item")
                if await options.count() > 0:
                    await options.nth(1).click()  # 选择第二项 (如20条)
                    await page.wait_for_timeout(2000)

                    # 检查切换后是否有错误
                    if await print_error_toast(page, "切换每页条数"):
                        test_passed = False
                        TEST_RESULTS["failed"] += 1
                    else:
                        print(f"    [OK] 切换每页条数成功")
            except Exception as e:
                print(f"    [FAIL] 切换每页条数失败: {e}")
                test_passed = False
                TEST_RESULTS["failed"] += 1

        except Exception as e:
            print(f"    [FAIL] 分页测试失败: {e}")
            test_passed = False
            TEST_RESULTS["failed"] += 1

        print_console_errors(console_errors)

        if test_passed:
            TEST_RESULTS["passed"] += 1
        await browser.close()
        return test_passed


async def main():
    print("\n" + "#"*60)
    print("# Open-TMS 基础数据模块 UI自动化测试")
    print("#"*60)

    # 先检查后端
    api_ok = await test_backend_api()
    if not api_ok:
        print("\n[ERROR] Backend is not available. Please start backend first.")
        print("  java -jar basedata/target/opentms-basedata-1.0.0-SNAPSHOT.jar")
        TEST_RESULTS["failed"] += 1
        return

    # ========== 1. 测试所有列表页面 ==========
    print("\n" + "="*60)
    print("[TEST 1] 列表页面UI测试 (含功能验证)")
    print("="*60)

    for page in BASEDATA_PAGES:
        await test_page_list_ui(page["name"], page["path"], page["api"])

    # ========== 2. 测试新增弹窗 ==========
    print("\n" + "="*60)
    print("[TEST 2] 新增弹窗UI测试 (含功能验证)")
    print("="*60)

    for page_name in ["Country", "Currency", "Trader", "Counterparty", "Subsidiary"]:
        # 获取对应页面的API名称
        page_info = next((p for p in BASEDATA_PAGES if p["name"] == page_name), None)
        if page_info:
            await test_add_dialog_ui(page_name, page_info["path"], page_info["api"])
        else:
            await test_add_dialog_ui(page_name, None, None)

    # ========== 3. 测试分页功能 ==========
    print("\n" + "="*60)
    print("[TEST 3] 分页功能测试 (含功能验证)")
    print("="*60)

    await test_pagination_ui()

    print("\n" + "#"*60)
    print("# Test Summary")
    print("#"*60)
    print(f"  Backend API: {'PASS' if api_ok else 'FAIL'}")
    print(f"  UI Pages Tested: {len(BASEDATA_PAGES)}")
    print(f"  Results: {TEST_RESULTS['passed']} passed, {TEST_RESULTS['failed']} failed, {TEST_RESULTS['warnings']} warnings")
    print("#"*60 + "\n")


if __name__ == "__main__":
    asyncio.run(main())
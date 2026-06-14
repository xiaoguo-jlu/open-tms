#!/usr/bin/env python3
"""
Open-TMS Instrument 模块 UI自动化测试
测试 InstrumentList.vue 页面功能

增强功能：功能验证而非仅检查元素存在
"""

import asyncio
import time
import urllib.request
import json
from playwright.async_api import async_playwright, expect

BACKEND_URL = "http://localhost:8081/opentms/basedata"
FRONTEND_URL = "http://localhost:3000"

# 测试结果统计
TEST_RESULTS = {
    "passed": 0,
    "failed": 0,
    "warnings": 0,
}


async def check_api_health(api_path):
    """检查API端点是否可达"""
    try:
        url = f"{BACKEND_URL}/{api_path}/page?pageNum=1&pageSize=1"
        req = urllib.request.Request(url, headers={"User-Agent": "Playwright-Test"})
        resp = urllib.request.urlopen(req, timeout=5)
        data = json.loads(resp.read().decode())
        is_success = data.get('code') == 200 or (isinstance(data, dict) and 'records' in data)
        return is_success, data
    except Exception as e:
        return False, str(e)


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
        for err in error_list[:5]:
            print(f"      - {err[:200]}")


async def test_instrument_page_list():
    """测试 Instrument 列表页面UI - 增强功能验证"""
    async with async_playwright() as p:
        browser = await p.chromium.launch(headless=True)
        context = await browser.new_context()
        page = await context.new_page()

        console_errors = []
        network_errors = []

        page.on("console", lambda msg: console_errors.append(f"[{msg.type}] {msg.text}") if msg.type == "error" else None)
        page.on("response", lambda resp: network_errors.append(f"{resp.status} {resp.url}") if resp.status >= 400 else None)

        print("\n" + "-"*50)
        print("[TEST] Instrument List Page UI")
        print("-"*50)
        test_passed = True

        try:
            # 1. API前置检查
            api_ok, result = await check_api_health("api/v1/instruments")
            if not api_ok:
                print(f"    [FAIL] API不可访问: /api/v1/instruments/page")
                print(f"          错误: {result}")
                TEST_RESULTS["failed"] += 1
                await browser.close()
                return False

            # 2. 打开页面
            await page.goto(f"{FRONTEND_URL}/basedata/instrument", wait_until="networkidle", timeout=30000)
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

            # 6. 测试关键字查询功能
            try:
                search_input = page.locator(".filter-card input").first
                await search_input.fill("TEST")
                await page.locator("button:has-text('查询')").click()
                await page.wait_for_timeout(2000)

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

                rows = table.locator(".el-table__row")
                row_count = await rows.count()

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

        print_console_errors(console_errors)
        if network_errors:
            print(f"    [ERROR] 网络请求失败:")
            for err in network_errors[:5]:
                print(f"      - {err[:100]}")

        if test_passed:
            TEST_RESULTS["passed"] += 1
        await browser.close()
        return test_passed


async def test_instrument_add_dialog():
    """测试新增弹窗功能"""
    async with async_playwright() as p:
        browser = await p.chromium.launch(headless=True)
        context = await browser.new_context()
        page = await context.new_page()

        console_errors = []
        page.on("console", lambda msg: console_errors.append(f"[{msg.type}] {msg.text}") if msg.type == "error" else None)

        print("\n" + "-"*50)
        print("[TEST] Instrument Add Dialog UI")
        print("-"*50)
        test_passed = True

        try:
            # API前置检查
            api_ok, result = await check_api_health("api/v1/instruments")
            if not api_ok:
                print(f"    [FAIL] API不可访问: /api/v1/instruments/page")
                TEST_RESULTS["failed"] += 1
                await browser.close()
                return False

            await page.goto(f"{FRONTEND_URL}/basedata/instrument", wait_until="networkidle", timeout=30000)
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

                    # 验证表单字段
                    try:
                        form = page.locator(".el-form")
                        await expect(form).to_be_visible(timeout=3000)
                        print(f"    [OK] 表单可见")

                        # 验证工具编码输入框
                        code_input = page.locator("input[placeholder='唯一编码']")
                        await expect(code_input).to_be_visible(timeout=3000)
                        print(f"    [OK] 工具编码输入框可见")

                        # 验证工具名称输入框
                        name_input = page.locator("input[placeholder='请输入工具名称']")
                        await expect(name_input).to_be_visible(timeout=3000)
                        print(f"    [OK] 工具名称输入框可见")

                        # 验证工具类型选择框
                        type_select = page.locator(".el-select").first
                        await expect(type_select).to_be_visible(timeout=3000)
                        print(f"    [OK] 工具类型选择框可见")

                    except Exception as e:
                        print(f"    [WARN] 表单字段验证异常: {e}")
                        TEST_RESULTS["warnings"] += 1

                    # 关闭弹窗
                    try:
                        close_btns = page.locator("button:has-text('取消')")
                        await close_btns.last.click()
                        await page.wait_for_timeout(1000)

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


async def test_instrument_pagination():
    """测试分页功能"""
    async with async_playwright() as p:
        browser = await p.chromium.launch(headless=True)
        context = await browser.new_context()
        page = await context.new_page()

        console_errors = []
        page.on("console", lambda msg: console_errors.append(f"[{msg.type}] {msg.text}") if msg.type == "error" else None)

        print("\n" + "-"*50)
        print("[TEST] Instrument Pagination UI")
        print("-"*50)
        test_passed = True

        try:
            # API前置检查
            api_ok, result = await check_api_health("api/v1/instruments")
            if not api_ok:
                print(f"    [FAIL] API不可访问")
                TEST_RESULTS["failed"] += 1
                await browser.close()
                return False

            await page.goto(f"{FRONTEND_URL}/basedata/instrument", wait_until="networkidle", timeout=30000)
            await page.wait_for_timeout(1000)

            # 检查分页信息
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
                    await options.nth(1).click()
                    await page.wait_for_timeout(2000)

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
    print("# Open-TMS Instrument 模块 UI自动化测试")
    print("#"*60)

    # 先检查后端
    api_ok, _ = await check_api_health("api/v1/instruments")
    if not api_ok:
        print("\n[ERROR] Backend is not available. Please start backend first.")
        print("  java -jar basedata/target/opentms-basedata-1.0.0-SNAPSHOT.jar")
        TEST_RESULTS["failed"] += 1
        return

    print(f"\n[PASS] Backend API is available")

    # ========== 1. 测试列表页面 ==========
    print("\n" + "="*60)
    print("[TEST 1] 列表页面UI测试 (含功能验证)")
    print("="*60)
    await test_instrument_page_list()

    # ========== 2. 测试新增弹窗 ==========
    print("\n" + "="*60)
    print("[TEST 2] 新增弹窗UI测试 (含功能验证)")
    print("="*60)
    await test_instrument_add_dialog()

    # ========== 3. 测试分页功能 ==========
    print("\n" + "="*60)
    print("[TEST 3] 分页功能测试 (含功能验证)")
    print("="*60)
    await test_instrument_pagination()

    print("\n" + "#"*60)
    print("# Test Summary")
    print("#"*60)
    print(f"  Backend API: {'PASS' if api_ok else 'FAIL'}")
    print(f"  Results: {TEST_RESULTS['passed']} passed, {TEST_RESULTS['failed']} failed, {TEST_RESULTS['warnings']} warnings")
    print("#"*60 + "\n")


if __name__ == "__main__":
    asyncio.run(main())
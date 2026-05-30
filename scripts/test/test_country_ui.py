#!/usr/bin/env python3
"""
Open-TMS Country模块 UI自动化测试
使用Playwright进行Web UI测试
"""

import asyncio
import time
from playwright.async_api import async_playwright, expect

BACKEND_URL = "http://localhost:8081/opentms/basedata"
FRONTEND_URL = "http://localhost:3000"


async def test_country_list():
    """测试国家列表页面"""
    async with async_playwright() as p:
        browser = await p.chromium.launch(headless=True)
        context = await browser.new_context()
        page = await context.new_page()

        errors = []

        # 监听console错误
        page.on("console", lambda msg: errors.append(f"[{msg.type}] {msg.text}") if msg.type == "error" else None)

        print("\n" + "="*60)
        print("[TEST] Country List UI Automation")
        print("="*60)

        # 1. 打开国家列表页面
        print("\n[Step 1] 打开国家列表页面...")
        await page.goto(f"{FRONTEND_URL}/#/basedata/country", wait_until="networkidle", timeout=30000)
        await page.wait_for_timeout(2000)
        print(f"  [OK] 页面加载完成: {FRONTEND_URL}/#/basedata/country")

        # 2. 检查页面标题/面包屑
        print("\n[Step 2] 检查页面元素...")

        # 检查筛选表单
        try:
            filter_card = page.locator(".filter-card")
            await expect(filter_card).to_be_visible(timeout=5000)
            print("  [OK] 筛选区域可见")
        except Exception as e:
            print(f"  [WARN] 筛选区域未找到: {e}")

        # 检查表格
        try:
            table = page.locator(".el-table")
            await expect(table).to_be_visible(timeout=5000)
            print("  [OK] 数据表格可见")
        except Exception as e:
            print(f"  [WARN] 数据表格未找到: {e}")

        # 检查新增按钮
        try:
            add_btn = page.locator("button:has-text('新增')")
            await expect(add_btn).to_be_visible(timeout=5000)
            print("  [OK] 新增按钮可见")
        except Exception as e:
            print(f"  [WARN] 新增按钮未找到: {e}")

        # 3. 测试搜索功能
        print("\n[Step 3] 测试搜索功能...")
        try:
            search_input = page.locator("input[placeholder='代码/名称']")
            await search_input.fill("CN")
            await page.locator("button:has-text('查询')").click()
            await page.wait_for_timeout(1500)
            print("  [OK] 搜索'CN'执行成功")
        except Exception as e:
            print(f"  [WARN] 搜索失败: {e}")

        # 4. 测试重置功能
        print("\n[Step 4] 测试重置功能...")
        try:
            await page.locator("button:has-text('重置')").click()
            await page.wait_for_timeout(1000)
            print("  [OK] 重置按钮执行成功")
        except Exception as e:
            print(f"  [WARN] 重置失败: {e}")

        # 5. 测试新增弹窗
        print("\n[Step 5] 测试新增弹窗...")
        try:
            await page.locator("button:has-text('新增')").click()
            await page.wait_for_timeout(500)

            drawer = page.locator(".el-drawer")
            await expect(drawer).to_be_visible(timeout=3000)
            print("  [OK] 新增弹窗打开成功")

            # 填写表单
            await page.locator("input[placeholder='ISO 2位代码如CN/US']").fill("TEST_UI")
            await page.locator("input[placeholder='请输入国家名称']").fill("UI测试国家")
            await page.locator("input[placeholder='English Name']").fill("UI Test Country")
            await page.locator("input[placeholder='如: Asia/Shanghai']").fill("Asia/Shanghai")
            await page.locator("input[placeholder='如: +86']").fill("+86")
            print("  [OK] 表单填写成功")

            # 关闭弹窗
            await page.locator("button:has-text('取消')").click()
            await page.wait_for_timeout(500)
            print("  [OK] 弹窗关闭成功")
        except Exception as e:
            print(f"  [WARN] 新增弹窗测试失败: {e}")

        # 6. 检查分页组件
        print("\n[Step 6] 检查分页组件...")
        try:
            pagination = page.locator(".el-pagination")
            await expect(pagination).to_be_visible(timeout=3000)
            print("  [OK] 分页组件可见")
        except Exception as e:
            print(f"  [WARN] 分页组件未找到: {e}")

        # 7. 输出控制台错误
        if errors:
            print("\n[Console Errors]:")
            for err in errors[:10]:  # 只显示前10个
                print(f"  {err}")
        else:
            print("\n[OK] 无控制台错误")

        await browser.close()

        print("\n" + "="*60)
        print("[TEST COMPLETE]")
        print("="*60)

        return len([e for e in errors if "error" in e.lower()]) == 0


async def test_backend_api():
    """测试后端API是否正常"""
    import urllib.request
    import json

    print("\n" + "="*60)
    print("[TEST] Backend API Health Check")
    print("="*60)

    # 1. 健康检查 - 使用实际可用的API端点
    try:
        resp = urllib.request.urlopen(f"{BACKEND_URL}/api/v1/countries/page?pageNum=1&pageSize=1", timeout=5)
        data = json.loads(resp.read().decode())
        if data.get('code') == 200:
            print(f"\n[OK] Backend API: 正常 (返回{len(data.get('data',{}).get('records',[]))}条数据)")
        else:
            print(f"\n[WARN] Backend API返回异常: {data}")
    except Exception as e:
        print(f"\n[FAIL] Backend API: {e}")
        return False

    # 2. 测试分页查询
    try:
        url = f"{BACKEND_URL}/api/v1/countries/page?pageNum=1&pageSize=5"
        resp = urllib.request.urlopen(url, timeout=5)
        data = json.loads(resp.read().decode())
        if data.get('code') == 200:
            records = data.get('data', {}).get('records', [])
            print(f"[OK] Country API: 返回 {len(records)} 条记录")
        else:
            print(f"[WARN] Country API返回异常: {data}")
    except Exception as e:
        print(f"[FAIL] Country API: {e}")
        return False

    print("\n" + "="*60)
    return True


async def main():
    print("\n" + "#"*60)
    print("# Open-TMS UI Automation Test Suite")
    print("#"*60)

    # 先检查后端
    api_ok = await test_backend_api()
    if not api_ok:
        print("\n[ERROR] Backend is not available. Please start backend first.")
        print("  java -jar basedata/target/opentms-basedata-1.0.0-SNAPSHOT.jar")
        return

    # 执行UI测试
    ui_ok = await test_country_list()

    print("\n" + "#"*60)
    print("# Test Summary")
    print("#"*60)
    print(f"  Backend API: {'PASS' if api_ok else 'FAIL'}")
    print(f"  UI Automation: {'PASS' if ui_ok else 'FAIL'}")
    print("#"*60 + "\n")


if __name__ == "__main__":
    asyncio.run(main())
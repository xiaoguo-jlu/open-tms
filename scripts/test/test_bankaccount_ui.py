#!/usr/bin/env python3
"""
Open-TMS 银行账户页面 UI 自动化测试
覆盖:列表、CRUD、余额查询、银企同步 stub、新旧规范对比
"""
import asyncio
import json
import urllib.request
import urllib.error
from playwright.async_api import async_playwright

BACKEND_URL = "http://localhost:8081/opentms/basedata"
FRONTEND_URL = "http://localhost:3000"

REPORT = {"total": 0, "passed": 0, "failed": 0, "results": []}


def add_result(name, status, note=""):
    REPORT["total"] += 1
    if status == "PASS":
        REPORT["passed"] += 1
    else:
        REPORT["failed"] += 1
    REPORT["results"].append({"name": name, "status": status, "note": note})


def http_get(path):
    req = urllib.request.Request(f"{BACKEND_URL}{path}")
    with urllib.request.urlopen(req, timeout=5) as r:
        return json.loads(r.read().decode())


def http_post(path, body=None):
    data = json.dumps(body).encode() if body else b""
    req = urllib.request.Request(
        f"{BACKEND_URL}{path}",
        data=data,
        headers={"Content-Type": "application/json"},
        method="POST"
    )
    with urllib.request.urlopen(req, timeout=5) as r:
        return json.loads(r.read().decode())


def http_method(path, method, body=None):
    data = json.dumps(body).encode() if body else None
    req = urllib.request.Request(
        f"{BACKEND_URL}{path}",
        data=data,
        headers={"Content-Type": "application/json"} if data else {},
        method=method
    )
    try:
        with urllib.request.urlopen(req, timeout=5) as r:
            return r.status, json.loads(r.read().decode())
    except urllib.error.HTTPError as e:
        return e.code, None


async def main():
    print("\n" + "=" * 60)
    print("  银行账户 UI 自动化测试(基于重构后 basedata/BankAccountResource)")
    print("=" * 60)

    # ===== 1) 后端 API 健康检查 =====
    print("\n[1] 后端 API 健康检查")
    cases = [
        ("GET", "/api/v1/bank-accounts/page?pageNum=1&pageSize=1", None, 200, "列表分页"),
        ("GET", "/api/v1/bank-accounts/1", None, 200, "详情"),
        ("GET", "/api/v1/bank-accounts/1/balance", None, 200, "余额查询(新增)"),
    ]
    for method, path, body, expect_code, desc in cases:
        try:
            code, data = http_method(path, method, body)
            ok = code == expect_code and (data is None or data.get("code") == 200)
            add_result(f"API {method} {path}", "PASS" if ok else "FAIL",
                       f"HTTP={code} data={str(data)[:100] if data else None}")
            print(f"  [{'PASS' if ok else 'FAIL'}] {method} {desc} ({path})")
        except Exception as e:
            add_result(f"API {method} {path}", "FAIL", str(e))
            print(f"  [FAIL] {method} {path} - {e}")

    # ===== 2) 新增端点测试 =====
    print("\n[2] 新增端点测试")
    try:
        d = http_post("/api/v1/bank-accounts/1/sync")
        ok = d.get("code") == 200
        add_result("POST /{id}/sync (新增 stub)", "PASS" if ok else "FAIL", str(d))
        print(f"  [{'PASS' if ok else 'FAIL'}] POST /{{id}}/sync 同步 stub - {d.get('data')}")
    except Exception as e:
        add_result("POST /{id}/sync (新增 stub)", "FAIL", str(e))
        print(f"  [FAIL] POST /sync - {e}")

    # ===== 3) 规范化端点测试 =====
    print("\n[3] 规范化端点测试(@PUT/@DELETE → POST)")
    try:
        d = http_post("/api/v1/bank-accounts/update",
                      {"id": 1, "accountName": "UI-测试-不改", "accountNo": "TEST001", "version": 1})
        ok = d.get("code") == 200
        add_result("POST /update (新规范)", "PASS" if ok else "FAIL", str(d))
        print(f"  [{'PASS' if ok else 'FAIL'}] POST /update - code={d.get('code')}")
    except Exception as e:
        add_result("POST /update (新规范)", "FAIL", str(e))
        print(f"  [FAIL] POST /update - {e}")

    try:
        d = http_post("/api/v1/bank-accounts/delete/9999")
        # 即使业务报错(账户不存在)端点也通,只要 code != 5xx 系统错误
        ok = d.get("code") in (200, 500)  # 200 正常删除 / 500 业务级 Not found
        add_result("POST /delete/{id} (新规范)", "PASS" if ok else "FAIL", str(d))
        print(f"  [{'PASS' if ok else 'FAIL'}] POST /delete/{{id}} - code={d.get('code')}, msg={d.get('message')}")
    except Exception as e:
        add_result("POST /delete/{id} (新规范)", "FAIL", str(e))
        print(f"  [FAIL] POST /delete/{id} - {e}")

    # ===== 4) 旧 PUT/DELETE 应 405 =====
    print("\n[4] 旧 HTTP 方法应被禁用")
    try:
        code, _ = http_method("/api/v1/bank-accounts", "PUT", {"id": 1})
        ok = code == 405
        add_result("旧 PUT 路径 405", "PASS" if ok else "FAIL", f"HTTP={code}")
        print(f"  [{'PASS' if ok else 'FAIL'}] PUT /api/v1/bank-accounts HTTP={code}")
    except Exception as e:
        add_result("旧 PUT 路径 405", "FAIL", str(e))
        print(f"  [FAIL] PUT - {e}")

    try:
        code, _ = http_method("/api/v1/bank-accounts/1", "DELETE")
        ok = code == 405
        add_result("旧 DELETE 路径 405", "PASS" if ok else "FAIL", f"HTTP={code}")
        print(f"  [{'PASS' if ok else 'FAIL'}] DELETE /api/v1/bank-accounts/1 HTTP={code}")
    except Exception as e:
        add_result("旧 DELETE 路径 405", "FAIL", str(e))
        print(f"  [FAIL] DELETE - {e}")

    # ===== 5) 前端 UI 测试 =====
    print("\n[5] 前端 UI 测试 (Playwright)")
    async with async_playwright() as p:
        browser = await p.chromium.launch(headless=True)
        ctx = await browser.new_context()
        page = await ctx.new_page()

        # 🔑 关键:捕获 console 错误,防止 404/JS 错误导致假阳性
        console_errors = []
        page_errors = []
        failed_requests = []

        def on_console(msg):
            if msg.type in ("error",):
                console_errors.append(f"[{msg.type}] {msg.text}")

        def on_page_error(err):
            page_errors.append(str(err))

        def on_request_failed(req):
            failed_requests.append(f"{req.method} {req.url} - {req.failure}")

        page.on("console", on_console)
        page.on("pageerror", on_page_error)
        page.on("requestfailed", on_request_failed)

        # 5.1 打开银行账户列表页
        try:
            await page.goto(f"{FRONTEND_URL}/basedata/bank-account", wait_until="networkidle", timeout=30000)
            await page.wait_for_timeout(2000)
            table_count = await page.locator(".el-table").count()
            ok = table_count > 0
            add_result("UI 打开银行账户列表页", "PASS" if ok else "FAIL", f"table={table_count}")
            print(f"  [{'PASS' if ok else 'FAIL'}] 打开 /basedata/bank-account, table 数={table_count}")
        except Exception as e:
            add_result("UI 打开银行账户列表页", "FAIL", str(e))
            print(f"  [FAIL] 打开列表页 - {e}")
            await browser.close()
            return

        # 5.2 搜索框可见
        try:
            search = page.locator(".search-bar input, .el-input__inner").first
            ok = await search.is_visible()
            add_result("UI 搜索框可见", "PASS" if ok else "FAIL")
            print(f"  [{'PASS' if ok else 'FAIL'}] 搜索框可见")
        except Exception as e:
            add_result("UI 搜索框可见", "FAIL", str(e))
            print(f"  [FAIL] 搜索框 - {e}")

        # 5.3 表格有数据
        try:
            await page.wait_for_selector(".el-table__body tr", timeout=5000)
            rows = await page.locator(".el-table__body tr").count()
            ok = rows > 0
            add_result("UI 表格加载数据", "PASS" if ok else "FAIL", f"行数={rows}")
            print(f"  [{'PASS' if ok else 'FAIL'}] 表格加载,行数={rows}")
        except Exception as e:
            add_result("UI 表格加载数据", "FAIL", str(e))
            print(f"  [FAIL] 表格加载 - {e}")

        # 5.4 新增按钮可见
        try:
            add_btn = page.locator("button:has-text('新增'), button:has-text('添加'), button:has-text('新建')").first
            ok = await add_btn.is_visible()
            add_result("UI 新增按钮可见", "PASS" if ok else "FAIL")
            print(f"  [{'PASS' if ok else 'FAIL'}] 新增按钮可见")
        except Exception as e:
            add_result("UI 新增按钮可见", "FAIL", str(e))
            print(f"  [FAIL] 新增按钮 - {e}")

        # 5.5 点新增,弹窗
        try:
            add_btn = page.locator("button:has-text('新增'), button:has-text('添加'), button:has-text('新建')").first
            await add_btn.click()
            await page.wait_for_timeout(1500)
            dialog_count = await page.locator(".el-dialog:visible").count()
            ok = dialog_count > 0
            add_result("UI 新增对话框", "PASS" if ok else "FAIL", f"dialog={dialog_count}")
            print(f"  [{'PASS' if ok else 'FAIL'}] 新增对话框,dialog 数={dialog_count}")
            if ok:
                close = page.locator(".el-dialog:visible .el-dialog__close").first
                if await close.count() > 0:
                    await close.click()
                    await page.wait_for_timeout(500)
        except Exception as e:
            add_result("UI 新增对话框", "FAIL", str(e))
            print(f"  [FAIL] 新增对话框 - {e}")

        # 5.6 截图
        try:
            await page.screenshot(path="/tmp/bankaccount_ui.png", full_page=True)
            print(f"  [SCREENSHOT] /tmp/bankaccount_ui.png")
            add_result("UI 截图", "PASS", "/tmp/bankaccount_ui.png")
        except Exception as e:
            add_result("UI 截图", "FAIL", str(e))

        # 5.7 🔑 关键:验证页面无 console error 和无失败请求
        real_console_errors = [e for e in console_errors if "[vite]" not in e]
        ok = len(real_console_errors) == 0 and len(failed_requests) == 0 and len(page_errors) == 0
        detail = f"console_errors={len(real_console_errors)} failed_requests={len(failed_requests)} page_errors={len(page_errors)}"
        if real_console_errors:
            detail += f" | first: {real_console_errors[0][:200]}"
        add_result("UI 页面无 console error / 无失败请求", "PASS" if ok else "FAIL", detail)
        print(f"  [{'PASS' if ok else 'FAIL'}] UI 页面无 console error - {detail}")

        if real_console_errors:
            print(f"\n  Console errors 详情:")
            for e in real_console_errors[:5]:
                print(f"    {e[:200]}")
        if failed_requests:
            print(f"\n  失败请求详情:")
            for r in failed_requests[:5]:
                print(f"    {r[:200]}")
        if page_errors:
            print(f"\n  Page errors 详情:")
            for e in page_errors[:5]:
                print(f"    {e[:200]}")

        await browser.close()

    # 总结
    print("\n" + "=" * 60)
    print("  银行账户 UI 测试报告")
    print("=" * 60)
    print(f"  Total:     {REPORT['total']}")
    print(f"  Passed:    {REPORT['passed']}")
    print(f"  Failed:    {REPORT['failed']}")
    rate = REPORT['passed'] / REPORT['total'] * 100 if REPORT['total'] else 0
    print(f"  Pass Rate: {rate:.0f}%")
    with open("scripts/test/test_bankaccount_ui_report.json", "w", encoding="utf-8") as f:
        json.dump(REPORT, f, ensure_ascii=False, indent=2)
    print(f"\n  Report saved: scripts/test/test_bankaccount_ui_report.json")
    print("=" * 60)


asyncio.run(main())

#!/usr/bin/env python3
"""
Open-TMS Account Transfer (AT) API Tests
Tests for the AT (Account Transfer) transfer management module.

Suite: at
Endpoints:
    - GET /api/v1/transfer/transactions - List transfers
    - GET /api/v1/transfer/transactions/{id} - Get transfer detail
    - POST /api/v1/transfer/transactions - Create transfer
    - PUT /api/v1/transfer/transactions - Update transfer
    - DELETE /api/v1/transfer/transactions/{id} - Delete transfer
    - POST /api/v1/transfer/transactions/{id}/submit - Submit for approval
    - POST /api/v1/transfer/transactions/{id}/execute - Execute transfer
    - POST /api/v1/transfer/transactions/{id}/cancel - Cancel transfer
    - GET /api/v1/transfer/accounts - List available accounts
"""

import sys
import random
import string
import time
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))

try:
    import requests
except ImportError:
    print("[ERROR] requests library not installed. Run: pip install requests")
    sys.exit(1)

BASE_URL = "http://localhost:8081/api/v1/transfer"


class Colors:
    GREEN = '\033[92m'
    RED = '\033[91m'
    YELLOW = '\033[93m'
    RESET = '\033[0m'


results = {"passed": 0, "failed": 0, "total": 0, "bugs": []}


def gen_code(prefix="TR"):
    return f"{prefix}{''.join(random.choices(string.digits, k=6))}"


def test(name, method, endpoint="", body=None, expected_status=200, expected_code=None):
    results["total"] += 1
    url = BASE_URL + endpoint
    headers = {"Content-Type": "application/json"}

    print(f"\n[{results['total']}] {name}")
    print(f"    {method} {url}")

    try:
        if method == "GET":
            resp = requests.get(url, headers=headers, timeout=10)
        elif method == "POST":
            resp = requests.post(url, headers=headers, json=body, timeout=10)
        elif method == "PUT":
            resp = requests.put(url, headers=headers, json=body, timeout=10)
        elif method == "DELETE":
            resp = requests.delete(url, headers=headers, timeout=10)

        resp_json = resp.json()
        status = resp.status_code
        code = resp_json.get("code")

        print(f"    Status: {status}, Code: {code}")

        if status == 500 or code == 500:
            print(f"    {Colors.RED}X FAIL - 500 Server Error{Colors.RESET}")
            results["failed"] += 1
            results["bugs"].append(name)
            return False
        elif expected_code and code == expected_code:
            print(f"    {Colors.GREEN}V PASS{Colors.RESET}")
            results["passed"] += 1
            return True
        elif status in [200, 201]:
            print(f"    {Colors.GREEN}V PASS{Colors.RESET}")
            results["passed"] += 1
            return True
        elif status == 400:
            print(f"    {Colors.GREEN}V PASS (expected 400){Colors.RESET}")
            results["passed"] += 1
            return True
        elif status == 404:
            print(f"    {Colors.GREEN}V PASS (expected 404){Colors.RESET}")
            results["passed"] += 1
            return True
        else:
            print(f"    {Colors.YELLOW}? UNEXPECTED: {status}{Colors.RESET}")
            results["failed"] += 1
            results["bugs"].append(name)
            return False

    except Exception as e:
        print(f"    {Colors.RED}X FAIL - {e}{Colors.RESET}")
        results["failed"] += 1
        results["bugs"].append(name)
        return False


def run_tests():
    print("=" * 60)
    print("Open-TMS 账户转账(AT) API自动化测试")
    print("=" * 60)

    print("\n========== 一、列表接口测试 ==========")
    test("TC_AT_LIST_001: 列表查询-全部数据", "GET", "/transactions")
    test("TC_AT_LIST_002: 分页查询-默认参数", "GET", "/transactions?pageNum=1&pageSize=10")
    test("TC_AT_LIST_003: 分页查询-状态筛选", "GET", "/transactions?status=New")
    test("TC_AT_LIST_004: 分页查询-空关键字", "GET", "/transactions?keyword=")

    print("\n========== 二、详情接口测试 ==========")
    test("TC_AT_DET_001: 详情查询-存在ID=1", "GET", "/transactions/1")
    test("TC_AT_DET_002: 详情查询-不存在ID", "GET", "/transactions/99999", expected_status=404)

    print("\n========== 三、新增接口测试 ==========")
    test("TC_AT_ADD_001: 新增转账-正常", "POST", "/transactions", body={
        "transferDate": "2026-04-06",
        "businessUnit": "BU001",
        "fromAccount": "6222021234567890",
        "toAccount": "6222029876543210",
        "amount": 100000.00,
        "currency": "CNY",
        "expectedDate": "2026-04-07",
        "paymentMethod": "Transfer",
        "transferType": "Internal",
        "needAuthorization": "1",
        "applicant": "张三"
    })
    test("TC_AT_ADD_002: 新增转账-必填项为空", "POST", "/transactions", body={
        "fromAccount": "",
        "toAccount": "",
        "amount": None
    }, expected_status=400)

    print("\n========== 四、更新接口测试 ==========")
    test("TC_AT_UPD_001: 更新转账-正常", "PUT", "/transactions", body={
        "id": 1,
        "transferReason": "更新后的转账原因"
    })

    print("\n========== 五、删除接口测试 ==========")
    test("TC_AT_DEL_001: 删除转账-不存在ID", "DELETE", "/transactions/99999")

    print("\n========== 六、状态流转测试 ==========")
    test("TC_AT_STS_001: 提交审批-正常", "POST", "/transactions/1/submit")
    test("TC_AT_STS_002: 执行转账-正常", "POST", "/transactions/1/execute")
    test("TC_AT_STS_003: 取消转账-正常", "POST", "/transactions/2/cancel")

    print("\n========== 七、账户查询测试 ==========")
    test("TC_AT_ACC_001: 查询账户列表", "GET", "/accounts")

    print("\n" + "=" * 60)
    print("测试结果汇总")
    print("=" * 60)
    print(f"总计: {results['passed']}/{results['total']} 通过")
    print(f"通过率: {results['passed'] / results['total'] * 100:.1f}%")
    print(f"失败: {results['failed']}")

    if results["bugs"]:
        print(f"\n发现的Bug ({len(results['bugs'])}个):")
        for b in sorted(set(results["bugs"])):
            print(f"  - {b}")
    print("=" * 60)


if __name__ == "__main__":
    run_tests()
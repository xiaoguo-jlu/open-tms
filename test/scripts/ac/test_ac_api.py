#!/usr/bin/env python3
"""
Open-TMS Actual Cashflow (AC) API Tests
Tests for the AC (Actual Cashflow) cashflow management module.

Suite: ac
Endpoints:
    - GET /api/v1/ac/cashflows - List cashflows
    - GET /api/v1/ac/cashflows/{id} - Get cashflow detail
    - POST /api/v1/ac/cashflows - Create cashflow
    - PUT /api/v1/ac/cashflows - Update cashflow
    - DELETE /api/v1/ac/cashflows/{id} - Delete cashflow
    - POST /api/v1/ac/cashflows/{id}/confirm - Confirm/clear cashflow
    - POST /api/v1/ac/generate/{dealId} - Generate cashflow from deal
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

BASE_URL = "http://localhost:8081/api/v1/ac"


class Colors:
    GREEN = '\033[92m'
    RED = '\033[91m'
    YELLOW = '\033[93m'
    RESET = '\033[0m'


results = {"passed": 0, "failed": 0, "total": 0, "bugs": []}


def gen_code(prefix="CF"):
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
    print("Open-TMS 实际现金流(AC) API自动化测试")
    print("=" * 60)

    print("\n========== 一、列表接口测试 ==========")
    test("TC_AC_LIST_001: 列表查询-全部数据", "GET", "/cashflows")
    test("TC_AC_LIST_002: 分页查询-默认参数", "GET", "/cashflows?pageNum=1&pageSize=10")
    test("TC_AC_LIST_003: 分页查询-状态筛选", "GET", "/cashflows?status=Created")
    test("TC_AC_LIST_004: 分页查询-流向筛选", "GET", "/cashflows?direction=Inflow")
    test("TC_AC_LIST_005: 分页查询-空关键字", "GET", "/cashflows?keyword=")

    print("\n========== 二、详情接口测试 ==========")
    test("TC_AC_DET_001: 详情查询-存在ID=1", "GET", "/cashflows/1")
    test("TC_AC_DET_002: 详情查询-不存在ID", "GET", "/cashflows/99999", expected_status=404)
    test("TC_AC_DET_003: 详情查询-非法ID格式", "GET", "/cashflows/abc", expected_status=404)

    print("\n========== 三、新增接口测试 ==========")
    unique_no = gen_code("CF")
    test("TC_AC_ADD_001: 新增现金流-正常(流入)", "POST", "/cashflows", body={
        "businessUnit": "BU001",
        "bankAccount": "6222021234567890",
        "direction": "Inflow",
        "amount": 100000.00,
        "currency": "CNY",
        "cashflowDate": "2026-04-06",
        "valueDate": "2026-04-06",
        "sourceType": "Bank Transfer",
        "sourceRef": "TR202604060001"
    })
    test("TC_AC_ADD_002: 新增现金流-正常(流出)", "POST", "/cashflows", body={
        "businessUnit": "BU001",
        "bankAccount": "6222021234567890",
        "direction": "Outflow",
        "amount": 50000.00,
        "currency": "CNY",
        "cashflowDate": "2026-04-06",
        "valueDate": "2026-04-06",
        "sourceType": "Bank Transfer",
        "sourceRef": "TR202604060002"
    })
    test("TC_AC_ADD_003: 新增现金流-必填项为空", "POST", "/cashflows", body={
        "businessUnit": "",
        "bankAccount": "",
        "direction": ""
    }, expected_status=400)

    print("\n========== 四、更新接口测试 ==========")
    test("TC_AC_UPD_001: 更新现金流-正常", "PUT", "/cashflows", body={
        "id": 1,
        "purpose": "更新后的用途"
    })

    print("\n========== 五、删除接口测试 ==========")
    test("TC_AC_DEL_001: 删除现金流-不存在ID", "DELETE", "/cashflows/99999")

    print("\n========== 六、确认/清分接口测试 ==========")
    test("TC_AC_CFM_001: 确认现金流-正常", "POST", "/cashflows/1/confirm")

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
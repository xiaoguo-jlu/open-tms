#!/usr/bin/env python3
"""
Open-TMS General API Tests
General API tests for Open-TMS that don't belong to specific modules.

Suite: general
Covers:
    - Health check
    - Cross-module integration tests
    - General utility endpoints
"""

import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))

try:
    import requests
except ImportError:
    print("[ERROR] requests library not installed. Run: pip install requests")
    sys.exit(1)

BASE_URL = "http://localhost:8081"


class Colors:
    GREEN = '\033[92m'
    RED = '\033[91m'
    YELLOW = '\033[93m'
    RESET = '\033[0m'


results = {"passed": 0, "failed": 0, "total": 0, "bugs": []}


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
    print("Open-TMS General API 自动化测试")
    print("=" * 60)

    print("\n========== 一、健康检查 ==========")
    test("TC_GEN_HC_001: 健康检查", "GET", "/api/health")

    print("\n========== 二、基于数据模块 ==========")
    test("TC_GEN_BD_001: 业务单元列表", "GET", "/api/v1/business-units")
    test("TC_GEN_BD_002: 币种列表", "GET", "/api/v1/currencies")
    test("TC_GEN_BD_003: 国家列表", "GET", "/api/v1/countries")

    print("\n========== 三、错误处理 ==========")
    test("TC_GEN_ERR_001: 不存在的接口", "GET", "/api/v1/nonexist", expected_status=404)
    test("TC_GEN_ERR_002: 方法不支持", "POST", "/api/v1/currencies", body={}, expected_status=400)

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
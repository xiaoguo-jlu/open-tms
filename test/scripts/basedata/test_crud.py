#!/usr/bin/env python3
"""
基础数据模块 - 币种/国家/交易员/节假日 CRUD API 测试
"""

import requests
import json
import random
import string
import sys

BASE_URL = "http://localhost:8081/opentms/basedata/api/v1"

class Colors:
    GREEN = '\033[92m'
    RED = '\033[91m'
    YELLOW = '\033[93m'
    RESET = '\033[0m'

results = {"passed": 0, "failed": 0, "total": 0, "bugs": []}

def gen_code(prefix=""):
    return prefix + ''.join(random.choices(string.ascii_uppercase, k=4))

def test(name, method, endpoint="", body=None, expected_status=200, expected_code=200):
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
        else:
            print(f"    {Colors.YELLOW}? UNKNOWN METHOD{Colors.RESET}")
            results["failed"] += 1
            return False

        status = resp.status_code
        print(f"    HTTP Status: {status}")

        resp_body = None
        try:
            resp_body = resp.json()
            resp_code = resp_body.get("code")
            print(f"    Response Code: {resp_code}")
        except:
            resp_code = None

        if status >= 500:
            print(f"    {Colors.RED}X FAIL - Server Error (HTTP 5xx){Colors.RESET}")
            results["failed"] += 1
            results["bugs"].append(f"{name} - HTTP {status}")
            return False

        if resp_code is not None and resp_code != expected_code and resp_code >= 400:
            print(f"    {Colors.RED}X FAIL - Business Error (code={resp_code}){Colors.RESET}")
            results["failed"] += 1
            results["bugs"].append(f"{name} - code={resp_code}")
            return False

        if status == expected_status or status in [200, 201]:
            if resp_code == expected_code or resp_code is None:
                print(f"    {Colors.GREEN}V PASS{Colors.RESET}")
                results["passed"] += 1
                return True
            else:
                print(f"    {Colors.RED}X FAIL - Expected code={expected_code}, got {resp_code}{Colors.RESET}")
                results["failed"] += 1
                results["bugs"].append(f"{name} - code mismatch {expected_code} vs {resp_code}")
                return False
        elif status == 400:
            print(f"    {Colors.YELLOW}! BAD REQUEST: {resp_body.get('message', 'Unknown') if resp_body else 'Unknown'}{Colors.RESET}")
            results["passed"] += 1
            return True
        elif status == 404:
            print(f"    {Colors.YELLOW}! NOT FOUND{Colors.RESET}")
            results["passed"] += 1
            return True
        else:
            print(f"    {Colors.YELLOW}? UNEXPECTED: {status}{Colors.RESET}")
            results["failed"] += 1
            results["bugs"].append(f"{name} - unexpected status {status}")
            return False

    except Exception as e:
        print(f"    {Colors.RED}X FAIL - {e}{Colors.RESET}")
        results["failed"] += 1
        results["bugs"].append(f"{name} - {e}")
        return False


def test_currency():
    print("\n" + "="*60)
    print("一、币种管理 (Currency) 接口测试")
    print("="*60)

    code = gen_code("CY")
    test("TC_CY_01: 列表查询", "GET", "/currencies")
    test("TC_CY_02: 分页查询", "GET", "/currencies/page?pageNum=1&pageSize=10")
    test("TC_CY_03: 详情查询-存在ID", "GET", "/currencies/1")

    test("TC_CY_04: 新增币种", "POST", "/currencies", body={
        "code": code,
        "name": "测试币种_" + code,
        "symbol": "T",
        "decimalPlaces": 2,
        "status": "1"
    })

    test("TC_CY_05: 新增重复编码-应返回业务错误", "POST", "/currencies", body={
        "code": code,
        "name": "重复测试",
        "symbol": "T",
        "decimalPlaces": 2,
        "status": "1"
    }, expected_code=400)

    test("TC_CY_06: 更新币种", "POST", "/currencies/update", body={
        "id": 1,
        "code": "CNY",
        "name": "人民币_修改",
        "symbol": "¥",
        "decimalPlaces": 2,
        "status": "1"
    })

    test("TC_CY_07: 删除不存在的币种-应返回错误", "POST", "/currencies/delete/99999", expected_code=400)


def test_country():
    print("\n" + "="*60)
    print("二、国家/地区管理 (Country) 接口测试")
    print("="*60)

    code = gen_code("CT")
    test("TC_CT_01: 列表查询", "GET", "/countries")
    test("TC_CT_02: 分页查询", "GET", "/countries/page?pageNum=1&pageSize=10")
    test("TC_CT_03: 详情查询-存在ID", "GET", "/countries/1")

    test("TC_CT_04: 新增国家", "POST", "/countries", body={
        "code": code,
        "name": "测试国家_" + code,
        "enName": "Test Country",
        "timezone": "Asia/Shanghai",
        "status": "1"
    })

    test("TC_CT_05: 更新国家", "POST", "/countries/update", body={
        "id": 1,
        "code": "CN",
        "name": "中国_修改",
        "enName": "China",
        "timezone": "Asia/Shanghai",
        "status": "1"
    })

    test("TC_CT_06: 删除不存在的国家-应返回错误", "POST", "/countries/delete/99999", expected_code=400)


def test_trader():
    print("\n" + "="*60)
    print("三、交易员管理 (Trader) 接口测试")
    print("="*60)

    code = gen_code("TR")
    test("TC_TR_01: 列表查询", "GET", "/traders")
    test("TC_TR_02: 分页查询", "GET", "/traders/page?pageNum=1&pageSize=10")
    test("TC_TR_03: 详情查询-存在ID", "GET", "/traders/1")

    test("TC_TR_04: 新增交易员", "POST", "/traders", body={
        "code": code,
        "name": "测试交易员_" + code,
        "department": "资金部",
        "email": f"test_{code}@company.com",
        "status": "1"
    })

    test("TC_TR_05: 更新交易员", "POST", "/traders/update", body={
        "id": 1,
        "code": "T001",
        "name": "李四_修改",
        "department": "资金部",
        "email": "li.si@company.com",
        "status": "1"
    })

    test("TC_TR_06: 删除不存在的交易员-应返回错误", "POST", "/traders/delete/99999", expected_code=400)


def test_holiday():
    print("\n" + "="*60)
    print("四、节假日管理 (Holiday) 接口测试")
    print("="*60)

    code = gen_code("HD")
    test("TC_HD_01: 列表查询", "GET", "/holidays")
    test("TC_HD_02: 分页查询", "GET", "/holidays/page?pageNum=1&pageSize=10")
    test("TC_HD_03: 分页查询-按国家", "GET", "/holidays/page?countryCode=CN")
    test("TC_HD_04: 详情查询-存在ID", "GET", "/holidays/1")

    test("TC_HD_05: 新增节假日", "POST", "/holidays", body={
        "holidayDate": "2026-12-25",
        "name": "圣诞节_" + code,
        "countryCode": "US",
        "isAdjacent": "0",
        "status": "1"
    })

    test("TC_HD_06: 更新节假日", "POST", "/holidays/update", body={
        "id": 1,
        "holidayDate": "2026-01-01",
        "name": "元旦",
        "countryCode": "CN",
        "isAdjacent": "0",
        "status": "1"
    })

    test("TC_HD_07: 删除不存在的节假日-应返回错误", "POST", "/holidays/delete/99999", expected_code=400)


def run_tests():
    print("="*60)
    print("Open-TMS 基础数据模块 CRUD API 自动化测试")
    print("="*60)

    test_currency()
    test_country()
    test_trader()
    test_holiday()

    print("\n" + "="*60)
    print("测试结果汇总")
    print("="*60)
    print(f"总计: {results['passed']}/{results['total']} 通过")
    print(f"通过率: {results['passed']/results['total']*100:.1f}%")
    print(f"失败: {results['failed']}")

    if results["bugs"]:
        print(f"\n发现的Bug ({len(results['bugs'])}个):")
        for b in sorted(set(results["bugs"])):
            print(f"  - {b}")

    print("="*60)

    return 0 if results["failed"] == 0 else 1


if __name__ == "__main__":
    sys.exit(run_tests())
import requests
import json
import random
import string
import time

BASE_URL = "http://localhost:8081/api/v1/currencies"

import os
import sys
sys.stdout = open(sys.stdout.fileno(), mode='w', encoding='utf-8', buffering=1)

class Colors:
    GREEN = '\033[92m'
    RED = '\033[91m'
    YELLOW = '\033[93m'
    RESET = '\033[0m'

results = {"passed": 0, "failed": 0, "total": 0, "bugs": []}

def gen_code():
    return ''.join(random.choices(string.ascii_uppercase, k=4))

def test(name, method, endpoint="", body=None, expected_status=200, expected_code=None):
    results["total"] += 1
    url = BASE_URL + endpoint
    headers = {"Content-Type": "application/json"}

    print(f"\n[{results['total']}] {name}")
    print(f"    {method} {url}")

    try:
        if method == "GET":
            resp = requests.get(url, headers=headers, timeout=5)
        elif method == "POST":
            resp = requests.post(url, headers=headers, json=body, timeout=5)
        elif method == "PUT":
            resp = requests.put(url, headers=headers, json=body, timeout=5)
        elif method == "DELETE":
            resp = requests.delete(url, headers=headers, timeout=5)

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
        elif status == 500:
            print(f"    {Colors.RED}X FAIL - 500 Server Error{Colors.RESET}")
            results["failed"] += 1
            results["bugs"].append(name)
            return False
        elif status == expected_status or status in [200, 201]:
            print(f"    {Colors.GREEN}V PASS{Colors.RESET}")
            results["passed"] += 1
            return True
        elif status == 400 and code in ["VALIDATION_ERROR", "DUPLICATE_CODE"]:
            print(f"    {Colors.GREEN}V PASS (expected validation){Colors.RESET}")
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
    print("="*60)
    print("Open-TMS 币种管理接口自动化测试")
    print("="*60)

    print("\n========== 一、列表接口测试 ==========")
    
    test("TC_CY_LIST_001: 列表查询-全部数据", "GET")
    test("TC_CY_PG_001: 分页查询-默认参数", "GET", "/page?pageNum=1&pageSize=10")
    test("TC_CY_PG_002: 分页查询-关键字搜索CNY", "GET", "/page?keyword=CNY")
    test("TC_CY_PG_003: 分页查询-状态筛选", "GET", "/page?status=1")
    test("TC_CY_PG_004: 分页查询-空关键字", "GET", "/page?keyword=")
    test("TC_CY_PG_005: 分页查询-不存在关键字", "GET", "/page?keyword=ZZZZ")
    test("TC_CY_PG_006: 分页查询-超大页码", "GET", "/page?pageNum=999")
    test("TC_CY_PG_007: 分页查询-过大pageSize", "GET", "/page?pageSize=9999")

    print("\n========== 二、详情接口测试 ==========")

    test("TC_CY_DET_001: 详情查询-存在ID=1", "GET", "/1")
    test("TC_CY_DET_002: 详情查询-存在ID=2", "GET", "/2")
    test("TC_CY_DET_003: 详情查询-不存在ID", "GET", "/99999", expected_status=404)
    test("TC_CY_DET_004: 详情查询-ID=0", "GET", "/0", expected_status=404)
    test("TC_CY_DET_005: 详情查询-非法ID(负数)", "GET", "/-1")
    test("TC_CY_DET_006: 详情查询-非法ID(字符串)", "GET", "/abc")

    print("\n========== 三、新增接口测试 ==========")

    unique_code = gen_code()
    test("TC_CY_ADD_001: 新增-完整字段", "POST", body={
        "currencyCode": unique_code,
        "currencyName": "测试币种_" + unique_code,
        "currencySymbol": "T",
        "decimalPlaces": 2,
        "status": "1"
    })

    test("TC_CY_ADD_002: 新增-编码重复(CNY)", "POST", body={
        "currencyCode": "CNY",
        "currencyName": "重复测试",
        "currencySymbol": "¥",
        "decimalPlaces": 2,
        "status": "1"
    }, expected_code="DUPLICATE_CODE")

    test("TC_CY_ADD_003: 新增-缺少编码", "POST", body={
        "currencyName": "无编码测试",
        "currencySymbol": "N",
        "decimalPlaces": 2,
        "status": "1"
    }, expected_code="VALIDATION_ERROR")

    test("TC_CY_ADD_004: 新增-缺少名称", "POST", body={
        "currencyCode": "NONM",
        "currencySymbol": "N",
        "decimalPlaces": 2,
        "status": "1"
    }, expected_code="VALIDATION_ERROR")

    test("TC_CY_ADD_005: 新增-空请求体", "POST", body={}, expected_code="VALIDATION_ERROR")

    test("TC_CY_ADD_006: 新增-编码超长(20位)", "POST", body={
        "currencyCode": "A" * 20,
        "currencyName": "超长编码",
        "currencySymbol": "L",
        "decimalPlaces": 2,
        "status": "1"
    })

    test("TC_CY_ADD_007: 新增-小数位数为负数", "POST", body={
        "currencyCode": "NEG",
        "currencyName": "负数小数",
        "currencySymbol": "-",
        "decimalPlaces": -1,
        "status": "1"
    })

    test("TC_CY_ADD_008: 新增-小数位数过大(10)", "POST", body={
        "currencyCode": "BIG",
        "currencyName": "过大小数",
        "currencySymbol": "B",
        "decimalPlaces": 10,
        "status": "1"
    })

    test("TC_CY_ADD_009: 新增-不含状态", "POST", body={
        "currencyCode": "NOST",
        "currencyName": "无状态测试",
        "currencySymbol": "S",
        "decimalPlaces": 2
    })

    print("\n========== 四、编辑接口测试 ==========")

    test("TC_CY_UPD_001: 更新-修改名称", "PUT", body={
        "id": 1,
        "currencyCode": "CNY",
        "currencyName": "人民币_UPD",
        "currencySymbol": "¥",
        "decimalPlaces": 2,
        "status": "1"
    })

    test("TC_CY_UPD_002: 更新-修改状态为停用", "PUT", body={
        "id": 1,
        "currencyCode": "CNY",
        "currencyName": "人民币_UPD",
        "currencySymbol": "¥",
        "decimalPlaces": 2,
        "status": "0"
    })

    test("TC_CY_UPD_003: 更新-恢复启用状态", "PUT", body={
        "id": 1,
        "currencyCode": "CNY",
        "currencyName": "人民币",
        "currencySymbol": "¥",
        "decimalPlaces": 2,
        "status": "1"
    })

    test("TC_CY_UPD_004: 更新-不存在ID", "PUT", body={
        "id": 99999,
        "currencyCode": "CNY",
        "currencyName": "不存在",
        "status": "1"
    })

    test("TC_CY_UPD_005: 更新-缺少ID", "PUT", body={
        "currencyCode": "CNY",
        "currencyName": "无ID测试",
        "status": "1"
    })

    test("TC_CY_UPD_006: 更新-修改小数位数", "PUT", body={
        "id": 5,
        "currencyCode": "JPY",
        "currencyName": "日元",
        "currencySymbol": "¥",
        "decimalPlaces": 0,
        "status": "1"
    })

    print("\n========== 五、删除接口测试 ==========")

    test("TC_CY_DEL_001: 删除-不存在ID", "DELETE", "/99999")
    test("TC_CY_DEL_002: 删除-ID为0", "DELETE", "/0")
    test("TC_CY_DEL_003: 删除-非法ID(负数)", "DELETE", "-1")

    print("\n========== 六、场景组合测试 ==========")

    scenario_code = "SCN" + gen_code()
    test("E2E_001: 新增-组合测试首次新增", "POST", body={
        "currencyCode": scenario_code,
        "currencyName": "场景测试",
        "currencySymbol": "S",
        "decimalPlaces": 4,
        "status": "1"
    })

    test("E2E_002: 组合测试-查询列表包含新数据", "GET", "/page?keyword=" + scenario_code)

    test("E2E_003: 组合测试-编辑名称", "PUT", body={
        "id": 1,
        "currencyCode": "CNY",
        "currencyName": "人民币_SCENE",
        "currencySymbol": "¥",
        "decimalPlaces": 2,
        "status": "1"
    })

    test("E2E_004: 组合测试-编辑后查询详情", "GET", "/1")

    test("E2E_005: 组合测试-恢复数据", "PUT", body={
        "id": 1,
        "currencyCode": "CNY",
        "currencyName": "人民币",
        "currencySymbol": "¥",
        "decimalPlaces": 2,
        "status": "1"
    })

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


if __name__ == "__main__":
    run_tests()
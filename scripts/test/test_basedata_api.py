#!/usr/bin/env python3
"""
Open-TMS 基础数据模块 API自动化测试
测试所有基础数据实体: Bank, Counterparty, CounterpartyAccount, Country, Currency, Holiday, Trader, BusinessUnit, Subsidiary, CurrencyPair
"""

import subprocess
import json
import time
import sys
import urllib.request
import urllib.error

BACKEND_URL = "http://localhost:8081/opentms/basedata"


def curl_cmd(method, path, data=None):
    """执行curl命令并返回JSON响应"""
    cmd = ["curl", "-s", "-X", method, f"{BACKEND_URL}{path}"]
    if data:
        cmd.extend(["-H", "Content-Type: application/json", "-d", json.dumps(data)])
    try:
        result = subprocess.run(cmd, capture_output=True, text=True, timeout=10, encoding='utf-8', errors='replace')
        if result.stdout:
            resp = json.loads(result.stdout)
            # Some APIs return Page directly without Result wrapper
            if isinstance(resp, dict) and "code" not in resp and "records" in resp:
                return {"code": 200, "data": resp}
            return resp
    except Exception as e:
        print(f"  [ERROR] {e}")
    return None


def check_api_health():
    """检查API是否可用"""
    print("\n[Health Check] 测试API连接...")
    try:
        resp = urllib.request.urlopen(f"{BACKEND_URL}/api/v1/countries/page?pageNum=1&pageSize=1", timeout=5)
        data = json.loads(resp.read().decode())
        if data.get('code') == 200:
            print("[PASS] API连接正常")
            return True
    except Exception as e:
        print(f"[FAIL] API连接失败: {e}")
    return False


# ========== Country API Tests ==========
def test_country():
    """测试Country CRUD"""
    print("\n" + "-"*50)
    print("[TEST] Country API")
    print("-"*50)
    results = []

    # Query
    print("  [1] Query Country List")
    resp = curl_cmd("GET", "/api/v1/countries/page?pageNum=1&pageSize=5")
    if resp and resp.get("code") == 200:
        records = resp.get("data", {}).get("records", [])
        total = resp.get("data", {}).get("total", 0)
        print(f"      [PASS] 返回 {len(records)} 条记录, total={total}")
        results.append(True)
    else:
        print(f"      [FAIL] 查询失败")
        results.append(False)

    # Create
    print("  [2] Create Country")
    new_code = f"AT{int(time.time()) % 100000}"
    new_country = {"code": new_code, "name": f"Test_{new_code}", "status": "1", "timezone": "Asia/Shanghai"}
    resp = curl_cmd("POST", "/api/v1/countries", new_country)
    if resp and resp.get("code") == 200:
        print(f"      [PASS] 创建成功: {new_code}")
        results.append(True)
    else:
        print(f"      [FAIL] 创建失败")
        results.append(False)
        return results

    # Get single
    print("  [3] Get Single Country")
    resp = curl_cmd("GET", f"/api/v1/countries/page?keyword={new_code}&pageNum=1&pageSize=1")
    if resp and resp.get("code") == 200:
        records = resp.get("data", {}).get("records", [])
        if records and records[0].get("code") == new_code:
            cid = records[0].get("id")
            resp2 = curl_cmd("GET", f"/api/v1/countries/{cid}")
            if resp2 and resp2.get("code") == 200:
                print(f"      [PASS] 获取成功 ID={cid}")
                results.append(True)
            else:
                print(f"      [FAIL] 获取失败")
                results.append(False)

            # Update
            print("  [4] Update Country")
            upd = dict(records[0])
            # Remove fields that might cause issues
            upd.pop("createdBy", None)
            upd.pop("createdAt", None)
            upd.pop("updatedBy", None)
            upd.pop("updatedAt", None)
            upd["name"] = f"Updated_{new_code}"
            resp3 = curl_cmd("POST", "/api/v1/countries/update", upd)
            if resp3 and resp3.get("code") == 200:
                print(f"      [PASS] 更新成功")
                results.append(True)
            else:
                print(f"      [FAIL] 更新失败")
                results.append(False)

            # Delete
            print("  [5] Delete Country")
            resp4 = curl_cmd("POST", f"/api/v1/countries/delete/{cid}")
            if resp4 and resp4.get("code") == 200:
                print(f"      [PASS] 删除成功")
                results.append(True)
            else:
                print(f"      [FAIL] 删除失败")
                results.append(False)
        else:
            print(f"      [FAIL] 记录未找到")
            results.append(False)
            results.append(False)
            results.append(False)
    else:
        print(f"      [FAIL] 查询失败")
        results.append(False)
        results.append(False)
        results.append(False)

    return results


# ========== Bank API Tests ==========
def test_bank():
    """测试Bank CRUD"""
    print("\n" + "-"*50)
    print("[TEST] Bank API")
    print("-"*50)
    results = []

    # Query
    print("  [1] Query Bank List")
    resp = curl_cmd("GET", "/api/v1/banks/page?pageNum=1&pageSize=5")
    if resp and resp.get("code") == 200:
        records = resp.get("data", {}).get("records", [])
        total = resp.get("data", {}).get("total", 0)
        print(f"      [PASS] 返回 {len(records)} 条记录, total={total}")
        results.append(True)
    else:
        print(f"      [FAIL] 查询失败")
        results.append(False)

    # Create
    print("  [2] Create Bank")
    new_code = f"BK{int(time.time()) % 100000}"
    new_bank = {"code": new_code, "name": f"TestBank_{new_code}", "swiftCode": "TESTUS33", "countryCode": "US", "status": "1"}
    resp = curl_cmd("POST", "/api/v1/banks", new_bank)
    if resp and resp.get("code") == 200:
        print(f"      [PASS] 创建成功: {new_code}")
        results.append(True)
    else:
        print(f"      [FAIL] 创建失败")
        results.append(False)
        return results

    # Get single
    print("  [3] Get Single Bank")
    resp = curl_cmd("GET", f"/api/v1/banks/page?keyword={new_code}&pageNum=1&pageSize=1")
    if resp and resp.get("code") == 200:
        records = resp.get("data", {}).get("records", [])
        if records and records[0].get("code") == new_code:
            bid = records[0].get("id")
            resp2 = curl_cmd("GET", f"/api/v1/banks/{bid}")
            if resp2 and resp2.get("code") == 200:
                print(f"      [PASS] 获取成功 ID={bid}")
                results.append(True)
            else:
                print(f"      [FAIL] 获取失败")
                results.append(False)

            # Update
            print("  [4] Update Bank")
            upd = dict(records[0])
            upd["name"] = f"Updated_{new_code}"
            resp3 = curl_cmd("POST", "/api/v1/banks/update", upd)  # Bank uses POST /update endpoint
            if resp3 and resp3.get("code") == 200:
                print(f"      [PASS] 更新成功")
                results.append(True)
            else:
                print(f"      [FAIL] 更新失败")
                results.append(False)

            # Delete
            print("  [5] Delete Bank")
            resp4 = curl_cmd("POST", f"/api/v1/banks/delete/{bid}")
            if resp4 and resp4.get("code") == 200:
                print(f"      [PASS] 删除成功")
                results.append(True)
            else:
                print(f"      [FAIL] 删除失败")
                results.append(False)
        else:
            print(f"      [FAIL] 记录未找到")
            results.extend([False, False, False])
    else:
        print(f"      [FAIL] 查询失败")
        results.extend([False, False, False])

    return results


# ========== Currency API Tests ==========
def test_currency():
    """测试Currency CRUD"""
    print("\n" + "-"*50)
    print("[TEST] Currency API")
    print("-"*50)
    results = []

    # Query
    print("  [1] Query Currency List")
    resp = curl_cmd("GET", "/api/v1/currencies/page?pageNum=1&pageSize=5")
    if resp and resp.get("code") == 200:
        records = resp.get("data", {}).get("records", [])
        total = resp.get("data", {}).get("total", 0)
        print(f"      [PASS] 返回 {len(records)} 条记录, total={total}")
        results.append(True)
    else:
        print(f"      [FAIL] 查询失败")
        results.append(False)

    # Create
    print("  [2] Create Currency")
    new_code = f"XT{int(time.time()) % 100000}"
    new_currency = {"code": new_code, "name": f"TestCurrency_{new_code}", "enName": "Test Currency", "status": "1", "precision": 2}
    resp = curl_cmd("POST", "/api/v1/currencies", new_currency)
    if resp and resp.get("code") == 200:
        print(f"      [PASS] 创建成功: {new_code}")
        results.append(True)
    else:
        print(f"      [FAIL] 创建失败")
        results.append(False)
        return results

    # Get single
    print("  [3] Get Single Currency")
    resp = curl_cmd("GET", f"/api/v1/currencies/page?keyword={new_code}&pageNum=1&pageSize=1")
    if resp and resp.get("code") == 200:
        records = resp.get("data", {}).get("records", [])
        if records and records[0].get("code") == new_code:
            cid = records[0].get("id")
            resp2 = curl_cmd("GET", f"/api/v1/currencies/{cid}")
            if resp2 and resp2.get("code") == 200:
                print(f"      [PASS] 获取成功 ID={cid}")
                results.append(True)
            else:
                print(f"      [FAIL] 获取失败")
                results.append(False)

            # Update
            print("  [4] Update Currency")
            upd = dict(records[0])
            upd["name"] = f"Updated_{new_code}"
            resp3 = curl_cmd("POST", "/api/v1/currencies/update", upd)
            if resp3 and resp3.get("code") == 200:
                print(f"      [PASS] 更新成功")
                results.append(True)
            else:
                print(f"      [FAIL] 更新失败")
                results.append(False)

            # Delete
            print("  [5] Delete Currency")
            resp4 = curl_cmd("POST", f"/api/v1/currencies/delete/{cid}")
            if resp4 and resp4.get("code") == 200:
                print(f"      [PASS] 删除成功")
                results.append(True)
            else:
                print(f"      [FAIL] 删除失败")
                results.append(False)
        else:
            print(f"      [FAIL] 记录未找到")
            results.extend([False, False, False])
    else:
        print(f"      [FAIL] 查询失败")
        results.extend([False, False, False])

    return results


# ========== Trader API Tests ==========
def test_trader():
    """测试Trader CRUD"""
    print("\n" + "-"*50)
    print("[TEST] Trader API")
    print("-"*50)
    results = []

    # Query
    print("  [1] Query Trader List")
    resp = curl_cmd("GET", "/api/v1/traders/page?pageNum=1&pageSize=5")
    if resp and resp.get("code") == 200:
        records = resp.get("data", {}).get("records", [])
        total = resp.get("data", {}).get("total", 0)
        print(f"      [PASS] 返回 {len(records)} 条记录, total={total}")
        results.append(True)
    else:
        print(f"      [FAIL] 查询失败")
        results.append(False)

    # Create
    print("  [2] Create Trader")
    new_code = f"TR{int(time.time()) % 100000}"
    new_trader = {"code": new_code, "name": f"TestTrader_{new_code}", "status": "1"}
    resp = curl_cmd("POST", "/api/v1/traders", new_trader)
    if resp and resp.get("code") == 200:
        print(f"      [PASS] 创建成功: {new_code}")
        results.append(True)
    else:
        print(f"      [FAIL] 创建失败")
        results.append(False)
        return results

    # Get single
    print("  [3] Get Single Trader")
    resp = curl_cmd("GET", f"/api/v1/traders/page?keyword={new_code}&pageNum=1&pageSize=1")
    if resp and resp.get("code") == 200:
        records = resp.get("data", {}).get("records", [])
        if records and records[0].get("code") == new_code:
            tid = records[0].get("id")
            resp2 = curl_cmd("GET", f"/api/v1/traders/{tid}")
            if resp2 and resp2.get("code") == 200:
                print(f"      [PASS] 获取成功 ID={tid}")
                results.append(True)
            else:
                print(f"      [FAIL] 获取失败")
                results.append(False)

            # Update
            print("  [4] Update Trader")
            upd = dict(records[0])
            upd.pop("createdBy", None)
            upd.pop("createdAt", None)
            upd.pop("updatedBy", None)
            upd.pop("updatedAt", None)
            upd.pop("version", None)
            upd.pop("deleted", None)
            upd["name"] = f"Updated_{new_code}"
            resp3 = curl_cmd("POST", "/api/v1/traders/update", upd)
            if resp3 and resp3.get("code") == 200:
                print(f"      [PASS] 更新成功")
                results.append(True)
            else:
                print(f"      [FAIL] 更新失败")
                results.append(False)

            # Delete
            print("  [5] Delete Trader")
            resp4 = curl_cmd("POST", f"/api/v1/traders/delete/{tid}")
            if resp4 and resp4.get("code") == 200:
                print(f"      [PASS] 删除成功")
                results.append(True)
            else:
                print(f"      [FAIL] 删除失败")
                results.append(False)
        else:
            print(f"      [FAIL] 记录未找到")
            results.extend([False, False, False])
    else:
        print(f"      [FAIL] 查询失败")
        results.extend([False, False, False])

    return results


# ========== Counterparty API Tests ==========
def test_counterparty():
    """测试Counterparty CRUD"""
    print("\n" + "-"*50)
    print("[TEST] Counterparty API")
    print("-"*50)
    results = []

    # Query
    print("  [1] Query Counterparty List")
    resp = curl_cmd("GET", "/api/v1/counterparties/page?pageNum=1&pageSize=5")
    if resp and resp.get("code") == 200:
        records = resp.get("data", {}).get("records", [])
        total = resp.get("data", {}).get("total", 0)
        print(f"      [PASS] 返回 {len(records)} 条记录, total={total}")
        results.append(True)
    else:
        print(f"      [FAIL] 查询失败")
        results.append(False)

    # Create
    print("  [2] Create Counterparty")
    new_code = f"CP{int(time.time()) % 100000}"
    new_cp = {"code": new_code, "name": f"TestCounterparty_{new_code}", "status": "1"}
    resp = curl_cmd("POST", "/api/v1/counterparties", new_cp)
    if resp and resp.get("code") == 200:
        print(f"      [PASS] 创建成功: {new_code}")
        results.append(True)
    else:
        print(f"      [FAIL] 创建失败")
        results.append(False)
        return results

    # Get single
    print("  [3] Get Single Counterparty")
    resp = curl_cmd("GET", f"/api/v1/counterparties/page?keyword={new_code}&pageNum=1&pageSize=1")
    if resp and resp.get("code") == 200:
        records = resp.get("data", {}).get("records", [])
        if records and records[0].get("code") == new_code:
            cid = records[0].get("id")
            resp2 = curl_cmd("GET", f"/api/v1/counterparties/{cid}")
            if resp2 and resp2.get("code") == 200:
                print(f"      [PASS] 获取成功 ID={cid}")
                results.append(True)
            else:
                print(f"      [FAIL] 获取失败")
                results.append(False)

            # Update
            print("  [4] Update Counterparty")
            upd = dict(records[0])
            upd.pop("createdBy", None)
            upd.pop("createdAt", None)
            upd.pop("updatedBy", None)
            upd.pop("updatedAt", None)
            upd["name"] = f"Updated_{new_code}"
            resp3 = curl_cmd("POST", "/api/v1/counterparties/update", upd)
            if resp3 and resp3.get("code") == 200:
                print(f"      [PASS] 更新成功")
                results.append(True)
            else:
                print(f"      [FAIL] 更新失败")
                results.append(False)

            # Delete
            print("  [5] Delete Counterparty")
            resp4 = curl_cmd("POST", f"/api/v1/counterparties/delete/{cid}")
            if resp4 and resp4.get("code") == 200:
                print(f"      [PASS] 删除成功")
                results.append(True)
            else:
                print(f"      [FAIL] 删除失败")
                results.append(False)
        else:
            print(f"      [FAIL] 记录未找到")
            results.extend([False, False, False])
    else:
        print(f"      [FAIL] 查询失败")
        results.extend([False, False, False])

    return results


# ========== Subsidiary API Tests ==========
def test_subsidiary():
    """测试Subsidiary CRUD"""
    print("\n" + "-"*50)
    print("[TEST] Subsidiary API")
    print("-"*50)
    results = []

    # Query
    print("  [1] Query Subsidiary List")
    resp = curl_cmd("GET", "/api/v1/subsidiaries/page?pageNum=1&pageSize=5")
    if resp and resp.get("code") == 200:
        records = resp.get("data", {}).get("records", [])
        total = resp.get("data", {}).get("total", 0)
        print(f"      [PASS] 返回 {len(records)} 条记录, total={total}")
        results.append(True)
    else:
        print(f"      [FAIL] 查询失败")
        results.append(False)

    # Create
    print("  [2] Create Subsidiary")
    new_code = f"SB{int(time.time()) % 100000}"
    new_sb = {"code": new_code, "name": f"TestSubsidiary_{new_code}", "status": "1"}
    resp = curl_cmd("POST", "/api/v1/subsidiaries", new_sb)
    if resp and resp.get("code") == 200:
        print(f"      [PASS] 创建成功: {new_code}")
        results.append(True)
    else:
        print(f"      [FAIL] 创建失败")
        results.append(False)
        return results

    # Get single
    print("  [3] Get Single Subsidiary")
    resp = curl_cmd("GET", f"/api/v1/subsidiaries/page?keyword={new_code}&pageNum=1&pageSize=1")
    if resp and resp.get("code") == 200:
        records = resp.get("data", {}).get("records", [])
        if records and records[0].get("code") == new_code:
            sid = records[0].get("id")
            resp2 = curl_cmd("GET", f"/api/v1/subsidiaries/{sid}")
            if resp2 and resp2.get("code") == 200:
                print(f"      [PASS] 获取成功 ID={sid}")
                results.append(True)
            else:
                print(f"      [FAIL] 获取失败")
                results.append(False)

            # Update
            print("  [4] Update Subsidiary")
            upd = {
                "id": records[0].get("id"),
                "code": records[0].get("code"),
                "name": f"Updated_{new_code}",
                "status": records[0].get("status", "1"),
                "enName": records[0].get("enName"),
                "parentCode": records[0].get("parentCode"),
                "businessUnitCode": records[0].get("businessUnitCode"),
                "legalPerson": records[0].get("legalPerson"),
                "registrationNo": records[0].get("registrationNo"),
                "taxNo": records[0].get("taxNo"),
                "address": records[0].get("address"),
                "phone": records[0].get("phone"),
                "email": records[0].get("email"),
                "remark": records[0].get("remark")
            }
            resp3 = curl_cmd("POST", "/api/v1/subsidiaries/update", upd)  # Subsidiary uses /update endpoint
            if resp3 and resp3.get("code") == 200:
                print(f"      [PASS] 更新成功")
                results.append(True)
            else:
                print(f"      [FAIL] 更新失败")
                results.append(False)

            # Delete
            print("  [5] Delete Subsidiary")
            resp4 = curl_cmd("POST", f"/api/v1/subsidiaries/delete/{sid}")  # Subsidiary uses /delete endpoint
            if resp4 and resp4.get("code") == 200:
                print(f"      [PASS] 删除成功")
                results.append(True)
            else:
                print(f"      [FAIL] 删除失败")
                results.append(False)
        else:
            print(f"      [FAIL] 记录未找到")
            results.extend([False, False, False])
    else:
        print(f"      [FAIL] 查询失败")
        results.extend([False, False, False])

    return results


# ========== CurrencyPair API Tests ==========
def test_currency_pair():
    """测试CurrencyPair CRUD"""
    print("\n" + "-"*50)
    print("[TEST] CurrencyPair API")
    print("-"*50)
    results = []

    # Query
    print("  [1] Query CurrencyPair List")
    resp = curl_cmd("GET", "/api/v1/currency-pairs/page?pageNum=1&pageSize=5")
    if resp and resp.get("code") == 200:
        records = resp.get("data", {}).get("records", [])
        total = resp.get("data", {}).get("total", 0)
        print(f"      [PASS] 返回 {len(records)} 条记录, total={total}")
        results.append(True)
    else:
        print(f"      [FAIL] 查询失败")
        results.append(False)

    # Create
    print("  [2] Create CurrencyPair")
    new_code = f"XTY{int(time.time()) % 100000}"
    new_cp = {"pairCode": new_code, "baseCurrency": "CNY", "quoteCurrency": "USD", "status": "1"}
    resp = curl_cmd("POST", "/api/v1/currency-pairs", new_cp)
    if resp and resp.get("code") == 200:
        print(f"      [PASS] 创建成功: {new_code}")
        results.append(True)
    else:
        print(f"      [FAIL] 创建失败")
        results.append(False)
        return results

    # Get single
    print("  [3] Get Single CurrencyPair")
    resp = curl_cmd("GET", f"/api/v1/currency-pairs/page?keyword={new_code}&pageNum=1&pageSize=1")
    if resp and resp.get("code") == 200:
        records = resp.get("data", {}).get("records", [])
        if records and records[0].get("pairCode") == new_code:
            cid = records[0].get("id")
            resp2 = curl_cmd("GET", f"/api/v1/currency-pairs/{cid}")
            if resp2 and resp2.get("code") == 200:
                print(f"      [PASS] 获取成功 ID={cid}")
                results.append(True)
            else:
                print(f"      [FAIL] 获取失败")
                results.append(False)

            # Update
            print("  [4] Update CurrencyPair")
            upd = dict(records[0])
            upd["name"] = f"Updated_{new_code}"
            resp3 = curl_cmd("POST", "/api/v1/currency-pairs/update", upd)
            if resp3 and resp3.get("code") == 200:
                print(f"      [PASS] 更新成功")
                results.append(True)
            else:
                print(f"      [FAIL] 更新失败")
                results.append(False)

            # Delete
            print("  [5] Delete CurrencyPair")
            resp4 = curl_cmd("POST", f"/api/v1/currency-pairs/delete/{cid}")
            if resp4 and resp4.get("code") == 200:
                print(f"      [PASS] 删除成功")
                results.append(True)
            else:
                print(f"      [FAIL] 删除失败")
                results.append(False)
        else:
            print(f"      [FAIL] 记录未找到")
            results.extend([False, False, False])
    else:
        print(f"      [FAIL] 查询失败")
        results.extend([False, False, False])

    return results


# ========== BusinessUnit API Tests ==========
def test_business_unit():
    """测试BusinessUnit CRUD"""
    print("\n" + "-"*50)
    print("[TEST] BusinessUnit API")
    print("-"*50)
    results = []

    # Query
    print("  [1] Query BusinessUnit List")
    resp = curl_cmd("GET", "/api/v1/management-entities/page?pageNum=1&pageSize=5")
    if resp and resp.get("code") == 200:
        records = resp.get("data", {}).get("records", [])
        total = resp.get("data", {}).get("total", 0)
        print(f"      [PASS] 返回 {len(records)} 条记录, total={total}")
        results.append(True)
    else:
        print(f"      [FAIL] 查询失败")
        results.append(False)

    # Create
    print("  [2] Create BusinessUnit")
    new_code = f"BU{int(time.time()) % 100000}"
    new_bu = {"code": new_code, "name": f"TestBusinessUnit_{new_code}", "status": "1"}
    resp = curl_cmd("POST", "/api/v1/management-entities", new_bu)
    if resp and resp.get("code") == 200:
        print(f"      [PASS] 创建成功: {new_code}")
        results.append(True)
    else:
        print(f"      [FAIL] 创建失败")
        results.append(False)
        return results

    # Get single
    print("  [3] Get Single BusinessUnit")
    resp = curl_cmd("GET", f"/api/v1/management-entities/page?keyword={new_code}&pageNum=1&pageSize=1")
    if resp and resp.get("code") == 200:
        records = resp.get("data", {}).get("records", [])
        if records and records[0].get("code") == new_code:
            bid = records[0].get("id")
            resp2 = curl_cmd("GET", f"/api/v1/management-entities/{bid}")
            if resp2 and resp2.get("code") == 200:
                print(f"      [PASS] 获取成功 ID={bid}")
                results.append(True)
            else:
                print(f"      [FAIL] 获取失败")
                results.append(False)

            # Update
            print("  [4] Update BusinessUnit")
            upd = dict(records[0])
            upd["name"] = f"Updated_{new_code}"
            resp3 = curl_cmd("POST", "/api/v1/management-entities/update", upd)
            if resp3 and resp3.get("code") == 200:
                print(f"      [PASS] 更新成功")
                results.append(True)
            else:
                print(f"      [FAIL] 更新失败")
                results.append(False)

            # Delete
            print("  [5] Delete BusinessUnit")
            resp4 = curl_cmd("POST", f"/api/v1/management-entities/delete/{bid}")
            if resp4 and resp4.get("code") == 200:
                print(f"      [PASS] 删除成功")
                results.append(True)
            else:
                print(f"      [FAIL] 删除失败")
                results.append(False)
        else:
            print(f"      [FAIL] 记录未找到")
            results.extend([False, False, False])
    else:
        print(f"      [FAIL] 查询失败")
        results.extend([False, False, False])

    return results


# ========== CounterpartyAccount API Tests ==========
def test_counterparty_account():
    """测试CounterpartyAccount CRUD"""
    print("\n" + "-"*50)
    print("[TEST] CounterpartyAccount API")
    print("-"*50)
    results = []

    # Query
    print("  [1] Query CounterpartyAccount List")
    resp = curl_cmd("GET", "/api/v1/counterparty-accounts/page?pageNum=1&pageSize=5")
    if resp and resp.get("code") == 200:
        records = resp.get("data", {}).get("records", [])
        total = resp.get("data", {}).get("total", 0)
        print(f"      [PASS] 返回 {len(records)} 条记录, total={total}")
        results.append(True)
    else:
        print(f"      [FAIL] 查询失败")
        results.append(False)

    # Create (note: requires counterpartyId)
    print("  [2] Create CounterpartyAccount")
    new_code = f"CA{int(time.time()) % 100000}"
    # Use valid counterpartyId (ID 5 exists from previous test data)
    new_ca = {"accountNo": new_code, "accountName": f"TestAccount_{new_code}", "counterpartyId": 5, "bankId": 5, "currency": "USD", "status": "1"}
    resp = curl_cmd("POST", "/api/v1/counterparty-accounts", new_ca)
    if resp and resp.get("code") == 200:
        print(f"      [PASS] 创建成功: {new_code}")
        results.append(True)
    else:
        print(f"      [FAIL] 创建失败 (可能需要有效的counterpartyId)")
        results.append(False)
        return results

    # Get single
    print("  [3] Get Single CounterpartyAccount")
    resp = curl_cmd("GET", f"/api/v1/counterparty-accounts/page?keyword={new_code}&pageNum=1&pageSize=1")
    if resp and resp.get("code") == 200:
        records = resp.get("data", {}).get("records", [])
        if records and records[0].get("accountNo") == new_code:
            cid = records[0].get("id")
            resp2 = curl_cmd("GET", f"/api/v1/counterparty-accounts/{cid}")
            if resp2 and resp2.get("code") == 200:
                print(f"      [PASS] 获取成功 ID={cid}")
                results.append(True)
            else:
                print(f"      [FAIL] 获取失败")
                results.append(False)

            # Update
            print("  [4] Update CounterpartyAccount")
            upd = dict(records[0])
            upd["name"] = f"Updated_{new_code}"
            resp3 = curl_cmd("POST", "/api/v1/counterparty-accounts", upd)
            if resp3 and resp3.get("code") == 200:
                print(f"      [PASS] 更新成功")
                results.append(True)
            else:
                print(f"      [FAIL] 更新失败")
                results.append(False)

            # Delete
            print("  [5] Delete CounterpartyAccount")
            resp4 = curl_cmd("POST", f"/api/v1/counterparty-accounts/delete/{cid}")
            if resp4 and resp4.get("code") == 200:
                print(f"      [PASS] 删除成功")
                results.append(True)
            else:
                print(f"      [FAIL] 删除失败")
                results.append(False)
        else:
            print(f"      [FAIL] 记录未找到")
            results.extend([False, False, False])
    else:
        print(f"      [FAIL] 查询失败")
        results.extend([False, False, False])

    return results


# ========== Holiday API Tests ==========
def test_holiday():
    """测试Holiday CRUD"""
    print("\n" + "-"*50)
    print("[TEST] Holiday API")
    print("-"*50)
    results = []

    # Query
    print("  [1] Query Holiday List")
    resp = curl_cmd("GET", "/api/v1/holidays/page?pageNum=1&pageSize=5")
    if resp and resp.get("code") == 200:
        records = resp.get("data", {}).get("records", [])
        total = resp.get("data", {}).get("total", 0)
        print(f"      [PASS] 返回 {len(records)} 条记录, total={total}")
        results.append(True)
    else:
        print(f"      [FAIL] 查询失败")
        results.append(False)

    # Create (note: requires countryCode)
    print("  [2] Create Holiday")
    new_name = f"TestHoliday_{int(time.time()) % 100000}"
    # Holiday uses countryCode (e.g. "CN", "US") not countryId
    new_holiday = {"holidayDate": "2026-12-25", "name": new_name, "countryCode": "CN", "year": 2026}
    resp = curl_cmd("POST", "/api/v1/holidays", new_holiday)
    if resp and resp.get("code") == 200:
        print(f"      [PASS] 创建成功: {new_name}")
        results.append(True)
    else:
        print(f"      [FAIL] 创建失败 (可能需要有效的countryId)")
        results.append(False)
        return results

    # Get single
    print("  [3] Get Single Holiday")
    resp = curl_cmd("GET", f"/api/v1/holidays/page?countryCode=CN&year=2026&pageNum=1&pageSize=1")
    if resp and resp.get("code") == 200:
        records = resp.get("data", {}).get("records", [])
        if records and records[0].get("name") == new_name:
            hid = records[0].get("id")
            resp2 = curl_cmd("GET", f"/api/v1/holidays/{hid}")
            if resp2 and resp2.get("code") == 200:
                print(f"      [PASS] 获取成功 ID={hid}")
                results.append(True)
            else:
                print(f"      [FAIL] 获取失败")
                results.append(False)

            # Update
            print("  [4] Update Holiday")
            upd = {
                "id": records[0].get("id"),
                "holidayDate": "2026-12-26",
                "name": f"Updated_{new_name}",
                "countryCode": "CN",
                "year": 2026
            }
            resp3 = curl_cmd("POST", "/api/v1/holidays/update", upd)
            if resp3 and resp3.get("code") == 200:
                print(f"      [PASS] 更新成功")
                results.append(True)
            else:
                print(f"      [FAIL] 更新失败")
                results.append(False)

            # Delete
            print("  [5] Delete Holiday")
            resp4 = curl_cmd("POST", f"/api/v1/holidays/delete/{hid}")
            if resp4 and resp4.get("code") == 200:
                print(f"      [PASS] 删除成功")
                results.append(True)
            else:
                print(f"      [FAIL] 删除失败")
                results.append(False)
        else:
            print(f"      [FAIL] 记录未找到")
            results.extend([False, False, False])
    else:
        print(f"      [FAIL] 查询失败")
        results.extend([False, False, False])

    return results


# ========== Main ==========
def main():
    print("\n" + "="*60)
    print("# Open-TMS 基础数据模块 API自动化测试")
    print("="*60)

    # Health Check
    if not check_api_health():
        print("\n[ERROR] Backend is not available. Please start backend first.")
        print("  java -jar basedata/target/opentms-basedata-1.0.0-SNAPSHOT.jar")
        return 1

    all_results = []

    # Run all tests
    all_results.extend(test_country())
    all_results.extend(test_bank())
    all_results.extend(test_currency())
    all_results.extend(test_trader())
    all_results.extend(test_counterparty())
    all_results.extend(test_subsidiary())
    all_results.extend(test_currency_pair())
    all_results.extend(test_business_unit())
    all_results.extend(test_counterparty_account())
    all_results.extend(test_holiday())

    # Summary
    print("\n" + "="*60)
    print("# 测试汇总")
    print("="*60)
    passed = sum(all_results)
    total = len(all_results)
    print(f"  Total: {total}")
    print(f"  Passed: {passed}")
    print(f"  Failed: {total - passed}")
    print(f"  Pass Rate: {passed*100//total}%")
    print("="*60)

    return 0 if passed == total else 1


if __name__ == "__main__":
    sys.exit(main())
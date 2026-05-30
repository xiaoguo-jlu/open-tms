#!/usr/bin/env python3
"""
Open-TMS Country模块 API自动化测试
"""

import subprocess
import json
import time
import sys

BASE_URL = "http://localhost:8081/opentms/basedata"


def curl_cmd(method, path, data=None):
    """执行curl命令并返回JSON响应"""
    cmd = ["curl", "-s", "-X", method, f"{BASE_URL}{path}"]
    if data:
        cmd.extend(["-H", "Content-Type: application/json", "-d", json.dumps(data)])
    try:
        result = subprocess.run(cmd, capture_output=True, text=True, timeout=10, encoding='utf-8', errors='replace')
        if result.stdout:
            return json.loads(result.stdout)
    except:
        pass
    return None


def main():
    print("\n" + "="*60)
    print("# Open-TMS Country API Test")
    print("="*60)

    results = []

    # Test 1: Query
    print("\n[TEST 1] Query Country List")
    resp = curl_cmd("GET", "/api/v1/countries/page?pageNum=1&pageSize=5")
    if resp and resp.get("code") == 200:
        records = resp.get("data", {}).get("records", [])
        total = resp.get("data", {}).get("total", 0)
        print(f"  [PASS] Returned {len(records)} records, total={total}")
        results.append(True)
    else:
        print(f"  [FAIL] Query failed")
        results.append(False)

    # Test 2: Create
    print("\n[TEST 2] Create Country")
    new_code = f"AT{int(time.time()) % 100000}"
    new_country = {"code": new_code, "name": f"Test_{new_code}", "status": "1", "timezone": "Asia/Shanghai"}
    resp = curl_cmd("POST", "/api/v1/countries", new_country)
    if resp and resp.get("code") == 200:
        print(f"  [PASS] Created: {new_code}")
        results.append(True)
    else:
        print(f"  [FAIL] Create failed")
        results.append(False)

    # Test 3: Get single by ID (search for the record we just created)
    print("\n[TEST 3] Get Single Country")
    resp = curl_cmd("GET", f"/api/v1/countries/page?keyword={new_code}&pageNum=1&pageSize=1")
    if resp and resp.get("code") == 200:
        records = resp.get("data", {}).get("records", [])
        if records and records[0].get("code") == new_code:
            cid = records[0].get("id")
            resp2 = curl_cmd("GET", f"/api/v1/countries/{cid}")
            if resp2 and resp2.get("code") == 200:
                print(f"  [PASS] Got country ID={cid}")
                results.append(True)
            else:
                print(f"  [FAIL] GetSingle failed")
                results.append(False)

            # Test 4: Update
            print("\n[TEST 4] Update Country")
            upd = dict(records[0])
            upd["name"] = f"Updated_{new_code}"
            resp3 = curl_cmd("PUT", "/api/v1/countries", upd)
            if resp3 and resp3.get("code") == 200:
                print(f"  [PASS] Updated successfully")
                results.append(True)
            else:
                print(f"  [FAIL] Update failed")
                results.append(False)

            # Test 5: Delete
            print("\n[TEST 5] Delete Country")
            resp4 = curl_cmd("DELETE", f"/api/v1/countries/{cid}")
            if resp4 and resp4.get("code") == 200:
                print(f"  [PASS] Deleted successfully")
                results.append(True)
            else:
                print(f"  [FAIL] Delete failed")
                results.append(False)
        else:
            print(f"  [FAIL] Record not found after create")
            results.append(False)
            results.append(False)
            results.append(False)
    else:
        print(f"  [FAIL] Search failed")
        results.append(False)
        results.append(False)
        results.append(False)

    # Summary
    print("\n" + "="*60)
    passed = sum(results)
    total = len(results)
    print(f"RESULT: {passed}/{total} passed")
    if passed == total:
        print("STATUS: ALL TESTS PASSED")
    else:
        print("STATUS: SOME TESTS FAILED")
    print("="*60)

    return 0 if passed == total else 1


if __name__ == "__main__":
    sys.exit(main())
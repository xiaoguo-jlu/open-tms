#!/usr/bin/env python3
"""
基础数据模块 POST API 测试脚本
使用urllib代替requests（内置库）
"""

import urllib.request
import urllib.error
import json
import sys

BASE_URL = "http://localhost:8081/api/v1"

def test_post(path, data, desc):
    """测试POST接口"""
    try:
        req = urllib.request.Request(
            f"{BASE_URL}{path}",
            data=json.dumps(data).encode('utf-8'),
            headers={'Content-Type': 'application/json'},
            method='POST'
        )
        response = urllib.request.urlopen(req, timeout=10)
        if response.status == 200:
            print(f"[OK] {desc} - {path}: {response.status} OK")
            return True
    except urllib.error.HTTPError as e:
        print(f"[FAIL] {desc} - {path}: {e.code}")
    except urllib.error.URLError as e:
        print(f"[FAIL] {desc} - {path}: ERROR - {e.reason}")
    except Exception as e:
        print(f"[FAIL] {desc} - {path}: ERROR - {e}")
    return False

def main():
    print("=== Basedata POST API Test ===\n")

    tests = [
        ("/banks", {"code": "BANK99", "name": "TestBank", "status": "1"}, "Bank"),
        ("/business-units", {"code": "BU99", "name": "TestUnit", "status": "1"}, "BusinessUnit"),
        ("/traders", {"code": "T99", "name": "Test", "status": "1"}, "Trader"),
        ("/countries", {"code": "YY", "name": "Test", "status": "1"}, "Country"),
        ("/counterparties", {"code": "CP99", "name": "TestCP", "status": "1"}, "Counterparty"),
        ("/currencies", {"code": "TST", "name": "Test", "status": "1"}, "Currency"),
        ("/counterparty-accounts", {"accountNo": "ACC99", "counterpartyId": 1, "bankId": 1, "currency": "USD", "status": "1"}, "CounterpartyAccount"),
    ]

    results = []
    for path, data, desc in tests:
        results.append(test_post(path, data, desc))

    passed = sum(results)
    total = len(results)
    print(f"\n=== Summary: {passed}/{total} passed ===")
    return 0 if all(results) else 1

if __name__ == "__main__":
    sys.exit(main())
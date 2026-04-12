#!/usr/bin/env python3
"""
基础数据模块 POST API 测试脚本
Usage: python test/scripts/basedata/test_all_post.py
"""

import requests
import json
import sys

BASE_URL = "http://localhost:8081/api/v1"

def test_post(path, data, desc):
    """测试POST接口"""
    try:
        response = requests.post(
            f"{BASE_URL}{path}",
            json=data,
            headers={"Content-Type": "application/json"},
            timeout=10
        )
        if response.status_code == 200:
            print(f"[✓] {desc} - {path}: {response.status_code} OK")
            return True
        else:
            print(f"[✗] {desc} - {path}: {response.status_code} FAILED")
            return False
    except requests.exceptions.RequestException as e:
        print(f"[✗] {desc} - {path}: ERROR - {e}")
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

    print(f"\n=== Summary: {sum(results)}/{len(results)} passed ===")
    return 0 if all(results) else 1

if __name__ == "__main__":
    sys.exit(main())
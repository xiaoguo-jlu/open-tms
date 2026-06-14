#!/usr/bin/env python3
"""
Open-TMS Instrument 模块 API自动化测试
测试 Instrument 的 CRUD 操作

增强功能:
- 详细的错误日志和响应内容
- API响应时间监控
- 响应数据正确性验证
"""

import subprocess
import json
import time
import sys
import urllib.request
import urllib.error
from datetime import datetime

BACKEND_URL = "http://localhost:8081/opentms/basedata"

# 慢请求阈值（毫秒）
SLOW_REQUEST_THRESHOLD_MS = 1000

# API端点配置
API_ENDPOINT = "/api/v1/instruments"


def format_timestamp():
    """返回当前时间戳字符串"""
    return datetime.now().strftime("%Y-%m-%d %H:%M:%S.%f")[:-3]


def curl_cmd_detailed(method, path, data=None):
    """执行curl命令并返回详细响应，包含响应时间监控"""
    start_time = time.time()
    cmd = ["curl", "-s", "-X", method, "-w", "\n%{http_code}", f"{BACKEND_URL}{path}"]
    if data:
        cmd.extend(["-H", "Content-Type: application/json", "-d", json.dumps(data)])

    try:
        result = subprocess.run(cmd, capture_output=True, text=True, timeout=30, encoding='utf-8', errors='replace')
        elapsed_ms = int((time.time() - start_time) * 1000)

        if not result.stdout:
            print(f"  [ERROR] 空响应 (耗时: {elapsed_ms}ms)")
            return None, elapsed_ms

        # 分离HTTP状态码和响应体
        lines = result.stdout.strip().split('\n')
        http_code = lines[-1] if len(lines) > 1 else "000"
        response_body = '\n'.join(lines[:-1]) if len(lines) > 1 else lines[0]

        # 解析JSON响应
        try:
            resp = json.loads(response_body) if response_body else {}
        except json.JSONDecodeError as e:
            print(f"  [ERROR] JSON解析失败: {e}, 响应: {response_body[:200]}")
            return None, elapsed_ms

        # 慢请求警告
        if elapsed_ms > SLOW_REQUEST_THRESHOLD_MS:
            print(f"  [WARN] 慢请求 ({elapsed_ms}ms > {SLOW_REQUEST_THRESHOLD_MS}ms): {method} {path}")

        # 异常响应详细日志
        if resp.get("code") != 200:
            print(f"  [WARN] 响应异常: code={resp.get('code')}, message={resp.get('message')}")
            print(f"  [DEBUG] HTTP状态码: {http_code}")
            print(f"  [DEBUG] 完整响应: {json.dumps(resp, indent=2)[:1000]}")

        return resp, elapsed_ms

    except subprocess.TimeoutExpired:
        elapsed_ms = int((time.time() - start_time) * 1000)
        print(f"  [ERROR] 请求超时 (>{elapsed_ms}ms): {method} {path}")
        return None, elapsed_ms
    except Exception as e:
        elapsed_ms = int((time.time() - start_time) * 1000)
        print(f"  [ERROR] 请求异常: {e} (耗时: {elapsed_ms}ms)")
        return None, elapsed_ms


def curl_cmd(method, path, data=None):
    """执行curl命令并返回JSON响应（兼容旧接口）"""
    resp, _ = curl_cmd_detailed(method, path, data)
    return resp


def validate_response_data(resp, expected_fields=None, entity_name="Entity"):
    """验证响应数据结构的正确性"""
    if not resp:
        return False, "响应为空"

    if "code" not in resp:
        return False, "响应缺少code字段"

    if resp.get("code") == 200:
        data = resp.get("data")
        if data is None:
            return False, "data字段为null"

        if expected_fields and isinstance(data, dict):
            missing_fields = [f for f in expected_fields if f not in data]
            if missing_fields:
                print(f"  [WARN] {entity_name} 缺少字段: {missing_fields}")

        return True, "OK"

    return False, resp.get("message", "未知错误")


def validate_record_data(record, required_fields, entity_name="Entity"):
    """验证单条记录数据的完整性"""
    if not record or not isinstance(record, dict):
        return False, "记录为空或格式错误"

    missing = [f for f in required_fields if f not in record or record[f] is None]
    if missing:
        print(f"  [WARN] {entity_name} 记录缺少必要字段: {missing}")
        return False, f"缺少字段: {missing}"

    return True, "OK"


def check_api_health():
    """检查API是否可用"""
    print("\n" + "="*60)
    print("[Health Check] Instrument API 连接检查...")
    print("="*60)

    try:
        resp = urllib.request.urlopen(f"{BACKEND_URL}{API_ENDPOINT}/page?pageNum=1&pageSize=1", timeout=5)
        data = json.loads(resp.read().decode())
        is_success = data.get('code') == 200 or (isinstance(data, dict) and 'records' in data)
        if is_success:
            print("      [PASS] Instrument API 正常")
            return True
        else:
            print(f"      [FAIL] 响应异常: code={data.get('code')}")
            return False
    except Exception as e:
        print(f"      [FAIL] 连接失败: {e}")
        return False


def test_instrument_crud():
    """测试 Instrument CRUD"""
    print("\n" + "-"*50)
    print("[TEST] Instrument API CRUD")
    print("-"*50)
    results = []

    # 1. Query - 分页查询
    print("  [1] Query Instrument List (Pagination)")
    resp, elapsed = curl_cmd_detailed("GET", f"{API_ENDPOINT}/page?pageNum=1&pageSize=5")
    is_valid, msg = validate_response_data(resp, expected_fields=["records", "total"], entity_name="Instrument")
    if is_valid:
        records = resp.get("data", {}).get("records", [])
        total = resp.get("data", {}).get("total", 0)
        print(f"      [PASS] 返回 {len(records)} 条记录, total={total} (耗时: {elapsed}ms)")

        if records:
            valid, _ = validate_record_data(records[0], ["id", "instrumentCode", "instrumentName"], "Instrument")
            if valid:
                print(f"      [DEBUG] 示例记录: id={records[0].get('id')}, code={records[0].get('instrumentCode')}")
        results.append(True)
    else:
        print(f"      [FAIL] 查询失败: {msg}")
        results.append(False)
        return results

    # 2. Create - 创建
    print("  [2] Create Instrument")
    new_code = f"INS{int(time.time()) % 100000}"
    new_instrument = {
        "instrumentCode": new_code,
        "instrumentName": f"TestInstrument_{new_code}",
        "enName": f"Test Instrument {new_code}",
        "instrumentType": "STOCK",
        "underlying": "TestUnderlying",
        "exchange": "TEST_EXCHANGE",
        "currency": "USD",
        "status": "1",
        "remark": "Test Remark"
    }
    resp, elapsed = curl_cmd_detailed("POST", API_ENDPOINT, new_instrument)
    if resp and resp.get("code") == 200:
        print(f"      [PASS] 创建成功: {new_code} (耗时: {elapsed}ms)")
        results.append(True)
    else:
        print(f"      [FAIL] 创建失败: {resp.get('message') if resp else 'None'}")
        print(f"      [DEBUG] 请求数据: {json.dumps(new_instrument)}")
        results.append(False)
        return results

    # 3. Get Single - 获取单个
    print("  [3] Get Single Instrument")
    resp, elapsed = curl_cmd_detailed("GET", f"{API_ENDPOINT}/page?keyword={new_code}&pageNum=1&pageSize=1")
    if resp and resp.get("code") == 200:
        records = resp.get("data", {}).get("records", [])
        if records and records[0].get("instrumentCode") == new_code:
            iid = records[0].get("id")
            resp2, elapsed2 = curl_cmd_detailed("GET", f"{API_ENDPOINT}/{iid}")
            if resp2 and resp2.get("code") == 200:
                print(f"      [PASS] 获取成功 ID={iid} (耗时: {elapsed2}ms)")
                results.append(True)
            else:
                print(f"      [FAIL] 获取失败")
                results.append(False)

            # 4. Update - 更新
            print("  [4] Update Instrument")
            upd = dict(records[0])
            upd.pop("createdBy", None)
            upd.pop("createdAt", None)
            upd.pop("updatedBy", None)
            upd.pop("updatedAt", None)
            upd["instrumentName"] = f"Updated_{new_code}"
            resp3, elapsed3 = curl_cmd_detailed("POST", f"{API_ENDPOINT}/update", upd)
            if resp3 and resp3.get("code") == 200:
                print(f"      [PASS] 更新成功 (耗时: {elapsed3}ms)")
                results.append(True)
            else:
                print(f"      [FAIL] 更新失败: {resp3.get('message') if resp3 else 'None'}")
                results.append(False)

            # 5. Delete - 删除
            print("  [5] Delete Instrument")
            resp4, elapsed4 = curl_cmd_detailed("POST", f"{API_ENDPOINT}/delete/{iid}")
            if resp4 and resp4.get("code") == 200:
                print(f"      [PASS] 删除成功 (耗时: {elapsed4}ms)")
                results.append(True)
            else:
                print(f"      [FAIL] 删除失败: {resp4.get('message') if resp4 else 'None'}")
                results.append(False)
        else:
            print(f"      [FAIL] 记录未找到, keyword={new_code}")
            results.extend([False, False, False])
    else:
        print(f"      [FAIL] 查询失败")
        results.extend([False, False, False])

    return results


def main():
    print("\n" + "="*60)
    print("# Open-TMS Instrument 模块 API自动化测试")
    print(f"# 测试时间: {format_timestamp()}")
    print("="*60)

    # Health Check
    if not check_api_health():
        print("\n[ERROR] Backend is not available. Please start backend first.")
        print("  java -jar basedata/target/opentms-basedata-1.0.0-SNAPSHOT.jar")
        return 1

    all_results = []
    all_results.extend(test_instrument_crud())

    # Summary
    print("\n" + "="*60)
    print("# 测试汇总")
    print("="*60)
    passed = sum(all_results)
    total = len(all_results)
    print(f"  Total: {total}")
    print(f"  Passed: {passed}")
    print(f"  Failed: {total - passed}")
    print(f"  Pass Rate: {passed*100//total if total > 0 else 0}%")
    print("="*60)

    return 0 if passed == total else 1


if __name__ == "__main__":
    sys.exit(main())
#!/usr/bin/env python3
"""
Open-TMS AC交易(Deal) API自动化测试
测试AC交易的完整生命周期: 创建 -> 查询 -> 提交 -> 审批 -> 执行
"""

import subprocess
import json
import time
import sys
import urllib.request
import urllib.error
import os

BACKEND_URL = "http://localhost:8082"
BASEDATA_URL = "http://localhost:8081/opentms/basedata"

jar_path = 'E:\\code-project\\open-tms\\open-tms\\dealing\\target\\dealing-1.0.0-SNAPSHOT.jar'


def curl_cmd(method, path, data=None, base_url=BACKEND_URL):
    """执行HTTP请求并返回JSON响应"""
    url = f"{base_url}{path}"
    try:
        if method == "GET":
            req = urllib.request.Request(url)
        else:  # POST
            req = urllib.request.Request(url, data=json.dumps(data).encode('utf-8'), method='POST')
            req.add_header('Content-Type', 'application/json')

        with urllib.request.urlopen(req, timeout=30) as resp:
            return json.loads(resp.read().decode('utf-8'))
    except urllib.error.HTTPError as e:
        error_body = e.read().decode('utf-8', errors='replace')
        try:
            return json.loads(error_body)
        except:
            return {"code": e.code, "error": error_body}
    except Exception as e:
        print(f"  [ERROR] {e}")
    return None


def check_backend_health(url, name):
    """检查后端服务是否可用"""
    try:
        resp = urllib.request.urlopen(f"{url}/api/v1/countries/page?pageNum=1&pageSize=1", timeout=5)
        data = json.loads(resp.read().decode())
        if data.get('code') == 200:
            return True
    except Exception as e:
        print(f"[WARN] {name} not available: {e}")
    return False


def kill_port(port):
    """Kill any existing process on port"""
    try:
        result = subprocess.run(['netstat', '-ano'], capture_output=True, text=True)
        for line in result.stdout.splitlines():
            if f':{port}' in line and 'LISTENING' in line:
                parts = line.split()
                if parts and parts[-1].isdigit():
                    pid = int(parts[-1])
                    try:
                        subprocess.run(['taskkill', '/F', '/PID', str(pid)], capture_output=True)
                        print(f'  Killed process {pid} on port {port}')
                    except:
                        pass
    except:
        pass
    time.sleep(2)


def start_backend(port, jar_path, name):
    """启动后端服务"""
    print(f'\n[START] Starting {name} on port {port}...')
    kill_port(port)

    proc = subprocess.Popen(['java', '-jar', jar_path], stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True)

    started = False
    for line in proc.stdout:
        print(f"  {line.strip()}")
        if 'Started DealingApplication' in line or 'DealingApplication' in line and 'started' in line.lower():
            started = True
            print(f'[OK] {name} started successfully!')
            break

    if started:
        time.sleep(3)
        return proc
    else:
        print(f'[FAIL] {name} failed to start')
        proc.terminate()
        return None


def get_existing_data(entity_url, keyword_field, keyword_value, page_size=5):
    """查询已存在的数据用于测试"""
    resp = curl_cmd("GET", f"/{entity_url}/page?pageNum=1&pageSize={page_size}", base_url=BASEDATA_URL)
    if resp and resp.get("code") == 200:
        records = resp.get("data", {}).get("records", [])
        for record in records:
            if keyword_value and record.get(keyword_field) == keyword_value:
                return record
        # Return first record if no exact match
        if records:
            return records[0]
    return None


def prepare_test_data():
    """准备测试所需的基础数据"""
    print("\n" + "="*60)
    print("#准备测试数据")
    print("="*60)

    test_data = {
        "businessUnit": None,
        "counterparty": None,
        "counterpartyAccount": None,
        "trader": None,
        "bankAccount": None,
        "instrument": None
    }

    # 1. Get businessUnit (管理单元)
    print("  [1] 查询 BusinessUnit...")
    resp = curl_cmd("GET", "/api/v1/management-entities/page?pageNum=1&pageSize=5", base_url=BASEDATA_URL)
    if resp and resp.get("code") == 200:
        records = resp.get("data", {}).get("records", [])
        if records:
            test_data["businessUnit"] = records[0].get("code")
            print(f"      [OK] businessUnit: {test_data['businessUnit']}")
        else:
            print(f"      [WARN] No businessUnit found")
    else:
        print(f"      [FAIL] Failed to query businessUnit")

    # 2. Get counterparty
    print("  [2] 查询 Counterparty...")
    resp = curl_cmd("GET", "/api/v1/counterparties/page?pageNum=1&pageSize=5", base_url=BASEDATA_URL)
    if resp and resp.get("code") == 200:
        records = resp.get("data", {}).get("records", [])
        if records:
            test_data["counterparty"] = records[0]
            print(f"      [OK] counterparty: id={records[0].get('id')}, code={records[0].get('code')}")
        else:
            print(f"      [WARN] No counterparty found")
    else:
        print(f"      [FAIL] Failed to query counterparty")

    # 3. Get counterpartyAccount
    print("  [3] 查询 CounterpartyAccount...")
    resp = curl_cmd("GET", "/api/v1/counterparty-accounts/page?pageNum=1&pageSize=5", base_url=BASEDATA_URL)
    if resp and resp.get("code") == 200:
        records = resp.get("data", {}).get("records", [])
        if records:
            test_data["counterpartyAccount"] = records[0]
            print(f"      [OK] counterpartyAccount: id={records[0].get('id')}")
        else:
            print(f"      [WARN] No counterpartyAccount found")
    else:
        print(f"      [FAIL] Failed to query counterpartyAccount")

    # 4. Get trader
    print("  [4] 查询 Trader...")
    resp = curl_cmd("GET", "/api/v1/traders/page?pageNum=1&pageSize=5", base_url=BASEDATA_URL)
    if resp and resp.get("code") == 200:
        records = resp.get("data", {}).get("records", [])
        if records:
            test_data["trader"] = records[0]
            print(f"      [OK] trader: id={records[0].get('id')}")
        else:
            print(f"      [WARN] No trader found")
    else:
        print(f"      [FAIL] Failed to query trader")

    # 5. Get bankAccount (from bankAccounts endpoint)
    print("  [5] 查询 BankAccount...")
    resp = curl_cmd("GET", "/api/v1/bank-accounts/page?pageNum=1&pageSize=5", base_url=BASEDATA_URL)
    if resp and resp.get("code") == 200:
        records = resp.get("data", {}).get("records", [])
        if records:
            test_data["bankAccount"] = records[0]
            print(f"      [OK] bankAccount: id={records[0].get('id')}")
        else:
            print(f"      [WARN] No bankAccount found")
    else:
        print(f"      [FAIL] Failed to query bankAccount")

    # 6. Get instrument (if exists)
    print("  [6] 查询 Instrument...")
    resp = curl_cmd("GET", "/api/v1/instruments/page?pageNum=1&pageSize=5", base_url=BASEDATA_URL)
    if resp and resp.get("code") == 200:
        records = resp.get("data", {}).get("records", [])
        if records:
            test_data["instrument"] = records[0]
            print(f"      [OK] instrument: id={records[0].get('id')}")
        else:
            print(f"      [WARN] No instrument found")
    else:
        print(f"      [FAIL] Failed to query instrument")

    return test_data


# ========== P0 Test Cases ==========

def test_create_deal(test_data):
    """P0-1: 创建交易"""
    print("\n" + "-"*50)
    print("[P0-1] 创建交易 - POST /api/v1/dealing/deals")
    print("-"*50)

    deal = {
        "dealType": "AC",
        "businessUnit": test_data.get("businessUnit") or "BU001",
        "counterpartyId": (test_data.get("counterparty") or {}).get("id") or 1,
        "instrumentId": (test_data.get("instrument") or {}).get("id") or 1,  # Required field
        "traderId": (test_data.get("trader") or {}).get("id") or 1000,
        "direction": "BUY",
        "amount": 100000.00,
        "currency": "USD",
        "dealDate": "2026-06-01",
        "valueDate": "2026-06-03",
        "status": "New",
        "description": "Test AC Deal",
        "remark": "API Test",
        "operator": "test_user",
        "bankAccountId": (test_data.get("bankAccount") or {}).get("id") or 1,
        "counterpartyAccountId": (test_data.get("counterpartyAccount") or {}).get("id") or 1,
        "paymentMethod": "SWIFT"
    }

    print(f"  Request: {json.dumps(deal, indent=2)}")
    resp = curl_cmd("POST", "/api/v1/dealing/deals", deal)

    result = {"name": "创建交易", "status": "failed", "response": None, "deal_id": None, "deal_number": None}

    if resp and resp.get("code") == 200:
        result["status"] = "passed"
        print(f"  [PASS] 创建成功")

        # Query to get the created deal
        time.sleep(1)
        resp2 = curl_cmd("GET", "/api/v1/dealing/deals/page?pageNum=1&pageSize=10")
        if resp2 and resp2.get("code") == 200:
            records = resp2.get("data", {}).get("records", [])
            if records:
                result["deal_id"] = records[0].get("id")
                result["deal_number"] = records[0].get("dealNumber")
                print(f"  [INFO] Created deal: id={result['deal_id']}, number={result['deal_number']}")
    elif resp and resp.get("code") == 500:
        # Check for backend bug: BeanUtils.copyProperties overwrites dealNumber to null
        error_msg = resp.get("message", "")
        if "deal_number" in error_msg.lower() or "null value" in error_msg.lower():
            print(f"  [BUG DETECTED] Backend bug: BeanUtils.copyProperties in saveDeal overwrites dealNumber to null")
            result["status"] = "failed"
            result["bug"] = True
        else:
            print(f"  [FAIL] 创建失败: {resp}")
    else:
        print(f"  [FAIL] 创建失败: {resp}")

    result["response"] = resp
    return result


def test_query_deals_page():
    """P0-2: 查询交易列表"""
    print("\n" + "-"*50)
    print("[P0-2] 查询交易列表 - GET /api/v1/dealing/deals/page")
    print("-"*50)

    resp = curl_cmd("GET", "/api/v1/dealing/deals/page?pageNum=1&pageSize=10")

    result = {"name": "查询交易列表", "status": "failed", "response": None}

    if resp and resp.get("code") == 200:
        records = resp.get("data", {}).get("records", [])
        total = resp.get("data", {}).get("total", 0)
        print(f"  [PASS] 返回 {len(records)} 条记录, total={total}")
        result["status"] = "passed"
        result["data"] = {"total": total, "count": len(records)}
    else:
        print(f"  [FAIL] 查询失败: {resp}")

    result["response"] = resp
    return result


def test_get_deal_by_id(deal_id):
    """P0-3: 获取交易详情"""
    print("\n" + "-"*50)
    print(f"[P0-3] 获取交易详情 - GET /api/v1/dealing/deals/{deal_id}")
    print("-"*50)

    if not deal_id:
        print(f"  [SKIP] No deal_id provided")
        return {"name": "获取交易详情", "status": "skipped", "response": None}

    resp = curl_cmd("GET", f"/api/v1/dealing/deals/{deal_id}")

    result = {"name": "获取交易详情", "status": "failed", "response": None}

    if resp and resp.get("code") == 200:
        deal = resp.get("data", {})
        print(f"  [PASS] dealNumber={deal.get('dealNumber')}, status={deal.get('status')}")
        result["status"] = "passed"
        result["data"] = deal
    else:
        print(f"  [FAIL] 获取失败: {resp}")

    result["response"] = resp
    return result


def test_submit_deal(deal_id):
    """P0-4: 提交审批"""
    print("\n" + "-"*50)
    print(f"[P0-4] 提交审批 - POST /api/v1/dealing/deals/{deal_id}/submit")
    print("-"*50)

    if not deal_id:
        print(f"  [SKIP] No deal_id provided")
        return {"name": "提交审批", "status": "skipped", "response": None}

    request = {"operator": "test_user"}
    resp = curl_cmd("POST", f"/api/v1/dealing/deals/{deal_id}/submit", request)

    result = {"name": "提交审批", "status": "failed", "response": None}

    if resp and resp.get("code") == 200:
        print(f"  [PASS] 提交成功")
        result["status"] = "passed"
    else:
        print(f"  [FAIL] 提交失败: {resp}")

    result["response"] = resp
    return result


def test_approve_deal(deal_id):
    """P0-5: 审批通过"""
    print("\n" + "-"*50)
    print(f"[P0-5] 审批通过 - POST /api/v1/dealing/deals/{deal_id}/approve")
    print("-"*50)

    if not deal_id:
        print(f"  [SKIP] No deal_id provided")
        return {"name": "审批通过", "status": "skipped", "response": None}

    request = {"approver": "approver_user", "approvalRemark": "Approved by API test"}
    resp = curl_cmd("POST", f"/api/v1/dealing/deals/{deal_id}/approve", request)

    result = {"name": "审批通过", "status": "failed", "response": None}

    if resp and resp.get("code") == 200:
        print(f"  [PASS] 审批通过")
        result["status"] = "passed"
    else:
        print(f"  [FAIL] 审批失败: {resp}")

    result["response"] = resp
    return result


# ========== P1 Test Cases ==========

def test_update_deal(deal_id, deal_number, test_data):
    """P1-1: 更新交易"""
    print("\n" + "-"*50)
    print(f"[P1-1] 更新交易 - POST /api/v1/dealing/deals/update")
    print("-"*50)

    if not deal_id:
        print(f"  [SKIP] No deal_id provided")
        return {"name": "更新交易", "status": "skipped", "response": None}

    deal = {
        "id": deal_id,
        "dealNumber": deal_number,
        "dealType": "AC",
        "businessUnit": test_data.get("businessUnit") or "BU001",
        "counterpartyId": (test_data.get("counterparty") or {}).get("id") or 1,
        "instrumentId": (test_data.get("instrument") or {}).get("id") or 1,
        "traderId": (test_data.get("trader") or {}).get("id") or 1000,
        "direction": "SELL",
        "amount": 200000.00,
        "currency": "EUR",
        "dealDate": "2026-06-01",
        "valueDate": "2026-06-05",
        "status": "New",
        "description": "Updated Test AC Deal",
        "remark": "API Test Updated",
        "operator": "test_user",
        "bankAccountId": (test_data.get("bankAccount") or {}).get("id") or 1,
        "counterpartyAccountId": (test_data.get("counterpartyAccount") or {}).get("id") or 1,
        "paymentMethod": "TELEX"
    }

    resp = curl_cmd("POST", "/api/v1/dealing/deals/update", deal)

    result = {"name": "更新交易", "status": "failed", "response": None}

    if resp and resp.get("code") == 200:
        print(f"  [PASS] 更新成功")
        result["status"] = "passed"
    else:
        print(f"  [FAIL] 更新失败: {resp}")

    result["response"] = resp
    return result


def test_reject_deal(deal_id):
    """P1-2: 审批拒绝"""
    print("\n" + "-"*50)
    print(f"[P1-2] 审批拒绝 - POST /api/v1/dealing/deals/{deal_id}/reject")
    print("-"*50)

    if not deal_id:
        print(f"  [SKIP] No deal_id provided")
        return {"name": "审批拒绝", "status": "skipped", "response": None}

    request = {"approver": "approver_user", "approvalRemark": "Rejected by API test"}
    resp = curl_cmd("POST", f"/api/v1/dealing/deals/{deal_id}/reject", request)

    result = {"name": "审批拒绝", "status": "failed", "response": None}

    if resp and resp.get("code") == 200:
        print(f"  [PASS] 审批拒绝成功")
        result["status"] = "passed"
    else:
        print(f"  [FAIL] 审批拒绝失败: {resp}")

    result["response"] = resp
    return result


def test_execute_deal(deal_id):
    """P1-3: 执行交易"""
    print("\n" + "-"*50)
    print(f"[P1-3] 执行交易 - POST /api/v1/dealing/deals/{deal_id}/execute")
    print("-"*50)

    if not deal_id:
        print(f"  [SKIP] No deal_id provided")
        return {"name": "执行交易", "status": "skipped", "response": None}

    request = {"operator": "operator_user"}
    resp = curl_cmd("POST", f"/api/v1/dealing/deals/{deal_id}/execute", request)

    result = {"name": "执行交易", "status": "failed", "response": None}

    if resp and resp.get("code") == 200:
        print(f"  [PASS] 执行成功")
        result["status"] = "passed"
    else:
        print(f"  [FAIL] 执行失败: {resp}")

    result["response"] = resp
    return result


def test_query_actions_page():
    """P1-4: 查询Action列表"""
    print("\n" + "-"*50)
    print("[P1-4] 查询Action列表 - GET /api/v1/dealing/actions/page")
    print("-"*50)

    resp = curl_cmd("GET", "/api/v1/dealing/actions/page?pageNum=1&pageSize=10")

    result = {"name": "查询Action列表", "status": "failed", "response": None}

    if resp and resp.get("code") == 200:
        records = resp.get("data", {}).get("records", [])
        total = resp.get("data", {}).get("total", 0)
        print(f"  [PASS] 返回 {len(records)} 条记录, total={total}")
        result["status"] = "passed"
        result["data"] = {"total": total, "count": len(records)}
    else:
        print(f"  [FAIL] 查询失败: {resp}")

    result["response"] = resp
    return result


def test_query_images_by_deal(deal_number):
    """P1-5: 查询镜像列表"""
    print("\n" + "-"*50)
    print(f"[P1-5] 查询镜像列表 - GET /api/v1/dealing/images/by-deal/{deal_number}")
    print("-"*50)

    if not deal_number:
        print(f"  [SKIP] No deal_number provided")
        return {"name": "查询镜像列表", "status": "skipped", "response": None}

    resp = curl_cmd("GET", f"/api/v1/dealing/images/by-deal/{deal_number}")

    result = {"name": "查询镜像列表", "status": "failed", "response": None}

    if resp and resp.get("code") == 200:
        images = resp.get("data", [])
        print(f"  [PASS] 返回 {len(images)} 条镜像记录")
        result["status"] = "passed"
        result["data"] = {"count": len(images)}
    else:
        print(f"  [FAIL] 查询失败: {resp}")

    result["response"] = resp
    return result


# ========== Main ==========
def main():
    print("\n" + "="*60)
    print("# Open-TMS AC交易(Deal) API自动化测试")
    print("="*60)

    # 检查服务状态
    basedata_running = check_backend_health(BASEDATA_URL, "Basedata Backend")
    dealing_running = check_backend_health(BACKEND_URL, "Dealing Backend")

    proc = None

    if not dealing_running:
        #启动 dealing 后端
        if not os.path.exists(jar_path):
            print(f"\n[ERROR] JAR file not found: {jar_path}")
            print("Please build the dealing module first: mvn clean package")
            return 1

        proc = start_backend(8082, jar_path, "Dealing Backend")
        if not proc:
            return 1
    else:
        print("\n[OK] Dealing Backend is already running")

    if not basedata_running:
        print("\n[WARN] Basedata Backend is not running. Test data preparation may fail.")
        print("       Some tests may use fallback values.")

    # 准备测试数据
    test_data = prepare_test_data()

    # 存储测试结果
    report = {
        "total": 0,
        "passed": 0,
        "failed": 0,
        "results": []
    }

    # P0测试用例
    p0_results = []

    # P0-1: 创建交易
    result = test_create_deal(test_data)
    p0_results.append(result)
    report["total"] += 1
    if result["status"] == "passed":
        report["passed"] += 1
    else:
        report["failed"] += 1
    report["results"].append(result)

    # P0-2: 查询交易列表
    result = test_query_deals_page()
    p0_results.append(result)
    report["total"] += 1
    if result["status"] == "passed":
        report["passed"] += 1
    else:
        report["failed"] += 1
    report["results"].append(result)

    # 获取刚创建的交易的ID用于后续测试
    created_deal_id = p0_results[0].get("deal_id")
    created_deal_number = p0_results[0].get("deal_number")

    # P0-3: 获取交易详情
    result = test_get_deal_by_id(created_deal_id)
    p0_results.append(result)
    report["total"] += 1
    if result["status"] == "passed":
        report["passed"] += 1
    else:
        report["failed"] += 1
    report["results"].append(result)

    # P0-4: 提交审批
    result = test_submit_deal(created_deal_id)
    p0_results.append(result)
    report["total"] += 1
    if result["status"] == "passed":
        report["passed"] += 1
    else:
        report["failed"] += 1
    report["results"].append(result)

    # P0-5: 审批通过
    result = test_approve_deal(created_deal_id)
    p0_results.append(result)
    report["total"] += 1
    if result["status"] == "passed":
        report["passed"] += 1
    else:
        report["failed"] += 1
    report["results"].append(result)

    # P1测试用例
    # P1-1: 更新交易 (使用新的deal进行测试，因为审批后的deal不能再更新)
    # 先创建一个新deal
    new_deal_result = test_create_deal(test_data)
    new_deal_id = new_deal_result.get("deal_id")
    new_deal_number = new_deal_result.get("deal_number")

    result = test_update_deal(new_deal_id, new_deal_number, test_data)
    report["total"] += 1
    if result["status"] == "passed":
        report["passed"] += 1
    else:
        report["failed"] += 1
    report["results"].append(result)

    # P1-2: 审批拒绝 (提交一个新的deal来拒绝)
    reject_deal_result = test_create_deal(test_data)
    reject_deal_id = reject_deal_result.get("deal_id")

    # 先提交
    test_submit_deal(reject_deal_id)
    # 再拒绝
    result = test_reject_deal(reject_deal_id)
    report["total"] += 1
    if result["status"] == "passed":
        report["passed"] += 1
    else:
        report["failed"] += 1
    report["results"].append(result)

    # P1-3: 执行交易 (提交并审批一个新deal来执行)
    execute_deal_result = test_create_deal(test_data)
    execute_deal_id = execute_deal_result.get("deal_id")

    # 先提交
    test_submit_deal(execute_deal_id)
    # 再审批通过 (需要两级审批，这里模拟第一级)
    test_approve_deal(execute_deal_id)
    # 再执行
    result = test_execute_deal(execute_deal_id)
    report["total"] += 1
    if result["status"] == "passed":
        report["passed"] += 1
    else:
        report["failed"] += 1
    report["results"].append(result)

    # P1-4: 查询Action列表
    result = test_query_actions_page()
    report["total"] += 1
    if result["status"] == "passed":
        report["passed"] += 1
    else:
        report["failed"] += 1
    report["results"].append(result)

    # P1-5: 查询镜像列表
    result = test_query_images_by_deal(created_deal_number or new_deal_number)
    report["total"] += 1
    if result["status"] == "passed":
        report["passed"] += 1
    else:
        report["failed"] += 1
    report["results"].append(result)

    # 关闭后端服务
    if proc:
        print("\n[STOP] Stopping Dealing Backend...")
        proc.terminate()
        time.sleep(2)
        proc.kill()
        print("[OK] Dealing Backend stopped")

    # 输出测试报告
    print("\n" + "="*60)
    print("# 测试报告")
    print("="*60)
    print(f"  Total: {report['total']}")
    print(f"  Passed: {report['passed']}")
    print(f"  Failed: {report['failed']}")
    print(f"  Pass Rate: {report['passed']*100//report['total']}%")
    print("="*60)

    print("\n[P0 测试结果]")
    p0_passed = sum(1 for r in p0_results if r["status"] == "passed")
    print(f"  Passed: {p0_passed}/{len(p0_results)}")
    for r in p0_results:
        status_icon = "[PASS]" if r["status"] == "passed" else "[FAIL]"
        print(f"  {status_icon} {r['name']}")

    print("\n[P1 测试结果]")
    p1_results = report["results"][5:]  # P1用例从第6个开始
    p1_passed = sum(1 for r in p1_results if r["status"] == "passed")
    print(f"  Passed: {p1_passed}/{len(p1_results)}")
    for r in p1_results:
        status_icon = "[PASS]" if r["status"] == "passed" else "[FAIL]"
        print(f"  {status_icon} {r['name']}")

    # 保存JSON报告
    report_file = "E:\\code-project\\open-tms\\open-tms\\scripts\\test\\test_deal_report.json"
    with open(report_file, 'w', encoding='utf-8') as f:
        json.dump(report, f, ensure_ascii=False, indent=2)
    print(f"\n[INFO] Report saved to: {report_file}")

    return 0 if report["failed"] == 0 else 1


if __name__ == "__main__":
    sys.exit(main())
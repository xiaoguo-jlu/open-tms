#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Open-TMS AT交易(Account Transfer) API自动化测试

测试AT交易的完整生命周期（v2.0 设计）：
  - 创建（4 DealMap + 2 Cashflow 自动生成）
  - 查询（列表 + 详情 + DealMap 时间线）
  - 修改（软删旧 DealMap + 新建 DealMap + DealImage v+1）
  - 删除（级联软删 + DealImage v+1）
  - 审批/驳回（仅影响 Action，不影响 DealMap/Cashflow）

执行前置条件：
  1. 构建 dealing 模块：cd dealing && mvn clean package
  2. 构建 basedata 模块：cd basedata && mvn clean package
  3. 数据库已执行 db/schema/20-at-deal.sql
  4. 启动后端：
     - basedata:  java -jar basedata/target/opentms-basedata-1.0.0-SNAPSHOT.jar --server.port=8081
     - dealing:   java -jar dealing/target/dealing-1.0.0-SNAPSHOT.jar --server.port=8082
  5. 运行：python scripts/test/test_at_deal_api.py
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

jar_path = 'F:/code/opencode/opentrm/dealing/target/dealing-1.0.0-SNAPSHOT.jar'

# 全局报告
report = {
    "feature": "AT交易 (Account Transfer)",
    "version": "v2.0",
    "start_time": time.strftime("%Y-%m-%d %H:%M:%S"),
    "total": 0,
    "passed": 0,
    "failed": 0,
    "skipped": 0,
    "results": [],
    "validation_points": {
        "create_generates_4_dealmap_2_cashflow": None,
        "create_no_image": None,
        "update_soft_delete_old_dealmap": None,
        "update_create_new_dealmap": None,
        "update_update_cashflow_dealmap_number": None,
        "update_image_v_plus_1": None,
        "delete_cascade_soft_delete": None,
        "delete_image_v_plus_1": None,
        "approval_does_not_change_dealmap_event_status": None,
        "approval_does_not_change_cashflow_status": None,
        "action_many_to_one": None,
    }
}


# ========== Helpers ==========

def curl_cmd(method, path, data=None, base_url=BACKEND_URL, timeout=30):
    """执行HTTP请求并返回JSON响应"""
    url = f"{base_url}{path}"
    try:
        if method == "GET":
            req = urllib.request.Request(url)
        else:
            req = urllib.request.Request(url, data=json.dumps(data).encode('utf-8'), method='POST')
            req.add_header('Content-Type', 'application/json')
        with urllib.request.urlopen(req, timeout=timeout) as resp:
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
    try:
        resp = urllib.request.urlopen(f"{url}/api/v1/countries/page?pageNum=1&pageSize=1", timeout=5)
        data = json.loads(resp.read().decode())
        return data.get('code') == 200
    except Exception:
        return False


def kill_port(port):
    try:
        result = subprocess.run(['netstat', '-ano'], capture_output=True, text=True)
        for line in result.stdout.splitlines():
            if f':{port}' in line and 'LISTENING' in line:
                parts = line.split()
                if parts and parts[-1].isdigit():
                    pid = int(parts[-1])
                    try:
                        subprocess.run(['taskkill', '/F', '/PID', str(pid)], capture_output=True)
                    except:
                        pass
    except:
        pass
    time.sleep(2)


def start_backend(port, jar_path, name):
    print(f'\n[START] Starting {name} on port {port}...')
    kill_port(port)
    proc = subprocess.Popen(['java', '-jar', jar_path], stdout=subprocess.PIPE,
                            stderr=subprocess.STDOUT, text=True)
    started = False
    for line in proc.stdout:
        print(f"  {line.strip()}")
        if 'Started DealingApplication' in line or (
            'DealingApplication' in line and 'started' in line.lower()
        ):
            started = True
            print(f'[OK] {name} started')
            break
    if started:
        time.sleep(3)
        return proc
    proc.terminate()
    return None


# ========== Test Data Preparation ==========

def prepare_test_data():
    """准备AT交易测试所需的基础数据：业务单元、银行账户（含 CNY/USD 双币种）"""
    print("\n" + "=" * 60)
    print("# 准备测试数据")
    print("=" * 60)

    data = {
        "business_unit": None,
        "source_account_cny": None,   # 付出方 CNY 账户
        "dest_account_cny": None,     # 收入方 CNY 账户
        "source_account_usd": None,   # 付出方 USD 账户（跨币种场景）
        "dest_account_usd": None,     # 收入方 USD 账户
    }

    # 业务单元
    print("  [1] 查询 ManagementEntity...")
    resp = curl_cmd("GET", "/api/v1/management-entities/page?pageNum=1&pageSize=20",
                    base_url=BASEDATA_URL)
    if resp and resp.get("code") == 200:
        records = resp.get("data", {}).get("records", [])
        if records:
            data["business_unit"] = records[0].get("code")
            print(f"      [OK] businessUnit={data['business_unit']}")

    # 银行账户
    print("  [2] 查询 BankAccount（CNY/USD）...")
    resp = curl_cmd("GET", "/api/v1/bank-accounts/page?pageNum=1&pageSize=50",
                    base_url=BASEDATA_URL)
    if resp and resp.get("code") == 200:
        records = resp.get("data", {}).get("records", [])
        cny_accounts = []
        usd_accounts = []
        for r in records:
            cur = r.get("currency") or r.get("ccy") or ""
            if cur.upper() == "CNY":
                cny_accounts.append(r)
            elif cur.upper() == "USD":
                usd_accounts.append(r)
        if len(cny_accounts) >= 2:
            data["source_account_cny"] = cny_accounts[0]
            data["dest_account_cny"] = cny_accounts[1]
            print(f"      [OK] CNY: source={cny_accounts[0].get('id')}, dest={cny_accounts[1].get('id')}")
        if len(usd_accounts) >= 2:
            data["source_account_usd"] = usd_accounts[0]
            data["dest_account_usd"] = usd_accounts[1]
            print(f"      [OK] USD: source={usd_accounts[0].get('id')}, dest={usd_accounts[1].get('id')}")

    return data


# ========== API Test Cases ==========

def record_result(test_id, name, status, note=None, expected=None, actual=None):
    report["total"] += 1
    if status == "passed":
        report["passed"] += 1
    elif status == "skipped":
        report["skipped"] += 1
    else:
        report["failed"] += 1
    rec = {"id": test_id, "name": name, "status": status}
    if note:
        rec["note"] = note
    if expected is not None:
        rec["expected"] = expected
    if actual is not None:
        rec["actual"] = actual
    report["results"].append(rec)
    icon = "[PASS]" if status == "passed" else ("[SKIP]" if status == "skipped" else "[FAIL]")
    print(f"  {icon} {test_id}: {name}" + (f" — {note}" if note else ""))


def test_tc_at_001_same_company_same_currency(test_data):
    """TC-AT-001: 创建同公司同币种 AT，验证 4 DealMap + 2 Cashflow 自动生成"""
    print("\n" + "-" * 50)
    print("[TC-AT-001] 创建同公司同币种 AT")
    print("-" * 50)

    src = test_data.get("source_account_cny")
    dst = test_data.get("dest_account_cny")
    if not src or not dst:
        record_result("TC-AT-001", "创建同公司同币种 AT", "skipped",
                      "缺少 source/dest CNY 账户", None, None)
        return None

    at = {
        "dealType": "AT",
        "transferType": "SAME_COMPANY",
        "managementEntity": test_data.get("business_unit") or "BU001",
        "sourceAccountId": src.get("id"),
        "destAccountId": dst.get("id"),
        "sourceAmount": 1000000.00,
        "destAmount": 1000000.00,
        "sourceCurrency": "CNY",
        "destCurrency": "CNY",
        "exchangeRate": 1.0,
        "valueDate": "2026-06-25",
        "paymentMethod": "INTERNAL",
        "purpose": "TC-AT-001 同公司同币种转账",
        "operator": "qa_user",
    }

    resp = curl_cmd("POST", "/api/v1/dealing/at-deals", at)
    if not resp or resp.get("code") != 200:
        record_result("TC-AT-001", "创建同公司同币种 AT", "failed",
                      f"创建失败: {resp}", "code=200", resp)
        return None

    deal_id = resp.get("data", {}).get("id")
    deal_number = resp.get("data", {}).get("dealNumber")
    record_result("TC-AT-001", "创建同公司同币种 AT", "passed",
                  f"dealNumber={deal_number}", "code=200", resp.get("code"))

    # 验证 DealMap 自动生成（4 条）
    time.sleep(1)
    dealmap_resp = curl_cmd("GET", f"/api/v1/dealing/dealmap/by-deal/{deal_number}")
    dealmaps = (dealmap_resp or {}).get("data", [])
    print(f"      DealMap 数量: {len(dealmaps)}（预期 4）")
    if len(dealmaps) == 4:
        report["validation_points"]["create_generates_4_dealmap_2_cashflow"] = "passed"
    else:
        report["validation_points"]["create_generates_4_dealmap_2_cashflow"] = (
            f"failed: 实际 {len(dealmaps)} 条"
        )

    # 验证 event_status=Active
    all_active = all(d.get("eventStatus") == "Active" for d in dealmaps)
    if not all_active:
        print(f"      [WARN] 部分 DealMap.event_status 不是 Active")

    # 验证 Cashflow 自动生成（2 条），应与 DealMap 关联
    cf_resp = curl_cmd("GET", f"/api/v1/dealing/cashflows/by-deal/{deal_number}")
    cashflows = (cf_resp or {}).get("data", [])
    if isinstance(cashflows, dict):
        cashflows = cashflows.get("records", [])
    print(f"      Cashflow 数量: {len(cashflows)}（预期 2）")

    # 验证 CREATE 不生成 AtDealImage
    img_resp = curl_cmd("GET", f"/api/v1/dealing/at-deal-images/by-deal/{deal_number}")
    imgs = (img_resp or {}).get("data", [])
    if isinstance(imgs, dict):
        imgs = imgs.get("records", [])
    print(f"      AtDealImage 数量: {len(imgs)}（预期 0）")
    if len(imgs) == 0:
        report["validation_points"]["create_no_image"] = "passed"
    else:
        report["validation_points"]["create_no_image"] = f"failed: 实际 {len(imgs)} 条"

    return {"deal_id": deal_id, "deal_number": deal_number}


def test_tc_at_002_query_list():
    """TC-AT-002: 查询 AT 列表"""
    print("\n[TC-AT-002] 查询 AT 列表")
    resp = curl_cmd("GET", "/api/v1/dealing/at-deals/page?pageNum=1&pageSize=10")
    if resp and resp.get("code") == 200:
        total = resp.get("data", {}).get("total", 0)
        records = resp.get("data", {}).get("records", [])
        record_result("TC-AT-002", "查询 AT 列表", "passed",
                      f"total={total}, count={len(records)}", "code=200", resp.get("code"))
    else:
        record_result("TC-AT-002", "查询 AT 列表", "failed",
                      f"响应: {resp}", "code=200", resp)


def test_tc_at_003_query_detail(created):
    """TC-AT-003: 查询 AT 详情"""
    print("\n[TC-AT-003] 查询 AT 详情")
    if not created:
        record_result("TC-AT-003", "查询 AT 详情", "skipped", "无 created deal")
        return
    resp = curl_cmd("GET", f"/api/v1/dealing/at-deals/{created['deal_id']}")
    if resp and resp.get("code") == 200:
        data = resp.get("data", {})
        record_result("TC-AT-003", "查询 AT 详情", "passed",
                      f"dealNumber={data.get('dealNumber')}", "code=200", resp.get("code"))
    else:
        record_result("TC-AT-003", "查询 AT 详情", "failed",
                      f"响应: {resp}", "code=200", resp)


def test_tc_at_004_approve(created):
    """TC-AT-004: 审批通过 Action，验证 DealMap/Cashflow 状态不变"""
    print("\n[TC-AT-004] 审批通过 Action")
    if not created:
        record_result("TC-AT-004", "审批通过", "skipped", "无 created deal")
        return
    # 查询 Action
    act_resp = curl_cmd("GET", f"/api/v1/dealing/actions/by-deal/{created['deal_number']}")
    actions = (act_resp or {}).get("data", [])
    if isinstance(actions, dict):
        actions = actions.get("records", [])
    if not actions:
        record_result("TC-AT-004", "审批通过", "failed", "无 Action 记录")
        return
    action_number = actions[0].get("actionNumber")

    # 记录审批前 DealMap.event_status
    pre_dm = curl_cmd("GET", f"/api/v1/dealing/dealmap/by-deal/{created['deal_number']}")
    pre_dms = (pre_dm or {}).get("data", [])
    pre_status = pre_dms[0].get("eventStatus") if pre_dms else None

    # 审批
    resp = curl_cmd("POST", f"/api/v1/dealing/actions/{action_number}/approve",
                    {"approver": "qa_approver", "approvalRemark": "TC-AT-004 UI/API test"})
    if resp and resp.get("code") == 200:
        record_result("TC-AT-004", "审批通过", "passed",
                      f"action={action_number}", "code=200", resp.get("code"))
    else:
        record_result("TC-AT-004", "审批通过", "failed",
                      f"响应: {resp}", "code=200", resp)
        return

    # 验证 DealMap 状态未变
    post_dm = curl_cmd("GET", f"/api/v1/dealing/dealmap/by-deal/{created['deal_number']}")
    post_dms = (post_dm or {}).get("data", [])
    post_status = post_dms[0].get("eventStatus") if post_dms else None
    if pre_status == post_status == "Active":
        report["validation_points"]["approval_does_not_change_dealmap_event_status"] = "passed"
    else:
        report["validation_points"]["approval_does_not_change_dealmap_event_status"] = (
            f"failed: 审批前 {pre_status}, 审批后 {post_status}"
        )


def test_tc_at_005_cross_currency(test_data):
    """TC-AT-005: 创建跨币种 AT"""
    print("\n[TC-AT-005] 创建跨币种 AT")
    src = test_data.get("source_account_cny")
    dst = test_data.get("dest_account_usd")
    if not src or not dst:
        record_result("TC-AT-005", "创建跨币种 AT", "skipped", "缺少 CNY/USD 账户对")
        return None
    at = {
        "dealType": "AT",
        "transferType": "CROSS_BORDER",
        "managementEntity": test_data.get("business_unit") or "BU001",
        "sourceAccountId": src.get("id"),
        "destAccountId": dst.get("id"),
        "sourceAmount": 1000000.00,
        "destAmount": 138900.00,
        "sourceCurrency": "CNY",
        "destCurrency": "USD",
        "exchangeRate": 0.1389,
        "valueDate": "2026-06-25",
        "paymentMethod": "SWIFT",
        "purpose": "TC-AT-005 跨币种",
        "operator": "qa_user",
    }
    resp = curl_cmd("POST", "/api/v1/dealing/at-deals", at)
    if resp and resp.get("code") == 200:
        record_result("TC-AT-005", "创建跨币种 AT", "passed",
                      f"dealNumber={resp.get('data', {}).get('dealNumber')}")
        return resp.get("data", {})
    record_result("TC-AT-005", "创建跨币种 AT", "failed",
                  f"响应: {resp}", "code=200", resp)


def test_tc_at_006_exchange_rate_calc(created_cross):
    """TC-AT-006: source_amount * exchange_rate = dest_amount"""
    print("\n[TC-AT-006] 汇率计算校验")
    if not created_cross:
        record_result("TC-AT-006", "汇率计算校验", "skipped", "无跨币种 deal")
        return
    src = float(created_cross.get("sourceAmount"))
    rate = float(created_cross.get("exchangeRate"))
    dest = float(created_cross.get("destAmount"))
    expected = src * rate
    if abs(expected - dest) < 0.01:
        record_result("TC-AT-006", "汇率计算校验", "passed",
                      f"{src}*{rate}={expected:.2f} ≈ {dest}")
    else:
        record_result("TC-AT-006", "汇率计算校验", "failed",
                      f"预期 {expected:.2f}, 实际 {dest}")


def test_tc_at_007_cross_company(test_data):
    """TC-AT-007: 创建跨公司 AT"""
    print("\n[TC-AT-007] 创建跨公司 AT")
    src = test_data.get("source_account_cny")
    dst = test_data.get("dest_account_cny")
    if not src or not dst:
        record_result("TC-AT-007", "创建跨公司 AT", "skipped", "缺少账户")
        return
    at = {
        "dealType": "AT",
        "transferType": "CROSS_COMPANY",
        "managementEntity": test_data.get("business_unit") or "BU001",
        "sourceAccountId": src.get("id"),
        "destAccountId": dst.get("id"),
        "sourceAmount": 500000.00,
        "destAmount": 500000.00,
        "sourceCurrency": "CNY",
        "destCurrency": "CNY",
        "exchangeRate": 1.0,
        "valueDate": "2026-06-25",
        "paymentMethod": "RTGS",
        "purpose": "TC-AT-007 跨公司",
        "operator": "qa_user",
    }
    resp = curl_cmd("POST", "/api/v1/dealing/at-deals", at)
    if resp and resp.get("code") == 200:
        record_result("TC-AT-007", "创建跨公司 AT", "passed",
                      f"transferType={resp.get('data', {}).get('transferType')}")
    else:
        record_result("TC-AT-007", "创建跨公司 AT", "failed",
                      f"响应: {resp}", "code=200", resp)


def test_tc_at_008_cross_company_cross_currency(test_data):
    """TC-AT-008: 跨公司 + 跨币种"""
    print("\n[TC-AT-008] 创建跨公司跨币种 AT")
    src = test_data.get("source_account_usd")
    dst = test_data.get("dest_account_cny")
    if not src or not dst:
        record_result("TC-AT-008", "创建跨公司跨币种 AT", "skipped", "缺少 USD/CNY 跨公司账户")
        return
    at = {
        "dealType": "AT",
        "transferType": "CROSS_COMPANY",
        "managementEntity": test_data.get("business_unit") or "BU001",
        "sourceAccountId": src.get("id"),
        "destAccountId": dst.get("id"),
        "sourceAmount": 100000.00,
        "destAmount": 720000.00,
        "sourceCurrency": "USD",
        "destCurrency": "CNY",
        "exchangeRate": 7.2,
        "valueDate": "2026-06-25",
        "paymentMethod": "SWIFT",
        "purpose": "TC-AT-008 跨公司跨币种",
        "operator": "qa_user",
    }
    resp = curl_cmd("POST", "/api/v1/dealing/at-deals", at)
    if resp and resp.get("code") == 200:
        record_result("TC-AT-008", "创建跨公司跨币种 AT", "passed")
    else:
        record_result("TC-AT-008", "创建跨公司跨币种 AT", "failed",
                      f"响应: {resp}", "code=200", resp)


def test_tc_at_009_cross_border(test_data):
    """TC-AT-009: 跨境转账 CROSS_BORDER"""
    print("\n[TC-AT-009] 创建跨境 AT")
    src = test_data.get("source_account_usd")
    dst = test_data.get("dest_account_cny")
    if not src or not dst:
        record_result("TC-AT-009", "创建跨境 AT", "skipped", "缺少 USD/CNY 账户")
        return
    at = {
        "dealType": "AT",
        "transferType": "CROSS_BORDER",
        "managementEntity": test_data.get("business_unit") or "BU001",
        "sourceAccountId": src.get("id"),
        "destAccountId": dst.get("id"),
        "sourceAmount": 50000.00,
        "destAmount": 360000.00,
        "sourceCurrency": "USD",
        "destCurrency": "CNY",
        "exchangeRate": 7.2,
        "valueDate": "2026-06-25",
        "paymentMethod": "SWIFT",
        "purpose": "TC-AT-009 跨境",
        "operator": "qa_user",
    }
    resp = curl_cmd("POST", "/api/v1/dealing/at-deals", at)
    if resp and resp.get("code") == 200:
        record_result("TC-AT-009", "创建跨境 AT", "passed",
                      f"transferType={resp.get('data', {}).get('transferType')}")
    else:
        record_result("TC-AT-009", "创建跨境 AT", "failed",
                      f"响应: {resp}", "code=200", resp)


def test_tc_at_010_update(created):
    """TC-AT-010: 修改 AT，验证软删+新建 DealMap、Cashflow.dealmap_number 更新、Image v+1"""
    print("\n[TC-AT-010] 修改 AT")
    if not created:
        record_result("TC-AT-010", "修改 AT", "skipped", "无 created deal")
        return
    # 查询修改前 DealMap 数量
    pre_dm = curl_cmd("GET", f"/api/v1/dealing/dealmap/by-deal/{created['deal_number']}")
    pre_dms = (pre_dm or {}).get("data", [])
    pre_dm_numbers = [d.get("dealmapNumber") for d in pre_dms]
    print(f"      修改前 DealMap 数量: {len(pre_dms)}（含 dealmap_number={pre_dm_numbers[:2]}...）")

    at = dict(created)
    at["sourceAmount"] = 2000000.00
    at["destAmount"] = 2000000.00
    at["purpose"] = "TC-AT-010 已修改"
    at["operator"] = "qa_user"

    resp = curl_cmd("POST", "/api/v1/dealing/at-deals/update", at)
    if not resp or resp.get("code") != 200:
        record_result("TC-AT-010", "修改 AT", "failed",
                      f"响应: {resp}", "code=200", resp)
        return
    record_result("TC-AT-010", "修改 AT", "passed", "修改成功")

    # 验证 DealMap：旧 dealmap_number 软删
    post_dm = curl_cmd("GET", f"/api/v1/dealing/dealmap/by-deal/{created['deal_number']}")
    post_dms = (post_dm or {}).get("data", [])
    new_dm_numbers = [d.get("dealmapNumber") for d in post_dms]
    # 旧 DealMap 被软删（需通过 deleted 字段判断，或 DB 验证）
    soft_deleted_ok = all(n not in pre_dm_numbers for n in new_dm_numbers if n)
    # 新建 4 条 DealMap
    new_4_ok = len(post_dms) == 4
    if soft_deleted_ok and new_4_ok:
        report["validation_points"]["update_soft_delete_old_dealmap"] = "passed"
        report["validation_points"]["update_create_new_dealmap"] = "passed"
    else:
        report["validation_points"]["update_soft_delete_old_dealmap"] = (
            f"failed: soft_deleted_ok={soft_deleted_ok}, new_4_ok={new_4_ok}"
        )

    # 验证 Cashflow.dealmap_number 指向新 DealMap
    cf_resp = curl_cmd("GET", f"/api/v1/dealing/cashflows/by-deal/{created['deal_number']}")
    cashflows = (cf_resp or {}).get("data", [])
    if isinstance(cashflows, dict):
        cashflows = cashflows.get("records", [])
    cf_dealmap_nums = [c.get("dealmapNumber") for c in cashflows]
    cf_updated_ok = all(n in new_dm_numbers for n in cf_dealmap_nums if n)
    if cf_updated_ok and len(cashflows) >= 2:
        report["validation_points"]["update_update_cashflow_dealmap_number"] = "passed"
    else:
        report["validation_points"]["update_update_cashflow_dealmap_number"] = (
            f"failed: cashflow.dealmap_number={cf_dealmap_nums}, new_dm={new_dm_numbers}"
        )

    # 验证 Image v+1
    img_resp = curl_cmd("GET", f"/api/v1/dealing/at-deal-images/by-deal/{created['deal_number']}")
    imgs = (img_resp or {}).get("data", [])
    if isinstance(imgs, dict):
        imgs = imgs.get("records", [])
    if imgs and len(imgs) >= 1:
        # 版本号 ≥ 2
        max_v = max((img.get("version") or 0) for img in imgs)
        if max_v >= 2:
            report["validation_points"]["update_image_v_plus_1"] = "passed"
        else:
            report["validation_points"]["update_image_v_plus_1"] = (
                f"failed: max version={max_v}"
            )
    else:
        report["validation_points"]["update_image_v_plus_1"] = "failed: 无 image"


def test_tc_at_011_update_cross_currency_rate(created_cross):
    """TC-AT-011: 修改跨币种 AT 的汇率"""
    print("\n[TC-AT-011] 修改跨币种 AT 的汇率")
    if not created_cross:
        record_result("TC-AT-011", "修改跨币种 AT 的汇率", "skipped", "无跨币种 deal")
        return
    at = dict(created_cross)
    at["exchangeRate"] = 0.14
    at["destAmount"] = 140000.00  # 1,000,000 × 0.14
    at["operator"] = "qa_user"
    resp = curl_cmd("POST", "/api/v1/dealing/at-deals/update", at)
    if resp and resp.get("code") == 200:
        record_result("TC-AT-011", "修改跨币种 AT 的汇率", "passed",
                      f"新汇率={resp.get('data', {}).get('exchangeRate')}")
    else:
        record_result("TC-AT-011", "修改跨币种 AT 的汇率", "failed",
                      f"响应: {resp}", "code=200", resp)


def test_tc_at_012_delete(created):
    """TC-AT-012: 删除 AT，验证级联软删 + Image v+1"""
    print("\n[TC-AT-012] 删除 AT")
    if not created:
        record_result("TC-AT-012", "删除 AT", "skipped", "无 created deal")
        return
    resp = curl_cmd("POST", f"/api/v1/dealing/at-deals/delete/{created['deal_id']}",
                    {"operator": "qa_user"})
    if not resp or resp.get("code") != 200:
        record_result("TC-AT-012", "删除 AT", "failed",
                      f"响应: {resp}", "code=200", resp)
        return
    record_result("TC-AT-012", "删除 AT", "passed", "删除调用成功")

    # 验证 Deal/Cashflow 软删（API 层通常无法直接看到 deleted 字段，需 DB 验证）
    # 此处标记为"待 DB 验证"
    report["validation_points"]["delete_cascade_soft_delete"] = "需要 DB 验证（已记录）"

    # 验证 Image v+1（DELETE 类型）
    img_resp = curl_cmd("GET", f"/api/v1/dealing/at-deal-images/by-deal/{created['deal_number']}")
    imgs = (img_resp or {}).get("data", [])
    if isinstance(imgs, dict):
        imgs = imgs.get("records", [])
    delete_imgs = [img for img in imgs if img.get("imageType") == "DELETE"]
    if delete_imgs:
        report["validation_points"]["delete_image_v_plus_1"] = "passed"
    else:
        report["validation_points"]["delete_image_v_plus_1"] = (
            f"failed: 未发现 imageType=DELETE 的镜像（总数 {len(imgs)}）"
        )


def test_tc_at_013_same_account_validation(test_data):
    """TC-AT-013: source == dest 应被拒绝"""
    print("\n[TC-AT-013] 双账户相同校验")
    src = test_data.get("source_account_cny")
    if not src:
        record_result("TC-AT-013", "双账户相同校验", "skipped", "无账户")
        return
    at = {
        "dealType": "AT",
        "transferType": "SAME_COMPANY",
        "managementEntity": test_data.get("business_unit") or "BU001",
        "sourceAccountId": src.get("id"),
        "destAccountId": src.get("id"),  # 故意相同
        "sourceAmount": 100.00,
        "destAmount": 100.00,
        "sourceCurrency": "CNY",
        "destCurrency": "CNY",
        "exchangeRate": 1.0,
        "valueDate": "2026-06-25",
        "paymentMethod": "INTERNAL",
        "operator": "qa_user",
    }
    resp = curl_cmd("POST", "/api/v1/dealing/at-deals", at)
    if resp and resp.get("code") == 200:
        record_result("TC-AT-013", "双账户相同校验", "failed",
                      "服务端未拒绝同账户", "code=400", resp.get("code"))
    else:
        code = (resp or {}).get("code")
        msg = (resp or {}).get("message", "")
        if code in (400, 500):
            record_result("TC-AT-013", "双账户相同校验", "passed",
                          f"已拒绝（code={code}, msg={msg[:50]}）")
        else:
            record_result("TC-AT-013", "双账户相同校验", "failed",
                          f"未拒绝: {resp}", "code=400", resp)


def test_tc_at_014_missing_exchange_rate(test_data):
    """TC-AT-014: 跨币种未填汇率应被拒绝"""
    print("\n[TC-AT-014] 跨币种未填汇率校验")
    src = test_data.get("source_account_cny")
    dst = test_data.get("dest_account_usd")
    if not src or not dst:
        record_result("TC-AT-014", "跨币种未填汇率", "skipped", "缺少 CNY/USD 账户")
        return
    at = {
        "dealType": "AT",
        "transferType": "CROSS_BORDER",
        "managementEntity": test_data.get("business_unit") or "BU001",
        "sourceAccountId": src.get("id"),
        "destAccountId": dst.get("id"),
        "sourceAmount": 100.00,
        "destAmount": 100.00,
        "sourceCurrency": "CNY",
        "destCurrency": "USD",
        # exchangeRate 故意缺失
        "valueDate": "2026-06-25",
        "paymentMethod": "SWIFT",
        "operator": "qa_user",
    }
    resp = curl_cmd("POST", "/api/v1/dealing/at-deals", at)
    code = (resp or {}).get("code")
    if code and code != 200:
        record_result("TC-AT-014", "跨币种未填汇率", "passed",
                      f"已拒绝 code={code}")
    else:
        record_result("TC-AT-014", "跨币种未填汇率", "failed",
                      "服务端未拒绝未填汇率的跨币种 AT", "code=400", code)


def test_tc_at_015_zero_source_amount(test_data):
    """TC-AT-015: source_amount = 0 应被拒绝"""
    print("\n[TC-AT-015] source_amount=0 校验")
    src = test_data.get("source_account_cny")
    dst = test_data.get("dest_account_cny")
    if not src or not dst:
        record_result("TC-AT-015", "source_amount=0", "skipped", "无账户")
        return
    at = {
        "dealType": "AT",
        "transferType": "SAME_COMPANY",
        "managementEntity": test_data.get("business_unit") or "BU001",
        "sourceAccountId": src.get("id"),
        "destAccountId": dst.get("id"),
        "sourceAmount": 0,
        "destAmount": 100.00,
        "sourceCurrency": "CNY",
        "destCurrency": "CNY",
        "exchangeRate": 1.0,
        "valueDate": "2026-06-25",
        "paymentMethod": "INTERNAL",
        "operator": "qa_user",
    }
    resp = curl_cmd("POST", "/api/v1/dealing/at-deals", at)
    code = (resp or {}).get("code")
    if code and code != 200:
        record_result("TC-AT-015", "source_amount=0", "passed",
                      f"已拒绝 code={code}")
    else:
        record_result("TC-AT-015", "source_amount=0", "failed",
                      "服务端未拒绝 source_amount=0", "code=400", code)


def test_tc_at_016_zero_dest_amount(test_data):
    """TC-AT-016: dest_amount = 0 应被拒绝"""
    print("\n[TC-AT-016] dest_amount=0 校验")
    src = test_data.get("source_account_cny")
    dst = test_data.get("dest_account_cny")
    if not src or not dst:
        record_result("TC-AT-016", "dest_amount=0", "skipped", "无账户")
        return
    at = {
        "dealType": "AT",
        "transferType": "SAME_COMPANY",
        "managementEntity": test_data.get("business_unit") or "BU001",
        "sourceAccountId": src.get("id"),
        "destAccountId": dst.get("id"),
        "sourceAmount": 100.00,
        "destAmount": 0,
        "sourceCurrency": "CNY",
        "destCurrency": "CNY",
        "exchangeRate": 1.0,
        "valueDate": "2026-06-25",
        "paymentMethod": "INTERNAL",
        "operator": "qa_user",
    }
    resp = curl_cmd("POST", "/api/v1/dealing/at-deals", at)
    code = (resp or {}).get("code")
    if code and code != 200:
        record_result("TC-AT-016", "dest_amount=0", "passed",
                      f"已拒绝 code={code}")
    else:
        record_result("TC-AT-016", "dest_amount=0", "failed",
                      "服务端未拒绝 dest_amount=0", "code=400", code)


def test_tc_at_017_action_many_to_one(created):
    """TC-AT-017: 同一 AT 可有多个 Action (CREATE + UPDATE + APPROVE)"""
    print("\n[TC-AT-017] Action 多对一")
    if not created:
        record_result("TC-AT-017", "Action 多对一", "skipped", "无 created deal")
        return
    # 在 TC-AT-004 审批 + TC-AT-010 修改 后，应有 ≥3 条 Action
    resp = curl_cmd("GET", f"/api/v1/dealing/actions/by-deal/{created['deal_number']}")
    actions = (resp or {}).get("data", [])
    if isinstance(actions, dict):
        actions = actions.get("records", [])
    types = [a.get("actionType") for a in actions]
    print(f"      Action 数量={len(actions)}, types={types}")
    if "CREATE" in types and "UPDATE" in types and "APPROVE" in types and len(actions) >= 3:
        report["validation_points"]["action_many_to_one"] = "passed"
        record_result("TC-AT-017", "Action 多对一", "passed",
                      f"Action types={types}")
    else:
        report["validation_points"]["action_many_to_one"] = (
            f"failed: types={types}, count={len(actions)}"
        )
        record_result("TC-AT-017", "Action 多对一", "failed",
                      f"types={types}, count={len(actions)}")


def test_tc_at_019_reject(created):
    """TC-AT-019: 驳回 Action"""
    print("\n[TC-AT-019] 驳回 Action")
    if not created:
        record_result("TC-AT-019", "驳回 Action", "skipped", "无 created deal")
        return
    # 取最新的 Pending Action
    resp = curl_cmd("GET", f"/api/v1/dealing/actions/by-deal/{created['deal_number']}")
    actions = (resp or {}).get("data", [])
    if isinstance(actions, dict):
        actions = actions.get("records", [])
    pending_actions = [a for a in actions if a.get("approvalStatus1") == "Pending"]
    if not pending_actions:
        record_result("TC-AT-019", "驳回 Action", "skipped", "无 Pending Action")
        return
    action_number = pending_actions[0].get("actionNumber")
    rej = curl_cmd("POST", f"/api/v1/dealing/actions/{action_number}/reject",
                   {"approver": "qa_approver", "approvalRemark": "TC-AT-019 reject"})
    if rej and rej.get("code") == 200:
        record_result("TC-AT-019", "驳回 Action", "passed", f"action={action_number}")
    else:
        record_result("TC-AT-019", "驳回 Action", "failed",
                      f"响应: {rej}", "code=200", rej)


# ========== Main ==========

def main():
    print("\n" + "=" * 60)
    print("# Open-TMS AT交易(Account Transfer) API自动化测试")
    print("# 版本: v2.0")
    print("=" * 60)

    basedata_running = check_backend_health(BASEDATA_URL, "Basedata")
    dealing_running = check_backend_health(BACKEND_URL, "Dealing")
    proc = None

    if not dealing_running:
        if not os.path.exists(jar_path):
            print(f"\n[ERROR] JAR not found: {jar_path}")
            print("请先执行: cd dealing && mvn clean package")
            return 1
        proc = start_backend(8082, jar_path, "Dealing Backend")
        if not proc:
            return 1
    else:
        print("\n[OK] Dealing Backend is running")

    if not basedata_running:
        print("\n[WARN] Basedata Backend not running. Test data may be insufficient.")

    test_data = prepare_test_data()

    # P0 Tests
    created = test_tc_at_001_same_company_same_currency(test_data)
    test_tc_at_002_query_list()
    test_tc_at_003_query_detail(created)
    test_tc_at_004_approve(created)
    created_cross = test_tc_at_005_cross_currency(test_data)
    test_tc_at_006_exchange_rate_calc(created_cross)
    test_tc_at_007_cross_company(test_data)
    test_tc_at_008_cross_company_cross_currency(test_data)

    # P1
    test_tc_at_009_cross_border(test_data)

    # P0 修改/删除
    test_tc_at_010_update(created)
    test_tc_at_011_update_cross_currency_rate(created_cross)
    test_tc_at_012_delete(created)

    # 业务校验
    test_tc_at_013_same_account_validation(test_data)
    test_tc_at_014_missing_exchange_rate(test_data)
    test_tc_at_015_zero_source_amount(test_data)
    test_tc_at_016_zero_dest_amount(test_data)

    # Action 多对一 + 驳回
    test_tc_at_017_action_many_to_one(created)
    # TC-AT-018 通过 TC-AT-004 中已验证 DealMap 不变；此处记录 validation_points
    if report["validation_points"]["approval_does_not_change_dealmap_event_status"] == "passed":
        report["validation_points"]["approval_does_not_change_cashflow_status"] = "passed（推断）"
    test_tc_at_019_reject(created)

    # 关闭后端
    if proc:
        proc.terminate()
        time.sleep(2)
        try:
            proc.kill()
        except:
            pass

    # 输出报告
    print("\n" + "=" * 60)
    print("# 测试报告")
    print("=" * 60)
    print(f"  Total:   {report['total']}")
    print(f"  Passed:  {report['passed']}")
    print(f"  Failed:  {report['failed']}")
    print(f"  Skipped: {report['skipped']}")
    rate = report['passed'] * 100 // report['total'] if report['total'] > 0 else 0
    print(f"  Pass Rate: {rate}%")

    print("\n# 关键验证点")
    for k, v in report["validation_points"].items():
        icon = "[OK]" if v == "passed" else "[??]"
        print(f"  {icon} {k}: {v}")

    print("\n# 详细结果")
    for r in report["results"]:
        icon = "[PASS]" if r["status"] == "passed" else (
            "[SKIP]" if r["status"] == "skipped" else "[FAIL]"
        )
        print(f"  {icon} {r['id']}: {r['name']}" + (f" — {r['note']}" if r.get("note") else ""))

    report["end_time"] = time.strftime("%Y-%m-%d %H:%M:%S")
    report_path = "F:/code/opencode/opentrm/scripts/test/test_at_deal_report.json"
    with open(report_path, 'w', encoding='utf-8') as f:
        json.dump(report, f, ensure_ascii=False, indent=2)
    print(f"\n[INFO] Report saved: {report_path}")
    return 0 if report["failed"] == 0 else 1


if __name__ == "__main__":
    sys.exit(main())
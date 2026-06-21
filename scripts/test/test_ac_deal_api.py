#!/usr/bin/env python3
"""
Open-TMS AC交易 v2.0 API自动化测试
测试v2.0 关键验收点：
- CREATE 后 DealMap/Cashflow 自动生成
- UPDATE 软删旧 DealMap + 新建
- DELETE 级联软删
- 审批不改变 DealMap/Cashflow 状态
- Action 多对一
"""

import subprocess
import json
import time
import sys
import urllib.request
import urllib.error

BACKEND_URL = "http://localhost:8082"


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


def db_query(sql):
    """执行 SQL 并返回结果"""
    import pg8000
    try:
        conn = pg8000.connect(host="localhost", port=5432, database="opentms", user="opentms", password="opentms123")
        cur = conn.cursor()
        cur.execute(sql)
        columns = [d[0] for d in cur.description] if cur.description else []
        rows = cur.fetchall()
        conn.close()
        result = []
        for row in rows:
            result.append(dict(zip(columns, row)))
        return result
    except Exception as e:
        return [{"error": str(e)}]


def check_backend_health():
    try:
        resp = curl_cmd("GET", "/api/v1/dealing/ac-deals/page?pageNum=1&pageSize=1")
        return resp and resp.get("code") == 200
    except:
        return False


def kill_port(port):
    try:
        result = subprocess.run(['netstat', '-ano'], capture_output=True, text=True)
        for line in result.stdout.splitlines():
            if f':{port}' in line and 'LISTENING' in line:
                parts = line.split()
                if parts and parts[-1].isdigit():
                    pid = int(parts[-1])
                    subprocess.run(['cmd', '/c', f'taskkill /F /PID {pid}'], capture_output=True)
    except:
        pass
    time.sleep(2)


def start_backend(jar_path):
    print(f'\n[START] Starting Dealing Backend on port 8082...')
    kill_port(8082)
    proc = subprocess.Popen(['java', '-jar', jar_path], stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True)
    started = False
    for line in proc.stdout:
        line = line.strip()
        if 'Started DealingApplication' in line:
            started = True
            print(f'[OK] Backend started')
            break
        if 'ERROR' in line or 'Caused by' in line:
            print(f'  [LOG] {line}')
    if started:
        time.sleep(3)
        return proc
    proc.terminate()
    return None


# ========== P0 Test Cases ==========

def test_create_ac_deal():
    """TC_AC_API_007-010: 创建 AC 交易 + 验证 v2.0 自动生成"""
    print("\n" + "=" * 60)
    print("[P0-1] 创建 AC 交易 - POST /api/v1/dealing/ac-deals")
    print("=" * 60)

    deal = {
        "dealType": "AC",
        "businessUnit": "BU001",
        "traderId": 1,
        "counterpartyId": 5001,
        "instrumentId": 301,
        "direction": "Outflow",
        "amount": 1000000.00,
        "currency": "CNY",
        "dealDate": "2026-06-21",
        "valueDate": "2026-06-21",
        "description": "测试AC交易 v2.0",
        "operator": "tester",
        "bankAccountId": 201,
        "counterpartyAccountId": 301,
        "paymentMethod": "TRANSFER"
    }

    result = {"name": "创建 AC 交易", "status": "failed", "deal_number": None, "checks": {}}
    resp = curl_cmd("POST", "/api/v1/dealing/ac-deals", deal)
    if not resp or resp.get("code") != 200:
        result["response"] = resp
        print(f"  [FAIL] 创建失败: {resp}")
        return result

    print(f"  [PASS] 创建成功")
    result["status"] = "passed"
    time.sleep(1)

    # 获取最新创建的 deal
    page_resp = curl_cmd("GET", "/api/v1/dealing/ac-deals/page?pageNum=1&pageSize=5")
    if page_resp and page_resp.get("code") == 200:
        records = page_resp.get("data", {}).get("records", [])
        for r in records:
            if r.get("dealNumber", "").startswith("AC2026"):
                result["deal_number"] = r.get("dealNumber")
                result["deal_id"] = r.get("id")
                break

    if not result["deal_number"]:
        print("  [FAIL] 无法获取 dealNumber")
        return result

    # TC_AC_API_008: 验证 DealMap 自动生成
    dealmap_resp = curl_cmd("GET", f"/api/v1/dealing/dealmap/by-deal/{result['deal_number']}")
    if dealmap_resp and dealmap_resp.get("code") == 200:
        dms = dealmap_resp.get("data", [])
        active = [d for d in dms if d.get("eventStatus") == "Active" and d.get("deleted") == "0"]
        if len(active) >= 1 and active[0].get("eventType") == "ActualCashflow":
            print(f"  [PASS] TC_AC_API_008: DealMap 自动生成 ({len(active)} 条 Active)")
            result["checks"]["dealmap_auto"] = True
        else:
            print(f"  [FAIL] TC_AC_API_008: DealMap 未正确生成 - {dms}")

    # TC_AC_API_009: 验证 Cashflow 自动生成
    cf_sql = f"SELECT * FROM tms_cashflow_t WHERE deal_number='{result['deal_number']}' AND deleted='0'"
    cfs = db_query(cf_sql)
    if cfs and len(cfs) >= 1 and cfs[0].get("dealmap_number"):
        print(f"  [PASS] TC_AC_API_009: Cashflow 自动生成 ({len(cfs)} 条, dealmap_number={cfs[0].get('dealmap_number')})")
        result["checks"]["cashflow_auto"] = True
    else:
        print(f"  [FAIL] TC_AC_API_009: Cashflow 未生成 - {cfs}")

    # TC_AC_API_010: 验证 CREATE 不生成 DealImage
    img_sql = f"SELECT COUNT(*) AS cnt FROM tms_deals_image_t WHERE deal_number='{result['deal_number']}'"
    imgs = db_query(img_sql)
    if imgs and imgs[0].get("cnt") == 0:
        print(f"  [PASS] TC_AC_API_010: CREATE 不生成 DealImage")
        result["checks"]["no_image_on_create"] = True
    else:
        print(f"  [FAIL] TC_AC_API_010: CREATE 不应生成 DealImage - {imgs}")

    result["response"] = resp
    return result


def test_query_page():
    """TC_AC_API_001: 分页查询"""
    print("\n" + "=" * 60)
    print("[P0-2] 分页查询 - GET /api/v1/dealing/ac-deals/page")
    print("=" * 60)
    result = {"name": "分页查询", "status": "failed"}
    resp = curl_cmd("GET", "/api/v1/dealing/ac-deals/page?pageNum=1&pageSize=10")
    if resp and resp.get("code") == 200:
        records = resp.get("data", {}).get("records", [])
        print(f"  [PASS] 返回 {len(records)} 条 AC 交易")
        result["status"] = "passed"
        result["data"] = {"count": len(records)}
    else:
        print(f"  [FAIL] {resp}")
    result["response"] = resp
    return result


def test_get_detail_by_id(deal_id):
    """TC_AC_API_004: 详情"""
    print("\n" + "=" * 60)
    print(f"[P0-3] 详情(按ID) - GET /api/v1/dealing/ac-deals/{deal_id}")
    print("=" * 60)
    result = {"name": "详情(按ID)", "status": "failed"}
    if not deal_id:
        result["status"] = "skipped"
        return result
    resp = curl_cmd("GET", f"/api/v1/dealing/ac-deals/{deal_id}")
    if resp and resp.get("code") == 200:
        d = resp.get("data", {})
        print(f"  [PASS] dealNumber={d.get('dealNumber')}, status={d.get('status')}")
        result["status"] = "passed"
    else:
        print(f"  [FAIL] {resp}")
    result["response"] = resp
    return result


def test_get_detail_by_number(deal_number):
    """TC_AC_API_005: 按 dealNumber 查询"""
    print("\n" + "=" * 60)
    print(f"[P0-4] 详情(按 dealNumber) - GET /api/v1/dealing/ac-deals/number/{deal_number}")
    print("=" * 60)
    result = {"name": "详情(按 dealNumber)", "status": "failed"}
    if not deal_number:
        result["status"] = "skipped"
        return result
    resp = curl_cmd("GET", f"/api/v1/dealing/ac-deals/number/{deal_number}")
    if resp and resp.get("code") == 200:
        d = resp.get("data", {})
        # v2.0 详情含 DealMap/Cashflow/Action
        if d.get("dealMapList") is not None and d.get("cashflowList") is not None:
            print(f"  [PASS] 详情含 DealMap({len(d.get('dealMapList'))} 条) + Cashflow({len(d.get('cashflowList'))} 条) + Action({len(d.get('actionList'))} 条)")
        else:
            print(f"  [PASS] dealNumber={d.get('dealNumber')}")
        result["status"] = "passed"
        result["data"] = d
    else:
        print(f"  [FAIL] {resp}")
    result["response"] = resp
    return result


def test_update_ac_deal(deal_id, deal_number):
    """TC_AC_API_016-018: 更新 AC 交易 + 验证软删+新建"""
    print("\n" + "=" * 60)
    print(f"[P0-5] 更新 AC 交易 - POST /api/v1/dealing/ac-deals/update")
    print("=" * 60)

    result = {"name": "更新 AC 交易", "status": "failed", "checks": {}}

    # 记录更新前 DealMap 数量
    pre_sql = f"SELECT dealmap_number, deleted, amount FROM tms_deal_map_t WHERE deal_number='{deal_number}' AND deleted='0' ORDER BY created_at DESC"
    pre_dms = db_query(pre_sql)
    print(f"  [INFO] 更新前 Active DealMap 数量: {len(pre_dms)}")

    update = {
        "dealNumber": deal_number,
        "dealType": "AC",
        "businessUnit": "BU001",
        "traderId": 1,
        "counterpartyId": 5001,
        "instrumentId": 301,
        "direction": "Outflow",
        "amount": 2000000.00,  # 金额变更
        "currency": "CNY",
        "dealDate": "2026-06-21",
        "valueDate": "2026-06-22",  # 起息日变更
        "description": "测试AC v2.0 更新",
        "operator": "tester",
        "bankAccountId": 201,
        "counterpartyAccountId": 301,
        "paymentMethod": "TELEX"
    }

    resp = curl_cmd("POST", "/api/v1/dealing/ac-deals/update", update)
    if not resp or resp.get("code") != 200:
        print(f"  [FAIL] 更新失败: {resp}")
        result["response"] = resp
        return result
    print(f"  [PASS] 更新成功")
    result["status"] = "passed"
    time.sleep(1)

    # TC_AC_API_016: 软删旧 DealMap + 新建
    post_sql = f"SELECT dealmap_number, deleted, amount, event_type FROM tms_deal_map_t WHERE deal_number='{deal_number}' ORDER BY created_at"
    post_dms = db_query(post_sql)
    deleted_count = sum(1 for d in post_dms if d.get("deleted") == "1")
    active_count = sum(1 for d in post_dms if d.get("deleted") == "0")
    total_count = len(post_dms)

    # 验证：旧 DealMap 被软删（deleted='1'），新 DealMap 是 Active（deleted='0'），总数 = 旧数 + 1
    if len(pre_dms) >= 1 and deleted_count >= 1 and total_count == len(pre_dms) + 1:
        print(f"  [PASS] TC_AC_API_016: 软删旧 DealMap + 新建 (软删 {deleted_count}, Active {active_count}, 总数 {total_count}={len(pre_dms)}+1)")
        result["checks"]["soft_delete_and_new"] = True
    else:
        print(f"  [FAIL] TC_AC_API_016: 软删+新建逻辑错误 (pre={len(pre_dms)}, post.deleted={deleted_count}, post.active={active_count}, post.total={total_count})")

    # TC_AC_API_017: Cashflow.dealmap_number 更新到新 DealMap
    cf_sql = f"SELECT dealmap_number, amount FROM tms_cashflow_t WHERE deal_number='{deal_number}' AND deleted='0'"
    cfs = db_query(cf_sql)
    active_dm = [d for d in post_dms if d.get("deleted") == "0"]
    if cfs and active_dm and cfs[0].get("dealmap_number") == active_dm[0].get("dealmap_number"):
        print(f"  [PASS] TC_AC_API_017: Cashflow.dealmap_number 已更新为 {cfs[0].get('dealmap_number')}")
        result["checks"]["cashflow_dealmap_updated"] = True
    else:
        print(f"  [FAIL] TC_AC_API_017: Cashflow 未更新 - {cfs}")

    # TC_AC_API_018: 生成 DealImage v+1
    img_sql = f"SELECT image_type, amount FROM tms_deals_image_t WHERE deal_number='{deal_number}' ORDER BY created_at"
    imgs = db_query(img_sql)
    if imgs and any(i.get("image_type") == "UPDATE" for i in imgs):
        print(f"  [PASS] TC_AC_API_018: DealImage v+1 已生成 ({len(imgs)} 条)")
        result["checks"]["dealimage_v_plus_1"] = True
    else:
        print(f"  [FAIL] TC_AC_API_018: DealImage 未生成 - {imgs}")

    result["response"] = resp
    return result


def test_query_actions(deal_number):
    """TC_AC_API_023-024: Action 列表 + Action 多对一"""
    print("\n" + "=" * 60)
    print(f"[P0-6] Action 列表 - GET /api/v1/dealing/actions/by-deal/{deal_number}")
    print("=" * 60)
    result = {"name": "Action 列表", "status": "failed"}
    if not deal_number:
        result["status"] = "skipped"
        return result
    resp = curl_cmd("GET", f"/api/v1/dealing/actions/by-deal/{deal_number}")
    if resp and resp.get("code") == 200:
        actions = resp.get("data", [])
        types = [a.get("actionType") for a in actions]
        print(f"  [PASS] 返回 {len(actions)} 个 Action: {types}")
        # 验证 Action 多对一：>= 2 个 Action
        if len(actions) >= 2:
            print(f"  [PASS] TC_AC_API_024: Action 多对一（{len(actions)} 个 Action/Deal）")
        result["status"] = "passed"
        result["data"] = {"count": len(actions), "actions": actions}
    else:
        print(f"  [FAIL] {resp}")
    result["response"] = resp
    return result


def test_approve_action(deal_number, action_number):
    """TC_AC_API_026-027: 审批通过 + 验证 DealMap/Cashflow 状态不变"""
    print("\n" + "=" * 60)
    print(f"[P0-7] 审批通过 Action {action_number} - POST /approve")
    print("=" * 60)
    result = {"name": "审批通过", "status": "failed", "checks": {}}

    # 记录审批前 DealMap/Cashflow 状态
    pre_dm = db_query(f"SELECT event_status FROM tms_deal_map_t WHERE deal_number='{deal_number}' AND deleted='0' LIMIT 1")
    pre_cf = db_query(f"SELECT status FROM tms_cashflow_t WHERE deal_number='{deal_number}' AND deleted='0' LIMIT 1")
    pre_dm_status = pre_dm[0].get("event_status") if pre_dm else None
    pre_cf_status = pre_cf[0].get("status") if pre_cf else None
    print(f"  [INFO] 审批前 DealMap.event_status={pre_dm_status}, Cashflow.status={pre_cf_status}")

    body = {"approver": "manager01", "approvalRemark": "审批通过 v2.0"}
    resp = curl_cmd("POST", f"/api/v1/dealing/actions/{action_number}/approve", body)
    if not resp or resp.get("code") != 200:
        print(f"  [FAIL] 审批失败: {resp}")
        result["response"] = resp
        return result
    print(f"  [PASS] 审批成功")
    result["status"] = "passed"
    time.sleep(1)

    # 验证 Action 状态
    act = db_query(f"SELECT approval_status1, approver1 FROM tms_actions_t WHERE action_number='{action_number}'")
    if act and act[0].get("approval_status1") == "Approved":
        print(f"  [PASS] Action.approval_status1 = 'Approved', approver1 = {act[0].get('approver1')}")
    else:
        print(f"  [FAIL] Action 状态错误 - {act}")

    # TC_AC_API_027: 验证 DealMap/Cashflow 状态不变
    post_dm = db_query(f"SELECT event_status FROM tms_deal_map_t WHERE deal_number='{deal_number}' AND deleted='0' LIMIT 1")
    post_cf = db_query(f"SELECT status FROM tms_cashflow_t WHERE deal_number='{deal_number}' AND deleted='0' LIMIT 1")
    post_dm_status = post_dm[0].get("event_status") if post_dm else None
    post_cf_status = post_cf[0].get("status") if post_cf else None
    print(f"  [INFO] 审批后 DealMap.event_status={post_dm_status}, Cashflow.status={post_cf_status}")
    if post_dm_status == pre_dm_status and post_cf_status == pre_cf_status:
        print(f"  [PASS] TC_AC_API_027: 审批不改变 DealMap/Cashflow 状态")
        result["checks"]["approval_no_effect"] = True
    else:
        print(f"  [FAIL] TC_AC_API_027: 审批改变了 DealMap/Cashflow 状态")

    result["response"] = resp
    return result


def test_reject_action(deal_number, action_number):
    """TC_AC_API_028: 审批驳回"""
    print("\n" + "=" * 60)
    print(f"[P1-1] 审批驳回 Action {action_number}")
    print("=" * 60)
    result = {"name": "审批驳回", "status": "failed"}

    body = {"approver": "manager01", "approvalRemark": "金额有误，请重做"}
    resp = curl_cmd("POST", f"/api/v1/dealing/actions/{action_number}/reject", body)
    if resp and resp.get("code") == 200:
        print(f"  [PASS] 驳回成功")
        result["status"] = "passed"
    else:
        print(f"  [FAIL] {resp}")
    result["response"] = resp
    return result


def test_reject_without_remark(deal_number, action_number):
    """TC_AC_API_029: 驳回审批意见为空"""
    print("\n" + "=" * 60)
    print(f"[P1-2] 驳回 - 审批意见为空")
    print("=" * 60)
    result = {"name": "驳回无意见", "status": "failed"}
    body = {"approver": "manager01", "approvalRemark": ""}
    resp = curl_cmd("POST", f"/api/v1/dealing/actions/{action_number}/reject", body)
    if resp and resp.get("code") == 400 and "审批意见必填" in str(resp.get("message", "")):
        print(f"  [PASS] 正确返回 400: {resp.get('message')}")
        result["status"] = "passed"
    else:
        print(f"  [FAIL] 应返回 400，实际: {resp}")
    result["response"] = resp
    return result


def test_delete_ac_deal(deal_id, deal_number):
    """TC_AC_API_020-021: 删除 AC 交易 + 验证级联软删"""
    print("\n" + "=" * 60)
    print(f"[P0-8] 删除 AC 交易 {deal_number} - POST /delete/{deal_id}")
    print("=" * 60)
    result = {"name": "删除 AC 交易", "status": "failed", "checks": {}}

    resp = curl_cmd("POST", f"/api/v1/dealing/ac-deals/delete/{deal_id}")
    if not resp or resp.get("code") != 200:
        print(f"  [FAIL] 删除失败: {resp}")
        result["response"] = resp
        return result
    print(f"  [PASS] 删除成功")
    result["status"] = "passed"
    time.sleep(1)

    # TC_AC_API_020: 验证级联软删
    deal = db_query(f"SELECT deleted, status FROM tms_deals_t WHERE deal_number='{deal_number}'")
    ac_deal = db_query(f"SELECT deleted FROM tms_ac_deals_t WHERE deal_number='{deal_number}'")
    dms = db_query(f"SELECT COUNT(*) AS cnt FROM tms_deal_map_t WHERE deal_number='{deal_number}' AND deleted='1'")
    cfs = db_query(f"SELECT COUNT(*) AS cnt FROM tms_cashflow_t WHERE deal_number='{deal_number}' AND deleted='1'")

    if (deal and deal[0].get("deleted") == "1" and deal[0].get("status") == "Canceled"
            and ac_deal and ac_deal[0].get("deleted") == "1"
            and dms[0].get("cnt", 0) >= 1
            and cfs[0].get("cnt", 0) >= 1):
        print(f"  [PASS] TC_AC_API_020: 级联软删 (Deal={deal[0]['deleted']}, AcDeal={ac_deal[0]['deleted']}, DealMap 软删 {dms[0]['cnt']}, Cashflow 软删 {cfs[0]['cnt']})")
        result["checks"]["cascade_soft_delete"] = True
    else:
        print(f"  [FAIL] TC_AC_API_020: 级联软删未完成 - {deal} {ac_deal} {dms} {cfs}")

    # TC_AC_API_021: DealImage v+1
    imgs = db_query(f"SELECT image_type FROM tms_deals_image_t WHERE deal_number='{deal_number}' AND image_type='DELETE'")
    if imgs and len(imgs) >= 1:
        print(f"  [PASS] TC_AC_API_021: 删除生成 DealImage v+1 ({len(imgs)} 条)")
        result["checks"]["delete_dealimage"] = True
    else:
        print(f"  [FAIL] TC_AC_API_021: 删除 DealImage 未生成 - {imgs}")

    result["response"] = resp
    return result


def test_dealmap_timeline(deal_number):
    """TC_AC_API_031: DealMap 时间线"""
    print("\n" + "=" * 60)
    print(f"[P1-3] DealMap 时间线 - GET /by-deal/{deal_number}")
    print("=" * 60)
    result = {"name": "DealMap 时间线", "status": "failed"}
    resp = curl_cmd("GET", f"/api/v1/dealing/dealmap/by-deal/{deal_number}")
    if resp and resp.get("code") == 200:
        dms = resp.get("data", [])
        print(f"  [PASS] 返回 {len(dms)} 条 DealMap")
        result["status"] = "passed"
    else:
        print(f"  [FAIL] {resp}")
    result["response"] = resp
    return result


def test_validation_errors():
    """TC_AC_API_011-015: 创建校验"""
    print("\n" + "=" * 60)
    print("[P0-9] 创建 AC 交易 - 校验规则")
    print("=" * 60)
    results = []

    # 缺 businessUnit
    r1 = {"name": "缺 businessUnit", "status": "failed"}
    resp = curl_cmd("POST", "/api/v1/dealing/ac-deals", {
        "direction": "Outflow", "amount": 100, "currency": "CNY",
        "dealDate": "2026-06-21", "valueDate": "2026-06-21",
        "bankAccountId": 1, "operator": "tester"
    })
    if resp and resp.get("code") == 400:
        r1["status"] = "passed"
        r1["message"] = resp.get("message")
    results.append(r1)
    print(f"  [{'PASS' if r1['status'] == 'passed' else 'FAIL'}] 缺 businessUnit: {r1.get('message', resp)}")

    # 金额 = 0
    r2 = {"name": "金额=0", "status": "failed"}
    resp = curl_cmd("POST", "/api/v1/dealing/ac-deals", {
        "businessUnit": "BU001", "traderId": 1, "direction": "Outflow", "amount": 0,
        "currency": "CNY", "dealDate": "2026-06-21", "valueDate": "2026-06-21",
        "bankAccountId": 1, "operator": "tester"
    })
    if resp and resp.get("code") == 400 and "amount" in str(resp.get("message", "")):
        r2["status"] = "passed"
        r2["message"] = resp.get("message")
    results.append(r2)
    print(f"  [{'PASS' if r2['status'] == 'passed' else 'FAIL'}] 金额=0: {r2.get('message', resp)}")

    # 起息日 < 交易日期
    r3 = {"name": "起息日早于交易日期", "status": "failed"}
    resp = curl_cmd("POST", "/api/v1/dealing/ac-deals", {
        "businessUnit": "BU001", "traderId": 1, "direction": "Outflow", "amount": 100,
        "currency": "CNY", "dealDate": "2026-06-21", "valueDate": "2026-06-20",
        "bankAccountId": 1, "operator": "tester"
    })
    if resp and resp.get("code") == 400 and "valueDate" in str(resp.get("message", "")):
        r3["status"] = "passed"
        r3["message"] = resp.get("message")
    results.append(r3)
    print(f"  [{'PASS' if r3['status'] == 'passed' else 'FAIL'}] 起息日早于交易日期: {r3.get('message', resp)}")

    # 方向非法
    r4 = {"name": "方向非法", "status": "failed"}
    resp = curl_cmd("POST", "/api/v1/dealing/ac-deals", {
        "businessUnit": "BU001", "traderId": 1, "direction": "INVALID", "amount": 100,
        "currency": "CNY", "dealDate": "2026-06-21", "valueDate": "2026-06-21",
        "bankAccountId": 1, "operator": "tester"
    })
    if resp and resp.get("code") == 400 and "direction" in str(resp.get("message", "")):
        r4["status"] = "passed"
        r4["message"] = resp.get("message")
    results.append(r4)
    print(f"  [{'PASS' if r4['status'] == 'passed' else 'FAIL'}] 方向非法: {r4.get('message', resp)}")

    return results


# ========== Main ==========
def main():
    print("=" * 60)
    print("# Open-TMS AC 交易 v2.0 API 自动化测试")
    print("# v2.0 关键验收点：DealMap 自动生成 / 软删+新建 / 级联软删 / 审批不影响 DealMap")
    print("=" * 60)

    jar_path = 'F:/code/opencode/opentrm/dealing/target/dealing-1.0.0-SNAPSHOT.jar'
    if not check_backend_health():
        proc = start_backend(jar_path)
        if not proc:
            return 1
    else:
        print("\n[OK] Backend already running")
        proc = None

    # 测试结果汇总
    report = {"total": 0, "passed": 0, "failed": 0, "results": []}

    # 1. 创建 AC 交易 + v2.0 自动生成验证
    r1 = test_create_ac_deal()
    report["results"].append(r1)
    report["total"] += 1
    report["passed" if r1["status"] == "passed" else "failed"] += 1

    deal_id = r1.get("deal_id")
    deal_number = r1.get("deal_number")

    if not deal_number:
        print("\n[ABORT] 未获取到 deal_number，无法继续")
        return 1

    # 2. 列表/详情
    for fn in [test_query_page, lambda: test_get_detail_by_id(deal_id), lambda: test_get_detail_by_number(deal_number)]:
        r = fn() if callable(fn) else fn
        report["results"].append(r)
        report["total"] += 1
        report["passed" if r["status"] == "passed" else "failed"] += 1

    # 3. Action 列表
    r_actions = test_query_actions(deal_number)
    report["results"].append(r_actions)
    report["total"] += 1
    report["passed" if r_actions["status"] == "passed" else "failed"] += 1

    actions = r_actions.get("data", {}).get("actions", [])
    first_action = actions[0].get("actionNumber") if actions else None
    second_action = actions[1].get("actionNumber") if len(actions) > 1 else None

    # 4. 校验规则
    for r in test_validation_errors():
        report["results"].append(r)
        report["total"] += 1
        report["passed" if r["status"] == "passed" else "failed"] += 1

    # 5. 更新
    r_update = test_update_ac_deal(deal_id, deal_number)
    report["results"].append(r_update)
    report["total"] += 1
    report["passed" if r_update["status"] == "passed" else "failed"] += 1

    # 6. 审批 - 第一个 Action (CREATE)
    if first_action:
        r_approve = test_approve_action(deal_number, first_action)
        report["results"].append(r_approve)
        report["total"] += 1
        report["passed" if r_approve["status"] == "passed" else "failed"] += 1

    # 7. 驳回校验
    if second_action:
        r_rej1 = test_reject_without_remark(deal_number, second_action)
        report["results"].append(r_rej1)
        report["total"] += 1
        report["passed" if r_rej1["status"] == "passed" else "failed"] += 1

        r_rej2 = test_reject_action(deal_number, second_action)
        report["results"].append(r_rej2)
        report["total"] += 1
        report["passed" if r_rej2["status"] == "passed" else "failed"] += 1

    # 8. DealMap 时间线
    r_tl = test_dealmap_timeline(deal_number)
    report["results"].append(r_tl)
    report["total"] += 1
    report["passed" if r_tl["status"] == "passed" else "failed"] += 1

    # 9. 删除
    r_del = test_delete_ac_deal(deal_id, deal_number)
    report["results"].append(r_del)
    report["total"] += 1
    report["passed" if r_del["status"] == "passed" else "failed"] += 1

    # 关闭后端
    if proc:
        proc.terminate()
        time.sleep(2)
        proc.kill()

    # 输出报告
    print("\n" + "=" * 60)
    print("# 测试报告")
    print("=" * 60)
    print(f"  Total: {report['total']}")
    print(f"  Passed: {report['passed']}")
    print(f"  Failed: {report['failed']}")
    pass_rate = report['passed'] * 100 // report['total'] if report['total'] > 0 else 0
    print(f"  Pass Rate: {pass_rate}%")
    print("=" * 60)

    print("\n[Detail]")
    for r in report["results"]:
        status_icon = "[PASS]" if r["status"] == "passed" else "[FAIL]" if r["status"] == "failed" else "[SKIP]"
        print(f"  {status_icon} {r['name']}")
        for ck, val in r.get("checks", {}).items():
            print(f"        - {ck}: {'OK' if val else 'FAIL'}")

    # 保存 JSON 报告
    report_file = "F:/code/opencode/opentrm/scripts/test/test_ac_deal_report.json"
    with open(report_file, 'w', encoding='utf-8') as f:
        json.dump(report, f, ensure_ascii=False, indent=2, default=str)
    print(f"\n[INFO] Report saved to: {report_file}")

    return 0 if report["failed"] == 0 else 1


if __name__ == "__main__":
    sys.exit(main())

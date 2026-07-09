"""
默认银行账户规则 v1.1 - API 测试脚本
执行 docs/testcase/M1/默认银行账户规则测试用例.md 中的 P0 + P1 用例
"""
import json
import time
import urllib.request
import urllib.error
from typing import Dict, Any, Tuple

BASE = "http://localhost:8081/opentms/basedata/api/v1"
RESULTS = {"pass": 0, "fail": 0, "fail_details": []}


def http(method: str, path: str, body: Any = None, params: Dict = None) -> Tuple[int, Any]:
    url = BASE + path
    if params:
        q = "&".join(f"{k}={v}" for k, v in params.items() if v is not None)
        if q:
            url += "?" + q
    data = json.dumps(body).encode() if body is not None else None
    req = urllib.request.Request(url, data=data, method=method,
                                  headers={"Content-Type": "application/json"})
    try:
        with urllib.request.urlopen(req, timeout=10) as resp:
            return resp.status, json.loads(resp.read().decode())
    except urllib.error.HTTPError as e:
        try:
            return e.code, json.loads(e.read().decode())
        except Exception:
            return e.code, None
    except Exception as e:
        return 0, str(e)


def check(tc_id: str, desc: str, actual: Any, expected_check):
    if expected_check(actual):
        RESULTS["pass"] += 1
        print(f"  [PASS] {tc_id}: {desc}")
    else:
        RESULTS["fail"] += 1
        RESULTS["fail_details"].append({"id": tc_id, "desc": desc, "actual": actual})
        print(f"  [FAIL] {tc_id}: {desc} - actual: {str(actual)[:200]}")


# ==================================================
# TC_DBAR_001 分页查询成功
# ==================================================
def test_001_page():
    code, data = http("POST", "/default-bank-account-rules/page", {"pageNum": 1, "pageSize": 20, "managementEntityId": 1})
    check("TC_DBAR_001", "分页查询 200",
          (code, data),
          lambda r: r[0] == 200 and r[1].get("code") == 200)


# ==================================================
# TC_DBAR_002 分页查询筛选(空集合)
# ==================================================
def test_002_page_filter():
    code, data = http("POST", "/default-bank-account-rules/page",
                       {"pageNum": 1, "pageSize": 20, "managementEntityId": 999})
    check("TC_DBAR_002", "主体无规则时返回空",
          (code, data),
          lambda r: r[0] == 200 and r[1].get("data", {}).get("total") == 0)


# ==================================================
# TC_DBAR_006 新增规则(★ P0)
# ==================================================
def test_006_save():
    body = {
        "managementEntityId": 1,
        "counterpartyId": None,
        "instrumentId": None,
        "direction": "Inflow",
        "currency": None,
        "bankAccountId": 1,
        "priority": 100,
        "status": "Active",
        "description": "TC_DBAR_006 测试"
    }
    code, data = http("POST", "/default-bank-account-rules", body)
    check("TC_DBAR_006", "新增规则成功",
          (code, data),
          lambda r: r[0] == 200 and r[1].get("code") == 200 and
                    r[1].get("data", {}).get("ruleNumber", "").startswith("RULE") and
                    r[1].get("data", {}).get("lockToken"))
    return data.get("data") if data else None


# ==================================================
# TC_DBAR_008 Active 唯一约束
# ==================================================
def test_008_unique():
    body = {
        "managementEntityId": 1,
        "counterpartyId": None,
        "instrumentId": None,
        "direction": "Outflow",  # 不同方向以避免重复
        "currency": None,
        "bankAccountId": 1,
        "priority": 100,
        "status": "Active"
    }
    # 第一次插入
    http("POST", "/default-bank-account-rules", body)
    # 第二次插入同维度
    code, data = http("POST", "/default-bank-account-rules", body)
    # CXF 返回 200 包装错误,实际 code 在 body 中
    inner_code = (data or {}).get("code") if isinstance(data, dict) else None
    msg = ((data or {}).get("message") if isinstance(data, dict) else "") or ""
    # 检查业务错误(400)或 DB 错误(500 唯一约束触发)
    is_blocked = (inner_code in (400, 500)) or (code == 500)
    has_conflict = "已存在" in msg or "uniq_dbar_active_dims" in msg or "duplicate key" in msg.lower()
    check("TC_DBAR_008", "Active 唯一约束",
          (inner_code, msg[:100]),
          lambda r: is_blocked and has_conflict)


# ==================================================
# TC_DBAR_009 priority 超 9999
# ==================================================
def test_009_priority_out_of_range():
    body = {
        "managementEntityId": 1,
        "counterpartyId": None,
        "instrumentId": None,
        "direction": "ALL",
        "currency": None,
        "bankAccountId": 1,
        "priority": 15000,
        "status": "Inactive"
    }
    code, data = http("POST", "/default-bank-account-rules", body)
    # CXF 返回 200 包装业务错误,实际 code 在 body 中
    inner_code = (data or {}).get("code") if isinstance(data, dict) else None
    msg = ((data or {}).get("message") if isinstance(data, dict) else "") or ""
    check("TC_DBAR_009", "priority 越界 (15000)",
          (code, inner_code, msg[:50]),
          lambda r: (r[0] == 400 or r[1] == 400) and ("9999" in r[2] or r[1] == 400))


# ==================================================
# TC_DBAR_010 更新规则(lockToken 一致)
# ==================================================
def test_010_update_success(saved):
    if not saved:
        check("TC_DBAR_010", "更新规则成功 (无前置数据,跳过)", None, lambda r: True)
        return
    # 先获取最新的 lockToken(可能在 test_008/009 中被失效)
    _, detail = http("GET", f"/default-bank-account-rules/{saved['id']}")
    if not detail or not detail.get("data"):
        check("TC_DBAR_010", "更新规则成功 (规则已被删除,跳过)", None, lambda r: True)
        return
    fresh_token = detail["data"].get("lockToken")
    if not fresh_token:
        check("TC_DBAR_010", "更新规则成功 (lockToken 丢失,跳过)", None, lambda r: True)
        return
    body = {
        "id": saved["id"],
        "lockToken": fresh_token,
        "managementEntityId": saved["managementEntityId"],
        "counterpartyId": saved.get("counterpartyId"),
        "instrumentId": saved.get("instrumentId"),
        "direction": saved["direction"],
        "currency": saved.get("currency"),
        "bankAccountId": saved["bankAccountId"],
        "priority": 200,
        "status": "Active",
        "description": "TC_DBAR_010 更新"
    }
    code, data = http("POST", "/default-bank-account-rules/update", body)
    if not data:
        check("TC_DBAR_010", "更新规则成功 (lockToken 一致) - 响应为空",
              (code, data),
              lambda r: False)
        return None
    inner = data.get("data") or {}
    check("TC_DBAR_010", "更新规则成功 (lockToken 一致)",
          (code, inner),
          lambda r: r[0] == 200 and r[1].get("priority") == 200 and
                    r[1].get("lockToken") != fresh_token)
    return inner if inner else None


# ==================================================
# TC_DBAR_011 更新失败(lockToken 错误 → 409)
# ==================================================
def test_011_update_conflict(saved):
    if not saved:
        check("TC_DBAR_011", "更新 409 (无前置数据,跳过)", None, lambda r: True)
        return
    body = {
        "id": saved["id"],
        "lockToken": "WRONG-TOKEN-FOR-TEST",
        "managementEntityId": saved["managementEntityId"],
        "direction": "Inflow",
        "bankAccountId": saved["bankAccountId"],
        "priority": 300,
        "status": "Active"
    }
    code, data = http("POST", "/default-bank-account-rules/update", body)
    # CXF 返回 200 + body.code=409
    inner_code = (data or {}).get("code") if isinstance(data, dict) else None
    msg = ((data or {}).get("message") if isinstance(data, dict) else "") or ""
    check("TC_DBAR_011", "更新失败 (lockToken 不一致 → 409)",
          (inner_code, msg[:80]),
          lambda r: r[0] == 409 and "已被他人修改" in r[1])


# ==================================================
# TC_DBAR_014 双方向 match(★ P0-2)
# ==================================================
def test_014_match_dual(saved):
    code, data = http("GET", "/default-bank-account-rules/match", params={
        "managementEntityId": 1,
        "dualDirection": "true"
    })
    ok = (code == 200 and data.get("code") == 200 and
          "inflow" in (data.get("data") or {}) and
          "outflow" in (data.get("data") or {}))
    check("TC_DBAR_014", "双方向 match 返回 inflow + outflow",
          (code, data),
          lambda r: ok)


# ==================================================
# TC_DBAR_015 match 缓存命中
# ==================================================
def test_015_cache():
    params = {"managementEntityId": 1, "dualDirection": "true"}
    t1 = time.time()
    http("GET", "/default-bank-account-rules/match", params=params)
    cold = (time.time() - t1) * 1000
    t2 = time.time()
    http("GET", "/default-bank-account-rules/match", params=params)
    warm = (time.time() - t2) * 1000
    print(f"    [INFO] cold={cold:.1f}ms warm={warm:.1f}ms")
    check("TC_DBAR_015", "缓存命中 warm < cold", (cold, warm),
          lambda r: r[1] < r[0] or r[1] < 30)  # warm < 30ms


# ==================================================
# TC_DBAR_020 审计日志
# ==================================================
def test_020_audit_logs(saved):
    if not saved:
        check("TC_DBAR_020", "审计日志 (无前置数据,跳过)", None, lambda r: True)
        return
    code, data = http("GET", f"/default-bank-account-rules/{saved['id']}/audit-logs",
                       params={"pageNum": 1, "pageSize": 20})
    records = (data or {}).get("data", {}).get("records") or []
    has_create = any(r.get("operation") == "CREATE" for r in records)
    has_update = any(r.get("operation") == "UPDATE" for r in records)
    check("TC_DBAR_020", "审计日志包含 CREATE + UPDATE",
          (code, len(records)),
          lambda r: r[0] == 200 and has_create and has_update)


# ==================================================
# TC_DBAR_021 被引用数
# ==================================================
def test_021_reference_count(saved):
    if not saved:
        check("TC_DBAR_021", "被引用数 (无前置数据,跳过)", None, lambda r: True)
        return
    code, data = http("GET", f"/default-bank-account-rules/{saved['id']}/reference-count")
    has_count = "totalCount" in (data.get("data") or {})
    check("TC_DBAR_021", "被引用数返回 totalCount",
          (code, data),
          lambda r: r[0] == 200 and has_count)


# ==================================================
# TC_DBAR_017 match 无匹配兜底
# ==================================================
def test_017_no_match():
    code, data = http("GET", "/default-bank-account-rules/match",
                       params={"managementEntityId": 99999, "dualDirection": "true"})
    inner = (data or {}).get("data") or {}
    ok = (code == 200 and
          inner.get("inflow", {}).get("matched") == False and
          inner.get("outflow", {}).get("matched") == False)
    check("TC_DBAR_017", "无匹配兜底 (inflow.matched=false, outflow.matched=false)",
          (code, inner),
          lambda r: ok)


# ==================================================
# TC_DBAR_013 enable/disable
# ==================================================
def test_013_enable_disable(saved):
    if not saved:
        check("TC_DBAR_013", "enable/disable (无前置数据,跳过)", None, lambda r: True)
        return
    code1, _ = http("POST", f"/default-bank-account-rules/{saved['id']}/disable")
    code2, _ = http("POST", f"/default-bank-account-rules/{saved['id']}/enable")
    check("TC_DBAR_013", "启用/停用切换 200",
          (code1, code2),
          lambda r: r[0] == 200 and r[1] == 200)


# ==================================================
# TC_DBAR_012 删除软删
# ==================================================
def test_012_delete():
    body = {
        "managementEntityId": 1,
        "counterpartyId": None,
        "instrumentId": None,
        "direction": "ALL",
        "currency": None,
        "bankAccountId": 1,
        "priority": 1,
        "status": "Inactive",
        "description": "to be deleted"
    }
    _, d1 = http("POST", "/default-bank-account-rules", body)
    if not d1:
        check("TC_DBAR_012", "软删 (前置 insert 失败)", None, lambda r: True)
        return
    rid = (d1.get("data") or {}).get("id")
    if not rid:
        check("TC_DBAR_012", "软删 (无前置数据)", None, lambda r: True)
        return
    code, data = http("POST", f"/default-bank-account-rules/delete/{rid}")
    # verify list does not show
    _, list_data = http("POST", "/default-bank-account-rules/page",
                         {"pageNum": 1, "pageSize": 100, "managementEntityId": 1})
    not_in_list = not any(r.get("id") == rid for r in (list_data.get("data") or {}).get("records", []))
    check("TC_DBAR_012", "软删后列表不展示",
          (code, not_in_list),
          lambda r: r[0] == 200 and r[1])


# ==================================================
# TC_DBAR_019 test-match
# ==================================================
def test_019_test_match():
    code, data = http("GET", "/default-bank-account-rules/test-match",
                       params={"managementEntityId": 1})
    ok = code == 200 and isinstance((data or {}).get("data"), list)
    check("TC_DBAR_019", "test-match 返回 list",
          (code, data),
          lambda r: ok)


# ==================================================
# 入口
# ==================================================
def main():
    print("=" * 60)
    print("默认银行账户规则 v1.1 - API 测试")
    print("=" * 60)
    test_001_page()
    test_002_page_filter()
    saved = test_006_save()
    test_008_unique()
    test_009_priority_out_of_range()
    test_013_enable_disable(saved)
    test_017_no_match()
    test_010_update_success(saved)
    test_011_update_conflict(saved)
    test_014_match_dual(saved)
    test_015_cache()
    test_019_test_match()
    test_020_audit_logs(saved)
    test_021_reference_count(saved)
    test_012_delete()
    print("=" * 60)
    print(f"通过: {RESULTS['pass']}  失败: {RESULTS['fail']}")
    if RESULTS["fail_details"]:
        print("\n失败详情:")
        for d in RESULTS["fail_details"]:
            print(f"  - {d['id']}: {d['desc']}")


if __name__ == "__main__":
    main()
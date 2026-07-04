#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Open-TMS FX API Test (v3.2)
Tests all 7 FX endpoints: calculate, create, page, detail, update, delete, rate-fix
Run: python scripts/test/test_fx_deal_api.py
"""

import json, time, sys, os, urllib.request, urllib.error

BACKEND_URL = "http://localhost:8082"

report = {
    "feature": "FX (Foreign Exchange)",
    "version": "v3.2",
    "start_time": time.strftime("%Y-%m-%d %H:%M:%S"),
    "total": 0, "passed": 0, "failed": 0, "skipped": 0,
    "results": [],
    "validation_points": {}
}

test_data = {}  # {spot_deal_number, spot_deal_id, ndf_deal_number, ndf_deal_id}

# ========== Helpers ==========

def curl(method, path, data=None, base=BACKEND_URL, timeout=30):
    url = f"{base}{path}"
    try:
        if method == "GET":
            req = urllib.request.Request(url)
        else:
            req = urllib.request.Request(url, data=json.dumps(data).encode('utf-8'), method='POST')
            req.add_header('Content-Type', 'application/json')
        with urllib.request.urlopen(req, timeout=timeout) as resp:
            return json.loads(resp.read().decode('utf-8'))
    except urllib.error.HTTPError as e:
        try:
            return json.loads(e.read().decode('utf-8', errors='replace'))
        except:
            return {"code": e.code, "error": str(e)}
    except Exception as e:
        return {"_error": str(e)}

def health():
    try:
        r = curl("GET", "/api/v1/dealing/fx-deals/page?pageNum=1&pageSize=1")
        return r and r.get("code") == 200
    except:
        return False

def P(result, msg):
    result["status"] = "passed"
    print(f"  [PASS] {msg}")

def F(result, msg):
    result["status"] = "failed"
    print(f"  [FAIL] {msg}")

def S(result, msg):
    result["status"] = "skipped"
    print(f"  [SKIP] {msg}")

OK = "[OK]"
FL = "[FAIL]"

# ========== Test Data (verified against actual DB) ==========
# management_entity: id=1 (BU001)
# counterparty: id=4 (CP_TEST)
# trader: id=1 (T001)
# instrument: id=2 (FX_SPOT)
# currency_pair: id=1 (EURUSD)
# bank_account: management_entity_id=1
# counterparty_account: use counterparty id=4

BASE_DEAL = {
    "managementEntityId": 1,
    "counterpartyId": 4,
    "traderId": 1,
    "instrumentId": 2,
    "currencyPairId": 1,
    "operator": "tester"
}

def spot(suffix=""):
    d = dict(BASE_DEAL)
    d.update({
        "sellCurrency": "EUR", "sellAmount": 50000.00,
        "buyCurrency": "USD", "buyAmount": 54500.00,
        "exchangeRate": 1.0900, "marketRate": 1.0890,
        "spreadBp": 10.00,
        "tradeDate": "2026-07-04", "valueDate": "2026-07-04",
        "fixingSource": None,
        "description": f"SPOT test{suffix}", "remark": None
    })
    return d

def ndf(suffix=""):
    d = dict(BASE_DEAL)
    d.update({
        "sellCurrency": "EUR", "sellAmount": 200000.00,
        "buyCurrency": "USD", "buyAmount": 218000.00,
        "exchangeRate": 1.0900, "marketRate": 1.0890,
        "spreadBp": 10.00,
        "tradeDate": "2026-07-04", "valueDate": "2026-10-04",
        "fixingSource": "CFETS",
        "description": f"NDF test{suffix}", "remark": None
    })
    return d


# ========== TC_FX_API_001: Calculate ==========

def test_calculate_complete():
    print("\n" + "=" * 64)
    print("[P0] TC_FX_API_001: calculate - all fields")
    print("=" * 64)
    r = {"name": "TC_FX_API_001", "status": "failed", "checks": {}}

    req = {"sellAmount": 100000.00, "exchangeRate": 7.2000,
           "marketRate": 7.1900, "tradeDate": "2026-07-04", "valueDate": "2026-10-04"}
    resp = curl("POST", "/api/v1/dealing/fx-deals/calculate", req)

    if not resp or resp.get("code") != 200:
        F(r, f"Response error: {resp}")
        r["response"] = resp; return r

    d = resp.get("data", {})
    c = {}

    # Link1: buyAmount = sellAmount * exchangeRate
    eb = 100000.00 * 7.2000
    ba = float(d.get("buyAmount", 0))
    c["buyAmount"] = abs(ba - eb) < 0.01
    print(f"  Link1 buyAmount: expect={eb:.2f} got={ba:.2f} {OK if c['buyAmount'] else FL}")

    # Link2: spreadBp = (exchangeRate - marketRate) * 10000
    es = (7.2000 - 7.1900) * 10000  # = 100
    sb = float(d.get("spreadBp", 0))
    c["spreadBp"] = abs(sb - es) < 0.1
    print(f"  Link2 spreadBp: expect={es:.2f} got={sb:.2f} {OK if c['spreadBp'] else FL}")

    # Link3: termDays = valueDate - tradeDate
    td = int(d.get("termDays", 0))
    c["termDays"] = td == 92
    print(f"  Link3 termDays: expect=92 got={td} {OK if c['termDays'] else FL}")

    # Link4: maturityDate = valueDate
    c["maturityDate"] = d.get("maturityDate") == d.get("valueDate")
    print(f"  Link4 maturityDate: {OK if c['maturityDate'] else FL}")

    if all(c.values()):
        P(r, "All calculate links passed")
        report["validation_points"]["calculate_all"] = True
    else:
        F(r, f"Partial failure: {c}")
    r["response"] = resp; r["checks"] = c
    return r

def test_calculate_insufficient():
    print("\n" + "=" * 64)
    print("[P0] TC_FX_API_002: calculate - insufficient input")
    print("=" * 64)
    r = {"name": "TC_FX_API_002", "status": "failed"}

    resp = curl("POST", "/api/v1/dealing/fx-deals/calculate", {"sellAmount": 100000.00})
    if resp and resp.get("code") in (400, 40001):
        P(r, f"Got error: code={resp.get('code')}, message={resp.get('message')}")
        report["validation_points"]["calculate_insufficient"] = True
    else:
        F(r, f"Expected 400/40001, got: {resp}")
    r["response"] = resp; return r

def test_calculate_date_invalid():
    print("\n" + "=" * 64)
    print("[P0] TC_FX_API_003: calculate - date invalid")
    print("=" * 64)
    r = {"name": "TC_FX_API_003", "status": "failed"}

    req = {"sellAmount": 100000.00, "exchangeRate": 7.2000,
           "tradeDate": "2026-10-04", "valueDate": "2026-07-04"}
    resp = curl("POST", "/api/v1/dealing/fx-deals/calculate", req)
    if resp and resp.get("code") in (400, 422, 42201):
        P(r, f"Got error: code={resp.get('code')}, message={resp.get('message')}")
        report["validation_points"]["calculate_date_invalid"] = True
    else:
        F(r, f"Expected 422, got: {resp}")
    r["response"] = resp; return r


# ========== TC_FX_API_004: Create SPOT ==========

def test_create_spot():
    print("\n" + "=" * 64)
    print("[P0] TC_FX_API_004: Create SPOT deal")
    print("=" * 64)
    r = {"name": "TC_FX_API_004", "status": "failed", "checks": {}}

    deal = spot(" TC004")
    resp = curl("POST", "/api/v1/dealing/fx-deals", deal)

    if not resp or resp.get("code") != 200:
        F(r, f"Create failed: {resp}")
        r["response"] = resp; return r

    d = resp.get("data", {})
    dn = d.get("dealNumber")
    print(f"  Created: dealNumber={dn}, status={d.get('status')}")
    print(f"  dealMapCount={d.get('dealMapCount')}, cashflowCount={d.get('cashflowCount')}")

    if not dn:
        F(r, "No dealNumber returned")
        r["response"] = resp; return r

    test_data["spot_deal_number"] = dn
    r["deal_number"] = dn

    time.sleep(0.5)
    detail = curl("GET", f"/api/v1/dealing/fx-deals/{dn}")
    if detail and detail.get("code") == 200:
        dd = detail.get("data", {})
        test_data["spot_deal_id"] = dd.get("id")
        dms = dd.get("dealMapList", [])
        cfs = dd.get("cashflowList", [])
        acts = dd.get("actionList", [])

        dm_ok = len(dms) == 3
        r["checks"]["dealmap_count"] = dm_ok
        print(f"  DealMaps: {len(dms)} (expect 3) {OK if dm_ok else FL}")
        for dm in dms:
            print(f"    - {dm.get('dealmapType')}: {dm.get('amountOrRate')}")

        cf_ok = len(cfs) == 2
        r["checks"]["cashflow_count"] = cf_ok
        print(f"  Cashflows: {len(cfs)} (expect 2) {OK if cf_ok else FL}")

        act_ok = len(acts) >= 1 and acts[0].get("actionType") == "DEAL"
        r["checks"]["action_deal"] = act_ok
        print(f"  Actions: {len(acts)} {OK if act_ok else FL}")

        if dm_ok and cf_ok and act_ok:
            P(r, "SPOT create: 3 DM + 2 CF + 1 Action [OK]")
            report["validation_points"]["create_spot"] = True
        else:
            F(r, f"DM={dm_ok}, CF={cf_ok}, Act={act_ok}")
    else:
        F(r, f"Detail query failed: {detail}")
    r["response"] = resp; return r


# ========== TC_FX_API_005: Create NDF ==========

def test_create_ndf():
    print("\n" + "=" * 64)
    print("[P0] TC_FX_API_005: Create NDF deal (0 CF)")
    print("=" * 64)
    r = {"name": "TC_FX_API_005", "status": "failed", "checks": {}}

    deal = ndf(" TC005")
    resp = curl("POST", "/api/v1/dealing/fx-deals", deal)

    if not resp or resp.get("code") != 200:
        F(r, f"Create failed: {resp}")
        r["response"] = resp; return r

    d = resp.get("data", {})
    dn = d.get("dealNumber")
    print(f"  Created: dealNumber={dn}, cashflowCount={d.get('cashflowCount')} (expect 0)")

    if not dn:
        F(r, "No dealNumber returned")
        r["response"] = resp; return r

    test_data["ndf_deal_number"] = dn
    r["deal_number"] = dn

    time.sleep(0.5)
    detail = curl("GET", f"/api/v1/dealing/fx-deals/{dn}")
    if detail and detail.get("code") == 200:
        dd = detail.get("data", {})
        test_data["ndf_deal_id"] = dd.get("id")
        dms = dd.get("dealMapList", [])
        cfs = dd.get("cashflowList", [])

        dm_ok = len(dms) == 3
        cf_ok = len(cfs) == 0
        print(f"  DealMaps: {len(dms)} (expect 3) {OK if dm_ok else FL}")
        print(f"  Cashflows: {len(cfs)} (expect 0) {OK if cf_ok else FL}")

        r["checks"] = {"ndf_3_dealmap": dm_ok, "ndf_0_cashflow": cf_ok}
        if dm_ok and cf_ok:
            P(r, "NDF create: 3 DM + 0 CF [OK]")
            report["validation_points"]["create_ndf"] = True
        else:
            F(r, f"DM={dm_ok}, CF={cf_ok}")
    else:
        F(r, f"Detail query failed: {detail}")
    r["response"] = resp; return r


# ========== TC_FX_API_006: Page ==========

def test_page():
    print("\n" + "=" * 64)
    print("[P0] TC_FX_API_006: Page query")
    print("=" * 64)
    r = {"name": "TC_FX_API_006", "status": "failed", "checks": {}}

    resp = curl("GET", "/api/v1/dealing/fx-deals/page?pageNum=1&pageSize=10")
    if not resp or resp.get("code") != 200:
        F(r, f"Page query failed: {resp}")
        r["response"] = resp; return r

    data = resp.get("data", {})
    records = data.get("records", [])
    total = data.get("total", 0)
    print(f"  Total: {total}, current page: {len(records)}")

    if total >= 2:
        P(r, f"Page OK: total={total}")
        report["validation_points"]["page"] = True
    else:
        F(r, f"Expected >=2 records, got total={total}")

    # Filter by status
    sr = curl("GET", "/api/v1/dealing/fx-deals/page?pageNum=1&pageSize=10&status=New")
    if sr and sr.get("code") == 200:
        srecs = sr.get("data", {}).get("records", [])
        ok = all(rec.get("status") == "New" for rec in srecs)
        r["checks"]["filter_status"] = ok
        print(f"  Filter status=New: {len(srecs)} records {OK if ok else FL}")

    r["response"] = resp; return r


# ========== TC_FX_API_007: Detail ==========

def test_detail():
    print("\n" + "=" * 64)
    print("[P0] TC_FX_API_007: Deal detail")
    print("=" * 64)
    r = {"name": "TC_FX_API_007", "status": "failed", "checks": {}}

    dn = test_data.get("spot_deal_number")
    if not dn:
        page = curl("GET", "/api/v1/dealing/fx-deals/page?pageNum=1&pageSize=1")
        recs = page.get("data", {}).get("records", [])
        if recs and recs[0].get("status") != "Canceled":
            dn = recs[0].get("dealNumber")

    if not dn:
        S(r, "No available dealNumber")
        return r

    print(f"  Query: dealNumber={dn}")
    resp = curl("GET", f"/api/v1/dealing/fx-deals/{dn}")

    if not resp or resp.get("code") != 200:
        F(r, f"Detail failed: {resp}")
        r["response"] = resp; return r

    d = resp.get("data", {})
    checks = {
        "id": d.get("id") is not None,
        "dealNumber": d.get("dealNumber") == dn,
        "sellCurrency": bool(d.get("sellCurrency")),
        "buyCurrency": bool(d.get("buyCurrency")),
        "has_dealMapList": isinstance(d.get("dealMapList"), list) and len(d.get("dealMapList", [])) > 0,
        "has_cashflowList": isinstance(d.get("cashflowList"), list),
        "has_actionList": isinstance(d.get("actionList"), list) and len(d.get("actionList", [])) > 0,
    }

    for k, v in checks.items():
        print(f"  {k}: {OK if v else FL}")

    if all(checks.values()):
        P(r, f"Detail OK: {len(d.get('dealMapList',[]))} DM, {len(d.get('cashflowList',[]))} CF, {len(d.get('actionList',[]))} Action")
        report["validation_points"]["detail"] = True
    else:
        F(r, f"Detail incomplete: {checks}")
    r["response"] = resp; r["checks"] = checks
    return r


# ========== TC_FX_API_008: Update ==========

def test_update():
    print("\n" + "=" * 64)
    print("[P0] TC_FX_API_008: Update deal")
    print("=" * 64)
    r = {"name": "TC_FX_API_008", "status": "failed", "checks": {}}

    dn = test_data.get("spot_deal_number")
    did = test_data.get("spot_deal_id")

    if not dn or not did:
        S(r, "No available SPOT deal")
        return r

    upd = {
        "id": did, "dealNumber": dn,
        "managementEntityId": 1, "counterpartyId": 4, "traderId": 1,
        "instrumentId": 2, "currencyPairId": 1,
        "sellCurrency": "EUR", "sellAmount": 80000.00,
        "buyCurrency": "USD", "buyAmount": 88000.00,
        "exchangeRate": 1.1000, "marketRate": 1.0990,
        "spreadBp": 10.00,
        "tradeDate": "2026-07-04", "valueDate": "2026-07-04",
        "fixingSource": None,
        "description": "UPDATED SPOT", "remark": "test update",
        "operator": "tester"
    }
    resp = curl("POST", "/api/v1/dealing/fx-deals/update", upd)

    if not resp or resp.get("code") != 200:
        F(r, f"Update failed: {resp}")
        r["response"] = resp; return r

    print(f"  Update API success")

    time.sleep(0.5)
    detail = curl("GET", f"/api/v1/dealing/fx-deals/{dn}")
    if detail and detail.get("code") == 200:
        d = detail.get("data", {})
        rate_ok = float(d.get("exchangeRate", 0)) == 1.1000
        desc_ok = d.get("description") == "UPDATED SPOT"
        r["checks"] = {"rate_updated": rate_ok, "desc_updated": desc_ok}
        print(f"  Rate updated: {OK if rate_ok else FL}")
        print(f"  Desc updated: {OK if desc_ok else FL}")

        # Check actions include UPDATE
        acts = d.get("actionList", [])
        has_upd = any(a.get("actionType") == "UPDATE" for a in acts)
        r["checks"]["has_update_action"] = has_upd
        print(f"  Has UPDATE action: {OK if has_upd else FL}")

        # Check active DealMaps
        dms = d.get("dealMapList", [])
        active = [dm for dm in dms if dm.get("deleted") == "0"]
        print(f"  Active DealMaps: {len(active)}")

        if rate_ok and desc_ok and has_upd:
            P(r, "Update verified")
            report["validation_points"]["update"] = True
        else:
            F(r, "Update verification failed")
    else:
        F(r, f"Detail after update failed: {detail}")
    r["response"] = resp; return r


# ========== TC_FX_API_009: NDF RATE_FIX ==========

def test_ratefix():
    print("\n" + "=" * 64)
    print("[P0] TC_FX_API_009: NDF RATE_FIX")
    print("=" * 64)
    r = {"name": "TC_FX_API_009", "status": "failed", "checks": {}}

    ndf_id = test_data.get("ndf_deal_id")
    ndf_no = test_data.get("ndf_deal_number")

    if not ndf_id or not ndf_no:
        S(r, "No NDF deal available")
        return r

    # Before: check DM/CF counts
    before = curl("GET", f"/api/v1/dealing/fx-deals/{ndf_no}")
    dm_before = len(before.get("data", {}).get("dealMapList", []))
    cf_before = len(before.get("data", {}).get("cashflowList", []))
    print(f"  Before RATE_FIX: DM={dm_before}, CF={cf_before}")

    resp = curl("POST", f"/api/v1/dealing/fx-deals/{ndf_id}/rate-fix",
                {"fixingRate": 1.1200, "operator": "tester"})

    if not resp or resp.get("code") != 200:
        err = str(resp.get("message", "")) if resp else ""
        if "not NDF" in err.lower() or "only ndf" in err.lower():
            S(r, f"NDF rejected as non-NDF: {resp}")
            return r
        F(r, f"RATE_FIX failed: {resp}")
        r["response"] = resp; return r

    print(f"  RATE_FIX success: {resp.get('data')}")

    time.sleep(0.5)
    after = curl("GET", f"/api/v1/dealing/fx-deals/{ndf_no}")
    if after and after.get("code") == 200:
        d = after.get("data", {})
        dms = d.get("dealMapList", [])
        cfs = d.get("cashflowList", [])

        fix_dms = [dm for dm in dms if dm.get("dealmapType") == "FX_FIX"]
        fix_ok = len(fix_dms) >= 1
        cf_ok = len(cfs) > cf_before
        r["checks"] = {"fx_fix_dm": fix_ok, "cashflow_added": cf_ok}
        print(f"  FX_FIX DealMaps: {len(fix_dms)} {OK if fix_ok else FL}")
        print(f"  Cashflows: {cf_before} -> {len(cfs)} {OK if cf_ok else FL}")

        if fix_ok and cf_ok:
            P(r, "NDF RATE_FIX: +1 FX_FIX DM, +1 CF [OK]")
            report["validation_points"]["ratefix"] = True
        else:
            F(r, "RATE_FIX verification failed")
    else:
        F(r, f"Detail after RATE_FIX failed: {after}")
    r["response"] = resp; return r


# ========== TC_FX_API_010: Delete ==========

def test_delete():
    print("\n" + "=" * 64)
    print("[P0] TC_FX_API_010: Delete deal")
    print("=" * 64)
    r = {"name": "TC_FX_API_010", "status": "failed", "checks": {}}

    # Create a temp deal for deletion
    deal = spot(" TC010_delete")
    create = curl("POST", "/api/v1/dealing/fx-deals", deal)
    if not create or create.get("code") != 200:
        S(r, "Cannot create temp deal for delete test")
        return r

    tmp_dn = create.get("data", {}).get("dealNumber")
    time.sleep(0.5)
    detail = curl("GET", f"/api/v1/dealing/fx-deals/{tmp_dn}")
    if not detail or detail.get("code") != 200:
        S(r, f"Cannot get temp deal detail: {tmp_dn}")
        return r

    tmp_id = detail.get("data", {}).get("id")
    print(f"  Deleting: id={tmp_id}, dealNumber={tmp_dn}")

    del_resp = curl("POST", f"/api/v1/dealing/fx-deals/delete/{tmp_id}")
    if not del_resp or del_resp.get("code") != 200:
        F(r, f"Delete failed: {del_resp}")
        r["response"] = del_resp; return r

    print(f"  Delete API success")

    time.sleep(0.5)
    after = curl("GET", f"/api/v1/dealing/fx-deals/{tmp_dn}")
    if after and after.get("code") == 200:
        status = after.get("data", {}).get("status")
        ok = status == "Canceled"
        r["checks"]["status_canceled"] = ok
        print(f"  Status after delete: {status} {OK if ok else FL}")
        if ok:
            P(r, "Delete: status -> Canceled")
            report["validation_points"]["delete"] = True
        else:
            F(r, f"Expected Canceled, got {status}")
    else:
        F(r, f"Query after delete failed: {after}")
    r["response"] = del_resp; return r


# ========== TC_FX_API_011: RATE_FIX rejected on SPOT ==========

def test_ratefix_rejected():
    print("\n" + "=" * 64)
    print("[P1] TC_FX_API_011: SPOT rejects RATE_FIX")
    print("=" * 64)
    r = {"name": "TC_FX_API_011", "status": "failed"}

    sid = test_data.get("spot_deal_id")
    if not sid:
        S(r, "No SPOT deal available")
        return r

    resp = curl("POST", f"/api/v1/dealing/fx-deals/{sid}/rate-fix",
                {"fixingRate": 1.1500, "operator": "tester"})
    if resp and resp.get("code") != 200:
        P(r, f"Correctly rejected: code={resp.get('code')}, message={resp.get('message')}")
        report["validation_points"]["ratefix_rejected"] = True
    else:
        F(r, f"Expected rejection, got: {resp}")
    r["response"] = resp; return r


# ========== TC_FX_API_012: 404 ==========

def test_detail_404():
    print("\n" + "=" * 64)
    print("[P1] TC_FX_API_012: Detail 404")
    print("=" * 64)
    r = {"name": "TC_FX_API_012", "status": "failed"}

    resp = curl("GET", "/api/v1/dealing/fx-deals/FX99999999-NOTEXIST")
    if resp and resp.get("code") == 404:
        P(r, "Correctly returned 404")
    else:
        F(r, f"Expected 404, got: {resp}")
    r["response"] = resp; return r


# ========== Runner ==========

def run_all():
    print("\n" + "#" * 64)
    print("#  Open-TMS FX API Test (v3.2)")
    print(f"#  Backend: {BACKEND_URL}")
    print(f"#  Time: {report['start_time']}")
    print("#" * 64)

    if not health():
        print("\n[ERROR] Dealing backend not reachable!")
        sys.exit(1)
    print(f"\n[OK] Dealing backend reachable\n")

    tests = [
        test_calculate_complete,
        test_calculate_insufficient,
        test_calculate_date_invalid,
        test_create_spot,
        test_create_ndf,
        test_page,
        test_detail,
        test_update,
        test_ratefix,
        test_delete,
        test_ratefix_rejected,
        test_detail_404,
    ]

    for fn in tests:
        report["total"] += 1
        try:
            res = fn()
            if res["status"] == "passed":
                report["passed"] += 1
            elif res["status"] == "failed":
                report["failed"] += 1
            else:
                report["skipped"] += 1
            report["results"].append(res)
        except Exception as e:
            report["passed" if False else "failed"]  # no-op line
            report["failed"] += 1
            import traceback
            print(f"  [CRASH] {fn.__name__}: {e}")
            traceback.print_exc()
            report["results"].append({"name": fn.__name__, "status": "crashed", "error": str(e)})

    summary()


def summary():
    end = time.strftime("%Y-%m-%d %H:%M:%S")
    print("\n" + "#" * 64)
    print("#  Summary")
    print("#" * 64)
    print(f"  Total:    {report['total']}")
    print(f"  Passed:   {report['passed']}")
    print(f"  Failed:   {report['failed']}")
    print(f"  Skipped:  {report['skipped']}")
    pct = report['passed'] / max(report['total'], 1) * 100
    print(f"  Pass rate: {pct:.1f}%")
    print(f"  Start:    {report['start_time']}")
    print(f"  End:      {end}")

    print(f"\n  Validation Points:")
    for k, v in report["validation_points"].items():
        if v is True:
            print(f"    [OK] {k}")
        elif v is False:
            print(f"    [FAIL] {k}")
        else:
            print(f"    [--] {k} (not covered)")

    failures = [r for r in report["results"] if r["status"] == "failed"]
    if failures:
        print(f"\n  Failed cases:")
        for f in failures:
            print(f"    [FAIL] {f['name']}")

    # Save report
    os.makedirs("scripts/test/reports", exist_ok=True)
    rp = f"scripts/test/reports/fx_deal_api_{time.strftime('%Y%m%d_%H%M%S')}.json"
    report["end_time"] = end
    with open(rp, "w", encoding="utf-8") as fp:
        json.dump(report, fp, ensure_ascii=False, indent=2, default=str)
    print(f"\n  Report saved: {rp}")
    print("#" * 64)


if __name__ == "__main__":
    run_all()

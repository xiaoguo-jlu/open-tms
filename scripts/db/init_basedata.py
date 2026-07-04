#!/usr/bin/env python3
"""
Open-TMS 基础数据初始化脚本
向 basedata 模块填充真实的基础数据。
先查询后插入，按 code 判断是否已存在。

Usage: python scripts/db/init_basedata.py
"""

import subprocess
import json
import time
import sys

BASE_URL = "http://localhost:8081/opentms/basedata"

# Statistics
stats = {}


def curl(method, path, data=None):
    """Execute curl command and return (response_dict, http_code)."""
    url = f"{BASE_URL}{path}"
    cmd = ["curl", "-s", "-X", method, "-w", "\n%{http_code}", url]
    if data:
        payload = json.dumps(data, ensure_ascii=False)
        cmd.extend(["-H", "Content-Type: application/json; charset=UTF-8", "-d", payload])

    try:
        result = subprocess.run(cmd, capture_output=True, text=True, timeout=30, encoding='utf-8', errors='replace')
        output = result.stdout.strip()
        if not output:
            return None, 0, "Empty response"

        # Separate HTTP code from body
        lines = output.split('\n')
        http_code = lines[-1].strip() if len(lines) > 1 else "000"
        body = '\n'.join(lines[:-1]) if len(lines) > 1 else ""

        if not body:
            return None, int(http_code), "Empty body"

        resp = json.loads(body)
        return resp, int(http_code), None
    except subprocess.TimeoutExpired:
        return None, 0, "Timeout"
    except json.JSONDecodeError as e:
        return None, 0, f"JSON parse error: {e}, body: {body[:200]}"
    except Exception as e:
        return None, 0, f"Exception: {e}"


def page_exists(resource, keyword):
    """Check if a record exists by searching with keyword (code)."""
    path = f"/api/v1/{resource}/page?keyword={keyword}&current=1&size=1"
    resp, code, err = curl("GET", path)
    if resp and resp.get("code") == 200:
        records = resp.get("data", {}).get("records", [])
        # Check if any record has matching code
        for r in records:
            # Try both 'code' and field-specific code names
            for key in ['code', 'instrumentCode', 'pairCode', 'accountNo']:
                if r.get(key) == keyword:
                    return True, r
        return False, None
    return False, None


def insert_if_not_exists(resource, payload, code_field='code'):
    """Insert a record if it doesn't already exist."""
    code_value = payload.get(code_field)
    category = resource.replace('-', ' ').title()
    stats.setdefault(category, {"inserted": 0, "skipped": 0, "errors": 0})

    if not code_value:
        print(f"  [ERROR] No {code_field} in payload: {payload}")
        stats[category]["errors"] += 1
        return False

    exists, existing = page_exists(resource, code_value)
    if exists:
        print(f"  [SKIP] {code_value}: already exists (id={existing.get('id')})")
        stats[category]["skipped"] += 1
        return existing
    else:
        print(f"  [INSERT] Creating {code_value}...", end=" ")
        resp, code, err = curl("POST", f"/api/v1/{resource}", payload)
        if resp and resp.get("code") == 200:
            print(f"OK (HTTP {code})")
            stats[category]["inserted"] += 1
            return True
        else:
            msg = resp.get('message', 'Unknown') if resp else (err or 'Unknown error')
            print(f"FAIL (HTTP {code}): {msg}")
            stats[category]["errors"] += 1
            return False


def main():
    print("=" * 60)
    print("# Open-TMS 基础数据初始化")
    print(f"# 时间: {time.strftime('%Y-%m-%d %H:%M:%S')}")
    print("=" * 60)

    # ===== 1. Management Entities =====
    print("\n### 1. Management Entities (管理主体) ###")
    mes = [
        {"code": "HZ_BRANCH", "name": "杭州分公司", "entityType": "SUBSIDIARY", "parentCode": "BU001", "levelDepth": 2, "status": "1", "createdBy": "system"},
        {"code": "SH_BRANCH", "name": "上海分公司", "entityType": "SUBSIDIARY", "parentCode": "BU001", "levelDepth": 2, "status": "1", "createdBy": "system"},
        {"code": "SZ_BRANCH", "name": "深圳分公司", "entityType": "SUBSIDIARY", "parentCode": "BU001", "levelDepth": 2, "status": "1", "createdBy": "system"},
    ]
    me_ids = {}
    for me in mes:
        result = insert_if_not_exists("management-entities", me)
        if isinstance(result, dict):
            me_ids[me['code']] = result['id']

    # ===== 2. Counterparties =====
    print("\n### 2. Counterparties (交易对手) ###")
    cps = [
        {"code": "BOC", "name": "中国银行", "counterpartyType": "BANK", "countryCode": "CN", "swiftCode": "BKCHCNBJ", "status": "1"},
        {"code": "ICBC", "name": "工商银行", "counterpartyType": "BANK", "countryCode": "CN", "swiftCode": "ICBKCNBJ", "status": "1"},
        {"code": "CITI", "name": "花旗银行", "counterpartyType": "BANK", "countryCode": "US", "swiftCode": "CITIUS33", "status": "1"},
        {"code": "HSBC", "name": "汇丰银行", "counterpartyType": "BANK", "countryCode": "HK", "swiftCode": "HSBCHKHH", "status": "1"},
    ]
    cp_ids = {}
    for cp in cps:
        result = insert_if_not_exists("counterparties", cp)
        if isinstance(result, dict):
            cp_ids[cp['code']] = result['id']

    # ===== 3. Traders =====
    print("\n### 3. Traders (交易员) ###")
    traders = [
        {"code": "TRADER_LB", "name": "李白", "department": "资金部", "status": "1"},
        {"code": "TRADER_DF", "name": "杜甫", "department": "资金部", "status": "1"},
        {"code": "TRADER_WW", "name": "王维", "department": "风险管理部", "status": "1"},
    ]
    for t in traders:
        insert_if_not_exists("traders", t)

    # ===== 4. Instruments =====
    print("\n### 4. Instruments (金融工具) ###")
    instruments = [
        {"instrumentCode": "FX_SPOT_01", "instrumentName": "FX-SPOT", "instrumentType": "FX", "currency": "USD", "status": "1"},
        {"instrumentCode": "FX_FWD_01", "instrumentName": "FX-FWD", "instrumentType": "FX", "currency": "USD", "status": "1"},
        {"instrumentCode": "FX_NDF_01", "instrumentName": "FX-NDF", "instrumentType": "FX", "currency": "USD", "status": "1"},
    ]
    for inst in instruments:
        insert_if_not_exists("instruments", inst, code_field='instrumentCode')

    # ===== 5. Currencies =====
    print("\n### 5. Currencies (币种) ###")
    currencies_needed = [
        {"code": "SGD", "name": "新加坡元", "symbol": "S$", "decimalPlaces": 2, "status": "1"},
        {"code": "HKD", "name": "港元", "symbol": "HK$", "decimalPlaces": 2, "status": "1"},
        {"code": "GBP", "name": "英镑", "symbol": "£", "decimalPlaces": 2, "status": "1"},
        {"code": "JPY", "name": "日元", "symbol": "¥", "decimalPlaces": 0, "status": "1"},
        {"code": "AUD", "name": "澳大利亚元", "symbol": "A$", "decimalPlaces": 2, "status": "1"},
    ]
    for curr in currencies_needed:
        insert_if_not_exists("currencies", curr)

    # ===== 6. Currency Pairs =====
    print("\n### 6. Currency Pairs (币种对) ###")
    pairs = [
        {"pairCode": "USDSGD", "currency1": "USD", "currency2": "SGD", "strongerCurrency": "USD", "bidDecimal": 4, "askDecimal": 4, "status": "1"},
        {"pairCode": "USDHKD", "currency1": "USD", "currency2": "HKD", "strongerCurrency": "USD", "bidDecimal": 4, "askDecimal": 4, "status": "1"},
        {"pairCode": "EURGBP", "currency1": "EUR", "currency2": "GBP", "strongerCurrency": "EUR", "bidDecimal": 4, "askDecimal": 4, "status": "1"},
        {"pairCode": "USDJPY", "currency1": "USD", "currency2": "JPY", "strongerCurrency": "USD", "bidDecimal": 4, "askDecimal": 4, "status": "1"},
        {"pairCode": "AUDUSD", "currency1": "AUD", "currency2": "USD", "strongerCurrency": "USD", "bidDecimal": 4, "askDecimal": 4, "status": "1"},
        {"pairCode": "EURJPY", "currency1": "EUR", "currency2": "JPY", "strongerCurrency": "EUR", "bidDecimal": 4, "askDecimal": 4, "status": "1"},
    ]
    for pair in pairs:
        insert_if_not_exists("currency-pairs", pair, code_field='pairCode')

    # ===== 7. Query IDs for Bank Accounts =====
    print("\n### 7. Bank Accounts (银行账户) - Fetching reference IDs ###")

    # Re-query management entities for their IDs
    me_ids = {}
    for code in ["HZ_BRANCH", "SH_BRANCH", "SZ_BRANCH"]:
        exists, record = page_exists("management-entities", code)
        if record:
            me_ids[code] = record['id']
            print(f"  [INFO] Management Entity {code}: id={record['id']}")
        else:
            print(f"  [WARN] Management Entity {code} not found!")
            me_ids[code] = None

    # Query counterparty IDs
    cp_ids = {}
    for code in ["BOC", "ICBC", "CITI", "HSBC"]:
        exists, record = page_exists("counterparties", code)
        if record:
            cp_ids[code] = record['id']
            print(f"  [INFO] Counterparty {code}: id={record['id']}")
        else:
            print(f"  [WARN] Counterparty {code} not found!")
            cp_ids[code] = None

    # Create bank accounts
    print("\n### Creating Bank Accounts ###")
    bank_accounts = [
        {
            "accountNo": "HZ_CNY_001",
            "accountName": "杭州分公司CNY基本户",
            "managementEntityId": me_ids.get("HZ_BRANCH"),
            "currency": "CNY",
            "accountType": "BASIC",
            "status": "1",
            "createdBy": "system",
        },
        {
            "accountNo": "HZ_USD_001",
            "accountName": "杭州分公司USD基本户",
            "managementEntityId": me_ids.get("HZ_BRANCH"),
            "currency": "USD",
            "accountType": "BASIC",
            "status": "1",
            "createdBy": "system",
        },
        {
            "accountNo": "SH_CNY_001",
            "accountName": "上海分公司CNY基本户",
            "managementEntityId": me_ids.get("SH_BRANCH"),
            "currency": "CNY",
            "accountType": "BASIC",
            "status": "1",
            "createdBy": "system",
        },
    ]
    for ba in bank_accounts:
        if ba.get("managementEntityId") is None:
            print(f"  [SKIP] {ba['accountNo']}: management entity not found")
            stats.setdefault("Bank Accounts", {"inserted": 0, "skipped": 1, "errors": 0})
            stats["Bank Accounts"]["skipped"] += 1
        else:
            insert_if_not_exists("bank-accounts", ba, code_field='accountNo')

    # ===== 8. Counterparty Accounts =====
    print("\n### 8. Counterparty Accounts (对手方账户) ###")
    ca_accounts = [
        {
            "accountNo": "BOC_CNY_001",
            "accountName": "中国银行CNY账户",
            "counterpartyId": cp_ids.get("BOC"),
            "currency": "CNY",
            "status": "1",
            "createdBy": "system",
        },
        {
            "accountNo": "ICBC_CNY_001",
            "accountName": "工商银行CNY账户",
            "counterpartyId": cp_ids.get("ICBC"),
            "currency": "CNY",
            "status": "1",
            "createdBy": "system",
        },
        {
            "accountNo": "CITI_USD_001",
            "accountName": "花旗银行USD账户",
            "counterpartyId": cp_ids.get("CITI"),
            "currency": "USD",
            "status": "1",
            "createdBy": "system",
        },
    ]
    for ca in ca_accounts:
        if ca.get("counterpartyId") is None:
            print(f"  [SKIP] {ca['accountNo']}: counterparty not found")
            stats.setdefault("Counterparty Accounts", {"inserted": 0, "skipped": 1, "errors": 0})
            stats["Counterparty Accounts"]["skipped"] += 1
        else:
            insert_if_not_exists("counterparty-accounts", ca, code_field='accountNo')

    # ===== Summary =====
    print("\n" + "=" * 60)
    print("# 初始化汇总")
    print("=" * 60)
    total_inserted = 0
    total_skipped = 0
    total_errors = 0
    for category, s in stats.items():
        i, sk, e = s.get("inserted", 0), s.get("skipped", 0), s.get("errors", 0)
        total_inserted += i
        total_skipped += sk
        total_errors += e
        status_icon = "OK" if e == 0 else "ERROR"
        print(f"  [{status_icon}] {category}: 新增={i}, 跳过={sk}, 错误={e}")

    print(f"\n  Total: 新增={total_inserted}, 跳过={total_skipped}, 错误={total_errors}")
    print("=" * 60)

    return 0 if total_errors == 0 else 1


if __name__ == "__main__":
    sys.exit(main())

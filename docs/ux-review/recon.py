"""
真实 Open-TMS 页面 UX Reconnaissance
- 3 服务已起: basedata 8081 / dealing 8082 / Vite 3000
- 采集 9 个关键页面的 full_page 截图 + 关键 DOM 信息
- 产物: docs/ux-review/screenshots/real/*.png
"""
import sys
import json
from pathlib import Path
from playwright.sync_api import sync_playwright

BASE = "http://localhost:3000"
OUT = Path("docs/ux-review/screenshots/real")
OUT.mkdir(parents=True, exist_ok=True)

# (path, 名称, 等待时间 ms)
PAGES = [
    ("/basedata/bank-account", "BankAccountList", 3000),
    ("/basedata/currency-pair", "CurrencyPairList", 2500),
    ("/basedata/subsidiary", "SubsidiaryList", 2500),
    ("/basedata/default-bank-account-rule", "DefaultBankAccountRuleList", 3000),
    ("/dealing/ac-deal", "AcDealList", 3000),
    ("/dealing/at-deal", "AtDealList", 3000),
    ("/dealing/fx-deal", "FxDealList", 3000),
    ("/dealing/ac-deal/detail", "AcDealDetail", 3500),
    ("/dealing/fx-deal/detail", "FxDealDetail", 3500),
]

results = []

with sync_playwright() as p:
    browser = p.chromium.launch(headless=True)
    ctx = browser.new_context(viewport={"width": 1440, "height": 900})
    page = ctx.new_page()

    console_errors = []
    page.on("console", lambda m: console_errors.append({"type": m.type, "text": m.text[:300]}) if m.type == "error" else None)
    page.on("pageerror", lambda e: console_errors.append({"type": "pageerror", "text": str(e)[:300]}))

    for path, name, wait_ms in PAGES:
        url = BASE + path
        print(f"[scan] {name}  {url}")
        try:
            resp = page.goto(url, wait_until="networkidle", timeout=20000)
            page.wait_for_timeout(wait_ms)
            status = resp.status if resp else "?"

            # 截全屏
            png = OUT / f"{name}.png"
            page.screenshot(path=str(png), full_page=True)
            png_size = png.stat().st_size

            # 提取关键 DOM 信息
            dom = page.evaluate("""() => {
                const cards = document.querySelectorAll('.el-card, .filter-card, .table-card');
                const filterItems = document.querySelectorAll('.el-form-item');
                const tableCols = document.querySelectorAll('.el-table__header th');
                const tableRows = document.querySelectorAll('.el-table__row');
                const buttons = document.querySelectorAll('button');
                const elDialogs = document.querySelectorAll('.el-dialog');
                const elAlerts = document.querySelectorAll('.el-alert');
                return {
                    card_count: cards.length,
                    filter_item_count: filterItems.length,
                    table_col_count: tableCols.length,
                    table_col_titles: Array.from(tableCols).map(c => c.innerText.trim().split('\\n')[0].slice(0, 30)),
                    table_row_count: tableRows.length,
                    button_count: buttons.length,
                    button_texts: Array.from(buttons).slice(0, 20).map(b => b.innerText.trim().slice(0, 30)),
                    dialog_visible: elDialogs.length,
                    alert_count: elAlerts.length,
                    alert_texts: Array.from(elAlerts).map(a => a.innerText.trim().slice(0, 100)),
                    body_h: document.body.scrollHeight,
                    viewport_h: window.innerHeight,
                    title: document.title,
                }
            }""")
            dom["status"] = status
            dom["path"] = path
            dom["name"] = name
            dom["png_kb"] = png_size // 1024
            dom["console_errors"] = [e for e in console_errors if e["type"] in ("error", "pageerror")][-5:]
            console_errors.clear()
            results.append(dom)
            print(f"        status={status} rows={dom['table_row_count']} cols={dom['table_col_count']} buttons={dom['button_count']} body_h={dom['body_h']}px")
        except Exception as e:
            print(f"        [ERR] {e}")
            results.append({"name": name, "path": path, "error": str(e)[:200]})

    browser.close()

# 写 JSON 报告
out_json = Path("docs/ux-review/screenshots/real/_recon.json")
out_json.write_text(json.dumps(results, ensure_ascii=False, indent=2), encoding="utf-8")
print(f"\n[REPORT] {out_json}  ({len(results)} pages)")
print(f"[SCREENSHOTS] {OUT}")

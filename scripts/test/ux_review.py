"""UX Review Script - Visits 7 pages and captures UX data with Playwright"""
from playwright.sync_api import sync_playwright
import json
import os
import sys

BASE = "http://localhost:3000"
OUT_DIR = "F:/code/opencode/opentrm/scripts/test/reports/ux-review"

# Pages to review: (name, url, expected mode actions)
PAGES = [
    ("ac-list",     "/dealing/ac-deal"),
    ("ac-detail",   "/dealing/ac-deal/detail/AC202607050001"),
    ("ac-edit",     "/dealing/ac-deal/detail/AC202607050001?mode=edit"),
    ("at-list",     "/dealing/at-deal"),
    ("at-detail",   "/dealing/at-deal/detail"),
    ("at-edit",     "/dealing/at-deal/detail?mode=edit"),
    ("fx-list",     "/dealing/fx-deal"),
    ("fx-detail",   "/dealing/fx-deal/detail"),
    ("fx-edit",     "/dealing/fx-deal/detail?mode=edit"),
    ("mgmt-entity", "/basedata/management-entity"),
    ("instrument",  "/basedata/instrument"),
]

def capture_page(page, name, url, results):
    """Visit page and capture UX data"""
    full_url = BASE + url
    print(f"\n=== {name}: {url} ===")
    errors = []
    console_msgs = []
    network_errors = []

    def on_console(msg):
        if msg.type in ("error", "warning"):
            console_msgs.append(f"[{msg.type}] {msg.text[:200]}")

    def on_response(resp):
        if resp.status >= 400:
            try:
                body = resp.text()[:300] if resp.status >= 500 else ""
            except Exception:
                body = ""
            network_errors.append(f"{resp.status} {resp.url} {body}")

    page.on("console", on_console)
    page.on("response", on_response)

    try:
        page.goto(full_url, wait_until="networkidle", timeout=15000)
        page.wait_for_timeout(1500)  # Allow tables to render
    except Exception as e:
        errors.append(f"NAV ERROR: {str(e)[:200]}")

    # Screenshot full page
    screenshot_path = os.path.join(OUT_DIR, f"{name}-full.png")
    try:
        page.screenshot(path=screenshot_path, full_page=True)
    except Exception as e:
        errors.append(f"SCREENSHOT ERROR: {str(e)[:100]}")

    # Capture viewport-only screenshot
    vp_path = os.path.join(OUT_DIR, f"{name}-viewport.png")
    try:
        page.screenshot(path=vp_path, full_page=False)
    except Exception as e:
        pass

    # Detect 404 / blank
    body_text = ""
    try:
        body_text = page.inner_text("body")[:500]
    except Exception:
        pass

    is_404 = "404" in body_text or "找不到" in body_text or "Not Found" in body_text

    # Inspect buttons
    buttons_info = []
    try:
        btns = page.locator("button:visible").all()
        for b in btns[:25]:
            try:
                txt = b.inner_text().strip()
                if txt:
                    buttons_info.append(txt)
            except Exception:
                pass
    except Exception:
        pass

    # Inspect inputs
    inputs_info = []
    try:
        inputs = page.locator("input:visible, .el-input__inner:visible").all()
        for i in inputs[:25]:
            try:
                ph = i.get_attribute("placeholder") or ""
                val = i.get_attribute("value") or i.input_value() or ""
                if ph or val:
                    inputs_info.append(f"placeholder='{ph[:30]}' value='{val[:30]}'")
            except Exception:
                pass
    except Exception:
        pass

    # Table info
    table_info = {}
    try:
        rows = page.locator(".el-table__row").all()
        table_info["row_count"] = len(rows)
        if rows:
            header_cells = page.locator(".el-table__header th").all()
            headers = []
            for h in header_cells:
                try:
                    headers.append(h.inner_text().strip())
                except Exception:
                    pass
            table_info["headers"] = headers
            # First row data
            first_row_cells = rows[0].locator(".el-table__cell").all()
            cells = []
            for c in first_row_cells[:15]:
                try:
                    cells.append(c.inner_text().strip()[:30])
                except Exception:
                    pass
            table_info["first_row"] = cells
    except Exception as e:
        table_info["error"] = str(e)[:100]

    # Look for tags / status colors
    status_tags = []
    try:
        tags = page.locator(".el-tag:visible").all()
        for t in tags[:15]:
            try:
                status_tags.append(t.inner_text().strip())
            except Exception:
                pass
    except Exception:
        pass

    result = {
        "name": name,
        "url": url,
        "is_404": is_404,
        "body_excerpt": body_text[:200],
        "buttons": buttons_info,
        "inputs": inputs_info[:10],
        "table": table_info,
        "status_tags": status_tags,
        "console_msgs": console_msgs[:20],
        "network_errors": network_errors[:20],
        "errors": errors,
    }
    results.append(result)

    page.remove_listener("console", on_console)
    page.remove_listener("response", on_response)

    return result

def main():
    results = []
    with sync_playwright() as p:
        browser = p.chromium.launch(headless=True)
        context = browser.new_context(viewport={"width": 1440, "height": 900})
        page = context.new_page()

        for name, url in PAGES:
            try:
                capture_page(page, name, url, results)
            except Exception as e:
                results.append({"name": name, "url": url, "fatal": str(e)})

        browser.close()

    # Save JSON results
    out_json = os.path.join(OUT_DIR, "ux-review-results.json")
    with open(out_json, "w", encoding="utf-8") as f:
        json.dump(results, f, ensure_ascii=False, indent=2)
    print(f"\nSaved: {out_json}")

    # Summary
    print("\n=== SUMMARY ===")
    for r in results:
        if "fatal" in r:
            print(f"  [FATAL] {r['name']}: {r['fatal'][:100]}")
            continue
        flag = " [404]" if r["is_404"] else ""
        net_err = len(r["network_errors"])
        con_err = len([m for m in r["console_msgs"] if "[error]" in m])
        rows = r.get("table", {}).get("row_count", "-")
        btns = len(r["buttons"])
        print(f"  {r['name']:15s}{flag}  rows={rows}  btns={btns}  net_err={net_err}  console_err={con_err}")

if __name__ == "__main__":
    main()
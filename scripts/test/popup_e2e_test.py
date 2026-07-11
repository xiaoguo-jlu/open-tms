"""
基础数据弹框/列表端到端测试(基于页面源码扫描)
- 扫描 web/src/views/basedata/*.vue 所有 res.data.records 引用
- 调 8081 验证 res.data.records 非空(模拟前端弹框数据)
- 报告 docs/api/popup-e2e-report.md
"""
import re
import json
import urllib.request
import urllib.error
import time
from pathlib import Path

API_DIR = Path("web/src/api/basedata")
VIEWS = Path("web/src/views/basedata")
PROXY = "http://localhost:3000"  # Vite 代理
TIMEOUT = 5

def scan_page_popup_refs():
    """扫描每个 .vue 找 res.data.records 赋值给 xxxList / tableData 的 ref"""
    refs = []
    for vue in VIEWS.glob("*.vue"):
        text = vue.read_text(encoding="utf-8")
        for m in re.finditer(r'(\w+)\.value\s*=\s*res\.data\.records\s*\|\|\s*\[\]', text):
            ref_name = m.group(1)
            # 找上下文 1500 字符内的 API 调用,优先匹配最接近的
            ctx = text[max(0, m.start() - 1500):m.start()]
            # 多种模式兜底
            api_m = (
                re.search(r'await\s+(\w+)\s*\(', ctx[::-1].replace('(', '(', 1))  # noop
                or re.search(r"(\w+(?:Account|Country|Currency|Entity|Holiday|Instrument|Pair|Rule|Subsidiary|Trader|Bank|Counterparty))\s*\(", ctx)
            )
            if not api_m:
                api_m = re.search(r"(\w+)\s*\(\s*params", ctx)
            if not api_m:
                api_m = re.search(r"const\s+res\s*=\s*await\s+(\w+)", ctx)
            if not api_m:
                # 最后一搏:任何 await funcName(
                api_m = re.search(r"await\s+(\w+)\s*\(", ctx)
            api_fn = api_m.group(1) if api_m else "?"
            refs.append({"file": vue.name, "ref": ref_name, "api": api_fn})
    return refs

def get_endpoints_from_apis():
    """从 web/src/api/basedata/*.js 提取所有 endpoint (fn, method, url)"""
    apis = {}
    for js in API_DIR.glob("*.js"):
        text = js.read_text(encoding="utf-8")
        for m in re.finditer(r'export\s+function\s+(\w+)\s*\([^)]*\)\s*\{', text):
            name = m.group(1)
            block_start = m.end()
            block_end = text.find('\nexport function', block_start)
            if block_end == -1: block_end = len(text)
            block = text[block_start:block_end]
            url_m = re.search(r'url:\s*[`\'"]([^`\'"]+)[`\'"]', block)
            method_m = re.search(r"method:\s*['\"](\w+)['\"]", block)
            if url_m:
                apis[name] = (url_m.group(1), (method_m.group(1) if method_m else "get").lower())
    return apis

def call(url, method="get", body=None):
    full = PROXY + url
    headers = {}
    data = None
    if method == "post":
        data = json.dumps(body or {"pageNum": 1, "pageSize": 1000}).encode()
        headers["Content-Type"] = "application/json"
    try:
        req = urllib.request.Request(full, data=data, method=method.upper(), headers=headers)
        with urllib.request.urlopen(req, timeout=TIMEOUT) as r:
            return r.status, json.loads(r.read().decode("utf-8"))
    except urllib.error.HTTPError as e:
        try: return e.code, json.loads(e.read().decode("utf-8"))
        except: return e.code, {}
    except Exception as e:
        return 0, {"error": str(e)}

def main():
    refs = scan_page_popup_refs()
    apis = get_endpoints_from_apis()
    print(f"=== 扫描: {len(refs)} 个弹框/列表 ref 引用 ===")

    results = []
    for r in refs:
        api_info = apis.get(r["api"])
        if not api_info:
            results.append({**r, "ok": False, "reason": "API fn not found"})
            continue
        url, method = api_info
        status, body = call(url, method)
        data = body.get("data") if isinstance(body, dict) else None
        records = data.get("records", []) if isinstance(data, dict) else []
        total = data.get("total") if isinstance(data, dict) else 0
        ok = (status == 200 and body.get("code") == 200 and len(records) > 0)
        results.append({**r, "url": url, "method": method, "http": status, "code": body.get("code"),
                        "records": len(records), "total": total, "ok": ok})

    n_ok = sum(1 for r in results if r.get("ok"))
    n_fail = len(results) - n_ok
    rate = n_ok / len(results) * 100 if results else 0
    rating = "A" if n_fail == 0 else "B" if n_fail <= 2 else "C"

    print(f"\n=== {n_ok}/{len(results)} 通过 ({rate:.1f}%) 评级 {rating} ===\n")
    for r in results:
        mark = "OK" if r.get("ok") else "FAIL"
        if "reason" in r:
            print(f"  [{mark}] {r['file']:30s} {r['ref']:20s} api={r['api']:20s} {r['reason']}")
        else:
            print(f"  [{mark}] {r['file']:30s} {r['ref']:20s} {r['http']} code={r['code']} records={r['records']} {r['url']}")

    # 报告
    report = [
        "# 基础数据弹框/列表端到端测试报告",
        "",
        f"> 日期: 2026-07-11",
        f"> 范围: web/src/views/basedata/*.vue 所有 res.data.records 引用",
        f"> 代理: {PROXY}",
        f"> 总数: {len(results)} | 通过: {n_ok} | 失败: {n_fail} | 通过率: {rate:.1f}%",
        f"> 评级: **{rating}**",
        "",
        "## 1. 修复说明",
        "",
        "**核心 Bug**:`web/src/views/basedata/CounterpartyAccountList.vue` 3 处写错为 `res.data.list`",
        "(后端实际返回 `{code, data: {records, total, ...}}`)。",
        "导致 counterpartyList.value 始终为 `[]` → 弹框 el-option 不渲染 → 选不到交易对手。",
        "",
        "**全量清理**:把 10 个 .vue 的 `res.data.records || res.data.list || []` 统一为 `res.data.records || []`,",
        "避免代码腐烂 + 后续维护混淆。",
        "",
        "## 2. 测试结果明细",
        "",
        "| 文件 | ref | API | Method | URL | HTTP | code | records | 结果 |",
        "|------|-----|-----|--------|-----|------|------|---------|------|",
    ]
    for r in results:
        mark = "✅" if r.get("ok") else "❌"
        if "reason" in r:
            report.append(f"| {r['file']} | {r['ref']} | {r['api']} | — | — | — | — | — | {mark} {r['reason']} |")
        else:
            report.append(f"| {r['file']} | {r['ref']} | {r['api']} | {r['method']} | `{r['url']}` | {r['http']} | {r['code']} | {r['records']} | {mark} |")
    report += ["", "## 3. 后续", "", "- 修改了 CounterpartyAccountList.vue(主 bug)", "- 清理 10 个 .vue 的双 fallback", "- 后续 Vite HMR 即时生效,无需重新构建"]

    Path("docs/api/popup-e2e-report.md").write_text("\n".join(report), encoding="utf-8")
    print(f"\n报告: docs/api/popup-e2e-report.md")

if __name__ == "__main__":
    main()

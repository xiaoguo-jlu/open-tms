#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Open-TMS 前端 API 一致性扫描器 — 自测脚本
=====================================================================
目的: 用 4 个故意带错 / 多参 / 少参的样本文件验证检测能力。

测试样本放在: web/src/api/_test_fixture.js  (扫描后删除)

测试步骤:
  1. 备份 web/src/api/_test_fixture.js 不存在 → 创建
  2. 跑 api_scanner.py (走默认流程) → 断言:
       a) P0 ≥ 1 (路径错)
       b) P0 ≥ 1 (缺失必传 query)
       c) P0 ≥ 1 (缺失必传 body 字段)
       d) P0 ≥ 1 (多余 body 字段)
       e) P2 至少 1 (静态分析跳过)
  3. 跑 --ci 模式 → 断言 exit code == 1
  4. 跑 --json 模式 → 断言 JSON 文件存在且 grade 字段
  5. 删除测试样本
"""
from __future__ import annotations

import json
import shutil
import subprocess
import sys
from pathlib import Path

PROJECT_ROOT = Path(__file__).resolve().parent.parent
FIXTURE = PROJECT_ROOT / "web" / "src" / "api" / "_test_fixture.js"
SCANNER = PROJECT_ROOT / "scripts" / "api_scanner.py"
REPORT_HTML = PROJECT_ROOT / "docs" / "api" / "frontend-api-consistency.html"
REPORT_JSON = PROJECT_ROOT / "docs" / "api" / "frontend-api-scanner-test.json"

# Windows: Python 子进程默认 GBK,导致中文 stdout 触发 UnicodeDecodeError。
# 强制子进程走 UTF-8,父进程读取时用 errors='replace' 兜底。
SUBPROC_ENV = {**__import__("os").environ, "PYTHONIOENCODING": "utf-8", "PYTHONUTF8": "1"}

# 测试样本 — 含 4 个 export function, 故意制造:
#   fixtureBadPath        → 路径错 (P0 path)
#   fixtureMissingQuery   → 缺失必传 query (P0 query-missing) — 比对 /api/v1/bank-accounts/page 必传不强制, 改用 /api/v1/default-bank-account-rules/test-match 缺 managementEntityId
#   fixtureExtraBody      → 多余 body 字段 (P0 body-extra) — /api/v1/bank-accounts POST 多传 unknownField
#   fixtureMissingBody    → 缺失必传 body (P0 body-missing) — /api/v1/dealing/fx-deals POST 缺必传字段
#   fixtureNonLiteral     → params 是变量 (P2 static-analyze)
FIXTURE_CONTENT = """import request from '@/utils/request'

// 1) 路径错 — 故意写一个不存在的端点
export function fixtureBadPath() {
  return request({
    url: '/api/v1/this-path-does-not-exist/list',
    method: 'get'
  })
}

// 2) 缺失必传 query — default-bank-account-rules/test-match 要求 managementEntityId
export function fixtureMissingQuery() {
  return request({
    url: '/api/v1/default-bank-account-rules/test-match',
    method: 'get',
    params: { currency: 'USD' }
  })
}

// 3) 多余 body 字段 — bank-accounts POST schema 没有 unknownField
export function fixtureExtraBody() {
  return request({
    url: '/api/v1/bank-accounts',
    method: 'post',
    data: {
      accountNo: '001',
      accountName: '测试',
      unknownField: 'should-not-be-here',
      anotherExtra: 42
    }
  })
}

// 4) 缺失必传 body 字段 — fx-deals POST 必传 buyAmount 等
export function fixtureMissingBody() {
  return request({
    url: '/api/v1/dealing/fx-deals',
    method: 'post',
    data: {
      remark: '只传了一个字段, 必传 buyAmount/sellAmount 都没传'
    }
  })
}

// 5) 非字面量 params — 静态分析跳过
export function fixtureNonLiteral(opts) {
  return request({
    url: '/api/v1/currencies/page',
    method: 'get',
    params: opts
  })
}
"""


def banner(msg: str) -> None:
    print(f"\n=== {msg} ===")


def main() -> int:
    if not SCANNER.exists():
        print(f"[FAIL] 找不到 {SCANNER}")
        return 1

    # 1. 创建样本
    if FIXTURE.exists():
        FIXTURE.unlink()
    FIXTURE.parent.mkdir(parents=True, exist_ok=True)
    FIXTURE.write_text(FIXTURE_CONTENT, encoding="utf-8")
    print(f"[setup] 写入样本: {FIXTURE}")

    failures = []
    try:
        # 2. 跑默认扫描
        banner("Step 1: 默认扫描 (期望发现 P0 + P2)")
        r = subprocess.run(
            [sys.executable, str(SCANNER), "--json", str(REPORT_JSON)],
            cwd=str(PROJECT_ROOT), capture_output=True, text=True,
            encoding="utf-8", errors="replace", env=SUBPROC_ENV,
        )
        print(r.stdout)
        if r.returncode != 0:
            print(f"[FAIL] 默认扫描退出码 {r.returncode}: {r.stderr}")
            failures.append("default-exit-code")
        if not REPORT_JSON.exists():
            failures.append("json-missing")
        else:
            data = json.loads(REPORT_JSON.read_text(encoding="utf-8"))
            issues = data.get("issues", [])
            print(f"[info] 报告 grade={data.get('grade')}  P0={data['summary']['p0']} P1={data['summary']['p1']} P2={data['summary']['p2']}")
            cats = {i["category"] for i in issues}
            print(f"[info] 命中 category: {sorted(cats)}")
            assert_set = {"path", "query-missing", "body-extra", "body-missing", "static-analyze"}
            missing = assert_set - cats
            if missing:
                failures.append(f"categories-missing:{sorted(missing)}")
            else:
                print("[PASS] 5 类预期 issue 全部命中")

        # 3. --ci 模式
        banner("Step 2: --ci 模式 (期望 exit 1)")
        r2 = subprocess.run(
            [sys.executable, str(SCANNER), "--ci"],
            cwd=str(PROJECT_ROOT), capture_output=True, text=True,
            encoding="utf-8", errors="replace", env=SUBPROC_ENV,
        )
        if r2.returncode == 1:
            tail = (r2.stdout or "").splitlines()[-3:]
            print(f"[PASS] --ci 模式正确退出 1 (stdout tail: {tail})")
        else:
            print(f"[FAIL] --ci 模式退出码 {r2.returncode} (期望 1) stderr: {r2.stderr}")
            failures.append("ci-exit-code")

        # 4. 报告 HTML 存在 + 大小 < 200KB
        banner("Step 3: HTML 报告存在 + 体积")
        if REPORT_HTML.exists():
            sz = REPORT_HTML.stat().st_size
            print(f"[info] HTML 报告: {REPORT_HTML} size={sz/1024:.1f}KB")
            if sz > 200 * 1024:
                failures.append("html-too-large")
            else:
                print("[PASS] HTML 报告 < 200KB")
        else:
            failures.append("html-missing")

    finally:
        # 5. 清理
        banner("Step 4: 清理")
        if FIXTURE.exists():
            FIXTURE.unlink()
            print(f"[cleanup] 删除 {FIXTURE}")
        if REPORT_JSON.exists():
            REPORT_JSON.unlink()
            print(f"[cleanup] 删除 {REPORT_JSON}")

    if failures:
        print(f"\n[FAIL] 自测失败: {failures}")
        return 1
    print("\n[PASS] 自测全部通过")
    return 0


if __name__ == "__main__":
    sys.exit(main())

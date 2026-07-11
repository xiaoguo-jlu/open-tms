#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Open-TMS 前端 API 一致性扫描工具 (frontend-api-scanner v1.0)
=====================================================================
纯静态分析工具 — 不跑后端,只对比契约。

功能:
  1. 拉取/加载 OpenAPI 规范 (docs/api/openapi.json 或基于数据/交易 在线)
  2. 扫描 web/src/api/**/*.{js,ts} 的 export function,提取 url/method/params/data
  3. 在 OpenAPI 中匹配 path + method,然后:
       - 路径检查:  模板字符串里的 ${var} 与 OpenAPI 路径变量名对比
       - query 参数: 多余 = P1, 缺失 = P0, 命名不一致 = P2
       - body 参数: 字段多余/缺失/类型错 = P0 (按 jsonschema 简化规则对比)
  4. 产出 HTML 报告 + JSON 数据;--ci 模式有 P0 时 exit 1

依赖: 仅 Python 3.8+ 标准库 (json, re, argparse, urllib, dataclasses, html 等)

用法:
  python scripts/api_scanner.py                                  # 默认扫
  python scripts/api_scanner.py --api-dir web/src/api
  python scripts/api_scanner.py --openapi docs/api/openapi.json
  python scripts/api_scanner.py --ci                            # CI 模式
  python scripts/api_scanner.py --json out.json                 # 额外 JSON
  python scripts/api_scanner.py --report-html docs/api/frontend-api-consistency.html
  python scripts/api_scanner.py --gen-openapi                   # 自动先 bash gen-openapi.sh
"""
from __future__ import annotations

import argparse
import dataclasses
import datetime
import html
import json
import os
import re
import subprocess
import sys
import urllib.error
import urllib.request
from collections import defaultdict
from dataclasses import dataclass, field
from pathlib import Path
from typing import Any, Dict, Iterable, List, Optional, Tuple

# ----------------------------------------------------------------------------
# 报告源标识 — 区别于 opentms-review-frontend 模板的 "Frontend · ..."
# ----------------------------------------------------------------------------
REPORT_SOURCE = "frontend-api-scanner"
REPORT_VERSION = "1.0"
TEMPLATE_TITLE = "Open-TMS 前端 API 一致性扫描报告"


# ============================================================================
# 数据模型
# ============================================================================
@dataclass
class ParamDecl:
    """前端单个 params/data 字段的声明 (在 export function 内)"""
    name: str
    decl_kind: str            # 'literal-key' / 'spread' / 'computed' / 'inline-obj'
    is_static: bool           # True=字面量对象,False=无法静态分析
    inline_fields: List[str] = field(default_factory=list)
    literal_value: Any = None
    source: str = "params"    # "params" 或 "data"


@dataclass
class FrontendApiCall:
    """一个 export function 的 API 调用声明"""
    file: str                  # 相对项目根
    name: str                  # export function name
    line: int                  # 起始行
    url: str                   # 原始 url 字面量
    method: str                # get/post/put/delete/...
    params: Optional[ParamDecl] = None
    data: Optional[ParamDecl] = None
    extra_options: List[Tuple[str, str]] = field(default_factory=list)  # (key, value) like responseType:'blob'
    raw_body: str = ""         # 原始片段(用于报告调试)


@dataclass
class Issue:
    """一个静态分析 issue"""
    severity: str              # P0 / P1 / P2
    category: str              # path / path-param / query-missing / query-extra / query-name / body-missing / body-extra / body-type / method / static-analyze
    file: str
    api: str
    line: int
    title: str                 # 一句话标题
    detail: str                # 详细描述
    standard: str              # 期望 (来自 OpenAPI)
    current: str               # 现状 (来自前端)
    fix: str = ""              # 修复建议


# ============================================================================
# 1. 加载 OpenAPI 规范
# ============================================================================
def load_openapi_from_file(path: Path) -> Dict[str, Any]:
    """从 JSON 文件加载 OpenAPI"""
    with path.open("r", encoding="utf-8") as f:
        return json.load(f)


def fetch_openapi_from_url(url: str, timeout: int = 5) -> Optional[Dict[str, Any]]:
    """从 HTTP 端点获取 OpenAPI (失败返回 None)"""
    try:
        with urllib.request.urlopen(url, timeout=timeout) as resp:
            data = resp.read()
        return json.loads(data.decode("utf-8"))
    except (urllib.error.URLError, urllib.error.HTTPError, json.JSONDecodeError, OSError) as exc:
        print(f"[warn] 拉取 {url} 失败: {exc}", file=sys.stderr)
        return None


def load_openapi(args, project_root: Path) -> Tuple[Dict[str, Any], str]:
    """
    加载 OpenAPI 规范。
    优先级: --openapi 显式指定 > 本地默认 docs/api/openapi.json > 在线端点(若启动)
    """
    openapi_path: Optional[Path] = None
    if args.openapi:
        openapi_path = (project_root / args.openapi).resolve() if not Path(args.openapi).is_absolute() else Path(args.openapi)
    else:
        default_path = project_root / "docs" / "api" / "openapi.json"
        if default_path.exists():
            openapi_path = default_path

    if openapi_path and openapi_path.exists():
        print(f"[info] 从文件加载 OpenAPI: {openapi_path}")
        return load_openapi_from_file(openapi_path), f"file:{openapi_path}"

    # 尝试在线
    print("[info] 本地 openapi.json 不存在,尝试在线端点 (8081/8082)…")
    basedata = fetch_openapi_from_url("http://localhost:8081/api/v1/openapi/cxf-spec")
    dealing = fetch_openapi_from_url("http://localhost:8082/v3/api-docs")
    if not basedata and not dealing:
        sys.exit("[ERROR] 找不到 OpenAPI — 既无文件也无在线端点。请先执行 `bash scripts/gen-openapi.sh`")
    return merge_openapi(basedata or {"paths": {}}, dealing or {"paths": {}}, "url:8081+8082")


def merge_openapi(a: Dict[str, Any], b: Dict[str, Any], source: str) -> Dict[str, Any]:
    """简单合并两个 OpenAPI 文档 (dealing 优先)"""
    out = {
        "openapi": "3.0.1",
        "info": {"title": "Open-TMS Combined (runtime merged)"},
        "paths": {},
        "components": {"schemas": {}},
    }
    for p, ops in (a.get("paths") or {}).items():
        out["paths"][p] = ops
    for n, s in (a.get("components", {}).get("schemas") or {}).items():
        out["components"]["schemas"][n] = s
    for p, ops in (b.get("paths") or {}).items():
        out["paths"][p] = ops
    for n, s in (b.get("components", {}).get("schemas") or {}).items():
        out["components"]["schemas"][n] = s
    return out


# ============================================================================
# 2. 解析前端 API 调用
# ============================================================================
# 匹配: export function NAME(args) { return request({...}) }
EXPORT_FN_RE = re.compile(
    r"export\s+function\s+(?P<name>[A-Za-z_$][\w$]*)\s*\((?P<args>[^)]*)\)\s*\{",
    re.MULTILINE,
)
# 匹配 url:'...' 或 url:"..." 或 url:`...`
URL_RE = re.compile(
    r"""url\s*:\s*(?P<q>['"`])(?P<v>.*?)(?P=q)\s*,""",
    re.DOTALL,
)
# 匹配 method:'...' / method:"..."
METHOD_RE = re.compile(
    r"""method\s*:\s*(?P<q>['"`])(?P<v>[A-Za-z]+)(?P=q)"""
)
# 匹配 params: <expr>  (expr 截到下一个 , 或 } 顶层,简化)
# 简化策略: 找到 "params:" 后的第一个顶层 "," 或 "}\n" 边界
PARAMS_RE = re.compile(r"params\s*:\s*", re.MULTILINE)
DATA_RE = re.compile(r"\bdata\s*:\s*", re.MULTILINE)


def slice_field_value(body: str, start: int) -> Tuple[str, int]:
    """
    从 `start` 位置开始截取一个字段值。
    规则:
      - { ... }  → 配平大括号
      - [...]    → 配平中括号
      - '...' / "..." / `...` → 整个字符串
      - 标识符   → 截到 , / } / \n
    返回 (值字符串, 结束索引(指向值之后))
    """
    # 跳过空白
    i = start
    while i < len(body) and body[i] in " \t\r\n":
        i += 1
    if i >= len(body):
        return "", i
    ch = body[i]
    if ch == "{":
        depth = 0
        j = i
        in_str = None
        while j < len(body):
            c = body[j]
            if in_str:
                if c == "\\":
                    j += 2
                    continue
                if c == in_str:
                    in_str = None
            else:
                if c in ("'", '"', "`"):
                    in_str = c
                elif c == "{":
                    depth += 1
                elif c == "}":
                    depth -= 1
                    if depth == 0:
                        return body[i:j + 1], j + 1
            j += 1
        return body[i:j], j
    if ch == "[":
        depth = 0
        j = i
        in_str = None
        while j < len(body):
            c = body[j]
            if in_str:
                if c == "\\":
                    j += 2
                    continue
                if c == in_str:
                    in_str = None
            else:
                if c in ("'", '"', "`"):
                    in_str = c
                elif c == "[":
                    depth += 1
                elif c == "]":
                    depth -= 1
                    if depth == 0:
                        return body[i:j + 1], j + 1
            j += 1
        return body[i:j], j
    if ch in ("'", '"', "`"):
        quote = ch
        j = i + 1
        while j < len(body):
            c = body[j]
            if c == "\\":
                j += 2
                continue
            if c == quote:
                return body[i:j + 1], j + 1
            j += 1
        return body[i:j], j
    # 标识符或其他表达式: 截到 ',' 或 '}' 或 '\n' 顶层
    j = i
    depth_paren = 0
    while j < len(body):
        c = body[j]
        if c == "(":
            depth_paren += 1
        elif c == ")":
            if depth_paren > 0:
                depth_paren -= 1
            else:
                break
        elif c in (",", "}", "\n") and depth_paren == 0:
            break
        j += 1
    return body[i:j].strip(), j


# 模板字符串 ${...}
TEMPLATE_VAR_RE = re.compile(r"\$\{([A-Za-z_$][\w$]*)\}")
# 模板字符串里行内 ${x.y} 取 x
TEMPLATE_DOT_RE = re.compile(r"\$\{([A-Za-z_$][\w$]*)(?:\.[^}]*)?\}")


def parse_template_string(url: str) -> Tuple[List[str], str]:
    """
    解析 url 模板字符串:
      `/api/v1/bank-accounts/${id}/balance`
    返回 (path_vars, normalized_path)
      path_vars: ['id']
      normalized_path: '/api/v1/bank-accounts/{id}/balance'
    """
    if "`" not in url and "${" not in url:
        return [], url
    path_vars = TEMPLATE_VAR_RE.findall(url)
    normalized = TEMPLATE_VAR_RE.sub(lambda m: "{" + m.group(1) + "}", url)
    return path_vars, normalized


def parse_object_literal(value: str) -> Tuple[bool, List[str], List[Tuple[str, str]]]:
    """
    解析对象字面量(简化):
      - params: { pageNum: 1, pageSize: 20, ... }  → static, fields=['pageNum',...]
      - params: { ...params, foo: 'bar' }          → static, fields=['foo',...], 提示 spread
      - params: id                                  → not-static
      - params: { ids }                             → static, fields=['ids'] (ES6 shorthand)
    返回 (is_static, field_names, notes)
    """
    v = value.strip()
    notes: List[Tuple[str, str]] = []
    if not v:
        return True, [], notes
    if not v.startswith("{"):
        return False, [], [("non-object", v)]
    if v == "{}":
        return True, [], notes
    # 截掉外层 {}
    inner = v[1:-1]
    # 拆分顶层 key-value
    fields: List[str] = []
    has_spread = False
    has_computed = False
    i = 0
    n = len(inner)
    while i < n:
        # 跳过空白与逗号
        while i < n and inner[i] in " \t\r\n,":
            i += 1
        if i >= n:
            break
        # spread: ...x
        if inner.startswith("...", i):
            has_spread = True
            i += 3
            # 跳到下一个 , 或 }
            depth = 0
            while i < n:
                c = inner[i]
                if c in ("{", "[", "("):
                    depth += 1
                elif c in ("}", "]", ")"):
                    if depth > 0:
                        depth -= 1
                    else:
                        break
                elif c == "," and depth == 0:
                    break
                i += 1
            continue
        # key
        if inner[i] in ("'", '"', "`"):
            quote = inner[i]
            j = i + 1
            key_chars = []
            while j < n:
                c = inner[j]
                if c == "\\":
                    j += 2
                    continue
                if c == quote:
                    break
                key_chars.append(c)
                j += 1
            i = j + 1
            key = "".join(key_chars)
        else:
            # 标识符 / computed
            j = i
            while j < n and inner[j] not in ("=", ":", ",", "}", "\n"):
                j += 1
            token = inner[i:j].strip()
            if not token:
                break
            if token.startswith("["):
                has_computed = True
                # computed key, 无法静态分析
                i = j
                # 找 colon
                while i < n and inner[i] != ":":
                    i += 1
                i += 1
                # skip value
                depth = 0
                while i < n:
                    c = inner[i]
                    if c in ("{", "[", "("):
                        depth += 1
                    elif c in ("}", "]", ")"):
                        if depth > 0:
                            depth -= 1
                        else:
                            break
                    elif c == "," and depth == 0:
                        break
                    i += 1
                continue
            key = token
            i = j
        # 期望 ':'
        while i < n and inner[i] in " \t\r\n":
            i += 1
        if i >= n or inner[i] != ":":
            break
        i += 1  # skip ':'
        # ES6 shorthand: { ids }   →  token 后无 ':'
        # 已经在前面如果 i==j, 是 shorthand
        # 这里因为 i>j 走过 ':', 视为 key:value
        # 但我们要兼容 { ids, foo:'bar' }: 当 token 是 'ids' 且 i==j, 那 inner[j] 可能是 ',' 或 '}'
        # 我们的外层 if-else 已经把 'ids' 当作 key, 然后 ':' 应该出现; 如果没 ':' 就是 shorthand
        # 已走到 ':' → key:value
        # skip value
        depth = 0
        in_str = None
        while i < n:
            c = inner[i]
            if in_str:
                if c == "\\":
                    i += 2
                    continue
                if c == in_str:
                    in_str = None
            else:
                if c in ("'", '"', "`"):
                    in_str = c
                elif c in ("{", "[", "("):
                    depth += 1
                elif c in (",", "}") and depth == 0:
                    break
                elif c in ("}", "]", ")"):
                    if depth > 0:
                        depth -= 1
                    else:
                        break
            i += 1
        if key and not has_computed:
            fields.append(key)
    if has_spread:
        notes.append(("spread", "params/data 含 ...spread,字段集合为近似估计"))
    if has_computed:
        notes.append(("computed", "params/data 含 computed key,字段集合为近似估计"))
    return True, fields, notes


def parse_param_decl(value: str, source: str) -> ParamDecl:
    """
    把 params: <expr> 解析为 ParamDecl。
    source: 'params' 或 'data'
    """
    is_static, fields, notes = parse_object_literal(value)
    return ParamDecl(
        name="",
        decl_kind="inline-obj" if is_static else "non-literal",
        is_static=is_static,
        inline_fields=fields,
        source=source,
    )


def scan_frontend_apis(api_dir: Path, project_root: Path) -> List[FrontendApiCall]:
    """
    扫描 api_dir 下所有 .js / .ts 文件,提取 export function 中的 request({...})。
    简化: 一个 export function 内只解析第一个 request(...) 调用;
          url/method 必须是字符串字面量,否则标记 SKIP。
    """
    results: List[FrontendApiCall] = []
    for fp in sorted(api_dir.rglob("*")):
        if not fp.is_file():
            continue
        if fp.suffix not in (".js", ".ts"):
            continue
        try:
            text = fp.read_text(encoding="utf-8")
        except UnicodeDecodeError:
            continue
        try:
            rel = str(fp.relative_to(project_root))
        except ValueError:
            rel = str(fp)
        for m in EXPORT_FN_RE.finditer(text):
            name = m.group("name")
            fn_start = m.end()  # 在 '{' 之后
            # 找到这个函数体的范围(配平大括号)
            depth = 1
            j = fn_start
            in_str = None
            while j < len(text) and depth > 0:
                c = text[j]
                if in_str:
                    if c == "\\":
                        j += 2
                        continue
                    if c == in_str:
                        in_str = None
                else:
                    if c in ("'", '"', "`"):
                        in_str = c
                    elif c == "{":
                        depth += 1
                    elif c == "}":
                        depth -= 1
                        if depth == 0:
                            break
                j += 1
            body = text[fn_start:j]
            line = text[:m.start()].count("\n") + 1

            # 找 url
            url_m = URL_RE.search(body)
            method_m = METHOD_RE.search(body)
            if not url_m or not method_m:
                continue
            url = url_m.group("v")
            method = method_m.group("v").lower()

            # params
            params_decl: Optional[ParamDecl] = None
            data_decl: Optional[ParamDecl] = None
            for regex, kind in ((PARAMS_RE, "params"), (DATA_RE, "data")):
                pm = regex.search(body)
                if not pm:
                    continue
                value, _ = slice_field_value(body, pm.end())
                if not value:
                    continue
                decl = parse_param_decl(value, kind)
                if kind == "params":
                    params_decl = decl
                else:
                    data_decl = decl

            # 额外字段(responseType 等)
            extras: List[Tuple[str, str]] = []
            for em in re.finditer(r"^\s*([A-Za-z_][A-Za-z0-9_]*)\s*:\s*['\"`]([^'\"`]+)['\"`]\s*,", body, re.MULTILINE):
                k = em.group(1)
                if k in ("url", "method", "params", "data"):
                    continue
                extras.append((k, em.group(2)))

            results.append(FrontendApiCall(
                file=rel,
                name=name,
                line=line,
                url=url,
                method=method,
                params=params_decl,
                data=data_decl,
                extra_options=extras,
                raw_body=body[:300],
            ))
    return results


# ============================================================================
# 3. OpenAPI 路径匹配
# ============================================================================
def normalize_openapi_path(path: str) -> str:
    """将 OpenAPI path 中的 {paramName} 保留"""
    return path


def build_path_index(openapi: Dict[str, Any]) -> Dict[str, List[Tuple[str, Dict[str, Any]]]]:
    """
    索引: normalized path → [(method, operation)]
    """
    idx: Dict[str, List[Tuple[str, Dict[str, Any]]]] = defaultdict(list)
    for path, ops in (openapi.get("paths") or {}).items():
        for m, op in ops.items():
            if m in ("get", "post", "put", "delete", "patch", "head", "options"):
                idx[path].append((m, op))
    return idx


def find_path_match(
    normalized_url: str,
    method: str,
    path_index: Dict[str, List[Tuple[str, Dict[str, Any]]]],
) -> Tuple[Optional[str], Optional[Dict[str, Any]], Optional[str]]:
    """
    返回 (matched_path, operation, reason_if_none)
    1) 先尝试精确
    2) 再尝试只比较大括号位置是否一致
    """
    # 1. 精确
    ops = path_index.get(normalized_url) or []
    for m, op in ops:
        if m == method:
            return normalized_url, op, None
    # 2. 形如 /a/b/{x}/c 与 /a/b/{y}/c 兼容
    url_parts = normalized_url.split("/")
    url_var_pos = [i for i, p in enumerate(url_parts) if p.startswith("{") and p.endswith("}")]
    for path, ops2 in path_index.items():
        if method not in (m for m, _ in ops2):
            continue
        parts = path.split("/")
        if len(parts) != len(url_parts):
            continue
        var_pos = [i for i, p in enumerate(parts) if p.startswith("{") and p.endswith("}")]
        if var_pos != url_var_pos:
            continue
        # 字面段必须完全相同
        ok = True
        for i, (a, b) in enumerate(zip(parts, url_parts)):
            if i in var_pos:
                continue
            if a != b:
                ok = False
                break
        if ok:
            for m, op in ops2:
                if m == method:
                    return path, op, None
    return None, None, f"未找到 path 匹配: {normalized_url} ({method})"


# ============================================================================
# 4. 参数对比
# ============================================================================
def path_vars_of(openapi_path: str) -> List[str]:
    return re.findall(r"\{([^}]+)\}", openapi_path)


def op_query_params(op: Dict[str, Any]) -> List[Dict[str, Any]]:
    return [p for p in op.get("parameters") or [] if p.get("in") == "query"]


def op_path_params(op: Dict[str, Any]) -> List[Dict[str, Any]]:
    return [p for p in op.get("parameters") or [] if p.get("in") == "path"]


def op_request_body_schema(op: Dict[str, Any], openapi: Dict[str, Any]) -> Optional[Dict[str, Any]]:
    rb = op.get("requestBody") or {}
    content = rb.get("content") or {}
    json_content = content.get("application/json") or {}
    schema = json_content.get("schema")
    if not schema:
        return None
    return resolve_ref(schema, openapi)


def resolve_ref(node: Any, openapi: Dict[str, Any]) -> Any:
    """极简 $ref 解析(一层,够用)"""
    if isinstance(node, dict):
        if "$ref" in node:
            ref = node["$ref"]
            # 形如 "#/components/schemas/Name"
            parts = ref.lstrip("#/").split("/")
            cur: Any = openapi
            for p in parts:
                if isinstance(cur, dict) and p in cur:
                    cur = cur[p]
                else:
                    return node
            return resolve_ref(cur, openapi)
        out = {}
        for k, v in node.items():
            out[k] = resolve_ref(v, openapi)
        return out
    if isinstance(node, list):
        return [resolve_ref(x, openapi) for x in node]
    return node


# 类型归一化
TYPE_MAP = {
    "string": "string",
    "integer": "number",
    "number": "number",
    "boolean": "boolean",
    "array": "array",
    "object": "object",
}

# 简单反推前端字面量类型
def infer_js_type(value: str) -> str:
    v = value.strip()
    if v.startswith("'") or v.startswith('"') or v.startswith("`"):
        return "string"
    if v in ("true", "false"):
        return "boolean"
    if v in ("null", "undefined"):
        return "null"
    if re.fullmatch(r"-?\d+", v):
        return "number"
    if re.fullmatch(r"-?\d+\.\d+", v):
        return "number"
    if v.startswith("[") and v.endswith("]"):
        return "array"
    if v.startswith("{") and v.endswith("}"):
        return "object"
    return "unknown"


# ============================================================================
# 5. 对比与生成 Issue
# ============================================================================
def compare_call(
    call: FrontendApiCall,
    openapi: Dict[str, Any],
    path_index: Dict[str, List[Tuple[str, Dict[str, Any]]]],
) -> List[Issue]:
    issues: List[Issue] = []
    path_vars, normalized = parse_template_string(call.url)
    if "`" in call.url and "${" not in call.url:
        # 模板字符串但无变量 — 仍当作普通字符串处理
        normalized = call.url
        path_vars = []

    # 1) 路径匹配
    matched_path, op, reason = find_path_match(normalized, call.method, path_index)
    if not matched_path:
        issues.append(Issue(
            severity="P0",
            category="path",
            file=call.file, api=call.name, line=call.line,
            title="路径未找到匹配",
            detail=f"前端 url `{call.url}` (method={call.method}) 在 OpenAPI 中找不到对应 path",
            standard=f"OpenAPI 应存在 `{normalized}` 的 {call.method.upper()} 端点",
            current=f"`{call.url}` ({call.method})",
            fix=f"检查 url 拼写;若为新接口,请补后端 Controller + 重跑 `bash scripts/gen-openapi.sh`",
        ))
        return issues

    # 2) path 参数名对比
    op_pvars = [p["name"] for p in op_path_params(op)]
    decl_pvars = path_vars
    # 模板里没用到变量 — 但 OpenAPI 需要
    if not decl_pvars and op_pvars:
        # 仅当 url 里有 {var} 却被替换成字面量时才算;这里前端硬编码属于另一种 bug, 给出 P1
        issues.append(Issue(
            severity="P1",
            category="path-param",
            file=call.file, api=call.name, line=call.line,
            title="path 参数缺失",
            detail=f"OpenAPI 期望 path 参数 {op_pvars},但前端 url `{call.url}` 没有模板变量",
            standard=f"路径变量: {op_pvars}",
            current=f"`{call.url}`",
            fix=f"改为模板字符串 `` `/api/.../.../${{var}}` `` 并传入 var",
        ))
    elif decl_pvars:
        for v in decl_pvars:
            if v not in op_pvars:
                issues.append(Issue(
                    severity="P1",
                    category="path-param",
                    file=call.file, api=call.name, line=call.line,
                    title="path 参数名不一致",
                    detail=f"前端模板变量 `${{{v}}}` 在 OpenAPI 中未找到;OpenAPI 期望 {op_pvars}",
                    standard=f"OpenAPI 变量: {op_pvars}",
                    current=f"前端: {decl_pvars}",
                    fix=f"将模板里 `${{{v}}}` 改为 OpenAPI 中的变量名,例如 `${{{op_pvars[0] if op_pvars else 'id'}}}`",
                ))

    # 3) query 参数对比
    op_q = op_query_params(op)
    op_q_required = {p["name"] for p in op_q if p.get("required")}
    op_q_all = {p["name"] for p in op_q}

    if call.params and call.params.is_static:
        decl_q = set(call.params.inline_fields)
        extra = decl_q - op_q_all
        missing_required = (op_q_required - decl_q) if op_q_required else set()
        missing_optional = (op_q_all - op_q_required) - decl_q
        if extra:
            issues.append(Issue(
                severity="P1",
                category="query-extra",
                file=call.file, api=call.name, line=call.line,
                title="多余 query 参数",
                detail=f"前端 params 多于 OpenAPI 定义: {sorted(extra)};后端可能忽略",
                standard=f"OpenAPI 允许: {sorted(op_q_all) or '—'}",
                current=f"前端传: {sorted(decl_q)}",
                fix=f"删除多余字段,或补后端 query 接收",
            ))
        if missing_required:
            issues.append(Issue(
                severity="P0",
                category="query-missing",
                file=call.file, api=call.name, line=call.line,
                title="缺失必传 query 参数",
                detail=f"OpenAPI 标记必传 {sorted(missing_required)},但前端 params 未传",
                standard=f"必传: {sorted(op_q_required)}",
                current=f"前端传: {sorted(decl_q)}",
                fix=f"补齐必传参数或在 OpenAPI 把 required=false",
            ))
        if missing_optional:
            issues.append(Issue(
                severity="P2",
                category="query-missing",
                file=call.file, api=call.name, line=call.line,
                title="可选 query 未传",
                detail=f"OpenAPI 定义了可选参数 {sorted(missing_optional)},前端未传 (无影响,仅提示)",
                standard=f"可选: {sorted(missing_optional)}",
                current=f"前端传: {sorted(decl_q)}",
                fix=f"按需补全,或忽略 (后端默认值)",
            ))
    elif call.params and not call.params.is_static:
        issues.append(Issue(
            severity="P2",
            category="static-analyze",
            file=call.file, api=call.name, line=call.line,
            title="params 非字面量,无法静态分析",
            detail=f"`params: {call.params.decl_kind}`,跳过字段对比",
            standard="—",
            current=f"params: {call.params.decl_kind}",
            fix="尽量改为对象字面量,或写 JSDoc 标注字段名",
        ))

    # 4) body 参数对比
    if call.data and call.data.is_static:
        schema = op_request_body_schema(op, openapi)
        if schema is None:
            # 端点没有 requestBody
            if call.data.inline_fields:
                issues.append(Issue(
                    severity="P0",
                    category="body-extra",
                    file=call.file, api=call.name, line=call.line,
                    title="多余 body 参数",
                    detail=f"端点 {matched_path} 不需要 requestBody,但前端传了 data: {call.data.inline_fields}",
                    standard="该方法无 requestBody",
                    current=f"前端传 data: {call.data.inline_fields}",
                    fix="删除 data 字段或拼错 method(GET 不能带 body)",
                ))
        else:
            op_props = (schema.get("properties") or {}) if isinstance(schema, dict) else {}
            op_required = set(schema.get("required") or [])
            op_keys = set(op_props.keys())
            decl = set(call.data.inline_fields)
            extra = decl - op_keys
            missing_required = (op_required - decl) if op_required else set()
            if extra:
                issues.append(Issue(
                    severity="P0",
                    category="body-extra",
                    file=call.file, api=call.name, line=call.line,
                    title="body 字段不在 schema 中",
                    detail=f"前端 data 含 schema 未声明字段: {sorted(extra)};后端可能忽略或 400",
                    standard=f"OpenAPI 允许: {sorted(op_keys) or '—'}",
                    current=f"前端传: {sorted(decl)}",
                    fix=f"删除多余字段,或补 DTO 与 OpenAPI 字段",
                ))
            if missing_required:
                issues.append(Issue(
                    severity="P0",
                    category="body-missing",
                    file=call.file, api=call.name, line=call.line,
                    title="缺失必传 body 字段",
                    detail=f"OpenAPI 标记必传 {sorted(missing_required)},但前端 data 未传",
                    standard=f"必传: {sorted(op_required)}",
                    current=f"前端传: {sorted(decl)}",
                    fix=f"补齐字段或在 DTO 把字段标记非必填",
                ))
    elif call.data and not call.data.is_static:
        issues.append(Issue(
            severity="P2",
            category="static-analyze",
            file=call.file, api=call.name, line=call.line,
            title="data 非字面量,无法静态分析",
            detail=f"`data: {call.data.decl_kind}`,跳过字段对比",
            standard="—",
            current=f"data: {call.data.decl_kind}",
            fix="尽量改为对象字面量,或写 JSDoc 标注字段名",
        ))

    return issues


# ============================================================================
# 6. 报告 HTML
# ============================================================================
def render_report_html(
    project_root: Path,
    openapi_source: str,
    calls: List[FrontendApiCall],
    issues: List[Issue],
) -> Tuple[str, str]:
    """
    返回 (html_text, grade) — grade ∈ {A,B,C,D}
    """
    p0 = [i for i in issues if i.severity == "P0"]
    p1 = [i for i in issues if i.severity == "P1"]
    p2 = [i for i in issues if i.severity == "P2"]
    if p0:
        grade = "D"
    elif p1:
        grade = "C"
    elif p2:
        grade = "B"
    else:
        grade = "A"
    matched = sum(1 for c in calls if not any(i.category == "path" and i.api == c.name for i in issues))
    path_mismatch = sum(1 for i in issues if i.category == "path")
    param_mismatch = sum(1 for i in issues if i.category.startswith(("query", "body", "path-param")))

    files_scanned = sorted({c.file for c in calls})
    today = datetime.date.today().isoformat()

    def escape(s: str) -> str:
        return html.escape(s, quote=True)

    rows = []
    for idx, grp in enumerate([p0, p1, p2], start=1):
        for it in grp:
            rows.append(
                f"<tr><td>{idx}</td><td><span class='op-sev op-sev-{it.severity}'>{it.severity}</span></td>"
                f"<td>{escape(it.api)} <small style='color:#909399'>@ {escape(it.file)}:{it.line}</small></td>"
                f"<td>{escape(it.title)}<br><small>{escape(it.category)}</small></td>"
                f"<td>{escape(it.standard)}</td>"
                f"<td>{escape(it.current)}</td>"
                f"<td>{escape(it.detail)}<br><b>建议:</b>{escape(it.fix)}</td></tr>"
            )
            idx += 1
    rows_html = "\n".join(rows) if rows else "<tr><td colspan='7' style='text-align:center;color:#67c23a'>✅ 未发现问题</td></tr>"

    file_list = "\n".join(escape(f) for f in files_scanned)

    recommendations = []
    if p0:
        recommendations.append("修复 P0 路径错 / 字段缺失 — 阻塞后续联调")
    if p1:
        recommendations.append("修复 P1 命名/参数差异 — 防止运行期 400/逻辑错误")
    if p2:
        recommendations.append("P2 优化项可在 PR 顺手做,或单独开 ticket")
    if not issues:
        recommendations.append("前端 API 与 OpenAPI 完全一致 — 评级 A,可直接进入集成阶段")

    html_text = f"""<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>{escape(TEMPLATE_TITLE)}</title>
  <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/element-plus@2.14.3/dist/index.css">
  <style>
    * {{ box-sizing: border-box; }}
    body {{ margin: 0; font-family: -apple-system, "PingFang SC", "Microsoft YaHei", sans-serif; background: #f5f7fa; color: #303133; }}
    .op-header {{ background: #1f2d3d; color: #fff; padding: 14px 24px; display: flex; align-items: center; justify-content: space-between; box-shadow: 0 2px 4px rgba(0,0,0,.1); }}
    .op-header .brand {{ font-size: 18px; font-weight: 600; }}
    .op-header .meta {{ font-size: 12px; opacity: .75; }}
    .op-main {{ max-width: 1280px; margin: 0 auto; padding: 24px; }}
    .op-card {{ background: #fff; border-radius: 12px; box-shadow: 0 2px 12px rgba(0,0,0,.04); padding: 20px; margin-bottom: 16px; }}
    .op-card h2 {{ margin: 0 0 12px; font-size: 18px; font-weight: 600; color: #1f2d3d; }}
    .op-card h3 {{ margin: 16px 0 8px; font-size: 15px; font-weight: 600; color: #303133; }}
    .op-grade-badge {{ display: inline-block; padding: 6px 14px; border-radius: 6px; font-size: 18px; font-weight: 700; color: #fff; }}
    .op-grade-A {{ background: #67c23a; }}
    .op-grade-B {{ background: #409eff; }}
    .op-grade-C {{ background: #e6a23c; }}
    .op-grade-D {{ background: #f56c6c; }}
    .op-meta-row {{ display: grid; grid-template-columns: repeat(4, 1fr); gap: 12px; margin-bottom: 16px; }}
    .op-meta-cell {{ background: #fafbfc; border-radius: 8px; padding: 10px 12px; }}
    .op-meta-cell .label {{ font-size: 12px; color: #909399; }}
    .op-meta-cell .value {{ font-size: 14px; color: #303133; font-weight: 600; margin-top: 2px; }}
    table.op-table {{ width: 100%; border-collapse: collapse; font-size: 13px; }}
    table.op-table th, table.op-table td {{ border: 1px solid #ebeef5; padding: 8px 10px; text-align: left; vertical-align: top; }}
    table.op-table th {{ background: #fafbfc; color: #606266; font-weight: 600; }}
    table.op-table tr:nth-child(even) td {{ background: #fafbfc; }}
    .op-sev {{ display: inline-block; padding: 2px 8px; border-radius: 4px; font-size: 12px; font-weight: 600; }}
    .op-sev-P0 {{ background: #fef0f0; color: #f56c6c; }}
    .op-sev-P1 {{ background: #fdf6ec; color: #e6a23c; }}
    .op-sev-P2 {{ background: #f4f4f5; color: #909399; }}
    .op-counter {{ display: flex; gap: 12px; }}
    .op-counter-item {{ flex: 1; padding: 16px; border-radius: 8px; text-align: center; }}
    .op-counter-item .num {{ font-size: 28px; font-weight: 700; }}
    .op-counter-item .lbl {{ font-size: 12px; color: #606266; margin-top: 4px; }}
    .op-counter-P0 {{ background: #fef0f0; color: #f56c6c; }}
    .op-counter-P1 {{ background: #fdf6ec; color: #e6a23c; }}
    .op-counter-P2 {{ background: #f4f4f5; color: #909399; }}
    .op-counter-pass {{ background: #f0f9eb; color: #67c23a; }}
    .op-file-list {{ background: #fafbfc; border-radius: 8px; padding: 12px 16px; font-family: 'JetBrains Mono', monospace; font-size: 13px; white-space: pre-wrap; }}
    .op-footer {{ text-align: center; color: #909399; font-size: 12px; padding: 16px; }}
    .op-kpi-row {{ display: grid; grid-template-columns: repeat(4, 1fr); gap: 12px; margin-top: 12px; }}
    .op-kpi {{ background: #fafbfc; border-radius: 8px; padding: 14px; text-align: center; }}
    .op-kpi .num {{ font-size: 26px; font-weight: 700; color: #1f2d3d; }}
    .op-kpi .lbl {{ font-size: 12px; color: #606266; margin-top: 4px; }}
  </style>
</head>
<body>
  <header class="op-header">
    <div class="brand">Open-TMS · {escape(REPORT_SOURCE)} 报告</div>
    <div class="meta">来源: {escape(REPORT_SOURCE)} v{REPORT_VERSION} · {today} · frontend-api-scanner</div>
  </header>

  <main class="op-main">
    <section class="op-card">
      <h2>📊 总评级</h2>
      <span class="op-grade-badge op-grade-{grade}">{grade}</span>
      <p style="color:#606266;margin-top:8px">
        {'无问题' if grade=='A' else '仅 P2 优化' if grade=='B' else '有 P1,需修复' if grade=='C' else '有 P0 阻塞,必须修复'}。
        OpenAPI 来源: <code>{escape(openapi_source)}</code>
      </p>
    </section>

    <section class="op-card">
      <h2>📈 扫描概览</h2>
      <div class="op-meta-row">
        <div class="op-meta-cell"><div class="label">扫描目录</div><div class="value">web/src/api (递归)</div></div>
        <div class="op-meta-cell"><div class="label">扫描文件数</div><div class="value">{len(files_scanned)}</div></div>
        <div class="op-meta-cell"><div class="label">项目根</div><div class="value">{escape(str(project_root))}</div></div>
        <div class="op-meta-cell"><div class="label">OpenAPI 来源</div><div class="value">{escape(openapi_source)}</div></div>
      </div>
      <div class="op-kpi-row">
        <div class="op-kpi"><div class="num">{len(calls)}</div><div class="lbl">扫描 API 数</div></div>
        <div class="op-kpi"><div class="num" style="color:#67c23a">{matched}</div><div class="lbl">已匹配</div></div>
        <div class="op-kpi"><div class="num" style="color:#f56c6c">{path_mismatch}</div><div class="lbl">路径错</div></div>
        <div class="op-kpi"><div class="num" style="color:#e6a23c">{param_mismatch}</div><div class="lbl">参数错</div></div>
      </div>
      <h3>📁 扫描文件</h3>
      <pre class="op-file-list">{file_list}</pre>
      <div class="op-counter" style="margin-top: 12px">
        <div class="op-counter-item op-counter-pass"><div class="num">{len(calls) - len(p0) - len(p1) - len(p2)}</div><div class="lbl">通过项</div></div>
        <div class="op-counter-item op-counter-P0"><div class="num">{len(p0)}</div><div class="lbl">P0</div></div>
        <div class="op-counter-item op-counter-P1"><div class="num">{len(p1)}</div><div class="lbl">P1</div></div>
        <div class="op-counter-item op-counter-P2"><div class="num">{len(p2)}</div><div class="lbl">P2</div></div>
      </div>
    </section>

    <section class="op-card">
      <h2>🚨 P0 问题(必须修复 · 阻塞)</h2>
      <table class="op-table">
        <thead><tr><th>#</th><th>严重度</th><th>位置</th><th>问题</th><th>标准</th><th>现状</th><th>建议</th></tr></thead>
        <tbody>
          {''.join(r for r in rows if 'op-sev-P0' in r) or '<tr><td colspan="7" style="text-align:center;color:#67c23a">✅ 无 P0</td></tr>'}
        </tbody>
      </table>
    </section>

    <section class="op-card">
      <h2>⚠️ P1 问题(重要)</h2>
      <table class="op-table">
        <thead><tr><th>#</th><th>严重度</th><th>位置</th><th>问题</th><th>标准</th><th>现状</th><th>建议</th></tr></thead>
        <tbody>
          {''.join(r for r in rows if 'op-sev-P1' in r) or '<tr><td colspan="7" style="text-align:center;color:#67c23a">✅ 无 P1</td></tr>'}
        </tbody>
      </table>
    </section>

    <section class="op-card">
      <h2>💡 P2 问题(优化)</h2>
      <table class="op-table">
        <thead><tr><th>#</th><th>严重度</th><th>位置</th><th>问题</th><th>标准</th><th>现状</th><th>建议</th></tr></thead>
        <tbody>
          {''.join(r for r in rows if 'op-sev-P2' in r) or '<tr><td colspan="7" style="text-align:center;color:#67c23a">✅ 无 P2</td></tr>'}
        </tbody>
      </table>
    </section>

    <section class="op-card">
      <h2>🛠️ 整改建议总览</h2>
      <ol>
        {''.join(f'<li>{escape(r)}</li>' for r in recommendations)}
      </ol>
    </section>
  </main>

  <footer class="op-footer">Open-TMS 前端 API 一致性扫描报告 · 由 {escape(REPORT_SOURCE)} v{REPORT_VERSION} 自动生成</footer>
</body>
</html>
"""
    return html_text, grade


# ============================================================================
# 7. CLI 主流程
# ============================================================================
def run_gen_openapi(project_root: Path) -> None:
    """自动调用 scripts/gen-openapi.sh"""
    script = project_root / "scripts" / "gen-openapi.sh"
    if not script.exists():
        print(f"[warn] {script} 不存在,跳过")
        return
    print(f"[info] 自动执行 {script} …")
    try:
        subprocess.run(["bash", str(script)], check=True, cwd=str(project_root))
    except subprocess.CalledProcessError as e:
        print(f"[warn] gen-openapi.sh 失败: {e}")


def main() -> int:
    parser = argparse.ArgumentParser(
        description="Open-TMS 前端 API 一致性扫描工具",
        formatter_class=argparse.RawDescriptionHelpFormatter,
    )
    parser.add_argument("--api-dir", default="web/src/api", help="前端 API 目录(相对项目根)")
    parser.add_argument("--openapi", default="", help="OpenAPI JSON 路径(默认 docs/api/openapi.json)")
    parser.add_argument("--report-html", default="docs/api/frontend-api-consistency.html",
                        help="HTML 报告输出路径(相对项目根)")
    parser.add_argument("--json", default="", help="可选 JSON 报告输出路径")
    parser.add_argument("--gen-openapi", action="store_true", help="先自动 bash scripts/gen-openapi.sh")
    parser.add_argument("--ci", action="store_true", help="CI 模式: 有 P0 时 exit 1")
    parser.add_argument("--quiet", action="store_true", help="仅打印摘要")
    args = parser.parse_args()

    project_root = Path(__file__).resolve().parent.parent
    print(f"[info] 项目根: {project_root}")

    if args.gen_openapi:
        run_gen_openapi(project_root)

    openapi, openapi_source = load_openapi(args, project_root)
    path_index = build_path_index(openapi)
    print(f"[info] OpenAPI paths: {len(path_index)}")

    api_dir = (project_root / args.api_dir).resolve() if not Path(args.api_dir).is_absolute() else Path(args.api_dir)
    if not api_dir.exists():
        sys.exit(f"[ERROR] API 目录不存在: {api_dir}")
    calls = scan_frontend_apis(api_dir, project_root)
    print(f"[info] 扫描到 API 调用: {len(calls)}")

    all_issues: List[Issue] = []
    for call in calls:
        all_issues.extend(compare_call(call, openapi, path_index))

    p0 = sum(1 for i in all_issues if i.severity == "P0")
    p1 = sum(1 for i in all_issues if i.severity == "P1")
    p2 = sum(1 for i in all_issues if i.severity == "P2")

    html_text, grade = render_report_html(project_root, openapi_source, calls, all_issues)

    report_path = Path(args.report_html)
    if not report_path.is_absolute():
        report_path = project_root / report_path
    report_path.parent.mkdir(parents=True, exist_ok=True)
    report_path.write_text(html_text, encoding="utf-8")
    print(f"[info] 报告: {report_path}  size={report_path.stat().st_size/1024:.1f}KB")

    if args.json:
        json_path = Path(args.json)
        if not json_path.is_absolute():
            json_path = project_root / json_path
        json_path.parent.mkdir(parents=True, exist_ok=True)
        json_data = {
            "report_source": REPORT_SOURCE,
            "version": REPORT_VERSION,
            "generated_at": datetime.datetime.utcnow().isoformat() + "Z",
            "project_root": str(project_root),
            "openapi_source": openapi_source,
            "grade": grade,
            "summary": {
                "scanned": len(calls),
                "p0": p0, "p1": p1, "p2": p2,
            },
            "issues": [dataclasses.asdict(i) for i in all_issues],
        }
        json_path.write_text(json.dumps(json_data, ensure_ascii=False, indent=2), encoding="utf-8")
        print(f"[info] JSON 报告: {json_path}")

    print()
    print(f"=== {REPORT_SOURCE} v{REPORT_VERSION} 摘要 ===")
    print(f"  扫描 API 数 : {len(calls)}")
    print(f"  P0: {p0}    P1: {p1}    P2: {p2}")
    print(f"  评级: {grade}  ({'通过' if grade in ('A','B') else '不通过'})")
    print(f"  报告: {report_path}")

    if args.ci and p0 > 0:
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main())

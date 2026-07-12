#!/usr/bin/env python3
"""
Open-TMS business_unit 残留扫描工具
====================================
背景: 历史 commit 23-rename-business-unit.sql + 23b-rename-business-unit-cleanup.sql
       已将 DB 中 business_unit 改名为 management_entity。
       本工具扫描系统中是否仍有残留(代码 / 文档 / DB)。

扫描类别:
  A. Java 代码  (basedata/dealing/common 模块)
  B. 前端代码  (web/src)
  C. 文档       (docs/** + 根目录 *.md)
  D. 数据库     (information_schema / pg_indexes)

用法:
  python scripts/scan_business_unit.py                       # 全量扫描 + 报告
  python scripts/scan_business_unit.py --json out.json       # 同时输出 JSON
  python scripts/scan_business_unit.py --only java           # 仅 java
  python scripts/scan_business_unit.py --only frontend       # 仅 frontend
  python scripts/scan_business_unit.py --only docs           # 仅 docs
  python scripts/scan_business_unit.py --only db             # 仅 db
  python scripts/scan_business_unit.py --root .              # 指定项目根
"""
import re
import sys
import json
import argparse
import datetime
from pathlib import Path
from collections import defaultdict

# 预置扫描模式(覆盖 4 种命名风格)
PATTERNS = {
    "snake_case": {
        "regex": r"business_unit",
        "severity_default": "P0",
        "desc": "DB snake_case 命名残留(列名/表名/索引名)",
    },
    "camelCase": {
        "regex": r"businessUnit",
        "severity_default": "P1",
        "desc": "Java/JS camelCase 命名残留(字段/方法/变量)",
    },
    "UPPER_CASE": {
        "regex": r"BUSINESS_UNIT",
        "severity_default": "P0",
        "desc": "UPPER_CASE 常量 / 注解残留",
    },
    "PascalCase": {
        "regex": r"BusinessUnit",
        "severity_default": "P1",
        "desc": "Java 类名 / 方法名残留",
    },
}

# 严重度升级规则:
#   - 注释中的残留 → 降为 P2
#   - 文档中的残留 → 降为 P2
#   - 含 `RENAME`、`rename`、`历史`、`history` 关键字 → 降为 P2
#   - DB DDL 脚本 (db/schema/) 中以历史 DDL 形式存在 → P2
#   - 其他情况使用默认
COMMENT_HINT = re.compile(r"^\s*(//|#|\*|/\*)")
HISTORY_HINT = re.compile(r"\b(改名|rename|RENAME|历史|history|deprecated)\b", re.IGNORECASE)
# False positive: "BUSINESS_UNIT" 作为实体类型枚举值(集团/公司/事业部)
# 当上下文出现 entityType / 事业部 / el-option / el-tag 时,实际为合法分类
ENTITY_TYPE_FALSE_POSITIVE = re.compile(
    r"(entityType|事业部|el-option|el-tag|value=\"|label=\")",
    re.IGNORECASE,
)


# ---------------------------------------------------------------------------
# 文件扫描
# ---------------------------------------------------------------------------
def _is_excluded(path: Path) -> bool:
    """排除 target 目录 / class 文件 / 工具自身的报告输出"""
    parts = path.parts
    if "target" in parts or "node_modules" in parts or ".git" in parts:
        return True
    if path.suffix in (".class", ".jar", ".war"):
        return True
    # 跳过工具自己的报告,避免自引用
    rel = str(path).replace("\\", "/")
    if rel.endswith("/docs/reviews/business-unit-audit.md"):
        return True
    return False


def scan_files(file_globs, project_root: Path, category_label: str):
    """对一组 glob 扫描 4 种模式,返回命中清单

    Returns: list of dicts {category, file, line, pattern, text, severity}
    """
    hits = []
    seen_files = set()
    for glob in file_globs:
        for path in project_root.glob(glob):
            if not path.is_file() or _is_excluded(path) or path in seen_files:
                continue
            seen_files.add(path)
            try:
                text = path.read_text(encoding="utf-8", errors="ignore")
            except Exception:
                continue
            for lineno, line in enumerate(text.splitlines(), 1):
                for pname, pinfo in PATTERNS.items():
                    if re.search(pinfo["regex"], line):
                        # False positive 过滤: 实体类型枚举值(事业部/集团/公司)
                        if pname == "UPPER_CASE" and ENTITY_TYPE_FALSE_POSITIVE.search(line):
                            continue
                        # 严重度推断
                        sev = pinfo["severity_default"]
                        if COMMENT_HINT.match(line):
                            sev = "P2"
                        elif HISTORY_HINT.search(line):
                            sev = "P2"
                        # docs/ 目录或 .md 文件 → 默认 P2(除非代码块外)
                        if category_label == "docs" and sev == "P0":
                            sev = "P2"
                        hits.append({
                            "category": category_label,
                            "file": str(path.relative_to(project_root)).replace("\\", "/"),
                            "line": lineno,
                            "pattern": pname,
                            "regex": pinfo["regex"],
                            "text": line.strip()[:200],
                            "severity": sev,
                        })
    return hits


# ---------------------------------------------------------------------------
# Java / 前端 / 文档 glob 集合
# ---------------------------------------------------------------------------
JAVA_GLOBS = [
    "basedata/src/main/java/**/*.java",
    "dealing/src/main/java/**/*.java",
    "common/src/main/java/**/*.java",
]

FRONTEND_GLOBS = [
    "web/src/**/*.vue",
    "web/src/**/*.js",
    "web/src/**/*.ts",
]

DOCS_GLOBS = [
    "docs/**/*.md",
    "*.md",  # 根目录 CLAUDE.md / README.md
]


# ---------------------------------------------------------------------------
# 数据库扫描
# ---------------------------------------------------------------------------
def scan_db():
    """通过 pg8000 查 information_schema + pg_indexes

    Returns: list of dicts {category:'db', object_type, name, detail, severity}
    """
    import pg8000  # 仅在调用时导入,允许离线运行

    DB_CONFIG = {
        "host": "localhost",
        "port": 5432,
        "database": "opentms",
        "user": "opentms",
        "password": "opentms123",
    }

    hits = []
    try:
        conn = pg8000.connect(**DB_CONFIG)
    except Exception as e:
        return [{
            "category": "db",
            "object_type": "CONNECTION",
            "name": "pg8000.connect",
            "detail": f"无法连接 PostgreSQL: {e}",
            "severity": "P1",
        }]

    try:
        cur = conn.cursor()

        # 1) 列名含 business_unit
        cur.execute("""
            SELECT table_schema, table_name, column_name, data_type
            FROM information_schema.columns
            WHERE column_name ILIKE '%business_unit%'
              AND table_schema NOT IN ('pg_catalog', 'information_schema')
            ORDER BY table_schema, table_name
        """)
        for row in cur.fetchall():
            hits.append({
                "category": "db",
                "object_type": "COLUMN",
                "name": f"{row[0]}.{row[1]}.{row[2]}",
                "detail": f"{row[2]} {row[3]} (table={row[1]})",
                "severity": "P0",
            })

        # 2) 表名含 business_unit
        cur.execute("""
            SELECT table_schema, table_name, table_type
            FROM information_schema.tables
            WHERE table_name ILIKE '%business_unit%'
              AND table_schema NOT IN ('pg_catalog', 'information_schema')
            ORDER BY table_schema, table_name
        """)
        for row in cur.fetchall():
            hits.append({
                "category": "db",
                "object_type": "TABLE",
                "name": f"{row[0]}.{row[1]}",
                "detail": f"{row[2]} (table_name={row[1]})",
                "severity": "P0",
            })

        # 3) 视图名含 business_unit
        cur.execute("""
            SELECT table_schema, table_name
            FROM information_schema.views
            WHERE table_name ILIKE '%business_unit%'
              AND table_schema NOT IN ('pg_catalog', 'information_schema')
            ORDER BY table_schema, table_name
        """)
        for row in cur.fetchall():
            hits.append({
                "category": "db",
                "object_type": "VIEW",
                "name": f"{row[0]}.{row[1]}",
                "detail": f"VIEW {row[1]}",
                "severity": "P1",
            })

        # 4) 索引名含 business_unit
        cur.execute("""
            SELECT schemaname, tablename, indexname
            FROM pg_indexes
            WHERE indexname ILIKE '%business_unit%'
              AND schemaname NOT IN ('pg_catalog', 'information_schema')
            ORDER BY schemaname, tablename
        """)
        for row in cur.fetchall():
            hits.append({
                "category": "db",
                "object_type": "INDEX",
                "name": f"{row[0]}.{row[1]}.{row[2]}",
                "detail": f"INDEX ON {row[1]} ({row[2]})",
                "severity": "P0",
            })

        # 5) 约束名含 business_unit
        cur.execute("""
            SELECT n.nspname, c.conrelid::regclass::text, c.conname, c.contype
            FROM pg_constraint c
            JOIN pg_namespace n ON n.oid = c.connamespace
            WHERE c.conname ILIKE '%business_unit%'
              AND n.nspname NOT IN ('pg_catalog', 'information_schema')
            ORDER BY n.nspname, c.conrelid::regclass::text
        """)
        for row in cur.fetchall():
            hits.append({
                "category": "db",
                "object_type": "CONSTRAINT",
                "name": f"{row[0]}.{row[1]}.{row[2]}",
                "detail": f"CONSTRAINT {row[3]} ON {row[1]} ({row[2]})",
                "severity": "P0",
            })

        # 6) 序列名含 business_unit
        cur.execute("""
            SELECT sequence_schema, sequence_name
            FROM information_schema.sequences
            WHERE sequence_name ILIKE '%business_unit%'
              AND sequence_schema NOT IN ('pg_catalog', 'information_schema')
            ORDER BY sequence_schema, sequence_name
        """)
        for row in cur.fetchall():
            hits.append({
                "category": "db",
                "object_type": "SEQUENCE",
                "name": f"{row[0]}.{row[1]}",
                "detail": f"SEQUENCE {row[1]}",
                "severity": "P0",
            })

        cur.close()
    except Exception as e:
        hits.append({
            "category": "db",
            "object_type": "QUERY",
            "name": "scan_db",
            "detail": f"查询失败: {e}",
            "severity": "P1",
        })
    finally:
        conn.close()

    return hits


# ---------------------------------------------------------------------------
# 评级
# ---------------------------------------------------------------------------
def grade(total: int) -> str:
    """legacy: 按总数评级(保留兼容)"""
    if total == 0:
        return "A"
    if total <= 2:
        return "B"
    if total <= 10:
        return "C"
    return "D"


def grade_by_p0(p0: int) -> str:
    """按 P0 数评级(只看运行时影响,文档/历史不算)"""
    if p0 == 0:
        return "A"
    if p0 <= 2:
        return "B"
    if p0 <= 10:
        return "C"
    return "D"


def severity_count(hits):
    out = {"P0": 0, "P1": 0, "P2": 0}
    for h in hits:
        s = h.get("severity", "P2")
        out[s] = out.get(s, 0) + 1
    return out


# ---------------------------------------------------------------------------
# 报告生成
# ---------------------------------------------------------------------------
REPORT_TEMPLATE = """# business_unit 残留审计报告

> 日期: {date}
> 工具: `scripts/scan_business_unit.py`
> 评级: **{grade}** ({grade_desc})
> 总残留: **{total}** 处 (P0={p0} / P1={p1} / P2={p2})

> **背景**: 历史 commit `23-rename-business-unit.sql` + `23b-rename-business-unit-cleanup.sql`
> 已将 DB 中 `business_unit` 重命名为 `management_entity`。本报告扫描系统是否仍有残留。

## 1. 总览

| 类别 | 命中数 | P0 | P1 | P2 |
|------|--------|----|----|----|
| Java 代码 | {java_n} | {java_p0} | {java_p1} | {java_p2} |
| 前端代码 | {fe_n} | {fe_p0} | {fe_p1} | {fe_p2} |
| 文档 | {doc_n} | {doc_p0} | {doc_p1} | {doc_p2} |
| 数据库 | {db_n} | {db_p0} | {db_p1} | {db_p2} |
| **合计** | **{total}** | **{p0}** | **{p1}** | **{p2}** |

## 2. 详情

{per_category}

## 3. 修复建议

### P0 — 必须修(影响运行/数据正确性)
{p0_actions}

### P1 — 建议修(代码腐烂/口径不一致)
{p1_actions}

### P2 — 可延后(注释/历史 DDL/需求说明)
{p2_actions}

## 4. 评级标准

| 评级 | 含义 | 行动 |
|------|------|------|
| **A** | 0 处残留 | 通过 |
| **B** | 1-2 处,多为注释/历史 DDL | 通过,清理即可 |
| **C** | 3-10 处,可能影响 | 1-2 天整改 |
| **D** | >10 处,系统性残留 | 需重新评估 rename 完整性 |

## 5. 工具用法

```bash
python scripts/scan_business_unit.py                       # 全量扫描
python scripts/scan_business_unit.py --json out.json       # 同时输出 JSON
python scripts/scan_business_unit.py --only java           # 仅 java
python scripts/scan_business_unit.py --only frontend       # 仅 frontend
python scripts/scan_business_unit.py --only docs           # 仅 docs
python scripts/scan_business_unit.py --only db             # 仅 db
python scripts/scan_business_unit.py --root .              # 指定项目根
```

## 6. 扫描覆盖

- **预置模式**: `business_unit` (snake_case) / `businessUnit` (camelCase) /
  `BUSINESS_UNIT` (UPPER_CASE) / `BusinessUnit` (PascalCase)
- **Java 范围**: `basedata / dealing / common` 三个模块 `src/main/java/**/*.java`
- **前端范围**: `web/src/**/*.{vue,js,ts}`
- **文档范围**: `docs/**/*.md` + 根目录 `*.md`
- **DB 范围**: `information_schema.columns/tables/views/sequences` + `pg_indexes` + `pg_constraint`
- **排除**: `target/` / `node_modules/` / `.class` / `.jar`
"""


GRADE_DESC = {
    "A": "无残留",
    "B": "1-2 处 P0,非阻塞",
    "C": "3-10 处 P0,需修",
    "D": ">10 处 P0,系统性残留",
}


def build_report(per_cat_hits: dict, total: int) -> str:
    sev_all = severity_count([h for hits in per_cat_hits.values() for h in hits])
    # 2026-07-12 主代理:评级按 P0 数(运行时影响),不看文档 P2
    grade_letter = grade_by_p0(sev_all["P0"])

    per_category_md = []
    for label, hits in per_cat_hits.items():
        per_category_md.append(f"### 2.{label_order(label)} {label} ({len(hits)} 处)\n")
        if not hits:
            per_category_md.append("> 无残留\n")
            continue
        per_category_md.append("| 文件 / 对象 | 行 | 模式 | 严重度 | 命中内容 | 建议 |\n")
        per_category_md.append("|-------------|----|------|--------|----------|------|\n")
        # 排序: P0 > P1 > P2, 然后按文件+行号
        sev_rank = {"P0": 0, "P1": 1, "P2": 2}
        hits_sorted = sorted(hits, key=lambda h: (sev_rank.get(h["severity"], 3), h.get("file", h.get("name", "")), h.get("line", 0)))
        for h in hits_sorted[:200]:  # 最多列 200 条
            target = h.get("file", h.get("name", "?"))
            line = h.get("line", "-")
            text = (h.get("text") or h.get("detail") or "").replace("|", "\\|")[:120]
            fix = suggest_fix(h)
            per_category_md.append(
                f"| `{target}` | {line} | {h.get('pattern', h.get('object_type', '?'))} | {h['severity']} | `{text}` | {fix} |\n"
            )
        if len(hits) > 200:
            per_category_md.append(f"\n> …还有 {len(hits) - 200} 处省略(详见 JSON 输出)\n")
        per_category_md.append("")

    # 使用 str.replace 而非 .format() 以避免模板中字面 { } 冲突
    replacements = {
        "{date}": datetime.date.today().isoformat(),
        "{grade}": grade_letter,
        "{grade_desc}": GRADE_DESC[grade_letter],
        "{total}": str(total),
        "{p0}": str(sev_all["P0"]),
        "{p1}": str(sev_all["P1"]),
        "{p2}": str(sev_all["P2"]),
        "{java_n}": str(len(per_cat_hits.get("Java", []))),
        "{java_p0}": str(severity_count(per_cat_hits.get("Java", []))["P0"]),
        "{java_p1}": str(severity_count(per_cat_hits.get("Java", []))["P1"]),
        "{java_p2}": str(severity_count(per_cat_hits.get("Java", []))["P2"]),
        "{fe_n}": str(len(per_cat_hits.get("前端", []))),
        "{fe_p0}": str(severity_count(per_cat_hits.get("前端", []))["P0"]),
        "{fe_p1}": str(severity_count(per_cat_hits.get("前端", []))["P1"]),
        "{fe_p2}": str(severity_count(per_cat_hits.get("前端", []))["P2"]),
        "{doc_n}": str(len(per_cat_hits.get("文档", []))),
        "{doc_p0}": str(severity_count(per_cat_hits.get("文档", []))["P0"]),
        "{doc_p1}": str(severity_count(per_cat_hits.get("文档", []))["P1"]),
        "{doc_p2}": str(severity_count(per_cat_hits.get("文档", []))["P2"]),
        "{db_n}": str(len(per_cat_hits.get("数据库", []))),
        "{db_p0}": str(severity_count(per_cat_hits.get("数据库", []))["P0"]),
        "{db_p1}": str(severity_count(per_cat_hits.get("数据库", []))["P1"]),
        "{db_p2}": str(severity_count(per_cat_hits.get("数据库", []))["P2"]),
        "{per_category}": "\n".join(per_category_md),
        "{p0_actions}": format_actions(per_cat_hits, "P0"),
        "{p1_actions}": format_actions(per_cat_hits, "P1"),
        "{p2_actions}": format_actions(per_cat_hits, "P2"),
    }
    out = REPORT_TEMPLATE
    for k, v in replacements.items():
        out = out.replace(k, v)
    return out


def label_order(label: str) -> int:
    order = {"Java": 1, "前端": 2, "文档": 3, "数据库": 4}
    return order.get(label, 9)


def suggest_fix(hit) -> str:
    """根据命中给具体修复建议"""
    pat = hit.get("pattern", "")
    obj = hit.get("object_type", "")
    if obj == "COLUMN":
        return f"`ALTER TABLE ... RENAME COLUMN ... TO management_entity_id`"
    if obj == "TABLE":
        return f"`ALTER TABLE ... RENAME TO tms_management_entity_t`"
    if obj == "INDEX":
        return f"`ALTER INDEX ... RENAME TO ...management_entity...`"
    if obj == "CONSTRAINT":
        return f"`ALTER TABLE ... RENAME CONSTRAINT ... TO ...management_entity...`"
    if obj == "SEQUENCE":
        return f"`ALTER SEQUENCE ... RENAME TO tms_management_entity_t_id_seq`"
    if obj == "VIEW":
        return f"修改视图定义,所有 business_unit → management_entity"
    if pat == "snake_case":
        return "替换为 `management_entity`"
    if pat == "camelCase":
        return "替换为 `managementEntity`"
    if pat == "UPPER_CASE":
        return "替换为 `MANAGEMENT_ENTITY`"
    if pat == "PascalCase":
        return "替换为 `ManagementEntity`"
    return "改用 `managementEntity` 命名"


def format_actions(per_cat_hits: dict, severity: str) -> str:
    lines = []
    for label, hits in per_cat_hits.items():
        for h in hits:
            if h["severity"] == severity:
                target = h.get("file") or h.get("name", "?")
                line = h.get("line", "-")
                text = (h.get("text") or h.get("detail") or "")[:80]
                fix = suggest_fix(h)
                lines.append(f"- `{target}`:{line} → `{text}`  →  {fix}")
    if not lines:
        return f"> 无 P{severity[1]} 级残留。\n"
    return "\n".join(lines) + "\n"


# ---------------------------------------------------------------------------
# Main
# ---------------------------------------------------------------------------
def main():
    ap = argparse.ArgumentParser(description="Open-TMS business_unit 残留扫描")
    ap.add_argument("--root", default=".", help="项目根(默认当前目录)")
    ap.add_argument("--json", help="同时输出 JSON 到指定路径")
    ap.add_argument("--report", help="报告输出路径(默认 docs/reviews/business-unit-audit.md)")
    ap.add_argument("--only", choices=["java", "frontend", "docs", "db", "all"],
                    default="all", help="只扫描某个类别")
    args = ap.parse_args()

    project_root = Path(args.root).resolve()
    if not project_root.exists():
        print(f"[ERROR] 项目根不存在: {project_root}", file=sys.stderr)
        return 1

    per_cat_hits: dict = {}
    per_cat_hits["Java"] = []
    per_cat_hits["前端"] = []
    per_cat_hits["文档"] = []
    per_cat_hits["数据库"] = []

    if args.only in ("java", "all"):
        print(f"[SCAN] Java: {len(JAVA_GLOBS)} globs ...")
        per_cat_hits["Java"] = scan_files(JAVA_GLOBS, project_root, "java")

    if args.only in ("frontend", "all"):
        print(f"[SCAN] 前端: {len(FRONTEND_GLOBS)} globs ...")
        per_cat_hits["前端"] = scan_files(FRONTEND_GLOBS, project_root, "frontend")

    if args.only in ("docs", "all"):
        print(f"[SCAN] 文档: {len(DOCS_GLOBS)} globs ...")
        per_cat_hits["文档"] = scan_files(DOCS_GLOBS, project_root, "docs")

    if args.only in ("db", "all"):
        print(f"[SCAN] 数据库: pg8000 ...")
        per_cat_hits["数据库"] = scan_db()

    # 汇总
    total = sum(len(v) for v in per_cat_hits.values())
    sev_all = severity_count([h for hits in per_cat_hits.values() for h in hits])
    # 2026-07-12 主代理:评级按 P0 数(运行时影响)
    grade_letter = grade_by_p0(sev_all["P0"])

    print(f"\n========== 扫描结果 ==========")
    for label, hits in per_cat_hits.items():
        sc = severity_count(hits)
        print(f"  {label:<8s} : {len(hits):>3d} 处 (P0={sc['P0']} P1={sc['P1']} P2={sc['P2']})")
    print(f"  {'合计':<8s} : {total:>3d} 处 (P0={sev_all['P0']} P1={sev_all['P1']} P2={sev_all['P2']})")
    print(f"  评级: {grade_letter} ({GRADE_DESC[grade_letter]})")
    print("================================\n")

    # 输出 JSON
    if args.json:
        out_json = {
            "date": datetime.date.today().isoformat(),
            "grade": grade_letter,
            "total": total,
            "severity": sev_all,
            "categories": {k: v for k, v in per_cat_hits.items()},
        }
        Path(args.json).write_text(json.dumps(out_json, ensure_ascii=False, indent=2), encoding="utf-8")
        print(f"[OK] JSON 写入: {args.json}")

    # 输出报告
    report_path = Path(args.report) if args.report else (project_root / "docs" / "reviews" / "business-unit-audit.md")
    report_path.parent.mkdir(parents=True, exist_ok=True)
    report_path.write_text(build_report(per_cat_hits, total), encoding="utf-8")
    print(f"[OK] 报告写入: {report_path}")

    return 0


if __name__ == "__main__":
    sys.exit(main())
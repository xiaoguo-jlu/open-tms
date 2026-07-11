"""
Open-TMS Bug 修复 skill 配套工具:同类问题扫描
- 预置常见 bug 模式
- 跨 .vue / .java 文件 grep
- 输出 docs/reviews/{bug-name}/05-similar.md 格式
"""
import re
import argparse
import json
from pathlib import Path
from collections import defaultdict

# 预置 bug 模式
BUG_PATTERNS = {
    "res.data.list": {
        "regex": r"res\.data\.list\b",
        "severity": "P0",
        "desc": "前端访问 res.data.list,但后端 MyBatis Plus Page 返回的是 res.data.records",
        "fix": "改为 res.data.records",
        "files": ["**/*.vue", "**/*.js", "**/*.ts"]
    },
    "res.data.total_zero": {
        "regex": r"res\.data\.total\b",
        "severity": "P1",
        "desc": "依赖 res.data.total 字段(已知后端此字段在分页时为 0,应改用 records.length)",
        "fix": "用 records.length 或 fallback 0",
        "files": ["**/*.vue"]
    },
    "res.data.X_猜": {
        "regex": r"res\.data\.\w+",
        "severity": "P2",
        "desc": "前端任意访问 res.data.X 字段(应查 OpenAPI schema 确认字段存在)",
        "fix": "对照 docs/api/openapi.json 验证",
        "files": ["**/*.vue"]
    },
    "hardcoded_id_1": {
        "regex": r"\btest_id\s*=\s*1\b",
        "severity": "P2",
        "desc": "测试代码硬编码 id=1(应改用动态数据)",
        "fix": "用真实测试数据或变量",
        "files": ["**/*.py", "**/*.js"]
    },
    "console.log_left": {
        "regex": r"^\s*console\.log\(",
        "severity": "P2",
        "desc": "遗留 console.log 调试代码(应在 release 前删除)",
        "fix": "删除或改用 ElMessage 提示",
        "files": ["web/src/**/*.vue", "web/src/**/*.js"]
    },
    "TODO_left": {
        "regex": r"\bTODO\b|\bFIXME\b|\bXXX\b",
        "severity": "P2",
        "desc": "遗留 TODO/FIXME 标记",
        "fix": "解决或转为 issue",
        "files": ["**/*.java", "**/*.vue", "**/*.js"]
    }
}


def scan_pattern(pattern_info, project_root="."):
    """扫描单个 bug 模式"""
    regex = pattern_info["regex"]
    results = defaultdict(list)
    for glob in pattern_info["files"]:
        for path in Path(project_root).glob(glob):
            if path.is_file():
                try:
                    text = path.read_text(encoding="utf-8")
                except:
                    continue
                for i, line in enumerate(text.splitlines(), 1):
                    if re.search(regex, line):
                        results[str(path)].append((i, line.strip()))
    return results


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--bug-type", help="预置 bug 类型,例 res.data.list")
    ap.add_argument("--pattern", help="自定义 regex 模式")
    ap.add_argument("--file-glob", default="**/*.vue", help="文件 glob")
    ap.add_argument("--root", default=".", help="项目根")
    ap.add_argument("--output", help="输出报告路径(默认 stdout)")
    args = ap.parse_args()

    # 选择模式
    if args.bug_type and args.bug_type in BUG_PATTERNS:
        pat = BUG_PATTERNS[args.bug_type]
        title = args.bug_type
    elif args.pattern:
        pat = {"regex": args.pattern, "severity": "P?", "desc": "自定义",
               "fix": "(待补)", "files": [args.file_glob]}
        title = f"custom:{args.pattern}"
    else:
        print("ERROR: 必须指定 --bug-type 或 --pattern")
        return

    print(f"=== 扫描 bug 类型: {title} ===")
    print(f"   regex: {pat['regex']}")
    print(f"   severity: {pat['severity']}")
    print(f"   files: {pat['files']}\n")

    results = scan_pattern(pat, args.root)
    total = 0
    for f, hits in results.items():
        print(f"  📁 {f} ({len(hits)} 处)")
        for line_no, line in hits[:5]:
            print(f"     L{line_no}: {line[:100]}")
        if len(hits) > 5:
            print(f"     ... +{len(hits)-5} more")
        total += len(hits)
    print(f"\n=== 总计: {total} 处命中, {len(results)} 个文件 ===")

    # 输出报告
    if args.output:
        report = [f"# 同类问题扫描报告 — {title}", "",
                  f"> 日期: 2026-07-11",
                  f"> 工具: scripts/test/find_similar_bugs.py",
                  f"> 模式: {pat['regex']}",
                  f"> 描述: {pat['desc']}",
                  f"> 严重度: {pat['severity']}",
                  f"> 建议修复: {pat['fix']}",
                  "", "## 命中清单", ""]
        for f, hits in results.items():
            report.append(f"### {f} ({len(hits)} 处)")
            report.append("```")
            for line_no, line in hits:
                report.append(f"L{line_no}: {line}")
            report.append("```")
            report.append("")
        report += [f"## 总结", "",
                   f"- 命中 {total} 处",
                   f"- 涉及 {len(results)} 个文件",
                   f"- 评级: {pat['severity']}",
                   ""]
        Path(args.output).write_text("\n".join(report), encoding="utf-8")
        print(f"\n报告写入: {args.output}")


if __name__ == "__main__":
    main()

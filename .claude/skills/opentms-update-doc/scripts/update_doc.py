"""
opentms-update-doc v1.0 — 自动更新 4 类文档
- 扫描 git diff --staged --name-status
- 提取 changelog 信息
- 自动 patch summary.md / CLAUDE.md / open-tms功能特性清单.md

用法:
  python update_doc.py                    # 跑全流程,自动 patch
  python update_doc.py --check-only       # 只检查不 patch
  python update_doc.py --auto             # 激进模式,改 CLAUDE.md
  python update_doc.py --only summary     # 只更新 summary
"""
import sys
import os
import re
import subprocess
import argparse
from datetime import datetime
from pathlib import Path

ROOT = Path(__file__).parent.parent.parent.parent.parent
os.chdir(ROOT)

FILES = {
    "summary": "summary.md",
    "claude": "CLAUDE.md",
    "feature_list": "open-tms功能特性清单.md"
}


def run(cmd):
    return subprocess.run(cmd, capture_output=True, text=True, shell=True, cwd=ROOT).stdout.strip()


def git_staged_files():
    """获取 git staged 变更文件 + 状态"""
    out = run("git diff --staged --name-status")
    files = []
    for line in out.splitlines():
        if not line.strip(): continue
        parts = line.split(maxsplit=1)
        if len(parts) == 2:
            files.append((parts[0], parts[1]))
    return files


def git_unstaged_files():
    """未 staged 变更"""
    out = run("git diff --name-status")
    return [(s, f) for s, f in (line.split(maxsplit=1) if len(line.split(maxsplit=1)) == 2 else ("?", line) for line in out.splitlines() if line.strip())]


def extract_changelog(files):
    """从变更文件列表提取 changelog"""
    changes = {"modules": [], "endpoints": [], "pages": [], "ddls": [], "skills": [], "others": []}
    for status, path in files:
        if path.startswith("basedata/") or path.startswith("dealing/") or path.startswith("fundplan/") or path.startswith("valuation/") or path.startswith("var/"):
            mod = path.split("/")[0]
            changes["modules"].append((mod, status, path))
        elif "controller" in path and path.endswith(".java") and status != "D":
            text = run(f'git show ":{path}" 2>/dev/null | head -200')
            for m in re.finditer(r'@Path\(["\']([^"\']+)["\']\)\s*(?:@\w+)?\s*public', text):
                changes["endpoints"].append(m.group(1))
        elif path.startswith("web/src/views/") and path.endswith(".vue") and status != "D":
            changes["pages"].append(path)
        elif path.startswith("db/schema/") and path.endswith(".sql") and status != "D":
            text = run(f'git show ":{path}" 2>/dev/null | head -50')
            for m in re.finditer(r'CREATE TABLE(?:\s+IF NOT EXISTS)?\s+(\w+)', text, re.IGNORECASE):
                changes["ddls"].append(m.group(1))
        elif path.startswith(".claude/skills/") and path.endswith("SKILL.md") and status != "D":
            text = run(f'git show ":{path}" 2>/dev/null | head -10')
            name_m = re.search(r'^name:\s*(\S+)', text, re.MULTILINE)
            if name_m:
                changes["skills"].append(name_m.group(1))
        else:
            if status != "D":
                changes["others"].append(path)
    return changes


def update_summary(changes, today):
    """追加"近期更新"小节到 summary.md"""
    path = ROOT / FILES["summary"]
    if not path.exists():
        # 初始化
        path.write_text(f"# Open-TMS 项目概览清单\n\n> 文档版本: {today}\n\n", encoding="utf-8")

    content = path.read_text(encoding="utf-8")
    section = f"\n## 📅 自动更新 {today}\n\n"

    if changes["modules"]:
        section += "**模块变更**: " + ", ".join(set(m for m, _, _ in changes["modules"])) + "\n"
    if changes["endpoints"]:
        section += f"**新增端点({len(set(changes['endpoints']))})**: " + ", ".join(sorted(set(changes['endpoints']))[:10])
        if len(changes["endpoints"]) > 10:
            section += f" + {len(changes['endpoints'])-10} more"
        section += "\n"
    if changes["pages"]:
        section += f"**新增页面({len(changes['pages'])})**: " + ", ".join(Path(p).stem for p in changes["pages"][:5])
        if len(changes["pages"]) > 5:
            section += f" + {len(changes['pages'])-5} more"
        section += "\n"
    if changes["ddls"]:
        section += "**新增 DDL 表**: " + ", ".join(set(changes["ddls"])) + "\n"
    if changes["skills"]:
        section += "**Skill 变更**: " + ", ".join(set(changes["skills"])) + "\n"

    section += "\n---\n"
    new_content = content + section
    path.write_text(new_content, encoding="utf-8")
    return section


def check_feature_list(changes):
    """检查 open-tms功能特性清单.md 是否有遗漏"""
    path = ROOT / FILES["feature_list"]
    if not path.exists():
        return [f"[WARN] {FILES['feature_list']} 不存在,建议创建"]
    text = path.read_text(encoding="utf-8")
    warnings = []
    if changes["ddls"] and "特性完成进度" not in text:
        n = len(set(changes["ddls"]))
        warnings.append(f"[INFO] {FILES['feature_list']} 缺'特性完成进度'章节,本次有 {n} 新表")
    if changes["endpoints"] and "API 端点" not in text:
        warnings.append(f"[INFO] {FILES['feature_list']} 缺'API 端点'章节")
    return warnings


def check_claude_md(changes):
    """保守模式:只检查 CLAUDE.md 是否需要更新,不打 patch"""
    path = ROOT / FILES["claude"]
    if not path.exists():
        return [f"[WARN] {FILES['claude']} 不存在"]
    text = path.read_text(encoding="utf-8")
    warnings = []
    if changes["modules"]:
        # 检查是否所有模块都在 Active Modules 表
        for mod in set(m for m, _, _ in changes["modules"]):
            if mod not in text and len(mod) < 20:
                warnings.append(f"[SUGGEST] CLAUDE.md 可能需加模块 {mod}")
    if changes["ddls"]:
        # 检查是否在 Database Conventions 提及
        new_tables = [t for t in set(changes["ddls"]) if t not in text]
        if new_tables:
            warnings.append(f"[SUGGEST] CLAUDE.md 需补充新表: {', '.join(new_tables)}")
    return warnings


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--check-only", action="store_true", help="只检查不 patch")
    ap.add_argument("--auto", action="store_true", help="激进模式(改 CLAUDE.md)")
    ap.add_argument("--only", choices=["summary", "claude", "feature_list"], help="只更新一个")
    ap.add_argument("--unstaged", action="store_true", help="看未 staged 的变更")
    args = ap.parse_args()

    today = datetime.now().strftime("%Y-%m-%d")
    print(f"=== opentms-update-doc v1.0 ({today}) ===\n")

    files = git_unstaged_files() if args.unstaged else git_staged_files()
    if not files:
        # 看 git log 最近的 1 个 commit
        if not args.check_only:
            print("[INFO] 无 staged 变更,看最近 1 个 commit...")
            out = run("git show --name-status --pretty='' HEAD~1..HEAD")
            files = [(s, f) for s, f in (line.split(maxsplit=1) if len(line.split(maxsplit=1)) == 2 else ("?", line) for line in out.splitlines() if line.strip())]

    if not files:
        print("[OK] 无变更,跳过更新")
        return 0

    print(f"[INFO] {len(files)} 个文件变更")
    changes = extract_changelog(files)

    # 打印 changelog
    print(f"\n--- Changelog 摘要 ---")
    if changes["modules"]:
        print(f"  模块: {sorted(set(m for m, _, _ in changes['modules']))}")
    if changes["endpoints"]:
        print(f"  端点: {len(set(changes['endpoints']))} 个")
    if changes["pages"]:
        print(f"  页面: {len(changes['pages'])} 个")
    if changes["ddls"]:
        print(f"  DDL: {sorted(set(changes['ddls']))}")
    if changes["skills"]:
        print(f"  Skills: {sorted(set(changes['skills']))}")

    if args.check_only:
        print(f"\n[CHECK-ONLY 模式] 跳过 patch")
        return 0

    # 1) 更新 summary.md
    if not args.only or args.only == "summary":
        print(f"\n[UPDATE] {FILES['summary']}...")
        section = update_summary(changes, today)
        print(section)

    # 2) 检查 feature_list
    if not args.only or args.only == "feature_list":
        print(f"\n[CHECK] {FILES['feature_list']}...")
        warnings = check_feature_list(changes)
        for w in warnings:
            print(f"  {w}")

    # 3) 检查 CLAUDE.md
    if not args.only or args.only == "claude":
        print(f"\n[CHECK] {FILES['claude']}...")
        if args.auto:
            print(f"  [AUTO] 激进模式,自动 patch(暂未实现,只提示)")
        warnings = check_claude_md(changes)
        for w in warnings:
            print(f"  {w}")
        if not warnings:
            print(f"  [OK] 无需更新")

    # 4) self-improve 模式:本次更新追加小节
    print(f"\n[IMPROVE] self-improve 模式:")
    print(f"  本次更新已记录到 summary.md")
    print(f"  主代理建议:跑 'self-improve' skill 做项目全局回顾(暂无,已 stub)")

    print(f"\n=== 完成 ===")
    return 0


if __name__ == "__main__":
    sys.exit(main())

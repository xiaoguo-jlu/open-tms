#!/usr/bin/env python3
"""
Open-TMS 会话导出工具
将 Claude Code JSONL 会话文件转换为可读 Markdown
用法: python scripts/maintain/export_conversations.py
"""

import json, os, glob, re
from datetime import datetime, timezone, timedelta

TZ = timezone(timedelta(hours=8))  # UTC+8

SESSION_DIR = os.path.expanduser(
    "~/.claude/projects/F--code-opencode-opentrm"
)
OUTPUT_DIR = "docs/conversations"
os.makedirs(OUTPUT_DIR, exist_ok=True)


def extract_content(message):
    """从 content 数组提取文本（Markdown）"""
    if isinstance(message, str):
        return message
    if not isinstance(message, dict):
        return str(message)

    content = message.get("content", "")
    if isinstance(content, str):
        return content
    if isinstance(content, list):
        parts = []
        for block in content:
            if isinstance(block, dict):
                t = block.get("type", "")
                if t == "text":
                    parts.append(block.get("text", ""))
                elif t == "tool_use":
                    name = block.get("name", "?")
                    inp = block.get("input", {})
                    parts.append(f"\n> 🔧 **{name}**\n> ```json\n> {json.dumps(inp, ensure_ascii=False, indent=2)[:800]}\n> ```\n")
                elif t == "tool_result":
                    parts.append(f"\n> 📋 *工具结果*\n")
            else:
                parts.append(str(block)[:500])
        return "\n".join(parts)
    return str(content)


def format_timestamp(ts):
    """ISO → 可读时间"""
    try:
        dt = datetime.fromisoformat(ts.replace("Z", "+00:00"))
        return dt.astimezone(TZ).strftime("%Y-%m-%d %H:%M")
    except:
        return ts[:16] if ts else "?"


def export_session(filepath):
    """导出单个会话 JSONL → Markdown"""
    basename = os.path.splitext(os.path.basename(filepath))[0]
    output_path = os.path.join(OUTPUT_DIR, f"{basename}.md")

    messages = []
    with open(filepath, encoding="utf-8") as f:
        for line in f:
            try:
                d = json.loads(line)
            except json.JSONDecodeError:
                continue
            t = d.get("type")
            if t in ("user", "assistant"):
                msg = d.get("message", {})
                content = extract_content(msg)
                if content and content.strip():
                    messages.append({
                        "role": t,
                        "content": content.strip(),
                        "time": format_timestamp(d.get("timestamp", ""))
                    })

    if not messages:
        return None

    first_time = messages[0]["time"]
    last_time = messages[-1]["time"]

    lines = [
        f"# 会话 {basename[:8]}",
        f"**时间**: {first_time} ~ {last_time}",
        f"**消息数**: {len(messages)}",
        "",
        "---",
        ""
    ]

    for m in messages:
        if m["role"] == "user":
            lines.append(f"## 👤 用户 ({m['time']})")
        else:
            lines.append(f"## 🤖 Claude ({m['time']})")
        lines.append("")
        lines.append(m["content"])
        lines.append("")
        lines.append("---")
        lines.append("")

    with open(output_path, "w", encoding="utf-8") as f:
        f.write("\n".join(lines))

    return {
        "file": output_path,
        "messages": len(messages),
        "time": f"{first_time} ~ {last_time}"
    }


def main():
    print("Open-TMS 会话导出工具")
    print("=" * 50)

    sessions = sorted(
        glob.glob(os.path.join(SESSION_DIR, "*.jsonl")),
        key=os.path.getmtime, reverse=True
    )

    exported = []
    for sp in sessions:
        name = os.path.basename(sp)
        out_file = os.path.join(OUTPUT_DIR, os.path.splitext(name)[0] + ".md")
        if os.path.exists(out_file):
            size = os.path.getsize(sp)
            if size < 5000:  # 跳过太小的（可能是空会话）
                continue

        result = export_session(sp)
        if result:
            exported.append(result)
            print(f"  [OK] {name[:16]}... → {result['messages']} 条消息, {result['time']}")

    if not exported:
        print("  (所有会话已导出，无新内容)")
    else:
        print(f"\n导出完成: {len(exported)} 个会话 → {OUTPUT_DIR}/")

    # 生成索引文件
    index_path = os.path.join(OUTPUT_DIR, "INDEX.md")
    with open(index_path, "w", encoding="utf-8") as f:
        f.write("# 会话索引\n\n")
        f.write("| 会话 | 时间 | 消息数 |\n")
        f.write("|------|------|--------|\n")
        for md in sorted(glob.glob(os.path.join(OUTPUT_DIR, "*.md"))):
            name = os.path.basename(md)
            if name == "INDEX.md":
                continue
            rel = os.path.relpath(md, OUTPUT_DIR)
            size = sum(1 for _ in open(md, encoding="utf-8"))
            f.write(f"| [{name}]({rel}) | — | {size} 行 |\n")
    print(f"索引: {index_path}")


if __name__ == "__main__":
    main()

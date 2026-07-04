#!/usr/bin/env python3
"""Claude Code Stop hook — 会话结束时自动导出对话到 Markdown"""
import subprocess, sys
script = "F:/code/opencode/opentrm/scripts/maintain/export_conversations.py"
try:
    subprocess.run([sys.executable, script], check=True, timeout=30)
except Exception:
    pass  # 静默失败，不影响会话结束

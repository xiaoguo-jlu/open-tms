#!/bin/bash
# opentms-update-doc skill — pre-commit hook 安装脚本
# 用法: bash .claude/skills/opentms-update-doc/scripts/install-hook.sh

set -e

ROOT="$(cd "$(dirname "$0")/../../../.." && pwd)"
HOOK_FILE="$ROOT/.git/hooks/pre-commit"
SOURCE="$ROOT/.claude/skills/opentms-update-doc/scripts/update_doc.py"

if [ ! -d "$ROOT/.git" ]; then
    echo "ERROR: $ROOT 不是 git 仓库"
    exit 1
fi

if [ ! -f "$SOURCE" ]; then
    echo "ERROR: $SOURCE 不存在"
    exit 1
fi

# 写 pre-commit hook
cat > "$HOOK_FILE" << 'EOF'
#!/bin/bash
# opentms-update-doc pre-commit hook(自动生成)

# 1) 跑 update_doc.py --check-only
python .claude/skills/opentms-update-doc/scripts/update_doc.py --check-only
RC=$?

if [ $RC -ne 0 ]; then
    echo "[opentms-update-doc] 检测到需要更新"
    echo "[AUTO] 跑自动更新..."
    python .claude/skills/opentms-update-doc/scripts/update_doc.py
    # 自动 stage 更新后的 3 个文件
    git add summary.md CLAUDE.md "open-tms功能特性清单.md" 2>/dev/null || true
fi
EOF

chmod +x "$HOOK_FILE"
echo "✓ pre-commit hook 已安装: $HOOK_FILE"
echo ""
echo "用法:"
echo "  1. git add . <files>"
echo "  2. git commit -m '...'"
echo "  3. hook 自动检查 + 更新文档"
echo ""
echo "跳过 hook(紧急): git commit --no-verify"

---
name: conversation-archiver
description: Use when user asks to save conversation to a file in harness/ directory. Saves chat messages to weekly files named chat-YYYY-MM-DD-YYYY-MM-DD.md in the harness directory.
---

# Conversation Archiver

## 用途

将用户与AI的对话保存到 `harness/` 目录下的每周文件中，文件命名格式：`chat-开始日期-结束日期.md`

## 使用场景

- 用户明确要求保存对话
- 每周结束需要归档本周对话
- 对话结束前需要存档

## 文件命名规则

```
chat-{开始日期}-{结束日期}.md
```

示例：`chat-2026-05-25-2026-05-31.md`

## 操作步骤

### 收集对话内容

1. 遍历本会话中的所有消息
2. 按时间顺序整理
3. 格式化为Markdown

### 保存文件

```bash
# 确定文件路径
# 当前日期决定文件名
# 如果是周一到周四：本周还未结束，用上周一到上周日的日期范围
# 如果是周五到周日：用本周一到本周日的日期范围

# 写入文件
# 文件路径: harness/chat-{开始日期}-{结束日期}.md
```

### 文件格式

```markdown
# Open-TMS 对话归档

## 基本信息
- 开始日期: YYYY-MM-DD
- 结束日期: YYYY-MM-DD
- 对话轮次: N

---

## 对话内容

### 用户
{用户消息}

### AI
{AI回复}

---

## 总结
{对话摘要或关键成果}
```

## 触发条件

**必须调用本skill的场景**：
- 用户说"保存对话"、"归档对话"
- 用户说"将对话保存到文件"
- 用户说"记住，保存我与你的对话"
- 任何明确要求保存对话的指令

## 注意事项

1. **立即保存**：收到保存请求时立即执行，不要等到会话结束
2. **覆盖检查**：如果文件已存在，追加内容而非覆盖
3. **时间戳准确**：使用实际对话发生的日期
4. **保留上下文**：保留技术上下文，便于后续参考
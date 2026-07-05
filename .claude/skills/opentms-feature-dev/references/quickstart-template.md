# 快速启动模板

用于快速创建一个新特性的完整跟踪体系。

```bash
# === 快速启动一个特性的全流程 ===
# 1. 确定特性基本信息
NAME="{特性名称}"
MODULE="{模块名}"
VERSION="M1-x"
PRIORITY="P0"

# 2. 创建Feature Issue
gh issue create \
  --title "[Feature] $NAME" \
  --body "## 特性描述\n...\n## 目标版本\n$VERSION\n## 优先级\n$PRIORITY" \
  --label "PM,Feature"

FEATURE_ISSUE={上一步返回的issue编号}

# 3. 创建各角色Task
gh issue create --title "[PM] ${NAME}PRD设计" --body "关联Feature: #$FEATURE_ISSUE" --label "PM,Task"
gh issue create --title "[UX] ${NAME}界面设计" --body "关联Feature: #$FEATURE_ISSUE" --label "UX,Task"
gh issue create --title "[TA] ${NAME}表结构设计" --body "关联Feature: #$FEATURE_ISSUE" --label "TA,Task"
gh issue create --title "[TA] ${NAME}接口设计" --body "关联Feature: #$FEATURE_ISSUE" --label "TA,Task"
gh issue create --title "[Dev] ${NAME}后端开发" --body "关联Feature: #$FEATURE_ISSUE" --label "Dev,Task"
gh issue create --title "[Dev] ${NAME}前端开发" --body "关联Feature: #$FEATURE_ISSUE" --label "Dev,Task"
gh issue create --title "[QA] ${NAME}测试" --body "关联Feature: #$FEATURE_ISSUE" --label "QA,Task"

# 4. 更新Feature Issue添加进度跟踪表
gh issue edit $FEATURE_ISSUE --body "## 进度跟踪\n| 阶段 | 状态 |\n|------|------|\n| PM设计 | ⬜ 待开始 |\n| UX设计 | ⬜ 待开始 |\n| 表结构设计 | ⬜ 待开始 |\n| 接口设计 | ⬜ 待开始 |\n| 后端开发 | ⬜ 待开始 |\n| 前端开发 | ⬜ 待开始 |\n| 测试设计 | ⬜ 待开始 |\n| 测试执行 | ⬜ 待开始 |"

echo "特性 $NAME 已启动！Feature Issue: #$FEATURE_ISSUE"
```

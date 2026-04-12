---
name: opentms-dev
description: Use when implementing Open-TMS backend features as Developer
---

# Open-TMS Developer Skill

## Overview
Dev负责功能代码实现、技术文档编写、代码审查。使用GitHub Projects管理开发任务。

## 项目信息
- **GitHub Projects**: https://github.com/users/xiaoguo-jlu/projects/2
- **仓库**: https://github.com/xiaoguo-jlu/open-tms
- **Java项目**: basedata, bankaccount, common等模块

## 核心职责

### 1. 领取任务
```bash
# 查看分配给自己的任务
gh issue list --label "Dev"

# 开始开发前将状态改为In Progress
# (通过编辑body添加标记)
gh issue edit <number> --body "## 状态\nIn Progress\n\n## 开发内容..."
```

### 2. 代码开发
```bash
# 创建分支
git checkout -b dev/issue-5-bank-account

# 开发完成后提交
git add .
git commit -m "feat: 银行账户表设计 (#5)"

# 推送
git push -u origin dev/issue-5-bank-account
```

### 3. 任务完成
```bash
# 更新Issue内容
gh issue edit <number> --body "## 完成情况
- [x] Entity类
- [x] Mapper接口
- [x] Service层

## 代码位置
- Entity: basedata/src/main/java/.../entity/BankAccount.java
- Mapper: basedata/src/main/java/.../mapper/BankAccountMapper.java
- Service: basedata/src/main/java/.../service/BankAccountService.java"

# 关闭任务
gh issue close <number>
```

## 项目结构
```
opentrm/
├── basedata/           # 基础数据模块
│   └── src/main/java/
│       └── com/opentrms/basedata/
│           ├── controller/
│           ├── service/
│           ├── mapper/
│           ├── entity/
│           └── dto/
├── common/             # 公共模块
│   └── src/main/java/com/opentrms/common/
├── bankaccount/       # 银行账户模块(M1-2)
└── web/                # 前端项目
```

## 开发规范

### Entity开发
```java
@Data
@Entity
@Table(name = "tms_bank_account_t")
public class BankAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(name = "account_name", length = 100)
    private String accountName;
    
    @Column(name = "account_no", length = 50)
    private String accountNo;
    
    @Column(name = "is_deleted")
    private Boolean isDeleted;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
```

### Service开发
```java
public interface BankAccountService {
    BankAccount create(BankAccountDTO dto);
    BankAccount update(String id, BankAccountDTO dto);
    void delete(String id);
    BankAccount getById(String id);
    Page<BankAccount> list(PageDTO dto);
}
```

### Controller开发
```java
@RestController
@RequestMapping("/api/v1/bank-accounts")
public class BankAccountController {
    
    @PostMapping
    public Result<BankAccount> create(@Valid @RequestBody BankAccountDTO dto) {
        return Result.success(service.create(dto));
    }
    
    @GetMapping("/{id}")
    public Result<BankAccount> getById(@PathVariable String id) {
        return Result.success(service.getById(id));
    }
}
```

## 提交规范
```
feat: 新功能
fix: 修复bug
refactor: 重构
docs: 文档更新
style: 格式调整
chore: 构建/辅助工具
```

## 交付检查清单
- [ ] 代码是否符合编码规范
- [ ] 是否有单元测试
- [ ] API文档是否齐全
- [ ] 提交信息是否清晰

## 快速参考

| 操作 | 命令 |
|------|------|
| 查看任务 | `gh issue list --label "Dev"` |
| 创建分支 | `git checkout -b dev/issue-<number>-描述` |
| 提交 | `git commit -m "feat: 描述 (#issue编号)"` |
| 完成任务 | `gh issue edit <number> --body` + `gh issue close <number>` |

---

## Bug修复流程

### 1. 拉取QA提出的Bug
```bash
# 查看所有Bug
gh issue list -R xiaoguo-jlu/open-tms --label "Bug"

# 查看具体Bug详情
gh issue view <number> -R xiaoguo-jlu/open-tms
```

### 2. 处理Bug
```bash
# 将Bug状态置为处理中
gh issue edit <number> -R xiaoguo-jlu/open-tms --add-label "Dev,Task" --body "## 状态\n处理中\n\n## 开发内容..."
```

### 3. 修复并验证
- 检查Entity字段与数据库表结构是否一致
- 编译验证: `mvn compile -f basedata/pom.xml`
- 运行测试脚本验证POST接口:
```powershell
powershell -File test/scripts/basedata/test_all_post.ps1
```

### 4. 提交并转交QA
```bash
# 提交代码
git add basedata/
git commit -m "fix: 修复POST接口500错误 (#<number>)"

# 更新Issue
gh issue edit <number> -R xiaoguo-jlu/open-tms --body "## 状态\n已完成修复，Dev验证通过，待QA验证\n\n## 修复内容\n- 修复Entity字段与数据库不匹配\n- 添加GlobalExceptionHandler\n\n## Dev验证结果\n- Bank: 200 OK\n- BusinessUnit: 200 OK\n..."
```

---

## 常见问题排查

### POST接口500错误排查步骤
1. **检查Entity字段与数据库是否一致**: Entity中的字段必须在数据库表中有对应列
2. **检查数据库表结构**: 查看 `db/schema/*.sql` 中表定义
3. **添加异常处理器**: 创建 `GlobalExceptionHandler` 捕获并记录详细错误
4. **检查必填字段**: 数据库NOT NULL字段在Entity中必须赋值
5. **检查唯一约束**: 避免重复提交导致唯一约束冲突

### Entity字段匹配检查
```bash
# 1. 查找Entity中的字段
grep "private String remark" basedata/src/main/java/com/opentms/basedata/entity/*.java

# 2. 检查数据库表是否有此字段
grep "remark" db/schema/01-basedata.sql

# 3. 如果数据库无此字段，从Entity中移除
```

### 编译验证
```bash
mvn compile -f basedata/pom.xml
```

---

## 调试技巧

### 1. 添加全局异常处理器
创建 `basedata/src/main/java/com/opentms/basedata/config/GlobalExceptionHandler.java`:
```java
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public Result<Void> handleRuntimeException(RuntimeException e) {
        log.error("业务异常: {}", e.getMessage(), e);
        return Result.error(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常: {}", e.getMessage(), e);
        return Result.error("系统异常: " + e.getMessage());
    }
}
```

### 2. 运行测试脚本验证接口
```powershell
# 测试基础数据模块所有POST接口
powershell -File test/scripts/basedata/test_all_post.ps1

# 其他模块测试脚本
# test/scripts/<module>/test_*.ps1
```
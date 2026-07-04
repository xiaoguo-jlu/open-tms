# 代码审查报告

**审查日期**: 2026-06-04
**审查范围**: 最近 5 个提交 (c41f662 ~ c41f662~5)
**审查级别**: medium

---

## 🔴 严重问题

### 1. 序列号生成存在并发竞态条件

**文件**:
- `dealing/src/main/java/com/opentms/dealing/service/impl/DealServiceImpl.java:561-577`
- `dealing/src/main/java/com/opentms/dealing/service/impl/AtDealServiceImpl.java:615-630`

```java
private String generateDealNumber() {
    // ...
    Deal lastDeal = getOne(wrapper);
    int seq = 1;
    if (lastDeal != null) {
        String lastSeqStr = lastNo.substring(prefix.length());
        seq = Integer.parseInt(lastSeqStr) + 1;  // ← 并发时多个请求可能获得相同序列号
    }
    return prefix + String.format("%04d", seq);
}
```

**问题**: 查询最后序列号和插入新记录之间不是原子操作，高并发下可能生成重复的 dealNumber。

**建议**: 使用数据库序列或分布式锁（如 Redisson）来保证原子性。

---

### 2. BeanUtils.copyProperties 会覆盖不应更新的字段

**文件**: `DealServiceImpl.java:243`

```java
BeanUtils.copyProperties(dealDTO, existingDeal);  // 会覆盖 id, dealNumber, createdBy, createdAt 等
```

**问题**: `updateDeal` 中使用 BeanUtils 复制所有属性，会把 `id`、`dealNumber`、`createdBy`、`createdAt` 等字段覆盖掉，可能导致数据损坏。

**建议**: 手动设置需要更新的字段，或使用 Spring 的 `BeanUtils.copyProperties(source, target, ignoreProperties...)` 忽略不需要复制的字段。

---

### 3. updateDeal 中缺失 dealNumber 设置

**文件**: `DealServiceImpl.java:243-248`

```java
// 4. Update Deal
BeanUtils.copyProperties(dealDTO, existingDeal);
existingDeal.setUpdatedBy(dealDTO.getOperator());
existingDeal.setUpdatedAt(now);
existingDeal.setVersion(newVersion);
existingDeal.setLatestActionNumber(existingAction.getActionNumber());
updateById(existingDeal);  // ← existingDeal 的 dealNumber 可能被 BeanUtils 覆盖为 null
```

**问题**: BeanUtils 复制时，如果 `dealDTO.dealNumber` 为 null，会把 `existingDeal.dealNumber` 也设为 null。

---

## 🟠 中等问题

### 4. 硬编码的 SQL 查询字符串

**文件**:
- `DealServiceImpl.java:522-556`
- `AtDealServiceImpl.java:543-612`

```java
"SELECT name FROM tms_management_entity_t WHERE code = ? AND deleted = '0'"
```

**问题**:
- 表名硬编码，如果表结构变更需要修改多处
- 使用字符串拼接 SQL（虽然用了参数化查询，但表名硬编码）
- 异常被静默吞掉，无法追踪问题

**建议**: 考虑使用 Mapper 或 Repository 模式替代 JDBC 直查。

---

### 5. 序列号格式可能溢出

**文件**: `DealServiceImpl.java:576`

```java
return prefix + String.format("%04d", seq);  // 超过 9999 会变成 10000，格式失效
```

**问题**: 当一天内交易超过 9999 笔时，格式 `%04d` 无法正确补零。

**建议**: 使用更长的序列号格式（如 `%06d`）或改用时间戳+随机数。

---

### 6. 嵌套的 try-catch 逻辑混乱

**文件**: `AtDealServiceImpl.java:574-590`

```java
try {
    String instName = jdbcTemplate.queryForObject(
            "SELECT instrument_name FROM tms_at_instruments_t WHERE ...", ...);
} catch (Exception e) {
    try {  // ← 嵌套 try-catch，逻辑不清晰
        String instName = jdbcTemplate.queryForObject(
                "SELECT instrument_name FROM tms_instruments_t WHERE ...", ...);
    } catch (Exception e2) { }
}
```

**问题**: 同一个 `instrumentId` 在两个不同的表中查询，逻辑不清晰且容易出错。

---

### 7. 代码重复 - approveDeal/rejectDeal/executeDeal

**文件**: `DealServiceImpl.java` 和 `AtDealServiceImpl.java`

两个 Service 中的 `approveDeal`、`rejectDeal`、`executeDeal`、`submitDeal`、`deleteDeal` 方法逻辑几乎完全相同，仅在访问 `Deal` 的方式上略有差异（一个用 `getById`，一个用 `dealMapper.selectById`）。

**建议**: 抽取到抽象父类或工具类中。

---

## 🟡 轻微问题

### 8. API 风格不一致

**文件**: `basedata/src/main/java/com/opentms/basedata/controller/BankAccountResource.java:209`

```java
@PUT  // 其他资源用 @POST + /update
public Object update(BankAccount account) { ... }
```

**问题**: 与项目其他 Controller 的 REST 风格不一致（其他用 `@POST /update`）。

---

### 9. 异常处理过于宽泛

**文件**: 多处 `catch (Exception e)`

**问题**: 捕获所有异常并只返回消息，丢失了堆栈信息，不利于排查问题。

**建议**:
```java
} catch (Exception e) {
    log.error("Failed to save deal", e);
    return Result.error(e.getMessage());
}
```

---

### 10. 缺少必要的输入验证

**文件**: `DealServiceImpl.saveDeal()`, `updateDeal()`

**问题**: 没有对 `dealDTO` 的必填字段进行非空校验。

---

## 📊 总结

| 严重程度 | 数量 |
|---------|------|
| 🔴 严重  | 3    |
| 🟠 中等  | 4    |
| 🟡 轻微  | 3    |

### 最需要修复的问题

| 优先级 | 问题 | 严重程度 |
|-------|------|---------|
| 1 | 序列号生成的并发问题 | 🔴 严重 |
| 2 | BeanUtils.copyProperties 覆盖不应更新的字段 | 🔴 严重 |
| 3 | 序列号格式溢出风险 | 🟠 中等 |

---

*审查者: Claude Code*

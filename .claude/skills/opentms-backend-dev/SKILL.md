---
name: opentms-backend-dev
description: Use when implementing Open-TMS backend API and business logic as Backend Developer
---

# Open-TMS 后端开发 Skill (BE)

## 简介

本 skill 指导后端开发人员完成 Java 17 + Spring Boot 3.2.0 + Apache CXF 4.0.3 (JAX-RS) + MyBatis Plus 3.5.5 技术栈下的接口实现，覆盖从 API 设计到可运行代码 + 自测的完整流程。

---

## 一、触发条件

**触发场景**: API 接口文档已完成且分配后端任务 / 需实现新接口 / 重构或修复后端代码 / QA 发现后端缺陷。

---

## 二、输入要求

| 输入项 | 来源 | 说明 |
|--------|------|------|
| API 接口文档 | Dev/TA | 接口契约 |
| 数据库设计 | TA | DDL / 表结构 |
| 模块已有代码 | `{module}/src/main/java/com/opentms/{module}/` | 风格参考 |
| 模块历史摘要 | `{module}/SUMMARY.md` | 若存在 |

可选: PRD 文档、技术方案文档。

---

## 三、输出规范

### 3.1 包结构

```
com.opentms.{module}/
├── controller/        # JAX-RS Resource (@Path/@GET/@POST)
├── service/          # 服务接口
│   └── impl/         # 服务实现
├── entity/           # 持久化实体
├── dto/              # 入参 DTO
├── vo/               # 出参 VO
├── mapper/           # MyBatis Mapper
└── constant/         # 常量
```

### 3.2 文件映射

| 类型 | 路径 |
|------|------|
| Controller | `{module}/src/main/java/com/opentms/{module}/controller/{Entity}Resource.java` |
| Service | `{module}/src/main/java/com/opentms/{module}/service/{Entity}Service.java` |
| Service Impl | `{module}/src/main/java/com/opentms/{module}/service/impl/{Entity}ServiceImpl.java` |
| Entity | `{module}/src/main/java/com/opentms/{module}/entity/{Entity}.java` |
| DTO | `{module}/src/main/java/com/opentms/{module}/dto/{Entity}DTO.java` |
| VO | `{module}/src/main/java/com/opentms/{module}/vo/{Entity}VO.java` |
| Mapper | `{module}/src/main/java/com/opentms/{module}/mapper/{Entity}Mapper.java` |
| Mapper XML | `{module}/src/main/resources/mapper/{Entity}Mapper.xml` |

---

## 四、执行步骤

### 步骤0: REST API 规范速查

**项目实际使用 Apache CXF JAX-RS,路由由 @Path 定义。遵循 CLAUDE.md 红线: update/delete 一律 POST。**

| 操作 | 方法 | 路径 |
|------|------|------|
| 分页查询 | GET | `/api/v1/{entities}/page` |
| 详情查询 | GET | `/api/v1/{entities}/{id}` |
| 新增 | POST | `/api/v1/{entities}` |
| 更新 | POST | `/api/v1/{entities}/update` |
| 删除 | POST | `/api/v1/{entities}/delete/{id}` |
| 提交/审批/驳回/执行 | POST | `/api/v1/{entities}/{id}/{action}` |

> 详细注解规范和 Result 响应格式见 `references/conventions.md`。

### 步骤1: 读取输入

1. 阅读 API 文档，理解接口定义和参数
2. 阅读 DDL，理解表结构 (严禁捏造表结构中不存在的字段)
3. 检查同模块已有代码，确认风格和可复用实现

### 步骤2: 检查设计一致性

对照 `docs/规范/Open-TMS开发规范文档.md` 和 `CLAUDE.md`:
- 类/方法命名 → `references/conventions.md`
- 包结构是否正确
- 状态字符串是否从 `GlobalConstants` 取
- 审计字段是否完整

### 步骤3-8: 分层开发 (Entity → DTO → VO → Mapper → Service → Controller)

**按以下顺序逐层开发，每层先参考 `examples/java-templates.md` 中的模板，再根据实际表结构定制。**

**步骤3 — Entity:**
- 继承 `BaseCodeEntity`(代码-名称型) 或 `BaseEntity`(通用型)
- `@TableName("tms_{entity}_t")` + 字段用 `@TableField` 标注蛇形列名
- 金额用 `BigDecimal`，日期用 `LocalDate`/`LocalDateTime`

**步骤4 — DTO:**
- 入参对象，添加 Jakarta Validation 注解 (`@NotBlank`/`@NotNull`/`@DecimalMin`)
- 与 Entity 字段一一对应但只保留前端传入字段

**步骤5 — VO:**
- 出参对象，可附加关联查询字段 (如 `countryName`)
- 日期字段加 `@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")`

**步骤6 — Mapper:**
- 继承 `BaseMapper<{Entity}>` + `@Mapper` 注解
- 复杂查询写 XML (`src/main/resources/mapper/`)

**步骤7 — Service:**

核心规则:
1. ServiceImpl **直接** extend `ServiceImpl<{Entity}Mapper, {Entity}>`，**不可**通过泛型中间层 (LambdaQueryWrapper 中 `T::getCode` 在运行时擦除为 `BaseEntity::getCode` 会导致 MyBatis Plus 缓存找不到)
2. entity → VO 转换：简单实体用 `BeanUtils.copyProperties`；复杂实体手动映射
3. 编码唯一性校验：新增/更新前调用 `checkCodeExists(code, excludeId)`
4. 写操作加 `@Transactional(rollbackFor = Exception.class)`

**步骤8 — Controller (JAX-RS Resource):**
- 注解: `@Component` + `@Path("/api/v1/{entities}")`
- 查询用 `@GET` + `@Produces(MediaType.APPLICATION_JSON)`
- 写操作用 `@POST` + `@Consumes(MediaType.APPLICATION_JSON)` + `@Produces`
- ID 参数用 `@PathParam`，查询参数用 `@QueryParam`
- ID 解析: catch `NumberFormatException` 返回 `Result.badRequest`
- 全局异常由 `GlobalExceptionHandler` 统一捕获，不要在 Controller try-catch 每个方法

> 完整模板代码: `examples/java-templates.md`

### 步骤9: 事务管理

- 写操作: `@Transactional(rollbackFor = Exception.class)`
- 只读操作: `@Transactional(readOnly = true)` (可选，Service 层)
- 复杂业务考虑编程式事务或手动 flush

### 步骤10: 异常处理

- 业务异常抛出 `BusinessException` (由 `GlobalExceptionHandler` 处理)
- **禁止**直接抛 `RuntimeException` 用于业务错误 — 用户看不到友好提示
- 参数校验异常由 Jakarta Validation + `GlobalExceptionHandler` 自动处理

### 步骤11: 日志记录

- 关键操作 INFO: `log.info("[{}] 用户[{}] 执行[{}], 业务编号[{}]", opType, user, entity, no)`
- 异常 ERROR: `log.error("交易[{}] 执行失败: {}", txNo, e.getMessage(), e)` — 必须含堆栈
- 不要在 prod 输出 SQL 参数 (用 `log.debug`)

### 步骤12: 单元测试

- Service 层单元测试: 覆盖正常 + 异常场景
- 使用 Mockito 模拟依赖
- 测试脚本: `scripts/test/test_{module}_api.py`

### 步骤13: API 自测验证 (必须)

**必须全部通过才能提交:**

1. 启动服务: `python scripts/test/test_all.py` 或手动启动对应模块 JAR
2. 必测场景:
   - [ ] GET `/api/v1/{entity}` 返回 200 + code 200
   - [ ] GET `/api/v1/{entity}/page` 分页正常
   - [ ] POST `/api/v1/{entity}` 新增成功
   - [ ] POST `/api/v1/{entity}/update` 更新成功
   - [ ] POST `/api/v1/{entity}/delete/{id}` 删除成功
3. 异常场景:
   - [ ] GET `/api/v1/{entity}/99999` → 不存在的 ID 返回友好提示
   - [ ] POST `/api/v1/{entity}/delete/99999` → 返回业务错误
   - [ ] 重复编码新增 → 返回编码已存在

**禁止**: API 返回 HTTP 200 但 body code 为 500 的情况。

### 步骤14: 编译验证

```bash
mvn clean package -pl {module} -am
```

修正所有编译错误直到通过。

### 步骤15: 生成开发摘要

更新 `{module}/SUMMARY.md`:
- 本次完成的接口列表
- 遇到的问题及解决方案
- 待确认事项

### 步骤16: Skill 优化

总结本次开发中导致问题的 skill 指导内容，更新本 skill 或对应 reference 文件。

---

## 五、业界优秀实践 (精简)

- **面向对象**: 合理继承、接口分离、依赖注入解耦
- **资金精度**: 金额一律 `BigDecimal`，计算用 `setScale` 指定舍入模式
- **事务一致性**: 写操作强事务，幂等性设计 (`X-Idempotency-Key`)
- **审计追溯**: 完整操作日志，变更前后值记录

---

## 六、与其他 Skill 的衔接

```
数据库设计 ──▶ 后端开发 ──▶ 前端开发
                  │
                  ▼
              代码审查 ──▶ 测试设计
```

前置: opentms-db-design(DDL) / opentms-api-design(接口文档)
后续: 前端开发 / 代码审查(opentms-review-backend) / 测试(opentms-test-case-design)

---

## 七、质量标准

| 检查项 | 标准 | 权重 |
|--------|------|------|
| 规范符合性 | 命名/包结构/注解符合规范 | 20% |
| 功能正确性 | 实现所有接口需求 | 25% |
| 事务正确性 | @Transactional + rollbackFor | 20% |
| 异常处理 | BusinessException + GlobalExceptionHandler | 15% |
| 日志规范 | 关键节点 INFO + 异常 ERROR | 10% |
| API 自测 | 全部必测场景通过 | 10% |

---

## 八、交付物检查清单

**代码**: Entity/DTO/VO/Mapper/Service/Controller 全部完成，编译通过
**功能**: CRUD + 分页正常，事务一致性正确
**异常**: 不存在ID/重复编码等场景返回友好错误
**API 自测**: 全部必测 + 异常场景通过
**摘要**: 开发摘要已记录

---

## 九、关键模式

### 9.1 BusinessException vs RuntimeException

```java
// 正确: 业务校验失败抛 BusinessException
if (checkCodeExists(entity.getCode(), entity.getId())) {
    throw new BusinessException("编码已存在: " + entity.getCode());
}
if (existing == null) {
    throw new BusinessException("记录不存在");
}

// 错误: 不要直接抛 RuntimeException
// throw new RuntimeException("编码已存在");  ← 前端看不到友好提示
```

**原则**: 所有面向用户的业务错误用 `BusinessException`；系统级不可恢复错误 (如 NPE、IO) 才走 RuntimeException，由 `GlobalExceptionHandler` 兜底。

### 9.2 @Version 乐观锁模式

BaseEntity 中声明 `@Version private Integer version`，MyBatis Plus 在 UPDATE 时自动在 WHERE 子句追加 `version = ?` 并 SET `version = version + 1`。并发更新场景下，version 不匹配会抛 `OptimisticLockException`。

```java
// BaseEntity.java — 项目已定义
@Version
private Integer version;

// UPDATE 时 MyBatis Plus 生成:
// UPDATE tms_xxx_t SET ..., version = version + 1 WHERE id = ? AND version = ?
```

**使用要点**:
1. 更新前不需要手动递增 version
2. 更新失败时捕获异常并提示 "数据已被他人修改，请刷新重试"
3. 资金交易场景务必保留 @Version

### 9.3 DealMap v3.2 单字段多行模式

交易属性不再用多个列 (如 `buy_amount`/`sell_amount`/`rate`)，而是用一个 `amount_or_rate` 字段 + `dealmap_type` 区分含义。一行 Deal 对应多行 DealMap，每行通过 `dealmap_type` 标识该行的含义。

```java
// DealMap v3.2 核心字段
@TableField("dealmap_type")
private String dealmapType;     // FX_BUY_AMOUNT / FX_SELL_AMOUNT / FX_RATE / FX_FIX / AC / AT

@TableField("amount_or_rate")
private BigDecimal amountOrRate; // 单字段替代 buy_amount/sell_amount/rate

// 示例: 一笔 FX Deal 产生 3 行 DealMap
// Row 1: dealmapType=FX_BUY_AMOUNT,  amountOrRate=1000000.00
// Row 2: dealmapType=FX_SELL_AMOUNT, amountOrRate=7000000.00
// Row 3: dealmapType=FX_RATE,         amountOrRate=7.12345678
```

**优点**: 属性可无限扩展而不需改表结构；DealMap 行通过 `dealmap_number` 关联到 Cashflow。

### 9.4 BaseService<DTO, Entity, VO> 抽象与 Lambda 类型擦除陷阱

**陷阱**: 如果在抽象 BaseService 中用泛型 T 写 LambdaQueryWrapper，运行时 T 被擦除为 `BaseEntity`，MyBatis Plus lambda 缓存找不到具体列名，导致异常。

```java
// 错误: 泛型 T 的 lambda 在运行时擦除
public abstract class BaseServiceImpl<M extends BaseMapper<T>, T extends BaseEntity> {
    public IPage<V> queryPage(...) {
        wrapper.orderByDesc(T::getCreatedAt); // 运行时 T = BaseEntity，缓存 miss!
    }
}

// 正确: 每个 ServiceImpl 直接 extend ServiceImpl<M, T>，用具体实体类
public class CountryServiceImpl extends ServiceImpl<CountryMapper, Country> {
    wrapper.orderByDesc(Country::getCreatedAt); // Country 是具体类，安全
}
```

**如果要写通用 BaseService**: 将 lambda 表达式作为参数传入，不由基类内部构造。

```java
// 安全模式: 子类提供具体 lambda
protected abstract <T> SFunction<T, ?> getOrderColumn(); // 子类返回 Country::getCreatedAt
```

---

## 十、参考资源

| 资源 | 路径 |
|------|------|
| Java 模板 (Entity/DTO/VO/Mapper/Service/Controller) | `examples/java-templates.md` |
| 命名/注解/API/Result 规范速查 | `references/conventions.md` |
| 服务管理脚本 | `references/service-scripts.md` |
| 项目总规范 | `CLAUDE.md` |
| 开发规范文档 | `docs/规范/Open-TMS开发规范文档.md` |
| AC/AT 核心架构 | `docs/architecture/business/AC交易与现金流分离架构设计.md` |
| DealMap 落地分析 | `docs/architecture/business/DealMap落地分析.md` |

---

## 版本历史

| 版本 | 日期 | 变更内容 |
|------|------|---------|
| v1.0 | 2025-05 | 初始版本 |
| v2.0 | 2026-07-05 | 精简: 模板→examples/java-templates.md, 规范→references/conventions.md; 新增关键模式章节 |

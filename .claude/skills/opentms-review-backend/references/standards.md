# Open-TMS 后端标准与对标参考

> 本文档收录 Open-TMS 后端审核所依据的标准映射表、业界对标资料、组件规范。

---

## 1. Open-TMS 后端技术栈(CLAUDE.md)

| 维度 | 技术 |
|------|------|
| 语言 | Java 17 |
| 框架 | SpringBoot 3.2.0 |
| Web | Apache CXF 4.0.3 (JAX-RS) |
| ORM | MyBatis Plus 3.5.5 |
| 数据库 | PostgreSQL 42.7.1 |
| 缓存 | Redis (Redisson 3.25.0) / Caffeine |
| 工具 | Lombok 1.18.30 |
| 构建 | Maven 多模块(18 个子模块) |

---

## 2. 强制审计字段(全表必备)

```sql
created_by  VARCHAR(50)  NOT NULL
created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
updated_by  VARCHAR(50)
updated_at  TIMESTAMP
version     INT          DEFAULT 0   -- 乐观锁(@Version)
deleted     CHAR(1)      DEFAULT '0' -- 软删除(@TableLogic)
```

---

## 3. 金额精度(CLAUDE.md 强制)

| 类型 | 精度 |
|------|------|
| 普通金额 | `DECIMAL(18,2)` |
| 汇率 | `DECIMAL(18,8)` |
| 利率 | `DECIMAL(10,4)` |
| **AC Deal / Cashflow** | **`DECIMAL(38,18)`** ← 资金交易强制 |

---

## 4. REST API 路径规范(CLAUDE.md)

```
GET    /api/v1/{resource}/page         # 分页查询
GET    /api/v1/{resource}/{id}         # 详情查询
POST   /api/v1/{resource}              # 新增
POST   /api/v1/{resource}/update       # 更新(2026-05-31 后统一)
POST   /api/v1/{resource}/delete/{id}  # 删除(2026-05-31 后统一)
POST   /api/v1/{resource}/{id}/submit  # 提交审批
POST   /api/v1/{resource}/{id}/approve # 审批通过
POST   /api/v1/{resource}/{id}/reject  # 审批驳回
POST   /api/v1/{resource}/{id}/execute # 执行(Deal 专属)
```

> ⚠️ **红线**: update/delete 一律 POST,不要用 PUT/DELETE(部分代理/网关不友好)。

---

## 5. 响应格式 (Result<T>)

```json
{
  "code": 200,
  "message": "success",
  "data": {...},
  "timestamp": 1704067200000
}
```

错误码: `200` 成功 / `400` 业务异常 / `401` 未授权 / `403` 无权限 / `404` 不存在 / `500` 系统异常

---

## 6. 事务与异常规范(CLAUDE.md)

```java
// 写操作必须加 rollbackFor
@Transactional(rollbackFor = Exception.class)

// 读操作加 readOnly
@Transactional(readOnly = true)
```

```java
// 业务异常统一抛 BusinessException
throw new BusinessException("DEAL_001", "交易不存在");

// 0 throw new RuntimeException 直抛
```

---

## 7. 全局枚举(GlobalConstants)

```java
public class GlobalConstants {
    // DealType: AC / AT / FX
    public static final String DEAL_TYPE_AC = "AC";
    public static final String DEAL_TYPE_AT = "AT";
    public static final String DEAL_TYPE_FX = "FX";

    // InstrumentType: AC/AT/FX/DEPOSIT/LOAN/IR/EQ/BOND/SWAP/OPTION/FORWARD/OTHER
    // Status: STATUS_ENABLED(1) / STATUS_DISABLED(0)
    // ActionType: CREATE/UPDATE/DELETE/SUBMIT/APPROVE/REJECT/EXECUTE
}
```

---

## 8. 幂等接口(CLAUDE.md)

- 请求头:`X-Idempotency-Key: <唯一标识>`
- 重复请求返回原结果,响应体含 `idempotent: true`
- 交易表必须含 `idempotency_key VARCHAR(64)`,配套幂等表 `tms_idempotency_t`

---

## 9. 业界对标要点

### 9.1 FIS Quantum

- **分层**: Domain / Application / Infrastructure / Interfaces
- **CQRS**: 读写模型分离
- **乐观锁**: 全表 version 字段,自动版本控制
- **审计**: append-only + 数字签名
- **事件溯源**: 关键业务事件(Event Sourcing)

### 9.2 Murex MX.3

- **模块化**: 每个领域独立模块
- **状态机**: 8-15 状态,Spring StateMachine
- **幂等**: 交易全链路幂等
- **工作流**: Approval Workflow + Rule Engine
- **跨服务**: REST + MQ 双链路

### 9.3 SAP TRM

- **TPM (Transaction Processing Manager)**: 统一交易框架
- **多账套**: 多 book 隔离
- **平行账**: IFRS / GAAP 平行核算
- **审计**: 不可篡改日志

### 9.4 Kyriba

- **云原生**: 微服务 + 容器化
- **多租户**: 集团多公司隔离
- **API 优先**: RESTful + OpenAPI

---

## 10. 后端审核项补强清单

### BE-006 三层分离
- Controller 仅路由 + 包装
- Service 业务编排
- Mapper 数据访问
- 0 越层调用

### BE-008 @Transactional 规范
- 写操作 `rollbackFor = Exception.class`
- 读操作 `readOnly = true`
- 事务粒度合理(避免大事务)

### BE-009 异常处理
- 统一 `BusinessException`
- 错误码 `GlobalConstants` 定义
- `GlobalExceptionHandler` 统一捕获

### BE-013 N+1 查询
- 避免循环中调 Mapper
- 用 JOIN 一次性查询
- 用 MyBatis Plus `selectBatchIds` 批量

### BE-014 大数据量
- 分页 `Page<T>`,max size 1000
- 流式 `MyBatis Plus Cursor`
- 导出分批写入

### BE-015 缓存
- Caffeine TTL 60s / maxSize 1000
- 防止穿透 / 击穿 / 雪崩
- `@CacheEvict` 在写操作清除

### BE-016 线程安全
- 0 `private static` 可变字段
- 用 `ThreadLocal` / `ConcurrentHashMap`
- 单例 Bean 无状态

### BE-017 软删除
- 全表 `@TableLogic`
- 软删除数据可查询
- 5 年保留(金融监管)

### BE-018 乐观锁
- 全表 `@Version`
- update 自动 `version = version + 1`
- 失败抛 `OptimisticLockingFailureException`

### BE-019 幂等
- `X-Idempotency-Key` 请求头
- `tms_idempotency_t` 表
- 重复请求返回原结果

### BE-020 跨模块依赖
- 走 Feign / API 调用
- 共享类放 `common` 模块
- Maven 依赖方向 DAG

### BE-021 跨服务通信
- 统一 OpenFeign
- 0 `RestTemplate` / `HttpURLConnection`
- 超时 / 重试 / 降级

### BE-022 配置外化
- `application.yml` 配置
- 动态配置 `sys_configs` 表
- `@ConfigurationProperties` 注入

### BE-023 日志规范
- 关键节点 `log.info`
- 异常 `log.error(msg, e)` 含堆栈
- 0 `System.out.println`

### BE-024 SQL 注入
- 全部 `#{}` 占位符
- 0 `${}` 字符串拼接
- 白名单 ORDER BY 等除外

### BE-025 敏感数据脱敏
- 密码 / 身份证 / 手机号 / 银行卡
- `@SensitiveInfo` 自动脱敏
- 密码 BCrypt 加密

### BE-026 状态机
- 状态枚举 + 校验器
- `canTransit(from, to)` 校验
- 0 散落 if-else

### BE-027 公共代码抽象
- `BaseService<DTO, Entity, VO>`
- `BaseEntity` / `BaseCodeEntity`
- 0 重复 CRUD 实现

---

## 11. Open-TMS 现有公共组件

| 类 | 路径 | 用途 |
|----|------|------|
| `BaseEntity` | `common/.../entity/BaseEntity.java` | 通用实体(含审计字段) |
| `BaseCodeEntity` | `common/.../entity/BaseCodeEntity.java` | 代码-名称-状态型实体 |
| `Result<T>` | `common/.../result/Result.java` | 统一响应 |
| `BusinessException` | `common/.../exception/BusinessException.java` | 业务异常 |
| `GlobalExceptionHandler` | `common/.../exception/GlobalExceptionHandler.java` | 全局异常处理 |
| `GlobalConstants` | `common/.../constant/GlobalConstants.java` | 全局枚举/常量 |
| `MybatisPlusConfig` | `common/.../config/MybatisPlusConfig.java` | MyBatis Plus 配置 |
| `MetaObjectHandler` | `common/.../handler/...` | 审计字段自动填充 |

---

## 12. Spring Boot 3.2.0 最佳实践

| 项 | 实践 |
|----|------|
| 依赖注入 | 构造器注入优于 `@Autowired` 字段 |
| 配置 | `@ConfigurationProperties` 优于 `@Value` |
| 异常 | `@RestControllerAdvice` 全局处理 |
| 校验 | `spring-boot-starter-validation` |
| 缓存 | `spring-boot-starter-cache` + Caffeine |
| 文档 | `springdoc-openapi-starter-webmvc-ui` |
| 测试 | JUnit 5 + Mockito + Spring Test |
| 安全 | `spring-boot-starter-security` + JWT |
| 监控 | Spring Actuator + Prometheus |
| 链路 | Micrometer Tracing + Zipkin |

---

## 13. 与 Open-TMS 既有模块参考

| 模块 | 参考路径 | 状态 |
|------|----------|------|
| basedata | `basedata/src/main/java/com/opentms/basedata/` | ✅ 14 Resource 合并完成 |
| dealing | `dealing/src/main/java/com/opentms/dealing/` | ✅ AC/AT 全流程 |
| common | `common/src/main/java/com/opentms/common/` | ✅ 公共基础 |

**审核时需对比以上模块**:
- 分层架构
- 命名规范
- 注解使用
- 异常处理
- 测试覆盖
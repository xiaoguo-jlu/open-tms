# Checklist 03 — 幂等/错误码审核清单

> 配合 `opentms-review-api` SKILL.md 使用。审核员按此清单逐项勾选。
> 聚焦幂等设计、错误码规范、异常处理、权限控制等。

---

## A. 幂等设计 (Idempotency)

### A1. 幂等键 (Open-TMS 强制)

- [ ] 所有 POST 写接口接收 `X-Idempotency-Key` 请求头
- [ ] 请求头值 UUID 或唯一字符串(≥ 16 字符)
- [ ] 服务端从 Header 读取(而非 Body)
- [ ] 缺失幂等键时返回 400 (业务要求) 或自动生成 UUID

### A2. 幂等表 (`tms_idempotency_t`)

```sql
CREATE TABLE tms_idempotency_t (
    id BIGSERIAL PRIMARY KEY,
    idempotency_key VARCHAR(64) NOT NULL UNIQUE,
    request_hash VARCHAR(64),
    response_body TEXT,
    status VARCHAR(20) DEFAULT 'Pending',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expired_at TIMESTAMP
);
```

- [ ] 幂等键 UNIQUE NOT NULL
- [ ] 存储原始请求 hash(防参数被篡改)
- [ ] 存储响应体(直接返回)
- [ ] 过期清理 (expired_at + 定时任务)

### A3. 幂等逻辑

```java
@PostMapping
public Result<DealVO> create(@RequestHeader(value = "X-Idempotency-Key", required = false) String key,
                              @RequestBody DealDTO dto) {
    if (key != null) {
        Result cached = idempotencyService.getCachedResponse(key);
        if (cached != null) {
            cached.setIdempotent(true);
            return cached;
        }
    }
    Result<DealVO> result = dealService.create(dto);
    if (key != null) {
        idempotencyService.saveResponse(key, result);
    }
    return result;
}
```

- [ ] 重复请求直接返回缓存结果
- [ ] 响应体含 `idempotent: true/false`
- [ ] 同 key 不同 request hash → 返回 409 Conflict
- [ ] 处理并发(分布式锁 / UNIQUE 约束)

### A4. 幂等接口

| 接口类型 | 是否幂等 |
|---------|---------|
| 新增 (POST /deals) | 必须 |
| 更新 (POST /deals/update) | 必须 |
| 删除 (POST /deals/delete/{id}) | 必须 |
| 业务动作 (POST /deals/{id}/submit) | 必须 |
| 查询 (GET) | 自然幂等,无需 X-Idempotency-Key |

### A5. 反例

```java
// 反例 1: 无幂等
@PostMapping("/deals")
public Result<DealVO> create(@RequestBody DealDTO dto) {  // 无 X-Idempotency-Key
    return dealService.create(dto);  // 重复提交会创建多条
}

// 反例 2: 幂等但未返回标识
if (key != null) {
    return cached;  // 但响应未标识 idempotent: true
}
```

---

## B. 错误码规范

### B1. 错误码枚举 (Open-TMS 强制)

| 错误码 | 含义 | HTTP Status | 场景 |
|--------|------|-------------|------|
| 200 | 成功 | 200 | 正常返回 |
| 400 | 业务异常 | 400 | BusinessException |
| 401 | 未授权 | 401 | AuthException |
| 403 | 无权限 | 403 | PermissionDeniedException |
| 404 | 资源不存在 | 404 | NotFoundException |
| 422 | 参数校验失败 | 422 | ValidationException |
| 500 | 系统异常 | 500 | RuntimeException |

### B2. 错误响应结构

```json
{
  "code": 400,
  "message": "交易类型不能为空",
  "data": null,
  "timestamp": 1704067200000,
  "traceId": "abc123"
}
```

- [ ] code 字段必填
- [ ] message 字段必填(中文友好)
- [ ] data 为 null(异常时)
- [ ] timestamp 必填
- [ ] traceId (可选, 用于日志追踪)

### B3. 异常处理 (`GlobalExceptionHandler`)

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusiness(BusinessException e) {
        return Result.error(400, e.getMessage());
    }
    
    @ExceptionHandler(NotFoundException.class)
    public Result<Void> handleNotFound(NotFoundException e) {
        return Result.error(404, e.getMessage());
    }
    
    @ExceptionHandler(ValidationException.class)
    public Result<Void> handleValidation(ValidationException e) {
        return Result.error(422, e.getMessage());
    }
    
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.error(500, "系统异常,请联系管理员");
    }
}
```

- [ ] 业务异常 → 400
- [ ] 未授权 → 401
- [ ] 无权限 → 403
- [ ] 资源不存在 → 404
- [ ] 参数校验失败 → 422
- [ ] 系统异常 → 500

### B4. 业务异常封装

```java
// 推荐: 业务异常带 code
throw new BusinessException(400, "交易类型不能为空");

// 不推荐: 字符串拼装
throw new RuntimeException("交易类型不能为空");
```

- [ ] 业务异常抛 `BusinessException`
- [ ] 异常含 code + message
- [ ] 不抛原始 RuntimeException

### B5. 错误码反例

```java
// 反例 1: 错误码混乱
return Result.error(0, "成功");  // 应为 200
return Result.error(500, "参数错误");  // 应为 400 或 422

// 反例 2: 异常未分类
throw new RuntimeException("找不到");  // 应为 NotFoundException(404)

// 反例 3: 异常吞掉
try {
    dealService.create(dto);
} catch (Exception e) {
    log.error(e.getMessage());  // 错, 应抛 BusinessException
}
```

---

## C. 权限控制

### C1. @PreAuthorize 注解 (Open-TMS 强制)

```java
@PreAuthorize("hasAuthority('deal:create')")
@PostMapping
public Result<DealVO> create(@RequestBody DealDTO dto) { ... }

@PreAuthorize("hasAuthority('deal:approve')")
@PostMapping("/{id}/approve")
public Result<Void> approve(@PathVariable Long id) { ... }
```

- [ ] 所有 Controller 方法加 `@PreAuthorize`
- [ ] 权限粒度细到按钮 (e.g. `deal:create` / `deal:approve`)
- [ ] 权限标识格式 `{resource}:{action}`

### C2. 权限粒度

| 操作 | 权限标识 |
|------|---------|
| 查询 | `{resource}:view` |
| 新增 | `{resource}:create` |
| 更新 | `{resource}:update` |
| 删除 | `{resource}:delete` |
| 提交 | `{resource}:submit` |
| 审批 | `{resource}:approve` |
| 驳回 | `{resource}:reject` |
| 执行 | `{resource}:execute` |
| 导出 | `{resource}:export` |

- [ ] 权限标识规范
- [ ] 无裸 Controller (无任何权限)

### C3. 权限反例

```java
// 反例 1: 无权限注解
@PostMapping("/deals")
public Result<DealVO> create(@RequestBody DealDTO dto) {  // 任何人都能调用
    return dealService.create(dto);
}

// 反例 2: 权限粒度粗
@PreAuthorize("hasRole('admin')")  // 太粗, 应细分到按钮

// 反例 3: 权限格式不规范
@PreAuthorize("hasAuthority('createDeal')")  // 应为 deal:create
```

---

## D. 接口文档

### D1. 文档路径
- [ ] Markdown 文档在 `docs/api/{module}/{resource}.md`
- [ ] 或 OpenAPI YAML 在 `docs/api/{module}/openapi.yaml`

### D2. 文档必备内容

| 内容 | 说明 |
|------|------|
| 接口名称 | 中文 + 英文 |
| URL | `/api/v1/...` |
| Method | GET / POST |
| 权限 | 所需权限标识 |
| 请求头 | X-Idempotency-Key 等 |
| 入参 | DTO 结构 + 字段说明 + 示例 |
| 出参 | VO 结构 + 字段说明 + 示例 |
| 错误码 | 200 / 400 / 401 / 403 / 404 / 422 / 500 |
| 业务规则 | 业务约束说明 |
| 示例 | curl / Java / Python 示例 |

### D3. 文档与代码同步
- [ ] 文档 URL 与 Controller 一致
- [ ] 文档入参与 DTO 一致
- [ ] 文档出参与 VO 一致
- [ ] 文档错误码与 GlobalExceptionHandler 一致

---

## E. 反例 (必须退回)

### E1. 幂等反例

```java
// 反例 1: 无幂等保护 (会导致重复创建)
@PostMapping("/deals")
public Result<DealVO> create(@RequestBody DealDTO dto) {
    return dealService.create(dto);
}

// 反例 2: 幂等但 key 在 Body
public class DealDTO {
    private String idempotencyKey;  // 应放 Header
}
```

### E2. 错误码反例

```java
// 反例 1: 错误码不规范
return Result.error(1, "成功");  // 应为 200
return Result.error(-1, "失败");  // 应为 400/500

// 反例 2: 异常未分类
throw new RuntimeException("找不到");  // 应为 NotFoundException
```

### E3. 权限反例

```java
// 反例 1: 无权限
@PostMapping("/deals")
public Result<DealVO> create(@RequestBody DealDTO dto) { ... }

// 反例 2: 权限粒度粗
@PreAuthorize("hasRole('admin')")
```

---

## 审核结论

通过项数 / 总项数 = ____%

| 等级 | 通过率 |
|------|--------|
| A | ≥95% |
| B | ≥85% |
| C | ≥70% |
| D | <70% |

**额外扣分项**:
- API-004 (无幂等) → 直接降至 D
- API-005 (错误码混乱) → 直接降至 D
- API-009 (无权限注解) → 直接降至 D
- API-016 (无接口文档) → 降至 C
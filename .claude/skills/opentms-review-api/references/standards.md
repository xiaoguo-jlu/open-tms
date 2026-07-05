# Open-TMS API 审核标准与对标参考

> 本文档收录 Open-TMS API 审核所依据的标准、业界对标资料、RESTful 最佳实践。

---

## 1. Open-TMS REST API 规范 (CLAUDE.md 提炼)

### 1.1 URL 规范

```
/api/v1/{resource}/{action}
  resource: 资源名(复数, 小写, snake_case)
  action:   动作(page / list / {id} / update / delete/{id} / submit / approve / ...)
```

**完整示例**:

| 操作 | URL | Method |
|------|-----|--------|
| 分页查询 | `/api/v1/deals/page` | GET |
| 详情查询 | `/api/v1/deals/{id}` | GET |
| 新增 | `/api/v1/deals` | POST |
| 更新 | `/api/v1/deals/update` | POST |
| 删除 | `/api/v1/deals/delete/{id}` | POST |
| 提交 | `/api/v1/deals/{id}/submit` | POST |
| 审批通过 | `/api/v1/deals/{id}/approve` | POST |
| 审批驳回 | `/api/v1/deals/{id}/reject` | POST |
| 执行 | `/api/v1/deals/{id}/execute` | POST |

### 1.2 HTTP Method 红线

| 方法 | 是否允许 | 说明 |
|------|---------|------|
| GET | ✓ | 仅查询 |
| POST | ✓ | 新增/更新/删除/业务动作 |
| **PUT** | ❌ | Open-TMS 用 POST /update 替代 |
| **DELETE** | ❌ | Open-TMS 用 POST /delete/{id} 替代 |
| **PATCH** | ❌ | 不使用 |

> ⚠️ **红线原因**: 部分代理/网关不支持 PUT/DELETE,统一 POST 兼容性更好。

### 1.3 响应格式 (Result<T>)

```json
{
  "code": 200,
  "message": "success",
  "data": { ... },
  "timestamp": 1704067200000
}
```

**分页响应**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [ ... ],
    "total": 100,
    "size": 20,
    "current": 1,
    "pages": 5
  },
  "timestamp": 1704067200000
}
```

### 1.4 错误码

| 错误码 | HTTP Status | 含义 | 场景 |
|--------|-------------|------|------|
| 200 | 200 | 成功 | 正常返回 |
| 400 | 400 | 业务异常 | BusinessException |
| 401 | 401 | 未授权 | Token 缺失/失效 |
| 403 | 403 | 无权限 | 权限不足 |
| 404 | 404 | 资源不存在 | NotFoundException |
| 422 | 422 | 参数校验失败 | ValidationException |
| 500 | 500 | 系统异常 | RuntimeException |

### 1.5 幂等设计

- 请求头: `X-Idempotency-Key: <唯一标识>`
- 重复请求返回原结果,响应体含 `idempotent: true`
- 交易表必含 `idempotency_key VARCHAR(64)`
- 配套幂等表 `tms_idempotency_t`

### 1.6 DTO/VO 强制分层

```
入参 dto/   (Controller 接收)
出参 vo/    (Service 返回)
持久化 entity/ (Mapper 操作)
```

转换用 `BeanUtil.copyProperties`。

---

## 2. 业界对标要点

### 2.1 FIS Quantum

| 规范 | 描述 |
|------|------|
| REST 化 | 100% REST API,无 SOAP |
| 版本控制 | URL 内嵌 (v1 / v2) |
| 幂等 | 所有写接口支持 |
| 权限 | OAuth 2.0 + RBAC |
| 错误码 | 标准 HTTP Status |
| 分页 | cursor-based (大数据量) |
| 异步 | 202 Accepted + 任务 ID |

### 2.2 Murex MX.3

| 规范 | 描述 |
|------|------|
| API 风格 | REST + GraphQL (混合) |
| 幂等 | Idempotency-Key 强制 |
| 字段命名 | camelCase |
| 批量操作 | 专门的 batch endpoint |
| 异步操作 | 任务 ID + 轮询 |
| 错误码 | 自定义 + HTTP Status |

### 2.3 SAP TRM

| 规范 | 描述 |
|------|------|
| API 风格 | OData (SAP 标准) |
| 字段命名 | PascalCase |
| 幂等 | 通过 ETags 实现 |
| 分页 | server-driven paging |
| 权限 | OAuth + XSUAA |

### 2.4 Kyriba

| 规范 | 描述 |
|------|------|
| API 风格 | REST |
| 幂等 | Idempotency-Key |
| 字段命名 | camelCase |
| 异步 | Webhook + 任务 ID |

### 2.5 Bloomberg AIM

| 规范 | 描述 |
|------|------|
| 实时性 | WebSocket + Server-Sent Events |
| API 风格 | REST + WS |
| 字段命名 | camelCase |
| 数据推送 | 流式 |

---

## 3. Open-TMS 现有 API 参考

### 3.1 M1 已贯通 API (基于 14 Resource + AC/AT)

| 模块 | Resource | 端口 | 主要接口 |
|------|----------|------|---------|
| basedata | Country | 8081 | GET/POST /api/v1/countries |
| basedata | Currency | 8081 | GET/POST /api/v1/currencies |
| basedata | Bank | 8081 | GET/POST /api/v1/banks |
| basedata | BankAccount | 8081 | GET/POST /api/v1/bank-accounts |
| basedata | Counterparty | 8081 | GET/POST /api/v1/counterparties |
| basedata | Trader | 8081 | GET/POST /api/v1/traders |
| basedata | BusinessUnit | 8081 | GET/POST /api/v1/business-units |
| basedata | CurrencyPair | 8081 | GET/POST /api/v1/currency-pairs |
| basedata | ExchangeRate | 8081 | GET/POST /api/v1/exchange-rates |
| basedata | ManagementEntity | 8081 | GET/POST /api/v1/management-entities |
| basedata | Instrument | 8081 | GET/POST /api/v1/instruments |
| basedata | Calendar | 8081 | GET/POST /api/v1/calendars |
| basedata | Holiday | 8081 | GET/POST /api/v1/holidays |
| basedata | SettlementAccount | 8081 | GET/POST /api/v1/settlement-accounts |
| dealing | Deal | 8082 | GET/POST /api/v1/deals + submit/approve/execute |
| dealing | DealAction | 8082 | GET/POST /api/v1/deal-actions |
| dealing | Cashflow | 8082 | GET /api/v1/cashflows |
| dealing | ApprovalTask | 8082 | GET/POST /api/v1/approval-tasks |

### 3.2 标准 Controller 模板

```java
@RestController
@RequestMapping("/api/v1/deals")
@PreAuthorize("hasAuthority('deal:view')")
public class DealController {

    @Autowired
    private DealService dealService;

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('deal:view')")
    public Result<PageResult<DealVO>> page(DealQuery query) {
        return Result.success(dealService.page(query));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('deal:view')")
    public Result<DealVO> get(@PathVariable Long id) {
        return Result.success(dealService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('deal:create')")
    public Result<DealVO> create(@RequestHeader(value = "X-Idempotency-Key", required = false) String key,
                                  @RequestBody @Valid DealDTO dto) {
        return dealService.create(key, dto);
    }

    @PostMapping("/update")
    @PreAuthorize("hasAuthority('deal:update')")
    public Result<DealVO> update(@RequestHeader(value = "X-Idempotency-Key", required = false) String key,
                                  @RequestBody @Valid DealDTO dto) {
        return dealService.update(key, dto);
    }

    @PostMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('deal:delete')")
    public Result<Void> delete(@RequestHeader(value = "X-Idempotency-Key", required = false) String key,
                                @PathVariable Long id) {
        return dealService.delete(key, id);
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("hasAuthority('deal:submit')")
    public Result<Void> submit(@RequestHeader(value = "X-Idempotency-Key", required = false) String key,
                                @PathVariable Long id) {
        return dealService.submit(key, id);
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('deal:approve')")
    public Result<Void> approve(@RequestHeader(value = "X-Idempotency-Key", required = false) String key,
                                 @PathVariable Long id, @RequestBody ApprovalDTO dto) {
        return dealService.approve(key, id, dto);
    }
}
```

### 3.3 标准 DTO 模板

```java
@Data
public class DealDTO {

    @NotNull(message = "交易类型不能为空")
    private String dealType;

    @NotBlank(message = "业务单元不能为空")
    private String businessUnit;

    @NotNull(message = "对手方不能为空")
    private Long counterpartyId;

    @NotNull(message = "金融工具不能为空")
    private Long instrumentId;

    @NotNull(message = "交易员不能为空")
    private Long traderId;

    @NotBlank(message = "交易方向不能为空")
    private String direction;

    @NotNull(message = "金额不能为空")
    @Positive(message = "金额必须大于0")
    @Digits(integer = 18, fraction = 2, message = "金额精度超出限制")
    private BigDecimal amount;

    @NotBlank(message = "币种不能为空")
    private String currency;

    @NotNull(message = "交易日不能为空")
    private LocalDate tradeDate;

    @NotNull(message = "起息日不能为空")
    private LocalDate valueDate;

    @Size(max = 500, message = "备注长度不能超过500")
    private String remark;
}
```

### 3.4 标准 VO 模板

```java
@Data
public class DealVO {

    private Long id;
    private String dealNo;
    private String dealType;
    private String businessUnit;
    private Long counterpartyId;
    private String counterpartyName;
    private Long instrumentId;
    private String instrumentName;
    private Long traderId;
    private String traderName;
    private String direction;
    private BigDecimal amount;
    private String currency;
    private LocalDate tradeDate;
    private LocalDate valueDate;
    private String status;
    private String remark;

    // 审计字段
    private String createdBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createdAt;

    private String updatedBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updatedAt;

    private Integer version;

    @JsonIgnore  // 默认隐藏软删除字段
    private String deleted;
}
```

### 3.5 标准 PageResult 模板

```java
@Data
public class PageResult<T> {
    private List<T> records;
    private Long total;
    private Long size;
    private Long current;
    private Long pages;

    public static <T> PageResult<T> of(IPage<T> page) {
        PageResult<T> result = new PageResult<>();
        result.setRecords(page.getRecords());
        result.setTotal(page.getTotal());
        result.setSize(page.getSize());
        result.setCurrent(page.getCurrent());
        result.setPages(page.getPages());
        return result;
    }
}
```

### 3.6 标准 Result 模板

```java
@Data
public class Result<T> {
    private Integer code;
    private String message;
    private T data;
    private Long timestamp;
    private Boolean idempotent;  // 幂等标识

    public static <T> Result<T> success(T data) {
        Result<T> r = new Result<>();
        r.setCode(200);
        r.setMessage("success");
        r.setData(data);
        r.setTimestamp(System.currentTimeMillis());
        return r;
    }

    public static <T> Result<T> error(Integer code, String message) {
        Result<T> r = new Result<>();
        r.setCode(code);
        r.setMessage(message);
        r.setTimestamp(System.currentTimeMillis());
        return r;
    }
}
```

---

## 4. 字段命名映射速查

### 4.1 三层映射

| 层级 | 命名风格 | 示例 |
|------|---------|------|
| JSON (API) | camelCase | `dealNo`, `buyAmount` |
| Java (Entity/VO) | camelCase | `dealNo`, `buyAmount` |
| DB (PostgreSQL) | snake_case | `deal_no`, `buy_amount` |

### 4.2 MyBatis Plus 自动映射

```java
// Entity 自动映射
@TableName("tms_deal_t")
public class Deal {
    private Long id;            // ← id
    private String dealNo;      // ← deal_no
    private BigDecimal buyAmount;  // ← buy_amount
    private String dealType;    // ← deal_type
}
```

### 4.3 特殊字段显式映射

```java
@TableField("buy_amount")
private BigDecimal buyAmount;

@TableField(value = "created_at", fill = FieldFill.INSERT)
private LocalDateTime createdAt;

@TableLogic
@TableField("deleted")
private String deleted;
```

---

## 5. 错误码定义

### 5.1 推荐位置: `com.opentms.common.constant.ErrorCode`

```java
public interface ErrorCode {
    Integer SUCCESS = 200;
    Integer BUSINESS_ERROR = 400;
    Integer UNAUTHORIZED = 401;
    Integer FORBIDDEN = 403;
    Integer NOT_FOUND = 404;
    Integer VALIDATION_ERROR = 422;
    Integer SYSTEM_ERROR = 500;
}
```

### 5.2 业务异常: `BusinessException`

```java
public class BusinessException extends RuntimeException {
    private final Integer code;

    public BusinessException(String message) {
        super(message);
        this.code = 400;
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }
}
```

### 5.3 全局异常处理: `GlobalExceptionHandler`

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusiness(BusinessException e) {
        log.warn("业务异常: {}", e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(NotFoundException.class)
    public Result<Void> handleNotFound(NotFoundException e) {
        return Result.error(404, e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return Result.error(422, msg);
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.error(500, "系统异常,请联系管理员");
    }
}
```

---

## 6. 权限标识规范

### 6.1 格式

```
{resource}:{action}
```

### 6.2 资源-动作 列表

| Resource | Action | 权限标识 |
|----------|--------|---------|
| deal | view / create / update / delete / submit / approve / reject / execute / export | `deal:view` / `deal:create` / ... |
| counterparty | view / create / update / delete / export | `counterparty:view` / ... |
| cashflow | view / export | `cashflow:view` / `cashflow:export` |
| approval | view / approve / reject | `approval:view` / `approval:approve` / ... |

### 6.3 注解使用

```java
// Controller 类级别
@PreAuthorize("hasAuthority('deal:view')")
@RequestMapping("/api/v1/deals")
public class DealController { ... }

// 方法级别
@PreAuthorize("hasAuthority('deal:approve')")
@PostMapping("/{id}/approve")
public Result<Void> approve(...) { ... }

// 多权限 (OR)
@PreAuthorize("hasAnyAuthority('deal:approve', 'deal:reject')")

// 多权限 (AND)
@PreAuthorize("hasAuthority('deal:approve') and hasAuthority('deal:high-value')")
```

---

## 7. 接口文档模板

### 7.1 Markdown 模板

```markdown
# {资源名} API

## 1. {接口名称}

### 基本信息
- URL: `/api/v1/{resource}/{action}`
- Method: GET / POST
- 权限: `{resource}:{action}`
- 幂等: 是 / 否

### 请求头
- `X-Idempotency-Key`: 唯一标识(POST 写接口必填)

### 请求参数

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 主键 |

### 响应

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "dealNo": "AC20260705-0001",
    ...
  },
  "timestamp": 1704067200000
}
```

### 错误码

| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 业务异常 |
| 404 | 资源不存在 |
| 422 | 参数校验失败 |

### 示例

```bash
curl -X POST http://localhost:8082/api/v1/deals \
  -H "Content-Type: application/json" \
  -H "X-Idempotency-Key: 550e8400-e29b-41d4-a716-446655440000" \
  -d '{ "dealType": "AC", ... }'
```
```

---

## 8. Spring Boot 3.2 注解速查

### 8.1 Web 注解

| 注解 | 用途 |
|------|------|
| `@RestController` | REST Controller (替代 @Controller + @ResponseBody) |
| `@RequestMapping` | 路径前缀 |
| `@GetMapping` | GET |
| `@PostMapping` | POST |
| `@PutMapping` | PUT (Open-TMS 不使用) |
| `@DeleteMapping` | DELETE (Open-TMS 不使用) |
| `@PathVariable` | 路径参数 |
| `@RequestParam` | 查询参数 |
| `@RequestBody` | 请求体 |
| `@RequestHeader` | 请求头 |

### 8.2 校验注解 (JSR-303)

| 注解 | 用途 |
|------|------|
| `@NotNull` | 非 null |
| `@NotBlank` | 非 null + 非空字符串 |
| `@NotEmpty` | 非 null + 非空集合 |
| `@Size(min, max)` | 长度/大小 |
| `@Min` / `@Max` | 数值范围 |
| `@Positive` / `@Negative` | 正数/负数 |
| `@Digits(integer, fraction)` | 数值精度 |
| `@Email` | 邮箱格式 |
| `@Pattern(regexp)` | 正则表达式 |
| `@Past` / `@Future` | 过去/未来日期 |
| `@PastOrPresent` / `@FutureOrPresent` | 过去或现在/未来或现在 |
| `@Valid` | 触发嵌套校验 |

### 8.3 权限注解

| 注解 | 用途 |
|------|------|
| `@PreAuthorize` | 方法执行前校验 |
| `@PostAuthorize` | 方法执行后校验 |
| `@Secured` | 角色校验 |

---

## 9. 性能与运维

### 9.1 性能

- 列表接口必须分页(默认 size=10,上限 100)
- 异步操作用 202 Accepted + 任务 ID
- 大数据导出用异步 + 下载 URL
- N+1 查询用 `@Many` / `@One` 预加载

### 9.2 安全

- 所有写接口幂等保护
- 所有 Controller 权限注解
- 敏感数据 `@JsonIgnore` / `@SensitiveInfo`
- 输入校验 JSR-303
- SQL 注入防护:MyBatis Plus 参数化

### 9.3 监控

- 全局 traceId (MDC)
- 接口响应时间日志(>1s 报警)
- 异常日志含堆栈
- 接口调用计数(Prometheus)
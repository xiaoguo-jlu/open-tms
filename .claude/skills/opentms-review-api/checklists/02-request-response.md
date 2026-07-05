# Checklist 02 — 请求/响应审核清单

> 配合 `opentms-review-api` SKILL.md 使用。审核员按此清单逐项勾选。
> 聚焦入参 DTO、出参 VO、字段命名、分页结构、响应包装等。

---

## A. 请求 (Request) 规范

### A1. DTO 强制分层 (Open-TMS CLAUDE.md 强制)
- [ ] 入参对象放 `dto/` 包
- [ ] 出参对象放 `vo/` 包
- [ ] 持久化对象放 `entity/` 包
- [ ] Controller 接收 DTO,不接收 Entity
- [ ] Service 返回 VO,不返回 Entity

### A2. DTO 字段命名
- [ ] DTO 字段 camelCase (e.g. `dealNo`, `buyAmount`)
- [ ] 无 DB 字段名 (snake_case) 暴露
- [ ] 无前缀 (无 `tDeal`)
- [ ] 命名与业务一致 (无 `param1` / `fieldA`)

### A3. DTO 字段类型

| 字段 | 类型 | 说明 |
|------|------|------|
| 主键 | `Long id` | 路径参数 |
| 业务编号 | `String code` / `String no` | |
| 金额 | `BigDecimal` (e.g. `buyAmount`) | 高精度 |
| 汇率 | `BigDecimal` | |
| 日期 | `LocalDate` (e.g. `tradeDate`) | |
| 时间 | `LocalDateTime` (e.g. `createdAt`) | |
| 状态/类型 | `String` | 使用 GlobalConstants |
| 外键 | `Long` (e.g. `counterpartyId`) | |

- [ ] 数值类型用包装类 (Long / Integer 而非 long / int)
- [ ] 金额用 BigDecimal
- [ ] 日期用 java.time (LocalDate / LocalDateTime)

### A4. JSR-303 校验注解

#### A4.1 必填校验

| 字段类型 | 注解 |
|---------|------|
| 字符串非空 | `@NotBlank` |
| 字符串非 null | `@NotNull` |
| 集合非空 | `@NotEmpty` |
| 数值非 null | `@NotNull` |
| 日期非空 | `@NotNull` |

- [ ] 业务必填字段全部有校验
- [ ] 校验 message 友好 (`@NotBlank(message = "交易类型不能为空")`)

#### A4.2 格式校验

| 校验 | 注解 |
|------|------|
| 长度 | `@Size(min, max)` |
| 范围 | `@Min` / `@Max` |
| 邮箱 | `@Email` |
| 手机 | `@Pattern(regexp="^1[3-9]\\d{9}$")` |
| 日期 | `@Past` / `@Future` / `@PastOrPresent` |
| 数值精度 | `@Digits(integer=18, fraction=2)` |

- [ ] 字符串有 `@Size` 限制
- [ ] 数值有 `@Min` / `@Max` / `@Digits`
- [ ] 日期有 `@Past` / `@Future`

#### A4.3 校验触发
- [ ] Controller 方法参数加 `@Valid`
- [ ] 嵌套 DTO 加 `@Valid`
- [ ] 校验失败返回 422 + 错误详情

### A5. DTO 反例

```java
// 反例 1: 接收 Entity
@PostMapping("/deals")
public Result<DealVO> create(@RequestBody Deal deal) {  // 错, 应接收 DTO
}

// 反例 2: 缺校验
public class DealDTO {
    private String dealNo;  // 缺 @NotBlank
    private BigDecimal amount;  // 缺 @NotNull + @Positive
}

// 反例 3: 数值用基本类型
private long counterpartyId;  // 应为 Long (包装类)

// 反例 4: 命名混用
private String deal_no;  // 应为 dealNo (camelCase)
```

---

## B. 响应 (Response) 规范

### B1. Result<T> 统一包装 (Open-TMS 强制)

```json
{
  "code": 200,
  "message": "success",
  "data": { ... },
  "timestamp": 1704067200000
}
```

- [ ] 所有接口返回 `Result<T>`
- [ ] 无裸对象返回 (无直接返回 DealVO)
- [ ] code / message / data / timestamp 字段齐全

### B2. 单条数据响应

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "dealNo": "AC20260705-0001",
    "dealType": "AC",
    ...
  }
}
```

- [ ] 详情接口 `data` 为对象
- [ ] 新增/更新返回新对象

### B3. 分页列表响应

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
  }
}
```

- [ ] `data` 含 `records` / `total` / `size` / `current` / `pages`
- [ ] 无裸 `Page<T>` 返回 (MyBatis Plus 原生)
- [ ] 无 `data: [ ... ]` (无分页信息)

### B4. 列表响应 (简单)

```json
{
  "code": 200,
  "message": "success",
  "data": [ { ... }, { ... } ]
}
```

- [ ] 仅用于下拉/选项等小数据量场景
- [ ] 数据量 > 100 必须用分页

### B5. VO 字段命名

| 类型 | 命名 | 示例 |
|------|------|------|
| 业务字段 | camelCase | `dealNo`, `buyAmount` |
| 主键 | `id` | |
| 业务编码 | `code` / `no` | |
| 金额 | BigDecimal | `buyAmount`, `sellAmount` |
| 日期 | LocalDate | `tradeDate`, `valueDate` |
| 时间 | LocalDateTime | `createdAt`, `updatedAt` |
| 状态 | String | `status` |
| 外键 | Long | `counterpartyId` |

- [ ] 100% camelCase
- [ ] 数值用包装类
- [ ] 金额用 BigDecimal
- [ ] 日期用 java.time

### B6. VO 审计字段 (强制)

- [ ] `id` (主键)
- [ ] `createdBy` / `createdAt`
- [ ] `updatedBy` / `updatedAt`
- [ ] `version` (乐观锁)
- [ ] `deleted` (可选,默认隐藏)

### B7. 时间字段格式

```java
@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
private LocalDateTime createdAt;
```

- [ ] `@JsonFormat` 指定 pattern + timezone
- [ ] 时间格式统一
- [ ] 无返回 timestamp 数字 (除非前端要求)

### B8. VO 反例

```java
// 反例 1: 命名混用
public class DealVO {
    private String deal_no;  // 错, 应为 dealNo
    private Long CounterpartyId;  // 错, 大小写
}

// 反例 2: 数值用基本类型
private long amount;  // 应为 BigDecimal

// 反例 3: 日期用 Date
private Date createdAt;  // 应为 LocalDateTime

// 反例 4: 缺审计字段
public class DealVO {
    private Long id;
    private String dealNo;
    // 缺 createdBy/At, updatedBy/At
}
```

---

## C. 分页 (Pagination) 规范

### C1. PageResult 统一结构 (Open-TMS 强制)

```java
public class PageResult<T> {
    private List<T> records;
    private Long total;
    private Long size;
    private Long current;
    private Long pages;
}
```

- [ ] 所有分页接口返回 PageResult<T>
- [ ] 无裸 Page<T> (MyBatis Plus 原生)
- [ ] 无自定义分页类

### C2. 分页参数

| 参数 | 默认 | 上限 |
|------|------|------|
| `current` | 1 | - |
| `size` | 10 | 100 |

- [ ] 默认 size=10
- [ ] size 上限 100
- [ ] 超限返回 400 / 默认最大值

### C3. 排序参数

```json
{
  "current": 1,
  "size": 10,
  "sort": "createdAt,desc"
}
```

- [ ] sort 字段白名单 (防 SQL 注入)
- [ ] 默认 sort `createdAt,desc`

---

## D. 字段命名规范 (跨层映射)

### D1. JSON ↔ Java ↔ DB 三方映射

| 层级 | 命名 | 示例 |
|------|------|------|
| JSON (API) | camelCase | `dealNo`, `buyAmount` |
| Java (Entity/VO) | camelCase | `dealNo`, `buyAmount` |
| DB (PostgreSQL) | snake_case | `deal_no`, `buy_amount` |

### D2. MyBatis Plus 自动映射
- [ ] 默认 `camelCase` ↔ `snake_case` 自动转换
- [ ] 特殊字段用 `@TableField("xxx_yyy")`
- [ ] 字段名差异需显式声明

### D3. 反例

```java
// 反例 1: DB 字段暴露到 JSON
public class DealVO {
    @JsonProperty("deal_no")  // 错, 应 camelCase
    private String dealNo;
}

// 反例 2: 命名不一致
public class DealVO {
    private String dealNumber;  // 应统一为 dealNo
}

// 反例 3: 无映射
public class DealEntity {
    @TableField("deal_no")
    private String dealNo;
    // 缺映射 → MyBatis Plus 找不到字段
}
```

---

## E. 反例 (必须退回)

### E1. 响应包装反例
```java
// 反例 1: 裸返回
@GetMapping("/{id}")
public DealVO get(@PathVariable Long id) {  // 错, 应返回 Result<DealVO>
    return dealService.get(id);
}

// 反例 2: 分页结构不规范
public class PageResult {
    private List data;  // 缺 records/total/size/current/pages
}
```

### E2. 字段命名反例
```java
// 反例 1: 命名混用
public class DealVO {
    private String dealNo;
    private String DealType;  // 错, 大写
    private String deal_type;  // 错, snake_case
}
```

### E3. 校验缺失反例
```java
// 反例 1: 无 @Valid
@PostMapping("/deals")
public Result<DealVO> create(@RequestBody DealDTO dto) {  // 缺 @Valid
    return Result.success(dealService.create(dto));
}

// 反例 2: DTO 字段无校验
public class DealDTO {
    private String dealNo;  // 缺 @NotBlank
    private BigDecimal amount;  // 缺 @NotNull + @Positive
}
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
- API-002/003/008/011 任何未通过 → 直接降至 D
- DTO/VO 混用 (无分层) → 降至 D
- 缺 Result 包装 → 降至 D
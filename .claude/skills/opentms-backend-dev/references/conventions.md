# Backend Conventions Reference

> 源文件: SKILL.md 附录A/B/C 及 REST API 规范部分。这些内容与 CLAUDE.md 有部分重叠，此处作为后端开发快速参考。
> CLAUDE.md 始终为权威来源，二者有冲突以 CLAUDE.md 为准。

## 命名规范

### 类命名

```
{Entity}Controller.java    # JAX-RS Resource (严格用 @Path/@GET/@POST,不用 Spring MVC)
{Entity}Service.java      # Service 接口
{Entity}ServiceImpl.java  # Service 实现
{Entity}Mapper.java       # MyBatis Plus Mapper
{Entity}DTO.java         # 入参 DTO
{Entity}VO.java          # 出参 VO
{Entity}Entity.java      # 持久化实体 (如不与其他冲突可简写 {Entity})
```

### 方法命名

```
# 查询
getById()           # 按ID查询
listAll()           # 查询所有
queryPage()         # 分页查询

# 写操作
save()              # 新增
update()            # 更新
delete()           # 删除
removeById()       # 按ID删除

# 业务流程
submit()           # 提交审批
approve()          # 审批通过
reject()           # 审批拒绝
execute()          # 执行(Deal 专属)
```

### 字段与表名

- Java 驼峰 `countryNo` → PostgreSQL 蛇形 `country_no` (MyBatis Plus 自动映射)
- 自定义映射用 `@TableField("column_name")`
- 表名: `tms_{module}_{type}` (类型: `_t` 主表 / `_d` 字典 / `_log` 日志 / `_rel` 关联 / `_his` 历史)
- 业务编码: `xxx_code VARCHAR(50) NOT NULL UNIQUE`
- 业务流水号: `xxx_no VARCHAR(50) NOT NULL UNIQUE` (如 `AC20260629-0001`)

## 注解速查

```java
// Entity
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tms_xxx_t")

// DTO - Jakarta Validation
@Data
public class XxxDTO {
    @NotBlank(message = "编码不能为空")
    @Size(max = 50, message = "编码最长50字符")
    private String code;

    @NotNull(message = "金额不能为空")
    @DecimalMin(value = "0.01", message = "金额必须大于0")
    private BigDecimal amount;
}

// JAX-RS Controller
@Path("/api/v1/xxx")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)

// Service
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)       // 写操作
@Transactional(readOnly = true)                     // 读操作
```

## REST API 规范

> 本项目使用 Apache CXF JAX-RS,路由通过 @Path 定义。以下模式是项目实际实现规范。

| 操作 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 分页查询 | GET | `/api/v1/{entities}/page` | QueryParam 传参 |
| 详情查询 | GET | `/api/v1/{entities}/{id}` | PathParam |
| 新增 | POST | `/api/v1/{entities}` | Body JSON |
| 更新 | POST | `/api/v1/{entities}/update` | Body JSON,含 id |
| 删除 | POST | `/api/v1/{entities}/delete/{id}` | PathParam |
| 提交 | POST | `/api/v1/{entities}/{id}/submit` | |
| 审批 | POST | `/api/v1/{entities}/{id}/approve` | |
| 驳回 | POST | `/api/v1/{entities}/{id}/reject` | |
| 执行 | POST | `/api/v1/{entities}/{id}/execute` | Deal 专属 |

**红线**:
- update/delete 一律 POST,不用 PUT/DELETE
- 所有接收 JSON 的方法必须加 `@Consumes(MediaType.APPLICATION_JSON)`
- 编码字段用 POST `/update` 路径,不用 PUT

## Result 响应格式

```java
// 成功
Result.success(data)
Result.success()

// 失败
Result.error(message)       // 通用错误
Result.badRequest(message)  // 参数错误
Result.notFound(message)    // 资源不存在
```

响应结构:
```json
{ "code": 200, "message": "success", "data": {...}, "timestamp": 1704067200000 }
```

分页响应:
```json
{ "code": 200, "data": { "records": [...], "total": 100, "size": 20, "current": 1 } }
```

## LambdaQueryWrapper 类型擦除警告

当使用抽象 BaseService 时，LambdaQueryWrapper 中的泛型 T 在运行时会擦除为 BaseEntity，
导致 MyBatis Plus 找不到正确的 lambda 缓存。**每个 ServiceImpl 必须直接 extend ServiceImpl<M, T> 并使用具体实体类的 lambda 引用**。

```java
// 正确: 直接使用具体实体类
wrapper.like(Country::getCode, keyword);    // Country, 不是泛型 T
wrapper.orderByDesc(Country::getCreatedAt);
```

# Checklist 01 — URL 设计审核清单

> 配合 `opentms-review-api` SKILL.md 使用。审核员按此清单逐项勾选。
> 聚焦 URL 路径、HTTP Method、资源命名等 URL 设计。

---

## A. URL 基础规范

### A1. URL 前缀
- [ ] 所有 URL 以 `/api/v1/` 开头
- [ ] 无版本缺失 (无 /api/ 直接开头)
- [ ] 无版本混乱 (v1 与 v2 混用)

### A2. URL 大小写
- [ ] URL 全部小写
- [ ] 无驼峰 (无 /dealList 这种)
- [ ] 无 kebab-case (无 /deal-list, 应该用 /deal-list 或 /deals)

### A3. URL 资源命名
- [ ] 资源名复数 (e.g. `/deals`, `/counters`, `/instruments`)
- [ ] 无单数 (无 /deal)
- [ ] 无缩写歧义 (无 /cp, 应该是 /counterparties)
- [ ] 无动词在 URL 中 (无 /createDeal, 应该 POST /deals)

---

## B. RESTful 资源结构

### B1. 资源集合操作

| 操作 | Method | URL |
|------|--------|-----|
| 分页查询 | GET | `/api/v1/{resource}/page` |
| 列表查询(简单) | GET | `/api/v1/{resource}/list` |
| 详情查询 | GET | `/api/v1/{resource}/{id}` |
| 新增 | POST | `/api/v1/{resource}` |
| 更新 | POST | `/api/v1/{resource}/update` |
| 删除 | POST | `/api/v1/{resource}/delete/{id}` |

- [ ] 资源命名 `deals` / `counters` / `instruments`
- [ ] 子资源路径正确 `/deals/{id}/actions` (子资源)

### B2. 业务动作 (子路径)

| 动作 | URL |
|------|-----|
| 提交 | `POST /api/v1/{resource}/{id}/submit` |
| 审批通过 | `POST /api/v1/{resource}/{id}/approve` |
| 审批驳回 | `POST /api/v1/{resource}/{id}/reject` |
| 执行 | `POST /api/v1/{resource}/{id}/execute` |
| 撤销 | `POST /api/v1/{resource}/{id}/cancel` |

- [ ] 业务动作用 `/xxx/{id}/action` 路径
- [ ] 动作名动词清晰 (submit/approve/reject/execute)

### B3. 批量操作

| 操作 | URL |
|------|-----|
| 批量审批 | `POST /api/v1/{resource}/batch-approve` |
| 批量删除 | `POST /api/v1/{resource}/batch-delete` |
| 批量更新 | `POST /api/v1/{resource}/batch-update` |

- [ ] 批量接口前缀 `batch-`
- [ ] 批量接口有专门路径 (非 ids: List 入参)

---

## C. HTTP Method 规范

### C1. Method 使用红线 (Open-TMS 强制)

- [ ] 无 `@DeleteMapping` (Open-TMS 用 POST /delete/{id})
- [ ] 无 `@PutMapping` (Open-TMS 用 POST /update)
- [ ] 无 `@PatchMapping`
- [ ] GET 方法无 Body
- [ ] POST/PUT 有 Body (JSON)

### C2. Method 语义

| Method | 用途 | 是否幂等 |
|--------|------|---------|
| GET | 查询 / 详情 / 列表 / 分页 | 幂等 |
| POST | 新增 / 更新 / 删除 / 业务动作 | 非幂等(需 X-Idempotency-Key) |

- [ ] GET 仅查询,无副作用
- [ ] POST 含幂等保护
- [ ] 无写操作用 GET 实现

### C3. 注解使用

```java
// 推荐
@GetMapping("/api/v1/deals/{id}")
@PostMapping("/api/v1/deals")
@PostMapping("/api/v1/deals/update")

// 错误
@DeleteMapping("/api/v1/deals/{id}")       // 错
@PutMapping("/api/v1/deals")               // 错
@RequestMapping(value = "/createDeal", method = RequestMethod.POST)  // URL 错
```

---

## D. 路径参数 (Path Variable)

### D1. 参数命名
- [ ] 路径参数命名清晰 (`/{id}` 而非 `/{dealId}`)
- [ ] 复合资源用嵌套 (`/deals/{dealId}/actions`)
- [ ] 路径参数类型明确 (`@PathVariable Long id`)

### D2. 参数验证
- [ ] 路径参数非空 (Spring 自动)
- [ ] 类型转换失败有 400 响应

---

## E. 查询参数 (Query Param)

### E1. 分页参数

| 参数 | 类型 | 默认 | 说明 |
|------|------|------|------|
| `current` | int | 1 | 当前页 |
| `size` | int | 10 | 每页大小,最大 100 |
| `sort` | string | - | 排序字段,如 `createdAt,desc` |

- [ ] 分页参数命名一致 (`current` / `size` / `sort`)
- [ ] size 有上限 (100)
- [ ] sort 字段白名单

### E2. 搜索/筛选参数
- [ ] 业务编码 `code`
- [ ] 名称模糊 `name`
- [ ] 状态 `status`
- [ ] 类型 `type`
- [ ] 时间范围 `startDate` / `endDate`
- [ ] 参数命名与字段一致 (camelCase)

### E3. 反例

```java
// 反例 1: 模糊命名
@GetMapping("/api/v1/deals/list")  // list 是动词
@GetMapping("/api/v1/deals/findAll")  // 同上

// 反例 2: 大小写混用
@GetMapping("/api/v1/Deal/list")

// 反例 3: 缩写歧义
@GetMapping("/api/v1/cp/list")  // 应为 /counterparties

// 反例 4: 动词在 URL 中
@PostMapping("/api/v1/createDeal")
```

---

## F. 反例 (必须退回)

### F1. URL 命名反例

```java
// 反例 1: 单数
@GetMapping("/api/v1/deal")  // 应为 /deals

// 反例 2: 动词
@PostMapping("/api/v1/createDeal")  // 应为 POST /api/v1/deals

// 反例 3: 大小写
@GetMapping("/api/v1/Deal/{Id}")  // 应全小写

// 反例 4: 缩写
@GetMapping("/api/v1/dealList")  // 应为 /deals (复数)
```

### F2. Method 反例

```java
// 反例 1: DELETE 用于删除 (Open-TMS 红线)
@DeleteMapping("/api/v1/deals/{id}")
// 应改为
@PostMapping("/api/v1/deals/delete/{id}")

// 反例 2: PUT 用于更新 (Open-TMS 红线)
@PutMapping("/api/v1/deals")
// 应改为
@PostMapping("/api/v1/deals/update")

// 反例 3: GET 删除 (CRITICAL!)
@GetMapping("/api/v1/deals/delete/{id}")
```

### F3. 路径反例

```java
// 反例 1: 多层路径不规范
@PostMapping("/api/v1/deal/action/submit/approve")
// 应拆分为
@PostMapping("/api/v1/deals/{id}/submit")
@PostMapping("/api/v1/deals/{id}/approve")

// 反例 2: 版本混乱
@GetMapping("/api/deals/page")  // 缺 v1
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
- 任何 API-001/017 (URL/Method 红线) 未通过 → 直接降至 D
- 使用 @DeleteMapping / @PutMapping → 直接降至 D
- GET 用于写操作 → 直接降至 D
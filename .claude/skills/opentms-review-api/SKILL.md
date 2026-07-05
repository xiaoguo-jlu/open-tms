---
name: opentms-review-api
description: |
  Open-TMS 接口审核 Skill。由 Technical Architect / Backend Lead 调用,用于审核
  REST API 设计(URL / Method / Request / Response / Idempotency / Error),
  确保符合 Open-TMS CLAUDE.md 规范(/api/v1/{resource} + POST 统一 update/delete)、
  Apache CXF 4.0.3 + Spring Boot 3.2.0 最佳实践,以及业界对标
  (FIS Quantum / Murex MX.3 / SAP TRM / Bloomberg AIM) 的 API 设计标准。

  Trigger: "接口审核"、"API 评审"、"REST 审核"、"接口 review"、"API check"
---

# opentms-review-api

REST API 审核 — 对 OpenAPI / 接口文档 / Controller 设计 进行结构化审核,
确保符合 Open-TMS RESTful 规范、幂等设计、错误码标准,以及成熟资金系统
(FIS Quantum / Murex MX.3) 的 API 设计标准。

> **本 skill 遵循** `opentms-review-common` 公共规范 — 统一评级体系、报告格式、调用方式、归档路径。

---

## 输入

- 待审核的 API 文档路径(必填,可多个,位于 `docs/api/{module}/`)
- 所属模块名(必填,如 `basedata` / `dealing` / `fx`)
- 关联的 Controller Java 源码路径(可选,用于交叉验证)
- 关联的 DTO / VO 类路径(可选)
- 是否新增 / 修改既有接口(必填)

## 输出

- 审核报告: `docs/reviews/{feature-name}/api-review.md`
- 按 `templates/report.md` 填充

## 工作流程

1. **加载公共规范** — 读取 `opentms-review-common/SKILL.md`
2. **读取 API 文档** — 用 `Read` 工具读取接口文档 / OpenAPI YAML
3. **交叉验证** — 用 `Grep` 搜索 Controller / DTO / VO 实现,确认文档与代码一致
4. **加载 checklist** — 按 `checklists/01-url-design.md` / `02-request-response.md` / `03-idempotency-error.md` 逐项打勾
5. **逐项审核** — 按下方 YAML checklist 逐项判定 PASS/FAIL
6. **输出报告** — 评级 A/B/C/D + P0/P1/P2 问题清单 + 整改建议

---

## 审核项结构化清单 (YAML 数组)

```yaml
api_review_items:

  # ============= 用户列出的 3 点 =============

  - id: API-001
    name: 接口规范 (URL 版本, HTTP 方法, 响应统一)
    severity: P0
    standard: Open-TMS CLAUDE.md 强制 —
              URL: /api/v1/{resource}/{action};
              HTTP: GET 查询 / POST 新增+更新+删除(POST 统一 update/delete 红线);
              响应: Result<T> 统一包装
    check_method: |
      1. Read docs/api/{module}/*.md;
      2. Grep 所有 URL,验证:
         - 必须 /api/v1/ 前缀
         - 资源名复数 + 小写
         - 无动词在 URL 中(GET/POST 已表达);
      3. Grep HTTP Method:
         - 查询 → GET
         - 新增 → POST
         - 更新 → POST /api/v1/{resource}/update(非 PUT)
         - 删除 → POST /api/v1/{resource}/delete/{id}(非 DELETE);
      4. Grep 响应,验证是否全部用 Result<T> 包装。
    pass_criteria: URL + Method + Response 100% 符合规范
    failure_action: 退回 Backend 重构

  - id: API-002
    name: 出入参字段命名统一 (camelCase JSON ↔ snake_case DB)
    severity: P0
    standard: Open-TMS CLAUDE.md 强制 — JSON 字段 camelCase (Java 字段一致),
              DB 字段 snake_case,MyBatis Plus 自动映射
    check_method: |
      1. Read DTO/VO 类,识别所有字段命名;
      2. 验证:
         - JSON 输出 camelCase (e.g. dealNo, buyAmount, createdAt)
         - 无 snake_case 暴露到 API(除非必须)
         - 无 kebab-case;
      3. 验证 DB 实体与 VO 字段一一对应(避免命名不一致)。
    pass_criteria: 100% 字段 camelCase,与 DB snake_case 映射正确
    failure_action: 修正字段命名

  - id: API-003
    name: 出入参结构 (单选/多选/列表/分页) 合理
    severity: P0
    standard: Open-TMS CLAUDE.md 强制 —
              单条: Result<T>;
              列表+分页: Result<PageResult<T>> 含 {records, total, size, current, pages};
              多选入参: List<Long> ids;
              批量操作: Result<List<T>>
    check_method: |
      1. Read API 文档,识别每个接口的响应结构;
      2. 验证:
         - 单条查询 → Result<T>
         - 分页列表 → Result<{records, total, size, current, pages}>
         - 批量操作 → Result<List<T>>
         - 多选入参 → ids: List<Long>;
      3. 验证无返回 List<T>(无分页信息)。
    pass_criteria: 100% 接口响应结构符合规范
    failure_action: 修正响应结构

  # ============= 业界补充审核项 (FIS Quantum / Murex MX.3 / Bloomberg AIM) =============

  - id: API-004
    name: 幂等设计 (X-Idempotency-Key)
    severity: P0
    standard: Open-TMS CLAUDE.md 强制 — 写接口(POST)必须支持幂等,
              请求头 X-Idempotency-Key, 重复请求返回原结果 + idempotent: true,
              交易表必含 idempotency_key + tms_idempotency_t
    check_method: |
      1. Read Controller / Service,识别所有 POST 接口;
      2. 验证:
         - 写接口接收 X-Idempotency-Key 请求头
         - 重复请求幂等表查重 → 直接返回原结果
         - 响应体含 idempotent: true/false;
      3. 验证交易表含 idempotency_key VARCHAR(64) 字段。
    pass_criteria: 100% 写接口幂等保护
    failure_action: 补充幂等逻辑

  - id: API-005
    name: 错误码标准化 (200/400/401/403/404/422/500)
    severity: P0
    standard: Open-TMS CLAUDE.md 强制 —
              200 成功 / 400 业务异常 / 401 未授权 / 403 无权限 / 404 不存在 / 422 参数校验 / 500 系统异常
    check_method: |
      1. Read GlobalExceptionHandler;
      2. Grep 所有抛异常点,验证 code 取值;
      3. Grep Result 返回,验证 code 字段;
      4. 验证 BusinessException 使用 400,NotFoundException 404,
         ValidationException 422,AuthException 401。
    pass_criteria: 错误码 100% 符合枚举
    failure_action: 修正错误码

  - id: API-006
    name: 字段命名 camelCase (JSON) ↔ snake_case (DB) 映射
    severity: P1
    standard: Murex MX.3 规范 — JSON 字段对外 camelCase,DB 内部 snake_case,
              用 @TableField / @JsonProperty 显式映射
    check_method: |
      1. Read 实体类 + VO 类;
      2. 验证:
         - @TableField("buy_amount") 显式映射
         - @JsonProperty 处理特殊字段
         - 无驼峰/蛇形混用;
      3. 验证 VO 与 Entity 字段一一对应(用 BeanUtil.copyProperties)。
    pass_criteria: 字段映射清晰,无歧义
    failure_action: 补充映射注解

  - id: API-007
    name: URL 全部小写, 资源用复数
    severity: P1
    standard: RESTful 最佳实践 — URL 全小写,资源名复数 (e.g. /deals, /counters),
              避免动词
    check_method: |
      1. Grep 所有 URL;
      2. 验证:
         - URL 全小写(无 /CreateDeal 这种)
         - 资源名复数(/deals 不是 /deal)
         - 无动词在 URL 中(/createDeal ❌ 应 POST /deals);
      3. 验证无驼峰(/dealList ❌ 应 /deals)。
    pass_criteria: URL 100% 小写 + 复数 + 无动词
    failure_action: 修正 URL

  - id: API-008
    name: 入参校验 (JSR-303 + 业务校验)
    severity: P0
    standard: Spring Boot 最佳实践 — @Valid + @NotNull/@NotBlank/@Size/@Min/@Max 等
              注解,Service 层做业务校验
    check_method: |
      1. Read DTO 类;
      2. 验证:
         - 必填字段 @NotNull / @NotBlank / @NotEmpty
         - 字符串长度 @Size(max=50)
         - 数值范围 @Min / @Max
         - 日期格式 @Past / @Future;
      3. 验证 Controller 加 @Valid 触发校验;
      4. 验证 Service 层有业务校验(状态/外键存在性)。
    pass_criteria: 100% 入参校验覆盖
    failure_action: 补充校验注解

  - id: API-009
    name: 权限注解 (@PreAuthorize)
    severity: P0
    standard: Open-TMS 既有规范 — Controller 方法加 @PreAuthorize,
              控制到按钮/接口粒度
    check_method: |
      1. Read Controller;
      2. Grep @PreAuthorize / @RequiresPermissions;
      3. 验证:
         - 每个 Controller 方法都有权限注解
         - 权限粒度细到按钮(如 'deal:create' / 'deal:approve');
      4. 验证无裸 Controller(无任何权限注解)。
    pass_criteria: 100% Controller 有权限控制
    failure_action: 补充权限注解

  - id: API-010
    name: 审计字段 (createdBy/At 等应在响应中)
    severity: P1
    standard: Open-TMS CLAUDE.md — 响应含完整审计字段
              (id, createdBy, createdAt, updatedBy, updatedAt, version)
    check_method: |
      1. Read VO 类;
      2. 验证字段完整性:
         - id (主键)
         - createdBy / createdAt
         - updatedBy / updatedAt
         - version (乐观锁)
         - deleted (或隐藏);
      3. 验证前端可读(无 @JsonIgnore)。
    pass_criteria: 审计字段 100% 暴露
    failure_action: 补充 VO 字段

  - id: API-011
    name: 列表分页固定结构 {records, total, size, current, pages}
    severity: P0
    standard: Open-TMS CLAUDE.md 强制 — 所有列表接口统一返回
              { records: [...], total: N, size: 20, current: 1, pages: M }
    check_method: |
      1. Read 所有分页接口;
      2. Grep PageResult / PageInfo / IPage;
      3. 验证响应结构统一(用 PageResult 或类似包装类);
      4. 验证无裸 Page<T>(MyBatis Plus 原生,字段不统一)。
    pass_criteria: 分页结构 100% 统一
    failure_action: 统一分页结构

  - id: API-012
    name: 单条数据响应 vs 列表响应 边界
    severity: P1
    standard: RESTful 最佳实践 —
              详情: GET /api/v1/{resource}/{id} → Result<T>;
              列表: GET /api/v1/{resource}/page → Result<PageResult<T>>
    check_method: |
      1. Read API 文档,识别详情/列表接口;
      2. 验证:
         - 详情返回 T(单个对象)
         - 列表返回 PageResult<T>;
      3. 验证无混用(列表接口返回 T 或详情接口返回 List)。
    pass_criteria: 详情/列表响应边界清晰
    failure_action: 修正响应

  - id: API-013
    name: 时间字段格式 (ISO 8601)
    severity: P1
    standard: Open-TMS 规范 — JSON 时间 ISO 8601 字符串
              (YYYY-MM-DDTHH:mm:ss.SSSZ) 或 timestamp(13 位毫秒)
    check_method: |
      1. Read VO 类,识别日期/时间字段;
      2. 验证 @JsonFormat 注解:
         - pattern = "yyyy-MM-dd HH:mm:ss" 或 ISO 8601
         - timezone = "GMT+8" 或 "UTC";
      3. 验证返回字符串格式一致。
    pass_criteria: 时间格式统一
    failure_action: 补充 @JsonFormat

  - id: API-014
    name: 大数据量接口是否分页
    severity: P1
    standard: 性能最佳实践 — 列表接口必须分页(默认 size=10,最大 size=100),
              严禁返回完整 List
    check_method: |
      1. Read Service,识别 findAll / listAll / exportAll 类无分页方法;
      2. Grep `findAll|listAll|selectAll|selectList\(` 无分页参数;
      3. 验证导出接口单独实现(异步 + 任务 ID)。
    pass_criteria: 列表接口 100% 分页
    failure_action: 补充分页

  - id: API-015
    name: 跨域 CORS 配置
    severity: P2
    standard: Spring Boot 最佳实践 — 全局 CORS 配置(开发环境允许所有,
              生产环境白名单)
    check_method: |
      1. Read application.yml + WebMvcConfig;
      2. Grep CorsConfiguration / WebMvcConfigurer;
      3. 验证:
         - 开发环境允许所有来源
         - 生产环境白名单(已知前端域名);
      4. 验证 allowedMethod 包含 GET/POST/PUT/DELETE/OPTIONS。
    pass_criteria: CORS 配置正确
    failure_action: 修正 CORS

  - id: API-016
    name: API 文档 (OpenAPI / Swagger)
    severity: P1
    standard: Open-TMS 规范 — 接口文档在 docs/api/{module}/ 下 Markdown + OpenAPI YAML,
              含请求/响应示例 + 错误码
    check_method: |
      1. Glob docs/api/{module}/*.md;
      2. 验证:
         - 每个接口有 Markdown 文档
         - 含 URL + Method + 入参 + 出参 + 示例 + 错误码;
      3. 验证 OpenAPI YAML 与 Controller 同步。
    pass_criteria: 接口文档 100% 覆盖
    failure_action: 补充接口文档

  - id: API-017
    name: RESTful 语义 (GET/POST/PUT/DELETE 正确)
    severity: P0
    standard: RESTful 红线 — 查询用 GET,新增/更新/删除用 POST(Open-TMS 强制),
              严禁混用 (GET 删除/PUT 新增)
    check_method: |
      1. Read Controller;
      2. Grep @GetMapping / @PostMapping / @PutMapping / @DeleteMapping;
      3. 验证:
         - 无 @DeleteMapping(Open-TMS 用 POST /delete/{id})
         - 无 @PutMapping(Open-TMS 用 POST /update)
         - GET 不写操作
         - POST 包含 body;
      4. 验证无 @RequestMapping 含动词路径。
    pass_criteria: RESTful 语义 100% 正确
    failure_action: 修正 Method + 路径

  - id: API-018
    name: URL 命名 (资源路径不应包含动词)
    severity: P1
    standard: RESTful 最佳实践 — URL 仅资源(/deals), 动作在 Method 中(POST 表新增)
    check_method: |
      1. Grep 所有 URL;
      2. 验证:
         - 无 /createDeal / /updateDeal / /deleteDeal 这种 URL
         - 动作通过 Method + Body 表达;
      3. 验证特殊动作 URL(如 /submit /approve)放子路径合理。
    pass_criteria: URL 仅含资源
    failure_action: 重构 URL

  - id: API-019
    name: 批量操作接口
    severity: P2
    standard: Murex MX.3 规范 — 批量审批 / 批量删除 / 批量更新必须有专门接口
    check_method: |
      1. Grep batch / bulk / multi / multiple 关键字;
      2. 验证批量接口存在:
         - POST /api/v1/{resource}/batch-approve
         - POST /api/v1/{resource}/batch-delete
         - 或 ids: List<Long> 入参;
      3. 验证批量响应含成功/失败明细。
    pass_criteria: 批量接口齐全
    failure_action: 补充批量接口

  - id: API-020
    name: 异步操作 (返回 202 Accepted + 任务 ID)
    severity: P2
    standard: FIS Quantum 最佳实践 — 大数据导出 / 长时间操作返回 202 + 任务 ID,
              前端轮询或 WebSocket 通知
    check_method: |
      1. Read 导出 / 大批量操作接口;
      2. 验证:
         - 异步任务返回 taskId
         - 提供 /tasks/{taskId} 轮询接口
         - 完成后返回下载 URL;
      3. 验证无同步阻塞(响应 < 30s)。
    pass_criteria: 异步操作规范
    failure_action: 改造为异步

  - id: API-021
    name: 软删除字段 (deleted) 是否暴露给前端
    severity: P2
    standard: Open-TMS 最佳实践 — 软删除字段 deleted 应通过 VO 默认隐藏,
              前端无需感知
    check_method: |
      1. Read VO 类;
      2. 验证 deleted 字段:
         - 默认不返回(@JsonIgnore 或用专用 VO)
         - 或仅管理员可见(@JsonView);
      3. 验证无直接暴露 deleted 字段给所有用户。
    pass_criteria: 软删除字段隐藏
    failure_action: 隐藏 deleted 字段

  - id: API-022
    name: Controller try-catch 返回 Result.badRequest() 规范
    severity: P0
    standard: Controller 层禁止 try-catch 后手动返回 Result.badRequest(),应由 GlobalExceptionHandler 统一拦截。Service 抛 BusinessException,Controller 零异常处理代码
    check_method: |
      1. Grep `try\s*\{` 在所有 Controller 中
      2. Grep `Result.badRequest|Result.error|Result.fail` 在 Controller catch 块中(禁止)
      3. 验证 Service 层使用 throw new BusinessException(...)
      4. 验证 GlobalExceptionHandler 存在 @ExceptionHandler(BusinessException.class)
    pass_criteria: 0 个 Controller try-catch 返回 Result.badRequest();100% 异常由 GlobalExceptionHandler 处理
    failure_action: 移除 Controller try-catch,统一由 GlobalExceptionHandler 处理
```

---

## 审核流程 (Agent 可执行)

### Step 1: 范围确认

```bash
# 通过 Glob 定位待审核 API
docs/api/{module}/{resource}.md
Glob: docs/api/{module}/*.md

# 交叉验证 Controller
Glob: {module}/src/main/java/com/opentms/{module}/controller/*.java
```

### Step 2: 静态检查 (Read / Grep)

| 模式 | 用途 |
|------|------|
| `Read docs/api/{module}/*.md` | 读取 API 文档 |
| `Read Controller/*.java` | 交叉验证 |
| `Grep "@GetMapping"` | 检查 GET 方法 |
| `Grep "@PostMapping"` | 检查 POST 方法 |
| `Grep "@PutMapping\|@DeleteMapping"` | 检测 RESTful 违规 |
| `Grep "@PreAuthorize"` | 检查权限注解 |
| `Grep "X-Idempotency-Key\|idempotency_key"` | 检查幂等 |
| `Grep "Result<"` | 检查响应包装 |

### Step 3: 对标检查

对比 `dealing/src/main/java/com/opentms/dealing/controller/DealController.java`
— M1 已贯通的 AC/AT 接口。

### Step 4: 评级与输出

- 按 3 级 (P0/P1/P2) 打标问题;
- 按 `templates/report.md` 输出报告;
- 按 `opentms-review-common` 评级:
  - 含 P0 → D (返工)
  - 含 P1 → C (修复后复审)
  - 仅 P2 → B (通过,记录待优化)
  - 无任何问题 → A

### Step 5: 整改建议

- 每个问题提供具体代码片段或修改路径;
- 标注预计工时;
- 归档到 `docs/reviews/{feature-name}/api-review.md`。

---

## 一票否决 (P0 直判 D)

- **API-001**: URL 不规范或 HTTP Method 违规 (用 PUT/DELETE) → D
- **API-002**: 字段命名混乱 (camelCase/snake_case 混用) → D
- **API-003**: 响应结构混乱 (无 Result 包装/无分页) → D
- **API-004**: 写接口无幂等保护 → D
- **API-005**: 错误码不符合标准枚举 → D
- **API-008**: 入参无 JSR-303 校验 → D
- **API-009**: Controller 无权限注解 → D
- **API-011**: 分页结构不统一 → D
- **API-017**: RESTful 语义错误 (GET 删除) → D

---

## 协作关系

```
opentms-api-design (API 设计)
   └─→ opentms-review-api (本次) ★ API 审核
        └─→ opentms-review-backend (后端开发)
             └─→ opentms-review-frontend (前端开发)
```

---

## 相关文件

- `checklists/01-url-design.md` — URL 设计清单
- `checklists/02-request-response.md` — 请求/响应清单
- `checklists/03-idempotency-error.md` — 幂等/错误清单
- `references/standards.md` — 业界对标参考
- `templates/report.md` — 审核报告模板
- `../../opentms-review-common/SKILL.md` — 公共规范
- `../../../CLAUDE.md` — Open-TMS 项目规范
- `../../../{module}/src/main/java/com/opentms/{module}/controller/` — 既有 Controller 参考

---

## 版本历史

| 版本 | 日期 | 变更内容 |
|------|------|---------|
| v1.0 | 2026-07-05 | 初始版本 — 21 项 API 审核项 (10 P0 / 8 P1 / 3 P2) |
| v1.1 | 2026-07-05 | 新增 API-022: Controller try-catch 规范检查 |
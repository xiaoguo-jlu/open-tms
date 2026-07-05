---
name: opentms-review-backend
description: |
  Open-TMS 后端代码审核 Skill。由 Backend Lead / Tech Architect 调用,用于审核
  Java 17 + Spring Boot 3.2.0 + MyBatis Plus + Apache CXF 4.0.3 后端实现,确保符合
  Open-TMS CLAUDE.md 规范、五层架构、18 Maven 模块边界、GlobalConstants 枚举、
  FIS Quantum / Murex MX.3 / Spring Boot 最佳实践。

  Trigger: "后端审核"、"后端评审"、"Backend review"、"Java 代码审核"、"代码 check"
---

# opentms-review-backend

后端代码审核 — 对 Java / Spring Boot / MyBatis Plus 实现进行结构化审核,
确保符合 Open-TMS 项目规范与企业级资金系统 (FIS Quantum / Murex MX.3) 后端架构标准。

> **本 skill 遵循** `opentms-review-common` 公共规范 — 统一评级体系、报告格式、调用方式、归档路径。

---

## 输入

- 待审核的 Java 文件路径(必填,可多个 / 模块)
- 所属 Maven 模块名(必填,如 `basedata` / `dealing`)
- 关联的 API 文档路径(必填,如 `docs/api/{module}/`)
- 是否新增 / 修改 / 重构(必填)

## 输出

- 审核报告: `docs/reviews/{feature-name}/backend-review.md`
- 按 `templates/report.md` 填充

## 工作流程

1. **加载公共规范** — 读取 `opentms-review-common/SKILL.md`
2. **定位代码** — 用 `Glob` 定位 `{module}/src/main/java/com/opentms/{module}/` 下文件
3. **分层检查** — 依次审核 Controller / Service / Mapper / Entity / DTO / VO
4. **静态检查** — 用 `Grep` 搜索关键模式(事务、异常、注入、幂等、软删除、乐观锁)
5. **对标检查** — 对比 Open-TMS 已合并模块(basedata / dealing)
6. **加载 checklist** — 按 `checklists/01-分层与规范.md` 逐项打勾
7. **逐项审核** — 按下方 YAML checklist 判定 PASS/FAIL
8. **输出报告** — 评级 A/B/C/D + P0/P1/P2 问题清单 + 整改建议

---

## 审核项结构化清单 (YAML 数组)

```yaml
backend_review_items:

  # ============= 用户列出的 5 点 =============

  - id: BE-001
    name: 接口实现与设计一致
    severity: P0
    standard: 与 opentms-api-design 产出的 API 文档 100% 一致(URL / Method / 入参 / 出参 / 错误码)
    check_method: |
      1. 读取 Controller 类,逐接口对比 API 文档;
      2. 用 Grep `@PathMapping|@RequestMapping|@PostMapping|@GetMapping` 列出所有端点;
      3. 校验 URL 命名规范(/api/v1/{resource}/{action});
      4. 校验 POST 统一 update/delete 红线(不能使用 PUT/DELETE);
      5. 校验入参 DTO / 出参 VO 类型与文档一致。
    pass_criteria: 接口 100% 与 API 文档一致;无 PUT/DELETE 用于 update/delete;无遗漏接口
    failure_action: 退回后端对齐 API 文档

  - id: BE-002
    name: 健壮性 (空指针/大数据量/精度)
    severity: P0
    standard: BigDecimal 精度 / 空指针防御 / 大数据量分页 / 边界条件处理
    check_method: |
      1. Grep `BigDecimal` 与 `double|float` (后者禁止用于金额);
      2. Grep `!= null` 校验关键判空;
      3. 检查分页查询 max=1000;
      4. 检查边界条件(id 不存在 / 状态非法 / 重复提交);
      5. 检查金额运算使用 `add|subtract|multiply|divide` (不用 +/-/*//)。
    pass_criteria: BigDecimal 100% 用于金额;分页 max=1000;边界条件 100% 处理
    failure_action: 退回修复健壮性

  - id: BE-003
    name: 数据修改接口 (版本号防并发 + 性能)
    severity: P0
    standard: @Version 乐观锁 + 索引命中 + 事务边界
    check_method: |
      1. 检查 Entity 含 `version INT DEFAULT 0` 字段 + `@Version` 注解;
      2. Grep `@Transactional` 在 update/delete 方法;
      3. Grep `rollbackFor = Exception.class` (写操作必备);
      4. 校验 Mapper SQL 使用索引(EXPLAIN 检查);
      5. 校验更新 SQL 不使用 SELECT * 预查询。
    pass_criteria: 100% 写方法有 @Transactional(rollbackFor);乐观锁字段存在;SQL 命中索引
    failure_action: 退回补充并发控制

  - id: BE-004
    name: 可扩展性/抽象 (公共代码 3+ 处复用)
    severity: P1
    standard: 重复代码 3+ 处必须抽象为公共类 / 方法
    check_method: |
      1. 识别业务中重复的 BeanUtil.copyProperties / 校验 / 转换逻辑;
      2. 识别 Service 中重复的事务 / 异常处理;
      3. 识别 Controller 中重复的参数校验 / 错误码;
      4. 检查 common/ 模块是否复用 (禁止业务代码放 common)。
    pass_criteria: 重复代码 3+ 处 100% 抽象;不出现明显的复制粘贴
    failure_action: 抽象为 BaseService / Util

  - id: BE-005
    name: 存量特性影响
    severity: P0
    standard: 不破坏 M1 已贯通特性(basedata 14 Resource + dealing AC/AT)
    check_method: |
      1. 识别本次修改涉及的存量资源(country / currency / bank / counterparty /
         business_unit / instrument / deal / action);
      2. 校验 schema / API / 前端兼容性;
      3. 校验是否需要数据迁移脚本;
      4. 校验是否新增字段会破坏现有字段。
    pass_criteria: 影响范围已列出;无破坏性变更或附带迁移脚本
    failure_action: 退回评估影响范围

  # ============= 业界补充 (FIS Quantum / Murex / Spring Boot 最佳实践) =============

  - id: BE-006
    name: Controller/Service/Mapper 分层严格
    severity: P0
    standard: Open-TMS 强制分层 — Controller 接收 DTO / Service 返回 VO / Mapper 数据访问
    check_method: |
      1. 校验目录结构 controller / service / mapper / entity / dto / vo;
      2. 校验 Controller 不直接调用 Mapper;
      3. 校验 Service 不返回 Entity (必须转 VO);
      4. 校验 Controller 不包含业务逻辑。
    pass_criteria: 分层 100% 严格;无跨层调用
    failure_action: 重构分层

  - id: BE-007
    name: Service 抽象接口 + Impl (便于 mock 测试)
    severity: P1
    standard: Spring Boot 最佳实践 — Service 接口 + Impl 实现分离
    check_method: |
      1. 检查 Service 接口定义与 Impl 实现;
      2. 校验 Controller 注入接口而非 Impl;
      3. 校验 Impl 类命名以 `Impl` 结尾。
    pass_criteria: 100% Service 接口 + Impl 分离
    failure_action: 拆分为接口 + Impl

  - id: BE-008
    name: @Transactional(rollbackFor = Exception.class) 写操作
    severity: P0
    standard: CLAUDE.md 强制 — 写操作必须有 rollbackFor
    check_method: |
      Grep `@Transactional` 在 Service 类/方法;
      校验参数含 `rollbackFor = Exception.class`;
      校验读方法用 `readOnly = true`。
    pass_criteria: 写方法 100% 有 rollbackFor;读方法有 readOnly
    failure_action: 补充事务注解

  - id: BE-009
    name: 异常处理 (BusinessException 而非 RuntimeException)
    severity: P0
    standard: CLAUDE.md 强制 — 业务异常用 BusinessException
    check_method: |
      Grep `throw new RuntimeException` 在 Service 中(禁止);
      Grep `throw new BusinessException` (强制);
      校验 GlobalExceptionHandler 存在并捕获 BusinessException。
    pass_criteria: 0 个裸 RuntimeException;100% 业务异常用 BusinessException
    failure_action: 替换异常类型

  - id: BE-010
    name: 入参校验 (JSR-303 @NotNull/@NotBlank 等)
    severity: P1
    standard: Spring Boot 最佳实践
    check_method: |
      Grep `@NotNull|@NotBlank|@NotEmpty|@Size|@Min|@Max|@Pattern` 在 DTO;
      校验 Controller 加 `@Valid` 触发校验;
      校验错误统一由 GlobalExceptionHandler 返回。
    pass_criteria: 100% 入参 DTO 有字段级校验;Controller 触发 @Valid
    failure_action: 补充校验注解

  - id: BE-011
    name: 权限控制 (@PreAuthorize 或拦截器)
    severity: P1
    standard: Spring Security 最佳实践 / Open-TMS 五层支撑层
    check_method: |
      Grep `@PreAuthorize|@Secured` 在 Controller 方法;
      校验关键写操作(删除/审批/执行)有权限注解;
      校验 Resource Server 配置。
    pass_criteria: 关键写操作 100% 有权限控制
    failure_action: 补充权限注解

  - id: BE-012
    name: 审计字段自动填充 (MetaObjectHandler / AOP)
    severity: P1
    standard: Open-TMS CLAUDE.md 必备审计字段
    check_method: |
      1. 检查 Entity 含 created_by/at/updated_by/at/version/deleted;
      2. 检查 MybatisPlusConfig 含 MetaObjectHandler 自动填充;
      3. Grep `setFieldValByName` 实现;
      4. 校验更新操作自动填 updated_by/at。
    pass_criteria: 100% 自动填充审计字段;无手动 setFieldVal
    failure_action: 配置 MetaObjectHandler

  - id: BE-013
    name: 性能 (N+1 查询 / 索引命中)
    severity: P1
    standard: MyBatis Plus 最佳实践
    check_method: |
      1. 识别 listByXxx 是否有 N+1 风险(后续遍历单查);
      2. 校验多表 JOIN 用 @Select 注解或 XML,避免 N+1;
      3. 校验高频查询字段建索引;
      4. 校验慢 SQL 日志(MyBatis Plus PerformanceInterceptor 或 p6spy)。
    pass_criteria: 无 N+1;高频查询有索引;慢 SQL 有监控
    failure_action: 优化 SQL 与索引

  - id: BE-014
    name: 大数据量接口 (分页 + 流式, max=1000)
    severity: P1
    standard: Open-TMS 强制规范 — 单次最多 1000 条
    check_method: |
      Grep `Page<|pageSize|size=` 在 Controller;
      校验 size 上限 1000;
      校验导出/批量接口用流式而非全量加载。
    pass_criteria: 100% 列表接口分页;size ≤1000;大数据量流式处理
    failure_action: 补充分页与限制

  - id: BE-015
    name: 缓存使用 (Caffeine, TTL 60s)
    severity: P2
    standard: Spring Boot 缓存最佳实践
    check_method: |
      Grep `@Cacheable|@CacheEvict|Caffeine` 在 Service;
      校验字典类(国家/币种/状态)有缓存;
      校验 TTL 配置(60s)。
    pass_criteria: 字典类 100% 有缓存;TTL 合理
    failure_action: 补充缓存

  - id: BE-016
    name: 线程安全 (避免 static 可变状态)
    severity: P1
    standard: Spring Boot 最佳实践
    check_method: |
      Grep `static (List|Map|Set|HashMap|ArrayList)` 在代码中(禁止);
      校验共享变量用 ThreadLocal 或方法局部变量。
    pass_criteria: 0 个 static 可变集合
    failure_action: 移除 static 状态

  - id: BE-017
    name: 软删除 (@TableLogic)
    severity: P0
    standard: CLAUDE.md 必备 — deleted='0' 过滤
    check_method: |
      1. 检查 Entity 含 `deleted CHAR(1) DEFAULT '0'` 字段;
      2. 校验字段加 `@TableLogic`;
      3. 校验所有查询自动过滤已删除数据。
    pass_criteria: 100% 业务主表有 @TableLogic
    failure_action: 补充 @TableLogic

  - id: BE-018
    name: 乐观锁 (@Version)
    severity: P0
    standard: CLAUDE.md 必备 — 并发更新防护
    check_method: |
      1. 检查 Entity 含 `version INT DEFAULT 0` 字段;
      2. 校验字段加 `@Version`;
      3. 校验更新 SQL 自动带 version 条件。
    pass_criteria: 100% 业务主表有 @Version
    failure_action: 补充 @Version

  - id: BE-019
    name: 幂等 (X-Idempotency-Key)
    severity: P0
    standard: CLAUDE.md 必备 — 防重复提交
    check_method: |
      1. 检查 Entity 含 `idempotency_key VARCHAR(64)`;
      2. 校验配套 `tms_idempotency_t` 表;
      3. Grep `X-Idempotency-Key` 在 Interceptor / Filter;
      4. 校验写操作前先查幂等表。
    pass_criteria: 交易表 100% 有幂等键;幂等拦截器存在
    failure_action: 补充幂等机制

  - id: BE-020
    name: 跨模块依赖 (不直接调用,通过 API)
    severity: P1
    standard: Open-TMS 模块边界规范
    check_method: |
      Grep `import com.opentms.{other-module}` 在代码中;
      校验跨模块调用用 HTTP/Feign 而非直接 import;
      校验 common/ 模块无业务代码。
    pass_criteria: 0 个跨模块直接 import;common/ 0 业务代码
    failure_action: 重构为 API 调用

  - id: BE-021
    name: 跨服务通信 (Feign / HTTP 客户端)
    severity: P2
    standard: Spring Cloud OpenFeign 最佳实践
    check_method: |
      1. Grep `@FeignClient` 在代码中;
      2. 校验 Feign 接口定义在 api 模块;
      3. 校验超时 / 重试 / 熔断配置。
    pass_criteria: 跨服务调用 100% 走 Feign;有超时配置
    failure_action: 引入 Feign

  - id: BE-022
    name: 配置外化 (application.yml + sys_configs)
    severity: P2
    standard: Spring Boot 最佳实践
    check_method: |
      1. 校验无硬编码业务参数(阈值 / 限额 / 利率等);
      2. Grep `@Value|@ConfigurationProperties`;
      3. 校验 sys_configs 动态配置表存在。
    pass_criteria: 0 个硬编码业务参数;配置 100% 外化
    failure_action: 外化配置

  - id: BE-023
    name: 日志规范 (关键操作 INFO, 异常 ERROR 含堆栈)
    severity: P1
    standard: Open-TMS CLAUDE.md 日志规范
    check_method: |
      1. Grep `log.info` 关键操作(提交/审批/执行);
      2. Grep `log.error(.*e\.getMessage\(\), e\)` (含堆栈);
      3. 校验日志格式 `[opType] 用户[user] 执行[action]操作, 业务编号[no]`;
      4. 校验不在生产输出 SQL 参数。
    pass_criteria: 关键节点 100% 有 INFO 日志;异常 ERROR 含堆栈;无 SQL 参数泄漏
    failure_action: 修正日志

  - id: BE-024
    name: SQL 注入防护 (MyBatis Plus #{} 占位符)
    severity: P0
    standard: MyBatis Plus 强制 — 用 #{} 而非 ${}
    check_method: |
      Grep `\${` 在 Mapper XML / @Select 注解(禁止);
      Grep `#{}` (强制);
      校验动态表名 / 排序字段用白名单校验后再用 ${}。
    pass_criteria: 100% 用 #{};0 个无防护的 ${}
    failure_action: 替换为 #{}

  - id: BE-025
    name: 敏感数据脱敏 (@SensitiveInfo)
    severity: P1
    standard: CLAUDE.md 红线 — 密码/手机号/身份证必须脱敏
    check_method: |
      1. Grep `@SensitiveInfo` 在 VO;
      2. 校验密码 BCrypt 加密存储;
      3. 校验身份证/手机号/银行卡号在 VO 序列化时脱敏;
      4. 校验日志不打印明文敏感数据。
    pass_criteria: 100% 敏感字段有脱敏;密码 BCrypt
    failure_action: 补充脱敏

  - id: BE-026
    name: 状态机实现 (枚举 + 校验)
    severity: P1
    standard: CLAUDE.md — 状态流转必须校验
    check_method: |
      1. Grep 状态字段与状态机校验方法;
      2. 校验 GlobalConstants 枚举定义所有状态;
      3. 校验状态流转校验(如 New → Submitted 合法,Submitted → Executed 不合法)。
    pass_criteria: 状态流转 100% 校验;枚举 100% 在 GlobalConstants
    failure_action: 补充状态机校验

  - id: BE-027
    name: 公共代码抽象 (BaseService)
    severity: P2
    standard: Spring Boot 最佳实践
    check_method: |
      1. 检查 common/ 是否有 BaseService / BaseController / BaseMapper;
      2. 校验业务 Service 继承 BaseService 复用 CRUD;
      3. 校验业务 Controller 继承 BaseController。
    pass_criteria: 业务模块 100% 继承 BaseService/BaseController
    failure_action: 抽象公共基类

  - id: BE-028
    name: 文档完整 (Javadoc + 业务说明)
    severity: P2
    standard: Open-TMS 编码规范
    check_method: |
      1. 检查 Service 方法有 Javadoc;
      2. 检查 Controller 有 @ApiOperation(Swagger);
      3. 复杂业务逻辑有 @author + 业务说明注释。
    pass_criteria: Service 方法 100% 有 Javadoc;Controller 100% 有 Swagger 注解
    failure_action: 补充文档

  - id: BE-029
    name: 与 Murex/FIS Quantum 后端架构对比
    severity: P2
    standard: 业界核心能力对标
    check_method: 对比 Murex MX.3 / FIS Quantum 后端架构,识别差距。
    pass_criteria: 关键能力 ≥70% 一致
    failure_action: 长期规划

  - id: BE-030
    name: 单测覆盖率 (Service 层 ≥70%)
    severity: P1
    standard: Open-TMS 工程实践
    check_method: |
      1. 检查 src/test/java/ 目录;
      2. 校验 Service 层有单测;
      3. 运行 mvn test 生成 JaCoCo 报告;
      4. 校验覆盖率 ≥70%。
    pass_criteria: Service 单测覆盖率 ≥70%
    failure_action: 补充单测
```

---

## 审核流程 (Agent 可执行)

### Step 1: 定位代码

```bash
# 定位模块代码
{module}/src/main/java/com/opentms/{module}/
├── controller/
├── service/   (+ impl/)
├── mapper/
├── entity/
├── dto/
└── vo/
```

### Step 2: 分层检查

| 层级 | 关键检查 |
|------|----------|
| Controller | @PostMapping(URL 规范)/ @Valid / 无业务逻辑 / 注入 Service 接口 |
| Service | @Transactional / 抛 BusinessException / 返回 VO / 接口+Impl |
| Mapper | #{} 占位符 / 索引字段 / 分页 |
| Entity | 审计字段 / @TableLogic / @Version / 继承 BaseEntity |
| DTO | 入参校验注解 |
| VO | @SensitiveInfo 脱敏 / 千分位 |

### Step 3: 静态检查 (Grep)

| 模式 | 用途 |
|------|------|
| `Grep "@Transactional"` | 事务注解 |
| `Grep "throw new RuntimeException"` | 裸 RuntimeException (禁止) |
| `Grep "throw new BusinessException"` | 业务异常 (强制) |
| `Grep "\${"` | SQL 注入风险 |
| `Grep "static (List\|Map)"` | 线程不安全 |
| `Grep "double\|float"` | 精度风险 |
| `Grep "@TableLogic"` | 软删除 |
| `Grep "@Version"` | 乐观锁 |
| `Grep "PUT\|@DeleteMapping"` | 违反 POST 红线 |
| `Grep "console.error\|System.out"` | 不规范的输出 |

### Step 4: 对标检查
对比 Open-TMS 已合并模块:
- `basedata/` — 14 Resource 完整实现
- `dealing/` — AC/AT 全流程

### Step 5: 评级与输出
按 `opentms-review-common` 评级:
- 含 P0 → D (返工)
- 含 P1 → C (修复后复审)
- 仅 P2 → B (通过,记录待优化)
- 无任何问题 → A

---

## 一票否决 (P0 直判 D)

| 编号 | 条款 |
|------|------|
| BE-001 | 接口与 API 文档不一致 |
| BE-002 | BigDecimal 用于金额违规 / 边界条件未处理 |
| BE-005 | 破坏存量特性 |
| BE-008 | 写操作无 @Transactional(rollbackFor) |
| BE-009 | 业务异常用裸 RuntimeException |
| BE-017 | 主表缺 @TableLogic |
| BE-018 | 主表缺 @Version |
| BE-019 | 交易表缺幂等键 |
| BE-024 | 用 ${} 无防护的 SQL 注入风险 |

---

## 协作关系

```
opentms-api-design (API 文档)
   └─→ opentms-backend-dev (后端实现)
        └─→ opentms-review-backend (本次) ★ 后端审核
             └─→ opentms-review-frontend (前端审核)
                  └─→ opentms-test-execution (QA 测试)
```

**与 opentms-review-api 区别**:
- `opentms-review-api`: API 文档层面 (URL / Method / Schema / 错误码)
- `opentms-review-backend`: 代码实现层面 (分层 / 事务 / 异常 / 性能)

---

## 相关文件

- `checklists/01-分层与规范.md` — 分层检查清单
- `references/standards.md` — 后端规范映射
- `templates/report.md` — 审核报告模板
- `../../opentms-review-common/SKILL.md` — 公共规范
- `../../../CLAUDE.md` — Open-TMS 项目规范

---

## 版本历史

| 版本 | 日期 | 变更内容 |
|------|------|----------|
| v1.0 | 2026-07-05 | 初始版本 — 30 项后端审核项 (9 P0 / 16 P1 / 5 P2) |
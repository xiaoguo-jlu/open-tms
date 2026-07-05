# Checklist 01 — 后端代码模式

> 配合 `opentms-review-backend` SKILL.md 使用。审核员按此清单逐项勾选。

---

## A. 分层架构 (BE-006 / BE-007)

### A1. 三层分离
- [ ] Controller 仅做路由 + 参数接收 + 响应包装
- [ ] Controller 中无 SQL / Mapper 调用 / 业务逻辑
- [ ] Service 中无 `@Mapper` 直接引用
- [ ] Mapper 中无业务逻辑 / 状态判断
- [ ] Controller → Service → Mapper 引用方向单向

### A2. 接口与实现分离
- [ ] Service 全部为 `interface IXxxService` + `class IXxxServiceImpl`
- [ ] 命名规范统一(IXxxService / IXxxServiceImpl)
- [ ] Controller 注入接口而非实现

### A3. 包结构
- [ ] `controller/` / `service/`(+`impl/`) / `mapper/` / `entity/` / `dto/` / `vo/` / `enums/`
- [ ] 强制分层 — 入参 `dto` / 出参 `vo` / 持久化 `entity` 分文件
- [ ] Controller 接收 DTO,Service 返回 VO(用 `BeanUtil.copyProperties` 转换)

---

## B. 事务与异常 (BE-008 / BE-009)

### B1. 事务注解
- [ ] 写操作全部 `@Transactional(rollbackFor = Exception.class)`
- [ ] 读操作 `@Transactional(readOnly = true)`
- [ ] 事务范围合理(避免大事务 / 循环调用)
- [ ] 嵌套事务传播级别正确(`REQUIRED` / `REQUIRES_NEW`)

### B2. 异常处理
- [ ] 业务异常统一抛 `BusinessException`
- [ ] 0 `throw new RuntimeException` 直抛
- [ ] 错误码在 `GlobalConstants` 中定义
- [ ] 异常由 `GlobalExceptionHandler` 统一捕获
- [ ] 异常日志含堆栈(`log.error(msg, e)`)

---

## C. 注解使用 (BE-010 / BE-011 / BE-012)

### C1. 入参校验 (JSR-303)
- [ ] Controller 入参 `@Valid`
- [ ] DTO 字段含 `@NotBlank` / `@NotNull` / `@Size` / `@Min` / `@Max` / `@Pattern`
- [ ] 自定义校验器(`@AssertTrue` / `ConstraintValidator`)
- [ ] 校验失败提示友好(`message = "..."`)

### C2. 权限控制
- [ ] 写操作 Controller / Service 含 `@PreAuthorize`
- [ ] 权限表达式清晰(`hasRole('ADMIN')` / `hasAuthority('deal:create')`)
- [ ] URL 级权限在 SecurityConfig 配置

### C3. 审计字段
- [ ] Entity 审计字段含 `@TableField(fill = FieldFill.INSERT/UPDATE)`
- [ ] 接入 `MetaObjectHandler` 实现自动填充
- [ ] `created_by/at` 自动取当前用户 / 时间
- [ ] `updated_by/at` 自动更新

---

## D. 日志规范 (BE-023)

### D1. 日志级别
- [ ] 关键节点 `log.info("[{}] 用户[{}] 执行[{}]操作, 业务编号[{}]", opType, user, entity, no)`
- [ ] 异常 `log.error("交易[{}] 执行失败: {}", txNo, e.getMessage(), e)`
- [ ] 不在生产输出 SQL 参数(`log.debug`)

### D2. 日志内容
- [ ] 异常日志含堆栈
- [ ] 业务日志含业务编号(便于追踪)
- [ ] 0 `System.out.println` / `printStackTrace`
- [ ] 0 `console.error` (前端范畴)

---

## E. 代码风格 (BE-028)

### E1. 命名规范
- [ ] 类名 PascalCase(`AcDealService`)
- [ ] 方法名 camelCase(`getDealById`)
- [ ] 常量 UPPER_SNAKE(`DEAL_STATUS_NEW`)
- [ ] 包名 lowercase(`com.opentms.dealing`)

### E2. 注释
- [ ] 公共 Service 方法有 Javadoc
- [ ] 复杂业务有行内注释
- [ ] 0 TODO / FIXME 残留
- [ ] 0 无意义注释(`// 循环开始`)

### E3. 公共代码复用
- [ ] 重复代码 0 处(3+ 处必须抽取)
- [ ] Service 继承 `BaseService<DTO, Entity, VO>`
- [ ] 状态机校验抽取公共校验器
- [ ] 转换逻辑抽取 `BeanUtil.copyProperties` 链

---

## F. SQL 与数据访问 (BE-013 / BE-024)

### F1. SQL 注入防护
- [ ] MyBatis Plus 全部用 `#{}` 占位符
- [ ] 0 `${}` 字符串拼接(白名单 ORDER BY 等除外)
- [ ] 复杂 SQL 用 `<bind>` 或预编译

### F2. 性能
- [ ] 0 N+1 查询(循环中调 Mapper)
- [ ] 关键查询命中索引(EXPLAIN 验证)
- [ ] 大表查询分页(避免全表扫描)
- [ ] 批量操作 `insertBatch` / `updateBatchById`

---

## G. 敏感数据 (BE-025)

### G1. 脱敏
- [ ] 密码 / 身份证 / 手机号 / 银行卡 用 `@SensitiveInfo`
- [ ] 响应 VO 自动脱敏
- [ ] 日志中敏感字段脱敏
- [ ] 密码 BCrypt 加密存储

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
- 任何 BE-001 / BE-002 / BE-003 / BE-005 / BE-006 / BE-008 / BE-009 / BE-017 / BE-018 / BE-019 / BE-024 / BE-025 (P0) 未通过 → 直接降至 C
- 3 个 P0 未通过 → 直接降至 D
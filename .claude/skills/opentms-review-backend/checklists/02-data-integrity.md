# Checklist 02 — 数据完整性 / 并发

> 配合 `opentms-review-backend` SKILL.md 使用。审核员按此清单逐项勾选。

---

## A. 审计字段 (CLAUDE.md 强制)

### A1. 必填审计字段
- [ ] `created_by VARCHAR(50) NOT NULL`
- [ ] `created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP`
- [ ] `updated_by VARCHAR(50)`
- [ ] `updated_at TIMESTAMP`
- [ ] `version INT DEFAULT 0`
- [ ] `deleted CHAR(1) DEFAULT '0'`

### A2. 自动填充
- [ ] Entity 含 `@TableField(fill = FieldFill.INSERT)` (created_*)
- [ ] Entity 含 `@TableField(fill = FieldFill.INSERT_UPDATE)` (updated_*)
- [ ] 接入 `MetaObjectHandler` 实现
- [ ] `created_by` 从 `SecurityContextHolder` 取当前用户

---

## B. 乐观锁 (BE-018)

### B1. @Version 注解
- [ ] Entity `version` 字段含 `@Version`
- [ ] MyBatis Plus 自动识别并附加 `WHERE version = ?`
- [ ] update SQL 自动 `version = version + 1`
- [ ] 0 手工 set version

### B2. 并发更新
- [ ] update 接口基于 Entity(含 version)而非 DTO
- [ ] Service `updateById(entity)` 而非 `update()` SQL
- [ ] 失败抛 `OptimisticLockingFailureException` / `BusinessException`

---

## C. 软删除 (BE-017)

### C1. @TableLogic
- [ ] Entity `deleted` 字段含 `@TableLogic`
- [ ] MyBatis Plus 自动附加 `WHERE deleted = 0`
- [ ] 删除接口走 `removeById()` 而非 SQL
- [ ] 物理删除仅 admin 操作

### C2. 数据保留
- [ ] 软删除数据可查询(管理后台)
- [ ] 5 年保留策略(金融监管)
- [ ] 历史数据归档到 `*_his` 表

---

## D. 幂等 (BE-019)

### D1. 幂等键
- [ ] 写接口支持 `X-Idempotency-Key` 请求头
- [ ] Controller 读取并传递到 Service
- [ ] Service 基于 `tms_idempotency_t` 表查询
- [ ] 重复请求返回原结果(`idempotent: true`)

### D2. 幂等表
- [ ] 交易表含 `idempotency_key VARCHAR(64)`
- [ ] 唯一索引 `UNIQUE(idempotency_key)`
- [ ] 配套 `tms_idempotency_t` 表
- [ ] TTL 设置(默认 24h)

---

## E. 金额精度 (BE-002)

### E1. 字段精度
- [ ] 普通金额 `DECIMAL(18,2)`
- [ ] 汇率 `DECIMAL(18,8)`
- [ ] 利率 `DECIMAL(10,4)`
- [ ] AC Deal / Cashflow `DECIMAL(38,18)` (高精度)

### E2. Java 类型
- [ ] 金额字段用 `BigDecimal` 而非 `Double` / `Float`
- [ ] BigDecimal 运算指定 `MathContext` / `RoundingMode`
- [ ] 序列化为字符串(避免 JSON 精度丢失)

### E3. 计算
- [ ] 0 浮点数运算精度问题
- [ ] 货币运算 `multiply` / `divide` 指定精度
- [ ] 舍入规则 `HALF_UP`

---

## F. 大数据量 (BE-014)

### F1. 分页
- [ ] 列表接口用 `Page<T>` 分页
- [ ] max size 限制(≤ 1000)
- [ ] 默认 size 10 / 20
- [ ] 返回 `total` / `size` / `current` / `records`

### F2. 流式
- [ ] 超大数据用游标 / 流式(MyBatis Plus Cursor)
- [ ] 导出接口用 `Stream<T>` / 分批写入
- [ ] 0 一次性 `selectList(*)` 返回百万级数据

### F3. 性能
- [ ] 索引命中(EXPLAIN 验证)
- [ ] 复杂查询用子查询 / JOIN 优化
- [ ] 缓存高频读接口(Caffeine TTL 60s)

---

## G. 缓存 (BE-015)

### G1. Caffeine
- [ ] 高频读接口 `@Cacheable`
- [ ] TTL 60s / maxSize 1000
- [ ] `@CacheEvict` 在写操作清除
- [ ] 缓存 key 含业务参数

### G2. 防穿透 / 击穿 / 雪崩
- [ ] 空值缓存(null cache)
- [ ] 分布式锁(synchronized / Redisson)
- [ ] 随机 TTL 避免雪崩

---

## H. 跨服务一致性

### H1. 分布式事务
- [ ] 跨服务调用 Seata AT 模式
- [ ] 最终一致性 / TCC 模式
- [ ] 本地消息表 / RocketMQ
- [ ] 0 强一致跨服务事务

### H2. 数据一致性
- [ ] 跨服务幂等
- [ ] 对账机制(基于 tms_reconciliation_t)
- [ ] 失败重试 + 死信队列

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
- 任何 BE-017 / BE-018 / BE-019 / BE-024 / BE-025 (P0) 未通过 → 直接降至 C
- 3 个 P0 未通过 → 直接降至 D
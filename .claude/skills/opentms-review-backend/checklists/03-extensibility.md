# Checklist 03 — 扩展性 / 抽象

> 配合 `opentms-review-backend` SKILL.md 使用。审核员按此清单逐项勾选。

---

## A. 公共代码抽象 (BE-027)

### A1. BaseService
- [ ] 通用 Service 继承 `BaseService<DTO, Entity, VO>`
- [ ] 公共方法 `save/update/getById/page/list/remove`
- [ ] 转换逻辑 `BeanUtil.copyProperties` 抽象
- [ ] 0 每个 Service 重复实现通用 CRUD

### A2. BaseMapper
- [ ] 继承 `BaseMapper<Entity>`
- [ ] 公共方法 `insert/updateById/selectById/deleteById/selectPage`
- [ ] 复杂 SQL 抽到 Mapper.xml

### A3. BaseEntity
- [ ] 公共字段 `id / created_by/at / updated_by/at / version / deleted`
- [ ] `BaseCodeEntity` 含 `xxx_no / xxx_name / status`
- [ ] 所有 Entity 继承 BaseEntity / BaseCodeEntity

---

## B. 状态机抽象 (BE-026)

### B1. 状态枚举
- [ ] 状态用 enum 定义(`DealStatus` / `ActionStatus`)
- [ ] 枚举值在 `GlobalConstants`
- [ ] 状态变更前后校验 `canTransit(from, to)`

### B2. 状态机引擎
- [ ] 复杂状态流转用状态机(Spring StateMachine / 自研)
- [ ] 状态变更触发事件(Event)
- [ ] 0 散落的 if-else 状态判断

---

## C. 策略模式 (BE-029)

### C1. 业务策略
- [ ] 业务规则可配置(阈值 / 税率 / 限额)
- [ ] 策略接口 + 多个实现(Strategy Pattern)
- [ ] Spring `@Component` 自动注入
- [ ] 0 硬编码 if-else 业务分支

### C2. 配置化
- [ ] 配置项在 `application.yml` / `sys_configs` 表
- [ ] `@ConfigurationProperties` 注入
- [ ] `@RefreshScope` 支持动态刷新
- [ ] 0 业务硬编码字符串 / 数字

---

## D. 模块边界 (BE-020 / BE-021)

### D1. 模块单向依赖
- [ ] Maven 依赖方向 DAG(无循环)
- [ ] 跨模块调用走 API / Feign
- [ ] 共享类放 `common` 模块
- [ ] 0 跨模块直接 Mapper / Service 调用

### D2. 跨服务通信
- [ ] 统一 OpenFeign
- [ ] 0 `RestTemplate` / `HttpURLConnection` 直连
- [ ] Feign 客户端超时 / 重试 / 降级
- [ ] 服务注册 / 发现(Nacos / Eureka)

### D3. API 版本管理
- [ ] URL 路径 `/api/v1/` / `/api/v2/`
- [ ] 兼容旧版本 ≥2 个迭代
- [ ] Deprecated 接口标注 `@Deprecated`

---

## E. 领域分层 (DDD)

### E1. 分层架构
- [ ] `domain` 领域模型(实体 / 值对象 / 领域服务)
- [ ] `application` 应用服务(用例编排)
- [ ] `infrastructure` 基础设施(Mapper / 缓存 / MQ)
- [ ] `interfaces` 接口层(Controller / DTO)

### E2. 聚合根
- [ ] 业务聚合根清晰(Deal + Action + Image)
- [ ] 跨聚合走领域事件
- [ ] 0 跨表事务滥用

---

## F. 单一职责 (SRP)

### F1. 类职责
- [ ] Controller 仅负责路由
- [ ] Service 仅负责业务编排
- [ ] Mapper 仅负责数据访问
- [ ] Util 类无状态(static method only)

### F2. 方法职责
- [ ] 方法长度 ≤ 50 行
- [ ] 圈复杂度 ≤ 10
- [ ] 参数 ≤ 5 个(过多用 DTO 包装)

---

## G. 开闭原则 (OCP)

### G1. 扩展开放
- [ ] 新增功能无需修改既有代码
- [ ] 通过接口 / 抽象类扩展
- [ ] 模板方法 / 策略模式应用
- [ ] 插件化机制(SPI / Spring Factories)

### G2. 修改封闭
- [ ] 核心逻辑稳定,变更影响面小
- [ ] 配置驱动而非代码修改

---

## H. 依赖倒置 (DIP)

### H1. 抽象依赖
- [ ] Service 注入接口而非实现
- [ ] `@Autowired` 按类型 / 名称注入
- [ ] 0 `new XxxServiceImpl()` 直 new

### H2. 测试友好
- [ ] Mock 接口便于单测
- [ ] `@MockBean` 注入 Mock
- [ ] 单测覆盖率 Service 层 ≥ 70%

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
- 任何 BE-005 / BE-020 / BE-021 (P1) 未通过 → 降至 C
- 核心模块循环依赖 → 直接 D
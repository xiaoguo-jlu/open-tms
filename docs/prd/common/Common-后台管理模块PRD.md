# Open-TMS Common - 后台管理模块 PRD

**版本**: v1.0
**日期**: 2026-07-05
**角色**: 产品经理 (PM)
**模块代号**: `admin` (Maven Module) / `common-admin` (代码路径前缀)
**文档状态**: 待评审

---

## 一、模块概述

### 1.1 模块名称与定位

**模块名称**: 后台管理 (Admin / System Management)

**功能定位**: 提供系统级运维与管理能力的统一入口,对标 **FIS Quantum Administration** / **SAP NetWeaver SLD / TAANA** / **Murex Back Office Configuration**。本模块是 Open-TMS 平台层 (五层架构中的"基础支撑层") 的核心子系统,集中管理系统运行所需的元数据、配置、字典、单据规则、栏目、通知与定时任务。

**用户角色**:

| 角色 | 职责 | 权限范围 |
|------|------|----------|
| 系统管理员 (SUPER_ADMIN) | 全部后台管理功能 | 全部 |
| 配置管理员 (CONFIG_ADMIN) | 字典 / 参数 / 编号规则 | 配置类 |
| 栏目管理员 (MENU_ADMIN) | 栏目 / 权限关联 | 栏目类 |
| 审计员 (AUDITOR) | 只读全部后台数据 | 查询类 |
| 普通用户 | 只能使用 `GET /api/v1/dicts/*` 与 `GET /api/v1/menus/user` | 受限 |

### 1.2 在五层架构中的位置

```
决策支持层 (Cockpit / AI)
     ↓
核心业务层 (流动性 / 投融资 / 风险)
     ↓
基础操作层 (账户 / 结算 / 对账)
     ↓
集成连接层 (银企 / ERP / 市场)
     ↓
基础支撑层  ← ★ 本模块位于此层
├─ 权限 (基于 M1 组织权限)
├─ 工作流
├─ 审计
└─ 后台管理 (admin)
   ├─ 栏目管理
   ├─ 数据字典
   ├─ 系统参数
   ├─ 单据编号规则
   ├─ 系统常量
   ├─ 通知中心
   └─ 定时任务
```

### 1.3 与其他模块的关系

| 上游 (本模块为依赖方) | 说明 |
|----------------------|------|
| basedata | 银行/币种/对手方/管理主体/交易员 (字典初始数据来源) |
| dealing | 现有 AC/AT/FX Deal 通过编号规则服务生成 deal_no |
| approval | 通知中心依赖 approval 事件推送 |

| 下游 (本模块提供服务) | 说明 |
|----------------------|------|
| 全业务模块 | 字典查询 / 参数查询 / 编号生成 |
| 前端 | 动态栏目加载 / 字典下拉 |
| 集成层 | 单据编号规则 (SWIFT/银行报文引用) |

### 1.4 业务背景与目标

**当前痛点**:

1. **菜单硬编码**: `web/src/App.vue` 静态菜单,`router/index.js` 静态路由,新增模块需前端重新发布
2. **字典散落**: `GlobalConstants.java` 仅含 4 个常量,业务枚举 (DealStatus/ActionType/InstrumentType) 散落在各模块 `enums` 包
3. **配置硬编码**: 审批阈值、汇率刷新频率、批处理上限等参数散落代码各处
4. **编号规则散乱**: `AC20260629-0001` / `CF20260629-0001` 等由各模块自行 `String.format` 生成,无法统一管控,易冲突
5. **通知缺失**: 审批结果无站内消息通道,依赖人工查询
6. **定时任务无统一视图**: 利息计提、日终批处理等任务无法集中监控

**本期目标**:

- 提供**统一后台管理门户** (admin 模块),为运维人员提供集中配置能力
- 提供**字典 / 配置 / 编号规则**服务,业务模块解耦硬编码
- 提供**动态栏目**,支持菜单热更新
- 打通**通知通道** (站内消息)
- 建立**定时任务监控**视图

---

## 二、业界对标

### 2.1 详细对比表

| 特性 | FIS Quantum | SAP S/4 TRM | Murex MX.3 | Kyriba | Open-TMS 设计 |
|------|------------|-------------|-----------|--------|---------------|
| **菜单管理** | 动态,基于 Role,支持多 workspace | BSP/WDA 静态 + SUCOMPANY 树 | Workspace + module + ribbon | 动态,基于 Permission Set | **动态 + 权限 + 热更新** (P0) |
| **数据字典** | T0002 / T0050 多语 + 灵活层级 | T0050 + 自定义域 | Domain / Code List,支持多语 | 集中 List of Values | **集中 + 单语 (多语预留)** |
| **编号规则** | Number Range (TNR) 内置模板 | Number Range Object (SNRO) | Sequence Generator,支持 variant | 灵活模板 | **灵活模板 + 乐观锁** |
| **系统参数** | IMG 路径 / 多层 Profile | Customizing IMG / SPRO | Parameter Set,支持分租户 | Tenant Settings | **数据库 + Caffeine 60s TTL** |
| **通知** | Email / SMS / Dashboard | Business Workplace (SBWP) | Alert Center + Email | Email + 站内 | **站内优先 + 邮件 P2** |
| **定时任务** | Batch Scheduling + ActiveMQ | SM36/SM37 Job 调度 | Murex Batch Scheduler | Job Engine | **Spring `@Scheduled` + Quartz** |
| **常量管理** | Customizing 表 + 程序集 | Domain Fixed Values | Code List | 字典表 | **DB 表 + Spring Cache** |
| **审计追踪** | Audit Trail | Application Log (SLG1) | Audit DB | Activity Log | **复用 `tms_audit_log_t`** |

### 2.2 关键设计借鉴

- **FIS Quantum**: 菜单 → 基于角色 + workspace 隔离;本模块采用 `permission_code` 关联权限系统
- **SAP SNRO**: 编号范围对象支持 prefix + 日期模板 + 序号,带 buffer 优化;本模块简化版,无 buffer
- **Murex Code List**: 字典支持多语;本模块本期单语,字典预留 `i18n_key` 字段

---

## 三、功能清单

### 3.1 栏目管理 - P0

#### 3.1.1 栏目维护 (tms_menus_t)

提供后台栏目 CRUD,支持树形层级 (最多 3 级)。

**字段 (完整见 §4.1)**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | BIGSERIAL | Y | 主键 |
| `menu_code` | VARCHAR(50) | Y | 栏目编码,唯一 |
| `menu_name` | VARCHAR(100) | Y | 栏目名称 |
| `parent_id` | BIGINT | N | 上级栏目 ID,顶级为 NULL |
| `path` | VARCHAR(200) | Y | 前端路由路径,以 / 开头 |
| `component` | VARCHAR(200) | N | 前端组件路径 |
| `icon` | VARCHAR(50) | N | Element-Plus 图标名 |
| `sort_order` | INT | Y | 同级排序,升序 |
| `permission_code` | VARCHAR(100) | Y | 关联权限系统的权限码 |
| `category` | VARCHAR(20) | Y | 栏目类型:MENU/BUTTON |
| `hidden` | CHAR(1) | Y | 是否隐藏:'1'/'0' |
| `status` | CHAR(1) | Y | 启用:'1'/禁用:'0' |

#### 3.1.2 栏目层级

- **一级**: 模块 (如 "基础数据" / "交易管理" / "后台管理")
- **二级**: 模块下栏目 (如 "币种管理" / "国家管理")
- **三级**: 栏目下子页面 (如 "币种详情" — 通常页面内嵌,本期预留)
- **最多 3 级**: 通过 `parent_id` 自引用实现,递归查询时深度超限拒绝 (业务规则 R11)

#### 3.1.3 栏目-权限关联

- 每个栏目绑定一个 `permission_code`,对应用户权限系统中的权限项 (如 `basedata:currency:view`)
- 用户登录后,后端根据用户权限过滤栏目树
- 前端调用 `GET /api/v1/menus/user` 获取当前用户可见栏目

#### 3.1.4 栏目缓存

- 启动时全量加载栏目树到 Caffeine 缓存 (key=`ALL_MENU_TREE`,TTL=10min)
- 后台修改栏目时主动失效缓存
- 提供 `POST /api/v1/admin/menus/refresh-cache` 手动刷新按钮

### 3.2 数据字典 - P0

#### 3.2.1 字典分类 (tms_dict_types_t)

按业务领域分组管理字典项。

**字段 (完整见 §4.2)**:

| 字段 | 类型 | 说明 |
|------|------|------|
| `dict_type_code` | VARCHAR(50) UNIQUE | 字典类型编码,如 `trade_status` |
| `dict_type_name` | VARCHAR(100) | 字典类型名称,如 "交易状态" |
| `description` | VARCHAR(500) | 描述 |
| `status` | CHAR(1) | '1' 启用 / '0' 禁用 |
| `i18n_key` | VARCHAR(100) | 国际化 key (预留) |

**初始化字典类型**:

| 字典编码 | 字典名称 | 说明 |
|---------|---------|------|
| `trade_status` | 交易状态 | New/Submitted/Approved/Settled/Rejected/Canceled |
| `action_type` | 动作类型 | CREATE/UPDATE/DELETE/SUBMIT/APPROVE/REJECT/EXECUTE |
| `deal_type` | 交易类型 | AC/AT/FX |
| `instrument_type` | 金融工具类型 | AC/AT/FX/DEPOSIT/LOAN/IR/EQ/BOND/SWAP/OPTION/FORWARD |
| `currency` | 币种 | 由 basedata `tms_currency_t` 同步 |
| `country` | 国家 | 由 basedata `tms_country_t` 同步 |
| `image_type` | 影像类型 | CREATE/UPDATE/DELETE |
| `approval_status` | 审批状态 | Pending/Approved/Rejected |
| `direction` | 资金方向 | Inflow/Outflow (M1.5 新增) |
| `cf_type` | 现金流类型 | Principal/Interest/Fee |
| `reset_cycle` | 编号重置周期 | None/Daily/Monthly/Yearly |

#### 3.2.2 字典项 (tms_dict_items_t)

字典分类下的具体枚举值。

**字段 (完整见 §4.3)**:

| 字段 | 类型 | 说明 |
|------|------|------|
| `dict_type_code` | VARCHAR(50) FK | 关联字典分类 |
| `item_code` | VARCHAR(50) | 项编码,如 `NEW` |
| `item_name` | VARCHAR(100) | 项名称,如 "新建" |
| `sort_order` | INT | 同类排序 |
| `status` | CHAR(1) | '1'/'0' |
| `color` | VARCHAR(20) | 前端展示色,如 `success/warning/info/danger` |
| `i18n_key` | VARCHAR(100) | 国际化 key |
| `remark` | VARCHAR(500) | 备注 |

#### 3.2.3 字典用途与替换策略

| 现状 | 本期目标 |
|------|----------|
| `GlobalConstants.STATUS_ENABLED/DISABLED` | 保持,**共存在迁移期** |
| `dealing/enums/DealStatus.java` | 启动时从 `trade_status` 字典加载到内存 Map |
| `dealing/enums/ActionType.java` | 启动时从 `action_type` 字典加载 |
| 硬编码字符串 `'AC'/'AT'/'FX'` | 字典项编码 (本期共存) |

**迁移策略** (本期只读新增,不改老代码):
- 业务模块**新增字段/状态**必须从字典读取
- 旧 `enums` 包保留,作为**默认值兜底**
- 字典接口 `GET /api/v1/dicts/{typeCode}` 返回最新配置
- 后续 Phase 5+ 逐步迁移存量代码

#### 3.2.4 字典缓存

- 启动时按 `dict_type_code` 分组全量加载到内存 `ConcurrentHashMap<String, List<DictItemVO>>`
- 缓存 key: `dict:type:{typeCode}`,TTL=10min
- 字典项变更时 (admin 操作) → 发送 `dict_change_event` → 主动清除缓存
- 提供 `POST /api/v1/admin/dicts/refresh-cache` 手动刷新按钮

### 3.3 系统参数配置 - P0

#### 3.3.1 参数分类 (tms_sys_configs_t)

按作用域分三级:**系统级 / 业务级 / 集成级**

**系统级** (CMS):

| 配置 Key | 默认值 | 说明 |
|---------|--------|------|
| `system.name` | Open-TMS | 系统名称 (前端 header 显示) |
| `system.logo.url` | - | Logo URL |
| `system.copyright` | © 2026 Open-TMS | 版权信息 |
| `system.theme.color` | #409EFF | 主题色 |

**业务级** (BUSINESS):

| 配置 Key | 默认值 | 说明 |
|---------|--------|------|
| `biz.approval.threshold.amount` | 100000.00 | 免审批金额上限 (CNY) |
| `biz.approval.threshold.fx_amount` | 50000.00 | 外汇免审批金额上限 (USD) |
| `biz.rate.refresh.frequency.min` | 60 | 汇率刷新频率(分钟) |
| `biz.batch.max.size` | 1000 | 批处理最大记录数 |
| `biz.deal.idempotency.ttl.hours` | 24 | 交易幂等键过期时间 |
| `biz.cashpool.max.levels` | 3 | 现金池最大层级 |
| `biz.limit.warning.threshold.pct` | 80 | 限额预警阈值百分比 |

**集成级** (INTEGRATION):

| 配置 Key | 默认值 | 说明 |
|---------|--------|------|
| `integ.bloomberg.api.key` | (加密) | Bloomberg API 密钥 |
| `integ.swift.endpoint.url` | https://swift.example.com | SWIFT 网关地址 |
| `integ.bank.api.timeout.ms` | 30000 | 银行 API 超时(毫秒) |

#### 3.3.2 字段 (完整见 §4.4)

| 字段 | 类型 | 说明 |
|------|------|------|
| `config_key` | VARCHAR(100) UNIQUE | 配置键 |
| `config_value` | TEXT | 配置值 (加密存储在 INTEG 类别) |
| `value_type` | VARCHAR(20) | STRING/NUMBER/BOOLEAN/JSON |
| `category` | VARCHAR(20) | SYSTEM/BUSINESS/INTEGRATION |
| `description` | VARCHAR(500) | 说明 |
| `editable` | CHAR(1) | '1' 可编辑 / '0' 系统只读 |
| `encrypted` | CHAR(1) | '1' 加密存储 (AES-256) |

#### 3.3.3 配置加载与缓存

- 启动时全量加载到 `ConcurrentHashMap<String, String>`
- Caffeine 缓存 (key=`config:{key}`, TTL=60s, maxSize=1000)
- 业务代码调用 `ConfigService.getString(key)` / `getNumber(key)` / `getBoolean(key)`
- 集成级加密字段 `config_value` 加密,返回时解密

### 3.4 单据编号规则 - P0

#### 3.4.1 规则配置 (tms_doc_number_rules_t)

字段 (完整见 §4.5):

| 字段 | 类型 | 说明 |
|------|------|------|
| `rule_code` | VARCHAR(50) UNIQUE | 规则编码,如 `AC_DEAL` |
| `rule_name` | VARCHAR(100) | 规则名称,如 "AC 交易编号" |
| `prefix` | VARCHAR(10) | 前缀,如 `AC` |
| `date_format` | VARCHAR(20) | 日期格式 Java 风格,如 `yyyyMMdd` |
| `sequence_length` | INT | 序号位数 (1-10) |
| `current_value` | BIGINT | 当前序号 |
| `increment` | INT | 步长 (默认 1) |
| `reset_cycle` | VARCHAR(20) | 重置周期:None/Daily/Monthly/Yearly |
| `max_value` | BIGINT | 最大值 (默认 9999999) |
| `enabled` | CHAR(1) | '1' 启用 / '0' 禁用 |

#### 3.4.2 现有单据规则初始化

| rule_code | 规则名称 | prefix | date_format | sequence_length | reset_cycle |
|-----------|---------|--------|-------------|-----------------|-------------|
| `AC_DEAL` | AC 交易编号 | `AC` | `yyyyMMdd` | 4 | Daily |
| `AT_DEAL` | AT 转账编号 | `AT` | `yyyyMMdd` | 4 | Daily |
| `FX_DEAL` | FX 外汇编号 | `FX` | `yyyyMMdd` | 4 | Daily |
| `ACTION` | Action 编号 | `ACT` | `yyyyMMdd` | 6 | Daily |
| `DEALMAP` | DealMap 编号 | `DMP` | `yyyyMMdd` | 6 | Daily |
| `CASHFLOW` | 现金流编号 | `CF` | `yyyyMMdd` | 6 | Daily |
| `APPROVAL_TASK` | 审批任务编号 | `APT` | `yyyyMMdd` | 4 | Daily |
| `DEAL_IMAGE` | 影像编号 | `IMG` | `yyyyMMdd` | 6 | Daily |
| `INSTRUMENT` | 金融工具编号 | `INS` | - | 6 | None |
| `TRADER` | 交易员编号 | `TR` | - | 4 | None |

#### 3.4.3 编号生成流程

```
业务请求 (如 creating AC Deal)
       ↓
[1] DocNumberService.generate("AC_DEAL")
       ↓
[2] 加载规则 (cache: ruleCache, TTL=10min)
       ↓
[3] 判断是否需要重置 (reset_cycle == "Daily" && 日期变更)
       ├─ 是 → current_value = 0
       └─ 否 → 不变
       ↓
[4] 乐观锁更新 current_value + 1
   UPDATE tms_doc_sequences_t
   SET current_value = current_value + increment,
       version = version + 1,
       updated_at = now()
   WHERE rule_code = 'AC_DEAL'
     AND version = #{oldVersion}
       ↓
[5] 重试 (最多 3 次) 或失败抛 ConcurrencyException
       ↓
[6] 拼接: prefix + formatted_date + padLeft(sequence, length, '0')
   例: "AC" + "20260705" + "0001" → "AC20260705-0001"
   (本期用 "-" 分隔符,后续可配置 separator)
       ↓
[7] 返回单据号
```

#### 3.4.4 并发安全

- 单规则串行: 使用乐观锁 (`version` 字段)
- 重试机制: 失败后随机退避 10-50ms 重试,最多 3 次
- 高并发场景 (QPS > 100): 后续 Phase 5+ 引入 Redis 分布式锁

#### 3.4.5 序号表 (tms_doc_sequences_t)

为保证单据号的全局唯一性,序号存储在独立表 `tms_doc_sequences_t`,与规则表分离 (1:N):

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | BIGSERIAL PK | 主键 |
| `rule_code` | VARCHAR(50) FK | 关联规则 |
| `current_value` | BIGINT | 当前序号 |
| `period_key` | VARCHAR(20) | 周期标识 (Daily: yyyyMMdd; None: 'NONE') |
| `version` | INT | 乐观锁 |
| `updated_at` | TIMESTAMP | 更新时间 |

### 3.5 系统常量 (P1)

#### 3.5.1 字段 (tms_system_constants_t, §4.7)

| 字段 | 类型 | 说明 |
|------|------|------|
| `const_code` | VARCHAR(100) UNIQUE | 常量编码 |
| `const_value` | TEXT | 常量值 |
| `const_type` | VARCHAR(20) | STRING/NUMBER/BOOLEAN |
| `description` | VARCHAR(500) | 说明 |

#### 3.5.2 用途

业务模块硬编码常量集中管理,与字典的区别:
- **字典**: 用户可见,前端下拉使用
- **常量**: 程序内部使用,不直接展示给用户

**示例常量**:

| const_code | const_value | 说明 |
|-----------|-------------|------|
| `state.deal.transition.submit` | `New->Submitted` | 状态机流转规则 |
| `state.deal.transition.approve` | `Submitted->Approved` | 状态机流转规则 |
| `cashpool.default.cycle.day` | `1` | 现金池默认日终周期 |
| `valuation.default.method` | `PVS` | 默认估值方法 |

### 3.6 通知中心 - P1

#### 3.6.1 通知类型

| 类型 | 编码 | 场景 |
|------|------|------|
| 系统通知 | `announcement` | 系统公告/版本升级通知 |
| 审批提醒 | `approval_request` | 待审批推送 |
| 待办 | `todo` | 业务待办 (Cashflow 核对等) |
| 预警 | `alert` | 限额预警/汇率异常 |
| 任务结果 | `task_result` | 定时任务执行结果 |

#### 3.6.2 字段 (tms_notifications_t, §4.8)

| 字段 | 类型 | 说明 |
|------|------|------|
| `user_id` | BIGINT | 接收人 |
| `type` | VARCHAR(20) | 通知类型 |
| `title` | VARCHAR(200) | 标题 |
| `content` | TEXT | 内容 |
| `link` | VARCHAR(500) | 跳转链接 |
| `read_status` | CHAR(1) | '0' 未读 / '1' 已读 |
| `priority` | VARCHAR(10) | LOW/NORMAL/HIGH/URGENT |
| `expire_at` | TIMESTAMP | 过期时间 |

#### 3.6.3 推送通道

| 通道 | 本期 | 后续 |
|------|------|------|
| 站内消息 (WebSocket + DB) | ✅ | ✅ |
| 邮件 | ❌ | P2 |
| 短信 | ❌ | P2 |
| APP Push | ❌ | P2 |

**本期站内消息机制**:
- 写入 `tms_notifications_t`
- WebSocket 推送 (`/ws/notifications/{userId}`),前端订阅
- 用户登录后调用 `GET /api/v1/admin/notifications/unread-count` 拉取未读数 (header 铃铛)
- 点击通知跳转 `link`

### 3.7 定时任务 - P1

#### 3.7.1 调度框架

- **简单任务**: Spring `@Scheduled` (cron 表达式)
- **复杂任务**: Quartz (动态启停、分布式)
- **持久化**: 任务定义存 `tms_scheduled_jobs_t`,执行历史存 `tms_scheduled_job_logs_t`

#### 3.7.2 字段 (tms_scheduled_jobs_t, §4.9)

| 字段 | 类型 | 说明 |
|------|------|------|
| `job_code` | VARCHAR(50) UNIQUE | 任务编码 |
| `job_name` | VARCHAR(100) | 任务名称 |
| `job_class` | VARCHAR(200) | Spring Bean 名 / Quartz Job 类 |
| `cron_expression` | VARCHAR(50) | Cron 表达式 |
| `job_type` | VARCHAR(20) | SCHEDULED / QUARTZ |
| `enabled` | CHAR(1) | '1' 启用 / '0' 禁用 |
| `last_run_at` | TIMESTAMP | 上次执行时间 |
| `next_run_at` | TIMESTAMP | 下次执行时间 |

#### 3.7.3 内置任务

| job_code | 任务 | 频率 | 优先级 |
|---------|------|------|--------|
| `INTEREST_ACCRUAL` | 利息计提 | 每日 23:00 | P0 |
| `EOD_BATCH` | 日终批处理 | 每日 00:30 | P0 |
| `RATE_REFRESH` | 汇率同步 | 每小时 | P0 |
| `LIMIT_ALERT_CHECK` | 限额预警扫描 | 每 30 分钟 | P1 |
| `DEAL_STATUS_RECONCILE` | 交易状态核对 | 每日 02:00 | P1 |
| `NOTIFICATION_CLEANUP` | 通知清理 | 每日 03:00 | P2 |

#### 3.7.4 任务监控

- 执行历史 (`tms_scheduled_job_logs_t`): 开始/结束时间、状态、耗时、错误信息
- 失败任务: 自动发通知给 SUPER_ADMIN (类型=`task_result`, priority=`HIGH`)
- 控制台: 任务列表 + 启停 + 手动触发 + 最近 N 次执行历史

---

## 四、字段设计

### 4.1 tms_menus_t (栏目表)

```sql
CREATE TABLE tms_menus_t (
    id                  BIGSERIAL PRIMARY KEY,
    menu_code           VARCHAR(50)  NOT NULL UNIQUE,
    menu_name           VARCHAR(100) NOT NULL,
    parent_id           BIGINT,
    path                VARCHAR(200) NOT NULL,
    component           VARCHAR(200),
    icon                VARCHAR(50),
    sort_order          INT          NOT NULL DEFAULT 0,
    permission_code     VARCHAR(100) NOT NULL,
    category            VARCHAR(20)  NOT NULL DEFAULT 'MENU',
    hidden              CHAR(1)      NOT NULL DEFAULT '0',
    status              CHAR(1)      NOT NULL DEFAULT '1',
    remark              VARCHAR(500),

    created_by          VARCHAR(50)  NOT NULL,
    created_at          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by          VARCHAR(50),
    updated_at          TIMESTAMP,
    version             INT          DEFAULT 0,
    deleted             CHAR(1)      DEFAULT '0',

    CONSTRAINT chk_menu_category CHECK (category IN ('MENU', 'BUTTON')),
    CONSTRAINT chk_menu_status CHECK (status IN ('0', '1')),
    CONSTRAINT chk_menu_hidden CHECK (hidden IN ('0', '1'))
);

CREATE INDEX idx_menus_parent_id ON tms_menus_t(parent_id);
CREATE INDEX idx_menus_permission ON tms_menus_t(permission_code);
CREATE INDEX idx_menus_status ON tms_menus_t(status);

COMMENT ON TABLE tms_menus_t IS '栏目管理表';
COMMENT ON COLUMN tms_menus_t.menu_code IS '栏目编码,业务唯一';
COMMENT ON COLUMN tms_menus_t.parent_id IS '上级栏目ID,顶级为NULL';
COMMENT ON COLUMN tms_menus_t.permission_code IS '权限码,关联权限系统';
COMMENT ON COLUMN tms_menus_t.category IS '类型: MENU/BUTTON';
```

### 4.2 tms_dict_types_t (字典分类)

```sql
CREATE TABLE tms_dict_types_t (
    id                  BIGSERIAL PRIMARY KEY,
    dict_type_code      VARCHAR(50)  NOT NULL UNIQUE,
    dict_type_name      VARCHAR(100) NOT NULL,
    description         VARCHAR(500),
    status              CHAR(1)      NOT NULL DEFAULT '1',
    i18n_key            VARCHAR(100),
    remark              VARCHAR(500),

    created_by          VARCHAR(50)  NOT NULL,
    created_at          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by          VARCHAR(50),
    updated_at          TIMESTAMP,
    version             INT          DEFAULT 0,
    deleted             CHAR(1)      DEFAULT '0',

    CONSTRAINT chk_dict_type_status CHECK (status IN ('0', '1'))
);

CREATE INDEX idx_dict_types_status ON tms_dict_types_t(status);

COMMENT ON TABLE tms_dict_types_t IS '数据字典分类表';
```

### 4.3 tms_dict_items_t (字典项)

```sql
CREATE TABLE tms_dict_items_t (
    id                  BIGSERIAL PRIMARY KEY,
    dict_type_code      VARCHAR(50)  NOT NULL,
    item_code           VARCHAR(50)  NOT NULL,
    item_name           VARCHAR(100) NOT NULL,
    sort_order          INT          NOT NULL DEFAULT 0,
    status              CHAR(1)      NOT NULL DEFAULT '1',
    color               VARCHAR(20),
    i18n_key            VARCHAR(100),
    remark              VARCHAR(500),

    created_by          VARCHAR(50)  NOT NULL,
    created_at          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by          VARCHAR(50),
    updated_at          TIMESTAMP,
    version             INT          DEFAULT 0,
    deleted             CHAR(1)      DEFAULT '0',

    CONSTRAINT uq_dict_items_type_code UNIQUE (dict_type_code, item_code, deleted),
    CONSTRAINT chk_dict_item_status CHECK (status IN ('0', '1'))
);

CREATE INDEX idx_dict_items_type_code ON tms_dict_items_t(dict_type_code);
CREATE INDEX idx_dict_items_status ON tms_dict_items_t(status);

COMMENT ON TABLE tms_dict_items_t IS '数据字典项表';
```

### 4.4 tms_sys_configs_t (系统参数)

```sql
CREATE TABLE tms_sys_configs_t (
    id                  BIGSERIAL PRIMARY KEY,
    config_key          VARCHAR(100) NOT NULL UNIQUE,
    config_value        TEXT         NOT NULL,
    value_type          VARCHAR(20)  NOT NULL DEFAULT 'STRING',
    category            VARCHAR(20)  NOT NULL DEFAULT 'BUSINESS',
    description         VARCHAR(500),
    editable            CHAR(1)      NOT NULL DEFAULT '1',
    encrypted           CHAR(1)      NOT NULL DEFAULT '0',
    status              CHAR(1)      NOT NULL DEFAULT '1',
    remark              VARCHAR(500),

    created_by          VARCHAR(50)  NOT NULL,
    created_at          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by          VARCHAR(50),
    updated_at          TIMESTAMP,
    version             INT          DEFAULT 0,
    deleted             CHAR(1)      DEFAULT '0',

    CONSTRAINT chk_config_value_type CHECK (value_type IN ('STRING', 'NUMBER', 'BOOLEAN', 'JSON')),
    CONSTRAINT chk_config_category CHECK (category IN ('SYSTEM', 'BUSINESS', 'INTEGRATION')),
    CONSTRAINT chk_config_editable CHECK (editable IN ('0', '1')),
    CONSTRAINT chk_config_encrypted CHECK (encrypted IN ('0', '1')),
    CONSTRAINT chk_config_status CHECK (status IN ('0', '1'))
);

CREATE INDEX idx_sys_configs_category ON tms_sys_configs_t(category);
CREATE INDEX idx_sys_configs_status ON tms_sys_configs_t(status);

COMMENT ON TABLE tms_sys_configs_t IS '系统参数配置表';
COMMENT ON COLUMN tms_sys_configs_t.encrypted IS '加密存储 (AES-256),仅 INTEGRATION 类使用';
```

### 4.5 tms_doc_number_rules_t (单据编号规则)

```sql
CREATE TABLE tms_doc_number_rules_t (
    id                  BIGSERIAL PRIMARY KEY,
    rule_code           VARCHAR(50)  NOT NULL UNIQUE,
    rule_name           VARCHAR(100) NOT NULL,
    prefix              VARCHAR(10)  NOT NULL,
    date_format         VARCHAR(20)  NOT NULL DEFAULT 'yyyyMMdd',
    sequence_length     INT          NOT NULL DEFAULT 4,
    separator           VARCHAR(2)   NOT NULL DEFAULT '-',
    increment           INT          NOT NULL DEFAULT 1,
    reset_cycle         VARCHAR(20)  NOT NULL DEFAULT 'Daily',
    max_value           BIGINT       NOT NULL DEFAULT 9999999,
    enabled             CHAR(1)      NOT NULL DEFAULT '1',
    description         VARCHAR(500),
    remark              VARCHAR(500),

    created_by          VARCHAR(50)  NOT NULL,
    created_at          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by          VARCHAR(50),
    updated_at          TIMESTAMP,
    version             INT          DEFAULT 0,
    deleted             CHAR(1)      DEFAULT '0',

    CONSTRAINT chk_dnrr_seq_length CHECK (sequence_length BETWEEN 1 AND 10),
    CONSTRAINT chk_dnrr_increment CHECK (increment >= 1),
    CONSTRAINT chk_dnrr_reset_cycle CHECK (reset_cycle IN ('None', 'Daily', 'Monthly', 'Yearly')),
    CONSTRAINT chk_dnrr_enabled CHECK (enabled IN ('0', '1'))
);

COMMENT ON TABLE tms_doc_number_rules_t IS '单据编号规则表';
COMMENT ON COLUMN tms_doc_number_rules_t.date_format IS 'Java风格日期格式,支持 yyyyMMdd / yyyy-MM-dd / yyMM';
```

### 4.6 tms_doc_sequences_t (单据序号,乐观锁)

```sql
CREATE TABLE tms_doc_sequences_t (
    id                  BIGSERIAL PRIMARY KEY,
    rule_code           VARCHAR(50)  NOT NULL,
    period_key          VARCHAR(20)  NOT NULL DEFAULT 'NONE',
    current_value       BIGINT       NOT NULL DEFAULT 0,
    version             INT          NOT NULL DEFAULT 0,
    updated_at          TIMESTAMP,

    CONSTRAINT uq_doc_seq_rule_period UNIQUE (rule_code, period_key)
);

CREATE INDEX idx_doc_seq_rule_code ON tms_doc_sequences_t(rule_code);

COMMENT ON TABLE tms_doc_sequences_t IS '单据序号表,按周期隔离';
COMMENT ON COLUMN tms_doc_sequences_t.period_key IS '周期标识: None→NONE / Daily→yyyyMMdd / Monthly→yyyyMM / Yearly→yyyy';
```

### 4.7 tms_system_constants_t (P1 系统常量)

```sql
CREATE TABLE tms_system_constants_t (
    id                  BIGSERIAL PRIMARY KEY,
    const_code          VARCHAR(100) NOT NULL UNIQUE,
    const_value         TEXT         NOT NULL,
    const_type          VARCHAR(20)  NOT NULL DEFAULT 'STRING',
    description         VARCHAR(500),
    remark              VARCHAR(500),

    created_by          VARCHAR(50)  NOT NULL,
    created_at          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by          VARCHAR(50),
    updated_at          TIMESTAMP,
    version             INT          DEFAULT 0,
    deleted             CHAR(1)      DEFAULT '0',

    CONSTRAINT chk_sys_const_type CHECK (const_type IN ('STRING', 'NUMBER', 'BOOLEAN', 'JSON'))
);

COMMENT ON TABLE tms_system_constants_t IS '系统常量表,程序内部使用';
```

### 4.8 tms_notifications_t (P1 通知表)

```sql
CREATE TABLE tms_notifications_t (
    id                  BIGSERIAL PRIMARY KEY,
    user_id             BIGINT       NOT NULL,
    type                VARCHAR(20)  NOT NULL,
    title               VARCHAR(200) NOT NULL,
    content             TEXT         NOT NULL,
    link                VARCHAR(500),
    priority            VARCHAR(10)  NOT NULL DEFAULT 'NORMAL',
    read_status         CHAR(1)      NOT NULL DEFAULT '0',
    read_at             TIMESTAMP,
    expire_at           TIMESTAMP,
    created_at          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_notif_type CHECK (type IN ('announcement', 'approval_request', 'todo', 'alert', 'task_result')),
    CONSTRAINT chk_notif_priority CHECK (priority IN ('LOW', 'NORMAL', 'HIGH', 'URGENT')),
    CONSTRAINT chk_notif_read_status CHECK (read_status IN ('0', '1'))
);

CREATE INDEX idx_notif_user_id ON tms_notifications_t(user_id);
CREATE INDEX idx_notif_read_status ON tms_notifications_t(read_status);
CREATE INDEX idx_notif_created_at ON tms_notifications_t(created_at);

COMMENT ON TABLE tms_notifications_t IS '站内通知表';
```

### 4.9 tms_scheduled_jobs_t (P1 定时任务)

```sql
CREATE TABLE tms_scheduled_jobs_t (
    id                  BIGSERIAL PRIMARY KEY,
    job_code            VARCHAR(50)  NOT NULL UNIQUE,
    job_name            VARCHAR(100) NOT NULL,
    job_class           VARCHAR(200) NOT NULL,
    cron_expression     VARCHAR(50)  NOT NULL,
    job_type            VARCHAR(20)  NOT NULL DEFAULT 'SCHEDULED',
    enabled             CHAR(1)      NOT NULL DEFAULT '1',
    description         VARCHAR(500),
    last_run_at         TIMESTAMP,
    last_run_status     VARCHAR(20),
    next_run_at         TIMESTAMP,
    remark              VARCHAR(500),

    created_by          VARCHAR(50)  NOT NULL,
    created_at          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by          VARCHAR(50),
    updated_at          TIMESTAMP,
    version             INT          DEFAULT 0,
    deleted             CHAR(1)      DEFAULT '0',

    CONSTRAINT chk_sj_type CHECK (job_type IN ('SCHEDULED', 'QUARTZ')),
    CONSTRAINT chk_sj_enabled CHECK (enabled IN ('0', '1'))
);

CREATE TABLE tms_scheduled_job_logs_t (
    id                  BIGSERIAL PRIMARY KEY,
    job_code            VARCHAR(50)  NOT NULL,
    start_time          TIMESTAMP    NOT NULL,
    end_time            TIMESTAMP,
    status              VARCHAR(20)  NOT NULL,
    duration_ms         BIGINT,
    error_message       TEXT,
    created_at          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_sjl_status CHECK (status IN ('RUNNING', 'SUCCESS', 'FAILED', 'CANCELLED'))
);

CREATE INDEX idx_sjl_job_code ON tms_scheduled_job_logs_t(job_code);
CREATE INDEX idx_sjl_start_time ON tms_scheduled_job_logs_t(start_time);

COMMENT ON TABLE tms_scheduled_jobs_t IS '定时任务定义表';
COMMENT ON TABLE tms_scheduled_job_logs_t IS '定时任务执行历史';
```

---

## 五、业务规则 (R1 ~ R17)

| # | 规则编码 | 规则描述 |
|---|---------|----------|
| **R1** | 字典类型唯一 | `tms_dict_types_t.dict_type_code` 全局唯一,删除使用软删除 (`deleted='1'`) |
| **R2** | 字典项唯一 | 同一 `dict_type_code` 下 `item_code` 唯一 (考虑软删除后的 UNIQUE 约束) |
| **R3** | 编号规则唯一 | `tms_doc_number_rules_t.rule_code` 全局唯一 |
| **R4** | 序号不超限 | `current_value + increment <= max_value`,超限抛 `SequenceExhaustedException` |
| **R5** | 配置键唯一 | `tms_sys_configs_t.config_key` 全局唯一 |
| **R6** | value_type 校验 | 配置项 `config_value` 必须与 `value_type` 类型一致 (STRING=任意/NUMBER=数字/BOOLEAN=true|false/JSON=合法 JSON) |
| **R7** | 栏目 path 格式 | 栏目 `path` 必须以 `/` 开头,不含空格 |
| **R8** | 删除栏目检查 | 删除栏目前必须无子栏目 (`SELECT COUNT(*) FROM tms_menus_t WHERE parent_id = #{id} AND deleted='0'`),有则拒绝 |
| **R9** | 重置周期逻辑 | `reset_cycle = 'None'` 时序号不重置,`period_key='NONE'`;其他周期按规则生成 `period_key` |
| **R10** | 配置缓存 TTL | 系统参数缓存 TTL = 60s (Caffeine `expireAfterWrite=60s`),变更后主动失效 |
| **R11** | 栏目层级上限 | 栏目树深度最多 3 级,新增子栏目时 `parent_id` 对应栏目已有 2 级祖先则拒绝 |
| **R12** | 编号生成串行 | 单规则的序号更新通过乐观锁串行,失败重试 3 次,3 次后抛异常 |
| **R13** | 字典变更广播 | 字典项变更后,通过 Spring `ApplicationEvent` 发布 `DictChangeEvent`,所有节点清除本地缓存 (单实例启动无需考虑,多实例后续 Redis 订阅) |
| **R14** | 集成配置加密 | `category='INTEGRATION'` 的配置 `encrypted='1'`,`config_value` AES-256 加密存储,接口返回时解密 |
| **R15** | 系统参数只读 | `editable='0'` 的配置项接口层拒绝修改 (返回 400) |
| **R16** | 通知接收人 | 通知 `user_id` 必须存在 (本期不强制 FK,后台校验) |
| **R17** | 任务手动触发 | `enabled='0'` 的任务**不可手动触发**,返回 400 |

---

## 六、与现有模块的集成

### 6.1 替代 App.vue 静态菜单

**现状**: `web/src/App.vue` 第 5-42 行硬编码菜单,`router/index.js` 第 5-63 行静态路由。

**改造方案**:

1. **Phase 3**: 前端保留 `router/index.js` 但只注册框架路由 (登录/404),业务路由从后端拉取
2. App.vue 改为动态渲染菜单:

```javascript
// 改造后 App.vue (伪代码)
const menuTree = ref([])
onMounted(async () => {
  const { data } = await getUserMenus()  // GET /api/v1/menus/user
  menuTree.value = data
})
```

3. 后端 `GET /api/v1/menus/user` 流程:
   - 获取当前用户 ID
   - 查询用户权限 (从权限系统)
   - 查询栏目树 (`tms_menus_t`),过滤 `permission_code IN (用户权限)`
   - 返回树形结构

### 6.2 替代硬编码枚举 (GlobalConstants 共存)

**现状**: 业务枚举散落各模块 `enums` 包,如 `dealing/enums/DealStatus`。

**共存策略**:

- 本期**新增字段/状态**必须先在字典中创建项,业务代码读字典
- 旧 `enums` 包保留,**Phase 5+ 逐步替换**
- 提供 `DictService.getByType(typeCode)` 替代 `DealStatus.values()`

**示例替换**:

```java
// 旧 (硬编码)
public enum DealStatus { New, Submitted, Approved, Settled }

// 新 (字典读取,保留 enum 作为默认值兜底)
public class DealStatusResolver {
    public static String getStatusName(String code) {
        DictItemVO item = DictService.getByCode("trade_status", code);
        return item != null ? item.getItemName() : DealStatus.valueOf(code).name();
    }
}
```

### 6.3 替代单据生成器的 if/else 逻辑

**现状**: 各业务模块自行 `String.format("%s%s-%04d", "AC", dateStr, seq)`,散落在 `DealService/AcDealService/FxDealService` 等多处。

**改造方案**:

```java
// 旧
String dealNo = "AC" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) +
                "-" + String.format("%04d", nextSeq);

// 新
String dealNo = docNumberService.generate("AC_DEAL");
```

**服务接口**: `DocNumberService.generate(String ruleCode) -> String`

**实施步骤**:

1. 创建 `tms_doc_number_rules_t` + `tms_doc_sequences_t` 初始化 10 条规则
2. 创建 `common` 模块 `DocNumberService` (供所有业务模块调用)
3. Phase 2: 业务模块逐步迁移,本期**双写** (新服务 + 旧逻辑并存)
4. Phase 5+: 删除旧代码

### 6.4 所有业务模块查询状态/动作类型时改为字典

**前端**: `<dict-tag :type="DICT_TYPE.TRADE_STATUS" :value="row.status" />` 已部分使用,本期新增字典类型后,前端统一从 `GET /api/v1/dicts/{typeCode}` 拉取。

**后端**: 业务模块返回 `VO` 时,状态/类型字段直接用 `code` (字典项编码),前端根据字典展示名称和颜色。

---

## 七、业务流程

### 7.1 单据编号生成流程

```
[业务模块] DealService.save(dto)
       ↓
[业务模块] 生成 dealNo = docNumberService.generate("AC_DEAL")
       ↓
[DocNumberService]
       ↓ (1) 加载规则 (从缓存 ruleCache)
       ↓ (2) 计算 periodKey:
       │   - resetCycle=None → "NONE"
       │   - resetCycle=Daily → yyyyMMdd
       │   - resetCycle=Monthly → yyyyMM
       │   - resetCycle=Yearly → yyyy
       ↓ (3) 查询当前序号:
       │   SELECT current_value, version
       │   FROM tms_doc_sequences_t
       │   WHERE rule_code = ? AND period_key = ?
       ↓ (4) 乐观锁更新:
       │   UPDATE tms_doc_sequences_t
       │   SET current_value = current_value + increment,
       │       version = version + 1,
       │       updated_at = now()
       │   WHERE rule_code = ? AND period_key = ? AND version = ?
       ↓ (5) 若 affected=0:
       │   - 重试 (最多 3 次, 随机退避 10-50ms)
       │   - 3 次后抛 SequenceConcurrencyException
       ↓ (6) 拼接:
       │   prefix + formattedDate + separator + padLeft(seq, length, '0')
       │   例: "AC" + "20260705" + "-" + "0001" = "AC20260705-0001"
       ↓ (7) 校验 current_value <= max_value,否则抛 SequenceExhaustedException
       ↓ (8) 返回 dealNo
       ↓
[业务模块] dealMapper.insert(deal)
```

### 7.2 字典项缓存刷新流程

**启动流程**:

```
[ApplicationContext 启动]
       ↓
[CommandLineRunner - DictCacheLoader]
       ↓ (1) SELECT * FROM tms_dict_types_t WHERE deleted='0' AND status='1'
       ↓ (2) for each type:
       │       SELECT * FROM tms_dict_items_t WHERE dict_type_code=? AND deleted='0' AND status='1'
       │       存入 dictCache: ConcurrentHashMap<typeCode, List<DictItemVO>>
       ↓ (3) 缓存启动时间 (便于监控)
       ↓
[Application Ready]
```

**后台修改时**:

```
[Admin] POST /api/v1/admin/dict-items/update
       ↓
[DictItemService.update(dto)]
       ↓ (1) 更新 DB
       ↓ (2) 发布 DictChangeEvent (typeCode)
       ↓
[DictChangeListener]
       ↓ (3) 清除 dictCache 中该 typeCode 的条目
       ↓
[下次 DictService.getByType(typeCode) 调用]
       ↓ (4) 缓存 miss,回源 DB 查询并重载
```

**手动刷新**:

```
[Admin] POST /api/v1/admin/dicts/refresh-cache
       ↓
[DictService.refreshCache()]
       ↓ (1) 清空全部 dictCache
       ↓ (2) 重新执行启动加载逻辑
```

### 7.3 栏目动态加载流程

```
[用户] 浏览器访问 /dealing/ac-deal
       ↓
[Frontend] router.beforeEach 守卫
       ↓ (1) 检查本地缓存 (localStorage: userMenus)
       │   - 若无 → 调用 GET /api/v1/menus/user
       │   - 若有 → 直接渲染
       ↓ (2) 调用 /api/v1/menus/user (带 Authorization Header)
       ↓
[MenuController.userMenus]
       ↓ (3) 从 Token 解析 userId
       ↓ (4) PermissionService.getPermissions(userId)
       │   返回 List<String> permissionCodes
       ↓ (5) MenuService.getUserMenus(permissionCodes)
       │   SELECT * FROM tms_menus_t
       │   WHERE deleted='0' AND status='1' AND hidden='0'
       │     AND permission_code IN (...)
       │   ORDER BY sort_order ASC
       ↓ (6) 递归构建树 (parent_id 关联)
       ↓ (7) 返回 List<MenuTreeVO>
       ↓
[Frontend] 渲染左侧菜单 + 注册动态路由
       ↓
[用户] 看到根据权限过滤后的菜单
```

---

## 八、验收标准 (35 条)

### 栏目管理 (5 条)

1. **AC1**: 栏目支持 CRUD,字段校验完整 (menu_code 唯一,path 以 / 开头)
2. **AC2**: 栏目树支持折叠展开,最多 3 级,深度超限返回 400
3. **AC3**: 删除栏目前检查子栏目,有子栏目时返回 400 + 提示"存在子栏目,无法删除"
4. **AC4**: 栏目权限过滤生效:用户无 `basedata:currency:view` 权限时,菜单不显示"币种管理"
5. **AC5**: 栏目修改后,缓存 10min 内主动失效,前端下次访问时拉取最新

### 数据字典 (6 条)

6. **AC6**: 字典分类 CRUD,`dict_type_code` 全局唯一
7. **AC7**: 字典项 CRUD,同 type 内 `item_code` 唯一
8. **AC8**: 字典缓存启动时加载,后续 `GET /api/v1/dicts/trade_status` 返回内存数据
9. **AC9**: 字典项更新后,缓存自动失效,下次调用重新加载
10. **AC10**: 字典批量导入支持 Excel,返回成功/失败条数
11. **AC11**: 字典接口返回数据按 `sort_order` 升序,只返回 `status='1'` 项

### 系统参数 (4 条)

12. **AC12**: 系统参数 CRUD,`config_key` 唯一
13. **AC13**: 配置值与 `value_type` 不一致时返回 400
14. **AC14**: 集成类配置 `config_value` 加密存储 (DB 中是密文,接口返回解密)
15. **AC15**: 配置缓存 60s 后自动失效,主动修改立即失效

### 单据编号 (5 条)

16. **AC16**: 编号规则 CRUD,`rule_code` 唯一
17. **AC17**: 编号生成正确:`AC` + `20260705` + `-` + `0001` = `AC20260705-0001`
18. **AC18**: 并发生成编号不重复 (10 并发线程生成 10 个 AC Deal,deal_no 全不同)
19. **AC19**: 重置周期生效:`reset_cycle=Daily`,跨日后 `current_value` 重置为 0
20. **AC20**: 序号达到 `max_value` 时抛 `SequenceExhaustedException`,接口返回 400

### 系统常量 (2 条)

21. **AC21**: 系统常量 CRUD,`const_code` 唯一
22. **AC22**: 业务模块可通过 `ConstantService.getString("state.deal.transition.submit")` 读取

### 通知中心 (4 条)

23. **AC23**: 通知发送:POST /api/v1/admin/notifications 写入 DB
24. **AC24**: 通知接收:GET /api/v1/admin/notifications/unread-count 返回未读数
25. **AC25**: 通知已读:POST /api/v1/admin/notifications/{id}/read 更新 `read_status='1'` 和 `read_at`
26. **AC26**: 通知类型过滤:GET /api/v1/admin/notifications/page?type=approval_request 只返回审批类型

### 定时任务 (4 条)

27. **AC27**: 定时任务列表 CRUD
28. **AC28**: 手动触发任务:`POST /api/v1/admin/scheduled-jobs/{id}/trigger` 立即执行
29. **AC29**: 任务执行历史查询:`GET /api/v1/admin/scheduled-jobs/{id}/logs?pageNo=1` 分页返回
30. **AC30**: 任务失败通知:执行失败时自动给 SUPER_ADMIN 发 `task_result` 通知

### 集成与缓存 (3 条)

31. **AC31**: 用户登录后调用 `/api/v1/menus/user` 返回权限过滤后的栏目树
32. **AC32**: 栏目权限变更后,用户**重新登录**才能看到新栏目 (本期不主动失效用户会话,后续 WebSocket 推送)
33. **AC33**: 字典修改后,前端下次访问时拉取最新字典 (axios 拦截器缓存 5min)

### 安全与审计 (2 条)

34. **AC34**: 后台接口 (`/api/v1/admin/*`) 需 `SUPER_ADMIN` 或 `CONFIG_ADMIN` 角色,无权限返回 403
35. **AC35**: 所有后台写操作记录到 `tms_audit_log_t`,包含操作人/IP/时间/前后值

---

## 九、接口需求

### 9.1 栏目管理接口

| 方法 | URL | 说明 | 角色 |
|------|-----|------|------|
| GET | `/api/v1/admin/menus/page` | 分页查询栏目 | MENU_ADMIN |
| GET | `/api/v1/admin/menus/{id}` | 栏目详情 | MENU_ADMIN |
| GET | `/api/v1/admin/menus/tree` | 栏目树 | MENU_ADMIN |
| POST | `/api/v1/admin/menus` | 新增栏目 | MENU_ADMIN |
| POST | `/api/v1/admin/menus/update` | 更新栏目 | MENU_ADMIN |
| POST | `/api/v1/admin/menus/delete/{id}` | 删除栏目 | MENU_ADMIN |
| POST | `/api/v1/admin/menus/refresh-cache` | 刷新栏目缓存 | MENU_ADMIN |
| **GET** | **`/api/v1/menus/user`** | **当前用户可见栏目 (业务接口)** | **登录用户** |

### 9.2 字典接口

| 方法 | URL | 说明 | 角色 |
|------|-----|------|------|
| GET | `/api/v1/admin/dict-types/page` | 分页查询字典分类 | CONFIG_ADMIN |
| GET | `/api/v1/admin/dict-types/{id}` | 字典分类详情 | CONFIG_ADMIN |
| POST | `/api/v1/admin/dict-types` | 新增字典分类 | CONFIG_ADMIN |
| POST | `/api/v1/admin/dict-types/update` | 更新字典分类 | CONFIG_ADMIN |
| POST | `/api/v1/admin/dict-types/delete/{id}` | 删除字典分类 | CONFIG_ADMIN |
| GET | `/api/v1/admin/dict-items/page` | 分页查询字典项 | CONFIG_ADMIN |
| GET | `/api/v1/admin/dict-items/{id}` | 字典项详情 | CONFIG_ADMIN |
| POST | `/api/v1/admin/dict-items` | 新增字典项 | CONFIG_ADMIN |
| POST | `/api/v1/admin/dict-items/update` | 更新字典项 | CONFIG_ADMIN |
| POST | `/api/v1/admin/dict-items/delete/{id}` | 删除字典项 | CONFIG_ADMIN |
| POST | `/api/v1/admin/dict-items/import` | Excel 批量导入 | CONFIG_ADMIN |
| POST | `/api/v1/admin/dicts/refresh-cache` | 刷新字典缓存 | CONFIG_ADMIN |
| **GET** | **`/api/v1/dicts/{typeCode}`** | **公共字典查询 (业务接口)** | **登录用户** |
| **GET** | **`/api/v1/dicts`** | **查询所有字典 (业务接口)** | **登录用户** |

### 9.3 系统参数接口

| 方法 | URL | 说明 | 角色 |
|------|-----|------|------|
| GET | `/api/v1/admin/sys-configs/page` | 分页查询 | CONFIG_ADMIN |
| GET | `/api/v1/admin/sys-configs/{id}` | 详情 | CONFIG_ADMIN |
| GET | `/api/v1/admin/sys-configs/by-key/{key}` | 按 key 查询 | CONFIG_ADMIN |
| POST | `/api/v1/admin/sys-configs` | 新增 | CONFIG_ADMIN |
| POST | `/api/v1/admin/sys-configs/update` | 更新 | CONFIG_ADMIN |
| POST | `/api/v1/admin/sys-configs/delete/{id}` | 删除 | CONFIG_ADMIN |
| POST | `/api/v1/admin/sys-configs/refresh-cache` | 刷新缓存 | CONFIG_ADMIN |
| **GET** | **`/api/v1/configs/{key}`** | **公共参数查询 (业务接口)** | **登录用户** |

### 9.4 单据编号规则接口

| 方法 | URL | 说明 | 角色 |
|------|-----|------|------|
| GET | `/api/v1/admin/doc-num-rules/page` | 分页查询 | CONFIG_ADMIN |
| GET | `/api/v1/admin/doc-num-rules/{id}` | 详情 | CONFIG_ADMIN |
| POST | `/api/v1/admin/doc-num-rules` | 新增 | CONFIG_ADMIN |
| POST | `/api/v1/admin/doc-num-rules/update` | 更新 | CONFIG_ADMIN |
| POST | `/api/v1/admin/doc-num-rules/delete/{id}` | 删除 | CONFIG_ADMIN |
| GET | `/api/v1/admin/doc-num-rules/{code}/preview` | 预览下一个编号 | CONFIG_ADMIN |
| GET | `/api/v1/admin/doc-num-rules/{code}/sequences` | 查询序号历史 | CONFIG_ADMIN |
| **内部** | **`DocNumberService.generate(ruleCode)`** | **生成编号 (Java API)** | **-** |

### 9.5 系统常量接口 (P1)

| 方法 | URL | 说明 | 角色 |
|------|-----|------|------|
| GET | `/api/v1/admin/sys-constants/page` | 分页查询 | CONFIG_ADMIN |
| POST | `/api/v1/admin/sys-constants` | 新增 | CONFIG_ADMIN |
| POST | `/api/v1/admin/sys-constants/update` | 更新 | CONFIG_ADMIN |
| POST | `/api/v1/admin/sys-constants/delete/{id}` | 删除 | CONFIG_ADMIN |
| **GET** | **`/api/v1/constants/{code}`** | **公共常量查询** | **登录用户** |

### 9.6 通知接口 (P1)

| 方法 | URL | 说明 | 角色 |
|------|-----|------|------|
| GET | `/api/v1/admin/notifications/page` | 分页查询通知 | 登录用户 |
| GET | `/api/v1/admin/notifications/unread-count` | 未读数 | 登录用户 |
| GET | `/api/v1/admin/notifications/{id}` | 通知详情 | 登录用户 |
| POST | `/api/v1/admin/notifications/{id}/read` | 标记已读 | 登录用户 |
| POST | `/api/v1/admin/notifications/read-all` | 全部已读 | 登录用户 |
| POST | `/api/v1/admin/notifications/send` | 发送通知 (管理员) | CONFIG_ADMIN |
| **WebSocket** | **`/ws/notifications/{userId}`** | **实时推送** | **登录用户** |

### 9.7 定时任务接口 (P1)

| 方法 | URL | 说明 | 角色 |
|------|-----|------|------|
| GET | `/api/v1/admin/scheduled-jobs/page` | 分页查询 | CONFIG_ADMIN |
| GET | `/api/v1/admin/scheduled-jobs/{id}` | 任务详情 | CONFIG_ADMIN |
| POST | `/api/v1/admin/scheduled-jobs` | 新增任务 | CONFIG_ADMIN |
| POST | `/api/v1/admin/scheduled-jobs/update` | 更新任务 | CONFIG_ADMIN |
| POST | `/api/v1/admin/scheduled-jobs/delete/{id}` | 删除任务 | CONFIG_ADMIN |
| POST | `/api/v1/admin/scheduled-jobs/{id}/trigger` | 手动触发 | CONFIG_ADMIN |
| POST | `/api/v1/admin/scheduled-jobs/{id}/enable` | 启用 | CONFIG_ADMIN |
| POST | `/api/v1/admin/scheduled-jobs/{id}/disable` | 禁用 | CONFIG_ADMIN |
| GET | `/api/v1/admin/scheduled-jobs/{id}/logs` | 执行历史 | CONFIG_ADMIN |

**接口总数**: 47 个 (其中 admin 端 38 个,公共查询 7 个,WebSocket 1 个,内部 API 1 个)

---

## 十、不在本期范围

| 范围 | 说明 | 后续 |
|------|------|------|
| 文件存储管理 | 附件上传、文件管理 | P2 (独立模块 `file-storage`) |
| 邮件推送 | SMTP 集成 | P2 |
| 短信推送 | 短信网关集成 | P2 |
| APP Push | 移动端推送 | P2 |
| 多语言字典 | i18n_key 实际生效 | P2 (前端集成 vue-i18n) |
| 任务调度高可用 | 分布式 Quartz | P2 (集群部署后) |
| Redis 分布式锁 | 替换乐观锁 | P2 (高并发场景) |
| 全文检索字典 | tms_dict_items_t 全文索引 | P2 |
| 工作流集成 | 与 approval 模块打通 | Phase 4 |
| 数据导入导出通用组件 | Excel/CSV 通用导入器 | P1 (基于现有 init_basedata.py) |

---

## 十一、实施阶段

### Phase 1: 字典 + 系统参数 (核心, 业务模块都需要)

**工期**: 1.5 周

**任务清单**:
1. 创建 `admin` Maven 模块 (端口 8098)
2. 建表 `tms_dict_types_t` / `tms_dict_items_t` / `tms_sys_configs_t`
3. 实现 `DictService` / `ConfigService` + Caffeine 缓存
4. 实现 CRUD Controller (8 + 7 个接口)
5. 初始化 11 个核心字典类型 (trade_status/action_type/...)
6. 初始化 20+ 条业务参数 (审批阈值/汇率频率/批处理上限)
7. 前端字典管理 UI + 参数管理 UI
8. 公共字典查询接口 `GET /api/v1/dicts/*`
9. API + UI 测试用例 (test_admin_dict_api.py / test_admin_config_ui.py)

**依赖**: 无 (前置)

### Phase 2: 单据编号规则 (替换硬编码)

**工期**: 1 周

**任务清单**:
1. 建表 `tms_doc_number_rules_t` / `tms_doc_sequences_t`
2. 实现 `DocNumberService` (乐观锁 + 重试)
3. 初始化 10 条规则 (AC/AT/FX/Action/DealMap/Cashflow/...)
4. 实现 CRUD Controller + preview 接口
5. dealing 模块迁移: `AcDealService/AtDealService/FxDealService` 改为调用 `docNumberService.generate(...)`
6. 双写验证: 新旧逻辑并存,数据一致性检查
7. 测试: 并发 100 线程生成编号,验证唯一性

**依赖**: Phase 1 (无强制依赖,但建议先做)

### Phase 3: 栏目管理 (替换静态菜单)

**工期**: 1.5 周

**任务清单**:
1. 建表 `tms_menus_t`
2. 实现 `MenuService` + 缓存
3. 实现 CRUD Controller
4. 初始化栏目数据: 把现有 `App.vue` 菜单 + `router/index.js` 路由导入 `tms_menus_t` (~30 条记录)
5. 实现 `GET /api/v1/menus/user` (权限过滤)
6. 前端 `App.vue` 重构为动态菜单
7. 前端 `router/index.js` 改造: 静态 + 动态路由合并
8. 与权限系统对接: `permission_code` 字段映射

**依赖**: Phase 1

### Phase 4: 通知中心 + 定时任务

**工期**: 2 周

**任务清单**:
1. 建表 `tms_notifications_t` + `tms_scheduled_jobs_t` + `tms_scheduled_job_logs_t`
2. 实现 `NotificationService` + WebSocket (`/ws/notifications/{userId}`)
3. 实现 `ScheduledJobService` + Spring `@Scheduled` 集成
4. 初始化 6 条内置任务 (INTEREST_ACCRUAL/EOD_BATCH/...)
5. 实现 CRUD Controller (通知 6 + 任务 8 个接口)
6. 前端: 通知铃铛 (header) + 通知中心页面
7. 前端: 定时任务管理页面 + 启停 + 触发 + 日志
8. approval 模块接入: 审批结果推送通知
9. 任务失败通知: 自动给 SUPER_ADMIN

**依赖**: Phase 1

### Phase 5: 系统常量 (轻量, P1)

**工期**: 0.5 周

**任务清单**:
1. 建表 `tms_system_constants_t`
2. 实现 `ConstantService` + 缓存
3. CRUD Controller
4. 初始化示例常量 (state_machine 规则)
5. 提供 Java API `ConstantService.getString(code)`

**依赖**: Phase 1

### 总工期汇总

| Phase | 内容 | 工期 | 累计 |
|-------|------|------|------|
| Phase 1 | 字典 + 参数 | 1.5 周 | 1.5 周 |
| Phase 2 | 编号规则 | 1.0 周 | 2.5 周 |
| Phase 3 | 栏目管理 | 1.5 周 | 4.0 周 |
| Phase 4 | 通知 + 任务 | 2.0 周 | 6.0 周 |
| Phase 5 | 系统常量 | 0.5 周 | 6.5 周 |

---

## 十二、与用户权限系统的关系

### 12.1 栏目管理依赖权限系统

**依赖关系**: `tms_menus_t.permission_code` 对应权限系统的权限项 (`tms_permissions_t.permission_code`)。

**集成方式**:

1. 权限系统在 `tms_permissions_t` 中维护所有权限码
2. admin 模块的栏目**初始化数据**时,需先确认 `permission_code` 已存在于权限系统
3. 后续栏目新增时,运维人员**手动**确认权限码 (本期不做自动同步,P2 改进)

**数据流**:

```
[用户登录] → [权限系统] 返回权限码列表
                          ↓
[admin/menus/user] → [过滤 tms_menus_t WHERE permission_code IN (权限列表)]
                          ↓
[返回菜单树]
```

### 12.2 后台管理操作权限

| 操作 | 必需角色 |
|------|----------|
| 栏目 CRUD | SUPER_ADMIN / MENU_ADMIN |
| 字典 CRUD | SUPER_ADMIN / CONFIG_ADMIN |
| 参数 CRUD | SUPER_ADMIN / CONFIG_ADMIN |
| 编号规则 CRUD | SUPER_ADMIN / CONFIG_ADMIN |
| 系统常量 CRUD | SUPER_ADMIN / CONFIG_ADMIN |
| 通知发送 | SUPER_ADMIN / CONFIG_ADMIN |
| 定时任务 CRUD | SUPER_ADMIN / CONFIG_ADMIN |
| 公共字典/参数查询 | 任意登录用户 |

**实现方式**:

```java
@SaCheckLogin
@SaCheckRole("SUPER_ADMIN")
@PostMapping("/api/v1/admin/menus")
public Result<MenuVO> save(@RequestBody @Valid MenuDTO dto) { ... }
```

### 12.3 操作审计

所有后台写操作通过 `AuditAspect` 切面记录到 `tms_audit_log_t`,字段:

| 字段 | 来源 |
|------|------|
| `table_name` | `tms_menus_t` / `tms_dict_types_t` / ... |
| `record_id` | 操作记录 ID |
| `operation_type` | CREATE / UPDATE / DELETE |
| `operation_user` | 当前登录用户 (从 Token 解析) |
| `operation_time` | LocalDateTime.now() |
| `before_value` / `after_value` | JSON 序列化 |
| `ip_address` | HttpServletRequest.getRemoteAddr() |
| `remark` | 可选备注 |

---

## 十三、关键设计决策

### D1: 与 GlobalConstants 共存,不强制替换

**原因**: 现有 `GlobalConstants.java` 已在 M1/M2 大量使用,本期强制替换工作量大且收益有限。

**决策**: 新模块读字典,旧模块保留 `enums` 兜底,Phase 5+ 渐进式迁移。

### D2: 编号规则序号独立表,1:N 分离

**原因**: 规则表存"模板",序号表存"运行时数据",`reset_cycle=Daily` 时按 `period_key=yyyyMMdd` 隔离,1 个规则有 N 个序号记录。

**决策**: `tms_doc_number_rules_t` (模板) + `tms_doc_sequences_t` (运行数据,按周期)。

### D3: 栏目树深度上限 3 级

**原因**: Open-TMS 模块层级有限 (管理驾驶舱/基础数据/交易管理/...),3 级足够覆盖。

**决策**: 业务规则 R11,深度超限拒绝新增。

### D4: 字典与配置分离

**原因**: 字典面向前端 UI,配置面向后端逻辑;字典有 `color` / `i18n_key` 等展示字段,配置有 `encrypted` / `editable` 等逻辑字段。

**决策**: 两表独立,通过不同服务访问 (`DictService` vs `ConfigService`)。

### D5: 通知 WebSocket 推送 + DB 兜底

**原因**: WebSocket 实时但可能丢失,DB 持久化保证不丢。

**决策**: 写入 DB → 尝试 WebSocket 推送 → 失败下次登录拉取未读。

### D6: 定时任务双引擎 (Scheduled + Quartz)

**原因**: 简单任务用 `@Scheduled` 轻量,复杂任务 (动态启停/参数) 用 Quartz。

**决策**: `tms_scheduled_jobs_t.job_type` 区分 `SCHEDULED` / `QUARTZ`。

### D7: 集成配置加密 (AES-256),其他明文

**原因**: 集成密钥需加密,业务参数 (如审批阈值) 加密会增加调试难度。

**决策**: 仅 `category='INTEGRATION'` 且 `encrypted='1'` 时加密。

### D8: 缓存 TTL 60s + 主动失效

**原因**: 60s 容忍一定延迟,主动失效保证关键变更即时生效。

**决策**: Caffeine `expireAfterWrite=60s` + Spring `ApplicationEvent` 主动清除。

---

## 十四、与现有系统的差异点

| 维度 | 现有做法 | 本期方案 | 差异原因 |
|------|----------|----------|----------|
| 菜单 | `App.vue` 静态 + `router/index.js` 静态 | 数据库动态 + 权限过滤 | 新增模块无需前端重新发布 |
| 字典 | 各模块 `enums` 包硬编码 | 数据库 + 启动加载 + 公共接口 | 业务调整无需发版 |
| 配置 | 散落代码 + `application.yml` | 数据库 + 60s 缓存 | 调参无需重启 |
| 编号 | `String.format` 散落 | `DocNumberService` 集中生成 | 并发安全 + 规则可配 |
| 通知 | 无 | DB + WebSocket | 用户体验 |
| 定时任务 | `@Scheduled` 注解硬编码 | DB + 管理界面 | 可观测可管控 |
| 审计 | 部分模块手动记录 | `AuditAspect` 切面统一 | 全量覆盖 |

---

## 十五、后续 P1/P2 增强方向

### P1 (版本内规划,M3 阶段)

- 多语言字典生效 (`vue-i18n` 集成)
- 字典全文检索 (PostgreSQL GIN 索引 on `item_name`)
- Excel 通用导入导出组件 (基于现有 `init_basedata.py` 经验)
- 通知批量推送优化 (消息队列)
- 定时任务执行时长监控 + 超时告警

### P2 (后续版本,M4+)

- 文件存储模块独立 (`file-storage` 模块)
- 邮件/SMS 通道集成
- 分布式 Redis 锁替代乐观锁 (高并发 QPS>100 场景)
- Quartz 分布式调度 (集群部署)
- 字典版本管理 (变更审计 + 回滚)
- 栏目拖拽排序 (前端 UI 改进)
- 通知模板引擎 (参数化消息)
- 通知聚合 (同类通知合并展示)
- 配置变更影响范围分析 (依赖图)

---

## 十六、附录

### 附录 A: 数据字典初始化清单

```sql
-- 11 个核心字典类型初始化
INSERT INTO tms_dict_types_t (dict_type_code, dict_type_name, description, created_by) VALUES
('trade_status', '交易状态', '交易单据状态流转', 'system'),
('action_type', '动作类型', 'Action 动作类型', 'system'),
('deal_type', '交易类型', 'Deal 类型 AC/AT/FX', 'system'),
('instrument_type', '金融工具类型', '金融工具分类', 'system'),
('image_type', '影像类型', 'CREATE/UPDATE/DELETE', 'system'),
('approval_status', '审批状态', 'Pending/Approved/Rejected', 'system'),
('direction', '资金方向', 'Inflow/Outflow', 'system'),
('cf_type', '现金流类型', 'Principal/Interest/Fee', 'system'),
('reset_cycle', '编号重置周期', 'None/Daily/Monthly/Yearly', 'system'),
('job_status', '任务状态', '任务执行结果', 'system'),
('menu_category', '栏目类型', 'MENU/BUTTON', 'system');

-- 字典项示例 (trade_status)
INSERT INTO tms_dict_items_t (dict_type_code, item_code, item_name, sort_order, color, created_by) VALUES
('trade_status', 'New', '新建', 1, 'info', 'system'),
('trade_status', 'Submitted', '已提交', 2, 'warning', 'system'),
('trade_status', 'Approved', '已审批', 3, 'success', 'system'),
('trade_status', 'Settled', '已结算', 4, 'success', 'system'),
('trade_status', 'Rejected', '已驳回', 5, 'danger', 'system'),
('trade_status', 'Canceled', '已撤销', 6, 'danger', 'system');
```

### 附录 B: 系统参数初始化清单

```sql
-- 系统级
INSERT INTO tms_sys_configs_t (config_key, config_value, value_type, category, description, editable, created_by) VALUES
('system.name', 'Open-TMS', 'STRING', 'SYSTEM', '系统名称', '1', 'system'),
('system.theme.color', '#409EFF', 'STRING', 'SYSTEM', '主题色', '1', 'system');

-- 业务级
INSERT INTO tms_sys_configs_t (config_key, config_value, value_type, category, description, editable, created_by) VALUES
('biz.approval.threshold.amount', '100000.00', 'NUMBER', 'BUSINESS', '免审批金额上限 (CNY)', '1', 'system'),
('biz.rate.refresh.frequency.min', '60', 'NUMBER', 'BUSINESS', '汇率刷新频率 (分钟)', '1', 'system'),
('biz.batch.max.size', '1000', 'NUMBER', 'BUSINESS', '批处理最大记录数', '1', 'system'),
('biz.deal.idempotency.ttl.hours', '24', 'NUMBER', 'BUSINESS', '交易幂等键过期 (小时)', '1', 'system'),
('biz.limit.warning.threshold.pct', '80', 'NUMBER', 'BUSINESS', '限额预警阈值 (%)', '1', 'system');

-- 集成级 (加密)
INSERT INTO tms_sys_configs_t (config_key, config_value, value_type, category, description, editable, encrypted, created_by) VALUES
('integ.bloomberg.api.key', 'PLACEHOLDER_ENCRYPTED', 'STRING', 'INTEGRATION', 'Bloomberg API 密钥', '1', '1', 'system'),
('integ.swift.endpoint.url', 'https://swift.example.com', 'STRING', 'INTEGRATION', 'SWIFT 网关', '1', '0', 'system');
```

### 附录 C: 编号规则初始化清单

```sql
INSERT INTO tms_doc_number_rules_t
(rule_code, rule_name, prefix, date_format, sequence_length, separator, increment, reset_cycle, max_value, created_by) VALUES
('AC_DEAL', 'AC 交易编号', 'AC', 'yyyyMMdd', 4, '-', 1, 'Daily', 9999, 'system'),
('AT_DEAL', 'AT 转账编号', 'AT', 'yyyyMMdd', 4, '-', 1, 'Daily', 9999, 'system'),
('FX_DEAL', 'FX 外汇编号', 'FX', 'yyyyMMdd', 4, '-', 1, 'Daily', 9999, 'system'),
('ACTION', 'Action 编号', 'ACT', 'yyyyMMdd', 6, '', 1, 'Daily', 999999, 'system'),
('DEALMAP', 'DealMap 编号', 'DMP', 'yyyyMMdd', 6, '', 1, 'Daily', 999999, 'system'),
('CASHFLOW', '现金流编号', 'CF', 'yyyyMMdd', 6, '', 1, 'Daily', 999999, 'system'),
('APPROVAL_TASK', '审批任务编号', 'APT', 'yyyyMMdd', 4, '-', 1, 'Daily', 9999, 'system'),
('DEAL_IMAGE', '影像编号', 'IMG', 'yyyyMMdd', 6, '', 1, 'Daily', 999999, 'system'),
('INSTRUMENT', '金融工具编号', 'INS', '', 6, '', 1, 'None', 999999, 'system'),
('TRADER', '交易员编号', 'TR', '', 4, '', 1, 'None', 9999, 'system');
```

### 附录 D: Maven 模块结构

```
opentms-parent
└── admin/                              # ★ 新模块, 端口 8098
    ├── pom.xml
    └── src/main/java/com/opentms/admin/
        ├── AdminApplication.java
        ├── config/
        │   └── AdminCacheConfig.java   # Caffeine 配置
        ├── controller/
        │   ├── MenuController.java
        │   ├── DictTypeController.java
        │   ├── DictItemController.java
        │   ├── SysConfigController.java
        │   ├── DocNumRuleController.java
        │   ├── SystemConstantController.java (P1)
        │   ├── NotificationController.java (P1)
        │   └── ScheduledJobController.java (P1)
        ├── service/
        │   ├── MenuService.java
        │   ├── DictService.java
        │   ├── ConfigService.java
        │   ├── DocNumberService.java
        │   ├── SystemConstantService.java (P1)
        │   ├── NotificationService.java (P1)
        │   └── ScheduledJobService.java (P1)
        ├── mapper/
        ├── entity/
        ├── dto/
        ├── vo/
        └── enums/
```

### 附录 E: 与 common 模块的关系

- 本模块依赖 `common` 模块 (Result, BaseEntity, GlobalConstants, Exception)
- 通用服务 `DocNumberService` / `ConfigService` / `DictService` 放在 `common` 模块供所有业务模块使用
- Controller/CRUD 放在 `admin` 模块

### 附录 F: 测试用例规划

| 测试类型 | 文件 | 用例数 |
|---------|------|--------|
| 字典 API | `scripts/test/test_admin_dict_api.py` | 20 |
| 参数 API | `scripts/test/test_admin_config_api.py` | 15 |
| 栏目 API | `scripts/test/test_admin_menu_api.py` | 15 |
| 编号 API | `scripts/test/test_admin_docnum_api.py` | 20 (含并发) |
| 通知 API | `scripts/test/test_admin_notify_api.py` | 10 |
| 字典 UI | `scripts/test/test_admin_dict_ui.py` | 8 |
| 参数 UI | `scripts/test/test_admin_config_ui.py` | 8 |
| 栏目 UI | `scripts/test/test_admin_menu_ui.py` | 8 |
| **合计** | | **104** |

---

*PM-Lead 产出 - Open-TMS Common 后台管理模块 PRD v1.0 - 2026-07-05*
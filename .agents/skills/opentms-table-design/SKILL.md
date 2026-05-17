---
name: opentms-table-design
description: Use when designing database tables for Open-TMS treasury management system, creating entity models, or reviewing data model designs
---

# Open-TMS 表结构设计规范

## Overview

本规范为 Open-TMS 企业资金管理系统提供数据库表结构设计指导。基于 PostgreSQL 特性、阿里巴巴开发规范和资金系统业务要求，确保数据模型满足高可用、可审计、幂等性的企业级标准。

## When to Use

**触发场景：**
- 设计新的业务表（交易表、账户表、限额表等）
- 审查现有数据库表结构
- 创建资金交易相关的实体模型
- 定义表之间的外键关系
- 设计分表策略或数据归档方案

**症状判断：**
- 需要新增数据表但不确定字段设计
- 字段类型选择困惑（DECIMAL精度、字符串长度）
- 外键约束是否应该启用
- 缺少审计字段或幂等设计
- 表之间关联关系不清晰

**不适用：**
- 简单的增删改查接口设计（见 opentms-api-design）
- 缓存数据结构设计
- 消息队列主题设计

---

## 一、命名规范

### 1.1 表命名

```sql
-- 格式: trm_{module}_{type}
-- module: 模块名 (basedata/dealing/cashpool/settlement/valuation等)
-- type: 表类型 (_t=主表, _d=字典, _log=日志, _rel=关联, _his=历史)

-- ✅ 正确示例
trm_deal_t              -- 交易主表
trm_currency_t          -- 币种表
trm_hedge_relation_t    -- 套保关联表
trm_audit_log_t         -- 审计日志表

-- ❌ 错误示例
deal                    -- 缺少前缀
deal_table              -- 冗余后缀
DealInfo                -- 大写+驼峰
```

### 1.2 字段命名

```sql
-- 通用字段（所有表必须包含）
id                  BIGSERIAL PRIMARY KEY
{entity}_code       VARCHAR(50) NOT NULL UNIQUE   -- 业务编码
{entity}_name       VARCHAR(200) NOT NULL         -- 业务名称

-- 审计字段
created_by          VARCHAR(50) NOT NULL
created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
updated_by          VARCHAR(50)
updated_at          TIMESTAMP
version             INT DEFAULT 0                 -- 乐观锁
deleted             CHAR(1) DEFAULT '0'           -- 软删除

-- 状态字段
status              CHAR(1) NOT NULL DEFAULT '1'  -- '1'=启用 '0'=禁用

-- 金额字段
amount              DECIMAL(18,2)                -- 一般金额
balance             DECIMAL(18,2)                -- 余额
rate                DECIMAL(18,8)                -- 汇率/利率高精度
interest_rate       DECIMAL(12,8)                -- 利率（衍生品需更高精度）
exchange_rate       DECIMAL(18,8)                -- 汇率

-- 日期字段
value_date          DATE NOT NULL                 -- 起息日
maturity_date       DATE NOT NULL                 -- 到期日
business_date       DATE NOT NULL                 -- 营业日
```

---

## 二、字段类型规范

### 2.1 数值类型

```sql
-- 金额类（固定2位小数）
amount          DECIMAL(18,2)    -- 常规金额（千万级）
large_amount    DECIMAL(24,2)    -- 大额资金（亿级）

-- 利率/汇率（高精度）
interest_rate   DECIMAL(12,8)    -- 利率（含衍生品）
exchange_rate   DECIMAL(18,8)    -- 汇率
discount_rate   DECIMAL(10,6)    -- 折现率

-- 百分比
percent         DECIMAL(5,2)     -- 0-100%
ratio           DECIMAL(8,4)     -- 比例（如套保比例）

-- 整数
quantity        INT              -- 数量
version         INT DEFAULT 0   -- 乐观锁版本
stage           INT DEFAULT 1   -- 减值阶段(1/2/3)

-- 布尔
is_master       CHAR(1) DEFAULT '0'  -- 主账户标识
is_adjacent     CHAR(1) DEFAULT '0'  -- 补休日
is_read         CHAR(1) DEFAULT '0'  -- 已读标记
```

### 2.2 字符串类型

```sql
-- 业务编码（唯一标识）
code             VARCHAR(50) NOT NULL UNIQUE

-- 名称类
name             VARCHAR(200)              -- 中文名
en_name          VARCHAR(200)              -- 英文名

-- 短代码
type             VARCHAR(20)               -- 类型编码
status           VARCHAR(20) DEFAULT 'DRAFT'  -- 状态

-- 长文本
remark           VARCHAR(500)              -- 备注
address          VARCHAR(500)               -- 地址
description      VARCHAR(1000)              -- 描述

-- 特殊字段
swift_code       VARCHAR(11)               -- SWIFT代码(11位)
account_no       VARCHAR(50)               -- 银行账号
phone            VARCHAR(30)               -- 电话
email            VARCHAR(100)              -- 邮箱
timezone         VARCHAR(50)               -- 时区

-- JSON/配置
config_json      TEXT                      -- JSON配置
report_data      TEXT                      -- 报表数据
widget_config    TEXT                      -- 组件配置
```

### 2.3 日期类型

```sql
-- 日期（不含时间）
holiday_date     DATE                      -- 节假日
value_date       DATE                      -- 起息日
maturity_date    DATE                      -- 到期日

-- 时间戳（带时区）
created_at       TIMESTAMP                 -- 创建时间
updated_at       TIMESTAMP                 -- 更新时间
operation_time   TIMESTAMP                 -- 操作时间

-- 特殊
business_date    DATE NOT NULL             -- 营业日（资金系统核心）
```

---

## 三、必需字段（企业级标准）

### 3.1 公共审计字段（所有表必须包含）

```sql
CREATE TABLE trm_template_t (
    id                  BIGSERIAL PRIMARY KEY,
    
    -- 审计字段（必需）
    created_by          VARCHAR(50) NOT NULL DEFAULT 'system',
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by          VARCHAR(50),
    updated_at          TIMESTAMP,
    version             INT DEFAULT 0,               -- 乐观锁，防止并发更新冲突
    deleted             CHAR(1) DEFAULT '0',         -- 软删除，数据可恢复
    
    -- 业务字段...
);
```

### 3.2 幂等性设计（交易表必需）

```sql
-- 资金交易表必须包含
CREATE TABLE trm_deal_t (
    id                  BIGSERIAL PRIMARY KEY,
    deal_no             VARCHAR(50) NOT NULL UNIQUE,  -- 业务流水号（幂等键）
    idempotency_key     VARCHAR(64),                  -- 外部幂等KEY（可选）
    
    -- 业务字段...
);

-- 幂等日志表（可选，用于防重复提交）
CREATE TABLE trm_idempotency_t (
    idempotency_key     VARCHAR(64) PRIMARY KEY,
    request_hash        VARCHAR(64),
    response_data       JSONB,
    expire_time         TIMESTAMP,
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### 3.3 审计日志表（必需）

```sql
-- 通用审计日志表
CREATE TABLE trm_audit_log_t (
    id                  BIGSERIAL PRIMARY KEY,
    table_name          VARCHAR(50) NOT NULL,
    record_id           BIGINT NOT NULL,
    operation_type      VARCHAR(20) NOT NULL,          -- CREATE/UPDATE/DELETE/SUBMIT/APPROVE
    operation_user      VARCHAR(50) NOT NULL,
    operation_time      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    before_value        JSONB,                         -- 变更前值
    after_value         JSONB,                         -- 变更后值
    ip_address          VARCHAR(50),
    remark              VARCHAR(500)
);

CREATE INDEX idx_audit_table ON trm_audit_log_t(table_name);
CREATE INDEX idx_audit_record ON trm_audit_log_t(record_id);
CREATE INDEX idx_audit_user ON trm_audit_log_t(operation_user);
CREATE INDEX idx_audit_time ON trm_audit_log_t(operation_time);
```

---

## 四、外键与索引

### 4.1 外键约束设计

```sql
-- ✅ 推荐：显式外键（数据一致性保证）
CREATE TABLE trm_deal_t (
    id                  BIGSERIAL PRIMARY KEY,
    counterparty_id    BIGINT NOT NULL,
    business_unit_id   BIGINT NOT NULL,
    
    CONSTRAINT fk_deal_counterparty FOREIGN KEY (counterparty_id) 
        REFERENCES trm_counterparty_t(id),
    CONSTRAINT fk_deal_business_unit FOREIGN KEY (business_unit_id) 
        REFERENCES trm_business_unit_t(id)
);

-- ⚠️ 谨慎：对于高频写入的表（如交易流水），可延迟外键约束
-- 在应用层保证数据一致性，数据库层仅做索引

-- ❌ 避免：循环依赖外键
-- A表引用B表，B表又引用A表
```

### 4.2 索引设计规范

```sql
-- 业务查询索引（必须创建）
CREATE INDEX idx_{table}_{column} ON {table}(column);

-- 复合索引（按查询频率排序，区分度高在前）
CREATE INDEX idx_deal_type_status ON trm_deal_t(deal_type, status);

-- 唯一索引（业务编码）
CREATE UNIQUE INDEX uidx_deal_no ON trm_deal_t(deal_no);

-- 部分索引（过滤高频条件）
CREATE INDEX idx_deal_status ON trm_deal_t(status) WHERE status != 'DELETED';

-- ❌ 避免：超过3个字段的复合索引
-- ❌ 避免：在低选择度字段上建索引（如性别）
```

---

## 五、PostgreSQL 特性应用

### 5.1 序列与自增

```sql
-- 方式一：BIGSERIAL（推荐用于单表）
id BIGSERIAL PRIMARY KEY

-- 方式二：UUID（分布式场景）
id UUID PRIMARY KEY DEFAULT gen_random_uuid()

-- 方式三：自定义序列（多表共享序列）
CREATE SEQUENCE trm_global_seq;
id BIGINT DEFAULT nextval('trm_global_seq')
```

### 5.2 JSONB 应用

```sql
-- 审计日志（存储变更前后值）
before_value    JSONB
after_value     JSONB

-- 配置扩展（动态字段）
config_json     JSONB

-- 案例：交易扩展字段
ALTER TABLE trm_deal_t ADD COLUMN ext_fields JSONB;
-- 存储: {"broker_fee": 0.002, "settlement_agent": "XXX", "references": [...]}
```

### 5.3 分区表（大数据量）

```sql
-- 按日期分区（交易流水表）
CREATE TABLE trm_deal_t (
    ...
) PARTITION BY RANGE (created_at);

CREATE TABLE trm_deal_202601 PARTITION OF trm_deal_t
    FOR VALUES FROM ('2026-01-01') TO ('2026-02-01');

CREATE TABLE trm_deal_202602 PARTITION OF trm_deal_t
    FOR VALUES FROM ('2026-02-01') TO ('2026-03-01');
```

### 5.4 约束应用

```sql
-- 状态枚举约束
CONSTRAINT chk_status CHECK (status IN ('DRAFT', 'PENDING', 'APPROVED', 'REJECTED', 'EXECUTED'))

-- 金额正数约束
CONSTRAINT chk_amount CHECK (amount > 0)

-- 百分比约束
CONSTRAINT chk_percent CHECK (percent >= 0 AND percent <= 100)

-- 日期逻辑约束
CONSTRAINT chk_date CHECK (maturity_date > value_date)
```

---

## 六、模块表设计模板

### 6.1 交易模块（dealing）

```sql
-- 交易主表
CREATE TABLE trm_deal_t (
    id                  BIGSERIAL PRIMARY KEY,
    deal_no             VARCHAR(50) NOT NULL UNIQUE,
    deal_type           VARCHAR(20) NOT NULL,          -- DEPOSIT/LOAN/FX/IRS等
    deal_subtype        VARCHAR(20),
    
    -- 关联字段
    instrument_id       BIGINT,                        -- 金融工具ID
    counterparty_id     BIGINT NOT NULL,              -- 交易对手
    business_unit_id    BIGINT NOT NULL,              -- 业务单元
    trader_id           BIGINT NOT NULL,              -- 交易员
    
    -- 金额字段
    amount              DECIMAL(18,2) NOT NULL,        -- 交易金额
    currency            VARCHAR(10) NOT NULL,         -- 币种
    
    -- 日期字段
    value_date          DATE NOT NULL,                -- 起息日
    maturity_date       DATE NOT NULL,                -- 到期日
    
    -- 利率（衍生品必需）
    interest_rate       DECIMAL(12,8),
    
    -- 状态与审计
    status              VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    idempotency_key     VARCHAR(64),                  -- 幂等KEY
    remark              VARCHAR(500),
    
    -- 公共字段
    created_by          VARCHAR(50) NOT NULL DEFAULT 'system',
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by          VARCHAR(50),
    updated_at          TIMESTAMP,
    version             INT DEFAULT 0,
    deleted             CHAR(1) DEFAULT '0'
);

CREATE INDEX idx_deal_no ON trm_deal_t(deal_no);
CREATE INDEX idx_deal_counterparty ON trm_deal_t(counterparty_id);
CREATE INDEX idx_deal_value_date ON trm_deal_t(value_date);
CREATE INDEX idx_deal_status ON trm_deal_t(status);
```

### 6.2 基础数据模块（basedata）

```sql
-- 业务单元表
CREATE TABLE trm_business_unit_t (
    id                  BIGSERIAL PRIMARY KEY,
    code                VARCHAR(50) NOT NULL UNIQUE,
    name                VARCHAR(200) NOT NULL,
    en_name             VARCHAR(200),
    legal_person        VARCHAR(50),                  -- 法人代表
    address             VARCHAR(500),
    tax_no              VARCHAR(50),                  -- 税务登记号
    
    status              CHAR(1) NOT NULL DEFAULT '1',
    created_by          VARCHAR(50) NOT NULL DEFAULT 'system',
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by          VARCHAR(50),
    updated_at          TIMESTAMP,
    version             INT DEFAULT 0,
    deleted             CHAR(1) DEFAULT '0'
);

-- 交易对手表
CREATE TABLE trm_counterparty_t (
    id                  BIGSERIAL PRIMARY KEY,
    code                VARCHAR(50) NOT NULL UNIQUE,
    name                VARCHAR(200) NOT NULL,
    en_name             VARCHAR(200),
    cp_type             VARCHAR(20),                  -- 对手方类型
    country_code        VARCHAR(10),
    swift_code          VARCHAR(11),
    
    -- 信用评级（可选）
    credit_rating       VARCHAR(10),
    external_rating     VARCHAR(10),
    
    status              CHAR(1) NOT NULL DEFAULT '1',
    created_by          VARCHAR(50) NOT NULL DEFAULT 'system',
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by          VARCHAR(50),
    updated_at          TIMESTAMP,
    version             INT DEFAULT 0,
    deleted             CHAR(1) DEFAULT '0'
);
```

### 6.3 限额模块（limit）

```sql
-- 流动性限额表
CREATE TABLE trm_limit_t (
    id                  BIGSERIAL PRIMARY KEY,
    limit_no            VARCHAR(50) NOT NULL UNIQUE,
    limit_name          VARCHAR(200) NOT NULL,
    limit_type          VARCHAR(20) NOT NULL,          -- LIQUIDITY/CREDIT/EXPOSURE
    
    business_unit_id   BIGINT,                        -- 适用业务单元
    currency            VARCHAR(10),                   -- 适用币种
    
    limit_amount       DECIMAL(18,2) NOT NULL,       -- 限额
    used_amount         DECIMAL(18,2) DEFAULT 0,     -- 已用金额
    available_amount    DECIMAL(18,2),                -- 可用金额（计算字段）
    
    warning_percent    DECIMAL(5,2) DEFAULT 80,      -- 预警线百分比
    critical_percent    DECIMAL(5,2) DEFAULT 90,     -- 警戒线百分比
    
    status              CHAR(1) NOT NULL DEFAULT '1',
    remark              VARCHAR(500),
    created_by          VARCHAR(50) NOT NULL DEFAULT 'system',
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by          VARCHAR(50),
    updated_at          TIMESTAMP,
    version             INT DEFAULT 0,
    deleted             CHAR(1) DEFAULT '0'
);

-- 限额预警记录表
CREATE TABLE trm_limit_warning_t (
    id                  BIGSERIAL PRIMARY KEY,
    limit_id            BIGINT NOT NULL,
    warn_type           VARCHAR(20),                  -- WARNING/CRITICAL/EXCEEDED
    warn_level          VARCHAR(20),                  -- YELLOW/RED/BLACK
    
    used_percent        DECIMAL(5,2),                 -- 使用百分比
    used_amount          DECIMAL(18,2),               -- 使用金额
    
    message             VARCHAR(500),                -- 预警信息
    is_read             CHAR(1) DEFAULT '0',
    read_at             TIMESTAMP,
    
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

---

## 七、审查检查清单

### 7.1 设计阶段检查

| 检查项 | 要求 |
|--------|------|
| 表名 | 使用 `trm_{module}_t` 格式 |
| 主键 | 自增ID或UUID |
| 审计字段 | 必须包含created_by/at/updated_by/at/version/deleted |
| 业务编码 | 使用 `{entity}_code`，唯一约束 |
| 金额字段 | 使用DECIMAL(18,2)或更高精度 |
| 利率/汇率 | 使用DECIMAL(12,8)或DECIMAL(18,8) |
| 索引 | 至少包含业务查询索引 |
| 外键 | 合理使用，避开循环依赖 |
| COMMENT | 表和关键字段添加注释 |

### 7.2 交易表额外检查

| 检查项 | 要求 |
|--------|------|
| 幂等键 | 必须有 `deal_no` 或 `idempotency_key` |
| 状态字段 | 包含完整状态流转 |
| 日期字段 | 必须有起息日和到期日 |
| 金额精度 | 考虑衍生品高利率精度需求 |
| 审计日志 | 记录关键操作（提交/审批/执行） |

### 7.3 PostgreSQL 特性检查

| 特性 | 适用场景 |
|------|----------|
| JSONB | 动态扩展字段、审计变更记录 |
| 序列 | 单表自增ID |
| UUID | 分布式主键 |
| 分区 | 大数据量表（交易流水、日志） |
| CHECK | 枚举值约束 |
| 注释 | 所有表和关键字段 |

---

## 八、常见错误

### ❌ 错误1：缺少审计字段

```sql
-- ❌ 错误：没有审计字段，无法追踪数据变更
CREATE TABLE trm_deal_t (
    id PRIMARY KEY,
    deal_no VARCHAR(50),
    amount DECIMAL(18,2)
);

-- ✅ 正确：包含完整审计字段
CREATE TABLE trm_deal_t (
    id BIGSERIAL PRIMARY KEY,
    deal_no VARCHAR(50) NOT NULL,
    amount DECIMAL(18,2) NOT NULL,
    created_by VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP,
    version INT DEFAULT 0,
    deleted CHAR(1) DEFAULT '0'
);
```

### ❌ 错误2：金额精度不足

```sql
-- ❌ 错误：利率精度不足，衍生品计算会丢失精度
interest_rate DECIMAL(10,6)   -- 精度: 0.000001
                    -- 问题: 无法精确表示某些利率

-- ✅ 正确：衍生品利率使用高精度
interest_rate DECIMAL(12,8)   -- 精度: 0.00000001
exchange_rate DECIMAL(18,8)  -- 汇率使用8位小数
```

### ❌ 错误3：交易表无幂等设计

```sql
-- ❌ 错误：资金交易表缺少幂等字段，重试会导致重复交易
CREATE TABLE trm_deal_t (
    id BIGSERIAL PRIMARY KEY,
    amount DECIMAL(18,2),
    ...
);

-- ✅ 正确：交易表包含幂等键
CREATE TABLE trm_deal_t (
    id BIGSERIAL PRIMARY KEY,
    deal_no VARCHAR(50) NOT NULL UNIQUE,   -- 业务流水号（天然幂等）
    idempotency_key VARCHAR(64),            -- 外部幂等KEY
    amount DECIMAL(18,2),
    ...
);
```

### ❌ 错误4：外键命名不规范

```sql
-- ❌ 错误：外键命名不清晰
CONSTRAINT fk_1 FOREIGN KEY (cp_id) REFERENCES trm_counterparty_t(id);

-- ✅ 正确：外键命名表达关联关系
CONSTRAINT fk_deal_counterparty FOREIGN KEY (counterparty_id) 
    REFERENCES trm_counterparty_t(id);
```

---

## 九、快速参考表

### 数据类型速查

| 业务场景 | 推荐类型 | 示例 |
|----------|----------|------|
| 主键ID | BIGSERIAL | id BIGSERIAL PRIMARY KEY |
| 业务编码 | VARCHAR(50) | deal_no VARCHAR(50) UNIQUE |
| 金额（一般） | DECIMAL(18,2) | amount DECIMAL(18,2) |
| 金额（大额） | DECIMAL(24,2) | large_amount DECIMAL(24,2) |
| 利率 | DECIMAL(12,8) | interest_rate DECIMAL(12,8) |
| 汇率 | DECIMAL(18,8) | exchange_rate DECIMAL(18,8) |
| 百分比 | DECIMAL(5,2) | percent DECIMAL(5,2) |
| 短文本 | VARCHAR(20) | status VARCHAR(20) |
| 长文本 | VARCHAR(500) | remark VARCHAR(500) |
| JSON数据 | JSONB | config_json JSONB |
| 日期 | DATE | value_date DATE |
| 时间戳 | TIMESTAMP | created_at TIMESTAMP |
| SWIFT代码 | VARCHAR(11) | swift_code VARCHAR(11) |
| 版本号 | INT | version INT DEFAULT 0 |
| 软删除 | CHAR(1) | deleted CHAR(1) DEFAULT '0' |

### 索引速查

| 场景 | 索引类型 |
|------|----------|
| 唯一业务编码 | UNIQUE INDEX |
| 单字段查询 | INDEX |
| 组合查询（高频） | INDEX (field1, field2) |
| 过滤查询 | PARTIAL INDEX |
| 大表分页 | 覆盖索引 |

---

## 十、相关文档

- [Open-TMS开发规范文档](../docs/规范/Open-TMS开发规范文档.md)
- [PostgreSQL官方文档](https://www.postgresql.org/docs/)
- [阿里巴巴Java开发手册 - 数据库规约](https://github.com/alibaba/AlibabaJavaCodingGuidelines)

---

**核心原则：资金系统数据必须满足可审计、可追溯、幂等性要求，所有表必须包含审计字段，交易表必须支持幂等设计。**
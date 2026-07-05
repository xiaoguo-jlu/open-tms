# Open-TMS 数据库审核标准与对标参考

> 本文档收录 Open-TMS DB 审核所依据的标准、业界对标资料、PG 最佳实践。

---

## 1. Open-TMS 表设计规范 (CLAUDE.md 提炼)

### 1.1 表命名

```
tms_{module}_{type}
  module: basedata / dealing / fx / irs / valuation / ...
  type:   t (主表) / d (字典) / log (日志) / rel (关联) / his (历史)
```

**示例**:
- `tms_country_t` — 国家主表
- `tms_deal_t` — 交易主表
- `tms_action_log` — 操作日志表
- `tms_deal_cashflow_rel` — 交易-现金流关联表

### 1.2 强制审计字段

```sql
id          BIGSERIAL PRIMARY KEY
created_by  VARCHAR(50)  NOT NULL DEFAULT 'system'
created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
updated_by  VARCHAR(50)
updated_at  TIMESTAMP
version     INT          DEFAULT 0
deleted     CHAR(1)      DEFAULT '0'
```

### 1.3 金额精度

| 场景 | 精度 |
|------|------|
| 普通金额 | `DECIMAL(18,2)` |
| 汇率 | `DECIMAL(18,8)` |
| 利率 | `DECIMAL(10,4)` |
| **AC Deal / Cashflow** | `DECIMAL(38,18)` |

### 1.4 字符集

PostgreSQL 默认 UTF8,客户端连接串显式指定 `client_encoding=UTF8`。

---

## 2. PostgreSQL 最佳实践

### 2.1 主键

```sql
-- 推荐:BIGSERIAL
id BIGSERIAL PRIMARY KEY

-- 不推荐:UUID(查询慢 + 存储大)
id UUID PRIMARY KEY DEFAULT gen_random_uuid()

-- 不推荐:VARCHAR 主键(性能差)
id VARCHAR(36) PRIMARY KEY
```

### 2.2 外键行为

| 场景 | ON DELETE | ON UPDATE |
|------|-----------|-----------|
| 业务主引用 (Deal→Counterparty) | RESTRICT | RESTRICT |
| 影像/快照表 (DealImage→Deal) | RESTRICT | RESTRICT |
| 日志表 (ActionLog→Deal) | SET NULL | CASCADE |
| 关联表 (DealCashflowRel) | CASCADE | CASCADE |

### 2.3 索引类型

| 场景 | 索引类型 |
|------|---------|
| 等值查询 (id / *_no) | B-tree (默认) |
| 范围查询 (*_date / *_at) | B-tree |
| JSONB 查询 | GIN |
| 全文搜索 | GIN (to_tsvector) |
| 时间序列大表 | BRIN |

### 2.4 索引创建原则

- **高基数在前**: `(counterparty_id, status)` ✅ `(status, counterparty_id)` ❌
- **覆盖索引**: `CREATE INDEX idx ON t (a, b) WHERE status = 'Active'`
- **部分索引**: 大表中只索引常用状态

### 2.5 CHECK 约束

```sql
-- 枚举校验
status VARCHAR(20) NOT NULL DEFAULT 'New'
  CHECK (status IN ('New', 'Submitted', 'Approved', 'Rejected', 'Settled', 'Canceled'))

-- 数值范围
amount DECIMAL(18,2) NOT NULL CHECK (amount > 0)
rate   DECIMAL(18,8) NOT NULL CHECK (rate > 0)
```

---

## 3. 业界对标 — 数据建模要点

### 3.1 FIS Quantum

| 规范 | 描述 |
|------|------|
| 表数量 | 静态数据 ~200 张,交易相关 ~150 张 |
| 主键策略 | BIGINT 自增 |
| 审计字段 | created_by/at, updated_by/at, version, deleted 强制 |
| 索引密度 | 主键 + 业务编号 + 5-10 业务字段索引 |
| CHECK 约束 | 业务规则强制下推 |
| 注释规范 | 100% 表/列注释 |

### 3.2 Murex MX.3

| 规范 | 描述 |
|------|------|
| 多分区 | 按 legal_entity / book 分区 |
| 历史表 | 所有交易表配套 `_his` 历史表 |
| 触发器 | 关键表有审计触发器(后被应用层替代) |
| 精度 | 金融字段 DECIMAL(38,18) 统一 |
| 状态机 | 数据库 CHECK 约束定义合法状态 |

### 3.3 SAP TRM

| 规范 | 描述 |
|------|------|
| 表命名 | 短前缀 + 模块代码 + 类型 |
| 主数据 | 集中管理(Country/Currency/Calendar) |
| 事务表 | 配套应用日志(JAVA 触发) |
| 数据归档 | 按年度分区 + 历史表 |

### 3.4 Kyriba

| 规范 | 描述 |
|------|------|
| 云原生 | 多租户 schema 隔离 |
| 审计 | 完整审计日志表 |
| 软删除 | 全表 deleted 字段 |

---

## 4. Open-TMS M1 已贯通表参考

### 4.1 主表清单 (基于 db/schema/)

| 表名 | 模块 | 说明 |
|------|------|------|
| `tms_country_t` | basedata | 国家 |
| `tms_currency_t` | basedata | 币种 |
| `tms_bank_t` | basedata | 银行 |
| `tms_bank_account_t` | basedata | 银行账户 |
| `tms_counterparty_t` | basedata | 对手方 |
| `tms_trader_t` | basedata | 交易员 |
| `tms_business_unit_t` | basedata | 管理主体 |
| `tms_currency_pair_t` | basedata | 币种对 |
| `tms_exchange_rate_t` | basedata | 汇率 |
| `tms_instrument_t` | basedata | 金融工具 |
| `tms_calendar_t` | basedata | 日历 |
| `tms_holiday_t` | basedata | 节假日 |
| `tms_settlement_account_t` | basedata | 结算账户 |
| `tms_deal_t` | dealing | 交易主表 |
| `tms_ac_deal_t` | dealing | AC 交易 |
| `tms_action_t` | dealing | 操作 |
| `tms_deal_image_t` | dealing | 交易影像 |
| `tms_cashflow_t` | dealing | 现金流 |
| `tms_approval_task_t` | dealing | 审批任务 |

### 4.2 模板参考

**基础数据表模板** (基于 tms_country_t):
```sql
CREATE TABLE tms_{resource}_t (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    en_name VARCHAR(200),
    status CHAR(1) NOT NULL DEFAULT '1',
    remark VARCHAR(500),
    created_by VARCHAR(50) NOT NULL DEFAULT 'system',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP,
    version INT DEFAULT 0,
    deleted CHAR(1) DEFAULT '0'
);
COMMENT ON TABLE tms_{resource}_t IS '{资源}表';
CREATE INDEX idx_{resource}_code ON tms_{resource}_t(code);
CREATE INDEX idx_{resource}_status ON tms_{resource}_t(status);
```

**Deal 模板** (基于 tms_deal_t):
```sql
CREATE TABLE tms_deal_t (
    id BIGSERIAL PRIMARY KEY,
    deal_no VARCHAR(50) NOT NULL UNIQUE,
    deal_type VARCHAR(20) NOT NULL,
    business_unit VARCHAR(50) NOT NULL,
    counterparty_id BIGINT NOT NULL,
    instrument_id BIGINT NOT NULL,
    trader_id BIGINT NOT NULL,
    direction VARCHAR(10) NOT NULL,
    amount DECIMAL(38,18) NOT NULL,
    currency VARCHAR(10) NOT NULL,
    deal_date DATE NOT NULL,
    value_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'New',
    description VARCHAR(500),
    remark VARCHAR(500),
    latest_action_no VARCHAR(50),
    created_by VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP,
    version INT DEFAULT 0,
    deleted CHAR(1) DEFAULT '0'
);
```

---

## 5. Open-TMS 字段命名速查

### 5.1 业务主字段

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | BIGSERIAL PK | 主键 |
| `*_code` | VARCHAR(50) UNIQUE | 业务编码 |
| `*_no` | VARCHAR(50) UNIQUE | 业务流水号 |
| `*_name` | VARCHAR(50-200) | 业务名称 |

### 5.2 业务关联字段

| 字段 | 类型 | 说明 |
|------|------|------|
| `*_id` | BIGINT FK | 外键引用 |
| `parent_*_id` | BIGINT FK | 自关联父节点 |
| `hierarchy_path` | VARCHAR(500) | 层级路径 |

### 5.3 业务属性字段

| 字段 | 类型 | 说明 |
|------|------|------|
| `status` | CHAR(1) / VARCHAR(20) | 状态 |
| `*_type` | VARCHAR(20) | 类型 |
| `*_flag` | CHAR(1) | 标记(0/1) |

### 5.4 业务数值字段

| 字段 | 类型 | 说明 |
|------|------|------|
| `*_amount` | DECIMAL(18,2) / DECIMAL(38,18) | 金额 |
| `*_rate` | DECIMAL(18,8) | 汇率 |
| `*_rate` | DECIMAL(10,4) | 利率 |
| `*_qty` | INT / BIGINT | 数量 |
| `*_count` | INT / BIGINT | 计数 |

### 5.5 业务日期字段

| 字段 | 类型 | 说明 |
|------|------|------|
| `*_date` | DATE | 日期(交易日/起息日) |
| `*_at` | TIMESTAMP | 时间戳(创建/更新) |

---

## 6. 注释规范

### 6.1 表注释 (强制)

```sql
COMMENT ON TABLE tms_country_t IS '国家/地区基础数据表';
```

### 6.2 列注释 (核心列强制)

```sql
COMMENT ON COLUMN tms_country_t.code IS '国家代码(ISO 3166-1 alpha-2/3)';
COMMENT ON COLUMN tms_country_t.status IS '状态:1=Active 0=Inactive';
COMMENT ON COLUMN tms_deal_t.deal_type IS '交易类型:AC/AT/FX';
```

---

## 7. Open-TMS 状态/类型字符串取值

(从 `com.opentms.common.constant.GlobalConstants` 提炼)

### 7.1 DealType

| 值 | 含义 |
|----|------|
| `AC` | 实体现金流交易 |
| `AT` | 内部转账 |
| `FX` | 外汇交易 |

### 7.2 DealStatus

| 值 | 含义 |
|----|------|
| `New` | 新建 |
| `Submitted` | 已提交 |
| `Approved` | 已审批 |
| `Rejected` | 已驳回 |
| `Settled` | 已结算 |
| `Canceled` | 已取消 |

### 7.3 ActionType

| 值 | 含义 |
|----|------|
| `CREATE` | 创建 |
| `UPDATE` | 更新 |
| `DELETE` | 删除 |
| `SUBMIT` | 提交 |
| `APPROVE` | 通过 |
| `REJECT` | 驳回 |
| `EXECUTE` | 执行 |

### 7.4 Status

| 值 | 含义 |
|----|------|
| `1` | Active(启用) |
| `0` | Inactive(禁用) |

---

## 8. PG 常用 DDL 模板

### 8.1 添加审计字段 (存量表)

```sql
ALTER TABLE tms_xxx_t
  ADD COLUMN created_by VARCHAR(50) NOT NULL DEFAULT 'system',
  ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  ADD COLUMN updated_by VARCHAR(50),
  ADD COLUMN updated_at TIMESTAMP,
  ADD COLUMN version INT DEFAULT 0,
  ADD COLUMN deleted CHAR(1) DEFAULT '0';
```

### 8.2 添加索引

```sql
-- 单列
CREATE INDEX idx_xxx_yyy ON tms_xxx_t(yyy);

-- 组合索引
CREATE INDEX idx_xxx_a_b ON tms_xxx_t(a, b);

-- 部分索引
CREATE INDEX idx_xxx_active ON tms_xxx_t(code) WHERE deleted = '0';

-- 唯一索引
CREATE UNIQUE INDEX uk_xxx_no ON tms_xxx_t(no);
```

### 8.3 添加外键

```sql
ALTER TABLE tms_deal_t
  ADD CONSTRAINT fk_deal_counterparty
  FOREIGN KEY (counterparty_id)
  REFERENCES tms_counterparty_t(id)
  ON DELETE RESTRICT
  ON UPDATE RESTRICT;
```

### 8.4 添加 CHECK

```sql
ALTER TABLE tms_deal_t
  ADD CONSTRAINT ck_deal_status
  CHECK (status IN ('New', 'Submitted', 'Approved', 'Rejected', 'Settled', 'Canceled'));

ALTER TABLE tms_deal_t
  ADD CONSTRAINT ck_deal_amount
  CHECK (amount > 0);
```

---

## 9. 性能与运维要点

### 9.1 性能

- 主键 BIGSERIAL 优于 UUID(8 字节 vs 16 字节,索引小)
- 外键必须有索引(否则级联/查询慢)
- 状态/类型字段可用部分索引(只索引 Active 数据)
- 时间字段推荐 BRIN(对大表)

### 9.2 运维

- 所有表必须有 COMMENT(运维/新人友好)
- DDL 变更需配套迁移脚本
- DROP TABLE 必须先备份
- 大表 ALTER 用 `pg_repack` / 锁等待

### 9.3 备份

- 全库每日 pg_dump
- 关键表单独备份(tms_deal_t / tms_action_t)
- 备份保留 30 天(生产)
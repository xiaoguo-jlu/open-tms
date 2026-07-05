# Checklist 02 — 列设计审核清单

> 配合 `opentms-review-db` SKILL.md 使用。审核员按此清单逐项勾选。
> 聚焦字段命名、类型、长度、精度等列级规范。

---

## A. 字段命名规范

### A1. 大小写
- [ ] 100% snake_case(无 camelCase)
- [ ] 字段名小写,无大写字母
- [ ] 字段名无下划线开头/结尾

### A2. 后缀约定 (按业务含义)

| 字段类型 | 后缀 | 示例 |
|---------|------|------|
| 业务编码 | `_code` | `country_code`, `instrument_code` |
| 业务流水号 | `_no` | `deal_no`, `cashflow_no` |
| 名称 | `_name` | `country_name`, `bank_name` |
| 金额 | `_amount` | `buy_amount`, `sell_amount` |
| 汇率 | `_rate` | `exchange_rate`, `fx_rate` |
| 利率 | `_rate` | `interest_rate`, `coupon_rate` |
| 日期 | `_date` | `trade_date`, `value_date` |
| 时间 | `_at` | `created_at`, `updated_at` |
| 状态 | `status` | `status`, `approval_status` |
| 类型 | `_type` | `deal_type`, `action_type` |
| 标识 | `_flag` | `enabled_flag`, `default_flag` |
| 描述/备注 | `_remark` 或 `remark` / `description` | `remark`, `description` |
| 外键 | `*_id` | `counterparty_id`, `bank_account_id` |
| 计数 | `_count` / `_qty` | `cashflow_count`, `total_qty` |

### A3. 命名禁忌
- [ ] 字段名无缩写歧义(`amt` → `amount`, `desc` → `description`)
- [ ] 字段名无中英文混用
- [ ] 字段名无拼写错误
- [ ] 字段名无 `temp_` / `test_` / `bak_` 这类临时命名

---

## B. 字段类型

### B1. 文本类型
- [ ] 业务编码 `VARCHAR(50)`
- [ ] 业务流水号 `VARCHAR(50)`
- [ ] 名称 `VARCHAR(50-200)`
- [ ] 备注/描述 `VARCHAR(500)` 或 `TEXT`
- [ ] 短码/币种 `VARCHAR(10)`
- [ ] 电话/邮箱 `VARCHAR(30-100)`

### B2. 数值类型

#### B2.1 金额精度(强制)

| 场景 | 类型 | 示例 |
|------|------|------|
| 普通金额 | `DECIMAL(18,2)` | `nominal_amount` |
| 汇率 | `DECIMAL(18,8)` | `exchange_rate` |
| 利率 | `DECIMAL(10,4)` | `interest_rate` |
| **AC Deal / Cashflow** | `DECIMAL(38,18)` | `buy_amount`, `sell_amount` |

- [ ] 100% 金额字段符合精度规范
- [ ] 无 `FLOAT` / `DOUBLE` 用于金额(精度丢失)
- [ ] 无 `DECIMAL(38,18)` 滥用(仅 AC/Cashflow)

#### B2.2 整数类型
- [ ] 主键 `BIGSERIAL`(8 字节)
- [ ] 计数 `INTEGER` 或 `BIGINT`
- [ ] 乐观锁 `version INT DEFAULT 0`
- [ ] 等级/层级 `INT`(如 `level_depth`)

### B3. 日期/时间类型

| 字段 | 类型 |
|------|------|
| 交易日 / 起息日 / 交割日 | `DATE` |
| 创建时间 / 更新时间 / 操作时间 | `TIMESTAMP` |
| 时区敏感时间 | `TIMESTAMP WITH TIME ZONE` |

- [ ] `*_date` 字段为 `DATE`(非 VARCHAR / TIMESTAMP)
- [ ] `*_at` 字段为 `TIMESTAMP`
- [ ] `created_at` `NOT NULL DEFAULT CURRENT_TIMESTAMP`

### B4. 布尔类型
- [ ] 不使用 `BOOLEAN`,统一用 `CHAR(1) DEFAULT '1'`(1=是 0=否)
- [ ] 状态字段 `status CHAR(1) DEFAULT '1'`(基础数据表)

### B5. JSON 类型
- [ ] 复杂结构用 `JSONB`(PG 原生,带索引)
- [ ] 简单结构用 `VARCHAR` / `TEXT`

---

## C. 字段长度

### C1. 长度合理性
- [ ] `VARCHAR(n)` n 值贴近实际业务(避免过长/过短)
- [ ] 无 `VARCHAR(255)` 这种含糊长度
- [ ] 无 `VARCHAR(1)` 用于姓名(过短)

### C2. 长度对中文安全
- [ ] 备注 `VARCHAR(500)` 可存 ~166 个中文(UTF8)
- [ ] 名称 `VARCHAR(200)` 可存 ~66 个中文

### C3. 推荐长度速查

| 字段类型 | 推荐长度 |
|---------|---------|
| 业务编码 | VARCHAR(50) |
| 业务流水号 | VARCHAR(50) |
| 国家/币种代码 | VARCHAR(10) |
| 名称 | VARCHAR(50-200) |
| 描述 | VARCHAR(500) |
| 备注 | VARCHAR(500) |
| 电话 | VARCHAR(30) |
| 邮箱 | VARCHAR(100) |
| 地址 | VARCHAR(500) |
| URL | VARCHAR(500) |
| UUID 字符串 | VARCHAR(36) |

---

## D. NULL vs NOT NULL

### D1. NOT NULL 字段 (业务关键)
- [ ] 业务编码 NOT NULL
- [ ] 业务流水号 NOT NULL
- [ ] 业务名称 NOT NULL
- [ ] 状态字段 NOT NULL
- [ ] 类型字段 NOT NULL
- [ ] 外键 NOT NULL(强制关系存在)
- [ ] 金额字段 NOT NULL
- [ ] 日期字段 NOT NULL
- [ ] `created_by` / `created_at` NOT NULL

### D2. NULL 字段 (允许空)
- [ ] `updated_by` / `updated_at` 可 NULL(创建后未更新)
- [ ] 备注/描述 可 NULL
- [ ] 可选关联外键 可 NULL
- [ ] 可选日期 可 NULL(无到期日等)

### D3. 默认值
- [ ] NOT NULL 字段必须有 DEFAULT
- [ ] DEFAULT 与业务语义一致
- [ ] `created_at` `DEFAULT CURRENT_TIMESTAMP`
- [ ] `version` `DEFAULT 0`
- [ ] `deleted` `DEFAULT '0'`

---

## E. CHECK 约束

### E1. 状态/枚举字段
- [ ] `status` CHECK (status IN ('1', '0'))
- [ ] `deal_type` CHECK (deal_type IN ('AC', 'AT', 'FX'))
- [ ] `direction` CHECK (direction IN ('BUY', 'SELL'))
- [ ] `image_type` CHECK (image_type IN ('CREATE', 'UPDATE', 'DELETE'))

### E2. 数值范围
- [ ] `amount > 0`
- [ ] `rate > 0`
- [ ] `version >= 0`
- [ ] `level_depth >= 1`

### E3. 字符串格式
- [ ] email 格式(若业务需要)
- [ ] 手机号格式(若业务需要)
- [ ] 日期范围(如 `trade_date <= value_date`)

---

## F. 反例 (必须退回)

### F1. 命名反例
```sql
-- 反例 1: camelCase
buyAmount DECIMAL(18,2)
dealNo VARCHAR(50)

-- 反例 2: 缩写歧义
amt DECIMAL(18,2)
desc VARCHAR(500)

-- 反例 3: 拼写错误
exchage_rate DECIMAL(18,8)
```

### F2. 类型反例
```sql
-- 反例 1: 金额用 FLOAT
amount FLOAT

-- 反例 2: 日期用 VARCHAR
trade_date VARCHAR(20)

-- 反例 3: 软删除用 BOOLEAN
deleted BOOLEAN DEFAULT FALSE
```

### F3. 精度反例
```sql
-- 反例 1: AC Deal 金额精度不够
buy_amount DECIMAL(18,2)  -- 应为 DECIMAL(38,18)

-- 反例 2: 普通金额用 DECIMAL(38,18)
nominal_amount DECIMAL(38,18)  -- 浪费,应为 DECIMAL(18,2)
```

### F4. 长度反例
```sql
-- 反例 1: 名称过短
bank_name VARCHAR(10)  -- 截断风险

-- 反例 2: 备注过短
remark VARCHAR(50)  -- 不够

-- 反例 3: 含糊长度
description VARCHAR(255)  -- 应具体
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
- DB-001/002/013/014/020 未通过 → 直接降至 D
- 字段类型混用 (DATE/VARCHAR/TIMESTAMP) → 降至 D
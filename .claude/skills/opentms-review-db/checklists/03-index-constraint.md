# Checklist 03 — 索引与约束审核清单

> 配合 `opentms-review-db` SKILL.md 使用。审核员按此清单逐项勾选。
> 聚焦索引设计、约束声明、命名一致性。

---

## A. 主键约束

### A1. 主键必填
- [ ] 每张表有 PRIMARY KEY
- [ ] 主键为单列 `id BIGSERIAL`
- [ ] 无复合主键

### A2. 主键约束命名
- [ ] 系统默认 (无需显式命名)
- [ ] 如显式命名:`pk_{table_name}`

---

## B. UNIQUE 约束

### B1. 业务编号 UNIQUE
- [ ] `*_no` 业务流水号 `UNIQUE NOT NULL`
- [ ] `*_code` 业务编码 `UNIQUE NOT NULL`
- [ ] 联合唯一:`UNIQUE (col1, col2)`

### B2. UNIQUE 约束命名
- [ ] 命名 `uk_{table}_{col}`
- [ ] 示例:`uk_country_code`, `uk_deal_no`

---

## C. FOREIGN KEY 约束

### C1. FK 必填 (跨表引用)
- [ ] 所有 `*_id` 字段有 FOREIGN KEY
- [ ] 外键引用目标表主键
- [ ] ON DELETE / ON UPDATE 行为明确

### C2. FK 命名
- [ ] 命名 `fk_{table}_{referenced_table}`
- [ ] 示例:`fk_deal_counterparty`

### C3. FK ON DELETE 行为

| 场景 | 行为 |
|------|------|
| 业务关键引用 (Deal→Counterparty) | `RESTRICT`(禁止删除被引用的对手方) |
| 历史/日志引用 | `SET NULL` 或 `CASCADE` |
| 影像/快照表 | `RESTRICT`(保留历史) |

### C4. FK ON UPDATE 行为
- [ ] 默认 `RESTRICT`(主键不更新)
- [ ] 或 `CASCADE`(与 ON DELETE 一致)

---

## D. NOT NULL 与 DEFAULT

### D1. NOT NULL 字段
- [ ] 关键业务字段 NOT NULL(详见 checklist 02 D1)

### D2. DEFAULT 值
- [ ] NOT NULL 字段有 DEFAULT
- [ ] DEFAULT 值业务合理
- [ ] 时间字段 `DEFAULT CURRENT_TIMESTAMP`

---

## E. CHECK 约束

### E1. 枚举类 CHECK
- [ ] `status IN (...)` 枚举校验
- [ ] `type IN (...)` 类型校验
- [ ] `direction IN (...)` 方向校验

### E2. 范围类 CHECK
- [ ] `amount > 0`
- [ ] `rate > 0 AND rate < 100`
- [ ] `version >= 0`

---

## F. 索引设计

### F1. 主键索引
- [ ] 主键自动创建索引(无需显式 CREATE INDEX)
- [ ] UNIQUE 约束自动创建索引

### F2. 外键索引 (强制)
- [ ] 100% `*_id` 外键字段有索引
- [ ] 组合索引 `(a_id, b_id)` 可覆盖单个 `a_id`
- [ ] 无外键无索引(性能风险)

### F3. 业务查询索引 (按场景)
- [ ] 状态字段 `status`(常用于筛选)
- [ ] 类型字段 `*_type`(常用于筛选)
- [ ] 业务编号 `*_no`(常用于精确查询)
- [ ] 日期字段 `*_date`(常用于范围查询)
- [ ] 业务单元/对手方/工具 `*_id`(常用于关联)

### F4. 组合索引
- [ ] 高基数字段在前(如 `deal_no` 在 `status` 前)
- [ ] WHERE 条件字段在前
- [ ] ORDER BY 字段在后
- [ ] 覆盖索引(SELECT 字段全部包含)

### F5. 索引命名
- [ ] 命名 `idx_{table}_{col1}_{col2}`
- [ ] 示例:`idx_deal_status_unit`, `idx_ac_deal_bank`

### F6. 避免过度索引
- [ ] 无低基数单列索引(如 `deleted`, `version`)
- [ ] 无重复索引(单列已被组合索引覆盖)
- [ ] 无冗余索引(只用于一次性查询)

### F7. 索引类型
- [ ] 默认 B-tree(等值/范围)
- [ ] JSONB 用 GIN
- [ ] 全文搜索用 GIN(to_tsvector)
- [ ] 时间序列用 BRIN(可选)

---

## G. 数据库对象命名一致性

### G1. 表/列
- [ ] 全小写 snake_case
- [ ] 无大写字母
- [ ] 无驼峰

### G2. 索引
- [ ] `idx_{table}_{col}`
- [ ] 无 `idx1`, `idx_a`

### G3. 约束
- [ ] 主键 `pk_{table}`
- [ ] 外键 `fk_{table}_{referenced_table}`
- [ ] 唯一 `uk_{table}_{col}`
- [ ] 检查 `ck_{table}_{col}`

### G4. 序列
- [ ] PG BIGSERIAL 自动创建 `*_id_seq`
- [ ] 序列命名自动管理(无需手动)

---

## H. 反例 (必须退回)

### H1. 索引缺失
```sql
-- 反例:外键无索引
counterparty_id BIGINT NOT NULL,
-- 缺少 CREATE INDEX idx_deal_counterparty ON tms_deal_t(counterparty_id)

-- 反例:业务编号无索引
deal_no VARCHAR(50) NOT NULL,
-- 缺少 CREATE INDEX idx_deal_no ON tms_deal_t(deal_no)
```

### H2. 索引过度
```sql
-- 反例:重复索引
CREATE INDEX idx_deal_status ON tms_deal_t(status);
CREATE INDEX idx_deal_status_type ON tms_deal_t(status, deal_type);
-- 第一个索引冗余

-- 反例:低基数单列索引
CREATE INDEX idx_deal_deleted ON tms_deal_t(deleted);  -- 值只有 0/1
```

### H3. 命名不一致
```sql
-- 反例 1: 索引命名不一致
CREATE INDEX idxDealStatus ON tms_deal_t(status);
CREATE INDEX a_idx ON tms_deal_t(deal_no);

-- 反例 2: 外键无命名
counterparty_id BIGINT REFERENCES tms_counterparty_t(id)
-- 缺少 CONSTRAINT fk_deal_counterparty
```

### H4. 缺失 CHECK
```sql
-- 反例:枚举字段无 CHECK
status VARCHAR(20) NOT NULL DEFAULT 'New'
-- 缺少 CHECK (status IN ('New', 'Submitted', 'Approved', 'Rejected', 'Settled', 'Canceled'))
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
- 外键无索引 (DB-007/015) → 直接降至 D
- 业务编号无 UNIQUE (DB-018) → 直接降至 D
- 任何 CHECK 严重缺失 → 降至 C
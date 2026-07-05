# Checklist 01 — 表设计审核清单

> 配合 `opentms-review-db` SKILL.md 使用。审核员按此清单逐项勾选。

---

## A. 表命名

### A1. 表名前缀
- [ ] 表名以 `tms_` 开头
- [ ] 表名全小写 + snake_case
- [ ] 表名第三段为模块名(basedata / dealing / fx / ...)

### A2. 表名后缀
- [ ] 主表后缀 `_t`(e.g. `tms_country_t`)
- [ ] 字典表后缀 `_d`(e.g. `tms_status_d`)
- [ ] 日志表后缀 `_log`
- [ ] 关联表后缀 `_rel`
- [ ] 历史表后缀 `_his`

### A3. 表名长度
- [ ] 表名总长度 ≤ 30 字符(PostgreSQL identifier 限制 63,推荐 ≤30)
- [ ] 表名无缩写歧义(避免 `tms_b_t` 这种)

---

## B. 主键

### B1. 主键策略
- [ ] 主键字段名 `id`
- [ ] 主键类型 `BIGSERIAL`(PG 自增 8 字节)
- [ ] 主键约束 `PRIMARY KEY`

### B2. 主键禁止事项
- [ ] 不使用 UUID 主键(Open-TMS 统一 BIGSERIAL)
- [ ] 不使用 VARCHAR 主键
- [ ] 不使用复合主键(用 UNIQUE 约束替代)

---

## C. 审计字段 (强制)

### C1. 创建/更新审计
- [ ] `created_by VARCHAR(50) NOT NULL`(默认 `'system'`)
- [ ] `created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP`
- [ ] `updated_by VARCHAR(50)`(允许 NULL)
- [ ] `updated_at TIMESTAMP`(允许 NULL)

### C2. 并发控制
- [ ] `version INT DEFAULT 0`(MyBatis Plus @Version 乐观锁)

### C3. 软删除
- [ ] `deleted CHAR(1) DEFAULT '0'`(0=未删 1=已删,MyBatis Plus @TableLogic)

---

## D. 表注释

### D1. 表级注释
- [ ] `COMMENT ON TABLE {table_name} IS '中文描述';`
- [ ] 注释清晰、不为空泛(非"业务表"这种)
- [ ] 注释与所属模块对应

### D2. 列级注释 (核心列)
- [ ] 状态列注释 (`COMMENT ON COLUMN ... status IS '...'`;)
- [ ] 类型列注释
- [ ] 外键列注释(说明引用哪个资源)
- [ ] 关键业务列注释(如 deal_no / cashflow_no)

---

## E. 表间关系

### E1. 外键约束
- [ ] 所有 `*_id` 字段有显式 `FOREIGN KEY` 约束
- [ ] 外键命名 `fk_{table}_{referenced_table}`
- [ ] 外键 `ON DELETE` / `ON UPDATE` 行为明确(RESTRICT / CASCADE / SET NULL)
- [ ] 外键引用的目标表存在且已建表

### E2. 引用关系 DAG
- [ ] 跨模块引用方向单向(无循环)
- [ ] `basedata` 模块不被其他模块反向依赖
- [ ] 共享实体放 `basedata` 而非业务模块

---

## F. 字符集与排序规则

### F1. 字符集
- [ ] 数据库字符集 UTF8
- [ ] 客户端编码 UTF8
- [ ] VARCHAR 长度对中文安全(中文按 3 字节计)

### F2. 排序规则 (Collation)
- [ ] 默认 collation(无需显式声明)
- [ ] 如有特殊排序需求,显式声明 `COLLATE`

---

## G. 反例 (必须退回)

### G1. 命名反例
```sql
-- 反例 1: 大写或 camelCase
CREATE TABLE TmsDeal (...);
CREATE TABLE tmsDealT (...);

-- 反例 2: 无模块名前缀
CREATE TABLE country (...);
CREATE TABLE deal (...);

-- 反例 3: 后缀错误
CREATE TABLE tms_country (...);  -- 缺 _t
CREATE TABLE tms_country_main (...);  -- 应为 _t
```

### G2. 主键反例
```sql
-- 反例 1: UUID
id UUID PRIMARY KEY DEFAULT gen_random_uuid()

-- 反例 2: 复合主键
PRIMARY KEY (deal_no, action_no)

-- 反例 3: 无默认值
created_by VARCHAR(50) NOT NULL  -- 无 DEFAULT
```

### G3. 审计字段反例
```sql
-- 反例 1: 软删除用 BOOLEAN
deleted BOOLEAN DEFAULT FALSE

-- 反例 2: 软删除字段名不规范
is_deleted BOOLEAN DEFAULT FALSE
soft_delete INT DEFAULT 0

-- 反例 3: 乐观锁命名不规范
lock_version INT DEFAULT 0
revision INT DEFAULT 0
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
- 任何 P0 (DB-001/002/003/004/007/013/014/018) 未通过 → 直接降至 D
- 2 个以上 P0 未通过 → 直接判 D
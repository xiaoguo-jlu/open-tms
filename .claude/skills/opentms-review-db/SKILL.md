---
name: opentms-review-db
description: |
  Open-TMS 数据库审核 Skill。由 Technical Architect / DBA 调用,用于审核
  DDL (CREATE TABLE / INDEX / CONSTRAINT) 设计,确保符合 Open-TMS 既有
  规范 (CLAUDE.md 命名/审计/精度) 、PostgreSQL 最佳实践,以及业界
  对标 (FIS Quantum / Murex MX.3 / SAP TRM) 的数据建模标准。

  Trigger: "数据库审核"、"DB 评审"、"DDL 审核"、"表设计审核"、"schema 审核"、"DB review"
---

# opentms-review-db

数据库审核 — 对 DDL / Schema 设计 进行结构化审核,确保符合
Open-TMS 命名规范、审计字段要求、精度规范,以及成熟资金系统
(FIS Quantum / Murex MX.3) 数据建模标准。

> **本 skill 遵循** `opentms-review-common` 公共规范 — 统一评级体系、报告格式、调用方式、归档路径。

---

## 输入

- 待审核的 `.sql` 文件路径(必填,可多个,位于 `db/schema/` 目录)
- 所属模块名(必填,如 `basedata` / `dealing` / `fx`)
- 关联的 PRD / 数据模型文档路径(可选,但建议提供)
- 是否新增 / 修改既有表(必填)

## 输出

- 审核报告: `docs/reviews/{feature-name}/db-review.md`
- 按 `templates/report.md` 填充

## 工作流程

1. **加载公共规范** — 读取 `opentms-review-common/SKILL.md`
2. **读取 DDL 文件** — 用 `Read` 工具读取 `.sql` 文件
3. **对比基准** — 对比 `db/schema/` 中既有 M1 已贯通表的 schema 风格
4. **加载 checklist** — 按 `checklists/01-table-design.md` / `02-column-design.md` / `03-index-constraint.md` 逐项打勾
5. **逐项审核** — 按下方 YAML checklist 逐项判定 PASS/FAIL
6. **输出报告** — 评级 A/B/C/D + P0/P1/P2 问题清单 + 整改建议

---

## 审核项结构化清单 (YAML 数组)

```yaml
db_review_items:

  # ============= 用户列出的 3 点 =============

  - id: DB-001
    name: 字段命名与长度设计规范
    severity: P0
    standard: Open-TMS CLAUDE.md 强制 — snake_case 命名,业务编码 50 字符,
              业务流水号 50 字符,名称 50-200 字符
    check_method: |
      1. Read db/schema/{module}/{table}.sql;
      2. Grep 所有字段命名,验证:
         - 100% snake_case (无 camelCase)
         - 业务编码 (*_code) VARCHAR(50)
         - 业务流水号 (*_no) VARCHAR(50) UNIQUE
         - 名称 (*_name) VARCHAR(50-200)
         - 金额字段后缀 _amount
         - 日期字段后缀 _date
         - 时间字段后缀 _at / *_time
      3. 对比 CLAUDE.md 「Database Naming」章节
    pass_criteria: 100% 字段符合命名 + 长度规范
    failure_action: 退回 TA 重命名 / 修改长度

  - id: DB-002
    name: 金额/利率/汇率小数位统一
    severity: P0
    standard: Open-TMS CLAUDE.md 强制 — 普通金额 DECIMAL(18,2),汇率 DECIMAL(18,8),
              利率 DECIMAL(10,4),AC Deal/Cashflow DECIMAL(38,18)
    check_method: |
      1. Read db/schema/{module}/{table}.sql;
      2. Grep 所有 DECIMAL 字段,逐字段验证:
         - amount / price / qty / balance → DECIMAL(18,2)
         - exchange_rate / fx_rate → DECIMAL(18,8)
         - interest_rate / coupon_rate → DECIMAL(10,4)
         - buy_amount / sell_amount (AC Deal/Cashflow) → DECIMAL(38,18)
      3. 对比 references/standards.md 「金额精度」表
    pass_criteria: 100% 金额/利率/汇率符合精度规范
    failure_action: 退回 TA 修改精度

  - id: DB-003
    name: 所有表都有注释/主键/审计字段/版本号
    severity: P0
    standard: Open-TMS CLAUDE.md 「Required Audit Fields」强制 — created_by/at,
              updated_by/at, version, deleted 必备;PG 强制 COMMENT ON TABLE/COLUMN
    check_method: |
      1. Read db/schema/{module}/{table}.sql;
      2. 验证每张表均包含:
         - id BIGSERIAL PRIMARY KEY
         - created_by VARCHAR(50) NOT NULL
         - created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
         - updated_by VARCHAR(50)
         - updated_at TIMESTAMP
         - version INT DEFAULT 0
         - deleted CHAR(1) DEFAULT '0'
         - COMMENT ON TABLE {table} IS '...'
      3. 验证核心列有 COMMENT ON COLUMN
    pass_criteria: 100% 表含完整审计字段 + 注释
    failure_action: 退回 TA 补齐

  # ============= 业界补充审核项 (FIS Quantum / Murex MX.3 / SAP TRM) =============

  - id: DB-004
    name: 表名遵循 tms_{module}_{type} 规范
    severity: P0
    standard: Open-TMS CLAUDE.md 「Database Naming」— tms_{module}_{type},
              type ∈ {t 主表 / d 字典 / log 日志 / rel 关联 / his 历史}
    check_method: |
      1. Grep 所有 CREATE TABLE,提取表名;
      2. 正则匹配 ^tms_[a-z_]+_(t|d|log|rel|his)$;
      3. module 名与所属 Maven 模块一致 (basedata / dealing / fx / ...)。
    pass_criteria: 100% 表名符合正则
    failure_action: 退回 TA 重命名

  - id: DB-005
    name: 外键约束 (FK 显式声明)
    severity: P1
    standard: Murex MX.3 / FIS Quantum 规范 — 跨表引用必须显式 FK,
              保证数据一致性 + 数据库层验证
    check_method: |
      1. 识别 *_id 字段 (counterparty_id / bank_account_id / instrument_id / ...);
      2. 验证是否声明 FOREIGN KEY 约束;
      3. 验证 ON DELETE / ON UPDATE 行为 (RESTRICT / CASCADE)。
    pass_criteria: 100% *_id 字段有显式 FK 约束
    failure_action: 补充 FK 约束

  - id: DB-006
    name: CHECK 约束 (业务规则在 DB 层强制)
    severity: P1
    standard: FIS Quantum 最佳实践 — 业务规则 (status in (...), amount > 0,
              rate > 0) 在 DB 层 CHECK,杜绝脏数据
    check_method: |
      1. Grep CHECK 约束;
      2. 识别可加 CHECK 的字段 (status / amount / rate / percentage);
      3. 验证是否对 status 字段加 IN 约束,对 amount > 0 加 CHECK。
    pass_criteria: 关键业务字段有 CHECK 约束
    failure_action: 补充 CHECK 约束

  - id: DB-007
    name: 索引合理性 (避免过度索引, 也不缺失)
    severity: P0
    standard: PostgreSQL 最佳实践 — 主键自动索引;外键必须有索引;
              频繁查询字段 (*_id / *_no / status / *_date) 必须有索引
    check_method: |
      1. Grep CREATE INDEX;
      2. 对照 *_id / *_no / status / *_date 字段,验证索引覆盖;
      3. 识别过度索引 (复合索引前缀已包含单列索引);
      4. 验证组合索引顺序 (高基数在前)。
    pass_criteria: 外键 + 业务查询字段 100% 有索引;无冗余索引
    failure_action: 补充缺失索引 / 删除冗余索引

  - id: DB-008
    name: NULL vs NOT NULL (默认值是否合理)
    severity: P0
    standard: Open-TMS 最佳实践 — 业务关键字段 NOT NULL;
              非关键字段允许 NULL 但必须有明确业务含义
    check_method: |
      1. Read DDL,识别每个字段的 NULL/NOT NULL 约束;
      2. 业务关键字段 (业务编码/名称/状态/外键) 必须 NOT NULL;
      3. 验证 NOT NULL 字段是否有 DEFAULT 值;
      4. 验证 NULL 字段是否确实允许空 (有业务解释)。
    pass_criteria: 关键字段 NOT NULL + DEFAULT;NULL 字段有文档化说明
    failure_action: 修改约束

  - id: DB-009
    name: 字符集统一 (PG utf8)
    severity: P2
    standard: PostgreSQL 默认 UTF8 (与 CLAUDE.md 一致) — 全库统一,
              无 ENCODING 显式声明也 OK
    check_method: |
      1. Grep CREATE DATABASE / ENCODING;
      2. 验证客户端编码 (PGCLIENTENCODING = UTF8);
      3. 验证 VARCHAR 字段对中文安全 (无长度限制过短)。
    pass_criteria: 字符集 UTF8;VARCHAR 长度对中文安全
    failure_action: 修改字符集 / 长度

  - id: DB-010
    name: 时间字段类型统一 (TIMESTAMP)
    severity: P1
    standard: Open-TMS CLAUDE.md 强制 — created_at / updated_at / operate_at
              必须 TIMESTAMP (PG 默认 TIMESTAMP WITHOUT TIME ZONE)
    check_method: |
      1. Grep TIMESTAMP / DATE / TIME 字段;
      2. 验证 *at 字段均为 TIMESTAMP;
      3. 验证 *date 字段均为 DATE;
      4. 验证无 TIME 类型单独使用 (应合并为 TIMESTAMP)。
    pass_criteria: 时间字段类型符合规范
    failure_action: 修改字段类型

  - id: DB-011
    name: 主键策略 (BIGSERIAL)
    severity: P1
    standard: Open-TMS CLAUDE.md 强制 — 主键 id BIGSERIAL PRIMARY KEY
              (PG 自增,无雪花/UUID)
    check_method: |
      1. Grep PRIMARY KEY;
      2. 验证 100% 均为 BIGSERIAL;
      3. 验证无 UUID / VARCHAR 主键。
    pass_criteria: 100% 主键为 BIGSERIAL
    failure_action: 修改主键策略

  - id: DB-012
    name: 字段长度是否合理
    severity: P1
    standard: FIS Quantum 最佳实践 — VARCHAR 长度应贴近实际业务,
              避免过长 (浪费空间) 或过短 (截断)
    check_method: |
      1. 列出所有 VARCHAR(n) 字段;
      2. 验证:
         - *_code VARCHAR(50) — 业务编码
         - *_no VARCHAR(50) — 业务流水号
         - *_name VARCHAR(50-200) — 名称
         - VARCHAR(500) — 备注/描述
         - VARCHAR(10) — 短码/币种
         - VARCHAR(30) — 电话/手机
      3. 验证无 VARCHAR(255) 这种含糊长度。
    pass_criteria: 字段长度符合业务实际
    failure_action: 调整长度

  - id: DB-013
    name: 软删除字段统一 (deleted CHAR(1) DEFAULT '0')
    severity: P0
    standard: Open-TMS CLAUDE.md 强制 — 全表 deleted CHAR(1) DEFAULT '0'
              (0=未删 1=已删),MyBatis Plus @TableLogic 自动处理
    check_method: |
      1. Grep deleted;
      2. 验证字段类型 CHAR(1) / DEFAULT '0';
      3. 验证无布尔类型 (BOOLEAN) 替代;
      4. 验证无 is_deleted / soft_delete 等别名。
    pass_criteria: 100% 表含统一软删除字段
    failure_action: 补齐 / 统一字段

  - id: DB-014
    name: 乐观锁字段 (version INT DEFAULT 0)
    severity: P0
    standard: Open-TMS CLAUDE.md 强制 — 全表 version INT DEFAULT 0,
              MyBatis Plus @Version 自动乐观锁
    check_method: |
      1. Grep version;
      2. 验证字段类型 INT / DEFAULT 0;
      3. 验证无 lock_version / revision 等别名。
    pass_criteria: 100% 表含乐观锁字段
    failure_action: 补齐字段

  - id: DB-015
    name: 跨表外键是否有索引
    severity: P0
    standard: PostgreSQL 最佳实践 — 外键列必须有索引,否则 JOIN/级联删除性能差
    check_method: |
      1. 列出所有外键字段 (*_id);
      2. 对照索引列表 (CREATE INDEX),验证每个外键都有索引;
      3. 验证组合索引 (a_id, b_id) 是否覆盖单个外键。
    pass_criteria: 100% 外键有索引
    failure_action: 补充索引

  - id: DB-016
    name: 表注释 + 列注释 100% 覆盖
    severity: P1
    standard: Open-TMS 强制 — COMMENT ON TABLE 必填;核心列 COMMENT ON COLUMN
    check_method: |
      1. Grep COMMENT ON TABLE — 100% 表有注释;
      2. Grep COMMENT ON COLUMN — 核心列(状态/类型/外键)有注释;
      3. 验证注释中文清晰,非空泛。
    pass_criteria: 表注释 100%,核心列注释 ≥80%
    failure_action: 补齐注释

  - id: DB-017
    name: 数据库对象命名 (索引/约束/序列) 一致性
    severity: P2
    standard: Open-TMS 最佳实践 — 索引 idx_{table}_{col},约束 pk_{table} / fk_{table}_{col} / uk_{table}_{col}
    check_method: |
      1. Grep CREATE INDEX,验证命名 idx_{table}_{col};
      2. Grep CONSTRAINT,验证命名规范;
      3. 验证无 idx_a / idx1 这类无意义命名。
    pass_criteria: 数据库对象命名规范统一
    failure_action: 重命名

  - id: DB-018
    name: 业务编号唯一约束 (xxx_no UNIQUE)
    severity: P0
    standard: Open-TMS CLAUDE.md 强制 — 业务流水号 (*_no) UNIQUE NOT NULL,
              例: deal_no / cashflow_no / approval_no
    check_method: |
      1. Grep *_no 字段,验证 NOT NULL UNIQUE;
      2. 验证业务编码 (*_code) 也 UNIQUE NOT NULL;
      3. 验证无遗漏唯一约束。
    pass_criteria: 业务编号 100% UNIQUE
    failure_action: 补充约束

  - id: DB-019
    name: 状态字段默认值 (Active/Inactive)
    severity: P1
    standard: Open-TMS CLAUDE.md — 基础数据表 status CHAR(1) DEFAULT '1'
              (1=Active 0=Inactive);业务表 status VARCHAR(20) DEFAULT 'New'
    check_method: |
      1. 识别状态字段 (status / enabled);
      2. 验证基础数据表 CHAR(1) DEFAULT '1';
      3. 验证业务表 (Deal/Action) VARCHAR(20) DEFAULT 'New' 或 'Pending';
      4. 验证状态值使用 GlobalConstants 中定义 (New / Active / ...)。
    pass_criteria: 状态字段类型/默认值统一
    failure_action: 修正状态字段

  - id: DB-020
    name: DECIMAL(38,18) 仅在必要时使用 (AC/Cashflow)
    severity: P2
    standard: Open-TMS CLAUDE.md — DECIMAL(38,18) 仅用于 AC Deal / Cashflow
              (高精度交易金额),其他场景严禁使用 (浪费空间 + 影响性能)
    check_method: |
      1. Grep DECIMAL(38,18);
      2. 验证仅在 tms_deal_t / tms_ac_deal_t / tms_cashflow_t 出现;
      3. 验证其他表未滥用 (其他金额用 DECIMAL(18,2))。
    pass_criteria: DECIMAL(38,18) 仅在必要处使用
    failure_action: 修改精度

  - id: DB-021
    name: management_entity 字段类型一致性
    severity: P0
    standard: 跨表引用管理主体(management_entity)时,字段类型必须统一 — management_entity_id BIGINT NOT NULL,严禁出现 management_entity_id 与 legal_entity_id 混用
    check_method: |
      1. Grep management_entity_id / legal_entity_id 在所有 DDL 中
      2. 验证所有引用处类型为 BIGINT NOT NULL
      3. 验证无 management_entity_code VARCHAR 替代(用 id 关联)
      4. 验证命名统一(严禁混用 management_entity_id / legal_entity_id)
    pass_criteria: 100% management_entity 引用统一使用 management_entity_id BIGINT
    failure_action: 统一字段类型与命名
```

---

## 审核流程 (Agent 可执行)

### Step 1: 范围确认

```bash
# 通过 Glob 定位待审核 DDL
db/schema/{module}/{table}.sql
```

### Step 2: 静态检查 (Read / Grep)

| 模式 | 用途 |
|------|------|
| `Read db/schema/{module}/*.sql` | 读取 DDL |
| `Grep "CREATE TABLE"` | 列出所有表 |
| `Grep "DECIMAL"` | 检查精度 |
| `Grep "VARCHAR"` | 检查字段长度 |
| `Grep "deleted\|version\|created_by"` | 检查审计字段 |
| `Grep "FOREIGN KEY"` | 检查 FK 约束 |
| `Grep "CREATE INDEX"` | 检查索引 |

### Step 3: 对标检查

对比 `db/schema/01-basedata.sql` / `db/schema/13-deal.sql` — M1 已贯通的 30+ 张表。

### Step 4: 评级与输出

- 按 3 级 (P0/P1/P2) 打标问题;
- 按 `templates/report.md` 输出报告;
- 按 `opentms-review-common` 评级:
  - 含 P0 → D (返工)
  - 含 P1 → C (修复后复审)
  - 仅 P2 → B (通过,记录待优化)
  - 无任何问题 → A

### Step 5: 整改建议

- 每个问题提供具体 DDL 修改片段;
- 标注预计工时;
- 归档到 `docs/reviews/{feature-name}/db-review.md`。

---

## 一票否决 (P0 直判 D)

- **DB-001**: 字段命名违规 (camelCase / 长度不合理) → D
- **DB-002**: 金额精度违规 (如 AC Deal 用 DECIMAL(18,2)) → D
- **DB-003**: 缺失审计字段或表注释 → D
- **DB-004**: 表名不符合 `tms_{module}_t` 规范 → D
- **DB-007**: 缺失外键索引 (影响性能) → D
- **DB-013**: 缺失 `deleted` 字段 → D
- **DB-014**: 缺失 `version` 字段 → D
- **DB-018**: 业务编号无 UNIQUE 约束 → D

---

## 协作关系

```
opentms-db-design (DB 设计)
   └─→ opentms-review-db (本次) ★ DB 审核
        └─→ opentms-review-api (API 设计)
             └─→ opentms-review-backend (后端开发)
```

---

## 相关文件

- `checklists/01-table-design.md` — 表设计清单
- `checklists/02-column-design.md` — 列设计清单
- `checklists/03-index-constraint.md` — 索引/约束清单
- `references/standards.md` — 业界对标参考
- `templates/report.md` — 审核报告模板
- `../../opentms-review-common/SKILL.md` — 公共规范
- `../../../CLAUDE.md` — Open-TMS 项目规范
- `../../../db/schema/` — 既有 DDL 参考

---

## 版本历史

| 版本 | 日期 | 变更内容 |
|------|------|---------|
| v1.0 | 2026-07-05 | 初始版本 — 20 项 DB 审核项 (8 P0 / 7 P1 / 5 P2) |
| v1.1 | 2026-07-05 | 新增 DB-021: management_entity 字段类型一致性 |
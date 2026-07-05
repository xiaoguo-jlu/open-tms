---
name: opentms-db-design
description: Use when designing Open-TMS database schema and table structures as Technical Architect
---

# Open-TMS 数据库设计 Skill (DB)

## 简介

本skill用于Open-TMS项目的数据库设计，指导架构师/开发人员完成从业务需求到数据表结构的完整流程。

---

## 一、触发条件

当需要进行以下工作时触发：PRD已确认需要设计数据表、已有功能需要新增/修改数据表、数据库变更管理、评审已有数据表设计。

触发信号：PM完成PRD评审分配数据库设计任务、Dev需要设计新数据表、TA要求审核数据表设计。

---

## 二、输入要求

### 2.1 必须输入

- **PRD文档**: PM提供 — 功能需求，明确数据需求
- **总体设计规范**: `docs/规范/Open-TMS开发规范文档.md`
- **模块schema**: `db/schema/` — 已有数据表结构
- **模块历史摘要**: `db/{模块}/SUMMARY.md` — 本模块历史数据库设计记录（若存在）
- **已有相关表**: `db/schema/*.sql`

### 2.2 可选输入

- **竞品调研**: FIS Quantum/SAP表结构参考
- **技术约束**: 数据库选型和技术约束
- **性能要求**: PM-Lead提供的性能指标

---

## 三、输出规范

### 3.1 交付件输出标准

每张表的设计文档必须包含: 表结构（字段名/类型/必填/说明）、索引设计、约束设计、ER关系。

完整表设计模板见 `references/table-design-template.md`。

### 3.2 DDL脚本标准

**重要：DROP TABLE 风险警示**

| 环境 | DDL策略 |
|------|---------|
| 开发/测试 | 使用 `DROP TABLE IF EXISTS` 保证脚本可重复执行 |
| 生产变更 | **禁止**使用 DROP TABLE，应使用 `ALTER TABLE` 增量变更 |

**@Version 注意事项（精简）**:
- 实体继承 `BaseEntity` 后携带 `@Version` 字段，MyBatis-Plus 在 UPDATE 时自动追加 `WHERE version = ?`
- 若 version 为 null 会导致 `MyBatisSystemException`
- **推荐做法**: 更新前先查询完整实体 → `BeanUtils.copyProperties` → 再 `updateById`
- **备选**: 使用 `JdbcTemplate` 手动执行 UPDATE，显式 `version = version + 1`

**DDL模板（使用 DROP IF EXISTS 保证可重复执行）**:
```sql
DROP TABLE IF EXISTS tms_{table}_t;
CREATE TABLE tms_{table}_t (
    id                  BIGSERIAL PRIMARY KEY,
    {table}_code        VARCHAR(50) NOT NULL UNIQUE,
    {table}_name        VARCHAR(200) NOT NULL,
    status              CHAR(1) NOT NULL DEFAULT '1',
    -- 公共审计字段
    created_by          VARCHAR(50) NOT NULL DEFAULT 'system',
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by          VARCHAR(50),
    updated_at          TIMESTAMP,
    version             INT DEFAULT 0,
    deleted             CHAR(1) DEFAULT '0'
);
COMMENT ON TABLE tms_{table}_t IS '{表描述}';
CREATE INDEX idx_{table}_code ON tms_{table}_t({table}_code);
```

### 3.3 存放路径规范

```
db/
├── schema/{序号}-{模块}.sql      # DDL脚本
├── migration/V{版本}__{描述}.sql  # 增量变更
└── docs/{模块}/                   # 数据库设计文档
```

### 3.4 设计摘要标准

每次完成后更新 `db/docs/{模块}/SUMMARY.md`，记录: 完成的表、遇到的问题、设计决策、ER关系变化、待确认事项。

---

## 四、执行步骤

### 步骤1：业界洞察

读取 `db/schema/` 了解现有表结构，研究FIS Quantum、SAP、Murex的数据库设计（表结构模式、命名规范、索引策略）。对标参考见 `references/db-patterns.md`。

### 步骤2：读取历史摘要

检查 `db/docs/{模块}/SUMMARY.md`，了解已完成数据表、设计决策、待优化结构。若为新模块则创建新摘要文件。

### 步骤3：读取PRD理解需求

详细阅读PRD，识别需要设计的数据表清单、跨表关联关系、需要确认的问题。

### 步骤4：概念模型设计（ER建模）

识别业务实体 → 确定属性 → 确定实体关系 → 范式评估（1NF/2NF/3NF）。

输出: ER图、实体清单、关系清单。

### 步骤5：逻辑模型设计（表结构设计）

1. **表命名**: > 详见 `CLAUDE.md` Database Naming 章节（主表 `_t`、字典 `_d`、日志 `_log`、关联 `_rel`、历史 `_his`）。
2. **字段设计**: 字段命名遵循项目规范，类型符合业务需求。
3. **金额精度**: > 详见 `CLAUDE.md` Amount Precision 章节。
4. **主键设计**: 交易表用 BIGSERIAL + 业务唯一键，配置表用 BIGSERIAL。
5. **索引设计**: 业务查询字段建立索引，唯一约束建唯一索引。
6. **审计字段**: > 详见 `CLAUDE.md` Required Audit Fields 章节（created_by, created_at, updated_by, updated_at, version, deleted）。交易表额外含幂等字段。

#### 5.1 management_entity 字段类型一致性检查

跨表引用 management_entity（管理主体）时，引用字段类型必须统一:
- 引用字段: `management_entity_id BIGINT NOT NULL`
- 关联表: `tms_management_entity_t` 的 id 字段（BIGSERIAL）
- 禁止使用: `management_entity_code VARCHAR`（应使用 id 关联，code 冗余）
- 所有引用 management_entity 的表，字段命名/类型必须一致，严禁 management_entity_id 与 legal_entity_id 混用

### 步骤6：检查设计一致性

对照 `docs/规范/Open-TMS开发规范文档.md` 检查: 命名规范、主键设计、字段类型、索引、审计字段、金额精度、约束完整性。

### 步骤7：性能评估

评估数据量增长趋势，识别高频查询字段，设计合适索引，考虑分区方案（如需），评估大表关联风险。

### 步骤8：创建GitHub Project工作项

创建数据库开发Task。具体命令 > 详见 `opentms-pm-lead` 第九节。

### 步骤9：生成设计摘要

更新 `db/docs/{模块}/SUMMARY.md`。

---

## 五、业界优秀实践

数据库设计原则: 规范化设计（3NF）、性能优先（适度反范式化）、可扩展性（VARCHAR编码）、安全性（敏感数据加密）。

金融系统特殊要求: 金额 DECIMAL(38,18) 非 FLOAT/DOUBLE、日期区分 DATE/TIMESTAMP 推荐UTC、状态 CHAR(1) 明确枚举、交易表含幂等键+状态机+审计日志。

> 详细设计模式和对标分析见 `references/db-patterns.md`。

---

## 六、与其他Skill的衔接

### 6.1 前置依赖: 产品设计(PM) 提供 PRD 文档

### 6.2 后续触发: 后端接口设计 → 后端代码开发 → 测试用例设计

### 6.3 协作流程: PRD → 数据库设计 → 后端接口设计 → 后端代码开发 → 测试用例设计

---

## 七、质量标准

### 7.1 数据库设计质量检查点

| 检查项 | 标准 | 权重 |
|--------|------|------|
| 命名规范符合性 | 符合项目命名规范 | 20% |
| 字段类型正确性 | 类型符合业务需求 | 20% |
| 索引设计合理性 | 高频查询有索引 | 15% |
| 约束完整性 | 必要的约束已添加 | 15% |
| 审计字段完整性 | 审计字段已包含 | 15% |
| 范式符合性 | 满足第三范式 | 15% |

### 7.2 量化指标

| 指标 | 目标值 | 最低值 |
|------|--------|--------|
| 规范符合率 | 100% | 95% |
| 字段定义完整率 | 100% | 95% |
| 索引覆盖率 | 100% | 90% |
| 约束完整率 | 100% | 90% |

### 7.3 评审通过标准

- [ ] 所有PRD中的数据需求已设计
- [ ] 表命名符合规范
- [ ] 字段类型正确、索引设计合理
- [ ] 审计字段完整
- [ ] 已创建数据库开发任务

---

## 八、交付物检查清单

### 8.1 DDL脚本

- [ ] 文件命名符合规范、存放路径正确
- [ ] 表结构完整、字段命名一致
- [ ] 索引创建正确、约束添加完整、注释说明清楚

### 8.2 设计摘要

- [ ] 已更新SUMMARY.md（含完成内容、问题、设计决策、ER关系变化、待确认事项）

### 8.3 GitHub工作项

- [ ] Task已创建，Label正确分配（TA,Task），验收标准明确

### 8.4 规范一致性

- [ ] 命名、主键设计、字段类型、金额精度、审计字段、索引设计符合规范

---

## 九、附录

- **附录A - 表设计模板**: 见 `references/table-design-template.md`
- **附录B - SUMMARY 模板**: 见原附录B
- **附录C - 字段类型规范**: > 详见 `CLAUDE.md` Amount Precision 章节
- **附录D - 表类型后缀**: > 详见 `CLAUDE.md` Database Naming 章节
- **附录E - 公共审计字段**: > 详见 `CLAUDE.md` Required Audit Fields 章节
- **业界实践**: 见 `references/db-patterns.md`

---

## 版本历史

| 版本 | 日期 | 变更内容 |
|------|------|---------|
| v1.0 | YYYY-MM-DD | 初始版本 |

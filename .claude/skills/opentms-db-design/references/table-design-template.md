# 数据库表设计模板

> 从 SKILL.md 附录A移出。

```markdown
# {模块名} 数据库设计

**模块**: {module}  
**版本**: v1.0  
**日期**: YYYY-MM-DD  
**设计师**: TA

---

## 一、表清单

| 序号 | 表名 | 说明 |
|------|------|------|
| 1 | tms_{entity}_t | {实体表} |
| 2 | tms_{dict}_d | {字典表} |

---

## 二、表结构详述

### tms_{entity}_t

**表说明**: {实体表}

**ER关系**:
- 通过 `{entity}_id` 关联 `tms_{related}_t`

**表结构**:
| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | BIGSERIAL | Y | 主键 |
| {entity}_code | VARCHAR(50) | Y | 编码，唯一 |
| {entity}_name | VARCHAR(200) | Y | 名称 |
| status | CHAR(1) | Y | 状态：1-启用，0-停用 |
| created_by | VARCHAR(50) | Y | 创建人 |
| created_at | TIMESTAMP | Y | 创建时间 |
| updated_by | VARCHAR(50) | N | 更新人 |
| updated_at | TIMESTAMP | N | 更新时间 |
| version | INT | Y | 乐观锁版本号 |
| deleted | CHAR(1) | Y | 软删除标记 |

**索引设计**:
| 索引名 | 类型 | 字段 | 说明 |
|--------|------|------|------|
| idx_{entity}_code | B-Tree | {entity}_code | 编码查询 |
| uidx_{entity}_code | Unique | {entity}_code | 唯一约束 |

**约束设计**:
| 约束类型 | 字段 | 条件 |
|----------|------|------|
| UNIQUE | {entity}_code | - |
| CHECK | status | status IN ('0', '1') |

**DDL**:
```sql
CREATE TABLE tms_{entity}_t (
    id                  BIGSERIAL PRIMARY KEY,
    {entity}_code       VARCHAR(50) NOT NULL UNIQUE,
    {entity}_name       VARCHAR(200) NOT NULL,
    status              CHAR(1) NOT NULL DEFAULT '1',
    created_by          VARCHAR(50) NOT NULL DEFAULT 'system',
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by          VARCHAR(50),
    updated_at          TIMESTAMP,
    version             INT DEFAULT 0,
    deleted             CHAR(1) DEFAULT '0'
);
COMMENT ON TABLE tms_{entity}_t IS '{实体表}';
CREATE INDEX idx_{entity}_code ON tms_{entity}_t({entity}_code);
```

---

## 三、ER图

```
┌─────────────────┐       ┌─────────────────┐
│ tms_entity_t    │       │ tms_related_t   │
├─────────────────┤       ├─────────────────┤
│ id (PK)         │       │ id (PK)         │
│ entity_code     │       │ related_code    │
│ entity_name     │──<───│ entity_id (FK)  │
│ status          │       │ related_name    │
└─────────────────┘       └─────────────────┘
```

---

*DB产出 - v{版本号}*
```

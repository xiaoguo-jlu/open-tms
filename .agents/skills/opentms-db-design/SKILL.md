---
name: opentms-db-design
description: Use when designing Open-TMS database schema and table structures as Technical Architect
---

# Open-TMS 数据库设计 Skill (DB)

## 简介

本skill用于Open-TMS项目的数据库设计，指导架构师/开发人员完成从业务需求到数据表结构的完整流程。

---

## 一、触发条件

**当需要进行以下工作时，触发本skill：**

- PRD已确认，需要设计数据表结构
- 已有功能需要新增/修改数据表
- 需要进行数据库变更管理
- 需要评审已有数据表设计

**触发信号：**
- PM完成PRD评审，分配数据库设计任务
- Dev需要设计新的数据表
- TA要求审核数据表设计
- 数据库变更需要评审

---

## 二、输入要求

### 2.1 必须输入

| 输入项 | 来源 | 说明 |
|--------|------|------|
| PRD文档 | PM提供 | 功能需求文档，明确数据需求 |
| 总体设计规范 | `docs/规范/Open-TMS开发规范文档.md` | 数据库设计规范 |
| 模块schema | `db/schema/` | 已有数据表结构 |
| 模块历史摘要 | `db/{模块}/SUMMARY.md` | 本模块历史数据库设计记录（若存在） |
| 已有相关表 | `db/schema/*.sql` | 同模块或相关模块的已有表结构 |

### 2.2 可选输入

| 输入项 | 来源 | 说明 |
|--------|------|------|
| 竞品数据库调研 | TA自行收集 | FIS Quantum/SAP表结构参考 |
| 技术约束 | TA提供 | 数据库选型和技术约束 |
| 性能要求 | PM-Lead提供 | 性能指标要求 |

---

## 三、输出规范

### 3.1 交付件输出标准

#### 3.1.1 数据库设计文档标准

每张表的设计文档必须包含：

```markdown
## {表名称}

### 表结构

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | BIGSERIAL | Y | 主键 |
| code | VARCHAR(50) | Y | 编码，唯一 |
| name | VARCHAR(200) | Y | 名称 |
| ... | ... | ... | ... |

### 索引设计

| 索引名 | 类型 | 字段 | 说明 |
|--------|------|------|------|
| idx_xxx | B-Tree | code | 编码查询 |
| uidx_xxx_code | Unique | code | 唯一约束 |

### 约束设计

| 约束类型 | 字段 | 条件 |
|----------|------|------|
| UNIQUE | code | - |
| CHECK | amount | amount > 0 |

### ER关系

- 关联表A：通过 `xxx_id` 关联 `tms_xxx_t`
- 关联表B：通过 `yyy_id` 关联 `tms_yyy_t`
```

#### 3.1.2 DDL脚本标准

**⚠️ 重要：DROP TABLE 风险警示**

| 环境 | DDL策略 |
|------|---------|
| 开发/测试 | 使用 `DROP TABLE IF EXISTS` 保证脚本可重复执行 |
| 生产变更 | **禁止**使用 DROP TABLE，应使用 `ALTER TABLE` 增量变更 |

**⚠️ 重要：应用层 @Version 注解注意事项**

实体类继承 `BaseEntity` 后会携带 `@Version` 字段，配合 MyBatis-Plus 的乐观锁机制。但在实际使用中存在以下问题：

1. **MyBatis-Plus updateById 的乐观锁问题**：
   - 当实体有 `@Version` 字段时，MyBatis-Plus 会在 UPDATE 时自动追加 `WHERE version = ?`
   - 如果 version 字段为 null，SQL 会变成 `WHERE version = null`（永远不匹配）
   - 导致 `MyBatisSystemException: could not resolve property`

2. **解决方案**：
   - **方案A（推荐）**：更新前先查询完整实体，再更新
     ```java
     // 不要这样用
     entity.setVersion(null);
     mapper.updateById(entity);
     
     // 应该这样用
     Entity existing = mapper.selectById(entity.getId());
     BeanUtils.copyProperties(entity, existing);
     mapper.updateById(existing);
     ```
   
   - **方案B**：使用直接JDBC执行更新，手动处理version
     ```java
     @Autowired
     private JdbcTemplate jdbcTemplate;
     
     public boolean updateById(Entity entity) {
         String sql = "UPDATE tms_currency_t SET currency_code = ?, currency_name = ?, " +
                      "updated_by = ?, updated_at = CURRENT_TIMESTAMP, version = version + 1 " +
                      "WHERE id = ? AND deleted = '0'";
         return jdbcTemplate.update(sql, entity.getCode(), entity.getName(), 
                entity.getUpdatedBy(), entity.getId()) > 0;
     }
     ```

3. **ID生成流程说明**：
   - 数据库使用 `BIGSERIAL` 自增
   - 应用层 new Entity() 时 id = null
   - baseMapper.insert(entity) 后，entity.getId() 才有值
   - return 时可以获取到完整的ID

**使用DROP IF EXISTS写法先删后增，保证DDL脚本永远可重复执行**
```sql
-- ============================================
-- {表描述}
-- 模块: {module}
-- 创建时间: YYYY-MM-DD
-- ============================================
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
DROP INDEX IF EXISTS idx_{table}_code ON tms_{table}_t({table}_code);
CREATE INDEX idx_{table}_code ON tms_{table}_t({table}_code);
```

### 3.2 存放路径规范

```
db/
├── schema/                              # DDL脚本根目录
│   ├── 00-init.sql                     # 初始化脚本
│   ├── 01-basedata.sql                # M0基础数据模块
│   ├── 02-dealing.sql                  # M1交易管理模块
│   ├── 03-bankaccount.sql             # M1银行账户模块
│   ├── 04-instrument.sql              # M2金融工具模块
│   ├── 05-fundplan.sql               # M2资金计划模块
│   ├── 06-cashpool.sql               # M2现金池模块
│   ├── 07-settlement.sql             # M2支付结算模块
│   ├── 08-limit.sql                  # M2限额管理模块
│   ├── 09-fx.sql                     # M3外汇交易模块
│   ├── 10-irs.sql                    # M3利率掉期模块
│   ├── 11-valuation.sql              # M3估值模块
│   ├── 12-exposure.sql               # M4敞口管理模块
│   ├── 13-hedge.sql                  # M4套期保值模块
│   ├── 14-impairment.sql             # M4减值计算模块
│   ├── 15-var.sql                    # M4 VaR模块
│   ├── 16-cockpit.sql                # M5驾驶舱模块
│   ├── 17-report.sql                # M5报表模块
│   └── docs/                         # 数据库设计文档
│       ├── SUMMARY.md                # 数据库设计历史摘要
│       └── ...
├── migration/                         # 变更脚本
│   ├── V1__add_xxx.sql
│   ├── V2__modify_xxx.sql
│   └── docs/
│       └── SUMMARY.md
└── docs/                              # 数据库设计文档根目录
    ├── common/                        # 公共模块设计
    │   └── docs/
    └── {模块}/                       # 各模块设计
        └── docs/
```

**路径选择规则：**

| 类型 | 存放路径 |
|------|----------|
| DDL脚本 | `db/schema/{序号}-{模块}.sql` |
| 数据库设计文档 | `db/docs/{模块}/` |
| 增量变更 | `db/migration/V{版本}__{描述}.sql` |
| 公共表 | `db/schema/00-init.sql` |

### 3.3 设计摘要标准

每次完成一组表设计后，生成设计摘要，存放在模块的`db/docs/{模块}/SUMMARY.md`：

```
# {模块名} 数据库设计摘要

## 最近更新
- **日期**: YYYY-MM-DD
- **设计师**: TA/Dev
- **本次完成**: {表列表}

## 设计过程记录

### YYYY-MM-DD - {本次主题}
**完成内容**:
- {已完成的数据表1}
- {已完成的数据表2}

**遇到的问题**:
- {问题1} → {解决方案}
- {问题2} → {解决方案}

**设计决策**:
- {决策1}: {原因}
- {决策2}: {原因}

**ER关系变化**:
- {表A} 新增字段 → 关联 {表B}
- {表C} 拆分为 {表C1} 和 {表C2}

**待确认事项**:
- {待确认事项1}
- {待确认事项2}

### 历史记录
- YYYY-MM-DD: {设计主题} - 完成{表列表}
```

---

## 四、执行步骤

### 步骤1：业界洞察

**目的**：了解业界专业资金系统的数据库设计，吸收优秀实践。

**操作**：

1. 读取 `db/schema/` 了解现有表结构
2. 研究业界产品（FIS Quantum、SAP、Murex）的数据库设计
3. 分析竞品的：
   - 表结构设计模式
   - 命名规范
   - 索引设计策略
   - 分库分表方案
4. 记录关键设计亮点作为参考

**输出**：
- 在设计文档中记录对标参考

### 步骤2：读取历史摘要

**目的**：了解本模块已有数据表设计，避免重复和冲突。

**操作**：

1. 检查是否存在历史摘要：`db/docs/{模块}/SUMMARY.md`
2. 若存在，读取摘要内容
3. 了解：
   - 已完成哪些数据表
   - 设计过程中遇到的问题及解决方案
   - 已有设计决策
   - 需要优化的表结构

**若为新模块**：
- 创建新的摘要文件
- 在设计开头注明"本模块为新建模块"

### 步骤3：读取PRD理解需求

**目的**：深入理解功能需求，确保数据表设计满足业务场景。

**操作**：

1. 详细阅读PM提供的PRD文档
2. 识别需要设计的数据表清单
3. 理解数据关系和业务规则
4. 标注需要与PM/TA确认的问题

**输出**：
- 确认需要设计的数据表范围
- 识别跨表关联关系
- 列出需要与PM确认的问题

### 步骤4：概念模型设计（ER建模）

**目的**：建立业务实体的概念模型，理清实体关系。

**操作**：

1. **识别实体**
   - 从PRD中识别业务实体（如：银行、账户、交易）
   - 确定实体的属性
   - 确定实体之间的关系

2. **ER建模**
   ```
   业务单元 (1) ─────< (N) 交易员
           │
           │
           < (N) 账户 >──── (1) 银行
                  │
                  │
                  < (N) 交易 >──── (1) 交易对手
   ```

3. **范式评估**
   - 1NF：字段原子性
   - 2NF：非主键字段完全依赖主键
   - 3NF：非主键字段之间无传递依赖

**输出**：
- ER图
- 实体清单
- 关系清单

### 步骤5：逻辑模型设计（表结构设计）

**目的**：输出符合项目规范的表结构设计。

**操作**：

1. **表命名**
   - 主表：`tms_{module}_{entity}_t`
   - 字典表：`tms_{module}_{dict}_d`
   - 日志表：`tms_{module}_{biz}_log`
   - 关联表：`tms_{module}_{rela}_rel_t`

2. **字段设计**
   - 字段命名遵循项目规范
   - 字段类型符合业务需求
   - 金额字段使用DECIMAL
   - 日期字段区分DATE和TIMESTAMP

3. **主键设计**
   - 交易表：使用BIGSERIAL自增主键 + 业务唯一键
   - 配置表：使用BIGSERIAL自增主键
   - 分布式场景：考虑使用UUID

4. **索引设计**
   - 业务查询字段建立索引
   - 唯一约束建立唯一索引
   - 复合索引考虑查询频率

5. **约束设计**
   - NOT NULL约束
   - UNIQUE约束
   - CHECK约束（金额>0、状态枚举）

6. **审计字段**
   - 所有表包含：`created_by, created_at, updated_by, updated_at, version, deleted`
   - 交易表额外包含幂等字段

7. **遵循** `docs/规范/Open-TMS开发规范文档.md` 中的数据库规范

**输出**：
- DDL脚本
- 表设计文档

### 步骤6：检查设计一致性

**目的**：确保数据表设计符合项目总体设计规范。

**操作**：

1. 读取 `docs/规范/Open-TMS开发规范文档.md` 中的数据库设计规范
2. 对照检查表设计中的：
   - [ ] 命名是否符合规范
   - [ ] 主键设计是否合理
   - [ ] 字段类型是否正确
   - [ ] 索引是否正确
   - [ ] 审计字段是否完整
   - [ ] 金额字段精度是否正确
   - [ ] 约束是否完整

**检查结果处理**：

| 检查结果 | 处理方式 |
|----------|----------|
| 符合规范 | 进入下一步 |
| 不符合规范，且设计不合理 | 修改设计至符合规范 |
| 不符合规范，但设计合理 | 弹出询问用户 |

### 步骤7：性能评估

**目的**：评估表设计是否满足性能要求。

**操作**：

1. 评估数据量增长趋势
2. 识别高频查询字段
3. 设计合适的索引
4. 考虑分区方案（如需要）
5. 评估是否存在大表关联风险

### 步骤8：创建GitHub Project工作项

**目的**：按照团队协作规范，创建对应的Task工作项。

**操作**：

根据数据库设计内容，在GitHub Projects创建工作项：

1. **Task**：数据库开发任务
   ```bash
   gh issue create --title "[{模块}-TA] {表名}表设计" --body "## 表设计\n{表名}\n\n## DDL\n```sql\n{CREATE TABLE语句}\n```\n\n## 验收标准\n- [ ] DDL执行成功\n- [ ] 索引创建正确\n- [ ] 初始数据插入" --label "TA,Task"
   ```

2. **US**：用户故事（若涉及功能）
   ```bash
   gh issue create --title "[US] {用户角色}可操作{功能}" --body "## 数据需求\n{数据表需求描述}" --label "TA,US"
   ```

### 步骤9：生成设计摘要

**目的**：记录数据库设计过程，便于后续追溯和团队共享。

**操作**：

1. 更新模块的 `SUMMARY.md`
2. 若模块无SUMMARY.md，则创建
3. 记录内容：
   - 本次完成的数据表
   - 遇到的问题及解决方案
   - 设计决策说明
   - ER关系变化
   - 待确认事项

---

## 五、业界优秀实践

### 5.1 数据库设计原则

**1. 规范化设计**
- 满足第三范式（3NF）
- 避免数据冗余
- 确保数据完整性

**2. 性能优先**
- 高频查询字段建立索引
- 避免过度规范化（反范式化）
- 考虑读写分离

**3. 可扩展性**
- 字段预留扩展空间
- 编码字段使用VARCHAR
- 避免硬编码

**4. 安全性**
- 敏感数据加密存储
- 审计字段完整
- 权限控制

### 5.2 金融系统数据库特殊要求

**1. 金额、利率、汇率字段**
- 精度要求：DECIMAL(38,18)
- 必须有CHECK约束 > 0
- 避免使用FLOAT/DOUBLE

**2. 日期时间字段**
- 业务日期使用DATE类型
- 业务时间使用TIMESTAMP类型
- 考虑时区存储（推荐UTC）

**3. 状态字段**
- 使用CHAR(1)或VARCHAR
- 明确的枚举值
- 清晰的命名（status而非state）

**4. 交易表设计**
- 幂等键（唯一业务编码）
- 状态机控制
- 审计日志

### 5.3 对标系统参考

| 系统 | 特色 | Open-TMS借鉴 |
|------|------|--------------|
| FIS Quantum | 高性能交易表设计 | 交易表分区、索引优化 |
| Murex | 实时估值数据 | 内存数据库+持久化 |
| SAP | 完整审计追踪 | 变更日志表设计 |

---

## 六、与其他Skill的衔接

### 6.1 前置依赖

| 前置Skill | 依赖内容 | 说明 |
|-----------|----------|------|
| 产品设计(PM) | PRD文档 | 明确数据需求 |

### 6.2 后续触发

| 后续Skill | 触发条件 | 输出 |
|-----------|----------|------|
| 后端接口设计 | 表结构已确定 | API文档 |
| 后端代码开发 | 表结构已确定 | 代码实现 |
| 测试用例设计 | 功能可用 | 测试用例 |

### 6.3 协作流程

```
PRD (PM)
    │
    ▼
┌─────────────────┐
│  数据库设计     │
│  (本skill)     │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  后端接口设计   │ ◄── API文档
│  (API Design)  │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  后端代码开发   │
└────────┬────────┘
         │
         ▼
   ┌───────────┐
   │ 测试用例设计 │
   └───────────┘
```

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
- [ ] 字段类型正确
- [ ] 索引设计合理
- [ ] 审计字段完整
- [ ] 已创建数据库开发任务

---

## 八、交付物检查清单

### 8.1 DDL脚本检查

- [ ] 文件命名符合规范：`{序号}-{模块}.sql`
- [ ] 存放路径正确
- [ ] 表结构完整
- [ ] 字段命名一致
- [ ] 索引创建正确
- [ ] 约束添加完整
- [ ] 注释说明清楚

### 8.2 设计摘要检查

- [ ] 已更新SUMMARY.md
- [ ] 包含本次完成内容
- [ ] 包含遇到的问题
- [ ] 包含设计决策
- [ ] 包含ER关系变化
- [ ] 包含待确认事项

### 8.3 GitHub工作项检查

- [ ] Task已创建（按表拆分）
- [ ] Label正确分配（TA,Task）
- [ ] 验收标准明确

### 8.4 规范一致性检查

- [ ] 命名符合规范
- [ ] 主键设计合理
- [ ] 字段类型正确
- [ ] 金额精度正确
- [ ] 审计字段完整
- [ ] 索引设计合理

---

## 九、附录

### 附录A：表设计模板

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

### 附录B：SUMMARY.md模板

```markdown
# {模块名} 数据库设计摘要

## 最近更新
- **日期**: YYYY-MM-DD
- **设计师**: TA/Dev
- **本次完成**: {表列表}

---

## 设计过程记录

### YYYY-MM-DD - {本次主题}
**完成内容**:
- {已完成的数据表1}
- {已完成的数据表2}

**遇到的问题**:
- {问题1} → {解决方案}
- {问题2} → {解决方案}

**设计决策**:
- {决策1}: {原因}
- {决策2}: {原因}

**ER关系变化**:
- {表A} 新增字段 → 关联 {表B}

**待确认事项**:
- {待确认事项1}
- {待确认事项2}

---

## 历史记录

| 日期 | 主题 | 完成内容 | 备注 |
|------|------|----------|------|
| YYYY-MM-DD | {主题} | {完成内容} | {备注} |
```

### 附录C：字段类型规范

| 业务类型 | PostgreSQL类型 | 精度/长度 | 说明 |
|----------|----------------|-----------|------|
| 金额 | DECIMAL | (18,2) | 2位小数 |
| 汇率 | DECIMAL | (18,8) | 8位小数 |
| 利率 | DECIMAL | (10,4) | 4位小数 |
| 余额 | DECIMAL | (18,2) | 2位小数 |
| 状态 | CHAR | (1) | 0/1 |
| 编码 | VARCHAR | (50) | 业务编码 |
| 流水号 | VARCHAR | (50) | 唯一流水号 |
| 名称 | VARCHAR | (200) | 名称描述 |
| 日期 | DATE | - | 业务日期 |
| 时间戳 | TIMESTAMP | - | 带时区时间 |
| 审计时间 | TIMESTAMP | - | 操作时间 |

### 附录D：表类型后缀规范

| 后缀 | 含义 | 示例 |
|------|------|------|
| `_t` | 主表/实体表 | tms_user_t |
| `_d` | 字典表/配置表 | tms_dict_d |
| `_log` | 日志表 | tms_audit_log |
| `_rel` | 关联表 | tms_user_role_rel |
| `_his` | 历史表 | tms_transaction_his |
| `_tmp` | 临时表 | tms_calc_tmp |

### 附录E：公共审计字段

所有表必须包含以下字段：

```sql
created_by    VARCHAR(50)   NOT NULL DEFAULT 'system'  -- 创建人
created_at    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP  -- 创建时间
updated_by    VARCHAR(50)                              -- 更新人
updated_at    TIMESTAMP                                -- 更新时间
version       INT           DEFAULT 0                  -- 乐观锁版本号
deleted       CHAR(1)       DEFAULT '0'                -- 软删除标记
```

交易表额外包含：

```sql
transaction_no  VARCHAR(50)  NOT NULL UNIQUE  -- 业务流水号（幂等键）
idempotency_key VARCHAR(64)                     -- 外部幂等key
```

---

## 版本历史

| 版本 | 日期 | 变更内容 |
|------|------|---------|
| v1.0 | YYYY-MM-DD | 初始版本 |
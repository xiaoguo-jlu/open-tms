# business_unit 残留审计报告

> 日期: 2026-07-12
> 工具: `scripts/scan_business_unit.py`
> 评级: **A** (无残留)
> 总残留: **287** 处 (P0=0 / P1=129 / P2=158)

> **背景**: 历史 commit `23-rename-business-unit.sql` + `23b-rename-business-unit-cleanup.sql`
> 已将 DB 中 `business_unit` 重命名为 `management_entity`。本报告扫描系统是否仍有残留。

## 1. 总览

| 类别 | 命中数 | P0 | P1 | P2 |
|------|--------|----|----|----|
| Java 代码 | 0 | 0 | 0 | 0 |
| 前端代码 | 0 | 0 | 0 | 0 |
| 文档 | 287 | 0 | 129 | 158 |
| 数据库 | 0 | 0 | 0 | 0 |
| **合计** | **287** | **0** | **129** | **158** |

## 2. 详情

### 2.1 Java (0 处)

> 无残留

### 2.2 前端 (0 处)

> 无残留

### 2.3 文档 (287 处)

| 文件 / 对象 | 行 | 模式 | 严重度 | 命中内容 | 建议 |

|-------------|----|------|--------|----------|------|

| `docs/CHANGELOG.md` | 68 | PascalCase | P1 | `- Java: `BusinessUnit` → `ManagementEntity`` | 替换为 `ManagementEntity` |

| `docs/api/cashflow-enhance-API.md` | 145 | PascalCase | P1 | `req.setManagementEntityId(cf.getBusinessUnitId());` | 替换为 `ManagementEntity` |

| `docs/architecture/business/AC交易与现金流分离架构设计.md` | 70 | camelCase | P1 | `\| businessUnit \| VARCHAR(50) \| 业务单元 \|` | 替换为 `managementEntity` |

| `docs/architecture/business/AC交易与现金流分离架构设计.md` | 103 | camelCase | P1 | `\| businessUnit \| VARCHAR(50) \| 业务单元 \|` | 替换为 `managementEntity` |

| `docs/architecture/business/AC交易与现金流分离架构设计.md` | 137 | camelCase | P1 | `\| businessUnit \| VARCHAR(50) \| 业务单元 \|` | 替换为 `managementEntity` |

| `docs/architecture/business/AC交易与现金流分离架构设计.md` | 455 | camelCase | P1 | `\| BusinessUnit \| ACDeal.businessUnit, Cashflow.businessUnit \|` | 替换为 `managementEntity` |

| `docs/architecture/business/AC交易与现金流分离架构设计.md` | 455 | PascalCase | P1 | `\| BusinessUnit \| ACDeal.businessUnit, Cashflow.businessUnit \|` | 替换为 `ManagementEntity` |

| `docs/archive/2026-07-04-redundant-versions/M1-AC交易ActualCashflow PRD v2.md` | 150 | PascalCase | P1 | `\| 业务单元(BusinessUnit) \| 现金流所属业务单元 \| 组织架构 \|` | 替换为 `ManagementEntity` |

| `docs/archive/2026-07-04-redundant-versions/M1-Deal交易 PRD v4.md` | 35 | camelCase | P1 | `\| businessUnit \| VARCHAR(50) \| Y \| 关联业务单元代码 \|` | 替换为 `managementEntity` |

| `docs/archive/2026-07-04-redundant-versions/M1-Deal交易 PRD v4.md` | 279 | camelCase | P1 | `\| businessUnit \| 业务单元选择器 \| businessUnit表，status=Active \|` | 替换为 `managementEntity` |

| `docs/archive/2026-07-04-redundant-versions/M1-Deal交易 PRD v4.md` | 421 | camelCase | P1 | `\| **BusinessUnit** \| 交易归属 \| businessUnit字段 \|` | 替换为 `managementEntity` |

| `docs/archive/2026-07-04-redundant-versions/M1-Deal交易 PRD v4.md` | 421 | PascalCase | P1 | `\| **BusinessUnit** \| 交易归属 \| businessUnit字段 \|` | 替换为 `ManagementEntity` |

| `docs/conversations/00867d0c-8954-482e-ad1e-d6122e0e60f9.md` | 796 | PascalCase | P1 | `- `BusinessUnitList.vue`, `CounterpartyAccountList.vue` 修改` | 替换为 `ManagementEntity` |

| `docs/conversations/00867d0c-8954-482e-ad1e-d6122e0e60f9.md` | 920 | PascalCase | P1 | `\| basedata \| ✅ 已完成 \| ✅ Bank/BusinessUnit/Currency/Country/Holiday/Trader \|` | 替换为 `ManagementEntity` |

| `docs/conversations/3a73c9ba-a60d-4e56-8d2d-94f205420fb8.md` | 2005 | camelCase | P1 | `这是 PR `69c34ab "Entity 与 DB 字段对齐"` 的**真实失败场景**:Java Entity 加了 `businessUnitId`(因为业务需要),但**DB 没有相应 `ALTER TABLE`** — 这次 P` | 替换为 `managementEntity` |

| `docs/conversations/3a73c9ba-a60d-4e56-8d2d-94f205420fb8.md` | 2034 | camelCase | P1 | `"command": "grep -E \"^\\s*\\*.*business_unit\|business_unit\" db/schema/*.sql \| head -10; echo \"---\"; grep \"busines` | 替换为 `managementEntity` |

| `docs/conversations/3a73c9ba-a60d-4e56-8d2d-94f205420fb8.md` | 2076 | camelCase | P1 | `"command": "cd /f/code/opencode/opentrm && grep -rE \"business_unit_id\|businessUnitId\" db/schema/ basedata/src/main/ja` | 替换为 `managementEntity` |

| `docs/conversations/3a73c9ba-a60d-4e56-8d2d-94f205420fb8.md` | 2152 | camelCase | P1 | `- 但 PR `69c34ab` 的目标是**让 Entity 与 DB 对齐**,应当把 Java 端 `businessUnitId` 改成 `managementEntityId`` | 替换为 `managementEntity` |

| `docs/conversations/3a73c9ba-a60d-4e56-8d2d-94f205420fb8.md` | 2163 | camelCase | P1 | `"command": "grep -E \"businessUnitId\|managementEntityId\" basedata/src/main/java/com/opentms/basedata/entity/BankAccoun` | 替换为 `managementEntity` |

| `docs/conversations/3a73c9ba-a60d-4e56-8d2d-94f205420fb8.md` | 2231 | camelCase | P1 | `"description": "把 BankAccount.businessUnitId 改回 managementEntityId(对应 DB 列),改完后端能正确查询",` | 替换为 `managementEntity` |

| `docs/conversations/3a73c9ba-a60d-4e56-8d2d-94f205420fb8.md` | 2480 | camelCase | P1 | `"command": "grep -E \"businessUnitId\|managementEntityId\" basedata/src/main/java/com/opentms/basedata/entity/BankAccoun` | 替换为 `managementEntity` |

| `docs/conversations/3a73c9ba-a60d-4e56-8d2d-94f205420fb8.md` | 2512 | camelCase | P1 | `"command": "grep -E \"businessUnitId\|managementEntityId\|@TableField\" basedata/src/main/java/com/opentms/basedata/enti` | 替换为 `managementEntity` |

| `docs/conversations/3a73c9ba-a60d-4e56-8d2d-94f205420fb8.md` | 2601 | camelCase | P1 | `"command": "grep -B 2 -A 1 \"@TableField.*code\\\|businessUnitCode\\\|managementEntityCode\" basedata/src/main/java/com/` | 替换为 `managementEntity` |

| `docs/conversations/3a73c9ba-a60d-4e56-8d2d-94f205420fb8.md` | 5168 | camelCase | P1 | `return bankAccountList.value.filter(a =&gt; a.businessUnitId === editForm.managementEntityId)` | 替换为 `managementEntity` |

| `docs/conversations/3a73c9ba-a60d-4e56-8d2d-94f205420fb8.md` | 5172 | camelCase | P1 | `这里**字段不匹配 (F)**:第 300 行按 `businessUnitId` 过滤,但 curl 实测**后端返回的是 `managementEntityId`**(7 条记录均有 `managementEntityId`,**没有 ` | 替换为 `managementEntity` |

| `docs/conversations/3a73c9ba-a60d-4e56-8d2d-94f205420fb8.md` | 5174 | camelCase | P1 | `而 `handleAdd` 第 358 行自动用 `queryForm.managementEntityId`(前一次查询时用户选的主体)预设给 `editForm.managementEntityId`,所以**几乎所有情况下** `ed` | 替换为 `managementEntity` |

| `docs/conversations/3a73c9ba-a60d-4e56-8d2d-94f205420fb8.md` | 5195 | camelCase | P1 | `\| 默认账户(过滤条件) \| `item.businessUnitId` \| **`managementEntityId`** \| ✗ **不匹配** ← **根因** \|` | 替换为 `managementEntity` |

| `docs/conversations/3a73c9ba-a60d-4e56-8d2d-94f205420fb8.md` | 5216 | camelCase | P1 | `-  // BankAccount 字段已对齐 DB:businessUnitId(原 managementEntityId)` | 替换为 `managementEntity` |

| `docs/conversations/3a73c9ba-a60d-4e56-8d2d-94f205420fb8.md` | 5217 | camelCase | P1 | `-  return bankAccountList.value.filter(a =&gt; a.businessUnitId === editForm.managementEntityId)` | 替换为 `managementEntity` |

| `docs/conversations/3a73c9ba-a60d-4e56-8d2d-94f205420fb8.md` | 5255 | camelCase | P1 | `4. **`filteredBankAccounts` 字段名漂移遗留的误导性注释**(第 299 行):"`BankAccount 字段已对齐 DB:businessUnitId(原 managementEntityId)`" 这条注释本` | 替换为 `managementEntity` |

| `docs/conversations/3a73c9ba-a60d-4e56-8d2d-94f205420fb8.md` | 5303 | camelCase | P1 | `"old_string": "const filteredBankAccounts = computed(() => {\n  if (!editForm.managementEntityId) return bankAccountList` | 替换为 `managementEntity` |

| `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md` | 10874 | camelCase | P1 | `"command": "echo \"=== 端到端验证 ===\"\necho \"--- 1) 创建一个 AC 交易,自动填充 + 写 CREATE 镜像 ---\"\nAC_RESP=$(curl -s -X POST http://` | 替换为 `managementEntity` |

| `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md` | 10922 | camelCase | P1 | `"command": "cat > /tmp/ac_create.json << 'EOF'\n{\n  \"businessUnit\":\"BU_HEAD\",\n  \"sourceAccount\":\"SH_CNY_001\",\` | 替换为 `managementEntity` |

| `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md` | 21798 | camelCase | P1 | `"description": "写 scripts/scan_business_unit.py:扫描 4 个层面残留:1) Java 代码(@Column/Entity/VO 字段名 businessUnit) 2) 前端 .vue/.js` | 替换为 `managementEntity` |

| `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md` | 21855 | camelCase | P1 | `1. **Java 代码**:`**/*.java` in basedata/dealing/common,找 `businessUnit` / `@Column("business_unit")` / `getBusinessUnit`` | 替换为 `managementEntity` |

| `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md` | 21855 | PascalCase | P1 | `1. **Java 代码**:`**/*.java` in basedata/dealing/common,找 `businessUnit` / `@Column("business_unit")` / `getBusinessUnit`` | 替换为 `ManagementEntity` |

| `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md` | 21856 | camelCase | P1 | `2. **前端代码**:`web/src/**/*.{vue,js,ts}`,找 `businessUnitId` / 变量 / prop` | 替换为 `managementEntity` |

| `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md` | 21913 | camelCase | P1 | `- **Java**: 6 处。`@TableField("business_unit")` / `@TableField("business_unit_code")` / `private String businessUnit` 在 `` | 替换为 `managementEntity` |

| `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md` | 21924 | camelCase | P1 | `6. `dealing/src/main/java/com/opentms/dealing/entity/CashflowImage.java:51-52` — `@TableField("business_unit")` + `priva` | 替换为 `managementEntity` |

| `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md` | 21928 | camelCase | P1 | `- 4 种命名模式: `business_unit` / `businessUnit` / `BUSINESS_UNIT` / `BusinessUnit`` | 替换为 `managementEntity` |

| `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md` | 21928 | PascalCase | P1 | `- 4 种命名模式: `business_unit` / `businessUnit` / `BUSINESS_UNIT` / `BusinessUnit`` | 替换为 `ManagementEntity` |

| `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md` | 22156 | camelCase | P1 | `"command": "cat docs/reviews/business-unit-audit.md \| grep -E \"tms_cashflow\|trm_business\|tms_subsidiary.*business_un` | 替换为 `managementEntity` |

| `docs/conversations/b04bc295-2776-422a-9dde-2770d157fe9a.md` | 8364 | PascalCase | P1 | `- `F:/code/opencode/opentrm/web/src/views/basedata/BusinessUnitList.vue`` | 替换为 `ManagementEntity` |

| `docs/conversations/b04bc295-2776-422a-9dde-2770d157fe9a.md` | 8425 | PascalCase | P1 | `- `web/src/views/basedata/BusinessUnitList.vue`` | 替换为 `ManagementEntity` |

| `docs/conversations/b04bc295-2776-422a-9dde-2770d157fe9a.md` | 8431 | camelCase | P1 | `> **注意：** `businessUnit.js` API 保留，因为其他页面（交易列表、银行账户等）还在使用 `listBusinessUnit`。` | 替换为 `managementEntity` |

| `docs/conversations/b04bc295-2776-422a-9dde-2770d157fe9a.md` | 8431 | PascalCase | P1 | `> **注意：** `businessUnit.js` API 保留，因为其他页面（交易列表、银行账户等）还在使用 `listBusinessUnit`。` | 替换为 `ManagementEntity` |

| `docs/conversations/b04bc295-2776-422a-9dde-2770d157fe9a.md` | 8671 | PascalCase | P1 | `entityList.value = (await listBusinessUnit({ pageSize: 1000 })).data.list \|\| []` | 替换为 `ManagementEntity` |

| `docs/conversations/b04bc295-2776-422a-9dde-2770d157fe9a.md` | 8677 | PascalCase | P1 | `entityList.value = (await listBusinessUnit({ pageSize: 1000 })).data.records \|\| []` | 替换为 `ManagementEntity` |

| `docs/conversations/b04bc295-2776-422a-9dde-2770d157fe9a.md` | 8774 | PascalCase | P1 | `"old_string": "const fetchLists = async () => {\n  entityList.value = (await listBusinessUnit({ pageSize: 1000 })).data.` | 替换为 `ManagementEntity` |

| `docs/conversations/b04bc295-2776-422a-9dde-2770d157fe9a.md` | 8775 | PascalCase | P1 | `"new_string": "const fetchLists = async () => {\n  entityList.value = (await listBusinessUnit({ pageSize: 1000 })).data.` | 替换为 `ManagementEntity` |

| `docs/conversations/b04bc295-2776-422a-9dde-2770d157fe9a.md` | 9008 | PascalCase | P1 | `1. **Missing Vue Page File** - The `BusinessUnitList.vue` file was missing from `web/src/views/basedata/`` | 替换为 `ManagementEntity` |

| `docs/conversations/b04bc295-2776-422a-9dde-2770d157fe9a.md` | 9014 | PascalCase | P1 | `1. **Created BusinessUnitList.vue** (`F:/code/opencode/opentrm/web/src/views/basedata/BusinessUnitList.vue`)` | 替换为 `ManagementEntity` |

| `docs/conversations/b04bc295-2776-422a-9dde-2770d157fe9a.md` | 9016 | PascalCase | P1 | `- Modified to display BusinessUnit fields (code, name, enName, entityType, legalPerson, registeredAddress, etc.)` | 替换为 `ManagementEntity` |

| `docs/conversations/b04bc295-2776-422a-9dde-2770d157fe9a.md` | 9021 | PascalCase | P1 | `- Added route: `{ path: 'business-unit', name: 'BusinessUnit', component: () =&gt; import('@/views/basedata/BusinessUnit` | 替换为 `ManagementEntity` |

| `docs/conversations/b04bc295-2776-422a-9dde-2770d157fe9a.md` | 9027 | camelCase | P1 | `- **API**: `web/src/api/basedata/businessUnit.js` - already exports `listBusinessUnit`, `saveBusinessUnit`, `updateBusin` | 替换为 `managementEntity` |

| `docs/conversations/b04bc295-2776-422a-9dde-2770d157fe9a.md` | 9027 | PascalCase | P1 | `- **API**: `web/src/api/basedata/businessUnit.js` - already exports `listBusinessUnit`, `saveBusinessUnit`, `updateBusin` | 替换为 `ManagementEntity` |

| `docs/conversations/b04bc295-2776-422a-9dde-2770d157fe9a.md` | 9028 | camelCase | P1 | `- **API Index**: `web/src/api/basedata/index.js` - already re-exports from `./businessUnit`` | 替换为 `managementEntity` |

| `docs/conversations/b04bc295-2776-422a-9dde-2770d157fe9a.md` | 9029 | PascalCase | P1 | `- **Backend Controller**: `BusinessUnitResource.java` at `/api/v1/management-entities`` | 替换为 `ManagementEntity` |

| `docs/conversations/b04bc295-2776-422a-9dde-2770d157fe9a.md` | 9030 | PascalCase | P1 | `- **Backend Service**: `BusinessUnitService.java` with `queryPage(keyword, status, entityType, pageNum, pageSize)` metho` | 替换为 `ManagementEntity` |

| `docs/conversations/b04bc295-2776-422a-9dde-2770d157fe9a.md` | 9031 | PascalCase | P1 | `- **Entity**: `BusinessUnit.java` with all required fields` | 替换为 `ManagementEntity` |

| `docs/conversations/b04bc295-2776-422a-9dde-2770d157fe9a.md` | 9066 | camelCase | P1 | `\| API (`businessUnit.js`) \| 已存在，包含CRUD方法 \|` | 替换为 `managementEntity` |

| `docs/conversations/b04bc295-2776-422a-9dde-2770d157fe9a.md` | 9068 | PascalCase | P1 | `\| 后端Controller \| `BusinessUnitResource.java` \|` | 替换为 `ManagementEntity` |

| `docs/conversations/b04bc295-2776-422a-9dde-2770d157fe9a.md` | 9069 | PascalCase | P1 | `\| 后端Service \| `BusinessUnitService.java` \|` | 替换为 `ManagementEntity` |

| `docs/conversations/b04bc295-2776-422a-9dde-2770d157fe9a.md` | 9070 | PascalCase | P1 | `\| 实体类 \| `BusinessUnit.java` \|` | 替换为 `ManagementEntity` |

| `docs/conversations/b04bc295-2776-422a-9dde-2770d157fe9a.md` | 13445 | camelCase | P1 | `"old_string": "            # Update\n            print(\"  [4] Update Subsidiary\")\n            upd = {\n              ` | 替换为 `managementEntity` |

| `docs/conversations/b04bc295-2776-422a-9dde-2770d157fe9a.md` | 24607 | camelCase | P1 | `\| `entityId` \| `businessUnitId` \| 前端 \|` | 替换为 `managementEntity` |

| `docs/conversations/b04bc295-2776-422a-9dde-2770d157fe9a.md` | 24934 | camelCase | P1 | `"content": "export * from './businessUnit'\nexport * from './trader'\nexport * from './currency'\nexport * from './count` | 替换为 `managementEntity` |

| `docs/conversations/b04bc295-2776-422a-9dde-2770d157fe9a.md` | 25974 | camelCase | P1 | `"command": "cd F:/code/opencode/opentrm && curl -s -X POST \"http://localhost:8082/api/v1/dealing/deals/update\" -H \"Co` | 替换为 `managementEntity` |

| `docs/conversations/b04bc295-2776-422a-9dde-2770d157fe9a.md` | 26028 | camelCase | P1 | `"command": "cd F:/code/opencode/opentrm && curl -s -X POST \"http://localhost:8082/api/v1/dealing/deals/update\" -H \"Co` | 替换为 `managementEntity` |

| `docs/conversations/b04bc295-2776-422a-9dde-2770d157fe9a.md` | 26190 | camelCase | P1 | `"command": "cd F:/code/opencode/opentrm && python -c \"\nimport urllib.request, json\n\ndata = {\n    'id': 2,\n    'dea` | 替换为 `managementEntity` |

| `docs/conversations/b04bc295-2776-422a-9dde-2770d157fe9a.md` | 26548 | camelCase | P1 | `"command": "cd F:/code/opencode/opentrm && python -c \"\nimport urllib.request, json\n\n# Step 1: Create a brand new dea` | 替换为 `managementEntity` |

| `docs/conversations/b04bc295-2776-422a-9dde-2770d157fe9a.md` | 26582 | camelCase | P1 | `"command": "cd F:/code/opencode/opentrm && python -c \"\nimport urllib.request, json\n\nupdate_data = {\n    'id': 5,\n ` | 替换为 `managementEntity` |

| `docs/conversations/db4c78f4-75ca-44ba-becd-821986d3c78b.md` | 1633 | PascalCase | P1 | `├── BusinessUnit（业务单元）` | 替换为 `ManagementEntity` |

| `docs/conversations/db4c78f4-75ca-44ba-becd-821986d3c78b.md` | 2076 | PascalCase | P1 | `│  BusinessUnit  │───▶│       Deal           │◀───│   Counterparty      │` | 替换为 `ManagementEntity` |

| `docs/conversations/db4c78f4-75ca-44ba-becd-821986d3c78b.md` | 2229 | camelCase | P1 | `├── businessUnit / counterparty` | 替换为 `managementEntity` |

| `docs/conversations/db4c78f4-75ca-44ba-becd-821986d3c78b.md` | 12307 | camelCase | P1 | `"command": "curl -s -X POST \"http://localhost:8081/opentms/basedata/api/v1/bank-accounts\" -H \"Content-Type: applicati` | 替换为 `managementEntity` |

| `docs/conversations/db4c78f4-75ca-44ba-becd-821986d3c78b.md` | 12601 | camelCase | P1 | `"command": "curl -s -X POST \"http://localhost:8082/api/v1/dealing/at-deals\" -H \"Content-Type: application/json\" -d '` | 替换为 `managementEntity` |

| `docs/conversations/db4c78f4-75ca-44ba-becd-821986d3c78b.md` | 12709 | camelCase | P1 | `"command": "curl -sv -X POST \"http://localhost:8082/api/v1/dealing/at-deals\" -H \"Content-Type: application/json\" -d ` | 替换为 `managementEntity` |

| `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md` | 6866 | camelCase | P1 | `\| **AC 交易创建/编辑** \| `AcDealForm.vue` \| 抽屉式(在 AcDealList 内) \| businessUnit / traderId / counterpartyId / instrumentId ` | 替换为 `managementEntity` |

| `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md` | 6867 | camelCase | P1 | `\| **AT 交易创建/编辑** \| `AtDealForm.vue` \| 独立路由 `/dealing/at-deal/form` \| businessUnit / sourceAccountId / destAccountId ` | 替换为 `managementEntity` |

| `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md` | 6883 | camelCase | P1 | `\| bank-account \| `GET /bank-accounts/page?keyword=&bankId=&currency=&accountType=&businessUnitId=&status=` \| accountN` | 替换为 `managementEntity` |

| `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md` | 6903 | camelCase | P1 | `:filters="{ businessUnitId: 12 }"  ← 额外过滤条件` | 替换为 `managementEntity` |

| `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md` | 9532 | camelCase | P1 | `\| `AcDealForm.vue` \| **7 个字段** \| bankAccountId / counterpartyId / counterpartyAccountId(联动)/ instrumentId / traderId ` | 替换为 `managementEntity` |

| `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md` | 9533 | camelCase | P1 | `\| `AtDealForm.vue` \| **5 个字段** \| sourceAccountId / destAccountId / sourceCurrency / destCurrency / businessUnit(同时移除 ` | 替换为 `managementEntity` |

| `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md` | 9593 | camelCase | P1 | `- `business-unit` and `currency` use `returnField: 'code'` (String), so v-model binds to the code string directly. Match` | 替换为 `managementEntity` |

| `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md` | 11889 | camelCase | P1 | `"preview": "改的地方:\n  - 侧边栏菜单 '交易管理 / 业务主体' → '管理主体'\n  - 表格列标题 '业务单元' → '管理主体'\n  - 表单 label '业务主体' → '管理主体'\n  - picker` | 替换为 `managementEntity` |

| `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md` | 11889 | PascalCase | P1 | `"preview": "改的地方:\n  - 侧边栏菜单 '交易管理 / 业务主体' → '管理主体'\n  - 表格列标题 '业务单元' → '管理主体'\n  - 表单 label '业务主体' → '管理主体'\n  - picker` | 替换为 `ManagementEntity` |

| `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md` | 11893 | camelCase | P1 | `"description": "改 UI 标签 + API 路径 + 前端 JS 变量名(businessUnit → managementEntity)。Java 代码不动(但接口依旧可调)。",` | 替换为 `managementEntity` |

| `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md` | 11983 | PascalCase | P1 | `\| **3. Backend Java** \| `BusinessUnit.java` → `ManagementEntity.java`(6 个新文件 + 6 个旧文件删除)+ 跨模块更新 AcDeal/AtDeal/Deal/Ban` | 替换为 `ManagementEntity` |

| `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md` | 13521 | camelCase | P1 | `"command": "grep -B 1 -A 3 \"businessUnit\\\|traderId\\\|counterpartyId\\\|instrumentId\" docs/prd/M1/M1-Deal交易\\ PRD\\ ` | 替换为 `managementEntity` |

| `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md` | 13576 | camelCase | P1 | `"content": "# Open-TMS M3-外汇交易 PRD\n\n**版本**: v2.0（重大重构版）\n**角色**: 产品经理 (PM)\n**日期**: 2026-07-04\n**基于**:\n- v1.0(2026-0` | 替换为 `managementEntity` |

| `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md` | 13647 | camelCase | P1 | `"content": "# M3-金融工具 设计摘要\n\n## 最近更新\n- **日期**: 2026-07-04\n- **设计师**: PM (Claude Code)\n- **本次完成**: M3-外汇交易 PRD v2.0(重` | 替换为 `managementEntity` |

| `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md` | 14823 | PascalCase | P1 | `- 7 BusinessUnit Java classes inventoried (entity/dto/vo/mapper/service/impl/controller)` | 替换为 `ManagementEntity` |

| `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md` | 14824 | PascalCase | P1 | `- 5 Java files (across basedata/dealing/fundplan/report) referencing BusinessUnit class or field` | 替换为 `ManagementEntity` |

| `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md` | 14829 | camelCase | P1 | `- 18 frontend files using businessUnit / 业务单元 / 业务主体 labels` | 替换为 `managementEntity` |

| `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md` | 14853 | PascalCase | P1 | `- Error messages translated to Chinese (e.g. "BusinessUnit not found" → "管理主体不存在")` | 替换为 `ManagementEntity` |

| `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md` | 14876 | camelCase | P1 | `web/src/api/basedata/businessUnit.js → managementEntity.js` | 替换为 `managementEntity` |

| `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md` | 14877 | PascalCase | P1 | `web/src/views/basedata/BusinessUnitList.vue → ManagementEntityList.vue` | 替换为 `ManagementEntity` |

| `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md` | 14878 | PascalCase | P1 | `- Removed legacy `listBusinessUnit` aliases from managementEntity.js` | 替换为 `ManagementEntity` |

| `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md` | 14879 | camelCase | P1 | `- Updated 18 Vue/JS files: businessUnit → managementEntity, 业务单元/业务主体 → 管理主体` | 替换为 `managementEntity` |

| `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md` | 14911 | camelCase | P1 | `- Subsidiary entity kept its internal `businessUnitCode` field semantic (subsidiary → which ME);` | 替换为 `managementEntity` |

| `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md` | 15025 | camelCase | P1 | `"command": "echo \"--- 服务状态 ---\"; netstat -ano \| grep -E ':(8081\|8082\|3000) ' \| grep LISTENING; echo \"--- 改名后端接口 -` | 替换为 `managementEntity` |

| `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md` | 15046 | PascalCase | P1 | `\| **1. Discovery** \| ✅ \| 7 个 BusinessUnit Java 类 / 18 个前端文件 / 8 个 FK 列 / 3 个 VARCHAR 列 / 1 个序列 \|` | 替换为 `ManagementEntity` |

| `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md` | 15051 | camelCase | P1 | `\| **6. 前端重命名** \| ✅ \| `businessUnit.js` → `managementEntity.js` / `BusinessUnitList.vue` → `ManagementEntityList.vue` ` | 替换为 `managementEntity` |

| `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md` | 15051 | PascalCase | P1 | `\| **6. 前端重命名** \| ✅ \| `businessUnit.js` → `managementEntity.js` / `BusinessUnitList.vue` → `ManagementEntityList.vue` ` | 替换为 `ManagementEntity` |

| `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md` | 15064 | camelCase | P1 | `\| **业务单元 / businessUnit 残留** \| ✅ **0 处** \|` | 替换为 `managementEntity` |

| `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md` | 15078 | camelCase | P1 | `\| `Subsidiary.businessUnitCode` 内部字段 \| 仅 Java 字段重命名,表列不变(内部语义) \|` | 替换为 `managementEntity` |

| `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md` | 15304 | camelCase | P1 | `\| `docs/api/fundplan/01-fund-plan.md`(使用旧 `businessUnit` 字段) \| fundplan \| 字段未同步重命名 \|` | 替换为 `managementEntity` |

| `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md` | 15323 | camelCase | P1 | `- `docs/api/fundplan/01-fund-plan.md`(`businessUnitId`)` | 替换为 `managementEntity` |

| `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md` | 15324 | camelCase | P1 | `- `docs/api/dealing/01-deal.md`(`businessUnitId`)` | 替换为 `managementEntity` |

| `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md` | 15325 | PascalCase | P1 | `- `docs/superpowers/plans/2026-04-06-basedata-impl.md`(实体 `BusinessUnit.java`)` | 替换为 `ManagementEntity` |

| `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md` | 15477 | camelCase | P1 | `全局将 `业务单元` / `businessUnit` / `business-unit` / `BusinessUnit` → `管理主体` / `managementEntity` / `management-entity` / `Ma` | 替换为 `managementEntity` |

| `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md` | 15477 | PascalCase | P1 | `全局将 `业务单元` / `businessUnit` / `business-unit` / `BusinessUnit` → `管理主体` / `managementEntity` / `management-entity` / `Ma` | 替换为 `ManagementEntity` |

| `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md` | 15953 | camelCase | P1 | `"description": "Update M3-外汇交易PRD: 增加交易日字段; businessUnit 改为 managementEntityId FK; term 计算公式; 交割日 = 交易日+term; 到期日默认=交割日且` | 替换为 `managementEntity` |

| `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md` | 16033 | camelCase | P1 | `\| 2 \| **管理主体关联 ID** \| `businessUnit`(VARCHAR 关联 code) → `managementEntityId`(BIGINT 关联 `tms_management_entity_t.id`) ` | 替换为 `managementEntity` |

| `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md` | 16059 | camelCase | P1 | `\| **跨模块一致性** \| AC/AT/FX 全部需要 `managementEntityId` 替代 `businessUnit`,这是大改动 \|` | 替换为 `managementEntity` |

| `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md` | 16149 | camelCase | P1 | `- Patterns replaced: 业务单元/businessUnit/business_unit/BusinessUnit/business-units → 管理主体/managementEntity/management_enti` | 替换为 `managementEntity` |

| `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md` | 16149 | PascalCase | P1 | `- Patterns replaced: 业务单元/businessUnit/business_unit/BusinessUnit/business-units → 管理主体/managementEntity/management_enti` | 替换为 `ManagementEntity` |

| `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md` | 16403 | camelCase | P1 | `"new_string": "## 〇、修订记录\n\n### v3.1(2026-07-04) - 本次细化调整(用户产品评审反馈)\n\n\| 修订项 \| 修订内容 \| 原因 \|\n\|--------\|---------\|-` | 替换为 `managementEntity` |

| `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md` | 16421 | camelCase | P1 | `"old_string": "### 3.4 通用交易字段(SPOT/FWD/NDF 共有) - P0\n\n\| 字段 \| 类型 \| 必填 \| 说明 \|\n\|------\|------\|------\|------\|\n\` | 替换为 `managementEntity` |

| `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md` | 16479 | camelCase | P1 | `"new_string": "> **v3.1 关键变化**:\n> - `businessUnit VARCHAR(50) code` → `managementEntityId BIGINT` 强 FK 关联 id\n> - 新增\"交` | 替换为 `managementEntity` |

| `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md` | 17042 | camelCase | P1 | `- v3.1 的 `managementEntityId` 是 BIGINT FK,`tms_deals_t.businessUnit VARCHAR(50)` 需同步改造` | 替换为 `managementEntity` |

| `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md` | 28146 | PascalCase | P1 | `"command": "git commit -m \"chore: CLAUDE.md 瘦身 + 文档整理 + 会话导出工具 + 基础数据脚本 + Token 优化\n\n- CLAUDE.md: 12KB → 3KB (-75%), 去` | 替换为 `ManagementEntity` |

| `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md` | 33444 | PascalCase | P1 | `\| 重构遗留 (BusinessUnit→ManagementEntity) 漏改 \| 对比字段名 \|` | 替换为 `ManagementEntity` |

| `docs/prd/M3/M3-外汇交易PRD.md` | 34 | camelCase | P1 | `\| **管理主体 FK 改为 id** \| `businessUnit VARCHAR(50) code` → `managementEntityId BIGINT NOT NULL`(关联 `tms_management_entity` | 替换为 `managementEntity` |

| `docs/prd/M3/M3-外汇交易PRD.md` | 205 | camelCase | P1 | `> - `businessUnit VARCHAR(50) code` → `managementEntityId BIGINT` 强 FK 关联 id` | 替换为 `managementEntity` |

| `docs/prd/M3/M3-现金流增强+Audit-History-PRD.md` | 472 | camelCase | P1 | `"businessUnit": "HK",` | 替换为 `managementEntity` |

| `summary.md` | 63 | PascalCase | P1 | `\| **basedata** \| basedata/ \| ✅ 开发中 \| Bank, BusinessUnit, Counterparty, CounterpartyAccount, Currency, Country, Holid` | 替换为 `ManagementEntity` |

| `summary.md` | 81 | PascalCase | P1 | `\| **basedata** \| BankList, BusinessUnitList, CounterpartyList, CounterpartyAccountList, CurrencyList, CountryList, Hol` | 替换为 `ManagementEntity` |

| `docs/CHANGELOG.md` | 45 | snake_case | P2 | `- Batch rename: `业务单元` → `管理主体` (and `businessUnit`/`business_unit`/`BusinessUnit` → management variants) across all doc` | 替换为 `management_entity` |

| `docs/CHANGELOG.md` | 45 | camelCase | P2 | `- Batch rename: `业务单元` → `管理主体` (and `businessUnit`/`business_unit`/`BusinessUnit` → management variants) across all doc` | 替换为 `managementEntity` |

| `docs/CHANGELOG.md` | 45 | PascalCase | P2 | `- Batch rename: `业务单元` → `管理主体` (and `businessUnit`/`business_unit`/`BusinessUnit` → management variants) across all doc` | 替换为 `ManagementEntity` |

| `docs/CHANGELOG.md` | 66 | snake_case | P2 | `- DB: `tms_business_unit_t` → `tms_management_entity_t`` | 替换为 `management_entity` |

| `docs/architecture/business/AC交易与现金流分离架构设计.md` | 322 | snake_case | P2 | `business_unit VARCHAR(50) NOT NULL,` | 替换为 `management_entity` |

| `docs/architecture/business/AC交易与现金流分离架构设计.md` | 353 | snake_case | P2 | `business_unit VARCHAR(50) NOT NULL,` | 替换为 `management_entity` |

| `docs/architecture/business/AC交易与现金流分离架构设计.md` | 387 | snake_case | P2 | `business_unit VARCHAR(50) NOT NULL,` | 替换为 `management_entity` |

| `docs/archive/2026-07-04-redundant-versions/M1-Deal交易 PRD v4.md` | 359 | snake_case | P2 | `business_unit VARCHAR(50) NOT NULL,` | 替换为 `management_entity` |

| `docs/archive/2026-07-04-redundant-versions/M1-Deal交易 PRD v4.md` | 381 | snake_case | P2 | `CREATE INDEX idx_deal_unit ON tms_deals_t(business_unit);` | 替换为 `management_entity` |

| `docs/conversations/3a73c9ba-a60d-4e56-8d2d-94f205420fb8.md` | 2003 | snake_case | P2 | `找到根因!**PostgreSQL 报错 `字段 "business_unit_id" 不存在`**,SQL 试图从 `tms_bank_account_t` 查 `business_unit_id`,但 DB 中这个列不存在。` | 替换为 `management_entity` |

| `docs/conversations/3a73c9ba-a60d-4e56-8d2d-94f205420fb8.md` | 2034 | snake_case | P2 | `"command": "grep -E \"^\\s*\\*.*business_unit\|business_unit\" db/schema/*.sql \| head -10; echo \"---\"; grep \"busines` | 替换为 `management_entity` |

| `docs/conversations/3a73c9ba-a60d-4e56-8d2d-94f205420fb8.md` | 2035 | snake_case | P2 | `"description": "查DB schema中business_unit 定义及Entity引用"` | 替换为 `management_entity` |

| `docs/conversations/3a73c9ba-a60d-4e56-8d2d-94f205420fb8.md` | 2076 | snake_case | P2 | `"command": "cd /f/code/opencode/opentrm && grep -rE \"business_unit_id\|businessUnitId\" db/schema/ basedata/src/main/ja` | 替换为 `management_entity` |

| `docs/conversations/3a73c9ba-a60d-4e56-8d2d-94f205420fb8.md` | 2077 | snake_case | P2 | `"description": "查business_unit 字段定义"` | 替换为 `management_entity` |

| `docs/conversations/3a73c9ba-a60d-4e56-8d2d-94f205420fb8.md` | 2109 | snake_case | P2 | `**关键发现**:`23-rename-business-unit.sql` 把 `business_unit_id` 重命名为 `management_entity_id`,但 **`23b-rename-business-unit-cl` | 替换为 `management_entity` |

| `docs/conversations/3a73c9ba-a60d-4e56-8d2d-94f205420fb8.md` | 2151 | snake_case | P2 | `- DB 里叫 `management_entity_id`(不是 `business_unit_id`) — `23-rename-business-unit.sql` 已执行` | 替换为 `management_entity` |

| `docs/conversations/3a73c9ba-a60d-4e56-8d2d-94f205420fb8.md` | 2198 | camelCase | P2 | `**Bug**:PR `69c34ab` 实际上**做了反向操作** — 它把 Java Entity 的 `managementEntityId` 改成 `businessUnitId`,但 DB 列被 `23-rename-busine` | 替换为 `managementEntity` |

| `docs/conversations/3a73c9ba-a60d-4e56-8d2d-94f205420fb8.md` | 2318 | snake_case | P2 | `**PR `69c34ab` 是把 Entity 字段名改回 `businessUnitId`**(配 `@TableField("business_unit_id")`),它认为 DB 已经有 `business_unit_id`。但**` | 替换为 `management_entity` |

| `docs/conversations/3a73c9ba-a60d-4e56-8d2d-94f205420fb8.md` | 2318 | camelCase | P2 | `**PR `69c34ab` 是把 Entity 字段名改回 `businessUnitId`**(配 `@TableField("business_unit_id")`),它认为 DB 已经有 `business_unit_id`。但**` | 替换为 `managementEntity` |

| `docs/conversations/3a73c9ba-a60d-4e56-8d2d-94f205420fb8.md` | 2320 | snake_case | P2 | `也就是说这个 PR 在作者本地是工作的(可能他本地 DB 状态与生产不一致),但合并后所有基于该 PR 的代码部署都用 `business_unit_id`,而 DB 没有这个列,所以 500。` | 替换为 `management_entity` |

| `docs/conversations/3a73c9ba-a60d-4e56-8d2d-94f205420fb8.md` | 2369 | snake_case | P2 | `"question": "远程 PR #9 (69c34ab) 有回归 bug——Java Entity 改成 businessUnitId + @TableField('business_unit_id'),但 DB 实际列名是 mana` | 替换为 `management_entity` |

| `docs/conversations/3a73c9ba-a60d-4e56-8d2d-94f205420fb8.md` | 2369 | camelCase | P2 | `"question": "远程 PR #9 (69c34ab) 有回归 bug——Java Entity 改成 businessUnitId + @TableField('business_unit_id'),但 DB 实际列名是 mana` | 替换为 `managementEntity` |

| `docs/conversations/3a73c9ba-a60d-4e56-8d2d-94f205420fb8.md` | 2374 | snake_case | P2 | `"description": "改 Java 代码: BankAccount/Subsidiary/ManagementEntity/CurrencyPair 去掉 @TableField('business_unit_id'),同步多个 ` | 替换为 `management_entity` |

| `docs/conversations/3a73c9ba-a60d-4e56-8d2d-94f205420fb8.md` | 2378 | snake_case | P2 | `"description": "依赖 DB 脚本脚本加 ALTER TABLE ... RENAME COLUMN management_entity_id TO business_unit_id(连同 CurrencyPair 3 个列)` | 替换为 `management_entity` |

| `docs/conversations/3a73c9ba-a60d-4e56-8d2d-94f205420fb8.md` | 2512 | snake_case | P2 | `"command": "grep -E \"businessUnitId\|managementEntityId\|@TableField\" basedata/src/main/java/com/opentms/basedata/enti` | 替换为 `management_entity` |

| `docs/conversations/3a73c9ba-a60d-4e56-8d2d-94f205420fb8.md` | 2882 | snake_case | P2 | `精确诊断:DB 中列叫 `business_unit_code`,SQL 用的仍是 `management_entity_code`。这是因为 **`Subsidiary` Entity 字段现在叫 `managementEntityCod` | 替换为 `management_entity` |

| `docs/conversations/3a73c9ba-a60d-4e56-8d2d-94f205420fb8.md` | 2950 | snake_case | P2 | `和 revert 结果一致 — 我加回 `@TableField("business_unit_code")` 注解是对的(等同 PR 的"子集"修复,因为 DB 列名是 `business_unit_code`,不是 `managemen` | 替换为 `management_entity` |

| `docs/conversations/3a73c9ba-a60d-4e56-8d2d-94f205420fb8.md` | 3458 | camelCase | P2 | `**远程 vs 本地 WIP 没有真实冲突**(只在 4 个对话记录文件上有重合,但内容互补不冲突)。真正的"智能合并"是**发现并修复了 PR #9 的回归 bug** —— 该 PR 反向把 Java Entity 字段从 DB 对齐的` | 替换为 `managementEntity` |

| `docs/conversations/3a73c9ba-a60d-4e56-8d2d-94f205420fb8.md` | 3464 | snake_case | P2 | `\| `BankAccount.managementEntityId` 字段恢复(移除 `@TableField("business_unit_id")`) \| ✓ bank-accounts 200 \|` | 替换为 `management_entity` |

| `docs/conversations/3a73c9ba-a60d-4e56-8d2d-94f205420fb8.md` | 3465 | snake_case | P2 | `\| `Subsidiary.managementEntityCode` 字段恢复 + 加 `@TableField("business_unit_code")` 注解 \| ✓ subsidiaries 200 \|` | 替换为 `management_entity` |

| `docs/conversations/3a73c9ba-a60d-4e56-8d2d-94f205420fb8.md` | 5181 | camelCase | P2 | `**根因汇总**: **(F) + (C 的衍生)** — `filteredBankAccounts` computed 用 `businessUnitId` 过滤,而后端记录实际是 `managementEntityId`,所以一旦 `` | 替换为 `managementEntity` |

| `docs/conversations/3a73c9ba-a60d-4e56-8d2d-94f205420fb8.md` | 5259 | camelCase | P2 | `**结论**: 单点修复 — `DefaultBankAccountRuleList.vue:300` 一行 `businessUnitId` → `managementEntityId` 即彻底解决"默认账户下拉空"问题。其它下拉(管理主` | 替换为 `managementEntity` |

| `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md` | 10935 | camelCase | P2 | `**400 提示"managementEntity 不能为空"** — dealing 的 AC 端点实际字段名是 `managementEntity`,不是 `businessUnit`。我之前用了 demo 数据。子代理的 5 步 cu` | 替换为 `managementEntity` |

| `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md` | 21347 | snake_case | P2 | `<command-args>开启子代理，编写工具扫描系统代码、文档、数据库表中是否还有业务单元（business_unit）的存在</command-args>` | 替换为 `management_entity` |

| `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md` | 21782 | snake_case | P2 | `ARGUMENTS: 开启子代理，编写工具扫描系统代码、文档、数据库表中是否还有业务单元（business_unit）的存在` | 替换为 `management_entity` |

| `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md` | 21797 | snake_case | P2 | `"subject": "扫描 business_unit 残留工具",` | 替换为 `management_entity` |

| `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md` | 21798 | snake_case | P2 | `"description": "写 scripts/scan_business_unit.py:扫描 4 个层面残留:1) Java 代码(@Column/Entity/VO 字段名 businessUnit) 2) 前端 .vue/.js` | 替换为 `management_entity` |

| `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md` | 21799 | snake_case | P2 | `"activeForm": "扫描 business_unit 残留"` | 替换为 `management_entity` |

| `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md` | 21834 | snake_case | P2 | `"description": "扫描 business_unit 残留工具",` | 替换为 `management_entity` |

| `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md` | 21835 | snake_case | P2 | `"prompt": "Open-TMS 项目位于 F:\\code\\opencode\\opentrm。**只读扫描** — 编写工具扫描系统中是否还有 \"business_unit\" 命名残留(背景:之前 commit 23-ren` | 替换为 `management_entity` |

| `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md` | 21835 | camelCase | P2 | `"prompt": "Open-TMS 项目位于 F:\\code\\opencode\\opentrm。**只读扫描** — 编写工具扫描系统中是否还有 \"business_unit\" 命名残留(背景:之前 commit 23-ren` | 替换为 `managementEntity` |

| `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md` | 21835 | UPPER_CASE | P2 | `"prompt": "Open-TMS 项目位于 F:\\code\\opencode\\opentrm。**只读扫描** — 编写工具扫描系统中是否还有 \"business_unit\" 命名残留(背景:之前 commit 23-ren` | 替换为 `MANAGEMENT_ENTITY` |

| `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md` | 21848 | snake_case | P2 | `## ⚡ 扫描 business_unit 残留子代理已启动` | 替换为 `management_entity` |

| `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md` | 21852 | snake_case | P2 | `\| `a26c31bfc3a01b1d6` \| 写 `scripts/scan_business_unit.py` + 跑报告 \| 🟢 \|` | 替换为 `management_entity` |

| `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md` | 21855 | snake_case | P2 | `1. **Java 代码**:`**/*.java` in basedata/dealing/common,找 `businessUnit` / `@Column("business_unit")` / `getBusinessUnit`` | 替换为 `management_entity` |

| `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md` | 21865 | snake_case | P2 | `- `scripts/scan_business_unit.py` — 工具` | 替换为 `management_entity` |

| `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md` | 21884 | snake_case | P2 | `<summary>Agent "扫描 business_unit 残留工具" finished</summary>` | 替换为 `management_entity` |

| `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md` | 21893 | snake_case | P2 | `- `F:\code\opencode\opentrm\scripts\scan_business_unit.py` — 587 行,沿用 `find_similar_bugs.py` 风格` | 替换为 `management_entity` |

| `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md` | 21912 | snake_case | P2 | `- **数据库**: 31 处 P0。包括整张遗留表 `trm_business_unit_t`(根本没改名)、`tms_cashflow_t.business_unit` / `tms_cashflow_image_t.business_` | 替换为 `management_entity` |

| `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md` | 21913 | snake_case | P2 | `- **Java**: 6 处。`@TableField("business_unit")` / `@TableField("business_unit_code")` / `private String businessUnit` 在 `` | 替换为 `management_entity` |

| `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md` | 21919 | snake_case | P2 | `1. `public.tms_cashflow_t.business_unit` — 列未改名(commit 23 注释说改了,实际未生效)` | 替换为 `management_entity` |

| `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md` | 21920 | snake_case | P2 | `2. `public.trm_business_unit_t` — 整张表未改名(注意不是 `tms_*`,是 `trm_*`,可能是另一个 schema 的旧表)` | 替换为 `management_entity` |

| `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md` | 21921 | snake_case | P2 | `3. `public.tms_subsidiary_t.business_unit_code` — 列名漏改(语义为"子公司所属业务单元",需决策)` | 替换为 `management_entity` |

| `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md` | 21922 | snake_case | P2 | `4. `basedata/src/main/java/com/opentms/basedata/entity/Subsidiary.java:24` — `@TableField("business_unit_code")`` | 替换为 `management_entity` |

| `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md` | 21923 | snake_case | P2 | `5. `dealing/src/main/java/com/opentms/dealing/entity/Cashflow.java:32` — `@TableField("business_unit")`` | 替换为 `management_entity` |

| `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md` | 21924 | snake_case | P2 | `6. `dealing/src/main/java/com/opentms/dealing/entity/CashflowImage.java:51-52` — `@TableField("business_unit")` + `priva` | 替换为 `management_entity` |

| `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md` | 21928 | snake_case | P2 | `- 4 种命名模式: `business_unit` / `businessUnit` / `BUSINESS_UNIT` / `BusinessUnit`` | 替换为 `management_entity` |

| `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md` | 21928 | UPPER_CASE | P2 | `- 4 种命名模式: `business_unit` / `businessUnit` / `BUSINESS_UNIT` / `BusinessUnit`` | 替换为 `MANAGEMENT_ENTITY` |

| `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md` | 21930 | UPPER_CASE | P2 | `- 自动严重度: 注释 / 历史关键字 → P2;docs 默认 P2;`BUSINESS_UNIT` 实体类型枚举值 → false positive 过滤` | 替换为 `MANAGEMENT_ENTITY` |

| `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md` | 21938 | snake_case | P2 | `1. **未触达的 schema**: `trm_business_unit_t`(整个 schema 与 `tms_` 并存)` | 替换为 `management_entity` |

| `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md` | 21939 | snake_case | P2 | `2. **未触达的表**: `tms_cashflow_t` / `tms_cashflow_image_t`(commit 23 注释列了 `tms_cashflow_t.business_unit` 但实际未生效或被回退)` | 替换为 `management_entity` |

| `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md` | 21940 | snake_case | P2 | `3. **未触达的约束 / 索引**: 大批 `idx_*_business_unit` / `*_not_null` 自动生成的约束名都未跟随列重命名而更新` | 替换为 `management_entity` |

| `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md` | 21950 | snake_case | P2 | `**扫描器完成,评级 D** — 发现**系统性残留 31 处 P0**!最关键是 commit 23 注释说改了 `tms_cashflow_t.business_unit`,**实际未生效**。看子代理报告:` | 替换为 `management_entity` |

| `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md` | 21959 | snake_case | P2 | `"command": "ls -la scripts/scan_business_unit.py docs/reviews/business-unit-audit.md 2>/dev/null\necho \"---head 30 of r` | 替换为 `management_entity` |

| `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md` | 21992 | snake_case | P2 | `## ✅ business_unit 扫描器完成 + 系统性残留 D 级警告` | 替换为 `management_entity` |

| `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md` | 21997 | snake_case | P2 | `\| `scripts/scan_business_unit.py` \| 22.5KB / 587 行 \| 4 模式 + 4 类别扫描器 \|` | 替换为 `management_entity` |

| `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md` | 22012 | snake_case | P2 | `1. **`tms_cashflow_t.business_unit`** 列没改(commit 23 注释说改了,实际未生效)` | 替换为 `management_entity` |

| `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md` | 22013 | snake_case | P2 | `2. **`trm_business_unit_t`** 整张表未改名(注意不是 `tms_*`,是 `trm_*`,可能是另一个 schema)` | 替换为 `management_entity` |

| `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md` | 22014 | snake_case | P2 | `3. **`tms_subsidiary_t.business_unit_code`** 列名漏改(语义"子公司所属业务单元",需决策)` | 替换为 `management_entity` |

| `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md` | 22016 | snake_case | P2 | `5. **Java 6 处 P0**:`Subsidiary.java` / `Cashflow.java` / `CashflowImage.java` 用 `@TableField("business_unit")`` | 替换为 `management_entity` |

| `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md` | 22019 | snake_case | P2 | `- `public.tms_cashflow_t.business_unit` 列(P0)` | 替换为 `management_entity` |


> …还有 87 处省略(详见 JSON 输出)


### 2.4 数据库 (0 处)

> 无残留


## 3. 修复建议

### P0 — 必须修(影响运行/数据正确性)
> 无 P0 级残留。


### P1 — 建议修(代码腐烂/口径不一致)
- `docs/CHANGELOG.md`:68 → `- Java: `BusinessUnit` → `ManagementEntity``  →  替换为 `ManagementEntity`
- `docs/api/cashflow-enhance-API.md`:145 → `req.setManagementEntityId(cf.getBusinessUnitId());`  →  替换为 `ManagementEntity`
- `docs/conversations/00867d0c-8954-482e-ad1e-d6122e0e60f9.md`:796 → `- `BusinessUnitList.vue`, `CounterpartyAccountList.vue` 修改`  →  替换为 `ManagementEntity`
- `docs/conversations/00867d0c-8954-482e-ad1e-d6122e0e60f9.md`:920 → `| basedata | ✅ 已完成 | ✅ Bank/BusinessUnit/Currency/Country/Holiday/Trader |`  →  替换为 `ManagementEntity`
- `docs/conversations/3a73c9ba-a60d-4e56-8d2d-94f205420fb8.md`:2005 → `这是 PR `69c34ab "Entity 与 DB 字段对齐"` 的**真实失败场景**:Java Entity 加了 `businessUnitId`(因`  →  替换为 `managementEntity`
- `docs/conversations/3a73c9ba-a60d-4e56-8d2d-94f205420fb8.md`:2034 → `"command": "grep -E \"^\\s*\\*.*business_unit|business_unit\" db/schema/*.sql | `  →  替换为 `managementEntity`
- `docs/conversations/3a73c9ba-a60d-4e56-8d2d-94f205420fb8.md`:2076 → `"command": "cd /f/code/opencode/opentrm && grep -rE \"business_unit_id|businessU`  →  替换为 `managementEntity`
- `docs/conversations/3a73c9ba-a60d-4e56-8d2d-94f205420fb8.md`:2152 → `- 但 PR `69c34ab` 的目标是**让 Entity 与 DB 对齐**,应当把 Java 端 `businessUnitId` 改成 `manage`  →  替换为 `managementEntity`
- `docs/conversations/3a73c9ba-a60d-4e56-8d2d-94f205420fb8.md`:2163 → `"command": "grep -E \"businessUnitId|managementEntityId\" basedata/src/main/java`  →  替换为 `managementEntity`
- `docs/conversations/3a73c9ba-a60d-4e56-8d2d-94f205420fb8.md`:2231 → `"description": "把 BankAccount.businessUnitId 改回 managementEntityId(对应 DB 列),改完后端`  →  替换为 `managementEntity`
- `docs/conversations/3a73c9ba-a60d-4e56-8d2d-94f205420fb8.md`:2480 → `"command": "grep -E \"businessUnitId|managementEntityId\" basedata/src/main/java`  →  替换为 `managementEntity`
- `docs/conversations/3a73c9ba-a60d-4e56-8d2d-94f205420fb8.md`:2512 → `"command": "grep -E \"businessUnitId|managementEntityId|@TableField\" basedata/s`  →  替换为 `managementEntity`
- `docs/conversations/3a73c9ba-a60d-4e56-8d2d-94f205420fb8.md`:2601 → `"command": "grep -B 2 -A 1 \"@TableField.*code\\|businessUnitCode\\|managementEn`  →  替换为 `managementEntity`
- `docs/conversations/3a73c9ba-a60d-4e56-8d2d-94f205420fb8.md`:5168 → `return bankAccountList.value.filter(a =&gt; a.businessUnitId === editForm.manage`  →  替换为 `managementEntity`
- `docs/conversations/3a73c9ba-a60d-4e56-8d2d-94f205420fb8.md`:5172 → `这里**字段不匹配 (F)**:第 300 行按 `businessUnitId` 过滤,但 curl 实测**后端返回的是 `managementEntity`  →  替换为 `managementEntity`
- `docs/conversations/3a73c9ba-a60d-4e56-8d2d-94f205420fb8.md`:5174 → `而 `handleAdd` 第 358 行自动用 `queryForm.managementEntityId`(前一次查询时用户选的主体)预设给 `editFo`  →  替换为 `managementEntity`
- `docs/conversations/3a73c9ba-a60d-4e56-8d2d-94f205420fb8.md`:5195 → `| 默认账户(过滤条件) | `item.businessUnitId` | **`managementEntityId`** | ✗ **不匹配** ← **`  →  替换为 `managementEntity`
- `docs/conversations/3a73c9ba-a60d-4e56-8d2d-94f205420fb8.md`:5216 → `-  // BankAccount 字段已对齐 DB:businessUnitId(原 managementEntityId)`  →  替换为 `managementEntity`
- `docs/conversations/3a73c9ba-a60d-4e56-8d2d-94f205420fb8.md`:5217 → `-  return bankAccountList.value.filter(a =&gt; a.businessUnitId === editForm.man`  →  替换为 `managementEntity`
- `docs/conversations/3a73c9ba-a60d-4e56-8d2d-94f205420fb8.md`:5255 → `4. **`filteredBankAccounts` 字段名漂移遗留的误导性注释**(第 299 行):"`BankAccount 字段已对齐 DB:busi`  →  替换为 `managementEntity`
- `docs/conversations/3a73c9ba-a60d-4e56-8d2d-94f205420fb8.md`:5303 → `"old_string": "const filteredBankAccounts = computed(() => {\n  if (!editForm.ma`  →  替换为 `managementEntity`
- `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md`:10874 → `"command": "echo \"=== 端到端验证 ===\"\necho \"--- 1) 创建一个 AC 交易,自动填充 + 写 CREATE 镜像 `  →  替换为 `managementEntity`
- `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md`:10922 → `"command": "cat > /tmp/ac_create.json << 'EOF'\n{\n  \"businessUnit\":\"BU_HEAD\`  →  替换为 `managementEntity`
- `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md`:21798 → `"description": "写 scripts/scan_business_unit.py:扫描 4 个层面残留:1) Java 代码(@Column/En`  →  替换为 `managementEntity`
- `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md`:21855 → `1. **Java 代码**:`**/*.java` in basedata/dealing/common,找 `businessUnit` / `@Colum`  →  替换为 `managementEntity`
- `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md`:21855 → `1. **Java 代码**:`**/*.java` in basedata/dealing/common,找 `businessUnit` / `@Colum`  →  替换为 `ManagementEntity`
- `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md`:21856 → `2. **前端代码**:`web/src/**/*.{vue,js,ts}`,找 `businessUnitId` / 变量 / prop`  →  替换为 `managementEntity`
- `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md`:21913 → `- **Java**: 6 处。`@TableField("business_unit")` / `@TableField("business_unit_cod`  →  替换为 `managementEntity`
- `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md`:21924 → `6. `dealing/src/main/java/com/opentms/dealing/entity/CashflowImage.java:51-52` —`  →  替换为 `managementEntity`
- `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md`:21928 → `- 4 种命名模式: `business_unit` / `businessUnit` / `BUSINESS_UNIT` / `BusinessUnit``  →  替换为 `managementEntity`
- `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md`:21928 → `- 4 种命名模式: `business_unit` / `businessUnit` / `BUSINESS_UNIT` / `BusinessUnit``  →  替换为 `ManagementEntity`
- `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md`:22156 → `"command": "cat docs/reviews/business-unit-audit.md | grep -E \"tms_cashflow|trm`  →  替换为 `managementEntity`
- `docs/conversations/b04bc295-2776-422a-9dde-2770d157fe9a.md`:8364 → `- `F:/code/opencode/opentrm/web/src/views/basedata/BusinessUnitList.vue``  →  替换为 `ManagementEntity`
- `docs/conversations/b04bc295-2776-422a-9dde-2770d157fe9a.md`:8425 → `- `web/src/views/basedata/BusinessUnitList.vue``  →  替换为 `ManagementEntity`
- `docs/conversations/b04bc295-2776-422a-9dde-2770d157fe9a.md`:8431 → `> **注意：** `businessUnit.js` API 保留，因为其他页面（交易列表、银行账户等）还在使用 `listBusinessUnit`。`  →  替换为 `managementEntity`
- `docs/conversations/b04bc295-2776-422a-9dde-2770d157fe9a.md`:8431 → `> **注意：** `businessUnit.js` API 保留，因为其他页面（交易列表、银行账户等）还在使用 `listBusinessUnit`。`  →  替换为 `ManagementEntity`
- `docs/conversations/b04bc295-2776-422a-9dde-2770d157fe9a.md`:8671 → `entityList.value = (await listBusinessUnit({ pageSize: 1000 })).data.list || []`  →  替换为 `ManagementEntity`
- `docs/conversations/b04bc295-2776-422a-9dde-2770d157fe9a.md`:8677 → `entityList.value = (await listBusinessUnit({ pageSize: 1000 })).data.records || `  →  替换为 `ManagementEntity`
- `docs/conversations/b04bc295-2776-422a-9dde-2770d157fe9a.md`:8774 → `"old_string": "const fetchLists = async () => {\n  entityList.value = (await lis`  →  替换为 `ManagementEntity`
- `docs/conversations/b04bc295-2776-422a-9dde-2770d157fe9a.md`:8775 → `"new_string": "const fetchLists = async () => {\n  entityList.value = (await lis`  →  替换为 `ManagementEntity`
- `docs/conversations/b04bc295-2776-422a-9dde-2770d157fe9a.md`:9008 → `1. **Missing Vue Page File** - The `BusinessUnitList.vue` file was missing from `  →  替换为 `ManagementEntity`
- `docs/conversations/b04bc295-2776-422a-9dde-2770d157fe9a.md`:9014 → `1. **Created BusinessUnitList.vue** (`F:/code/opencode/opentrm/web/src/views/bas`  →  替换为 `ManagementEntity`
- `docs/conversations/b04bc295-2776-422a-9dde-2770d157fe9a.md`:9016 → `- Modified to display BusinessUnit fields (code, name, enName, entityType, legal`  →  替换为 `ManagementEntity`
- `docs/conversations/b04bc295-2776-422a-9dde-2770d157fe9a.md`:9021 → `- Added route: `{ path: 'business-unit', name: 'BusinessUnit', component: () =&g`  →  替换为 `ManagementEntity`
- `docs/conversations/b04bc295-2776-422a-9dde-2770d157fe9a.md`:9027 → `- **API**: `web/src/api/basedata/businessUnit.js` - already exports `listBusines`  →  替换为 `managementEntity`
- `docs/conversations/b04bc295-2776-422a-9dde-2770d157fe9a.md`:9027 → `- **API**: `web/src/api/basedata/businessUnit.js` - already exports `listBusines`  →  替换为 `ManagementEntity`
- `docs/conversations/b04bc295-2776-422a-9dde-2770d157fe9a.md`:9028 → `- **API Index**: `web/src/api/basedata/index.js` - already re-exports from `./bu`  →  替换为 `managementEntity`
- `docs/conversations/b04bc295-2776-422a-9dde-2770d157fe9a.md`:9029 → `- **Backend Controller**: `BusinessUnitResource.java` at `/api/v1/management-ent`  →  替换为 `ManagementEntity`
- `docs/conversations/b04bc295-2776-422a-9dde-2770d157fe9a.md`:9030 → `- **Backend Service**: `BusinessUnitService.java` with `queryPage(keyword, statu`  →  替换为 `ManagementEntity`
- `docs/conversations/b04bc295-2776-422a-9dde-2770d157fe9a.md`:9031 → `- **Entity**: `BusinessUnit.java` with all required fields`  →  替换为 `ManagementEntity`
- `docs/conversations/b04bc295-2776-422a-9dde-2770d157fe9a.md`:9066 → `| API (`businessUnit.js`) | 已存在，包含CRUD方法 |`  →  替换为 `managementEntity`
- `docs/conversations/b04bc295-2776-422a-9dde-2770d157fe9a.md`:9068 → `| 后端Controller | `BusinessUnitResource.java` |`  →  替换为 `ManagementEntity`
- `docs/conversations/b04bc295-2776-422a-9dde-2770d157fe9a.md`:9069 → `| 后端Service | `BusinessUnitService.java` |`  →  替换为 `ManagementEntity`
- `docs/conversations/b04bc295-2776-422a-9dde-2770d157fe9a.md`:9070 → `| 实体类 | `BusinessUnit.java` |`  →  替换为 `ManagementEntity`
- `docs/conversations/b04bc295-2776-422a-9dde-2770d157fe9a.md`:13445 → `"old_string": "            # Update\n            print(\"  [4] Update Subsidiary`  →  替换为 `managementEntity`
- `docs/conversations/b04bc295-2776-422a-9dde-2770d157fe9a.md`:24607 → `| `entityId` | `businessUnitId` | 前端 |`  →  替换为 `managementEntity`
- `docs/conversations/b04bc295-2776-422a-9dde-2770d157fe9a.md`:24934 → `"content": "export * from './businessUnit'\nexport * from './trader'\nexport * f`  →  替换为 `managementEntity`
- `docs/conversations/b04bc295-2776-422a-9dde-2770d157fe9a.md`:25974 → `"command": "cd F:/code/opencode/opentrm && curl -s -X POST \"http://localhost:80`  →  替换为 `managementEntity`
- `docs/conversations/b04bc295-2776-422a-9dde-2770d157fe9a.md`:26028 → `"command": "cd F:/code/opencode/opentrm && curl -s -X POST \"http://localhost:80`  →  替换为 `managementEntity`
- `docs/conversations/b04bc295-2776-422a-9dde-2770d157fe9a.md`:26190 → `"command": "cd F:/code/opencode/opentrm && python -c \"\nimport urllib.request, `  →  替换为 `managementEntity`
- `docs/conversations/b04bc295-2776-422a-9dde-2770d157fe9a.md`:26548 → `"command": "cd F:/code/opencode/opentrm && python -c \"\nimport urllib.request, `  →  替换为 `managementEntity`
- `docs/conversations/b04bc295-2776-422a-9dde-2770d157fe9a.md`:26582 → `"command": "cd F:/code/opencode/opentrm && python -c \"\nimport urllib.request, `  →  替换为 `managementEntity`
- `docs/conversations/db4c78f4-75ca-44ba-becd-821986d3c78b.md`:1633 → `├── BusinessUnit（业务单元）`  →  替换为 `ManagementEntity`
- `docs/conversations/db4c78f4-75ca-44ba-becd-821986d3c78b.md`:2076 → `│  BusinessUnit  │───▶│       Deal           │◀───│   Counterparty      │`  →  替换为 `ManagementEntity`
- `docs/conversations/db4c78f4-75ca-44ba-becd-821986d3c78b.md`:2229 → `├── businessUnit / counterparty`  →  替换为 `managementEntity`
- `docs/conversations/db4c78f4-75ca-44ba-becd-821986d3c78b.md`:12307 → `"command": "curl -s -X POST \"http://localhost:8081/opentms/basedata/api/v1/bank`  →  替换为 `managementEntity`
- `docs/conversations/db4c78f4-75ca-44ba-becd-821986d3c78b.md`:12601 → `"command": "curl -s -X POST \"http://localhost:8082/api/v1/dealing/at-deals\" -H`  →  替换为 `managementEntity`
- `docs/conversations/db4c78f4-75ca-44ba-becd-821986d3c78b.md`:12709 → `"command": "curl -sv -X POST \"http://localhost:8082/api/v1/dealing/at-deals\" -`  →  替换为 `managementEntity`
- `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md`:6866 → `| **AC 交易创建/编辑** | `AcDealForm.vue` | 抽屉式(在 AcDealList 内) | businessUnit / trade`  →  替换为 `managementEntity`
- `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md`:6867 → `| **AT 交易创建/编辑** | `AtDealForm.vue` | 独立路由 `/dealing/at-deal/form` | businessUni`  →  替换为 `managementEntity`
- `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md`:6883 → `| bank-account | `GET /bank-accounts/page?keyword=&bankId=&currency=&accountType`  →  替换为 `managementEntity`
- `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md`:6903 → `:filters="{ businessUnitId: 12 }"  ← 额外过滤条件`  →  替换为 `managementEntity`
- `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md`:9532 → `| `AcDealForm.vue` | **7 个字段** | bankAccountId / counterpartyId / counterpartyAc`  →  替换为 `managementEntity`
- `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md`:9533 → `| `AtDealForm.vue` | **5 个字段** | sourceAccountId / destAccountId / sourceCurrenc`  →  替换为 `managementEntity`
- `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md`:9593 → `- `business-unit` and `currency` use `returnField: 'code'` (String), so v-model `  →  替换为 `managementEntity`
- `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md`:11889 → `"preview": "改的地方:\n  - 侧边栏菜单 '交易管理 / 业务主体' → '管理主体'\n  - 表格列标题 '业务单元' → '管理主体'\n`  →  替换为 `managementEntity`
- `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md`:11889 → `"preview": "改的地方:\n  - 侧边栏菜单 '交易管理 / 业务主体' → '管理主体'\n  - 表格列标题 '业务单元' → '管理主体'\n`  →  替换为 `ManagementEntity`
- `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md`:11893 → `"description": "改 UI 标签 + API 路径 + 前端 JS 变量名(businessUnit → managementEntity)。Ja`  →  替换为 `managementEntity`
- `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md`:11983 → `| **3. Backend Java** | `BusinessUnit.java` → `ManagementEntity.java`(6 个新文件 + 6`  →  替换为 `ManagementEntity`
- `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md`:13521 → `"command": "grep -B 1 -A 3 \"businessUnit\\|traderId\\|counterpartyId\\|instrume`  →  替换为 `managementEntity`
- `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md`:13576 → `"content": "# Open-TMS M3-外汇交易 PRD\n\n**版本**: v2.0（重大重构版）\n**角色**: 产品经理 (PM)\n**`  →  替换为 `managementEntity`
- `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md`:13647 → `"content": "# M3-金融工具 设计摘要\n\n## 最近更新\n- **日期**: 2026-07-04\n- **设计师**: PM (Clau`  →  替换为 `managementEntity`
- `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md`:14823 → `- 7 BusinessUnit Java classes inventoried (entity/dto/vo/mapper/service/impl/con`  →  替换为 `ManagementEntity`
- `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md`:14824 → `- 5 Java files (across basedata/dealing/fundplan/report) referencing BusinessUni`  →  替换为 `ManagementEntity`
- `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md`:14829 → `- 18 frontend files using businessUnit / 业务单元 / 业务主体 labels`  →  替换为 `managementEntity`
- `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md`:14853 → `- Error messages translated to Chinese (e.g. "BusinessUnit not found" → "管理主体不存在`  →  替换为 `ManagementEntity`
- `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md`:14876 → `web/src/api/basedata/businessUnit.js → managementEntity.js`  →  替换为 `managementEntity`
- `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md`:14877 → `web/src/views/basedata/BusinessUnitList.vue → ManagementEntityList.vue`  →  替换为 `ManagementEntity`
- `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md`:14878 → `- Removed legacy `listBusinessUnit` aliases from managementEntity.js`  →  替换为 `ManagementEntity`
- `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md`:14879 → `- Updated 18 Vue/JS files: businessUnit → managementEntity, 业务单元/业务主体 → 管理主体`  →  替换为 `managementEntity`
- `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md`:14911 → `- Subsidiary entity kept its internal `businessUnitCode` field semantic (subsidi`  →  替换为 `managementEntity`
- `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md`:15025 → `"command": "echo \"--- 服务状态 ---\"; netstat -ano | grep -E ':(8081|8082|3000) ' |`  →  替换为 `managementEntity`
- `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md`:15046 → `| **1. Discovery** | ✅ | 7 个 BusinessUnit Java 类 / 18 个前端文件 / 8 个 FK 列 / 3 个 VAR`  →  替换为 `ManagementEntity`
- `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md`:15051 → `| **6. 前端重命名** | ✅ | `businessUnit.js` → `managementEntity.js` / `BusinessUnitLi`  →  替换为 `managementEntity`
- `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md`:15051 → `| **6. 前端重命名** | ✅ | `businessUnit.js` → `managementEntity.js` / `BusinessUnitLi`  →  替换为 `ManagementEntity`
- `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md`:15064 → `| **业务单元 / businessUnit 残留** | ✅ **0 处** |`  →  替换为 `managementEntity`
- `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md`:15078 → `| `Subsidiary.businessUnitCode` 内部字段 | 仅 Java 字段重命名,表列不变(内部语义) |`  →  替换为 `managementEntity`
- `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md`:15304 → `| `docs/api/fundplan/01-fund-plan.md`(使用旧 `businessUnit` 字段) | fundplan | 字段未同步重`  →  替换为 `managementEntity`
- `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md`:15323 → `- `docs/api/fundplan/01-fund-plan.md`(`businessUnitId`)`  →  替换为 `managementEntity`
- `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md`:15324 → `- `docs/api/dealing/01-deal.md`(`businessUnitId`)`  →  替换为 `managementEntity`
- `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md`:15325 → `- `docs/superpowers/plans/2026-04-06-basedata-impl.md`(实体 `BusinessUnit.java`)`  →  替换为 `ManagementEntity`
- `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md`:15477 → `全局将 `业务单元` / `businessUnit` / `business-unit` / `BusinessUnit` → `管理主体` / `manag`  →  替换为 `managementEntity`
- `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md`:15477 → `全局将 `业务单元` / `businessUnit` / `business-unit` / `BusinessUnit` → `管理主体` / `manag`  →  替换为 `ManagementEntity`
- `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md`:15953 → `"description": "Update M3-外汇交易PRD: 增加交易日字段; businessUnit 改为 managementEntityId F`  →  替换为 `managementEntity`
- `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md`:16033 → `| 2 | **管理主体关联 ID** | `businessUnit`(VARCHAR 关联 code) → `managementEntityId`(BIG`  →  替换为 `managementEntity`
- `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md`:16059 → `| **跨模块一致性** | AC/AT/FX 全部需要 `managementEntityId` 替代 `businessUnit`,这是大改动 |`  →  替换为 `managementEntity`
- `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md`:16149 → `- Patterns replaced: 业务单元/businessUnit/business_unit/BusinessUnit/business-units`  →  替换为 `managementEntity`
- `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md`:16149 → `- Patterns replaced: 业务单元/businessUnit/business_unit/BusinessUnit/business-units`  →  替换为 `ManagementEntity`
- `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md`:16403 → `"new_string": "## 〇、修订记录\n\n### v3.1(2026-07-04) - 本次细化调整(用户产品评审反馈)\n\n| 修订项 | 修`  →  替换为 `managementEntity`
- `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md`:16421 → `"old_string": "### 3.4 通用交易字段(SPOT/FWD/NDF 共有) - P0\n\n| 字段 | 类型 | 必填 | 说明 |\n|-`  →  替换为 `managementEntity`
- `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md`:16479 → `"new_string": "> **v3.1 关键变化**:\n> - `businessUnit VARCHAR(50) code` → `manageme`  →  替换为 `managementEntity`
- `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md`:17042 → `- v3.1 的 `managementEntityId` 是 BIGINT FK,`tms_deals_t.businessUnit VARCHAR(50)``  →  替换为 `managementEntity`
- `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md`:28146 → `"command": "git commit -m \"chore: CLAUDE.md 瘦身 + 文档整理 + 会话导出工具 + 基础数据脚本 + Token`  →  替换为 `ManagementEntity`
- `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md`:33444 → `| 重构遗留 (BusinessUnit→ManagementEntity) 漏改 | 对比字段名 |`  →  替换为 `ManagementEntity`
- `docs/prd/M3/M3-外汇交易PRD.md`:34 → `| **管理主体 FK 改为 id** | `businessUnit VARCHAR(50) code` → `managementEntityId BIGI`  →  替换为 `managementEntity`
- `docs/prd/M3/M3-外汇交易PRD.md`:205 → `> - `businessUnit VARCHAR(50) code` → `managementEntityId BIGINT` 强 FK 关联 id`  →  替换为 `managementEntity`
- `docs/prd/M3/M3-现金流增强+Audit-History-PRD.md`:472 → `"businessUnit": "HK",`  →  替换为 `managementEntity`
- `docs/archive/2026-07-04-redundant-versions/M1-AC交易ActualCashflow PRD v2.md`:150 → `| 业务单元(BusinessUnit) | 现金流所属业务单元 | 组织架构 |`  →  替换为 `ManagementEntity`
- `docs/archive/2026-07-04-redundant-versions/M1-Deal交易 PRD v4.md`:35 → `| businessUnit | VARCHAR(50) | Y | 关联业务单元代码 |`  →  替换为 `managementEntity`
- `docs/archive/2026-07-04-redundant-versions/M1-Deal交易 PRD v4.md`:279 → `| businessUnit | 业务单元选择器 | businessUnit表，status=Active |`  →  替换为 `managementEntity`
- `docs/archive/2026-07-04-redundant-versions/M1-Deal交易 PRD v4.md`:421 → `| **BusinessUnit** | 交易归属 | businessUnit字段 |`  →  替换为 `managementEntity`
- `docs/archive/2026-07-04-redundant-versions/M1-Deal交易 PRD v4.md`:421 → `| **BusinessUnit** | 交易归属 | businessUnit字段 |`  →  替换为 `ManagementEntity`
- `docs/architecture/business/AC交易与现金流分离架构设计.md`:70 → `| businessUnit | VARCHAR(50) | 业务单元 |`  →  替换为 `managementEntity`
- `docs/architecture/business/AC交易与现金流分离架构设计.md`:103 → `| businessUnit | VARCHAR(50) | 业务单元 |`  →  替换为 `managementEntity`
- `docs/architecture/business/AC交易与现金流分离架构设计.md`:137 → `| businessUnit | VARCHAR(50) | 业务单元 |`  →  替换为 `managementEntity`
- `docs/architecture/business/AC交易与现金流分离架构设计.md`:455 → `| BusinessUnit | ACDeal.businessUnit, Cashflow.businessUnit |`  →  替换为 `managementEntity`
- `docs/architecture/business/AC交易与现金流分离架构设计.md`:455 → `| BusinessUnit | ACDeal.businessUnit, Cashflow.businessUnit |`  →  替换为 `ManagementEntity`
- `summary.md`:63 → `| **basedata** | basedata/ | ✅ 开发中 | Bank, BusinessUnit, Counterparty, Counterpa`  →  替换为 `ManagementEntity`
- `summary.md`:81 → `| **basedata** | BankList, BusinessUnitList, CounterpartyList, CounterpartyAccou`  →  替换为 `ManagementEntity`


### P2 — 可延后(注释/历史 DDL/需求说明)
- `docs/CHANGELOG.md`:45 → `- Batch rename: `业务单元` → `管理主体` (and `businessUnit`/`business_unit`/`BusinessUni`  →  替换为 `management_entity`
- `docs/CHANGELOG.md`:45 → `- Batch rename: `业务单元` → `管理主体` (and `businessUnit`/`business_unit`/`BusinessUni`  →  替换为 `managementEntity`
- `docs/CHANGELOG.md`:45 → `- Batch rename: `业务单元` → `管理主体` (and `businessUnit`/`business_unit`/`BusinessUni`  →  替换为 `ManagementEntity`
- `docs/CHANGELOG.md`:66 → `- DB: `tms_business_unit_t` → `tms_management_entity_t``  →  替换为 `management_entity`
- `docs/conversations/3a73c9ba-a60d-4e56-8d2d-94f205420fb8.md`:2003 → `找到根因!**PostgreSQL 报错 `字段 "business_unit_id" 不存在`**,SQL 试图从 `tms_bank_account_t` `  →  替换为 `management_entity`
- `docs/conversations/3a73c9ba-a60d-4e56-8d2d-94f205420fb8.md`:2034 → `"command": "grep -E \"^\\s*\\*.*business_unit|business_unit\" db/schema/*.sql | `  →  替换为 `management_entity`
- `docs/conversations/3a73c9ba-a60d-4e56-8d2d-94f205420fb8.md`:2035 → `"description": "查DB schema中business_unit 定义及Entity引用"`  →  替换为 `management_entity`
- `docs/conversations/3a73c9ba-a60d-4e56-8d2d-94f205420fb8.md`:2076 → `"command": "cd /f/code/opencode/opentrm && grep -rE \"business_unit_id|businessU`  →  替换为 `management_entity`
- `docs/conversations/3a73c9ba-a60d-4e56-8d2d-94f205420fb8.md`:2077 → `"description": "查business_unit 字段定义"`  →  替换为 `management_entity`
- `docs/conversations/3a73c9ba-a60d-4e56-8d2d-94f205420fb8.md`:2109 → `**关键发现**:`23-rename-business-unit.sql` 把 `business_unit_id` 重命名为 `management_ent`  →  替换为 `management_entity`
- `docs/conversations/3a73c9ba-a60d-4e56-8d2d-94f205420fb8.md`:2151 → `- DB 里叫 `management_entity_id`(不是 `business_unit_id`) — `23-rename-business-unit`  →  替换为 `management_entity`
- `docs/conversations/3a73c9ba-a60d-4e56-8d2d-94f205420fb8.md`:2198 → `**Bug**:PR `69c34ab` 实际上**做了反向操作** — 它把 Java Entity 的 `managementEntityId` 改成 `b`  →  替换为 `managementEntity`
- `docs/conversations/3a73c9ba-a60d-4e56-8d2d-94f205420fb8.md`:2318 → `**PR `69c34ab` 是把 Entity 字段名改回 `businessUnitId`**(配 `@TableField("business_unit_`  →  替换为 `management_entity`
- `docs/conversations/3a73c9ba-a60d-4e56-8d2d-94f205420fb8.md`:2318 → `**PR `69c34ab` 是把 Entity 字段名改回 `businessUnitId`**(配 `@TableField("business_unit_`  →  替换为 `managementEntity`
- `docs/conversations/3a73c9ba-a60d-4e56-8d2d-94f205420fb8.md`:2320 → `也就是说这个 PR 在作者本地是工作的(可能他本地 DB 状态与生产不一致),但合并后所有基于该 PR 的代码部署都用 `business_unit_id`,而`  →  替换为 `management_entity`
- `docs/conversations/3a73c9ba-a60d-4e56-8d2d-94f205420fb8.md`:2369 → `"question": "远程 PR #9 (69c34ab) 有回归 bug——Java Entity 改成 businessUnitId + @TableF`  →  替换为 `management_entity`
- `docs/conversations/3a73c9ba-a60d-4e56-8d2d-94f205420fb8.md`:2369 → `"question": "远程 PR #9 (69c34ab) 有回归 bug——Java Entity 改成 businessUnitId + @TableF`  →  替换为 `managementEntity`
- `docs/conversations/3a73c9ba-a60d-4e56-8d2d-94f205420fb8.md`:2374 → `"description": "改 Java 代码: BankAccount/Subsidiary/ManagementEntity/CurrencyPair `  →  替换为 `management_entity`
- `docs/conversations/3a73c9ba-a60d-4e56-8d2d-94f205420fb8.md`:2378 → `"description": "依赖 DB 脚本脚本加 ALTER TABLE ... RENAME COLUMN management_entity_id T`  →  替换为 `management_entity`
- `docs/conversations/3a73c9ba-a60d-4e56-8d2d-94f205420fb8.md`:2512 → `"command": "grep -E \"businessUnitId|managementEntityId|@TableField\" basedata/s`  →  替换为 `management_entity`
- `docs/conversations/3a73c9ba-a60d-4e56-8d2d-94f205420fb8.md`:2882 → `精确诊断:DB 中列叫 `business_unit_code`,SQL 用的仍是 `management_entity_code`。这是因为 **`Subsi`  →  替换为 `management_entity`
- `docs/conversations/3a73c9ba-a60d-4e56-8d2d-94f205420fb8.md`:2950 → `和 revert 结果一致 — 我加回 `@TableField("business_unit_code")` 注解是对的(等同 PR 的"子集"修复,因为 D`  →  替换为 `management_entity`
- `docs/conversations/3a73c9ba-a60d-4e56-8d2d-94f205420fb8.md`:3458 → `**远程 vs 本地 WIP 没有真实冲突**(只在 4 个对话记录文件上有重合,但内容互补不冲突)。真正的"智能合并"是**发现并修复了 PR #9 的回归 `  →  替换为 `managementEntity`
- `docs/conversations/3a73c9ba-a60d-4e56-8d2d-94f205420fb8.md`:3464 → `| `BankAccount.managementEntityId` 字段恢复(移除 `@TableField("business_unit_id")`) | `  →  替换为 `management_entity`
- `docs/conversations/3a73c9ba-a60d-4e56-8d2d-94f205420fb8.md`:3465 → `| `Subsidiary.managementEntityCode` 字段恢复 + 加 `@TableField("business_unit_code")``  →  替换为 `management_entity`
- `docs/conversations/3a73c9ba-a60d-4e56-8d2d-94f205420fb8.md`:5181 → `**根因汇总**: **(F) + (C 的衍生)** — `filteredBankAccounts` computed 用 `businessUnitId``  →  替换为 `managementEntity`
- `docs/conversations/3a73c9ba-a60d-4e56-8d2d-94f205420fb8.md`:5259 → `**结论**: 单点修复 — `DefaultBankAccountRuleList.vue:300` 一行 `businessUnitId` → `manag`  →  替换为 `managementEntity`
- `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md`:10935 → `**400 提示"managementEntity 不能为空"** — dealing 的 AC 端点实际字段名是 `managementEntity`,不是 `  →  替换为 `managementEntity`
- `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md`:21347 → `<command-args>开启子代理，编写工具扫描系统代码、文档、数据库表中是否还有业务单元（business_unit）的存在</command-args>`  →  替换为 `management_entity`
- `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md`:21782 → `ARGUMENTS: 开启子代理，编写工具扫描系统代码、文档、数据库表中是否还有业务单元（business_unit）的存在`  →  替换为 `management_entity`
- `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md`:21797 → `"subject": "扫描 business_unit 残留工具",`  →  替换为 `management_entity`
- `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md`:21798 → `"description": "写 scripts/scan_business_unit.py:扫描 4 个层面残留:1) Java 代码(@Column/En`  →  替换为 `management_entity`
- `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md`:21799 → `"activeForm": "扫描 business_unit 残留"`  →  替换为 `management_entity`
- `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md`:21834 → `"description": "扫描 business_unit 残留工具",`  →  替换为 `management_entity`
- `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md`:21835 → `"prompt": "Open-TMS 项目位于 F:\\code\\opencode\\opentrm。**只读扫描** — 编写工具扫描系统中是否还有 \"`  →  替换为 `management_entity`
- `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md`:21835 → `"prompt": "Open-TMS 项目位于 F:\\code\\opencode\\opentrm。**只读扫描** — 编写工具扫描系统中是否还有 \"`  →  替换为 `managementEntity`
- `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md`:21835 → `"prompt": "Open-TMS 项目位于 F:\\code\\opencode\\opentrm。**只读扫描** — 编写工具扫描系统中是否还有 \"`  →  替换为 `MANAGEMENT_ENTITY`
- `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md`:21848 → `## ⚡ 扫描 business_unit 残留子代理已启动`  →  替换为 `management_entity`
- `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md`:21852 → `| `a26c31bfc3a01b1d6` | 写 `scripts/scan_business_unit.py` + 跑报告 | 🟢 |`  →  替换为 `management_entity`
- `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md`:21855 → `1. **Java 代码**:`**/*.java` in basedata/dealing/common,找 `businessUnit` / `@Colum`  →  替换为 `management_entity`
- `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md`:21865 → `- `scripts/scan_business_unit.py` — 工具`  →  替换为 `management_entity`
- `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md`:21884 → `<summary>Agent "扫描 business_unit 残留工具" finished</summary>`  →  替换为 `management_entity`
- `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md`:21893 → `- `F:\code\opencode\opentrm\scripts\scan_business_unit.py` — 587 行,沿用 `find_simi`  →  替换为 `management_entity`
- `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md`:21912 → `- **数据库**: 31 处 P0。包括整张遗留表 `trm_business_unit_t`(根本没改名)、`tms_cashflow_t.business`  →  替换为 `management_entity`
- `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md`:21913 → `- **Java**: 6 处。`@TableField("business_unit")` / `@TableField("business_unit_cod`  →  替换为 `management_entity`
- `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md`:21919 → `1. `public.tms_cashflow_t.business_unit` — 列未改名(commit 23 注释说改了,实际未生效)`  →  替换为 `management_entity`
- `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md`:21920 → `2. `public.trm_business_unit_t` — 整张表未改名(注意不是 `tms_*`,是 `trm_*`,可能是另一个 schema 的旧`  →  替换为 `management_entity`
- `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md`:21921 → `3. `public.tms_subsidiary_t.business_unit_code` — 列名漏改(语义为"子公司所属业务单元",需决策)`  →  替换为 `management_entity`
- `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md`:21922 → `4. `basedata/src/main/java/com/opentms/basedata/entity/Subsidiary.java:24` — `@T`  →  替换为 `management_entity`
- `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md`:21923 → `5. `dealing/src/main/java/com/opentms/dealing/entity/Cashflow.java:32` — `@Table`  →  替换为 `management_entity`
- `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md`:21924 → `6. `dealing/src/main/java/com/opentms/dealing/entity/CashflowImage.java:51-52` —`  →  替换为 `management_entity`
- `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md`:21928 → `- 4 种命名模式: `business_unit` / `businessUnit` / `BUSINESS_UNIT` / `BusinessUnit``  →  替换为 `management_entity`
- `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md`:21928 → `- 4 种命名模式: `business_unit` / `businessUnit` / `BUSINESS_UNIT` / `BusinessUnit``  →  替换为 `MANAGEMENT_ENTITY`
- `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md`:21930 → `- 自动严重度: 注释 / 历史关键字 → P2;docs 默认 P2;`BUSINESS_UNIT` 实体类型枚举值 → false positive 过滤`  →  替换为 `MANAGEMENT_ENTITY`
- `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md`:21938 → `1. **未触达的 schema**: `trm_business_unit_t`(整个 schema 与 `tms_` 并存)`  →  替换为 `management_entity`
- `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md`:21939 → `2. **未触达的表**: `tms_cashflow_t` / `tms_cashflow_image_t`(commit 23 注释列了 `tms_cash`  →  替换为 `management_entity`
- `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md`:21940 → `3. **未触达的约束 / 索引**: 大批 `idx_*_business_unit` / `*_not_null` 自动生成的约束名都未跟随列重命名而更新`  →  替换为 `management_entity`
- `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md`:21950 → `**扫描器完成,评级 D** — 发现**系统性残留 31 处 P0**!最关键是 commit 23 注释说改了 `tms_cashflow_t.busine`  →  替换为 `management_entity`
- `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md`:21959 → `"command": "ls -la scripts/scan_business_unit.py docs/reviews/business-unit-audi`  →  替换为 `management_entity`
- `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md`:21992 → `## ✅ business_unit 扫描器完成 + 系统性残留 D 级警告`  →  替换为 `management_entity`
- `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md`:21997 → `| `scripts/scan_business_unit.py` | 22.5KB / 587 行 | 4 模式 + 4 类别扫描器 |`  →  替换为 `management_entity`
- `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md`:22012 → `1. **`tms_cashflow_t.business_unit`** 列没改(commit 23 注释说改了,实际未生效)`  →  替换为 `management_entity`
- `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md`:22013 → `2. **`trm_business_unit_t`** 整张表未改名(注意不是 `tms_*`,是 `trm_*`,可能是另一个 schema)`  →  替换为 `management_entity`
- `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md`:22014 → `3. **`tms_subsidiary_t.business_unit_code`** 列名漏改(语义"子公司所属业务单元",需决策)`  →  替换为 `management_entity`
- `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md`:22016 → `5. **Java 6 处 P0**:`Subsidiary.java` / `Cashflow.java` / `CashflowImage.java` 用 `  →  替换为 `management_entity`
- `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md`:22019 → `- `public.tms_cashflow_t.business_unit` 列(P0)`  →  替换为 `management_entity`
- `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md`:22020 → `- `public.trm_business_unit_t` 整张表(P0)`  →  替换为 `management_entity`
- `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md`:22021 → `- `public.tms_subsidiary_t.business_unit_code` 列(P0)`  →  替换为 `management_entity`
- `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md`:22022 → `- `basedata/.../Subsidiary.java:24` `@TableField("business_unit_code")`(P0)`  →  替换为 `management_entity`
- `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md`:22023 → `- `dealing/.../Cashflow.java:32` `@TableField("business_unit")`(P0)`  →  替换为 `management_entity`
- `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md`:22028 → `- ALTER TABLE tms_cashflow_t RENAME COLUMN business_unit TO management_entity`  →  替换为 `management_entity`
- `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md`:22030 → `- 改 trm_business_unit_t → trm_management_entity_t`  →  替换为 `management_entity`
- `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md`:22085 → `- API 扫描器 / 集成 skill / 端到端 / 根因 / 5 bug 回溯 / **business_unit 扫描器(D 级,系统残留 271 处)`  →  替换为 `management_entity`
- `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md`:22095 → `- **修 business_unit 系统残留**(扫描器评级 D,34 P0)— 写 30-fix-business-unit-residue.sql + `  →  替换为 `management_entity`
- `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md`:22098 → `主对话可继续发新指令(例:开子代理修 business_unit 残留,或 commit 近期所有产出)。`  →  替换为 `management_entity`
- `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md`:22119 → `"subject": "business_unit 残留清理迁移",`  →  替换为 `management_entity`
- `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md`:22120 → `"description": "扫描器发现 271 处 business_unit 残留,34 P0。子任务:1) 写 30-fix-business-unit`  →  替换为 `management_entity`
- `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md`:22121 → `"activeForm": "清理 business_unit 残留"`  →  替换为 `management_entity`
- `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md`:22156 → `"command": "cat docs/reviews/business-unit-audit.md | grep -E \"tms_cashflow|trm`  →  替换为 `management_entity`
- `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md`:22171 → `- 2 个列名 `business_unit` 改 `management_entity_id`(cashflow + cashflow_image)`  →  替换为 `management_entity`
- `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md`:22172 → `- 1 个 `business_unit_code` 改 `management_entity_id`(subsidiary)`  →  替换为 `management_entity`
- `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md`:22173 → `- 1 个表 `trm_business_unit_t` 改 `trm_management_entity_t``  →  替换为 `management_entity`
- `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md`:22187 → `"description": "清理 business_unit 系统残留",`  →  替换为 `management_entity`
- `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md`:22188 → `"prompt": "Open-TMS 项目位于 F:\\code\\opencode\\opentrm。**完整清理业务单元残留** — 含 5 个子任务。\`  →  替换为 `management_entity`
- `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md`:22201 → `## ⚡ 清理 business_unit 残留子代理已启动`  →  替换为 `management_entity`
- `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md`:22210 → `3. **MyBatis SQL** — XML / 注解里的 `business_unit` 全改`  →  替换为 `management_entity`
- `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md`:22215 → `- 2 列:`tms_cashflow_t.business_unit` + `tms_cashflow_image_t.business_unit` → `m`  →  替换为 `management_entity`
- `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md`:22216 → `- 1 列:`tms_subsidiary_t.business_unit_code` → `management_entity_id``  →  替换为 `management_entity`
- `docs/conversations/3f267ac3-2677-4c18-8f7a-fc6f57bd235b.md`:22217 → `- 1 表:`trm_business_unit_t` → `trm_management_entity_t``  →  替换为 `management_entity`
- `docs/conversations/b04bc295-2776-422a-9dde-2770d157fe9a.md`:8370 → `**Note:** The `businessUnit.js` API was kept because other pages (DealList, Deal`  →  替换为 `managementEntity`
- `docs/conversations/b04bc295-2776-422a-9dde-2770d157fe9a.md`:8370 → `**Note:** The `businessUnit.js` API was kept because other pages (DealList, Deal`  →  替换为 `ManagementEntity`
- `docs/conversations/b04bc295-2776-422a-9dde-2770d157fe9a.md`:9046 → `**文件**: `web/src/views/basedata/BusinessUnitList.vue``  →  替换为 `ManagementEntity`
- `docs/conversations/b04bc295-2776-422a-9dde-2770d157fe9a.md`:25620 → `"command": "cd F:/code/opencode/opentrm && python -c \"\nimport pg8000\nconn = p`  →  替换为 `management_entity`
- `docs/conversations/db4c78f4-75ca-44ba-becd-821986d3c78b.md`:3411 → `| 6 | 缺少 `business_unit` 字段 | 无 | Cashflow 已有 business_unit，DealMap 是否需要冗余？ |`  →  替换为 `management_entity`
- `docs/conversations/db4c78f4-75ca-44ba-becd-821986d3c78b.md`:3585 → `"description": "应增加 business_unit、counterparty_id 等基础信息（不依赖下游表反查）"`  →  替换为 `management_entity`
- `docs/conversations/db4c78f4-75ca-44ba-becd-821986d3c78b.md`:3615 → `| **Q7 表结构** | 补充业务维度字段 | 添加 `business_unit`、`counterparty_id` 等不依赖下游表反查的字段 |`  →  替换为 `management_entity`
- `docs/conversations/db4c78f4-75ca-44ba-becd-821986d3c78b.md`:3652 → `"description": "应用本次决策：(1) 表结构增加 business_unit、counterparty_id 等业务维度字段；(2) 重写 AC`  →  替换为 `management_entity`
- `docs/conversations/db4c78f4-75ca-44ba-becd-821986d3c78b.md`:3697 → `我开始应用 v1.2 修订。首先更新表结构（添加 `business_unit` + `counterparty_id`）：`  →  替换为 `management_entity`
- `docs/conversations/db4c78f4-75ca-44ba-becd-821986d3c78b.md`:3901 → `"new_string": "## 设计过程记录\n\n### 2026-06-21 - DealMap 生命周期事件设计 v1.2 修订\n**完成内容**:`  →  替换为 `management_entity`
- `docs/conversations/db4c78f4-75ca-44ba-becd-821986d3c78b.md`:3934 → `| `business_unit` | VARCHAR(50) | ✅ 必填 | 资金管理主体（避免反查 Deal/Cashflow） |`  →  替换为 `management_entity`
- `docs/conversations/db4c78f4-75ca-44ba-becd-821986d3c78b.md`:3939 → `并新增 **CHECK 约束** + 3 个索引（`business_unit` / `counterparty_id` / `account_role` + `  →  替换为 `management_entity`
- `docs/conversations/db4c78f4-75ca-44ba-becd-821986d3c78b.md`:4419 → `"old_string": "## 十、决策记录\n\n### v1.2 决策（2026-06-21）\n\n| # | 决策项 | 决策 | 决策依据 |\n`  →  替换为 `management_entity`
- `docs/conversations/db4c78f4-75ca-44ba-becd-821986d3c78b.md`:4443 → `"old_string": "## 十二、验收标准\n\n### v1.2 新增验收点\n\n| 功能 | 验收条件 |\n|------|----------`  →  替换为 `management_entity`
- `docs/conversations/db4c78f4-75ca-44ba-becd-821986d3c78b.md`:4467 → `"old_string": "-- 1. 主表（v1.2 完整版，含业务维度字段）\nCREATE TABLE tms_deal_map_t (\n    id`  →  替换为 `management_entity`
- `docs/conversations/db4c78f4-75ca-44ba-becd-821986d3c78b.md`:4491 → `"old_string": "## 最近更新\n- **日期**: 2026-06-21\n- **设计师**: BA + PM\n- **本次完成**: De`  →  替换为 `management_entity`
- `docs/conversations/db4c78f4-75ca-44ba-becd-821986d3c78b.md`:4723 → `│    - business_unit/amount/currency/... = 修改前的旧值      │`  →  替换为 `management_entity`
- `docs/conversations/db4c78f4-75ca-44ba-becd-821986d3c78b.md`:4768 → `business_unit, amount, currency, value_date, status,`  →  替换为 `management_entity`
- `docs/conversations/db4c78f4-75ca-44ba-becd-821986d3c78b.md`:4881 → `business_unit, amount, currency, status,`  →  替换为 `management_entity`
- `docs/conversations/db4c78f4-75ca-44ba-becd-821986d3c78b.md`:4951 → `business_unit, counterparty_id, product_type, account_role,`  →  替换为 `management_entity`
- `docs/conversations/db4c78f4-75ca-44ba-becd-821986d3c78b.md`:4978 → `business_unit, bank_account, counterparty_account,`  →  替换为 `management_entity`
- `docs/conversations/db4c78f4-75ca-44ba-becd-821986d3c78b.md`:5465 → `counterparty_id, business_unit, product_type, account_role`  →  替换为 `management_entity`
- `docs/conversations/db4c78f4-75ca-44ba-becd-821986d3c78b.md`:5631 → `deal_number, deal_type, business_unit,`  →  替换为 `management_entity`
- `docs/conversations/db4c78f4-75ca-44ba-becd-821986d3c78b.md`:5672 → `business_unit, bank_account, counterparty_account,`  →  替换为 `management_entity`
- `docs/conversations/db4c78f4-75ca-44ba-becd-821986d3c78b.md`:5832 → `business_unit, amount, currency, deal_date, value_date, status,`  →  替换为 `management_entity`
- `docs/conversations/db4c78f4-75ca-44ba-becd-821986d3c78b.md`:5971 → `business_unit, amount, currency, deal_date, value_date, status,`  →  替换为 `management_entity`
- `docs/conversations/db4c78f4-75ca-44ba-becd-821986d3c78b.md`:12192 → `"command": "cd \"F:/code/opencode/opentrm\" && python scripts/db/db_tool.py -s \`  →  替换为 `management_entity`
- `docs/conversations/db4c78f4-75ca-44ba-becd-821986d3c78b.md`:12467 → `"command": "cd \"F:/code/opencode/opentrm\" && cat > /tmp/insert_accounts.sql <<`  →  替换为 `management_entity`
- `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md`:4250 → `| transfer_type | `SAME_COMPANY` / `CROSS_COMPANY` / `CROSS_BORDER`(系统按 `busines`  →  替换为 `management_entity`
- `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md`:6570 → `balance, available_balance, frozen_balance, is_main, business_unit_id,`  →  替换为 `management_entity`
- `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md`:6581 → `business_unit_id, status, remark,`  →  替换为 `management_entity`
- `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md`:11763 → `"description": "Find all files referencing 业务单元/业务主体/BusinessUnit/businessUnit; `  →  替换为 `managementEntity`
- `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md`:11763 → `"description": "Find all files referencing 业务单元/业务主体/BusinessUnit/businessUnit; `  →  替换为 `ManagementEntity`
- `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md`:11889 → `"preview": "改的地方:\n  - 侧边栏菜单 '交易管理 / 业务主体' → '管理主体'\n  - 表格列标题 '业务单元' → '管理主体'\n`  →  替换为 `management_entity`
- `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md`:11960 → `"prompt": "You are a subagent doing a full-stack refactor on the Open-TMS projec`  →  替换为 `management_entity`
- `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md`:11960 → `"prompt": "You are a subagent doing a full-stack refactor on the Open-TMS projec`  →  替换为 `ManagementEntity`
- `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md`:11982 → `| **2. DB Migration** | `tms_business_unit_t` → `tms_management_entity_t` + `bus`  →  替换为 `management_entity`
- `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md`:11986 → `| **6. Frontend rename** | `businessUnit.js` → `managementEntity.js` + 所有 vue 文件`  →  替换为 `managementEntity`
- `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md`:13743 → `business_unit      VARCHAR(50)   -- 引用 management-entity.code`  →  替换为 `management_entity`
- `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md`:14825 → `- 1 active DB table: tms_business_unit_t (5 test rows); 1 orphan trm_business_un`  →  替换为 `management_entity`
- `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md`:14826 → `- 8 tables had business_unit_id FK column`  →  替换为 `management_entity`
- `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md`:14827 → `- 3 tables had business_unit VARCHAR column`  →  替换为 `management_entity`
- `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md`:14828 → `- 1 FK constraint fk_deal_business_unit`  →  替换为 `management_entity`
- `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md`:14835 → `tms_business_unit_t → tms_management_entity_t  (5 rows preserved)`  →  替换为 `management_entity`
- `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md`:14836 → `business_unit_id → management_entity_id (in 8 tables)`  →  替换为 `management_entity`
- `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md`:14837 → `business_unit → management_entity (in 3 tables)`  →  替换为 `management_entity`
- `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md`:14838 → `tms_business_unit_t_id_seq → tms_management_entity_t_id_seq`  →  替换为 `management_entity`
- `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md`:14839 → `fk_deal_business_unit → fk_deal_management_entity`  →  替换为 `management_entity`
- `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md`:14851 → `- Subsidiary entity/DTO/VO/service (businessUnitCode → managementEntityCode, int`  →  替换为 `managementEntity`
- `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md`:15122 → `3. **`subsidiary` 内部 Java 字段 `businessUnitCode` 保留语义**(子公司→关联哪个管理主体),仅 Java came`  →  替换为 `managementEntity`
- `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md`:15322 → `- `docs/api/basedata/01-bank-accounts.md`(`business_unit_id`)`  →  替换为 `management_entity`
- `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md`:15439 → `- `docs/api/` 有 `README.md`(类似 SUMMARY),但内容陈旧 — 引用 `tms_country_t`、`tms_business`  →  替换为 `management_entity`
- `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md`:15478 → `- DB schema 是否同步更新?(需要确认 `tms_business_unit_t` 表是否重命名为 `tms_management_entity_t``  →  替换为 `management_entity`
- `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md`:15782 → `- `docs/api/basedata/01-bank-accounts.md`(`business_unit_id`)`  →  替换为 `management_entity`
- `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md`:16149 → `- Patterns replaced: 业务单元/businessUnit/business_unit/BusinessUnit/business-units`  →  替换为 `management_entity`
- `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md`:21601 → `"prompt": "You are a subagent designing **Phase 3 (DB) + Phase 4 (API)** for the`  →  替换为 `management_entity`
- `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md`:24329 → `- Naming consistency: Renamed 业务单元/业务主体→管理主体 globally, refactored tms_business_u`  →  替换为 `management_entity`
- `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md`:24471 → `- `dealing/src/main/java/com/opentms/dealing/entity/Cashflow.java` — fixed `mana`  →  替换为 `management_entity`
- `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md`:24489 → `- **Cashflow entity column mapping**: `management_entity` field mapped to column`  →  替换为 `management_entity`
- `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md`:26182 → `| 4 | Cashflow `management_entity` 列映射 | 修复为 `business_unit` |`  →  替换为 `management_entity`
- `docs/conversations/e28d9219-31df-4e73-915f-1e4179bf7880.md`:28107 → `"command": "git commit -m \"feat(dealing): FX 外汇交易 v3.2 全流程研发 + AC/AT 复制功能 + Bas`  →  替换为 `management_entity`
- `docs/prd/M3/M3-现金流增强+Audit-History-PRD.md`:186 → `- deal_number, deal_type, business_unit, counterparty_id, instrument_id, ...`  →  替换为 `management_entity`
- `docs/prd/M3/M3-现金流增强+Audit-History-PRD.md`:246 → `business_unit               VARCHAR(50),`  →  替换为 `management_entity`
- `docs/prd/M3/M3-货币市场MM交易PRD.md`:620 → `- `id / deal_number / deal_type / business_unit / counterparty_id / instrument_i`  →  替换为 `management_entity`
- `docs/archive/2026-07-04-redundant-versions/M1-Deal交易 PRD v4.md`:359 → `business_unit VARCHAR(50) NOT NULL,`  →  替换为 `management_entity`
- `docs/archive/2026-07-04-redundant-versions/M1-Deal交易 PRD v4.md`:381 → `CREATE INDEX idx_deal_unit ON tms_deals_t(business_unit);`  →  替换为 `management_entity`
- `docs/architecture/business/AC交易与现金流分离架构设计.md`:322 → `business_unit VARCHAR(50) NOT NULL,`  →  替换为 `management_entity`
- `docs/architecture/business/AC交易与现金流分离架构设计.md`:353 → `business_unit VARCHAR(50) NOT NULL,`  →  替换为 `management_entity`
- `docs/architecture/business/AC交易与现金流分离架构设计.md`:387 → `business_unit VARCHAR(50) NOT NULL,`  →  替换为 `management_entity`


## 4. 评级标准

| 评级 | 含义 | 行动 |
|------|------|------|
| **A** | 0 处残留 | 通过 |
| **B** | 1-2 处,多为注释/历史 DDL | 通过,清理即可 |
| **C** | 3-10 处,可能影响 | 1-2 天整改 |
| **D** | >10 处,系统性残留 | 需重新评估 rename 完整性 |

## 5. 工具用法

```bash
python scripts/scan_business_unit.py                       # 全量扫描
python scripts/scan_business_unit.py --json out.json       # 同时输出 JSON
python scripts/scan_business_unit.py --only java           # 仅 java
python scripts/scan_business_unit.py --only frontend       # 仅 frontend
python scripts/scan_business_unit.py --only docs           # 仅 docs
python scripts/scan_business_unit.py --only db             # 仅 db
python scripts/scan_business_unit.py --root .              # 指定项目根
```

## 6. 扫描覆盖

- **预置模式**: `business_unit` (snake_case) / `businessUnit` (camelCase) /
  `BUSINESS_UNIT` (UPPER_CASE) / `BusinessUnit` (PascalCase)
- **Java 范围**: `basedata / dealing / common` 三个模块 `src/main/java/**/*.java`
- **前端范围**: `web/src/**/*.{vue,js,ts}`
- **文档范围**: `docs/**/*.md` + 根目录 `*.md`
- **DB 范围**: `information_schema.columns/tables/views/sequences` + `pg_indexes` + `pg_constraint`
- **排除**: `target/` / `node_modules/` / `.class` / `.jar`

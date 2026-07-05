# Open-TMS Skills 架构

> 项目级 Skill 索引 — 编排器(Orchestrator)+ 角色(Role)+ 审核(Review)三类 Skill 的整体架构。

---

## 1. Skill 分类

### 1.1 编排器 Skill (Orchestrator) — 1 个

| Skill | 职责 |
|-------|------|
| `opentms-feature-dev` | 全流程编排器:PM→UX→DB→API→BE→FE→QA,**在每个 Phase 出口嵌入审核门禁** |

### 1.2 角色 Skill (Role) — 10+ 个

#### PM/产品 (2)
- `opentms-pm-lead` — PM-Lead 管理(Sprint/团队/交付)
- `opentms-product-design` — PRD 设计

#### BA/业务架构 (1)
- `opentms-business-architect` — 业务架构 + 业界对标(FIS Quantum / Murex / SAP TRM)

#### UX/设计 (2)
- `opentms-ux-design` — UX 交互设计
- `opentms-ux` — UX 设计(备用)

#### TA/技术架构 (3)
- `opentms-ta` — 技术架构
- `opentms-db-design` — 数据库设计
- `opentms-api-design` — 接口设计

#### Dev/开发 (3)
- `opentms-backend-dev` — 后端开发
- `opentms-frontend-dev` — 前端开发
- `opentms-basedata-backend-develop` — 基础数据模块开发

#### QA/测试 (3)
- `opentms-qa` — QA 管理
- `opentms-test-case-design` — 测试用例设计
- `opentms-test-execution` — 测试执行

#### 工具 (1)
- `skill-optimization` — Skill 自优化

### 1.3 审核 Skill (Review) — 6 + 1 公共

#### 公共 (1)
- `opentms-review-common` — **审核公共基础**(评级体系 / 报告模板 / 调用方式)

#### 6 维审核 (6)
| Skill | 审核维度 | 触发位置 | 状态 |
|-------|---------|---------|------|
| `opentms-review-requirement` | 需求审核 | Phase 1 → Phase 2 (P0) | ✅ 已创建 |
| `opentms-review-ux` | UX 审核 | Phase 2 → Phase 3 | ✅ 已创建 |
| `opentms-review-db` | DB 审核 | Phase 3 → Phase 4 (P0) | 📋 待创建 |
| `opentms-review-api` | API 审核 | Phase 4 → Phase 5 (P0) | 📋 待创建 |
| `opentms-review-backend` | 后端代码审核 | Phase 5 → Phase 6 | 📋 待创建 |
| `opentms-review-frontend` | 前端代码审核 | Phase 6 → Phase 7 | 📋 待创建 |

---

## 2. 调用顺序(在 feature-dev 内嵌)

```
特征启动
  │
  ├─ Phase 1: 产品设计 (opentms-product-design)
  │       ↓
  │   [门禁]  opentms-review-requirement    ← P0 必做
  │       ↓
  ├─ Phase 2: UX 设计 (opentms-ux-design)
  │       ↓
  │   [门禁]  opentms-review-ux
  │       ↓
  ├─ Phase 3: DB 设计 (opentms-db-design)
  │       ↓
  │   [门禁]  opentms-review-db             ← P0 必做
  │       ↓
  ├─ Phase 4: API 设计 (opentms-api-design)
  │       ↓
  │   [门禁]  opentms-review-api            ← P0 必做
  │       ↓
  ├─ Phase 5: 后端开发 (opentms-backend-dev)
  │       ↓
  │   [门禁]  opentms-review-backend
  │       ↓
  ├─ Phase 6: 前端开发 (opentms-frontend-dev)
  │       ↓
  │   [门禁]  opentms-review-frontend
  │       ↓
  ├─ Phase 7: 测试设计 (opentms-test-case-design)
  │       ↓
  │   [门禁]  QA 自评
  │       ↓
  ├─ Phase 8: 测试执行 (opentms-test-execution)
  │       ↓
  │   [门禁]  测试报告审核
  │       ↓
  └─ Phase 9: 交付前总审核(6 维全量复审)
```

---

## 3. 审核体系核心规范

| 维度 | 规范 |
|------|------|
| 评级 | A(无问题) / B(仅 P2) / C(有 P1) / D(有 P0) |
| 严重度 | P0(阻塞) / P1(重要) / P2(优化) |
| 报告路径 | `docs/reviews/{feature}/{dimension}-review.md` |
| 公共规范 | `opentms-review-common` (必读) |
| 一票否决 | 模块循环依赖 / 魔术字符串 / 缺审计字段 / 表名违规 / API 违反 POST 红线 |

详见 `.claude/skills/opentms-review-common/SKILL.md`。

---

## 4. 文档与规范

| 文档 | 路径 | 说明 |
|------|------|------|
| **CLAUDE.md** | `/CLAUDE.md` | 项目根规范 |
| **开发规范** | `docs/规范/Open-TMS开发规范文档.md` | 详细编码规范 |
| **业务架构** | `docs/architecture/business/` | AC/AT/DealMap 架构 |
| **团队协作** | `open-tms团队协作规范.md` | GitHub Projects 操作 |
| **审核公共** | `.claude/skills/opentms-review-common/SKILL.md` | 审核体系公共基础 |

---

## 5. 版本

| 版本 | 日期 | 变更 |
|------|------|------|
| v1.0 | 2026-07-05 | 初始版本 — Skill 架构总览 + 审核门禁体系引入 |
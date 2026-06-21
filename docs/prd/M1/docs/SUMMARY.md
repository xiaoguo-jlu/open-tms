# M1-基础数据 模块设计摘要

## 最近更新
- **日期**: 2026-06-21
- **设计师**: BA + PM
- **本次完成**: DealMap 生命周期事件PRD v2.0 重大重构（字段精简 + 流程重构）

---

## 设计过程记录

### 2026-06-21 - DealMap v2.0 重大重构
**完成内容**:
- M1-DealMap 生命周期事件PRD v2.0
- DealMap 字段精简：从 50+ 字段 → **25 字段**（移除 event_category/subtype/event_timing/trigger_source/action_id/deal_id/cflow_id/bank_account 等）
- Action 表允许多个/Deal（移除 deal_number UNIQUE 约束）
- Cashflow 表新增 `dealmap_number VARCHAR(50)` 字段（非 FK）
- 交易创建后**自动**创建 DealMap + Cashflow
- AC/AT 操作精简：只有 save / delete / approve / reject（无 submit / execute）
- 审批基于 Action，**不改变** DealMap/Cashflow 状态
- 修改时软删除旧 DealMap + 创建新 DealMap
- 删除时级联软删 Deal + AcDeal + DealMap + Cashflow
- CREATE 不生成 DealImage；UPDATE/DELETE 生成

**用户最新决策**:
1. DealMap 字段精简（仅核心 4 字段 + 必要辅助）
2. CREATE/UPDATE/DELETE 都生成 Action 并触发 DealMap 变化
3. 审批作用于 Action（修改 approval_status），不改 DealMap/Cashflow
4. AC/AT 无 submit/execute 功能
5. 交易创建自动创建 DealMap 和 Cashflow
6. 修改软删旧 + 建新
7. 删除软删交易 + 级联软删 DealMap

### 2026-06-21 - DealMap v1.2 用户理念强化
**完成内容**:
- 同步 event_timing 维度 + Action 必填触发 + Cashflow 反向关联
- 此版本已被 v2.0 取代

### 2026-06-21 - DealMap 生命周期事件设计 v1.1 修订
**完成内容**:
- M1-DealMap 生命周期事件PRD v1.1（评审后修订）
- 重新明确 DealMap 设计边界：仅记录业务事件，不记录状态变化与对象内部操作
- 精简事件分类：从 9 大类 → **8 大类 16 事件类型**
- 新增决策记录章节（10 项评审决策全部落地）

**v1.1 关键设计决策**:
- ❌ 移除 TRADE（NewTrade/AmendTrade/CancelTrade）—— 状态变化，由 Action + DealImage 覆盖
- ❌ 移除 APPROVAL（Submit/Approve/Reject）—— 状态变化，已由 Action 覆盖
- ❌ 移除 SETTLEMENT（Settlement/Reconciliation）—— 现金流对象内部操作
- ❌ 移除 LIFECYCLE/Maturity —— 状态变化；到期触发的本金/付息事件仍记录
- ✅ 保留 CASHFLOW / TRANSFER / INTEREST / LIFECYCLE（Unwind/Rollover/Exercise）/ VALUATION
- ✅ 编号前缀 **DMP**（DealMap 业务术语）
- ✅ 冲销采用**新增记录模式**（保留完整审计痕迹）
- ✅ 系统事件**不关联 Action**（operator='SYSTEM'，action_id 留空）

**验证要点**:
- 一笔普通 AC Deal 的 DealMap 应仅有 1 条（执行产生的现金流），不含审批/修改记录

### 2026-06-20 - DealMap 生命周期事件设计 v1.0
**完成内容**:
- M1-DealMap 生命周期事件PRD v1.0（已废止，被 v1.1 替代）
- 设计 tms_deal_map_t 表（单表结构化方案，对标 FIS Quantum Deal Events）
- 梳理 9 大事件分类、20+ 事件类型

### 2026-05-26 - 资金管理主体设计
**完成内容**:
- M1-资金管理主体PRD - 包含主体基础信息、监管信息、会计准则、税务配置、组织层级关系
- 设计了4张新表：管理主体表、主体监管信息表、主体会计准则配置表、主体税务配置表

**遇到的问题**:
- 现有BusinessUnit实体字段过于简单，不满足TMS对管理主体的要求 → 设计新的管理主体表替代/扩展

**待确认事项**:
- 主体类型是否需要更细致的分类
- 监管信息字段是否需要根据中国监管要求调整
- 多准则核算的实现方式

### 历史记录
| 日期 | 主题 | 完成内容 | 备注 |
|------|------|----------|------|
| 2026-04-06 | 组织架构与权限管理 | M1-组织架构与权限管理PRD | 包含业务单元、用户、角色、权限 |
| 2026-04-05 | 基础数据公共PRD | 基础数据PRD | 包含业务单元、交易员、币种、国家、节假日、银行、对手方 |
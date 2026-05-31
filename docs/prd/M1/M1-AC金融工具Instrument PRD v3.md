# Open-TMS M1-AC金融工具(AC Instrument) PRD

**版本**: v3.0
**角色**: 产品经理 (PM)
**日期**: 2026-06-01
**状态**: 更新 - 简化设计

---

## 一、模块概述

**模块名称**: instrument - AC金融工具管理
**功能定位**: AC交易的金融工具选择，仅作为交易录入时的产品选择参考
**用户角色**: 产品经理、资金经理、IT运维人员

**设计原则**: AC金融工具仅为简单的参考数据，用于交易录入时选择产品类型。不涉及外汇、计息、复杂金融产品。

---

## 二、功能清单

### 2.1 AC金融工具管理

| 功能 | 说明 | 优先级 |
|------|------|--------|
| 工具列表 | 分页展示AC金融工具 | P0 |
| 新增工具 | 创建AC金融工具 | P0 |
| 编辑工具 | 修改AC金融工具 | P0 |
| 删除工具 | 逻辑删除 | P0 |

### 2.2 AC金融工具字段(简化版)

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| instrumentId | BIGINT | 系统 | 主键 |
| instrumentCode | VARCHAR(50) | Y | 唯一编码，如 "AC_BP_001" |
| instrumentName | VARCHAR(100) | Y | 中文名称，如 "银行收付" |
| enName | VARCHAR(100) | N | 英文名称 |
| description | VARCHAR(500) | N | 工具描述/用途说明 |
| status | VARCHAR(20) | Y | Active/Inactive |
| createdBy | VARCHAR(50) | 系统 | - |
| createdAt | DATETIME | 系统 | - |
| updatedBy | VARCHAR(50) | 系统 | - |
| updatedAt | DATETIME | 系统 | - |

### 2.3 AC工具类型分类(简化)

| 类型 | 代码 | 说明 |
|------|------|------|
| 银行收付 | BANK_PAYMENT | 银行账户的资金收付 |
| 票据收付 | NOTE_PAYMENT | 票据的收付 |
| 其他收付 | OTHER_PAYMENT | 其他资金收付 |

---

## 三、与其他模块的关系

| 模块 | 关系 | 说明 |
|------|------|------|
| **AC Deal** | 交易时可选择工具 | ACDeal.instrumentId → ACInstrument |
| **Cashflow** | 无直接关系 | Cashflow由Deal生成，不直接关联Instrument |

---

## 四、API接口清单

### 4.1 AC金融工具管理

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/v1/dealing/ac-instruments/page` | GET | 分页查询AC金融工具 |
| `/api/v1/dealing/ac-instruments/{id}` | GET | 获取AC金融工具详情 |
| `/api/v1/dealing/ac-instruments` | POST | 创建AC金融工具 |
| `/api/v1/dealing/ac-instruments/update` | POST | 更新AC金融工具 |
| `/api/v1/dealing/ac-instruments/{id}` | DELETE | 删除AC金融工具 |
| `/api/v1/dealing/ac-instruments/types` | GET | 获取AC工具类型分类 |

---

## 五、验收标准

| 功能 | 验收条件 |
|------|----------|
| 工具管理 | CRUD正常，代码唯一性校验 |
| 状态控制 | Active可选择，Inactive不可选择 |
| 与AC交易集成 | 新建AC交易时可选择AC工具 |

---

## 六、页面原型(FIS风格)

**参考**: FIS Quantum TMS - 专业金融系统界面风格

### 6.1 AC金融工具列表页
- 顶部: 搜索条件(代码/名称/状态)
- 中部: 工具列表(分页展示)
- 支持按类型分类筛选

### 6.2 AC金融工具编辑页
- 基本信息区域(代码/名称/英文名/描述)
- 状态选择

---

*PM产出 - M1 v3.0 (2026-06-01)*
# M3-金融工具 设计摘要

## 最近更新
- **日期**: 2026-07-04
- **设计师**: PM (Claude Code)
- **本次完成**: M3-外汇交易 PRD v3.2(架构再调整 — 后端 calculate 接口 + 日期字段移到公共表 + DealMap Amount_or_rate + 多行独立 DealMap)

---

## 设计过程记录

### 2026-07-04 - M3-外汇交易 PRD v3.2(架构再调整)

**完成内容**(6 项用户反馈):

#### 1. 后端 calculate 接口
- 金额/汇率/term 的计算**全部由后端统一封装**
- 新增 `POST /api/v1/dealing/fx-deals/calculate` 接口
- 前端只录入,联动通过调用 calculate 自动填充
- 节流 300ms,避免频繁请求
- **单一计算源,前后端逻辑一致**

#### 2. 日期字段移到公共表
- `交易日/交割日/到期日/期限` 全部移到 `tms_deals_t` 公共表
- `tms_fx_deals_t` 不再存这些日期字段
- 跨 deal_type 共享日期语义(AC/AT/FX 统一)
- v3.1 时这些字段在 FX 表,v3.2 移到公共表

#### 3. DealMap 单字段化
- 原 3 字段 `buy_amount / sell_amount / rate` → **1 字段** `amount_or_rate`
- 配合 `dealmap_type` 区分(BUY_AMOUNT / SELL_AMOUNT / RATE / FIX)
- 简化 DealMap 模型,支持任意类型快照

#### 4. DealMap 多行独立存储
- 一笔 FX 交易产生 **3-4 行 DealMap**:
  - `FX_BUY_AMOUNT`(1 行)→ 触发 1 CF(买账)
  - `FX_SELL_AMOUNT`(1 行)→ 触发 1 CF(卖账)
  - `FX_RATE`(1 行)→ 0 CF(仅记录)
  - `FX_FIX`(NDF RATE_FIX 后,1 行)→ 触发 1 CF(差额)
- 每行有独立生命周期

#### 5. 1 DealMap → 1 Cashflow 约束
- 一笔 DealMap 最多生成 1 条 Cashflow(0 或 1)
- Cashflow 上存触发它的 dealmap_number(强引用)
- 例外:`FX_RATE` 快照不生成 CF

#### 6. 移除 chk_dm_fx_amounts
- 原 3 字段非空约束失效(现在只 1 字段)
- 新增 `chk_dm_fx_type_amount` 约束(单字段非空)
- instrument 灵活变化

**关键决策**:
- 决策 1:后端 calculate 是**唯一计算源**,前端无任何计算逻辑
- 决策 2:日期字段在公共表,FX 表只存 FX 特有(币种对/价值/DealMap 关联)
- 决策 3:DealMap 用 `dealmap_type` 枚举区分,字段统一为 `amount_or_rate`
- 决策 4:每行 DealMap 独立,与 Cashflow 严格 1:0 或 1:1
- 决策 5:4 种 FX DealMap 类型(BUY/SELL/RATE/FIX)
- 决策 6:DEAL 触发 3 行 DealMap,RATE_FIX 触发 1 行(仅 NDF)

**遇到的冲突与解决**:
- 冲突:v3.1 用前端 watch + computed 实现联动 → 解决:v3.2 全部交给后端 calculate
- 冲突:v3.1 在 FX 表存日期 → 解决:v3.2 移到公共表 `tms_deals_t`
- 冲突:v3.1 DealMap 3 字段 → 解决:v3.2 单字段 `amount_or_rate` + `dealmap_type`
- 冲突:如何保证 DealMap 与 CF 的 1:1 关系 → 解决:CF 上存 dealmap_number,1 DealMap 最多 1 CF

**v3.1 → v3.2 字段差异表**:
| 字段/规则 | v3.1 | v3.2 |
|----------|------|------|
| 联动计算 | 前端 watch + computed | **后端 calculate 接口** |
| 日期字段位置 | `tms_fx_deals_t` | **`tms_deals_t`(公共表)** |
| DealMap buy/sell/rate | 3 个独立字段 | **1 个 `amount_or_rate` + `dealmap_type`** |
| DealMap 行数/笔 | 1 | **3-4(BUY/SELL/RATE[/FIX])** |
| Cashflow 数量(SPOT/FWD) | 2 | **2(从 BUY/SELL DealMap 各 1)** |
| chk_dm_fx_amounts | 有 | **移除** |
| chk_dm_fx_type_amount | 无 | **新增** |
| 1 DealMap → 1 CF | 部分 | **明确:0 或 1** |

---

### 2026-07-04 - M3-外汇交易 PRD v3.1(细化调整)

**完成内容**(6 项用户反馈):

#### 1. 新增"交易日"(`tradeDate`)
- 代表交易达成的日子(签约日/谈定日)
- 与"交割日"(`value_date`)区分
- 通常 = today

#### 2. term 自动计算(`term_days` INT)
- 后端自动算:`term_days = value_date - trade_date`
- 前端只读,无下拉选择
- 替代 v3.0 的 `term VARCHAR(10)` 字符下拉

#### 3. 到期日 = 交割日(不可改)
- `maturityDate` 强制 = `valueDate`
- 前端不暴露,后端校验
- FWD/NDF 的"到期"就是"交割",无需区分

#### 4. 交割日 DB 字段名统一为 `value_date`
- v3.0 字段名 `settlement_date` → **v3.1 改为 `value_date`**
- 与 AC/AT 的 `tms_deals_t.value_date` 对齐
- 前端显示"交割日",DB 字段名 `value_date`

#### 5. 管理主体 FK 改为 id 强类型关联
- v3.0:`managementEntity VARCHAR(50)`(关联 `code`)
- **v3.1**:`managementEntityId BIGINT NOT NULL FK → tms_management_entity_t.id`
- 强类型约束,避免字符 code 误填

#### 6. 共享主键 + 移除 chk 约束
- v3.0:`tms_fx_deals_t` 有独立 `id BIGSERIAL PK` + `deal_id FK`
- **v3.1**:`tms_fx_deals_t.id = tms_deals_t.id`(共享主键,无独立 PK,无 deal_id FK)
- 移除 `chk_fx_settlement` 和 `chk_fx_ndf_fields` 约束(instrument 可能变化)

**关键决策**:
- 决策 1:增加"交易日"语义字段,业务流更清晰
- 决策 2:term 用天数(int)代替字符(1M/3M),自动计算,减少用户输入
- 决策 3:FWD/NDF 的"到期" = "交割",合并字段,简化数据模型
- 决策 4:管理主体走 BIGINT id FK 强类型,跨模块统一
- 决策 5:FX 表共享 `tms_deals_t.id` 主键,无独立生命周期
- 决策 6:不再硬约束 SPOT/FWD 互斥和 NDF fixing_source 必填,instrument 变化时自动适配

**遇到的冲突与解决**:
- 冲突:v3.0 用 `settlement_date`,v3.1 改 `value_date` → 解决:与 AC/AT 对齐,统一 `value_date`
- 冲突:v3.0 用 `term VARCHAR(10)` 下拉 → 解决:v3.1 用 `term_days INT` 自动算
- 冲突:v3.0 用 `maturity_date` 独立列 → 解决:v3.1 强制等于 value_date,可省略

**v3.0 → v3.1 字段差异表**:
| 字段 | v3.0 | v3.1 |
|------|------|------|
| 主键 | `id BIGSERIAL PK` + `deal_id FK` | `id = tms_deals_t.id`(共享) |
| 管理主体 | `managementEntity VARCHAR(50) code` | `managementEntityId BIGINT id FK` |
| 交割日 | `settlement_date DATE` | `value_date DATE` |
| 交易日 | 无 | `trade_date DATE NOT NULL` |
| 期限 | `term VARCHAR(10)` | `term_days INT`(自动) |
| 到期日 | `maturity_date DATE`(可选) | 省略(= value_date) |
| chk_fx_settlement | 有 | **移除** |
| chk_fx_ndf_fields | 有 | **移除** |

---

### 2026-07-04 - M3-外汇交易 PRD v3.0(用户反馈简化版)

**完成内容**(6 项用户反馈):

#### 1. 录入联动
- **联动 1**(金额↔汇率):卖出金额 / 买入金额 / 成交汇率,任两者输入 → 第三者自动计算
  - 公式:`buyAmount = sellAmount × exchangeRate`
- **联动 2**(汇率↔市场↔点差):成交汇率 / 市场汇率 / 点差,任两者输入 → 第三者自动计算
  - 公式:`exchangeRate = marketRate + spreadBp / 10000`

#### 2. 字段重命名
- FX `value_date` → 统一叫"交割日"(settlement_date)
- v2.0 已有"业务主体"标签 → 全部统一为"管理主体"(响应 2026-07-04 全局重命名)

#### 3. DealMap 极简化(★★★ 核心变化)
- v2.0:FDealMap 9+ 字段(含 eventType、eventStatus、accountRole)
- **v3.0**:只保留 **3 个字段**:`buy_amount / sell_amount / rate`
- 9 种 Fx* 事件类型全部取消
- 状态机信息由 Action 类型编码,生命周期轨迹由 DealMap 行时序记录

#### 4. Action 简化(只 4 种)
- v2.0(FX):CREATE / UPDATE / DELETE / SUBMIT / APPROVE / REJECT / EXECUTE / RATE_FIX
- **v3.0(FX)**:**DEAL / UPDATE / DELETE / RATE_FIX**
- 删除 SUBMIT / APPROVE / REJECT / EXECUTE(FX 无审批流)
- RATE_FIX 显式化(NDF fixing 触发)

#### 5. 基础数据约束
- **币种对约束**:sell/buy 币种必须从 `tms_currency_pair_t` 选取,不再支持任意币种对
- **产品类型隐式**:录入界面**无** productType 字段,由 `instrument` 自动决定(产品类型嵌入 instrument)

#### 6. 现金流生成时机
- **SPOT/FWD**:DEAL Action 触发 → 同步生成 1 条 DealMap + 2 条 Cashflow
- **NDF**:DEAL Action 触发 → **不生成**(等 RATE_FIX)
- **NDF RATE_FIX** → 生成 1 条 DealMap(fixing 快照)+ 1 条差额 Cashflow

#### 7. 暂不做项(明确控制范围)
- 锁汇额度 → P2+ 不做
- 询价 → P2+ 不做
- MTM 实时计算 → P2+ 不做
- 完整 FX(SWAP / OPTION / 货币掉期)→ P2+ 不做
- FX 审批流 → 不做(FX 业务本身无审批需求)

**关键决策**:
- 决策 1:FDealMap 极简化到 3 字段(用户明确要求),状态机信息由 Action 类型承载
- 决策 2:FX 无审批流,Action 简化为 DEAL/UPDATE/DELETE/RATE_FIX
- 决策 3:币种对 picker 取代两个独立币种 picker(从 currency_pair 配置选)
- 决策 4:产品类型隐式(由 instrument 决定)
- 决策 5:NDF 两阶段流程(DEAL 不生成 CF,RATE_FIX 后生成)
- 决策 6:严格控制范围,做最小可用产品,MTM/锁汇/询价全 P2+

**遇到的冲突与解决**:
- 冲突:v2.0 设计了完整 FX 生态(MTM/锁汇/询价/SWAP/OPTION)→ 解决:v3.0 全部 P2+,本版本只做核心
- 冲突:v2.0 有复杂状态机(9 态) → 解决:v3.0 简化为 3 态(New/Active/Deleted)
- 冲突:v2.0 DealMap 9+ 字段 + 9 种 Fx* 事件 → 解决:v3.0 极简为 3 字段

**依赖模块**:
- basedata:`management-entity` / `counterparty` / `trader` / `instrument` / `currency-pair`
- dealing:共享 `tms_deals_t` 公共主表 / `tms_actions_t` / `tms_deal_map_t` / `tms_cashflow_t`

**待确认事项**:
- Q1: FWD 期限 > 1Y 是否需要 2 级审批? → **不需要**(v3.0 无审批流)
- Q2: NDF fixing 数据源 → 暂定手工(后续接 Bloomberg API)
- Q4: 联动字段小数位显示 → 暂定 4 位显示,8 位存储
- Q5: 联动字段输入框可手动改? → **是**(改后其它两字段同步)
- Q7: NDF RATE_FIX fixing 汇率录入方式 → **手工**(v3.0)

---

### 2026-07-04 - M3-外汇交易 PRD v2.0(字段补全 + 命名对齐 + 架构升级)

**完成内容**:
- 新增 4 个通用字段(管理主体/对手方/交易员/金融工具)
- 命名统一为"管理主体"
- FX 并入 dealing 模块
- API 路径标准化
- 9 态状态机 / 9 种 DealMap 事件类型
- 锁汇额度管理设计

**v2.0 → v3.0 重大简化**(由用户反馈触发):
- 删除 9 态状态机 → 3 态
- 删除 9 种 Fx* 事件 → 极简 3 字段 DealMap
- 删除 7 种 Action → 4 种
- 删除 MTM / 锁汇 / 询价 / 完整 FX 套件(全部 P2+)
- 新增币种对 picker 约束

---

### 2026-04-11 - M3-外汇交易 PRD v1.0(已废弃)
- 独立 `fx` 模块,无通用字段
- 仅 3-4 态状态机

---

## 历史记录

| 日期 | 主题 | 完成内容 | 备注 |
|------|------|----------|------|
| 2026-07-04 | M3-外汇交易 PRD v3.2 | 6 项架构再调整(后端 calculate/日期到公共表/DealMap 单字段+多行/1→1 CF) | **当前版本** |
| 2026-07-04 | M3-外汇交易 PRD v3.1 | 6 项细化调整(交易日/term 自动算/到期=交割/value_date/管理主体 id/共享主键) | 被 v3.2 调整 |
| 2026-07-04 | M3-外汇交易 PRD v3.0 | 6 项用户反馈简化(联动/DealMap/Action/币种对/现金流/不做项) | 被 v3.1 细化 |
| 2026-07-04 | M3-外汇交易 PRD v2.0 | 字段补全 + 命名对齐 + 架构升级 | 中间版本,被 v3.0 简化 |
| 2026-04-11 | M3-外汇交易 PRD v1.0 | 首次发布,独立 fx 模块 | 已废弃 |
| 2026-04-11 | M3-利率掉期 PRD v1.0 | 利率掉期设计 | - |
| 2026-04-11 | M3-金融工具估值 PRD v1.0 | 估值/MTM 通用设计 | - |

---

## v3.2 范围总结

**做**:
- 通用字段:管理主体(id FK)/对手方/交易员/金融工具/币种对
- 价值字段(后端 calculate):卖出金额/买入金额/成交汇率/市场汇率/点差
- 日期(在 `tms_deals_t` 公共表):交易日 + 交割日(value_date) + 自动 termDays + 到期日 = 交割日
- **后端 calculate 接口**:`POST /api/v1/dealing/fx-deals/calculate`(统一计算)
- Action:DEAL/UPDATE/DELETE/RATE_FIX
- **DealMap 多行单字段**:1 笔 deal 产生 3-4 行 DealMap,`amount_or_rate` 单字段
- **4 种 DealMap 类型**:FX_BUY_AMOUNT / FX_SELL_AMOUNT / FX_RATE / FX_FIX
- **1 DealMap → 1 Cashflow(最多)**:CF 上存 dealmap_number
- 状态机:New/Active/Deleted
- SPOT/FWD/NDF 完整录入流程
- 币种对 picker 约束
- NDF 两阶段(DEAL → RATE_FIX)
- 基于 BaseDataPicker 的录入组件
- 共享主键(`tms_fx_deals_t.id = tms_deals_t.id`)
- 移除 chk 约束(instrument 灵活变化)

**不做**(v3.2 范围外,同 v3.0):
- ❌ 锁汇额度(签约即占、到期释放、多级预警)
- ❌ 询价(询价接口、市场数据对接)
- ❌ MTM 实时计算(每日盯市)
- ❌ 完整 FX 套件(外汇掉期、外汇期权、货币掉期)
- ❌ 衍生品会计(IFRS 9 双借双贷)— M1.3 阶段
- ❌ FX 审批流(多级审批)— FX 业务无审批需求
- ❌ MTM 历史 Tab
- ❌ 会计分录 Tab(占位)— M1.3 阶段

---

*PM Skill 产出 - 2026-07-04 v3.2*

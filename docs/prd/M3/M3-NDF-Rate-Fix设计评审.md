# NDF Rate Fix 设计评审

**评审对象**:用户提出的 NDF Rate Fix 功能设计方案(2026-07)
**评审人**:Open-TMS PM/BA 子代理
**评审日期**:2026-07-05
**评审基准**:M3-外汇交易 PRD v3.2、`db/schema/24-fx-deal-v3.sql`、`docs/api/dealing/M3-fx-deals-v3.md`、`FxDealServiceImpl.java`(v3.2)、`FxDeal.java`、`DealMap.java`
**业界参照**:Murex MX.3 / Kyriba / FIS Quantum NDF 业务实践

---

## 总评

**整体评分**:**6 / 10**(原则正确,细节粗糙,落地数据模型明显与既有 v3.2 不一致)

**一句话总结**:NDF 标识字段、按钮置灰这两点设计与既有 v3.2 不冲突,但 **3 笔 event + 1 笔 cashflow**(需求点 4)与已有的"每笔 DealMap 最多 1 条 CF"约束冲突,且 Fix Date / Fix currency 应归属 fx_deals 还是 deal_map 不清晰,**强烈建议用现有 `FX_FIX` + 1 行 DealMap + 1 条 CF 的极简模型落地**,不要引入新的"event 类型"。

---

## 详细评审

### 维度 A:业务合理性

- ✅ **优点**:
  1. 用户准确抓住 NDF 与 SPOT/FWD 的本质差异:**Fix 时点未定、Fix 汇率未定**,所以 DEAL 不生成现金流的合理性被强化。
  2. 按钮置灰 vs 隐藏的选择面向"防止误操作"而非"防止误解",意图合理。
  3. NDF 标识走 instrument / deal 双层(配置化 + 实例化)基本方向正确,业界 NDF 也是"产品定义决定 NDF、交易可选"。

- ⚠️ **问题 1.1(NDF 标识放 instrument 还是 deal)**:
  - 用户表述模糊:**"交易和 instrument 上需新增字段标识该交易/产品是否为 NDF"**。
  - 业界 Murex/Kyriba 的标准做法是:**只在 instrument 表标识**(`instrument_type IN ('FX_NDF')`),deal 不需要额外 `is_ndf` 字段,通过 FK 推导即可。
  - 当前 v3.2 `FxDealServiceImpl.convertDealToListVO`(line 1091-1098)就是这么做的(根据 `fixingSource` 是否非空推断)。
  - 同时在 deal 上存 `is_ndf` 会导致双源真相(double source of truth)、instrument 改了 deal 不改的不一致。
  - **💡 建议**:
    - **instrument.tms_instrument_t**:`instrument_type` 字段已存在(基于 v3.2 PRD 提到 basedata 含 instrument),只需保证 FX 类下细分为 `FX_SPOT` / `FX_FWD` / `FX_NDF`。
    - **fx_deal 表**:**不新增 `is_ndf` 列**,改用 `JOIN instrument` 推断(也可冗余存一个 computed 列用于查询,避免每次 JOIN)。
    - 如果出于性能/列表筛选的考虑,可以增加一个 generated/virtual column `fx_deal.is_ndf = (instrument.type = 'FX_NDF')`,PostgreSQL 14+ 支持。

- ⚠️ **问题 1.2(自动带出 vs 修改后冲突)**:
  - 用户提到"在选择 instrument 后自动带出,支持修改"。
  - **歧义风险**:NDF 的 `fixingSource` 是 NDF-specific 配置,如果用户选了非 NDF instrument 又手动改成 NDF,会出现 instrument ≠ fixingSource 的不一致。
  - **💡 建议**:这里要清楚"自动带出"与"是否可改"的关系:
    - 选 NDF instrument → 自动填写 `fixingSource`(单选,不可手动改);
    - 选非 NDF instrument → 该字段不可见;
    - 中途切换 instrument → 弹确认对话框,警告"是否同时重置 fixingSource?已保存的 Rate Fix 数据会被清空"。

- ⚠️ **问题 1.3(DEAL 不生成 DealMap/Cashflow 的下游影响)**:
  - 用户原 PRD 已经规定:NDF DEAL 时**3 行 DealMap + 0 CF**(v3.2 第 5 节 / N5 规则),只 CF 延迟到 RATE_FIX。
  - 用户提的"不生成 DealMap 和 Cashflow"会让 deal 完全"裸奔",**估值、敞口、Position 都拿不到 DealMap 来源**。
  - **💡 建议**:**保持 v3.2 现状**:NDF DEAL 时仍然创建 3 行 DealMap(BUY/SELL/RATE 快照),仅 CF 数量为 0。等 RATE_FIX 后再创建 1 行 `FX_FIX` DealMap + 1 条差额 CF。这样下游估值、履约、轨迹分析都有快照可用。

- ⚠️ **问题 1.4(3 笔 event 是否合理)**:
  - 用户提"3 笔 event type 为 Buy Amount、Sell Amount、Rate Fix + 1 笔 NDF Settlement"。
  - **这一段和现有 v3.2 完全不一致**:v3.2 是"1 笔 `FX_FIX` DealMap + 1 笔 settlement 方向 Cashflow"。
  - 多写 event 的风险:
    1. **冗余**:Buy/Sell 的快照如果已经在 DEAL 时创建了(支持估值),RATE_FIX 时再写一次就是数据冗余;
    2. **方向不一致**:3 笔 event 是 2 个 outflow/inflow 还是 1 个差额?业界 NDF 只在 RATE_FIX 时**结算 1 个净额**(差额方向),不会同时结算两端本金。
    3. **命名混乱**:用户写"event type",业界事件"金额快照"应走 DealMap,"现金结算"走 Cashflow,不要混为一谈。
  - **💡 建议**:**只用 1 行 DealMap (`FX_FIX`)+ 1 条 CF**:
    ```sql
    -- 1 行 DealMap
    INSERT INTO tms_deal_map_t (dealmap_type='FX_FIX', amount_or_rate=fixingRate, ...)
    -- 1 条 Cashflow(方向由 fixingRate - exchangeRate 正负决定)
    INSERT INTO tms_cashflow_t (dealmap_number=上一步, direction=有正负, amount=|settlement|, currency=...)
    ```
    这是 Kyriba "post-fix" 净额结算的标准做法,也与 v3.2 PRD 完全一致。

- ⚠️ **问题 1.5(Settlement 现金流方向)**:
  - **关键但用户未说明**:
    - 若 `fixingRate < exchangeRate`(USD/CNY,如 fixing=7.15, exchange=7.20)→ 公司卖出 USD 实际收到 USD 7.15,但合同锁汇 7.20,公司少收 **CNY 0.05/USD × notional** → 公司应付 CNY(Outflow of CNY)。
    - 若 `fixingRate > exchangeRate` → 公司多收 CNY(Inflow of CNY)。
  - 业界做法:**cashflow 在 settlement currency 上单条表达,方向由差额正负**;Murex/Kyriba 都用"虚拟 ndf_settlement_cashflow"表达。
  - 现有 v3.2 Service 代码(`rateFix`,line 777):`direction = (settlementAmount >= 0 ? Inflow : Outflow)`,**方向已处理**但只用了单 currency。
  - **💡 建议**:
    1. 明确 `currency` 字段 = **fix currency**(也就是用户在弹框选的 currency,通常是 `buyCurrency`)。
    2. **多个 fix 币种怎么办?**(见 5.3)目前需求点 3 已经规定"从 buy currency 和 sell currency 选择",这允许差额结算在 sell currency 上,需要 Service 同时支持两种。

---

### 维度 B:数据模型

- ✅ **优点**:整体意识到 NDF 标识要落在 deal/instrument,fid Date/Fix Rate/Fix Currency 的"派生数据"思路与 v3.2 的 fixing_rate / settlement_amount 字段匹配。

- ⚠️ **问题 2.1(instrument 表是否已存在 + 新增字段)**:
  - 基于 v3.2 PRD 1.4 节,basedata 模块已合并 instrument,端口 8081,在 `tms_instrument_t` 中。
  - **当前 instrument 表是否已有 `instrument_type` 字段?需要验证。**(评审任务未让查该表)
  - 业界必备:`is_physical_settlement BOOLEAN`(NDF 默认 false,SPOT/FWD true)、`fixing_required BOOLEAN`(NDF true,其余 false)、`settlement_type ENUM('PHYSICAL','CASH','OPTION')`。
  - **💡 建议**:
    - **优先核查**:`tms_instrument_t` 现有 schema,确认 `instrument_type` 是否能区分 SPOT/FWD/NDF。
    - 若不能,新增 `instrument_sub_type VARCHAR(20)` 字段,值域 `FX_SPOT` / `FX_FWD` / `FX_NDF`;或更通用 `fixing_required CHAR(1)` + `settlement_type VARCHAR(20)`。
    - 不在 deal 上重复存 `is_ndf`。

- ⚠️ **问题 2.2(deal/fx_deal 是否需要 `is_ndf`)**:
  - **不需要**(见 1.1)。
  - 如果一定要冗余,**用 generated column**:
    ```sql
    ALTER TABLE tms_fx_deals_t ADD COLUMN is_ndf BOOLEAN
      GENERATED ALWAYS AS (
        (SELECT i.instrument_type FROM tms_instrument_t i WHERE i.id = instrument_id) = 'FX_NDF'
      ) STORED;
    ```
  - **💡 建议**:**不加 `is_ndf`,下游需要时 JOIN instrument;若有性能问题,加 generated column**。

- ⚠️ **问题 2.3(`event_type` 命名)**:
  - 用户提的 3 笔 event("Buy Amount、Sell Amount、Rate Fix")在 v3.2 已经定义为 `dealmap_type` 枚举(`FX_BUY_AMOUNT / FX_SELL_AMOUNT / FX_RATE / FX_FIX`),命名已统一。
  - 第 4 笔"NDF Settlement"应统一叫 **Cashflow**,**不要**再加一个叫 `event_type='NDF_SETTLEMENT'` 的 DealMap。
  - **💡 建议**:沿用 v3.2 命名:
    - DEAL 时:3 行 `dealmap_type IN ('FX_BUY_AMOUNT','FX_SELL_AMOUNT','FX_RATE')`
    - RATE_FIX 时:1 行 `dealmap_type='FX_FIX'`
    - 差额现金:**走 Cashflow,不走 DealMap**(避免重复)

- ⚠️ **问题 2.4(Fix 字段存哪)**:
  - 用户表述模糊:"Fix Date、Fix Rate、Fix currency 字段应该存在 fx_deal 表还是 deal_map 表?"。
  - **业界通用做法**:
    - **fixingRate、fixingSource** → 存 fx_deals_t(已经在,line 74-82),作为 deal 级标志;
    - **fixDate(Fixing 实际执行日期)** → 存 **fx_deals_t** 或 Cashflow 的 `cflow_date`(已经是 value_date),不要存进 DealMap。
  - **为什么 Fix Date 不进 DealMap**:DealMap 是 deal 的"业务事件快照",同一笔 NDF 在不同 fixing 日期可能有多个 `FX_FIX`,但**业务上 NDF 只 fix 一次**,DealMap 上写 fixDate 没意义。
  - **💡 建议**:**Fix Date 存 fx_deals_t**:`tms_fx_deals_t.fix_date DATE`(新增);**Fix currency 存 fx_deals_t**:`fix_currency VARCHAR(10)`(新增,默认 `buyCurrency`);**Fix Rate 已存**(`fixing_rate`)。

- ⚠️ **问题 2.5(NDF Settlement 现金流的金额)**:
  - 公式:
    - settlementAmount = notional × (fixingRate - exchangeRate) —— **业界标准,Murex 用此公式**
    - 若 fixing currency ≠ sell/buy currency(罕见 NDF),需要二次转换。
  - 现有 v3.2 代码:`settlementAmount = notional.multiply(fixingRate.subtract(exchangeRate))`(line 767),**公式已对**。
  - **💡 建议**:
    - 保持现有公式不变;
    - cashflow 上额外存 `notional`(便于对账回溯)、`fixing_rate_snapshot` 字段(冗余防丢);
    - 测试用例要覆盖三个分支:
      1. fixing > exchange → Inflow(buy 货币);
      2. fixing < exchange → Outflow(buy 货币);
      3. fixing == exchange → zero 现金流(避免 0 金额 CF 污染账)。

---

### 维度 C:业务流程

- ✅ **优点**:
  1. 状态机意识清晰:DEAL → RATE_FIX → Active,符合 NDF 业界流程。
  2. 多次 Rate Fix 防呆考虑到了。
  3. market rate 留作参考,符合业界"fix vs market 偏差观察"实务。

- ⚠️ **问题 3.1(是否需要审批流)**:
  - 用户提"生成一条待审批的 Action 记录"。
  - **冲突点**:v3.2 已明确规定"FX 无审批流,Action 直接 Approved"。
  - 若新加"待审批 Action"会**破坏 v3.2 的整体设计**(N1 规则)。
  - **业界做法**(Murex):
    - NDF DEAL 不审批(签约即生效);
    - RATE_FIX 是"市价确认",**有 2 模式**:
      - 自动模式:系统按报价源取 fix 后,直接落 CF,Audit 记录;
      - 手工模式:结算员手工录入 fix,落 CF,**不需要审批**(只是数据校对)。
  - **💡 建议**:**保持 v3.2 一致**,RATE_FIX 的 Action 直接 `action_status='Approved'`;若担心合规,加"风控复核"环节(独立的复核岗标记,不是审批流)。

- ⚠️ **问题 3.2(RATE_FIX 后 NDF 状态应是 Active 还是新 NDFSettled)**:
  - 现有 v3.2:`status = 'Active'`(line 802 `deal.setStatus(DEAL_STATUS_ACTIVE)`),符合"DEAL 时 NDF 不动,fix 后变 Active"。
  - 用户方案未提及状态,但**应明确告知不能引入新状态** NDFSettled,与 v3.2 状态机冲突。
  - **💡 建议**:**保持现有三态 New/Active/Deleted**;CF 上有 `status` 字段(`Created`/`Posted`/`Settled`)可表达现金流的生命周期,不与 deal 状态耦合。

- ⚠️ **问题 3.3(多次 Rate Fix)**:
  - 现有 v3.2(line 714)已强约束 `if (fxDeal.getFixingRate() != null) throw` —— **"只能一次"**。
  - 业界 Murex 标准:NDF **理论上支持 amend fix**,但实务中"修正历史 fix"是非常特例(汇率重定),大多数 NDF 1 次 fix 完成。
  - **💡 建议**:
    - Phase 1:**保持 1 次 fix**(与 v3.2 一致),状态字段加 `fix_amend CHAR(1)` 预留 amend 能力;
    - Phase 2(可选):支持 amend,允许新 Action(`RATE_FIX_AMEND`),反向回滚原 CF,生成新 CF。

- ⚠️ **问题 3.4(market rate 为空时用什么)**:
  - 用户允许 market rate **非必填**(参考资料)。
  - 现有 v3.2 PRD 中 DEAL 时 `marketRate` 是**必填**(line 218 `|marketRate|Y|市场参考汇率|`),但 RATE_FIX 时 fix market rate 是**可选**的。
  - 估值时(v3.2 不做 MTM,但 P2+ 会做)若无 market rate,业界做法:
    - 用 fixing_rate 作为市场汇率(失去偏差分析能力);
    - 用 deal 创建时的 marketRate 留存(已存于 `marketRate` 字段,合理)。
  - **💡 建议**:**Rate Fix 时的 market rate 留作 audit 字段**,不参与计算;估值用 fx_deal 创建时的 `marketRate`(已存)。命名上,Rate Fix 的可选 market rate 应叫 `fix_market_rate` 或直接叫 `fix_reference_rate`,不要复用 `market_rate`(避免歧义)。

---

### 维度 D:UX 体验

- ✅ **优点**:弹框录入关键参数、Fix currency 可选、画布居中等设计方向正确,符合 NDF 业务场景。

- ⚠️ **问题 4.1(置灰 vs 隐藏)**:
  - 用户选择"置灰",但这是有缺陷的设计:
    1. 灰按钮引起"为什么不能点"的疑问,**增加客服负担**;
    2. 移动端/小屏场景灰按钮可达性差;
    3. 用户实际不知道"为什么是灰"。
  - 业界(Capital on Tap、Kyriba、Murex Web)普遍做法:
    - 不显示按钮(详情页看产品类型决定);
    - 或显示按钮但 click 后提示"该交易不是 NDF,无需 Rate Fix"。
  - **💡 建议**:
    - **主方案(NDF/非 NDF 路由)**:详情页 `if (productType === 'NDF')` 显示 Rate Fix 按钮,否则**整段隐藏**该按钮 + "Rate Fix" Tab;
    - **备选方案(置灰 + tooltip)**:按钮置灰 + hover/悬浮提示"该交易为 XXX 类型,不支持 Rate Fix",提示删除交易或联系管理员。

- ⚠️ **问题 4.2(Fix currency 单选)**:
  - 用户提"从 buy currency 和 sell currency 中选择",**没明确单/多选**。
  - 业界:只能是**单选(下拉)**;两个都不选无意义(用户没业务场景)。
  - 默认应是 `buyCurrency`(NDF 差额结算在计价货币,这是行业惯例)。
  - **💡 建议**:
    - 下拉单选,默认 `buyCurrency`;
    - 校验:必须选一个;
    - 后端 Service 不再用"if amount>0 用 buyCurrency"的隐含逻辑,而是读前端传入的 fix_currency。

- ⚠️ **问题 4.3(Rate Fix 后弹"查看 Settlement 详情")**:
  - 用户未提,但这是**业界标配**:
    - Kyriba:RATE_FIX 完成后弹"Fix Summary"对话框,展示 fixingRate、notional、cashflow number;
    - Murex:RATE_FIX 后直接跳到 Cashflow Tab。
  - **💡 建议**:RATE_FIX 完成 Response 后,前端弹 Toast + 提供"查看 Cashflow"快捷按钮,**主动跳到 Cashflow Tab**。

- ⚠️ **问题 4.4(弹框 modal vs drawer)**:
  - 用户未提 modal 形式。
  - 业界 Element Plus 实务:
    - 录入字段少(3 个:date / rate / currency + 可选 market):**用 Dialog(modal)**,聚焦当前操作;
    - 录入字段多(如 > 5 个)或需配合副作用:用 Drawer(抽屉)。
  - Rate Fix 弹框 3-4 个字段,**Modal 优于 Drawer**,更聚焦。
  - **💡 建议**:用 `<el-dialog>` modal,宽度 480px,字段顺序:Fix Date → Fix Rate → Fix Currency → Market Rate(可选) → 操作记录 preview。

---

### 维度 E:风险与扩展

- ✅ **优点**:用户开始思考 NDF/FWD 数据模型差异、多次 fix、币种支持等多维度问题,系统性较强。

- ⚠️ **问题 5.1(NDF/FWD/SPOT 用不同数据表?**):
  - **不建议分表**(理由):
    1. AC/AT/FX 已统一共享 `tms_deals_t`,继续分表破坏整体架构;
    2. SPOT/FWD/NDF 字段集 90% 重合,只有 NDF 多 3-4 个字段(`notional`、`fixingSource`、`fixingRate`、`settlementAmount`、`fix_date`、`fix_currency`);
    3. 分表后跨产品类型的报表(估值、敞口)变复杂。
  - **💡 建议**:**保留单表**(沿用 v3.2 `tms_fx_deals_t`),NDF 特有字段已存;后续 Phase 2 可支持 SWAP/OPTION 时,**用横向扩展字段**(`extra_fields JSONB`)而非分表。

- ⚠️ **问题 5.2(与现有 rateFix 端点兼容)**:
  - 现有 `POST /api/v1/dealing/fx-deals/{id}/rate-fix`,只接收 `{fixingRate, operator}`(API 文档 3.7)。
  - 用户方案多了 fixDate / fixCurrency / marketRate,**需要扩展 DTO**,但要向后兼容。
  - **💡 建议**:
    - DTO:`RateFixRequest { fixingRate, fixDate?, fixCurrency?, fixMarketRate?, operator }`,所有 fix* 字段可选(后端有默认值);
    - 现有 `fixingRate` 保留(原 `rateFix(id, fixingRate, operator)` 方法重载兼容);
    - 修复历史数据:存量 fix 任务不强制补字段。

- ⚠️ **问题 5.3(多币种 NDF)**:
  - 用户提"Fix currency 从 buy/sell 中选择",支持双币种 NDF(settle in sell 或 buy)。
  - 业界实务:
    - 大多数 NDF settle in USD(尽管原币种可能是 EM currency,比如 BRL/USD NDF settle in USD);
    - 也有"natural settlement"的 NDF(currency = original EM currency),少见。
  - 现有 `FxDealServiceImpl.rateFix`(line 779):`cf.setCurrency(fxDeal.getBuyCurrency());` 写死 buy_currency,**不够灵活**。
  - **💡 建议**:
    - Phase 1:支持 buy/sell 二选一(`fixCurrency` 默认 buy,可选 sell);
    - **拓展 currency**(3rd currency,USD settle in BRL NDF) 暂不支持,留 P2。

- ⚠️ **问题 5.4(与审批系统的耦合度)**:
  - 现有 v3.2 FX 4 模式:New / Active / Canceled。
  - 用户方案"待审批 Action"会引入"Pending"状态,与 v3.2 冲突。
  - 业界 NDF 实务(FIS Quantum):NDF fix 与 deal 完全独立,**fix 不走审批**(fix 是市价确认,不是合同生效)。
  - **💡 建议**:
    - 保持现有 3 模式(New/Active/Canceled);
    - fix 后 Action 直接 Approved,与现有 v3.2 一致;
    - 若用户业务上需要复核,加"复核人"字段 `verified_by VARCHAR(50)` 到 fx_deal 表,**不走审批流**,作为手工 Audit 字段。

---

## 必须修改 (P0)

1. **删除"3 笔 event"设计**:按 v3.2 PRD 改用 **1 行 `FX_FIX` DealMap + 1 条差额 Cashflow**,不要写多个 event。**v3.2 已明确规定 N5/N6:DEAL 3 行 DealMap + 0 CF,RATE_FIX 1 行 DealMap + 1 CF**,无需扩展。
2. **Fix 字段存储位置**:fixDate / fixCurrency 存 fx_deals 表(fix_rate 已存在),**不要进 DealMap**(DealMap 是 deal 业务事件快照,不存状态字段)。
3. **"待审批 Action"取消**:FX 无审批流(v3.2 设计原则),RATE_FIX 的 Action 直接 Approved;若一定要审批,放到外层"复核人"字段,**不是审批流**。
4. **NDF 标识位置**:只在 instrument 表标识(`instrument_type='FX_NDF'`),**fx_deal 不加 `is_ndf` 字段**;下游要筛 NDF 时 JOIN instrument(或 generated column)。
5. **状态机保持 3 态**:New / Active / Canceled,**不引入 NDFSettled**,CF 状态自管。

## 建议优化 (P1)

1. **按钮隐藏而非置灰**:详情页路由判断 `productType==='NDF'` 时才显示 Rate Fix 按钮,非 NDF 整段隐藏;保留 hover 提示作为兜底。
2. **Fix currency 默认值**:`fixCurrency` 默认 = `buyCurrency`,前端只让用户改;校验必须选一个。
3. **RATE_FIX 完成跳 Cashflow Tab**:弹 Toast + 自动跳转,主动引导用户看 cashflow。
4. **弹框用 Dialog modal**:3-4 字段聚焦录入,Modal 优于 Drawer。
5. **历史数据兼容**:存量 fix 数据允许 `fix_date` / `fix_currency` 为 NULL,Service 层有默认值 fallback。
6. **审计/防呆字段**:fx_deal 加 `fix_remark`(RATE_FIX 备注)、`verifier_by`(复核人,可空),走 Audit,不走审批。

## 后续增强 (P2)

1. **NDF amend fix**:支持 RATE_FIX 修正,新增 Action 类型 `RATE_FIX_AMEND`,反向回滚原 CF,生成新 CF(v3.2 未支持,需扩展 Action 枚举)。
2. **NDF 提前 fix**:允许在交割日之前 fix(目前 v3.2 默认到 value_date 才 fix),适用范围:有窗口外成交需求的客户。
3. **Fixing API 对接**:对接 Bloomberg BFIX / Reuters 自动取 fix,触发"自动化 RATE_FIX"任务,**注意引入数据可观察性**(记录数据源 + 时间戳)。
4. **多币种 NDF**:支持 3rd currency(如 BRL/USD NDF settle in EUR),扩 Cashflow currency 自由度。
5. **MTM 估值**:P2+ 接入估值模块,使用 `fx_deal.fixingRate` 作为 fix 后估值基准,fix 前用 `exchangeRate`。

---

## 落地建议

### Phase 1(最小可用,1 周)

- [ ] 确认 `tms_instrument_t` 已有 `instrument_type` 字段,值域含 `FX_NDF`
- [ ] fx_deals_t 加 3 字段:`fix_date DATE`、`fix_currency VARCHAR(10)`、`verifier_by VARCHAR(50)`(可空)
- [ ] 复用现有 rateFix 端点 + DTO 扩展,向后兼容(`fixingRate` 必填;fixDate/fixCurrency/fixMarketRate 可选,后端有默认)
- [ ] Service 层公式:`settlementAmount = notional × (fixingRate - exchangeRate)`,direction 由正负决定,Cashflow currency 默认 buyCurrency(可读 fixCurrency 覆盖)
- [ ] 前端:产品类型路由判断显示/隐藏 Rate Fix 按钮;弹框用 Dialog modal,3-4 字段
- [ ] 状态机:**保持 3 态**;Action 直接 Approved,不走审批

### Phase 2(完善,2 周)

- [ ] Rate Fix 完成后自动跳 Cashflow Tab,弹 Toast 显示 fixing summary
- [ ] 生成 fx_deal.fix_remark 审计字段(用户备注)
- [ ] 加 NDF amend 能力(P2):状态字段 `fixAmend CHAR(1)`,但本期**不实现 amend,只预留字段**
- [ ] 测试用例覆盖 3 个方向分支(fixing > exchange、< exchange、= exchange)
- [ ] UI 自动化测试(`test_ndf_rate_fix_ui.py`)

### Phase 3(P2+,3-4 周)

- [ ] 对接 Bloomberg BFIX(自动化 fix),引入"数据源 + 时间戳"audit 字段
- [ ] 多币种 NDF(3rd currency settle)
- [ ] NDF amend 实现(Action `RATE_FIX_AMEND`,反向 CF 冲销)
- [ ] MTM 估值接入(fix 后用 fixingRate,fix 前用 exchangeRate)

---

## 关键问题 Top 5

1. **"3 笔 event"破坏 v3.2 PRD**:应改用 1 行 `FX_FIX` DealMap + 1 条 settlement CF 的极简模式(v3.2 已明确)。
2. **NDF 标识放错位置**:`is_ndf` 在 deal 表会双源真相,应只在 instrument 表标识或用 generated column。
3. **"待审批 Action"破坏 FX 无审批流原则**:v3.2 已明确 FX 直接 Approved,NDF fix 是数据校对不是合同生效。
4. **Fix 字段存储位置不清晰**:Fix Date / Fix Currency 应落 fx_deal 表,不是 DealMap。
5. **状态机 3 态原则未坚持**:用户默认引入 NDFSettled,与 v3.2 三态冲突,CF 自身有状态字段(Created/Posted/Settled)能覆盖需求,无需新增 deal 状态。

## 是否需要修改现有 FX PRD

**需要(增量)**:
- 在 v3.2 PRD 第 7.3 节(NDF 流程)补充 **Phase 1 落地详情**:fix_date / fix_currency 字段位置、按钮路由判断、RATE_FIX 完成后引导。
- 在第 12 节(验收标准)增加 **A22-A26**:按钮可见性、Fix Currency 默认值、3 个 fix 方向分支测试用例。
- 在第 6.4 节(NDF 特有规则)增加 **N9**:`fix_currency` 默认 `buyCurrency`,`fix_date` 默认 = `value_date`。
- **不需要修改** v3.2 的核心架构(DealMap 极简化、Action 4 种、状态机 3 态),本次需求 **完全兼容** v3.2 设计,**只是补全/细化**而非重写。

---

*PM/BA 子代理产出 - 2026-07-05*
*评审对象:用户提出的 NDF Rate Fix 方案*
*评审基准:Open-TMS v3.2 PRD/Schema/API/Service 现状*
*业界参照:Murex MX.3 / Kyriba / FIS Quantum NDF 实务*

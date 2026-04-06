# TMS交易类型及字段参考文档

**来源**: Oracle Treasury / FIS Quantum 对标分析  
**日期**: 2026-04-06  
**用途**: 交易录入功能设计参考

---

## 一、交易类型分类

| 类别 | 交易类型 | 子类型 | 优先级 |
|------|----------|--------|--------|
| **货币市场** | 短期货币(Short Term Money) | 通知存款/隔夜/短期融资 | P0 |
| | 零售定期(Retail Term Money) | 按揭/定期存款 | P0 |
| | 批发定期(Wholesale Term Money) | 大额存单/私募债 | P1 |
| | 公司间资金(Intercompany Funding) | 集团内部借贷 | P1 |
| | 可转让证券(Negotiable Securities) | CD/票据 | P1 |
| **外汇** | 即期FX Spot | 买入/卖出 | P0 |
| | 远期FX Forward | NDF/择期 | P0 |
| | 外汇掉期FX Swap | 即期+远期组合 | P0 |
| | 外汇期权FX Option | 看涨/看跌 | P0 |
| **衍生品** | 利率掉期(IRS) | 固定/浮动 | P0 |
| | 远期利率协议(FRA) | 利率远期 | P0 |
| | 利率期权 | Cap/Floor/Swaption | P0 |
| | 债券期权 | 债券期权 | P1 |
| **债券** | 固定收益 | 国债/企业债/金融债 | P0 |
| **同业** | 同业存放 | 同业资金往来 | P0 |
| **贷款** | 短期贷款 | 流动资金贷款 | P0 |
| | 中长期贷款 | 项目贷款/固定资产贷款 | P0 |
| **资金调拨** | 账户转账(Account Transfer) | 内部账户间资金划转 | P0 |
| **现金流** | 实际现金流(Actual Cashflow) | 银行流水生成的现金流 | P0 |
| **利息** | 账户利息(Account Interest) | 银行账户利息收支 | P0 |

---

## 二、各交易类型核心字段

### 2.1 短期货币(Short Term Money)

| 字段区域 | 字段名 | 类型 | 必填 | 说明 |
|----------|--------|------|------|------|
| **公共区域** | Company | VARCHAR(50) | Y | 业务单元 |
| | Counterparty | VARCHAR(50) | Y | 交易对手 |
| | Currency | VARCHAR(10) | Y | 币种 |
| | Day Count Basis | VARCHAR(20) | Y | 计息天数基准 |
| | Portfolio | VARCHAR(50) | N | 组合 |
| | Client | VARCHAR(50) | N | 客户 |
| | Company Account | VARCHAR(50) | Y | 公司账户 |
| **交易明细** | Deal Subtype | VARCHAR(20) | Y | 交易子类型 |
| | Security Type | VARCHAR(20) | N | 担保类型(有/无担保) |
| | Product Type | VARCHAR(20) | N | 产品类型 |
| | Settlement Date | DATE | Y | 结算日 |
| | Maturity Date | DATE | Y | 到期日 |
| | Days | INT | N | 期限(天) |
| | Principal Amt | DECIMAL(18,2) | Y | 本金金额 |
| | Limit | VARCHAR(50) | N | 额度 |
| **利息明细** | Interest Rate | DECIMAL(10,4) | Y | 利率 |
| | Interest Amount | DECIMAL(18,2) | N | 利息金额(可覆盖) |
| | Prepaid Interest | CHAR(1) | N | 预付利息标识 |
| | Interest Due On | DATE | N | 利息到期日 |
| | Interest Rounding | VARCHAR(20) | N | 利息舍入规则 |
| | Interest Includes | VARCHAR(20) | N | 计息包含日 |

### 2.2 零售定期(Retail Term Money)

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| Company | VARCHAR(50) | Y | 业务单元 |
| Counterparty | VARCHAR(50) | Y | 交易对手 |
| Client | VARCHAR(50) | N | 客户 |
| Portfolio | VARCHAR(50) | N | 组合 |
| Deal Subtype | VARCHAR(20) | Y | 交易子类型 |
| Product Type | VARCHAR(20) | N | 产品类型 |
| Currency | VARCHAR(10) | Y | 币种 |
| Principal Amount | DECIMAL(18,2) | Y | 本金 |
| Day Count Basis | VARCHAR(20) | Y | 计息基准 |
| Start Date | DATE | Y | 起息日 |
| Maturity Date | DATE | Y | 到期日 |
| Payment Schedule | VARCHAR(20) | Y | 还款计划 |
| Initial Basis | VARCHAR(10) | Y | 利率类型(Fixed/Floating) |
| Interest Rate | DECIMAL(10,4) | Y | 利率 |
| Interest Rounding | VARCHAR(20) | N | 利息舍入规则 |
| Repayment Amount | DECIMAL(18,2) | Y | 还款金额 |

### 2.3 批发定期(Wholesale Term Money)

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| Company | VARCHAR(50) | Y | 业务单元 |
| Counterparty | VARCHAR(50) | Y | 交易对手 |
| Client | VARCHAR(50) | N | 客户 |
| Portfolio | VARCHAR(50) | N | 组合 |
| Deal Subtype | VARCHAR(20) | Y | 交易子类型 |
| Product Type | VARCHAR(20) | N | 产品类型 |
| Currency | VARCHAR(10) | Y | 币种 |
| Principal Amount | DECIMAL(18,2) | Y | 本金金额 |
| Day Count Basis | VARCHAR(20) | Y | 计息天数 |
| Start Date | DATE | Y | 起始日 |
| Maturity Date | DATE | Y | 到期日 |
| Initial Basis | VARCHAR(10) | Y | Fixed/Floating |
| Fixed Until Date | DATE | N | 固定利率截止日 |
| Benchmark Rate | VARCHAR(20) | N | 基准利率 |
| Margin | DECIMAL(10,4) | N | 利差(bps) |
| Payment Frequency | VARCHAR(20) | Y | 付息频率 |
| Business Day Convention | VARCHAR(20) | N | 业务日规则 |
| Prepaid Interest | CHAR(1) | N | 预付利息 |
| Interest Rounding | VARCHAR(20) | N | 利息舍入规则 |
| Interest Includes | VARCHAR(20) | N | 计息包含日 |

### 2.4 外汇交易(FX Spot/Forward)

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| Deal Type | VARCHAR(20) | Y | 交易类型(FX) |
| Deal Subtype | VARCHAR(20) | Y | 子类型(Spot/Forward/NDF/Swap) |
| Company | VARCHAR(50) | Y | 业务单元 |
| Counterparty | VARCHAR(50) | Y | 交易对手 |
| Buy Currency | VARCHAR(10) | Y | 买入币种 |
| Sell Currency | VARCHAR(10) | Y | 卖出币种 |
| Buy Amount | DECIMAL(18,2) | Y | 买入金额 |
| Sell Amount | DECIMAL(18,2) | Y | 卖出金额 |
| Exchange Rate | DECIMAL(18,8) | Y | 汇率 |
| Value Date | DATE | Y | 交割日 |
| Settlement Date | DATE | Y | 结算日 |
| Spot Rate | DECIMAL(18,8) | N | 即期汇率 |
| Forward Points | DECIMAL(18,8) | N | 远期点数 |
| Premium/Discount | VARCHAR(10) | N | 升水/贴水 |
| Settlement Currency | VARCHAR(10) | N | 结算币种 |
| Non-Deliverable | CHAR(1) | N | 是否NDF |

### 2.5 外汇掉期(FX Swap)

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| Near Leg | - | Y | 近端交易(同FX Spot) |
| Far Leg | - | Y | 远端交易(同FX Forward) |
| Swap Points | DECIMAL(18,8) | N | 掉期点 |

### 2.6 利率掉期(IRS)

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| Company | VARCHAR(50) | Y | 业务单元 |
| Counterparty | VARCHAR(50) | Y | 交易对手 |
| Currency | VARCHAR(10) | Y | 币种 |
| Notional Amount | DECIMAL(18,2) | Y | 名义本金 |
| Effective Date | DATE | Y | 生效日 |
| Maturity Date | DATE | Y | 到期日 |
| Pay Leg Fixed Rate | DECIMAL(10,4) | N | 支付端固定利率 |
| Receive Leg Fixed Rate | DECIMAL(10,4) | N | 收取端固定利率 |
| Pay Leg Floating Rate | VARCHAR(20) | N | 支付端浮动利率(Benchmark) |
| Receive Leg Floating Rate | VARCHAR(20) | N | 收取端浮动利率(Benchmark) |
| Pay Leg Margin | DECIMAL(10,4) | N | 支付端利差 |
| Receive Leg Margin | DECIMAL(10,4) | N | 收取端利差 |
| Payment Frequency | VARCHAR(20) | Y | 付息频率 |
| Day Count Basis | VARCHAR(20) | Y | 计息基准 |
| Business Day Convention | VARCHAR(20) | N | 业务日规则 |

### 2.7 远期利率协议(FRA)

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| Company | VARCHAR(50) | Y | 业务单元 |
| Counterparty | VARCHAR(50) | Y | 交易对手 |
| Currency | VARCHAR(10) | Y | 币种 |
| Notional Amount | DECIMAL(18,2) | Y | 名义本金 |
| Deal Date | DATE | Y | 交易日期 |
| Start Date | DATE | Y | 起始日 |
| End Date | DATE | Y | 结束日 |
| FRA Rate | DECIMAL(10,4) | Y | FRA利率 |
| Settlement Date | DATE | Y | 结算日 |
| Discount/Accrue | VARCHAR(10) | N | 贴现/应计方式 |

### 2.8 同业存放(Interbank)

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| Company | VARCHAR(50) | Y | 业务单元 |
| Counterparty | VARCHAR(50) | Y | 交易对手(银行) |
| Currency | VARCHAR(10) | Y | 币种 |
| Principal Amount | DECIMAL(18,2) | Y | 金额 |
| Interest Rate | DECIMAL(10,4) | Y | 利率 |
| Start Date | DATE | Y | 起息日 |
| Maturity Date | DATE | Y | 到期日 |
| Day Count Basis | VARCHAR(20) | Y | 计息基准 |
| Payment Frequency | VARCHAR(20) | N | 付息频率 |

### 2.9 贷款(Loan)

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| Company | VARCHAR(50) | Y | 业务单元 |
| Counterparty | VARCHAR(50) | Y | 贷款银行 |
| Currency | VARCHAR(10) | Y | 币种 |
| Loan Amount | DECIMAL(18,2) | Y | 贷款本金 |
| Interest Rate | DECIMAL(10,4) | Y | 利率 |
| Start Date | DATE | Y | 发放日 |
| Maturity Date | DATE | Y | 到期日 |
| Loan Type | VARCHAR(20) | Y | 贷款类型 |
| Repayment Method | VARCHAR(20) | Y | 还款方式 |
| Guarantee Type | VARCHAR(20) | N | 担保方式 |

### 2.10 账户转账(Account Transfer)

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| Transfer ID | VARCHAR(50) | 系统 | 转账编号 |
| Transfer Date | DATE | Y | 转账日期 |
| Source Bank Account | VARCHAR(50) | Y | 源账户 |
| Destination Bank Account | VARCHAR(50) | Y | 目标账户 |
| Transfer Amount | DECIMAL(18,2) | Y | 转账金额 |
| Currency | VARCHAR(10) | Y | 币种 |
| Value Date | DATE | Y | 预计到账日 |
| Payment Method | VARCHAR(20) | Y | 支付方式(转账/电汇) |
| Transfer Reason | VARCHAR(200) | N | 转账原因 |
| Transfer Type | VARCHAR(20) | Y | 转账类型(内部/跨行) |
| Status | VARCHAR(20) | 系统 | 状态 |
| Authorized | CHAR(1) | Y | 是否需要授权 |

### 2.11 实际现金流(Actual Cashflow)

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| Cashflow Number | VARCHAR(50) | 系统 | 现金流编号 |
| Transaction Type | VARCHAR(20) | Y | 来源类型(转账/流水) |
| Transaction Number | VARCHAR(50) | Y | 来源交易编号 |
| Bank Account | VARCHAR(50) | Y | 银行账户 |
| Counterparty Bank Account | VARCHAR(50) | N | 对手方账户 |
| Direction | VARCHAR(10) | Y | 流向(Inflow/Outflow) |
| Amount | DECIMAL(18,2) | Y | 金额 |
| Currency | VARCHAR(10) | Y | 币种 |
| Cashflow Date | DATE | Y | 现金流日期 |
| Value Date | DATE | Y | 起息日 |
| Status | VARCHAR(20) | 系统 | 状态(Created/Cleared/Reconciled) |
| Cleared Amount | DECIMAL(18,2) | N | 清分金额 |
| Cleared Date | DATE | N | 清分日期 |
| Bank Reference | VARCHAR(50) | N | 银行参考号 |
| Statement Number | VARCHAR(50) | N | 对账单号 |

### 2.12 账户利息(Account Interest)

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| Interest ID | VARCHAR(50) | 系统 | 利息编号 |
| Bank Account | VARCHAR(50) | Y | 银行账户 |
| Interest Type | VARCHAR(20) | Y | 利息类型(应收/应付) |
| Interest Amount | DECIMAL(18,2) | Y | 利息金额 |
| Currency | VARCHAR(10) | Y | 币种 |
| Accrual Start Date | DATE | Y | 计息开始日 |
| Accrual End Date | DATE | Y | 计息结束日 |
| Interest Rate | DECIMAL(10,4) | Y | 利率 |
| Day Count Basis | VARCHAR(20) | Y | 计息基准 |
| Settlement Date | DATE | Y | 结算日 |
| Tax Amount | DECIMAL(18,2) | N | 税额 |
| Net Amount | DECIMAL(18,2) | Y | 净额 |
| Status | VARCHAR(20) | 系统 | 状态 |
| Related Deal | VARCHAR(50) | N | 关联交易 |
| Period | VARCHAR(20) | Y | 计息周期 |

---

## 三、共同字段(所有交易)

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| Deal ID | VARCHAR(50) | 系统 | 交易编号(自动生成) |
| Deal Date | DATE | 系统 | 交易日期 |
| Deal Status | VARCHAR(20) | 系统 | 交易状态 |
| Dealer | VARCHAR(50) | Y | 交易员 |
| Broker | VARCHAR(50) | N | 经纪人 |
| Brokerage Amount | DECIMAL(18,2) | N | 经纪费 |
| Tax Amount | DECIMAL(18,2) | N | 税额 |
| Description | VARCHAR(500) | N | 描述 |
| Remarks | VARCHAR(1000) | N | 备注 |
| Created By | VARCHAR(50) | 系统 | 创建人 |
| Creation Date | DATETIME | 系统 | 创建时间 |
| Last Updated By | VARCHAR(50) | 系统 | 修改人 |
| Last Update Date | DATETIME | 系统 | 修改时间 |

---

## 四、交易状态

| 状态 | 说明 |
|------|------|
| TEMP | 暂存/临时 |
| CURRENT | 当前生效 |
| MATURED | 已到期 |
| SETTLED | 已结算 |
| VOIDED | 已作废 |
| REVERSED | 已冲销 |

### 4.1 账户转账状态

| 状态 | 说明 |
|------|------|
| NEW | 新建 |
| VALIDATED | 已验证 |
| AUTHORIZED | 已授权 |
| SETTLEMENT_IN_PROCESS | 处理中 |
| SETTLED | 已结算 |
| FAILED | 失败 |
| CANCELED | 已取消 |

### 4.2 现金流状态

| 状态 | 说明 |
|------|------|
| CREATED | 已创建 |
| CANCELED | 已取消 |
| CLEARED | 已清分 |
| RECONCILED | 已对账 |

### 4.3 利息状态

| 状态 | 说明 |
|------|------|
| ACCRUED | 已计息 |
| CALCULATED | 已计算 |
| SETTLED | 已结算 |
| POSTED | 已过账 |

---

## 五、基础数据依赖

所有交易录入均需提前配置以下基础数据：

### 5.1 组织架构
- [ ] Company(业务单元)
- [ ] Department(部门)
- [ ] Cost Center(成本中心)

### 5.2 客户对手方
- [ ] Counterparty(交易对手)
- [ ] Counterparty Bank Account(对手方银行账户)
- [ ] Credit Rating(信用评级)

### 5.3 银行信息
- [ ] Bank(银行)
- [ ] SWIFT Code
- [ ] Bank Account(公司银行账户)

### 5.4 产品数据
- [ ] Product Type(产品类型)
- [ ] Portfolio(资金组合)
- [ ] Deal Subtype(交易子类型)

### 5.5 汇率利率
- [ ] Currency(币种)
- [ ] Exchange Rate(汇率)
- [ ] Interest Rate(基准利率)
- [ ] Day Count Basis(计息基准)

### 5.6 额度管理
- [ ] Counterparty Limit(对手方额度)
- [ ] Bank Limit(银行额度)
- [ ] Deal Limit(交易额度)

---

## 六、计息基准(Day Count Basis)

| 代码 | 名称 | 计算方式 |
|------|------|----------|
| ACT/ACT | 实际/实际 | 实际天数/实际天数 |
| ACT/360 | 实际/360 | 实际天数/360 |
| ACT/365 | 实际/365 | 实际天数/365 |
| 30/360 | 30/360 | 月按30天/年按360天 |
| 30E/360 | 30E/360 | 欧洲30/360 |
| 30E+/360 | 30E+/360 | 欧洲30+/360 |

---

## 七、业务日规则(Business Day Convention)

| 代码 | 说明 |
|------|------|
| Following | 顺延至下一工作日 |
| Modified Following | 顺延至下半月 |
| Modified Previous | 提前至上半月 |
| Previous | 提前至上一工作日 |
| No Adjustment | 不调整 |

---

*参考文档 - 源自Oracle Treasury/FIS Quantum*

# Checklist 01 — 业务字段完整性

## 目的

验证 PRD 中的业务对象字段是否与成熟资金系统对标(FIS Quantum / Murex MX.3)一致,
核心字段缺失将导致后续 DB / API / Frontend 全链路返工。

## 必填字段模板(业界对标)

### Deal(交易单据)类

| 字段 | FIS Quantum | Murex MX.3 | Open-TMS 是否覆盖 | 严重度 |
|------|-------------|------------|-------------------|--------|
| deal_no 交易编号 | ✓ | ✓ | 必须 | P0 |
| deal_type 交易类型 | ✓ | ✓ | 必须(GlobalConstants.DealType) | P0 |
| legal_entity_id 法人主体 | ✓ | ✓ | 必须 | P0 |
| book_id 账套 | ✓ | ✓ | 必须 | P0 |
| counterparty_id 对手方 | ✓ | ✓ | 必须 | P0 |
| trader_id 交易员 | ✓ | ✓ | 必须 | P0 |
| instrument_id 金融工具 | ✓ | ✓ | 必须 | P0 |
| buy_ccy / sell_ccy | ✓ | ✓ | 必须 | P0 |
| buy_amount / sell_amount | ✓ | ✓ | 必须(DECIMAL(38,18)) | P0 |
| exchange_rate 汇率 | ✓ | ✓ | 必须(DECIMAL(18,8)) | P0 |
| trade_date 交易日 | ✓ | ✓ | 必须 | P0 |
| value_date 起息日 | ✓ | ✓ | 必须 | P0 |
| settlement_date 交割日 | ✓ | ✓ | 必须 | P0 |
| maturity_date 到期日 | ✓ | ✓ | 必须(若适用) | P0 |
| status 状态 | ✓ | ✓ | 必须 | P0 |
| approval_status 审批状态 | ✓ | ✓ | 必须 | P0 |
| idempotency_key 幂等键 | ✓ | ✓ | 必须 | P0 |
| created_by/at | ✓ | ✓ | 必须(CLAUDE.md 强制) | P0 |
| updated_by/at | ✓ | ✓ | 必须 | P0 |
| version 乐观锁 | ✓ | ✓ | 必须 | P0 |
| deleted 软删除 | ✓ | ✓ | 必须 | P0 |
| remarks 备注 | ✓ | ✓ | 建议 | P2 |

### Counterparty(对手方)类

| 字段 | 严重度 |
|------|--------|
| counterparty_no | P0 |
| counterparty_name | P0 |
| counterparty_type | P0 |
| legal_entity_id | P0 |
| credit_rating | P1 |
| country_code | P0 |
| swift_code | P1 |
| status | P0 |
| audit fields | P0 |

### Instrument(金融工具)类

| 字段 | 严重度 |
|------|--------|
| instrument_no | P0 |
| instrument_name | P0 |
| instrument_type (GlobalConstants.InstrumentType) | P0 |
| ccy (主币种) | P0 |
| nominal_amount | P1 |
| coupon_rate | P1 |
| maturity_date | P1 |
| issuer | P1 |
| status | P0 |
| audit fields | P0 |

## 检查方法

```bash
# 1. 读取 PRD
Read: docs/prd/{module}/{feature}.md

# 2. grep 所有字段定义
Grep: "\\| .* \\|" pattern=...
     path=docs/prd/{module}/{feature}.md
     output=content

# 3. 对照本清单核对
```

## 缺失处理

- P0 缺失:直接退回 PM,不得进入 DB/API 设计
- P1 缺失:打标记,PM 在 PRD 评审会上说明延期
- P2 缺失:记录到 backlog,不做阻塞
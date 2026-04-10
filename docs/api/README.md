# Open-TMS 全模块接口文档

**版本**: v1.0  
**日期**: 2026-04-11

---

## 目录

### M1-交易管理模块

| 序号 | 文档 | 路径 |
|------|------|------|
| 01 | [交易管理](./dealing/01-deal.md) | `/api/v1/deals` |
| 02 | [银行账户](./bankaccount/01-bank-account.md) | `/api/v1/bank-accounts` |
| 03 | [金融工具](./instrument/01-instrument.md) | `/api/v1/instruments` |

### M2-资金管理模块

| 序号 | 文档 | 路径 |
|------|------|------|
| 04 | [资金计划](./fundplan/01-fund-plan.md) | `/api/v1/fund-plans` |
| 05 | [现金池](./cashpool/01-cash-pool.md) | `/api/v1/cash-pools` |
| 06 | [支付结算](./settlement/01-settlement.md) | `/api/v1/settlements` |
| 07 | [流动性限额](./limit/01-limit.md) | `/api/v1/limits` |

### M3-交易模块

| 序号 | 文档 | 路径 |
|------|------|------|
| 08 | [外汇交易](./fx/01-fx-deal.md) | `/api/v1/fx-deals` |
| 09 | [利率掉期](./irs/01-irs-deal.md) | `/api/v1/irs-deals` |
| 10 | [金融工具估值](./valuation/01-valuation.md) | `/api/v1/valuations` |

### M4-风险管理模块

| 序号 | 文档 | 路径 |
|------|------|------|
| 11 | [敞口管理](./exposure/01-exposure.md) | `/api/v1/exposures` |
| 12 | [套期保值](./hedge/01-hedge-relation.md) | `/api/v1/hedge-relations` |
| 13 | [减值计算](./impairment/01-impairment.md) | `/api/v1/impairments` |
| 14 | [市场风险VaR](./var/01-var-report.md) | `/api/v1/var-reports` |

### M5-报表分析模块

| 序号 | 文档 | 路径 |
|------|------|------|
| 15 | [管理驾驶舱](./cockpit/01-cockpit.md) | `/api/v1/cockpit` |
| 16 | [报表分析](./report/01-report.md) | `/api/v1/reports` |

---

## 通用接口模式

每个模块遵循以下RESTful模式：

```
GET    /api/v1/{module}               # 列表查询
GET    /api/v1/{module}/page          # 分页查询
GET    /api/v1/{module}/{id}          # 详情查询
POST   /api/v1/{module}               # 新增
PUT    /api/v1/{module}               # 更新
DELETE /api/v1/{module}/{id}          # 删除
POST   /api/v1/{module}/batch-delete  # 批量删除
POST   /api/v1/{module}/import        # 批量导入
GET    /api/v1/{module}/export        # 导出
POST   /api/v1/{module}/{id}/submit   # 提交
POST   /api/v1/{module}/{id}/approve  # 审批通过
POST   /api/v1/{module}/{id}/reject   # 审批驳回
```

---

## 通用响应结构

```json
{
  "code": 0,
  "message": "success",
  "data": {},
  "timestamp": 1704067200000
}
```

---

## 通用错误码

| 错误码 | 说明 |
|--------|------|
| 0 | 成功 |
| BUSINESS_ERROR | 业务异常 |
| VALIDATION_ERROR | 参数校验失败 |
| NOT_FOUND | 资源不存在 |
| DUPLICATE_CODE | 编码重复 |
| SYSTEM_ERROR | 系统异常 |

---

*End of Index*
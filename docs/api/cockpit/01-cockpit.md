# 管理驾驶舱接口

**模块**: cockpit  
**版本**: v1.0  
**路径**: `/api/v1/cockpit`

---

## 1. 仪表盘数据

```
GET /api/v1/cockpit/dashboard
```

### 响应
```json
{
  "code": 0,
  "data": {
    "liquidity": {
      "totalBalance": 100000000.00,
      "availableBalance": 80000000.00,
      "dailyInflow": 5000000.00,
      "dailyOutflow": 3000000.00
    },
    "profit": {
      "monthlyInterestIncome": 2500000.00,
      "monthlyInterestExpense": 1200000.00,
      "monthlyNetInterest": 1300000.00
    },
    "risk": {
      "totalVar": 2500000.00,
      "exposureLimitUsage": 65.0,
      "alertCount": 2
    },
    "deals": {
      "todayDealCount": 5,
      "pendingApprovalCount": 3,
      "settledTodayCount": 2
    }
  }
}
```

---

## 2. 资金概览

```
GET /api/v1/cockpit/cash-overview
```

---

## 3. 收益分析

```
GET /api/v1/cockpit/profit-analysis?startDate=xxx&endDate=xxx
```

---

## 4. 风险指标

```
GET /api/v1/cockpit/risk-metrics
```

---

## 5. 交易统计

```
GET /api/v1/cockpit/deal-statistics?startDate=xxx&endDate=xxx
```

### 响应
```json
{
  "code": 0,
  "data": {
    "totalDealCount": 100,
    "totalDealAmount": 500000000.00,
    "byType": [
      {"type": "DEPOSIT", "count": 50, "amount": 200000000.00},
      {"type": "LOAN", "count": 30, "amount": 250000000.00},
      {"type": "FX", "count": 20, "amount": 50000000.00}
    ]
  }
}
```
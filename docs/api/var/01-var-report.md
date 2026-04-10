# 市场风险VaR接口

**模块**: var  
**版本**: v1.0  
**路径**: `/api/v1/var-reports`

---

## 1. VaR报告列表

```
GET /api/v1/var-reports?pageNo=1&pageSize=20&reportDate=xxx&varType=xxx
```

## 2. 计算VaR

```
POST /api/v1/var-reports/calculate
```

```json
{
  "reportDate": "2026-04-11",
  "varType": "历史模拟法",
  "confidenceLevel": 0.99,
  "holdingPeriod": 1,
  "businessUnitIds": [1, 2]
}
```

---

## 3. VaR结果

```
GET /api/v1/var-reports/{id}
```

### 响应
```json
{
  "code": 0,
  "data": {
    "id": 1,
    "reportDate": "2026-04-11",
    "varType": "历史模拟法",
    "confidenceLevel": 0.99,
    "holdingPeriod": 1,
    "totalVar": 2500000.00,
    "fxVar": 800000.00,
    "irVar": 1200000.00,
    "creditVar": 500000.00,
    "varPercent": 2.5,
    "calculationTime": "2026-04-11T18:30:00"
  }
}
```

---

## 4. VaR历史趋势

```
GET /api/v1/var-reports/trend?startDate=xxx&endDate=xxx
```

---

## 5. 压力测试

```
POST /api/v1/var-reports/stress-test
```

```json
{
  "scenario": "USD+10%",
  "businessUnitId": 1
}
```

---

## 6. 返回分布

```
GET /api/v1/var-reports/{id}/distribution
```

## VaR类型

| 类型 | 说明 |
|------|------|
| 历史模拟法 | Historical Simulation |
| 方差-协方差法 | Variance-Covariance |
| 蒙特卡洛模拟 | Monte Carlo |
# 减值计算接口

**模块**: impairment  
**版本**: v1.0  
**路径**: `/api/v1/impairments`

---

## 1. 减值测试列表

```
GET /api/v1/impairments?pageNo=1&pageSize=20&assessmentDate=xxx&status=xxx
```

## 2. 新建减值测试

```
POST /api/v1/impairments
```

```json
{
  "assessmentDate": "2026-03-31",
  "businessUnitId": 1,
  "assessmentType": "期末",
  "remark": "Q1减值测试"
}
```

---

## 3. 计提减值

```
POST /api/v1/impairments/{id}/calculate
```

---

## 4. 减值结果

```
GET /api/v1/impairments/{id}/result
```

### 响应
```json
{
  "code": 0,
  "data": {
    "id": 1,
    "assessmentDate": "2026-03-31",
    "businessUnitId": 1,
    "totalExposure": 50000000.00,
    "expectedLoss": 500000.00,
    "provisionRate": 1.0,
    "status": "CALCULATED"
  }
}
```

---

## 5. 减值明细

```
GET /api/v1/impairments/{id}/details
```

## 评估方法

| 方法 | 说明 |
|------|------|
| 预期信用损失ECL | 三阶段模型 |
| 简化为12个月 | 简化模型 |
| 违约概率法 | PD×LGD×EAD |
# 报表分析接口

**模块**: report  
**版本**: v1.0  
**路径**: `/api/v1/reports`

---

## 1. 报表列表

```
GET /api/v1/reports?pageNo=1&pageSize=20&reportType=xxx&status=xxx
```

## 2. 报表模板

```
GET /api/v1/reports/templates
```

## 3. 生成报表

```
POST /api/v1/reports/generate
```

```json
{
  "templateId": 1,
  "reportName": "资金日报20260411",
  "businessUnitId": 1,
  "startDate": "2026-04-11",
  "endDate": "2026-04-11",
  "parameters": {}
}
```

---

## 4. 报表详情

```
GET /api/v1/reports/{id}
```

### 响应
```json
{
  "code": 0,
  "data": {
    "id": 1,
    "reportName": "资金日报20260411",
    "templateName": "资金日报",
    "reportDate": "2026-04-11",
    "status": "GENERATED",
    "fileUrl": "/reports/20260411/daily.pdf",
    "createdBy": "admin",
    "createdAt": "2026-04-11T18:00:00"
  }
}
```

---

## 5. 导出报表

```
GET /api/v1/reports/{id}/export?format=PDF
GET /api/v1/reports/{id}/export?format=EXCEL
```

---

## 6. 定时报表任务

```
GET /api/v1/reports/schedules
POST /api/v1/reports/schedules
DELETE /api/v1/reports/schedules/{id}
```

---

## 报表类型

| 类型 | 说明 |
|------|------|
| 资金日报 | Daily Cash Report |
| 资金周报 | Weekly Cash Report |
| 月度报表 | Monthly Report |
| 季度报表 | Quarterly Report |
| 审计报表 | Audit Report |
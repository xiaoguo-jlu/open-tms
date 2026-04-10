# 金融工具管理接口

**模块**: instrument  
**版本**: v1.0  
**路径**: `/api/v1/instruments`

---

## 1. 列表查询
```
GET /api/v1/instruments?pageNo=1&pageSize=20&keyword=xxx&instrumentType=xxx&status=xxx
```

## 2. 详情查询
```
GET /api/v1/instruments/{id}
```

## 3. 新增
```
POST /api/v1/instruments
```

## 4. 更新
```
PUT /api/v1/instruments
```

## 5. 删除
```
DELETE /api/v1/instruments/{id}
```

## 6. 批量删除
```
POST /api/v1/instruments/batch-delete
```

## 7. 导出
```
GET /api/v1/instruments/export
```

## 8. 导入
```
POST /api/v1/instruments/import
```

## 字段

| 字段 | 类型 | 说明 |
|------|------|------|
| instrumentCode | VARCHAR(50) | 工具代码 |
| instrumentName | VARCHAR(200) | 工具名称 |
| instrumentType | VARCHAR(20) | 工具类型 |
| underlyingType | VARCHAR(20) | 标的类型 |
| currency | VARCHAR(10) | 币种 |
| maturityDate | DATE | 到期日 |
| status | CHAR(1) | 状态 |
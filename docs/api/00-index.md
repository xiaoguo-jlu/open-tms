# Open-TMS 基础数据模块 接口文档

**版本**: v1.0  
**日期**: 2026-04-06

---

## 目录

| 序号 | 文档 | 资源路径 |
|------|------|----------|
| 01 | [业务单元管理](./01-business-unit.md) | `/api/v1/business-units` |
| 02 | [交易员管理](./02-trader.md) | `/api/v1/traders` |
| 03 | [币种管理](./03-currency.md) | `/api/v1/currencies` |
| 04 | [国家/地区管理](./04-country.md) | `/api/v1/countries` |
| 05 | [节假日管理](./05-holiday.md) | `/api/v1/holidays` |
| 06 | [银行信息管理](./06-bank.md) | `/api/v1/banks` |
| 07 | [交易对手管理](./07-counterparty.md) | `/api/v1/counterparties` |
| 08 | [对手方银行账户](./08-counterparty-account.md) | `/api/v1/counterparty-accounts` |

---

## 通用接口模式

每个资源遵循以下RESTful模式：

```
GET    /{resources}              # 列表查询（分页）
GET    /{resources}/page         # 分页查询
GET    /{resources}/{id}         # 详情查询
POST   /{resources}              # 新增
PUT    /{resources}             # 更新
DELETE /{resources}/{id}        # 删除
POST   /{resources}/batch-delete # 批量删除
POST   /{resources}/import      # 批量导入
GET    /{resources}/export      # 导出
```

---

## 通用响应结构

### 成功响应
```json
{
  "code": 0,
  "message": "success",
  "data": {},
  "timestamp": 1704067200000
}
```

### 分页响应
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [],
    "total": 100,
    "pageNo": 1,
    "pageSize": 20
  },
  "timestamp": 1704067200000
}
```

### 失败响应
```json
{
  "code": "BUSINESS_ERROR",
  "message": "编码已存在",
  "data": null,
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

## 分页参数

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| pageNo | int | 1 | 页码 |
| pageSize | int | 20 | 每页条数 |
| sortField | string | createdAt | 排序字段 |
| sortOrder | string | desc | 排序方向 |

---

## 通用查询参数

| 参数 | 类型 | 说明 |
|------|------|------|
| keyword | string | 关键字搜索 |
| status | string | 状态筛选（0-停用 1-启用） |

---

*End of Index*

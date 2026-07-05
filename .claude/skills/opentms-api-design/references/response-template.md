# API 响应结构模板

> 从 SKILL.md 附录D移出。

```json
// 成功响应
{
  "code": 0,
  "message": "success",
  "data": {},
  "timestamp": 1704067200000
}

// 分页响应
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

// 错误响应
{
  "code": "BUSINESS_ERROR",
  "message": "错误描述",
  "data": null,
  "timestamp": 1704067200000
}
```

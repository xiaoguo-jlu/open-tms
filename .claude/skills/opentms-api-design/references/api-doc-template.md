# API 接口文档模板

> 从 SKILL.md 附录A移出。

```markdown
# {模块名称}接口

**模块**: {module}  
**版本**: v1.0  
**日期**: YYYY-MM-DD  
**路径**: `/api/v1/{resource}`

---

## 1. {接口名称}

### 请求
```
{METHOD} /api/v1/{resource}
```

### 参数
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| {param1} | string | Y | 参数说明 |
| {param2} | int | N | 参数说明 |

### 请求体（若适用）
```json
{
  "field1": "value1",
  "field2": "value2"
}
```

### 响应
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": 1,
    "name": "示例"
  },
  "timestamp": 1704067200000
}
```

### 错误码
| 错误码 | 说明 |
|--------|------|
| BUSINESS_ERROR | 业务异常 |
| VALIDATION_ERROR | 参数校验失败 |
| NOT_FOUND | 资源不存在 |

---

## 2. {接口名称2}

...

---

## 接口清单

| 序号 | 接口 | 方法 | 说明 |
|------|------|------|------|
| 1 | /{resource} | GET | 列表查询 |
| 2 | /{resource}/{id} | GET | 详情查询 |
| 3 | /{resource} | POST | 新增 |
| 4 | /{resource}/update | POST | 更新 |
| 5 | /{resource}/delete/{id} | POST | 删除 |
| 6 | /{resource}/{id}/action | POST | 业务操作 |

---

*API产出 - v{版本号}*
```

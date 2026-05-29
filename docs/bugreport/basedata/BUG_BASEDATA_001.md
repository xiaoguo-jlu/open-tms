## Bug编号
BUG_BASEDATA_001

## 基本信息
| 项目 | 内容 |
|------|------|
| 缺陷编号 | BUG_BASEDATA_001 |
| 缺陷标题 | 基于数据模块POST请求返回415 Unsupported Media Type |
| 发现日期 | 2026-05-27 |
| 发现版本 | v1.0.0-SNAPSHOT |
| 测试人员 | QA |
| 开发人员 | 待定 |
| 严重程度 | 高 |
| 缺陷状态 | 待修复 |

## 环境信息
- 测试环境：http://localhost:8081
- 基础路径：/opentms/basedata/api/v1/
- 操作系统：Windows
- 数据库：PostgreSQL

## 缺陷描述

### 问题概述
基于数据模块所有写操作接口(POST/PUT)返回HTTP 415 Unsupported Media Type，无法正常新增或更新数据。

### 复现步骤
1. 发送POST请求到 `http://localhost:8081/opentms/basedata/api/v1/currencies`
2. 请求头设置 `Content-Type: application/json`
3. 请求体包含有效JSON数据：`{"code":"TST","name":"Test","status":"1"}`

### 预期结果
返回HTTP 200或201，数据成功创建或更新

### 实际结果
返回HTTP 415 Unsupported Media Type，无响应体

### 错误日志
```
> POST /opentms/basedata/api/v1/currencies HTTP/1.1
> Host: localhost:8081
> Content-Type: application/json
> Content-Length: 2
< HTTP/1.1 415 
< Date: Wed, 27 May 2026 15:21:24 GMT
```

## 缺陷分析

### 根本原因
`JacksonJsonBodyWriter` (basedata/src/main/java/com/opentms/basedata/config/JacksonJsonBodyWriter.java) 只配置了 `@Produces` (JSON输出)，缺少JSON输入的反序列化支持。

CXF JAX-RS 无法将JSON请求体反序列化为Java对象，因为没有配置相应的 `MessageBodyReader`。

### 影响范围
所有基于数据模块的写操作接口：
- POST `/currencies` - 新增币种
- POST `/banks` - 新增银行
- POST `/business-units` - 新增业务单元
- POST `/traders` - 新增交易员
- POST `/counterparties` - 新增交易对手
- POST `/counterparty-accounts` - 新增对手账户
- 所有 PUT 更新接口

### 修复建议
方案1: 添加 JacksonJsonBodyReader
```java
@Component
@Provider
@Consumes(MediaType.APPLICATION_JSON)
public class JacksonJsonBodyReader implements MessageBodyReader<Object> {
    // 实现反序列化逻辑
}
```

方案2: 在 application.yml 中配置Jackson作为CXF的JSON Provider

## 测试结果

| 接口 | 方法 | 结果 |
|------|------|------|
| /currencies | GET | ✅ 正常 |
| /currencies/{id} | GET | ✅ 正常 |
| /currencies | POST | ❌ 415 |
| /currencies | PUT | ❌ 415 |
| /currencies/{id} | DELETE | ❌ 500 |

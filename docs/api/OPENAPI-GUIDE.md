# Open-TMS OpenAPI 自动生成与 Swagger UI 使用指南

> 状态:**基于数据 (CXF) + 交易 (Spring MVC) 已打通**  
> 版本:v1.0(2026-07-10)  
> 维护者:Open-TMS Tech Lead

---

## 1. 概述

Open-TMS 是一个多模块 Maven 项目,**REST 框架混用**:

| 模块 | 框架 | OpenAPI 来源 |
|------|------|---------------|
| `basedata`(8081) | Apache CXF 4.0.3(JAX-RS) | **自写反射扫描器** `OpenApiCxfScanner` |
| `dealing`(8082) | Spring MVC | **springdoc-openapi-starter-webmvc-ui 2.3.0** 自动扫描 |
| `fundplan` / `valuation` / `var` | Spring MVC | 复用 `springdoc`(未启用) |

由于 CXF 默认不生成 OpenAPI 规范,我们采用了 **A 路线**:
- `basedata`:反射扫描 `@Path` 注解的 JAX-RS Resource 类,手写构造 `io.swagger.v3.oas.models.OpenAPI`
- `dealing`:SpringDoc 自动扫描 `@RestController`,完全免配置
- 顶层 `docs/api/openapi.json`:启动两个服务后由 `scripts/gen-openapi.sh` 合并两个 JSON

---

## 2. 依赖矩阵(已固化在 `pom.xml`)

```xml
<!-- 父 pom dependencyManagement -->
<springdoc.version>2.3.0</springdoc.version>
<swagger-core.version>2.2.19</swagger-core.version>
```

| 模块 | 依赖 |
|------|------|
| `basedata/pom.xml` | `io.swagger.core.v3:swagger-core-jakarta` + `swagger-models-jakarta` |
| `dealing/pom.xml` | `org.springdoc:springdoc-openapi-starter-webmvc-ui` |
| `web/package.json` | `swagger-ui-dist@5.17.14`(运行时直接消费预编译的 UI bundle) |

> ⚠️ **Spring Boot 3.x 一律使用 `swagger-core-jakarta`**(不能用 `swagger-core`,否则 javax/jakarta 包不兼容)

---

## 3. 启动

### 3.1 后端

```bash
# 1. 构建
mvn clean package -pl basedata,dealing -am -DskipTests

# 2. 启动基于数据(端口 8081)
java -jar basedata/target/opentms-basedata-1.0.0-SNAPSHOT.jar

# 3. 另开终端启动交易(端口 8082)
java -jar dealing/target/dealing-1.0.0-SNAPSHOT.jar

# 4. 健康检查
curl http://localhost:8081/actuator/health
curl http://localhost:8082/v3/api-docs
```

### 3.2 前端

```bash
cd web
npm install
npm run dev
```

打开浏览器:<http://localhost:3000/system/api-docs>

---

## 4. OpenAPI 端点

| 模块 | 端点 | 返回 |
|------|------|------|
| basedata | `GET http://localhost:8081/api/v1/openapi/cxf-spec` | **纯 OpenAPI 3.0 规范 JSON**(Swagger UI urls 选择器直接消费) |
| basedata | `GET http://localhost:8081/api/v1/openapi/cxf` | 包装在 `{module, framework, serverUrl, spec}` 中,便于人读 |
| dealing | `GET http://localhost:8082/v3/api-docs` | SpringDoc 默认端点 |
| dealing | `GET http://localhost:8082/swagger-ui.html` | SpringDoc 自带 UI(可单独访问) |

### Vite 代理映射(已配置在 `web/vite.config.js`)

| 前端 URL | 后端 |
|---------|------|
| `/api/v1/openapi/cxf` | `http://localhost:8081` |
| `/v3/api-docs` | `http://localhost:8082` |
| `/swagger-ui` | `http://localhost:8082` |

---

## 5. Swagger UI 前端页面

文件:`web/src/views/system/ApiDocs.vue`

- 顶部 tab 切换:**全部模块 / 基于数据 (CXF) / 交易 (Spring MVC)**
- 使用 `swagger-ui-dist` 提供的 `SwaggerUIBundle` + `urls` 选择器
- 路由:`/system/api-docs`
- 侧边栏菜单:**系统管理 → 接口文档 (Swagger UI)**

---

## 6. 自动生成 docs/api/openapi.json

```bash
bash scripts/gen-openapi.sh
```

执行步骤:
1. 健康检查 basedata + dealing
2. 拉取 `/api/v1/openapi/cxf-spec` 和 `/v3/api-docs`
3. 用 Python 合并到 `docs/api/openapi.json`,顶层带 `servers[]` 区分模块
4. 记录 `x-modules` 元数据(每个模块的 paths 数 + server)

环境变量覆盖:

```bash
BASEDATA_URL=http://localhost:8081 \
DEALING_URL=http://localhost:8082 \
OUTPUT=docs/api/openapi.json \
bash scripts/gen-openapi.sh
```

---

## 7. 新增 controller 后怎么办?

### 7.1 dealing(Spring MVC)— 不用动

SpringDoc 启动时一次性扫描,新加 `@RestController` 后**重启 dealing** 即可。

### 7.2 basedata(CXF)— 也不用动

`OpenApiCxfScanner` 启动时通过 `ApplicationContext.getBeansWithAnnotation(Path.class)`
反射拿到所有带 `@Path` 注解的 Bean,**新加 `@Path("/api/v1/xxx")` Resource 后重启 basedata 即可**。

> 注:扫描结果缓存 60s。如果热加载 / Class 重载后没看到变化,等 60s 或重启。

### 7.3 给 OpenAPI 加 metadata(可选)

如果想给特定 controller 单独写 `@Operation(summary=...)` 注解,需要:
- 在 basedata 加 `io.swagger.core.v3:swagger-annotations-jakarta`(已在 swagger-core 传递依赖中)
- `OpenApiCxfScanner` 当前未读取这些注解;后续可扩展 `scanOperationMeta` 方法

---

## 8. 关键设计决策

| 决策 | 原因 |
|------|------|
| 用 `swagger-core-jakarta` 而非 `swagger-core` | Spring Boot 3.x 用 jakarta 包,swagger-core(2.2.0-)还是 javax 包,反射会 ClassNotFoundException |
| 自写 CXF 扫描器 vs 用 swagger-jaxrs2-integration | 后者要 CXF 的 Bus 集成,在 Spring Boot 启动顺序下不稳定;自写扫描器 < 400 行,依赖更少 |
| dealing 不写自定义代码 | SpringDoc 2.3.0 + Spring Boot 3.2 自动发现 `@RestController`,已验证 51 paths |
| Swagger UI 用 swagger-ui-dist 而非 springdoc-swagger-ui | 前端集成需要走 Vite 代理,springdoc 默认 UI 与 Spring 强耦合 |
| `/api/v1/openapi/cxf-spec` 同时返回包装 + 裸 spec | 包装版人读友好,裸 spec 可直接喂 Swagger UI |

---

## 9. 已知限制 / 后续可优化

1. **`@BeanParam` 未支持**(目前 Open-TMS 没有使用)
2. **泛型嵌套超过 3 层可能掉信息**(反射列字段时,字段类型为 `Object` 时不展开)
3. **CXF Resource 方法返回类型大多是 `Object`** → 我们无法从签名推断 `Result<T>` 的 T,响应 schema 退化为通用 `{code, message, data, timestamp}` 包装
4. **未读取 `@Operation` / `@Parameter` 注解**(基于数据当前 controller 也没有这些注解)
5. **未做鉴权接入**(`securitySchemes` 暂未配置;Open-TMS 当前未启用 Spring Security)
6. **fundplan / valuation / var** 三个模块未启用 springdoc;若需要,在对应 `pom.xml` 加依赖并在 `application.yml` 加配置即可
7. **`SwaggerUIBundle` 在 Vue 卸载时无法完全清理** → 已用 `innerHTML = ''` 兜底,长会话偶有内存泄漏

---

## 10. 验证清单

完成本工程后,**所有项必须通过**:

- [x] `mvn clean package -pl basedata,dealing -am -DskipTests` BUILD SUCCESS
- [x] basedata 启动 < 30s,`actuator/health` 返回 200
- [x] `curl /api/v1/openapi/cxf-spec` 返回有效 JSON,**80 paths**
- [x] dealing 启动 < 30s,`/v3/api-docs` 返回有效 JSON,**51 paths**
- [x] `bash scripts/gen-openapi.sh` 成功生成 `docs/api/openapi.json`(**131 paths 合并**)
- [x] `npm run dev` 后 `http://localhost:3000/system/api-docs` 可访问
- [x] Swagger UI 顶部 tab 切换 "全部 / basedata / dealing" 工作正常
- [x] 侧边栏"系统管理 → 接口文档"入口可点
- [x] Vite 代理 `/v3/api-docs` 与 `/api/v1/openapi/cxf` 都返回 200
---
name: opentms-api-design
description: Use when designing Open-TMS backend API contracts and documentation as Technical Architect
---

# Open-TMS 后端接口设计 Skill (API)

## 简介

本skill用于Open-TMS项目的后端接口设计，指导开发人员/架构师完成从PRD到API文档的完整流程。

---

## 一、触发条件

当需要进行以下工作时触发：PRD已确认需要设计接口方案、已有功能需要新增/修改接口、跨模块调用需要定义接口契约、评审已有接口设计。

触发信号：PM完成PRD评审分配接口设计任务、TA要求设计接口契约、Dev需要明确接口规范、QA需要接口文档进行测试。

---

## 二、输入要求

### 2.1 必须输入

- **PRD文档**: PM提供 — 功能需求，明确接口需求
- **总体设计规范**: `docs/规范/Open-TMS开发规范文档.md`
- **通用接口模式**: `docs/api/README.md` — 模块接口列表和通用模式
- **模块历史摘要**: `docs/api/{模块}/SUMMARY.md` — 本模块历史接口设计记录（若存在）
- **已有相关接口**: `docs/api/`

### 2.2 可选输入

- **竞品接口调研**: FIS Quantum/SAP接口参考
- **技术约束**: TA提供
- **前端接口需求**: UX提供

---

## 三、输出规范

### 3.1 交付件输出标准

每个接口文档必须包含: 请求（Method + URL + 参数）、请求体、响应、响应码定义、错误码。

完整模板见 `references/api-doc-template.md`。响应结构模板见 `references/response-template.md`。

### 3.2 接口分类

| 分类 | 说明 | 示例 |
|------|------|------|
| CRUD接口 | 基础增删改查 | 列表、新增、更新、删除、详情 |
| 业务接口 | 业务操作 | 提交、审批、执行、撤销 |
| 导出接口 | 数据导出 | Excel导出、PDF导出 |
| 导入接口 | 数据导入 | Excel批量导入 |
| 统计接口 | 聚合查询 | 汇总、统计、报表 |

### 3.3 存放路径规范

```
docs/api/
├── README.md                  # 全模块接口索引
├── common/docs/               # 公共接口
├── basedata/                  # M0基础数据
├── dealing/                   # M1交易管理
├── bankaccount/              # M1银行账户
├── cashpool/                 # M2现金池
├── fx/ valuation/            # M3金融工具
├── exposure/ var/            # M4风险管理
└── cockpit/ report/          # M5分析报表
```

### 3.4 设计摘要标准

每次完成后更新 `docs/api/{模块}/SUMMARY.md`，记录: 完成的接口、遇到的问题、跨模块接口契约、待确认事项。

---

## 四、执行步骤

### 步骤1：业界洞察

读取 `docs/api/README.md` 了解现有接口模式，研究FIS Quantum、SAP、Murex的接口设计。对标参考见 `references/api-patterns.md`。

### 步骤2：读取历史摘要

检查 `docs/api/{模块}/SUMMARY.md`，了解已完成接口、设计决策、待优化接口。若为新模块则创建新摘要文件。

### 步骤3：读取PRD理解需求

详细阅读PRD，识别需要设计的接口清单、数据流转和跨模块调用。

### 步骤4：接口设计

1. **URL设计**: 资源命名用复数名词，版本 `/api/v1/`，路径参数 `/{id}`，业务操作 `/{id}/action`。

2. **HTTP方法规范（重要）**: > 详见 `CLAUDE.md` REST API Patterns 章节。
   ```
   GET    /api/v1/resources              # 列表查询
   GET    /api/v1/resources/page         # 分页查询
   GET    /api/v1/resources/{id}         # 详情查询
   POST   /api/v1/resources              # 新增
   POST   /api/v1/resources/update       # 更新
   POST   /api/v1/resources/delete/{id}  # 删除
   POST   /api/v1/resources/{id}/action  # 业务操作
   ```
   红线: update/delete 一律 POST，勿用 PUT/DELETE。

3. **请求参数**: 查询参数（分页/排序/筛选）、路径参数（资源ID）、请求体 JSON（Content-Type: application/json）。

4. **响应结构**: 统一包装 `{code, message, data, timestamp}`。分页 `{records, total, size, current}`。详见 `CLAUDE.md` API Response Format。

5. **幂等性**: 写操作接口支持幂等，使用 `X-Idempotency-Key` 请求头。

#### 4.1 Controller try-catch 规范 (BadRequest 统一处理)

所有 Controller 方法必须遵循统一的异常处理模式:

```java
// 正确做法: Controller 不写 try-catch，由 GlobalExceptionHandler 统一处理
@PostMapping
public Result<Void> create(@Valid @RequestBody XxxDTO dto) {
    xxxService.create(dto);
    return Result.success();
}

// Service 层抛 BusinessException
public void create(XxxDTO dto) {
    if (exists(dto.getCode())) {
        throw new BusinessException(ResultCode.BAD_REQUEST, "编码已存在");
    }
    // ...
}
```

**禁止做法**:
- Controller 中 try-catch 后手动返回 `Result.badRequest()` → 应由 GlobalExceptionHandler 统一拦截
- Service 中 catch 后吞异常不抛出 → 必须向上传播给 GlobalExceptionHandler
- Controller 中直接 `return Result.badRequest("xxx")` → 改用 `throw new BusinessException(...)`

**设计原则**: Controller 层零异常处理代码，所有异常由 GlobalExceptionHandler 统一转换为 Result 响应。

### 步骤5：检查设计一致性

对照规范检查: URL RESTful、HTTP方法、响应结构、错误码、分页参数、幂等设计。

### 步骤6：定义接口契约

识别跨模块调用场景，明确接口提供方/消费方、数据格式约定、错误处理约定。

### 步骤7：创建GitHub Project工作项

创建后端开发Task。具体命令 > 详见 `opentms-pm-lead` 第九节。

### 步骤8：生成设计摘要

更新 `docs/api/{模块}/SUMMARY.md`。

---

## 五、业界优秀实践

RESTful API设计原则: 资源导向（URL代表资源）、统一接口（标准HTTP方法）、错误处理（适当状态码+错误详情）、分页过滤（默认分页避免全量返回）。

金融系统特殊要求: 金额 DECIMAL(18,2)，汇率 DECIMAL(18,8)，利率 DECIMAL(10,4)；ISO 8601日期、UTC时区；状态机流转+审计日志；幂等性保证（所有写操作）。

> 详细设计模式和对标分析见 `references/api-patterns.md`。

---

## 六、与其他Skill的衔接

### 6.1 前置依赖: 产品设计(PM) 提供 PRD + 数据库设计(DB) 提供表结构

### 6.2 后续触发: 后端代码开发、前端代码开发、测试用例设计

### 6.3 协作流程: PRD → 数据库设计 → 后端接口设计 → 后端/前端开发 → 测试用例设计

---

## 七、质量标准

### 7.1 接口设计质量检查点

| 检查项 | 标准 | 权重 |
|--------|------|------|
| RESTful规范符合性 | 符合RESTful设计原则 | 25% |
| 响应结构统一性 | 符合项目响应标准 | 20% |
| 参数设计合理性 | 参数命名、类型、必填合理 | 20% |
| 错误处理完整性 | 错误码覆盖全面 | 15% |
| 文档完整性 | 包含所有必要章节 | 10% |
| 幂等性设计 | 写操作接口支持幂等 | 10% |

### 7.2 量化指标

| 指标 | 目标值 | 最低值 |
|------|--------|--------|
| RESTful符合率 | 100% | 95% |
| 错误码覆盖率 | 100% | 90% |
| 文档完整率 | 100% | 95% |
| 接口一致性（与前端对齐） | 100% | 95% |

### 7.3 评审通过标准

- [ ] 所有PRD中的接口需求已设计
- [ ] URL符合RESTful规范、响应结构统一
- [ ] 错误码定义完整
- [ ] 跨模块接口契约已确认
- [ ] 已创建后端开发任务

---

## 八、交付物检查清单

### 8.1 接口文档

- [ ] 文件命名符合规范、存放路径正确、包含所有必要章节
- [ ] 请求参数完整、响应结构明确、错误码定义完整

### 8.2 设计摘要

- [ ] 已更新SUMMARY.md（含完成内容、问题、跨模块契约、待确认事项）

### 8.3 GitHub工作项

- [ ] Task已创建，Label正确分配（Dev,Task），验收标准明确

### 8.4 规范一致性

- [ ] URL符合项目规范、HTTP方法正确、响应结构统一、分页参数符合规范、幂等设计考虑

---

## 九、附录

- **附录A - 接口文档模板**: 见 `references/api-doc-template.md`
- **附录B - SUMMARY 模板**: 见原附录B
- **附录C - 通用接口模式**: > 详见 `CLAUDE.md` REST API Patterns 章节
- **附录D - 响应结构模板**: 见 `references/response-template.md`
- **业界实践**: 见 `references/api-patterns.md`

---

## 版本历史

| 版本 | 日期 | 变更内容 |
|------|------|---------|
| v1.0 | YYYY-MM-DD | 初始版本 |

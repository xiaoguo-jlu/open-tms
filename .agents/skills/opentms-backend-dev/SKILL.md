---
name: opentms-backend-dev
description: Use when implementing Open-TMS backend API and business logic as Backend Developer
---

# Open-TMS 后端开发 Skill (BE)

## 简介

本skill用于Open-TMS项目的后端代码开发，指导开发人员完成从API设计到可运行后端代码的完整流程。

---

## 一、触发条件

**当需要进行以下工作时，触发本skill：**

- API接口文档已完成，分配后端开发任务
- 需要实现新的后端接口
- 需要优化/重构已有后端代码
- 需要修复后端缺陷

**触发信号：**
- TA在GitHub Projects创建Task分配给后端
- PM-Lead分配后端开发任务
- QA发现后端缺陷

---

## 二、输入要求

### 2.1 必须输入

| 输入项 | 来源 | 说明 |
|--------|------|------|
| API接口文档 | Dev提供 | 接口定义和接口契约 |
| 数据库设计 | TA提供 | 数据表结构 |
| 总体设计规范 | `docs/规范/Open-TMS开发规范文档.md` | 后端开发规范 |
| 模块已有代码 | `{module}/src/main/java/com/opentms/{module}/` | 同模块已有代码参考 |
| 模块历史摘要 | `{module}/SUMMARY.md` | 本模块历史开发记录（若存在） |

### 2.2 可选输入

| 输入项 | 来源 | 说明 |
|--------|------|------|
| PRD文档 | PM提供 | 功能需求说明 |
| 技术方案文档 | TA提供 | 关键技术方案 |

---

## 三、输出规范

### 3.1 交付件输出标准

#### 3.1.1 Java代码标准

每个后端模块必须遵循以下包结构：
```
com.opentms.{module}/
├── controller/        # 接口层（JAX-RS Resource）
├── service/          # 服务层接口
│   └── impl/         # 服务层实现
├── entity/           # 实体类
├── dto/              # 数据传输对象
├── vo/               # 视图对象
├── mapper/           # MyBatis Mapper
└── constant/         # 常量定义
```

#### 3.1.2 目录结构规范

```
{module}/src/main/java/com/opentms/{module}/
├── controller/
│   └── {Entity}Resource.java    # JAX-RS Resource
├── service/
│   ├── {Entity}Service.java     # Service接口
│   └── impl/
│       └── {Entity}ServiceImpl.java  # Service实现
├── entity/
│   └── {Entity}.java            # 实体类
├── dto/
│   └── {Entity}DTO.java        # DTO
├── vo/
│   └── {Entity}VO.java        # VO
├── mapper/
│   └── {Entity}Mapper.java    # Mapper接口
└── constant/
    └── {Module}Constants.java # 常量类

{module}/src/main/resources/
└── mapper/
    └── {Entity}Mapper.xml     # MyBatis XML
```

### 3.2 存放路径规范

| 类型 | 路径 |
|------|------|
| Controller | `{module}/src/main/java/com/opentms/{module}/controller/` |
| Service | `{module}/src/main/java/com/opentms/{module}/service/` |
| Entity | `{module}/src/main/java/com/opentms/{module}/entity/` |
| DTO | `{module}/src/main/java/com/opentms/{module}/dto/` |
| VO | `{module}/src/main/java/com/opentms/{module}/vo/` |
| Mapper | `{module}/src/main/java/com/opentms/{module}/mapper/` |
| Mapper XML | `{module}/src/main/resources/mapper/` |

### 3.3 开发摘要标准

每次完成一组接口开发后，生成开发摘要：

```
# {模块名} 后端开发摘要

## 最近更新
- **日期**: YYYY-MM-DD
- **开发者**: BE
- **本次完成**: {接口列表}

## 开发过程记录

### YYYY-MM-DD - {本次主题}
**完成内容**:
- {已完成的接口1}
- {已完成的接口2}

**遇到的问题**:
- {问题1} → {解决方案}
- {问题2} → {解决方案}

**性能优化**:
- {优化项}

**待确认事项**:
- {待确认事项1}
- {待确认事项2}

### 历史记录
- YYYY-MM-DD: {开发主题} - 完成{接口列表}
```

---

## 四、执行步骤

### 步骤1：读取输入

**目的**：理解接口需求和数据库设计。

**操作**：

1. 阅读API接口文档，理解接口定义和参数
2. 阅读数据库设计文档，理解数据表结构
3. 检查同模块已有代码，了解项目风格
4. 确认是否有相似实现可参考

**输出**：
- 确认开发范围
- 识别开发重点和难点
- 列出需要确认的问题

### 步骤2：检查设计一致性

**目的**：确保开发符合项目规范。

**操作**：

1. 读取 `docs/规范/Open-TMS开发规范文档.md`
2. 确认以下规范：
   - [ ] 类命名规范
   - [ ] 方法命名规范
   - [ ] 包结构规范
   - [ ] 注解使用规范

### 步骤3：Entity层开发

**目的**：定义数据库实体映射。

**操作**：

1. 创建Entity类：`entity/{Entity}.java`
2. 继承基础实体类（如有）
3. 添加MyBatis注解

**Entity模板**：
```java
package com.opentms.{module}.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.opentms.common.model.BaseCodeEntity;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tms_{entity}_t")
public class {Entity} extends BaseCodeEntity {
    
    // 额外字段
    private String field1;
    private Integer field2;
}
```

### 步骤4：DTO层开发

**目的**：定义数据传输对象。

**操作**：

1. 创建DTO类：`dto/{Entity}DTO.java`
2. 添加校验注解

**DTO模板**：
```java
package com.opentms.{module}.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
public class {Entity}DTO {
    
    private Long id;
    
    @NotBlank(message = "编码不能为空")
    private String code;
    
    @NotBlank(message = "名称不能为空")
    private String name;
    
    private String status;
}
```

### 步骤5：VO层开发

**目的**：定义视图对象。

**操作**：

1. 创建VO类：`vo/{Entity}VO.java`
2. 添加JSON格式化注解

**VO模板**：
```java
package com.opentms.{module}.vo;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;

@Data
public class {Entity}VO {
    
    private Long id;
    private String code;
    private String name;
    private String status;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
```

### 步骤6：Mapper层开发

**目的**：定义数据访问接口。

**操作**：

1. 创建Mapper接口：`mapper/{Entity}Mapper.java`
2. 继承BaseMapper

**Mapper模板**：
```java
package com.opentms.{module}.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.opentms.{module}.entity.{Entity};
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface {Entity}Mapper extends BaseMapper<{Entity}> {
}
```

### 步骤7：Service层开发

**目的**：实现业务逻辑。

**操作**：

1. 创建Service接口：`service/{Entity}Service.java`
2. 创建Service实现：`service/impl/{Entity}ServiceImpl.java`

**Service接口模板**：
```java
package com.opentms.{module}.service;

import com.opentms.{module}.entity.{Entity};
import com.opentms.{module}.dto.{Entity}DTO;
import com.opentms.{module}.vo.{Entity}VO;
import com.baomidou.mybatisplus.core.metadata.IPage;

public interface {Entity}Service extends BaseService<{Entity}, {Entity}DTO, {Entity}VO> {
}
```

**Service实现模板**：
```java
package com.opentms.{module}.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.opentms.common.model.BaseCodeEntity;
import com.opentms.{module}.dto.{Entity}DTO;
import com.opentms.{module}.entity.{Entity};
import com.opentms.{module}.mapper.{Entity}Mapper;
import com.opentms.{module}.service.{Entity}Service;
import com.opentms.{module}.vo.{Entity}VO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Slf4j
@Service
public class {Entity}ServiceImpl extends BaseServiceImpl<{Entity}Mapper, {Entity}> implements {Entity}Service {

    @Override
    public IPage<{Entity}VO> queryPage({Entity}DTO dto, int pageNum, int pageSize) {
        LambdaQueryWrapper<{Entity}> wrapper = new LambdaQueryWrapper<>();
        if (dto.getKeyword() != null && !dto.getKeyword().isEmpty()) {
            wrapper.like({Entity}::getCode, dto.getKeyword())
                   .or()
                   .like({Entity}::getName, dto.getKeyword());
        }
        if (dto.getStatus() != null && !dto.getStatus().isEmpty()) {
            wrapper.eq({Entity}::getStatus, dto.getStatus());
        }
        wrapper.orderByDesc({Entity}::getCreatedAt);
        
        Page<{Entity}> page = new Page<>(pageNum, pageSize);
        IPage<{Entity}> result = this.page(page, wrapper);
        
        return result.convert(this::toVO);
    }

    @Override
    public {Entity}VO getById(Long id) {
        {Entity} entity = this.getById(id);
        return entity != null ? toVO(entity) : null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean save({Entity}DTO dto) {
        {Entity} entity = toEntity(dto);
        return this.save(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean update({Entity}DTO dto) {
        {Entity} entity = toEntity(dto);
        return this.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delete(Long id) {
        return this.removeById(id);
    }

    @Override
    public List<{Entity}VO> listAll() {
        return this.list().stream().map(this::toVO).toList();
    }

    private {Entity} toEntity({Entity}DTO dto) {
        {Entity} entity = new {Entity}();
        BeanUtils.copyProperties(dto, entity);
        return entity;
    }

    private {Entity}VO toVO({Entity} entity) {
        {Entity}VO vo = new {Entity}VO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }
}
```

### 步骤8：Controller层开发

**目的**：实现REST接口。

**操作**：

1. 创建Resource类：`controller/{Entity}Resource.java`
2. 实现CRUD接口
3. 实现业务操作接口

**Controller模板**：
```java
package com.opentms.{module}.controller;

import com.opentms.{module}.dto.{Entity}DTO;
import com.opentms.{module}.service.{Entity}Service;
import com.opentms.{module}.vo.{Entity}VO;
import com.opentms.common.model.Result;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Path("/api/v1/{entities}")
public class {Entity}Resource {

    @Autowired
    private {Entity}Service {entity}Service;

    @GET
    public Object list() {
        return Result.success({entity}Service.listAll());
    }

    @GET
    @Path("/{id}")
    public Object getById(@PathParam("id") String id) {
        try {
            long parseId = Long.parseLong(id);
            if (parseId <= 0) {
                return Result.badRequest("ID必须为正整数");
            }
            {Entity}VO vo = {entity}Service.getById(parseId);
            return vo != null ? 
                Result.success(vo) : 
                Result.notFound("{Entity}不存在");
        } catch (NumberFormatException e) {
            return Result.badRequest("ID参数格式不正确");
        }
    }

    @POST
    public Object save({Entity}DTO dto) {
        try {
            return Result.success({entity}Service.save(dto));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PUT
    public Object update({Entity}DTO dto) {
        try {
            if (dto.getId() == null) {
                return Result.badRequest("ID不能为空");
            }
            return Result.success({entity}Service.update(dto));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DELETE
    @Path("/{id}")
    public Object delete(@PathParam("id") String id) {
        try {
            long parseId = Long.parseLong(id);
            if (parseId <= 0) {
                return Result.badRequest("ID必须为正整数");
            }
            {entity}Service.removeById(parseId);
            return Result.success();
        } catch (NumberFormatException e) {
            return Result.badRequest("ID参数格式不正确");
        }
    }
}
```

### 步骤9：事务管理

**目的**：确保数据一致性。

**操作**：

1. 写操作添加 `@Transactional(rollbackFor = Exception.class)`
2. 复杂业务考虑编程式事务
3. 注意事务边界

### 步骤10：异常处理

**目的**：统一异常处理。

**操作**：

1. 使用项目定义的异常类
2. 业务异常抛出 `BusinessException`
3. 参数校验异常自动处理

### 步骤11：日志记录

**目的**：便于问题排查。

**操作**：

1. 关键操作添加INFO日志
2. 异常添加ERROR日志
3. 调试信息添加DEBUG日志

### 步骤12：单元测试

**目的**：确保代码质量。

**操作**：

1. 编写Service层单元测试
2. 覆盖正常场景和异常场景
3. 使用Mockito模拟依赖

### 步骤13：创建GitHub Project工作项

**目的**：按照团队协作规范，更新任务状态。

**操作**：

```bash
# 更新任务状态
gh issue edit <issue-number> --add-label "Done"

# 或创建新的缺陷任务
gh issue create --title "[Bug] {描述}" --body "## 缺陷描述\n..." --label "BE,Bug"
```

### 步骤14：生成开发摘要

**目的**：记录开发过程，便于追溯。

**操作**：

1. 更新模块的开发摘要
2. 记录：
   - 本次完成的接口
   - 遇到的问题及解决方案
   - 性能优化项
   - 待确认事项

---

## 五、业界优秀实践

### 5.1 Java后端最佳实践

**1. 面向对象设计**
- 合理使用继承和组合
- 依赖注入解耦
- 接口分离原则

**2. 异常处理**
- 业务异常与系统异常分离
- 异常链保留完整堆栈
- 异常信息对用户友好

**3. 日志规范**
- 分级使用日志（ERROR/WARN/INFO/DEBUG）
- 日志包含业务上下文
- 敏感数据脱敏

### 5.2 金融系统特殊要求

**1. 资金精确性**
- 金额计算使用BigDecimal
- 避免浮点数运算
- 金额字段DECIMAL(18,2)

**2. 事务一致性**
- 强一致性事务
- 分布式事务考虑Seata
- 幂等性设计

**3. 审计追溯**
- 完整操作日志
- 变更前后值记录
- 操作人追踪

---

## 六、与其他Skill的衔接

### 6.1 前置依赖

| 前置Skill | 依赖内容 | 说明 |
|-----------|----------|------|
| 数据库设计 | 数据表结构 | 明确Entity映射 |
| API接口设计 | 接口文档 | 明确接口定义 |

### 6.2 后续触发

| 后续Skill | 触发条件 | 输出 |
|-----------|----------|------|
| 前端开发 | API接口可用 | 前端调用 |
| 测试用例设计 | 接口开发完成 | 测试用例 |
| 代码审查 | 代码提交 | Review意见 |

### 6.3 协作流程

```
数据库设计 ──▶ 后端开发 ──▶ 前端开发
                  │
                  ▼
              代码审查
                  │
                  ▼
              测试用例设计
```

---

## 七、质量标准

### 7.1 代码质量检查点

| 检查项 | 标准 | 权重 |
|--------|------|------|
| 规范符合性 | 符合项目命名和结构规范 | 20% |
| 功能正确性 | 实现所有接口需求 | 25% |
| 事务正确性 | 数据一致性保证 | 20% |
| 异常处理 | 完善的异常处理 | 15% |
| 日志规范 | 关键节点日志记录 | 10% |
| 单元测试 | 核心逻辑覆盖 | 10% |

### 7.2 评审通过标准

- [ ] 所有API接口已实现
- [ ] 数据一致性保证
- [ ] 异常处理完善
- [ ] 日志记录规范
- [ ] 单元测试通过

---

## 八、交付物检查清单

### 8.1 代码检查

- [ ] Entity类正确映射表
- [ ] DTO添加校验注解
- [ ] Service实现业务逻辑
- [ ] Controller实现接口定义

### 8.2 功能检查

- [ ] CRUD接口正常
- [ ] 分页查询正常
- [ ] 事务一致性正确
- [ ] 异常处理正确

### 8.3 规范检查

- [ ] 类命名符合规范
- [ ] 方法命名符合规范
- [ ] 包结构符合规范
- [ ] 日志记录规范

### 8.4 GitHub状态更新

- [ ] 任务状态已更新
- [ ] 开发摘要已记录

---

## 九、附录

### 附录A：命名规范

**类命名**
```
{Entity}Controller.java    # JAX-RS Resource
{Entity}Service.java      # Service接口
{Entity}ServiceImpl.java  # Service实现
{Entity}Mapper.java       # Mapper接口
{Entity}DTO.java         # 数据传输对象
{Entity}VO.java          # 视图对象
{Entity}Entity.java      # 实体类
```

**方法命名**
```
# 查询
getById()           # 按ID查询
listAll()           # 查询所有
queryPage()         # 分页查询

# 保存
save()              # 新增
update()            # 更新

# 删除
delete()           # 删除
removeById()       # 按ID删除

# 业务
submit()           # 提交
approve()          # 审批通过
reject()           # 审批拒绝
```

### 附录B：注解使用

```java
// Entity
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tms_xxx_t")

// DTO
@Data
public class xxxDTO {
    @NotBlank(message = "编码不能为空")
    private String code;
}

// Controller
@Path("/api/v1/xxx")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)

// Service
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
```

### 附录C：Result响应

```java
// 成功响应
Result.success(data)
Result.success()

// 失败响应
Result.error(message)
Result.badRequest(message)
Result.notFound(message)
```

---

## 版本历史

| 版本 | 日期 | 变更内容 |
|------|------|---------|
| v1.0 | YYYY-MM-DD | 初始版本 |

---

## 附录D：服务管理脚本

### 后端服务管理

**脚本位置**: `.agents/skills/opentms-backend-dev/scripts/run_backend.py`

**功能**:
- 启动/停止/重启后端Spring Boot服务
- 自动构建（如JAR不存在）
- 检查后端运行状态

**使用方法**:
```bash
# 启动后端 (http://localhost:8081)
python .agents/skills/opentms-backend-dev/scripts/run_backend.py start

# 停止后端
python .agents/skills/opentms-backend-dev/scripts/run_backend.py stop

# 重启后端
python .agents/skills/opentms-backend-dev/scripts/run_backend.py restart

# 检查状态
python .agents/skills/opentms-backend-dev/scripts/run_backend.py status
```

**依赖**:
- Java 17+ 必须已安装
- Maven 必须已安装
- 基于数据模块 (basedata/) 已构建

**端口**: 8081 (默认)

**模块端口映射**:
| 模块 | 端口 |
|------|------|
| basedata | 8081 |
| dealing | 8082 |
| bankaccount | 8083 |
| instrument | 8084 |
| fundplan | 8085 |
| cashpool | 8086 |
| settlement | 8087 |
| limit | 8088 |
| fx | 8089 |
| irs | 8090 |
| valuation | 8091 |
| exposure | 8092 |
| hedge | 8093 |
| impairment | 8094 |
| var | 8095 |
| cockpit | 8096 |
| report | 8097 |
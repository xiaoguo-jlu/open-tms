---
name: opentms-basedata-backend-develop
description: Use when developing backend code for Open-TMS basedata module (currency, country, bank, counterparty, trader, business unit,currency pair, rate, management entity), including entity design, service implementation, REST API development, or code review
---

# Open-TMS 基础数据模块后端开发规范

## Overview

本规范为 Open-TMS 企业资金管理系统的基础数据模块（币种、国家、银行、交易对手、业务单元、交易员等）提供后端代码开发指导。基于 Spring Boot 3 + MyBatis Plus + PostgreSQL 技术栈，遵循阿里巴巴开发手册和 Open-TMS 开发规范。

## When to Use

**触发场景：**
- 开发新的基础数据类型（国家、节假日、银行等）
- 创建基础数据的增删改查接口
- 编写基础数据 Service 业务逻辑
- 设计基础数据实体与数据库表映射
- 代码审查基础数据模块

**症状判断：**
- 需要新增基础数据实体
- 不确定各层代码的文件命名规范
- 不知道如何处理业务校验
- 不了解分页查询实现方式
- 缺少接口幂等性设计

**不适用：**
- 交易模块开发（见 opentms-dealing-backend）
- 估值计算模块（见 opentms-valuation-backend）
- 前端代码开发

---

## 一、项目结构

### 1.1 模块目录

```
basedata/src/main/java/com/opentms/basedata/
├── BasedataApplication.java          # 启动类
├── config/                           # 配置类
│   └── GlobalExceptionHandler.java  # 全局异常处理
├── controller/                       # 接口层
│   ├── CurrencyController.java
│   ├── CountryController.java
│   ├── BankController.java
│   └── ...
├── service/                          # 服务层
│   ├── CurrencyService.java         # 接口
│   └── impl/
│       ├── CurrencyServiceImpl.java # 实现
│       └── BaseServiceImpl.java     # 基础实现
├── mapper/                           # 数据访问层
│   └── CurrencyMapper.java
├── entity/                           # 实体类
│   └── Currency.java
├── dto/                              # 数据传输对象
│   └── CurrencyDTO.java
└── vo/                               # 视图对象
    └── CurrencyVO.java
```

---

## 二、Entity 层规范

### 2.1 实体类模板

```java
package com.opentms.basedata.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 币种表实体
 * 表名: tms_currency_t
 */
@Data
@TableName("tms_currency_t")
public class Currency {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String code;              // 币种代码 (唯一)
    private String name;              // 币种名称
    private String symbol;            // 货币符号
    private Integer decimalPlaces;   // 小数位数
    private String status;           // 状态 (1=启用, 0=禁用)
    private String remark;           // 备注

    // 审计字段（必需）
    private String createdBy;         // 创建人
    private LocalDateTime createdAt;  // 创建时间
    private String updatedBy;         // 更新人
    private LocalDateTime updatedAt; // 更新时间
    private Integer version;         // 乐观锁版本
    private String deleted;           // 软删除标记
}
```

### 2.2 字段命名映射

| 数据库字段 | Java字段 | 类型 |
|------------|----------|------|
| id | id | Long |
| code | code | String |
| name | name | String |
| created_by | createdBy | String |
| created_at | createdAt | LocalDateTime |
| decimal_places | decimalPlaces | Integer |
| version | version | Integer |

### 2.3 注解使用

```java
// 表名映射
@TableName("tms_currency_t")

// 主键自增
@TableId(type = IdType.AUTO)

// 逻辑删除（需配置MyBatis Plus逻辑删除插件）
@TableLogic
private String deleted;
```

---

## 三、DTO 层规范

### 3.1 DTO 模板

```java
package com.opentms.basedata.dto;

import lombok.Data;

/**
 * 币种DTO（用于新增/更新）
 */
@Data
public class CurrencyDTO {

    private Long id;                  // ID（更新时必填）

    private String code;              // 币种代码 (新增必填)
    private String name;              // 币种名称 (必填)
    private String symbol;            // 货币符号
    private Integer decimalPlaces;    // 小数位数 (默认2)
    private String status;            // 状态
    private String remark;            // 备注
}
```

### 3.2 校验注解

```java
package com.opentms.basedata.dto;

import lombok.Data;
import jakarta.validation.constraints.*;

@Data
public class CurrencyDTO {

    @NotBlank(message = "币种代码不能为空")
    @Size(max = 10, message = "币种代码长度不能超过10位")
    private String code;

    @NotBlank(message = "币种名称不能为空")
    @Size(max = 50, message = "币种名称长度不能超过50位")
    private String name;

    @Size(max = 10, message = "货币符号长度不能超过10位")
    private String symbol;

    @Range(min = 0, max = 6, message = "小数位数范围0-6")
    private Integer decimalPlaces;

    @Pattern(regexp = "^[01]$", message = "状态只能是0或1")
    private String status;

    @Size(max = 500, message = "备注长度不能超过500位")
    private String remark;
}
```

---

## 四、VO 层规范

### 4.1 VO 模板

```java
package com.opentms.basedata.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 币种VO（用于接口返回）
 */
@Data
public class CurrencyVO {

    private Long id;
    private String code;
    private String name;
    private String symbol;
    private Integer decimalPlaces;
    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
```

### 4.2 脱敏处理（可选）

```java
// 账户号脱敏
@SensitiveInfo(SensitiveType.BANK_ACCOUNT)
private String accountNumber;
```

---

## 五、Mapper 层规范

### 5.1 Mapper 模板

```java
package com.opentms.basedata.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.opentms.basedata.entity.Currency;
import org.apache.ibatis.annotations.Mapper;

/**
 * 币种Mapper
 */
@Mapper
public interface CurrencyMapper extends BaseMapper<Currency> {
    // 自定义方法写在此处
}
```

### 5.2 常用方法

| 方法 | 说明 |
|------|------|
| selectById(id) | 按ID查询 |
| selectOne(wrapper) | 单条查询 |
| selectList(wrapper) | 列表查询 |
| selectPage(page, wrapper) | 分页查询 |
| insert(entity) | 插入 |
| updateById(entity) | 更新 |
| deleteById(id) | 删除 |

---

## 六、Service 层规范

### 6.1 Service 接口模板

```java
package com.opentms.basedata.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.opentms.basedata.dto.CurrencyDTO;
import com.opentms.basedata.vo.CurrencyVO;

import java.util.List;

/**
 * 币种Service接口
 */
public interface CurrencyService {

    /**
     * 查询所有启用状态的币种
     */
    List<CurrencyVO> listAll();

    /**
     * 分页查询
     */
    Page<CurrencyVO> queryPage(String keyword, String status, int pageNo, int pageSize);

    /**
     * 根据ID查询
     */
    CurrencyVO getById(Long id);

    /**
     * 根据代码查询
     */
    CurrencyVO getByCode(String code);

    /**
     * 新增
     */
    CurrencyVO save(CurrencyDTO dto);

    /**
     * 更新
     */
    CurrencyVO updateById(CurrencyDTO dto);

    /**
     * 删除
     */
    void removeById(Long id);
}
```

### 6.2 ServiceImpl 实现模板

```java
package com.opentms.basedata.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.opentms.basedata.dto.CurrencyDTO;
import com.opentms.basedata.entity.Currency;
import com.opentms.basedata.mapper.CurrencyMapper;
import com.opentms.basedata.service.CurrencyService;
import com.opentms.basedata.vo.CurrencyVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 币种Service实现
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CurrencyServiceImpl implements CurrencyService {

    private final CurrencyMapper currencyMapper;

    @Override
    public List<CurrencyVO> listAll() {
        LambdaQueryWrapper<Currency> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Currency::getStatus, "1")
               .orderByAsc(Currency::getCode);
        return currencyMapper.selectList(wrapper).stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public Page<CurrencyVO> queryPage(String keyword, String status, int pageNo, int pageSize) {
        LambdaQueryWrapper<Currency> wrapper = new LambdaQueryWrapper<>();

        // 关键字查询（代码或名称模糊匹配）
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Currency::getCode, keyword)
                   .or()
                   .like(Currency::getName, keyword);
        }

        // 状态过滤
        if (StringUtils.hasText(status)) {
            wrapper.eq(Currency::getStatus, status);
        }

        wrapper.orderByDesc(Currency::getCreatedAt);

        Page<Currency> page = currencyMapper.selectPage(
            new Page<>(pageNo, pageSize), wrapper);
        
        // 转换为VO
        Page<CurrencyVO> result = new Page<>();
        result.setCurrent(page.getCurrent());
        result.setSize(page.getSize());
        result.setTotal(page.getTotal());
        result.setRecords(page.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList()));
        
        return result;
    }

    @Override
    public CurrencyVO getById(Long id) {
        Currency entity = currencyMapper.selectById(id);
        return entity != null ? convertToVO(entity) : null;
    }

    @Override
    public CurrencyVO getByCode(String code) {
        LambdaQueryWrapper<Currency> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Currency::getCode, code);
        Currency entity = currencyMapper.selectOne(wrapper);
        return entity != null ? convertToVO(entity) : null;
    }

    @Override
    public CurrencyVO save(CurrencyDTO dto) {
        log.info("[新增币种] code={}", dto.getCode());
        
        // 业务校验：代码唯一性
        Currency exist = getByCode(dto.getCode());
        if (exist != null) {
            throw new BusinessException("币种代码已存在: " + dto.getCode());
        }
        
        Currency entity = convertToEntity(dto);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setCreatedBy(getCurrentUser());  // 获取当前用户
        entity.setStatus("1");
        
        currencyMapper.insert(entity);
        
        log.info("[新增币种] success id={}", entity.getId());
        return convertToVO(entity);
    }

    @Override
    public CurrencyVO updateById(CurrencyDTO dto) {
        log.info("[更新币种] id={}", dto.getId());
        
        Currency entity = currencyMapper.selectById(dto.getId());
        if (entity == null) {
            throw new BusinessException("币种不存在");
        }
        
        // 更新字段
        entity.setName(dto.getName());
        entity.setSymbol(dto.getSymbol());
        entity.setDecimalPlaces(dto.getDecimalPlaces());
        entity.setStatus(dto.getStatus());
        entity.setRemark(dto.getRemark());
        entity.setUpdatedAt(LocalDateTime.now());
        entity.setUpdatedBy(getCurrentUser());
        entity.setVersion(entity.getVersion() + 1);  // 乐观锁
        
        currencyMapper.updateById(entity);
        
        log.info("[更新币种] success id={}", entity.getId());
        return convertToVO(entity);
    }

    @Override
    public void removeById(Long id) {
        log.info("[删除币种] id={}", id);
        
        Currency entity = currencyMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("币种不存在");
        }
        
        // 逻辑删除
        entity.setDeleted("1");
        entity.setUpdatedAt(LocalDateTime.now());
        currencyMapper.updateById(entity);
        
        log.info("[删除币种] success id={}", id);
    }

    // ========== 私有方法 ==========

    private CurrencyVO convertToVO(Currency entity) {
        CurrencyVO vo = new CurrencyVO();
        vo.setId(entity.getId());
        vo.setCode(entity.getCode());
        vo.setName(entity.getName());
        vo.setSymbol(entity.getSymbol());
        vo.setDecimalPlaces(entity.getDecimalPlaces());
        vo.setStatus(entity.getStatus());
        vo.setCreatedAt(entity.getCreatedAt());
        vo.setUpdatedAt(entity.getUpdatedAt());
        return vo;
    }

    private Currency convertToEntity(CurrencyDTO dto) {
        Currency entity = new Currency();
        entity.setId(dto.getId());
        entity.setCode(dto.getCode());
        entity.setName(dto.getName());
        entity.setSymbol(dto.getSymbol());
        entity.setDecimalPlaces(dto.getDecimalPlaces());
        entity.setStatus(dto.getStatus());
        entity.setRemark(dto.getRemark());
        return entity;
    }

    private String getCurrentUser() {
        // 从SecurityContext获取当前用户
        // TODO: 实现用户上下文获取
        return "system";
    }

    /**
     * 业务异常
     */
    public static class BusinessException extends RuntimeException {
        public BusinessException(String message) {
            super(message);
        }
    }
}
```

### 6.3 ⚠️ 重要：避免Java泛型类型擦除导致MyBatis Plus lambda查询失败

**问题现象**：
```
can not find lambda cache for this entity [com.opentms.basedata.entity.BasedataEntity]
```

**根因分析**：
- Java泛型类型擦除：`<T extends BasedataEntity>` 在运行时变为 `BasedataEntity`
- MyBatis Plus的lambda缓存基于具体类生成：`entity.getClass()` 返回的是 `Country.class`
- 当代码使用 `T::getCreatedAt` 时，MyBatis尝试从 `BasedataEntity.class` 查找lambda缓存，但该类是抽象类，无法生成缓存

**错误示例（会导致MyBatis Plus lambda缓存找不到）：**
```java
// 错误：使用泛型T的lambda方法引用
public abstract class BasedataServiceImpl<M extends BaseMapper<T>, T extends BasedataEntity> {
    public IPage<V> queryPage(D dto, int pageNum, int pageSize) {
        LambdaQueryWrapper<T> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(T::getCreatedAt);  // 运行时T被擦除为BasedataEntity
    }
}

// 子类继承 - 仍有问题！因为父类的泛型在运行时被擦除
public class CountryServiceImpl extends BasedataServiceImpl<CountryMapper, Country> {
}
```

**正确示例：**
```java
// 正确：每个ServiceImpl直接继承ServiceImpl，使用具体实体类
public class CountryServiceImpl extends ServiceImpl<CountryMapper, Country> implements CountryService {
    // lambda使用具体的Country类，如Country::getCode, Country::getCreatedAt
}

public class TraderServiceImpl extends ServiceImpl<TraderMapper, Trader> implements TraderService {
}

public class CounterpartyServiceImpl extends ServiceImpl<CounterpartyMapper, Counterparty> implements CounterpartyService {
}
```

**关键原则**：
1. **不要使用泛型基类封装MyBatis Plus的lambda查询**
2. **每个ServiceImpl直接继承`ServiceImpl<Mapper, Entity>`**
3. **Lambda方法引用必须使用具体类，如`Country::getCode`，不能使用`T::getCode`**
4. **Service接口使用具体参数类型，不要使用泛型DTO**

---

## 七、Controller 层规范

### 7.1 Controller 模板

**⚠️ 重要：JAX-RS注解规范**
- 使用 `@Path`, `@GET`, `@POST` 等JAX-RS注解（jakarta.ws.rs包）
- 所有接收JSON请求的方法必须添加 `@Consumes(MediaType.APPLICATION_JSON)`
- 响应JSON必须添加 `@Produces(MediaType.APPLICATION_JSON)`

```java
package com.opentms.basedata.controller;

import com.opentms.basedata.dto.CurrencyDTO;
import com.opentms.basedata.service.CurrencyService;
import com.opentms.basedata.vo.CurrencyVO;
import com.opentms.common.model.Result;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Path("/api/v1/currencies")
@Produces(MediaType.APPLICATION_JSON)
public class CurrencyResource {

    @Autowired
    private CurrencyService currencyService;

    /**
     * 查询所有（无分页，用于下拉选择）
     */
    @GET
    public Object list() {
        return Result.success(currencyService.listAll());
    }

    /**
     * 分页查询
     */
    @GET
    @Path("/page")
    public Object page(
            @QueryParam("keyword") String keyword,
            @QueryParam("status") String status,
            @QueryParam("pageNum") @DefaultValue("1") int pageNum,
            @QueryParam("pageSize") @DefaultValue("20") int pageSize) {
        return Result.success(currencyService.queryPage(keyword, status, pageNo, pageSize));
    }

    /**
     * 根据ID查询
     */
    @GET
    @Path("/{id}")
    public Object getById(@PathParam("id") String id) {
        try {
            long parseId = Long.parseLong(id);
            if (parseId <= 0) {
                return Result.badRequest("ID必须为正整数");
            }
            CurrencyVO vo = currencyService.getById(parseId);
            return vo != null ? Result.success(vo) : Result.notFound("币种不存在");
        } catch (NumberFormatException e) {
            return Result.badRequest("ID参数格式不正确");
        }
    }

    /**
     * 根据代码查询
     */
    @GET
    @Path("/code/{code}")
    public Object getByCode(@PathParam("code") String code) {
        CurrencyVO vo = currencyService.getByCode(code);
        return vo != null ? Result.success(vo) : Result.notFound("币种不存在");
    }

    /**
     * 新增
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Object save(CurrencyDTO dto) {
        try {
            return Result.success(currencyService.save(dto));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新（使用POST方法，路径 /update 区分）
     */
    @POST
    @Path("/update")
    @Consumes(MediaType.APPLICATION_JSON)
    public Object update(CurrencyDTO dto) {
        try {
            if (dto.getId() == null) {
                return Result.badRequest("ID不能为空");
            }
            return Result.success(currencyService.updateById(dto));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除（使用POST方法，路径 /delete/{id} 区分）
     */
    @POST
    @Path("/delete/{id}")
    public Object delete(@PathParam("id") String id) {
        try {
            long parseId = Long.parseLong(id);
            if (parseId <= 0) {
                return Result.badRequest("ID必须为正整数");
            }
            currencyService.removeById(parseId);
            return Result.success();
        } catch (NumberFormatException e) {
            return Result.badRequest("ID参数格式不正确");
        }
    }
}
```

### 7.2 HTTP方法规范

**⚠️ 重要：所有修改、删除操作统一使用POST方法**

| 操作 | HTTP方法 | URL | 说明 |
|------|----------|-----|------|
| 查询所有 | GET | /api/v1/currencies | 无分页 |
| 分页查询 | GET | /api/v1/currencies/page | 带分页参数 |
| 根据ID查询 | GET | /api/v1/currencies/{id} | - |
| 根据代码查询 | GET | /api/v1/currencies/code/{code} | - |
| 新增 | POST | /api/v1/currencies | JSON body |
| 更新 | POST | /api/v1/currencies/update | JSON body |
| 删除 | POST | /api/v1/currencies/delete/{id} | - |

---

## 八、异常处理

### 8.1 全局异常处理器

```java
package com.opentms.basedata.config;

import com.opentms.common.model.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 全局异常处理
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 业务异常
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.warn("业务异常: {}", e.getMessage());
        return Result.error(400, e.getMessage());
    }

    /**
     * 参数校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("参数校验失败: {}", message);
        return Result.badRequest(message);
    }

    /**
     * 绑定异常
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleBindException(BindException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("参数绑定失败: {}", message);
        return Result.badRequest(message);
    }

    /**
     * 未知异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.error(500, "系统异常，请稍后重试");
    }
}
```

---

## 九、日志规范

### 9.1 操作日志

```java
log.info("[新增币种] code={}, name={}", dto.getCode(), dto.getName());
log.info("[更新币种] id={}, fields={}", dto.getId(), updateFields);
log.info("[删除币种] id={}", id);
```

### 9.2 业务日志

```java
log.info("交易[{}] 提交审批, 金额[{}] {}", 
    transactionNo, amount, currencyCode);
```

### 9.3 异常日志

```java
log.error("查询币种失败, id={}, error={}", id, e.getMessage(), e);
```

---

## 十、代码审查检查清单

### 10.1 Entity 层检查

| 检查项 | 要求 |
|--------|------|
| 表名映射 | @TableName 注解正确 |
| 主键 | @TableId 注解正确 |
| 字段命名 | 驼峰命名，与数据库下划线对应 |
| 审计字段 | 包含 createdBy/at, updatedBy/at, version, deleted |

### 10.2 DTO 层检查

| 检查项 | 要求 |
|--------|------|
| 校验注解 | 必填字段有 @NotBlank/@NotNull |
| 长度限制 | @Size 限制字符串长度 |
| 格式校验 | @Pattern 校验格式 |
| 无ID字段 | 新增时ID由数据库生成 |

### 10.3 Service 层检查

| 检查项 | 要求 |
|--------|------|
| 业务校验 | 新增校验代码唯一性 |
| 乐观锁 | 更新时 version + 1 |
| 日志记录 | 关键操作有日志 |
| 异常抛出 | 业务异常抛出明确信息 |

### 10.4 Controller 层检查

| 检查项 | 要求 |
|--------|------|
| URL规范 | 使用 /api/v1/ 前缀 |
| 请求方法 | GET/POST/PUT/DELETE 正确 |
| 参数校验 | @Validated 注解 |
| 路径变量 | @PathVariable 正确标注 |
| 返回类型 | Result<T> 统一包装 |

---

## 十一、完整示例：新增"银行"模块

### 11.1 数据库表

```sql
-- 银行表
CREATE TABLE tms_bank_t (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    en_name VARCHAR(200),
    swift_code VARCHAR(11),
    country_code VARCHAR(10),
    status CHAR(1) DEFAULT '1',
    remark VARCHAR(500),
    created_by VARCHAR(50) DEFAULT 'system',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP,
    version INT DEFAULT 0,
    deleted CHAR(1) DEFAULT '0'
);
```

### 11.2 生成的代码文件

| 层级 | 文件 |
|------|------|
| Entity | Bank.java |
| DTO | BankDTO.java |
| VO | BankVO.java |
| Mapper | BankMapper.java |
| Service | BankService.java |
| ServiceImpl | BankServiceImpl.java |
| Controller | BankController.java |

---

## 十二、相关规范

- [Open-TMS 开发规范文档](../docs/规范/Open-TMS开发规范文档.md)
- [Open-TMS 表结构设计规范](./opentms-table-design)
- [阿里巴巴 Java 开发手册](https://github.com/alibaba/AlibabaJavaCodingGuidelines)

---

**核心原则：基础数据模块代码必须遵循分层清晰、职责明确、异常统一、日志完整的原则。所有接口返回统一使用 Result 包装，分页接口使用 Page 包装。**
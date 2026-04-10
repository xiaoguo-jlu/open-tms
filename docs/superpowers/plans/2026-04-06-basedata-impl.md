# 基础数据模块 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现基础数据模块的完整后端代码，包含实体抽象、通用Service/Controller和8个子模块的完整CRUD功能

**Architecture:** 使用继承和多态实现实体、服务、控制的抽象。BaseEntity包含id/code/name/status/审计字段；BaseService包含通用CRUD/导入导出方法；BaseController包含通用RESTful接口。各子模块只需定义特有字段并继承父类方法。

**Tech Stack:** Spring Boot, MyBatis-Plus, PostgreSQL

---

## 文件结构

```
common/src/main/java/com/opentms/common/
├── model/
│   ├── BaseEntity.java              # 基础实体（已有，扩展）
│   ├── BaseCodeEntity.java          # 新增：含code/name的实体
│   ├── BaseIdEntity.java            # 新增：含id的实体
│   └── Result.java                  # 已有
├── constant/
│   └── GlobalConstants.java         # 新增：全局常量
└── config/
    └── MybatisPlusConfig.java       # 已有

basedata/src/main/java/com/opentms/basedata/
├── entity/
│   ├── Currency.java                # 已有
│   ├── BusinessUnit.java            # 新增
│   ├── Trader.java                  # 新增
│   ├── Country.java                 # 新增
│   ├── Holiday.java                 # 新增
│   ├── Bank.java                    # 新增
│   ├── Counterparty.java            # 新增
│   └── CounterpartyAccount.java     # 新增
├── dto/
│   ├── CurrencyDTO.java             # 新增
│   ├── BusinessUnitDTO.java         # 新增
│   └── ...
├── vo/
│   ├── CurrencyVO.java              # 新增
│   ├── BusinessUnitVO.java          # 新增
│   └── ...
├── mapper/
│   ├── CurrencyMapper.java          # 已有
│   ├── BusinessUnitMapper.java       # 新增
│   └── ...
├── service/
│   ├── CurrencyService.java         # 已有
│   ├── BaseService.java             # 新增：抽象基类
│   ├── BusinessUnitService.java     # 新增
│   └── ...
├── service/impl/
│   ├── CurrencyServiceImpl.java     # 已有
│   ├── BaseServiceImpl.java         # 新增：抽象实现
│   ├── BusinessUnitServiceImpl.java # 新增
│   └── ...
├── controller/
│   ├── CurrencyController.java      # 已有
│   ├── BaseController.java          # 新增：抽象基类
│   ├── BusinessUnitController.java  # 新增
│   └── ...
└── handler/
    └── IdHandler.java               # 新增：脱敏处理器

docs/api/                             # 已有接口文档
```

---

## Task 1: 扩展BaseEntity并创建BaseCodeEntity

**Files:**
- Modify: `common/src/main/java/com/opentms/common/model/BaseEntity.java`
- Create: `common/src/main/java/com/opentms/common/model/BaseCodeEntity.java`

- [ ] **Step 1: 扩展BaseEntity，添加id/status/deleted/version字段**

```java
// common/src/main/java/com/opentms/common/model/BaseEntity.java
package com.opentms.common.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField(fill = FieldFill.INSERT)
    private String createdBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updatedBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @Version
    private Integer version;

    @TableLogic
    private String deleted;
}
```

- [ ] **Step 2: 创建BaseCodeEntity，添加code/name/status字段**

```java
// common/src/main/java/com/opentms/common/model/BaseCodeEntity.java
package com.opentms.common.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class BaseCodeEntity extends BaseEntity {

    private String code;

    private String name;

    private String status;
}
```

- [ ] **Step 3: Commit**

```bash
git add common/src/main/java/com/opentms/common/model/BaseEntity.java common/src/main/java/com/opentms/common/model/BaseCodeEntity.java
git commit -m "refactor: 扩展BaseEntity添加审计字段，创建BaseCodeEntity"
```

---

## Task 2: 创建全局常量和通用DTO/VO

**Files:**
- Create: `common/src/main/java/com/opentms/common/constant/GlobalConstants.java`
- Create: `basedata/src/main/java/com/opentms/basedata/dto/BaseDTO.java`
- Create: `basedata/src/main/java/com/opentms/basedata/vo/BaseVO.java`

- [ ] **Step 1: 创建GlobalConstants**

```java
// common/src/main/java/com/opentms/common/constant/GlobalConstants.java
package com.opentms.common.constant;

public class GlobalConstants {

    public static final String STATUS_ENABLED = "1";
    public static final String STATUS_DISABLED = "0";

    public static final String YES = "1";
    public static final String NO = "0";

    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;
}
```

- [ ] **Step 2: 创建BaseDTO**

```java
// basedata/src/main/java/com/opentms/basedata/dto/BaseDTO.java
package com.opentms.basedata.dto;

import lombok.Data;

@Data
public class BaseDTO {

    private Long id;

    private String code;

    private String name;

    private String status;
}
```

- [ ] **Step 3: 创建BaseVO**

```java
// basedata/src/main/java/com/opentms/basedata/vo/BaseVO.java
package com.opentms.basedata.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class BaseVO {

    private Long id;

    private String code;

    private String name;

    private String status;

    private String createdBy;

    private LocalDateTime createdAt;

    private String updatedBy;

    private LocalDateTime updatedAt;
}
```

- [ ] **Step 4: Commit**

```bash
git add common/src/main/java/com/opentms/common/constant/GlobalConstants.java basedata/src/main/java/com/opentms/basedata/dto/BaseDTO.java basedata/src/main/java/com/opentms/basedata/vo/BaseVO.java
git commit -m "feat: 添加全局常量和通用DTO/VO"
```

---

## Task 3: 创建抽象BaseService和BaseServiceImpl

**Files:**
- Create: `basedata/src/main/java/com/opentms/basedata/service/BaseService.java`
- Create: `basedata/src/main/java/com/opentms/basedata/service/impl/BaseServiceImpl.java`

- [ ] **Step 1: 创建BaseService接口**

```java
// basedata/src/main/java/com/opentms/basedata/service/BaseService.java
package com.opentms.basedata.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.opentms.basedata.dto.BaseDTO;
import com.opentms.basedata.vo.BaseVO;

import java.util.List;

public interface BaseService<T extends com.opentms.common.model.BaseCodeEntity, D extends BaseDTO, V extends BaseVO> extends IService<T> {

    IPage<V> queryPage(D dto, int pageNum, int pageSize);

    V getById(Long id);

    boolean save(D dto);

    boolean update(D dto);

    boolean delete(Long id);

    boolean batchDelete(List<Long> ids);

    List<V> listAll();
}
```

- [ ] **Step 2: 创建BaseServiceImpl抽象类**

```java
// basedata/src/main/java/com/opentms/basedata/service/impl/BaseServiceImpl.java
package com.opentms.basedata.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.opentms.basedata.dto.BaseDTO;
import com.opentms.basedata.entity.Currency;
import com.opentms.basedata.mapper.CurrencyMapper;
import com.opentms.basedata.service.CurrencyService;
import com.opentms.basedata.vo.BaseVO;
import org.springframework.beans.BeanUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseServiceImpl<M extends CurrencyMapper, T extends Currency, D extends BaseDTO, V extends BaseVO> 
        extends ServiceImpl<M, T> implements BaseService<T, D, V> {

    @Override
    public IPage<V> queryPage(D dto, int pageNum, int pageSize) {
        Page<T> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<T> wrapper = buildQueryWrapper(dto);
        IPage<T> result = page(page, wrapper);
        return convertPage(result);
    }

    @Override
    public V getById(Long id) {
        T entity = super.getById(id);
        return entity == null ? null : convertToVO(entity);
    }

    @Override
    public boolean save(D dto) {
        T entity = convertToEntity(dto);
        return super.save(entity);
    }

    @Override
    public boolean update(D dto) {
        T entity = convertToEntity(dto);
        return super.updateById(entity);
    }

    @Override
    public boolean delete(Long id) {
        return super.removeById(id);
    }

    @Override
    public boolean batchDelete(List<Long> ids) {
        return super.removeByIds(ids);
    }

    @Override
    public List<V> listAll() {
        List<T> list = super.list();
        return convertList(list);
    }

    protected LambdaQueryWrapper<T> buildQueryWrapper(D dto) {
        LambdaQueryWrapper<T> wrapper = new LambdaQueryWrapper<>();
        if (dto != null) {
            if (StringUtils.hasText(dto.getKeyword())) {
                wrapper.like(getEntityClass().getDeclaredField("code") != null ? 
                    getCodeField() : null, dto.getKeyword())
                    .or()
                    .like(getNameField(), dto.getKeyword());
            }
            if (StringUtils.hasText(dto.getStatus())) {
                wrapper.eq(getStatusField(), dto.getStatus());
            }
        }
        wrapper.orderByDesc(getCreatedAtField());
        return wrapper;
    }

    protected IPage<V> convertPage(IPage<T> page) {
        IPage<V> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(convertList(page.getRecords()));
        return voPage;
    }

    protected List<V> convertList(List<T> list) {
        List<V> result = new ArrayList<>();
        if (list != null) {
            for (T entity : list) {
                result.add(convertToVO(entity));
            }
        }
        return result;
    }

    protected V convertToVO(T entity) {
        if (entity == null) {
            return null;
        }
        try {
            V vo = getVOClass().getDeclaredConstructor().newInstance();
            BeanUtils.copyProperties(entity, vo);
            return vo;
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert entity to VO", e);
        }
    }

    protected T convertToEntity(D dto) {
        if (dto == null) {
            return null;
        }
        try {
            T entity = getEntityClass().getDeclaredConstructor().newInstance();
            BeanUtils.copyProperties(dto, entity);
            return entity;
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert DTO to entity", e);
        }
    }

    protected abstract Class<T> getEntityClass();

    protected abstract Class<V> getVOClass();

    protected abstract java.lang.reflect.Field getCodeField();

    protected abstract java.lang.reflect.Field getNameField();

    protected abstract java.lang.reflect.Field getStatusField();

    protected abstract java.lang.reflect.Field getCreatedAtField();
}
```

- [ ] **Step 3: Commit**

```bash
git add basedata/src/main/java/com/opentms/basedata/service/BaseService.java basedata/src/main/java/com/opentms/basedata/service/impl/BaseServiceImpl.java
git commit -m "feat: 创建抽象BaseService和BaseServiceImpl"
```

---

## Task 4: 创建抽象BaseController

**Files:**
- Create: `basedata/src/main/java/com/opentms/basedata/controller/BaseController.java`

- [ ] **Step 1: 创建BaseController**

```java
// basedata/src/main/java/com/opentms/basedata/controller/BaseController.java
package com.opentms.basedata.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.opentms.basedata.dto.BaseDTO;
import com.opentms.basedata.service.BaseService;
import com.opentms.basedata.vo.BaseVO;
import com.opentms.common.model.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public abstract class BaseController<T extends com.opentms.common.model.BaseCodeEntity, D extends BaseDTO, V extends BaseVO> {

    protected abstract BaseService<T, D, V> getService();

    @GetMapping("/page")
    public Result<IPage<V>> page(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "20") int pageSize) {
        D dto = createDTO();
        dto.setKeyword(keyword);
        dto.setStatus(status);
        IPage<V> page = getService().queryPage(dto, pageNo, pageSize);
        return Result.success(page);
    }

    @GetMapping("/{id}")
    public Result<V> getById(@PathVariable Long id) {
        V vo = getService().getById(id);
        return vo == null ? Result.notFound("Not found") : Result.success(vo);
    }

    @PostMapping
    public Result<Void> save(@RequestBody D dto) {
        boolean success = getService().save(dto);
        return success ? Result.success() : Result.error("Save failed");
    }

    @PutMapping
    public Result<Void> update(@RequestBody D dto) {
        boolean success = getService().update(dto);
        return success ? Result.success() : Result.error("Update failed");
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        boolean success = getService().delete(id);
        return success ? Result.success() : Result.error("Delete failed");
    }

    @PostMapping("/batch-delete")
    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
        boolean success = getService().batchDelete(ids);
        return success ? Result.success() : Result.error("Delete failed");
    }

    @GetMapping("/list")
    public Result<List<V>> list() {
        return Result.success(getService().listAll());
    }

    protected abstract D createDTO();
}
```

- [ ] **Step 2: Commit**

```bash
git add basedata/src/main/java/com/opentms/basedata/controller/BaseController.java
git commit -m "feat: 创建抽象BaseController"
```

---

## Task 5: 实现BusinessUnit实体及完整业务

**Files:**
- Create: `basedata/src/main/java/com/opentms/basedata/entity/BusinessUnit.java`
- Create: `basedata/src/main/java/com/opentms/basedata/dto/BusinessUnitDTO.java`
- Create: `basedata/src/main/java/com/opentms/basedata/vo/BusinessUnitVO.java`
- Create: `basedata/src/main/java/com/opentms/basedata/mapper/BusinessUnitMapper.java`
- Create: `basedata/src/main/java/com/opentms/basedata/service/BusinessUnitService.java`
- Create: `basedata/src/main/java/com/opentms/basedata/service/impl/BusinessUnitServiceImpl.java`
- Create: `basedata/src/main/java/com/opentms/basedata/controller/BusinessUnitController.java`
- Create: `basedata/src/main/resources/mapper/BusinessUnitMapper.xml` (如需)

- [ ] **Step 1: 创建BusinessUnit实体**

```java
// basedata/src/main/java/com/opentms/basedata/entity/BusinessUnit.java
package com.opentms.basedata.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.opentms.common.model.BaseCodeEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("trm_business_unit_t")
public class BusinessUnit extends BaseCodeEntity {

    private String enName;

    private String legalPerson;

    private String address;

    private String taxNo;
}
```

- [ ] **Step 2: 创建BusinessUnitDTO**

```java
// basedata/src/main/java/com/opentms/basedata/dto/BusinessUnitDTO.java
package com.opentms.basedata.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class BusinessUnitDTO extends BaseDTO {

    private String enName;

    private String legalPerson;

    private String address;

    private String taxNo;
}
```

- [ ] **Step 3: 创建BusinessUnitVO**

```java
// basedata/src/main/java/com/opentms/basedata/vo/BusinessUnitVO.java
package com.opentms.basedata.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class BusinessUnitVO extends BaseVO {

    private String enName;

    private String legalPerson;

    private String address;

    private String taxNo;
}
```

- [ ] **Step 4: 创建BusinessUnitMapper**

```java
// basedata/src/main/java/com/opentms/basedata/mapper/BusinessUnitMapper.java
package com.opentms.basedata.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.opentms.basedata.entity.BusinessUnit;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BusinessUnitMapper extends BaseMapper<BusinessUnit> {
}
```

- [ ] **Step 5: 创建BusinessUnitService**

```java
// basedata/src/main/java/com/opentms/basedata/service/BusinessUnitService.java
package com.opentms.basedata.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.opentms.basedata.entity.BusinessUnit;

public interface BusinessUnitService extends IService<BusinessUnit> {
}
```

- [ ] **Step 6: 创建BusinessUnitServiceImpl**

```java
// basedata/src/main/java/com/opentms/basedata/service/impl/BusinessUnitServiceImpl.java
package com.opentms.basedata.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.opentms.basedata.entity.BusinessUnit;
import com.opentms.basedata.mapper.BusinessUnitMapper;
import com.opentms.basedata.service.BusinessUnitService;
import org.springframework.stereotype.Service;

@Service
public class BusinessUnitServiceImpl extends ServiceImpl<BusinessUnitMapper, BusinessUnit> implements BusinessUnitService {
}
```

- [ ] **Step 7: 创建BusinessUnitController**

```java
// basedata/src/main/java/com/opentms/basedata/controller/BusinessUnitController.java
package com.opentms.basedata.controller;

import com.opentms.basedata.dto.BusinessUnitDTO;
import com.opentms.basedata.entity.BusinessUnit;
import com.opentms.basedata.service.BusinessUnitService;
import com.opentms.basedata.vo.BusinessUnitVO;
import com.opentms.common.model.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/business-units")
@RequiredArgsConstructor
public class BusinessUnitController {

    private final BusinessUnitService businessUnitService;

    @GetMapping("/page")
    public Result<com.baomidou.mybatisplus.core.metadata.IPage<BusinessUnitVO>> page(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "20") int pageSize) {
        return Result.success(businessUnitService.queryPage(keyword, status, pageNo, pageSize));
    }

    @GetMapping("/{id}")
    public Result<BusinessUnitVO> getById(@PathVariable Long id) {
        BusinessUnit entity = businessUnitService.getById(id);
        if (entity == null) {
            return Result.notFound("Not found");
        }
        return Result.success(convertToVO(entity));
    }

    @PostMapping
    public Result<Void> save(@RequestBody BusinessUnitDTO dto) {
        businessUnitService.saveBusiness(dto);
        return Result.success();
    }

    @PutMapping
    public Result<Void> update(@RequestBody BusinessUnitDTO dto) {
        businessUnitService.updateBusiness(dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        businessUnitService.deleteBusiness(id);
        return Result.success();
    }

    @PostMapping("/batch-delete")
    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
        businessUnitService.removeByIds(ids);
        return Result.success();
    }

    @GetMapping("/list")
    public Result<List<BusinessUnitVO>> list() {
        return Result.success(businessUnitService.listAll());
    }

    private BusinessUnitVO convertToVO(BusinessUnit entity) {
        BusinessUnitVO vo = new BusinessUnitVO();
        vo.setId(entity.getId());
        vo.setCode(entity.getCode());
        vo.setName(entity.getName());
        vo.setEnName(entity.getEnName());
        vo.setLegalPerson(entity.getLegalPerson());
        vo.setAddress(entity.getAddress());
        vo.setTaxNo(entity.getTaxNo());
        vo.setStatus(entity.getStatus());
        vo.setCreatedBy(entity.getCreatedBy());
        vo.setCreatedAt(entity.getCreatedAt());
        vo.setUpdatedBy(entity.getUpdatedBy());
        vo.setUpdatedAt(entity.getUpdatedAt());
        return vo;
    }
}
```

- [ ] **Step 8: 更新BusinessUnitService添加业务方法**

```java
// basedata/src/main/java/com/opentms/basedata/service/BusinessUnitService.java
package com.opentms.basedata.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.opentms.basedata.dto.BusinessUnitDTO;
import com.opentms.basedata.entity.BusinessUnit;
import com.opentms.basedata.vo.BusinessUnitVO;

public interface BusinessUnitService extends IService<BusinessUnit> {

    IPage<BusinessUnitVO> queryPage(String keyword, String status, int pageNo, int pageSize);

    void saveBusiness(BusinessUnitDTO dto);

    void updateBusiness(BusinessUnitDTO dto);

    void deleteBusiness(Long id);

    List<BusinessUnitVO> listAll();
}
```

- [ ] **Step 9: 更新BusinessUnitServiceImpl添加业务实现**

```java
// basedata/src/main/java/com/opentms/basedata/service/impl/BusinessUnitServiceImpl.java
package com.opentms.basedata.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.opentms.basedata.dto.BusinessUnitDTO;
import com.opentms.basedata.entity.BusinessUnit;
import com.opentms.basedata.mapper.BusinessUnitMapper;
import com.opentms.basedata.service.BusinessUnitService;
import com.opentms.basedata.vo.BusinessUnitVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class BusinessUnitServiceImpl extends ServiceImpl<BusinessUnitMapper, BusinessUnit> implements BusinessUnitService {

    @Override
    public IPage<BusinessUnitVO> queryPage(String keyword, String status, int pageNo, int pageSize) {
        LambdaQueryWrapper<BusinessUnit> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(keyword)) {
            wrapper.like(BusinessUnit::getCode, keyword)
                   .or()
                   .like(BusinessUnit::getName, keyword);
        }
        
        if (StringUtils.hasText(status)) {
            wrapper.eq(BusinessUnit::getStatus, status);
        }
        
        wrapper.orderByDesc(BusinessUnit::getCreatedAt);
        
        Page<BusinessUnit> page = new Page<>(pageNo, pageSize);
        IPage<BusinessUnit> result = this.page(page, wrapper);
        
        return result.convert(this::convertToVO);
    }

    @Override
    public void saveBusiness(BusinessUnitDTO dto) {
        BusinessUnit entity = new BusinessUnit();
        BeanUtils.copyProperties(dto, entity);
        this.save(entity);
    }

    @Override
    public void updateBusiness(BusinessUnitDTO dto) {
        BusinessUnit entity = new BusinessUnit();
        BeanUtils.copyProperties(dto, entity);
        this.updateById(entity);
    }

    @Override
    public void deleteBusiness(Long id) {
        this.removeById(id);
    }

    @Override
    public List<BusinessUnitVO> listAll() {
        List<BusinessUnit> list = this.list();
        List<BusinessUnitVO> result = new ArrayList<>();
        for (BusinessUnit entity : list) {
            result.add(convertToVO(entity));
        }
        return result;
    }

    private BusinessUnitVO convertToVO(BusinessUnit entity) {
        BusinessUnitVO vo = new BusinessUnitVO();
        vo.setId(entity.getId());
        vo.setCode(entity.getCode());
        vo.setName(entity.getName());
        vo.setEnName(entity.getEnName());
        vo.setLegalPerson(entity.getLegalPerson());
        vo.setAddress(entity.getAddress());
        vo.setTaxNo(entity.getTaxNo());
        vo.setStatus(entity.getStatus());
        vo.setCreatedBy(entity.getCreatedBy());
        vo.setCreatedAt(entity.getCreatedAt());
        vo.setUpdatedBy(entity.getUpdatedBy());
        vo.setUpdatedAt(entity.getUpdatedAt());
        return vo;
    }
}
```

- [ ] **Step 10: Commit**

```bash
git add basedata/src/main/java/com/opentms/basedata/entity/BusinessUnit.java basedata/src/main/java/com/opentms/basedata/dto/BusinessUnitDTO.java basedata/src/main/java/com/opentms/basedata/vo/BusinessUnitVO.java basedata/src/main/java/com/opentms/basedata/mapper/BusinessUnitMapper.java basedata/src/main/java/com/opentms/basedata/service/BusinessUnitService.java basedata/src/main/java/com/opentms/basedata/service/impl/BusinessUnitServiceImpl.java basedata/src/main/java/com/opentms/basedata/controller/BusinessUnitController.java
git commit -m "feat(basedata): 实现BusinessUnit实体及完整CRUD"
```

---

## Task 6-12: 实现其余实体 (Trader, Country, Holiday, Bank, Counterparty, CounterpartyAccount)

按照Task 5的模式，实现其余6个实体模块。每个模块包含：
- Entity (继承BaseCodeEntity)
- DTO
- VO
- Mapper
- Service接口
- ServiceImpl
- Controller

### Task 6: Trader (交易员)
### Task 7: Country (国家)
### Task 8: Holiday (节假日)
### Task 9: Bank (银行)
### Task 10: Counterparty (对手方)
### Task 11: CounterpartyAccount (对手方账户)

---

## Task 13: 创建数据库表SQL脚本

**Files:**
- Create: `db/basedata.sql`

- [ ] **Step 1: 创建基础数据表SQL**

```sql
-- Open-TMS 基础数据表
-- 数据库: opentms_dev

-- 业务单元表
CREATE TABLE trm_business_unit_t (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    en_name VARCHAR(200),
    legal_person VARCHAR(50),
    address VARCHAR(500),
    tax_no VARCHAR(50),
    status CHAR(1) NOT NULL DEFAULT '1',
    created_by VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP,
    version INT DEFAULT 0,
    deleted CHAR(1) DEFAULT '0'
);
CREATE INDEX idx_business_unit_code ON trm_business_unit_t(code);
CREATE INDEX idx_business_unit_status ON trm_business_unit_t(status);

-- 交易员表
CREATE TABLE trm_trader_t (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(50) NOT NULL,
    en_name VARCHAR(50),
    department VARCHAR(100),
    phone VARCHAR(30),
    email VARCHAR(100),
    status CHAR(1) NOT NULL DEFAULT '1',
    created_by VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP,
    version INT DEFAULT 0,
    deleted CHAR(1) DEFAULT '0'
);
CREATE INDEX idx_trader_code ON trm_trader_t(code);
CREATE INDEX idx_trader_status ON trm_trader_t(status);

-- 币种表
CREATE TABLE trm_currency_t (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(10) NOT NULL UNIQUE,
    name VARCHAR(50) NOT NULL,
    symbol VARCHAR(10),
    decimal_places INT NOT NULL DEFAULT 2,
    status CHAR(1) NOT NULL DEFAULT '1',
    created_by VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP,
    version INT DEFAULT 0,
    deleted CHAR(1) DEFAULT '0'
);
CREATE INDEX idx_currency_code ON trm_currency_t(code);
CREATE INDEX idx_currency_status ON trm_currency_t(status);

-- 国家表
CREATE TABLE trm_country_t (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(10) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    en_name VARCHAR(100),
    timezone VARCHAR(50),
    area_code VARCHAR(10),
    status CHAR(1) NOT NULL DEFAULT '1',
    created_by VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP,
    version INT DEFAULT 0,
    deleted CHAR(1) DEFAULT '0'
);
CREATE INDEX idx_country_code ON trm_country_t(code);
CREATE INDEX idx_country_status ON trm_country_t(status);

-- 节假日表
CREATE TABLE trm_holiday_t (
    id BIGSERIAL PRIMARY KEY,
    date DATE NOT NULL,
    name VARCHAR(100) NOT NULL,
    country_code VARCHAR(10) NOT NULL,
    is_adjust CHAR(1) NOT NULL DEFAULT '0',
    remark VARCHAR(500),
    created_by VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP,
    version INT DEFAULT 0,
    deleted CHAR(1) DEFAULT '0'
);
CREATE INDEX idx_holiday_date ON trm_holiday_t(date);
CREATE INDEX idx_holiday_country ON trm_holiday_t(country_code);

-- 银行表
CREATE TABLE trm_bank_t (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    en_name VARCHAR(200),
    swift_code VARCHAR(11),
    bank_no VARCHAR(20),
    country_code VARCHAR(10) NOT NULL,
    bank_type VARCHAR(20),
    status CHAR(1) NOT NULL DEFAULT '1',
    created_by VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP,
    version INT DEFAULT 0,
    deleted CHAR(1) DEFAULT '0'
);
CREATE INDEX idx_bank_code ON trm_bank_t(code);
CREATE INDEX idx_bank_country ON trm_bank_t(country_code);
CREATE INDEX idx_bank_status ON trm_bank_t(status);

-- 对手方表
CREATE TABLE trm_counterparty_t (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    en_name VARCHAR(200),
    type VARCHAR(20) NOT NULL,
    country_code VARCHAR(10) NOT NULL,
    credit_rating VARCHAR(10),
    ext_rating VARCHAR(10),
    address VARCHAR(500),
    phone VARCHAR(30),
    status CHAR(1) NOT NULL DEFAULT '1',
    created_by VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP,
    version INT DEFAULT 0,
    deleted CHAR(1) DEFAULT '0'
);
CREATE INDEX idx_counterparty_code ON trm_counterparty_t(code);
CREATE INDEX idx_counterparty_type ON trm_counterparty_t(type);
CREATE INDEX idx_counterparty_status ON trm_counterparty_t(status);

-- 对手方账户表
CREATE TABLE trm_counterparty_account_t (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    counterparty_id BIGINT NOT NULL,
    bank_id BIGINT NOT NULL,
    account_name VARCHAR(200) NOT NULL,
    account_no VARCHAR(50) NOT NULL,
    currency VARCHAR(10) NOT NULL,
    account_type VARCHAR(20) NOT NULL,
    status CHAR(1) NOT NULL DEFAULT '1',
    created_by VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP,
    version INT DEFAULT 0,
    deleted CHAR(1) DEFAULT '0'
);
CREATE INDEX idx_counterparty_account_code ON trm_counterparty_account_t(code);
CREATE INDEX idx_counterparty_account_counterparty ON trm_counterparty_account_t(counterparty_id);
CREATE INDEX idx_counterparty_account_bank ON trm_counterparty_account_t(bank_id);
CREATE INDEX idx_counterparty_account_status ON trm_counterparty_account_t(status);
```

- [ ] **Step 2: Commit**

```bash
git add db/basedata.sql
git commit -m "feat(db): 创建基础数据表SQL脚本"
```

---

## Task 14: 编译验证

- [ ] **Step 1: 编译项目**

```bash
cd basedata && mvn compile -DskipTests
```

- [ ] **Step 2: 验证通过后Commit**

---

## 计划完成

**Plan complete and saved to `docs/superpowers/plans/2026-04-06-basedata-impl.md`. Two execution options:**

**1. Subagent-Driven (recommended)** - I dispatch a fresh subagent per task, review between tasks, fast iteration

**2. Inline Execution** - Execute tasks in this session using executing-plans, batch execution with checkpoints

**Which approach?**

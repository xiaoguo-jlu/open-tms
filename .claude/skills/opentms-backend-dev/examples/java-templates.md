# Java Templates Reference

> 源文件: SKILL.md 步骤3-8 的代码模板，此处集中存放，主文件通过引用使用。

## Entity Template

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

## DTO Template

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

## VO Template

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

## Mapper Template

```java
package com.opentms.{module}.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.opentms.{module}.entity.{Entity};
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface {Entity}Mapper extends BaseMapper<{Entity}> {
}
```

## Service Interface Template

```java
package com.opentms.{module}.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.opentms.{module}.entity.{Entity};
import com.opentms.{module}.vo.{Entity}VO;

public interface {Entity}Service {

    Page<{Entity}VO> queryPage(String keyword, String status, int pageNum, int pageSize);

    {Entity}VO getById(Long id);

    {Entity}VO getByCode(String code);

    {Entity}VO save({Entity} entity);

    {Entity}VO updateById({Entity} entity);

    boolean deleteById(Long id);

    boolean checkCodeExists(String code, Long excludeId);
}
```

## Service Impl Template (Simple — extends ServiceImpl)

```java
package com.opentms.{module}.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.opentms.{module}.entity.{Entity};
import com.opentms.{module}.mapper.{Entity}Mapper;
import com.opentms.{module}.service.{Entity}Service;
import com.opentms.{module}.vo.{Entity}VO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.stream.Collectors;

@Slf4j
@Service
public class {Entity}ServiceImpl extends ServiceImpl<{Entity}Mapper, {Entity}> implements {Entity}Service {

    @Override
    public Page<{Entity}VO> queryPage(String keyword, String status, int pageNum, int pageSize) {
        LambdaQueryWrapper<{Entity}> wrapper = new LambdaQueryWrapper<>();

        // 必须使用具体类{Entity}，不能使用泛型T
        if (StringUtils.hasText(keyword)) {
            wrapper.like({Entity}::getCode, keyword)
                   .or()
                   .like({Entity}::getName, keyword);
        }

        if (StringUtils.hasText(status)) {
            wrapper.eq({Entity}::getStatus, status);
        }

        // 必须使用具体类{Entity}::getCreatedAt
        wrapper.orderByDesc({Entity}::getCreatedAt);

        Page<{Entity}> page = baseMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);

        Page<{Entity}VO> result = new Page<>();
        result.setCurrent(page.getCurrent());
        result.setSize(page.getSize());
        result.setTotal(page.getTotal());
        result.setRecords(page.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList()));

        return result;
    }

    @Override
    public {Entity}VO getById(Long id) {
        {Entity} entity = baseMapper.selectById(id);
        return entity != null ? convertToVO(entity) : null;
    }

    @Override
    public {Entity}VO save({Entity} entity) {
        if (checkCodeExists(entity.getCode(), null)) {
            throw new BusinessException("编码已存在: " + entity.getCode());
        }
        baseMapper.insert(entity);
        return convertToVO(entity);
    }

    @Override
    public {Entity}VO updateById({Entity} entity) {
        if (entity.getId() == null) {
            throw new BusinessException("ID不能为空");
        }
        {Entity} existing = baseMapper.selectById(entity.getId());
        if (existing == null) {
            throw new BusinessException("记录不存在");
        }
        if (checkCodeExists(entity.getCode(), entity.getId())) {
            throw new BusinessException("编码已存在: " + entity.getCode());
        }
        baseMapper.updateById(entity);
        return convertToVO(baseMapper.selectById(entity.getId()));
    }

    @Override
    public boolean deleteById(Long id) {
        {Entity} existing = baseMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("记录不存在");
        }
        return removeById(id);
    }

    @Override
    public boolean checkCodeExists(String code, Long excludeId) {
        if (!StringUtils.hasText(code)) {
            return false;
        }
        LambdaQueryWrapper<{Entity}> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq({Entity}::getCode, code);
        if (excludeId != null) {
            wrapper.ne({Entity}::getId, excludeId);
        }
        return count(wrapper) > 0;
    }

    private {Entity}VO convertToVO({Entity} entity) {
        {Entity}VO vo = new {Entity}VO();
        vo.setId(entity.getId());
        vo.setCode(entity.getCode());
        vo.setName(entity.getName());
        vo.setStatus(entity.getStatus());
        vo.setCreatedAt(entity.getCreatedAt());
        vo.setUpdatedAt(entity.getUpdatedAt());
        return vo;
    }
}
```

## Service Impl Template (DTO-based — extends BaseServiceImpl)

```java
package com.opentms.{module}.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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

## Controller Template

> 注意: 本项目实际使用 Apache CXF JAX-RS (@Path/@GET/@POST) 而非 Spring MVC (@RestController/@GetMapping)。
> 遵循 CLAUDE.md 红线: update/delete 一律 POST,不用 PUT/DELETE。

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
    @Produces(MediaType.APPLICATION_JSON)
    public Object list() {
        return Result.success({entity}Service.listAll());
    }

    @GET
    @Path("/page")
    @Produces(MediaType.APPLICATION_JSON)
    public Object page(@QueryParam("keyword") String keyword,
                       @QueryParam("status") String status,
                       @QueryParam("pageNum") @DefaultValue("1") int pageNum,
                       @QueryParam("pageSize") @DefaultValue("10") int pageSize) {
        return Result.success({entity}Service.queryPage(keyword, status, pageNum, pageSize));
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
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
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Object save({Entity}DTO dto) {
        try {
            return Result.success({entity}Service.save(dto));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @POST
    @Path("/update")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Object update({Entity}DTO dto) {
        try {
            if (dto.getId() == null) {
                return Result.badRequest("ID不能为空");
            }
            return Result.success({entity}Service.updateById(dto));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @POST
    @Path("/delete/{id}")
    @Produces(MediaType.APPLICATION_JSON)
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

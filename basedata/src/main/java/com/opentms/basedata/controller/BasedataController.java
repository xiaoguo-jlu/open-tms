package com.opentms.basedata.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.opentms.basedata.dto.BasedataDTO;
import com.opentms.basedata.service.BasedataService;
import com.opentms.basedata.vo.BasedataVO;
import com.opentms.common.model.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RequiredArgsConstructor
public abstract class BasedataController<
        S extends BasedataService<?, D, V>,
        D extends BasedataDTO,
        V extends BasedataVO> {

    protected abstract S getService();

    protected abstract String getEntityName();

    @RequestMapping
    public Result<List<V>> list() {
        return Result.success(getService().listAll());
    }

    @RequestMapping("/page")
    public Result<IPage<V>> page(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "20") int pageSize) {
        D dto = createDTO();
        dto.setKeyword(keyword);
        dto.setStatus(status);
        return Result.success(getService().queryPage(dto, pageNo, pageSize));
    }

    @RequestMapping("/{id}")
    public Result<V> getById(@PathVariable String id) {
        try {
            Long parseId = Long.parseLong(id);
            if (parseId <= 0) {
                return Result.badRequest("ID必须为正整数");
            }
            V vo = getService().getById(parseId);
            return vo != null ? Result.success(vo) : Result.notFound(getEntityName() + "不存在");
        } catch (NumberFormatException e) {
            return Result.badRequest("ID参数格式不正确");
        }
    }

    @RequestMapping("/code/{code}")
    public Result<V> getByCode(@PathVariable String code) {
        V vo = getService().getByCode(code);
        return vo != null ? Result.success(vo) : Result.notFound(getEntityName() + "不存在");
    }

    @RequestMapping
    public Result<V> save(@RequestBody @Validated D dto) {
        return Result.success(getService().save(dto));
    }

    @RequestMapping
    public Result<V> update(@RequestBody @Validated D dto) {
        if (dto.getId() == null) {
            return Result.badRequest("ID不能为空");
        }
        return Result.success(getService().updateById(dto));
    }

    @RequestMapping("/{id}")
    public Result<Void> delete(@PathVariable String id) {
        try {
            Long parseId = Long.parseLong(id);
            if (parseId <= 0) {
                return Result.badRequest("ID必须为正整数");
            }
            getService().removeById(parseId);
            return Result.success();
        } catch (NumberFormatException e) {
            return Result.badRequest("ID参数格式不正确");
        }
    }

    protected abstract D createDTO();
}
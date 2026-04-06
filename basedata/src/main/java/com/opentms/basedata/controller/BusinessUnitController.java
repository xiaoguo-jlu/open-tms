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
        BusinessUnitVO vo = businessUnitService.getById(id);
        return vo == null ? Result.notFound("Not found") : Result.success(vo);
    }

    @PostMapping
    public Result<Void> save(@RequestBody BusinessUnitDTO dto) {
        boolean success = businessUnitService.save(dto);
        return success ? Result.success() : Result.error("Save failed");
    }

    @PutMapping
    public Result<Void> update(@RequestBody BusinessUnitDTO dto) {
        boolean success = businessUnitService.update(dto);
        return success ? Result.success() : Result.error("Update failed");
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        boolean success = businessUnitService.delete(id);
        return success ? Result.success() : Result.error("Delete failed");
    }

    @PostMapping("/batch-delete")
    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
        boolean success = businessUnitService.batchDelete(ids);
        return success ? Result.success() : Result.error("Delete failed");
    }

    @GetMapping("/list")
    public Result<List<BusinessUnitVO>> list() {
        return Result.success(businessUnitService.listAll());
    }
}

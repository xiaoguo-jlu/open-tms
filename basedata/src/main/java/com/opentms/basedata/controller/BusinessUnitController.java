package com.opentms.basedata.controller;

import com.opentms.basedata.entity.BusinessUnit;
import com.opentms.basedata.service.BusinessUnitService;
import com.opentms.common.model.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/business-units")
@RequiredArgsConstructor
public class BusinessUnitController {

    private final BusinessUnitService businessUnitService;

    @GetMapping("/page")
    public Result<com.baomidou.mybatisplus.extension.plugins.pagination.Page<BusinessUnit>> page(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(businessUnitService.queryPage(keyword, status, pageNum, pageSize));
    }

    @GetMapping("/{id}")
    public Result<BusinessUnit> getById(@PathVariable Long id) {
        BusinessUnit businessUnit = businessUnitService.getBusinessUnitById(id);
        if (businessUnit == null) {
            return Result.notFound("Business unit not found");
        }
        return Result.success(businessUnit);
    }

    @PostMapping
    public Result<Void> save(@RequestBody BusinessUnit businessUnit) {
        businessUnitService.saveBusinessUnit(businessUnit);
        return Result.success();
    }

    @PutMapping
    public Result<Void> update(@RequestBody BusinessUnit businessUnit) {
        businessUnitService.updateBusinessUnit(businessUnit);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        businessUnitService.deleteBusinessUnit(id);
        return Result.success();
    }
}
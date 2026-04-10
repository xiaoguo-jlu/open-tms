package com.opentms.limit.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.opentms.common.model.Result;
import com.opentms.limit.entity.Limit;
import com.opentms.limit.service.LimitService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/limits")
@RequiredArgsConstructor
public class LimitController {

    private final LimitService limitService;

    @GetMapping("/page")
    public Result<Page<Limit>> page(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String limitType,
            @RequestParam(required = false) Long businessUnitId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(limitService.queryPage(keyword, limitType, businessUnitId, pageNum, pageSize));
    }

    @GetMapping("/{id}")
    public Result<Limit> getById(@PathVariable Long id) {
        Limit limit = limitService.getLimitById(id);
        if (limit == null) {
            return Result.notFound("Limit not found");
        }
        return Result.success(limit);
    }

    @GetMapping("/{id}/monitor")
    public Result<Limit> monitor(@PathVariable Long id) {
        Limit limit = limitService.monitor(id);
        if (limit == null) {
            return Result.notFound("Limit not found");
        }
        return Result.success(limit);
    }

    @GetMapping("/alerts")
    public Result<List<Limit>> alerts() {
        return Result.success(limitService.getAlerts());
    }

    @PostMapping
    public Result<Void> save(@RequestBody Limit limit) {
        limitService.saveLimit(limit);
        return Result.success();
    }

    @PutMapping
    public Result<Void> update(@RequestBody Limit limit) {
        limitService.updateLimit(limit);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        limitService.deleteLimit(id);
        return Result.success();
    }
}
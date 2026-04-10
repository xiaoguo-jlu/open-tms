package com.opentms.impairment.controller;

import com.opentms.impairment.entity.Impairment;
import com.opentms.impairment.service.ImpairmentService;
import com.opentms.impairment.vo.ImpairmentVO;
import com.opentms.common.model.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/impairments")
@RequiredArgsConstructor
public class ImpairmentController {

    private final ImpairmentService impairmentService;

    @GetMapping("/page")
    public Result<com.baomidou.mybatisplus.extension.plugins.pagination.Page<Impairment>> page(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long businessUnitId,
            @RequestParam(required = false) String assessmentType,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(impairmentService.queryPage(keyword, status, businessUnitId, assessmentType, pageNum, pageSize));
    }

    @GetMapping("/{id}")
    public Result<Impairment> getById(@PathVariable Long id) {
        Impairment impairment = impairmentService.getById(id);
        if (impairment == null) {
            return Result.notFound("Impairment not found");
        }
        return Result.success(impairment);
    }

    @GetMapping("/{id}/result")
    public Result<ImpairmentVO> getResult(@PathVariable Long id) {
        ImpairmentVO vo = impairmentService.getResult(id);
        if (vo == null) {
            return Result.notFound("Impairment not found");
        }
        return Result.success(vo);
    }

    @GetMapping("/{id}/details")
    public Result<ImpairmentVO> getDetails(@PathVariable Long id) {
        ImpairmentVO vo = impairmentService.getDetails(id);
        if (vo == null) {
            return Result.notFound("Impairment not found");
        }
        return Result.success(vo);
    }

    @PostMapping("/{id}/calculate")
    public Result<Impairment> calculate(@PathVariable Long id) {
        try {
            Impairment impairment = impairmentService.calculate(id);
            return Result.success(impairment);
        } catch (Exception e) {
            return Result.badRequest(e.getMessage());
        }
    }

    @PostMapping
    public Result<Void> save(@RequestBody Impairment impairment) {
        impairmentService.save(impairment);
        return Result.success();
    }
}
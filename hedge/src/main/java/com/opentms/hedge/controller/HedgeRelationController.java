package com.opentms.hedge.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.opentms.common.model.Result;
import com.opentms.hedge.entity.HedgeRelation;
import com.opentms.hedge.service.HedgeRelationService;
import com.opentms.hedge.vo.HedgeRelationVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/hedge-relations")
@RequiredArgsConstructor
public class HedgeRelationController {

    private final HedgeRelationService hedgeRelationService;

    @GetMapping("/page")
    public Result<Page<HedgeRelationVO>> page(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(hedgeRelationService.queryPage(keyword, status, pageNum, pageSize));
    }

    @GetMapping("/{id}")
    public Result<HedgeRelationVO> getById(@PathVariable Long id) {
        HedgeRelationVO vo = hedgeRelationService.getHedgeRelationById(id);
        if (vo == null) {
            return Result.notFound("Hedge relation not found");
        }
        return Result.success(vo);
    }

    @GetMapping("/{id}/effectiveness")
    public Result<HedgeRelationVO> getEffectiveness(@PathVariable Long id) {
        HedgeRelationVO vo = hedgeRelationService.getEffectiveness(id);
        if (vo == null) {
            return Result.notFound("Hedge relation not found");
        }
        return Result.success(vo);
    }

    @GetMapping("/{id}/pnl")
    public Result<HedgeRelationVO> getPnL(@PathVariable Long id) {
        HedgeRelationVO vo = hedgeRelationService.getPnL(id);
        if (vo == null) {
            return Result.notFound("Hedge relation not found");
        }
        return Result.success(vo);
    }

    @PostMapping
    public Result<Void> save(@RequestBody HedgeRelation hedgeRelation) {
        boolean success = hedgeRelationService.saveHedgeRelation(hedgeRelation);
        return success ? Result.success() : Result.error("Save failed");
    }

    @PutMapping
    public Result<Void> update(@RequestBody HedgeRelation hedgeRelation) {
        boolean success = hedgeRelationService.updateHedgeRelation(hedgeRelation);
        return success ? Result.success() : Result.error("Update failed");
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        boolean success = hedgeRelationService.deleteHedgeRelation(id);
        return success ? Result.success() : Result.error("Delete failed");
    }

    @PostMapping("/{id}/terminate")
    public Result<Void> terminate(@PathVariable Long id) {
        boolean success = hedgeRelationService.terminate(id);
        return success ? Result.success() : Result.error("Terminate failed");
    }
}
package com.opentms.fx.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.opentms.fx.entity.FxDeal;
import com.opentms.fx.service.FxDealService;
import com.opentms.fx.vo.FxDealVO;
import com.opentms.common.model.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/fx-deals")
@RequiredArgsConstructor
public class FxDealController {

    private final FxDealService fxDealService;

    @GetMapping("/page")
    public Result<IPage<FxDealVO>> page(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String fxType,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(fxDealService.queryPage(keyword, fxType, status, pageNum, pageSize));
    }

    @GetMapping("/{id}")
    public Result<FxDealVO> getById(@PathVariable Long id) {
        FxDealVO fxDealVO = fxDealService.getFxDealById(id);
        if (fxDealVO == null) {
            return Result.notFound("FX Deal not found");
        }
        return Result.success(fxDealVO);
    }

    @PostMapping
    public Result<Void> save(@RequestBody FxDeal fxDeal) {
        fxDealService.saveFxDeal(fxDeal);
        return Result.success();
    }

    @PutMapping
    public Result<Void> update(@RequestBody FxDeal fxDeal) {
        fxDealService.updateFxDeal(fxDeal);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        fxDealService.deleteFxDeal(id);
        return Result.success();
    }

    @PostMapping("/{id}/submit")
    public Result<Void> submit(@PathVariable Long id) {
        fxDealService.submitFxDeal(id);
        return Result.success();
    }

    @PostMapping("/{id}/approve")
    public Result<Void> approve(@PathVariable Long id) {
        fxDealService.approveFxDeal(id);
        return Result.success();
    }

    @PostMapping("/{id}/execute")
    public Result<Void> execute(@PathVariable Long id) {
        fxDealService.executeFxDeal(id);
        return Result.success();
    }
}
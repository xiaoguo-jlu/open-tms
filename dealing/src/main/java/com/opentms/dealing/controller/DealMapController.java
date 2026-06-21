package com.opentms.dealing.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.opentms.common.model.Result;
import com.opentms.dealing.entity.DealMap;
import com.opentms.dealing.service.DealMapService;
import com.opentms.dealing.vo.DealMapVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * DealMap Controller（v2.0 - 业务事件时间线）
 * <p>路径前缀：/api/v1/dealing/dealmap</p>
 */
@RestController
@RequestMapping("/api/v1/dealing/dealmap")
@RequiredArgsConstructor
public class DealMapController {

    private final DealMapService dealMapService;

    /**
     * 分页查询 DealMap
     */
    @GetMapping("/page")
    public Result<Page<DealMapVO>> page(
            @RequestParam(required = false) String dealNumber,
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false) String eventStatus,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return Result.success(dealMapService.queryPage(dealNumber, eventType, eventStatus, pageNum, pageSize));
    }

    /**
     * 按 dealNumber 查询 DealMap 时间线
     */
    @GetMapping("/by-deal/{dealNumber}")
    public Result<List<DealMapVO>> listByDeal(@PathVariable String dealNumber) {
        return Result.success(dealMapService.listByDealNumber(dealNumber));
    }

    /**
     * 获取 DealMap 详情
     */
    @GetMapping("/{id}")
    public Result<DealMapVO> getById(@PathVariable Long id) {
        DealMapVO vo = dealMapService.getById(id);
        if (vo == null) {
            return Result.notFound("DealMap not found");
        }
        return Result.success(vo);
    }

    /**
     * 冲销 DealMap
     */
    @PostMapping("/{id}/reverse")
    public Result<DealMapVO> reverse(@PathVariable Long id, @RequestBody Map<String, String> request) {
        String operator = request.getOrDefault("operator", "system");
        String remark = request.get("remark");
        try {
            DealMap reversed = dealMapService.reverseDealMap(id, operator, remark);
            return Result.success(dealMapService.getById(reversed.getId()));
        } catch (IllegalArgumentException e) {
            return Result.badRequest(e.getMessage());
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}

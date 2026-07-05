package com.opentms.dealing.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.opentms.common.model.Result;
import com.opentms.dealing.dto.FxCalculateRequest;
import com.opentms.dealing.dto.FxCalculateResponse;
import com.opentms.dealing.dto.FxDealDTO;
import com.opentms.dealing.dto.RateFixRequest;
import com.opentms.dealing.service.FxDealService;
import com.opentms.dealing.vo.FxDealDetailVO;
import com.opentms.dealing.vo.FxDealVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * FX 交易 REST 控制器（v3.2）
 * <p>路径：/api/v1/dealing/fx-deals</p>
 * <p>7 个端点：calculate / list / detail / create / update / delete / rate-fix</p>
 */
@RestController
@RequestMapping("/api/v1/dealing/fx-deals")
@RequiredArgsConstructor
public class FxDealController {

    private final FxDealService fxDealService;

    /**
     * 后端统一计算接口（v3.2 - 单一可信源）
     */
    @PostMapping("/calculate")
    public Result<FxCalculateResponse> calculate(@RequestBody FxCalculateRequest req) {
        try {
            FxCalculateResponse resp = fxDealService.calculate(req);
            return Result.success(resp);
        } catch (IllegalArgumentException e) {
            return Result.badRequest(e.getMessage());
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 分页查询
     */
    @GetMapping("/page")
    public Result<Page<FxDealVO>> page(
            @RequestParam(required = false) Long managementEntityId,
            @RequestParam(required = false) Long counterpartyId,
            @RequestParam(required = false) String productType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(fxDealService.queryPage(
                managementEntityId, counterpartyId, productType, status,
                startDate, endDate, pageNum, pageSize));
    }

    /**
     * 按 dealNumber 获取可复制字段（id/dealNumber 置 null）
     */
    @GetMapping("/{dealNumber}/copy")
    public Result<FxDealDTO> getCopyData(@PathVariable String dealNumber) {
        FxDealDTO dto = fxDealService.getCopyData(dealNumber);
        if (dto == null) {
            return Result.notFound("FX Deal not found: " + dealNumber);
        }
        return Result.success(dto);
    }

    /**
     * 按 dealNumber 获取详情
     */
    @GetMapping("/{dealNumber}")
    public Result<FxDealDetailVO> getByDealNumber(@PathVariable String dealNumber) {
        FxDealDetailVO detail = fxDealService.getDetailByDealNumber(dealNumber);
        if (detail == null) {
            return Result.notFound("FX Deal not found: " + dealNumber);
        }
        return Result.success(detail);
    }

    /**
     * 创建 FX 交易（DEAL Action）
     */
    @PostMapping
    public Result<Map<String, Object>> create(@RequestBody FxDealDTO dto) {
        try {
            String dealNumber = fxDealService.createFxDeal(dto);
            Map<String, Object> data = new HashMap<>();
            data.put("dealNumber", dealNumber);
            data.put("status", "New");
            data.put("dealMapCount", 3);
            data.put("cashflowCount", "NDF".equalsIgnoreCase(deriveProductType(dto)) ? 0 : 2);
            return Result.success(data);
        } catch (IllegalArgumentException e) {
            return Result.badRequest(e.getMessage());
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新 FX 交易（UPDATE Action）
     */
    @PostMapping("/update")
    public Result<Void> update(@RequestBody FxDealDTO dto) {
        try {
            fxDealService.updateFxDeal(dto);
            return Result.success();
        } catch (IllegalArgumentException e) {
            return Result.badRequest(e.getMessage());
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除 FX 交易（DELETE Action）
     */
    @PostMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        try {
            fxDealService.deleteFxDeal(id);
            return Result.success();
        } catch (IllegalArgumentException e) {
            return Result.badRequest(e.getMessage());
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * NDF RATE_FIX（Phase 1: 支持 fixDate/fixCurrency/fixMarketRate/verifierBy/fixRemark）
     */
    @PostMapping("/{id}/rate-fix")
    public Result<Map<String, Object>> rateFix(@PathVariable Long id, @RequestBody RateFixRequest req) {
        try {
            Map<String, Object> data = fxDealService.rateFix(id, req);
            return Result.success(data);
        } catch (IllegalArgumentException e) {
            return Result.badRequest(e.getMessage());
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    private String deriveProductType(FxDealDTO dto) {
        if (dto == null) return null;
        if (dto.getFixingSource() != null && !dto.getFixingSource().isEmpty()) return "NDF";
        if (dto.getValueDate() != null && dto.getTradeDate() != null
                && !dto.getValueDate().isEqual(dto.getTradeDate())) return "FWD";
        return "SPOT";
    }

    /**
     * 审批通过 Action
     */
    @PostMapping("/actions/{actionNumber}/approve")
    public Result<Void> approve(@PathVariable String actionNumber,
                                @RequestBody Map<String, String> request) {
        try {
            String approver = request.getOrDefault("approver", "system");
            String remark = request.get("remark");
            fxDealService.approveAction(actionNumber, approver, remark);
            return Result.success();
        } catch (RuntimeException e) {
            return Result.badRequest(e.getMessage());
        }
    }

    /**
     * 驳回 Action
     */
    @PostMapping("/actions/{actionNumber}/reject")
    public Result<Void> reject(@PathVariable String actionNumber,
                               @RequestBody Map<String, String> request) {
        try {
            String approver = request.getOrDefault("approver", "system");
            String remark = request.get("remark");
            fxDealService.rejectAction(actionNumber, approver, remark);
            return Result.success();
        } catch (RuntimeException e) {
            return Result.badRequest(e.getMessage());
        }
    }
}
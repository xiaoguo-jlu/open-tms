package com.opentms.dealing.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.opentms.common.model.Result;
import com.opentms.dealing.dto.AcDealDTO;
import com.opentms.dealing.dto.AcDealDetailVO;
import com.opentms.dealing.dto.ActionApprovalDTO;
import com.opentms.dealing.service.AcDealService;
import com.opentms.dealing.vo.DealVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * AC 交易 Controller（v2.0 - DealMap 字段精简 + Action 多对一 + 审批仅作用于 Action）
 * <p>路径前缀：/api/v1/dealing/ac-deals</p>
 */
@RestController
@RequestMapping("/api/v1/dealing/ac-deals")
@RequiredArgsConstructor
public class AcDealController {

    private final AcDealService acDealService;

    /**
     * 列表查询（分页）
     */
    @GetMapping("/page")
    public Result<Page<DealVO>> page(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String direction,
            @RequestParam(required = false) String businessUnit,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(acDealService.queryPage(keyword, status, direction, businessUnit, pageNum, pageSize));
    }

    /**
     * 获取详情（按 ID）
     */
    @GetMapping("/{id}")
    public Result<AcDealDetailVO> getById(@PathVariable Long id) {
        AcDealDetailVO detail = acDealService.getDetail(id);
        if (detail == null) {
            return Result.notFound("AC Deal not found");
        }
        return Result.success(detail);
    }

    /**
     * 获取详情（按 dealNumber）
     */
    @GetMapping("/number/{dealNumber}")
    public Result<AcDealDetailVO> getByDealNumber(@PathVariable String dealNumber) {
        AcDealDetailVO detail = acDealService.getDetailByDealNumber(dealNumber);
        if (detail == null) {
            return Result.notFound("AC Deal not found");
        }
        return Result.success(detail);
    }

    /**
     * 新增 AC 交易（v2.0 - 事务内自动生成 Action + Deal + AcDeal + DealMap + Cashflow）
     */
    @PostMapping
    public Result<Void> save(@RequestBody AcDealDTO dto) {
        try {
            acDealService.createAcDeal(dto);
            return Result.success();
        } catch (IllegalArgumentException e) {
            return Result.badRequest(e.getMessage());
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新 AC 交易（v2.0 - 软删旧 DealMap + 新建 DealMap + 更新 Cashflow）
     */
    @PostMapping("/update")
    public Result<Void> update(@RequestBody AcDealDTO dto) {
        try {
            acDealService.updateAcDeal(dto);
            return Result.success();
        } catch (IllegalArgumentException e) {
            return Result.badRequest(e.getMessage());
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除 AC 交易（v2.0 - 级联软删 Deal + AcDeal + DealMap + Cashflow）
     */
    @PostMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        try {
            acDealService.deleteAcDeal(id);
            return Result.success();
        } catch (IllegalArgumentException e) {
            return Result.badRequest(e.getMessage());
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}

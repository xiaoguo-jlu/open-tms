package com.opentms.dealing.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.opentms.common.model.Result;
import com.opentms.dealing.dto.AtDealDTO;
import com.opentms.dealing.service.AtDealService;
import com.opentms.dealing.vo.ActionVO;
import com.opentms.dealing.vo.AtDealImageVO;
import com.opentms.dealing.vo.AtDealVO;
import com.opentms.dealing.vo.CashflowVO;
import com.opentms.dealing.vo.DealMapVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * AT 交易 REST 控制器
 * 路径：/api/v1/dealing/at-deals
 */
@RestController
@RequestMapping("/api/v1/dealing/at-deals")
@RequiredArgsConstructor
public class AtDealController {

    private final AtDealService atDealService;

    /**
     * 分页查询 AT 交易
     */
    @GetMapping("/page")
    public Result<Page<AtDealVO>> page(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String transferType,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(atDealService.queryPage(keyword, transferType, status, pageNum, pageSize));
    }

    /**
     * 按 ID 或 dealNumber 自动识别获取详情
     * <p>2026-07-05 修复: 历史路径 /at-deals/{id} 使用 Long,前端偶发传入 dealNumber
     * (如 AT202607050003) 时被 Spring 抛 400。改为 String 并在控制器内根据是否纯数字
     * 路由到 getById(Long) 或 getByDealNumber(String)。</p>
     */
    @GetMapping("/{id}")
    public Result<AtDealVO> getById(@PathVariable String id) {
        AtDealVO vo;
        if (id != null && id.matches("\\d+")) {
            vo = atDealService.getById(Long.valueOf(id));
        } else {
            vo = atDealService.getByDealNumber(id);
        }
        if (vo == null) {
            return Result.notFound("AT Deal not found");
        }
        return Result.success(vo);
    }

    /**
     * 按 dealNumber 获取详情
     */
    @GetMapping("/number/{dealNumber}")
    public Result<AtDealVO> getByDealNumber(@PathVariable String dealNumber) {
        AtDealVO vo = atDealService.getByDealNumber(dealNumber);
        if (vo == null) {
            return Result.notFound("AT Deal not found");
        }
        return Result.success(vo);
    }

    /**
     * 创建 AT 交易
     */
    @PostMapping
    public Result<Void> save(@RequestBody AtDealDTO dto) {
        try {
            atDealService.saveAtDeal(dto);
            return Result.success();
        } catch (RuntimeException e) {
            return Result.badRequest(e.getMessage());
        }
    }

    /**
     * 更新 AT 交易
     */
    @PostMapping("/update")
    public Result<Void> update(@RequestBody AtDealDTO dto) {
        try {
            atDealService.updateAtDeal(dto);
            return Result.success();
        } catch (RuntimeException e) {
            return Result.badRequest(e.getMessage());
        }
    }

    /**
     * 删除 AT 交易
     */
    @PostMapping("/{id}/delete")
    public Result<Void> delete(@PathVariable Long id) {
        atDealService.deleteAtDeal(id);
        return Result.success();
    }

    /**
     * 查询某 AT 交易的所有 DealMap（双腿）
     */
    @GetMapping("/{dealNumber}/dealmap")
    public Result<List<DealMapVO>> listDealMap(@PathVariable String dealNumber) {
        return Result.success(atDealService.listDealMapsByDeal(dealNumber));
    }

    /**
     * 查询某 AT 交易的所有 Cashflow
     */
    @GetMapping("/{dealNumber}/cashflow")
    public Result<List<CashflowVO>> listCashflow(@PathVariable String dealNumber) {
        return Result.success(atDealService.listCashflowsByDeal(dealNumber));
    }

    /**
     * 查询某 AT 交易的所有 Action
     */
    @GetMapping("/{dealNumber}/actions")
    public Result<List<ActionVO>> listActions(@PathVariable String dealNumber) {
        return Result.success(atDealService.listActionsByDeal(dealNumber));
    }

    /**
     * 查询某 AT 交易的所有镜像
     */
    @GetMapping("/{dealNumber}/images")
    public Result<List<AtDealImageVO>> listImages(@PathVariable String dealNumber) {
        return Result.success(atDealService.listImagesByDeal(dealNumber));
    }

    /**
     * 审批通过 Action
     */
    @PostMapping("/actions/{actionNumber}/approve")
    public Result<Void> approve(@PathVariable String actionNumber,
                                @RequestBody Map<String, String> request) {
        String approver = request.getOrDefault("approver", "system");
        String remark = request.get("remark");
        atDealService.approveAction(actionNumber, approver, remark);
        return Result.success();
    }

    /**
     * 驳回 Action
     */
    @PostMapping("/actions/{actionNumber}/reject")
    public Result<Void> reject(@PathVariable String actionNumber,
                               @RequestBody Map<String, String> request) {
        String approver = request.getOrDefault("approver", "system");
        String remark = request.get("remark");
        atDealService.rejectAction(actionNumber, approver, remark);
        return Result.success();
    }

    /**
     * 复制 AT 交易 — 返回可编辑字段（不含 dealNumber/id/createdAt 等系统字段）
     */
    @GetMapping("/{dealNumber}/copy")
    public Result<AtDealDTO> copy(@PathVariable String dealNumber) {
        AtDealDTO dto = atDealService.getCopyData(dealNumber);
        if (dto == null) {
            return Result.notFound("AT Deal not found");
        }
        return Result.success(dto);
    }
}

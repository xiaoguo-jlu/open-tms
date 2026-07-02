package com.opentms.dealing.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.opentms.common.model.Result;
import com.opentms.dealing.dto.ActionApprovalDTO;
import com.opentms.dealing.entity.Action;
import com.opentms.dealing.mapper.ActionMapper;
import com.opentms.dealing.service.AcDealService;
import com.opentms.dealing.vo.ActionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Action Controller（v2.0 - 多 Action/Deal + 审批仅作用于 Action）
 * <p>路径前缀：/api/v1/dealing/actions</p>
 */
@RestController
@RequestMapping("/api/v1/dealing/actions")
@RequiredArgsConstructor
public class ActionV2Controller {

    private final ActionMapper actionMapper;
    private final AcDealService acDealService;

    /**
     * 查询某 Deal 的所有 Action（v2.0 - 一笔 Deal 可有多个 Action）
     */
    @GetMapping("/by-deal/{dealNumber}")
    public Result<List<ActionVO>> listByDeal(@PathVariable String dealNumber) {
        List<Action> actions = actionMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Action>()
                        .eq(Action::getDealNumber, dealNumber)
                        .orderByAsc(Action::getCreatedAt)
        );
        List<ActionVO> vos = actions.stream().map(this::toVO).collect(Collectors.toList());
        return Result.success(vos);
    }

    /**
     * 查询待审批 Action 列表
     */
    @GetMapping("/pending")
    public Result<Page<ActionVO>> pending(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Action> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(Action::getApprovalStatus1, "Pending")
               .orderByAsc(Action::getCreatedAt);
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Action> page =
                actionMapper.selectPage(new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageNum, pageSize), wrapper);
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<ActionVO> voPage =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(page.getRecords().stream().map(this::toVO).collect(Collectors.toList()));
        return Result.success(voPage);
    }

    /**
     * Action 分页查询（供 ActionList 页面使用）
     * 支持按 dealNumber / approvalStatus / keyword 过滤
     */
    @GetMapping("/page")
    public Result<Page<ActionVO>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String dealNumber,
            @RequestParam(required = false) String approvalStatus,
            @RequestParam(required = false) String keyword) {
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Action> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        if (dealNumber != null && !dealNumber.isEmpty()) {
            wrapper.eq(Action::getDealNumber, dealNumber);
        }
        if (approvalStatus != null && !approvalStatus.isEmpty()) {
            wrapper.eq(Action::getApprovalStatus1, approvalStatus);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(Action::getActionNumber, keyword)
                    .or().like(Action::getDealNumber, keyword)
                    .or().like(Action::getActionType, keyword));
        }
        wrapper.orderByDesc(Action::getCreatedAt);
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Action> page =
                actionMapper.selectPage(new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageNum, pageSize), wrapper);
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<ActionVO> voPage =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(page.getRecords().stream().map(this::toVO).collect(Collectors.toList()));
        return Result.success(voPage);
    }

    /**
     * 审批通过 Action（v2.0 关键 - 仅更新 Action 状态，DealMap/Cashflow 状态不变）
     */
    @PostMapping("/{actionNumber}/approve")
    public Result<Void> approve(@PathVariable String actionNumber, @RequestBody ActionApprovalDTO dto) {
        try {
            acDealService.approveAction(actionNumber, dto.getApprover(), dto.getApprovalRemark());
            return Result.success();
        } catch (IllegalArgumentException e) {
            return Result.badRequest(e.getMessage());
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 审批驳回 Action（v2.0 关键 - 仅更新 Action 状态，DealMap/Cashflow 状态不变）
     */
    @PostMapping("/{actionNumber}/reject")
    public Result<Void> reject(@PathVariable String actionNumber, @RequestBody ActionApprovalDTO dto) {
        try {
            acDealService.rejectAction(actionNumber, dto.getApprover(), dto.getApprovalRemark());
            return Result.success();
        } catch (IllegalArgumentException e) {
            return Result.badRequest(e.getMessage());
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    private ActionVO toVO(Action action) {
        ActionVO vo = new ActionVO();
        BeanUtils.copyProperties(action, vo);
        return vo;
    }
}

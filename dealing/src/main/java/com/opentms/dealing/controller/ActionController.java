package com.opentms.dealing.controller;

import com.opentms.dealing.service.ActionService;
import com.opentms.dealing.vo.ActionVO;
import com.opentms.common.model.Result;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/dealing/actions")
@RequiredArgsConstructor
public class ActionController {

    private final ActionService actionService;

    @GetMapping("/page")
    public Result<Page<ActionVO>> page(
            @RequestParam(required = false) String dealType,
            @RequestParam(required = false) String actionType,
            @RequestParam(required = false) String actionStatus,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(actionService.queryPage(dealType, actionType, actionStatus, pageNum, pageSize));
    }

    @GetMapping("/{id}")
    public Result<ActionVO> getById(@PathVariable Long id) {
        ActionVO action = actionService.getActionById(id);
        if (action == null) {
            return Result.notFound("Action not found");
        }
        return Result.success(action);
    }

    @GetMapping("/by-deal/{dealNumber}")
    public Result<ActionVO> getByDealNumber(@PathVariable String dealNumber) {
        ActionVO action = actionService.getActionByDealNumber(dealNumber);
        if (action == null) {
            return Result.notFound("Action not found");
        }
        return Result.success(action);
    }
}
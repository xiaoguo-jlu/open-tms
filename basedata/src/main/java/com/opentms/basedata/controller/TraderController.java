package com.opentms.basedata.controller;

import com.opentms.basedata.dto.TraderDTO;
import com.opentms.basedata.entity.Trader;
import com.opentms.basedata.service.TraderService;
import com.opentms.basedata.vo.TraderVO;
import com.opentms.common.model.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/traders")
@RequiredArgsConstructor
public class TraderController {

    private final TraderService traderService;

    @GetMapping("/page")
    public Result<com.baomidou.mybatisplus.core.metadata.IPage<TraderVO>> page(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "20") int pageSize) {
        return Result.success(traderService.queryPage(keyword, status, pageNo, pageSize));
    }

    @GetMapping("/{id}")
    public Result<TraderVO> getById(@PathVariable Long id) {
        TraderVO vo = traderService.getById(id);
        return vo == null ? Result.notFound("Not found") : Result.success(vo);
    }

    @PostMapping
    public Result<Void> save(@RequestBody TraderDTO dto) {
        boolean success = traderService.save(dto);
        return success ? Result.success() : Result.error("Save failed");
    }

    @PutMapping
    public Result<Void> update(@RequestBody TraderDTO dto) {
        boolean success = traderService.update(dto);
        return success ? Result.success() : Result.error("Update failed");
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        boolean success = traderService.delete(id);
        return success ? Result.success() : Result.error("Delete failed");
    }

    @PostMapping("/batch-delete")
    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
        boolean success = traderService.batchDelete(ids);
        return success ? Result.success() : Result.error("Delete failed");
    }

    @GetMapping("/list")
    public Result<List<TraderVO>> list() {
        return Result.success(traderService.listAll());
    }
}

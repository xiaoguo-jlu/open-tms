package com.opentms.basedata.controller;

import com.opentms.basedata.dto.CounterpartyDTO;
import com.opentms.basedata.entity.Counterparty;
import com.opentms.basedata.service.CounterpartyService;
import com.opentms.basedata.vo.CounterpartyVO;
import com.opentms.common.model.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/counterparties")
@RequiredArgsConstructor
public class CounterpartyController {

    private final CounterpartyService counterpartyService;

    @GetMapping("/page")
    public Result<com.baomidou.mybatisplus.core.metadata.IPage<CounterpartyVO>> page(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String countryCode,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "20") int pageSize) {
        return Result.success(counterpartyService.queryPage(keyword, type, countryCode, status, pageNo, pageSize));
    }

    @GetMapping("/{id}")
    public Result<CounterpartyVO> getById(@PathVariable Long id) {
        CounterpartyVO vo = counterpartyService.getById(id);
        return vo == null ? Result.notFound("Not found") : Result.success(vo);
    }

    @PostMapping
    public Result<Void> save(@RequestBody CounterpartyDTO dto) {
        boolean success = counterpartyService.save(dto);
        return success ? Result.success() : Result.error("Save failed");
    }

    @PutMapping
    public Result<Void> update(@RequestBody CounterpartyDTO dto) {
        boolean success = counterpartyService.update(dto);
        return success ? Result.success() : Result.error("Update failed");
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        boolean success = counterpartyService.delete(id);
        return success ? Result.success() : Result.error("Delete failed");
    }

    @PostMapping("/batch-delete")
    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
        boolean success = counterpartyService.batchDelete(ids);
        return success ? Result.success() : Result.error("Delete failed");
    }

    @GetMapping("/list")
    public Result<List<CounterpartyVO>> list() {
        return Result.success(counterpartyService.listAll());
    }
}

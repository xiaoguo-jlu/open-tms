package com.opentms.basedata.controller;

import com.opentms.basedata.dto.BankDTO;
import com.opentms.basedata.entity.Bank;
import com.opentms.basedata.service.BankService;
import com.opentms.basedata.vo.BankVO;
import com.opentms.common.model.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/banks")
@RequiredArgsConstructor
public class BankController {

    private final BankService bankService;

    @GetMapping("/page")
    public Result<com.baomidou.mybatisplus.core.metadata.IPage<BankVO>> page(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String countryCode,
            @RequestParam(required = false) String bankType,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "20") int pageSize) {
        return Result.success(bankService.queryPage(keyword, countryCode, bankType, status, pageNo, pageSize));
    }

    @GetMapping("/{id}")
    public Result<BankVO> getById(@PathVariable Long id) {
        BankVO vo = bankService.getById(id);
        return vo == null ? Result.notFound("Not found") : Result.success(vo);
    }

    @PostMapping
    public Result<Void> save(@RequestBody BankDTO dto) {
        boolean success = bankService.save(dto);
        return success ? Result.success() : Result.error("Save failed");
    }

    @PutMapping
    public Result<Void> update(@RequestBody BankDTO dto) {
        boolean success = bankService.update(dto);
        return success ? Result.success() : Result.error("Update failed");
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        boolean success = bankService.delete(id);
        return success ? Result.success() : Result.error("Delete failed");
    }

    @PostMapping("/batch-delete")
    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
        boolean success = bankService.batchDelete(ids);
        return success ? Result.success() : Result.error("Delete failed");
    }

    @GetMapping("/list")
    public Result<List<BankVO>> list() {
        return Result.success(bankService.listAll());
    }
}

package com.opentms.basedata.controller;

import com.opentms.basedata.dto.CounterpartyAccountDTO;
import com.opentms.basedata.entity.CounterpartyAccount;
import com.opentms.basedata.service.CounterpartyAccountService;
import com.opentms.basedata.vo.CounterpartyAccountVO;
import com.opentms.common.model.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/counterparty-accounts")
@RequiredArgsConstructor
public class CounterpartyAccountController {

    private final CounterpartyAccountService counterpartyAccountService;

    @GetMapping("/page")
    public Result<com.baomidou.mybatisplus.core.metadata.IPage<CounterpartyAccountVO>> page(
            @RequestParam(required = false) Long counterpartyId,
            @RequestParam(required = false) String currency,
            @RequestParam(required = false) String accountType,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "20") int pageSize) {
        return Result.success(counterpartyAccountService.queryPage(counterpartyId, currency, accountType, status, pageNo, pageSize));
    }

    @GetMapping("/{id}")
    public Result<CounterpartyAccountVO> getById(@PathVariable Long id) {
        CounterpartyAccountVO vo = counterpartyAccountService.getById(id);
        return vo == null ? Result.notFound("Not found") : Result.success(vo);
    }

    @PostMapping
    public Result<Void> save(@RequestBody CounterpartyAccountDTO dto) {
        boolean success = counterpartyAccountService.save(dto);
        return success ? Result.success() : Result.error("Save failed");
    }

    @PutMapping
    public Result<Void> update(@RequestBody CounterpartyAccountDTO dto) {
        boolean success = counterpartyAccountService.update(dto);
        return success ? Result.success() : Result.error("Update failed");
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        boolean success = counterpartyAccountService.delete(id);
        return success ? Result.success() : Result.error("Delete failed");
    }

    @PostMapping("/batch-delete")
    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
        boolean success = counterpartyAccountService.batchDelete(ids);
        return success ? Result.success() : Result.error("Delete failed");
    }

    @GetMapping("/list")
    public Result<List<CounterpartyAccountVO>> list() {
        return Result.success(counterpartyAccountService.listAll());
    }
}

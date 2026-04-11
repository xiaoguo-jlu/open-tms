package com.opentms.basedata.controller;

import com.opentms.basedata.dto.CurrencyDTO;
import com.opentms.basedata.service.CurrencyService;
import com.opentms.basedata.vo.CurrencyVO;
import com.opentms.common.model.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/currencies")
@RequiredArgsConstructor
public class CurrencyController {

    private final CurrencyService currencyService;

    @GetMapping
    public Result<List<CurrencyVO>> list() {
        return Result.success(currencyService.listAll());
    }

    @GetMapping("/page")
    public Result<com.baomidou.mybatisplus.extension.plugins.pagination.Page<CurrencyVO>> page(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "20") int pageSize) {
        return Result.success(currencyService.queryPage(keyword, status, pageNo, pageSize));
    }

    @GetMapping("/{id}")
    public Result<CurrencyVO> getById(@PathVariable Long id) {
        return Result.success(currencyService.getById(id));
    }

    @GetMapping("/code/{code}")
    public Result<CurrencyVO> getByCode(@PathVariable String code) {
        CurrencyVO vo = currencyService.getByCode(code);
        return vo != null ? Result.success(vo) : Result.notFound("币种不存在");
    }

    @PostMapping
    public Result<CurrencyVO> save(@RequestBody CurrencyDTO dto) {
        return Result.success(currencyService.save(dto));
    }

    @PutMapping
    public Result<CurrencyVO> update(@RequestBody CurrencyDTO dto) {
        return Result.success(currencyService.updateById(dto));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        currencyService.removeById(id);
        return Result.success();
    }
}
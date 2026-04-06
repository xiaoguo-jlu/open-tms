package com.opentms.basedata.controller;

import com.opentms.basedata.dto.CountryDTO;
import com.opentms.basedata.entity.Country;
import com.opentms.basedata.service.CountryService;
import com.opentms.basedata.vo.CountryVO;
import com.opentms.common.model.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/countries")
@RequiredArgsConstructor
public class CountryController {

    private final CountryService countryService;

    @GetMapping("/page")
    public Result<com.baomidou.mybatisplus.core.metadata.IPage<CountryVO>> page(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "20") int pageSize) {
        return Result.success(countryService.queryPage(keyword, status, pageNo, pageSize));
    }

    @GetMapping("/{id}")
    public Result<CountryVO> getById(@PathVariable Long id) {
        CountryVO vo = countryService.getById(id);
        return vo == null ? Result.notFound("Not found") : Result.success(vo);
    }

    @PostMapping
    public Result<Void> save(@RequestBody CountryDTO dto) {
        boolean success = countryService.save(dto);
        return success ? Result.success() : Result.error("Save failed");
    }

    @PutMapping
    public Result<Void> update(@RequestBody CountryDTO dto) {
        boolean success = countryService.update(dto);
        return success ? Result.success() : Result.error("Update failed");
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        boolean success = countryService.delete(id);
        return success ? Result.success() : Result.error("Delete failed");
    }

    @PostMapping("/batch-delete")
    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
        boolean success = countryService.batchDelete(ids);
        return success ? Result.success() : Result.error("Delete failed");
    }

    @GetMapping("/list")
    public Result<List<CountryVO>> list() {
        return Result.success(countryService.listAll());
    }
}

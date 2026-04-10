package com.opentms.basedata.controller;

import com.opentms.basedata.entity.Country;
import com.opentms.basedata.service.CountryService;
import com.opentms.common.model.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/country")
@RequiredArgsConstructor
public class CountryController {

    private final CountryService countryService;

    @GetMapping("/page")
    public Result<com.baomidou.mybatisplus.extension.plugins.pagination.Page<Country>> page(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(countryService.queryPage(keyword, status, pageNum, pageSize));
    }

    @GetMapping("/{id}")
    public Result<Country> getById(@PathVariable Long id) {
        Country country = countryService.getCountryById(id);
        if (country == null) {
            return Result.notFound("Country not found");
        }
        return Result.success(country);
    }

    @PostMapping
    public Result<Void> save(@RequestBody Country country) {
        countryService.saveCountry(country);
        return Result.success();
    }

    @PutMapping
    public Result<Void> update(@RequestBody Country country) {
        countryService.updateCountry(country);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        countryService.deleteCountry(id);
        return Result.success();
    }
}
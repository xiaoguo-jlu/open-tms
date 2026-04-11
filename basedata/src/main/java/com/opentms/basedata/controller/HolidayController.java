package com.opentms.basedata.controller;

import com.opentms.basedata.entity.Holiday;
import com.opentms.basedata.service.HolidayService;
import com.opentms.common.model.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/holidays")
@RequiredArgsConstructor
public class HolidayController {

    private final HolidayService holidayService;

    @GetMapping("/page")
    public Result<com.baomidou.mybatisplus.extension.plugins.pagination.Page<Holiday>> page(
            @RequestParam(required = false) String countryCode,
            @RequestParam(required = false) Integer year,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(holidayService.queryPage(countryCode, year, pageNum, pageSize));
    }

    @GetMapping("/{id}")
    public Result<Holiday> getById(@PathVariable Long id) {
        Holiday holiday = holidayService.getHolidayById(id);
        if (holiday == null) {
            return Result.notFound("Holiday not found");
        }
        return Result.success(holiday);
    }

    @PostMapping
    public Result<Void> save(@RequestBody Holiday holiday) {
        holidayService.saveHoliday(holiday);
        return Result.success();
    }

    @PutMapping
    public Result<Void> update(@RequestBody Holiday holiday) {
        holidayService.updateHoliday(holiday);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        holidayService.deleteHoliday(id);
        return Result.success();
    }
}
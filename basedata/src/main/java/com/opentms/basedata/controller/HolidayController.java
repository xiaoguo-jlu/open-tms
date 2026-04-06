package com.opentms.basedata.controller;

import com.opentms.basedata.dto.HolidayDTO;
import com.opentms.basedata.service.HolidayService;
import com.opentms.basedata.vo.HolidayVO;
import com.opentms.common.model.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/holidays")
@RequiredArgsConstructor
public class HolidayController {

    private final HolidayService holidayService;

    @GetMapping("/page")
    public Result<com.baomidou.mybatisplus.core.metadata.IPage<HolidayVO>> page(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String countryCode,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "20") int pageSize) {
        return Result.success(holidayService.queryPage(year, countryCode, pageNo, pageSize));
    }

    @GetMapping("/{id}")
    public Result<HolidayVO> getById(@PathVariable Long id) {
        HolidayVO vo = holidayService.getById(id);
        return vo == null ? Result.notFound("Not found") : Result.success(vo);
    }

    @PostMapping
    public Result<Void> save(@RequestBody HolidayDTO dto) {
        boolean success = holidayService.save(dto);
        return success ? Result.success() : Result.error("Save failed");
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        boolean success = holidayService.delete(id);
        return success ? Result.success() : Result.error("Delete failed");
    }

    @PostMapping("/batch-delete")
    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
        boolean success = holidayService.batchDelete(ids);
        return success ? Result.success() : Result.error("Delete failed");
    }
}

package com.opentms.instrument.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.opentms.common.model.Result;
import com.opentms.instrument.entity.Instrument;
import com.opentms.instrument.service.InstrumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/instruments")
@RequiredArgsConstructor
public class InstrumentController {

    private final InstrumentService instrumentService;

    @GetMapping("/page")
    public Result<Page<Instrument>> page(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String instrumentType,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(instrumentService.queryPage(keyword, instrumentType, status, pageNum, pageSize));
    }

    @GetMapping("/{id}")
    public Result<Instrument> getById(@PathVariable Long id) {
        Instrument instrument = instrumentService.getInstrumentById(id);
        if (instrument == null) {
            return Result.notFound("Instrument not found");
        }
        return Result.success(instrument);
    }

    @PostMapping
    public Result<Void> save(@RequestBody Instrument instrument) {
        boolean success = instrumentService.saveInstrument(instrument);
        return success ? Result.success() : Result.error("Save failed");
    }

    @PutMapping
    public Result<Void> update(@RequestBody Instrument instrument) {
        boolean success = instrumentService.updateInstrument(instrument);
        return success ? Result.success() : Result.error("Update failed");
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        boolean success = instrumentService.deleteInstrument(id);
        return success ? Result.success() : Result.error("Delete failed");
    }

    @PostMapping("/batch-delete")
    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
        boolean success = instrumentService.batchDelete(ids);
        return success ? Result.success() : Result.error("Batch delete failed");
    }
}
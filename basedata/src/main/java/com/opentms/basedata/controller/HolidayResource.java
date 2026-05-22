package com.opentms.basedata.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.opentms.basedata.entity.Holiday;
import com.opentms.basedata.service.HolidayService;
import jakarta.ws.rs.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Path("/api/v1/holidays")
public class HolidayResource {

    @Autowired
    private HolidayService holidayService;

    public HolidayResource() {
    }

    @GET
    @Path("/page")
    public Object page(
            @QueryParam("countryCode") String countryCode,
            @QueryParam("year") Integer year,
            @QueryParam("pageNum") @DefaultValue("1") int pageNum,
            @QueryParam("pageSize") @DefaultValue("10") int pageSize) {
        return holidayService.queryPage(countryCode, year, pageNum, pageSize);
    }

    @GET
    @Path("/{id}")
    public Object getById(@PathParam("id") Long id) {
        Holiday holiday = holidayService.getHolidayById(id);
        if (holiday == null) {
            return com.opentms.common.model.Result.notFound("Holiday not found");
        }
        return com.opentms.common.model.Result.success(holiday);
    }

    @POST
    public Object save(Holiday holiday) {
        try {
            holidayService.saveHoliday(holiday);
            return com.opentms.common.model.Result.success();
        } catch (Exception e) {
            return com.opentms.common.model.Result.error(e.getMessage());
        }
    }

    @PUT
    public Object update(Holiday holiday) {
        try {
            holidayService.updateHoliday(holiday);
            return com.opentms.common.model.Result.success();
        } catch (Exception e) {
            return com.opentms.common.model.Result.error(e.getMessage());
        }
    }

    @DELETE
    @Path("/{id}")
    public Object delete(@PathParam("id") Long id) {
        try {
            holidayService.deleteHoliday(id);
            return com.opentms.common.model.Result.success();
        } catch (Exception e) {
            return com.opentms.common.model.Result.error(e.getMessage());
        }
    }
}
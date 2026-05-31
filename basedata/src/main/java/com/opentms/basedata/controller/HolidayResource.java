package com.opentms.basedata.controller;

import com.opentms.basedata.dto.HolidayDTO;
import com.opentms.basedata.entity.Holiday;
import com.opentms.basedata.service.HolidayService;
import com.opentms.common.model.Result;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Path("/api/v1/holidays")
@Produces(MediaType.APPLICATION_JSON)
public class HolidayResource {

    @Autowired
    private HolidayService holidayService;

    @GET
    public Object list() {
        return Result.success(holidayService.listAll());
    }

    @GET
    @Path("/page")
    public Object page(
            @QueryParam("countryCode") String countryCode,
            @QueryParam("year") Integer year,
            @QueryParam("pageNum") @DefaultValue("1") int pageNum,
            @QueryParam("pageSize") @DefaultValue("10") int pageSize) {
        return Result.success(holidayService.queryPage(countryCode, year, pageNum, pageSize));
    }

    @GET
    @Path("/{id}")
    public Object getById(@PathParam("id") Long id) {
        if (id == null || id <= 0) {
            return Result.badRequest("ID必须为正整数");
        }
        Holiday holiday = holidayService.getHolidayById(id);
        if (holiday == null) {
            return Result.notFound("节假日不存在");
        }
        return Result.success(holiday);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Object save(HolidayDTO dto) {
        try {
            Holiday entity = new Holiday();
            entity.setHolidayDate(dto.getHolidayDate());
            entity.setName(dto.getName());
            entity.setCountryCode(dto.getCountryCode());
            entity.setIsAdjacent(dto.getIsAdjacent());
            entity.setRemark(dto.getRemark());
            holidayService.saveHoliday(entity);
            return Result.success(entity);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @POST
    @Path("/update")
    @Consumes(MediaType.APPLICATION_JSON)
    public Object update(HolidayDTO dto) {
        try {
            if (dto.getId() == null) {
                return Result.badRequest("ID不能为空");
            }
            Holiday entity = holidayService.getHolidayById(dto.getId());
            if (entity == null) {
                return Result.notFound("节假日不存在");
            }
            entity.setHolidayDate(dto.getHolidayDate());
            entity.setName(dto.getName());
            entity.setCountryCode(dto.getCountryCode());
            entity.setIsAdjacent(dto.getIsAdjacent());
            entity.setRemark(dto.getRemark());
            holidayService.updateHoliday(entity);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @POST
    @Path("/delete/{id}")
    public Object delete(@PathParam("id") Long id) {
        try {
            if (id == null || id <= 0) {
                return Result.badRequest("ID必须为正整数");
            }
            holidayService.deleteHoliday(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
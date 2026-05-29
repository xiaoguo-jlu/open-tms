package com.opentms.basedata.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.opentms.basedata.entity.BusinessUnit;
import com.opentms.basedata.service.BusinessUnitService;
import com.opentms.common.model.Result;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Path("/api/v1/business-units")
@Produces(MediaType.APPLICATION_JSON)
public class BusinessUnitResource {

    @Autowired
    private BusinessUnitService businessUnitService;

    @GET
    @Path("/page")
    public Object page(
            @QueryParam("keyword") String keyword,
            @QueryParam("status") String status,
            @QueryParam("pageNum") @DefaultValue("1") int pageNum,
            @QueryParam("pageSize") @DefaultValue("10") int pageSize) {
        return Result.success(businessUnitService.queryPage(keyword, status, pageNum, pageSize));
    }

    @GET
    @Path("/{id}")
    public Object getById(@PathParam("id") String id) {
        try {
            long parseId = Long.parseLong(id);
            if (parseId <= 0) {
                return Result.badRequest("ID必须为正整数");
            }
            BusinessUnit businessUnit = businessUnitService.getBusinessUnitById(parseId);
            if (businessUnit == null) {
                return Result.notFound("Business unit not found");
            }
            return Result.success(businessUnit);
        } catch (NumberFormatException e) {
            return Result.badRequest("ID参数格式不正确");
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Object save(BusinessUnit businessUnit) {
        try {
            businessUnitService.saveBusinessUnit(businessUnit);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Object update(BusinessUnit businessUnit) {
        try {
            businessUnitService.updateBusinessUnit(businessUnit);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DELETE
    @Path("/{id}")
    public Object delete(@PathParam("id") String id) {
        try {
            long parseId = Long.parseLong(id);
            if (parseId <= 0) {
                return Result.badRequest("ID必须为正整数");
            }
            businessUnitService.deleteBusinessUnit(parseId);
            return Result.success();
        } catch (NumberFormatException e) {
            return Result.badRequest("ID参数格式不正确");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
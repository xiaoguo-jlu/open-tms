package com.opentms.basedata.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.opentms.basedata.entity.BusinessUnit;
import com.opentms.basedata.service.BusinessUnitService;
import jakarta.ws.rs.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Path("/api/v1/business-units")
public class BusinessUnitResource {

    @Autowired
    private BusinessUnitService businessUnitService;

    public BusinessUnitResource() {
    }

    @GET
    @Path("/page")
    public Object page(
            @QueryParam("keyword") String keyword,
            @QueryParam("status") String status,
            @QueryParam("pageNum") @DefaultValue("1") int pageNum,
            @QueryParam("pageSize") @DefaultValue("10") int pageSize) {
        return businessUnitService.queryPage(keyword, status, pageNum, pageSize);
    }

    @GET
    @Path("/{id}")
    public Object getById(@PathParam("id") Long id) {
        BusinessUnit businessUnit = businessUnitService.getBusinessUnitById(id);
        if (businessUnit == null) {
            return com.opentms.common.model.Result.notFound("Business unit not found");
        }
        return com.opentms.common.model.Result.success(businessUnit);
    }

    @POST
    public Object save(BusinessUnit businessUnit) {
        try {
            businessUnitService.saveBusinessUnit(businessUnit);
            return com.opentms.common.model.Result.success();
        } catch (Exception e) {
            return com.opentms.common.model.Result.error(e.getMessage());
        }
    }

    @PUT
    public Object update(BusinessUnit businessUnit) {
        try {
            businessUnitService.updateBusinessUnit(businessUnit);
            return com.opentms.common.model.Result.success();
        } catch (Exception e) {
            return com.opentms.common.model.Result.error(e.getMessage());
        }
    }

    @DELETE
    @Path("/{id}")
    public Object delete(@PathParam("id") Long id) {
        try {
            businessUnitService.deleteBusinessUnit(id);
            return com.opentms.common.model.Result.success();
        } catch (Exception e) {
            return com.opentms.common.model.Result.error(e.getMessage());
        }
    }
}
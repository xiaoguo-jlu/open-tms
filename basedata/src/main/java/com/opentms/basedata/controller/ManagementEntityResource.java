package com.opentms.basedata.controller;

import com.opentms.basedata.entity.BusinessUnit;
import com.opentms.basedata.service.BusinessUnitService;
import com.opentms.basedata.vo.BusinessUnitVO;
import com.opentms.common.model.Result;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Path("/api/v1/management-entities")
@Produces(MediaType.APPLICATION_JSON)
public class BusinessUnitResource {

    @Autowired
    private BusinessUnitService businessUnitService;

    @GET
    @Path("/page")
    public Object page(
            @QueryParam("keyword") String keyword,
            @QueryParam("status") String status,
            @QueryParam("entityType") String entityType,
            @QueryParam("pageNum") @DefaultValue("1") int pageNum,
            @QueryParam("pageSize") @DefaultValue("10") int pageSize) {
        return Result.success(businessUnitService.queryPage(keyword, status, entityType, pageNum, pageSize));
    }

    @GET
    @Path("/tree")
    public Object tree() {
        List<BusinessUnitVO> tree = businessUnitService.getHierarchyTree();
        return Result.success(tree);
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
                return Result.notFound("Management entity not found");
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

    @POST
    @Path("/update")
    @Consumes(MediaType.APPLICATION_JSON)
    public Object update(BusinessUnit businessUnit) {
        try {
            businessUnitService.updateBusinessUnit(businessUnit);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @POST
    @Path("/delete/{id}")
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
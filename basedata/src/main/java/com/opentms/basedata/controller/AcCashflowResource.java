package com.opentms.basedata.controller;

import com.opentms.basedata.dto.AcCashflowDTO;
import com.opentms.basedata.service.AcCashflowService;
import com.opentms.basedata.vo.AcCashflowVO;
import com.opentms.common.model.Result;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Path("/api/v1/ac/cashflows")
public class AcCashflowResource {

    @Autowired
    private AcCashflowService acCashflowService;

    public AcCashflowResource() {
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Object page(
            @QueryParam("keyword") String keyword,
            @QueryParam("status") String status,
            @QueryParam("bankAccount") String bankAccount,
            @QueryParam("direction") String direction,
            @QueryParam("pageNum") @DefaultValue("1") int pageNum,
            @QueryParam("pageSize") @DefaultValue("10") int pageSize) {
        AcCashflowDTO dto = new AcCashflowDTO();
        dto.setKeyword(keyword);
        dto.setStatus(status);
        dto.setBankAccount(bankAccount);
        dto.setDirection(direction);
        return acCashflowService.queryPage(dto, pageNum, pageSize);
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Object getById(@PathParam("id") String id) {
        try {
            long parseId = Long.parseLong(id);
            if (parseId <= 0) {
                return Result.badRequest("ID必须为正整数");
            }
            AcCashflowVO vo = acCashflowService.getById(parseId);
            return vo != null ?
                    Result.success(vo) :
                    Result.notFound("现金流水不存在");
        } catch (NumberFormatException e) {
            return Result.badRequest("ID参数格式不正确");
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Object save(AcCashflowDTO dto) {
        try {
            return Result.success(acCashflowService.save(dto));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Object update(AcCashflowDTO dto) {
        try {
            if (dto.getId() == null) {
                return Result.badRequest("ID不能为空");
            }
            return Result.success(acCashflowService.updateById(dto));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Object delete(@PathParam("id") String id) {
        try {
            long parseId = Long.parseLong(id);
            if (parseId <= 0) {
                return Result.badRequest("ID必须为正整数");
            }
            acCashflowService.removeById(parseId);
            return Result.success();
        } catch (NumberFormatException e) {
            return Result.badRequest("ID参数格式不正确");
        }
    }

    @POST
    @Path("/{id}/confirm")
    @Produces(MediaType.APPLICATION_JSON)
    public Object confirm(@PathParam("id") String id) {
        try {
            long parseId = Long.parseLong(id);
            if (parseId <= 0) {
                return Result.badRequest("ID必须为正整数");
            }
            return Result.success(acCashflowService.confirm(parseId));
        } catch (NumberFormatException e) {
            return Result.badRequest("ID参数格式不正确");
        }
    }
}

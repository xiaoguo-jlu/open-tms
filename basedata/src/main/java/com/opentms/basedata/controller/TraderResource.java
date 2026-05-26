package com.opentms.basedata.controller;

import com.opentms.basedata.dto.TraderDTO;
import com.opentms.basedata.service.TraderService;
import com.opentms.basedata.vo.TraderVO;
import com.opentms.common.model.Result;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Path("/api/v1/traders")
public class TraderResource {

    @Autowired
    private TraderService traderService;

    public TraderResource() {
    }

    @GET
    @Path("/page")
    @Produces(MediaType.APPLICATION_JSON)
    public Object page(
            @QueryParam("keyword") String keyword,
            @QueryParam("status") String status,
            @QueryParam("pageNum") @DefaultValue("1") int pageNum,
            @QueryParam("pageSize") @DefaultValue("10") int pageSize) {
        TraderDTO dto = new TraderDTO();
        dto.setKeyword(keyword);
        dto.setStatus(status);
        return traderService.queryPage(dto, pageNum, pageSize);
    }

    @GET
    @Path("/{id}")
    public Object getById(@PathParam("id") String id) {
        try {
            long parseId = Long.parseLong(id);
            if (parseId <= 0) {
                return Result.badRequest("ID必须为正整数");
            }
            TraderVO vo = traderService.getById(parseId);
            return vo != null ?
                Result.success(vo) :
                Result.notFound("交易员不存在");
        } catch (NumberFormatException e) {
            return Result.badRequest("ID参数格式不正确");
        }
    }

    @POST
    public Object save(TraderDTO dto) {
        try {
            return Result.success(traderService.save(dto));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PUT
    public Object update(TraderDTO dto) {
        try {
            if (dto.getId() == null) {
                return Result.badRequest("ID不能为空");
            }
            return Result.success(traderService.updateById(dto));
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
            traderService.removeById(parseId);
            return Result.success();
        } catch (NumberFormatException e) {
            return Result.badRequest("ID参数格式不正确");
        }
    }

    @POST
    @Path("/batch-delete")
    public Object batchDelete(java.util.Map<String, java.util.List<Long>> body) {
        try {
            java.util.List<Long> ids = body.get("ids");
            if (ids == null || ids.isEmpty()) {
                return Result.badRequest("ID列表不能为空");
            }
            for (Long id : ids) {
                traderService.removeById(id);
            }
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}

package com.opentms.basedata.controller;

import com.opentms.basedata.dto.AtTransferDTO;
import com.opentms.basedata.service.AtTransferService;
import com.opentms.basedata.vo.AtTransferVO;
import com.opentms.common.model.Result;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Path("/api/v1/transfer/transactions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AtTransferResource {

    @Autowired
    private AtTransferService atTransferService;

    @GET
    public Object list(
            @QueryParam("keyword") String keyword,
            @QueryParam("status") String status,
            @QueryParam("pageNum") @DefaultValue("1") int pageNum,
            @QueryParam("pageSize") @DefaultValue("10") int pageSize) {
        AtTransferDTO dto = new AtTransferDTO();
        dto.setKeyword(keyword);
        dto.setStatus(status);
        return atTransferService.queryPage(dto, pageNum, pageSize);
    }

    @GET
    @Path("/{id}")
    public Object getById(@PathParam("id") Long id) {
        AtTransferVO vo = atTransferService.getById(id);
        if (vo == null) {
            return Result.notFound("AT记录不存在");
        }
        return Result.success(vo);
    }

    @POST
    public Object save(AtTransferDTO dto) {
        try {
            return Result.success(atTransferService.save(dto));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PUT
    public Object update(AtTransferDTO dto) {
        try {
            if (dto.getId() == null) {
                return Result.badRequest("ID不能为空");
            }
            return Result.success(atTransferService.update(dto));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DELETE
    @Path("/{id}")
    public Object delete(@PathParam("id") Long id) {
        try {
            atTransferService.delete(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @POST
    @Path("/{id}/submit")
    public Object submit(@PathParam("id") Long id) {
        try {
            atTransferService.submit(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @POST
    @Path("/{id}/execute")
    public Object execute(@PathParam("id") Long id) {
        try {
            atTransferService.execute(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @POST
    @Path("/{id}/cancel")
    public Object cancel(@PathParam("id") Long id) {
        try {
            atTransferService.cancel(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}

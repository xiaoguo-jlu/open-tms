package com.opentms.basedata.controller;

import com.opentms.basedata.dto.CurrencyDTO;
import com.opentms.basedata.service.CurrencyService;
import com.opentms.basedata.vo.CurrencyVO;
import jakarta.ws.rs.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Path("/api/v1/currencies")
public class CurrencyResource {

    @Autowired
    private CurrencyService currencyService;

    public CurrencyResource() {
    }

    @GET
    @Produces(jakarta.ws.rs.core.MediaType.APPLICATION_JSON)
    public Object list() {
        return com.opentms.common.model.Result.success(currencyService.listAll());
    }

    @GET
    @Path("/{id}")
    public Object getById(@PathParam("id") String id) {
        try {
            long parseId = Long.parseLong(id);
            if (parseId <= 0) {
                return com.opentms.common.model.Result.badRequest("ID必须为正整数");
            }
            CurrencyVO vo = currencyService.getById(parseId);
            return vo != null ? 
                com.opentms.common.model.Result.success(vo) : 
                com.opentms.common.model.Result.notFound("币种不存在");
        } catch (NumberFormatException e) {
            return com.opentms.common.model.Result.badRequest("ID参数格式不正确");
        }
    }

    @POST
    public Object save(CurrencyDTO dto) {
        try {
            return com.opentms.common.model.Result.success(currencyService.save(dto));
        } catch (Exception e) {
            return com.opentms.common.model.Result.error(e.getMessage());
        }
    }

    @PUT
    public Object update(CurrencyDTO dto) {
        try {
            if (dto.getId() == null) {
                return com.opentms.common.model.Result.badRequest("ID不能为空");
            }
            return com.opentms.common.model.Result.success(currencyService.updateById(dto));
        } catch (Exception e) {
            return com.opentms.common.model.Result.error(e.getMessage());
        }
    }

    @DELETE
    @Path("/{id}")
    public Object delete(@PathParam("id") String id) {
        try {
            long parseId = Long.parseLong(id);
            if (parseId <= 0) {
                return com.opentms.common.model.Result.badRequest("ID必须为正整数");
            }
            currencyService.removeById(parseId);
            return com.opentms.common.model.Result.success();
        } catch (NumberFormatException e) {
            return com.opentms.common.model.Result.badRequest("ID参数格式不正确");
        }
    }
}
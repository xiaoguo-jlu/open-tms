package com.opentms.basedata.controller;

import com.opentms.basedata.dto.CurrencyDTO;
import com.opentms.basedata.service.CurrencyService;
import com.opentms.basedata.vo.CurrencyVO;
import com.opentms.common.model.Result;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Path("/api/v1/currencies")
@Produces(MediaType.APPLICATION_JSON)
public class CurrencyResource {

    @Autowired
    private CurrencyService currencyService;

    @GET
    @Path("/page")
    @Produces(MediaType.APPLICATION_JSON)
    public Object page(
            @QueryParam("keyword") String keyword,
            @QueryParam("status") String status,
            @QueryParam("pageNum") @DefaultValue("1") int pageNum,
            @QueryParam("pageSize") @DefaultValue("10") int pageSize) {
        return Result.success(currencyService.queryPage(keyword, status, pageNum, pageSize));
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Object list() {
        return Result.success(currencyService.listAll());
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
            CurrencyVO vo = currencyService.getById(parseId);
            return vo != null ?
                Result.success(vo) :
                Result.notFound("币种不存在");
        } catch (NumberFormatException e) {
            return Result.badRequest("ID参数格式不正确");
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Object save(CurrencyDTO dto) {
        try {
            return Result.success(currencyService.save(dto));
        } catch (CurrencyServiceImpl.BusinessException e) {
            return Result.badRequest(e.getMessage());
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @POST
    @Path("/update")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Object update(CurrencyDTO dto) {
        try {
            if (dto.getId() == null) {
                return Result.badRequest("ID不能为空");
            }
            return Result.success(currencyService.updateById(dto));
        } catch (CurrencyServiceImpl.BusinessException e) {
            return Result.badRequest(e.getMessage());
        } catch (RuntimeException e) {
            String msg = e.getMessage();
            return Result.error(msg != null ? msg : e.getClass().getSimpleName());
        } catch (Exception e) {
            String msg = e.getMessage();
            if (msg == null) {
                StringBuilder sb = new StringBuilder();
                sb.append(e.getClass().getSimpleName());
                Throwable cause = e.getCause();
                if (cause != null) {
                    sb.append(": ").append(cause.getClass().getSimpleName());
                    if (cause.getMessage() != null) {
                        sb.append(" - ").append(cause.getMessage());
                    }
                }
                msg = sb.toString();
            }
            return Result.error(msg);
        }
    }

    @POST
    @Path("/delete/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Object delete(@PathParam("id") String id) {
        try {
            long parseId = Long.parseLong(id);
            if (parseId <= 0) {
                return Result.badRequest("ID必须为正整数");
            }
            currencyService.removeById(parseId);
            return Result.success();
        } catch (CurrencyServiceImpl.BusinessException e) {
            return Result.badRequest(e.getMessage());
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("删除失败: " + e.getMessage());
        }
    }
}
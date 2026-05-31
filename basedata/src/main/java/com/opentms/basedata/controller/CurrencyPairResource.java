package com.opentms.basedata.controller;

import com.opentms.basedata.dto.CurrencyPairDTO;
import com.opentms.basedata.service.CurrencyPairService;
import com.opentms.basedata.service.impl.CurrencyPairServiceImpl;
import com.opentms.basedata.vo.CurrencyPairVO;
import com.opentms.common.model.Result;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Path("/api/v1/currency-pairs")
@Produces(MediaType.APPLICATION_JSON)
public class CurrencyPairResource {

    @Autowired
    private CurrencyPairService currencyPairService;

    @GET
    @Path("/page")
    @Produces(MediaType.APPLICATION_JSON)
    public Object page(
            @QueryParam("keyword") String keyword,
            @QueryParam("status") String status,
            @QueryParam("pageNum") @DefaultValue("1") int pageNum,
            @QueryParam("pageSize") @DefaultValue("10") int pageSize) {
        return Result.success(currencyPairService.queryPage(keyword, status, pageNum, pageSize));
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Object list() {
        return Result.success(currencyPairService.listAll());
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
            CurrencyPairVO vo = currencyPairService.getById(parseId);
            return vo != null ?
                Result.success(vo) :
                Result.notFound("币种对不存在");
        } catch (NumberFormatException e) {
            return Result.badRequest("ID参数格式不正确");
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Object save(CurrencyPairDTO dto) {
        try {
            return Result.success(currencyPairService.save(dto));
        } catch (CurrencyPairServiceImpl.BusinessException e) {
            return Result.badRequest(e.getMessage());
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @POST
    @Path("/update")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Object update(CurrencyPairDTO dto) {
        try {
            if (dto.getId() == null) {
                return Result.badRequest("ID不能为空");
            }
            return Result.success(currencyPairService.updateById(dto));
        } catch (CurrencyPairServiceImpl.BusinessException e) {
            return Result.badRequest(e.getMessage());
        } catch (RuntimeException e) {
            String msg = e.getMessage();
            return Result.error(msg != null ? msg : e.getClass().getSimpleName());
        } catch (Exception e) {
            return Result.error(e.getMessage());
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
            currencyPairService.removeById(parseId);
            return Result.success();
        } catch (CurrencyPairServiceImpl.BusinessException e) {
            return Result.badRequest(e.getMessage());
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("删除失败: " + e.getMessage());
        }
    }
}
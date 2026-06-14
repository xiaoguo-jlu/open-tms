package com.opentms.basedata.controller;

import com.opentms.basedata.dto.InstrumentDTO;
import com.opentms.basedata.service.InstrumentService;
import com.opentms.basedata.service.impl.InstrumentServiceImpl;
import com.opentms.basedata.vo.InstrumentVO;
import com.opentms.common.model.Result;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Path("/api/v1/instruments")
@Produces(MediaType.APPLICATION_JSON)
public class InstrumentResource {

    @Autowired
    private InstrumentService instrumentService;

    @GET
    @Path("/page")
    @Produces(MediaType.APPLICATION_JSON)
    public Object page(
            @QueryParam("keyword") String keyword,
            @QueryParam("instrumentType") String instrumentType,
            @QueryParam("status") String status,
            @QueryParam("pageNum") @DefaultValue("1") int pageNum,
            @QueryParam("pageSize") @DefaultValue("10") int pageSize) {
        return Result.success(instrumentService.queryPage(keyword, instrumentType, status, pageNum, pageSize));
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Object list() {
        return Result.success(instrumentService.listAll());
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
            InstrumentVO vo = instrumentService.getById(parseId);
            return vo != null ?
                Result.success(vo) :
                Result.notFound("金融工具不存在");
        } catch (NumberFormatException e) {
            return Result.badRequest("ID参数格式不正确");
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Object save(InstrumentDTO dto) {
        try {
            return Result.success(instrumentService.save(dto));
        } catch (InstrumentServiceImpl.BusinessException e) {
            return Result.badRequest(e.getMessage());
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @POST
    @Path("/update")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Object update(InstrumentDTO dto) {
        try {
            if (dto.getId() == null) {
                return Result.badRequest("ID不能为空");
            }
            return Result.success(instrumentService.updateById(dto));
        } catch (InstrumentServiceImpl.BusinessException e) {
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
            instrumentService.removeById(parseId);
            return Result.success();
        } catch (InstrumentServiceImpl.BusinessException e) {
            return Result.badRequest(e.getMessage());
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("删除失败: " + e.getMessage());
        }
    }

    @POST
    @Path("/batch-delete")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Object batchDelete(List<Long> ids) {
        try {
            instrumentService.batchDelete(ids);
            return Result.success();
        } catch (InstrumentServiceImpl.BusinessException e) {
            return Result.badRequest(e.getMessage());
        } catch (Exception e) {
            return Result.error("批量删除失败: " + e.getMessage());
        }
    }
}
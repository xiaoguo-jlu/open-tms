package com.opentms.basedata.controller;

import com.opentms.basedata.dto.SubsidiaryDTO;
import com.opentms.basedata.service.SubsidiaryService;
import com.opentms.basedata.vo.SubsidiaryVO;
import com.opentms.common.model.Result;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 子公司Controller
 */
@Component
@Path("/api/v1/subsidiaries")
@Produces(MediaType.APPLICATION_JSON)
public class SubsidiaryResource {

    @Autowired
    private SubsidiaryService subsidiaryService;

    /**
     * 分页查询
     */
    @GET
    @Path("/page")
    public Object page(
            @QueryParam("keyword") String keyword,
            @QueryParam("status") String status,
            @QueryParam("pageNum") @DefaultValue("1") int pageNum,
            @QueryParam("pageSize") @DefaultValue("10") int pageSize) {
        return Result.success(subsidiaryService.queryPage(keyword, status, pageNum, pageSize));
    }

    /**
     * 根据ID查询
     */
    @GET
    @Path("/{id}")
    public Object getById(@PathParam("id") String id) {
        try {
            long parseId = Long.parseLong(id);
            if (parseId <= 0) {
                return Result.badRequest("ID必须为正整数");
            }
            SubsidiaryVO vo = subsidiaryService.getSubsidiaryById(parseId);
            if (vo == null) {
                return Result.notFound("子公司不存在");
            }
            return Result.success(vo);
        } catch (NumberFormatException e) {
            return Result.badRequest("ID参数格式不正确");
        }
    }

    /**
     * 检查编码是否存在
     */
    @GET
    @Path("/check-code")
    public Object checkCodeExists(@QueryParam("code") String code, @QueryParam("excludeId") String excludeId) {
        boolean exists = subsidiaryService.checkCodeExists(code, excludeId != null ? Long.parseLong(excludeId) : null);
        return Result.success(Map.of("exists", exists));
    }

    /**
     * 保存
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Object save(SubsidiaryDTO dto) {
        try {
            return Result.success(subsidiaryService.saveSubsidiary(dto));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新
     */
    @POST
    @Path("/update")
    @Consumes(MediaType.APPLICATION_JSON)
    public Object update(SubsidiaryDTO dto) {
        try {
            return Result.success(subsidiaryService.updateSubsidiary(dto));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除
     */
    @POST
    @Path("/delete/{id}")
    public Object delete(@PathParam("id") String id) {
        try {
            long parseId = Long.parseLong(id);
            if (parseId <= 0) {
                return Result.badRequest("ID必须为正整数");
            }
            subsidiaryService.deleteSubsidiary(parseId);
            return Result.success();
        } catch (NumberFormatException e) {
            return Result.badRequest("ID参数格式不正确");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
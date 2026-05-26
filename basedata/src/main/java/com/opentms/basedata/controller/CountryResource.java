package com.opentms.basedata.controller;
import com.opentms.basedata.dto.CountryDTO;
import com.opentms.basedata.service.CountryService;
import com.opentms.basedata.vo.CountryVO;
import com.opentms.common.model.Result;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Path("/api/v1/countries")
public class CountryResource {
    @Autowired
    private CountryService countryService;
    public CountryResource() {}
    @GET @Path("/page") @Produces(MediaType.APPLICATION_JSON)
    public Object page(@QueryParam("keyword") String keyword, @QueryParam("status") String status,
            @QueryParam("pageNum") @DefaultValue("1") int pageNum, @QueryParam("pageSize") @DefaultValue("10") int pageSize) {
        CountryDTO dto = new CountryDTO(); dto.setKeyword(keyword); dto.setStatus(status);
        return countryService.queryPage(dto, pageNum, pageSize);
    }
    @GET @Path("/{id}")
    public Object getById(@PathParam("id") String id) {
        try { long parseId = Long.parseLong(id);
            if (parseId <= 0) { return Result.badRequest("ID必须为正整数"); }
            CountryVO vo = countryService.getById(parseId);
            return vo != null ? Result.success(vo) : Result.notFound("国家不存在");
        } catch (NumberFormatException e) { return Result.badRequest("ID参数格式不正确"); }
    }
    @POST
    public Object save(CountryDTO dto) {
        try { return Result.success(countryService.save(dto)); }
        catch (Exception e) { return Result.error(e.getMessage()); }
    }
    @PUT
    public Object update(CountryDTO dto) {
        try {
            if (dto.getId() == null) { return Result.badRequest("ID不能为空"); }
            return Result.success(countryService.updateById(dto));
        } catch (Exception e) { return Result.error(e.getMessage()); }
    }
    @DELETE @Path("/{id}")
    public Object delete(@PathParam("id") String id) {
        try { long parseId = Long.parseLong(id);
            if (parseId <= 0) { return Result.badRequest("ID必须为正整数"); }
            countryService.removeById(parseId); return Result.success();
        } catch (NumberFormatException e) { return Result.badRequest("ID参数格式不正确"); }
    }
    @POST @Path("/batch-delete")
    public Object batchDelete(java.util.Map<String, java.util.List<Long>> body) {
        try { java.util.List<Long> ids = body.get("ids");
            if (ids == null || ids.isEmpty()) { return Result.badRequest("ID列表不能为空"); }
            for (Long id : ids) { countryService.removeById(id); }
            return Result.success();
        } catch (Exception e) { return Result.error(e.getMessage()); }
    }
}

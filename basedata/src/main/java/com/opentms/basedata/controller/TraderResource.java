package com.opentms.basedata.controller;

import com.opentms.basedata.entity.Trader;
import com.opentms.basedata.service.TraderService;
import com.opentms.basedata.vo.TraderVO;
import com.opentms.common.model.Result;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Path("/api/v1/traders")
@Produces(MediaType.APPLICATION_JSON)
public class TraderResource {

    @Autowired
    private TraderService traderService;

    @GET
    public Object list() {
        return Result.success(traderService.listAll());
    }

    @GET
    @Path("/page")
    public Object page(
            @QueryParam("keyword") String keyword,
            @QueryParam("status") String status,
            @QueryParam("pageNum") @DefaultValue("1") int pageNum,
            @QueryParam("pageSize") @DefaultValue("10") int pageSize) {
        return Result.success(traderService.queryPage(keyword, status, pageNum, pageSize));
    }

    @GET
    @Path("/{id}")
    public Object getById(@PathParam("id") String idStr) {
        try {
            long id = Long.parseLong(idStr);
            if (id <= 0) {
                return Result.badRequest("ID must be positive");
            }
            TraderVO trader = traderService.getTraderById(id);
            if (trader == null) {
                return Result.notFound("Trader not found");
            }
            return Result.success(trader);
        } catch (NumberFormatException e) {
            return Result.badRequest("Invalid ID format");
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Object save(Trader trader) {
        try {
            traderService.saveTrader(trader);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @POST
    @Path("/update")
    @Consumes(MediaType.APPLICATION_JSON)
    public Object update(Trader trader) {
        try {
            traderService.updateTrader(trader);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @POST
    @Path("/delete/{id}")
    public Object delete(@PathParam("id") String idStr) {
        try {
            long id = Long.parseLong(idStr);
            if (id <= 0) {
                return Result.badRequest("ID must be positive");
            }
            traderService.deleteTrader(id);
            return Result.success();
        } catch (NumberFormatException e) {
            return Result.badRequest("Invalid ID format");
        } catch (RuntimeException e) {
            return Result.badRequest(e.getMessage());
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
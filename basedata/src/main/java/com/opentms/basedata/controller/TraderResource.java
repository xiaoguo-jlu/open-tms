package com.opentms.basedata.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.opentms.basedata.entity.Trader;
import com.opentms.basedata.service.TraderService;
import jakarta.ws.rs.*;
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
    public Object page(
            @QueryParam("keyword") String keyword,
            @QueryParam("status") String status,
            @QueryParam("pageNum") @DefaultValue("1") int pageNum,
            @QueryParam("pageSize") @DefaultValue("10") int pageSize) {
        return traderService.queryPage(keyword, status, pageNum, pageSize);
    }

    @GET
    @Path("/{id}")
    public Object getById(@PathParam("id") Long id) {
        Trader trader = traderService.getTraderById(id);
        if (trader == null) {
            return com.opentms.common.model.Result.notFound("Trader not found");
        }
        return com.opentms.common.model.Result.success(trader);
    }

    @POST
    public Object save(Trader trader) {
        try {
            traderService.saveTrader(trader);
            return com.opentms.common.model.Result.success();
        } catch (Exception e) {
            return com.opentms.common.model.Result.error(e.getMessage());
        }
    }

    @PUT
    public Object update(Trader trader) {
        try {
            traderService.updateTrader(trader);
            return com.opentms.common.model.Result.success();
        } catch (Exception e) {
            return com.opentms.common.model.Result.error(e.getMessage());
        }
    }

    @DELETE
    @Path("/{id}")
    public Object delete(@PathParam("id") Long id) {
        try {
            traderService.deleteTrader(id);
            return com.opentms.common.model.Result.success();
        } catch (Exception e) {
            return com.opentms.common.model.Result.error(e.getMessage());
        }
    }
}
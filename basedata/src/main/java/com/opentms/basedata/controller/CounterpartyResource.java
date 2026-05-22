package com.opentms.basedata.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.opentms.basedata.entity.Counterparty;
import com.opentms.basedata.service.CounterpartyService;
import jakarta.ws.rs.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Path("/api/v1/counterparties")
public class CounterpartyResource {

    @Autowired
    private CounterpartyService counterpartyService;

    public CounterpartyResource() {
    }

    @GET
    @Path("/page")
    public Object page(
            @QueryParam("keyword") String keyword,
            @QueryParam("counterpartyType") String counterpartyType,
            @QueryParam("countryCode") String countryCode,
            @QueryParam("status") String status,
            @QueryParam("pageNum") @DefaultValue("1") int pageNum,
            @QueryParam("pageSize") @DefaultValue("10") int pageSize) {
        return counterpartyService.queryPage(keyword, counterpartyType, countryCode, status, pageNum, pageSize);
    }

    @GET
    @Path("/{id}")
    public Object getById(@PathParam("id") Long id) {
        Counterparty counterparty = counterpartyService.getCounterpartyById(id);
        if (counterparty == null) {
            return com.opentms.common.model.Result.notFound("Counterparty not found");
        }
        return com.opentms.common.model.Result.success(counterparty);
    }

    @POST
    public Object save(Counterparty counterparty) {
        try {
            counterpartyService.saveCounterparty(counterparty);
            return com.opentms.common.model.Result.success();
        } catch (Exception e) {
            return com.opentms.common.model.Result.error(e.getMessage());
        }
    }

    @PUT
    public Object update(Counterparty counterparty) {
        try {
            counterpartyService.updateCounterparty(counterparty);
            return com.opentms.common.model.Result.success();
        } catch (Exception e) {
            return com.opentms.common.model.Result.error(e.getMessage());
        }
    }

    @DELETE
    @Path("/{id}")
    public Object delete(@PathParam("id") Long id) {
        try {
            counterpartyService.deleteCounterparty(id);
            return com.opentms.common.model.Result.success();
        } catch (Exception e) {
            return com.opentms.common.model.Result.error(e.getMessage());
        }
    }
}
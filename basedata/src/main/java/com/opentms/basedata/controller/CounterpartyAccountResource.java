package com.opentms.basedata.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.opentms.basedata.entity.CounterpartyAccount;
import com.opentms.basedata.service.CounterpartyAccountService;
import jakarta.ws.rs.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Path("/api/v1/counterparty-accounts")
public class CounterpartyAccountResource {

    @Autowired
    private CounterpartyAccountService counterpartyAccountService;

    public CounterpartyAccountResource() {
    }

    @GET
    @Path("/page")
    public Object page(
            @QueryParam("counterpartyId") Long counterpartyId,
            @QueryParam("keyword") String keyword,
            @QueryParam("status") String status,
            @QueryParam("pageNum") @DefaultValue("1") int pageNum,
            @QueryParam("pageSize") @DefaultValue("10") int pageSize) {
        return counterpartyAccountService.queryPage(counterpartyId, keyword, status, pageNum, pageSize);
    }

    @GET
    @Path("/{id}")
    public Object getById(@PathParam("id") Long id) {
        CounterpartyAccount account = counterpartyAccountService.getCounterpartyAccountById(id);
        if (account == null) {
            return com.opentms.common.model.Result.notFound("Counterparty account not found");
        }
        return com.opentms.common.model.Result.success(account);
    }

    @POST
    public Object save(CounterpartyAccount account) {
        try {
            counterpartyAccountService.saveCounterpartyAccount(account);
            return com.opentms.common.model.Result.success();
        } catch (Exception e) {
            return com.opentms.common.model.Result.error(e.getMessage());
        }
    }

    @POST
    @Path("/update")
    @Consumes(jakarta.ws.rs.core.MediaType.APPLICATION_JSON)
    public Object update(CounterpartyAccount account) {
        try {
            counterpartyAccountService.updateCounterpartyAccount(account);
            return com.opentms.common.model.Result.success();
        } catch (Exception e) {
            return com.opentms.common.model.Result.error(e.getMessage());
        }
    }

    @POST
    @Path("/delete/{id}")
    public Object delete(@PathParam("id") Long id) {
        try {
            counterpartyAccountService.deleteCounterpartyAccount(id);
            return com.opentms.common.model.Result.success();
        } catch (Exception e) {
            return com.opentms.common.model.Result.error(e.getMessage());
        }
    }
}
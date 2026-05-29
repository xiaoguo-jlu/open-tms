package com.opentms.basedata.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.opentms.basedata.entity.Bank;
import com.opentms.basedata.service.BankService;
import jakarta.ws.rs.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Path("/api/v1/banks")
public class BankResource {

    @Autowired
    private BankService bankService;

    public BankResource() {
    }

    @GET
    @Path("/page")
    public Object page(
            @QueryParam("keyword") String keyword,
            @QueryParam("countryCode") String countryCode,
            @QueryParam("status") String status,
            @QueryParam("pageNum") @DefaultValue("1") int pageNum,
            @QueryParam("pageSize") @DefaultValue("10") int pageSize) {
        return bankService.queryPage(keyword, countryCode, status, pageNum, pageSize);
    }

    @GET
    @Path("/{id}")
    public Object getById(@PathParam("id") Long id) {
        Bank bank = bankService.getBankById(id);
        if (bank == null) {
            return com.opentms.common.model.Result.notFound("Bank not found");
        }
        return com.opentms.common.model.Result.success(bank);
    }

    @POST
    public Object save(Bank bank) {
        try {
            bankService.saveBank(bank);
            return com.opentms.common.model.Result.success();
        } catch (Exception e) {
            return com.opentms.common.model.Result.error(e.getMessage());
        }
    }

    @POST
    public Object update(Bank bank) {
        try {
            bankService.updateBank(bank);
            return com.opentms.common.model.Result.success();
        } catch (Exception e) {
            return com.opentms.common.model.Result.error(e.getMessage());
        }
    }

    @DELETE
    @Path("/{id}")
    public Object delete(@PathParam("id") Long id) {
        try {
            bankService.deleteBank(id);
            return com.opentms.common.model.Result.success();
        } catch (Exception e) {
            return com.opentms.common.model.Result.error(e.getMessage());
        }
    }
}
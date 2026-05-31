package com.opentms.basedata.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.opentms.basedata.entity.BankAccount;
import com.opentms.basedata.service.BankAccountService;
import com.opentms.common.model.Result;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Path("/api/v1/bank-accounts")
public class BankAccountResource {

    @Autowired
    private BankAccountService bankAccountService;

    @GET
    @Path("/page")
    public Object page(
            @QueryParam("keyword") String keyword,
            @QueryParam("bankId") Long bankId,
            @QueryParam("currency") String currency,
            @QueryParam("accountType") String accountType,
            @QueryParam("businessUnitId") Long businessUnitId,
            @QueryParam("status") String status,
            @QueryParam("pageNum") @DefaultValue("1") int pageNum,
            @QueryParam("pageSize") @DefaultValue("10") int pageSize) {
        return Result.success(bankAccountService.queryPage(keyword, bankId, currency, accountType, businessUnitId, status, pageNum, pageSize));
    }

    @GET
    @Path("/{id}")
    public Object getById(@PathParam("id") Long id) {
        BankAccount account = bankAccountService.getBankAccountById(id);
        if (account == null) {
            return Result.notFound("Bank account not found");
        }
        return Result.success(account);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Object save(BankAccount account) {
        try {
            bankAccountService.saveBankAccount(account);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Object update(BankAccount account) {
        try {
            bankAccountService.updateBankAccount(account);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DELETE
    @Path("/{id}")
    public Object delete(@PathParam("id") Long id) {
        try {
            bankAccountService.deleteBankAccount(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}

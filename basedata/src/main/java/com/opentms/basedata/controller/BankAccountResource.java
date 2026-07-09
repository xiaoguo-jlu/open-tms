package com.opentms.basedata.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.opentms.basedata.entity.BankAccount;
import com.opentms.basedata.service.BankAccountService;
import com.opentms.common.model.Result;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 银行账户 Resource(统一规范版 — 2026-06-29)
 * <p>
 * 写操作一律 POST:/update 和 POST:/delete/{id},禁用 @PUT/@DELETE。
 * 新增 /balance(余额查询)与 /sync(银企同步 stub)端点。
 * </p>
 */
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

    @GET
    @Path("/{id}/balance")
    public Object getBalance(@PathParam("id") Long id) {
        BankAccount account = bankAccountService.getBankAccountById(id);
        if (account == null) {
            return Result.notFound("Bank account not found");
        }
        Map<String, Object> data = new HashMap<>();
        data.put("balance", account.getBalance());
        data.put("availableBalance", account.getAvailableBalance());
        data.put("frozenBalance", account.getFrozenBalance());
        return Result.success(data);
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

    @POST
    @Path("/update")
    @Consumes(MediaType.APPLICATION_JSON)
    public Object update(BankAccount account) {
        try {
            bankAccountService.updateBankAccount(account);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @POST
    @Path("/delete/{id}")
    public Object delete(@PathParam("id") Long id) {
        try {
            bankAccountService.deleteBankAccount(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @POST
    @Path("/{id}/sync")
    public Object sync(@PathParam("id") Long id) {
        // Stub:实际对接银企接口异步同步
        BankAccount account = bankAccountService.getBankAccountById(id);
        if (account == null) {
            return Result.notFound("Bank account not found");
        }
        Map<String, Object> data = new HashMap<>();
        data.put("accountId", id);
        data.put("accountNo", account.getAccountNo());
        data.put("message", "同步任务已提交(Stub)");
        return Result.success(data);
    }
}

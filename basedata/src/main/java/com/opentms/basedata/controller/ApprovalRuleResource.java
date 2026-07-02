package com.opentms.basedata.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.opentms.basedata.entity.ApprovalRule;
import com.opentms.basedata.service.ApprovalRuleService;
import jakarta.ws.rs.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Path("/api/v1/approval-rules")
public class ApprovalRuleResource {

    @Autowired
    private ApprovalRuleService approvalRuleService;

    public ApprovalRuleResource() {
    }

    @GET
    @Path("/page")
    public Object page(
            @QueryParam("keyword") String keyword,
            @QueryParam("bizType") String bizType,
            @QueryParam("status") String status,
            @QueryParam("pageNum") @DefaultValue("1") int pageNum,
            @QueryParam("pageSize") @DefaultValue("10") int pageSize) {
        return com.opentms.common.model.Result.success(approvalRuleService.queryPage(keyword, bizType, status, pageNum, pageSize));
    }

    @GET
    @Path("/{id}")
    public Object getById(@PathParam("id") Long id) {
        ApprovalRule approvalRule = approvalRuleService.getApprovalRuleById(id);
        if (approvalRule == null) {
            return com.opentms.common.model.Result.notFound("Approval rule not found");
        }
        return com.opentms.common.model.Result.success(approvalRule);
    }

    @POST
    public Object save(ApprovalRule approvalRule) {
        try {
            approvalRuleService.saveApprovalRule(approvalRule);
            return com.opentms.common.model.Result.success();
        } catch (Exception e) {
            return com.opentms.common.model.Result.error(e.getMessage());
        }
    }

    @POST
    @Path("/update")
    public Object update(ApprovalRule approvalRule) {
        try {
            approvalRuleService.updateApprovalRule(approvalRule);
            return com.opentms.common.model.Result.success();
        } catch (Exception e) {
            return com.opentms.common.model.Result.error(e.getMessage());
        }
    }

    @POST
    @Path("/delete/{id}")
    public Object delete(@PathParam("id") Long id) {
        try {
            approvalRuleService.deleteApprovalRule(id);
            return com.opentms.common.model.Result.success();
        } catch (Exception e) {
            return com.opentms.common.model.Result.error(e.getMessage());
        }
    }
}
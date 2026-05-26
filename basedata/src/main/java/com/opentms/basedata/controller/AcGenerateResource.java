package com.opentms.basedata.controller;

import com.opentms.basedata.service.AcCashflowService;
import com.opentms.common.model.Result;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Path("/api/v1/ac/generate")
public class AcGenerateResource {

    @Autowired
    private AcCashflowService acCashflowService;

    public AcGenerateResource() {
    }

    @POST
    @Path("/{dealId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Object generateFromDeal(@PathParam("dealId") String dealId) {
        try {
            long parseId = Long.parseLong(dealId);
            if (parseId <= 0) {
                return Result.badRequest("交易ID必须为正整数");
            }
            return Result.success(acCashflowService.generateFromDeal(parseId));
        } catch (NumberFormatException e) {
            return Result.badRequest("交易ID参数格式不正确");
        }
    }
}

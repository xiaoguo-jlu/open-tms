package com.opentms.basedata.controller;

import com.opentms.basedata.dto.DealApprovalRuleMatchRequestDTO;
import com.opentms.basedata.dto.DealApprovalRuleQueryDTO;
import com.opentms.basedata.dto.DealApprovalRuleSaveDTO;
import com.opentms.basedata.dto.DealApprovalRuleUpdateDTO;
import com.opentms.basedata.entity.DealApprovalRule;
import com.opentms.basedata.service.DealApprovalRuleService;
import com.opentms.basedata.vo.DealApprovalRuleMatchResponseVO;
import com.opentms.basedata.vo.DealApprovalRuleVO;
import com.opentms.common.model.Result;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 交易审批规则 Resource(基于 v1.1 11 端点 + 1 image 端点)
 *
 * <p>风格:CXF JAX-RS,与 DefaultBankAccountRuleResource 完全对齐</p>
 * <p>路由:/api/v1/deal-approval-rules</p>
 *
 * @author Open-TMS
 * @since 2026-07-11
 */
@Component
@Path("/api/v1/deal-approval-rules")
public class DealApprovalRuleController {

    @Autowired
    private DealApprovalRuleService ruleService;

    // ============= 1. 分页查询 =============

    @POST
    @Path("/page")
    @Consumes(MediaType.APPLICATION_JSON)
    public Object page(DealApprovalRuleQueryDTO query) {
        try {
            return Result.success(ruleService.queryPage(query));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // ============= 2. 详情 =============

    @GET
    @Path("/{id}")
    public Object getById(@PathParam("id") String id) {
        try {
            DealApprovalRule rule;
            try {
                rule = ruleService.getRuleById(Long.parseLong(id));
            } catch (NumberFormatException e) {
                rule = ruleService.getRuleByNumber(id);
            }
            if (rule == null) {
                return Result.notFound("规则不存在: " + id);
            }
            return Result.success(ruleService.enrichWithNames(DealApprovalRuleVO.from(rule)));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // ============= 3. 新增 =============

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Object save(DealApprovalRuleSaveDTO dto) {
        try {
            DealApprovalRule rule = ruleService.saveRule(dto);
            return Result.success(ruleService.enrichWithNames(DealApprovalRuleVO.from(rule)));
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // ============= 4. 更新(★ lockToken) =============

    @POST
    @Path("/update")
    @Consumes(MediaType.APPLICATION_JSON)
    public Object update(DealApprovalRuleUpdateDTO dto) {
        try {
            DealApprovalRule rule = ruleService.updateRule(dto);
            return Result.success(ruleService.enrichWithNames(DealApprovalRuleVO.from(rule)));
        } catch (java.util.ConcurrentModificationException e) {
            // ★ 409 Conflict
            return Result.error(Response.Status.CONFLICT.getStatusCode(), e.getMessage());
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    // ============= 5. 删除 =============

    @POST
    @Path("/delete/{id}")
    public Object delete(@PathParam("id") Long id) {
        try {
            boolean ok = ruleService.deleteRule(id);
            if (!ok) return Result.notFound("规则不存在");
            return Result.success("删除成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // ============= 6. 启用 =============

    @POST
    @Path("/{id}/enable")
    public Object enable(@PathParam("id") Long id) {
        try {
            boolean ok = ruleService.enableRule(id);
            if (!ok) return Result.notFound("规则不存在");
            return Result.success("启用成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // ============= 7. 停用 =============

    @POST
    @Path("/{id}/disable")
    public Object disable(@PathParam("id") Long id) {
        try {
            boolean ok = ruleService.disableRule(id);
            if (!ok) return Result.notFound("规则不存在");
            return Result.success("停用成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // ============= 8. ★ 运行时匹配 =============

    @GET
    @Path("/match")
    public Object match(
            @QueryParam("managementEntityId") Long managementEntityId,
            @QueryParam("counterpartyId") Long counterpartyId,
            @QueryParam("instrumentId") Long instrumentId,
            @QueryParam("dealerId") Long dealerId,
            @QueryParam("actionType") String actionType) {
        try {
            DealApprovalRuleMatchRequestDTO req = new DealApprovalRuleMatchRequestDTO();
            req.setManagementEntityId(managementEntityId);
            req.setCounterpartyId(counterpartyId);
            req.setInstrumentId(instrumentId);
            req.setDealerId(dealerId);
            req.setActionType(actionType);
            return Result.success(ruleService.match(req));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // ============= 9. ★ 测试匹配 =============

    @GET
    @Path("/test-match")
    public Object testMatch(
            @QueryParam("managementEntityId") Long managementEntityId,
            @QueryParam("counterpartyId") Long counterpartyId,
            @QueryParam("instrumentId") Long instrumentId,
            @QueryParam("dealerId") Long dealerId,
            @QueryParam("actionType") String actionType,
            @QueryParam("limit") @DefaultValue("50") Integer limit) {
        try {
            DealApprovalRuleMatchRequestDTO req = new DealApprovalRuleMatchRequestDTO();
            req.setManagementEntityId(managementEntityId);
            req.setCounterpartyId(counterpartyId);
            req.setInstrumentId(instrumentId);
            req.setDealerId(dealerId);
            req.setActionType(actionType);
            req.setLimit(limit == null ? 50 : limit);
            return Result.success(ruleService.testMatch(req));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // ============= 10. 审计日志 =============

    @GET
    @Path("/{id}/audit-logs")
    public Object auditLogs(
            @PathParam("id") Long id,
            @QueryParam("pageNum") @DefaultValue("1") int pageNum,
            @QueryParam("pageSize") @DefaultValue("20") int pageSize) {
        try {
            return Result.success(ruleService.getAuditLogs(id, pageNum, pageSize));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // ============= 11. 被引用数 =============

    @GET
    @Path("/{id}/reference-count")
    public Object referenceCount(@PathParam("id") Long id) {
        try {
            return Result.success(ruleService.getReferenceCount(id));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // ============= 12. ★ 镜像列表(本特性新增) =============

    @GET
    @Path("/{id}/images")
    public Object images(
            @PathParam("id") Long id,
            @QueryParam("imageType") String imageType,
            @QueryParam("pageNum") @DefaultValue("1") int pageNum,
            @QueryParam("pageSize") @DefaultValue("20") int pageSize) {
        try {
            return Result.success(ruleService.getImages(id, imageType, pageNum, pageSize));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
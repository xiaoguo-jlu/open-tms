package com.opentms.basedata.controller;

import com.opentms.basedata.dto.DefaultBankAccountRuleMatchRequestDTO;
import com.opentms.basedata.dto.DefaultBankAccountRuleQueryDTO;
import com.opentms.basedata.dto.DefaultBankAccountRuleSaveDTO;
import com.opentms.basedata.dto.DefaultBankAccountRuleUpdateDTO;
import com.opentms.basedata.entity.DefaultBankAccountRule;
import com.opentms.basedata.entity.RuleAuditLog;
import com.opentms.basedata.service.DefaultBankAccountRuleService;
import com.opentms.basedata.vo.DefaultBankAccountRuleVO;
import com.opentms.common.model.Result;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 默认银行账户规则 Resource(v1.1 — 11 端点)
 *
 * <p>风格:CXF JAX-RS,基于基于 BankAccountResource 模式
 *
 * @author Open-TMS
 * @since 2026-07-08
 */
@Component
@Path("/api/v1/default-bank-account-rules")
public class DefaultBankAccountRuleResource {

    @Autowired
    private DefaultBankAccountRuleService ruleService;

    // ============= 1. 分页查询 =============

    @POST
    @Path("/page")
    @Consumes(MediaType.APPLICATION_JSON)
    public Object page(DefaultBankAccountRuleQueryDTO query) {
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
            DefaultBankAccountRule rule;
            try {
                rule = ruleService.getRuleById(Long.parseLong(id));
            } catch (NumberFormatException e) {
                rule = ruleService.getRuleByNumber(id);
            }
            if (rule == null) {
                return Result.notFound("规则不存在: " + id);
            }
            return Result.success(DefaultBankAccountRuleVO.from(rule));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // ============= 3. 新增 =============

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Object save(DefaultBankAccountRuleSaveDTO dto) {
        try {
            DefaultBankAccountRule rule = ruleService.saveRule(dto);
            return Result.success(DefaultBankAccountRuleVO.from(rule));
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // ============= 4. 更新(★ v1.1 lockToken) =============

    @POST
    @Path("/update")
    @Consumes(MediaType.APPLICATION_JSON)
    public Object update(DefaultBankAccountRuleUpdateDTO dto) {
        try {
            DefaultBankAccountRule rule = ruleService.updateRule(dto);
            return Result.success(DefaultBankAccountRuleVO.from(rule));
        } catch (java.util.ConcurrentModificationException e) {
            // ★ v1.1 409 Conflict
            return Result.error(Response.Status.CONFLICT.getStatusCode(), e.getMessage());
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            // ★ 打印完整堆栈用于调试
            e.printStackTrace();
            return Result.error(500, e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    // ============= 5. 删除(软删) =============

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

    // ============= 8. ★ v1.1 运行时匹配 =============

    @GET
    @Path("/match")
    public Object match(
            @QueryParam("managementEntityId") Long managementEntityId,
            @QueryParam("counterpartyId") Long counterpartyId,
            @QueryParam("instrumentId") Long instrumentId,
            @QueryParam("direction") String direction,
            @QueryParam("currency") String currency,
            @QueryParam("dualDirection") @DefaultValue("false") Boolean dualDirection) {
        try {
            DefaultBankAccountRuleMatchRequestDTO req = new DefaultBankAccountRuleMatchRequestDTO();
            req.setManagementEntityId(managementEntityId);
            req.setCounterpartyId(counterpartyId);
            req.setInstrumentId(instrumentId);
            req.setDirection(direction);
            req.setCurrency(currency);
            req.setDualDirection(dualDirection);
            return Result.success(ruleService.match(req));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // ============= 9. ★ v1.1 测试匹配 =============

    @GET
    @Path("/test-match")
    public Object testMatch(
            @QueryParam("managementEntityId") Long managementEntityId,
            @QueryParam("counterpartyId") Long counterpartyId,
            @QueryParam("instrumentId") Long instrumentId,
            @QueryParam("direction") String direction,
            @QueryParam("currency") String currency,
            @QueryParam("dualDirection") @DefaultValue("false") Boolean dualDirection) {
        try {
            DefaultBankAccountRuleMatchRequestDTO req = new DefaultBankAccountRuleMatchRequestDTO();
            req.setManagementEntityId(managementEntityId);
            req.setCounterpartyId(counterpartyId);
            req.setInstrumentId(instrumentId);
            req.setDirection(direction);
            req.setCurrency(currency);
            req.setDualDirection(dualDirection);
            List<DefaultBankAccountRule> rules = ruleService.testMatch(req);
            List<DefaultBankAccountRuleVO> vos = rules.stream()
                    .map(DefaultBankAccountRuleVO::from)
                    .collect(Collectors.toList());
            return Result.success(vos);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // ============= 10. ★ v1.1 审计日志 =============

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

    // ============= 11. ★ v1.1 被引用数 =============

    @GET
    @Path("/{id}/reference-count")
    public Object referenceCount(@PathParam("id") Long id) {
        try {
            return Result.success(ruleService.getReferenceCount(id));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
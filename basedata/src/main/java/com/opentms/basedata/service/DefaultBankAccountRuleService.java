package com.opentms.basedata.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.opentms.basedata.dto.DefaultBankAccountRuleMatchRequestDTO;
import com.opentms.basedata.dto.DefaultBankAccountRuleQueryDTO;
import com.opentms.basedata.dto.DefaultBankAccountRuleSaveDTO;
import com.opentms.basedata.dto.DefaultBankAccountRuleUpdateDTO;
import com.opentms.basedata.entity.DefaultBankAccountRule;
import com.opentms.basedata.vo.RuleDualMatchResultVO;
import com.opentms.basedata.vo.RuleMatchResultVO;
import com.opentms.basedata.vo.RuleReferenceCountVO;

/**
 * 默认银行账户规则 Service 接口
 *
 * @author Open-TMS
 * @since 2026-07-08
 */
public interface DefaultBankAccountRuleService {

    Page<DefaultBankAccountRule> queryPage(DefaultBankAccountRuleQueryDTO query);

    DefaultBankAccountRule getRuleById(Long id);

    DefaultBankAccountRule getRuleByNumber(String ruleNumber);

    /** 新增规则,生成 rule_number + lockToken,写审计日志 */
    DefaultBankAccountRule saveRule(DefaultBankAccountRuleSaveDTO dto);

    /** 更新规则(★ v1.1 校验 lockToken,失败抛 409 Conflict 异常) */
    DefaultBankAccountRule updateRule(DefaultBankAccountRuleUpdateDTO dto);

    /** 软删规则,写审计日志,清理 lockToken */
    boolean deleteRule(Long id);

    /** 启用规则 */
    boolean enableRule(Long id);

    /** 停用规则 */
    boolean disableRule(Long id);

    /**
     * ★ v1.1 运行时匹配(支持双方向)
     * <p>支持 Redis 缓存(TTL 5 分钟),降级为 DB 查询
     */
    Object match(DefaultBankAccountRuleMatchRequestDTO req);

    /** ★ v1.1 测试匹配(返回所有命中) */
    java.util.List<DefaultBankAccountRule> testMatch(DefaultBankAccountRuleMatchRequestDTO req);

    /** ★ v1.1 查询规则审计历史 */
    Page<com.opentms.basedata.entity.RuleAuditLog> getAuditLogs(Long ruleId, int pageNum, int pageSize);

    /** ★ v1.1 查询被引用数(未结算 + 近 90 天已结算) */
    RuleReferenceCountVO getReferenceCount(Long id);
}
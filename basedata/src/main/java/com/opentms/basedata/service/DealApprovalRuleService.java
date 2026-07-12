package com.opentms.basedata.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.opentms.basedata.dto.DealApprovalRuleMatchRequestDTO;
import com.opentms.basedata.dto.DealApprovalRuleQueryDTO;
import com.opentms.basedata.dto.DealApprovalRuleSaveDTO;
import com.opentms.basedata.dto.DealApprovalRuleUpdateDTO;
import com.opentms.basedata.entity.DealApprovalRule;
import com.opentms.basedata.entity.DealApprovalRuleAuditLog;
import com.opentms.basedata.entity.DealApprovalRuleImage;
import com.opentms.basedata.vo.DealApprovalRuleMatchResponseVO;
import com.opentms.basedata.vo.DealApprovalRuleReferenceCountVO;
import com.opentms.basedata.vo.DealApprovalRuleVO;

import java.util.List;

/**
 * 交易审批规则 Service 接口
 *
 * @author Open-TMS
 * @since 2026-07-11
 */
public interface DealApprovalRuleService {

    Page<DealApprovalRule> queryPage(DealApprovalRuleQueryDTO query);

    DealApprovalRule getRuleById(Long id);

    DealApprovalRule getRuleByNumber(String ruleNumber);

    /** 新增规则,生成 rule_number + lockToken,写审计日志 + 镜像 */
    DealApprovalRule saveRule(DealApprovalRuleSaveDTO dto);

    /** 更新规则(★ 校验 lockToken,失败抛 ConcurrentModificationException → 409 Conflict) */
    DealApprovalRule updateRule(DealApprovalRuleUpdateDTO dto);

    /** 软删规则,写审计日志 + DELETE 镜像,清理 lockToken */
    boolean deleteRule(Long id);

    /** 启用规则 */
    boolean enableRule(Long id);

    /** 停用规则 */
    boolean disableRule(Long id);

    /**
     * ★ 运行时匹配(支持 5min 内存缓存,降级方案)
     * <p>返回 approvalLevel + level1Roles + level2Roles + matchedRule + candidates。</p>
     */
    DealApprovalRuleMatchResponseVO match(DealApprovalRuleMatchRequestDTO req);

    /** ★ 测试匹配(返回所有命中候选,按 specificityScore DESC 排序) */
    List<DealApprovalRuleMatchResponseVO.MatchCandidate> testMatch(DealApprovalRuleMatchRequestDTO req);

    /** 审计日志分页 */
    Page<DealApprovalRuleAuditLog> getAuditLogs(Long ruleId, int pageNum, int pageSize);

    /** 镜像列表分页 */
    Page<DealApprovalRuleImage> getImages(Long ruleId, String imageType, int pageNum, int pageSize);

    /** 被引用数 */
    DealApprovalRuleReferenceCountVO getReferenceCount(Long id);

    /**
     * 用 4 个基础数据维度的 name 填充 VO(主体/对手方/金融工具/交易员)
     * 用于前端列表/详情展示名称而非 ID。
     * @return 同 vo(链式方便)
     */
    DealApprovalRuleVO enrichWithNames(DealApprovalRuleVO vo);
}
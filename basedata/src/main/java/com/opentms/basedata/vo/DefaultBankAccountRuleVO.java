package com.opentms.basedata.vo;

import com.opentms.basedata.entity.DefaultBankAccountRule;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 默认银行账户规则 VO(API 返回对象,含展示字段)
 *
 * @author Open-TMS
 * @since 2026-07-08
 */
@Data
public class DefaultBankAccountRuleVO {

    private Long id;
    private String ruleNumber;
    private Long managementEntityId;
    private String managementEntityName;
    private Long counterpartyId;
    private String counterpartyName;
    private Long instrumentId;
    private String instrumentName;
    private String direction;
    private String currency;
    private Long bankAccountId;
    private String bankAccountName;
    private String status;
    private Integer priority;
    private LocalDate startDate;
    private String description;
    private String remark;
    /** ★ v1.1 */
    private String lockToken;
    private String lockedBy;
    private LocalDateTime lockedAt;
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;
    private Integer version;
    private String deleted;

    public static DefaultBankAccountRuleVO from(DefaultBankAccountRule rule) {
        if (rule == null) return null;
        DefaultBankAccountRuleVO vo = new DefaultBankAccountRuleVO();
        BeanUtils.copyProperties(rule, vo);
        return vo;
    }
}
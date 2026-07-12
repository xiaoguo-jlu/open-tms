package com.opentms.basedata.vo;

import com.opentms.basedata.entity.DealApprovalRuleImage;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.time.LocalDateTime;

/**
 * 交易审批规则镜像 VO(API 返回对象)
 *
 * @author Open-TMS
 * @since 2026-07-11
 */
@Data
public class DealApprovalRuleImageVO {

    private Long id;
    private String imageNumber;
    private String ruleNumber;
    private Long ruleId;
    private Integer version;
    private String imageType;
    private String operator;
    private LocalDateTime operateAt;
    private String remark;

    public static DealApprovalRuleImageVO from(DealApprovalRuleImage img) {
        if (img == null) return null;
        DealApprovalRuleImageVO vo = new DealApprovalRuleImageVO();
        BeanUtils.copyProperties(img, vo);
        return vo;
    }
}
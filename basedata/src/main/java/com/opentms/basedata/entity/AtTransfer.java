package com.opentms.basedata.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("tms_transfer_t")
public class AtTransfer {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String transferNo;

    private LocalDate transferDate;

    private String businessUnit;

    private String fromAccount;

    private String toAccount;

    private BigDecimal amount;

    private String currency;

    private LocalDate expectedDate;

    private String paymentMethod;

    private String transferReason;

    private String transferType;

    private String needAuthorization;

    private String status;

    private String applicant;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private String createdBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updatedBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @Version
    private Integer version;

    @TableLogic
    private String deleted;
}

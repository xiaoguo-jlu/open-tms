package com.opentms.basedata.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("tms_bank_account_t")
public class BankAccount {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("account_no")
    private String accountNo;

    @TableField("account_name")
    private String accountName;

    @TableField("bank_id")
    private Long bankId;

    private String currency;

    @TableField("account_type")
    private String accountType;

    @TableField("business_unit_id")
    private Long businessUnitId;

    private String status;

    private String remark;

    private String createdBy;

    private LocalDateTime createdAt;

    private String updatedBy;

    private LocalDateTime updatedAt;

    private Integer version;

    private String deleted;
}

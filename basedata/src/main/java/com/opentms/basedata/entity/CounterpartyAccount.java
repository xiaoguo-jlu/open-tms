package com.opentms.basedata.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("tms_counterparty_account_t")
public class CounterpartyAccount {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long counterpartyId;

    private String accountNo;

    private String accountName;

    private Long bankId;

    private String currency;

    private String accountType;

    private String status;

    private String createdBy;

    private java.time.LocalDateTime createdAt;

    private String updatedBy;

    private java.time.LocalDateTime updatedAt;

    private Integer version;

    private String deleted;
}
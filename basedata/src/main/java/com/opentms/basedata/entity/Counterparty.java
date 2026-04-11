package com.opentms.basedata.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("tms_counterparty_t")
public class Counterparty {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String code;

    private String name;

    private String enName;

    private String counterpartyType;

    private String countryCode;

    private String swiftCode;

    private String status;

    private String remark;

    private String createdBy;

    private java.time.LocalDateTime createdAt;

    private String updatedBy;

    private java.time.LocalDateTime updatedAt;

    private Integer version;

    private String deleted;
}
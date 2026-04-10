package com.opentms.basedata.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.opentms.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("trm_holiday_t")
public class Holiday extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private LocalDate holidayDate;

    private String holidayName;

    private String countryCode;

    private String isAdjustment;

    private String remark;
}
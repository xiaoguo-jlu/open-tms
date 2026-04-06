package com.opentms.basedata.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.opentms.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("trm_holiday_t")
public class Holiday extends BaseEntity {

    @TableField("holiday_date")
    private LocalDate holidayDate;

    private String name;

    @TableField("country_code")
    private String countryCode;

    @TableField("is_adjust")
    private String isAdjust;

    private String remark;
}

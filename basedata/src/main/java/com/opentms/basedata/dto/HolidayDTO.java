package com.opentms.basedata.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.time.LocalDate;

@Data
public class HolidayDTO {

    private Long id;

    @TableField("holiday_date")
    private LocalDate holidayDate;

    private String name;

    @TableField("country_code")
    private String countryCode;

    @TableField("is_adjust")
    private String isAdjust;

    private String remark;
}

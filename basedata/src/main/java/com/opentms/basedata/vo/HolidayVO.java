package com.opentms.basedata.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class HolidayVO {

    private Long id;

    @TableField("holiday_date")
    private LocalDate holidayDate;

    private String name;

    @TableField("country_code")
    private String countryCode;

    @TableField("is_adjust")
    private String isAdjust;

    private String remark;

    private String createdBy;

    private LocalDateTime createdAt;

    private String updatedBy;

    private LocalDateTime updatedAt;
}

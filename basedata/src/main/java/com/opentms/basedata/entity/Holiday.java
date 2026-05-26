package com.opentms.basedata.entity;
import com.baomidou.mybatisplus.annotation.TableName;
import com.opentms.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDate;
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tms_holiday_t")
public class Holiday extends BaseEntity {
    private LocalDate holidayDate;
    private String name;
    private String countryCode;
    private String isAdjacent;
    private String remark;
}

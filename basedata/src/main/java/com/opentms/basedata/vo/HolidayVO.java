package com.opentms.basedata.vo;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
@Data
public class HolidayVO {
    private Long id;
    private LocalDate holidayDate;
    private String name;
    private String countryCode;
    private String isAdjacent;
    private String remark;
    private String createdBy;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    private String updatedBy;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}

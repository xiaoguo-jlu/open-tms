package com.opentms.basedata.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class BaseVO {

    private Long id;

    private String code;

    private String name;

    private String status;

    private String createdBy;

    private LocalDateTime createdAt;

    private String updatedBy;

    private LocalDateTime updatedAt;
}

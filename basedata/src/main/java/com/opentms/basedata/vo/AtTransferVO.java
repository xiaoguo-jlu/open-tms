package com.opentms.basedata.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class AtTransferVO {

    private Long id;

    private String transferNo;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate transferDate;

    private String businessUnit;

    private String fromAccount;

    private String toAccount;

    private BigDecimal amount;

    private String currency;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expectedDate;

    private String paymentMethod;

    private String transferReason;

    private String transferType;

    private String needAuthorization;

    private String status;

    private String applicant;

    private String remark;

    private String createdBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    private String updatedBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    private Integer version;
}

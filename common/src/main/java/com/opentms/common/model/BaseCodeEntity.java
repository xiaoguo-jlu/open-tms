package com.opentms.common.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class BaseCodeEntity extends BaseEntity {

    private String code;

    private String name;

    private String status;
}

package com.opentms.basedata.enums;

/**
 * 资金方向枚举(v1.1)
 *
 * @author Open-TMS
 * @since 2026-07-08
 */
public enum Direction {
    INFLOW("Inflow", "收款"),
    OUTFLOW("Outflow", "付款"),
    ALL("ALL", "通配");

    private final String code;
    private final String desc;

    Direction(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() { return code; }
    public String getDesc() { return desc; }
}
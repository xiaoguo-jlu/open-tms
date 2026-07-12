package com.opentms.basedata.enums;

/**
 * 审批层级枚举(交易审批规则新增)
 *
 * @author Open-TMS
 * @since 2026-07-11
 */
public enum ApprovalLevel {
    LEVEL_0("LEVEL_0", "无需审批"),
    LEVEL_1("LEVEL_1", "一层审批"),
    LEVEL_2("LEVEL_2", "二层审批");

    private final String code;
    private final String desc;

    ApprovalLevel(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() { return code; }
    public String getDesc() { return desc; }

    public static boolean isValid(String value) {
        if (value == null) return false;
        for (ApprovalLevel lvl : values()) {
            if (lvl.code.equals(value)) return true;
        }
        return false;
    }
}
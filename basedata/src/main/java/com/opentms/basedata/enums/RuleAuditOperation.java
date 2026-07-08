package com.opentms.basedata.enums;

/**
 * 规则审计操作类型(v1.1 新增)
 *
 * @author Open-TMS
 * @since 2026-07-08
 */
public enum RuleAuditOperation {
    CREATE("CREATE", "创建"),
    UPDATE("UPDATE", "更新"),
    DELETE("DELETE", "删除"),
    ENABLE("ENABLE", "启用"),
    DISABLE("DISABLE", "停用");

    private final String code;
    private final String desc;

    RuleAuditOperation(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() { return code; }
    public String getDesc() { return desc; }
}
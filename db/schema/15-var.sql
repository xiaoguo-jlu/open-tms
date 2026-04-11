-- Open-TMS VaR模块表
-- PostgreSQL
-- 执行顺序: 15

-- VaR报表表 (修复: 精度提高)
CREATE TABLE tms_var_report_t (
    id BIGSERIAL PRIMARY KEY,
    report_date DATE NOT NULL,
    var_type VARCHAR(50),
    confidence_level DECIMAL(8,4),
    holding_period INT,
    total_var DECIMAL(24,4),
    fx_var DECIMAL(24,4),
    ir_var DECIMAL(24,4),
    credit_var DECIMAL(24,4),
    status CHAR(1) NOT NULL DEFAULT '1',
    remark VARCHAR(500),
    created_by VARCHAR(50) NOT NULL DEFAULT 'system',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP,
    version INT DEFAULT 0,
    deleted CHAR(1) DEFAULT '0'
);
COMMENT ON TABLE tms_var_report_t IS 'VaR报表表';
CREATE INDEX idx_var_date ON tms_var_report_t(report_date);
CREATE INDEX idx_var_type ON tms_var_report_t(var_type);
CREATE INDEX idx_var_status ON tms_var_report_t(status);
-- Open-TMS VaR模块表
-- PostgreSQL

CREATE TABLE trm_var_report_t (
    id BIGSERIAL PRIMARY KEY,
    report_date DATE NOT NULL,
    var_type VARCHAR(50),
    confidence_level DECIMAL(8,4),
    holding_period INT,
    total_var DECIMAL(18,2),
    fx_var DECIMAL(18,2),
    ir_var DECIMAL(18,2),
    credit_var DECIMAL(18,2),
    status CHAR(1) NOT NULL DEFAULT '1',
    created_by VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP,
    version INT DEFAULT 0,
    deleted CHAR(1) DEFAULT '0'
);
CREATE INDEX idx_var_report_date ON trm_var_report_t(report_date);
CREATE INDEX idx_var_type ON trm_var_report_t(var_type);
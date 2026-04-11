-- Open-TMS 报表模块表
-- PostgreSQL
-- 执行顺序: 17

-- 报表表 (修复: 精度提高)
CREATE TABLE tms_report_t (
    id BIGSERIAL PRIMARY KEY,
    report_no VARCHAR(50) NOT NULL UNIQUE,
    report_name VARCHAR(200) NOT NULL,
    report_type VARCHAR(20) NOT NULL,
    report_template VARCHAR(50),
    business_unit_id BIGINT,
    currency VARCHAR(10),
    start_date DATE,
    end_date DATE,
    report_data TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    created_by VARCHAR(50) NOT NULL DEFAULT 'system',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP,
    version INT DEFAULT 0,
    deleted CHAR(1) DEFAULT '0'
);
COMMENT ON TABLE tms_report_t IS '报表表';
CREATE INDEX idx_rpt_no ON tms_report_t(report_no);
CREATE INDEX idx_rpt_type ON tms_report_t(report_type);
CREATE INDEX idx_rpt_business_unit ON tms_report_t(business_unit_id);
CREATE INDEX idx_rpt_start_date ON tms_report_t(start_date);
CREATE INDEX idx_rpt_status ON tms_report_t(status);
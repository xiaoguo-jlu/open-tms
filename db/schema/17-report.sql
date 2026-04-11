-- Open-TMS 报表模块表
-- PostgreSQL

CREATE TABLE trm_report_t (
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
    status VARCHAR(20),
    created_by VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP,
    version INT DEFAULT 0,
    deleted CHAR(1) DEFAULT '0'
);
CREATE INDEX idx_report_no ON trm_report_t(report_no);
CREATE INDEX idx_report_type ON trm_report_t(report_type);
CREATE INDEX idx_business_unit ON trm_report_t(business_unit_id);
CREATE INDEX idx_report_date ON trm_report_t(start_date);
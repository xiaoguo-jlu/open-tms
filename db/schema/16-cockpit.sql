-- Open-TMS 驾驶舱模块表
-- PostgreSQL

CREATE TABLE trm_cockpit_dashboard_t (
    id BIGSERIAL PRIMARY KEY,
    dashboard_code VARCHAR(50) NOT NULL UNIQUE,
    dashboard_name VARCHAR(200) NOT NULL,
    dashboard_type VARCHAR(20),
    config_json TEXT,
    status CHAR(1) NOT NULL DEFAULT '1',
    created_by VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP,
    version INT DEFAULT 0,
    deleted CHAR(1) DEFAULT '0'
);
CREATE INDEX idx_dashboard_code ON trm_cockpit_dashboard_t(dashboard_code);

CREATE TABLE trm_cockpit_widget_t (
    id BIGSERIAL PRIMARY KEY,
    dashboard_id BIGINT NOT NULL,
    widget_code VARCHAR(50) NOT NULL,
    widget_name VARCHAR(200),
    widget_type VARCHAR(20),
    widget_config TEXT,
    display_order INT,
    status CHAR(1) NOT NULL DEFAULT '1',
    created_by VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP,
    version INT DEFAULT 0,
    deleted CHAR(1) DEFAULT '0'
);
CREATE INDEX idx_widget_dashboard ON trm_cockpit_widget_t(dashboard_id);
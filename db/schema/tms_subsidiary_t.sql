-- 子公司表
CREATE TABLE tms_subsidiary_t (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    en_name VARCHAR(200),
    parent_code VARCHAR(50),
    business_unit_code VARCHAR(50),
    legal_person VARCHAR(50),
    registration_no VARCHAR(50),
    tax_no VARCHAR(50),
    address VARCHAR(500),
    phone VARCHAR(30),
    email VARCHAR(100),
    status CHAR(1) NOT NULL DEFAULT '1',
    remark VARCHAR(500),
    created_by VARCHAR(50) NOT NULL DEFAULT 'system',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP,
    version INT DEFAULT 0,
    deleted CHAR(1) DEFAULT '0'
);
COMMENT ON TABLE tms_subsidiary_t IS '子公司表';
CREATE INDEX idx_subsidiary_code ON tms_subsidiary_t(code);
CREATE INDEX idx_subsidiary_parent ON tms_subsidiary_t(parent_code);
CREATE INDEX idx_subsidiary_bu ON tms_subsidiary_t(business_unit_code);
CREATE INDEX idx_subsidiary_status ON tms_subsidiary_t(status);
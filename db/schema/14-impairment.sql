-- Open-TMS 减值计算模块表
-- PostgreSQL
-- 执行顺序: 14

-- 减值计算表
CREATE TABLE trm_impairment_t (
    id BIGSERIAL PRIMARY KEY,
    inst_id BIGINT NOT NULL,
    impairment_date DATE NOT NULL,
    ead DECIMAL(18,2),
    probability_of_default DECIMAL(8,4),
    loss_given_default DECIMAL(8,4),
    expected_loss DECIMAL(18,2),
    stage INT DEFAULT 1,
    model_used VARCHAR(50),
    status CHAR(1) NOT NULL DEFAULT '1',
    remark VARCHAR(500),
    created_by VARCHAR(50) NOT NULL DEFAULT 'system',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP,
    version INT DEFAULT 0,
    deleted CHAR(1) DEFAULT '0'
);
COMMENT ON TABLE trm_impairment_t IS '减值计算表';
CREATE INDEX idx_imp_inst ON trm_impairment_t(inst_id);
CREATE INDEX idx_imp_date ON trm_impairment_t(impairment_date);
CREATE INDEX idx_imp_stage ON trm_impairment_t(stage);
CREATE INDEX idx_imp_status ON trm_impairment_t(status);
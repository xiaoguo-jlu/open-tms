# M1-资金管理主体 PRD

**版本**: v1.0  
**角色**: 产品经理 (PM)  
**日期**: 2026-05-26

---

## 一、模块概述

**模块名称**: management-entity - 资金管理主体  
**功能定位**: 管理企业资金管理的核心组织单元，包含法人主体、子公司、分公司等各类组织实体，支持多层级组织架构、监管合规配置和财务报表核算  
**用户角色**: IT运维人员、资金管理人员、财务人员、合规人员  
**与其他模块的关系**: 
- 是银行账户、交易、限额的归属载体
- 是监管报表、合规检查的数据归集维度
- 是权限控制的数据隔离维度

---

## 二、业界对标

| 特性 | FIS Quantum | SAP TRMS | Open-TMS |
|------|------------|----------|----------|
| 管理主体类型 | 法律主体/运营主体 | 公司代码/业务范围 | 本版本支持法人主体 |
| 多准则核算 | IFRS/GAAP/Local GAAP | 多账套 | P2支持 |
| 监管信息 | CRD/BRRD/EMIR | 监管编码 | 本版本P0 |
| 税务配置 | VAT/GST注册 | 税务管辖权 | 本版本P1 |
| 组织层级 | 集团→区域→公司→部门 | 公司代码层级 | 本版本P0 |

---

## 三、功能清单

### 3.1 管理主体基础信息

| 功能 | 说明 | 优先级 |
|------|------|--------|
| 主体列表 | 分页展示管理主体，支持按类型/状态筛选 | P0 |
| 新增主体 | 录入管理主体基本信息 | P0 |
| 编辑主体 | 修改主体信息 | P0 |
| 删除主体 | 逻辑删除，关联数据校验 | P0 |
| 主体详情 | 查看完整信息及下级组织 | P0 |
| 主体启用/停用 | 停用后不可新增交易 | P0 |

**字段定义**:
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| 主体编码 | VARCHAR(50) | Y | 唯一编码，如 BU001 |
| 主体名称 | VARCHAR(200) | Y | 中文名称 |
| 英文名称 | VARCHAR(200) | N | English Name |
| 主体类型 | VARCHAR(20) | Y | HEADQUARTER-总部<br>SUBSIDIARY-子公司<br>BRANCH-分公司<br>REPRESENTATIVE-代表处 |
| 所属集团 | VARCHAR(50) | N | 父级主体编码（顶级为空） |
| 法人代表 | VARCHAR(50) | N | 法人姓名 |
| 注册地址 | VARCHAR(500) | N | 注册地址 |
| 实际办公地址 | VARCHAR(500) | N | 实际经营地址 |
| 税号 | VARCHAR(50) | N | 税务登记号 |
| 统一社会信用代码 | VARCHAR(18) | N | 18位统一社会信用代码 |
| 营业执照号 | VARCHAR(50) | N | 营业执照编号 |
| 成立日期 | DATE | N | 成立日期 |
| 状态 | CHAR(1) | Y | 0-停用 1-启用 |
| 创建人 | VARCHAR(50) | 系统 | - |
| 创建时间 | DATETIME | 系统 | - |
| 修改人 | VARCHAR(50) | 系统 | - |
| 修改时间 | DATETIME | 系统 | - |

### 3.2 监管合规信息

| 功能 | 说明 | 优先级 |
|------|------|--------|
| 监管信息维护 | 录入主体监管合规信息 | P0 |
| 监管类型配置 | 配置适用的监管法规 | P1 |
| 金融许可证 | 记录金融业务许可证信息 | P1 |

**字段定义**:
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| 主体编码 | VARCHAR(50) | Y | 关联主体 |
| 监管机构 | VARCHAR(100) | N | 人行/银保监/证监会等 |
| 监管牌照号 | VARCHAR(100) | N | 金融许可证号 |
| 牌照到期日 | DATE | N | 许可证到期日期 |
| 注册资本 | DECIMAL(18,2) | N | 注册资本金额 |
| 注册资本币种 | VARCHAR(10) | N | 注册资本币种 |
| 净资产 | DECIMAL(18,2) | N | 最新净资产 |
| 净资产截止日期 | DATE | N | 净资产核算日期 |
| 杠杆率 | DECIMAL(10,4) | N | 杠杆率 |
| 流动性覆盖率要求 | DECIMAL(10,2) | N | LCR要求(%) |
| 净稳定资金要求 | DECIMAL(10,2) | N | NSFR要求(%) |

### 3.3 会计准则配置

| 功能 | 说明 | 优先级 |
|------|------|--------|
| 会计准则配置 | 配置主体适用的会计准则 | P0 |
| 账套启用 | 启用/停用主体的会计准则 | P0 |
| 本位币配置 | 主体财务报表本位币 | P0 |
| 折算汇率来源 | 境外主体报表折算汇率来源 | P1 |

**字段定义**:
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| 主体编码 | VARCHAR(50) | Y | 关联主体 |
| 会计准则 | VARCHAR(20) | Y | IFRS/USGAAP/CNGBA/OTHER |
| 报表本位币 | VARCHAR(10) | Y | 报表币种 |
| 是否启用 | CHAR(1) | Y | 0-停用 1-启用 |
| 首次适用日期 | DATE | N | 首次采用该准则日期 |

**会计准则枚举**:
| 值 | 说明 |
|----|------|
| IFRS | 国际财务报告准则 |
| USGAAP | 美国通用会计准则 |
| CNGBA | 中国企业会计准则 |
| CNGBS | 中国小企业会计准则 |
| OTHER | 其他会计准则 |

### 3.4 税务配置

| 功能 | 说明 | 优先级 |
|------|------|--------|
| 税务信息维护 | 录入主体税务信息 | P1 |
| 增值税号 | 增值税一般纳税人资格 | P1 |
| 税务管辖地 | 所属税务主管机关 | P1 |

**字段定义**:
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| 主体编码 | VARCHAR(50) | Y | 关联主体 |
| 增值税一般纳税人 | CHAR(1) | N | 0-否 1-是 |
| 增值税率 | DECIMAL(5,2) | N | 默认税率(%) |
| 税务管辖地 | VARCHAR(100) | N | 税务主管机关名称 |
| 税务联系人 | VARCHAR(50) | N | 税务联系人 |
| 税务联系电话 | VARCHAR(30) | N | 联系电话 |
| 企业所得税率 | DECIMAL(5,2) | N | 企业所得税率(%) |
| 预提所得税率 | DECIMAL(5,2) | N | 预提所得税率(%) |

### 3.5 组织层级关系

| 功能 | 说明 | 优先级 |
|------|------|--------|
| 层级关系维护 | 维护母子公司的层级关系 | P0 |
| 层级路径查询 | 查询主体的完整层级路径 | P0 |
| 下级主体列表 | 查询直接下级和全部下级 | P0 |
| 上级主体 | 查询直接上级和最终母公司 | P0 |
| 层级深度限制 | 最大支持6级组织层级 | P0 |

**层级查询示例**:
```
集团母公司( Level 1 )
  ├── 区域公司A( Level 2 )
  │     ├── 子公司A1( Level 3 )
  │     │     ├── 分公司A1a( Level 4 )
  │     │     └── 分公司A1b( Level 4 )
  │     └── 子公司A2( Level 3 )
  └── 区域公司B( Level 2 )
        └── 子公司B1( Level 3 )
```

**字段定义**:
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| 主体编码 | VARCHAR(50) | Y | 主体编码 |
| 父级编码 | VARCHAR(50) | N | 上级主体编码（顶级为空） |
| 层级深度 | INT | N | 层级深度（1-6） |
| 层级路径 | VARCHAR(200) | N | 完整路径，如 /BU001/BU002/BU003 |

### 3.6 关联信息管理

| 功能 | 说明 | 优先级 |
|------|------|--------|
| 关联银行账户 | 查询属于该主体的银行账户 | P0 |
| 关联交易员 | 查询该主体的交易员 | P0 |
| 关联对手方 | 查询该主体的交易对手 | P1 |
| 关联报表 | 查询该主体生成的监管报表 | P1 |

---

## 四、业务规则

1. **编码规则**: 主体编码格式为字母前缀+数字，如BU001、BU002；编码唯一
2. **层级限制**: 组织层级最大深度为6级，超过报错
3. **循环引用禁止**: 父级不能是自身或自身下级
4. **删除校验**: 
   - 存在下级主体时不允许删除
   - 有关联银行账户时不允许删除
   - 有关联交易时不允许删除
5. **停用限制**: 存在有效关联数据时不允许停用
6. **本位置换**: 境内主体报表本位币默认为CNY，境外主体需配置
7. **数据权限**: 按主体维度隔离数据，用户只能看到有权限的主体数据

---

## 五、验收标准

| 功能 | 验收条件 |
|------|----------|
| 主体CRUD | 新增/编辑/删除/查询功能正常，状态正确变更 |
| 主体类型 | 四种类型可正确选择，层级关系正确 |
| 层级关系 | 父子关系正确维护，层级路径正确计算 |
| 层级深度限制 | 超过6级时报错，不允许保存 |
| 循环引用校验 | 选择自身或下级时提示错误 |
| 删除校验 | 存在下级/关联数据时不允许删除，提示具体关联 |
| 监管信息 | 监管信息字段正确保存和显示 |
| 会计准则 | 多种准则可配置，默认本位币正确 |
| 税务配置 | 税务信息正确保存 |
| 数据权限 | 用户只能看到有权限的主体数据 |

---

## 六、界面原型(UX待设计)

### 6.1 主体列表页
- 左侧：组织树形结构展示层级关系
- 右侧：主体列表，支持筛选和搜索
- 操作：新增/编辑/删除/启用停用

### 6.2 主体详情页
- Tab页签：基本信息 / 监管信息 / 会计准则 / 税务配置 / 关联信息
- 基本信息：主体基础字段
- 监管信息：金融监管相关字段
- 会计准则：适用准则配置
- 税务配置：税务信息
- 关联信息：关联的账户、交易员等

### 6.3 新增/编辑页
- 表单形式录入字段
- 主体类型下拉选择
- 所属集团级联选择

---

## 七、接口需求

| 接口 | 说明 | 调用方 |
|------|------|--------|
| POST /api/v1/management-entities | 新增主体 | 前端 |
| GET /api/v1/management-entities | 查询主体列表 | 前端 |
| GET /api/v1/management-entities/{id} | 查询主体详情 | 前端 |
| PUT /api/v1/management-entities/{id} | 编辑主体 | 前端 |
| DELETE /api/v1/management-entities/{id} | 删除主体 | 前端 |
| GET /api/v1/management-entities/tree | 获取主体层级树 | 前端/其他模块 |
| GET /api/v1/management-entities/{id}/children | 获取下级主体 | 前端 |
| GET /api/v1/management-entities/{id}/hierarchy | 获取完整层级路径 | 其他模块 |
| GET /api/v1/management-entities/{id}/bank-accounts | 关联银行账户 | 银行账户模块 |
| GET /api/v1/management-entities/{id}/traders | 关联交易员 | 交易员模块 |
| GET /api/v1/management-entities/regulatory-info | 获取主体监管信息 | 报表模块 |
| GET /api/v1/management-entities/accounting-standard | 获取会计准则配置 | 报表模块 |

---

## 八、数据库设计

### 8.1 表结构

```sql
-- 管理主体表
CREATE TABLE tms_management_entity_t (
    id BIGSERIAL PRIMARY KEY,
    entity_code VARCHAR(50) NOT NULL UNIQUE,
    entity_name VARCHAR(200) NOT NULL,
    en_name VARCHAR(200),
    entity_type VARCHAR(20) NOT NULL,  -- HEADQUARTER/SUBSIDIARY/BRANCH/REPRESENTATIVE
    parent_code VARCHAR(50),            -- 父级主体编码
    level_depth INT DEFAULT 1,          -- 层级深度 1-6
    hierarchy_path VARCHAR(500),         -- 层级路径 /BU001/BU002/
    legal_person VARCHAR(50),
    registered_address VARCHAR(500),
    office_address VARCHAR(500),
    tax_no VARCHAR(50),
    unified_social_credit_code VARCHAR(18),
    business_license_no VARCHAR(50),
    establishment_date DATE,
    status CHAR(1) NOT NULL DEFAULT '1',
    remark VARCHAR(500),
    created_by VARCHAR(50) NOT NULL DEFAULT 'system',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP,
    version INT DEFAULT 0,
    deleted CHAR(1) DEFAULT '0'
);

-- 主体监管信息表
CREATE TABLE tms_entity_regulatory_t (
    id BIGSERIAL PRIMARY KEY,
    entity_code VARCHAR(50) NOT NULL,
    regulator VARCHAR(100),            -- 监管机构
    license_no VARCHAR(100),           -- 牌照号
    license_expire_date DATE,           -- 牌照到期日
    registered_capital DECIMAL(18,2), -- 注册资本
    capital_currency VARCHAR(10),      -- 注册资本币种
    net_asset DECIMAL(18,2),           -- 净资产
    net_asset_date DATE,               -- 净资产核算日期
    leverage_ratio DECIMAL(10,4),      -- 杠杆率
    lcr_requirement DECIMAL(10,2),     -- LCR要求(%)
    nsfr_requirement DECIMAL(10,2),    -- NSFR要求(%)
    created_by VARCHAR(50) NOT NULL DEFAULT 'system',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP,
    version INT DEFAULT 0,
    deleted CHAR(1) DEFAULT '0'
);

-- 主体会计准则配置表
CREATE TABLE tms_entity_accounting_std_t (
    id BIGSERIAL PRIMARY KEY,
    entity_code VARCHAR(50) NOT NULL,
    accounting_standard VARCHAR(20) NOT NULL,  -- IFRS/USGAAP/CNGBA/CNGBS/OTHER
    reporting_currency VARCHAR(10) NOT NULL,    -- 报表本位币
    is_enabled CHAR(1) NOT NULL DEFAULT '1',
    first_adoption_date DATE,
    created_by VARCHAR(50) NOT NULL DEFAULT 'system',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP,
    version INT DEFAULT 0,
    deleted CHAR(1) DEFAULT '0',
    UNIQUE(entity_code, accounting_standard)
);

-- 主体税务配置表
CREATE TABLE tms_entity_tax_config_t (
    id BIGSERIAL PRIMARY KEY,
    entity_code VARCHAR(50) NOT NULL,
    vat_general_taxpayer CHAR(1) DEFAULT '0',  -- 增值税一般纳税人
    vat_rate DECIMAL(5,2),                     -- 增值税率
    tax_jurisdiction VARCHAR(100),            -- 税务管辖地
    tax_contact VARCHAR(50),
    tax_contact_phone VARCHAR(30),
    corporate_income_tax_rate DECIMAL(5,2),    -- 企业所得税率
    withholding_tax_rate DECIMAL(5,2),         -- 预提所得税率
    created_by VARCHAR(50) NOT NULL DEFAULT 'system',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP,
    version INT DEFAULT 0,
    deleted CHAR(1) DEFAULT '0'
);

-- 索引
CREATE INDEX idx_me_code ON tms_management_entity_t(entity_code);
CREATE INDEX idx_me_parent ON tms_management_entity_t(parent_code);
CREATE INDEX idx_me_status ON tms_management_entity_t(status);
CREATE INDEX idx_me_type ON tms_management_entity_t(entity_type);
CREATE INDEX idx_er_entity ON tms_entity_regulatory_t(entity_code);
CREATE INDEX idx_eas_entity ON tms_entity_accounting_std_t(entity_code);
CREATE INDEX idx_etc_entity ON tms_entity_tax_config_t(entity_code);
```

### 8.2 实体关系图

```
tms_management_entity_t (管理主体)
         │
         ├── tms_entity_regulatory_t (监管信息)  1:1
         ├── tms_entity_accounting_std_t (会计准则)  1:N
         ├── tms_entity_tax_config_t (税务配置)  1:1
         │
         ├── tms_bank_account_t (银行账户)  1:N
         ├── tms_trader_t (交易员)  1:N
         └── tms_transfer_t (转账交易)  1:N
```

---

## 九、待确认事项

1. [ ] 主体类型是否需要更细致的分类（如：保险公司、证券公司特殊类型）
2. [ ] 监管信息的具体字段是否需要根据中国监管要求调整
3. [ ] 多准则核算的实现方式：是通过多账套还是通过报表转换
4. [ ] 境外主体的报表折算汇率来源是使用央行中间价还是其他来源

---

## 十、依赖关系

| 前置模块 | 依赖内容 | 状态 |
|----------|----------|------|
| 基础数据-币种 | 币种基础数据 | ✅ 已就绪 |
| 基础数据-节假日 | 节假日数据 | ✅ 已就绪 |

---

*PM产出 - v1.0*
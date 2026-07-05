# Open-TMS 用户权限系统 (RBAC) PRD

**版本**: v1.0
**角色**: 产品经理 (PM)
**日期**: 2026-07-05
**模块归属**: common (公共模块,被所有业务模块依赖)
**对标系统**: FIS Quantum / SAP TRM / Murex MX.3 / Kyriba

---

## 一、模块概述

### 1.1 模块名称

**rbac - 用户权限系统**(Resource-Based Access Control,基于资源的访问控制)

### 1.2 功能定位

Open-TMS 用户权限系统是**全局公共基础模块**,为整个 Open-TMS 提供统一的用户认证、角色管理、资源权限控制、数据权限隔离、菜单动态化和操作审计能力。系统的所有业务模块(basedata / dealing / fundplan / valuation / var / settlement / cockpit / report)都依赖本模块进行权限校验。

本期设计目标:
- 解决 Open-TMS 当前"所有用户都是 system/admin"的硬编码问题
- 提供**用户-角色-权限**三层 RBAC 模型
- 提供**页面级 + 按钮级 + 数据级**三层权限粒度
- 提供基于 **JWT** 的无状态登录认证
- 提供按**管理主体(management_entity)**的数据隔离能力

### 1.3 用户角色(系统使用者角色)

| 系统使用者 | 角色代码 | 职责 | 数量上限 |
|------------|----------|------|----------|
| 系统管理员 | SYS_ADMIN | 维护用户、角色、权限、菜单;系统配置 | ≤3 |
| 安全管理员 | SEC_ADMIN | 审计日志查看、密码策略、安全事件处理 | ≤3 |
| 业务管理员 | ADMIN | 业务用户管理、角色分配 | ≤5 |
| 交易员 | TRADER | 录入/修改/查询交易(AC/AT/FX) | 不限 |
| 复核员 | REVIEWER | 复核/审批交易 | 不限 |
| 观察员 | VIEWER | 只读权限,看报表 | 不限 |
| 审计员 | AUDITOR | 查看所有审计日志(只读) | ≤3 |
| 超级管理员 | SUPER_ADMIN | 系统最高权限,绕过一切权限校验 | 1(内置,不可降级) |

### 1.4 与其他模块的关系

```
                        ┌─────────────────────┐
                        │  common - rbac      │  (本期 PRD)
                        │  (用户/角色/权限)    │
                        └──────────┬──────────┘
                                   │ 依赖(被所有模块依赖)
        ┌──────────────┬───────────┼───────────┬──────────────┐
        ▼              ▼           ▼           ▼              ▼
   ┌────────┐    ┌────────┐  ┌────────┐  ┌─────────┐   ┌─────────┐
   │basedata│    │dealing │  │fundplan│  │valuation│   │  ...    │
   └────────┘    └────────┘  └────────┘  └─────────┘   └─────────┘

依赖关系:
- basedata → rbac(查询用户信息、菜单权限)
- dealing  → rbac(交易录入人 = current_user; 数据范围 = management_entity)
- fundplan → rbac(计划编制人 = current_user)
- valuation→ rbac(估值操作人 = current_user)
- 全部模块 → rbac(路由权限 + 按钮权限 + 数据权限)
```

**关键变更**:当前所有业务表(交易/审批/审计)的 `created_by` 字段均为硬编码 "system"。本 PRD 上线后,所有写入操作必须从 **JWT context 中获取 current_user.login_name** 填充。

---

## 二、业界对标

### 2.1 详细对比表

| 特性 | FIS Quantum | SAP TRM | Murex MX.3 | Kyriba | **Open-TMS 本期** |
|------|-------------|---------|------------|--------|--------------------|
| **用户认证** | LDAP + SSO(SAML 2.0) | OAuth 2.0 + SAML | LDAP + Kerberos | OAuth 2.0 + MFA | **JWT (本期) / LDAP+SSO (P1)** |
| **认证协议** | JWT + Refresh Token | OAuth 2.0 + JWT | 自定义 Token | JWT | **JWT (HS256, 8h 过期 + 7d refresh)** |
| **密码存储** | BCrypt + Salt | BCrypt + Pepper | SHA-512 + Salt | Argon2id | **BCrypt (strength=10)** |
| **角色层级** | 多层级 + 继承(组织树) | 多层级 + 角色组 | 二级(角色+组) | 多层级 | **二级(角色 + 用户组,本期不做组)** |
| **资源粒度** | 页面 + 按钮 + 数据 + 字段 | 页面 + 按钮 + 字段 | 页面 + 按钮 + 数据 | 页面 + 按钮 | **页面 + 按钮 + 数据(本期)/ 字段(P2)** |
| **权限模型** | RBAC + ABAC(混合) | RBAC + 组织对象 | RBAC + 资源实例 | RBAC + 数据范围 | **纯 RBAC + 数据范围(本期)** |
| **数据隔离** | 按 entity + 银行 + 币种 | 按 org + company code | 按 portfolio + book | 按 entity + ledger | **按 management_entity(本期)** |
| **审计粒度** | 全字段变更 + 操作上下文 | 关键操作 + 主数据 | 全字段 + 时间戳 | 全字段 + IP | **全字段 + 登录 + 操作(本期)/ 上下文(P1)** |
| **审计存储** | 独立审计库(append-only) | 同库审计表 | 独立审计库 | 同库 + 归档 | **同库 `tms_audit_logs_t` + 归档(P1)** |
| **审计保留期** | 10 年 | 10 年 | 7 年 | 7 年 | **5 年(本期)/ 10 年(P1 可配)** |
| **多因素认证** | 强制(交易类操作) | 可选(高权限用户) | 强制(管理员) | 强制 | **P1(本期不做)** |
| **密码策略** | 12 位+复杂度+90天 | 10 位+复杂度+定期 | 12 位+复杂度+60天 | 12 位+复杂度+90天 | **8 位+复杂度+90天(可配)** |
| **失败锁定** | 5 次/30 分钟 | 3 次/15 分钟 | 5 次/30 分钟 | 5 次/60 分钟 | **5 次/30 分钟** |
| **临时授权** | 支持(delegation) | 支持 | 支持 | 支持 | **P2** |
| **IP 白名单** | 支持 | 支持 | 支持 | 支持 | **P2** |
| **角色继承** | 支持 | 支持(角色组) | 不支持 | 支持 | **P2(本期仅内置角色不可删)** |
| **字段级权限** | 支持 | 支持(EAP) | 支持 | 支持 | **P2** |
| **权限变更生效** | 实时 | 实时(推模式) | 准实时(5 分钟) | 实时 | **准实时(Redis 缓存 5 分钟)** |
| **菜单来源** | 后端动态 | 后端动态 + 静态 | 后端动态 | 后端动态 | **后端动态(从菜单表)** |
| **多语言支持** | 中/英/日/韩 | 30+ 语言 | 10+ 语言 | 20+ 语言 | **中英双语(P1)** |

### 2.2 业界最佳实践借鉴

1. **FIS Quantum**:借鉴其"数据权限=资源实例属性"的细粒度模型,本期简化为按 management_entity 隔离,后续可扩展到 portfolio/desk 维度
2. **SAP TRM**:借鉴其"权限码 + 权限组"分类方式,本期 `permission_category` 字段支持 VIEW/CREATE/UPDATE/DELETE/APPROVE/EXPORT 六类
3. **Murex MX.3**:借鉴其"共享主键 + 跨实体权限"模型,本期权限码采用 `模块.实体.操作` 命名规范(如 `dealing.fx.create`)
4. **Kyriba**:借鉴其"内置角色 + 自定义角色"双类型设计,本期通过 `built_in BOOLEAN` 区分

---

## 三、功能清单

### 3.1 用户管理 (User Management) - P0

#### 3.1.1 功能列表

| 功能 | 说明 | 优先级 | 接口 |
|------|------|--------|------|
| 用户列表(分页) | 按 login_name/email/status 筛选 | P0 | GET /api/v1/users/page |
| 用户详情 | 查询用户完整信息(含角色) | P0 | GET /api/v1/users/{id} |
| 新增用户 | 创建用户并分配初始角色 | P0 | POST /api/v1/users |
| 更新用户 | 修改用户基本信息 | P0 | POST /api/v1/users/update |
| 删除用户 | 软删除(需先清除所有角色绑定) | P0 | POST /api/v1/users/delete/{id} |
| 启用/停用 | 切换用户 status 字段 | P0 | POST /api/v1/users/{id}/toggle-status |
| 重置密码 | 管理员重置为临时密码 | P0 | POST /api/v1/users/{id}/reset-password |
| 修改密码 | 用户修改自己的密码 | P0 | POST /api/v1/users/me/change-password |
| 分配角色 | 给用户分配一个/多个角色 + 数据范围 | P0 | POST /api/v1/users/{id}/roles |
| 移除角色 | 从用户移除一个角色 | P0 | POST /api/v1/users/{id}/roles/{roleId}/remove |
| 查询用户的角色 | 查看用户的角色清单 | P0 | GET /api/v1/users/{id}/roles |
| 解锁账户 | 管理员强制解锁被锁账户 | P0 | POST /api/v1/users/{id}/unlock |
| 当前用户信息 | 获取当前登录用户的完整信息 | P0 | GET /api/v1/auth/me |

#### 3.1.2 字段设计 (`tms_users_t`)

| 字段 | 类型 | 必填 | 默认 | 说明 |
|------|------|------|------|------|
| id | BIGSERIAL | Y | auto | 主键 |
| login_name | VARCHAR(50) | Y | - | 登录账号(唯一,字母数字下划线,3-50 字符) |
| display_name | VARCHAR(100) | Y | - | 显示名称(中文姓名) |
| email | VARCHAR(100) | N | - | 邮箱 |
| phone | VARCHAR(20) | N | - | 手机号 |
| dept_id | BIGINT | N | - | 部门 ID(本期可空,预留 FK) |
| dept_name | VARCHAR(100) | N | - | 部门名称(冗余,便于显示) |
| default_management_entity_id | BIGINT | N | - | 主所属管理主体 ID(FK→tms_management_entity_t.id) |
| status | VARCHAR(20) | Y | 'ENABLED' | 用户状态:ENABLED / DISABLED / LOCKED |
| password_hash | VARCHAR(100) | Y | - | BCrypt 加密后的密码哈希 |
| password_updated_at | TIMESTAMP | N | - | 上次密码修改时间 |
| password_expire_at | TIMESTAMP | N | - | 密码到期时间(90 天后) |
| failed_login_count | INT | Y | 0 | 连续登录失败次数 |
| lock_until | TIMESTAMP | N | - | 锁定截止时间(5 次失败后 +30 分钟) |
| last_login_at | TIMESTAMP | N | - | 上次登录时间 |
| last_login_ip | VARCHAR(45) | N | - | 上次登录 IP(支持 IPv6) |
| last_login_browser | VARCHAR(100) | N | - | 上次登录浏览器 |
| avatar_url | VARCHAR(500) | N | - | 头像 URL |
| language | VARCHAR(10) | Y | 'zh-CN' | 偏好语言(zh-CN/en-US) |
| timezone | VARCHAR(50) | Y | 'Asia/Shanghai' | 时区 |
| must_change_password | BOOLEAN | Y | false | 下次登录必须改密(管理员重置后置 true) |
| remark | VARCHAR(500) | N | - | 备注 |
| created_by | VARCHAR(50) | Y | 'system' | 创建人 |
| created_at | TIMESTAMP | Y | now() | 创建时间 |
| updated_by | VARCHAR(50) | N | - | 最后更新人 |
| updated_at | TIMESTAMP | N | - | 更新时间 |
| version | INT | Y | 0 | 乐观锁 |
| deleted | CHAR(1) | Y | '0' | 软删除标记 |

**约束**:
- `UNIQUE (login_name) WHERE deleted = '0'`(部分唯一索引,软删除后可复用)
- `UNIQUE (email) WHERE email IS NOT NULL`
- `CHECK (failed_login_count >= 0)`
- `CHECK (status IN ('ENABLED', 'DISABLED', 'LOCKED'))`

#### 3.1.3 业务流程

**新增用户**:
1. 管理员录入 login_name(系统校验唯一性 + 字符规则)
2. 系统自动生成 12 位临时密码(s临时密码写入 `must_change_password=true`)
3. 管理员选择初始角色(可选 1+ 个)和默认管理主体
4. 系统创建用户、绑定用户角色、记录审计日志
5. 管理员将临时密码告知用户(线下)

**重置密码**:
1. 管理员点击"重置密码"
2. 系统生成新临时密码(12 位,大小写+数字+特殊字符)
3. 更新 `password_hash`、`password_updated_at`、`password_expire_at`、`must_change_password=true`
4. 记录审计日志(操作人=管理员,被操作人=用户,操作=RESET_PASSWORD)

---

### 3.2 角色管理 (Role Management) - P0

#### 3.2.1 功能列表

| 功能 | 说明 | 优先级 | 接口 |
|------|------|--------|------|
| 角色列表(分页) | 按 role_code/name/status 筛选 | P0 | GET /api/v1/roles/page |
| 角色详情 | 查询角色及权限清单 | P0 | GET /api/v1/roles/{id} |
| 新增角色 | 创建自定义角色 | P0 | POST /api/v1/roles |
| 更新角色 | 修改角色基本信息(不可改 role_code) | P0 | POST /api/v1/roles/update |
| 删除角色 | 软删除(需先清除所有用户绑定) | P0 | POST /api/v1/roles/delete/{id} |
| 启用/停用角色 | 停用后所有用户该角色失效 | P0 | POST /api/v1/roles/{id}/toggle-status |
| 分配权限 | 给角色绑定多个权限 | P0 | POST /api/v1/roles/{roleId}/permissions |
| 移除权限 | 从角色移除一个权限 | P0 | POST /api/v1/roles/{roleId}/permissions/{permissionId}/remove |
| 查询角色的权限 | 查看角色的权限清单 | P0 | GET /api/v1/roles/{roleId}/permissions |
| 查询角色的用户 | 查看拥有此角色的用户 | P0 | GET /api/v1/roles/{roleId}/users |
| 角色复制 | 基于现有角色快速创建新角色 | P1 | POST /api/v1/roles/{id}/copy |

#### 3.2.2 字段设计 (`tms_roles_t`)

| 字段 | 类型 | 必填 | 默认 | 说明 |
|------|------|------|------|------|
| id | BIGSERIAL | Y | auto | 主键 |
| role_code | VARCHAR(50) | Y | - | 角色编码(唯一,SUPER_ADMIN/ADMIN/TRADER/REVIEWER/VIEWER/AUDITOR/SYS_ADMIN/SEC_ADMIN 等) |
| role_name | VARCHAR(100) | Y | - | 角色名称(中文) |
| role_name_en | VARCHAR(100) | N | - | 英文名 |
| role_type | VARCHAR(20) | Y | 'CUSTOM' | 内置/自定义:BUILT_IN / CUSTOM |
| description | VARCHAR(500) | N | - | 角色描述 |
| data_scope | VARCHAR(20) | Y | 'ENTITY' | 数据范围:ALL / ENTITY / DEPT / SELF / CUSTOM(本期支持 ALL + ENTITY) |
| status | VARCHAR(20) | Y | 'ENABLED' | 状态:ENABLED / DISABLED |
| built_in | BOOLEAN | Y | false | 内置标记(内置角色不可删除) |
| sort_order | INT | Y | 0 | 排序 |
| remark | VARCHAR(500) | N | - | 备注 |
| created_by | VARCHAR(50) | Y | 'system' | 创建人 |
| created_at | TIMESTAMP | Y | now() | 创建时间 |
| updated_by | VARCHAR(50) | N | - | 最后更新人 |
| updated_at | TIMESTAMP | N | - | 更新时间 |
| version | INT | Y | 0 | 乐观锁 |
| deleted | CHAR(1) | Y | '0' | 软删除标记 |

**约束**:
- `UNIQUE (role_code) WHERE deleted = '0'`
- `CHECK (role_type IN ('BUILT_IN', 'CUSTOM'))`
- `CHECK (data_scope IN ('ALL', 'ENTITY', 'DEPT', 'SELF', 'CUSTOM'))`

**预置角色(本期内置)**:

| role_code | role_name | data_scope | 权限范围 |
|-----------|-----------|------------|----------|
| SUPER_ADMIN | 超级管理员 | ALL | 所有权限(绕过一切校验) |
| SYS_ADMIN | 系统管理员 | ALL | 用户/角色/权限/菜单 CRUD |
| SEC_ADMIN | 安全管理员 | ALL | 审计日志查询、密码策略、安全事件 |
| ADMIN | 业务管理员 | ENTITY | 业务数据全权限(本主体) |
| TRADER | 交易员 | ENTITY | 交易录入/修改/查询(本主体) |
| REVIEWER | 复核员 | ENTITY | 交易查询/审批(本主体) |
| VIEWER | 观察员 | ENTITY | 只读(本主体) |
| AUDITOR | 审计员 | ALL | 审计日志只读 |

---

### 3.3 权限管理 (Permission Management) - P0

#### 3.3.1 功能列表

| 功能 | 说明 | 优先级 | 接口 |
|------|------|--------|------|
| 权限列表(分页) | 按 resource_code/action/category 筛选 | P0 | GET /api/v1/permissions/page |
| 权限详情 | 查询权限详情 | P0 | GET /api/v1/permissions/{id} |
| 新增权限 | 创建自定义权限 | P0 | POST /api/v1/permissions |
| 更新权限 | 修改权限描述 | P0 | POST /api/v1/permissions/update |
| 删除权限 | 软删除 | P0 | POST /api/v1/permissions/delete/{id} |
| 按模块分组 | 按模块返回权限树 | P0 | GET /api/v1/permissions/tree |

#### 3.3.2 字段设计 (`tms_permissions_t`)

| 字段 | 类型 | 必填 | 默认 | 说明 |
|------|------|------|------|------|
| id | BIGSERIAL | Y | auto | 主键 |
| permission_code | VARCHAR(100) | Y | - | 权限码(唯一,如 `dealing.fx.create`,格式:模块.实体.操作) |
| resource_code | VARCHAR(50) | Y | - | 资源码(如 `fx-deal`,对应菜单) |
| action | VARCHAR(20) | Y | - | 操作:VIEW / CREATE / UPDATE / DELETE / APPROVE / EXPORT / EXECUTE / CUSTOM |
| category | VARCHAR(50) | Y | - | 权限分类(模块名,如 dealing/basedata/rbac) |
| permission_name | VARCHAR(100) | Y | - | 权限名(如 "外汇交易-新增") |
| permission_name_en | VARCHAR(100) | N | - | 英文名 |
| description | VARCHAR(500) | N | - | 权限描述 |
| api_pattern | VARCHAR(200) | N | - | 关联的 API 路径(如 `POST /api/v1/dealing/fx-deals`) |
| http_method | VARCHAR(10) | N | - | HTTP 方法(GET/POST/PUT/DELETE) |
| sort_order | INT | Y | 0 | 排序 |
| built_in | BOOLEAN | Y | false | 内置标记 |
| status | VARCHAR(20) | Y | 'ENABLED' | 状态 |
| created_by | VARCHAR(50) | Y | 'system' | 创建人 |
| created_at | TIMESTAMP | Y | now() | 创建时间 |
| updated_by | VARCHAR(50) | N | - | 最后更新人 |
| updated_at | TIMESTAMP | N | - | 更新时间 |
| version | INT | Y | 0 | 乐观锁 |
| deleted | CHAR(1) | Y | '0' | 软删除标记 |

**约束**:
- `UNIQUE (permission_code) WHERE deleted = '0'`
- `UNIQUE (resource_code, action) WHERE deleted = '0'`(同一资源+操作只一条)
- `CHECK (action IN ('VIEW', 'CREATE', 'UPDATE', 'DELETE', 'APPROVE', 'EXPORT', 'EXECUTE', 'CUSTOM'))`

#### 3.3.3 预置权限清单(本期)

| 权限码 | 操作 | 所属模块 | 权限名 |
|--------|------|----------|--------|
| basedata.trader.view | VIEW | basedata | 交易员-查看 |
| basedata.trader.create | CREATE | basedata | 交易员-新增 |
| basedata.trader.update | UPDATE | basedata | 交易员-修改 |
| basedata.trader.delete | DELETE | basedata | 交易员-删除 |
| basedata.currency.view | VIEW | basedata | 币种-查看 |
| basedata.currency.create | CREATE | basedata | 币种-新增 |
| basedata.country.view | VIEW | basedata | 国家-查看 |
| basedata.bank.view | VIEW | basedata | 银行-查看 |
| basedata.bankAccount.view | VIEW | basedata | 银行账户-查看 |
| basedata.bankAccount.create | CREATE | basedata | 银行账户-新增 |
| basedata.bankAccount.update | UPDATE | basedata | 银行账户-修改 |
| basedata.bankAccount.delete | DELETE | basedata | 银行账户-删除 |
| basedata.counterparty.view | VIEW | basedata | 对手方-查看 |
| basedata.counterparty.create | CREATE | basedata | 对手方-新增 |
| basedata.counterparty.update | UPDATE | basedata | 对手方-修改 |
| basedata.counterparty.delete | DELETE | basedata | 对手方-删除 |
| basedata.subsidiary.view | VIEW | basedata | 子公司-查看 |
| basedata.managementEntity.view | VIEW | basedata | 管理主体-查看 |
| basedata.managementEntity.create | CREATE | basedata | 管理主体-新增 |
| basedata.managementEntity.update | UPDATE | basedata | 管理主体-修改 |
| basedata.instrument.view | VIEW | basedata | 金融工具-查看 |
| basedata.instrument.create | CREATE | basedata | 金融工具-新增 |
| basedata.instrument.update | UPDATE | basedata | 金融工具-修改 |
| basedata.currencyPair.view | VIEW | basedata | 币种对-查看 |
| basedata.holiday.view | VIEW | basedata | 节假日-查看 |
| dealing.acDeal.view | VIEW | dealing | AC 交易-查看 |
| dealing.acDeal.create | CREATE | dealing | AC 交易-新增 |
| dealing.acDeal.update | UPDATE | dealing | AC 交易-修改 |
| dealing.acDeal.delete | DELETE | dealing | AC 交易-删除 |
| dealing.acDeal.submit | CUSTOM | dealing | AC 交易-提交 |
| dealing.acDeal.approve | APPROVE | dealing | AC 交易-审批 |
| dealing.acDeal.execute | EXECUTE | dealing | AC 交易-执行 |
| dealing.acDeal.export | EXPORT | dealing | AC 交易-导出 |
| dealing.atDeal.view | VIEW | dealing | AT 交易-查看 |
| dealing.atDeal.create | CREATE | dealing | AT 交易-新增 |
| dealing.atDeal.update | UPDATE | dealing | AT 交易-修改 |
| dealing.atDeal.delete | DELETE | dealing | AT 交易-删除 |
| dealing.atDeal.submit | CUSTOM | dealing | AT 交易-提交 |
| dealing.atDeal.approve | APPROVE | dealing | AT 交易-审批 |
| dealing.atDeal.execute | EXECUTE | dealing | AT 交易-执行 |
| dealing.fxDeal.view | VIEW | dealing | FX 交易-查看 |
| dealing.fxDeal.create | CREATE | dealing | FX 交易-新增 |
| dealing.fxDeal.update | UPDATE | dealing | FX 交易-修改 |
| dealing.fxDeal.delete | DELETE | dealing | FX 交易-删除 |
| dealing.fxDeal.rateFix | CUSTOM | dealing | FX 交易-定价(NDF) |
| dealing.action.view | VIEW | dealing | 动作历史-查看 |
| fundplan.plan.view | VIEW | fundplan | 资金计划-查看 |
| fundplan.plan.create | CREATE | fundplan | 资金计划-新增 |
| fundplan.plan.approve | APPROVE | fundplan | 资金计划-审批 |
| valuation.valuation.view | VIEW | valuation | 估值-查看 |
| valuation.valuation.execute | EXECUTE | valuation | 估值-执行 |
| risk.var.view | VIEW | risk | VaR-查看 |
| risk.var.execute | EXECUTE | risk | VaR-执行 |
| report.cockpit.view | VIEW | report | 驾驶舱-查看 |
| report.fundReport.view | VIEW | report | 资金报表-查看 |
| approval.task.view | VIEW | approval | 审批任务-查看 |
| approval.task.approve | APPROVE | approval | 审批任务-处理 |
| rbac.user.view | VIEW | rbac | 用户-查看 |
| rbac.user.create | CREATE | rbac | 用户-新增 |
| rbac.user.update | UPDATE | rbac | 用户-修改 |
| rbac.user.delete | DELETE | rbac | 用户-删除 |
| rbac.user.resetPassword | CUSTOM | rbac | 用户-重置密码 |
| rbac.user.assignRole | CUSTOM | rbac | 用户-分配角色 |
| rbac.role.view | VIEW | rbac | 角色-查看 |
| rbac.role.create | CREATE | rbac | 角色-新增 |
| rbac.role.update | UPDATE | rbac | 角色-修改 |
| rbac.role.delete | DELETE | rbac | 角色-删除 |
| rbac.role.assignPermission | CUSTOM | rbac | 角色-分配权限 |
| rbac.permission.view | VIEW | rbac | 权限-查看 |
| rbac.permission.create | CREATE | rbac | 权限-新增 |
| rbac.permission.update | UPDATE | rbac | 权限-修改 |
| rbac.permission.delete | DELETE | rbac | 权限-删除 |
| rbac.menu.view | VIEW | rbac | 菜单-查看 |
| rbac.menu.create | CREATE | rbac | 菜单-新增 |
| rbac.menu.update | UPDATE | rbac | 菜单-修改 |
| rbac.menu.delete | DELETE | rbac | 菜单-删除 |
| rbac.auditLog.view | VIEW | rbac | 审计日志-查看 |

**共计**:约 80+ 预置权限码,覆盖所有 7 个业务模块 + rbac 自管理。

---

### 3.4 菜单管理 (Menu Management) - P0

#### 3.4.1 功能列表

| 功能 | 说明 | 优先级 | 接口 |
|------|------|--------|------|
| 菜单树(完整) | 返回所有菜单(管理员用) | P0 | GET /api/v1/menus/tree |
| 当前用户菜单 | 返回当前用户可见菜单(按权限过滤) | P0 | GET /api/v1/menus/user |
| 菜单列表(分页) | 按层级平铺分页 | P0 | GET /api/v1/menus/page |
| 菜单详情 | 查询菜单详情 | P0 | GET /api/v1/menus/{id} |
| 新增菜单 | 创建菜单项 | P0 | POST /api/v1/menus |
| 更新菜单 | 修改菜单信息 | P0 | POST /api/v1/menus/update |
| 删除菜单 | 软删除(需先无子菜单) | P0 | POST /api/v1/menus/delete/{id} |
| 同步菜单 | 从前端 router 配置扫描同步 | P1 | POST /api/v1/menus/sync |
| 菜单排序 | 拖拽排序 | P0 | POST /api/v1/menus/sort |

#### 3.4.2 字段设计 (`tms_menus_t`)

| 字段 | 类型 | 必填 | 默认 | 说明 |
|------|------|------|------|------|
| id | BIGSERIAL | Y | auto | 主键 |
| menu_code | VARCHAR(50) | Y | - | 菜单编码(唯一,如 `dealing_fx_deal`) |
| menu_name | VARCHAR(100) | Y | - | 菜单名称(中文) |
| menu_name_en | VARCHAR(100) | N | - | 英文名 |
| parent_id | BIGINT | N | - | 父菜单 ID(顶级为 NULL) |
| path | VARCHAR(200) | N | - | 路由路径(如 `/dealing/fx-deal`) |
| component | VARCHAR(200) | N | - | 组件路径(如 `@/views/dealing/FxDealList.vue`) |
| redirect | VARCHAR(200) | N | - | 重定向路径 |
| icon | VARCHAR(50) | N | - | 菜单图标(Element Plus 图标名) |
| sort_order | INT | Y | 0 | 排序 |
| menu_type | VARCHAR(20) | Y | 'MENU' | 类型:DIRECTORY(目录)/ MENU(菜单)/ BUTTON(按钮) |
| permission_code | VARCHAR(100) | N | - | 关联权限码(如 `dealing.fxDeal.view`) |
| hidden | BOOLEAN | Y | false | 是否隐藏 |
| keep_alive | BOOLEAN | Y | false | 是否缓存组件 |
| status | VARCHAR(20) | Y | 'ENABLED' | 状态 |
| is_external | BOOLEAN | Y | false | 是否外链 |
| external_url | VARCHAR(500) | N | - | 外链 URL |
| remark | VARCHAR(500) | N | - | 备注 |
| created_by | VARCHAR(50) | Y | 'system' | 创建人 |
| created_at | TIMESTAMP | Y | now() | 创建时间 |
| updated_by | VARCHAR(50) | N | - | 最后更新人 |
| updated_at | TIMESTAMP | N | - | 更新时间 |
| version | INT | Y | 0 | 乐观锁 |
| deleted | CHAR(1) | Y | '0' | 软删除标记 |

**约束**:
- `UNIQUE (menu_code) WHERE deleted = '0'`
- `CHECK (menu_type IN ('DIRECTORY', 'MENU', 'BUTTON'))`
- `CHECK (parent_id <> id)`(防止自己成为自己的父级)

#### 3.4.3 菜单数据来源

本期采用**数据库存储 + 前端静态 router 双轨**模式:
- **菜单**:以数据库为主,前端 router 作为初始同步源(系统启动时若菜单表为空则从 router 自动同步)
- **按钮**:完全由数据库管理,前端通过 `v-if="hasPerm('xxx')"` 控制

---

### 3.5 用户-角色绑定 (User-Role) - P0

#### 3.5.1 功能列表

| 功能 | 说明 | 优先级 | 接口 |
|------|------|--------|------|
| 给用户分配角色(多) | 一次分配多个角色,每个角色可指定数据范围 | P0 | POST /api/v1/users/{id}/roles |
| 查询用户的所有角色 | 含角色详情和数据范围 | P0 | GET /api/v1/users/{id}/roles |
| 移除用户的某个角色 | 按 user_role_rel_id 删除 | P0 | POST /api/v1/users/{id}/roles/{roleId}/remove |
| 更新用户某角色的数据范围 | 单独修改某 user-role 的 management_entity_id | P0 | POST /api/v1/users/{id}/roles/{roleId}/update-scope |
| 查询用户可见的所有数据范围 | 聚合用户所有角色的 management_entity_id 列表 | P0 | GET /api/v1/users/{id}/management-entities |

#### 3.5.2 字段设计 (`tms_user_roles_t`)

| 字段 | 类型 | 必填 | 默认 | 说明 |
|------|------|------|------|------|
| id | BIGSERIAL | Y | auto | 主键 |
| user_id | BIGINT | Y | - | 用户 ID(FK→tms_users_t.id) |
| role_id | BIGINT | Y | - | 角色 ID(FK→tms_roles_t.id) |
| management_entity_id | BIGINT | N | - | 数据范围-管理主体 ID(FK→tms_management_entity_t.id,NULL 表示不限制) |
| valid_from | TIMESTAMP | N | - | 生效时间 |
| valid_to | TIMESTAMP | N | - | 失效时间 |
| assigned_by | VARCHAR(50) | Y | - | 分配人(管理员 login_name) |
| assigned_at | TIMESTAMP | Y | now() | 分配时间 |
| remark | VARCHAR(500) | N | - | 备注 |
| created_by | VARCHAR(50) | Y | - | 创建人 |
| created_at | TIMESTAMP | Y | now() | 创建时间 |
| updated_by | VARCHAR(50) | N | - | 最后更新人 |
| updated_at | TIMESTAMP | N | - | 更新时间 |
| version | INT | Y | 0 | 乐观锁 |
| deleted | CHAR(1) | Y | '0' | 软删除标记 |

**约束**:
- `UNIQUE (user_id, role_id, management_entity_id) WHERE deleted = '0'`(同一用户同一角色同一数据范围不重复)
- `CHECK (valid_from IS NULL OR valid_to IS NULL OR valid_from <= valid_to)`

**业务逻辑**:
- 一个用户可绑定多个角色,角色之间权限**取并集**
- 数据范围:**取并集**(若任一角色 data_scope=ALL,则用户拥有 ALL)
- 失效时间 < 当前时间:角色自动失效(查询时过滤)

---

### 3.6 角色-权限绑定 (Role-Permission) - P0

#### 3.6.1 功能列表

| 功能 | 说明 | 优先级 | 接口 |
|------|------|--------|------|
| 给角色分配权限(多) | 一次分配多个权限码 | P0 | POST /api/v1/roles/{roleId}/permissions |
| 查询角色的所有权限 | 含权限详情 | P0 | GET /api/v1/roles/{roleId}/permissions |
| 移除角色的某个权限 | 按 role_permission_rel_id 删除 | P0 | POST /api/v1/roles/{roleId}/permissions/{permissionId}/remove |
| 清空角色所有权限 | 一键移除所有权限 | P1 | POST /api/v1/roles/{roleId}/permissions/clear |
| 复制角色权限 | 从源角色复制权限到目标 | P1 | POST /api/v1/roles/{targetRoleId}/copy-permissions/{sourceRoleId} |

#### 3.6.2 字段设计 (`tms_role_permissions_t`)

| 字段 | 类型 | 必填 | 默认 | 说明 |
|------|------|------|------|------|
| id | BIGSERIAL | Y | auto | 主键 |
| role_id | BIGINT | Y | - | 角色 ID(FK→tms_roles_t.id) |
| permission_id | BIGINT | Y | - | 权限 ID(FK→tms_permissions_t.id) |
| granted_by | VARCHAR(50) | Y | - | 授权人 |
| granted_at | TIMESTAMP | Y | now() | 授权时间 |
| created_by | VARCHAR(50) | Y | - | 创建人 |
| created_at | TIMESTAMP | Y | now() | 创建时间 |
| updated_by | VARCHAR(50) | N | - | 最后更新人 |
| updated_at | TIMESTAMP | N | - | 更新时间 |
| version | INT | Y | 0 | 乐观锁 |
| deleted | CHAR(1) | Y | '0' | 软删除标记 |

**约束**:
- `UNIQUE (role_id, permission_id) WHERE deleted = '0'`

---

### 3.7 登录认证 (Authentication) - P0

#### 3.7.1 功能列表

| 功能 | 说明 | 优先级 | 接口 |
|------|------|--------|------|
| 登录 | 用户名+密码登录,返回 JWT | P0 | POST /api/v1/auth/login |
| 登出 | 客户端清除 token,服务端可选加入黑名单 | P0 | POST /api/v1/auth/logout |
| 刷新 token | 用 refresh_token 换新 access_token | P0 | POST /api/v1/auth/refresh |
| 当前用户信息 | 返回 user + roles + permissions + menus | P0 | GET /api/v1/auth/me |
| 修改自己的密码 | 用户登录后修改密码 | P0 | POST /api/v1/auth/change-password |

#### 3.7.2 JWT Token 设计

**Access Token**:
```json
{
  "header": {
    "alg": "HS256",
    "typ": "JWT"
  },
  "payload": {
    "sub": "zhangsan",             // 用户 login_name
    "uid": 10001,                  // 用户 ID
    "roles": ["ADMIN", "TRADER"],  // 角色编码列表
    "data_scope": "ENTITY",        // 聚合后的数据范围
    "entity_ids": [1, 2, 3],       // 可见的管理主体 ID 列表
    "iss": "opentms",              // 签发者
    "iat": 1720051200,             // 签发时间
    "exp": 1720080000              // 过期时间(8 小时)
  }
}
```

**Refresh Token**:
- 独立的 refresh_token,有效期 7 天
- 存储于 Redis:`refresh_token:{user_id} = {token_hash}`,TTL = 7d
- 刷新时轮换:旧 refresh_token 失效,签发新的

**签名密钥**:
- `application.yml` 中配置 `opentms.jwt.secret`(HS256,Base64 编码,≥32 字节)
- **生产环境必须从环境变量/密钥管理服务注入**,禁止硬编码

#### 3.7.3 登录流程(详细时序)

```
[前端]                  [后端 AuthController]            [UserService]              [JWT Util]
   │                            │                              │                          │
   │  POST /auth/login          │                              │                          │
   │  {loginName, password}     │                              │                          │
   │ ────────────────────────>  │                              │                          │
   │                            │  1.校验请求频率(Redis 限流)  │                          │
   │                            │  2.查询用户(按 login_name)    │                          │
   │                            │ ─────────────────────────>  │                          │
   │                            │                              │  3.校验 status            │
   │                            │                              │  4.校验 lock_until        │
   │                            │                              │  5.BCrypt 校验密码        │
   │                            │                              │  6.成功 → 清零失败次数    │
   │                            │                              │     失败 → +1, 5 次锁定   │
   │                            │                              │  7.查询用户角色+数据范围  │
   │                            │ <─────────────────────────  │                          │
   │                            │  8.生成 JWT(8h)              │                          │
   │                            │ ────────────────────────────────────────────────────> │
   │                            │ <──────────────────────────────────────────────────── │
   │                            │  9.生成 refresh_token(7d)    │                          │
   │                            │  10.写入 Redis               │                          │
   │                            │  11.记录登录日志(audit)      │                          │
   │ <────────────────────────  │                              │                          │
   │  {accessToken,             │                              │                          │
   │   refreshToken,            │                              │                          │
   │   userInfo,                │                              │                          │
   │   permissions,             │                              │                          │
   │   menus}                   │                              │                          │
```

#### 3.7.4 失败锁定规则

| 条件 | 动作 |
|------|------|
| 密码错误 1-4 次 | `failed_login_count += 1`,返回"用户名或密码错误" |
| 密码错误第 5 次 | `status='LOCKED'`,`lock_until = now() + 30min`,返回"账户已锁定 30 分钟" |
| 锁定期间登录 | 返回"账户已锁定,剩余 X 分钟",不计失败次数 |
| 锁定 30 分钟后登录 | 正常校验(允许自动解锁)或由管理员手动解锁 |
| 登录成功 | `failed_login_count = 0`,`last_login_at = now()`,`last_login_ip = <ip>` |

#### 3.7.5 密码策略

| 规则 | 说明 | 配置项 |
|------|------|--------|
| 最小长度 | 8 位 | `opentms.security.password.min-length=8` |
| 复杂度 | 至少包含:大写字母、小写字母、数字、特殊字符(各 1+) | `opentms.security.password.complexity=true` |
| 强制更换 | 90 天 | `opentms.security.password.expire-days=90` |
| 不能与旧密码相同 | 最近 3 次 | `opentms.security.password.history-count=3` |
| 不能包含用户名 | login_name 子串 | `opentms.security.password.contain-username=false` |

**密码加密**:
- 算法:BCrypt(strength=10)
- 不存明文,不解密(单向)
- 重置密码时生成 12 位临时密码,首次登录强制修改

---

### 3.8 数据权限 (Data Scope) - P0

#### 3.8.1 数据范围枚举

| data_scope | 含义 | SQL 注入条件 |
|------------|------|--------------|
| ALL | 全部数据 | `1=1`(无过滤) |
| ENTITY | 本管理主体(含下级,本期仅自身) | `management_entity_id IN (用户可见 entity_ids)` |
| DEPT | 本部门 | 预留字段,本期未实现 |
| SELF | 仅本人 | `created_by = current_user.login_name` |
| CUSTOM | 自定义 | 预留,本期未实现 |

#### 3.8.2 数据权限传递规则

当用户拥有多个角色时:
- **若任一角色 data_scope=ALL** → 用户 data_scope = ALL
- **否则** → 用户 data_scope = ENTITY(取所有角色的 management_entity_id 并集)

#### 3.8.3 SQL 拦截实现

**MyBatis 拦截器** `DataScopeInterceptor`:
```java
@Intercepts(@Signature(type = Executor.class, method = "query", ...))
public class DataScopeInterceptor implements Interceptor {
    @Override
    public Object intercept(Invocation invocation) {
        // 1. 从 ThreadLocal 获取 currentUser
        // 2. 若 data_scope = ALL 或 SUPER_ADMIN → 跳过
        // 3. 否则在 SQL 末尾追加 AND management_entity_id IN (...)
        // 4. 同时处理 created_by = ? 条件(SELF 模式)
    }
}
```

**ThreadLocal 设计**:
- `SecurityContextHolder.setUserContext(userContext)` 在 JWT 过滤器中设置
- 业务代码通过 `SecurityContextHolder.getCurrentUser()` 获取
- `@Transactional` 方法结束后清理

#### 3.8.4 SUPER_ADMIN 特殊处理

- role_code = `SUPER_ADMIN` 的用户**绕过一切数据权限校验**
- 同时绕过功能权限校验(`@PreAuthorize` 直接放行)
- 不可降级(不能被移除 SUPER_ADMIN 角色)
- 系统启动时若不存在 SUPER_ADMIN,则自动创建默认账户 `admin / Admin@123`(首次启动后强制修改)

---

## 四、字段设计(核心表)

### 4.1 表总览

| # | 表名 | 说明 | 记录数估算 |
|---|------|------|-----------|
| 1 | `tms_users_t` | 用户主表 | 10-1000 |
| 2 | `tms_roles_t` | 角色主表 | 5-50 |
| 3 | `tms_permissions_t` | 权限主表 | 50-500 |
| 4 | `tms_menus_t` | 菜单主表 | 30-200 |
| 5 | `tms_user_roles_t` | 用户-角色关联表 | 100-5000 |
| 6 | `tms_role_permissions_t` | 角色-权限关联表 | 200-10000 |
| 7 | `tms_audit_logs_t` | 审计日志(P1) | 100000+ |

### 4.2 完整 DDL(`tms_users_t`)

```sql
CREATE TABLE tms_users_t (
    id                          BIGSERIAL       PRIMARY KEY,
    login_name                  VARCHAR(50)     NOT NULL,
    display_name                VARCHAR(100)    NOT NULL,
    email                       VARCHAR(100),
    phone                       VARCHAR(20),
    dept_id                     BIGINT,
    dept_name                   VARCHAR(100),
    default_management_entity_id BIGINT,
    status                      VARCHAR(20)     NOT NULL DEFAULT 'ENABLED',
    password_hash               VARCHAR(100)    NOT NULL,
    password_updated_at         TIMESTAMP,
    password_expire_at          TIMESTAMP,
    failed_login_count          INT             NOT NULL DEFAULT 0,
    lock_until                  TIMESTAMP,
    last_login_at               TIMESTAMP,
    last_login_ip               VARCHAR(45),
    last_login_browser          VARCHAR(100),
    avatar_url                  VARCHAR(500),
    language                    VARCHAR(10)     NOT NULL DEFAULT 'zh-CN',
    timezone                    VARCHAR(50)     NOT NULL DEFAULT 'Asia/Shanghai',
    must_change_password        BOOLEAN         NOT NULL DEFAULT false,
    remark                      VARCHAR(500),
    created_by                  VARCHAR(50)     NOT NULL DEFAULT 'system',
    created_at                  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by                  VARCHAR(50),
    updated_at                  TIMESTAMP,
    version                     INT             NOT NULL DEFAULT 0,
    deleted                     CHAR(1)         NOT NULL DEFAULT '0',
    
    CONSTRAINT uidx_users_login_name UNIQUE (login_name),
    CONSTRAINT uidx_users_email UNIQUE (email),
    CONSTRAINT chk_users_status CHECK (status IN ('ENABLED', 'DISABLED', 'LOCKED')),
    CONSTRAINT chk_users_failed_count CHECK (failed_login_count >= 0)
);
CREATE INDEX idx_users_status ON tms_users_t(status) WHERE deleted = '0';
CREATE INDEX idx_users_display_name ON tms_users_t(display_name);
```

### 4.3 完整 DDL(`tms_roles_t`)

```sql
CREATE TABLE tms_roles_t (
    id                          BIGSERIAL       PRIMARY KEY,
    role_code                   VARCHAR(50)     NOT NULL,
    role_name                   VARCHAR(100)    NOT NULL,
    role_name_en                VARCHAR(100),
    role_type                   VARCHAR(20)     NOT NULL DEFAULT 'CUSTOM',
    description                 VARCHAR(500),
    data_scope                  VARCHAR(20)     NOT NULL DEFAULT 'ENTITY',
    status                      VARCHAR(20)     NOT NULL DEFAULT 'ENABLED',
    built_in                    BOOLEAN         NOT NULL DEFAULT false,
    sort_order                  INT             NOT NULL DEFAULT 0,
    remark                      VARCHAR(500),
    created_by                  VARCHAR(50)     NOT NULL DEFAULT 'system',
    created_at                  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by                  VARCHAR(50),
    updated_at                  TIMESTAMP,
    version                     INT             NOT NULL DEFAULT 0,
    deleted                     CHAR(1)         NOT NULL DEFAULT '0',
    
    CONSTRAINT uidx_roles_code UNIQUE (role_code),
    CONSTRAINT chk_roles_type CHECK (role_type IN ('BUILT_IN', 'CUSTOM')),
    CONSTRAINT chk_roles_data_scope CHECK (data_scope IN ('ALL', 'ENTITY', 'DEPT', 'SELF', 'CUSTOM')),
    CONSTRAINT chk_roles_status CHECK (status IN ('ENABLED', 'DISABLED'))
);
```

### 4.4 完整 DDL(`tms_permissions_t`)

```sql
CREATE TABLE tms_permissions_t (
    id                          BIGSERIAL       PRIMARY KEY,
    permission_code             VARCHAR(100)    NOT NULL,
    resource_code               VARCHAR(50)     NOT NULL,
    action                      VARCHAR(20)     NOT NULL,
    category                    VARCHAR(50)     NOT NULL,
    permission_name             VARCHAR(100)    NOT NULL,
    permission_name_en          VARCHAR(100),
    description                 VARCHAR(500),
    api_pattern                 VARCHAR(200),
    http_method                 VARCHAR(10),
    sort_order                  INT             NOT NULL DEFAULT 0,
    built_in                    BOOLEAN         NOT NULL DEFAULT false,
    status                      VARCHAR(20)     NOT NULL DEFAULT 'ENABLED',
    created_by                  VARCHAR(50)     NOT NULL DEFAULT 'system',
    created_at                  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by                  VARCHAR(50),
    updated_at                  TIMESTAMP,
    version                     INT             NOT NULL DEFAULT 0,
    deleted                     CHAR(1)         NOT NULL DEFAULT '0',
    
    CONSTRAINT uidx_perm_code UNIQUE (permission_code),
    CONSTRAINT uidx_perm_resource_action UNIQUE (resource_code, action),
    CONSTRAINT chk_perm_action CHECK (action IN ('VIEW','CREATE','UPDATE','DELETE','APPROVE','EXPORT','EXECUTE','CUSTOM')),
    CONSTRAINT chk_perm_status CHECK (status IN ('ENABLED', 'DISABLED'))
);
CREATE INDEX idx_perm_category ON tms_permissions_t(category);
```

### 4.5 完整 DDL(`tms_menus_t`)

```sql
CREATE TABLE tms_menus_t (
    id                          BIGSERIAL       PRIMARY KEY,
    menu_code                   VARCHAR(50)     NOT NULL,
    menu_name                   VARCHAR(100)    NOT NULL,
    menu_name_en                VARCHAR(100),
    parent_id                   BIGINT,
    path                        VARCHAR(200),
    component                   VARCHAR(200),
    redirect                    VARCHAR(200),
    icon                        VARCHAR(50),
    sort_order                  INT             NOT NULL DEFAULT 0,
    menu_type                   VARCHAR(20)     NOT NULL DEFAULT 'MENU',
    permission_code             VARCHAR(100),
    hidden                      BOOLEAN         NOT NULL DEFAULT false,
    keep_alive                  BOOLEAN         NOT NULL DEFAULT false,
    status                      VARCHAR(20)     NOT NULL DEFAULT 'ENABLED',
    is_external                 BOOLEAN         NOT NULL DEFAULT false,
    external_url                VARCHAR(500),
    remark                      VARCHAR(500),
    created_by                  VARCHAR(50)     NOT NULL DEFAULT 'system',
    created_at                  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by                  VARCHAR(50),
    updated_at                  TIMESTAMP,
    version                     INT             NOT NULL DEFAULT 0,
    deleted                     CHAR(1)         NOT NULL DEFAULT '0',
    
    CONSTRAINT uidx_menu_code UNIQUE (menu_code),
    CONSTRAINT chk_menu_type CHECK (menu_type IN ('DIRECTORY', 'MENU', 'BUTTON')),
    CONSTRAINT chk_menu_status CHECK (status IN ('ENABLED', 'DISABLED')),
    CONSTRAINT chk_menu_not_self_parent CHECK (parent_id IS NULL OR parent_id <> id)
);
CREATE INDEX idx_menu_parent ON tms_menus_t(parent_id);
CREATE INDEX idx_menu_sort ON tms_menus_t(sort_order);
```

### 4.6 完整 DDL(`tms_user_roles_t`)

```sql
CREATE TABLE tms_user_roles_t (
    id                          BIGSERIAL       PRIMARY KEY,
    user_id                     BIGINT          NOT NULL,
    role_id                     BIGINT          NOT NULL,
    management_entity_id        BIGINT,
    valid_from                  TIMESTAMP,
    valid_to                    TIMESTAMP,
    assigned_by                 VARCHAR(50)     NOT NULL,
    assigned_at                 TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    remark                      VARCHAR(500),
    created_by                  VARCHAR(50)     NOT NULL,
    created_at                  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by                  VARCHAR(50),
    updated_at                  TIMESTAMP,
    version                     INT             NOT NULL DEFAULT 0,
    deleted                     CHAR(1)         NOT NULL DEFAULT '0',
    
    CONSTRAINT uidx_user_role_entity UNIQUE (user_id, role_id, management_entity_id),
    CONSTRAINT chk_ur_valid_range CHECK (valid_from IS NULL OR valid_to IS NULL OR valid_from <= valid_to),
    CONSTRAINT fk_ur_user FOREIGN KEY (user_id) REFERENCES tms_users_t(id),
    CONSTRAINT fk_ur_role FOREIGN KEY (role_id) REFERENCES tms_roles_t(id)
);
CREATE INDEX idx_ur_user ON tms_user_roles_t(user_id) WHERE deleted = '0';
CREATE INDEX idx_ur_role ON tms_user_roles_t(role_id) WHERE deleted = '0';
CREATE INDEX idx_ur_entity ON tms_user_roles_t(management_entity_id) WHERE deleted = '0';
```

### 4.7 完整 DDL(`tms_role_permissions_t`)

```sql
CREATE TABLE tms_role_permissions_t (
    id                          BIGSERIAL       PRIMARY KEY,
    role_id                     BIGINT          NOT NULL,
    permission_id               BIGINT          NOT NULL,
    granted_by                  VARCHAR(50)     NOT NULL,
    granted_at                  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by                  VARCHAR(50)     NOT NULL,
    created_at                  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by                  VARCHAR(50),
    updated_at                  TIMESTAMP,
    version                     INT             NOT NULL DEFAULT 0,
    deleted                     CHAR(1)         NOT NULL DEFAULT '0',
    
    CONSTRAINT uidx_rp_role_perm UNIQUE (role_id, permission_id),
    CONSTRAINT fk_rp_role FOREIGN KEY (role_id) REFERENCES tms_roles_t(id),
    CONSTRAINT fk_rp_perm FOREIGN KEY (permission_id) REFERENCES tms_permissions_t(id)
);
CREATE INDEX idx_rp_role ON tms_role_permissions_t(role_id) WHERE deleted = '0';
CREATE INDEX idx_rp_perm ON tms_role_permissions_t(permission_id) WHERE deleted = '0';
```

### 4.8 完整 DDL(`tms_audit_logs_t`) - P1

```sql
CREATE TABLE tms_audit_logs_t (
    id                          BIGSERIAL       PRIMARY KEY,
    log_type                    VARCHAR(20)     NOT NULL,         -- LOGIN / LOGOUT / CREATE / UPDATE / DELETE / APPROVE / EXECUTE / EXPORT
    user_id                     BIGINT,
    login_name                  VARCHAR(50),
    operation                   VARCHAR(200)    NOT NULL,         -- 操作描述(如 "新增交易")
    resource_type               VARCHAR(50),                     -- 资源类型(如 "FX_DEAL")
    resource_id                 VARCHAR(50),                     -- 资源 ID(如 deal_no)
    request_method              VARCHAR(10),
    request_url                 VARCHAR(500),
    request_params              TEXT,                            -- JSON 格式
    response_status             INT,
    error_message               TEXT,
    client_ip                   VARCHAR(45),
    user_agent                  VARCHAR(500),
    duration_ms                 BIGINT,
    trace_id                    VARCHAR(64),
    operation_time              TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_log_type CHECK (log_type IN ('LOGIN','LOGOUT','CREATE','UPDATE','DELETE','APPROVE','EXECUTE','EXPORT','QUERY','CUSTOM'))
);
CREATE INDEX idx_log_user_time ON tms_audit_logs_t(login_name, operation_time DESC);
CREATE INDEX idx_log_resource ON tms_audit_logs_t(resource_type, resource_id);
CREATE INDEX idx_log_time ON tms_audit_logs_t(operation_time DESC);
```

**保留策略**:本期保留 5 年,按月分区(`PARTITION BY RANGE (operation_time)`),P1 实现自动归档。

---

## 五、业务规则

### R1 - 密码复杂度
密码至少 8 位,必须包含:大写字母(A-Z)、小写字母(a-z)、数字(0-9)、特殊字符(`!@#$%^&*()_+-=[]{}|;:,.<>?`),各至少 1 个。BCrypt 加密存储,strength=10。

### R2 - 失败锁定
连续登录失败 5 次后,账户 `status='LOCKED'`,`lock_until=now()+30min`。锁定期间禁止登录(返回剩余锁定时间)。锁定 30 分钟后允许再次尝试(自动解锁)。管理员可手动解锁。

### R3 - 密码定期更换
密码 90 天强制更换。`password_expire_at < now()` 时,登录后强制跳转修改密码页。`must_change_password=true` 时同样强制修改。

### R4 - 内置角色保护
`built_in=true` 的角色不可删除、不可修改 role_code,但可修改 description。新增用户时可分配内置角色。

### R5 - 删除用户前置检查
删除用户前必须先清除所有角色绑定(`tms_user_roles_t` 中 `user_id=X` 的所有记录)。删除采用软删除(`deleted='1'`),保留审计追溯能力。

### R6 - 删除角色前置检查
删除角色前必须先清除所有用户绑定(`tms_user_roles_t` 中 `role_id=X` 的所有记录)和所有权限绑定(`tms_role_permissions_t` 中 `role_id=X` 的所有记录)。内置角色(`built_in=true`)不可删除。

### R7 - 权限唯一性
同一 `resource_code + action` 组合只允许存在一条权限记录(未删除状态下)。系统初始化时按预置清单 INSERT,后续不允许重复 INSERT。

### R8 - 超级管理员不可降级
role_code = `SUPER_ADMIN` 的用户不可被移除该角色、不可被删除、不可被停用。系统中至少有 1 个 SUPER_ADMIN(系统初始化保证)。尝试移除时返回 403 "超级管理员角色不可移除"。

### R9 - 菜单权限码校验
创建/更新菜单时,若 `permission_code` 非空,必须对应 `tms_permissions_t` 中已存在且未删除的权限码。否则返回 400 "权限码不存在"。

### R10 - 用户默认数据范围
用户登录后,系统聚合该用户所有角色的数据范围:
- 若任一角色 data_scope='ALL' → 用户 data_scope='ALL'
- 否则用户 data_scope='ENTITY',entity_ids 为所有 `tms_user_roles_t.management_entity_id` 的并集

### R11 - 多角色权限合并
用户拥有的所有角色权限**取并集**。例如用户同时拥有 TRADER(可创建交易)和 REVIEWER(可审批交易),则用户既可创建也可审批。

### R12 - JWT 过期处理
- access_token 过期 → 前端拦截 401,自动调用 `/auth/refresh` 获取新 token(用 refresh_token)
- refresh_token 过期 → 强制跳登录页
- 同一 refresh_token 只能使用 1 次(轮换机制)

### R13 - 登录限流
同一 login_name 1 分钟内最多 10 次登录尝试(IP 维度 1 分钟 20 次)。超过返回 429 "请求过于频繁"。

### R14 - 操作幂等
登录、刷新 token、分配角色等接口必须支持 `X-Idempotency-Key` 头(60 秒内同 key 返回原结果)。分配角色使用幂等表 `tms_idempotency_t`。

### R15 - 审计必填字段
所有**写操作**(POST/PUT/DELETE)必须记录审计日志:操作人(login_name)、操作时间、操作类型、resource_type、resource_id、IP、User-Agent。查询操作(GET)本期不记录,P1 可选。

### R16 - 软删除恢复
用户/角色/权限/菜单删除采用软删除(`deleted='1'`)。系统不提供 UI 恢复功能,需 DBA 直接 SQL 恢复。审计日志表**不**做软删除(append-only)。

### R17 - 并发登录控制
同一用户同一时间最多 5 个有效 session。超过时:踢掉最早登录的 session(将其 token 加入 Redis 黑名单,TTL=剩余有效期)。

### R18 - 跨域与 CORS
API 默认仅允许同源请求,需在 `application.yml` 中显式配置 `opentms.cors.allowed-origins`(逗号分隔)。CSRF 防护:所有写操作校验 `Origin` / `Referer` 头。

### R19 - 时间字段规范
所有时间字段统一 `TIMESTAMP` (不带时区),数据库存服务器本地时间(`Asia/Shanghai`)。前端显示时按用户 `timezone` 字段转换。

### R20 - 接口路径与权限映射
权限码 `xxx.yyy.create` 对应 API `POST /api/v1/xxx/yyy`。后端通过拦截器从 JWT 获取用户权限码列表,匹配 `api_pattern + http_method` 字段(预置时已关联)。无权限时返回 403,body 含 `{code:403, message:"无权限:dealing.fx.create"}`。

---

## 六、菜单与权限映射

### 6.1 一级菜单

| menu_code | menu_name | path | icon | 子菜单 |
|-----------|-----------|------|------|--------|
| dashboard | 首页 | /dashboard | HomeFilled | - |
| basedata | 基础数据 | /basedata | Files | 见下 |
| dealing | 交易管理 | /dealing | Tickets | 见下 |
| approval | 审批中心 | /approval | CircleCheck | 见下 |
| fundplan | 资金计划 | /fundplan | Money | 见下 |
| valuation | 估值 | /valuation | DataAnalysis | 见下 |
| risk | 风险管理 | /risk | Warning | 见下 |
| report | 报表中心 | /report | Document | 见下 |
| system | 系统管理 | /system | Setting | 见下 |

### 6.2 二级菜单 + 权限映射

| 父菜单 | 子菜单 menu_code | 子菜单 path | component | 权限码 | 排序 |
|--------|------------------|-------------|-----------|--------|------|
| basedata | basedata_trader | /basedata/trader | TraderList.vue | basedata.trader.view | 1 |
| basedata | basedata_currency | /basedata/currency | CurrencyList.vue | basedata.currency.view | 2 |
| basedata | basedata_country | /basedata/country | CountryList.vue | basedata.country.view | 3 |
| basedata | basedata_holiday | /basedata/holiday | HolidayList.vue | basedata.holiday.view | 4 |
| basedata | basedata_bank | /basedata/bank | BankList.vue | basedata.bank.view | 5 |
| basedata | basedata_bank_account | /basedata/bank-account | BankAccountList.vue | basedata.bankAccount.view | 6 |
| basedata | basedata_counterparty | /basedata/counterparty | CounterpartyList.vue | basedata.counterparty.view | 7 |
| basedata | basedata_counterparty_account | /basedata/counterparty-account | CounterpartyAccountList.vue | basedata.counterparty.view | 8 |
| basedata | basedata_currency_pair | /basedata/currency-pair | CurrencyPairList.vue | basedata.currencyPair.view | 9 |
| basedata | basedata_subsidiary | /basedata/subsidiary | SubsidiaryList.vue | basedata.subsidiary.view | 10 |
| basedata | basedata_management_entity | /basedata/management-entity | ManagementEntityList.vue | basedata.managementEntity.view | 11 |
| basedata | basedata_instrument | /basedata/instrument | InstrumentList.vue | basedata.instrument.view | 12 |
| dealing | dealing_ac_deal | /dealing/ac-deal | AcDealList.vue | dealing.acDeal.view | 1 |
| dealing | dealing_at_deal | /dealing/at-deal | AtDealList.vue | dealing.atDeal.view | 2 |
| dealing | dealing_fx_deal | /dealing/fx-deal | FxDealList.vue | dealing.fxDeal.view | 3 |
| dealing | dealing_action | /dealing/action | ActionList.vue | dealing.action.view | 4 |
| approval | approval_template | /approval/template | WorkflowTemplate.vue | approval.task.view | 1 |
| approval | approval_task | /approval/task | ApprovalTask.vue | approval.task.view | 2 |
| fundplan | fundplan_list | /fundplan/list | FundPlanList.vue | fundplan.plan.view | 1 |
| risk | risk_var | /risk/var | VarReportList.vue | risk.var.view | 1 |
| report | report_list | /report/list | ReportList.vue | report.fundReport.view | 1 |
| system | system_user | /system/user | UserList.vue | rbac.user.view | 1 |
| system | system_role | /system/role | RoleList.vue | rbac.role.view | 2 |
| system | system_permission | /system/permission | PermissionList.vue | rbac.permission.view | 3 |
| system | system_menu | /system/menu | MenuList.vue | rbac.menu.view | 4 |
| system | system_audit_log | /system/audit-log | AuditLogList.vue | rbac.auditLog.view | 5 |

### 6.3 按钮权限映射

| 页面 | 按钮 | 权限码 | 元素 |
|------|------|--------|------|
| AcDealList | 新增 | dealing.acDeal.create | `<el-button v-if="hasPerm('dealing.acDeal.create')">` |
| AcDealList | 修改 | dealing.acDeal.update | `<el-button>` |
| AcDealList | 删除 | dealing.acDeal.delete | `<el-button>` |
| AcDealList | 提交 | dealing.acDeal.submit | `<el-button>` |
| AcDealList | 审批 | dealing.acDeal.approve | `<el-button>` |
| AcDealList | 执行 | dealing.acDeal.execute | `<el-button>` |
| AcDealList | 导出 | dealing.acDeal.export | `<el-button>` |
| UserList | 新增用户 | rbac.user.create | `<el-button>` |
| UserList | 重置密码 | rbac.user.resetPassword | `<el-button>` |
| UserList | 分配角色 | rbac.user.assignRole | `<el-button>` |
| RoleList | 分配权限 | rbac.role.assignPermission | `<el-button>` |

---

## 七、登录流程(端到端)

### 7.1 时序图(细化)

```
用户          前端(Vue)              后端(Spring)               MySQL/Redis
 │                │                       │                          │
 │ 1.输入账号密码  │                       │                          │
 │ ──────────────>│                       │                          │
 │                │ 2.POST /auth/login    │                          │
 │                │  {loginName,pwd}       │                          │
 │                │ ──────────────────────>│                          │
 │                │                       │ 3.查询用户(按 login_name)│
 │                │                       │ ────────────────────────>│
 │                │                       │ <────────────────────────│
 │                │                       │ 4.校验 status            │
 │                │                       │ 5.校验 lock_until        │
 │                │                       │ 6.BCrypt.checkpwd        │
 │                │                       │ 7.失败 +1 / 成功清零     │
 │                │                       │ 8.查询角色+数据范围       │
 │                │                       │ ────────────────────────>│
 │                │                       │ <────────────────────────│
 │                │                       │ 9.生成 JWT(8h)          │
 │                │                       │ 10.生成 refresh_token(7d)│
 │                │                       │ ────────────────────────>│ (Redis SET)
 │                │                       │ 11.记录审计日志           │
 │                │                       │ ────────────────────────>│
 │                │ <─────────────────────│                          │
 │                │ 200 OK                │                          │
 │                │ {accessToken,         │                          │
 │                │  refreshToken,        │                          │
 │                │  userInfo,            │                          │
 │                │  permissions[],       │                          │
 │                │  menus[]}             │                          │
 │                │                       │                          │
 │ 13.跳转 /dashboard                     │                          │
 │ <──────────────│                       │                          │
 │                │                       │                          │
 │ 14.访问 /basedata/currency             │                          │
 │ ──────────────>│                       │                          │
 │                │ 15.GET /basedata/     │                          │
 │                │   currency/page       │                          │
 │                │ Authorization:        │                          │
 │                │   Bearer <token>      │                          │
 │                │ ──────────────────────>│                          │
 │                │                       │ 16.JwtFilter 解析        │
 │                │                       │ 17.设置 SecurityContext  │
 │                │                       │ 18.@PreAuthorize 校验    │
 │                │                       │ 19.DataScopeInterceptor  │
 │                │                       │ 20.业务查询+返回          │
 │                │ <─────────────────────│                          │
```

### 7.2 前端存储策略

- **accessToken**:内存 + Pinia store(不持久化,XSS 风险)
- **refreshToken**:`httpOnly` cookie(7 天)或 sessionStorage(P1 实现)
- **userInfo / permissions / menus**:Pinia store + sessionStorage(刷新页面恢复)
- **路由跳转后立即校验 token**:Pinia 启动时从 sessionStorage 恢复 token 并调用 `/auth/me` 校验有效性

### 7.3 Token 刷新机制

```
[前端]                          [后端]
  │                                │
  │ 1.请求带过期 token             │
  │ ──────────────────────────────>│
  │                                │ 2.解析 JWT,发现已过期
  │ <──────────────────────────────│ 401 {needRefresh:true}
  │                                │
  │ 3.请求 /auth/refresh           │
  │  Cookie: refresh_token=xxx     │
  │ ──────────────────────────────>│
  │                                │ 4.校验 refresh_token
  │                                │ 5.查询 Redis 比对 hash
  │                                │ 6.删除旧 refresh_token
  │                                │ 7.生成新 access+refresh
  │                                │ 8.写入新 refresh_token
  │ <──────────────────────────────│ 200 {accessToken,refreshToken}
  │                                │
  │ 9.重发原请求                   │
  │ ──────────────────────────────>│
  │ <──────────────────────────────│ 200 OK
```

---

## 八、权限验证(后端)

### 8.1 三层防护

| 层级 | 实现 | 触发时机 | 失败响应 |
|------|------|----------|----------|
| L1 - JWT 认证 | `JwtAuthenticationFilter` | 每个请求 | 401 Unauthorized |
| L2 - 功能权限 | `@PreAuthorize` + `MethodInterceptor` | 业务方法调用 | 403 Forbidden |
| L3 - 数据权限 | `DataScopeInterceptor`(MyBatis) | SQL 执行 | 自动注入条件,业务无感 |

### 8.2 注解式权限校验

**单权限**:
```java
@PreAuthorize("hasAuthority('dealing.fx.create')")
@PostMapping("/fx-deals")
public Result<FxDealVO> create(@RequestBody FxDealDTO dto) { ... }
```

**多权限(任一)**:
```java
@PreAuthorize("hasAnyAuthority('dealing.fx.update', 'dealing.fx.rateFix')")
@PostMapping("/fx-deals/{id}/update")
public Result<FxDealVO> update(...) { ... }
```

**角色级**:
```java
@PreAuthorize("hasRole('SUPER_ADMIN')")
@PostMapping("/users/{id}/unlock")
public Result<Void> unlock(...) { ... }
```

### 8.3 拦截器实现

**`JwtAuthenticationFilter`**:
```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain) {
        String token = extractToken(req);
        if (token == null) {
            // 公开路径:/auth/login, /auth/refresh, /health
            if (isPublicPath(req)) { chain.doFilter(req, res); return; }
            throw new UnauthorizedException("未登录");
        }
        try {
            Claims claims = jwtUtil.parse(token);
            UserContext ctx = buildContext(claims);
            SecurityContextHolder.setContext(ctx);
            chain.doFilter(req, res);
        } catch (ExpiredJwtException e) {
            throw new UnauthorizedException("token 已过期");
        } catch (Exception e) {
            throw new UnauthorizedException("token 无效");
        }
    }
}
```

**`DataScopeInterceptor`**:
```java
@Intercepts(@Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}))
public class DataScopeInterceptor implements Interceptor {
    @Override
    public Object intercept(Invocation invocation) {
        MappedStatement ms = (MappedStatement) invocation.getArgs()[0];
        Object parameter = invocation.getArgs()[1];
        BoundSql boundSql = ms.getBoundSql(parameter);
        String originalSql = boundSql.getSql();
        
        UserContext ctx = SecurityContextHolder.getCurrentUser();
        if (ctx == null || ctx.isSuperAdmin() || ctx.getDataScope() == DataScope.ALL) {
            return invocation.proceed();
        }
        
        String scopedSql = injectDataScope(originalSql, ctx);
        // 重写 SQL 并执行
        ...
    }
}
```

### 8.4 SUPER_ADMIN 特殊通道

- `JwtAuthenticationFilter` 中若 `claims.roles` 包含 `SUPER_ADMIN`,设置 `isSuperAdmin=true`
- `DataScopeInterceptor` 优先检查该标志,跳过条件注入
- `@PreAuthorize` 注解默认需要 SUPER_ADMIN 时用 `hasRole('SUPER_ADMIN')`,与其他角色互斥

---

## 九、前端集成

### 9.1 目录结构新增

```
web/src/
├── api/
│   ├── auth/
│   │   ├── login.js          # login / logout / refresh / me
│   ├── rbac/
│   │   ├── user.js           # 用户 CRUD + 角色分配
│   │   ├── role.js           # 角色 CRUD + 权限分配
│   │   ├── permission.js     # 权限 CRUD
│   │   └── menu.js           # 菜单 CRUD + 树
├── stores/
│   ├── user.js               # Pinia: 当前用户 + token + permissions
│   └── permission.js         # Pinia: 路由 + 按钮权限
├── views/
│   ├── login/
│   │   └── Login.vue         # 登录页
│   └── system/
│       ├── UserList.vue      # 用户管理
│       ├── RoleList.vue      # 角色管理
│       ├── PermissionList.vue # 权限管理
│       ├── MenuList.vue      # 菜单管理
│       └── AuditLogList.vue  # 审计日志(P1)
├── router/
│   ├── index.js              # 改造为动态 router
│   └── permission.js         # 路由守卫
└── utils/
    ├── auth.js               # token 存储/读取
    ├── request.js            # axios 拦截器(401/403 处理)
    └── permission.js         # hasPerm / hasRole 工具函数
```

### 9.2 路由守卫(`router/permission.js`)

```javascript
import router from './index'
import { useUserStore } from '@/stores/user'
import { usePermissionStore } from '@/stores/permission'

const whiteList = ['/login', '/auth-redirect']

router.beforeEach(async (to, from, next) => {
    const userStore = useUserStore()
    const permStore = usePermissionStore()
    
    // 1. 已登录
    if (userStore.token) {
        if (to.path === '/login') {
            next('/dashboard')
        } else {
            // 2. 检查是否已加载权限
            if (!permStore.loaded) {
                try {
                    await userStore.fetchUserInfo()
                    await permStore.generateRoutes()
                    next({ ...to, replace: true })
                } catch (e) {
                    userStore.reset()
                    next('/login')
                }
            } else {
                next()
            }
        }
    } else {
        // 3. 未登录
        if (whiteList.includes(to.path)) {
            next()
        } else {
            next(`/login?redirect=${to.path}`)
        }
    }
})
```

### 9.3 按钮级权限(`utils/permission.js`)

```javascript
import { usePermissionStore } from '@/stores/permission'

export function hasPerm(permCode) {
    const store = usePermissionStore()
    if (store.isSuperAdmin) return true
    return store.permissions.includes(permCode)
}

export function hasAnyPerm(...permCodes) {
    return permCodes.some(code => hasPerm(code))
}

export function hasAllPerm(...permCodes) {
    return permCodes.every(code => hasPerm(code))
}

export function hasRole(roleCode) {
    const store = usePermissionStore()
    if (store.isSuperAdmin) return true
    return store.roles.includes(roleCode)
}
```

### 9.4 组件中使用

```vue
<template>
    <el-button v-if="hasPerm('dealing.acDeal.create')" type="primary" @click="onAdd">
        <el-icon><Plus /></el-icon>新增交易
    </el-button>
    
    <el-button v-if="hasPerm('dealing.acDeal.approve')" type="success" @click="onApprove">
        审批
    </el-button>
    
    <el-button v-if="hasPerm('dealing.acDeal.delete')" type="danger" @click="onDelete">
        删除
    </el-button>
</template>

<script setup>
import { hasPerm, hasAnyPerm } from '@/utils/permission'

defineExpose({ hasPerm })
</script>
```

### 9.5 动态菜单生成

```javascript
// stores/permission.js
export const usePermissionStore = defineStore('permission', {
    state: () => ({
        menus: [],
        permissions: [],
        roles: [],
        isSuperAdmin: false,
        loaded: false
    }),
    actions: {
        async generateRoutes() {
            const { data } = await getUserMenus()
            this.menus = data
            this.permissions = data.flatMap(m => m.permissions || [])
            
            // 添加动态路由
            const routes = transformMenuToRoutes(this.menus)
            routes.forEach(r => router.addRoute(r))
            
            this.loaded = true
        }
    }
})
```

### 9.6 axios 拦截器(`utils/request.js`)

```javascript
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import router from '@/router'

const service = axios.create({ baseURL: '/api/v1', timeout: 30000 })

service.interceptors.request.use(config => {
    const userStore = useUserStore()
    if (userStore.token) {
        config.headers['Authorization'] = `Bearer ${userStore.token}`
    }
    return config
})

service.interceptors.response.use(
    response => response.data,
    async error => {
        const { response } = error
        const userStore = useUserStore()
        
        if (response?.status === 401) {
            // 尝试刷新 token
            if (!error.config._retry) {
                error.config._retry = true
                try {
                    await userStore.refreshToken()
                    error.config.headers['Authorization'] = `Bearer ${userStore.token}`
                    return service(error.config)
                } catch (e) {
                    userStore.reset()
                    router.push('/login')
                }
            } else {
                userStore.reset()
                router.push('/login')
            }
        } else if (response?.status === 403) {
            ElMessage.error(response.data.message || '无权限')
        } else if (response?.status >= 500) {
            ElMessage.error('服务器异常')
        }
        return Promise.reject(error)
    }
)
```

---

## 十、验收标准

### 10.1 用户管理验收

| # | 验收项 | 验收条件 |
|---|--------|----------|
| AC-01 | 用户列表 | 分页查询、按 login_name/status 筛选、显示角色数量正确 |
| AC-02 | 新增用户 | login_name 重复返回 400;必填字段校验;成功创建并分配初始角色 |
| AC-03 | 更新用户 | 修改 display_name/email/phone 成功;不能改 login_name;乐观锁生效 |
| AC-04 | 删除用户 | 用户有角色绑定时返回 400;清除绑定后可删;软删除后 login_name 可复用 |
| AC-05 | 启用/停用 | 停用后用户登录返回 "账户已停用";启用后可恢复 |
| AC-06 | 重置密码 | 生成 12 位临时密码;must_change_password=true;用户登录强制改密 |
| AC-07 | 修改密码 | 旧密码错误返回 400;新密码不符合策略返回 400;成功后 password_updated_at 更新 |
| AC-08 | 解锁账户 | LOCKED 用户可被解锁;status 改为 ENABLED;failed_login_count 清零 |
| AC-09 | 当前用户信息 | 返回 user + roles + permissions + menus;与 JWT 一致 |

### 10.2 角色管理验收

| # | 验收项 | 验收条件 |
|---|--------|----------|
| AC-10 | 角色列表 | 分页查询、按 role_code/name 筛选 |
| AC-11 | 新增角色 | role_code 唯一;非空校验;成功创建 |
| AC-12 | 更新角色 | 不能改 role_code;description 可改 |
| AC-13 | 删除角色 | 内置角色(BUILT_IN)删除返回 400;有用户绑定时返回 400;无绑定时可删 |
| AC-14 | 启用/停用角色 | 停用后所有用户该角色失效(查询时不返回该角色相关菜单) |
| AC-15 | 分配权限 | 一次分配多个;重复分配返回 400(已存在) |
| AC-16 | 移除权限 | 成功移除;不影响其他角色 |
| AC-17 | 查询角色的权限 | 返回该角色所有权限码;按 category 分组 |

### 10.3 权限管理验收

| # | 验收项 | 验收条件 |
|---|--------|----------|
| AC-18 | 权限列表 | 按 category/action 筛选 |
| AC-19 | 新增权限 | resource_code + action 唯一;自动生成 permission_code |
| AC-20 | 删除权限 | 软删除;关联的角色权限绑定也失效 |
| AC-21 | 权限树 | 按 category 分组返回 |

### 10.4 菜单管理验收

| # | 验收项 | 验收条件 |
|---|--------|----------|
| AC-22 | 菜单树 | 按 parent_id 层级返回 |
| AC-23 | 当前用户菜单 | 仅返回用户有权限的菜单;按 sort_order 排序 |
| AC-24 | 新增菜单 | menu_code 唯一;parent_id 不能是自己 |
| AC-25 | 删除菜单 | 有子菜单时返回 400;无子菜单时可删 |
| AC-26 | 菜单排序 | 拖拽后 sort_order 更新;前端立即生效 |

### 10.5 登录认证验收

| # | 验收项 | 验收条件 |
|---|--------|----------|
| AC-27 | 登录成功 | 返回 accessToken + refreshToken + userInfo + permissions + menus |
| AC-28 | 登录失败(密码错) | 第 1-4 次返回"用户名或密码错误";第 5 次返回"账户已锁定 30 分钟" |
| AC-29 | 锁定期间登录 | 返回"账户已锁定,剩余 X 分钟";不计失败次数 |
| AC-30 | 30 分钟后登录 | 自动解锁(若到 lock_until 时间);正常校验 |
| AC-31 | 停用账户登录 | 返回"账户已停用,请联系管理员" |
| AC-32 | JWT 验证 | 有效 token 可访问受保护接口;无效/过期 token 返回 401 |
| AC-33 | Token 刷新 | 过期 access_token + 有效 refresh_token 可换新 token;refresh_token 轮换 |
| AC-34 | Refresh 过期 | 强制跳登录页 |
| AC-35 | 登出 | 清除客户端 token;服务端将 token 加入 Redis 黑名单(TTL=剩余有效期) |
| AC-36 | 登录限流 | 同 login_name 1 分钟内 10 次以上返回 429 |

### 10.6 数据权限验收

| # | 验收项 | 验收条件 |
|---|--------|----------|
| AC-37 | SUPER_ADMIN | 角色为 SUPER_ADMIN 的用户可访问所有数据,无任何过滤 |
| AC-38 | ALL 角色 | data_scope=ALL 的角色用户可访问所有数据 |
| AC-39 | ENTITY 角色 | 普通用户只能看到自己管理主体下的交易/账户 |
| AC-40 | 多角色合并 | 用户同时拥有 2 个 ENTITY 角色时,看到 2 个主体并集的数据 |
| AC-41 | SQL 拦截 | 无 data_scope 的接口不受影响(如登录);有 data_scope 的接口自动加过滤 |
| AC-42 | 跨主体写入 | 用户尝试访问其他主体的数据(如 URL 拼接 ID)返回 403 或 404 |

### 10.7 路由与按钮权限验收

| # | 验收项 | 验收条件 |
|---|--------|----------|
| AC-43 | 路由守卫 | 无 token 访问受保护路由跳 /login;有 token 但无权限跳 /403 |
| AC-44 | 动态菜单 | 用户登录后菜单按权限动态显示;无权限的菜单不出现 |
| AC-45 | 按钮隐藏 | 无权限的按钮不渲染(有 v-if 控制) |
| AC-46 | API 调用 | 前端绕过按钮直接调 API 返回 403(后端二次校验) |
| AC-47 | Token 失效 | 自动跳登录页;redirect 参数保留 |

### 10.8 审计日志验收 (P1)

| # | 验收项 | 验收条件 |
|---|--------|----------|
| AC-48 | 登录日志 | 登录成功/失败/登出都有日志 |
| AC-49 | 操作日志 | 所有 POST/PUT/DELETE 请求都有日志(含 user, IP, params) |
| AC-50 | 日志查询 | 按 user_id/log_type/operation_time 范围筛选;分页 |

### 10.9 集成验收

| # | 验收项 | 验收条件 |
|---|--------|----------|
| AC-51 | AC Deal 集成 | 录入交易时 created_by 来自 JWT;列表查询自动按 management_entity_id 过滤 |
| AC-52 | AT Deal 集成 | 同 AC |
| AC-53 | FX Deal 集成 | 同 AC |
| AC-54 | 审批集成 | 审批人 = 当前登录用户;无审批权限返回 403 |
| AC-55 | BaseData 集成 | 银行账户/对手方列表按 management_entity_id 过滤 |

---

## 十一、接口需求

### 11.1 认证模块(`/api/v1/auth`)

| # | 方法 | 路径 | 说明 | 权限 |
|---|------|------|------|------|
| 1 | POST | /auth/login | 登录 | 公开 |
| 2 | POST | /auth/logout | 登出 | 登录后 |
| 3 | POST | /auth/refresh | 刷新 token | 公开(用 refresh_token cookie) |
| 4 | GET | /auth/me | 当前用户信息 | 登录后 |
| 5 | POST | /auth/change-password | 修改自己的密码 | 登录后 |

**登录请求**:
```json
POST /api/v1/auth/login
{
    "loginName": "zhangsan",
    "password": "Pass@1234"
}
```

**登录响应**:
```json
{
    "code": 200,
    "data": {
        "accessToken": "eyJhbGciOiJIUzI1...",
        "refreshToken": "dGhpcyBpcyBhIHJlZnJlc2g...",
        "expiresIn": 28800,
        "userInfo": {
            "id": 10001,
            "loginName": "zhangsan",
            "displayName": "张三",
            "email": "zhangsan@example.com",
            "defaultManagementEntityId": 1,
            "language": "zh-CN",
            "timezone": "Asia/Shanghai"
        },
        "roles": ["TRADER", "REVIEWER"],
        "permissions": [
            "dealing.fx.view", "dealing.fx.create", ...
        ],
        "menus": [
            { "menuCode": "basedata", "menuName": "基础数据", "children": [...] }
        ],
        "dataScope": "ENTITY",
        "managementEntityIds": [1, 2, 3]
    }
}
```

### 11.2 用户管理(`/api/v1/users`)

| # | 方法 | 路径 | 说明 | 权限 |
|---|------|------|------|------|
| 6 | GET | /api/v1/users/page | 用户分页 | rbac.user.view |
| 7 | GET | /api/v1/users/{id} | 用户详情 | rbac.user.view |
| 8 | POST | /api/v1/users | 新增用户 | rbac.user.create |
| 9 | POST | /api/v1/users/update | 更新用户 | rbac.user.update |
| 10 | POST | /api/v1/users/delete/{id} | 删除用户 | rbac.user.delete |
| 11 | POST | /api/v1/users/{id}/toggle-status | 启用/停用 | rbac.user.update |
| 12 | POST | /api/v1/users/{id}/reset-password | 重置密码 | rbac.user.resetPassword |
| 13 | POST | /api/v1/users/{id}/unlock | 解锁账户 | rbac.user.update |
| 14 | GET | /api/v1/users/{id}/roles | 查询用户的角色 | rbac.user.view |
| 15 | POST | /api/v1/users/{id}/roles | 分配角色(多) | rbac.user.assignRole |
| 16 | POST | /api/v1/users/{id}/roles/{roleId}/remove | 移除用户的角色 | rbac.user.assignRole |
| 17 | POST | /api/v1/users/{id}/roles/{roleId}/update-scope | 更新数据范围 | rbac.user.assignRole |
| 18 | GET | /api/v1/users/{id}/management-entities | 查询用户可见管理主体 | rbac.user.view |

### 11.3 角色管理(`/api/v1/roles`)

| # | 方法 | 路径 | 说明 | 权限 |
|---|------|------|------|------|
| 19 | GET | /api/v1/roles/page | 角色分页 | rbac.role.view |
| 20 | GET | /api/v1/roles/{id} | 角色详情 | rbac.role.view |
| 21 | GET | /api/v1/roles/all | 全部角色(下拉用) | 登录后 |
| 22 | POST | /api/v1/roles | 新增角色 | rbac.role.create |
| 23 | POST | /api/v1/roles/update | 更新角色 | rbac.role.update |
| 24 | POST | /api/v1/roles/delete/{id} | 删除角色 | rbac.role.delete |
| 25 | POST | /api/v1/roles/{id}/toggle-status | 启用/停用 | rbac.role.update |
| 26 | GET | /api/v1/roles/{id}/permissions | 查询角色的权限 | rbac.role.view |
| 27 | POST | /api/v1/roles/{id}/permissions | 分配权限(多) | rbac.role.assignPermission |
| 28 | POST | /api/v1/roles/{id}/permissions/{permId}/remove | 移除权限 | rbac.role.assignPermission |
| 29 | GET | /api/v1/roles/{id}/users | 查询角色的用户 | rbac.role.view |

### 11.4 权限管理(`/api/v1/permissions`)

| # | 方法 | 路径 | 说明 | 权限 |
|---|------|------|------|------|
| 30 | GET | /api/v1/permissions/page | 权限分页 | rbac.permission.view |
| 31 | GET | /api/v1/permissions/all | 全部权限(下拉用) | 登录后 |
| 32 | GET | /api/v1/permissions/tree | 权限树(按 category) | rbac.permission.view |
| 33 | GET | /api/v1/permissions/{id} | 权限详情 | rbac.permission.view |
| 34 | POST | /api/v1/permissions | 新增权限 | rbac.permission.create |
| 35 | POST | /api/v1/permissions/update | 更新权限 | rbac.permission.update |
| 36 | POST | /api/v1/permissions/delete/{id} | 删除权限 | rbac.permission.delete |

### 11.5 菜单管理(`/api/v1/menus`)

| # | 方法 | 路径 | 说明 | 权限 |
|---|------|------|------|------|
| 37 | GET | /api/v1/menus/tree | 菜单树(全量) | rbac.menu.view |
| 38 | GET | /api/v1/menus/user | 当前用户菜单 | 登录后 |
| 39 | GET | /api/v1/menus/page | 菜单分页 | rbac.menu.view |
| 40 | GET | /api/v1/menus/{id} | 菜单详情 | rbac.menu.view |
| 41 | POST | /api/v1/menus | 新增菜单 | rbac.menu.create |
| 42 | POST | /api/v1/menus/update | 更新菜单 | rbac.menu.update |
| 43 | POST | /api/v1/menus/delete/{id} | 删除菜单 | rbac.menu.delete |
| 44 | POST | /api/v1/menus/sort | 菜单排序 | rbac.menu.update |
| 45 | POST | /api/v1/menus/sync | 同步菜单(P1) | rbac.menu.create |

### 11.6 审计日志(P1)(`/api/v1/audit-logs`)

| # | 方法 | 路径 | 说明 | 权限 |
|---|------|------|------|------|
| 46 | GET | /api/v1/audit-logs/page | 审计日志分页 | rbac.auditLog.view |
| 47 | GET | /api/v1/audit-logs/{id} | 审计日志详情 | rbac.auditLog.view |

**合计**:P0 共 45 个接口,P1 共 2 个接口,合计 47 个接口。

---

## 十二、与现有模块的集成

### 12.1 AC/AT/FX 交易模块(`dealing`)

**变更点**:
1. **当前 `created_by = 'system'`** → 改为从 `SecurityContextHolder.getCurrentUser().getLoginName()` 获取
2. **当前所有交易列表查询** → 增加 `DataScopeInterceptor` 自动注入 `management_entity_id IN (...)` 条件
3. **当前审批人** = 硬编码 "admin" → 改为当前登录用户
4. **审批接口** → 增加 `@PreAuthorize("hasAuthority('dealing.acDeal.approve')")`
5. **执行接口** → 增加 `@PreAuthorize("hasAuthority('dealing.acDeal.execute')")`

**兼容性**:
- 现有数据保留 `created_by = 'system'` 的旧记录(不修改)
- 旧记录对 SUPER_ADMIN 可见,普通用户不可见(因为 `management_entity_id` 为空)

**数据回填脚本**(可选 P1):
```sql
-- 将历史 'system' 记录的管理主体改为默认主体(假设 ID=1)
UPDATE tms_deals_t SET management_entity_id = 1 WHERE created_by = 'system' AND management_entity_id IS NULL;
```

### 12.2 基础数据模块(`basedata`)

**变更点**:
- `tms_management_entity_t` / `tms_counterparty_t` / `tms_bank_account_t` 等所有列表查询自动注入 `management_entity_id` 过滤
- `tms_trader_t`(交易员)新增可选字段:`user_id` (FK→`tms_users_t.id`),关联登录用户

### 12.3 审批模块(`approval`)

**变更点**:
- 审批人 = 当前登录用户(替代硬编码)
- 审批操作权限校验:`@PreAuthorize("hasAuthority('approval.task.approve')")`
- 审批历史表中 `approver_login_name` 来自 JWT

### 12.4 报表与驾驶舱(`report`/`cockpit`)

**变更点**:
- 报表查询自动按管理主体过滤(用户只能看到自己主体的报表数据)
- 驾驶舱 KPI 自动按管理主体聚合

### 12.5 common 模块自身

**当前状态**:`common` 已包含 `Result/BaseEntity/BaseCodeEntity/GlobalConstants/MybatisPlusConfig` 等基础类。

**新增内容**:
```
common/src/main/java/com/opentms/common/
├── security/                          # 本期新增
│   ├── annotation/
│   │   ├── RequirePerm.java           # 自定义权限注解
│   │   └── DataScope.java             # 自定义数据范围注解
│   ├── context/
│   │   ├── SecurityContextHolder.java # 当前用户上下文
│   │   └── UserContext.java           # 用户上下文 VO
│   ├── filter/
│   │   └── JwtAuthenticationFilter.java
│   ├── interceptor/
│   │   └── DataScopeInterceptor.java  # MyBatis 拦截器
│   ├── util/
│   │   ├── JwtUtil.java               # JWT 生成/解析
│   │   ├── PasswordUtil.java          # BCrypt 加密/校验
│   │   └── IdempotencyUtil.java
│   ├── exception/
│   │   ├── UnauthorizedException.java
│   │   ├── ForbiddenException.java
│   │   └── RateLimitException.java
│   └── constant/
│       └── SecurityConstants.java     # JWT/限流/路径白名单
└── aspect/
    ├── PermissionAspect.java          # @RequirePerm 切面
    └── DataScopeAspect.java           # @DataScope 切面
```

**业务模块需要依赖**:
```xml
<!-- basedata/dealing/fundplan 等模块的 pom.xml -->
<dependency>
    <groupId>com.opentms</groupId>
    <artifactId>opentms-common</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### 12.6 Vite 代理调整

新增 `/api/v1/auth`、`/api/v1/users`、`/api/v1/roles`、`/api/v1/permissions`、`/api/v1/menus`、`/api/v1/audit-logs` 路由,默认走 `common` 服务端口(新增 8080)。

```
web/vite.config.js:
{
  '/api/v1/auth': 'http://localhost:8080',
  '/api/v1/users': 'http://localhost:8080',
  '/api/v1/roles': 'http://localhost:8080',
  '/api/v1/permissions': 'http://localhost:8080',
  '/api/v1/menus': 'http://localhost:8080',
  '/api/v1/audit-logs': 'http://localhost:8080',
  '/api/v1/basedata': 'http://localhost:8081/opentms/basedata',
  '/api/v1/dealing': 'http://localhost:8082',
  ...
}
```

---

## 十三、本期不在范围

| 功能 | 状态 | 说明 |
|------|------|------|
| LDAP 集成 | P1 | 本期仅 JWT |
| SSO / OAuth2 / SAML | P1 | 本期仅账号密码 |
| 双因素认证 (2FA) | P1 | TOTP/SMS 验证码 |
| 密码自助找回 | P1 | 邮箱/短信找回 |
| 字段级权限 | P2 | 行级权限(按字段脱敏) |
| 临时授权 (delegate) | P2 | A 委托 B 处理 A 的审批 |
| IP 白名单 | P2 | 按 IP 限制登录 |
| 风险分级审批 | P2 | 基于金额自动选择审批人 |
| 角色继承 | P2 | 父子角色继承权限 |
| 部门管理 | P1 | `tms_departments_t`(本期预留 `dept_id` 字段) |
| 多语言 (i18n) | P1 | 本期仅 zh-CN |
| 操作审计 (P1) | P1 | 本期只记录登录日志 |
| 密码自助找回 | P1 | 邮箱/短信 |
| 移动端适配 | 不做 | 现有 web 端为 PC 优先 |
| 用户画像 / 偏好设置 | P2 | 主题/快捷键等 |

---

## 十四、实施阶段

### Phase 1 - MVP(预计 2 周)

**目标**:可登录、可按角色分配权限、可路由守卫

**任务清单**:
| # | 任务 | 角色 | 工时 |
|---|------|------|------|
| T1 | 建表 SQL + 初始化数据 SQL(SUPER_ADMIN + 默认角色 + 默认权限码 + 默认菜单) | DB | 0.5d |
| T2 | common 模块新增 security 包(JwtUtil/PasswordUtil/SecurityContextHolder/UserContext) | BE | 1d |
| T3 | common 模块新增 filter/interceptor/exception | BE | 1d |
| T4 | rbac 实体/Mapper/Service/Controller(用户/角色/权限/菜单 CRUD + 绑定) | BE | 3d |
| T5 | AuthController(login/logout/refresh/me) | BE | 1d |
| T6 | 全局异常处理 + 错误码 | BE | 0.5d |
| T7 | 前端 login 页面 + Pinia stores + axios 拦截器 | FE | 2d |
| T8 | 前端 5 个系统管理页(UserList/RoleList/PermissionList/MenuList) | FE | 3d |
| T9 | 前端 路由守卫 + 按钮级权限工具函数 | FE | 1d |
| T10 | basedata/dealing 模块集成 `created_by = currentUser`,加 `@PreAuthorize` | BE | 2d |
| T11 | DataScopeInterceptor 接入 dealing + basedata 查询 | BE | 1d |
| T12 | API 测试脚本(login/user/role/permission/menu) | QA | 1d |
| T13 | UI 测试脚本(登录/权限/路由) | QA | 1d |

**交付**:可登录系统 + 基础权限管理 + 现有业务模块集成

### Phase 2 - 数据权限 + 审计(预计 1.5 周)

**任务清单**:
| # | 任务 | 角色 | 工时 |
|---|------|------|------|
| T14 | DataScope 全量测试(所有列表查询自动过滤) | QA | 1d |
| T15 | 动态菜单(从后端拉取,前端动态生成 router) | FE | 1d |
| T16 | 审计日志表 + AOP 拦截所有写操作 | BE | 2d |
| T17 | 审计日志查询 UI + 后端 API | FE+BE | 1d |
| T18 | 密码策略配置化(application.yml + 实时校验) | BE | 0.5d |
| T19 | 跨模块集成测试(AC/AT/FX 创建/审批/查询) | QA | 2d |

**交付**:数据权限完整 + 审计可查 + 密码策略

### Phase 3 - LDAP/SSO/2FA(P1,预计 2 周)

**任务清单**:
| # | 任务 |
|---|------|
| T20 | LDAP 适配器(可配置 LDAP server URL) |
| T21 | OAuth2 / SAML 2.0 适配 |
| T22 | TOTP 2FA(Google Authenticator) |
| T23 | 短信/邮箱 2FA |
| T24 | 密码自助找回(邮箱验证) |

### Phase 4 - 高级功能(P2)

| # | 任务 |
|---|------|
| T25 | 字段级权限 |
| T26 | 临时授权(delegation) |
| T27 | IP 白名单 |
| T28 | 风险分级审批 |
| T29 | 角色继承(父子) |
| T30 | 多语言(i18n) |

---

## 附录 A - 默认数据初始化 SQL

```sql
-- 1. 插入超级管理员角色
INSERT INTO tms_roles_t (role_code, role_name, role_type, data_scope, built_in, sort_order, created_by)
VALUES ('SUPER_ADMIN', '超级管理员', 'BUILT_IN', 'ALL', true, 1, 'system');

-- 2. 插入内置角色
INSERT INTO tms_roles_t (role_code, role_name, role_type, data_scope, built_in, sort_order, created_by) VALUES
('SYS_ADMIN', '系统管理员', 'BUILT_IN', 'ALL', true, 2, 'system'),
('SEC_ADMIN', '安全管理员', 'BUILT_IN', 'ALL', true, 3, 'system'),
('ADMIN', '业务管理员', 'BUILT_IN', 'ENTITY', true, 4, 'system'),
('TRADER', '交易员', 'BUILT_IN', 'ENTITY', true, 5, 'system'),
('REVIEWER', '复核员', 'BUILT_IN', 'ENTITY', true, 6, 'system'),
('VIEWER', '观察员', 'BUILT_IN', 'ENTITY', true, 7, 'system'),
('AUDITOR', '审计员', 'BUILT_IN', 'ALL', true, 8, 'system');

-- 3. 插入默认 admin 用户(SUPER_ADMIN)
-- 密码 Admin@123 → BCrypt hash
INSERT INTO tms_users_t (login_name, display_name, password_hash, status, must_change_password, created_by)
VALUES ('admin', '系统管理员', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ENABLED', true, 'system');

-- 4. 绑定 admin → SUPER_ADMIN
INSERT INTO tms_user_roles_t (user_id, role_id, assigned_by, created_by)
SELECT u.id, r.id, 'system', 'system'
FROM tms_users_t u, tms_roles_t r
WHERE u.login_name = 'admin' AND r.role_code = 'SUPER_ADMIN';

-- 5. 插入预置权限码(详见 3.3.3 节,共 80+ 条)
-- 6. 插入预置菜单(详见 6.2 节,共 25+ 条)
-- 7. 为各内置角色分配默认权限(详见附录 B)
```

## 附录 B - 预置角色默认权限

```sql
-- TRADER 默认权限
INSERT INTO tms_role_permissions_t (role_id, permission_id, granted_by, created_by)
SELECT r.id, p.id, 'system', 'system'
FROM tms_roles_t r, tms_permissions_t p
WHERE r.role_code = 'TRADER'
  AND p.permission_code IN (
      'basedata.trader.view', 'basedata.currency.view', 'basedata.country.view',
      'basedata.bank.view', 'basedata.bankAccount.view', 'basedata.counterparty.view',
      'basedata.subsidiary.view', 'basedata.managementEntity.view', 'basedata.instrument.view',
      'basedata.currencyPair.view', 'basedata.holiday.view',
      'dealing.acDeal.view', 'dealing.acDeal.create', 'dealing.acDeal.update', 'dealing.acDeal.delete', 'dealing.acDeal.submit', 'dealing.acDeal.export',
      'dealing.atDeal.view', 'dealing.atDeal.create', 'dealing.atDeal.update', 'dealing.atDeal.delete', 'dealing.atDeal.submit', 'dealing.atDeal.export',
      'dealing.fxDeal.view', 'dealing.fxDeal.create', 'dealing.fxDeal.update', 'dealing.fxDeal.delete', 'dealing.fxDeal.rateFix',
      'dealing.action.view',
      'approval.task.view',
      'report.cockpit.view', 'report.fundReport.view'
  );

-- REVIEWER 默认权限
INSERT INTO tms_role_permissions_t (role_id, permission_id, granted_by, created_by)
SELECT r.id, p.id, 'system', 'system'
FROM tms_roles_t r, tms_permissions_t p
WHERE r.role_code = 'REVIEWER'
  AND p.permission_code IN (
      'basedata.trader.view', 'basedata.currency.view', 'basedata.bankAccount.view',
      'dealing.acDeal.view', 'dealing.acDeal.approve',
      'dealing.atDeal.view', 'dealing.atDeal.approve',
      'dealing.fxDeal.view',
      'approval.task.view', 'approval.task.approve',
      'report.cockpit.view', 'report.fundReport.view'
  );

-- ADMIN 默认权限
INSERT INTO tms_role_permissions_t (role_id, permission_id, granted_by, created_by)
SELECT r.id, p.id, 'system', 'system'
FROM tms_roles_t r, tms_permissions_t p
WHERE r.role_code = 'ADMIN'
  AND p.category IN ('basedata', 'dealing', 'approval', 'fundplan', 'valuation', 'risk', 'report');

-- VIEWER 默认权限
INSERT INTO tms_role_permissions_t (role_id, permission_id, granted_by, created_by)
SELECT r.id, p.id, 'system', 'system'
FROM tms_roles_t r, tms_permissions_t p
WHERE r.role_code = 'VIEWER'
  AND p.action = 'VIEW';

-- AUDITOR 默认权限
INSERT INTO tms_role_permissions_t (role_id, permission_id, granted_by, created_by)
SELECT r.id, p.id, 'system', 'system'
FROM tms_roles_t r, tms_permissions_t p
WHERE r.role_code = 'AUDITOR' AND p.permission_code = 'rbac.auditLog.view';

-- SYS_ADMIN 默认权限(所有 rbac.*)
INSERT INTO tms_role_permissions_t (role_id, permission_id, granted_by, created_by)
SELECT r.id, p.id, 'system', 'system'
FROM tms_roles_t r, tms_permissions_t p
WHERE r.role_code = 'SYS_ADMIN' AND p.category = 'rbac';

-- SEC_ADMIN 默认权限
INSERT INTO tms_role_permissions_t (role_id, permission_id, granted_by, created_by)
SELECT r.id, p.id, 'system', 'system'
FROM tms_roles_t r, tms_permissions_t p
WHERE r.role_code = 'SEC_ADMIN' AND p.permission_code = 'rbac.auditLog.view';
```

## 附录 C - 关键设计决策摘要

| # | 决策 | 理由 |
|---|------|------|
| 1 | 使用 JWT 而非 Session | 无状态,易扩展,支持多服务 |
| 2 | refresh_token 用 Redis 存储 + 轮换 | 支持服务端撤销,防 token 滥用 |
| 3 | 数据权限用 MyBatis 拦截器而非业务代码 | 业务无感,统一管控,防遗漏 |
| 4 | SUPER_ADMIN 完全绕过权限校验 | 系统最高权限,避免被锁 |
| 5 | 权限码命名规范: `模块.实体.操作` | 清晰、易检索、易分组 |
| 6 | 内置角色 + 自定义角色双类型 | 内置不可删,自定义可灵活配置 |
| 7 | 用户-角色绑定可指定 management_entity_id | 支持用户在不同主体有不同角色 |
| 8 | 多角色权限取并集 | 符合业务直觉(总权限增加) |
| 9 | BCrypt strength=10 | 性能与安全平衡 |
| 10 | 5 次失败/30 分钟锁定 | 与业界主流一致(FIS Quantum / Kyriba) |

---

## 版本历史

| 版本 | 日期 | 变更内容 |
|------|------|---------|
| v1.0 | 2026-07-05 | 初始版本(用户/角色/权限/菜单/绑定/登录/数据权限) |

---

*PM 产出 - Common v1.0*
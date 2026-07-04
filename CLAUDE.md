# CLAUDE.md

Open-TMS (Open Treasury Management System) — 企业级资金管理系统,对标 FIS Quantum / SAP TRM / Murex MX.3。

## Tech Stack
- **Backend**: Java 17, SpringBoot 3.2, Apache CXF 4.0.3 (JAX-RS), MyBatis Plus 3.5.5, Lombok
- **Frontend**: Vue 3, Element Plus, Vite (port 3000)
- **Database**: PostgreSQL 42.7.1 + Redis/Redisson — 库名 `opentms`, user/pwd = `opentms/opentms123`
- **Build**: Maven 多模块,单库多服务共享 PG,通过端口区分

## Active Modules (6)
| Module | Port | Status |
|--------|------|--------|
| basedata | 8081 | ✅ CXF (含银行账户+金融工具+币种对) |
| dealing | 8082 | ✅ Spring MVC (AC/AT/FX Deal) |
| fundplan | 8085 | 📋 |
| valuation | 8091 | 🔄 |
| var | 8095 | 🔄 |
| common | — | ✅ 公共基础 |

## Build & Run
```bash
mvn clean install                                        # 构建全部
mvn clean package -pl dealing -am                        # 构建 dealing + 依赖
java -jar dealing/target/dealing-1.0.0-SNAPSHOT.jar      # 启动 dealing (8082)
java -jar basedata/target/opentms-basedata-1.0.0-SNAPSHOT.jar  # 启动 basedata (8081)
cd web && npm install && npm run dev                     # 启动前端 (3000)
```

## Testing
```bash
python scripts/test/test_fx_deal_api.py                  # FX API 测试
python scripts/test/test_fx_deal_ui.py                   # FX UI 测试
python scripts/test/test_ac_deal_api.py                  # AC API 测试
python scripts/test/test_at_deal_api.py                  # AT API 测试
python scripts/db/db_tool.py -t                          # 列出所有表
python scripts/db/db_tool.py -d tms_country_t            # 描述表结构
python scripts/db/db_tool.py -q tms_country_t            # 查询数据
```

## Code Organization (per module)
```
src/main/java/com/opentms/{module}/
├── controller/   # REST 端点
├── service/      # 业务逻辑 (impl/)
├── mapper/       # MyBatis Plus
├── entity/       # 持久化实体 (继承 BaseEntity/BaseCodeEntity)
├── dto/          # 入参
├── vo/           # 出参
└── enums/        # 业务枚举
```
**分层红线**: dto/vo/entity 必须分文件; Controller 收 DTO, Service 返回 VO (`BeanUtils.copyProperties`)。

## Database Conventions
- 表名: `tms_{module}_t` (主表) / `_d` (字典) / `_log` (日志) / `_rel` (关联)
- 主键: `id BIGSERIAL PRIMARY KEY`
- 业务编码: `xxx_code VARCHAR(50) NOT NULL UNIQUE`
- 流水号: `xxx_no` (如 `FX20260704-0001`)
- **审计字段(全表必备)**: `created_by`, `created_at`, `updated_by`, `updated_at`, `version`(@Version), `deleted`(@TableLogic)
- 金额精度: 普通 DECIMAL(18,2), 汇率 DECIMAL(18,8), 资金交易 DECIMAL(38,18)

## REST API Patterns
```
GET    /api/v1/{resource}/page              # 分页
GET    /api/v1/{resource}/{id}              # 详情
POST   /api/v1/{resource}                   # 新增
POST   /api/v1/{resource}/update            # 更新
POST   /api/v1/{resource}/delete/{id}       # 删除
```
> ⚠️ update/delete 一律 POST,不要用 PUT/DELETE。

## Frontend Conventions
- API 函数: `web/src/api/{module}/{entity}.js`, 命名 `listX/getX/saveX/updateX/deleteX`
- 列表页: 搜索区 + 工具栏 + 表格 + 分页
- 状态标签: `<el-tag :type="getStatusType(row.status)">`
- 视图目录: `web/src/views/{module}/`
- Vite 代理: `/api/v1/dealing` → 8082, `/api/v1/*` → 8081/opentms/basedata

## Key Enums (GlobalConstants)
- **DealType**: AC / AT / FX
- **InstrumentType**: AC/AT/FX/DEPOSIT/LOAN/IR/EQ/BOND/SWAP/OPTION/FORWARD/OTHER
- **DealStatus**: New → Submitted → Approved → Settled / Rejected / Canceled
- **ActionType**: CREATE/UPDATE/DELETE/SUBMIT/APPROVE/REJECT/EXECUTE

## Java Conventions
```java
@Transactional(rollbackFor = Exception.class)   // 写操作
@Transactional(readOnly = true)                 // 读操作
```
- 业务异常: `throw new BusinessException("message")`
- 写操作必须幂等 (`X-Idempotency-Key` header)

## Git Commit
```
<type>(<scope>): <subject>
类型: feat/fix/docs/style/refactor/test/chore
```

## Vite Proxy Map
basedata 路径 → `http://localhost:8081/opentms/basedata`, dealing → `http://localhost:8082`

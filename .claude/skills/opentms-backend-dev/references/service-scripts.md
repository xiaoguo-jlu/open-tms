# Service Management Scripts

> 已从 SKILL.md 附录D 移出。此处保留服务管理脚本的使用说明。

## 后端服务管理

**脚本位置**: `scripts/test/start_test.py`

**功能**:
- 启动/停止/重启后端 Spring Boot 服务
- 自动构建（如 JAR 不存在）
- 检查后端运行状态

**使用方法**:
```bash
# 全部测试（含启动服务）
python scripts/test/test_all.py

# 启动特定模块
java -jar basedata/target/opentms-basedata-1.0.0-SNAPSHOT.jar

# 自定义端口
java -jar basedata/target/opentms-basedata-1.0.0-SNAPSHOT.jar --server.port=8081
```

**依赖**:
- Java 17+
- Maven 已安装
- PostgreSQL 已运行 (localhost:5432, opentms/opentms123)

**模块端口映射**:
| 模块 | 端口 | 状态 |
|------|------|------|
| basedata | 8081 | ✅ |
| dealing | 8082 | ✅ |
| settlement | 8087 | 🔄 |
| fundplan | 8085 | 📋 |
| cashpool | 8086 | 📋 |
| fx | 8089 | 📋 |
| irs | 8090 | 📋 |
| valuation | 8091 | 🔄 |
| exposure | 8092 | 📋 |
| hedge | 8093 | 📋 |
| impairment | 8094 | 📋 |
| var | 8095 | 🔄 |
| cockpit | 8096 | 📋 |
| report | 8097 | 📋 |

启动后访问: `http://localhost:8081` 等对应端口

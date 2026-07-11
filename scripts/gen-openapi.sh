#!/usr/bin/env bash
# Open-TMS OpenAPI 自动生成脚本
# -----------------------------------------------------------------------------
# 用途:
#   1. 等待基于数据 (8081) 与交易 (8082) 服务健康
#   2. 分别拉取:
#      - 基于数据 → /api/v1/openapi/cxf-spec      (基于自写 CXF 扫描器)
#      - 交易     → /v3/api-docs                   (基于 SpringDoc 自动扫描)
#   3. 合并到 docs/api/openapi.json(顶层用 servers[] 区分模块)
#   4. 记录生成时间戳与各模块路径数
#
# 前置:
#   - basedata 8081 端口健康
#   - dealing  8082 端口健康
#
# 用法:
#   bash scripts/gen-openapi.sh
#
# 环境变量覆盖(可选):
#   BASEDATA_URL=http://localhost:8081
#   DEALING_URL=http://localhost:8082
#   OUTPUT=docs/api/openapi.json
# -----------------------------------------------------------------------------

set -euo pipefail

BASEDATA_URL="${BASEDATA_URL:-http://localhost:8081}"
DEALING_URL="${DEALING_URL:-http://localhost:8082}"
OUTPUT="${OUTPUT:-docs/api/openapi.json}"

GREEN='\033[0;32m'
YELLOW='\033[0;33m'
RED='\033[0;31m'
NC='\033[0m'

log()  { echo -e "${GREEN}[$(date +%H:%M:%S)]${NC} $*"; }
warn() { echo -e "${YELLOW}[$(date +%H:%M:%S)]${NC} $*"; }
err()  { echo -e "${RED}[$(date +%H:%M:%S)]${NC} $*" >&2; }

# -------- 健康检查 --------
wait_for() {
    local url=$1
    local max=${2:-30}
    log "等待服务健康: $url"
    for i in $(seq 1 "$max"); do
        code=$(curl -s -o /dev/null -w "%{http_code}" "$url" 2>/dev/null || echo 000)
        # 200 / 404 / 401 都算可用(后端可能未挂 actuator,但只要 TCP 通)
        if [[ "$code" =~ ^(200|401|404|405)$ ]]; then
            log "  ✓ 就绪 (HTTP $code)"
            return 0
        fi
        sleep 1
    done
    err "  ✗ 超时未就绪"
    return 1
}

wait_for "$BASEDATA_URL/actuator/health"
# dealing 模块没有 actuator,直接探 Spring MVC 一个已知端点
wait_for "$DEALING_URL/v3/api-docs"

# -------- 拉取 OpenAPI --------
log "拉取 basedata OpenAPI (CXF 自定义扫描)…"
BASEDATA_SPEC=$(curl -fsS "$BASEDATA_URL/api/v1/openapi/cxf-spec")
BASEDATA_COUNT=$(echo "$BASEDATA_SPEC" | python -c "import sys,json; print(len(json.load(sys.stdin).get('paths',{})))")
log "  ✓ basedata paths: $BASEDATA_COUNT"

log "拉取 dealing OpenAPI (SpringDoc 自动扫描)…"
DEALING_SPEC=$(curl -fsS "$DEALING_URL/v3/api-docs")
DEALING_COUNT=$(echo "$DEALING_SPEC" | python -c "import sys,json; print(len(json.load(sys.stdin).get('paths',{})))")
log "  ✓ dealing paths: $DEALING_COUNT"

# -------- 合并 --------
mkdir -p "$(dirname "$OUTPUT")"
log "合并到 $OUTPUT"

python - <<PY
import json, sys, datetime

basedata = json.loads(r'''$BASEDATA_SPEC''')
dealing = json.loads(r'''$DEALING_SPEC''')

# 用顶层 servers[] 区分模块
out = {
    "openapi": "3.0.1",
    "info": {
        "title": "Open-TMS Combined API",
        "description": "基于数据 (CXF) + 交易 (Spring MVC) — 自动合并",
        "version": "1.0.0",
        "x-generated-at": datetime.datetime.utcnow().isoformat() + "Z",
        "x-modules": [
            {"module": "basedata", "framework": "cxf", "paths": $BASEDATA_COUNT, "server": "$BASEDATA_URL/opentms/basedata"},
            {"module": "dealing", "framework": "spring-mvc", "paths": $DEALING_COUNT, "server": "$DEALING_URL"}
        ]
    },
    "servers": [
        {"url": "$BASEDATA_URL/opentms/basedata", "description": "基于数据 (Apache CXF)"},
        {"url": "$DEALING_URL", "description": "交易 (Spring MVC)"}
    ],
    "paths": {},
    "components": {"schemas": {}}
}

# 基于数据 paths + schemas
for path, ops in basedata.get("paths", {}).items():
    out["paths"][path] = ops
for name, schema in (basedata.get("components") or {}).get("schemas", {}).items():
    out["components"]["schemas"][name] = schema

# 交易 paths + schemas(冲突时 dealing 优先)
for path, ops in dealing.get("paths", {}).items():
    out["paths"][path] = ops
for name, schema in (dealing.get("components") or {}).get("schemas", {}).items():
    out["components"]["schemas"][name] = schema

with open("$OUTPUT", "w", encoding="utf-8") as f:
    json.dump(out, f, ensure_ascii=False, indent=2)

print(f"merged {len(out['paths'])} paths into $OUTPUT")
PY

log "✓ 完成。可在 Swagger UI 中通过 /v3/api-docs 或 /api/v1/openapi/cxf-spec 直接加载。"
log "  合并产物: $OUTPUT"
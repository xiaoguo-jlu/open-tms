#!/bin/bash
# Open-TMS 3 服务一键启动/重启脚本
# 用法: bash scripts/restart-all.sh [start|stop|status|restart]

set -e

LOG_DIR="/tmp"
LOG_BD="${LOG_DIR}/basedata.log"
LOG_DL="${LOG_DIR}/dealing.log"
LOG_VITE="${LOG_DIR}/vite.log"

start_one() {
    local name=$1
    local dir=$2
    local jar=$3
    local log=$4
    if [ -f "$log" ] && pgrep -f "$jar" > /dev/null; then
        echo "[OK] $name already running"
        return
    fi
    echo "[START] $name..."
    cd "$dir" && nohup java -jar "$jar" > "$log" 2>&1 &
    disown
    cd - > /dev/null
}

start_vite() {
    if pgrep -f "vite" > /dev/null; then
        echo "[OK] vite already running"
        return
    fi
    echo "[START] vite..."
    cd "F:/code/opencode/opentrm/web" && nohup npm run dev > "$LOG_VITE" 2>&1 &
    disown
    cd - > /dev/null
}

stop_one() {
    local name=$1
    local pattern=$2
    pkill -f "$pattern" 2>/dev/null && echo "[STOP] $name killed" || echo "[OK] $name not running"
}

wait_ready() {
    local url=$1
    local name=$2
    for i in $(seq 1 30); do
        code=$(curl -s -o /dev/null -w "%{http_code}" "$url" 2>/dev/null)
        if [ "$code" = "200" ]; then
            echo "[READY] $name t=${i}s"
            return
        fi
        sleep 1
    done
    echo "[TIMEOUT] $name"
}

cmd="${1:-status}"
case "$cmd" in
    start)
        start_one "basedata" "F:/code/opencode/opentrm/basedata" "opentms-basedata-1.0.0-SNAPSHOT.jar" "$LOG_BD"
        start_one "dealing" "F:/code/opencode/opentrm/dealing" "dealing-1.0.0-SNAPSHOT.jar" "$LOG_DL"
        start_vite
        sleep 1
        wait_ready "http://localhost:8081/actuator/health" "basedata 8081"
        wait_ready "http://localhost:8082/v3/api-docs" "dealing 8082"
        wait_ready "http://localhost:3000/" "vite 3000"
        ;;
    stop)
        stop_one "basedata" "opentms-basedata"
        stop_one "dealing" "dealing-1.0.0"
        stop_one "vite" "vite"
        ;;
    status)
        echo "=== Service Status ==="
        for url in "8081:8081/actuator/health:basedata" "8082:8082/v3/api-docs:dealing" "3000:3000/:vite"; do
            port=$(echo "$url" | cut -d: -f1)
            path=$(echo "$url" | cut -d: -f2)
            name=$(echo "$url" | cut -d: -f3)
            code=$(curl -s -o /dev/null -w "%{http_code}" "http://localhost:${port}/${path}" 2>/dev/null)
            echo "  ${name} (:${port}): ${code}"
        done
        ;;
    restart)
        bash "$0" stop
        sleep 2
        bash "$0" start
        ;;
    *)
        echo "Usage: $0 {start|stop|status|restart}"
        ;;
esac

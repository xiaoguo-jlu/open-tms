#!/bin/bash
# Open-TMS 3 服务一键管理(Windows 兼容,PowerShell Start-Process)
# 用法: bash scripts/restart-all.sh {start|stop|status|restart}

BD_JAR="F:\\code\\opencode\\opentrm\\basedata\\target\\opentms-basedata-1.0.0-SNAPSHOT.jar"
DL_JAR="F:\\code\\opencode\\opentrm\\dealing\\target\\dealing-1.0.0-SNAPSHOT.jar"
VITE_DIR="F:\\code\\opencode\\opentrm\\web"
LOG_BD="F:\\code\\opencode\\opentrm\\tmp\\bd.log"
LOG_DL="F:\\code\\opencode\\opentrm\\tmp\\dl.log"
LOG_VITE="F:\\code\\opencode\\opentrm\\tmp\\vite.log"
mkdir -p tmp 2>/dev/null

is_up() {
    local port=$1 path=$2
    local code=$(curl -s -o /dev/null -w "%{http_code}" --max-time 2 "http://localhost:${port}${path}" 2>/dev/null)
    [ "$code" = "200" ]
}

start_java() {
    local jar=$1 log=$2
    powershell -Command "Start-Process -FilePath 'java' -ArgumentList '-jar','${jar}' -RedirectStandardOutput '${log}' -RedirectStandardError '${log}' -WindowStyle Hidden" 2>/dev/null
}

start_vite() {
    powershell -Command "Start-Process -FilePath 'npm.cmd' -ArgumentList 'run','dev' -WorkingDirectory '${VITE_DIR}' -RedirectStandardOutput '${LOG_VITE}' -RedirectStandardError '${LOG_VITE}' -WindowStyle Hidden" 2>/dev/null
}

stop_one() {
    local name=$1 pattern=$2
    powershell -Command "Get-Process -Name '${pattern}' -ErrorAction SilentlyContinue | Stop-Process -Force" 2>/dev/null
    echo "[STOP] ${name} killed"
}

wait_ready() {
    local url=$1 name=$2
    for i in $(seq 1 30); do
        code=$(curl -s -o /dev/null -w "%{http_code}" --max-time 2 "$url" 2>/dev/null)
        if [ "$code" = "200" ]; then echo "[READY] ${name} t=${i}s"; return; fi
        sleep 1
    done
    echo "[TIMEOUT] ${name}"
}

cmd="${1:-status}"
case "$cmd" in
    start)
        if ! is_up 8081 /actuator/health; then
            echo "[START] basedata 8081"
            start_java "$BD_JAR" "$LOG_BD"
        else echo "[OK] basedata 8081"; fi
        if ! is_up 8082 /v3/api-docs; then
            echo "[START] dealing 8082"
            start_java "$DL_JAR" "$LOG_DL"
        else echo "[OK] dealing 8082"; fi
        if ! is_up 3000 /; then
            echo "[START] vite 3000"
            start_vite
        else echo "[OK] vite 3000"; fi
        sleep 2
        wait_ready "http://localhost:8081/actuator/health" "basedata 8081"
        wait_ready "http://localhost:8082/v3/api-docs" "dealing 8082"
        wait_ready "http://localhost:3000/" "vite 3000"
        ;;
    stop)
        stop_one "basedata/dealing" "java"
        stop_one "vite" "node"
        ;;
    status)
        for s in "8081:/actuator/health:basedata" "8082:/v3/api-docs:dealing" "3000:/:vite"; do
            port=$(echo $s | cut -d: -f1)
            path=$(echo $s | cut -d: -f2)
            name=$(echo $s | cut -d: -f3)
            code=$(curl -s -o /dev/null -w "%{http_code}" --max-time 2 "http://localhost:${port}/${path}" 2>/dev/null)
            echo "  ${name} (:${port}): HTTP ${code}"
        done
        ;;
    restart)
        bash "$0" stop
        sleep 3
        bash "$0" start
        ;;
    *)
        echo "用法: $0 {start|stop|status|restart}"
        ;;
esac

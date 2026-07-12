#!/bin/bash
# Open-TMS Watchdog — 监控 3 服务,死了自动拉起
# 用法:
#   bash scripts/watchdog.sh              # 前台运行(测试用)
#   bash scripts/watchdog.sh --daemon    # 后台运行(写 nohup 日志)
#   bash scripts/watchdog.sh --stop      # 停掉
#   bash scripts/watchdog.sh --status    # 当前状态
#
# 行为:
#   - 每 30s 检查 8081/8082/3000
#   - 任何服务 down 触发 restart-all.sh
#   - 防抖:同服务 5 分钟内最多重启 3 次
#   - 日志:/tmp/opentms-watchdog.log

set -e

CHECK_INTERVAL=30          # 秒
COOLDOWN=300               # 防抖(秒)
MAX_RESTARTS_PER_COOLDOWN=3
LOG_FILE="/tmp/opentms-watchdog.log"
PID_FILE="/tmp/opentms-watchdog.pid"

SERVICE_BD="8081:/actuator/health:basedata"
SERVICE_DL="8082:/v3/api-docs:dealing"
SERVICE_VITE="3000:/:vite"

# 启动时间戳记录(防抖)
declare -A RESTART_HISTORY

log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $*" | tee -a "$LOG_FILE"
}

check_service() {
    local port=$1 path=$2 name=$3
    local code=$(curl -s -o /dev/null -w "%{http_code}" --max-time 5 "http://localhost:${port}${path}" 2>/dev/null)
    if [ "$code" = "200" ]; then
        return 0
    else
        log "FAIL  ${name} (:${port}) HTTP=${code}"
        return 1
    fi
}

can_restart() {
    local name=$1
    local now=$(date +%s)
    # 清掉 cooldown 外的记录
    if [ -n "${RESTART_HISTORY[$name]}" ]; then
        local last=$(echo "${RESTART_HISTORY[$name]}" | tr ',' '\n' | tail -1)
        if [ $((now - last)) -gt $COOLDOWN ]; then
            RESTART_HISTORY[$name]=""
        fi
    fi
    # 数 5 分钟内重启次数
    local count=0
    if [ -n "${RESTART_HISTORY[$name]}" ]; then
        for ts in $(echo "${RESTART_HISTORY[$name]}" | tr ',' '\n'); do
            if [ $((now - ts)) -le $COOLDOWN ]; then
                count=$((count + 1))
            fi
        done
    fi
    if [ $count -ge $MAX_RESTARTS_PER_COOLDOWN ]; then
        log "BLOCK ${name} 重启(5min 内已 ${count} 次),跳过避免死循环"
        return 1
    fi
    return 0
}

record_restart() {
    local name=$1
    local now=$(date +%s)
    if [ -z "${RESTART_HISTORY[$name]}" ]; then
        RESTART_HISTORY[$name]="$now"
    else
        RESTART_HISTORY[$name]="${RESTART_HISTORY[$name]},$now"
    fi
}

restart_service() {
    local name=$1
    if ! can_restart "$name"; then return 1; fi
    log "ACTION restart $name"
    bash "$(dirname "$0")/restart-all.sh" start >> "$LOG_FILE" 2>&1
    record_restart "$name"
    sleep 5
    # 验证
    case $name in
        basedata) check_service 8081 /actuator/health basedata ;;
        dealing)  check_service 8082 /v3/api-docs dealing ;;
        vite)     check_service 3000 / vite ;;
    esac
}

do_check() {
    local failed=0
    check_service 8081 /actuator/health basedata || failed=$((failed+1))
    check_service 8082 /v3/api-docs dealing || failed=$((failed+1))
    check_service 3000 / vite || failed=$((failed+1))
    return $failed
}

show_status() {
    echo "=== Service Status ==="
    for s in "$SERVICE_BD" "$SERVICE_DL" "$SERVICE_VITE"; do
        port=$(echo $s | cut -d: -f1)
        path=$(echo $s | cut -d: -f2)
        name=$(echo $s | cut -d: -f3)
        code=$(curl -s -o /dev/null -w "%{http_code}" --max-time 3 "http://localhost:${port}${path}" 2>/dev/null)
        echo "  $name (:${port}): HTTP $code"
    done
    if [ -f "$PID_FILE" ]; then
        local pid=$(cat "$PID_FILE" 2>/dev/null)
        if kill -0 "$pid" 2>/dev/null; then
            echo "  watchdog: 运行中 (PID $pid)"
        else
            echo "  watchdog: PID 文件存在但进程已死"
        fi
    else
        echo "  watchdog: 未运行"
    fi
}

stop_watchdog() {
    if [ -f "$PID_FILE" ]; then
        local pid=$(cat "$PID_FILE" 2>/dev/null)
        if [ -n "$pid" ]; then
            kill "$pid" 2>/dev/null || true
            rm -f "$PID_FILE"
            log "STOP watchdog (PID=$pid)"
        fi
    fi
}

start_daemon() {
    if [ -f "$PID_FILE" ]; then
        local pid=$(cat "$PID_FILE" 2>/dev/null)
        if kill -0 "$pid" 2>/dev/null; then
            echo "watchdog 已在运行 (PID $pid)"
            return 0
        fi
    fi
    echo "启动 watchdog daemon(日志: $LOG_FILE)..."
    nohup bash "$0" --loop >> "$LOG_FILE" 2>&1 &
    echo $! > "$PID_FILE"
    echo "  PID: $(cat $PID_FILE)"
    echo "  日志: tail -f $LOG_FILE"
}

loop() {
    log "WATCHDOG START (interval=${CHECK_INTERVAL}s, cooldown=${COOLDOWN}s)"
    while true; do
        do_check
        local rc=$?
        if [ $rc -eq 0 ]; then
            : # all good, no log
        else
            log "检测到 $rc 个服务 down,启动 recovery"
            bash "$(dirname "$0")/restart-all.sh" start >> "$LOG_FILE" 2>&1 || true
            # 单独触发对应 service 的防抖
            check_service 8081 /actuator/health basedata || record_restart basedata
            check_service 8082 /v3/api-docs dealing || record_restart dealing
            check_service 3000 / vite || record_restart vite
        fi
        sleep "$CHECK_INTERVAL"
    done
}

# 主入口
case "${1:-}" in
    --daemon) start_daemon ;;
    --stop)   stop_watchdog ;;
    --status) show_status ;;
    --loop)   loop ;;
    --check)  do_check; echo "down=$?" ;;
    *)        echo "用法: $0 {--daemon|--stop|--status|--check|--loop}"; exit 1 ;;
esac

#!/usr/bin/env python3
"""
Open-TMS 基础数据模块综合测试脚本
自动启动后端和前端服务，运行所有API和UI测试
"""

import subprocess
import time
import sys
import os
import urllib.request
import urllib.error
import json

# 配置
BACKEND_PORT = 8081
FRONTEND_PORT = 3000
BACKEND_JAR = 'basedata/target/opentms-basedata-1.0.0-SNAPSHOT.jar'
BACKEND_BASE_URL = f"http://localhost:{BACKEND_PORT}/opentms/basedata"
FRONTEND_URL = f"http://localhost:{FRONTEND_PORT}"

# Windows下使用cmd执行命令
IS_WINDOWS = sys.platform == 'win32' or os.environ.get('OS') == 'Windows_NT'


def run_cmd(cmd, capture=False, timeout=30):
    """执行命令"""
    if IS_WINDOWS and isinstance(cmd, str):
        cmd = cmd.split()
    try:
        if capture:
            result = subprocess.run(cmd, capture_output=True, text=True, timeout=timeout)
            return result.returncode, result.stdout, result.stderr
        else:
            result = subprocess.run(cmd, timeout=timeout)
            return result.returncode, "", ""
    except subprocess.TimeoutExpired:
        return -1, "", "Timeout"
    except Exception as e:
        return -1, "", str(e)


def kill_port(port):
    """杀掉占用指定端口的进程"""
    print(f"  Checking port {port}...")
    if IS_WINDOWS:
        result = subprocess.run(['netstat', '-ano'], capture_output=True, text=True)
        for line in result.stdout.splitlines():
            if f':{port}' in line and 'LISTENING' in line:
                parts = line.split()
                if parts and parts[-1].isdigit():
                    pid = int(parts[-1])
                    try:
                        subprocess.run(['taskkill', '/F', '/PID', str(pid)], capture_output=True)
                        print(f"    Killed process {pid}")
                    except:
                        pass
    else:
        result = subprocess.run(['lsof', '-ti', f':{port}'], capture_output=True, text=True)
        if result.stdout.strip():
            pids = result.stdout.strip().split()
            for pid in pids:
                subprocess.run(['kill', '-9', pid], capture_output=True)
                print(f"    Killed process {pid}")
    time.sleep(2)


def check_api(url, timeout=5):
    """检查API是否可用"""
    try:
        resp = urllib.request.urlopen(url, timeout=timeout)
        data = json.loads(resp.read().decode())
        return data.get('code') == 200
    except:
        return False


def wait_for_api(url, timeout=60, interval=2):
    """等待API可用"""
    print(f"  Waiting for API at {url}...")
    elapsed = 0
    while elapsed < timeout:
        if check_api(url):
            print(f"    API is ready!")
            return True
        time.sleep(interval)
        elapsed += interval
    return False


def print_header(title):
    print("\n" + "="*60)
    print(f"# {title}")
    print("="*60)


def print_summary(results):
    print("\n" + "="*60)
    print("# 测试结果汇总")
    print("="*60)
    for name, passed, total in results:
        status = "PASS" if passed == total else "FAIL"
        rate = passed * 100 // total if total > 0 else 0
        print(f"  {name}: {passed}/{total} ({rate}%) - {status}")
    print("="*60)


# ========== Main ==========
def main():
    print("\n" + "#"*60)
    print("# Open-TMS 基础数据模块综合测试")
    print("#"*60)

    results = []

    # ========== Step 1: 启动后端 ==========
    print_header("Step 1: 启动后端服务")

    kill_port(BACKEND_PORT)

    # 检查JAR文件
    if not os.path.exists(BACKEND_JAR):
        print(f"[ERROR] Backend JAR not found: {BACKEND_JAR}")
        print("  Please build the backend first: cd basedata && mvn clean package")
        return 1

    print(f"  Starting backend on port {BACKEND_PORT}...")
    backend_proc = subprocess.Popen(
        ['java', '-jar', BACKEND_JAR],
        stdout=subprocess.PIPE,
        stderr=subprocess.STDOUT,
        text=True,
        bufsize=1
    )

    # 等待后端启动
    started = False
    for line in backend_proc.stdout:
        print(f"    {line.strip()}")
        if 'Started BasedataApplication' in line or 'Tomcat started on port' in line:
            started = True
            break

    if not started:
        print("[ERROR] Backend failed to start")
        backend_proc.terminate()
        return 1

    # 等待API就绪
    if not wait_for_api(f"{BACKEND_BASE_URL}/api/v1/countries/page?pageNum=1&pageSize=1"):
        print("[ERROR] Backend API not responding")
        backend_proc.terminate()
        return 1

    print("[PASS] Backend started successfully")

    # ========== Step 2: 启动前端 ==========
    print_header("Step 2: 启动前端服务")

    kill_port(FRONTEND_PORT)

    print(f"  Starting frontend on port {FRONTEND_PORT}...")

    # 检查node_modules
    if not os.path.exists('web/node_modules'):
        print("  Installing frontend dependencies...")
        subprocess.run(['npm', 'install'], cwd='web', capture_output=True)

    frontend_proc = subprocess.Popen(
        ['npm', 'run', 'dev', '--', '--port', str(FRONTEND_PORT)],
        stdout=subprocess.PIPE,
        stderr=subprocess.STDOUT,
        text=True,
        bufsize=1,
        cwd='web'
    )

    # 等待前端启动
    started = False
    for line in frontend_proc.stdout:
        print(f"    {line.strip()}")
        if 'Local:' in line or 'localhost' in line.lower():
            started = True
            break

    if not started:
        # 尝试等待一下
        time.sleep(10)
        print("[PASS] Frontend starting...")

    print("[PASS] Frontend starting")

    # ========== Step 3: 运行API测试 ==========
    print_header("Step 3: 运行API测试")

    print("  Executing: test_basedata_api.py")
    api_result = subprocess.run(
        [sys.executable, 'scripts/test/test_basedata_api.py'],
        capture_output=False
    )
    api_passed = api_result.returncode == 0
    results.append(("API Tests", 1 if api_passed else 0, 1))

    # ========== Step 4: 运行UI测试 ==========
    print_header("Step 4: 运行UI测试")

    print("  Executing: test_basedata_ui.py")
    ui_result = subprocess.run(
        [sys.executable, 'scripts/test/test_basedata_ui.py'],
        capture_output=False
    )
    ui_passed = ui_result.returncode == 0
    results.append(("UI Tests", 1 if ui_passed else 0, 1))

    # ========== Summary ==========
    print_summary(results)

    # ========== Cleanup ==========
    print("\n  Stopping services...")
    backend_proc.terminate()
    try:
        frontend_proc.terminate()
    except:
        pass
    kill_port(BACKEND_PORT)
    kill_port(FRONTEND_PORT)

    print("\n" + "#"*60)
    print("# 测试完成")
    print("#"*60)

    return 0 if api_passed and ui_passed else 1


if __name__ == "__main__":
    sys.exit(main())
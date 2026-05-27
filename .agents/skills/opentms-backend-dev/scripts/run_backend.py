#!/usr/bin/env python3
"""
Open-TMS Backend Service Manager
Manages the lifecycle of the Open-TMS Spring Boot backend application.

Usage:
    python run_backend.py start     # Start the backend server
    python run_backend.py stop     # Stop the backend server
    python run_backend.py restart   # Restart the backend server
    python run_backend.py status    # Check if backend is running

Requirements:
    - Java 17+ must be installed
    - Maven project must be built
    - Backend module at basedata/

The backend service runs on port 8081 by default.

Module structure:
    basedata/       -> port 8081 (main REST API)
    dealing/        -> port 8082
    bankaccount/    -> port 8083
    instrument/    -> port 8084
    fundplan/      -> port 8085
    cashpool/      -> port 8086
    settlement/    -> port 8087
    limit/         -> port 8088
    fx/            -> port 8089
    irs/           -> port 8090
    valuation/     -> port 8091
    exposure/      -> port 8092
    hedge/         -> port 8093
    impairment/    -> port 8094
    var/           -> port 8095
    cockpit/       -> port 8096
    report/        -> port 8097
"""

import argparse
import os
import socket
import subprocess
import sys
import time
from pathlib import Path

PROJECT_ROOT = Path(__file__).resolve().parent.parent.parent.parent
BACKEND_DIR = PROJECT_ROOT / "basedata"
BACKEND_PORT = 8081
BACKEND_HOST = "localhost"
JAR_NAME = "opentms-basedata-1.0.0-SNAPSHOT.jar"


def is_port_in_use(port: int, host: str = "localhost") -> bool:
    """Check if a port is already in use."""
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
        try:
            s.bind((host, port))
            return False
        except OSError:
            return True


def get_process_by_port(port: int) -> list:
    """Get process IDs using the specified port (Windows)."""
    try:
        result = subprocess.run(
            ["netstat", "-ano"],
            capture_output=True,
            text=True,
            creationflags=subprocess.CREATE_NO_WINDOW if hasattr(subprocess, 'CREATE_NO_WINDOW') else 0
        )
        processes = []
        for line in result.stdout.splitlines():
            if f":{port}" in line and "LISTENING" in line:
                parts = line.split()
                if parts and parts[-1].isdigit():
                    processes.append(int(parts[-1]))
        return processes
    except Exception:
        return []


def get_java_process_by_port(port: int) -> list:
    """Get Java process PIDs running on the specified port."""
    pids = get_process_by_port(port)
    java_procs = []
    for pid in pids:
        try:
            result = subprocess.run(
                ["wmic", "process", "where", f"processid={pid}", "get", "commandline"],
                capture_output=True,
                text=True,
                creationflags=subprocess.CREATE_NO_WINDOW if hasattr(subprocess, 'CREATE_NO_WINDOW') else 0
            )
            if "java" in result.stdout.lower() or "basedata" in result.stdout.lower():
                java_procs.append(pid)
        except Exception:
            pass
    return java_procs


def kill_process_on_port(port: int) -> bool:
    """Kill all processes using the specified port."""
    pids = get_process_by_port(port)
    killed = []
    for pid in pids:
        try:
            subprocess.run(
                ["taskkill", "/F", "/PID", str(pid)],
                capture_output=True,
                creationflags=subprocess.CREATE_NO_WINDOW if hasattr(subprocess, 'CREATE_NO_WINDOW') else 0
            )
            killed.append(pid)
        except Exception:
            pass
    return len(killed) > 0


def find_jar_file() -> Path | None:
    """Find the built JAR file in target directory."""
    for pattern in ["target/*.jar", "target/jars/*.jar"]:
        jars = list(BACKEND_DIR.glob(pattern))
        if jars:
            return jars[0]
    return None


def start_backend() -> int:
    """Start the backend Spring Boot application."""
    if is_port_in_use(BACKEND_PORT, BACKEND_HOST):
        print(f"[INFO] Backend already running on port {BACKEND_PORT}")
        return 0

    jar_path = find_jar_file()

    if jar_path is None or not jar_path.exists():
        print(f"[INFO] JAR not found. Attempting to build...")
        try:
            build_result = subprocess.run(
                ["mvn", "clean", "package", "-DskipTests"],
                cwd=str(BACKEND_DIR),
                capture_output=True,
                text=True,
                timeout=300,
                creationflags=subprocess.CREATE_NO_WINDOW if hasattr(subprocess, 'CREATE_NO_WINDOW') else 0
            )
            if build_result.returncode != 0:
                print(f"[WARN] Build output: {build_result.stdout[-500:] if build_result.stdout else ''}")
                print(f"[ERROR] Failed to build backend")
                return 1

            jar_path = find_jar_file()
            if jar_path is None:
                print(f"[ERROR] JAR still not found after build")
                return 1
            print(f"[OK] Build successful: {jar_path.name}")
        except FileNotFoundError:
            print(f"[ERROR] Maven not found. Is Maven installed and in PATH?")
            return 1
        except subprocess.TimeoutExpired:
            print(f"[ERROR] Build timed out after 300 seconds")
            return 1
        except Exception as e:
            print(f"[ERROR] Build failed: {e}")
            return 1

    print(f"[INFO] Starting backend on port {BACKEND_PORT}")
    print(f"[INFO] JAR: {jar_path}")

    try:
        process = subprocess.Popen(
            ["java", "-jar", str(jar_path), "--server.port=" + str(BACKEND_PORT)],
            cwd=str(BACKEND_DIR),
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            text=True,
            creationflags=subprocess.CREATE_NO_WINDOW if hasattr(subprocess, 'CREATE_NO_WINDOW') else 0
        )

        print(f"[INFO] Backend started with PID: {process.pid}")
        print(f"[INFO] Waiting for server to be ready...")

        for i in range(60):
            time.sleep(2)
            if is_port_in_use(BACKEND_PORT, BACKEND_HOST):
                print(f"[OK] Backend is ready on http://{BACKEND_HOST}:{BACKEND_PORT}")
                return 0
            if process.poll() is not None:
                stderr = process.stderr.read() if process.stderr else ""
                print(f"[ERROR] Backend process died. stderr: {stderr[:500]}")
                return 1
            if i % 5 == 0:
                print(f"[INFO] Still starting... ({i * 2}s elapsed)")

        print(f"[WARN] Backend started but port {BACKEND_PORT} not responding yet")
        return 0

    except FileNotFoundError:
        print(f"[ERROR] Java not found. Is Java installed and in PATH?")
        return 1
    except Exception as e:
        print(f"[ERROR] Failed to start backend: {e}")
        return 1


def stop_backend() -> int:
    """Stop the backend Spring Boot application."""
    if not is_port_in_use(BACKEND_PORT, BACKEND_HOST):
        print(f"[INFO] Backend not running on port {BACKEND_PORT}")
        return 0

    print(f"[INFO] Stopping backend on port {BACKEND_PORT}...")

    killed = kill_process_on_port(BACKEND_PORT)

    time.sleep(2)

    if is_port_in_use(BACKEND_PORT, BACKEND_HOST):
        print(f"[WARN] Could not fully release port {BACKEND_PORT}")
        return 1

    print(f"[OK] Backend stopped")
    return 0


def restart_backend() -> int:
    """Restart the backend Spring Boot application."""
    print(f"[INFO] Restarting backend...")
    stop_backend()
    time.sleep(3)
    return start_backend()


def status_backend() -> int:
    """Check if backend is running."""
    if is_port_in_use(BACKEND_PORT, BACKEND_HOST):
        pids = get_process_by_port(BACKEND_PORT)
        print(f"[RUNNING] Backend running on http://{BACKEND_HOST}:{BACKEND_PORT} (PIDs: {pids})")
        return 0
    else:
        print(f"[STOPPED] Backend not running on port {BACKEND_PORT}")
        return 1


def main():
    parser = argparse.ArgumentParser(
        description="Open-TMS Backend Service Manager",
        formatter_class=argparse.RawDescriptionHelpFormatter
    )
    subparsers = parser.add_subparsers(dest="command", help="Available commands")

    subparsers.add_parser("start", help="Start the backend server")
    subparsers.add_parser("stop", help="Stop the backend server")
    subparsers.add_parser("restart", help="Restart the backend server")
    subparsers.add_parser("status", help="Check if backend is running")

    args = parser.parse_args()

    if args.command is None:
        parser.print_help()
        sys.exit(0 if is_port_in_use(BACKEND_PORT, BACKEND_HOST) else 1)

    commands = {
        "start": start_backend,
        "stop": stop_backend,
        "restart": restart_backend,
        "status": status_backend,
    }

    sys.exit(commands[args.command]())


if __name__ == "__main__":
    main()
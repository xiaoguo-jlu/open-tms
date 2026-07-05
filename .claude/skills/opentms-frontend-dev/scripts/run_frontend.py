#!/usr/bin/env python3
"""
Open-TMS Frontend Service Manager
Manages the lifecycle of the Open-TMS Vue.js frontend application.

Usage:
    python run_frontend.py start     # Start the frontend server
    python run_frontend.py stop    # Stop the frontend server
    python run_frontend.py restart # Restart the frontend server
    python run_frontend.py status  # Check if frontend is running

Requirements:
    - Node.js and npm must be installed
    - Frontend dependencies must be installed (npm install)
    - Vue.js project at web/ directory
"""

import argparse
import os
import signal
import socket
import subprocess
import sys
import time
from pathlib import Path

PROJECT_ROOT = Path(__file__).resolve().parent.parent.parent.parent
WEB_DIR = PROJECT_ROOT / "web"
FRONTEND_PORT = 3000
FRONTEND_HOST = "localhost"


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


def start_frontend() -> int:
    """Start the frontend development server."""
    if is_port_in_use(FRONTEND_PORT, FRONTEND_HOST):
        print(f"[INFO] Frontend already running on port {FRONTEND_PORT}")
        return 0

    if not WEB_DIR.exists():
        print(f"[ERROR] Frontend directory not found: {WEB_DIR}")
        return 1

    if not (WEB_DIR / "package.json").exists():
        print(f"[ERROR] package.json not found in {WEB_DIR}")
        return 1

    print(f"[INFO] Starting frontend on http://{FRONTEND_HOST}:{FRONTEND_PORT}")
    print(f"[INFO] Working directory: {WEB_DIR}")

    try:
        process = subprocess.Popen(
            ["npm", "run", "dev"],
            cwd=str(WEB_DIR),
            stdout=subprocess.PIPE,
            stderr=subprocess.STDOUT,
            text=True,
            bufsize=1,
            creationflags=subprocess.CREATE_NO_WINDOW if hasattr(subprocess, 'CREATE_NO_WINDOW') else 0
        )

        print(f"[INFO] Frontend started with PID: {process.pid}")
        print(f"[INFO] Waiting for server to be ready...")

        for i in range(30):
            time.sleep(1)
            if is_port_in_use(FRONTEND_PORT, FRONTEND_HOST):
                print(f"[OK] Frontend is ready on http://{FRONTEND_HOST}:{FRONTEND_PORT}")
                return 0
            print(f"[INFO] Waiting... ({i + 1}/30)")

        print(f"[WARN] Frontend started but port {FRONTEND_PORT} not responding yet")
        return 0

    except FileNotFoundError:
        print(f"[ERROR] npm not found. Is Node.js installed and in PATH?")
        return 1
    except Exception as e:
        print(f"[ERROR] Failed to start frontend: {e}")
        return 1


def stop_frontend() -> int:
    """Stop the frontend development server."""
    if not is_port_in_use(FRONTEND_PORT, FRONTEND_HOST):
        print(f"[INFO] Frontend not running on port {FRONTEND_PORT}")
        return 0

    print(f"[INFO] Stopping frontend on port {FRONTEND_PORT}...")

    killed = kill_process_on_port(FRONTEND_PORT)

    time.sleep(1)

    if is_port_in_use(FRONTEND_PORT, FRONTEND_HOST):
        print(f"[WARN] Could not fully release port {FRONTEND_PORT}")
        return 1

    print(f"[OK] Frontend stopped")
    return 0


def restart_frontend() -> int:
    """Restart the frontend development server."""
    print(f"[INFO] Restarting frontend...")
    stop_frontend()
    time.sleep(2)
    return start_frontend()


def status_frontend() -> int:
    """Check if frontend is running."""
    if is_port_in_use(FRONTEND_PORT, FRONTEND_HOST):
        pids = get_process_by_port(FRONTEND_PORT)
        print(f"[RUNNING] Frontend is running on http://{FRONTEND_HOST}:{FRONTEND_PORT} (PIDs: {pids})")
        return 0
    else:
        print(f"[STOPPED] Frontend is not running on port {FRONTEND_PORT}")
        return 1


def main():
    parser = argparse.ArgumentParser(
        description="Open-TMS Frontend Service Manager",
        formatter_class=argparse.RawDescriptionHelpFormatter
    )
    subparsers = parser.add_subparsers(dest="command", help="Available commands")

    subparsers.add_parser("start", help="Start the frontend development server")
    subparsers.add_parser("stop", help="Stop the frontend development server")
    subparsers.add_parser("restart", help="Restart the frontend development server")
    subparsers.add_parser("status", help="Check if frontend is running")

    args = parser.parse_args()

    if args.command is None:
        parser.print_help()
        sys.exit(0 if is_port_in_use(FRONTEND_PORT, FRONTEND_HOST) else 1)

    commands = {
        "start": start_frontend,
        "stop": stop_frontend,
        "restart": restart_frontend,
        "status": status_frontend,
    }

    sys.exit(commands[args.command]())


if __name__ == "__main__":
    main()
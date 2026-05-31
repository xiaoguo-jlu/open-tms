#!/usr/bin/env python3
"""
Open-TMS 测试服务启动器
同时启动前端和后端服务，然后运行测试脚本

Usage:
    python scripts/with_server.py \
      --server "cd web && npm run dev" --port 5173 \
      --server "java -jar dealing/target/dealing-1.0.0-SNAPSHOT.jar --server.port=8082" --port 8082 \
      -- python scripts/test/test_deal_ui.py
"""

import subprocess
import time
import os
import sys
import signal
import argparse
import urllib.request
import json


class ServerManager:
    def __init__(self):
        self.processes = []

    def kill_port(self, port):
        """Kill any existing process on port"""
        try:
            result = subprocess.run(['netstat', '-ano'], capture_output=True, text=True)
            for line in result.stdout.splitlines():
                if f':{port}' in line and 'LISTENING' in line:
                    parts = line.split()
                    if parts and parts[-1].isdigit():
                        pid = int(parts[-1])
                        try:
                            subprocess.run(['taskkill', '/F', '/PID', str(pid)], capture_output=True)
                            print(f'  Killed process {pid} on port {port}')
                        except:
                            pass
        except:
            pass
        time.sleep(1)

    def start_server(self, command, port, name=None):
        """Start a server process"""
        if name is None:
            name = f"Server on port {port}"

        print(f'\n[START] Starting {name} on port {port}...')
        self.kill_port(port)

        # Determine the command type and run accordingly
        if command.startswith("cd "):
            # Frontend command like "cd web && npm run dev"
            parts = command.split(" && ")
            cwd = parts[0].replace("cd ", "")
            cmd = " && ".join(parts[1:])
            proc = subprocess.Popen(
                cmd,
                shell=True,
                cwd=cwd,
                stdout=subprocess.PIPE,
                stderr=subprocess.STDOUT,
                text=True,
                bufsize=1
            )
        else:
            # Backend command like "java -jar ..."
            proc = subprocess.Popen(
                command,
                shell=True,
                stdout=subprocess.PIPE,
                stderr=subprocess.STDOUT,
                text=True,
                bufsize=1
            )

        self.processes.append({"proc": proc, "port": port, "name": name})

        # Wait for startup
        started = False
        for line in proc.stdout:
            print(f"  {line.strip()}")
            if 'Started ' in line or 'running at' in line.lower() or 'VITE' in line:
                started = True
                break

        if started:
            print(f'[OK] {name} started successfully!')
            time.sleep(2)
            return True
        else:
            print(f'[WARN] Could not confirm startup for {name}')
            time.sleep(5)  # Give it more time
            return True

    def check_port(self, port, url_path="/api/v1/dealing/deals/page"):
        """Check if a port is responding"""
        try:
            url = f"http://localhost:{port}{url_path}?pageNum=1&pageSize=1"
            req = urllib.request.Request(url)
            with urllib.request.urlopen(req, timeout=5) as resp:
                data = json.loads(resp.read().decode())
                return data.get('code') == 200
        except Exception as e:
            return False

    def wait_for_services(self, ports, timeout=60):
        """Wait for all services to be ready"""
        print("\n[WAIT] Waiting for services to be ready...")
        start_time = time.time()

        while time.time() - start_time < timeout:
            all_ready = True
            for port in ports:
                if self.check_port(port):
                    print(f"  [OK] Port {port} is ready")
                else:
                    print(f"  [WAIT] Port {port} not ready yet...")
                    all_ready = False

            if all_ready:
                print("[OK] All services are ready!")
                return True

            time.sleep(2)

        print("[WARN] Some services may not be ready yet, continuing anyway...")
        return False

    def stop_all(self):
        """Stop all server processes"""
        print("\n[STOP] Stopping all servers...")
        for server in self.processes:
            try:
                server["proc"].terminate()
                print(f"  [OK] Stopped {server['name']}")
            except:
                pass

        time.sleep(2)

        for server in self.processes:
            try:
                server["proc"].kill()
            except:
                pass

        self.processes = []
        print("[OK] All servers stopped")


def parse_args():
    """Parse command line arguments"""
    parser = argparse.ArgumentParser(description='Start servers and run test')

    parser.add_argument('--server', action='append', help='Server command to run')
    parser.add_argument('--port', action='append', type=int, help='Port for the server')
    parser.add_argument('command', nargs=argparse.REMAINDER, help='Command to run after servers start')

    return parser.parse_args()


def main():
    args = parse_args()

    if not args.server or not args.port:
        print("Error: --server and --port are required")
        print("Usage: python with_server.py --server 'npm run dev' --port 5173 -- python test.py")
        return 1

    if len(args.server) != len(args.port):
        print("Error: Number of --server and --port arguments must match")
        return 1

    manager = ServerManager()

    try:
        # Start all servers
        for i in range(len(args.server)):
            manager.start_server(args.server[i], args.port[i])

        # Wait for services to be ready
        manager.wait_for_services(args.port)

        # Run the test command
        if args.command:
            cmd = " ".join(args.command)
            print(f"\n[RUN] Executing: {cmd}")
            result = subprocess.run(cmd, shell=True)
            return result.returncode
        else:
            print("\n[INFO] No test command provided")
            print("Servers are still running. Press Ctrl+C to stop.")

            # Wait indefinitely
            while True:
                time.sleep(1)

    except KeyboardInterrupt:
        print("\n[INT] Interrupted by user")
    finally:
        manager.stop_all()

    return 0


if __name__ == "__main__":
    sys.exit(main())
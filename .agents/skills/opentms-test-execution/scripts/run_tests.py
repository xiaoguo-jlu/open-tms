#!/usr/bin/env python3
"""
Open-TMS Test Automation Runner
Manages and executes API automation test scripts for Open-TMS.

Usage:
    python run_tests.py start              # Start all tests
    python run_tests.py stop               # Stop running tests
    python run_tests.py list              # List available test suites
    python run_tests.py run <suite>       # Run specific test suite
    python run_tests.py run all            # Run all test suites
    python run_tests.py report <suite>    # Show last test report

Test suites available:
    - basedata      -> All basedata API tests (currency, country, bank, etc.)
    - ac            -> Actual cashflow (AC) API tests
    - at            -> Account transfer (AT) API tests
    - dealing       -> Dealing module API tests
    - full          -> Complete API test suite

Requirements:
    - Python 3.8+
    - requests library: pip install requests
    - Backend must be running on port 8081
"""

import argparse
import os
import subprocess
import sys
import time
from pathlib import Path
from datetime import datetime

PROJECT_ROOT = Path(__file__).resolve().parent.parent.parent.parent.parent
TEST_DIR = PROJECT_ROOT / "test"
SCRIPTS_DIR = PROJECT_ROOT / "test" / "scripts"
SCRIPTS_DIR = TEST_DIR / "scripts"
BACKEND_PORT = 8081
BACKEND_HOST = "localhost"


def is_backend_ready() -> bool:
    """Check if the backend server is ready."""
    import socket
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
        try:
            s.bind((BACKEND_HOST, BACKEND_PORT))
            return False
        except OSError:
            return True


def list_test_suites() -> dict:
    """List available test suites and their scripts."""
    suites = {}
    for item in SCRIPTS_DIR.iterdir():
        if item.is_dir():
            suite_name = item.name
            scripts = []
            for f in item.glob("*.py"):
                if not f.name.startswith("run_") and not f.name.startswith("check_"):
                    scripts.append(f.name)
            if scripts:
                suites[suite_name] = scripts

    all_scripts = []
    for f in SCRIPTS_DIR.glob("*.py"):
        if not f.name.startswith("run_") and not f.name.startswith("check_"):
            all_scripts.append(f.name)
    if all_scripts:
        suites["general"] = all_scripts

    for f in SCRIPTS_DIR.glob("*.js"):
        all_scripts.append(f.name)
    if "general" not in suites and all_scripts:
        suites["general"] = all_scripts

    return suites


def get_suite_script(suite: str) -> Path | None:
    """Get the main test script for a suite."""
    if suite == "basedata":
        scripts = list((SCRIPTS_DIR / "basedata").glob("test_all*.py"))
        return scripts[0] if scripts else None
    elif suite == "ac":
        scripts = list((SCRIPTS_DIR / "ac").glob("test*.py")) if (SCRIPTS_DIR / "ac").exists() else []
        if not scripts:
            scripts = list(SCRIPTS_DIR.glob("test_ac*.py"))
        return scripts[0] if scripts else None
    elif suite == "at":
        scripts = list((SCRIPTS_DIR / "at").glob("test*.py")) if (SCRIPTS_DIR / "at").exists() else []
        if not scripts:
            scripts = list(SCRIPTS_DIR.glob("test_at*.py"))
        return scripts[0] if scripts else None
    elif suite == "full":
        scripts = list(SCRIPTS_DIR.glob("test_full*.py"))
        return scripts[0] if scripts else None
    elif suite == "general":
        scripts = list(SCRIPTS_DIR.glob("test_*.py"))
        filtered = [s for s in scripts if not any(x in s.name for x in ["ac", "at", "basedata"])]
        return filtered[0] if filtered else None
    else:
        suite_path = SCRIPTS_DIR / suite
        if suite_path.exists():
            scripts = list(suite_path.glob("test*.py"))
            return scripts[0] if scripts else None
        scripts = list(SCRIPTS_DIR.glob(f"test_{suite}*.py"))
        return scripts[0] if scripts else None


def list_suites() -> int:
    """List all available test suites."""
    suites = list_test_suites()

    print("\nAvailable Test Suites:")
    print("=" * 50)
    for name, scripts in suites.items():
        print(f"  {name:15} -> {', '.join(scripts[:3])}{'...' if len(scripts) > 3 else ''}")
    print("=" * 50)
    print("\nUsage examples:")
    print("  python run_tests.py run basedata")
    print("  python run_tests.py run ac")
    print("  python run_tests.py run at")
    print("  python run_tests.py run full")
    print("  python run_tests.py run all")
    return 0


def run_suite(suite: str) -> int:
    """Run a specific test suite."""
    if not is_backend_ready():
        print(f"[ERROR] Backend not ready on port {BACKEND_PORT}")
        print(f"[INFO] Please start backend first: python run_backend.py start")
        return 1

    script_path = get_suite_script(suite)

    if script_path is None or not script_path.exists():
        print(f"[ERROR] Test suite '{suite}' not found")
        print(f"[INFO] Run 'list' command to see available suites")
        return 1

    print(f"[INFO] Running test suite: {suite}")
    print(f"[INFO] Script: {script_path}")
    print(f"[INFO] Backend: http://{BACKEND_HOST}:{BACKEND_PORT}")
    print("=" * 50)

    try:
        result = subprocess.run(
            [sys.executable, str(script_path)],
            cwd=str(SCRIPTS_DIR),
            text=True
        )
        return result.returncode
    except Exception as e:
        print(f"[ERROR] Failed to run tests: {e}")
        return 1


def run_all() -> int:
    """Run all test suites."""
    if not is_backend_ready():
        print(f"[ERROR] Backend not ready on port {BACKEND_PORT}")
        return 1

    suites = list_test_suites()
    total_results = {}
    total_passed = 0
    total_failed = 0

    print(f"[INFO] Running all test suites ({len(suites)} suites)...")
    print("=" * 50)

    for suite_name in suites.keys():
        print(f"\n[SUITE] {suite_name}")
        print("-" * 40)
        result = run_suite(suite_name)
        total_results[suite_name] = result
        if result == 0:
            total_passed += 1
        else:
            total_failed += 1

    print("\n" + "=" * 50)
    print("ALL SUITES SUMMARY")
    print("=" * 50)
    print(f"Total suites: {len(suites)}")
    print(f"Passed: {total_passed}")
    print(f"Failed: {total_failed}")
    print("=" * 50)

    return 0 if total_failed == 0 else 1


def stop_tests() -> int:
    """Stop running test processes."""
    try:
        result = subprocess.run(
            ["taskkill", "/F", "/IM", "python.exe"],
            capture_output=True,
            creationflags=subprocess.CREATE_NO_WINDOW if hasattr(subprocess, 'CREATE_NO_WINDOW') else 0
        )
        print("[OK] Python test processes terminated")
        return 0
    except Exception as e:
        print(f"[WARN] Could not stop processes: {e}")
        return 1


def show_report(suite: str = None) -> int:
    """Show the last test report."""
    reports_dir = TEST_DIR / "reports"
    if not reports_dir.exists():
        print(f"[INFO] No reports directory found: {reports_dir}")
        return 1

    if suite:
        report_files = list(reports_dir.glob(f"{suite}_*.txt"))
    else:
        report_files = list(reports_dir.glob("*.txt"))

    if not report_files:
        print(f"[INFO] No reports found for suite: {suite or 'all'}")
        return 1

    latest = max(report_files, key=lambda f: f.stat().st_mtime)
    print(f"[INFO] Latest report: {latest.name}")
    print("=" * 50)
    try:
        print(latest.read_text(encoding="utf-8"))
    except Exception as e:
        print(f"[ERROR] Could not read report: {e}")
        return 1
    return 0


def main():
    parser = argparse.ArgumentParser(
        description="Open-TMS Test Automation Runner",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""
Examples:
  python run_tests.py list              # Show available test suites
  python run_tests.py run basedata      # Run basedata tests
  python run_tests.py run ac           # Run cashflow tests
  python run_tests.py run at           # Run transfer tests
  python run_tests.py run full         # Run complete test suite
  python run_tests.py run all          # Run all test suites
  python run_tests.py stop             # Stop running tests
  python run_tests.py report basedata  # Show last basedata report
        """
    )
    subparsers = parser.add_subparsers(dest="command", help="Available commands")

    subparsers.add_parser("list", help="List available test suites")
    subparsers.add_parser("start", help="Start test execution (same as 'run all')")

    run_parser = subparsers.add_parser("run", help="Run a specific test suite")
    run_parser.add_argument("suite", nargs="?", default="all", help="Test suite name (default: all)")

    subparsers.add_parser("stop", help="Stop running test processes")
    subparsers.add_parser("restart", help="Restart test execution")

    report_parser = subparsers.add_parser("report", help="Show test report")
    report_parser.add_argument("suite", nargs="?", default=None, help="Test suite name")

    args = parser.parse_args()

    if args.command is None:
        parser.print_help()
        return 0

    commands = {
        "list": list_suites,
        "start": lambda: run_all(),
        "run": lambda: run_all() if args.suite == "all" else run_suite(args.suite),
        "stop": stop_tests,
        "restart": lambda: (stop_tests(), run_all())[1],
        "report": lambda: show_report(args.suite if hasattr(args, 'suite') else None),
    }

    sys.exit(commands[args.command]())


if __name__ == "__main__":
    main()
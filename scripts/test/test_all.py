#!/usr/bin/env python3
"""
Open-TMS 测试套件入口脚本
整合API测试和UI测试

使用方法:
  python scripts/test/test_all.py              # 运行所有测试
  python scripts/test/test_basedata_api.py     # 仅运行API测试
  python scripts/test/test_basedata_ui.py      # 仅运行UI测试
  python scripts/test/test_basedata_all.py     # 启动服务并运行所有测试
"""

import sys
import subprocess

def main():
    print("""
╔══════════════════════════════════════════════════════════════╗
║            Open-TMS Test Suite                               ║
║            基础数据模块自动化测试                              ║
╚══════════════════════════════════════════════════════════════╝
""")

    print("Available test scripts:")
    print("  1. test_basedata_api.py   - API测试 (需要后端运行)")
    print("  2. test_basedata_ui.py    - UI测试 (需要后端+前端运行)")
    print("  3. test_basedata_all.py   - 综合测试 (自动启动所有服务)")
    print("")
    print("Usage:")
    print("  python scripts/test/test_basedata_api.py")
    print("  python scripts/test/test_basedata_ui.py")
    print("  python scripts/test/test_basedata_all.py")
    print("")

    # 如果传入参数，执行对应测试
    if len(sys.argv) > 1:
        cmd = sys.argv[1]
        if cmd == "api":
            subprocess.run([sys.executable, "scripts/test/test_basedata_api.py"])
        elif cmd == "ui":
            subprocess.run([sys.executable, "scripts/test/test_basedata_ui.py"])
        elif cmd == "all":
            subprocess.run([sys.executable, "scripts/test/test_basedata_all.py"])
        else:
            print(f"Unknown command: {cmd}")
    else:
        print("Run specific test: python scripts/test/test_all.py [api|ui|all]")


if __name__ == "__main__":
    main()
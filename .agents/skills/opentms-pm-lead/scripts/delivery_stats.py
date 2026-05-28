#!/usr/bin/env python3
"""
Open-TMS 交付效率统计脚本
PM-Lead用于分析研发交付效率、识别优化机会

Usage:
    python delivery_stats.py weekly    # 本周统计
    python delivery_stats.py monthly   # 本月统计
    python delivery_stats.py trend     # 趋势分析
"""

import argparse
import subprocess
import json
from datetime import datetime, timedelta
from pathlib import Path

PROJECT_ROOT = Path(__file__).resolve().parent.parent.parent.parent


def run_gh_command(args: list) -> dict:
    """执行gh命令并返回结果"""
    try:
        result = subprocess.run(
            ["gh", "issue", "list", "--json", "number,title,labels,createdAt,closedAt,state"] + args,
            capture_output=True,
            text=True,
            cwd=str(PROJECT_ROOT)
        )
        return json.loads(result.stdout) if result.stdout else []
    except Exception as e:
        print(f"Error executing gh command: {e}")
        return []


def get_issues_by_label(labels: list, state: str = "all") -> list:
    """获取指定标签的Issue列表"""
    args = ["--state", state, "--limit", "100"]
    for label in labels:
        args.extend(["--label", label])
    return run_gh_command(args)


def get_closed_issues(days: int = 7) -> list:
    """获取最近N天关闭的Issue"""
    since_date = (datetime.now() - timedelta(days=days)).strftime("%Y-%m-%d")
    args = ["--state", "closed", "--limit", "100", "--search", f"created:>={since_date}"]
    return run_gh_command(args)


def calculate_cycle_time(issue: dict) -> int | None:
    """计算Issue的周转时间（天）"""
    if not issue.get("closedAt"):
        return None
    created = datetime.fromisoformat(issue["createdAt"].replace("Z", "+00:00"))
    closed = datetime.fromisoformat(issue["closedAt"].replace("Z", "+00:00"))
    return (closed - created).days


def analyze_delivery_stats(days: int = 7) -> dict:
    """分析交付统计数据"""
    closed_issues = get_closed_issues(days)

    # 分类统计
    feature_count = 0
    task_count = 0
    bug_count = 0
    total_cycle_time = 0
    valid_cycle_time_count = 0

    for issue in closed_issues:
        labels = [l.get("name", "") for l in issue.get("labels", [])]
        cycle_time = calculate_cycle_time(issue)

        if "Feature" in labels:
            feature_count += 1
        elif "Bug" in labels:
            bug_count += 1
        else:
            task_count += 1

        if cycle_time is not None:
            total_cycle_time += cycle_time
            valid_cycle_time_count += 1

    avg_cycle_time = total_cycle_time / valid_cycle_time_count if valid_cycle_time_count > 0 else 0

    return {
        "period_days": days,
        "total_closed": len(closed_issues),
        "feature_count": feature_count,
        "task_count": task_count,
        "bug_count": bug_count,
        "avg_cycle_time": round(avg_cycle_time, 1),
        "closed_issues": closed_issues
    }


def print_weekly_report():
    """打印周报"""
    stats = analyze_delivery_stats(days=7)

    print("\n" + "=" * 60)
    print("📊 Open-TMS 本周交付统计报告")
    print(f"统计周期: 最近7天 ({datetime.now().strftime('%Y-%m-%d')})")
    print("=" * 60)

    print(f"\n📈 交付概览:")
    print(f"  总完成Issue: {stats['total_closed']} 个")
    print(f"    - Feature: {stats['feature_count']} 个")
    print(f"    - Task: {stats['task_count']} 个")
    print(f"    - Bug: {stats['bug_count']} 个")

    print(f"\n⏱️ 效率指标:")
    print(f"  平均周转时间: {stats['avg_cycle_time']} 天")

    print(f"\n📋 完成详情:")
    for issue in stats["closed_issues"][:10]:
        labels = [l.get("name", "") for l in issue.get("labels", [])]
        print(f"  #{issue['number']} [{','.join(labels[:2])}] {issue['title'][:40]}")

    if len(stats["closed_issues"]) > 10:
        print(f"  ... 还有 {len(stats['closed_issues']) - 10} 个")

    print("\n" + "=" * 60)


def print_monthly_report():
    """打印月报"""
    stats = analyze_delivery_stats(days=30)

    print("\n" + "=" * 60)
    print("📊 Open-TMS 本月交付统计报告")
    print(f"统计周期: 最近30天 ({datetime.now().strftime('%Y-%m-%d')})")
    print("=" * 60)

    print(f"\n📈 交付概览:")
    print(f"  总完成Issue: {stats['total_closed']} 个")
    print(f"    - Feature: {stats['feature_count']} 个")
    print(f"    - Task: {stats['task_count']} 个")
    print(f"    - Bug: {stats['bug_count']} 个")

    print(f"\n⏱️ 效率指标:")
    print(f"  平均周转时间: {stats['avg_cycle_time']} 天")

    # 按标签分组统计平均时间
    label_cycle_times = {}
    for issue in stats["closed_issues"]:
        labels = [l.get("name", "") for l in issue.get("labels", [])]
        for label in labels:
            if label in ["Feature", "Bug", "Task"]:
                cycle_time = calculate_cycle_time(issue)
                if cycle_time is not None:
                    if label not in label_cycle_times:
                        label_cycle_times[label] = []
                    label_cycle_times[label].append(cycle_time)

    print(f"\n📊 分类平均周转时间:")
    for label, times in label_cycle_times.items():
        avg = sum(times) / len(times)
        print(f"  {label}: {avg:.1f} 天")

    print("\n" + "=" * 60)


def print_trend_analysis():
    """打印趋势分析"""
    print("\n" + "=" * 60)
    print("📈 Open-TMS 交付趋势分析")
    print("=" * 60)

    # 获取最近4周的数据
    weeks = []
    for i in range(4):
        days = (i + 1) * 7
        stats = analyze_delivery_stats(days=days)
        weeks.append(stats)

    print(f"\n📊 周度趋势:")
    print(f"{'周期':<15} {'完成数':<10} {'平均周转(天)':<15}")
    print("-" * 40)
    for i, stats in enumerate(reversed(weeks)):
        period = f"第{4-i}周" if i > 0 else "本周"
        print(f"{period:<15} {stats['total_closed']:<10} {stats['avg_cycle_time']:<15}")

    print("\n" + "=" * 60)


def main():
    parser = argparse.ArgumentParser(description="Open-TMS 交付效率统计")
    parser.add_argument("mode", choices=["weekly", "monthly", "trend"],
                        help="统计模式: weekly(周报), monthly(月报), trend(趋势)")
    args = parser.parse_args()

    if args.mode == "weekly":
        print_weekly_report()
    elif args.mode == "monthly":
        print_monthly_report()
    elif args.mode == "trend":
        print_trend_analysis()


if __name__ == "__main__":
    main()
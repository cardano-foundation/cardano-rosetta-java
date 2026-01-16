#!/usr/bin/env python3
"""
Soak testing CLI wrapper for Cardano Rosetta API.

Runs Locust-based load tests with sensible defaults and generates reports
matching the stability_test.py format.

Requires uv for environment isolation. Install with:
    curl -LsSf https://astral.sh/uv/install.sh | sh

Usage:
    # Quick soak test (5 min, 10 users)
    ./soak_test.py --url http://localhost:8082 --users 10 --duration 5m

    # Full soak test (1 hour, 50 users)
    ./soak_test.py --url http://localhost:8082 --users 50 --duration 1h

    # With specific network data
    ./soak_test.py --url http://localhost:8082 --network preprod --duration 30m
"""

import argparse
import csv
import datetime
import os
import random
import re
import shutil
import string
import subprocess
import sys
from typing import Dict, List, Optional

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))


def as_float(value: Optional[str]) -> float:
    """Best-effort conversion to float, returning 0.0 on failure."""
    try:
        return float(value)
    except (TypeError, ValueError):
        return 0.0


def parse_duration(duration_str: str) -> int:
    """Parse duration string to seconds. Supports: 30s, 5m, 1h, 2h30m."""
    total_seconds = 0
    pattern = re.compile(r'(\d+)([smh])')
    matches = pattern.findall(duration_str.lower())

    if not matches:
        # Try parsing as pure number (assume seconds)
        try:
            return int(duration_str)
        except ValueError:
            raise ValueError(f"Invalid duration format: {duration_str}")

    for value, unit in matches:
        value = int(value)
        if unit == 's':
            total_seconds += value
        elif unit == 'm':
            total_seconds += value * 60
        elif unit == 'h':
            total_seconds += value * 3600

    return total_seconds


def create_output_dir(prefix: str = "soak", release: str = "dev") -> str:
    """Create a unique output directory for test results."""
    now = datetime.datetime.now()
    date_str = now.strftime("%Y-%m-%d")
    time_str = now.strftime("%H-%M")
    random_suffix = ''.join(random.choices(string.ascii_lowercase + string.digits, k=6))
    safe_release = re.sub(r'[^A-Za-z0-9._-]', '_', release)
    dir_name = f"testresults_{date_str}_{time_str}_{safe_release}_{prefix}_{random_suffix}"

    full_path = os.path.join(SCRIPT_DIR, dir_name)
    os.makedirs(full_path, exist_ok=True)
    return full_path


def ensure_uv_available() -> str:
    """Return uv executable path or raise if missing."""
    uv_path = shutil.which("uv")
    if not uv_path:
        raise FileNotFoundError(
            "uv executable not found. Install uv with: curl -LsSf https://astral.sh/uv/install.sh | sh"
        )
    return uv_path


def run_locust(
    host: str,
    users: int,
    spawn_rate: int,
    duration_seconds: int,
    output_dir: str,
    network: str,
    locustfile: str,
) -> int:
    """Run Locust in headless mode and capture results."""

    # Set environment variable for network
    env = os.environ.copy()
    env['ROSETTA_NETWORK'] = network
    # Avoid leaking an unrelated virtualenv path into uv (prevents warning)
    env.pop('VIRTUAL_ENV', None)
    uv_executable = ensure_uv_available()

    # Build Locust command (use python -m locust to invoke the module)
    cmd = [
        uv_executable, 'run', 'python', '-m', 'locust',
        '--headless',
        f'--host={host}',
        f'--users={users}',
        f'--spawn-rate={spawn_rate}',
        f'--run-time={duration_seconds}s',
        f'--locustfile={locustfile}',
        f'--csv={os.path.join(output_dir, "results")}',
        f'--html={os.path.join(output_dir, "report.html")}',
    ]

    print(f"Running Locust command:")
    print(f"  {' '.join(cmd)}")
    print()

    # Run Locust
    result = subprocess.run(cmd, env=env, cwd=SCRIPT_DIR)
    return result.returncode


def parse_locust_csv(output_dir: str) -> Dict:
    """Parse Locust CSV output files and return structured data."""
    results = {
        'stats': [],
        'failures': [],
    }

    stats_file = os.path.join(output_dir, "results_stats.csv")
    if os.path.exists(stats_file):
        with open(stats_file, 'r') as f:
            reader = csv.DictReader(f)
            for row in reader:
                results['stats'].append(row)

    failures_file = os.path.join(output_dir, "results_failures.csv")
    if os.path.exists(failures_file):
        with open(failures_file, 'r') as f:
            reader = csv.DictReader(f)
            for row in reader:
                results['failures'].append(row)

    return results


def generate_summary_report(output_dir: str, results: Dict, args) -> None:
    """Generate a summary markdown report matching stability_test.py format."""
    summary_file = os.path.join(output_dir, "summary_results.md")

    with open(summary_file, 'w') as f:
        f.write("# Soak Test Results Summary\n\n")
        f.write(f"**Test Parameters:**\n")
        f.write(f"- URL: {args.url}\n")
        f.write(f"- Users: {args.users}\n")
        f.write(f"- Spawn Rate: {args.spawn_rate}/s\n")
        f.write(f"- Duration: {args.duration}\n")
        f.write(f"- Network: {args.network}\n")
        f.write(f"- Release: {args.release}\n\n")

        f.write("## Per-Endpoint Results\n\n")
        f.write("| Endpoint | Requests | Failures | Avg (ms) | p50 (ms) | p95 (ms) | p99 (ms) | Req/s |\n")
        f.write("| --- | --- | --- | --- | --- | --- | --- | --- |\n")

        for stat in results['stats']:
            if stat.get('Name') and stat['Name'] != 'Aggregated':
                req_per_sec = as_float(stat.get('Requests/s'))
                f.write(f"| {stat.get('Name', '')} | ")
                f.write(f"{stat.get('Request Count', '')} | ")
                f.write(f"{stat.get('Failure Count', '')} | ")
                f.write(f"{stat.get('Average Response Time', '')} | ")
                f.write(f"{stat.get('50%', '')} | ")
                f.write(f"{stat.get('95%', '')} | ")
                f.write(f"{stat.get('99%', '')} | ")
                f.write(f"{req_per_sec:.3f} |\n")

        # Add aggregated row
        for stat in results['stats']:
            if stat.get('Name') == 'Aggregated':
                f.write(f"\n**Aggregated:** ")
                f.write(f"{stat.get('Request Count', '')} requests, ")
                f.write(f"{stat.get('Failure Count', '')} failures, ")
                f.write(f"avg {stat.get('Average Response Time', '')}ms, ")
                f.write(f"p95 {stat.get('95%', '')}ms, ")
                f.write(f"p99 {stat.get('99%', '')}ms, ")
                f.write(f"{as_float(stat.get('Requests/s')):.3f} req/s\n")

        if results['failures']:
            f.write("\n## Failures\n\n")
            f.write("| Endpoint | Method | Count | Message |\n")
            f.write("| --- | --- | --- | --- |\n")
            for failure in results['failures']:
                f.write(f"| {failure.get('Name', '')} | ")
                f.write(f"{failure.get('Method', '')} | ")
                f.write(f"{failure.get('Occurrences', '')} | ")
                f.write(f"{failure.get('Error', '')[:100]} |\n")

    print(f"Summary report: {summary_file}")


def generate_details_csv(output_dir: str, results: Dict, args) -> None:
    """Generate a details CSV file matching stability_test.py format."""
    details_file = os.path.join(output_dir, "details_results.csv")

    with open(details_file, 'w', newline='') as f:
        writer = csv.writer(f)
        writer.writerow([
            "Network", "Users", "Duration", "Endpoint",
            "Requests", "Failures", "Avg_ms", "p50_ms", "p95_ms", "p99_ms", "Req_per_sec"
        ])

        for stat in results['stats']:
            if stat.get('Name') and stat['Name'] != 'Aggregated':
                req_per_sec = as_float(stat.get('Requests/s'))
                writer.writerow([
                    args.network,
                    args.users,
                    args.duration,
                    stat.get('Name', ''),
                    stat.get('Request Count', ''),
                    stat.get('Failure Count', ''),
                    stat.get('Average Response Time', ''),
                    stat.get('50%', ''),
                    stat.get('95%', ''),
                    stat.get('99%', ''),
                    f"{req_per_sec:.6f}",
                ])

    print(f"Details CSV: {details_file}")


def parse_args():
    parser = argparse.ArgumentParser(
        description='Soak testing CLI for Cardano Rosetta API',
        formatter_class=argparse.ArgumentDefaultsHelpFormatter
    )

    parser.add_argument('--url', required=True,
                        help='Base URL for the Rosetta API (e.g., http://localhost:8082)')
    parser.add_argument('--users', type=int, default=10,
                        help='Number of concurrent users')
    parser.add_argument('--spawn-rate', type=int, default=5,
                        help='Users to spawn per second')
    parser.add_argument('--duration', default='5m',
                        help='Test duration (e.g., 30s, 5m, 1h, 2h30m)')
    parser.add_argument('--network', default='preprod',
                        choices=['mainnet', 'preprod'],
                        help='Network for test data')
    parser.add_argument('--release', default='1.2.8',
                        help='Release/version label for output naming')
    parser.add_argument('--locustfile', default=os.path.join(SCRIPT_DIR, 'locustfile.py'),
                        help='Path to Locust test file')

    return parser.parse_args()


def main():
    args = parse_args()

    print("=" * 80)
    print("Cardano Rosetta Soak Test")
    print("=" * 80)
    print(f"URL: {args.url}")
    print(f"Users: {args.users}")
    print(f"Spawn Rate: {args.spawn_rate}/s")
    print(f"Duration: {args.duration}")
    print(f"Network: {args.network}")
    print(f"Release: {args.release}")
    print("=" * 80)
    print()

    # Parse duration
    try:
        duration_seconds = parse_duration(args.duration)
    except ValueError as e:
        print(f"Error: {e}")
        sys.exit(1)

    # Create output directory
    output_dir = create_output_dir(f"soak_{args.duration}", args.release)
    print(f"Output directory: {output_dir}")
    print()

    # Run Locust
    try:
        return_code = run_locust(
            host=args.url,
            users=args.users,
            spawn_rate=args.spawn_rate,
            duration_seconds=duration_seconds,
            output_dir=output_dir,
            network=args.network,
            locustfile=args.locustfile,
        )
    except FileNotFoundError as exc:
        print(f"Error: {exc}")
        return 1

    if return_code != 0:
        print(f"\nLocust exited with code {return_code}")

    # Parse results and generate reports
    print("\n" + "=" * 80)
    print("Generating reports...")
    print("=" * 80)

    results = parse_locust_csv(output_dir)
    generate_summary_report(output_dir, results, args)
    generate_details_csv(output_dir, results, args)

    print(f"\nHTML Report: {os.path.join(output_dir, 'report.html')}")
    print(f"\nDone! Results saved to: {output_dir}")

    return return_code


if __name__ == "__main__":
    sys.exit(main())

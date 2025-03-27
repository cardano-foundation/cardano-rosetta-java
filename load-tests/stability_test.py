#!/usr/bin/env python3
"""
A Python script for stability testing of Cardano Rosetta API endpoints:
- Runs extended duration tests to verify API reliability under constant load
- Monitors endpoint stability across multiple concurrent connections
- Measures response times (p95, p99) and validates against SLA thresholds
- Identifies potential degradation or failures during sustained operation

Usage examples:
  ./stability_test.py --url=http://127.0.0.1:8082 --csv=./my-data.csv --duration=30
  ./stability_test.py --hardware-profile=mid_level --machine-specs="8 cores, 64GB RAM" --sla=500
  ./stability_test.py --concurrency=1,2,4,8,16,32 --verbose --skip-header
"""

import csv
import subprocess
import re
import sys
import os
import datetime
import random
import string
import time
import argparse
from textwrap import dedent

###############################################################################
# COMMAND LINE ARGUMENTS
###############################################################################

def parse_args():
    """Parse command line arguments."""
    parser = argparse.ArgumentParser(
        description='Cardano Rosetta API Stability Testing Tool',
        formatter_class=argparse.ArgumentDefaultsHelpFormatter
    )

    # Basic configuration options
    parser.add_argument('--url', dest='base_url', default="http://127.0.0.1:8082",
                        help='Base URL for the Rosetta API service')
    parser.add_argument('--csv', dest='csv_file', 
                        default=os.path.join(os.path.dirname(os.path.abspath(__file__)), "mainnet-data.csv"),
                        help='Path to CSV file with test data')
    parser.add_argument('--release', dest='release_version', default="1.2.6-dev",
                        help='Release version for reporting')
    
    # Hardware profile options
    parser.add_argument('--hardware-profile', dest='hardware_profile', default="entry_level",
                        help='Hardware profile ID for reporting')
    parser.add_argument('--machine-specs', dest='machine_specs', 
                        default="16 cores, 16 threads, 125GB RAM, 3.9TB NVMe, QEMU Virtual CPU v2.5+",
                        help='Detailed machine specifications for reporting')
    
    # Test configuration options
    parser.add_argument('--concurrency', dest='concurrencies', type=lambda s: [int(item) for item in s.split(',')],
                        default=[1, 2, 4, 8, 12, 16, 20, 24, 28, 32, 36, 40, 44, 48, 52, 56, 60, 64, 68, 72, 76, 80, 84, 88, 92, 96, 100, 125, 150, 175, 200],
                        help='Comma-separated list of concurrency levels to test')
    parser.add_argument('--duration', dest='test_duration', type=int, default=60,
                        help='Duration in seconds for each concurrency level test')
    parser.add_argument('--sla', dest='sla_threshold', type=int, default=1000,
                        help='SLA threshold in milliseconds')
    
    # Misc options
    parser.add_argument('--skip-header', dest='skip_header', action='store_true',
                        help='Skip the header row in the CSV file')
    parser.add_argument('-v', '--verbose', dest='verbose', action='store_true',
                        help='Enable verbose output')
    parser.add_argument('--cooldown', dest='cooldown', type=int, default=60,
                        help='Cooldown period in seconds between endpoint tests')
    
    return parser.parse_args()

###############################################################################
# CONFIGURATION
###############################################################################

args = parse_args()

BASE_URL = args.base_url
SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
CSV_FILE = args.csv_file
RELEASE_VERSION = args.release_version
HARDWARE_PROFILE = args.hardware_profile
MACHINE_SPECS = args.machine_specs
CONCURRENCIES = args.concurrencies
TEST_DURATION = args.test_duration
SLA_THRESHOLD = args.sla_threshold
SKIP_HEADER = args.skip_header
VERBOSE = args.verbose
COOLDOWN_PERIOD = args.cooldown

###############################################################################
# FILES AND DIRECTORIES
###############################################################################

# Create a unique output directory based on date and version
def create_output_dir():
    """Create a unique output directory for test results."""
    now = datetime.datetime.now()
    today = now.strftime("%Y-%m-%d")
    time_str = now.strftime("%H-%M")  # Add time in HH-MM format
    random_suffix = ''.join(random.choices(string.ascii_lowercase + string.digits, k=6))
    dir_name = f"testresults_{today}_{time_str}_{RELEASE_VERSION}_{random_suffix}"
    
    # Create the directory inside the ab-tests folder
    full_path = os.path.join(SCRIPT_DIR, dir_name)
    os.makedirs(full_path, exist_ok=True)
    return full_path

OUTPUT_DIR = create_output_dir()
DETAILS_FILE = os.path.join(OUTPUT_DIR, "details_results.csv")
SUMMARY_FILE = os.path.join(OUTPUT_DIR, "summary_results.csv")
DETAILS_MD_FILE = os.path.join(OUTPUT_DIR, "details_results.md")
SUMMARY_MD_FILE = os.path.join(OUTPUT_DIR, "summary_results.md")
COMMANDS_FILE = os.path.join(OUTPUT_DIR, "ab_commands.log")

###############################################################################
# AB COMMAND GENERATOR AND PARSER 
###############################################################################

def get_ab_command(endpoint_path, concurrency, json_file):
    """
    Generate the ab command for the given endpoint and concurrency.
    """
    cmd = [
        "ab",
        "-t", str(TEST_DURATION),
        "-c", str(concurrency),
        "-p", json_file,
        "-T", "application/json",
        f"{BASE_URL}{endpoint_path}"
    ]
    return cmd

def log_command(cmd, endpoint_name, concurrency):
    """
    Log the ab command to the commands file.
    """
    with open(COMMANDS_FILE, 'a') as f:
        f.write(f"Endpoint: {endpoint_name}, Concurrency: {concurrency}\n")
        f.write(f"Command: {' '.join(cmd)}\n")
        f.write("-" * 80 + "\n")

def parse_ab_output(ab_stdout: str):
    """
    Given the raw output from `ab`, parse out the 95% and 99% lines and additional metrics.
    Returns (p95, p99, complete_requests, requests_per_sec, mean_time) as integers/floats.
    If not found, returns large defaults.
    """
    p95 = 999999
    p99 = 999999
    complete_requests = 0
    requests_per_sec = 0.0
    mean_time = 0.0

    # Parse each metric
    for line in ab_stdout.splitlines():
        line = line.strip()
        # Parse p95/p99
        if re.match(r'^95%\s+\d+', line):
            parts = line.split()
            if len(parts) >= 2:
                p95 = int(parts[1])
        elif re.match(r'^99%\s+\d+', line):
            parts = line.split()
            if len(parts) >= 2:
                p99 = int(parts[1])
        # Parse Complete requests
        elif "Complete requests:" in line:
            parts = line.split()
            if len(parts) >= 3:
                complete_requests = int(parts[2])
        # Parse Requests per second
        elif "Requests per second:" in line:
            parts = line.split()
            if len(parts) >= 4:
                requests_per_sec = float(parts[3])
        # Parse Mean time per request (first occurrence)
        elif "Time per request:" in line and "mean" in line:
            parts = line.split()
            if len(parts) >= 4:
                mean_time = float(parts[3])

    return p95, p99, complete_requests, requests_per_sec, mean_time

###############################################################################
# PAYLOAD GENERATORS
###############################################################################
# Each function receives CSV fields in a consistent order, then returns a JSON string.
# Adjust these if your CSV structure is different.

def payload_network_status(*_):
    """
    /network/status does not really need CSV data.
    """
    return dedent("""\
    {
      "network_identifier": {
        "blockchain": "cardano",
        "network": "mainnet"
      },
      "metadata": {}
    }
    """)

def payload_account_balance(address, *_):
    """
    /account/balance requires an address.
    """
    return dedent(f"""\
    {{
      "network_identifier": {{
        "blockchain": "cardano",
        "network": "mainnet"
      }},
      "account_identifier": {{
        "address": "{address}"
      }}
    }}
    """)

def payload_account_coins(address, *_):
    """
    /account/coins requires an address.
    """
    return dedent(f"""\
    {{
      "network_identifier": {{
        "blockchain": "cardano",
        "network": "mainnet"
      }},
      "account_identifier": {{
        "address": "{address}"
      }},
      "include_mempool": true
    }}
    """)

def payload_block(_addr, block_index, block_hash, *_):
    """
    /block requires block_index, block_hash
    """
    return dedent(f"""\
    {{
      "network_identifier": {{
        "blockchain": "cardano",
        "network": "mainnet"
      }},
      "block_identifier": {{
        "index": {block_index},
        "hash": "{block_hash}"
      }}
    }}
    """)

def payload_block_transaction(_addr, block_index, block_hash, _tx_size, _ttl, transaction_hash):
    """
    /block/transaction requires block_index, block_hash, transaction_hash
    """
    return dedent(f"""\
    {{
      "network_identifier": {{
        "blockchain": "cardano",
        "network": "mainnet"
      }},
      "block_identifier": {{
        "index": {block_index},
        "hash": "{block_hash}"
      }},
      "transaction_identifier": {{
        "hash": "{transaction_hash}"
      }}
    }}
    """)

def payload_search_transactions(_addr, _block_index, _block_hash, _tx_size, _ttl, transaction_hash):
    """
    /search/transactions requires transaction_hash.
    """
    return dedent(f"""\
    {{
      "network_identifier": {{
        "blockchain": "cardano",
        "network": "mainnet"
      }},
      "transaction_identifier": {{
        "hash": "{transaction_hash}"
      }}
    }}
    """)

def payload_construction_metadata(_addr, _block_index, _block_hash, transaction_size, relative_ttl, _tx_hash):
    """
    /construction/metadata requires transaction_size, relative_ttl
    """
    return dedent(f"""\
    {{
      "network_identifier": {{
        "blockchain": "cardano",
        "network": "mainnet"
      }},
      "options": {{
        "transaction_size": {transaction_size},
        "relative_ttl": {relative_ttl}
      }}
    }}
    """)

###############################################################################
# ENDPOINT DEFINITION
###############################################################################
# We'll define 7 endpoints with: (Name, Path, Payload Generator Function)
ENDPOINTS = [
    ("Network Status",       "/network/status",          payload_network_status),
    ("Account Balance",      "/account/balance",         payload_account_balance),
    ("Account Coins",        "/account/coins",           payload_account_coins),
    ("Block",                "/block",                   payload_block),
    ("Block Transaction",    "/block/transaction",       payload_block_transaction),
    ("Search Transactions",  "/search/transactions",     payload_search_transactions),
    ("Construction Metadata","/construction/metadata",   payload_construction_metadata),
]

###############################################################################
# RESULTS STORAGE
###############################################################################
# We'll keep two data structures:
# 1) details_results: a list of dicts, one per concurrency step
#    {endpoint, concurrency, p95, p99, meets_sla (bool)}
# 2) summary_results: a list of dicts, one per endpoint
#    {id, release, endpoint, max_concurrency, p95, p99}

details_results = []
summary_results = []

# Initialize CSV files with headers
def initialize_csv_files():
    """Initialize the CSV files with headers."""
    with open(DETAILS_FILE, 'w', newline='') as f:
        writer = csv.writer(f)
        writer.writerow(["Hardware_Profile", "Machine_Specs", "Endpoint", "Concurrency", "p95(ms)", "p99(ms)", 
                        "Meets_SLA", "Complete_Requests", "Requests_per_sec", "Mean_Time(ms)"])
    
    with open(SUMMARY_FILE, 'w', newline='') as f:
        writer = csv.writer(f)
        writer.writerow(["ID", "Release", "Hardware_Profile", "Machine_Specs", "Endpoint", "Max_Concurrency", "p95(ms)", "p99(ms)"])
    
    # Initialize command log file
    with open(COMMANDS_FILE, 'w') as f:
        f.write(f"AB Commands Log - {datetime.datetime.now()}\n")
        f.write("=" * 80 + "\n\n")

###############################################################################
# MARKDOWN FILE GENERATORS
###############################################################################

def generate_markdown_tables():
    """Generate markdown versions of the results tables with properly spaced columns."""
    # --- Details markdown table ---
    # First determine max width for each column in details table
    details_cols = ["Hardware", "Machine Specs", "Endpoint", "Concurrency", "p95 (ms)", "p99 (ms)", 
                    "Meets SLA", "Complete Reqs", "Reqs/sec", "Mean Time (ms)"]
    details_widths = {col: len(col) for col in details_cols}
    
    # Calculate max width needed for each column
    for record in details_results:
        details_widths["Hardware"] = max(details_widths["Hardware"], len(HARDWARE_PROFILE))
        details_widths["Machine Specs"] = max(details_widths["Machine Specs"], len(MACHINE_SPECS))
        details_widths["Endpoint"] = max(details_widths["Endpoint"], len(record["endpoint"]))
        details_widths["Concurrency"] = max(details_widths["Concurrency"], len(str(record["concurrency"])))
        details_widths["p95 (ms)"] = max(details_widths["p95 (ms)"], len(f"{record['p95']}ms"))
        details_widths["p99 (ms)"] = max(details_widths["p99 (ms)"], len(f"{record['p99']}ms"))
        details_widths["Meets SLA"] = max(details_widths["Meets SLA"], len(record["meets_sla"]))
        details_widths["Complete Reqs"] = max(details_widths["Complete Reqs"], len(str(record["complete_requests"])))
        details_widths["Reqs/sec"] = max(details_widths["Reqs/sec"], len(f"{record['requests_per_sec']:.2f}"))
        details_widths["Mean Time (ms)"] = max(details_widths["Mean Time (ms)"], len(f"{record['mean_time']:.2f}ms"))
    
    # Add padding to each column width
    details_widths = {k: v + 2 for k, v in details_widths.items()}
    
    with open(DETAILS_MD_FILE, 'w') as f:
        f.write("# Detailed Load Test Results\n\n")
        f.write("Per concurrency step results for each endpoint\n\n")
        
        # Write header with proper spacing
        header = "| " + " | ".join(f"{col:{details_widths[col]}}" for col in details_cols) + " |"
        f.write(header + "\n")
        
        # Write separator with proper width
        separator = "|" + "|".join(f"{'-' * details_widths[col]}" for col in details_cols) + "|"
        f.write(separator + "\n")
        
        # Table rows with proper spacing
        for record in details_results:
            row_values = [
                f"{HARDWARE_PROFILE:{details_widths['Hardware']}}",
                f"{MACHINE_SPECS:{details_widths['Machine Specs']}}",
                f"{record['endpoint']:{details_widths['Endpoint']}}",
                f"{record['concurrency']:{details_widths['Concurrency']}}",
                f"{record['p95']}ms".ljust(details_widths["p95 (ms)"]),
                f"{record['p99']}ms".ljust(details_widths["p99 (ms)"]),
                f"{record['meets_sla']:{details_widths['Meets SLA']}}",
                f"{record['complete_requests']:{details_widths['Complete Reqs']}}",
                f"{record['requests_per_sec']:.2f}".ljust(details_widths["Reqs/sec"]),
                f"{record['mean_time']:.2f}ms".ljust(details_widths["Mean Time (ms)"])
            ]
            f.write("| " + " | ".join(row_values) + " |\n")
    
    # --- Summary markdown table ---
    # First determine max width for each column in summary table
    summary_cols = ["ID", "Release", "Hardware", "Machine Specs", "Endpoint", "Max Concurrency", "p95 (ms)", "p99 (ms)"]
    summary_widths = {col: len(col) for col in summary_cols}
    
    # Calculate max width needed for each column
    for sr in summary_results:
        summary_widths["ID"] = max(summary_widths["ID"], len(str(sr["id"])))
        summary_widths["Release"] = max(summary_widths["Release"], len(sr["release"]))
        summary_widths["Hardware"] = max(summary_widths["Hardware"], len(HARDWARE_PROFILE))
        summary_widths["Machine Specs"] = max(summary_widths["Machine Specs"], len(MACHINE_SPECS))
        summary_widths["Endpoint"] = max(summary_widths["Endpoint"], len(sr["endpoint"]))
        summary_widths["Max Concurrency"] = max(summary_widths["Max Concurrency"], len(str(sr["max_concurrency"])))
        summary_widths["p95 (ms)"] = max(summary_widths["p95 (ms)"], len(f"{sr['p95']}ms"))
        summary_widths["p99 (ms)"] = max(summary_widths["p99 (ms)"], len(f"{sr['p99']}ms"))
    
    # Add padding to each column width
    summary_widths = {k: v + 2 for k, v in summary_widths.items()}
    
    with open(SUMMARY_MD_FILE, 'w') as f:
        f.write("# Summary Load Test Results\n\n")
        f.write("Maximum concurrency achieved per endpoint\n\n")
        
        # Write header with proper spacing
        header = "| " + " | ".join(f"{col:{summary_widths[col]}}" for col in summary_cols) + " |"
        f.write(header + "\n")
        
        # Write separator with proper width
        separator = "|" + "|".join(f"{'-' * summary_widths[col]}" for col in summary_cols) + "|"
        f.write(separator + "\n")
        
        # Table rows with proper spacing
        for sr in summary_results:
            row_values = [
                f"{sr['id']:{summary_widths['ID']}}",
                f"{sr['release']:{summary_widths['Release']}}",
                f"{HARDWARE_PROFILE:{summary_widths['Hardware']}}",
                f"{MACHINE_SPECS:{summary_widths['Machine Specs']}}",
                f"{sr['endpoint']:{summary_widths['Endpoint']}}",
                f"{sr['max_concurrency']:{summary_widths['Max Concurrency']}}",
                f"{sr['p95']}ms".ljust(summary_widths["p95 (ms)"]),
                f"{sr['p99']}ms".ljust(summary_widths["p99 (ms)"])
            ]
            f.write("| " + " | ".join(row_values) + " |\n")

###############################################################################
# TEST FUNCTION
###############################################################################

def test_endpoint(endpoint_name, endpoint_path, payload_func, csv_row):
    """
    - Extract needed fields from csv_row
    - Generate payload once
    - Step through concurrency
    - For each concurrency, call `ab`, parse p95/p99, record details, check SLA
    - Return (max_sla_concurrency, p95_at_that_concurrency, p99_at_that_concurrency)
    """
    # Example CSV columns:
    # address, block_index, block_hash, transaction_size, relative_ttl, transaction_hash
    #
    # Adjust if your CSV has different columns or order.
    address, block_index, block_hash, transaction_size, relative_ttl, transaction_hash = csv_row

    # Generate JSON payload
    json_payload = payload_func(address, block_index, block_hash, transaction_size, relative_ttl, transaction_hash)

    # Write the payload to a temp file in the output directory
    endpoint_safe_name = endpoint_name.replace(' ', '_')
    tmp_file = os.path.join(OUTPUT_DIR, f"{endpoint_safe_name}.json")
    with open(tmp_file, "w") as f:
        f.write(json_payload)

    max_sla_conc = 0
    best_p95 = 0
    best_p99 = 0

    for c in CONCURRENCIES:
        if VERBOSE:
            print(f"{'-' * 80}")
            print(f"Endpoint: {endpoint_name}, Concurrency: {c}")
            print(f"Running ab for {TEST_DURATION} seconds against {endpoint_path}")
            print(f"{'-' * 80}")
        
        # Generate and log the ab command
        cmd = get_ab_command(endpoint_path, c, tmp_file)
        log_command(cmd, endpoint_name, c)

        try:
            proc = subprocess.run(cmd, capture_output=True, text=True, check=True)
            ab_output = proc.stdout
        except subprocess.CalledProcessError as e:
            print(f"ERROR: ab command failed at concurrency {c} for endpoint {endpoint_name}")
            print("-" * 80)
            print(e.output)
            print("-" * 80)
            break

        # Parse p95, p99 and additional metrics
        p95, p99, complete_requests, requests_per_sec, mean_time = parse_ab_output(ab_output)

        meets_sla = (p95 < SLA_THRESHOLD) and (p99 < SLA_THRESHOLD)
        meets_sla_str = "Yes" if meets_sla else "No"

        # Store detail record in memory - use endpoint_path instead of endpoint_name
        details_results.append({
            "endpoint": endpoint_path,
            "concurrency": c,
            "p95": p95,
            "p99": p99,
            "meets_sla": meets_sla_str,
            "complete_requests": complete_requests,
            "requests_per_sec": requests_per_sec,
            "mean_time": mean_time
        })
        
        # Write to CSV file - use endpoint_path instead of endpoint_name
        with open(DETAILS_FILE, 'a', newline='') as f:
            writer = csv.writer(f)
            writer.writerow([
                HARDWARE_PROFILE,
                MACHINE_SPECS,
                endpoint_path,
                c,
                f"{p95}ms",
                f"{p99}ms",
                meets_sla_str,
                complete_requests,
                requests_per_sec,
                f"{mean_time}ms"
            ])

        if VERBOSE:
            print(f"Results for {endpoint_name} at concurrency {c}:")
            print(f"  p95: {p95}ms")
            print(f"  p99: {p99}ms")
            print(f"  Meets SLA: {meets_sla_str}")
            print(f"  Complete Requests: {complete_requests}")
            print(f"  Requests/sec: {requests_per_sec}")
            print(f"  Mean Time/Request: {mean_time}ms")
            print()

        if meets_sla:
            max_sla_conc = c
            best_p95 = p95
            best_p99 = p99
        else:
            print(f"SLA threshold of {SLA_THRESHOLD}ms exceeded at concurrency {c}. Stopping tests for {endpoint_name}.")
            break

    return max_sla_conc, best_p95, best_p99


###############################################################################
# MAIN
###############################################################################

def main():
    try:
        # Create output directory and initialize CSV files
        print(f"Creating output directory: {OUTPUT_DIR}")
        initialize_csv_files()
        
        # 1) Read CSV
        try:
            with open(CSV_FILE, "r", newline="") as f:
                reader = csv.reader(f)
                rows = list(reader)
        except FileNotFoundError:
            print(f"ERROR: CSV file '{CSV_FILE}' not found.")
            sys.exit(1)

        if not rows:
            print(f"ERROR: CSV file '{CSV_FILE}' is empty.")
            sys.exit(1)

        if SKIP_HEADER:
            rows = rows[1:]

        # For demonstration, pick the *first* row only.
        # If you want to test multiple rows, you can loop here or adapt logic.
        if not rows:
            print("ERROR: No CSV data after skipping header.")
            sys.exit(1)

        first_line = rows[0]

        # 2) Test each endpoint with the same CSV row
        summary_id = 1
        for idx, (ep_name, ep_path, ep_func) in enumerate(ENDPOINTS):
            print("=" * 80)
            print(f"TESTING ENDPOINT: {ep_name} ({ep_path})")
            print("=" * 80)

            # Add a sleep between tests (except before the first test)
            if idx > 0:
                print(f"Sleeping for {COOLDOWN_PERIOD} seconds to allow connection pool recovery...")
                time.sleep(COOLDOWN_PERIOD)
                print("Resuming tests...")

            max_conc, p95_val, p99_val = test_endpoint(ep_name, ep_path, ep_func, first_line)

            # Add summary record to memory
            summary_results.append({
                "id": summary_id,
                "release": RELEASE_VERSION,
                "endpoint": ep_path,
                "max_concurrency": max_conc,
                "p95": p95_val,
                "p99": p99_val,
            })
            
            # Write to summary CSV file
            with open(SUMMARY_FILE, 'a', newline='') as f:
                writer = csv.writer(f)
                writer.writerow([
                    summary_id,
                    RELEASE_VERSION,
                    HARDWARE_PROFILE,
                    MACHINE_SPECS,
                    ep_path,
                    max_conc,
                    f"{p95_val}ms",
                    f"{p99_val}ms"
                ])
            
            summary_id += 1

            print()
            print(f"Completed testing for {ep_name} - Max concurrency: {max_conc}, p95: {p95_val}ms, p99: {p99_val}ms")
            print()

        # Generate markdown files from our results
        generate_markdown_tables()

        # 3) Print tables
        print("\n" + "=" * 80)
        print(" DETAILED RESULTS (per concurrency step) ")
        print("=" * 80)

        # Print header
        print("| %-15s | %-15s | %-20s | %-10s | %-18s | %-18s | %-12s | %-18s | %-18s | %-18s |" % (
            "Hardware", "Machine Specs", "Endpoint", "Concurrency", "p95 (ms)", "p99 (ms)", "SLA?", "Complete Reqs", "Reqs/sec", "Mean Time (ms)"
        ))
        print("-" * 80)

        for record in details_results:
            print("| %-15s | %-15s | %-20s | %-10s | %-18s | %-18s | %-12s | %-18s | %-18s | %-18s |" % (
                HARDWARE_PROFILE,
                MACHINE_SPECS,
                record["endpoint"],
                record["concurrency"],
                f"{record['p95']}ms",
                f"{record['p99']}ms",
                record["meets_sla"],
                record["complete_requests"],
                f"{record['requests_per_sec']:.2f}",
                f"{record['mean_time']:.2f}ms"
            ))

        print("\n" + "=" * 80)
        print(" FINAL SUMMARY (maximum concurrency per endpoint) ")
        print("=" * 80)

        # Print header
        print("| %-2s | %-6s | %-15s | %-15s | %-22s | %-16s | %-16s | %-16s |" % (
            "ID", "Rel.", "Hardware", "Machine Specs", "Endpoint", "Max Concurrency", "p95 (ms)", "p99 (ms)"
        ))
        print("-" * 80)

        for sr in summary_results:
            print("| %-2s | %-6s | %-15s | %-15s | %-22s | %-16s | %-16s | %-16s |" % (
                sr["id"],
                sr["release"],
                HARDWARE_PROFILE,
                MACHINE_SPECS,
                sr["endpoint"],
                sr["max_concurrency"],
                f"{sr['p95']}ms",
                f"{sr['p99']}ms"
            ))

        print(f"\nDone. Results saved to: {OUTPUT_DIR}")
        print(f"Files generated:")
        print(f"  - {DETAILS_FILE}")
        print(f"  - {SUMMARY_FILE}")
        print(f"  - {DETAILS_MD_FILE}")
        print(f"  - {SUMMARY_MD_FILE}")
        print(f"  - {COMMANDS_FILE}")
        print(f"  - JSON payload files for each endpoint")
        
    except KeyboardInterrupt:
        print("\n\n" + "=" * 80)
        print("Test interrupted by user (Ctrl+C)")
        print("=" * 80)
        
        # Get the current endpoint being tested (if available)
        current_endpoint = None
        if 'ep_name' in locals() and 'c' in locals():
            current_endpoint = f"{ep_name} at concurrency level {c}"
        
        if current_endpoint:
            print(f"\nTest was interrupted while testing: {current_endpoint}")
        
        if details_results:
            print("\nPartial results were collected and saved to:", OUTPUT_DIR)
            print(f"\nResults collected for {len(details_results)} test iterations.")
            
            # Generate markdown files from the partial results
            try:
                generate_markdown_tables()
                print("Partial markdown reports were generated successfully.")
            except Exception as e:
                print(f"Could not generate markdown reports: {e}")
        else:
            print("\nNo results were collected before interruption.")
        
        print("\nExiting gracefully...")
        sys.exit(0)
    except Exception as e:
        print("\n\n" + "=" * 80)
        print(f"An error occurred: {str(e)}")
        print("=" * 80)
        
        if details_results:
            print("\nPartial results were saved to:", OUTPUT_DIR)
        
        print("\nExiting with error...")
        sys.exit(1)


if __name__ == "__main__":
    main()
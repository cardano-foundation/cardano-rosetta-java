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
  ./stability_test.py --concurrency=1,2,4,8,16,32 --verbose --no-header
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
import logging
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
                        default=os.path.join(os.path.dirname(os.path.abspath(__file__)), "data/mainnet-data.csv"),
                        help='Path to CSV file with test data')
    parser.add_argument('--release', dest='release_version', default="1.2.7",
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
    parser.add_argument('--no-header', dest='no_header', action='store_true',
                        help='Specify this flag if the CSV file does not have a header row')
    parser.add_argument('-v', '--verbose', dest='verbose', action='store_true',
                        help='Enable verbose output')
    parser.add_argument('--cooldown', dest='cooldown', type=int, default=60,
                        help='Cooldown period in seconds between endpoint tests')
    
    # Endpoint selection
    parser.add_argument('--endpoints', dest='selected_endpoints', type=str,
                        help='Comma-separated list of endpoint names or paths to test (e.g. "Network Status,Block" or "/account/balance,/block"). If not specified, all endpoints will be tested.')
    
    # List available endpoints without running tests
    parser.add_argument('--list-endpoints', dest='list_endpoints', action='store_true',
                        help='List all available endpoints and exit without running tests')
    
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
NO_HEADER = args.no_header
VERBOSE = args.verbose
COOLDOWN_PERIOD = args.cooldown

# Global logger variable
logger = None

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
# LOGGING SETUP
###############################################################################

def setup_logging(output_dir, verbose):
    """Configure logging to file and console."""
    global logger
    logger = logging.getLogger('stability_test')
    logger.setLevel(logging.DEBUG) # Capture all levels

    log_format = logging.Formatter('%(asctime)s - %(levelname)s - %(message)s')

    # File handler - always logs DEBUG level and above
    log_file = os.path.join(output_dir, 'stability_test.log')
    file_handler = logging.FileHandler(log_file)
    file_handler.setLevel(logging.DEBUG)
    file_handler.setFormatter(log_format)
    logger.addHandler(file_handler)

    # Console handler - logs INFO or DEBUG based on verbosity
    console_handler = logging.StreamHandler(sys.stdout)
    console_level = logging.DEBUG if verbose else logging.INFO
    console_handler.setLevel(console_level)
    console_handler.setFormatter(log_format)
    logger.addHandler(console_handler)

    # Prevent double logging if run multiple times (e.g., in tests)
    logger.propagate = False

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
    with open(DETAILS_MD_FILE, 'w') as f:
        f.write("# Detailed Load Test Results\n\n")
        f.write("Per concurrency step results for each endpoint\n\n")
        
        # Write header with proper spacing
        header = "| Hardware | Machine Specs | Endpoint | Concurrency | p95 (ms) | p99 (ms) | Meets SLA | Complete Reqs | Reqs/sec | Mean Time (ms) |"
        f.write(header + "\n")
        
        # Write separator with proper width - at least 3 dashes per column
        separator = "| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |"
        f.write(separator + "\n")
        
        # Table rows
        for record in details_results:
            row_values = [
                f"{HARDWARE_PROFILE}",
                f"{MACHINE_SPECS}",
                f"{record['endpoint']}",
                f"{record['concurrency']}",
                f"{record['p95']}ms",
                f"{record['p99']}ms",
                f"{record['meets_sla']}",
                f"{record['complete_requests']}",
                f"{record['requests_per_sec']:.2f}",
                f"{record['mean_time']:.2f}ms"
            ]
            f.write("| " + " | ".join(row_values) + " |\n")
    
    # --- Summary markdown table ---
    with open(SUMMARY_MD_FILE, 'w') as f:
        f.write("# Summary Load Test Results\n\n")
        f.write("Maximum concurrency achieved per endpoint\n\n")
        
        # Write header with proper spacing
        header = "| ID | Release | Hardware | Machine Specs | Endpoint | Max Concurrency | p95 (ms) | p99 (ms) |"
        f.write(header + "\n")
        
        # Write separator with proper width - at least 3 dashes per column
        separator = "| --- | --- | --- | --- | --- | --- | --- | --- |"
        f.write(separator + "\n")
        
        # Table rows
        for sr in summary_results:
            row_values = [
                f"{sr['id']}",
                f"{sr['release']}",
                f"{HARDWARE_PROFILE}",
                f"{MACHINE_SPECS}",
                f"{sr['endpoint']}",
                f"{sr['max_concurrency']}",
                f"{sr['p95']}ms",
                f"{sr['p99']}ms"
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
        # Use logger.debug for verbose output
        logger.debug(f"{'-' * 80}")
        logger.debug(f"Endpoint: {endpoint_name}, Concurrency: {c}")
        logger.debug(f"Running ab for {TEST_DURATION} seconds against {endpoint_path}")
        logger.debug(f"{'-' * 80}")
        
        # Generate and log the ab command
        cmd = get_ab_command(endpoint_path, c, tmp_file)
        log_command(cmd, endpoint_name, c)

        try:
            proc = subprocess.run(cmd, capture_output=True, text=True, check=True)
            ab_output = proc.stdout
        except subprocess.CalledProcessError as e:
            # Use logger.error for errors
            logger.error(f"ab command failed at concurrency {c} for endpoint {endpoint_name}")
            logger.error(f"Command: {' '.join(cmd)}")
            logger.error(f"Output: {e.output}")
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

        # Use logger.debug for verbose results
        logger.debug(f"Results for {endpoint_name} at concurrency {c}:")
        logger.debug(f"  p95: {p95}ms")
        logger.debug(f"  p99: {p99}ms")
        logger.debug(f"  Meets SLA: {meets_sla_str}")
        logger.debug(f"  Complete Requests: {complete_requests}")
        logger.debug(f"  Requests/sec: {requests_per_sec}")
        logger.debug(f"  Mean Time/Request: {mean_time}ms")

        if meets_sla:
            max_sla_conc = c
            best_p95 = p95
            best_p99 = p99
        else:
            # Use logger.info for standard flow messages
            logger.info(f"SLA threshold of {SLA_THRESHOLD}ms exceeded at concurrency {c}. Stopping tests for {endpoint_name}.")
            break

    return max_sla_conc, best_p95, best_p99


###############################################################################
# MAIN
###############################################################################

def main():
    try:
        # Check if we just need to list available endpoints
        if args.list_endpoints:
            # Keep print for direct user output like this list
            print("Available endpoints:")
            print("-" * 80)
            for name, path, _ in ENDPOINTS:
                print(f"{name:<25} {path}")
            print()
            sys.exit(0)
            
        # Create output directory
        print(f"Creating output directory: {OUTPUT_DIR}") # Keep initial print before logger is set up

        # Set up logging *after* OUTPUT_DIR is created
        setup_logging(OUTPUT_DIR, VERBOSE)
        logger.info(f"Logging initialized. Log file: {os.path.join(OUTPUT_DIR, 'stability_test.log')}")
        logger.info(f"Script arguments: {vars(args)}")

        initialize_csv_files()
        logger.info(f"Output files initialized in {OUTPUT_DIR}")

        # 1) Read CSV
        try:
            with open(CSV_FILE, "r", newline="") as f:
                reader = csv.reader(f)
                rows = list(reader)
        except FileNotFoundError:
            # Use logger.error and exit
            logger.error(f"CSV file '{CSV_FILE}' not found.")
            sys.exit(1)

        if not rows:
            # Use logger.error and exit
            logger.error(f"CSV file '{CSV_FILE}' is empty.")
            sys.exit(1)

        # By default, assume the CSV file has a header row (column names)
        # Skip the header row when processing the data
        # Only if --no-header is specified, treat all rows as data
        if not NO_HEADER:
            header_row = rows[0]
            rows = rows[1:]
            # Use logger.debug for verbose info
            logger.debug(f"{'-' * 80}")
            logger.debug(f"CSV DATA INFORMATION")
            logger.debug(f"{'-' * 80}")
            logger.debug(f"Header: {', '.join(header_row)}")
            logger.debug(f"Data row: {', '.join(rows[0]) if rows else 'No data available'}")
            logger.debug(f"{'-' * 80}")

        # For demonstration, pick the *first* row only.
        # If you want to test multiple rows, you can loop here or adapt logic.
        if not rows:
            # Use logger.error and exit
            logger.error("No CSV data after skipping header.")
            sys.exit(1)

        first_line = rows[0]
        logger.info(f"Using CSV data row: {first_line}")

        # 2) Test each endpoint with the same CSV row
        summary_id = 1
        
        # Filter endpoints based on command-line parameter if specified
        endpoints_to_test = ENDPOINTS
        if args.selected_endpoints:
            selected = [item.strip() for item in args.selected_endpoints.split(',')]
            # Try to match by name first, then by path if no name matches are found
            endpoints_to_test = [(name, path, func) for (name, path, func) in ENDPOINTS if name in selected]
            
            # If no matches by name, try matching by path
            if not endpoints_to_test:
                endpoints_to_test = [(name, path, func) for (name, path, func) in ENDPOINTS if path in selected]
            
            if not endpoints_to_test:
                # Use logger.error and exit
                logger.error("None of the specified endpoints were found.")
                # Keep print for user help message
                print(f"Available endpoints (name): {', '.join(ep[0] for ep in ENDPOINTS)}")
                print(f"Available endpoints (path): {', '.join(ep[1] for ep in ENDPOINTS)}")
                sys.exit(1)
                
            # Use logger.info
            logger.info(f"Testing {len(endpoints_to_test)} of {len(ENDPOINTS)} endpoints:")
            for name, path, _ in endpoints_to_test:
                logger.info(f" - {name} ({path})")
            print()
        
        for idx, (ep_name, ep_path, ep_func) in enumerate(endpoints_to_test):
            logger.info("=" * 80)
            logger.info(f"STARTING TEST FOR ENDPOINT: {ep_name} ({ep_path})")
            logger.info("=" * 80)

            # Add a sleep between tests (except before the first test)
            if idx > 0:
                logger.info(f"Sleeping for {COOLDOWN_PERIOD} seconds to allow connection pool recovery...")
                time.sleep(COOLDOWN_PERIOD)
                logger.info("Resuming tests...")

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

            # Use logger.info for completion summary
            logger.info(f"Completed testing for {ep_name} - Max concurrency: {max_conc}, p95: {p95_val}ms, p99: {p99_val}ms")
            print()

        # Generate markdown files from our results
        generate_markdown_tables()
        logger.info("Generated markdown report files.")

        # 3) Print tables - Keep these as print for final console summary
        print("=" * 80)
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

        print("=" * 80)
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

        # Keep print for final summary message
        print(f"Done. Results saved to: {OUTPUT_DIR}")
        print(f"Files generated:")
        print(f"  - {DETAILS_FILE}")
        print(f"  - {SUMMARY_FILE}")
        print(f"  - {DETAILS_MD_FILE}")
        print(f"  - {SUMMARY_MD_FILE}")
        print(f"  - {COMMANDS_FILE}")
        print(f"  - {os.path.join(OUTPUT_DIR, 'stability_test.log')} (Log File)") # <-- Add log file here
        print(f"  - JSON payload files for each endpoint")
        
    except KeyboardInterrupt:
        # Use logger.warning for interruptions
        if logger: # Check if logger was initialized
             logger.warning("=" * 80)
             logger.warning("Test interrupted by user (Ctrl+C)")
             logger.warning("=" * 80)

             current_endpoint = None
             if 'ep_name' in locals() and 'c' in locals():
                 current_endpoint = f"{ep_name} at concurrency level {c}"

             if current_endpoint:
                 logger.warning(f"Test was interrupted while testing: {current_endpoint}")

             if details_results:
                 logger.warning(f"Partial results were collected and saved to: {OUTPUT_DIR}")
                 logger.warning(f"Results collected for {len(details_results)} test iterations.")

                 # Attempt to generate markdown from partial results
                 try:
                     generate_markdown_tables()
                     logger.warning("Partial markdown reports were generated successfully.")
                 except Exception as e_md:
                     logger.error(f"Could not generate markdown reports from partial results: {e_md}")
             else:
                 logger.warning("No results were collected before interruption.")

             logger.warning("Exiting gracefully...")
        else:
            # Fallback print if logger isn't set up yet
            print("Test interrupted by user (Ctrl+C) before logging was fully configured. Exiting.")
        sys.exit(0) # Use exit code 0 for graceful exit on interrupt

    except Exception as e:
        # Use logger.exception for unexpected errors to capture traceback
        if logger:
            logger.exception("=" * 80)
            logger.exception(f"An unexpected error occurred: {str(e)}")
            logger.exception("=" * 80)

            if details_results:
                 logger.warning(f"Partial results (if any) were saved to: {OUTPUT_DIR}")
            logger.error("Exiting with error...")
        else:
             # Fallback print if logger isn't set up yet
             print(f"An unexpected error occurred before logging was fully configured: {str(e)}. Exiting.")
        sys.exit(1) # Use non-zero exit code for errors


if __name__ == "__main__":
    main()
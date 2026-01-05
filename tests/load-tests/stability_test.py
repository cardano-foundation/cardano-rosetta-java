#!/usr/bin/env python3
"""
A Python script for capacity testing of Cardano Rosetta API endpoints using
dimension-isolated data profiles.

Features:
- Tests ONE dimension at a time to identify specific performance bottlenecks
- Uses power-of-10 levels (1, 10, 100, 1000, ...) for power-law distributions
- Uses percentile levels (p50, p75, p90, p95, p99) for log-normal distributions
- Uses era names (byron, shelley, etc.) for block age dimension
- Displays friendly value ranges (e.g., "1-9 UTXOs" or "≤85 bytes")
- Supports exponential+binary search for fast max concurrency discovery

Usage examples:
  # Test a specific dimension at specific level
  ./stability_test.py --url http://localhost:8082 --dimension utxo_count --level 10

  # Test all levels of a dimension
  ./stability_test.py --url http://localhost:8082 --dimension utxo_count --level all

  # Test multiple dimensions and levels
  ./stability_test.py --url http://localhost:8082 --dimensions "utxo_count:1,100;block_body_size:p90"

  # Test all dimensions at all levels (comprehensive)
  ./stability_test.py --url http://localhost:8082 --dimension all --level all

  # Test specific endpoint with dimension
  ./stability_test.py --url http://localhost:8082 --dimension utxo_count --level 100 --endpoint /account/balance

Available dimensions:
  Address:  utxo_count, token_count, tx_history, tx_token_count
  Block:    block_tx_count, block_body_size, block_era
  Transaction: tx_io_count, tx_has_script, tx_has_tokens

Level types (dimension-specific):
  Power-of-10: 1, 10, 100, 1000, ... (for power-law distributions)
  Percentile:  p50 (median), p75, p90, p95, p99 (for log-normal distributions)
  Era names:   byron, shelley, allegra, mary, alonzo, babbage, conway
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
import statistics
import json
from textwrap import dedent
from typing import Dict, List, Tuple, Optional

###############################################################################
# COMMAND LINE ARGUMENTS
###############################################################################

# Dimension definitions matching populate_data.py
DIMENSIONS = {
    'utxo_count': {
        'description': 'Address UTXO Count',
        'unit': 'UTXOs',
        'endpoints': ['/account/balance', '/account/coins'],
        'type': 'power_of_10',  # R² >= 0.95 (power-law)
        'data_subdir': 'addresses',
    },
    'token_count': {
        'description': 'Address Native Token Count',
        'unit': 'tokens',
        'endpoints': ['/account/balance', '/account/coins'],
        'type': 'power_of_10',  # R² >= 0.95 (power-law)
        'data_subdir': 'addresses',
    },
    'tx_history': {
        'description': 'Address Transaction History',
        'unit': 'transactions',
        'endpoints': ['/search/transactions'],
        'type': 'power_of_10',  # R² >= 0.95 (power-law)
        'data_subdir': 'addresses',
    },
    'block_tx_count': {
        'description': 'Block Transaction Count',
        'unit': 'transactions',
        'endpoints': ['/block'],
        'type': 'power_of_10',  # R² >= 0.95 (power-law)
        'data_subdir': 'blocks',
    },
    'block_body_size': {
        'description': 'Block Body Size',
        'unit': 'bytes',
        'endpoints': ['/block'],
        'type': 'percentile',  # R² = 0.76 (log-normal)
        'data_subdir': 'blocks',
    },
    'block_era': {
        'description': 'Block Era (Age)',
        'unit': 'era',
        'endpoints': ['/block'],
        'type': 'era',  # Categorical - era names from database
        'data_subdir': 'blocks',
    },
    'tx_io_count': {
        'description': 'Transaction I/O Count',
        'unit': 'inputs+outputs',
        'endpoints': ['/block/transaction', '/search/transactions'],
        'type': 'percentile',  # R² < 0.95 (not power-law)
        'data_subdir': 'transactions',
    },
    'tx_has_script': {
        'description': 'Transaction Has Plutus Script',
        'unit': 'boolean',
        'endpoints': ['/block/transaction', '/search/transactions'],
        'type': 'boolean',
        'data_subdir': 'transactions',
    },
    'tx_token_count': {
        'description': 'Transaction Token Types',
        'unit': 'token types',
        'endpoints': ['/block/transaction', '/search/transactions'],
        'type': 'power_of_10',  # R² = 0.98 (power-law)
        'data_subdir': 'transactions',
    },
    'tx_has_tokens': {
        'description': 'Transaction Has Native Tokens',
        'unit': 'boolean',
        'endpoints': ['/block/transaction', '/search/transactions'],
        'type': 'boolean',
        'data_subdir': 'transactions',
    },
}

PERCENTILE_LEVELS = ['p50', 'p75', 'p90', 'p95', 'p99']
QUARTILE_LEVELS = ['q1', 'q2', 'q3', 'q4']


def parse_args():
    """Parse command line arguments."""
    parser = argparse.ArgumentParser(
        description='Cardano Rosetta API Capacity Testing Tool (Dimension-Isolated)',
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""
Examples:
  # Test specific dimension at specific level
  %(prog)s --url http://localhost:8082 --dimension utxo_count --level p95

  # Test all levels of a dimension
  %(prog)s --url http://localhost:8082 --dimension utxo_count --level all

  # Test multiple dimensions (format: dim1:level1,level2;dim2:level3)
  %(prog)s --url http://localhost:8082 --dimensions "utxo_count:p50,p95;block_tx_count:p90"

  # Test all dimensions at all levels
  %(prog)s --url http://localhost:8082 --dimension all --level all

Available dimensions:
  utxo_count      - Address UTXO count (for /account/balance, /account/coins)
  token_count     - Address native token count (for /account/balance, /account/coins)
  tx_history      - Address transaction history (for /search/transactions)
  block_tx_count  - Block transaction count (for /block)
  block_body_size - Block body size (for /block)
  block_era       - Block era/age (for /block)
  tx_io_count     - Transaction I/O count (for /block/transaction, /search/transactions)
  tx_has_script   - Transaction has Plutus script (for /block/transaction)
  tx_has_tokens   - Transaction has native tokens (for /block/transaction)
"""
    )

    # Basic configuration options
    parser.add_argument('--url', dest='base_url', default="http://127.0.0.1:8082",
                        help='Base URL for the Rosetta API service')
    parser.add_argument('--release', dest='release_version', default="2.0.0",
                        help='Release version for reporting')

    # Hardware profile options
    parser.add_argument('--hardware-profile', dest='hardware_profile', default="entry_level",
                        help='Hardware profile ID for reporting')
    parser.add_argument('--machine-specs', dest='machine_specs',
                        default="16 cores, 16 threads, 125GB RAM, 3.9TB NVMe, QEMU Virtual CPU v2.5+",
                        help='Detailed machine specifications for reporting')

    # Dimension-based data selection (NEW - replaces --data-profile)
    dim_group = parser.add_argument_group('Dimension Selection (required)')
    dim_group.add_argument('--dimension', dest='dimension',
                           help='Single dimension to test (e.g., utxo_count, block_tx_count). Use "all" for all dimensions.')
    dim_group.add_argument('--level', dest='level',
                           help='Level(s) to test: p50/p75/p90/p95/p99 for percentiles, q1/q2/q3/q4 for era. Use "all" for all levels.')
    dim_group.add_argument('--dimensions', dest='dimensions_spec',
                           help='Multi-dimension spec: "dim1:level1,level2;dim2:level3" (overrides --dimension/--level)')

    # Network selection
    parser.add_argument('--network', dest='network', default="mainnet",
                        choices=['mainnet', 'preprod', 'preview'],
                        help='Network identifier for API requests and data files')

    # Test configuration options
    parser.add_argument('--concurrency', dest='concurrencies', type=lambda s: [int(item) for item in s.split(',')],
                        default=[1, 2, 4, 8, 12, 16, 20, 24, 28, 32, 36, 40, 44, 48, 52, 56, 60, 64, 68, 72, 76, 80, 84, 88, 92, 96, 100, 125, 150, 175, 200, 225, 250, 275, 300, 325, 350, 375, 400, 425, 450, 475, 500],
                        help='Comma-separated list of concurrency levels for linear search')
    parser.add_argument('--duration', dest='test_duration', type=int, default=60,
                        help='Duration in seconds for each concurrency level test')
    parser.add_argument('--sla', dest='sla_threshold', type=int, default=1000,
                        help='SLA threshold in milliseconds')
    parser.add_argument('--error-threshold', dest='error_threshold', type=float, default=1.0,
                        help='Threshold for non-2xx errors (percentage, e.g., 1.0 means 1%%)')

    # Data rotation options
    parser.add_argument('--rotate-data', dest='rotate_data', action='store_true',
                        help='Test with all CSV rows and aggregate results (provides data diversity)')
    parser.add_argument('--row-duration', dest='row_duration', type=int, default=30,
                        help='Duration in seconds per row when using --rotate-data')
    parser.add_argument('--max-rows', dest='max_rows', type=int, default=None,
                        help='Maximum number of rows to test when using --rotate-data')

    # Search strategy options
    parser.add_argument('--search-strategy', dest='search_strategy', default='exponential',
                        choices=['linear', 'exponential'],
                        help='Strategy: exponential (fast, O(log n)) or linear (test all levels)')
    parser.add_argument('--max-concurrency', dest='max_concurrency', type=int, default=2048,
                        help='Maximum concurrency level for exponential search')

    # Misc options
    parser.add_argument('-v', '--verbose', dest='verbose', action='store_true',
                        help='Enable verbose output')
    parser.add_argument('--cooldown', dest='cooldown', type=int, default=60,
                        help='Cooldown period in seconds between tests')
    parser.add_argument('--max-retries', dest='max_retries', type=int, default=2,
                        help='Maximum number of retries when an ab command fails')

    # Endpoint selection (optional - defaults to dimension-specific endpoints)
    parser.add_argument('--endpoint', dest='selected_endpoint', type=str,
                        help='Specific endpoint to test (e.g., /account/balance). Defaults to dimension-appropriate endpoints.')

    # List options
    parser.add_argument('--list-dimensions', dest='list_dimensions', action='store_true',
                        help='List all available dimensions and exit')
    parser.add_argument('--list-endpoints', dest='list_endpoints', action='store_true',
                        help='List all available endpoints and exit')

    return parser.parse_args()

###############################################################################
# CONFIGURATION
###############################################################################

args = parse_args()

BASE_URL = args.base_url
SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
DATA_DIR = os.path.join(SCRIPT_DIR, "data")

RELEASE_VERSION = args.release_version
HARDWARE_PROFILE = args.hardware_profile
MACHINE_SPECS = args.machine_specs
CONCURRENCIES = args.concurrencies
TEST_DURATION = args.test_duration
SLA_THRESHOLD = args.sla_threshold
ERROR_THRESHOLD = args.error_threshold
VERBOSE = args.verbose
COOLDOWN_PERIOD = args.cooldown
MAX_RETRIES = args.max_retries
NETWORK_ID = args.network
ROTATE_DATA = args.rotate_data
ROW_DURATION = args.row_duration
MAX_ROWS = args.max_rows
SEARCH_STRATEGY = args.search_strategy
MAX_CONCURRENCY = args.max_concurrency


# Global logger variable
logger = None

# Dimensions metadata loaded from dimensions.json
DIMENSIONS_METADATA: Optional[Dict] = None


###############################################################################
# DIMENSION DATA LOADING
###############################################################################

def load_dimensions_metadata(network: str) -> Optional[Dict]:
    """Load dimensions.json with thresholds and friendly names."""
    dimensions_file = os.path.join(DATA_DIR, network, "dimensions.json")
    if not os.path.exists(dimensions_file):
        return None
    with open(dimensions_file, 'r') as f:
        return json.load(f)


def get_levels_for_dimension(dimension: str, metadata: Optional[Dict] = None) -> List[str]:
    """Get appropriate levels for a dimension type.

    If metadata is provided, reads actual levels from dimensions.json.
    Otherwise, falls back to default levels based on type.
    """
    dim_config = DIMENSIONS.get(dimension)
    if not dim_config:
        return []

    # Try to get levels from metadata first
    if metadata and 'dimensions' in metadata:
        dim_meta = metadata.get('dimensions', {}).get(dimension, {})
        if 'thresholds' in dim_meta:
            levels = list(dim_meta['thresholds'].keys())
            if levels:
                return levels

    # Fallback to defaults based on type
    dim_type = dim_config['type']
    if dim_type == 'power_of_10':
        # Default power-of-10 levels (will be overridden by metadata)
        return ['1', '10', '100', '1000', '10000']
    elif dim_type == 'percentile':
        return PERCENTILE_LEVELS
    elif dim_type == 'era':
        # Era names from database - should come from metadata
        return ['byron', 'shelley', 'allegra', 'mary', 'alonzo', 'babbage', 'conway']
    elif dim_type == 'quartile':
        return QUARTILE_LEVELS
    elif dim_type == 'boolean':
        return ['true']  # Boolean dimensions have a single "true" level
    return []


def get_friendly_display(dimension: str, level: str, metadata: Optional[Dict]) -> str:
    """Get friendly display string for a dimension/level combination."""
    if not metadata or 'dimensions' not in metadata:
        return level

    dim_meta = metadata.get('dimensions', {}).get(dimension, {})
    thresholds = dim_meta.get('thresholds', {})

    if level in thresholds:
        return thresholds[level].get('display', level)
    return level


def get_csv_file_path(network: str, dimension: str, level: str) -> str:
    """Get the path to the CSV file for a dimension/level combination."""
    dim_config = DIMENSIONS.get(dimension)
    if not dim_config:
        raise ValueError(f"Unknown dimension: {dimension}")

    subdir = dim_config['data_subdir']

    # Boolean dimensions use "{dimension}_true.csv"
    if dim_config['type'] == 'boolean':
        filename = f"{dimension}_true.csv"
    else:
        filename = f"{dimension}_{level}.csv"

    return os.path.join(DATA_DIR, network, subdir, filename)


def parse_dimensions_spec(spec: str, metadata: Optional[Dict] = None) -> List[Tuple[str, str]]:
    """
    Parse multi-dimension spec string.
    Format: "dim1:level1,level2;dim2:level3,level4"
    Returns list of (dimension, level) tuples.
    """
    result = []
    for dim_spec in spec.split(';'):
        dim_spec = dim_spec.strip()
        if ':' in dim_spec:
            dim_name, levels_str = dim_spec.split(':', 1)
            dim_name = dim_name.strip()
            for level in levels_str.split(','):
                level = level.strip()
                if level:
                    result.append((dim_name, level))
        else:
            # Just dimension name, use all levels
            dim_name = dim_spec
            for level in get_levels_for_dimension(dim_name, metadata):
                result.append((dim_name, level))
    return result


def get_test_combinations(args) -> List[Tuple[str, str]]:
    """
    Get list of (dimension, level) combinations to test based on CLI arguments.
    """
    # Multi-dimension spec takes precedence
    if args.dimensions_spec:
        return parse_dimensions_spec(args.dimensions_spec, DIMENSIONS_METADATA)

    # Single dimension + level
    if args.dimension:
        dimensions_to_test = []

        # Handle "all" dimensions
        if args.dimension == 'all':
            dimensions_to_test = list(DIMENSIONS.keys())
        else:
            dimensions_to_test = [args.dimension]

        # Validate dimensions
        for dim in dimensions_to_test:
            if dim not in DIMENSIONS:
                print(f"Error: Unknown dimension '{dim}'")
                print(f"Available dimensions: {', '.join(DIMENSIONS.keys())}")
                sys.exit(1)

        result = []
        for dim in dimensions_to_test:
            # Handle "all" levels
            if args.level == 'all':
                levels = get_levels_for_dimension(dim, DIMENSIONS_METADATA)
            elif args.level:
                levels = [args.level]
            else:
                # Default to all levels if not specified
                levels = get_levels_for_dimension(dim, DIMENSIONS_METADATA)

            for level in levels:
                # Validate level for dimension type
                valid_levels = get_levels_for_dimension(dim, DIMENSIONS_METADATA)
                if level not in valid_levels:
                    print(f"Warning: Level '{level}' not valid for dimension '{dim}' (type: {DIMENSIONS[dim]['type']})")
                    print(f"Valid levels: {', '.join(valid_levels)}")
                    continue
                result.append((dim, level))

        return result

    return []


def get_endpoints_for_dimension(dimension: str, selected_endpoint: Optional[str]) -> List[str]:
    """Get endpoints to test for a dimension."""
    dim_config = DIMENSIONS.get(dimension)
    if not dim_config:
        return []

    if selected_endpoint:
        # Validate that selected endpoint is valid for this dimension
        if selected_endpoint in dim_config['endpoints']:
            return [selected_endpoint]
        else:
            print(f"Warning: Endpoint '{selected_endpoint}' not valid for dimension '{dimension}'")
            print(f"Valid endpoints: {', '.join(dim_config['endpoints'])}")
            return dim_config['endpoints']

    return dim_config['endpoints']

###############################################################################
# FILES AND DIRECTORIES
###############################################################################

# Create a unique output directory based on date and version
def create_output_dir(dimension_info: str = ""):
    """Create a unique output directory for test results."""
    now = datetime.datetime.now()
    today = now.strftime("%Y-%m-%d")
    time_str = now.strftime("%H-%M")  # Add time in HH-MM format
    random_suffix = ''.join(random.choices(string.ascii_lowercase + string.digits, k=6))

    # Include dimension info in directory name if provided
    if dimension_info:
        dir_name = f"testresults_{today}_{time_str}_{RELEASE_VERSION}_{dimension_info}_{random_suffix}"
    else:
        dir_name = f"testresults_{today}_{time_str}_{RELEASE_VERSION}_{random_suffix}"

    # Create the directory inside the load-tests folder
    full_path = os.path.join(SCRIPT_DIR, dir_name)
    os.makedirs(full_path, exist_ok=True)
    return full_path

# OUTPUT_DIR and file paths will be set in main() after parsing arguments
OUTPUT_DIR = None
DETAILS_FILE = None
SUMMARY_FILE = None
DETAILS_MD_FILE = None
SUMMARY_MD_FILE = None
COMMANDS_FILE = None


def setup_output_files(output_dir: str):
    """Set up global file paths after OUTPUT_DIR is created."""
    global DETAILS_FILE, SUMMARY_FILE, DETAILS_MD_FILE, SUMMARY_MD_FILE, COMMANDS_FILE
    DETAILS_FILE = os.path.join(output_dir, "details_results.csv")
    SUMMARY_FILE = os.path.join(output_dir, "summary_results.csv")
    DETAILS_MD_FILE = os.path.join(output_dir, "details_results.md")
    SUMMARY_MD_FILE = os.path.join(output_dir, "summary_results.md")
    COMMANDS_FILE = os.path.join(output_dir, "ab_commands.log")

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
    Returns a tuple with various metrics.

    When p95/p99 can't be parsed (insufficient requests), uses mean_time as fallback
    since with few requests mean ≈ p95 ≈ p99.
    """
    p95 = None
    p99 = None
    complete_requests = 0
    requests_per_sec = 0.0
    mean_time = 0.0
    non_2xx_responses = 0

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
        # Parse Non-2xx responses
        elif "Non-2xx responses:" in line:
            parts = line.split()
            if len(parts) >= 3:
                non_2xx_responses = int(parts[2])

    # Fallback: use mean_time when percentiles can't be calculated
    # (happens when ab has too few requests for percentile distribution)
    if p95 is None:
        p95 = int(mean_time) if mean_time > 0 else 0
    if p99 is None:
        p99 = int(mean_time) if mean_time > 0 else 0

    return p95, p99, complete_requests, requests_per_sec, mean_time, non_2xx_responses


###############################################################################
# EXPONENTIAL + BINARY SEARCH STRATEGY
###############################################################################

def generate_exponential_levels(max_level):
    """
    Generate exponential concurrency levels: 1, 2, 4, 8, 16, 32, 64, 128, 256, 512...
    Stops at max_level.
    """
    levels = []
    level = 1
    while level <= max_level:
        levels.append(level)
        level *= 2
    return levels


def run_ab_test_for_search(endpoint_path, concurrency, json_file, endpoint_name, duration=None):
    """
    Run a single ab test and return (passes_sla, metrics).
    Used by the search strategy to test if a concurrency level passes.

    Args:
        duration: Test duration in seconds. If None, uses TEST_DURATION.

    Returns: (passes_sla, p95, p99, complete_requests, requests_per_sec, mean_time, non_2xx_responses, error_rate)
    """
    if duration is None:
        duration = TEST_DURATION

    logger.debug(f"Testing concurrency {concurrency} for {endpoint_name} ({duration}s)...")

    cmd = [
        "ab",
        "-t", str(duration),
        "-c", str(concurrency),
        "-p", json_file,
        "-T", "application/json",
        f"{BASE_URL}{endpoint_path}"
    ]
    log_command(cmd, endpoint_name, concurrency)

    max_retries = MAX_RETRIES
    retry_count = 0
    ab_success = False
    ab_output = ""

    while retry_count <= max_retries and not ab_success:
        try:
            proc = subprocess.Popen(cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True, bufsize=1)
            ab_output_lines = []

            if VERBOSE:
                box_width = 80
                logger.debug("┌" + "─" * (box_width - 2) + "┐")
                logger.debug(f"│ AB OUTPUT (concurrency={concurrency})" + " " * (box_width - 32 - len(str(concurrency))) + "│")
                logger.debug("├" + "─" * (box_width - 2) + "┤")

            for line in iter(proc.stdout.readline, ''):
                line_stripped = line.strip()
                ab_output_lines.append(line)
                if VERBOSE and line_stripped:
                    max_content_width = box_width - 4
                    if len(line_stripped) > max_content_width:
                        line_stripped = line_stripped[:max_content_width - 3] + "..."
                    content = "│ " + line_stripped
                    padding = " " * (box_width - len(content) - 1)
                    logger.debug(content + padding + "│")

            proc.stdout.close()
            proc.stderr.close()
            return_code = proc.wait()

            if VERBOSE:
                logger.debug("└" + "─" * (box_width - 2) + "┘")

            if return_code != 0:
                retry_count += 1
                if retry_count <= max_retries:
                    logger.info(f"ab failed, retrying in {COOLDOWN_PERIOD}s... (attempt {retry_count}/{max_retries})")
                    time.sleep(COOLDOWN_PERIOD)
                    continue
                else:
                    logger.warning(f"Max retries reached for concurrency {concurrency}")
                    return (False, 0, 0, 0, 0.0, 0.0, 0, 0.0)
            else:
                ab_success = True
                ab_output = "".join(ab_output_lines)

        except FileNotFoundError:
            logger.error("'ab' command not found. Please ensure ApacheBench is installed.")
            sys.exit(1)
        except Exception as e:
            retry_count += 1
            if retry_count <= max_retries:
                logger.info(f"Error occurred, retrying... (attempt {retry_count}/{max_retries})")
                time.sleep(COOLDOWN_PERIOD)
                continue
            else:
                logger.error(f"Max retries reached due to error: {e}")
                return (False, 0, 0, 0, 0.0, 0.0, 0, 0.0)

    if not ab_success:
        return (False, 0, 0, 0, 0.0, 0.0, 0, 0.0)

    # Parse results
    p95, p99, complete_requests, requests_per_sec, mean_time, non_2xx_responses = parse_ab_output(ab_output)

    # Calculate error rate
    error_rate = 0.0
    if complete_requests > 0:
        error_rate = (non_2xx_responses / complete_requests) * 100

    # Check SLA and error threshold
    meets_sla = (p95 < SLA_THRESHOLD) and (p99 < SLA_THRESHOLD)
    meets_error_threshold = error_rate <= ERROR_THRESHOLD
    passes = meets_sla and meets_error_threshold

    logger.debug(f"  Concurrency {concurrency}: p95={p95}ms, p99={p99}ms, error_rate={error_rate:.2f}%, passes={passes}")

    return (passes, p95, p99, complete_requests, requests_per_sec, mean_time, non_2xx_responses, error_rate)


def exponential_binary_search(endpoint_name, endpoint_path, json_file, max_concurrency):
    """
    Find maximum concurrency using exponential + binary search.

    Phase 1 (Exponential): Test 1, 2, 4, 8, 16... until failure
    Phase 2 (Binary): Binary search between last pass and first fail

    Returns: (max_sla_concurrency, p95, p99, non_2xx, error_rate, requests_per_sec, all_results)
    where all_results is a list of test results for logging.
    """
    all_results = []

    # Phase 1: Exponential search to find upper bound
    logger.info(f"Phase 1: Exponential search (max={max_concurrency})")

    exp_levels = generate_exponential_levels(max_concurrency)
    last_pass_conc = 0
    last_pass_metrics = (0, 0, 0, 0.0, 0.0, 0, 0.0)  # p95, p99, reqs, rps, mean, non2xx, err_rate
    first_fail_conc = None

    for c in exp_levels:
        passes, p95, p99, complete_reqs, rps, mean_time, non_2xx, err_rate = run_ab_test_for_search(
            endpoint_path, c, json_file, endpoint_name
        )

        all_results.append({
            "concurrency": c,
            "p95": p95,
            "p99": p99,
            "complete_requests": complete_reqs,
            "requests_per_sec": rps,
            "mean_time": mean_time,
            "non_2xx_responses": non_2xx,
            "error_rate": err_rate,
            "passes": passes
        })

        if passes:
            last_pass_conc = c
            last_pass_metrics = (p95, p99, complete_reqs, rps, mean_time, non_2xx, err_rate)
            logger.info(f"  ✓ Concurrency {c}: PASS (p95={p95}ms, p99={p99}ms, rps={rps:.2f}, error_rate={err_rate:.2f}%)")
        else:
            first_fail_conc = c
            logger.info(f"  ✗ Concurrency {c}: FAIL (p95={p95}ms, p99={p99}ms, rps={rps:.2f}, error_rate={err_rate:.2f}%)")
            break

    # If all levels passed, return the highest level
    if first_fail_conc is None:
        logger.info(f"All exponential levels passed! Max: {last_pass_conc}")
        p95, p99, reqs, rps, mean, non_2xx, err = last_pass_metrics
        return last_pass_conc, p95, p99, non_2xx, err, rps, all_results

    # If first level failed, return 1 with actual metrics (not 0)
    # We DID test concurrency 1, so report it even though SLA failed
    if last_pass_conc == 0:
        logger.info("First concurrency level (1) failed SLA. Reporting actual metrics.")
        return 1, p95, p99, non_2xx, err_rate, rps, all_results

    # Phase 2: Binary search between last_pass_conc and first_fail_conc
    logger.info(f"Phase 2: Binary search between {last_pass_conc} and {first_fail_conc}")

    low = last_pass_conc
    high = first_fail_conc
    best_conc = last_pass_conc
    best_metrics = last_pass_metrics

    while high - low > 1:
        mid = (low + high) // 2
        logger.info(f"  Testing midpoint: {mid} (range: {low}-{high})")

        passes, p95, p99, complete_reqs, rps, mean_time, non_2xx, err_rate = run_ab_test_for_search(
            endpoint_path, mid, json_file, endpoint_name
        )

        all_results.append({
            "concurrency": mid,
            "p95": p95,
            "p99": p99,
            "complete_requests": complete_reqs,
            "requests_per_sec": rps,
            "mean_time": mean_time,
            "non_2xx_responses": non_2xx,
            "error_rate": err_rate,
            "passes": passes
        })

        if passes:
            low = mid
            best_conc = mid
            best_metrics = (p95, p99, complete_reqs, rps, mean_time, non_2xx, err_rate)
            logger.info(f"  ✓ Concurrency {mid}: PASS (p95={p95}ms, p99={p99}ms, rps={rps:.2f}, error_rate={err_rate:.2f}%)")
        else:
            high = mid
            logger.info(f"  ✗ Concurrency {mid}: FAIL (p95={p95}ms, p99={p99}ms, rps={rps:.2f}, error_rate={err_rate:.2f}%)")

    p95, p99, reqs, rps, mean, non_2xx, err = best_metrics
    logger.info(f"Binary search complete. Max concurrency: {best_conc}")
    return best_conc, p95, p99, non_2xx, err, rps, all_results


def test_endpoint_exponential(endpoint_name, endpoint_path, payload_func, csv_row: Dict[str, str], dimension: str = '', level: str = '', friendly_display: str = ''):
    """
    Test endpoint using exponential + binary search strategy.
    Much faster than linear search: O(log n) instead of O(n).

    Args:
        csv_row: Dict with CSV column names as keys (e.g., {'address': '...', 'block_index': '...'})
        dimension: Dimension name (e.g., 'utxo_count')
        level: Level within dimension (e.g., '1000', 'p95')
        friendly_display: Human-readable display (e.g., '1000-9999 UTXOs')

    Returns (max_sla_concurrency, p95, p99, non_2xx, error_rate, requests_per_sec)
    """
    # Generate JSON payload - payload functions now accept a dict
    json_payload = payload_func(csv_row)

    # Write the payload to a temp file
    endpoint_safe_name = endpoint_name.replace(' ', '_').replace('/', '_').lstrip('_').replace('/', '_').lstrip('_')
    tmp_file = os.path.join(OUTPUT_DIR, f"{endpoint_safe_name}.json")
    with open(tmp_file, "w") as f:
        f.write(json_payload)

    # Run exponential + binary search
    max_conc, p95, p99, non_2xx, error_rate, rps, all_results = exponential_binary_search(
        endpoint_name, endpoint_path, tmp_file, MAX_CONCURRENCY
    )

    # Record all results to details
    for result in all_results:
        c = result["concurrency"]
        meets_sla = (result["p95"] < SLA_THRESHOLD) and (result["p99"] < SLA_THRESHOLD)
        meets_error = result["error_rate"] <= ERROR_THRESHOLD

        details_results.append({
            "dimension": dimension,
            "level": level,
            "friendly_display": friendly_display,
            "endpoint": endpoint_path,
            "concurrency": c,
            "p95": result["p95"],
            "p99": result["p99"],
            "meets_sla": "Yes" if meets_sla else "No",
            "complete_requests": result["complete_requests"],
            "requests_per_sec": result["requests_per_sec"],
            "mean_time": result["mean_time"],
            "non_2xx_responses": result["non_2xx_responses"],
            "error_rate": result["error_rate"],
            "meets_error_threshold": "Yes" if meets_error else "No"
        })

        # Write to CSV
        with open(DETAILS_FILE, 'a', newline='') as f:
            writer = csv.writer(f)
            writer.writerow([
                HARDWARE_PROFILE,
                MACHINE_SPECS,
                "N/A",  # TODO: CSV output format needs refactor for dimension isolation
                endpoint_path,
                c,
                f"{result['p95']}ms",
                f"{result['p99']}ms",
                "Yes" if meets_sla else "No",
                result["complete_requests"],
                result["requests_per_sec"],
                f"{result['mean_time']}ms",
                result["non_2xx_responses"],
                f"{result['error_rate']:.2f}%",
                "Yes" if meets_error else "No"
            ])

    tests_count = len(all_results)
    logger.info(f"Exponential search completed in {tests_count} tests (vs ~43 with linear)")

    return max_conc, p95, p99, non_2xx, error_rate, rps


def test_concurrency_with_all_rows(endpoint_name, endpoint_path, concurrency, payload_files):
    """
    Test a specific concurrency level with all data rows and return aggregated results.

    IMPORTANT: All rows are included in the average calculation, regardless of individual
    pass/fail status. This ensures metrics accurately represent the full data diversity.

    Returns: (passes, avg_p95, avg_p99, total_requests, avg_rps, avg_mean_time, total_non_2xx, avg_error_rate, failed_rows, max_p95, max_p99, std_p95, std_p99)
    """
    all_p95 = []
    all_p99 = []
    all_mean_times = []
    total_requests = 0
    total_non_2xx = 0
    sum_rps = 0.0
    successful_runs = 0  # ab command executed successfully (not SLA pass)
    failed_rows = 0  # rows that failed SLA (but ab ran successfully)

    for row_idx, json_file in enumerate(payload_files):
        logger.debug(f"  Row {row_idx + 1}/{len(payload_files)}: testing concurrency {concurrency} ({ROW_DURATION}s)...")

        row_passes, p95, p99, complete_reqs, rps, mean_time, non_2xx, err_rate = run_ab_test_for_search(
            endpoint_path, concurrency, json_file, endpoint_name, duration=ROW_DURATION
        )

        # Include ALL rows in metrics, regardless of SLA pass/fail
        # Only skip if ab command itself failed (complete_reqs == 0)
        if complete_reqs > 0:
            all_p95.append(p95)
            all_p99.append(p99)
            all_mean_times.append(mean_time)
            total_requests += complete_reqs
            total_non_2xx += non_2xx
            sum_rps += rps
            successful_runs += 1

            if not row_passes:
                failed_rows += 1

    if successful_runs == 0:
        return (False, 0, 0, 0, 0.0, 0.0, 0, 100.0, 0, 0, 0, 0, 0)

    # Calculate aggregated metrics from ALL rows (not just passing ones!)
    avg_p95 = sum(all_p95) / len(all_p95) if all_p95 else 0
    avg_p99 = sum(all_p99) / len(all_p99) if all_p99 else 0
    max_p95 = max(all_p95) if all_p95 else 0
    max_p99 = max(all_p99) if all_p99 else 0
    # Standard deviation (requires at least 2 data points)
    std_p95 = statistics.stdev(all_p95) if len(all_p95) >= 2 else 0
    std_p99 = statistics.stdev(all_p99) if len(all_p99) >= 2 else 0
    avg_rps = sum_rps / successful_runs
    avg_mean_time = sum(all_mean_times) / len(all_mean_times) if all_mean_times else 0
    avg_error_rate = (total_non_2xx / total_requests) * 100 if total_requests > 0 else 0

    # Check SLA and error threshold based on AVERAGE metrics
    meets_sla = (avg_p95 < SLA_THRESHOLD) and (avg_p99 < SLA_THRESHOLD)
    meets_error_threshold = avg_error_rate <= ERROR_THRESHOLD
    passes = meets_sla and meets_error_threshold

    return (passes, int(avg_p95), int(avg_p99), total_requests, avg_rps, avg_mean_time, total_non_2xx, avg_error_rate, failed_rows, int(max_p95), int(max_p99), int(std_p95), int(std_p99))


def exponential_binary_search_with_rotation(endpoint_name, endpoint_path, payload_files, max_concurrency):
    """
    Find maximum concurrency using exponential + binary search with data rotation.
    At each concurrency level, tests ALL data rows and aggregates results.

    Returns: (max_sla_concurrency, p95, p99, non_2xx, error_rate, requests_per_sec, all_results)
    """
    all_results = []
    num_rows = len(payload_files)

    logger.info(f"Phase 1: Exponential search with {num_rows} data rows (max={max_concurrency})")

    exp_levels = generate_exponential_levels(max_concurrency)
    last_pass_conc = 0
    last_pass_metrics = (0, 0, 0, 0.0, 0.0, 0, 0.0)  # p95, p99, reqs, rps, mean_time, non2xx, err_rate
    first_fail_conc = None

    for c in exp_levels:
        logger.info(f"  Testing concurrency {c} with {num_rows} data rows...")

        passes, avg_p95, avg_p99, total_reqs, avg_rps, avg_mean_time, total_non_2xx, avg_err_rate, failed_rows, max_p95, max_p99, std_p95, std_p99 = test_concurrency_with_all_rows(
            endpoint_name, endpoint_path, c, payload_files
        )

        all_results.append({
            "concurrency": c,
            "p95": avg_p95,
            "p99": avg_p99,
            "std_p95": std_p95,
            "std_p99": std_p99,
            "max_p95": max_p95,
            "max_p99": max_p99,
            "complete_requests": total_reqs,
            "requests_per_sec": avg_rps,
            "mean_time": avg_mean_time,
            "non_2xx_responses": total_non_2xx,
            "error_rate": avg_err_rate,
            "failed_rows": failed_rows,
            "passes": passes
        })

        # Build detailed status message
        failed_info = f", failed_rows={failed_rows}/{num_rows}" if failed_rows > 0 else ""
        std_info = f"±{std_p95}" if std_p95 > 0 else ""

        if passes:
            last_pass_conc = c
            last_pass_metrics = (avg_p95, avg_p99, total_reqs, avg_rps, avg_mean_time, total_non_2xx, avg_err_rate)
            logger.info(f"  ✓ Concurrency {c}: PASS (p95={avg_p95}{std_info}ms, p99={avg_p99}ms, rps={avg_rps:.2f}, err={avg_err_rate:.2f}%{failed_info})")
        else:
            first_fail_conc = c
            logger.info(f"  ✗ Concurrency {c}: FAIL (p95={avg_p95}{std_info}ms, p99={avg_p99}ms, rps={avg_rps:.2f}, err={avg_err_rate:.2f}%{failed_info})")
            break

    # If all levels passed, return the highest level
    if first_fail_conc is None:
        logger.info(f"All exponential levels passed! Max: {last_pass_conc}")
        p95, p99, reqs, rps, mean_time, non_2xx, err = last_pass_metrics
        return last_pass_conc, p95, p99, non_2xx, err, rps, all_results

    # If first level failed, return 1 with actual metrics (not 0)
    # We DID test concurrency 1, so report it even though SLA failed
    if last_pass_conc == 0:
        logger.info("First concurrency level (1) failed SLA. Reporting actual metrics.")
        return 1, avg_p95, avg_p99, total_non_2xx, avg_err_rate, avg_rps, all_results

    # Phase 2: Binary search between last_pass_conc and first_fail_conc
    logger.info(f"Phase 2: Binary search between {last_pass_conc} and {first_fail_conc}")

    low = last_pass_conc
    high = first_fail_conc
    best_conc = last_pass_conc
    best_metrics = last_pass_metrics

    while high - low > 1:
        mid = (low + high) // 2
        logger.info(f"  Testing midpoint: {mid} (range: {low}-{high}) with {num_rows} data rows...")

        passes, avg_p95, avg_p99, total_reqs, avg_rps, avg_mean_time, total_non_2xx, avg_err_rate, failed_rows, max_p95, max_p99, std_p95, std_p99 = test_concurrency_with_all_rows(
            endpoint_name, endpoint_path, mid, payload_files
        )

        all_results.append({
            "concurrency": mid,
            "p95": avg_p95,
            "p99": avg_p99,
            "std_p95": std_p95,
            "std_p99": std_p99,
            "max_p95": max_p95,
            "max_p99": max_p99,
            "complete_requests": total_reqs,
            "requests_per_sec": avg_rps,
            "mean_time": avg_mean_time,
            "non_2xx_responses": total_non_2xx,
            "error_rate": avg_err_rate,
            "failed_rows": failed_rows,
            "passes": passes
        })

        # Build detailed status message
        failed_info = f", failed_rows={failed_rows}/{num_rows}" if failed_rows > 0 else ""
        std_info = f"±{std_p95}" if std_p95 > 0 else ""

        if passes:
            low = mid
            best_conc = mid
            best_metrics = (avg_p95, avg_p99, total_reqs, avg_rps, avg_mean_time, total_non_2xx, avg_err_rate)
            logger.info(f"  ✓ Concurrency {mid}: PASS (p95={avg_p95}{std_info}ms, p99={avg_p99}ms, rps={avg_rps:.2f}, err={avg_err_rate:.2f}%{failed_info})")
        else:
            high = mid
            logger.info(f"  ✗ Concurrency {mid}: FAIL (p95={avg_p95}{std_info}ms, p99={avg_p99}ms, rps={avg_rps:.2f}, err={avg_err_rate:.2f}%{failed_info})")

    p95, p99, reqs, rps, mean_time, non_2xx, err = best_metrics
    logger.info(f"Binary search complete. Max concurrency: {best_conc}")
    return best_conc, p95, p99, non_2xx, err, rps, all_results


def test_endpoint_exponential_with_rotation(endpoint_name, endpoint_path, payload_func, csv_rows: List[Dict[str, str]], dimension: str = '', level: str = '', friendly_display: str = ''):
    """
    Test endpoint using exponential + binary search with data rotation.
    Combines fast search with data diversity.

    Args:
        csv_rows: List of dicts with CSV column names as keys
        dimension: Dimension name
        level: Level within dimension
        friendly_display: Human-readable display

    Returns (max_sla_concurrency, p95, p99, non_2xx, error_rate, requests_per_sec)
    """
    num_rows = len(csv_rows)
    if MAX_ROWS and num_rows > MAX_ROWS:
        csv_rows = csv_rows[:MAX_ROWS]
        num_rows = MAX_ROWS

    logger.info(f"Exponential+rotation mode: {num_rows} data rows, {ROW_DURATION}s per row")

    # Generate all payload files upfront
    endpoint_safe_name = endpoint_name.replace(' ', '_').replace('/', '_').lstrip('_')
    payload_files = []

    for idx, csv_row in enumerate(csv_rows):
        # csv_row is now a dict - payload functions accept dicts directly
        json_payload = payload_func(csv_row)

        tmp_file = os.path.join(OUTPUT_DIR, f"{endpoint_safe_name}_row{idx}.json")
        with open(tmp_file, "w") as f:
            f.write(json_payload)
        payload_files.append(tmp_file)

    if not payload_files:
        logger.error("No valid payload files generated")
        return 0, 0, 0, 0, 0.0, 0.0

    # Run exponential + binary search with rotation
    max_conc, p95, p99, non_2xx, error_rate, rps, all_results = exponential_binary_search_with_rotation(
        endpoint_name, endpoint_path, payload_files, MAX_CONCURRENCY
    )

    # Record all results to details
    for result in all_results:
        c = result["concurrency"]
        meets_sla = (result["p95"] < SLA_THRESHOLD) and (result["p99"] < SLA_THRESHOLD)
        meets_error = result["error_rate"] <= ERROR_THRESHOLD

        # Format p95/p99 with std if available
        std_p95 = result.get("std_p95", 0)
        std_p99 = result.get("std_p99", 0)
        p95_str = f"{result['p95']}±{std_p95}ms" if std_p95 > 0 else f"{result['p95']}ms"
        p99_str = f"{result['p99']}±{std_p99}ms" if std_p99 > 0 else f"{result['p99']}ms"

        details_results.append({
            "dimension": dimension,
            "level": level,
            "friendly_display": friendly_display,
            "endpoint": f"{endpoint_path} [AGG]",
            "concurrency": c,
            "p95": result["p95"],
            "p99": result["p99"],
            "std_p95": std_p95,
            "std_p99": std_p99,
            "meets_sla": "Yes" if meets_sla else "No",
            "complete_requests": result["complete_requests"],
            "requests_per_sec": result["requests_per_sec"],
            "mean_time": result["mean_time"],
            "non_2xx_responses": result["non_2xx_responses"],
            "error_rate": result["error_rate"],
            "meets_error_threshold": "Yes" if meets_error else "No"
        })

        # Write to CSV
        with open(DETAILS_FILE, 'a', newline='') as f:
            writer = csv.writer(f)
            writer.writerow([
                HARDWARE_PROFILE,
                MACHINE_SPECS,
                "N/A",  # TODO: CSV output format needs refactor for dimension isolation
                f"{endpoint_path} [AGG]",
                c,
                p95_str,
                p99_str,
                "Yes" if meets_sla else "No",
                result["complete_requests"],
                f"{result['requests_per_sec']:.2f}",
                f"{result['mean_time']:.2f}ms",
                result["non_2xx_responses"],
                f"{result['error_rate']:.2f}%",
                "Yes" if meets_error else "No"
            ])

    tests_count = len(all_results)
    logger.info(f"Exponential+rotation search completed in {tests_count} concurrency levels (vs ~43 with linear)")

    return max_conc, p95, p99, non_2xx, error_rate, rps


###############################################################################
# PAYLOAD GENERATORS
###############################################################################
# Each function receives CSV fields in a consistent order, then returns a JSON string.
# Adjust these if your CSV structure is different.

def payload_network_status(data: Dict[str, str]):
    """
    /network/status does not really need CSV data.
    """
    return dedent(f"""\
    {{
      "network_identifier": {{
        "blockchain": "cardano",
        "network": "{NETWORK_ID}"
      }},
      "metadata": {{}}
    }}
    """)

def payload_account_balance(data: Dict[str, str]):
    """
    /account/balance requires an address.
    """
    address = data.get('address', '')
    return dedent(f"""\
    {{
      "network_identifier": {{
        "blockchain": "cardano",
        "network": "{NETWORK_ID}"
      }},
      "account_identifier": {{
        "address": "{address}"
      }}
    }}
    """)

def payload_account_coins(data: Dict[str, str]):
    """
    /account/coins requires an address.
    """
    address = data.get('address', '')
    return dedent(f"""\
    {{
      "network_identifier": {{
        "blockchain": "cardano",
        "network": "{NETWORK_ID}"
      }},
      "account_identifier": {{
        "address": "{address}"
      }},
      "include_mempool": true
    }}
    """)

def payload_block(data: Dict[str, str]):
    """
    /block requires block_index, block_hash
    """
    block_index = data.get('block_index', '0')
    block_hash = data.get('block_hash', '')
    return dedent(f"""\
    {{
      "network_identifier": {{
        "blockchain": "cardano",
        "network": "{NETWORK_ID}"
      }},
      "block_identifier": {{
        "index": {block_index},
        "hash": "{block_hash}"
      }}
    }}
    """)

def payload_block_transaction(data: Dict[str, str]):
    """
    /block/transaction requires block_index, block_hash, transaction_hash
    """
    block_index = data.get('block_index', '0')
    block_hash = data.get('block_hash', '')
    transaction_hash = data.get('transaction_hash', '')
    return dedent(f"""\
    {{
      "network_identifier": {{
        "blockchain": "cardano",
        "network": "{NETWORK_ID}"
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

def payload_search_transactions_by_hash(data: Dict[str, str]):
    """
    /search/transactions requires transaction_hash.
    """
    transaction_hash = data.get('transaction_hash', '')
    return dedent(f"""\
    {{
      "network_identifier": {{
        "blockchain": "cardano",
        "network": "{NETWORK_ID}"
      }},
      "transaction_identifier": {{
        "hash": "{transaction_hash}"
      }}
    }}
    """)

def payload_search_transactions_by_address(data: Dict[str, str]):
    """
    /search/transactions with account_identifier (address-based query).
    Uses the 'address' field from the CSV data.
    With dimension isolation, addresses in tx_history dimension are already
    selected for high transaction history count.
    """
    address = data.get('address', '')
    return dedent(f"""\
    {{
      "network_identifier": {{
        "blockchain": "cardano",
        "network": "{NETWORK_ID}"
      }},
      "account_identifier": {{
        "address": "{address}"
      }}
    }}
    """)

def payload_construction_metadata(data: Dict[str, str]):
    """
    /construction/metadata requires transaction_size, relative_ttl
    """
    transaction_size = data.get('transaction_size', '0')
    relative_ttl = data.get('relative_ttl', '0')
    return dedent(f"""\
    {{
      "network_identifier": {{
        "blockchain": "cardano",
        "network": "{NETWORK_ID}"
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
# We'll define 8 endpoints with: (Name, Path, Payload Generator Function)
ENDPOINTS = [
    ("Network Status",                  "/network/status",          payload_network_status),
    ("Account Balance",                 "/account/balance",         payload_account_balance),
    ("Account Coins",                   "/account/coins",           payload_account_coins),
    ("Block",                           "/block",                   payload_block),
    ("Block Transaction",               "/block/transaction",       payload_block_transaction),
    ("Search Transactions by Hash",     "/search/transactions",     payload_search_transactions_by_hash),
    ("Search Transactions by Address",  "/search/transactions",     payload_search_transactions_by_address),
    ("Construction Metadata",           "/construction/metadata",   payload_construction_metadata),
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
        writer.writerow(["Hardware_Profile", "Machine_Specs", "Data_Profile", "Endpoint", "Concurrency", "p95(ms)", "p99(ms)",
                        "Meets_SLA", "Complete_Requests", "Requests_per_sec", "Mean_Time(ms)",
                        "Non_2xx_Responses", "Error_Rate(%)", "Meets_Error_Threshold"])

    with open(SUMMARY_FILE, 'w', newline='') as f:
        writer = csv.writer(f)
        writer.writerow(["ID", "Release", "Hardware_Profile", "Machine_Specs", "Data_Profile", "Endpoint",
                         "Max_Concurrency", "p95(ms)", "p99(ms)", "Non_2xx_Responses", "Error_Rate(%)", "Requests_per_sec"])
    
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

        # Group by endpoint for better readability
        from collections import defaultdict
        results_by_endpoint = defaultdict(list)
        for record in details_results:
            endpoint = record.get('endpoint', 'Unknown')
            results_by_endpoint[endpoint].append(record)

        for endpoint, records in sorted(results_by_endpoint.items()):
            f.write(f"## {endpoint}\n\n")

            # Write header with proper spacing
            header = "| Dimension | Level | Concurrency | p95 (ms) | p99 (ms) | Meets SLA | Complete Reqs | Reqs/sec | Mean Time (ms) | Non-2xx | Error Rate (%) | Meets Error Threshold |"
            f.write(header + "\n")

            # Write separator with proper width - at least 3 dashes per column
            separator = "| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |"
            f.write(separator + "\n")

            # Table rows
            for record in records:
                row_values = [
                    f"{record.get('dimension', 'N/A')}",
                    f"{record.get('friendly_display', record.get('level', 'N/A'))}",
                    f"{record['concurrency']}",
                    f"{record['p95']}ms",
                    f"{record['p99']}ms",
                    f"{record['meets_sla']}",
                    f"{record['complete_requests']}",
                    f"{record['requests_per_sec']:.2f}",
                    f"{record['mean_time']:.2f}ms",
                    f"{record['non_2xx_responses']}",
                    f"{record['error_rate']:.2f}%",
                    f"{record['meets_error_threshold']}"
                ]
                f.write("| " + " | ".join(row_values) + " |\n")

            f.write("\n")

    # --- Summary markdown table ---
    with open(SUMMARY_MD_FILE, 'w') as f:
        f.write("# Summary Load Test Results\n\n")
        f.write("Maximum concurrency achieved per endpoint\n\n")

        # Write header with proper spacing
        header = "| ID | Release | Dimension | Level | Endpoint | Max Concurrency | p95 (ms) | p99 (ms) | Non-2xx | Error Rate (%) | Reqs/sec |"
        f.write(header + "\n")

        # Write separator with proper width - at least 3 dashes per column
        separator = "| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |"
        f.write(separator + "\n")

        # Table rows
        for sr in summary_results:
            row_values = [
                f"{sr['id']}",
                f"{sr['release']}",
                f"{sr.get('dimension', 'N/A')}",
                f"{sr.get('friendly_display', sr.get('level', 'N/A'))}",
                f"{sr['endpoint']}",
                f"{sr['max_concurrency']}",
                f"{sr['p95']}ms",
                f"{sr['p99']}ms",
                f"{sr['non_2xx_responses']}",
                f"{sr['error_rate']:.2f}%",
                f"{sr['requests_per_sec']:.2f}"
            ]
            f.write("| " + " | ".join(row_values) + " |\n")

###############################################################################
# TEST FUNCTION
###############################################################################

def test_endpoint(endpoint_name, endpoint_path, payload_func, csv_row: Dict[str, str], dimension: str = '', level: str = '', friendly_display: str = ''):
    """
    - Generate payload from csv_row dict
    - Step through concurrency
    - For each concurrency, call `ab`, parse p95/p99, record details, check SLA
    - Return (max_sla_concurrency, p95_at_that_concurrency, p99_at_that_concurrency, non_2xx_at_that_concurrency, error_rate_at_that_concurrency, reqs_per_sec_at_that_concurrency)

    Args:
        csv_row: Dict with CSV column names as keys (e.g., {'address': '...', 'block_index': '...'})
        dimension: Dimension name
        level: Level within dimension
        friendly_display: Human-readable display
    """
    # Generate JSON payload - payload functions now accept a dict
    json_payload = payload_func(csv_row)

    # Write the payload to a temp file in the output directory
    endpoint_safe_name = endpoint_name.replace(' ', '_').replace('/', '_').lstrip('_')
    tmp_file = os.path.join(OUTPUT_DIR, f"{endpoint_safe_name}.json")
    with open(tmp_file, "w") as f:
        f.write(json_payload)

    max_sla_conc = 0
    best_p95 = 0
    best_p99 = 0
    best_non_2xx = 0
    best_error_rate = 0.0
    best_requests_per_sec = 0.0

    # Track last test metrics (for reporting actual values even when tests fail)
    last_p95 = 0
    last_p99 = 0
    last_non_2xx = 0
    last_error_rate = 0.0
    last_requests_per_sec = 0.0

    for c in CONCURRENCIES:
        # Use logger.debug for verbose output
        logger.debug(f"{'-' * 80}")
        logger.debug(f"Endpoint: {endpoint_name}, Concurrency: {c}")
        logger.debug(f"Running ab for {TEST_DURATION} seconds against {endpoint_path}")
        logger.debug(f"{'-' * 80}")
        
        # Generate and log the ab command
        cmd = get_ab_command(endpoint_path, c, tmp_file)
        log_command(cmd, endpoint_name, c)

        max_retries = MAX_RETRIES
        retry_count = 0
        ab_success = False

        while retry_count <= max_retries and not ab_success:
            # Execute ab command using Popen for real-time output in verbose mode
            ab_output_lines = []
            try:
                # Use Popen to start the process
                proc = subprocess.Popen(cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True, bufsize=1)
                
                # Read stdout line by line
                if VERBOSE:
                    box_width = 80
                    logger.debug("┌" + "─" * (box_width - 2) + "┐")
                    logger.debug("│ AB OUTPUT" + " " * (box_width - 12) + "│")
                    logger.debug("├" + "─" * (box_width - 2) + "┤")
                for line in iter(proc.stdout.readline, ''):
                    line_stripped = line.strip()
                    ab_output_lines.append(line)
                    if VERBOSE:
                        # Format each line with box borders
                        if line_stripped:
                            # Truncate long lines to fit box width
                            max_content_width = box_width - 4  # 2 for borders, 2 for padding
                            if len(line_stripped) > max_content_width:
                                line_stripped = line_stripped[:max_content_width - 3] + "..."
                            content = "│ " + line_stripped
                            padding = " " * (box_width - len(content) - 1)
                            logger.debug(content + padding + "│")
                        else:
                            logger.debug("│" + " " * (box_width - 2) + "│")
                proc.stdout.close()

                # Read stderr line by line (only log if verbose, but always capture)
                stderr_lines = []
                for line in iter(proc.stderr.readline, ''):
                    stderr_lines.append(line)
                proc.stderr.close()

                # Wait for the process to complete and get the return code
                return_code = proc.wait()
                
                ab_output = "".join(ab_output_lines)
                ab_stderr = "".join(stderr_lines)

                # Close the box before showing any error messages
                if VERBOSE:
                    logger.debug("└" + "─" * (box_width - 2) + "┘")
                    
                if return_code != 0:
                    # Log error if ab command failed
                    logger.error(f"ab command failed with exit code {return_code} at concurrency {c} for endpoint {endpoint_name}")
                    logger.error(f"Command: {' '.join(cmd)}")
                    if ab_stderr: # Log stderr if it contains anything
                        logger.error(f"Stderr: {ab_stderr.strip()}")
                    
                    # Retry logic
                    retry_count += 1
                    if retry_count <= max_retries:
                        logger.info(f"Retrying in {COOLDOWN_PERIOD} seconds... (attempt {retry_count}/{max_retries})")
                        time.sleep(COOLDOWN_PERIOD)
                        continue  # Try again
                    else:
                        logger.warning(f"Max retries ({max_retries}) reached for {endpoint_name} at concurrency {c}. Moving to next concurrency level.")
                        break  # Stop retrying this concurrency level
                else:
                    ab_success = True  # Command succeeded
                    
            except FileNotFoundError:
                # Make sure to close the box if open
                if VERBOSE:
                    logger.debug("└" + "─" * (box_width - 2) + "┘")
                logger.error("'ab' command not found. Please ensure ApacheBench is installed and in your PATH.")
                sys.exit(1) # Exit script if ab is not found
            except Exception as e:
                # Make sure to close the box if open
                if VERBOSE:
                    logger.debug("└" + "─" * (box_width - 2) + "┘")
                logger.exception(f"An unexpected error occurred while running ab for {endpoint_name} at concurrency {c}: {e}")
                
                # Retry logic
                retry_count += 1
                if retry_count <= max_retries:
                    logger.info(f"Retrying in {COOLDOWN_PERIOD} seconds... (attempt {retry_count}/{max_retries})")
                    time.sleep(COOLDOWN_PERIOD)
                    continue  # Try again
                else:
                    logger.warning(f"Max retries ({max_retries}) reached for {endpoint_name} at concurrency {c} due to exception. Moving to next concurrency level.")
                    break  # Stop retrying this concurrency level

        # If we didn't succeed after all retries, move to the next concurrency level
        if not ab_success:
            break

        # Parse p95, p99 and additional metrics from the captured stdout
        p95, p99, complete_requests, requests_per_sec, mean_time, non_2xx_responses = parse_ab_output(ab_output)

        # Calculate error rate as a percentage
        error_rate = 0.0
        if complete_requests > 0:
            error_rate = (non_2xx_responses / complete_requests) * 100

        # Always track last test metrics (for reporting actual values even when tests fail)
        last_p95 = p95
        last_p99 = p99
        last_non_2xx = non_2xx_responses
        last_error_rate = error_rate
        last_requests_per_sec = requests_per_sec

        # Check both SLA and error threshold
        meets_sla = (p95 < SLA_THRESHOLD) and (p99 < SLA_THRESHOLD)
        meets_error_threshold = error_rate <= ERROR_THRESHOLD
        meets_sla_str = "Yes" if meets_sla else "No"
        meets_error_threshold_str = "Yes" if meets_error_threshold else "No"

        # Store detail record in memory - use endpoint_path instead of endpoint_name
        details_results.append({
            "dimension": dimension,
            "level": level,
            "friendly_display": friendly_display,
            "endpoint": endpoint_path,
            "concurrency": c,
            "p95": p95,
            "p99": p99,
            "meets_sla": meets_sla_str,
            "complete_requests": complete_requests,
            "requests_per_sec": requests_per_sec,
            "mean_time": mean_time,
            "non_2xx_responses": non_2xx_responses,
            "error_rate": error_rate,
            "meets_error_threshold": meets_error_threshold_str
        })
        
        # Write to CSV file - use endpoint_path instead of endpoint_name
        with open(DETAILS_FILE, 'a', newline='') as f:
            writer = csv.writer(f)
            writer.writerow([
                HARDWARE_PROFILE,
                MACHINE_SPECS,
                "N/A",  # TODO: CSV output format needs refactor for dimension isolation
                endpoint_path,
                c,
                f"{p95}ms",
                f"{p99}ms",
                meets_sla_str,
                complete_requests,
                requests_per_sec,
                f"{mean_time}ms",
                non_2xx_responses,
                f"{error_rate:.2f}%",
                meets_error_threshold_str
            ])

        # Use logger.debug for verbose results
        logger.debug(f"Results for {endpoint_name} at concurrency {c}:")
        logger.debug(f"  p95: {p95}ms")
        logger.debug(f"  p99: {p99}ms")
        logger.debug(f"  Meets SLA: {meets_sla_str}")
        logger.debug(f"  Complete Requests: {complete_requests}")
        logger.debug(f"  Requests/sec: {requests_per_sec}")
        logger.debug(f"  Mean Time/Request: {mean_time}ms")
        logger.debug(f"  Non-2xx Responses: {non_2xx_responses}")
        logger.debug(f"  Error Rate: {error_rate:.2f}%")
        logger.debug(f"  Meets Error Threshold: {meets_error_threshold_str}")

        if meets_sla and meets_error_threshold:
            max_sla_conc = c
            best_p95 = p95
            best_p99 = p99
            best_non_2xx = non_2xx_responses
            best_error_rate = error_rate
            best_requests_per_sec = requests_per_sec
        else:
            # Use logger.info for standard flow messages
            if not meets_sla:
                logger.info(f"SLA threshold of {SLA_THRESHOLD}ms exceeded at concurrency {c}. Stopping tests for {endpoint_name}.")
            if not meets_error_threshold:
                logger.info(f"Error threshold of {ERROR_THRESHOLD}% exceeded at concurrency {c} (actual: {error_rate:.2f}%). Stopping tests for {endpoint_name}.")
            break

    # If no concurrency level passed, return last test metrics so we still show actual values
    if max_sla_conc == 0:
        return max_sla_conc, last_p95, last_p99, last_non_2xx, last_error_rate, last_requests_per_sec
    return max_sla_conc, best_p95, best_p99, best_non_2xx, best_error_rate, best_requests_per_sec


def run_single_ab_test(endpoint_path, concurrency, json_file, duration):
    """
    Run a single ab test and return parsed metrics.
    Returns tuple: (success, p95, p99, complete_requests, requests_per_sec, mean_time, non_2xx_responses)
    """
    cmd = [
        "ab",
        "-t", str(duration),
        "-c", str(concurrency),
        "-p", json_file,
        "-T", "application/json",
        f"{BASE_URL}{endpoint_path}"
    ]

    try:
        proc = subprocess.Popen(cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True, bufsize=1)
        ab_output_lines = []
        for line in iter(proc.stdout.readline, ''):
            ab_output_lines.append(line)
        proc.stdout.close()
        proc.stderr.close()
        return_code = proc.wait()

        if return_code != 0:
            return (False, 0, 0, 0, 0.0, 0.0, 0)

        ab_output = "".join(ab_output_lines)
        p95, p99, complete_requests, requests_per_sec, mean_time, non_2xx_responses = parse_ab_output(ab_output)
        return (True, p95, p99, complete_requests, requests_per_sec, mean_time, non_2xx_responses)

    except Exception as e:
        logger.error(f"Error running ab: {e}")
        return (False, 0, 0, 0, 0.0, 0.0, 0)


def test_endpoint_with_rotation(endpoint_name, endpoint_path, payload_func, csv_rows: List[Dict[str, str]], dimension: str = '', level: str = '', friendly_display: str = ''):
    """
    Test endpoint with data rotation - runs ab for each CSV row and aggregates results.

    For each concurrency level:
    - Run ab for each row (with ROW_DURATION)
    - Aggregate: avg p95, avg p99, sum requests, weighted avg error rate
    - Check SLA on aggregated results

    Args:
        csv_rows: List of dicts with CSV column names as keys
        dimension: Dimension name
        level: Level within dimension
        friendly_display: Human-readable display

    Returns (max_sla_concurrency, avg_p95, avg_p99, total_non_2xx, avg_error_rate, total_reqs_per_sec)
    """
    num_rows = len(csv_rows)
    if MAX_ROWS and num_rows > MAX_ROWS:
        csv_rows = csv_rows[:MAX_ROWS]
        num_rows = MAX_ROWS

    logger.info(f"Rotate-data mode: Testing with {num_rows} data rows, {ROW_DURATION}s per row")

    # Generate all payload files upfront
    endpoint_safe_name = endpoint_name.replace(' ', '_').replace('/', '_').lstrip('_')
    payload_files = []

    for idx, csv_row in enumerate(csv_rows):
        # csv_row is now a dict - payload functions accept dicts directly
        json_payload = payload_func(csv_row)

        tmp_file = os.path.join(OUTPUT_DIR, f"{endpoint_safe_name}_row{idx}.json")
        with open(tmp_file, "w") as f:
            f.write(json_payload)
        payload_files.append(tmp_file)

    if not payload_files:
        logger.error("No valid payload files generated")
        return 0, 0, 0, 0, 0.0, 0.0

    max_sla_conc = 0
    best_p95 = 0
    best_p99 = 0
    best_non_2xx = 0
    best_error_rate = 0.0
    best_requests_per_sec = 0.0

    # Track last test metrics
    last_p95 = 0
    last_p99 = 0
    last_non_2xx = 0
    last_error_rate = 0.0
    last_requests_per_sec = 0.0

    for c in CONCURRENCIES:
        logger.info(f"Testing concurrency {c} with {len(payload_files)} data rows...")

        # Collect metrics from all rows
        all_p95 = []
        all_p99 = []
        all_mean_times = []
        total_requests = 0
        total_non_2xx = 0
        sum_requests_per_sec = 0.0
        successful_runs = 0

        for row_idx, json_file in enumerate(payload_files):
            logger.debug(f"  Row {row_idx + 1}/{len(payload_files)}: running ab for {ROW_DURATION}s...")

            success, p95, p99, complete_requests, requests_per_sec, mean_time, non_2xx = run_single_ab_test(
                endpoint_path, c, json_file, ROW_DURATION
            )

            if success and complete_requests > 0:
                all_p95.append(p95)
                all_p99.append(p99)
                all_mean_times.append(mean_time)
                total_requests += complete_requests
                total_non_2xx += non_2xx
                sum_requests_per_sec += requests_per_sec
                successful_runs += 1

                # Store individual row result in details
                error_rate = (non_2xx / complete_requests) * 100 if complete_requests > 0 else 0
                meets_sla = (p95 < SLA_THRESHOLD) and (p99 < SLA_THRESHOLD)
                meets_error_threshold = error_rate <= ERROR_THRESHOLD

                details_results.append({
                    "dimension": dimension,
                    "level": level,
                    "friendly_display": friendly_display,
                    "endpoint": f"{endpoint_path} [row {row_idx + 1}]",
                    "concurrency": c,
                    "p95": p95,
                    "p99": p99,
                    "meets_sla": "Yes" if meets_sla else "No",
                    "complete_requests": complete_requests,
                    "requests_per_sec": requests_per_sec,
                    "mean_time": mean_time,
                    "non_2xx_responses": non_2xx,
                    "error_rate": error_rate,
                    "meets_error_threshold": "Yes" if meets_error_threshold else "No"
                })

        if successful_runs == 0:
            logger.warning(f"No successful ab runs at concurrency {c}")
            break

        # Calculate aggregated metrics (AVERAGES, not sums!)
        avg_p95 = sum(all_p95) / len(all_p95) if all_p95 else 0
        avg_p99 = sum(all_p99) / len(all_p99) if all_p99 else 0
        avg_rps = sum_requests_per_sec / successful_runs  # Average reqs/sec across all rows
        avg_mean_time = sum(all_mean_times) / len(all_mean_times) if all_mean_times else 0
        avg_error_rate = (total_non_2xx / total_requests) * 100 if total_requests > 0 else 0

        # Update last metrics
        last_p95 = int(avg_p95)
        last_p99 = int(avg_p99)
        last_non_2xx = total_non_2xx
        last_error_rate = avg_error_rate
        last_requests_per_sec = avg_rps

        # Store aggregated result
        meets_sla = (avg_p95 < SLA_THRESHOLD) and (avg_p99 < SLA_THRESHOLD)
        meets_error_threshold = avg_error_rate <= ERROR_THRESHOLD

        details_results.append({
            "dimension": dimension,
            "level": level,
            "friendly_display": friendly_display,
            "endpoint": f"{endpoint_path} [AGGREGATED]",
            "concurrency": c,
            "p95": int(avg_p95),
            "p99": int(avg_p99),
            "meets_sla": "Yes" if meets_sla else "No",
            "complete_requests": total_requests,
            "requests_per_sec": avg_rps,
            "mean_time": avg_mean_time,
            "non_2xx_responses": total_non_2xx,
            "error_rate": avg_error_rate,
            "meets_error_threshold": "Yes" if meets_error_threshold else "No"
        })

        # Write aggregated to CSV
        with open(DETAILS_FILE, 'a', newline='') as f:
            writer = csv.writer(f)
            writer.writerow([
                HARDWARE_PROFILE,
                MACHINE_SPECS,
                "N/A",  # TODO: CSV output format needs refactor for dimension isolation
                f"{endpoint_path} [AGG]",
                c,
                f"{int(avg_p95)}ms",
                f"{int(avg_p99)}ms",
                "Yes" if meets_sla else "No",
                total_requests,
                f"{avg_rps:.2f}",
                f"{avg_mean_time:.2f}ms",
                total_non_2xx,
                f"{avg_error_rate:.2f}%",
                "Yes" if meets_error_threshold else "No"
            ])

        logger.info(f"  Aggregated: p95={int(avg_p95)}ms, p99={int(avg_p99)}ms, reqs={total_requests}, avg_rps={avg_rps:.2f}, error_rate={avg_error_rate:.2f}%")

        if meets_sla and meets_error_threshold:
            max_sla_conc = c
            best_p95 = int(avg_p95)
            best_p99 = int(avg_p99)
            best_non_2xx = total_non_2xx
            best_error_rate = avg_error_rate
            best_requests_per_sec = avg_rps
        else:
            if not meets_sla:
                logger.info(f"SLA threshold of {SLA_THRESHOLD}ms exceeded at concurrency {c}. Stopping tests for {endpoint_name}.")
            if not meets_error_threshold:
                logger.info(f"Error threshold of {ERROR_THRESHOLD}% exceeded at concurrency {c} (actual: {avg_error_rate:.2f}%). Stopping tests for {endpoint_name}.")
            break

    # If no concurrency level passed, return last test metrics
    if max_sla_conc == 0:
        return max_sla_conc, last_p95, last_p99, last_non_2xx, last_error_rate, last_requests_per_sec
    return max_sla_conc, best_p95, best_p99, best_non_2xx, best_error_rate, best_requests_per_sec


###############################################################################
# ESTIMATED DURATION CALCULATOR
###############################################################################

def estimate_exponential_tests(max_concurrency):
    """
    Estimate the number of tests for exponential + binary search.

    Phase 1 (Exponential): log2(max_concurrency) tests
    Phase 2 (Binary): Up to log2(gap) tests between last pass and first fail

    Worst case: We find failure at last exponential level, then binary search the full gap.
    Average case: About log2(max_concurrency) + 5 tests.
    """
    import math
    # Exponential phase: tests at 1, 2, 4, 8, ... up to max_concurrency
    exp_tests = int(math.log2(max_concurrency)) + 1 if max_concurrency > 0 else 1
    # Binary phase: worst case is log2 of the largest gap (half of max_concurrency)
    binary_tests = int(math.log2(max_concurrency / 2)) + 1 if max_concurrency > 1 else 0
    return exp_tests + binary_tests


def calculate_estimated_duration(num_endpoints, num_rows, search_strategy, rotate_data):
    """
    Calculate estimated test duration based on configuration.

    Returns: (total_seconds, description_string)
    """
    if rotate_data:
        # Each concurrency level tests all rows
        duration_per_level = num_rows * ROW_DURATION
    else:
        # Single row per concurrency level
        duration_per_level = TEST_DURATION

    if search_strategy == 'exponential':
        # Exponential + binary search: ~12-17 tests
        tests_per_endpoint = estimate_exponential_tests(MAX_CONCURRENCY)
    else:
        # Linear: test all concurrency levels
        tests_per_endpoint = len(CONCURRENCIES)

    # Total per endpoint
    per_endpoint_secs = tests_per_endpoint * duration_per_level

    # Add cooldown between endpoints (except before first)
    cooldowns = (num_endpoints - 1) * COOLDOWN_PERIOD if num_endpoints > 1 else 0

    total_secs = (num_endpoints * per_endpoint_secs) + cooldowns

    return total_secs, tests_per_endpoint


def format_duration(seconds):
    """Format seconds into human-readable string."""
    if seconds < 60:
        return f"{seconds}s"
    elif seconds < 3600:
        mins = seconds // 60
        secs = seconds % 60
        return f"{mins}m {secs}s" if secs > 0 else f"{mins}m"
    else:
        hours = seconds // 3600
        mins = (seconds % 3600) // 60
        return f"{hours}h {mins}m" if mins > 0 else f"{hours}h"


def display_estimated_duration(num_endpoints, num_rows, search_strategy, rotate_data, endpoints_to_test):
    """Display estimated duration and expected completion time."""
    total_secs, tests_per_endpoint = calculate_estimated_duration(
        num_endpoints, num_rows, search_strategy, rotate_data
    )

    now = datetime.datetime.now()
    completion_time = now + datetime.timedelta(seconds=total_secs)

    logger.info("=" * 80)
    logger.info("ESTIMATED TEST DURATION")
    logger.info("=" * 80)
    logger.info(f"Endpoints to test:     {num_endpoints}")
    for name, path, _ in endpoints_to_test:
        logger.info(f"  - {name} ({path})")
    logger.info(f"Search strategy:       {search_strategy}")
    if search_strategy == 'exponential':
        exp_levels = generate_exponential_levels(MAX_CONCURRENCY)
        num_exp_levels = len(exp_levels)
        num_binary_levels = tests_per_endpoint - num_exp_levels
        logger.info(f"  Concurrency levels: {exp_levels}")
        logger.info(f"  Est. tests per endpoint: ~{tests_per_endpoint} ({num_exp_levels} exponential + {num_binary_levels} binary search worst-case)")
    else:
        logger.info(f"  Concurrency levels: {CONCURRENCIES}")
        logger.info(f"  Tests per endpoint: {len(CONCURRENCIES)} (linear scan)")

    if rotate_data:
        logger.info(f"Data rotation:         ON ({num_rows} rows × {ROW_DURATION}s each)")
    else:
        logger.info(f"Data rotation:         OFF ({TEST_DURATION}s per level)")

    logger.info(f"Cooldown between endpoints: {COOLDOWN_PERIOD}s")
    logger.info("-" * 40)
    logger.info(f"Estimated duration:    {format_duration(total_secs)}")
    logger.info(f"Expected completion:   {completion_time.strftime('%Y-%m-%d %H:%M:%S')}")
    logger.info("=" * 80)
    logger.info("")


###############################################################################
# MAIN
###############################################################################

def list_dimensions():
    """List all available dimensions with their details."""
    print("\nAvailable dimensions:")
    print("=" * 100)
    print(f"{'Dimension':<18} {'Description':<35} {'Type':<12} {'Levels':<25} {'Endpoints'}")
    print("-" * 100)
    for dim_name, dim_config in DIMENSIONS.items():
        dim_type = dim_config['type']
        if dim_type == 'power_of_10':
            levels = '1, 10, 100, ...'
        elif dim_type == 'percentile':
            levels = ', '.join(PERCENTILE_LEVELS)
        elif dim_type == 'era':
            levels = 'byron, shelley, ...'
        elif dim_type == 'quartile':
            levels = ', '.join(QUARTILE_LEVELS)
        elif dim_type == 'boolean':
            levels = 'true, false'
        else:
            levels = 'N/A'
        endpoints = ', '.join(dim_config['endpoints'])
        print(f"{dim_name:<18} {dim_config['description']:<35} {dim_type:<12} {levels:<25} {endpoints}")
    print()


def list_endpoints():
    """List all available endpoints with their details."""
    print("\nAvailable endpoints:")
    print("=" * 80)
    for name, path, _ in ENDPOINTS:
        print(f"  {path:<25} {name}")
    print()


def read_csv_data(csv_file: str) -> List[Dict[str, str]]:
    """Read and parse CSV data file, returning list of dicts keyed by header.

    The dimension-isolated CSV files have different columns depending on the type:
    - addresses/*.csv: 'address' column
    - blocks/*.csv: 'block_index', 'block_hash' columns
    - transactions/*.csv: 'transaction_hash', 'block_hash' columns

    Returns list of dicts where each dict maps column_name -> value.
    """
    try:
        with open(csv_file, "r", newline="") as f:
            reader = csv.DictReader(f)
            return list(reader)
    except FileNotFoundError:
        return []


def run_dimension_test(
    dimension: str,
    level: str,
    endpoint_path: str,
    csv_rows: List[Dict[str, str]],
    metadata: Optional[Dict],
    summary_id: int
) -> Tuple[int, Dict]:
    """
    Run capacity test for a specific dimension/level/endpoint combination.

    Returns: (new_summary_id, summary_record)
    """
    dim_config = DIMENSIONS[dimension]
    friendly_display = get_friendly_display(dimension, level, metadata)

    # Determine endpoint name and payload function
    endpoint_name = f"{endpoint_path} [{dimension}:{level}]"
    ep_func = get_payload_function_for_endpoint(endpoint_path)

    if not ep_func:
        logger.error(f"No payload function for endpoint: {endpoint_path}")
        return summary_id, None

    logger.info("=" * 80)
    logger.info(f"TESTING: {dim_config['description']} - {friendly_display}")
    logger.info(f"  Dimension: {dimension}")
    logger.info(f"  Level: {level}")
    logger.info(f"  Endpoint: {endpoint_path}")
    logger.info(f"  Data rows: {len(csv_rows)}")
    logger.info("=" * 80)

    # Determine test mode and run test
    if ROTATE_DATA and SEARCH_STRATEGY == 'exponential':
        max_conc, p95_val, p99_val, non_2xx_val, error_rate_val, reqs_per_sec_val = test_endpoint_exponential_with_rotation(
            endpoint_name, endpoint_path, ep_func, csv_rows,
            dimension=dimension, level=level, friendly_display=friendly_display
        )
    elif ROTATE_DATA:
        max_conc, p95_val, p99_val, non_2xx_val, error_rate_val, reqs_per_sec_val = test_endpoint_with_rotation(
            endpoint_name, endpoint_path, ep_func, csv_rows,
            dimension=dimension, level=level, friendly_display=friendly_display
        )
    elif SEARCH_STRATEGY == 'exponential':
        max_conc, p95_val, p99_val, non_2xx_val, error_rate_val, reqs_per_sec_val = test_endpoint_exponential(
            endpoint_name, endpoint_path, ep_func, csv_rows[0] if csv_rows else {},
            dimension=dimension, level=level, friendly_display=friendly_display
        )
    else:
        max_conc, p95_val, p99_val, non_2xx_val, error_rate_val, reqs_per_sec_val = test_endpoint(
            endpoint_name, endpoint_path, ep_func, csv_rows[0] if csv_rows else {},
            dimension=dimension, level=level, friendly_display=friendly_display
        )

    # Create summary record
    summary_record = {
        "id": summary_id,
        "release": RELEASE_VERSION,
        "dimension": dimension,
        "level": level,
        "friendly_display": friendly_display,
        "endpoint": endpoint_path,
        "max_concurrency": max_conc,
        "p95": p95_val,
        "p99": p99_val,
        "non_2xx_responses": non_2xx_val,
        "error_rate": error_rate_val,
        "requests_per_sec": reqs_per_sec_val
    }

    # Write to summary CSV file
    with open(SUMMARY_FILE, 'a', newline='') as f:
        writer = csv.writer(f)
        writer.writerow([
            summary_id,
            RELEASE_VERSION,
            HARDWARE_PROFILE,
            dimension,
            level,
            friendly_display,
            endpoint_path,
            max_conc,
            f"{p95_val}ms",
            f"{p99_val}ms",
            non_2xx_val,
            f"{error_rate_val:.2f}%",
            f"{reqs_per_sec_val:.2f}"
        ])

    logger.info(f"Completed: {dimension}:{level} on {endpoint_path}")
    logger.info(f"  Max concurrency: {max_conc}, p95: {p95_val}ms, p99: {p99_val}ms")
    logger.info(f"  Error rate: {error_rate_val:.2f}%, Reqs/sec: {reqs_per_sec_val:.2f}")

    return summary_id + 1, summary_record


def get_payload_function_for_endpoint(endpoint_path: str):
    """Get the payload generator function for an endpoint."""
    for name, path, func in ENDPOINTS:
        if path == endpoint_path:
            return func
    return None


def generate_dimension_markdown(metadata: Optional[Dict]):
    """Generate dimension-aware markdown summary."""
    with open(SUMMARY_MD_FILE, 'w') as f:
        f.write("# Capacity Test Results - Dimension Isolated\n\n")
        f.write(f"**Network:** {NETWORK_ID}\n")
        f.write(f"**Release:** {RELEASE_VERSION}\n")
        f.write(f"**Hardware:** {HARDWARE_PROFILE}\n")
        f.write(f"**SLA Threshold:** {SLA_THRESHOLD}ms (p95 & p99)\n\n")

        # Group results by dimension
        results_by_dimension: Dict[str, List[Dict]] = {}
        for sr in summary_results:
            dim = sr.get('dimension', 'unknown')
            if dim not in results_by_dimension:
                results_by_dimension[dim] = []
            results_by_dimension[dim].append(sr)

        for dim_name, results in results_by_dimension.items():
            dim_config = DIMENSIONS.get(dim_name, {})
            f.write(f"## {dim_config.get('description', dim_name)}\n\n")

            # Write table header
            f.write("| Level | Range | Endpoint | Max Conc | p95 (ms) | p99 (ms) | Req/s |\n")
            f.write("|-------|-------|----------|----------|----------|----------|-------|\n")

            for sr in results:
                f.write(f"| {sr['level']} | {sr['friendly_display']} | {sr['endpoint']} | ")
                f.write(f"{sr['max_concurrency']} | {sr['p95']} | {sr['p99']} | {sr['requests_per_sec']:.2f} |\n")

            # Add insight
            if len(results) >= 2:
                first = results[0]
                last = results[-1]
                if first['max_concurrency'] > 0 and last['max_concurrency'] > 0:
                    ratio = first['max_concurrency'] / last['max_concurrency']
                    if ratio != 1:
                        f.write(f"\n**Insight:** Performance varies {ratio:.1f}x from {first['level']} to {last['level']}\n")

            f.write("\n")


def initialize_dimension_csv_files():
    """Initialize CSV files with dimension-aware headers."""
    with open(DETAILS_FILE, 'w', newline='') as f:
        writer = csv.writer(f)
        writer.writerow([
            "Hardware_Profile", "Dimension", "Level", "Friendly_Display",
            "Endpoint", "Concurrency", "p95(ms)", "p99(ms)", "Meets_SLA",
            "Complete_Requests", "Requests_per_sec", "Mean_Time(ms)",
            "Non_2xx_Responses", "Error_Rate(%)", "Meets_Error_Threshold"
        ])

    with open(SUMMARY_FILE, 'w', newline='') as f:
        writer = csv.writer(f)
        writer.writerow([
            "ID", "Release", "Hardware_Profile", "Dimension", "Level", "Friendly_Display",
            "Endpoint", "Max_Concurrency", "p95(ms)", "p99(ms)",
            "Non_2xx_Responses", "Error_Rate(%)", "Requests_per_sec"
        ])

    # Initialize command log file
    with open(COMMANDS_FILE, 'w') as f:
        f.write(f"AB Commands Log - {datetime.datetime.now()}\n")
        f.write("=" * 80 + "\n\n")


def main():
    global OUTPUT_DIR, DIMENSIONS_METADATA

    try:
        # Handle list options first
        if args.list_dimensions:
            list_dimensions()
            sys.exit(0)

        if args.list_endpoints:
            list_endpoints()
            sys.exit(0)

        # Load dimensions metadata first (needed for level resolution)
        DIMENSIONS_METADATA = load_dimensions_metadata(NETWORK_ID)
        if not DIMENSIONS_METADATA:
            print(f"Warning: dimensions.json not found for network '{NETWORK_ID}'")
            print(f"Run: ./populate_data.py --network {NETWORK_ID} --db-url ...")
            print("Continuing with default level names...")

        # Get test combinations from CLI args
        test_combinations = get_test_combinations(args)

        if not test_combinations:
            print("Error: No dimension/level specified.")
            print("Use --dimension DIMENSION --level LEVEL or --dimensions SPEC")
            print("Use --list-dimensions to see available dimensions")
            print("\nExamples:")
            print("  ./stability_test.py --dimension utxo_count --level p95")
            print("  ./stability_test.py --dimension utxo_count --level all")
            print("  ./stability_test.py --dimensions 'utxo_count:p50,p95;block_tx_count:p90'")
            sys.exit(1)

        # Create output directory with dimension info
        dim_info = "_".join(sorted(set(d for d, _ in test_combinations)))[:30]
        OUTPUT_DIR = create_output_dir(dim_info)
        setup_output_files(OUTPUT_DIR)

        print(f"Creating output directory: {OUTPUT_DIR}")

        # Set up logging
        setup_logging(OUTPUT_DIR, VERBOSE)
        logger.info(f"Logging initialized. Log file: {os.path.join(OUTPUT_DIR, 'stability_test.log')}")

        # Log configuration
        logger.info("Configuration:")
        logger.info(f"  URL: {BASE_URL}")
        logger.info(f"  Network: {NETWORK_ID}")
        logger.info(f"  Search strategy: {SEARCH_STRATEGY}")
        logger.info(f"  Test duration: {TEST_DURATION}s per level")
        logger.info(f"  SLA threshold: {SLA_THRESHOLD}ms (p95 & p99)")
        logger.info(f"  Error threshold: {ERROR_THRESHOLD}%")
        logger.info(f"  Data rotation: {'ON' if ROTATE_DATA else 'OFF'}")
        if ROTATE_DATA:
            logger.info(f"  Row duration: {ROW_DURATION}s")

        # Initialize CSV files
        initialize_dimension_csv_files()
        logger.info(f"Output files initialized in {OUTPUT_DIR}")

        # Log test plan
        logger.info("=" * 80)
        logger.info("TEST PLAN")
        logger.info("=" * 80)
        logger.info(f"Testing {len(test_combinations)} dimension/level combinations:")
        for dim, level in test_combinations:
            friendly = get_friendly_display(dim, level, DIMENSIONS_METADATA)
            endpoints = get_endpoints_for_dimension(dim, args.selected_endpoint)
            logger.info(f"  - {dim}:{level} ({friendly}) -> {', '.join(endpoints)}")
        logger.info("=" * 80)

        # Run tests for each combination
        summary_id = 1
        test_idx = 0

        for dimension, level in test_combinations:
            # Get CSV data file for this dimension/level
            csv_file = get_csv_file_path(NETWORK_ID, dimension, level)

            if not os.path.exists(csv_file):
                logger.warning(f"CSV file not found: {csv_file}")
                logger.warning(f"Skipping {dimension}:{level}")
                continue

            csv_rows = read_csv_data(csv_file)
            if not csv_rows:
                logger.warning(f"No data in CSV file: {csv_file}")
                logger.warning(f"Skipping {dimension}:{level}")
                continue

            # Limit rows if specified
            if MAX_ROWS and len(csv_rows) > MAX_ROWS:
                csv_rows = csv_rows[:MAX_ROWS]

            # Get endpoints for this dimension
            endpoints = get_endpoints_for_dimension(dimension, args.selected_endpoint)

            for endpoint_path in endpoints:
                # Add cooldown between tests
                if test_idx > 0:
                    logger.info(f"Cooldown: {COOLDOWN_PERIOD}s...")
                    time.sleep(COOLDOWN_PERIOD)

                summary_id, summary_record = run_dimension_test(
                    dimension=dimension,
                    level=level,
                    endpoint_path=endpoint_path,
                    csv_rows=csv_rows,
                    metadata=DIMENSIONS_METADATA,
                    summary_id=summary_id
                )

                if summary_record:
                    summary_results.append(summary_record)

                test_idx += 1

        # Generate markdown reports
        generate_dimension_markdown(DIMENSIONS_METADATA)
        generate_markdown_tables()
        logger.info("Generated markdown report files.")

        # Print final summary
        print("\n" + "=" * 100)
        print(" CAPACITY TEST RESULTS - DIMENSION ISOLATED ")
        print("=" * 100)

        # Group by dimension for display
        results_by_dimension: Dict[str, List[Dict]] = {}
        for sr in summary_results:
            dim = sr.get('dimension', 'unknown')
            if dim not in results_by_dimension:
                results_by_dimension[dim] = []
            results_by_dimension[dim].append(sr)

        for dim_name, results in results_by_dimension.items():
            dim_config = DIMENSIONS.get(dim_name, {})
            print(f"\n{dim_config.get('description', dim_name).upper()}")
            print("-" * 100)
            print(f"{'Level':<8} {'Range':<25} {'Endpoint':<25} {'Max Conc':>10} {'p95':>10} {'p99':>10} {'Req/s':>10}")
            print("-" * 100)

            for sr in results:
                print(f"{sr['level']:<8} {sr['friendly_display']:<25} {sr['endpoint']:<25} "
                      f"{sr['max_concurrency']:>10} {sr['p95']:>9}ms {sr['p99']:>9}ms {sr['requests_per_sec']:>10.2f}")

        print("\n" + "=" * 100)
        print(f"Done. Results saved to: {OUTPUT_DIR}")
        print("Files generated:")
        print(f"  - {DETAILS_FILE}")
        print(f"  - {SUMMARY_FILE}")
        print(f"  - {DETAILS_MD_FILE}")
        print(f"  - {SUMMARY_MD_FILE}")
        print(f"  - {COMMANDS_FILE}")

    except KeyboardInterrupt:
        if logger:
            logger.warning("=" * 80)
            logger.warning("Test interrupted by user (Ctrl+C)")
            logger.warning("=" * 80)

            if details_results:
                logger.warning(f"Partial results were collected and saved to: {OUTPUT_DIR}")
                try:
                    generate_dimension_markdown(DIMENSIONS_METADATA)
                    generate_markdown_tables()
                    logger.warning("Partial markdown reports were generated successfully.")
                except Exception as e_md:
                    logger.error(f"Could not generate markdown reports: {e_md}")

            logger.warning("Exiting gracefully...")
        else:
            print("Test interrupted by user (Ctrl+C). Exiting.")
        sys.exit(0)

    except Exception as e:
        if logger:
            logger.exception(f"An unexpected error occurred: {str(e)}")
            if details_results:
                logger.warning(f"Partial results (if any) were saved to: {OUTPUT_DIR}")
            logger.error("Exiting with error...")
        else:
            print(f"An unexpected error occurred: {str(e)}. Exiting.")
        sys.exit(1)


if __name__ == "__main__":
    main()
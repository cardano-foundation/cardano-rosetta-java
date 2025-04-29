# Cardano Rosetta Stability Test

A tool for testing the stability and performance of Cardano Rosetta API endpoints under sustained load.

## Overview

This script performs stability testing of Cardano Rosetta API endpoints by:

- Running extended duration tests to verify API reliability under constant load
- Monitoring endpoint stability across multiple concurrent connections
- Measuring response times (p95, p99) and validating against SLA thresholds
- Identifying potential degradation during sustained operation

## Requirements

- Python 3.6+
- Apache Benchmark (`ab`) command-line tool
- CSV file with test data in format: `address,block_index,block_hash,transaction_size,relative_ttl,transaction_hash`

## Hardware Specifications

The test records hardware specifications to provide context for performance results. You can specify your hardware details using the command-line options:

```
--hardware-profile HARDWARE_PROFILE
--machine-specs MACHINE_SPECS
```

This information appears in test reports, allowing meaningful comparison between different hardware configurations.

### Getting Hardware Information in Ubuntu

To gather system specifications in Ubuntu, use these commands:

```bash
# CPU info (cores, threads, model)
lscpu

# RAM info
free -h

# Storage info
lsblk
sudo hdparm -I /dev/sda | grep Model

# All-in-one detailed hardware report
sudo apt install inxi
inxi -Fxz
```

For a GUI tool with comprehensive hardware information:

```bash
sudo apt install hardinfo
hardinfo
```

## Running the Test

### Basic Usage

Run with default parameters:

```bash
./load-tests/stability_test.py
```

### Command-Line Options

The script supports several command-line options to customize the test:

```
usage: stability_test.py [-h] [--url BASE_URL] [--csv CSV_FILE]
                         [--release RELEASE_VERSION]
                         [--hardware-profile HARDWARE_PROFILE]
                         [--machine-specs MACHINE_SPECS]
                         [--concurrency CONCURRENCIES] [--duration TEST_DURATION]
                         [--sla SLA_THRESHOLD] [--skip-header] [-v]
                         [--cooldown COOLDOWN] [--endpoints SELECTED_ENDPOINTS]
                         [--list-endpoints]

Cardano Rosetta API Stability Testing Tool

options:
  -h, --help            show this help message and exit
  --url BASE_URL        Base URL for the Rosetta API service (default: http://127.0.0.1:8082)
  --csv CSV_FILE        Path to CSV file with test data (default: ./mainnet-data.csv)
  --release RELEASE_VERSION
                        Release version for reporting (default: 1.2.7)
  --hardware-profile HARDWARE_PROFILE
                        Hardware profile ID for reporting (default: entry_level)
  --machine-specs MACHINE_SPECS
                        Detailed machine specifications for reporting (default: 16 cores, 16 threads, 125GB RAM, 3.9TB HDD, QEMU Virtual CPU v2.5+)
  --concurrency CONCURRENCIES
                        Comma-separated list of concurrency levels to test (default: 1,2,4,8,12,16,20,24,28,32,36,40,44,48,52,56,60,64,68,72,76,80,84,88,92,96,100,125,150,175,200)
  --duration TEST_DURATION
                        Duration in seconds for each concurrency level test (default: 60)
  --sla SLA_THRESHOLD   SLA threshold in milliseconds (default: 1000)
  --skip-header         Skip the header row in the CSV file (default: False)
  -v, --verbose         Enable verbose output (default: False)
  --cooldown COOLDOWN   Cooldown period in seconds between endpoint tests (default: 60)
  --endpoints SELECTED_ENDPOINTS
                        Comma-separated list of endpoint names or paths to test (e.g. "Network Status,Block"
                        or "/account/balance,/block"). If not specified, all endpoints will be tested.
  --list-endpoints      List all available endpoints and exit without running tests
```

### Example Usage

Test against a different API server:

```bash
./load-tests/stability_test.py --url http://example.com:8082
```

Use a custom CSV file and reduce test duration:

```bash
./load-tests/stability_test.py --csv custom-data.csv --duration 30
```

Test a specific set of concurrency levels:

```bash
./load-tests/stability_test.py --concurrency 1,2,4,8,16,32
```

Test only specific endpoints by name:

```bash
./load-tests/stability_test.py --endpoints "Network Status,Block,Account Balance"
```

Test only specific endpoints by path:

```bash
./load-tests/stability_test.py --endpoints "/network/status,/block,/account/balance"
```

Test only search/transactions endpoint with stake address data:

```bash
./load-tests/stability_test.py --endpoints "/search/transactions" --csv load-tests/data/mainnet-data-stake-address.csv
```

List all available endpoints without running tests:

```bash
./load-tests/stability_test.py --list-endpoints
```

Set custom hardware profile and SLA:

```bash
./load-tests/stability_test.py --hardware-profile high_performance --machine-specs "16 cores, 64GB RAM, SSD" --sla 500
```

## Output

The script creates a timestamped directory containing:

1. **CSV Reports**:

   - `details_results.csv`: Complete performance data for each endpoint/concurrency
   - `summary_results.csv`: Maximum concurrency achieved per endpoint

2. **Markdown Reports**:

   - `details_results.md`: Formatted table of detailed results
   - `summary_results.md`: Formatted table of summary results

3. **Log Files**:
   - `ab_commands.log`: Record of all Apache Benchmark commands executed
   - JSON payload files for each endpoint

## Example Output

### Detailed Results (excerpt)

| Hardware    | Machine Specs                                    | Endpoint         | Concurrency | p95 (ms) | p99 (ms) | Meets SLA | Complete Reqs | Reqs/sec | Mean Time (ms) |
| ----------- | ------------------------------------------------ | ---------------- | ----------- | -------- | -------- | --------- | ------------- | -------- | -------------- |
| entry_level | 4 cores, 8 threads, 32GB RAM, SSD, Intel Core i3 | /network/status  | 1           | 1ms      | 2ms      | Yes       | 9441          | 943.97   | 1.06ms         |
| entry_level | 4 cores, 8 threads, 32GB RAM, SSD, Intel Core i3 | /network/status  | 2           | 1ms      | 1ms      | Yes       | 20538         | 2053.68  | 0.49ms         |
| entry_level | 4 cores, 8 threads, 32GB RAM, SSD, Intel Core i3 | /network/status  | 4           | 1ms      | 1ms      | Yes       | 39249         | 3924.89  | 0.26ms         |
| entry_level | 4 cores, 8 threads, 32GB RAM, SSD, Intel Core i3 | /account/balance | 1           | 34ms     | 40ms     | Yes       | 454           | 45.32    | 22.06ms        |
| entry_level | 4 cores, 8 threads, 32GB RAM, SSD, Intel Core i3 | /block           | 4           | 120ms    | 129ms    | Yes       | 361           | 36.02    | 27.76ms        |

### Summary Results

| ID  | Release   | Hardware    | Machine Specs                                    | Endpoint               | Max Concurrency | p95 (ms) | p99 (ms) |
| --- |-----------| ----------- | ------------------------------------------------ | ---------------------- | --------------- | -------- | -------- |
| 1   | 1.2.6 | entry_level | 4 cores, 8 threads, 32GB RAM, SSD, Intel Core i3 | /network/status        | 4               | 1ms      | 1ms      |
| 2   | 1.2.6 | entry_level | 4 cores, 8 threads, 32GB RAM, SSD, Intel Core i3 | /account/balance       | 4               | 35ms     | 39ms     |
| 3   | 1.2.6 | entry_level | 4 cores, 8 threads, 32GB RAM, SSD, Intel Core i3 | /account/coins         | 4               | 33ms     | 39ms     |
| 4   | 1.2.6 | entry_level | 4 cores, 8 threads, 32GB RAM, SSD, Intel Core i3 | /block                 | 4               | 120ms    | 129ms    |
| 5   | 1.2.6 | entry_level | 4 cores, 8 threads, 32GB RAM, SSD, Intel Core i3 | /block/transaction     | 4               | 84ms     | 95ms     |
| 6   | 1.2.6 | entry_level | 4 cores, 8 threads, 32GB RAM, SSD, Intel Core i3 | /search/transactions   | 4               | 4ms      | 4ms      |
| 7   | 1.2.6 | entry_level | 4 cores, 8 threads, 32GB RAM, SSD, Intel Core i3 | /construction/metadata | 4               | 1ms      | 1ms      |

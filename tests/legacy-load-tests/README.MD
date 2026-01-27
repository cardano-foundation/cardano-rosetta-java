# Cardano Rosetta Stability Test

A tool for testing the stability and performance of Cardano Rosetta API endpoints under sustained load.

## Overview

This script performs stability testing of Cardano Rosetta API endpoints by:

- Running extended duration tests to verify API reliability under constant load
- Monitoring endpoint stability across multiple concurrent connections
- Measuring response times (p95, p99) and validating against SLA thresholds
- Identifying potential degradation during sustained operation
- Tracking non-2xx error responses and validating against error thresholds

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
                         [--sla SLA_THRESHOLD] [--error-threshold ERROR_THRESHOLD]
                         [--skip-header] [-v] [--cooldown COOLDOWN]
                         [--network NETWORK]
                         [--endpoints SELECTED_ENDPOINTS] [--list-endpoints]

Cardano Rosetta API Stability Testing Tool

options:
  -h, --help            show this help message and exit
  --url BASE_URL        Base URL for the Rosetta API service (default: http://127.0.0.1:8082)
  --csv CSV_FILE        Path to CSV file with test data (default: ./mainnet-data.csv)
  --release RELEASE_VERSION
                        Release version for reporting (default: 1.2.8)
  --hardware-profile HARDWARE_PROFILE
                        Hardware profile ID for reporting (default: entry_level)
  --machine-specs MACHINE_SPECS
                        Detailed machine specifications for reporting (default: 16 cores, 16 threads, 125GB RAM, 3.9TB HDD, QEMU Virtual CPU v2.5+)
  --concurrency CONCURRENCIES
                        Comma-separated list of concurrency levels to test (default: 1,2,4,8,12,16,20,24,28,32,36,40,44,48,52,56,60,64,68,72,76,80,84,88,92,96,100,125,150,175,200)
  --duration TEST_DURATION
                        Duration in seconds for each concurrency level test (default: 60)
  --sla SLA_THRESHOLD   SLA threshold in milliseconds (default: 1000)
  --error-threshold ERROR_THRESHOLD
                        Threshold for non-2xx errors as percentage (default: 1.0)
  --skip-header         Skip the header row in the CSV file (default: False)
  -v, --verbose         Enable verbose output (default: False)
  --cooldown COOLDOWN   Cooldown period in seconds between endpoint tests (default: 60)
  --network NETWORK     Network identifier for API requests: mainnet or preprod (default: mainnet)
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

Test search/transactions by hash lookup:

```bash
./load-tests/stability_test.py --endpoints "Search Transactions by Hash"
```

Test search/transactions by address (more resource-intensive):

```bash
./load-tests/stability_test.py --endpoints "Search Transactions by Address" --csv load-tests/data/mainnet-data.csv
```

Test on preprod network:

```bash
./load-tests/stability_test.py --network preprod --url http://127.0.0.1:8082 --csv data/preprod-data.csv
```

List all available endpoints without running tests:

```bash
./load-tests/stability_test.py --list-endpoints
```

Set custom hardware profile and SLA:

```bash
./load-tests/stability_test.py --hardware-profile high_performance --machine-specs "16 cores, 64GB RAM, SSD" --sla 500
```

Test with custom SLA and error thresholds:

```bash
./load-tests/stability_test.py --sla 500 --error-threshold 0.5
```

## Test Data

### CSV Format Requirements

Each CSV row must have 6 fields with specific associations:

```
address,block_index,block_hash,transaction_size,relative_ttl,transaction_hash
```

**Critical Data Associations (Implicit Rules):**

1. **Block Consistency**: The `block_hash` MUST be the hash of the block at `block_index`
2. **Transaction in Block**: The `transaction_hash` MUST exist in the specified block (`block_hash`)
3. **Address in Transaction**: The `address` MUST be involved in the transaction (appear in operations as input/output)
4. **Transaction Size**: The `transaction_size` MUST match the actual size of the transaction in bytes
5. **Valid Address**: The `address` MUST have a balance and UTXO history (for account endpoints)
6. **TTL Value**: The `relative_ttl` is used by construction/metadata endpoint (1000 is standard)

These associations ensure all 8 endpoints can successfully use the same data row:
- Network Status: No specific data needed
- Account Balance/Coins: Requires valid address with balance
- Block: Requires valid block_index and block_hash
- Block Transaction: Requires transaction in specified block
- Search by Hash: Requires valid transaction_hash
- Search by Address: Requires address involved in transactions
- Construction Metadata: Requires transaction_size and relative_ttl

### Available Data Files

The `data/` directory contains pre-validated CSV files for different networks:

### Mainnet Data (`mainnet-data.csv`)
- **Block**: 11573705
- **Transaction**: 3a954835b69ca01ff9cf3b30ce385d5d9ef0cea502bd0f2ad156684dfbaf325a
- **Address**: addr1qxw5ly68dml8ceg7eawa7we8pjw8j8hn74n2djt2upmnq9th42p6lrke4yj3e0xqg3sdqm6lzksa53wd2550vrpkedks4fttnm

### Preprod Data (`preprod-data.csv`)
- **Block**: 4070700
- **Transaction**: bf540a825d5d40af7435801ce6adcac010f3f9f29ae102aee8cff8007f68c3d4
- **Address**: addr_test1wzn5ee2qaqvly3hx7e0nk3vhm240n5muq3plhjcnvx9ppjgf62u6a

All data has been validated to work with all 8 stability test endpoints, with proper associations between blocks, transactions, and addresses.

## Endpoint Details

### Search Transactions Endpoints

The stability test includes two variants of the `/search/transactions` endpoint:

1. **Search Transactions by Hash**: Queries transactions using `transaction_identifier`. This is a fast, direct lookup by transaction hash.

2. **Search Transactions by Address**: Queries transactions using `account_identifier` with an address. This is more resource-intensive as it requires scanning transaction operations to find all transactions involving the specified address.

Both endpoints use the same API path (`/search/transactions`) but with different query parameters, allowing independent performance testing of each query pattern.

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

| Hardware    | Machine Specs                                    | Endpoint         | Concurrency | p95 (ms) | p99 (ms) | Meets SLA | Complete Reqs | Reqs/sec | Mean Time (ms) | Non-2xx | Error Rate (%) | Meets Error Threshold |
| ----------- | ------------------------------------------------ | ---------------- | ----------- | -------- | -------- | --------- | ------------- | -------- | -------------- | ------- | -------------- | --------------------- |
| entry_level | 4 cores, 8 threads, 32GB RAM, SSD, Intel Core i3 | /network/status  | 1           | 1ms      | 2ms      | Yes       | 9441          | 943.97   | 1.06ms         | 0       | 0.00%          | Yes                   |
| entry_level | 4 cores, 8 threads, 32GB RAM, SSD, Intel Core i3 | /network/status  | 2           | 1ms      | 1ms      | Yes       | 20538         | 2053.68  | 0.49ms         | 0       | 0.00%          | Yes                   |
| entry_level | 4 cores, 8 threads, 32GB RAM, SSD, Intel Core i3 | /network/status  | 4           | 1ms      | 1ms      | Yes       | 39249         | 3924.89  | 0.26ms         | 0       | 0.00%          | Yes                   |
| entry_level | 4 cores, 8 threads, 32GB RAM, SSD, Intel Core i3 | /account/balance | 1           | 34ms     | 40ms     | Yes       | 454           | 45.32    | 22.06ms        | 0       | 0.00%          | Yes                   |
| entry_level | 4 cores, 8 threads, 32GB RAM, SSD, Intel Core i3 | /block           | 4           | 120ms    | 129ms    | Yes       | 361           | 36.02    | 27.76ms        | 0       | 0.00%          | Yes                   |

### Summary Results

| ID  | Release | Hardware    | Machine Specs                                    | Endpoint               | Max Concurrency | p95 (ms) | p99 (ms) | Non-2xx | Error Rate (%) | Reqs/sec |
| --- | ------- | ----------- | ------------------------------------------------ | ---------------------- | --------------- | -------- | -------- | ------- | -------------- | -------- |
| 1   | 1.2.6   | entry_level | 4 cores, 8 threads, 32GB RAM, SSD, Intel Core i3 | /network/status        | 4               | 1ms      | 1ms      | 0       | 0.00%          | 3924.89  |
| 2   | 1.2.6   | entry_level | 4 cores, 8 threads, 32GB RAM, SSD, Intel Core i3 | /account/balance       | 4               | 35ms     | 39ms     | 0       | 0.00%          | 107.42   |
| 3   | 1.2.6   | entry_level | 4 cores, 8 threads, 32GB RAM, SSD, Intel Core i3 | /account/coins         | 4               | 33ms     | 39ms     | 0       | 0.00%          | 102.51   |
| 4   | 1.2.6   | entry_level | 4 cores, 8 threads, 32GB RAM, SSD, Intel Core i3 | /block                 | 4               | 120ms    | 129ms    | 0       | 0.00%          | 36.02    |
| 5   | 1.2.6   | entry_level | 4 cores, 8 threads, 32GB RAM, SSD, Intel Core i3 | /block/transaction     | 4               | 84ms     | 95ms     | 0       | 0.00%          | 24.75    |
| 6   | 1.2.6   | entry_level | 4 cores, 8 threads, 32GB RAM, SSD, Intel Core i3 | /search/transactions   | 4               | 4ms      | 4ms      | 0       | 0.00%          | 1854.29  |
| 7   | 1.2.6   | entry_level | 4 cores, 8 threads, 32GB RAM, SSD, Intel Core i3 | /construction/metadata | 4               | 1ms      | 1ms      | 0       | 0.00%          | 4015.67  |
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

The test records hardware specifications to provide context for performance results. To set your environment specs:

1. Edit the `MACHINE_SPECS` variable in `stability_test.py`:

   ```python
   MACHINE_SPECS = "16 cores, 16 threads, 125GB RAM, 3.9TB HDD, QEMU Virtual CPU v2.5+"
   ```

2. Include relevant details that impact performance:
   - CPU (cores, threads, model)
   - RAM (total amount)
   - Storage type and size
   - Any virtualization environment

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

1. Configure the test parameters in the script header (or use defaults):

   ```python
   BASE_URL = "http://127.0.0.1:8082"
   CSV_FILE = "mainnet-data.csv"
   CONCURRENCIES = [1, 2, 4, 8, 12, 16, 24, 32]
   TEST_DURATION = 10  # seconds per concurrency level
   SLA_THRESHOLD = 1000  # ms
   ...
   ```

2. Execute the script:
   ```bash
   ./load-tests/stability_test.py
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
| --- | --------- | ----------- | ------------------------------------------------ | ---------------------- | --------------- | -------- | -------- |
| 1   | 1.2.5-dev | entry_level | 4 cores, 8 threads, 32GB RAM, SSD, Intel Core i3 | /network/status        | 4               | 1ms      | 1ms      |
| 2   | 1.2.5-dev | entry_level | 4 cores, 8 threads, 32GB RAM, SSD, Intel Core i3 | /account/balance       | 4               | 35ms     | 39ms     |
| 3   | 1.2.5-dev | entry_level | 4 cores, 8 threads, 32GB RAM, SSD, Intel Core i3 | /account/coins         | 4               | 33ms     | 39ms     |
| 4   | 1.2.5-dev | entry_level | 4 cores, 8 threads, 32GB RAM, SSD, Intel Core i3 | /block                 | 4               | 120ms    | 129ms    |
| 5   | 1.2.5-dev | entry_level | 4 cores, 8 threads, 32GB RAM, SSD, Intel Core i3 | /block/transaction     | 4               | 84ms     | 95ms     |
| 6   | 1.2.5-dev | entry_level | 4 cores, 8 threads, 32GB RAM, SSD, Intel Core i3 | /search/transactions   | 4               | 4ms      | 4ms      |
| 7   | 1.2.5-dev | entry_level | 4 cores, 8 threads, 32GB RAM, SSD, Intel Core i3 | /construction/metadata | 4               | 1ms      | 1ms      |

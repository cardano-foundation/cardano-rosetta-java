# Cardano Rosetta Load Testing Suite

A comprehensive load testing suite for Cardano Rosetta API using **dimension-isolated testing** to identify specific performance bottlenecks.

## Overview

This suite uses **dimension-isolated testing** - testing ONE variable at a time with **percentile-based thresholds** from actual data distribution.

| Question | Tool | Approach |
|----------|------|----------|
| "What's the max throughput for addresses with many UTXOs?" | `stability_test.py --dimension utxo_count --level p99` | Percentile stress testing |
| "How does block age affect performance?" | `stability_test.py --dimension block_era` | Era-based testing |
| "How does the system behave under sustained load?" | `soak_test.py` (Locust) | Long-running stability |

## Quick Start

### Prerequisites

```bash
cd tests/load-tests

# Install Python dependencies
uv sync

# Install Apache Bench (for capacity testing)
sudo apt-get install apache2-utils
```

### Generate Test Data

First, generate dimension-isolated test data from your database:

```bash
# Port-forward to database first (if remote)
ssh -L 5432:localhost:5432 your-server

# Generate data for preprod
./populate_data.py --network preprod --db-url postgresql://user:pass@localhost:5432/rosetta

# Generate data for mainnet
./populate_data.py --network mainnet --db-url postgresql://user:pass@localhost:5432/rosetta
```

### Capacity Testing (Dimension-Isolated)

Test specific dimensions to identify bottlenecks:

```bash
# Test addresses by UTXO count at 95th percentile
./stability_test.py --url http://localhost:8082 --dimension utxo_count --level p95 --network preprod

# Test all percentile levels of a dimension
./stability_test.py --url http://localhost:8082 --dimension utxo_count --level all --network preprod

# Test block performance by era (oldest to newest)
./stability_test.py --url http://localhost:8082 --dimension block_era --level all --network preprod

# Test multiple dimensions
./stability_test.py --url http://localhost:8082 --dimensions "utxo_count:p50,p95;block_tx_count:p90"

# Test all dimensions at all levels (comprehensive)
./stability_test.py --url http://localhost:8082 --dimension all --level all --network preprod
```

### Soak Testing (Locust-based)

Test stability under sustained load:

```bash
# Quick soak test (5 minutes, 10 users)
./soak_test.py --url http://localhost:8082 --users 10 --duration 5m

# Full soak test (1 hour, 50 users)
./soak_test.py --url http://localhost:8082 --users 50 --duration 1h

# Extended soak test (4 hours)
./soak_test.py --url http://localhost:8082 --users 50 --duration 4h --network preprod
```

## Dimensions

Each dimension is tested independently to identify specific performance bottlenecks. All numeric dimensions use **percentile levels** (p50, p75, p90, p95, p99) to target different stress levels from typical to worst-case.

### Address Dimensions

| Dimension | Description | Endpoints | Levels |
|-----------|-------------|-----------|--------|
| `utxo_count` | Number of unspent UTXOs | `/account/balance`, `/account/coins` | p50, p75, p90, p95, p99 |
| `token_count` | Number of native tokens | `/account/balance`, `/account/coins` | p50, p75, p90, p95, p99 |
| `tx_history` | Transaction history count | `/search/transactions` | p50, p75, p90, p95, p99 |

### Block Dimensions

| Dimension | Description | Endpoints | Levels |
|-----------|-------------|-----------|--------|
| `block_tx_count` | Transactions in block | `/block` | p50, p75, p90, p95, p99 |
| `block_body_size` | Block data volume | `/block` | p50, p75, p90, p95, p99 |
| `block_era` | Block age/era | `/block` | shelley, allegra, mary, alonzo, babbage, conway |

### Transaction Dimensions

| Dimension | Description | Endpoints | Levels |
|-----------|-------------|-----------|--------|
| `tx_io_count` | Inputs + outputs count | `/block/transaction`, `/search/transactions` | p50, p75, p90, p95, p99 |
| `tx_token_count` | Token types in transaction | `/block/transaction`, `/search/transactions` | p50, p75, p90, p95, p99 |
| `tx_has_script` | Has Plutus script | `/block/transaction`, `/search/transactions` | true, false |

### Percentile Levels

| Level | Percentile | Use Case |
|-------|------------|----------|
| `p50` | 50th | Median/typical case |
| `p75` | 75th | Above average |
| `p90` | 90th | High stress |
| `p95` | 95th | Very high stress |
| `p99` | 99th | Extreme/worst case (heavy tail) |

Percentiles are calculated using `PERCENTILE_CONT` from actual data distribution. This means:
- **p50** targets addresses/blocks/transactions at the median
- **p99** targets the worst 1% - the heavy tail that stress tests performance limits

## CLI Reference

### stability_test.py (Capacity Testing)

```bash
./stability_test.py [options]
```

**Dimension selection (required):**
- `--dimension DIM` - Single dimension to test (e.g., `utxo_count`). Use `all` for all dimensions.
- `--level LEVEL` - Level to test (e.g., `p95`, `p99`). Use `all` for all levels.
- `--dimensions SPEC` - Multi-dimension spec: `"dim1:level1,level2;dim2:level3"`

**Core options:**
- `--url URL` - Base URL for the Rosetta API (default: http://127.0.0.1:8082)
- `--network NETWORK` - Network: mainnet, preprod, preview (default: mainnet)
- `--endpoint PATH` - Specific endpoint to test (defaults to dimension-appropriate endpoints)
- `--list-dimensions` - List available dimensions and exit
- `--list-endpoints` - List available endpoints and exit

**Search strategy options:**
- `--search-strategy STRATEGY` - Search strategy: exponential (default), linear
- `--max-concurrency N` - Max concurrency for exponential search (default: 2048)
- `--concurrency LEVELS` - Comma-separated levels for linear strategy

**Data rotation options:**
- `--rotate-data` - Test with all CSV rows and aggregate results
- `--row-duration SECONDS` - Duration per row when rotating (default: 30)
- `--max-rows N` - Limit rows to test when rotating

**SLA options:**
- `--duration SECONDS` - Duration per concurrency level (default: 60)
- `--sla MILLISECONDS` - SLA threshold for p95/p99 (default: 1000)
- `--error-threshold PCT` - Error rate threshold percentage (default: 1.0)

### populate_data.py (Data Generator)

```bash
./populate_data.py --network NETWORK [options]
```

**Required:**
- `--network NETWORK` - Network: mainnet, preprod, preview

**Database connection:**
- `--db-url URL` - PostgreSQL connection URL
- Or individual params: `--db-host`, `--db-port`, `--db-name`, `--db-user`, `--db-password`

**Output:**
- `--output-dir DIR` - Output directory (default: data)

### soak_test.py (Soak Testing)

```bash
./soak_test.py --url URL [options]
```

- `--url URL` - Base URL for the Rosetta API (required)
- `--users N` - Number of concurrent users (default: 10)
- `--spawn-rate N` - Users to spawn per second (default: 5)
- `--duration DURATION` - Test duration: 30s, 5m, 1h, 2h30m (default: 5m)
- `--network NETWORK` - Network: mainnet, preprod (default: preprod)

## Data Files

After running `populate_data.py`, the following structure is created:

```
data/{network}/
├── dimensions.json              # Thresholds metadata with percentile boundaries
├── addresses/
│   ├── utxo_count_p50.csv      # All dimensions use percentiles: p50, p75, p90, p95, p99
│   ├── utxo_count_p75.csv
│   ├── utxo_count_p90.csv
│   ├── utxo_count_p95.csv
│   ├── utxo_count_p99.csv
│   ├── token_count_p50.csv
│   ├── token_count_p99.csv
│   ├── tx_history_p50.csv
│   └── tx_history_p99.csv
├── blocks/
│   ├── block_tx_count_p50.csv
│   ├── block_tx_count_p99.csv
│   ├── block_body_size_p50.csv
│   ├── block_body_size_p99.csv
│   ├── block_era_shelley.csv   # Era-based (actual era names)
│   ├── block_era_alonzo.csv
│   └── block_era_conway.csv
└── transactions/
    ├── tx_io_count_p50.csv
    ├── tx_io_count_p99.csv
    ├── tx_token_count_p50.csv
    ├── tx_token_count_p99.csv
    ├── tx_has_script_true.csv  # Boolean
    └── tx_has_script_false.csv
```

### dimensions.json

Contains percentile thresholds with human-readable descriptions:

```json
{
  "network": "mainnet",
  "dimensions": {
    "utxo_count": {
      "description": "Address UTXO Count",
      "unit": "UTXOs",
      "type": "percentile",
      "thresholds": {
        "p50": {"min": 0, "max": 1, "display": "≤1 UTXOs"},
        "p75": {"min": 1, "max": 2, "display": "1-2 UTXOs"},
        "p90": {"min": 2, "max": 5, "display": "2-5 UTXOs"},
        "p95": {"min": 5, "max": 15, "display": "5-15 UTXOs"},
        "p99": {"min": 15, "max": 104370, "display": ">15 UTXOs"}
      }
    },
    "block_era": {
      "description": "Block Era (Age)",
      "unit": "era",
      "type": "era",
      "thresholds": {
        "shelley": {"min": 1, "max": 1, "display": "Shelley (epochs 0-3)"},
        "alonzo": {"min": 4, "max": 4, "display": "Alonzo (epochs 6-6)"},
        "conway": {"min": 6, "max": 6, "display": "Conway (epochs 12-162)"}
      }
    }
  }
}
```

## Output

### Capacity Test Output

```
testresults_YYYY-MM-DD_HH-MM_VERSION_DIMENSION_SUFFIX/
├── details_results.csv      # Per-concurrency metrics
├── summary_results.csv      # Max concurrency per dimension/level
├── details_results.md       # Markdown details table
├── summary_results.md       # Markdown summary with insights
├── ab_commands.log          # All ab commands executed
├── stability_test.log       # Full test log
└── *.json                   # Request payloads per endpoint
```

### Example Output

```
====================================================================================================
 CAPACITY TEST RESULTS - DIMENSION ISOLATED
====================================================================================================

ADDRESS UTXO COUNT
----------------------------------------------------------------------------------------------------
Level    Range                     Endpoint                  Max Conc        p95        p99      Req/s
----------------------------------------------------------------------------------------------------
p50      ≤85 UTXOs                 /account/balance               128       23ms       31ms     450.20
p75      85-280 UTXOs              /account/balance                64       67ms       89ms     180.50
p90      280-847 UTXOs             /account/balance                32      156ms      234ms      78.30
p95      847-2100 UTXOs            /account/balance                16      423ms      678ms      28.10
p99      >2100 UTXOs               /account/balance                 4     1890ms     2450ms       3.20

BLOCK ERA (AGE)
----------------------------------------------------------------------------------------------------
Level    Range                     Endpoint                  Max Conc        p95        p99      Req/s
----------------------------------------------------------------------------------------------------
conway   Conway (epochs 12-162)    /block                        128       34ms       45ms     380.00
babbage  Babbage (epochs 7-11)     /block                         64       89ms      123ms     150.00
alonzo   Alonzo (epochs 6-6)       /block                         32      234ms      345ms      60.00
shelley  Shelley (epochs 0-3)      /block                          8      890ms     1234ms      12.00
```

**Insights from dimension-isolated testing:**
- UTXO count: Performance degrades 14x from p50 to p99
- Block era: Old era blocks (Byron/Shelley) are 26x slower than current era
- These insights help identify specific optimization targets

## Architecture

```
tests/load-tests/
├── stability_test.py      # Capacity testing (dimension-isolated, ab-based)
├── soak_test.py           # Soak testing CLI (Locust wrapper)
├── locustfile.py          # Locust test definitions
├── populate_data.py       # Dimension-isolated data generator
├── pyproject.toml         # Python dependencies (uv)
├── .env.example           # Environment template
└── data/
    ├── {network}/
    │   ├── dimensions.json    # Thresholds metadata
    │   ├── addresses/         # Address dimension CSVs
    │   ├── blocks/            # Block dimension CSVs
    │   └── transactions/      # Transaction dimension CSVs
    └── {network}-data.csv     # Legacy single-row data
```

## Troubleshooting

### "CSV file not found" error

Run `populate_data.py` first to generate dimension-isolated data:

```bash
./populate_data.py --network preprod --db-url postgresql://...
```

### "dimensions.json not found" warning

The test will continue without friendly display names. Run `populate_data.py` to generate it.

### High error rates during capacity test

- API may be under-provisioned for that dimension/level
- Reduce concurrency levels to find stable point
- Check API logs for specific errors

### Understanding results

- **p50 results** = typical/median case performance
- **p99 results** = worst-case performance (addresses with most UTXOs, etc.)
- **Performance ratio** (p50 vs p99) indicates optimization potential

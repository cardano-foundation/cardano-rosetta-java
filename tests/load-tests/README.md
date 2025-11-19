# Locust Load Testing for Cardano Rosetta API

This directory contains a **prototype** Locust-based load testing setup for comparing against the existing Apache Bench (`ab`) stability tests.

## 🎯 Purpose (Spike Investigation)

This is a **spike** to evaluate whether Locust can provide better insights than Apache Bench by:

1. **Varying data per request** - avoiding database caching bias
2. **Categorizing data** - revealing performance patterns (light/medium/heavy loads)
3. **Tracking metrics by category** - identifying which data types are slow
4. **Providing richer metrics** - p95/p99, real-time UI, per-endpoint breakdown

## 🏗️ Architecture

```
tests/load-tests/
├── pyproject.toml       # uv dependencies (locust, python-dotenv)
├── locustfile.py        # Main load test with all 7 endpoints
├── test_data.py         # Categorized test data (light/medium/heavy)
└── README.md           # This file
```

## 📦 Setup

```bash
cd tests/load-tests

# Install dependencies with uv
uv sync

# Activate virtual environment
source .venv/bin/activate
```

## 🗄️ Populate Test Data

Before running load tests, you need to populate `test_data.py` with real preprod data.

### Step 1: Port-forward the Yaci Store Database

```bash
# SSH into preview machine and forward PostgreSQL port
ssh -L 5432:localhost:5432 preview
```

### Step 2: Configure Database Connection

```bash
# Copy example environment file
cp .env.example .env

# Edit .env with your database credentials
# DB_HOST=localhost
# DB_PORT=5432
# DB_NAME=preprod
# DB_USER=postgres
# DB_PASSWORD=your_password
```

### Step 3: Run the Population Script

```bash
uv run python populate_test_data.py
```

This will:
- Query Yaci Store database for diverse addresses/blocks/transactions
- Categorize by performance characteristics (light/medium/heavy)
- Generate `test_data.py` with real preprod data

**Expected output:**
```
📍 Querying addresses...
  ✓ Light addresses (1-10 UTXOs): 10
  ✓ Medium addresses (100-1K UTXOs): 10
  ✓ Heavy addresses (10K+ UTXOs): 10

🧱 Querying blocks...
  ✓ Light blocks (1-5 txs): 5
  ✓ Heavy blocks (100+ txs): 5

📄 Querying transactions...
  ✓ Small transactions (<500 bytes): 10
  ✓ Large transactions (>10KB): 10
```

## 🚀 Usage

### 1. Port-forward Preprod Rosetta Instance

```bash
# SSH into preview server and forward port 8082
ssh -L 8082:localhost:8082 preview
```

### 2. Run Locust

**Web UI Mode (Recommended for exploration):**
```bash
uv run locust --host=http://localhost:8082
```

Then open http://localhost:8089 in your browser to:
- Set number of users
- Set spawn rate
- Monitor real-time metrics
- View charts and breakdowns

**Headless Mode (CI/CD friendly):**
```bash
uv run locust --host=http://localhost:8082 \
    --users 50 \
    --spawn-rate 5 \
    --run-time 300s \
    --headless
```

**Generate HTML Report:**
```bash
uv run locust --host=http://localhost:8082 \
    --users 50 \
    --spawn-rate 5 \
    --run-time 300s \
    --headless \
    --html=report.html \
    --csv=results
```

This creates:
- `report.html` - Interactive HTML report
- `results_stats.csv` - Request statistics
- `results_stats_history.csv` - Time-series data
- `results_failures.csv` - Failure details

## 📊 Metrics Provided

Locust provides these metrics **out of the box**:

- **Response time percentiles**: p50, p66, p75, p80, p90, p95, p99
- **Throughput**: Requests per second (RPS)
- **Failure rate**: Count and percentage
- **Per-endpoint breakdown**: All metrics split by endpoint
- **Per-category breakdown**: Metrics split by data category (light/medium/heavy)

### Example Output:

```
/account/balance [light]
  Requests: 7000
  Failures: 0
  Avg: 45.23ms
  p95: 89.12ms
  p99: 123.45ms

/account/balance [heavy]
  Requests: 1000
  Failures: 0
  Avg: 456.78ms
  p95: 890.12ms
  p99: 1234.56ms
```

This clearly shows that **heavy addresses (10K+ UTXOs) are ~10x slower** - something that Apache Bench's identical payloads would miss!

## 🎭 Comparison with Apache Bench

| Feature | Apache Bench | Locust |
|---------|-------------|---------|
| **Data variation** | ❌ Identical payload | ✅ Categorized data |
| **Cache bias** | ❌ Heavy caching | ✅ Avoids caching |
| **Percentiles** | ✅ p95, p99 | ✅ p50-p99 |
| **Real-time UI** | ❌ CLI only | ✅ Web UI |
| **Endpoint weights** | ❌ Manual | ✅ Task decorators |
| **Category tracking** | ❌ Not possible | ✅ Built-in |
| **CI/CD** | ✅ Scriptable | ✅ Headless mode |
| **Reports** | 📊 Text output | 📊 HTML + CSV |

## 📝 Test Data Structure

Data is organized in `test_data.py` by **categories**:

```python
ADDRESSES = {
    "light": [...],   # 1-10 UTXOs (fast)
    "medium": [...],  # 100-1K UTXOs (moderate)
    "heavy": [...]    # 10K+ UTXOs (slow)
}

BLOCKS = {
    "light": [...],   # 1-5 transactions
    "heavy": [...]    # 100+ transactions
}

TRANSACTIONS = {
    "small": [...],   # <500 bytes
    "large": [...]    # >10KB
}
```

**Weights** control distribution:
```python
CATEGORY_WEIGHTS = {
    "address_light": 0.7,   # 70% of requests
    "address_heavy": 0.1,   # 10% of requests
}
```

## 🔧 Next Steps (TODOs)

- [ ] **Populate test_data.py** with actual preprod addresses/blocks/transactions
  - Query preprod Rosetta to find addresses with varying UTXO counts
  - Identify light vs heavy blocks
  - Categorize transactions by size
- [ ] **Run comparison test**: ab vs Locust with identical vs varied data
- [ ] **Document findings**: metrics differences, insights, recommendations
- [ ] **Decide**: Full migration? Hybrid approach? Keep current ab tests?

## 🎯 Success Criteria

This spike is successful if:

1. ✅ Locust can vary data per request
2. ✅ Metrics reveal performance degradation patterns by category
3. ✅ p95/p99 metrics match or exceed ab capabilities
4. ✅ CI/CD integration path is clear
5. ⏳ Comparison shows meaningful differences vs ab

## 🚫 Out of Scope (for this spike)

- Full implementation (prototype only)
- Grafana/monitoring integration
- Automated CSV generation
- Full endpoint coverage (7 endpoints is enough for spike)

## 📚 Resources

- [Locust Documentation](https://docs.locust.io/)
- [Task #638: Replace ab with Locust](https://github.com/cardano-foundation/cardano-rosetta-java/issues/638)
- Existing ab tests: `../../load-tests/stability_test.py`

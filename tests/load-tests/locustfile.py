"""
Locust load testing for Cardano Rosetta API with categorized data.

This demonstrates the key advantage over Apache Bench: ability to cycle through
CATEGORIZED test data per request, revealing performance patterns and avoiding
database caching bias.

The data is organized by categories (light/medium/heavy) to measure how
performance degrades with data complexity (UTXO count, block size, tx size).

Usage:
    # Port-forward preprod Rosetta instance first:
    ssh -L 8082:localhost:8082 preview

    # Web UI mode (real-time monitoring)
    uv run locust --host=http://localhost:8082

    # Headless mode (CI/CD friendly)
    uv run locust --host=http://localhost:8082 \
        --users 10 --spawn-rate 2 --run-time 60s --headless

    # Generate HTML report
    uv run locust --host=http://localhost:8082 \
        --users 50 --spawn-rate 5 --run-time 300s --headless \
        --html=report.html
"""

import os
import random
import csv
import json
from pathlib import Path
from typing import Dict, List, Tuple

from locust import HttpUser, between, events, task
import locust.stats as locust_stats

SCRIPT_DIR = Path(__file__).parent

def load_csv(csv_file: Path) -> List[Dict]:
    """Load CSV file, return list of dicts."""
    with open(csv_file) as f:
        return list(csv.DictReader(f))

def load_test_data():
    """Load all dimension/level data from dimensions.json and CSV files."""
    network = os.environ.get('ROSETTA_NETWORK', 'mainnet')
    data_dir = SCRIPT_DIR / 'data' / network

    with open(data_dir / 'dimensions.json') as f:
        dims = json.load(f)['dimensions']

    addresses, blocks, transactions = {}, {}, {}
    weights = {}

    # Map dimension subdirs to output dicts
    subdir_map = {'addresses': addresses, 'blocks': blocks, 'transactions': transactions}

    for dim_name, dim_config in dims.items():
        # Determine subdir based on endpoints
        endpoints_str = str(dim_config['endpoints'])
        if '/account/' in endpoints_str:
            subdir = 'addresses'
        elif '/block/transaction' in endpoints_str or '/search/transactions' in endpoints_str:
            subdir = 'transactions'
        else:
            subdir = 'blocks'

        target = subdir_map[subdir]

        # Load CSV for each level
        for level in dim_config['thresholds'].keys():
            cat = f"{dim_name}_{level}"
            csv_file = data_dir / subdir / f"{cat}.csv"

            if not csv_file.exists():
                continue

            rows = load_csv(csv_file)

            # Convert to format expected by tasks
            if subdir == 'addresses':
                target[cat] = [r['address'] for r in rows]
            elif subdir == 'blocks':
                target[cat] = [{'index': int(r['block_index']), 'hash': r['block_hash']} for r in rows]
            else:  # transactions
                target[cat] = [{'hash': r['transaction_hash'], 'block_index': int(r['block_index']),
                                'block_hash': r['block_hash']} for r in rows]

            # Uniform weights (can tune later based on real distribution)
            prefix = {'addresses': 'address', 'blocks': 'block', 'transactions': 'tx'}[subdir]
            weights[f"{prefix}_{cat}"] = 1.0

    # Construction metadata (hardcoded - not dimension-based)
    construction_metadata = {
        "small_tx": [{"transaction_size": 500, "relative_ttl": 1000}],
        "large_tx": [{"transaction_size": 15000, "relative_ttl": 3600}],
    }
    weights.update({"construction_small_tx": 0.7, "construction_large_tx": 0.3})

    return network, addresses, blocks, transactions, weights, construction_metadata

NETWORK, ADDRESSES, BLOCKS, TRANSACTIONS, CATEGORY_WEIGHTS, CONSTRUCTION_METADATA = load_test_data()
DEFAULT_NETWORK = NETWORK


def _log_response_time_no_bucketing(self, response_time: int) -> None:
    """
    Copy of StatsEntry._log_response_time but without bucketing into 10/100/1000ms bins.
    Keeps full millisecond precision so percentiles don’t all end in 000.
    """
    if response_time is None:
        self.num_none_requests += 1
        return

    self.total_response_time += response_time

    if self.min_response_time is None:
        self.min_response_time = response_time
    else:
        self.min_response_time = min(self.min_response_time, response_time)
    self.max_response_time = max(self.max_response_time, response_time)

    rounded_response_time = int(response_time)
    self.response_times[rounded_response_time] += 1


# Patch Locust to keep per-request response times at ms precision (no bucket rounding)
locust_stats.StatsEntry._log_response_time = _log_response_time_no_bucketing


class CategorizedDataProvider:
    """
    Provides test data based on categories and weights.

    This allows us to:
    1. Control distribution (70% light, 20% medium, 10% heavy)
    2. Track performance by category
    3. Reveal degradation patterns
    """

    @staticmethod
    def get_weighted_choice(
        items_dict: Dict[str, List], weight_prefix: str
    ) -> Tuple[str, any]:
        """
        Select an item from a categorized dictionary based on weights.

        Returns: (category, item)
        """
        categories = list(items_dict.keys())
        weights = [
            CATEGORY_WEIGHTS.get(f"{weight_prefix}_{cat}", 1.0) for cat in categories
        ]

        # Normalize weights to sum to 1.0
        total = sum(weights)
        normalized_weights = [w / total for w in weights]

        # Choose category based on weights
        category = random.choices(categories, weights=normalized_weights)[0]

        # Choose random item from that category
        items = items_dict[category]
        if not items:
            # Fallback to first available category if selected category is empty
            for cat, cat_items in items_dict.items():
                if cat_items:
                    return cat, random.choice(cat_items)
            raise ValueError(f"No data available in {weight_prefix}")

        item = random.choice(items)
        return category, item


data_provider = CategorizedDataProvider()


class RosettaUser(HttpUser):
    """
    Simulates a user making requests to Cardano Rosetta API.

    Task weights simulate realistic traffic distribution:
    - Account Balance: 10x (most common)
    - Account Coins: 8x
    - Search Transactions: 5x
    - Block: 5x
    - Block Transaction: 3x
    - Construction Metadata: 2x
    - Network Status: 1x (baseline)
    """

    # Wait 1-3 seconds between requests (simulates real user behavior)
    wait_time = between(1, 3)

    @task(1)
    def network_status(self):
        """
        Test /network/status endpoint.

        Weight: 1 (baseline)
        No data variation needed.
        """
        payload = {
            "network_identifier": {"blockchain": "cardano", "network": NETWORK},
            "metadata": {},
        }

        with self.client.post(
            "/network/status", json=payload, catch_response=True, name="/network/status"
        ) as response:
            if response.status_code == 200:
                response.success()
            else:
                response.failure(f"Status {response.status_code}")

    @task(10)
    def account_balance(self):
        """
        Test /account/balance with CATEGORIZED addresses.

        Weight: 10 (most frequent)
        Reveals performance degradation with UTXO count:
        - Light addresses: fast
        - Medium addresses: moderate
        - Heavy addresses: slow (10K+ UTXOs)
        """
        category, address = data_provider.get_weighted_choice(ADDRESSES, "address")

        payload = {
            "network_identifier": {"blockchain": "cardano", "network": NETWORK},
            "account_identifier": {"address": address},
        }

        with self.client.post(
            "/account/balance",
            json=payload,
            catch_response=True,
            name=f"/account/balance [{category}]",  # Track by category
        ) as response:
            if response.status_code == 200:
                response.success()
            else:
                response.failure(f"Status {response.status_code}")

    @task(8)
    def account_coins(self):
        """
        Test /account/coins with CATEGORIZED addresses.

        Weight: 8
        Similar to account/balance but includes mempool.
        """
        category, address = data_provider.get_weighted_choice(ADDRESSES, "address")

        payload = {
            "network_identifier": {"blockchain": "cardano", "network": NETWORK},
            "account_identifier": {"address": address},
        }

        with self.client.post(
            "/account/coins",
            json=payload,
            catch_response=True,
            name=f"/account/coins [{category}]",
        ) as response:
            if response.status_code == 200:
                response.success()
            else:
                response.failure(f"Status {response.status_code}")

    @task(5)
    def block(self):
        """
        Test /block with CATEGORIZED blocks.

        Weight: 5
        Reveals performance degradation with block size:
        - Light blocks (1-5 txs): fast
        - Heavy blocks (100+ txs): slow
        """
        category, block = data_provider.get_weighted_choice(BLOCKS, "block")

        payload = {
            "network_identifier": {"blockchain": "cardano", "network": NETWORK},
            "block_identifier": {"index": block["index"], "hash": block["hash"]},
        }

        with self.client.post(
            "/block", json=payload, catch_response=True, name=f"/block [{category}]"
        ) as response:
            if response.status_code == 200:
                response.success()
            else:
                response.failure(f"Status {response.status_code}")

    @task(3)
    def block_transaction(self):
        """
        Test /block/transaction with CATEGORIZED transactions.

        Weight: 3
        Reveals performance with transaction complexity.
        """
        category, tx = data_provider.get_weighted_choice(TRANSACTIONS, "tx")

        payload = {
            "network_identifier": {"blockchain": "cardano", "network": NETWORK},
            "block_identifier": {"index": tx["block_index"], "hash": tx["block_hash"]},
            "transaction_identifier": {"hash": tx["hash"]},
        }

        with self.client.post(
            "/block/transaction",
            json=payload,
            catch_response=True,
            name=f"/block/transaction [{category}]",
        ) as response:
            if response.status_code == 200:
                response.success()
            else:
                response.failure(f"Status {response.status_code}")

    @task(5)
    def search_transactions(self):
        """
        Test /search/transactions with CATEGORIZED transactions.

        Weight: 5
        """
        category, tx = data_provider.get_weighted_choice(TRANSACTIONS, "tx")

        payload = {
            "network_identifier": {"blockchain": "cardano", "network": NETWORK},
            "transaction_identifier": {"hash": tx["hash"]},
        }

        with self.client.post(
            "/search/transactions",
            json=payload,
            catch_response=True,
            name=f"/search/transactions [{category}]",
        ) as response:
            if response.status_code == 200:
                response.success()
            else:
                response.failure(f"Status {response.status_code}")

    @task(2)
    def construction_metadata(self):
        """
        Test /construction/metadata with CATEGORIZED sizes.

        Weight: 2
        Reveals performance with transaction size estimation.
        """
        category, metadata = data_provider.get_weighted_choice(
            CONSTRUCTION_METADATA, "construction"
        )

        payload = {
            "network_identifier": {"blockchain": "cardano", "network": NETWORK},
            "options": {
                "transaction_size": metadata["transaction_size"],
                "relative_ttl": metadata["relative_ttl"],
            },
        }

        with self.client.post(
            "/construction/metadata",
            json=payload,
            catch_response=True,
            name=f"/construction/metadata [{category}]",
        ) as response:
            if response.status_code == 200:
                response.success()
            else:
                response.failure(f"Status {response.status_code}")


@events.test_start.add_listener
def on_test_start(environment, **kwargs):
    """Event handler called when test starts."""
    print("=" * 80)
    print("Starting Locust load test")
    print(f"Target: {environment.host}")
    print(f"Network: {NETWORK}")
    print("Data categories loaded:")
    print(f"  - Addresses: {sum(len(v) for v in ADDRESSES.values())} total")
    for cat, items in ADDRESSES.items():
        print(
            f"    - {cat}: {len(items)} items (weight: {CATEGORY_WEIGHTS.get(f'address_{cat}', 1.0)})"
        )
    print(f"  - Blocks: {sum(len(v) for v in BLOCKS.values())} total")
    for cat, items in BLOCKS.items():
        print(
            f"    - {cat}: {len(items)} items (weight: {CATEGORY_WEIGHTS.get(f'block_{cat}', 1.0)})"
        )
    print(f"  - Transactions: {sum(len(v) for v in TRANSACTIONS.values())} total")
    for cat, items in TRANSACTIONS.items():
        print(
            f"    - {cat}: {len(items)} items (weight: {CATEGORY_WEIGHTS.get(f'tx_{cat}', 1.0)})"
        )
    print("=" * 80)


@events.test_stop.add_listener
def on_test_stop(environment, **kwargs):
    """Event handler called when test stops."""
    print("=" * 80)
    print("Load test completed")
    print(f"Total requests: {environment.stats.total.num_requests}")
    print(f"Total failures: {environment.stats.total.num_failures}")
    print(f"Average response time: {environment.stats.total.avg_response_time:.2f}ms")
    print(f"p95: {environment.stats.total.get_response_time_percentile(0.95):.2f}ms")
    print(f"p99: {environment.stats.total.get_response_time_percentile(0.99):.2f}ms")
    print(f"Requests/sec: {environment.stats.total.total_rps:.2f}")
    print("=" * 80)
    print("\nPer-endpoint breakdown:")
    print("-" * 80)
    for name, stats in environment.stats.entries.items():
        if stats.num_requests > 0:
            print(f"{name}")
            print(f"  Requests: {stats.num_requests}")
            print(f"  Failures: {stats.num_failures}")
            print(f"  Avg: {stats.avg_response_time:.2f}ms")
            print(f"  p95: {stats.get_response_time_percentile(0.95):.2f}ms")
            print(f"  p99: {stats.get_response_time_percentile(0.99):.2f}ms")
    print("=" * 80)

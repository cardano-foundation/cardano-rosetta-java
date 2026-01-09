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
    """Load test data organized by endpoint from dimensions.json."""
    network = os.environ.get('ROSETTA_NETWORK', 'mainnet')
    data_dir = SCRIPT_DIR / 'data' / network

    with open(data_dir / 'dimensions.json') as f:
        dims = json.load(f)['dimensions']

    # Data organized by endpoint: {'/account/balance': {'utxo_count_1': [...], ...}}
    endpoint_data = {}
    weights = {}

    # Row parsers by data_type
    parsers = {
        'addresses': lambda r: r['address'],
        'blocks': lambda r: {'index': int(r['block_index']), 'hash': r['block_hash']},
        'transactions': lambda r: {'hash': r['transaction_hash'], 'block_index': int(r['block_index']), 'block_hash': r['block_hash']},
    }

    for dim_name, dim_config in dims.items():
        data_type = dim_config['data_type']
        endpoints = dim_config['endpoints']
        parse = parsers[data_type]

        for level in dim_config['thresholds'].keys():
            cat = f"{dim_name}_{level}"
            csv_file = data_dir / data_type / f"{cat}.csv"
            if not csv_file.exists():
                continue

            items = [parse(r) for r in load_csv(csv_file)]
            weights[cat] = 1.0

            for endpoint in endpoints:
                endpoint_data.setdefault(endpoint, {})[cat] = items

    # Construction metadata (hardcoded)
    construction_metadata = {"small_tx": [{"transaction_size": 500, "relative_ttl": 1000}],
                             "large_tx": [{"transaction_size": 15000, "relative_ttl": 3600}]}
    weights.update({"construction_small_tx": 0.7, "construction_large_tx": 0.3})

    return network, endpoint_data, weights, construction_metadata

NETWORK, ENDPOINT_DATA, CATEGORY_WEIGHTS, CONSTRUCTION_METADATA = load_test_data()
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
        """Test /account/balance with categorized addresses. Weight: 10"""
        data = ENDPOINT_DATA.get('/account/balance', {})
        if not data:
            return
        category, address = data_provider.get_weighted_choice(data, "")
        payload = {"network_identifier": {"blockchain": "cardano", "network": NETWORK},
                   "account_identifier": {"address": address}}
        with self.client.post("/account/balance", json=payload, catch_response=True,
                              name=f"/account/balance [{category}]") as response:
            response.success() if response.status_code == 200 else response.failure(f"Status {response.status_code}")

    @task(8)
    def account_coins(self):
        """Test /account/coins with categorized addresses. Weight: 8"""
        data = ENDPOINT_DATA.get('/account/coins', {})
        if not data:
            return
        category, address = data_provider.get_weighted_choice(data, "")
        payload = {"network_identifier": {"blockchain": "cardano", "network": NETWORK},
                   "account_identifier": {"address": address}}
        with self.client.post("/account/coins", json=payload, catch_response=True,
                              name=f"/account/coins [{category}]") as response:
            response.success() if response.status_code == 200 else response.failure(f"Status {response.status_code}")

    @task(5)
    def block(self):
        """Test /block with categorized blocks. Weight: 5"""
        data = ENDPOINT_DATA.get('/block', {})
        if not data:
            return
        category, block = data_provider.get_weighted_choice(data, "")
        payload = {"network_identifier": {"blockchain": "cardano", "network": NETWORK},
                   "block_identifier": {"index": block["index"], "hash": block["hash"]}}
        with self.client.post("/block", json=payload, catch_response=True,
                              name=f"/block [{category}]") as response:
            response.success() if response.status_code == 200 else response.failure(f"Status {response.status_code}")

    @task(3)
    def block_transaction(self):
        """Test /block/transaction with categorized transactions. Weight: 3"""
        data = ENDPOINT_DATA.get('/block/transaction', {})
        if not data:
            return
        category, tx = data_provider.get_weighted_choice(data, "")
        payload = {"network_identifier": {"blockchain": "cardano", "network": NETWORK},
                   "block_identifier": {"index": tx["block_index"], "hash": tx["block_hash"]},
                   "transaction_identifier": {"hash": tx["hash"]}}
        with self.client.post("/block/transaction", json=payload, catch_response=True,
                              name=f"/block/transaction [{category}]") as response:
            response.success() if response.status_code == 200 else response.failure(f"Status {response.status_code}")

    @task(5)
    def search_transactions(self):
        """Test /search/transactions with categorized data. Weight: 5

        Uses tx_history (addresses) for address-based search,
        other dimensions (transactions) for hash-based search.
        """
        data = ENDPOINT_DATA.get('/search/transactions', {})
        if not data:
            return
        category, item = data_provider.get_weighted_choice(data, "")

        # tx_history uses address-based search, others use hash-based
        if category.startswith('tx_history_'):
            payload = {"network_identifier": {"blockchain": "cardano", "network": NETWORK},
                       "account_identifier": {"address": item}}
        else:
            payload = {"network_identifier": {"blockchain": "cardano", "network": NETWORK},
                       "transaction_identifier": {"hash": item["hash"]}}

        with self.client.post("/search/transactions", json=payload, catch_response=True,
                              name=f"/search/transactions [{category}]") as response:
            response.success() if response.status_code == 200 else response.failure(f"Status {response.status_code}")

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
    print(f"Locust load test | Target: {environment.host} | Network: {NETWORK}")
    print("Data loaded by endpoint:")
    for endpoint, categories in sorted(ENDPOINT_DATA.items()):
        total = sum(len(items) for items in categories.values())
        print(f"  {endpoint}: {len(categories)} categories, {total} items")
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

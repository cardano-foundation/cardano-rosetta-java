#!/usr/bin/env python3
"""
Helper script to populate test_data.py with real preprod data from Yaci Store DB.

Usage:
    1. Port-forward: ssh -L 5435:localhost:5435 preview
    2. Run: uv run python populate_test_data.py
"""

import os
import sys
from typing import List, Dict
from dotenv import load_dotenv

try:
    import psycopg2
    from psycopg2.extras import RealDictCursor
except ImportError:
    print("Error: psycopg2 not installed. Run: uv add psycopg2-binary")
    sys.exit(1)

load_dotenv()

DB_CONFIG = {
    'host': os.getenv('DB_HOST', 'localhost'),
    'port': os.getenv('DB_PORT', '5435'),
    'database': os.getenv('DB_NAME', 'rosetta-java'),
    'user': os.getenv('DB_USER', 'rosetta_db_admin'),
    'password': os.getenv('DB_PASSWORD', ''),
}

DATABASE_URL = os.getenv('DATABASE_URL')
TARGET_PER_CATEGORY = 10


class YaciDBQuerier:
    """Query Yaci Store database for categorized test data."""

    def __init__(self):
        self.conn = None
        self.schema = os.getenv('DB_SCHEMA', 'public')

    def connect(self):
        """Establish database connection."""
        try:
            if DATABASE_URL:
                self.conn = psycopg2.connect(DATABASE_URL)
            else:
                self.conn = psycopg2.connect(**DB_CONFIG)
            self.conn.autocommit = True  # Prevent transaction issues
            print(f"✓ Connected to database: {DB_CONFIG['database']}")
            print(f"✓ Using schema: {self.schema}")
        except Exception as e:
            print(f"✗ Database connection failed: {e}")
            sys.exit(1)

    def close(self):
        if self.conn:
            self.conn.close()

    def get_light_addresses(self) -> List[str]:
        """Find addresses with 1-10 UTXOs (fast queries)."""
        query = f"""
        SELECT owner_addr, COUNT(*) as utxo_count
        FROM {self.schema}.address_utxo
        WHERE owner_addr IS NOT NULL
        GROUP BY owner_addr
        HAVING COUNT(*) BETWEEN 1 AND 10
        ORDER BY RANDOM()
        LIMIT %s
        """
        return self._execute_query(query, (TARGET_PER_CATEGORY,), fetch_column='owner_addr')

    def get_medium_addresses(self) -> List[str]:
        """Find addresses with 100-1000 UTXOs (moderate load)."""
        query = f"""
        SELECT owner_addr, COUNT(*) as utxo_count
        FROM {self.schema}.address_utxo
        WHERE owner_addr IS NOT NULL
        GROUP BY owner_addr
        HAVING COUNT(*) BETWEEN 100 AND 1000
        ORDER BY RANDOM()
        LIMIT %s
        """
        return self._execute_query(query, (TARGET_PER_CATEGORY,), fetch_column='owner_addr')

    def get_heavy_addresses(self) -> List[str]:
        """Find addresses with 10000+ UTXOs (slow queries)."""
        query = f"""
        SELECT owner_addr, COUNT(*) as utxo_count
        FROM {self.schema}.address_utxo
        WHERE owner_addr IS NOT NULL
        GROUP BY owner_addr
        HAVING COUNT(*) >= 10000
        ORDER BY RANDOM()
        LIMIT %s
        """
        return self._execute_query(query, (TARGET_PER_CATEGORY,), fetch_column='owner_addr')

    def get_light_blocks(self) -> List[Dict]:
        """Find blocks with 1-5 transactions (fast to process)."""
        query = f"""
        SELECT number as index, hash, no_of_txs as tx_count
        FROM {self.schema}.block
        WHERE no_of_txs BETWEEN 1 AND 5
        ORDER BY RANDOM()
        LIMIT %s
        """
        return self._execute_query(query, (TARGET_PER_CATEGORY // 2,))

    def get_heavy_blocks(self) -> List[Dict]:
        """Find blocks with 100+ transactions (slow to process)."""
        query = f"""
        SELECT number as index, hash, no_of_txs as tx_count
        FROM {self.schema}.block
        WHERE no_of_txs >= 100
        ORDER BY RANDOM()
        LIMIT %s
        """
        return self._execute_query(query, (TARGET_PER_CATEGORY // 2,))

    def get_small_transactions(self) -> List[Dict]:
        """Find transactions with few inputs/outputs (simple)."""
        query = f"""
        SELECT
            t.tx_hash as hash,
            t.block as block_index,
            t.block_hash,
            jsonb_array_length(t.inputs) as input_count,
            jsonb_array_length(t.outputs) as output_count
        FROM {self.schema}.transaction t
        WHERE
            jsonb_array_length(t.inputs) <= 2
            AND jsonb_array_length(t.outputs) <= 2
            AND t.block IS NOT NULL
        ORDER BY RANDOM()
        LIMIT %s
        """
        return self._execute_query(query, (TARGET_PER_CATEGORY,))

    def get_large_transactions(self) -> List[Dict]:
        """Find transactions with many inputs/outputs (complex)."""
        query = f"""
        SELECT
            t.tx_hash as hash,
            t.block as block_index,
            t.block_hash,
            jsonb_array_length(t.inputs) as input_count,
            jsonb_array_length(t.outputs) as output_count
        FROM {self.schema}.transaction t
        WHERE
            (jsonb_array_length(t.inputs) >= 10
             OR jsonb_array_length(t.outputs) >= 10)
            AND t.block IS NOT NULL
        ORDER BY RANDOM()
        LIMIT %s
        """
        return self._execute_query(query, (TARGET_PER_CATEGORY,))

    def _execute_query(self, query: str, params: tuple = (), fetch_column: str = None) -> List:
        """Execute query and return results."""
        try:
            with self.conn.cursor(cursor_factory=RealDictCursor) as cur:
                cur.execute(query, params)
                results = cur.fetchall()

                if fetch_column:
                    return [row[fetch_column] for row in results if row[fetch_column]]
                else:
                    return [dict(row) for row in results]

        except Exception as e:
            print(f"✗ Query failed: {e}")
            return []


def generate_test_data_file(querier: YaciDBQuerier):
    """Generate test_data.py with real preprod data."""

    print("\n" + "=" * 80)
    print("Querying Yaci Store database for categorized test data...")
    print("=" * 80)

    print("\n📍 Querying addresses...")
    light_addrs = querier.get_light_addresses()
    print(f"  ✓ Light addresses (1-10 UTXOs): {len(light_addrs)}")

    medium_addrs = querier.get_medium_addresses()
    print(f"  ✓ Medium addresses (100-1K UTXOs): {len(medium_addrs)}")

    heavy_addrs = querier.get_heavy_addresses()
    print(f"  ✓ Heavy addresses (10K+ UTXOs): {len(heavy_addrs)}")

    print("\n🧱 Querying blocks...")
    light_blocks = querier.get_light_blocks()
    print(f"  ✓ Light blocks (1-5 txs): {len(light_blocks)}")

    heavy_blocks = querier.get_heavy_blocks()
    print(f"  ✓ Heavy blocks (100+ txs): {len(heavy_blocks)}")

    print("\n📄 Querying transactions...")
    small_txs = querier.get_small_transactions()
    print(f"  ✓ Small transactions (few inputs/outputs): {len(small_txs)}")

    large_txs = querier.get_large_transactions()
    print(f"  ✓ Large transactions (many inputs/outputs): {len(large_txs)}")

    print("\n📝 Generating test_data.py...")

    content = f'''"""
Test data for Locust load testing - Preprod Network.

AUTO-GENERATED by populate_test_data.py from Yaci Store database.
DO NOT EDIT MANUALLY - regenerate by running: uv run python populate_test_data.py
"""

# Network configuration
NETWORK = "preprod"

# Addresses categorized by UTXO count
ADDRESSES = {{
    "light": {light_addrs},
    "medium": {medium_addrs},
    "heavy": {heavy_addrs},
}}

# Blocks categorized by transaction count
BLOCKS = {{
    "light": {light_blocks},
    "heavy": {heavy_blocks},
}}

# Transactions categorized by complexity (inputs/outputs count)
TRANSACTIONS = {{
    "small": {small_txs},
    "large": {large_txs},
}}

# Construction metadata test cases
CONSTRUCTION_METADATA = {{
    "small_tx": [
        {{"transaction_size": 500, "relative_ttl": 1000}},
        {{"transaction_size": 800, "relative_ttl": 1500}},
    ],
    "large_tx": [
        {{"transaction_size": 15000, "relative_ttl": 3600}},
        {{"transaction_size": 20000, "relative_ttl": 7200}},
    ],
}}

# Weights control the distribution of requests across categories
CATEGORY_WEIGHTS = {{
    "address_light": 0.7,
    "address_medium": 0.2,
    "address_heavy": 0.1,

    "block_light": 0.8,
    "block_heavy": 0.2,

    "tx_small": 0.7,
    "tx_large": 0.3,

    "construction_small": 0.6,
    "construction_large": 0.4,
}}
'''

    with open('test_data.py', 'w') as f:
        f.write(content)

    print("  ✓ test_data.py generated successfully!")

    print("\n" + "=" * 80)
    print("Summary:")
    print("=" * 80)
    print(f"Total addresses: {len(light_addrs) + len(medium_addrs) + len(heavy_addrs)}")
    print(f"Total blocks: {len(light_blocks) + len(heavy_blocks)}")
    print(f"Total transactions: {len(small_txs) + len(large_txs)}")
    print("\n✓ Ready for load testing!")
    print("\nNext steps:")
    print("  1. Review test_data.py to verify data quality")
    print("  2. Port-forward Rosetta API: ssh -L 8082:localhost:8082 preview")
    print("  3. Run Locust: uv run locust --host=http://localhost:8082")


def main():
    print("Preprod Load Test Data Generator")
    print("=" * 80)

    querier = YaciDBQuerier()

    try:
        querier.connect()
        generate_test_data_file(querier)
    finally:
        querier.close()


if __name__ == "__main__":
    main()

#!/usr/bin/env python3
"""
Dimension-isolated data generator for Cardano Rosetta load tests.

Generates test data organized by isolated dimensions with percentile-based levels.
Each dimension (utxo_count, tx_history, tx_count, etc.) is tested independently
to identify specific performance bottlenecks.

Usage:
    # Using DATABASE_URL environment variable
    ./populate_data.py --network preprod

    # Using individual DB parameters
    ./populate_data.py --network mainnet --db-host localhost --db-port 5432 --db-name rosetta

    # Port-forward first if needed
    ssh -L 5432:localhost:5432 preview
"""

import argparse
import json
import os
import sys
from datetime import datetime
from typing import Dict, List, Tuple, Optional

from dotenv import load_dotenv

try:
    import psycopg2
    from psycopg2.extras import RealDictCursor
except ImportError:
    print("Error: psycopg2 not installed. Run: uv add psycopg2-binary")
    sys.exit(1)

load_dotenv()

# Percentile levels to generate for numeric dimensions
# p50 = median (typical case), p99 = worst 1% (stress test heavy tail)
PERCENTILE_LEVELS = ['p50', 'p75', 'p90', 'p95', 'p99']

# Number of data points to collect per level
TARGET_PER_LEVEL = 20

# Cardano era names (protocol constants, not stored in DB)
# The `era` table only stores era number, start_slot, block info
ERA_NAMES = {
    1: "Byron",
    2: "Shelley",
    3: "Allegra",
    4: "Mary",
    5: "Alonzo",
    6: "Babbage",
    7: "Conway",
}

# Dimension definitions with their endpoints and query types
# Types:
#   'percentile'  - p50, p75, p90, p95, p99 based on actual data distribution
#                   Best for log-normal distributions (R² < 0.95)
#   'power_of_10' - 1, 10, 100, 1000, ... based on powers of 10
#                   Best for power-law distributions (R² >= 0.95)
#   'boolean'     - true/false
#   'era'         - era names from ERA_NAMES constant (Byron, Shelley, Allegra, etc.)
DIMENSIONS = {
    # Address dimensions
    'utxo_count': {
        'description': 'Address UTXO Count',
        'unit': 'UTXOs',
        'endpoints': ['/account/balance', '/account/coins'],
        'type': 'power_of_10',  # R² >= 0.95 (power-law)
    },
    'token_count': {
        'description': 'Address Distinct Token Types',
        'unit': 'token types',
        'endpoints': ['/account/balance', '/account/coins'],
        'type': 'power_of_10',  # R² >= 0.95 (power-law)
    },
    'tx_history': {
        'description': 'Address Transaction History',
        'unit': 'transactions',
        'endpoints': ['/search/transactions'],
        'type': 'power_of_10',  # R² >= 0.95 (power-law)
    },
    # Block dimensions
    'block_tx_count': {
        'description': 'Block Transaction Count',
        'unit': 'transactions',
        'endpoints': ['/block'],
        'type': 'percentile',  # R² = 0.89
    },
    'block_body_size': {
        'description': 'Block Body Size',
        'unit': 'bytes',
        'endpoints': ['/block'],
        'type': 'percentile',  # R² = 0.61
    },
    'block_era': {
        'description': 'Block Era (Age)',
        'unit': 'era',
        'endpoints': ['/block'],
        'type': 'era',  # Categorical - era names from ERA_NAMES constant
    },
    # Transaction dimensions
    'tx_io_count': {
        'description': 'Transaction I/O Count',
        'unit': 'inputs+outputs',
        'endpoints': ['/block/transaction', '/search/transactions'],
        'type': 'power_of_10',  # R² >= 0.95
    },
    'tx_has_script': {
        'description': 'Transaction Has Plutus Script',
        'unit': 'boolean',
        'endpoints': ['/block/transaction', '/search/transactions'],
        'type': 'boolean',
    },
    'tx_token_count': {
        'description': 'Transaction Token Types',
        'unit': 'token types',
        'endpoints': ['/block/transaction', '/search/transactions'],
        'type': 'percentile',  # R² = 0.90 (mainnet)
    },
}


class YaciStoreQuerier:
    """Query Yaci Store database for dimension-isolated test data."""

    def __init__(self, db_config: Dict[str, str]):
        self.db_config = db_config
        self.conn = None
        self.schema = db_config.get('schema', 'public')
        self._percentile_cache: Dict[str, Dict] = {}
        self._power_of_10_cache: Dict[str, Dict] = {}
        self._era_cache: Optional[Dict] = None
        self._reference_block: Optional[Dict] = None

    def connect(self):
        """Establish database connection."""
        try:
            if 'database_url' in self.db_config and self.db_config['database_url']:
                self.conn = psycopg2.connect(self.db_config['database_url'])
            else:
                connect_params = {
                    'host': self.db_config['host'],
                    'port': self.db_config['port'],
                    'database': self.db_config['database'],
                    'user': self.db_config['user'],
                    'password': self.db_config['password'],
                }
                self.conn = psycopg2.connect(**connect_params)
            self.conn.autocommit = True
            print(f"Connected to database: {self.db_config.get('database', 'from DATABASE_URL')}")
            print(f"Using schema: {self.schema}")
            self._verify_schema()
        except Exception as e:
            print(f"Database connection failed: {e}")
            sys.exit(1)

    def _verify_schema(self):
        """Verify required tables exist in the specified schema."""
        required_tables = ['transaction', 'block', 'address_utxo', 'epoch_param']
        with self.conn.cursor() as cur:
            cur.execute("""
                SELECT table_name FROM information_schema.tables
                WHERE table_schema = %s AND table_name = ANY(%s)
            """, (self.schema, required_tables))
            found_tables = [row[0] for row in cur.fetchall()]

        missing_tables = set(required_tables) - set(found_tables)
        if missing_tables:
            print(f"\n  WARNING: Missing tables in schema '{self.schema}': {missing_tables}")
        else:
            print(f"  Tables verified: {', '.join(found_tables)}")

    def close(self):
        if self.conn:
            self.conn.close()

    def get_reference_block(self) -> Dict:
        """Get and cache the current blockchain tip as reference block.

        All data collection uses this block as the reference point to ensure
        deterministic test results. Tests should query using this block_identifier.
        """
        if self._reference_block is not None:
            return self._reference_block

        query = f"""
        SELECT number as index, hash
        FROM {self.schema}.block
        ORDER BY number DESC
        LIMIT 1
        """
        results = self._execute_query(query, (), description="reference block (tip)")
        if not results:
            raise RuntimeError("Could not get blockchain tip - database may be empty")

        self._reference_block = {
            'index': results[0]['index'],
            'hash': results[0]['hash']
        }
        print(f"  Reference block: {self._reference_block['index']} ({self._reference_block['hash'][:16]}...)")
        return self._reference_block

    def _execute_query(self, query: str, params: tuple, description: str = "") -> List[Dict]:
        """Execute query and return results as list of dicts."""
        import time
        start = time.time()
        if description:
            print(f"    [{description}] ", end='', flush=True)
        try:
            with self.conn.cursor(cursor_factory=RealDictCursor) as cur:
                cur.execute(query, params)
                result = [dict(row) for row in cur.fetchall()]
                elapsed = time.time() - start
                if description:
                    print(f"OK ({elapsed:.1f}s, {len(result)} rows)")
                return result
        except Exception as e:
            elapsed = time.time() - start
            print(f"FAILED ({elapsed:.1f}s)")
            print(f"    Error: {e}")
            print(f"    Query: {query[:300]}...")
            raise RuntimeError(f"Query failed: {description or 'unnamed'} - {e}") from e

    # =========================================================================
    # Percentile Threshold Queries
    # =========================================================================

    def get_percentile_thresholds(self, dimension: str) -> Dict[str, Dict]:
        """
        Query percentile thresholds for a dimension from actual data.
        Returns dict: level -> {min, max, display, description}
        """
        if dimension in self._percentile_cache:
            return self._percentile_cache[dimension]

        if dimension == 'utxo_count':
            result = self._get_utxo_count_percentiles()
        elif dimension == 'token_count':
            result = self._get_token_count_percentiles()
        elif dimension == 'tx_history':
            result = self._get_tx_history_percentiles()
        elif dimension == 'block_tx_count':
            result = self._get_block_tx_count_percentiles()
        elif dimension == 'block_body_size':
            result = self._get_block_body_size_percentiles()
        elif dimension == 'tx_io_count':
            result = self._get_tx_io_count_percentiles()
        elif dimension == 'tx_token_count':
            result = self._get_tx_token_count_percentiles()
        else:
            raise ValueError(f"Unknown percentile dimension: {dimension}")

        self._percentile_cache[dimension] = result
        return result

    def _get_utxo_count_percentiles(self) -> Dict[str, Dict]:
        """Get percentile thresholds for address UTXO count (excludes genesis block).

        Note: For percentile estimation only, we skip the unspent filter (NOT EXISTS) for performance.
        """
        # Skip NOT EXISTS for percentile estimation - significantly faster
        query = f"""
        WITH addr_utxos AS (
            SELECT au.owner_addr, COUNT(*) as cnt
            FROM {self.schema}.address_utxo au
            WHERE au.owner_addr IS NOT NULL
              AND au.block > 0  -- Exclude genesis block
            GROUP BY au.owner_addr
            HAVING COUNT(*) > 0
        )
        SELECT
            percentile_cont(0.50) WITHIN GROUP (ORDER BY cnt) as p50,
            percentile_cont(0.75) WITHIN GROUP (ORDER BY cnt) as p75,
            percentile_cont(0.90) WITHIN GROUP (ORDER BY cnt) as p90,
            percentile_cont(0.95) WITHIN GROUP (ORDER BY cnt) as p95,
            percentile_cont(0.99) WITHIN GROUP (ORDER BY cnt) as p99,
            MAX(cnt) as max_val
        FROM addr_utxos
        """
        return self._build_percentile_dict(query, 'UTXOs')

    def _get_token_count_percentiles(self) -> Dict[str, Dict]:
        """Get percentile thresholds for address native token count (excludes lovelace).

        Note: For percentile estimation only, we skip the unspent filter (NOT EXISTS) for performance.
        """
        # Skip NOT EXISTS for percentile estimation - significantly faster
        query = f"""
        WITH utxo_tokens AS (
            SELECT au.owner_addr, t.token->>'unit' as unit
            FROM {self.schema}.address_utxo au,
            LATERAL jsonb_array_elements(au.amounts) AS t(token)
            WHERE au.owner_addr IS NOT NULL
              AND au.block > 0  -- Exclude genesis block
              AND t.token->>'unit' <> 'lovelace'  -- Exclude lovelace
        ),
        addr_tokens AS (
            SELECT owner_addr, COUNT(DISTINCT unit) as cnt
            FROM utxo_tokens
            GROUP BY owner_addr
            HAVING COUNT(DISTINCT unit) > 0  -- At least 1 non-lovelace token
        )
        SELECT
            percentile_cont(0.50) WITHIN GROUP (ORDER BY cnt) as p50,
            percentile_cont(0.75) WITHIN GROUP (ORDER BY cnt) as p75,
            percentile_cont(0.90) WITHIN GROUP (ORDER BY cnt) as p90,
            percentile_cont(0.95) WITHIN GROUP (ORDER BY cnt) as p95,
            percentile_cont(0.99) WITHIN GROUP (ORDER BY cnt) as p99,
            MAX(cnt) as max_val
        FROM addr_tokens
        """
        return self._build_percentile_dict(query, 'tokens')

    def _get_tx_history_percentiles(self) -> Dict[str, Dict]:
        """Get percentile thresholds for address transaction history count."""
        query = f"""
        WITH addr_history AS (
            SELECT owner_addr, COUNT(DISTINCT tx_hash) as cnt
            FROM {self.schema}.address_utxo
            WHERE owner_addr IS NOT NULL
              AND block > 0  -- Exclude genesis block
            GROUP BY owner_addr
            HAVING COUNT(DISTINCT tx_hash) > 0
        )
        SELECT
            percentile_cont(0.50) WITHIN GROUP (ORDER BY cnt) as p50,
            percentile_cont(0.75) WITHIN GROUP (ORDER BY cnt) as p75,
            percentile_cont(0.90) WITHIN GROUP (ORDER BY cnt) as p90,
            percentile_cont(0.95) WITHIN GROUP (ORDER BY cnt) as p95,
            percentile_cont(0.99) WITHIN GROUP (ORDER BY cnt) as p99,
            MAX(cnt) as max_val
        FROM addr_history
        """
        return self._build_percentile_dict(query, 'txs')

    def _get_block_tx_count_percentiles(self) -> Dict[str, Dict]:
        """Get percentile thresholds for block transaction count."""
        query = f"""
        SELECT
            percentile_cont(0.50) WITHIN GROUP (ORDER BY no_of_txs) as p50,
            percentile_cont(0.75) WITHIN GROUP (ORDER BY no_of_txs) as p75,
            percentile_cont(0.90) WITHIN GROUP (ORDER BY no_of_txs) as p90,
            percentile_cont(0.95) WITHIN GROUP (ORDER BY no_of_txs) as p95,
            percentile_cont(0.99) WITHIN GROUP (ORDER BY no_of_txs) as p99,
            MAX(no_of_txs) as max_val
        FROM {self.schema}.block
        WHERE no_of_txs > 0
          AND number > 0  -- Exclude genesis block
        """
        return self._build_percentile_dict(query, 'txs')

    def _get_block_body_size_percentiles(self) -> Dict[str, Dict]:
        """Get percentile thresholds for block body size."""
        query = f"""
        SELECT
            percentile_cont(0.50) WITHIN GROUP (ORDER BY body_size) as p50,
            percentile_cont(0.75) WITHIN GROUP (ORDER BY body_size) as p75,
            percentile_cont(0.90) WITHIN GROUP (ORDER BY body_size) as p90,
            percentile_cont(0.95) WITHIN GROUP (ORDER BY body_size) as p95,
            percentile_cont(0.99) WITHIN GROUP (ORDER BY body_size) as p99,
            MAX(body_size) as max_val
        FROM {self.schema}.block
        WHERE body_size > 0
          AND number > 0  -- Exclude genesis block
        """
        result = self._build_percentile_dict(query, 'bytes')
        # Convert bytes to human-readable in display
        for level, data in result.items():
            data['display'] = self._format_bytes_range(data['min'], data['max'])
        return result

    def _get_tx_io_count_percentiles(self) -> Dict[str, Dict]:
        """Get percentile thresholds for transaction I/O count."""
        query = f"""
        WITH tx_io AS (
            SELECT t.tx_hash,
                   jsonb_array_length(t.inputs) + jsonb_array_length(t.outputs) as cnt
            FROM {self.schema}.transaction t
            WHERE t.inputs IS NOT NULL AND t.outputs IS NOT NULL
              AND t.block > 0  -- Exclude genesis block
        )
        SELECT
            percentile_cont(0.50) WITHIN GROUP (ORDER BY cnt) as p50,
            percentile_cont(0.75) WITHIN GROUP (ORDER BY cnt) as p75,
            percentile_cont(0.90) WITHIN GROUP (ORDER BY cnt) as p90,
            percentile_cont(0.95) WITHIN GROUP (ORDER BY cnt) as p95,
            percentile_cont(0.99) WITHIN GROUP (ORDER BY cnt) as p99,
            MAX(cnt) as max_val
        FROM tx_io
        """
        return self._build_percentile_dict(query, 'I/O')

    def _get_tx_token_count_percentiles(self) -> Dict[str, Dict]:
        """Get percentile thresholds for transaction token types count."""
        query = f"""
        WITH tx_tokens AS (
            SELECT au.tx_hash,
                   COUNT(DISTINCT amt->>'unit') as cnt
            FROM {self.schema}.address_utxo au,
                 LATERAL jsonb_array_elements(au.amounts) AS amt
            WHERE au.amounts IS NOT NULL
              AND au.amounts::text <> 'null'
              AND amt->>'unit' <> 'lovelace'
              AND au.block > 0  -- Exclude genesis block
            GROUP BY au.tx_hash
            HAVING COUNT(DISTINCT amt->>'unit') > 0
        )
        SELECT
            percentile_cont(0.50) WITHIN GROUP (ORDER BY cnt) as p50,
            percentile_cont(0.75) WITHIN GROUP (ORDER BY cnt) as p75,
            percentile_cont(0.90) WITHIN GROUP (ORDER BY cnt) as p90,
            percentile_cont(0.95) WITHIN GROUP (ORDER BY cnt) as p95,
            percentile_cont(0.99) WITHIN GROUP (ORDER BY cnt) as p99,
            MAX(cnt) as max_val
        FROM tx_tokens
        """
        return self._build_percentile_dict(query, 'tokens')

    def _build_percentile_dict(self, query: str, unit: str, description: str = "") -> Dict[str, Dict]:
        """Execute percentile query and build threshold dict."""
        results = self._execute_query(query, (), description=description or f"percentile thresholds for {unit}")
        if not results:
            raise RuntimeError(f"Percentile query for {unit} returned no results - check database connection and schema")

        r = results[0]
        if r['p50'] is None or r['max_val'] is None:
            raise RuntimeError(f"Percentile query for {unit} returned NULL values - no data in table?")

        p50 = int(r['p50'])
        p75 = int(r['p75'])
        p90 = int(r['p90'])
        p95 = int(r['p95'])
        p99 = int(r['p99'])
        max_val = int(r['max_val'])

        # Print actual percentile values
        print(f"      p50={p50}, p75={p75}, p90={p90}, p95={p95}, p99={p99}, max={max_val}")

        return {
            'p50': {'min': 0, 'max': p50, 'display': f"≤{p50} {unit}"},
            'p75': {'min': p50, 'max': p75, 'display': f"{p50}-{p75} {unit}"},
            'p90': {'min': p75, 'max': p90, 'display': f"{p75}-{p90} {unit}"},
            'p95': {'min': p90, 'max': p95, 'display': f"{p90}-{p95} {unit}"},
            'p99': {'min': p95, 'max': max_val, 'display': f">{p95} {unit}"},
        }

    def _format_bytes_range(self, min_val: int, max_val: Optional[int]) -> str:
        """Format byte range in human-readable form."""
        def fmt(b):
            if b >= 1024 * 1024:
                return f"{b / (1024*1024):.1f}MB"
            elif b >= 1024:
                return f"{b / 1024:.1f}KB"
            return f"{b}B"

        if max_val is None or max_val == min_val:
            return f">{fmt(min_val)}"
        return f"{fmt(min_val)}-{fmt(max_val)}"

    # =========================================================================
    # Power-of-10 Threshold Queries (for power-law distributions)
    # =========================================================================

    def get_power_of_10_thresholds(self, dimension: str) -> Dict[str, Dict]:
        """
        Generate power-of-10 buckets for a dimension from actual data range.
        Returns dict: level -> {min, max, display}

        Buckets are: 1-9 (10^0), 10-99 (10^1), 100-999 (10^2), etc.
        """
        if dimension in self._power_of_10_cache:
            return self._power_of_10_cache[dimension]

        # Get min/max for the dimension
        if dimension == 'utxo_count':
            min_val, max_val, unit = self._get_utxo_count_range()
        elif dimension == 'token_count':
            min_val, max_val, unit = self._get_token_count_range()
        elif dimension == 'tx_history':
            min_val, max_val, unit = self._get_tx_history_range()
        elif dimension == 'block_tx_count':
            min_val, max_val, unit = self._get_block_tx_count_range()
        elif dimension == 'tx_token_count':
            min_val, max_val, unit = self._get_tx_token_count_range()
        elif dimension == 'tx_io_count':
            min_val, max_val, unit = self._get_tx_io_count_range()
        else:
            raise ValueError(f"Unknown power_of_10 dimension: {dimension}")

        result = self._build_power_of_10_dict(min_val, max_val, unit)
        self._power_of_10_cache[dimension] = result
        return result

    def _get_utxo_count_range(self) -> Tuple[int, int, str]:
        """Get min/max UTXO count for addresses (excludes genesis block).

        Note: For range estimation only, we skip the unspent filter (NOT EXISTS) for performance.
        The actual data retrieval queries will filter for unspent UTXOs.
        """
        query = f"""
        WITH addr_utxos AS (
            SELECT COUNT(*) as cnt
            FROM {self.schema}.address_utxo au
            WHERE au.owner_addr IS NOT NULL
              AND au.block > 0
            GROUP BY au.owner_addr
            HAVING COUNT(*) > 0
        )
        SELECT MAX(cnt) as max_val FROM addr_utxos
        """
        results = self._execute_query(query, (), description="utxo_count range")
        if not results or not results[0]['max_val']:
            raise RuntimeError("utxo_count range query returned no data - check database connection and schema")
        return 1, int(results[0]['max_val']), 'UTXOs'

    def _get_token_count_range(self) -> Tuple[int, int, str]:
        """Get min/max token count for addresses (excludes lovelace, genesis block).

        Note: For range estimation only, we skip the unspent filter (NOT EXISTS) for performance.
        The actual data retrieval queries will filter for unspent UTXOs.
        """
        query = f"""
        WITH addr_tokens AS (
            SELECT COUNT(DISTINCT t.token->>'unit') as cnt
            FROM {self.schema}.address_utxo au,
            LATERAL jsonb_array_elements(au.amounts) AS t(token)
            WHERE au.owner_addr IS NOT NULL
              AND au.block > 0
              AND t.token->>'unit' <> 'lovelace'
            GROUP BY au.owner_addr
            HAVING COUNT(DISTINCT t.token->>'unit') > 0
        )
        SELECT MAX(cnt) as max_val FROM addr_tokens
        """
        results = self._execute_query(query, (), description="token_count range")
        if not results or not results[0]['max_val']:
            raise RuntimeError("token_count range query returned no data - check database connection and schema")
        return 1, int(results[0]['max_val']), 'tokens'

    def _get_tx_history_range(self) -> Tuple[int, int, str]:
        """Get min/max transaction history for addresses (excludes genesis block)."""
        query = f"""
        WITH addr_history AS (
            SELECT owner_addr, COUNT(DISTINCT tx_hash) as cnt
            FROM {self.schema}.address_utxo
            WHERE owner_addr IS NOT NULL
              AND block > 0
            GROUP BY owner_addr
            HAVING COUNT(DISTINCT tx_hash) > 0
        )
        SELECT MAX(cnt) as max_val FROM addr_history
        """
        results = self._execute_query(query, (), description="tx_history range")
        if not results or not results[0]['max_val']:
            raise RuntimeError("tx_history range query returned no data - check database connection and schema")
        return 1, int(results[0]['max_val']), 'txs'

    def _get_block_tx_count_range(self) -> Tuple[int, int, str]:
        """Get min/max transaction count for blocks (excludes genesis block, empty blocks)."""
        query = f"""
        SELECT MIN(no_of_txs) as min_val, MAX(no_of_txs) as max_val
        FROM {self.schema}.block
        WHERE no_of_txs > 0 AND number > 0
        """
        results = self._execute_query(query, (), description="block_tx_count range")
        if not results or results[0]['max_val'] is None or results[0]['min_val'] is None:
            raise RuntimeError("block_tx_count range query returned no data - check database connection and schema")
        return int(results[0]['min_val']), int(results[0]['max_val']), 'txs'

    def _get_tx_token_count_range(self) -> Tuple[int, int, str]:
        """Get min/max token count for transactions (excludes lovelace, genesis block)."""
        query = f"""
        WITH tx_tokens AS (
            SELECT COUNT(DISTINCT amt->>'unit') as cnt
            FROM {self.schema}.address_utxo au,
                 LATERAL jsonb_array_elements(au.amounts) AS amt
            WHERE au.amounts IS NOT NULL
              AND au.amounts::text <> 'null'
              AND amt->>'unit' <> 'lovelace'
              AND au.block > 0
            GROUP BY au.tx_hash
            HAVING COUNT(DISTINCT amt->>'unit') > 0
        )
        SELECT MAX(cnt) as max_val FROM tx_tokens
        """
        results = self._execute_query(query, (), description="tx_token_count range")
        if not results or not results[0]['max_val']:
            raise RuntimeError("tx_token_count range query returned no data - check database connection and schema")
        return 1, int(results[0]['max_val']), 'tokens'

    def _get_tx_io_count_range(self) -> Tuple[int, int, str]:
        """Get min/max I/O count for transactions (excludes genesis block)."""
        query = f"""
        SELECT MIN(jsonb_array_length(inputs) + jsonb_array_length(outputs)) as min_val,
               MAX(jsonb_array_length(inputs) + jsonb_array_length(outputs)) as max_val
        FROM {self.schema}.transaction
        WHERE inputs IS NOT NULL AND outputs IS NOT NULL
          AND block > 0
        """
        results = self._execute_query(query, (), description="tx_io_count range")
        if not results or results[0]['max_val'] is None or results[0]['min_val'] is None:
            raise RuntimeError("tx_io_count range query returned no data - check database connection and schema")
        return int(results[0]['min_val']), int(results[0]['max_val']), 'I/O'

    def _build_power_of_10_dict(self, min_val: int, max_val: int, unit: str) -> Dict[str, Dict]:
        """Build power-of-10 threshold dict from min/max values."""
        import math

        # Determine the range of powers of 10 needed
        min_power = 0  # Always start from 10^0 = 1
        # Use floor to ensure the last bucket contains actual data
        # e.g., max_val=49964 -> max_power=4 (10000 bucket), not 5 (100000 bucket)
        max_power = int(math.floor(math.log10(max(max_val, 1))))

        result = {}
        print(f"      range: {min_val}-{max_val}, buckets: ", end='')
        bucket_names = []

        for power in range(min_power, max_power + 1):
            lower = 10 ** power
            upper = 10 ** (power + 1) - 1

            # Last bucket includes everything up to max_val
            is_last = (power == max_power)
            if is_last:
                upper = max_val

            # Use the lower bound as the bucket name (1, 10, 100, etc.)
            bucket_name = str(lower)
            bucket_names.append(bucket_name)

            if is_last:
                display = f"≥{lower} {unit}"
            else:
                display = f"{lower}-{upper} {unit}"

            result[bucket_name] = {
                'min': lower,
                'max': upper,
                'display': display,
            }

        print(', '.join(bucket_names))
        return result

    # =========================================================================
    # Quartile Threshold Queries (for skewed distributions)
    # =========================================================================

    def get_quartile_thresholds(self, dimension: str) -> Dict[str, Dict]:
        """
        Query quartile thresholds for a dimension based on max value / 4.
        Used for skewed distributions where percentiles don't work well.
        Returns dict: level -> {min, max, display}
        """
        if dimension == 'utxo_count':
            return self._get_utxo_count_quartiles()
        elif dimension == 'token_count':
            return self._get_token_count_quartiles()
        elif dimension == 'tx_history':
            return self._get_tx_history_quartiles()
        elif dimension == 'block_tx_count':
            return self._get_block_tx_count_quartiles()
        elif dimension == 'block_body_size':
            return self._get_block_body_size_quartiles()
        elif dimension == 'tx_io_count':
            return self._get_tx_io_count_quartiles()
        elif dimension == 'tx_token_count':
            return self._get_tx_token_count_quartiles()
        else:
            raise ValueError(f"Unknown quartile dimension: {dimension}")

    def _get_utxo_count_quartiles(self) -> Dict[str, Dict]:
        """Get quartile thresholds for address UTXO count using actual data distribution."""
        query = f"""
        WITH addr_utxos AS (
            SELECT au.owner_addr, COUNT(*) as cnt
            FROM {self.schema}.address_utxo au
            WHERE au.owner_addr IS NOT NULL
              AND au.block > 0  -- Exclude genesis block
              AND NOT EXISTS (
                SELECT 1 FROM {self.schema}.tx_input ti
                WHERE ti.tx_hash = au.tx_hash AND ti.output_index = au.output_index
              )
            GROUP BY au.owner_addr
            HAVING COUNT(*) > 0
        )
        SELECT
            MIN(cnt) as min_val,
            PERCENTILE_CONT(0.25) WITHIN GROUP (ORDER BY cnt) as p25,
            PERCENTILE_CONT(0.50) WITHIN GROUP (ORDER BY cnt) as p50,
            PERCENTILE_CONT(0.75) WITHIN GROUP (ORDER BY cnt) as p75,
            MAX(cnt) as max_val
        FROM addr_utxos
        """
        results = self._execute_query(query, (), description="utxo_count percentiles")
        if not results or results[0]['max_val'] is None:
            raise RuntimeError("utxo_count quartile query returned no data - check database connection and schema")

        r = results[0]
        return self._build_quartile_dict_from_percentiles(
            int(r['min_val']), int(r['p25']), int(r['p50']), int(r['p75']), int(r['max_val']), 'UTXOs'
        )

    def _get_token_count_quartiles(self) -> Dict[str, Dict]:
        """Get quartile thresholds for address distinct token TYPES using actual data distribution."""
        # Count distinct token unit (policy_id + asset_name hex) per address (exclude lovelace)
        query = f"""
        WITH addr_tokens AS (
            SELECT au.owner_addr, COUNT(DISTINCT t.token->>'unit') as cnt
            FROM {self.schema}.address_utxo au,
            LATERAL jsonb_array_elements(au.amounts) AS t(token)
            WHERE au.owner_addr IS NOT NULL
              AND au.block > 0  -- Exclude genesis block
              AND t.token->>'unit' != 'lovelace'
              AND NOT EXISTS (
                SELECT 1 FROM {self.schema}.tx_input ti
                WHERE ti.tx_hash = au.tx_hash AND ti.output_index = au.output_index
              )
            GROUP BY au.owner_addr
            HAVING COUNT(DISTINCT t.token->>'unit') > 0
        )
        SELECT
            MIN(cnt) as min_val,
            PERCENTILE_CONT(0.25) WITHIN GROUP (ORDER BY cnt) as p25,
            PERCENTILE_CONT(0.50) WITHIN GROUP (ORDER BY cnt) as p50,
            PERCENTILE_CONT(0.75) WITHIN GROUP (ORDER BY cnt) as p75,
            MAX(cnt) as max_val
        FROM addr_tokens
        """
        results = self._execute_query(query, (), description="token_count percentiles")
        if not results or results[0]['max_val'] is None:
            raise RuntimeError("token_count quartile query returned no data - check database connection and schema")

        r = results[0]
        return self._build_quartile_dict_from_percentiles(
            int(r['min_val']), int(r['p25']), int(r['p50']), int(r['p75']), int(r['max_val']), 'token types'
        )

    def _get_tx_history_quartiles(self) -> Dict[str, Dict]:
        """Get quartile thresholds for address transaction history using actual data distribution."""
        query = f"""
        WITH addr_history AS (
            SELECT owner_addr, COUNT(DISTINCT tx_hash) as cnt
            FROM {self.schema}.address_utxo
            WHERE owner_addr IS NOT NULL
              AND block > 0  -- Exclude genesis block
            GROUP BY owner_addr
            HAVING COUNT(DISTINCT tx_hash) > 0
        )
        SELECT
            MIN(cnt) as min_val,
            PERCENTILE_CONT(0.25) WITHIN GROUP (ORDER BY cnt) as p25,
            PERCENTILE_CONT(0.50) WITHIN GROUP (ORDER BY cnt) as p50,
            PERCENTILE_CONT(0.75) WITHIN GROUP (ORDER BY cnt) as p75,
            MAX(cnt) as max_val
        FROM addr_history
        """
        results = self._execute_query(query, (), description="tx_history percentiles")
        if not results or results[0]['max_val'] is None:
            raise RuntimeError("tx_history quartile query returned no data - check database connection and schema")

        r = results[0]
        return self._build_quartile_dict_from_percentiles(
            int(r['min_val']), int(r['p25']), int(r['p50']), int(r['p75']), int(r['max_val']), 'txs'
        )

    def _get_block_tx_count_quartiles(self) -> Dict[str, Dict]:
        """Get quartile thresholds for block transaction count using actual data distribution."""
        # Exclude genesis block (number=0) and empty blocks
        query = f"""
        SELECT
            MIN(no_of_txs) as min_val,
            PERCENTILE_CONT(0.25) WITHIN GROUP (ORDER BY no_of_txs) as p25,
            PERCENTILE_CONT(0.50) WITHIN GROUP (ORDER BY no_of_txs) as p50,
            PERCENTILE_CONT(0.75) WITHIN GROUP (ORDER BY no_of_txs) as p75,
            MAX(no_of_txs) as max_val
        FROM {self.schema}.block
        WHERE no_of_txs > 0
          AND number > 0
        """
        results = self._execute_query(query, (), description="block_tx_count percentiles")
        if not results or results[0]['max_val'] is None:
            raise RuntimeError("block_tx_count quartile query returned no data - check database connection and schema")

        r = results[0]
        return self._build_quartile_dict_from_percentiles(
            int(r['min_val']), int(r['p25']), int(r['p50']), int(r['p75']), int(r['max_val']), 'txs'
        )

    def _get_block_body_size_quartiles(self) -> Dict[str, Dict]:
        """Get quartile thresholds for block body size using actual data distribution."""
        query = f"""
        SELECT
            MIN(body_size) as min_val,
            PERCENTILE_CONT(0.25) WITHIN GROUP (ORDER BY body_size) as p25,
            PERCENTILE_CONT(0.50) WITHIN GROUP (ORDER BY body_size) as p50,
            PERCENTILE_CONT(0.75) WITHIN GROUP (ORDER BY body_size) as p75,
            MAX(body_size) as max_val
        FROM {self.schema}.block
        WHERE body_size > 0
          AND number > 0  -- Exclude genesis block
        """
        results = self._execute_query(query, (), description="block_body_size percentiles")
        if not results or results[0]['max_val'] is None:
            raise RuntimeError("block_body_size quartile query returned no data - check database connection and schema")

        r = results[0]
        result = self._build_quartile_dict_from_percentiles(
            int(r['min_val']), int(r['p25']), int(r['p50']), int(r['p75']), int(r['max_val']), 'bytes'
        )
        # Convert bytes to human-readable in display
        for level, data in result.items():
            data['display'] = self._format_bytes_range(data['min'], data['max'])
        return result

    def _get_tx_io_count_quartiles(self) -> Dict[str, Dict]:
        """Get quartile thresholds for transaction I/O count using actual data distribution."""
        query = f"""
        WITH tx_io AS (
            SELECT jsonb_array_length(inputs) + jsonb_array_length(outputs) as cnt
            FROM {self.schema}.transaction
            WHERE inputs IS NOT NULL AND outputs IS NOT NULL
              AND block > 0  -- Exclude genesis block
        )
        SELECT
            MIN(cnt) as min_val,
            PERCENTILE_CONT(0.25) WITHIN GROUP (ORDER BY cnt) as p25,
            PERCENTILE_CONT(0.50) WITHIN GROUP (ORDER BY cnt) as p50,
            PERCENTILE_CONT(0.75) WITHIN GROUP (ORDER BY cnt) as p75,
            MAX(cnt) as max_val
        FROM tx_io
        """
        results = self._execute_query(query, (), description="tx_io_count percentiles")
        if not results or results[0]['max_val'] is None:
            raise RuntimeError("tx_io_count quartile query returned no data - check database connection and schema")

        r = results[0]
        return self._build_quartile_dict_from_percentiles(
            int(r['min_val']), int(r['p25']), int(r['p50']), int(r['p75']), int(r['max_val']), 'I/O'
        )

    def _get_tx_token_count_quartiles(self) -> Dict[str, Dict]:
        """Get quartile thresholds for transaction token types count using actual data distribution.

        Uses address_utxo table which contains the actual amounts JSONB data.
        """
        query = f"""
        WITH tx_tokens AS (
            SELECT au.tx_hash,
                   COUNT(DISTINCT amt->>'unit') as cnt
            FROM {self.schema}.address_utxo au,
                 LATERAL jsonb_array_elements(au.amounts) AS amt
            WHERE au.amounts IS NOT NULL
              AND au.amounts::text <> 'null'
              AND amt->>'unit' <> 'lovelace'
              AND au.block > 0  -- Exclude genesis block
            GROUP BY au.tx_hash
            HAVING COUNT(DISTINCT amt->>'unit') > 0
        )
        SELECT
            MIN(cnt) as min_val,
            PERCENTILE_CONT(0.25) WITHIN GROUP (ORDER BY cnt) as p25,
            PERCENTILE_CONT(0.50) WITHIN GROUP (ORDER BY cnt) as p50,
            PERCENTILE_CONT(0.75) WITHIN GROUP (ORDER BY cnt) as p75,
            MAX(cnt) as max_val
        FROM tx_tokens
        """
        results = self._execute_query(query, (), description="tx_token_count percentiles")
        if not results or results[0]['max_val'] is None:
            raise RuntimeError("tx_token_count quartile query returned no data - check database connection and schema")

        r = results[0]
        return self._build_quartile_dict_from_percentiles(
            int(r['min_val']), int(r['p25']), int(r['p50']), int(r['p75']), int(r['max_val']), 'token types'
        )

    def _build_quartile_dict_from_percentiles(
        self, min_val: int, p25: int, p50: int, p75: int, max_val: int, unit: str
    ) -> Dict[str, Dict]:
        """Build quartile threshold dict from actual percentile values.

        This ensures each quartile contains ~25% of the actual data points,
        rather than dividing the value range which skews heavily for outliers.
        """
        return {
            'q1': {'min': min_val, 'max': p25, 'display': f"{min_val}-{p25} {unit}"},
            'q2': {'min': p25, 'max': p50, 'display': f"{p25}-{p50} {unit}"},
            'q3': {'min': p50, 'max': p75, 'display': f"{p50}-{p75} {unit}"},
            'q4': {'min': p75, 'max': max_val, 'display': f">{p75} {unit}"},
        }

    def get_era_quartiles(self) -> Dict[str, Dict]:
        """
        Query era distribution from block table.
        Returns dict: era_name -> {min_era, max_era, display}

        Note: Era names (Byron, Shelley, etc.) are protocol constants defined in ERA_NAMES.
        The DB only stores era numbers in block.era and era table.
        """
        if self._era_cache:
            return self._era_cache

        # Get all eras present in the block table with their epoch ranges
        query = f"""
        SELECT era, MIN(epoch) as min_epoch, MAX(epoch) as max_epoch, COUNT(*) as block_count
        FROM {self.schema}.block
        WHERE era IS NOT NULL
        GROUP BY era
        ORDER BY era
        """
        results = self._execute_query(query, (), description="era distribution")
        if not results:
            raise RuntimeError("Era distribution query returned no data - check database connection and schema")

        # Build era info using ERA_NAMES constant (protocol-level names not stored in DB)
        self._era_cache = {}
        print(f"      Found {len(results)} eras:")
        for r in results:
            era_num = r['era']
            if era_num not in ERA_NAMES:
                raise RuntimeError(f"Unknown era number {era_num} - update ERA_NAMES constant")
            era_name = ERA_NAMES[era_num]
            key = era_name.lower()
            self._era_cache[key] = {
                'min': era_num,
                'max': era_num,
                'display': f"{era_name} (epochs {r['min_epoch']}-{r['max_epoch']})",
            }
            print(f"        {era_name}: era={era_num}, epochs {r['min_epoch']}-{r['max_epoch']}, {r['block_count']} blocks")

        return self._era_cache

    # =========================================================================
    # Data Retrieval by Dimension and Level
    # =========================================================================

    def get_addresses_by_utxo_count(self, level: str) -> List[Dict]:
        """Get addresses at specific UTXO count percentile level.

        Uses block column directly from address_utxo (no joins needed).
        """
        thresholds = self.get_percentile_thresholds('utxo_count')
        if level not in thresholds:
            raise ValueError(f"Unknown level '{level}' for utxo_count - valid levels: {list(thresholds.keys())}")

        t = thresholds[level]
        min_val, max_val = t['min'], t['max']

        if max_val is None or level == 'p99':
            condition = "HAVING COUNT(*) > %s"
            params = (min_val, TARGET_PER_LEVEL)
        else:
            condition = "HAVING COUNT(*) BETWEEN %s AND %s"
            params = (min_val, max_val, TARGET_PER_LEVEL)

        query = f"""
        SELECT au.owner_addr as address, COUNT(*) as utxo_count
        FROM {self.schema}.address_utxo au
        WHERE au.owner_addr IS NOT NULL
          AND au.block > 0  -- Exclude genesis block
          AND NOT EXISTS (
            SELECT 1 FROM {self.schema}.tx_input ti
            WHERE ti.tx_hash = au.tx_hash AND ti.output_index = au.output_index
          )
        GROUP BY au.owner_addr
        {condition}
        ORDER BY RANDOM()
        LIMIT %s
        """
        return self._execute_query(query, params, description=f"utxo_count {level}")

    def get_addresses_by_token_count(self, level: str) -> List[Dict]:
        """Get addresses at specific native token count percentile level.

        Uses block column directly from address_utxo (no joins needed).
        """
        thresholds = self.get_percentile_thresholds('token_count')
        if level not in thresholds:
            raise ValueError(f"Unknown level '{level}' for token_count - valid levels: {list(thresholds.keys())}")

        t = thresholds[level]
        min_val, max_val = t['min'], t['max']

        # Count distinct tokens per address (excludes lovelace)
        query = f"""
        WITH addr_tokens AS (
            SELECT au.owner_addr,
                   COUNT(DISTINCT (elem->>'unit')) as token_count
            FROM {self.schema}.address_utxo au,
                 jsonb_array_elements(au.amounts) as elem
            WHERE au.owner_addr IS NOT NULL
              AND au.block > 0  -- Exclude genesis block
              AND elem->>'unit' <> 'lovelace'  -- Exclude lovelace
              AND NOT EXISTS (
                SELECT 1 FROM {self.schema}.tx_input ti
                WHERE ti.tx_hash = au.tx_hash AND ti.output_index = au.output_index
              )
            GROUP BY au.owner_addr
            HAVING COUNT(DISTINCT (elem->>'unit')) > 0
        )
        SELECT address, token_count FROM (
            SELECT owner_addr as address, token_count
            FROM addr_tokens
            WHERE token_count BETWEEN %s AND %s
            ORDER BY RANDOM()
            LIMIT %s
        ) sub
        """
        # For p99, use a very high max since it's "above p95 threshold"
        max_param = max_val if max_val and level != 'p99' else 2**31 - 1
        return self._execute_query(query, (min_val, max_param, TARGET_PER_LEVEL), description=f"token_count {level}")

    def get_addresses_by_tx_history(self, level: str) -> List[Dict]:
        """Get addresses at specific transaction history percentile level.

        Uses block column directly from address_utxo (no joins needed).
        """
        thresholds = self.get_percentile_thresholds('tx_history')
        if level not in thresholds:
            raise ValueError(f"Unknown level '{level}' for tx_history - valid levels: {list(thresholds.keys())}")

        t = thresholds[level]
        min_val, max_val = t['min'], t['max']

        if max_val is None or level == 'p99':
            condition = "HAVING COUNT(DISTINCT au.tx_hash) > %s"
            params = (min_val, TARGET_PER_LEVEL)
        else:
            condition = "HAVING COUNT(DISTINCT au.tx_hash) BETWEEN %s AND %s"
            params = (min_val, max_val, TARGET_PER_LEVEL)

        query = f"""
        SELECT au.owner_addr as address, COUNT(DISTINCT au.tx_hash) as tx_count
        FROM {self.schema}.address_utxo au
        WHERE au.owner_addr IS NOT NULL
          AND au.block > 0  -- Exclude genesis block
        GROUP BY au.owner_addr
        {condition}
        ORDER BY RANDOM()
        LIMIT %s
        """
        return self._execute_query(query, params, description=f"tx_history {level}")

    def get_blocks_by_tx_count(self, level: str) -> List[Dict]:
        """Get blocks at specific transaction count percentile level."""
        thresholds = self.get_percentile_thresholds('block_tx_count')
        if level not in thresholds:
            raise ValueError(f"Unknown level '{level}' for block_tx_count - valid levels: {list(thresholds.keys())}")

        t = thresholds[level]
        min_val, max_val = t['min'], t['max']

        if max_val is None or level == 'p99':
            condition = "WHERE no_of_txs > %s AND number > 0"
            params = (min_val, TARGET_PER_LEVEL)
        else:
            condition = "WHERE no_of_txs BETWEEN %s AND %s AND number > 0"
            params = (min_val, max_val, TARGET_PER_LEVEL)

        query = f"""
        SELECT number as block_index, hash as block_hash, no_of_txs as tx_count
        FROM {self.schema}.block
        {condition}
        ORDER BY RANDOM()
        LIMIT %s
        """
        return self._execute_query(query, params, description=f"block_tx_count {level}")

    def get_blocks_by_body_size(self, level: str) -> List[Dict]:
        """Get blocks at specific body size percentile level."""
        thresholds = self.get_percentile_thresholds('block_body_size')
        if level not in thresholds:
            raise ValueError(f"Unknown level '{level}' for block_body_size - valid levels: {list(thresholds.keys())}")

        t = thresholds[level]
        min_val, max_val = t['min'], t['max']

        if max_val is None or level == 'p99':
            condition = "WHERE body_size > %s AND number > 0"
            params = (min_val, TARGET_PER_LEVEL)
        else:
            condition = "WHERE body_size BETWEEN %s AND %s AND number > 0"
            params = (min_val, max_val, TARGET_PER_LEVEL)

        query = f"""
        SELECT number as block_index, hash as block_hash, body_size
        FROM {self.schema}.block
        {condition}
        ORDER BY RANDOM()
        LIMIT %s
        """
        return self._execute_query(query, params, description=f"block_body_size {level}")

    def get_blocks_by_era(self, level: str) -> List[Dict]:
        """Get blocks at specific era quartile level."""
        quartiles = self.get_era_quartiles()
        if level not in quartiles:
            raise ValueError(f"Unknown era '{level}' - valid eras: {list(quartiles.keys())}")

        q = quartiles[level]
        query = f"""
        SELECT number as block_index, hash as block_hash, era, epoch
        FROM {self.schema}.block
        WHERE era BETWEEN %s AND %s
        ORDER BY RANDOM()
        LIMIT %s
        """
        return self._execute_query(query, (q['min'], q['max'], TARGET_PER_LEVEL), description=f"block_era {level}")

    # =========================================================================
    # Two-Phase Verified Data Retrieval
    # =========================================================================
    #
    # For power-of-10 dimensions, we use two-phase verification:
    #   1. Find candidate entities matching the target count range
    #   2. Verify each candidate at the reference block (for time-consistent data)
    #   3. Keep only those that truly belong in the bucket at that block
    # =========================================================================

    def _get_threshold(self, dimension: str, level: str, threshold_type: str) -> Dict:
        """Get threshold for a dimension/level, raising ValueError if not found."""
        if threshold_type == 'power_of_10':
            thresholds = self.get_power_of_10_thresholds(dimension)
        elif threshold_type == 'percentile':
            thresholds = self.get_percentile_thresholds(dimension)
        else:
            raise ValueError(f"Unknown threshold type: {threshold_type}")

        if level not in thresholds:
            raise ValueError(f"Unknown level '{level}' for {dimension} - valid: {list(thresholds.keys())}")
        return thresholds[level]

    def _get_verified_data(
        self,
        candidate_query: str,
        candidate_params: tuple,
        verify_query: str,
        min_val: int,
        max_val: float,
        id_field: str,
        count_field: str,
        description: str,
        needs_ref_block: bool = True,
        ref_block_params: int = 2
    ) -> List[Dict]:
        """Get data with two-phase verification: find candidates, then verify at reference block.

        Args:
            candidate_query: SQL query to get candidates
            candidate_params: Parameters for candidate query
            verify_query: SQL query to verify exact count (%s for entity_id, then ref_block(s))
            min_val: Minimum count for bucket (inclusive)
            max_val: Maximum count for bucket (inclusive), use float('inf') for unbounded
            id_field: Field name containing entity ID in candidate results
            count_field: Field name for count in verification result
            description: Description for logging
            needs_ref_block: Whether verify_query needs ref_block parameter(s)
            ref_block_params: Number of ref_block parameters (1 or 2)
        """
        ref_block = self.get_reference_block()

        # Phase 1: Get candidates (fast, potentially inaccurate counts)
        candidates = self._execute_query(candidate_query, candidate_params, description=f"{description} candidates")

        # Phase 2: Verify each candidate
        verified = []
        for candidate in candidates:
            entity_id = candidate[id_field]

            try:
                with self.conn.cursor(cursor_factory=RealDictCursor) as cur:
                    if needs_ref_block:
                        if ref_block_params == 2:
                            cur.execute(verify_query, (entity_id, ref_block['index'], ref_block['index']))
                        else:
                            cur.execute(verify_query, (entity_id, ref_block['index']))
                    else:
                        cur.execute(verify_query, (entity_id,))
                    result = cur.fetchone()
                    exact_count = result[count_field] if result else None
            except Exception:
                continue

            if exact_count is not None and min_val <= exact_count <= max_val:
                candidate[count_field] = exact_count
                candidate['reference_block_index'] = ref_block['index']
                candidate['reference_block_hash'] = ref_block['hash']
                verified.append(candidate)

                if len(verified) >= TARGET_PER_LEVEL:
                    break

        print(f"      Verified {len(verified)}/{len(candidates)} candidates")
        return verified

    # =========================================================================
    # Power-of-10 Data Retrieval (for power-law distributions)
    # =========================================================================

    def get_addresses_by_utxo_count_power(self, level: str) -> List[Dict]:
        """Get addresses at specific UTXO count power-of-10 level."""
        t = self._get_threshold('utxo_count', level, 'power_of_10')

        candidate_query = f"""
        SELECT au.owner_addr as address, COUNT(*) as utxo_count
        FROM {self.schema}.address_utxo au
        WHERE au.owner_addr IS NOT NULL AND au.block > 0
          AND NOT EXISTS (
            SELECT 1 FROM {self.schema}.tx_input ti
            WHERE ti.tx_hash = au.tx_hash AND ti.output_index = au.output_index
          )
        GROUP BY au.owner_addr
        HAVING COUNT(*) BETWEEN %s AND %s
        ORDER BY RANDOM() LIMIT %s
        """

        verify_query = f"""
        SELECT COUNT(*) as utxo_count
        FROM {self.schema}.address_utxo au
        WHERE au.owner_addr = %s AND au.block > 0 AND au.block <= %s
          AND NOT EXISTS (
            SELECT 1 FROM {self.schema}.tx_input ti
            WHERE ti.tx_hash = au.tx_hash AND ti.output_index = au.output_index
              AND ti.spent_at_block <= %s
          )
        """

        return self._get_verified_data(
            candidate_query, (t['min'], t['max'], TARGET_PER_LEVEL * 5),
            verify_query, t['min'], t['max'],
            'address', 'utxo_count', f"utxo_count {level}"
        )

    def get_addresses_by_token_count_power(self, level: str) -> List[Dict]:
        """Get addresses at specific token count power-of-10 level."""
        t = self._get_threshold('token_count', level, 'power_of_10')

        candidate_query = f"""
        WITH addr_tokens AS (
            SELECT au.owner_addr, COUNT(DISTINCT (elem->>'unit')) as token_count
            FROM {self.schema}.address_utxo au,
                 jsonb_array_elements(au.amounts) as elem
            WHERE au.owner_addr IS NOT NULL AND au.block > 0
              AND elem->>'unit' <> 'lovelace'
              AND NOT EXISTS (
                SELECT 1 FROM {self.schema}.tx_input ti
                WHERE ti.tx_hash = au.tx_hash AND ti.output_index = au.output_index
              )
            GROUP BY au.owner_addr
            HAVING COUNT(DISTINCT (elem->>'unit')) > 0
        )
        SELECT owner_addr as address, token_count FROM addr_tokens
        WHERE token_count BETWEEN %s AND %s
        ORDER BY RANDOM() LIMIT %s
        """

        verify_query = f"""
        SELECT COUNT(DISTINCT (elem->>'unit')) as token_count
        FROM {self.schema}.address_utxo au, jsonb_array_elements(au.amounts) as elem
        WHERE au.owner_addr = %s AND au.block > 0 AND au.block <= %s
          AND elem->>'unit' <> 'lovelace'
          AND NOT EXISTS (
            SELECT 1 FROM {self.schema}.tx_input ti
            WHERE ti.tx_hash = au.tx_hash AND ti.output_index = au.output_index
              AND ti.spent_at_block <= %s
          )
        """

        return self._get_verified_data(
            candidate_query, (t['min'], t['max'], TARGET_PER_LEVEL * 5),
            verify_query, t['min'], t['max'],
            'address', 'token_count', f"token_count {level}"
        )

    def get_addresses_by_tx_history_power(self, level: str) -> List[Dict]:
        """Get addresses at specific transaction history power-of-10 level."""
        t = self._get_threshold('tx_history', level, 'power_of_10')

        candidate_query = f"""
        SELECT au.owner_addr as address, COUNT(DISTINCT au.tx_hash) as tx_count
        FROM {self.schema}.address_utxo au
        WHERE au.owner_addr IS NOT NULL AND au.block > 0
        GROUP BY au.owner_addr
        HAVING COUNT(DISTINCT au.tx_hash) BETWEEN %s AND %s
        ORDER BY RANDOM() LIMIT %s
        """

        # tx_history only needs one ref_block param (no spent check)
        verify_query = f"""
        SELECT COUNT(DISTINCT tx_hash) as tx_count
        FROM {self.schema}.address_utxo
        WHERE owner_addr = %s AND block > 0 AND block <= %s
        """

        return self._get_verified_data(
            candidate_query, (t['min'], t['max'], TARGET_PER_LEVEL * 5),
            verify_query, t['min'], t['max'],
            'address', 'tx_count', f"tx_history {level}",
            needs_ref_block=True, ref_block_params=1
        )

    def get_blocks_by_tx_count_power(self, level: str) -> List[Dict]:
        """Get blocks at specific transaction count power-of-10 level."""
        thresholds = self.get_power_of_10_thresholds('block_tx_count')
        if level not in thresholds:
            raise ValueError(f"Unknown level '{level}' for block_tx_count - valid levels: {list(thresholds.keys())}")

        t = thresholds[level]
        min_val, max_val = t['min'], t['max']

        query = f"""
        SELECT number as block_index, hash as block_hash, no_of_txs as tx_count
        FROM {self.schema}.block
        WHERE no_of_txs BETWEEN %s AND %s
          AND number > 0
        ORDER BY RANDOM()
        LIMIT %s
        """
        return self._execute_query(query, (min_val, max_val, TARGET_PER_LEVEL), description=f"block_tx_count {level}")

    def get_transactions_by_token_count_power(self, level: str) -> List[Dict]:
        """Get transactions at specific token count power-of-10 level."""
        t = self._get_threshold('tx_token_count', level, 'power_of_10')

        candidate_query = f"""
        WITH tx_tokens AS (
            SELECT au.tx_hash, MAX(au.block_hash) as block_hash, MAX(au.block) as block_index,
                   COUNT(DISTINCT amt->>'unit') as token_count
            FROM {self.schema}.address_utxo au,
                 LATERAL jsonb_array_elements(au.amounts) AS amt
            WHERE au.amounts IS NOT NULL AND au.amounts::text <> 'null'
              AND amt->>'unit' <> 'lovelace' AND au.block > 0
            GROUP BY au.tx_hash
            HAVING COUNT(DISTINCT amt->>'unit') > 0
        )
        SELECT tx_hash as transaction_hash, block_hash, block_index, token_count
        FROM tx_tokens WHERE token_count BETWEEN %s AND %s
        ORDER BY RANDOM() LIMIT %s
        """

        # Transaction token count is immutable - no ref_block needed
        verify_query = f"""
        SELECT COUNT(DISTINCT amt->>'unit') as token_count
        FROM {self.schema}.address_utxo au, LATERAL jsonb_array_elements(au.amounts) AS amt
        WHERE au.tx_hash = %s AND au.amounts IS NOT NULL
          AND au.amounts::text <> 'null' AND amt->>'unit' <> 'lovelace'
        """

        return self._get_verified_data(
            candidate_query, (t['min'], t['max'], TARGET_PER_LEVEL * 5),
            verify_query, t['min'], t['max'],
            'transaction_hash', 'token_count', f"tx_token_count {level}",
            needs_ref_block=False
        )

    def get_transactions_by_io_count_power(self, level: str) -> List[Dict]:
        """Get transactions at specific I/O count power-of-10 level."""
        thresholds = self.get_power_of_10_thresholds('tx_io_count')
        if level not in thresholds:
            raise ValueError(f"Unknown level '{level}' for tx_io_count - valid levels: {list(thresholds.keys())}")

        t = thresholds[level]
        min_val, max_val = t['min'], t['max']

        query = f"""
        SELECT t.tx_hash as transaction_hash, t.block_hash, t.block as block_index,
               jsonb_array_length(t.inputs) as input_count,
               jsonb_array_length(t.outputs) as output_count
        FROM {self.schema}.transaction t
        WHERE t.inputs IS NOT NULL AND t.outputs IS NOT NULL
          AND t.block > 0
          AND jsonb_array_length(t.inputs) + jsonb_array_length(t.outputs) BETWEEN %s AND %s
        ORDER BY RANDOM()
        LIMIT %s
        """
        return self._execute_query(query, (min_val, max_val, TARGET_PER_LEVEL), description=f"tx_io_count {level}")

    # =========================================================================
    # Quartile-Based Data Retrieval (for skewed distributions)
    # =========================================================================

    def get_addresses_by_utxo_count_quartile(self, level: str) -> List[Dict]:
        """Get addresses at specific UTXO count quartile level."""
        quartiles = self.get_quartile_thresholds('utxo_count')
        if level not in quartiles:
            raise ValueError(f"Unknown level '{level}' for utxo_count quartile - valid levels: {list(quartiles.keys())}")

        q = quartiles[level]
        min_val, max_val = q['min'], q['max']

        query = f"""
        SELECT au.owner_addr as address, COUNT(*) as utxo_count
        FROM {self.schema}.address_utxo au
        WHERE au.owner_addr IS NOT NULL
          AND au.block > 0  -- Exclude genesis block
          AND NOT EXISTS (
            SELECT 1 FROM {self.schema}.tx_input ti
            WHERE ti.tx_hash = au.tx_hash AND ti.output_index = au.output_index
          )
        GROUP BY au.owner_addr
        HAVING COUNT(*) BETWEEN %s AND %s
        ORDER BY RANDOM()
        LIMIT %s
        """
        return self._execute_query(query, (min_val, max_val, TARGET_PER_LEVEL), description=f"utxo_count {level}")

    def get_addresses_by_token_count_quartile(self, level: str) -> List[Dict]:
        """Get addresses at specific native token count quartile level (excludes lovelace)."""
        quartiles = self.get_quartile_thresholds('token_count')
        if level not in quartiles:
            raise ValueError(f"Unknown level '{level}' for token_count quartile - valid levels: {list(quartiles.keys())}")

        q = quartiles[level]
        min_val, max_val = q['min'], q['max']

        query = f"""
        WITH addr_tokens AS (
            SELECT au.owner_addr,
                   COUNT(DISTINCT (elem->>'unit')) as token_count
            FROM {self.schema}.address_utxo au,
                 jsonb_array_elements(au.amounts) as elem
            WHERE au.owner_addr IS NOT NULL
              AND au.block > 0  -- Exclude genesis block
              AND elem->>'unit' != 'lovelace'
              AND NOT EXISTS (
                SELECT 1 FROM {self.schema}.tx_input ti
                WHERE ti.tx_hash = au.tx_hash AND ti.output_index = au.output_index
              )
            GROUP BY au.owner_addr
            HAVING COUNT(DISTINCT (elem->>'unit')) > 0
        )
        SELECT owner_addr as address, token_count
        FROM addr_tokens
        WHERE token_count BETWEEN %s AND %s
        ORDER BY RANDOM()
        LIMIT %s
        """
        return self._execute_query(query, (min_val, max_val, TARGET_PER_LEVEL), description=f"token_count {level}")

    def get_addresses_by_tx_history_quartile(self, level: str) -> List[Dict]:
        """Get addresses at specific transaction history quartile level."""
        quartiles = self.get_quartile_thresholds('tx_history')
        if level not in quartiles:
            raise ValueError(f"Unknown level '{level}' for tx_history quartile - valid levels: {list(quartiles.keys())}")

        q = quartiles[level]
        min_val, max_val = q['min'], q['max']

        query = f"""
        SELECT owner_addr as address, COUNT(DISTINCT tx_hash) as tx_count
        FROM {self.schema}.address_utxo
        WHERE owner_addr IS NOT NULL
          AND block > 0  -- Exclude genesis block
        GROUP BY owner_addr
        HAVING COUNT(DISTINCT tx_hash) BETWEEN %s AND %s
        ORDER BY RANDOM()
        LIMIT %s
        """
        return self._execute_query(query, (min_val, max_val, TARGET_PER_LEVEL), description=f"tx_history {level}")

    def get_blocks_by_tx_count_quartile(self, level: str) -> List[Dict]:
        """Get blocks at specific transaction count quartile level."""
        quartiles = self.get_quartile_thresholds('block_tx_count')
        if level not in quartiles:
            raise ValueError(f"Unknown level '{level}' for block_tx_count quartile - valid levels: {list(quartiles.keys())}")

        q = quartiles[level]
        min_val, max_val = q['min'], q['max']

        query = f"""
        SELECT number as block_index, hash as block_hash, no_of_txs as tx_count
        FROM {self.schema}.block
        WHERE no_of_txs BETWEEN %s AND %s
          AND number > 0  -- Exclude genesis block
        ORDER BY RANDOM()
        LIMIT %s
        """
        return self._execute_query(query, (min_val, max_val, TARGET_PER_LEVEL), description=f"block_tx_count {level}")

    def get_blocks_by_body_size_quartile(self, level: str) -> List[Dict]:
        """Get blocks at specific body size quartile level."""
        quartiles = self.get_quartile_thresholds('block_body_size')
        if level not in quartiles:
            raise ValueError(f"Unknown level '{level}' for block_body_size quartile - valid levels: {list(quartiles.keys())}")

        q = quartiles[level]
        min_val, max_val = q['min'], q['max']

        query = f"""
        SELECT number as block_index, hash as block_hash, body_size
        FROM {self.schema}.block
        WHERE body_size BETWEEN %s AND %s
          AND number > 0  -- Exclude genesis block
        ORDER BY RANDOM()
        LIMIT %s
        """
        return self._execute_query(query, (min_val, max_val, TARGET_PER_LEVEL), description=f"block_body_size {level}")

    def get_blocks_by_era_name(self, era_name: str) -> List[Dict]:
        """Get blocks by era name (byron, shelley, allegra, mary, alonzo, babbage, conway)."""
        eras = self.get_era_quartiles()
        if era_name not in eras:
            raise ValueError(f"Unknown era '{era_name}' - valid eras: {list(eras.keys())}")

        e = eras[era_name]
        query = f"""
        SELECT number as block_index, hash as block_hash, era, epoch
        FROM {self.schema}.block
        WHERE era = %s
        ORDER BY RANDOM()
        LIMIT %s
        """
        return self._execute_query(query, (e['min'], TARGET_PER_LEVEL), description=f"block_era {era_name}")

    def get_transactions_by_io_count_quartile(self, level: str) -> List[Dict]:
        """Get transactions at specific I/O count quartile level."""
        quartiles = self.get_quartile_thresholds('tx_io_count')
        if level not in quartiles:
            raise ValueError(f"Unknown level '{level}' for tx_io_count quartile - valid levels: {list(quartiles.keys())}")

        q = quartiles[level]
        min_val, max_val = q['min'], q['max']

        query = f"""
        SELECT tx_hash as transaction_hash, block_hash, block as block_index,
               jsonb_array_length(inputs) as input_count,
               jsonb_array_length(outputs) as output_count
        FROM {self.schema}.transaction
        WHERE inputs IS NOT NULL AND outputs IS NOT NULL
          AND block > 0  -- Exclude genesis block
          AND jsonb_array_length(inputs) + jsonb_array_length(outputs) BETWEEN %s AND %s
        ORDER BY RANDOM()
        LIMIT %s
        """
        return self._execute_query(query, (min_val, max_val, TARGET_PER_LEVEL), description=f"tx_io_count {level}")

    def get_transactions_by_token_count_quartile(self, level: str) -> List[Dict]:
        """Get transactions at specific token count quartile level.

        Uses address_utxo table which contains the actual amounts JSONB data.
        """
        quartiles = self.get_quartile_thresholds('tx_token_count')
        if level not in quartiles:
            raise ValueError(f"Unknown level '{level}' for tx_token_count quartile - valid levels: {list(quartiles.keys())}")

        q = quartiles[level]
        min_val, max_val = q['min'], q['max']

        query = f"""
        WITH tx_tokens AS (
            SELECT au.tx_hash,
                   MAX(au.block_hash) as block_hash,
                   MAX(au.block) as block_index,
                   COUNT(DISTINCT amt->>'unit') as token_count
            FROM {self.schema}.address_utxo au,
                 LATERAL jsonb_array_elements(au.amounts) AS amt
            WHERE au.amounts IS NOT NULL
              AND au.amounts::text <> 'null'
              AND amt->>'unit' <> 'lovelace'
              AND au.block > 0
            GROUP BY au.tx_hash
        )
        SELECT tx_hash as transaction_hash, block_hash, block_index, token_count
        FROM tx_tokens
        WHERE token_count BETWEEN %s AND %s
        ORDER BY RANDOM()
        LIMIT %s
        """
        return self._execute_query(query, (min_val, max_val, TARGET_PER_LEVEL), description=f"tx_token_count {level}")

    def get_transactions_by_io_count(self, level: str) -> List[Dict]:
        """Get transactions at specific I/O count percentile level."""
        thresholds = self.get_percentile_thresholds('tx_io_count')
        if level not in thresholds:
            raise ValueError(f"Unknown level '{level}' for tx_io_count - valid levels: {list(thresholds.keys())}")

        t = thresholds[level]
        min_val, max_val = t['min'], t['max']

        if max_val is None or level == 'p99':
            condition = "AND jsonb_array_length(t.inputs) + jsonb_array_length(t.outputs) > %s"
            params = (min_val, TARGET_PER_LEVEL)
        else:
            condition = "AND jsonb_array_length(t.inputs) + jsonb_array_length(t.outputs) BETWEEN %s AND %s"
            params = (min_val, max_val, TARGET_PER_LEVEL)

        query = f"""
        SELECT t.tx_hash as transaction_hash, t.block_hash, t.block as block_index,
               jsonb_array_length(t.inputs) as input_count,
               jsonb_array_length(t.outputs) as output_count
        FROM {self.schema}.transaction t
        WHERE t.block > 0
          {condition}
        ORDER BY RANDOM()
        LIMIT %s
        """
        return self._execute_query(query, params, description=f"tx_io_count {level}")

    def get_transactions_by_token_count(self, level: str) -> List[Dict]:
        """Get transactions at specific token count percentile level."""
        t = self._get_threshold('tx_token_count', level, 'percentile')
        min_val = t['min']
        max_val = t['max'] if t['max'] is not None and level != 'p99' else float('inf')

        # Build candidate query with appropriate HAVING clause
        if max_val == float('inf'):
            having = "HAVING COUNT(DISTINCT amt->>'unit') > %s"
            params = (min_val, TARGET_PER_LEVEL * 5)
        else:
            having = "HAVING COUNT(DISTINCT amt->>'unit') BETWEEN %s AND %s"
            params = (min_val, int(max_val), TARGET_PER_LEVEL * 5)

        candidate_query = f"""
        WITH tx_tokens AS (
            SELECT au.tx_hash, MAX(au.block_hash) as block_hash, MAX(au.block) as block_index,
                   COUNT(DISTINCT amt->>'unit') as token_count
            FROM {self.schema}.address_utxo au,
                 LATERAL jsonb_array_elements(au.amounts) AS amt
            WHERE au.amounts IS NOT NULL AND au.amounts::text <> 'null'
              AND amt->>'unit' <> 'lovelace' AND au.block > 0
            GROUP BY au.tx_hash {having}
        )
        SELECT tx_hash as transaction_hash, block_hash, block_index, token_count
        FROM tx_tokens ORDER BY RANDOM() LIMIT %s
        """

        verify_query = f"""
        SELECT COUNT(DISTINCT amt->>'unit') as token_count
        FROM {self.schema}.address_utxo au, LATERAL jsonb_array_elements(au.amounts) AS amt
        WHERE au.tx_hash = %s AND au.amounts IS NOT NULL
          AND au.amounts::text <> 'null' AND amt->>'unit' <> 'lovelace'
        """

        return self._get_verified_data(
            candidate_query, params, verify_query,
            min_val, max_val,
            'transaction_hash', 'token_count', f"tx_token_count {level}",
            needs_ref_block=False
        )

    def get_transactions_with_scripts(self) -> List[Dict]:
        """Get transactions that have Plutus scripts (excludes genesis block)."""
        query = f"""
        SELECT t.tx_hash as transaction_hash, t.block_hash, t.block as block_index
        FROM {self.schema}.transaction t
        WHERE t.script_datahash IS NOT NULL
          AND t.block > 0
        ORDER BY RANDOM()
        LIMIT %s
        """
        return self._execute_query(query, (TARGET_PER_LEVEL,), description="tx_has_script=true")

    def get_transactions_without_scripts(self) -> List[Dict]:
        """Get transactions without Plutus scripts (excludes genesis block)."""
        query = f"""
        SELECT t.tx_hash as transaction_hash, t.block_hash, t.block as block_index
        FROM {self.schema}.transaction t
        WHERE t.script_datahash IS NULL
          AND t.block > 0
        ORDER BY RANDOM()
        LIMIT %s
        """
        return self._execute_query(query, (TARGET_PER_LEVEL,), description="tx_has_script=false")

    def get_transactions_with_tokens(self) -> List[Dict]:
        """Get transactions that have native tokens (excludes genesis block)."""
        query = f"""
        SELECT DISTINCT t.tx_hash as transaction_hash, t.block_hash, t.block as block_index
        FROM {self.schema}.transaction t
        WHERE EXISTS (
            SELECT 1 FROM jsonb_array_elements(t.outputs) as o
            WHERE jsonb_array_length(COALESCE(o->'amounts', '[]'::jsonb)) > 1
        )
          AND t.block > 0
        ORDER BY RANDOM()
        LIMIT %s
        """
        return self._execute_query(query, (TARGET_PER_LEVEL,), description="tx_has_tokens=true")

    def get_transactions_without_tokens(self) -> List[Dict]:
        """Get transactions without native tokens - ADA only (excludes genesis block)."""
        query = f"""
        SELECT t.tx_hash as transaction_hash, t.block_hash, t.block as block_index
        FROM {self.schema}.transaction t
        WHERE NOT EXISTS (
            SELECT 1 FROM jsonb_array_elements(t.outputs) as o
            WHERE jsonb_array_length(COALESCE(o->'amounts', '[]'::jsonb)) > 1
        )
          AND t.block > 0
        ORDER BY RANDOM()
        LIMIT %s
        """
        return self._execute_query(query, (TARGET_PER_LEVEL,), description="tx_has_tokens=false")


def generate_dimensions_json(querier: YaciStoreQuerier, network: str, output_dir: str):
    """Generate dimensions.json with all thresholds and metadata."""
    # Get reference block first - all data is pinned to this block
    ref_block = querier.get_reference_block()

    dimensions_data = {
        'network': network,
        'generated_at': datetime.now(tz=None).astimezone().isoformat(),
        'reference_block': {
            'index': ref_block['index'],
            'hash': ref_block['hash']
        },
        'target_per_level': TARGET_PER_LEVEL,
        'dimensions': {}
    }

    print("\nCalculating thresholds...", flush=True)

    for dim_name, dim_config in DIMENSIONS.items():
        print(f"\n  {dim_name}:", flush=True)

        dim_data = {
            'description': dim_config['description'],
            'unit': dim_config['unit'],
            'endpoints': dim_config['endpoints'],
            'type': dim_config['type'],
            'thresholds': {}
        }

        if dim_config['type'] == 'power_of_10':
            thresholds = querier.get_power_of_10_thresholds(dim_name)
            for level, t in thresholds.items():
                dim_data['thresholds'][level] = {
                    'min': t['min'],
                    'max': t['max'],
                    'display': t['display'],
                }
            print(f"    => {len(thresholds)} power-of-10 levels: {', '.join(thresholds.keys())}")
        elif dim_config['type'] == 'percentile':
            thresholds = querier.get_percentile_thresholds(dim_name)
            for level, t in thresholds.items():
                dim_data['thresholds'][level] = {
                    'min': t['min'],
                    'max': t['max'],
                    'display': t['display'],
                }
            print(f"    => {len(thresholds)} percentile levels")
        elif dim_config['type'] == 'era':
            eras = querier.get_era_quartiles()
            for level, e in eras.items():
                dim_data['thresholds'][level] = {
                    'min': e['min'],
                    'max': e['max'],
                    'display': e['display'],
                }
            print(f"    => {len(eras)} eras: {', '.join(eras.keys())}")
        elif dim_config['type'] == 'boolean':
            dim_data['thresholds'] = {
                'true': {'display': f"with {dim_config['description'].split()[-1].lower()}"},
                'false': {'display': f"without {dim_config['description'].split()[-1].lower()}"},
            }
            print("    => boolean dimension")

        dimensions_data['dimensions'][dim_name] = dim_data

    # Write dimensions.json
    output_path = os.path.join(output_dir, 'dimensions.json')
    with open(output_path, 'w') as f:
        json.dump(dimensions_data, f, indent=2)
    print(f"\nWrote: {output_path}")

    return dimensions_data


def generate_data_files(querier: YaciStoreQuerier, network: str, output_dir: str, dimensions_data: Dict):
    """Generate CSV data files for each dimension and level."""
    print("\nGenerating data files...")

    # Create subdirectories
    for subdir in ['addresses', 'blocks', 'transactions']:
        os.makedirs(os.path.join(output_dir, subdir), exist_ok=True)

    # Common columns for reference block (added by verified methods)
    ref_cols = ['reference_block_index', 'reference_block_hash']

    # Mapping: dimension name -> (retrieval_method, subdir, columns)
    # power_of_10 methods return verified data with reference block info
    dimension_handlers = {
        # Address dimensions (power_of_10 uses verified two-phase retrieval)
        'utxo_count': {
            'percentile': (querier.get_addresses_by_utxo_count, 'addresses', ['address']),
            'power_of_10': (querier.get_addresses_by_utxo_count_power, 'addresses',
                           ['address', 'utxo_count'] + ref_cols),
        },
        'token_count': {
            'percentile': (querier.get_addresses_by_token_count, 'addresses', ['address']),
            'power_of_10': (querier.get_addresses_by_token_count_power, 'addresses',
                           ['address', 'token_count'] + ref_cols),
        },
        'tx_history': {
            'percentile': (querier.get_addresses_by_tx_history, 'addresses', ['address']),
            'power_of_10': (querier.get_addresses_by_tx_history_power, 'addresses',
                           ['address', 'tx_count'] + ref_cols),
        },
        # Block dimensions (blocks are immutable, no verification needed)
        'block_tx_count': {
            'percentile': (querier.get_blocks_by_tx_count, 'blocks', ['block_index', 'block_hash']),
            'power_of_10': (querier.get_blocks_by_tx_count_power, 'blocks', ['block_index', 'block_hash']),
        },
        'block_body_size': {
            'percentile': (querier.get_blocks_by_body_size, 'blocks', ['block_index', 'block_hash']),
        },
        'block_era': {
            'era': (querier.get_blocks_by_era_name, 'blocks', ['block_index', 'block_hash']),
        },
        # Transaction dimensions
        'tx_io_count': {
            'percentile': (querier.get_transactions_by_io_count, 'transactions', ['transaction_hash', 'block_hash', 'block_index']),
            'power_of_10': (querier.get_transactions_by_io_count_power, 'transactions', ['transaction_hash', 'block_hash', 'block_index']),
        },
        'tx_token_count': {
            'percentile': (querier.get_transactions_by_token_count, 'transactions',
                          ['transaction_hash', 'block_hash', 'block_index', 'token_count'] + ref_cols),
            'power_of_10': (querier.get_transactions_by_token_count_power, 'transactions',
                           ['transaction_hash', 'block_hash', 'block_index', 'token_count'] + ref_cols),
        },
        'tx_has_script': {
            'boolean': (None, 'transactions', ['transaction_hash', 'block_hash', 'block_index']),
        },
        'tx_has_tokens': {
            'boolean': (None, 'transactions', ['transaction_hash', 'block_hash', 'block_index']),
        },
    }

    # Generate files based on dimension config
    for dim_name, dim_data in dimensions_data['dimensions'].items():
        dim_type = dim_data['type']
        thresholds = dim_data['thresholds']

        handler_info = dimension_handlers.get(dim_name)
        if not handler_info:
            raise RuntimeError(f"No handler defined for dimension '{dim_name}' - update dimension_handlers")

        # Boolean dimension - special handling
        if dim_type == 'boolean':
            print(f"\n  {dim_name}:")
            data_true = querier.get_transactions_with_scripts()
            data_false = querier.get_transactions_without_scripts()
            subdir, columns = handler_info['boolean'][1], handler_info['boolean'][2]
            write_csv(os.path.join(output_dir, subdir, f'{dim_name}_true.csv'),
                      data_true, columns)
            write_csv(os.path.join(output_dir, subdir, f'{dim_name}_false.csv'),
                      data_false, columns)
            print(f"    true: {len(data_true)}, false: {len(data_false)}")
            continue

        # Get retrieval method based on type
        if dim_type not in handler_info:
            raise RuntimeError(f"No '{dim_type}' handler for dimension '{dim_name}' - update dimension_handlers")

        retrieval_method, subdir, columns = handler_info[dim_type]

        print(f"\n  {dim_name} ({dim_type}):")
        for level in thresholds.keys():
            data = retrieval_method(level)
            filename = f"{dim_name}_{level}.csv"
            filepath = os.path.join(output_dir, subdir, filename)
            write_csv(filepath, data, columns)

            # Format output message based on subdir
            item_type = subdir.rstrip('es').rstrip('s')  # addresses->address, blocks->block, etc.
            print(f"    {level}: {len(data)} {item_type}{'s' if len(data) != 1 else ''}")


def write_csv(filepath: str, data: List[Dict], columns: List[str]):
    """Write data to CSV file."""
    import csv
    with open(filepath, 'w', newline='') as f:
        writer = csv.DictWriter(f, fieldnames=columns, extrasaction='ignore')
        writer.writeheader()
        writer.writerows(data)


def parse_args():
    parser = argparse.ArgumentParser(
        description='Generate dimension-isolated test data for Cardano Rosetta load tests',
        formatter_class=argparse.ArgumentDefaultsHelpFormatter
    )

    parser.add_argument('--network', required=True, choices=['mainnet', 'preprod', 'preview'],
                        help='Network to generate data for')
    parser.add_argument('--output-dir', default='data',
                        help='Output directory for generated files')

    # Database connection options
    db_group = parser.add_argument_group('Database connection')
    db_group.add_argument('--db-url', dest='database_url',
                          default=os.getenv('DATABASE_URL'),
                          help='PostgreSQL connection URL (overrides individual params)')
    db_group.add_argument('--db-host', default=os.getenv('DB_HOST', 'localhost'),
                          help='Database host')
    db_group.add_argument('--db-port', default=os.getenv('DB_PORT', '5432'),
                          help='Database port')
    db_group.add_argument('--db-name', default=os.getenv('DB_NAME', 'rosetta-java'),
                          help='Database name')
    db_group.add_argument('--db-user', default=os.getenv('DB_USER', 'rosetta_db_admin'),
                          help='Database user')
    db_group.add_argument('--db-password', default=os.getenv('DB_PASSWORD', ''),
                          help='Database password')
    db_group.add_argument('--db-schema', default=os.getenv('DB_SCHEMA', 'public'),
                          help='Database schema')

    return parser.parse_args()


def main():
    args = parse_args()

    print("=" * 80)
    print("Cardano Rosetta Load Test Data Generator")
    print("Dimension-Isolated Percentile-Based Approach")
    print("=" * 80)

    # Build database config
    db_config = {
        'database_url': args.database_url,
        'host': args.db_host,
        'port': args.db_port,
        'database': args.db_name,
        'user': args.db_user,
        'password': args.db_password,
        'schema': args.db_schema,
    }

    # Determine output directory
    script_dir = os.path.dirname(os.path.abspath(__file__))
    output_dir = os.path.join(script_dir, args.output_dir, args.network)
    os.makedirs(output_dir, exist_ok=True)

    querier = YaciStoreQuerier(db_config)

    try:
        querier.connect()

        # Generate dimensions.json with thresholds
        dimensions_data = generate_dimensions_json(querier, args.network, output_dir)

        # Generate data files for each dimension and level
        generate_data_files(querier, args.network, output_dir, dimensions_data)

        print("\n" + "=" * 80)
        print("Summary")
        print("=" * 80)
        print(f"Network: {args.network}")
        print(f"Output directory: {output_dir}")
        print("\nGenerated files:")
        print("  - dimensions.json (thresholds and metadata)")
        print("  - addresses/*.csv (by utxo_count, token_count, tx_history)")
        print("  - blocks/*.csv (by tx_count, body_size, era)")
        print("  - transactions/*.csv (by io_count, has_script, has_tokens)")
        print("\nDone!")

    finally:
        querier.close()


if __name__ == "__main__":
    main()

#!/usr/bin/env python3
"""
Cardano Rosetta - Dimensional Distribution Analysis

Analyzes statistical distributions to guide bucketing strategy decisions for load testing.

Usage:
    python analyze_dimensions.py                              # Run all analyses
    python analyze_dimensions.py --dimension utxo_count       # Run specific dimension
    python analyze_dimensions.py --network mainnet            # Run for specific network
    python analyze_dimensions.py --host localhost:5432        # Use custom DB host:port
    python analyze_dimensions.py --strategy power_of_10       # Force bucketing strategy
    python analyze_dimensions.py --list                       # List available dimensions
    python analyze_dimensions.py --no-export                  # Skip image export
"""

import argparse
import os
import signal
import sys
import warnings
from collections import Counter
from pathlib import Path
from typing import Dict, List, Optional

# Suppress Kaleido deprecation warning
warnings.filterwarnings("ignore", message=".*Kaleido.*", category=DeprecationWarning)

import numpy as np

# Force exit on Ctrl+C (os._exit bypasses Python's cleanup which can hang)
signal.signal(signal.SIGINT, lambda s, f: os._exit(1))
import plotly.graph_objects as go
import plotly.io as pio
import psycopg2
from plotly.subplots import make_subplots
from psycopg2.extras import RealDictCursor

# =============================================================================
# Configuration
# =============================================================================

# Output paths
VAULT_SLIDES_STATIC = Path("/mnt/c/vault/1. projects/cardano-rosetta-java/slides/static")

# Data cache directory (for CSV persistence)
DATA_DIR = Path(__file__).parent / "data"

# Database configs
DB_CONFIGS = {
    "preprod": {
        "host": "localhost",
        "port": 5430,
        "database": "rosetta-java",
        "user": "rosetta_db_admin",
        "password": "weakpwd#123_d",
        "schema": "preprod",
    },
    "mainnet": {
        "host": "localhost",
        "port": 5432,
        "database": "rosetta-java",
        "user": "rosetta_db_admin",
        "password": "weakpwd#123_d",
        "schema": "public",
    },
}

# Global cache for fit quality (dimension_name, network) -> {'alpha': float, 'r_squared': float}
FIT_QUALITY: Dict[tuple, Dict] = {}

# Percentile style definitions (shared across panels)
PCT_STYLES = {
    "p50": {"color": "#2ca02c", "dash": "solid", "name": "p50"},
    "p90": {"color": "#ff7f0e", "dash": "dash", "name": "p90"},
    "p99": {"color": "#d62728", "dash": "dot", "name": "p99"},
    "p99.9": {"color": "#9467bd", "dash": "dashdot", "name": "p99.9"},
}

# Base axis template (for linear axes)
AXIS_TEMPLATE = dict(
    showgrid=True,
    gridwidth=1,
    gridcolor="#cccccc",
    showline=True,
    linewidth=1.5,
    linecolor="#333333",
    mirror=True,
    ticks="outside",
    tickwidth=1,
    tickcolor="#333333",
    ticklen=5,
)

# Log axis template (with minor gridlines at log intervals)
LOG_AXIS_TEMPLATE = dict(
    **AXIS_TEMPLATE,
    minor=dict(
        showgrid=True,
        gridwidth=0.5,
        gridcolor="#e0e0e0",
        dtick="D1",
    ),
    dtick=1,
)

# Era labels mapping
ERA_LABELS = {
    1: "Byron (1)",
    2: "Shelley (2)",
    3: "Allegra (3)",
    4: "Mary (4)",
    5: "Alonzo (5)",
    6: "Babbage (6)",
    7: "Conway (7)",
}

# Boolean labels mapping
SCRIPT_LABELS = {0: "No Script", 1: "Has Script"}

# =============================================================================
# SQL Queries
# =============================================================================

QUERIES = {
    # Uses TABLESAMPLE SYSTEM (1%) - full scan of 470GB address_utxo + 159GB tx_input is too slow
    # Only ~3.4% of UTXOs are unspent, but 1% sample still yields ~115k unspent records
    "utxo_count": """
        WITH utxo_counts AS (
            SELECT au.owner_addr, COUNT(*) as cnt
            FROM {schema}.address_utxo AS au TABLESAMPLE SYSTEM (1)
            WHERE au.owner_addr IS NOT NULL
              AND NOT EXISTS (
                SELECT 1 FROM {schema}.tx_input ti
                WHERE ti.tx_hash = au.tx_hash AND ti.output_index = au.output_index
              )
            GROUP BY au.owner_addr
        )
        SELECT cnt FROM utxo_counts
    """,
    # Uses TABLESAMPLE SYSTEM (1%) - LATERAL jsonb + NOT EXISTS on 470GB table is extremely slow
    "token_count": """
        WITH address_tokens AS (
            SELECT au.owner_addr, COUNT(DISTINCT amt->>'unit') as cnt
            FROM {schema}.address_utxo AS au TABLESAMPLE SYSTEM (1),
                 LATERAL jsonb_array_elements(au.amounts) AS amt
            WHERE au.owner_addr IS NOT NULL
              AND au.amounts IS NOT NULL
              AND amt->>'unit' <> 'lovelace'
              AND NOT EXISTS (
                SELECT 1 FROM {schema}.tx_input ti
                WHERE ti.tx_hash = au.tx_hash AND ti.output_index = au.output_index
              )
            GROUP BY au.owner_addr
            HAVING COUNT(DISTINCT amt->>'unit') > 0
        )
        SELECT cnt FROM address_tokens
    """,
    "block_tx_count": """
        SELECT no_of_txs as cnt
        FROM {schema}.block
        WHERE number > 0 AND no_of_txs > 0
    """,
    "block_body_size": """
        SELECT body_size as cnt
        FROM {schema}.block
        WHERE number > 0 AND no_of_txs > 0
    """,
    # Uses TABLESAMPLE SYSTEM (1%) - full scan of 470GB address_utxo is too slow
    "tx_history": """
        SELECT COUNT(DISTINCT tx_hash) as cnt
        FROM {schema}.address_utxo AS au TABLESAMPLE SYSTEM (1)
        WHERE owner_addr IS NOT NULL
        GROUP BY owner_addr
    """,
    # Uses TABLESAMPLE SYSTEM (1%) - transaction table is 155GB
    "tx_io_count": """
        SELECT
            COALESCE(jsonb_array_length(t.inputs), 0) + COALESCE(jsonb_array_length(t.outputs), 0) as cnt
        FROM {schema}.transaction AS t TABLESAMPLE SYSTEM (1)
    """,
    # Uses TABLESAMPLE SYSTEM (1%) for performance - full scan takes 30+ min on 231GB table
    # Accuracy: <2% variance on p50-p99 vs full scan (validated with multiple sample comparisons)
    "tx_token_count": """
        WITH tx_tokens AS (
            SELECT au.tx_hash,
                   COUNT(DISTINCT amt->>'unit') as cnt
            FROM {schema}.address_utxo AS au TABLESAMPLE SYSTEM (1),
                 LATERAL jsonb_array_elements(au.amounts) AS amt
            WHERE au.amounts IS NOT NULL
              AND au.amounts::text <> 'null'
              AND amt->>'unit' <> 'lovelace'
            GROUP BY au.tx_hash
            HAVING COUNT(DISTINCT amt->>'unit') > 0
        )
        SELECT cnt FROM tx_tokens
    """,
    "block_era": """
        SELECT era as cnt
        FROM {schema}.block
        WHERE number > 0 AND era IS NOT NULL
    """,
    # Uses TABLESAMPLE SYSTEM (1%) - transaction table is 155GB
    "tx_has_script": """
        SELECT CASE WHEN t.script_datahash IS NOT NULL THEN 1 ELSE 0 END as cnt
        FROM {schema}.transaction AS t TABLESAMPLE SYSTEM (1)
    """,
}

# Dimension metadata
DIMENSIONS = {
    "utxo_count": {
        "name": "Address UTXO Count",
        "question": "How many UTXOs do addresses have?",
        "unit": "UTXOs",
        "type": "numeric",
    },
    "token_count": {
        "name": "Address Token Count",
        "question": "How many token types do addresses hold?",
        "unit": "token types",
        "type": "numeric",
    },
    "block_tx_count": {
        "name": "Block Transaction Count",
        "question": "How many transactions per block?",
        "unit": "transactions",
        "type": "numeric",
    },
    "block_body_size": {
        "name": "Block Body Size",
        "question": "How large are blocks?",
        "unit": "bytes",
        "type": "numeric",
    },
    "tx_history": {
        "name": "Address Transaction History",
        "question": "How many transactions do addresses have?",
        "unit": "transactions",
        "type": "numeric",
    },
    "tx_io_count": {
        "name": "Transaction I/O Count",
        "question": "How many inputs/outputs per transaction?",
        "unit": "inputs+outputs",
        "type": "numeric",
    },
    "tx_token_count": {
        "name": "Transaction Token Types",
        "question": "How many token types per transaction?",
        "unit": "token types",
        "type": "numeric",
    },
    "block_era": {
        "name": "Block Era",
        "question": "Which eras have the most blocks?",
        "unit": "era",
        "type": "categorical",
        "labels": ERA_LABELS,
    },
    "tx_has_script": {
        "name": "Transaction Script",
        "question": "What % of transactions use smart contracts?",
        "unit": "boolean",
        "type": "categorical",
        "labels": SCRIPT_LABELS,
    },
}


# =============================================================================
# Data Persistence Functions
# =============================================================================


def save_data(data: List[int], name: str, network: str) -> None:
    """Save data to CSV file."""
    DATA_DIR.mkdir(parents=True, exist_ok=True)
    path = DATA_DIR / f"{name}_{network}.csv"
    np.savetxt(path, data, fmt="%d")
    print(f"  -> Saved to {path}")


def load_data(name: str, network: str) -> Optional[List[int]]:
    """Load data from CSV file if exists."""
    path = DATA_DIR / f"{name}_{network}.csv"
    if path.exists():
        data = np.loadtxt(path, dtype=int).tolist()
        print(f"✓ Loaded {len(data):,} records from {path}")
        return data
    return None


# =============================================================================
# Database Functions
# =============================================================================


def get_db_connection(network: str):
    """Get database connection for the specified network."""
    import time
    config = DB_CONFIGS[network]
    print(f"  Connecting to {config['host']}:{config['port']}/{config['database']} (schema: {config['schema']})...", flush=True)
    start = time.time()
    conn = psycopg2.connect(
        host=config["host"],
        port=config["port"],
        database=config["database"],
        user=config["user"],
        password=config["password"],
        connect_timeout=10,
    )
    # Set statement timeout to allow interruption
    conn.set_session(autocommit=True)
    print(f"  Connected in {time.time() - start:.1f}s", flush=True)
    return conn


def query_dimension(network: str, query: str) -> List[int]:
    """Execute query and return list of counts (memory-efficient for large result sets)."""
    import time

    conn = get_db_connection(network)
    schema = DB_CONFIGS[network]["schema"]
    formatted_query = query.format(schema=schema)

    # Use server-side cursor ONLY for queries without GROUP BY and without TABLESAMPLE
    # (huge result sets that aren't already sampled)
    query_upper = formatted_query.upper()
    use_streaming = "GROUP BY" not in query_upper and "TABLESAMPLE" not in query_upper
    mode = "streaming" if use_streaming else "batch"

    print(f"  Executing query [{mode}]...", flush=True)
    start = time.time()

    if use_streaming:
        cur = conn.cursor(name="fetch_cursor", cursor_factory=RealDictCursor)
        cur.execute(formatted_query)
        print(f"  Query started in {time.time() - start:.1f}s, fetching rows...", flush=True)

        data = []
        batch_size = 100000
        last_print = 0
        while True:
            rows = cur.fetchmany(batch_size)
            if not rows:
                break
            data.extend([row["cnt"] for row in rows])
            # Print progress every 500K rows
            if len(data) - last_print >= 500000:
                elapsed = time.time() - start
                rate = len(data) / elapsed if elapsed > 0 else 0
                print(f"  ... {len(data):,} rows ({elapsed:.0f}s, {rate/1000:.0f}K rows/s)", flush=True)
                last_print = len(data)
    else:
        cur = conn.cursor(cursor_factory=RealDictCursor)
        cur.execute(formatted_query)
        print(f"  Query executed in {time.time() - start:.1f}s, fetching results...", flush=True)
        data = [row["cnt"] for row in cur.fetchall()]

    elapsed = time.time() - start
    print(f"  ✓ {len(data):,} records in {elapsed:.1f}s", flush=True)

    cur.close()
    conn.close()
    return data


def load_or_query(name: str, network: str, query: str) -> List[int]:
    """Load from CSV if exists, otherwise query database and save."""
    data = load_data(name, network)
    if data is not None:
        return data
    data = query_dimension(network, query)
    save_data(data, name, network)
    return data


# =============================================================================
# Statistics Functions
# =============================================================================


def calc_stats(data: List[int]) -> Dict:
    """Calculate statistics."""
    arr = np.array(data)
    return {
        "count": len(data),
        "min": int(np.min(arr)),
        "max": int(np.max(arr)),
        "mean": float(np.mean(arr)),
        "median": float(np.median(arr)),
        "p50": float(np.percentile(arr, 50)),
        "p75": float(np.percentile(arr, 75)),
        "p90": float(np.percentile(arr, 90)),
        "p95": float(np.percentile(arr, 95)),
        "p99": float(np.percentile(arr, 99)),
        "p99.9": float(np.percentile(arr, 99.9)),
    }


def print_stats(stats: Dict, network: str) -> None:
    """Pretty print statistics."""
    print(f"\n{network.upper()}:")
    print(f"  Count: {stats['count']:,}")
    print(f"  Range: {stats['min']} to {stats['max']:,}")
    print(f"  Mean/Median: {stats['mean']:.1f} / {stats['median']:.1f}")
    print(
        f"  Percentiles: p50={stats['p50']:.1f}, p90={stats['p90']:.1f}, "
        f"p99={stats['p99']:.1f}, p99.9={stats['p99.9']:.1f}"
    )


# =============================================================================
# Formatting Functions
# =============================================================================


def format_power_of_10(val: float) -> str:
    """Format value as 10^n superscript."""
    if val <= 0:
        return "0"
    exp = int(np.log10(val))
    return f"10<sup>{exp}</sup>"


def format_percent(val: float) -> str:
    """Format percentage value cleanly."""
    if val >= 1:
        return f"{val:.0f}%"
    if val >= 0.1:
        return f"{val:.1f}%"
    if val >= 0.01:
        return f"{val:.2f}%"
    return f"{val:.3f}%"


def format_count(val: int) -> str:
    """Format count with K/M suffix."""
    if val >= 1_000_000:
        return f"{val/1_000_000:.1f}M"
    if val >= 1_000:
        return f"{val/1_000:.0f}K"
    return f"{val:,.0f}"


def format_value(val: float, unit: str) -> str:
    """Format value based on unit type."""
    if unit == "bytes":
        if val >= 1024 * 1024:
            return f"{val/(1024*1024):.1f}MB"
        if val >= 1024:
            return f"{val/1024:.1f}KB"
        return f"{int(val)}B"
    if val >= 1000:
        return f"{int(val):,}"
    return f"{int(val)}"


# =============================================================================
# Power Law Fitting
# =============================================================================


def fit_power_law(sorted_data: np.ndarray, ccdf: np.ndarray, x_min: int = 10):
    """Fit power-law to CCDF tail: P(X > x) ~ x^(-alpha)

    Returns: (alpha, intercept, x_fit, y_fit, r_squared)
    """
    mask = sorted_data >= x_min
    if mask.sum() < 10:
        return None, None, None, None, None

    x_tail, y_tail = sorted_data[mask], ccdf[mask]
    valid = (x_tail > 0) & (y_tail > 0)
    x_tail, y_tail = x_tail[valid], y_tail[valid]

    if len(x_tail) < 10:
        return None, None, None, None, None

    # Fit in log-log space
    log_x = np.log10(x_tail)
    log_y = np.log10(y_tail)
    slope, intercept = np.polyfit(log_x, log_y, 1)

    # Calculate R²
    y_pred = intercept + slope * log_x
    ss_res = np.sum((log_y - y_pred) ** 2)
    ss_tot = np.sum((log_y - np.mean(log_y)) ** 2)
    r_squared = 1 - (ss_res / ss_tot) if ss_tot > 0 else 0

    x_fit = np.logspace(np.log10(x_min), np.log10(x_tail.max()), 100)
    y_fit = 10 ** (intercept + slope * np.log10(x_fit))

    return -slope, intercept, x_fit, y_fit, r_squared


# =============================================================================
# Bucketing Functions
# =============================================================================


def calculate_power_of_10_buckets(
    data: List[int], stats: Dict, unit: str
) -> List[Dict]:
    """Generate power-of-10 buckets for power law distributions."""
    min_exp = int(np.floor(np.log10(max(1, stats["min"]))))
    max_exp = int(np.floor(np.log10(stats["max"])))

    arr = np.array(data)
    total = len(arr)
    buckets = []

    for exp in range(min_exp, max_exp + 1):
        lower = 10**exp
        upper = 10 ** (exp + 1)
        is_last = exp == max_exp

        if is_last:
            count = np.sum(arr >= lower)
        else:
            count = np.sum((arr >= lower) & (arr < upper))

        coverage_pct = (count / total * 100) if total > 0 else 0
        display_upper = stats["max"] if is_last else upper

        buckets.append(
            {
                "name": f"10<sup>{exp}</sup>",
                "range": (lower, display_upper),
                "display": f"{format_value(lower, unit)}-{format_value(display_upper, unit)}",
                "coverage_pct": coverage_pct,
                "count": count,
            }
        )

    return buckets


def calculate_percentile_buckets(data: List[int], stats: Dict, unit: str) -> List[Dict]:
    """Generate percentile buckets (p50, p75, p90, p95, p99)."""
    total = len(data)

    return [
        {
            "name": "p50",
            "range": (stats["min"], stats["p50"]),
            "display": f"≤{format_value(stats['p50'], unit)}",
            "coverage_pct": 50,
            "count": int(total * 0.50),
        },
        {
            "name": "p75",
            "range": (stats["p50"], stats["p75"]),
            "display": f"{format_value(stats['p50'], unit)}-{format_value(stats['p75'], unit)}",
            "coverage_pct": 25,
            "count": int(total * 0.25),
        },
        {
            "name": "p90",
            "range": (stats["p75"], stats["p90"]),
            "display": f"{format_value(stats['p75'], unit)}-{format_value(stats['p90'], unit)}",
            "coverage_pct": 15,
            "count": int(total * 0.15),
        },
        {
            "name": "p95",
            "range": (stats["p90"], stats["p95"]),
            "display": f"{format_value(stats['p90'], unit)}-{format_value(stats['p95'], unit)}",
            "coverage_pct": 5,
            "count": int(total * 0.05),
        },
        {
            "name": "p99",
            "range": (stats["p95"], stats["max"]),
            "display": f">{format_value(stats['p95'], unit)}",
            "coverage_pct": 4,
            "count": int(total * 0.04),
        },
    ]


# =============================================================================
# Plotting Functions
# =============================================================================


def plot_distribution(
    data: List[int],
    stats: Dict,
    network_name: str,
    dimension_name: str,
    unit: str,
    question: Optional[str] = None,
) -> go.Figure:
    """Plot 3-panel distribution: CDF + Histogram + CCDF with power-law fit."""
    title = f"{question} — {network_name}" if question else f"{dimension_name} Distribution — {network_name}"
    fig = make_subplots(
        rows=1,
        cols=3,
        subplot_titles=[
            "What % have ≤X?",
            "How common is each value?",
            "What % exceed X? (Tail analysis)"
        ],
        horizontal_spacing=0.08,
    )

    arr = np.array(data, dtype=float)
    n = len(arr)
    min_val, max_val = max(1, arr.min()), arr.max()
    sorted_data = np.sort(arr)

    # Downsample for performance
    if n > 10000:
        idx = np.linspace(0, n - 1, 10000, dtype=int)
        plot_x = sorted_data[idx]
        plot_y_cdf = np.arange(1, n + 1)[idx] / n * 100
        plot_y_ccdf = (n - idx) / n * 100
    else:
        idx = np.arange(n)
        plot_x = sorted_data
        plot_y_cdf = np.arange(1, n + 1) / n * 100
        plot_y_ccdf = (n - np.arange(n)) / n * 100

    # === PANEL 1: CDF ===
    fig.add_trace(
        go.Scatter(
            x=plot_x,
            y=plot_y_cdf,
            mode="lines",
            name="CDF",
            line=dict(color="#1f77b4", width=2),
            showlegend=False,
            hovertemplate=f"{unit}: %{{x:,.0f}}<br>Cumulative: %{{y:.2f}}%<extra></extra>",
        ),
        row=1,
        col=1,
    )

    # === PANEL 2: Histogram ===
    arr_positive = arr[arr > 0]
    if len(arr_positive) > 0:
        log_bins = np.logspace(np.log10(min_val), np.log10(max_val), 50)
        hist_counts, bin_edges = np.histogram(arr_positive, bins=log_bins)
        bin_centers = np.sqrt(bin_edges[:-1] * bin_edges[1:])

        fig.add_trace(
            go.Bar(
                x=bin_centers,
                y=hist_counts,
                name="Histogram",
                width=(bin_edges[1:] - bin_edges[:-1]) * 0.9,
                showlegend=False,
                marker=dict(
                    color="#1f77b4",
                    opacity=0.8,
                    line=dict(width=0.5, color="#0d47a1"),
                ),
                hovertemplate=f"{unit}: %{{x:,.0f}}<br>Count: %{{y:,}}<extra></extra>",
            ),
            row=1,
            col=2,
        )

    # === PANEL 3: CCDF + Power Law ===
    ccdf_full = (n - np.arange(n)) / n * 100
    fig.add_trace(
        go.Scatter(
            x=plot_x,
            y=plot_y_ccdf,
            mode="lines",
            name="Data (CCDF)",
            line=dict(color="#1f77b4", width=2),
            showlegend=True,
            hovertemplate=f"{unit}: %{{x:,.0f}}<br>P(X > x): %{{y:.4f}}%<extra></extra>",
        ),
        row=1,
        col=3,
    )

    alpha, _, x_fit, y_fit, r_squared = fit_power_law(sorted_data, ccdf_full, x_min=10)
    if alpha is not None:
        FIT_QUALITY[(dimension_name, network_name)] = {
            "alpha": alpha,
            "r_squared": r_squared,
        }

        fig.add_trace(
            go.Scatter(
                x=x_fit,
                y=y_fit,
                mode="lines",
                name=f"Fit: α={alpha:.2f}",
                line=dict(color="#ff7f0e", width=2),
                showlegend=True,
                hovertemplate=f"Power law<br>{unit}: %{{x:,.0f}}<br>P(X > x): %{{y:.4f}}%<extra></extra>",
            ),
            row=1,
            col=3,
        )

        if r_squared >= 0.95:
            fit_label = "Power Law ✓"
            fit_color = "#2ca02c"
        elif r_squared >= 0.85:
            fit_label = "Weak Power Law"
            fit_color = "#ff7f0e"
        else:
            fit_label = "Not Power Law"
            fit_color = "#d62728"

        fig.add_annotation(
            x=0.98,
            y=0.05,
            xref="x3 domain",
            yref="y3 domain",
            text=f"R²={r_squared:.2f}<br><b>{fit_label}</b>",
            showarrow=False,
            font=dict(size=10, color=fit_color, family="Arial"),
            bgcolor="rgba(255,255,255,0.9)",
            bordercolor=fit_color,
            borderwidth=1,
            borderpad=4,
            xanchor="right",
            yanchor="bottom",
        )

    # === Percentile lines ===
    for pct_name, pct_val in [("p50", 50), ("p90", 90), ("p99", 99), ("p99.9", 99.9)]:
        style = PCT_STYLES[pct_name]
        fig.add_hline(
            y=pct_val,
            line=dict(color=style["color"], dash=style["dash"], width=1.5),
            row=1,
            col=1,
        )
        fig.add_annotation(
            x=0.98,
            y=pct_val,
            text=pct_name,
            showarrow=False,
            xref="x domain",
            yref="y",
            xanchor="right",
            font=dict(color=style["color"], size=10, family="Arial", weight="bold"),
            row=1,
            col=1,
        )

    for i, (pct_name, pct_val) in enumerate(
        [(p, stats[p]) for p in ["p50", "p90", "p99", "p99.9"]]
    ):
        if pct_val > 0:
            style = PCT_STYLES[pct_name]
            fig.add_vline(
                x=pct_val,
                line=dict(color=style["color"], dash=style["dash"], width=1.5),
                row=1,
                col=2,
            )
            fig.add_annotation(
                x=pct_val,
                y=[0.92, 0.84, 0.76, 0.68][i],
                text=f"{pct_name}={pct_val:.0f}",
                showarrow=True,
                arrowhead=0,
                arrowwidth=1,
                arrowcolor=style["color"],
                ax=20,
                ay=-20,
                xref="x2",
                yref="y2 domain",
                font=dict(
                    color=style["color"], size=9, family="Arial", weight="bold"
                ),
                bgcolor="rgba(255,255,255,0.85)",
                row=1,
                col=2,
            )

    for pct_name, ccdf_val in [("p50", 50), ("p90", 10), ("p99", 1), ("p99.9", 0.1)]:
        style = PCT_STYLES[pct_name]
        fig.add_hline(
            y=ccdf_val,
            line=dict(color=style["color"], dash=style["dash"], width=1.5),
            row=1,
            col=3,
        )
        fig.add_annotation(
            x=0.02,
            y=ccdf_val,
            text=pct_name,
            showarrow=False,
            xref="x3 domain",
            yref="y3",
            xanchor="left",
            font=dict(color=style["color"], size=10, family="Arial", weight="bold"),
            row=1,
            col=3,
        )

    # === Layout ===
    fig.update_layout(
        title=dict(
            text=title,
            font=dict(size=16, color="#333333", family="Arial"),
            x=0.5,
        ),
        legend=dict(
            orientation="h",
            yanchor="bottom",
            y=1.02,
            xanchor="right",
            x=0.99,
            font=dict(size=10, family="Arial"),
            bgcolor="rgba(255,255,255,0.8)",
        ),
        height=450,
        width=1500,
        paper_bgcolor="white",
        plot_bgcolor="white",
        font=dict(family="Arial", size=11, color="#333333"),
        margin=dict(l=60, r=30, t=80, b=60),
    )

    # Generate power-of-10 tick values for x-axis
    x_min_log = int(np.floor(np.log10(max(0.5, min_val))))
    x_max_log = int(np.ceil(np.log10(max_val * 1.5)))
    x_tickvals = [10**i for i in range(x_min_log, x_max_log + 1)]
    x_ticktext = [f"10<sup>{i}</sup>" for i in range(x_min_log, x_max_log + 1)]

    x_range = [np.log10(max(0.5, min_val * 0.8)), np.log10(max_val * 1.5)]
    min_ccdf = max(0.001, 100 / n)

    # Panel 1: CDF
    fig.update_xaxes(
        type="log",
        title=dict(text=unit.capitalize(), font=dict(size=11)),
        range=x_range,
        tickvals=x_tickvals,
        ticktext=x_ticktext,
        **LOG_AXIS_TEMPLATE,
        row=1,
        col=1,
    )
    fig.update_yaxes(
        title=dict(text="Cumulative %", font=dict(size=11)),
        range=[-2, 105],
        tickvals=[0, 25, 50, 75, 100],
        **AXIS_TEMPLATE,
        row=1,
        col=1,
    )

    # Panel 2: Histogram
    fig.update_xaxes(
        type="log",
        title=dict(text=unit.capitalize(), font=dict(size=11)),
        range=x_range,
        tickvals=x_tickvals,
        ticktext=x_ticktext,
        **LOG_AXIS_TEMPLATE,
        row=1,
        col=2,
    )
    fig.update_yaxes(
        type="log",
        title=dict(text="Frequency", font=dict(size=11)),
        **LOG_AXIS_TEMPLATE,
        row=1,
        col=2,
    )

    # Panel 3: CCDF
    fig.update_xaxes(
        type="log",
        title=dict(text=unit.capitalize(), font=dict(size=11)),
        range=x_range,
        tickvals=x_tickvals,
        ticktext=x_ticktext,
        **LOG_AXIS_TEMPLATE,
        row=1,
        col=3,
    )

    y_min_log = int(np.floor(np.log10(min_ccdf)))
    y_tickvals_ccdf = [10**i for i in range(y_min_log, 3)]
    y_ticktext_ccdf = [format_percent(v) for v in y_tickvals_ccdf]
    fig.update_yaxes(
        type="log",
        title=dict(text="P(X > x) %", font=dict(size=11)),
        range=[np.log10(min_ccdf * 0.5), np.log10(150)],
        tickvals=y_tickvals_ccdf,
        ticktext=y_ticktext_ccdf,
        **LOG_AXIS_TEMPLATE,
        row=1,
        col=3,
    )

    return fig


def plot_categorical(
    data: List[int],
    network_name: str,
    dimension_name: str,
    labels: Optional[Dict] = None,
    question: Optional[str] = None,
) -> go.Figure:
    """Plot horizontal bar chart for categorical data."""
    title = f"{question} — {network_name}" if question else f"{dimension_name} Distribution — {network_name}"
    counts = Counter(data)
    categories = sorted(counts.keys())
    values = [counts[c] for c in categories]
    total = sum(values)
    percentages = [v / total * 100 for v in values]

    if labels:
        cat_labels = [labels.get(c, str(c)) for c in categories]
    else:
        cat_labels = [str(c) for c in categories]

    cat_labels_rev = cat_labels[::-1]
    percentages_rev = percentages[::-1]
    values_rev = values[::-1]

    colors = [
        "#1f77b4",
        "#ff7f0e",
        "#2ca02c",
        "#d62728",
        "#9467bd",
        "#8c564b",
        "#e377c2",
        "#7f7f7f",
    ]
    bar_colors = [colors[i % len(colors)] for i in range(len(categories))][::-1]

    text_labels = [
        f"{format_count(v)} ({p:.1f}%)" for v, p in zip(values_rev, percentages_rev)
    ]

    fig = go.Figure()

    fig.add_trace(
        go.Bar(
            y=cat_labels_rev,
            x=percentages_rev,
            orientation="h",
            marker=dict(color=bar_colors, line=dict(width=1, color="#333333")),
            text=text_labels,
            textposition="outside",
            textfont=dict(size=11),
            hovertemplate="<b>%{y}</b><br>Count: %{customdata:,}<br>Percentage: %{x:.2f}%<extra></extra>",
            customdata=values_rev,
        )
    )

    fig.update_layout(
        title=dict(
            text=title,
            font=dict(size=16, color="#333333", family="Arial"),
            x=0.5,
        ),
        height=max(250, 40 * len(categories) + 80),
        width=800,
        paper_bgcolor="white",
        plot_bgcolor="white",
        font=dict(family="Arial", size=11, color="#333333"),
        margin=dict(l=120, r=100, t=60, b=50),
        xaxis=dict(
            title="Percentage",
            range=[0, max(percentages) * 1.25],
            ticksuffix="%",
            showgrid=True,
            gridcolor="#e0e0e0",
            showline=True,
            linewidth=1,
            linecolor="#333333",
        ),
        yaxis=dict(
            title=None,
            showgrid=False,
            showline=True,
            linewidth=1,
            linecolor="#333333",
        ),
        bargap=0.3,
    )

    return fig


def plot_bucket_ranges(
    data: List[int],
    stats: Dict,
    dimension_name: str,
    network_name: str,
    unit: str,
    force_strategy: Optional[str] = None,
) -> go.Figure:
    """Plot range visualization showing bucket boundaries."""
    fit_info = FIT_QUALITY.get((dimension_name, network_name))

    if force_strategy == "power_of_10":
        buckets = calculate_power_of_10_buckets(data, stats, unit)
        alpha_text = f", α={fit_info['alpha']:.2f}" if fit_info else ""
        r2_text = f", R²={fit_info['r_squared']:.2f}" if fit_info else ""
        strategy_label = f"Power-of-10 (forced{alpha_text}{r2_text})"
    elif force_strategy == "percentile":
        buckets = calculate_percentile_buckets(data, stats, unit)
        r2_text = f", R²={fit_info['r_squared']:.2f}" if fit_info else ""
        strategy_label = f"Percentile (forced{r2_text})"
    elif fit_info and fit_info["r_squared"] >= 0.95:
        buckets = calculate_power_of_10_buckets(data, stats, unit)
        strategy_label = (
            f"Power-of-10 (α={fit_info['alpha']:.2f}, R²={fit_info['r_squared']:.2f})"
        )
    else:
        buckets = calculate_percentile_buckets(data, stats, unit)
        r2_text = f", R²={fit_info['r_squared']:.2f}" if fit_info else ""
        strategy_label = f"Percentile{r2_text}"

    colors = [
        "#1f77b4",
        "#ff7f0e",
        "#2ca02c",
        "#d62728",
        "#9467bd",
        "#8c564b",
        "#e377c2",
    ]

    fig = go.Figure()
    buckets_rev = buckets[::-1]

    for i, bucket in enumerate(buckets_rev):
        lower, upper = bucket["range"]
        color = colors[i % len(colors)]
        y_pos = len(buckets_rev) - i - 1

        fig.add_trace(
            go.Scatter(
                x=[lower, upper],
                y=[y_pos, y_pos],
                mode="lines+markers",
                line=dict(color=color, width=3),
                marker=dict(
                    size=12,
                    color=color,
                    symbol="circle",
                    line=dict(width=2, color="#333333"),
                ),
                hovertemplate=(
                    f"<b>{bucket['name']}</b><br>Range: {bucket['display']}<br>"
                    f"Coverage: {bucket['coverage_pct']:.1f}%<br>"
                    f"Count: {format_count(bucket['count'])}<extra></extra>"
                ),
                showlegend=False,
            )
        )

        fig.add_annotation(
            x=0.02,
            y=y_pos,
            text=f"<b>{bucket['name']}</b>",
            showarrow=False,
            xref="paper",
            yref="y",
            xanchor="right",
            font=dict(size=11, color="#333333", family="Arial"),
        )

        fig.add_annotation(
            x=1.02,
            y=y_pos,
            text=f"{bucket['display']} ({bucket['coverage_pct']:.1f}%)",
            showarrow=False,
            xref="paper",
            yref="y",
            xanchor="left",
            font=dict(size=10, color="#333333", family="Arial"),
        )

    min_exp = int(np.floor(np.log10(max(0.5, stats["min"]))))
    max_exp = int(np.ceil(np.log10(stats["max"])))
    x_tickvals = [10**i for i in range(min_exp, max_exp + 1)]
    x_ticktext = [f"10<sup>{i}</sup>" for i in range(min_exp, max_exp + 1)]

    fig.update_layout(
        title=dict(
            text=f"Bucketing Strategy: {dimension_name}<br><sub>{network_name} | {strategy_label}</sub>",
            font=dict(size=14, color="#333333", family="Arial"),
            x=0.5,
        ),
        height=max(200, 50 * len(buckets) + 80),
        width=1000,
        paper_bgcolor="white",
        plot_bgcolor="white",
        font=dict(family="Arial", size=11, color="#333333"),
        margin=dict(l=100, r=200, t=80, b=60),
        xaxis=dict(
            type="log",
            title=unit.capitalize(),
            tickvals=x_tickvals,
            ticktext=x_ticktext,
            range=[
                np.log10(max(0.5, stats["min"] * 0.5)),
                np.log10(stats["max"] * 2),
            ],
            showgrid=True,
            gridcolor="#e0e0e0",
            gridwidth=1,
            showline=True,
            linewidth=1.5,
            linecolor="#333333",
            mirror=True,
        ),
        yaxis=dict(
            title=None,
            range=[-0.5, len(buckets) - 0.5],
            showticklabels=False,
            showgrid=True,
            gridcolor="#f0f0f0",
            gridwidth=0.5,
            zeroline=False,
        ),
    )

    return fig


# =============================================================================
# Combined Network Plotting Functions
# =============================================================================

NETWORK_COLORS = {
    "preprod": {"main": "#1f77b4", "light": "#aec7e8"},  # Blue
    "mainnet": "#ff7f0e",  # Orange
}


def plot_distribution_combined(
    data_by_network: Dict[str, List[int]],
    stats_by_network: Dict[str, Dict],
    dimension_name: str,
    unit: str,
    question: Optional[str] = None,
) -> go.Figure:
    """Plot 3-panel distribution with both networks overlaid, including power law fits."""
    title = question or f"{dimension_name} Distribution"
    fig = make_subplots(
        rows=1,
        cols=3,
        subplot_titles=[
            "What % have ≤X?",
            "How common is each value?",
            "What % exceed X? (Tail analysis)"
        ],
        horizontal_spacing=0.08,
    )

    # Find global min/max across both networks
    all_data = []
    for data in data_by_network.values():
        all_data.extend(data)
    global_min = max(1, min(all_data))
    global_max = max(all_data)

    fit_results = {}

    for network, data in data_by_network.items():
        color = NETWORK_COLORS.get(network, "#333333")
        if isinstance(color, dict):
            color = color["main"]

        stats = stats_by_network[network]
        arr = np.array(data, dtype=float)
        n = len(arr)
        sorted_data = np.sort(arr)

        # Downsample for performance
        if n > 10000:
            idx = np.linspace(0, n - 1, 10000, dtype=int)
            plot_x = sorted_data[idx]
            plot_y_cdf = np.arange(1, n + 1)[idx] / n * 100
            plot_y_ccdf = (n - idx) / n * 100
        else:
            idx = np.arange(n)
            plot_x = sorted_data
            plot_y_cdf = np.arange(1, n + 1) / n * 100
            plot_y_ccdf = (n - np.arange(n)) / n * 100

        # CDF
        fig.add_trace(
            go.Scatter(
                x=plot_x, y=plot_y_cdf, mode="lines",
                name=f"{network.capitalize()}",
                line=dict(color=color, width=2),
                legendgroup=network,
                showlegend=True,
            ),
            row=1, col=1,
        )

        # Histogram (proper bars)
        arr_positive = arr[arr > 0]
        if len(arr_positive) > 0:
            log_bins = np.logspace(np.log10(global_min), np.log10(global_max), 50)
            hist_counts, bin_edges = np.histogram(arr_positive, bins=log_bins)
            bin_centers = np.sqrt(bin_edges[:-1] * bin_edges[1:])

            # Parse color to RGB for opacity
            r, g, b = [int(color.lstrip('#')[i:i+2], 16) for i in (0, 2, 4)]

            fig.add_trace(
                go.Bar(
                    x=bin_centers, y=hist_counts,
                    name=f"{network.capitalize()}",
                    width=(bin_edges[1:] - bin_edges[:-1]) * 0.9,
                    legendgroup=network,
                    showlegend=False,
                    marker=dict(
                        color=f"rgba({r},{g},{b},0.6)",
                        line=dict(width=0.5, color=color),
                    ),
                ),
                row=1, col=2,
            )

        # CCDF
        fig.add_trace(
            go.Scatter(
                x=plot_x, y=plot_y_ccdf, mode="lines",
                name=f"{network.capitalize()}",
                line=dict(color=color, width=2),
                legendgroup=network,
                showlegend=False,
            ),
            row=1, col=3,
        )

        # Power law fit
        ccdf_full = (n - np.arange(n)) / n * 100
        alpha, _, x_fit, y_fit, r_squared = fit_power_law(sorted_data, ccdf_full, x_min=10)
        if alpha is not None:
            fit_results[network] = {"alpha": alpha, "r_squared": r_squared}
            # Lighter/dashed line for fit
            fig.add_trace(
                go.Scatter(
                    x=x_fit, y=y_fit, mode="lines",
                    name=f"{network.capitalize()} fit (α={alpha:.2f})",
                    line=dict(color=color, width=1.5, dash="dash"),
                    legendgroup=network,
                    showlegend=False,
                ),
                row=1, col=3,
            )

    # Add R² annotation for fits
    if fit_results:
        annotation_text = "<br>".join([
            f"{net.capitalize()}: R²={info['r_squared']:.2f}, α={info['alpha']:.2f}"
            for net, info in fit_results.items()
        ])
        fig.add_annotation(
            x=0.98, y=0.05,
            xref="x3 domain", yref="y3 domain",
            text=annotation_text,
            showarrow=False,
            font=dict(size=9, family="Arial"),
            bgcolor="rgba(255,255,255,0.9)",
            bordercolor="#333333",
            borderwidth=1,
            borderpad=4,
            xanchor="right", yanchor="bottom",
        )

    # Percentile lines on CDF (horizontal)
    for pct_name, pct_val in [("p50", 50), ("p90", 90), ("p99", 99)]:
        style = PCT_STYLES[pct_name]
        fig.add_hline(
            y=pct_val,
            line=dict(color=style["color"], dash=style["dash"], width=1),
            row=1, col=1,
        )
        fig.add_annotation(
            x=0.98, y=pct_val,
            text=pct_name,
            showarrow=False,
            xref="x domain", yref="y",
            xanchor="right",
            font=dict(color=style["color"], size=9, family="Arial", weight="bold"),
            row=1, col=1,
        )

    # Percentile lines on CCDF (horizontal)
    for pct_name, ccdf_val in [("p50", 50), ("p90", 10), ("p99", 1)]:
        style = PCT_STYLES[pct_name]
        fig.add_hline(
            y=ccdf_val,
            line=dict(color=style["color"], dash=style["dash"], width=1),
            row=1, col=3,
        )
        fig.add_annotation(
            x=0.02, y=ccdf_val,
            text=pct_name,
            showarrow=False,
            xref="x3 domain", yref="y3",
            xanchor="left",
            font=dict(color=style["color"], size=9, family="Arial", weight="bold"),
            row=1, col=3,
        )

    # Layout
    fig.update_layout(
        title=dict(
            text=title,
            font=dict(size=16, color="#333333", family="Arial"),
            x=0.5,
            y=0.98,
        ),
        legend=dict(
            orientation="h", yanchor="top", y=1.15, xanchor="center", x=0.5,
            font=dict(size=11, family="Arial"),
        ),
        barmode="overlay",
        height=480, width=1500,
        paper_bgcolor="white", plot_bgcolor="white",
        font=dict(family="Arial", size=11, color="#333333"),
        margin=dict(l=60, r=30, t=110, b=60),
    )

    # Axis formatting
    x_min_log = int(np.floor(np.log10(max(0.5, global_min))))
    x_max_log = int(np.ceil(np.log10(global_max * 1.5)))
    x_tickvals = [10**i for i in range(x_min_log, x_max_log + 1)]
    x_ticktext = [f"10<sup>{i}</sup>" for i in range(x_min_log, x_max_log + 1)]
    x_range = [np.log10(max(0.5, global_min * 0.8)), np.log10(global_max * 1.5)]

    for col in [1, 2, 3]:
        fig.update_xaxes(
            type="log", title=dict(text=unit.capitalize(), font=dict(size=11)),
            range=x_range, tickvals=x_tickvals, ticktext=x_ticktext,
            **LOG_AXIS_TEMPLATE, row=1, col=col,
        )

    fig.update_yaxes(title="Cumulative %", range=[-2, 105], **AXIS_TEMPLATE, row=1, col=1)
    fig.update_yaxes(type="log", title="Frequency", **LOG_AXIS_TEMPLATE, row=1, col=2)

    min_ccdf = max(0.001, 100 / max(len(d) for d in data_by_network.values()))
    fig.update_yaxes(
        type="log", title="P(X > x) %",
        range=[np.log10(min_ccdf * 0.5), np.log10(150)],
        **LOG_AXIS_TEMPLATE, row=1, col=3,
    )

    return fig


def plot_categorical_combined(
    data_by_network: Dict[str, List[int]],
    dimension_name: str,
    labels: Optional[Dict] = None,
    question: Optional[str] = None,
) -> go.Figure:
    """Plot grouped bar chart comparing both networks."""
    title = question or f"{dimension_name} Distribution"
    # Get all categories across both networks
    all_categories = set()
    for data in data_by_network.values():
        all_categories.update(data)
    categories = sorted(all_categories)

    if labels:
        cat_labels = [labels.get(c, str(c)) for c in categories]
    else:
        cat_labels = [str(c) for c in categories]

    fig = go.Figure()

    for network, data in data_by_network.items():
        color = NETWORK_COLORS.get(network, "#333333")
        if isinstance(color, dict):
            color = color["main"]

        counts = Counter(data)
        total = len(data)
        percentages = [(counts.get(c, 0) / total * 100) for c in categories]

        fig.add_trace(
            go.Bar(
                name=network.capitalize(),
                x=cat_labels,
                y=percentages,
                marker_color=color,
                text=[f"{p:.1f}%" for p in percentages],
                textposition="outside",
            )
        )

    width = max(600, 120 * len(categories))

    fig.update_layout(
        title=dict(
            text=title,
            font=dict(size=14, color="#333333", family="Arial"),
            x=0.5,
        ),
        barmode="group",
        height=220, width=width,
        paper_bgcolor="white", plot_bgcolor="white",
        font=dict(family="Arial", size=10, color="#333333"),
        margin=dict(l=50, r=25, t=60, b=40),
        yaxis=dict(title="Percentage", ticksuffix="%"),
        xaxis=dict(tickangle=-45 if len(categories) > 4 else 0),
        legend=dict(orientation="h", yanchor="bottom", y=1.02, xanchor="center", x=0.5),
    )

    return fig


# =============================================================================
# Analysis Functions
# =============================================================================


def analyze_numeric_dimension(
    dim_key: str,
    network: str,
    export: bool = True,
    force_strategy: Optional[str] = None,
) -> Optional[Dict]:
    """Analyze a numeric dimension for a specific network. Returns stats dict."""
    dim = DIMENSIONS[dim_key]
    query = QUERIES[dim_key]

    print(f"\n{'='*60}")
    print(f"Analyzing: {dim['name']} ({network})")
    print("=" * 60)

    # Load or query data
    data = load_or_query(dim_key, network, query)
    stats = calc_stats(data)
    print_stats(stats, network)

    # Plot distribution
    question = dim.get("question")
    fig_dist = plot_distribution(data, stats, network.capitalize(), dim["name"], dim["unit"], question)
    fig_dist.show()

    # Plot bucket strategy
    fig_bucket = plot_bucket_ranges(
        data, stats, dim["name"], network.capitalize(), dim["unit"], force_strategy
    )
    fig_bucket.show()

    # Export
    if export:
        VAULT_SLIDES_STATIC.mkdir(parents=True, exist_ok=True)
        dist_path = VAULT_SLIDES_STATIC / f"loadtest-dimension_{dim_key}_{network}.png"
        fig_dist.write_image(dist_path, width=1600, height=500, scale=2)
        print(f"✓ Saved distribution to {dist_path}")

    return {"type": "numeric", "dim_key": dim_key, "network": network, **stats}


def analyze_categorical_dimension(
    dim_key: str,
    network: str,
    export: bool = True,
    force_strategy: Optional[str] = None,  # Not used for categorical, but kept for API consistency
) -> Optional[Dict]:
    """Analyze a categorical dimension for a specific network. Returns stats dict."""
    dim = DIMENSIONS[dim_key]
    query = QUERIES[dim_key]

    print(f"\n{'='*60}")
    print(f"Analyzing: {dim['name']} ({network})")
    print("=" * 60)

    # Load or query data
    data = load_or_query(dim_key, network, query)
    print(f"{network.capitalize()}: {len(data):,} records")

    # Plot
    labels = dim.get("labels")
    question = dim.get("question")
    fig = plot_categorical(data, network.capitalize(), dim["name"], labels, question)
    fig.show()

    # Calculate category distribution
    counts = Counter(data)
    total = len(data)

    # Print bucket strategy for boolean dimensions
    if dim_key == "tx_has_script":
        pct_false = (counts[0] / total * 100) if 0 in counts else 0
        pct_true = (counts[1] / total * 100) if 1 in counts else 0
        print(f"\nBucket 'false' (no script):   {counts.get(0, 0):>10,} ({pct_false:>5.1f}%)")
        print(f"Bucket 'true'  (has script):  {counts.get(1, 0):>10,} ({pct_true:>5.1f}%)")

    # Export
    if export:
        VAULT_SLIDES_STATIC.mkdir(parents=True, exist_ok=True)
        path = VAULT_SLIDES_STATIC / f"loadtest-dimension_{dim_key}_{network}.png"
        fig.write_image(path, width=1200, height=500, scale=2)
        print(f"✓ Saved to {path}")

    return {
        "type": "categorical",
        "dim_key": dim_key,
        "network": network,
        "count": total,
        "categories": len(counts),
        "distribution": dict(counts),
    }


def analyze_dimension(
    dim_key: str,
    network: str,
    export: bool = True,
    force_strategy: Optional[str] = None,
) -> Optional[Dict]:
    """Analyze a dimension (dispatches to correct handler based on type). Returns stats dict."""
    dim = DIMENSIONS[dim_key]
    if dim["type"] == "numeric":
        return analyze_numeric_dimension(dim_key, network, export, force_strategy)
    else:
        return analyze_categorical_dimension(dim_key, network, export, force_strategy)


def run_all_analyses(
    networks: List[str],
    export: bool = True,
    force_strategy: Optional[str] = None,
) -> List[Dict]:
    """Run all dimension analyses. Returns list of stats dicts."""
    results = []
    for dim_key in DIMENSIONS:
        for network in networks:
            try:
                result = analyze_dimension(dim_key, network, export, force_strategy)
                if result:
                    results.append(result)
            except Exception as e:
                print(f"✗ Error analyzing {dim_key} for {network}: {e}")
    return results


def generate_combined_plots(
    dim_key: Optional[str] = None,
    export: bool = True,
) -> List[Dict]:
    """Generate combined plots comparing preprod vs mainnet. Returns stats for all dimensions."""
    dimensions_to_process = [dim_key] if dim_key else list(DIMENSIONS.keys())
    all_results = []

    for dk in dimensions_to_process:
        dim = DIMENSIONS[dk]
        query = QUERIES[dk]

        print(f"\n{'='*60}")
        print(f"Combined Plot: {dim['name']}")
        print("=" * 60)

        # Load data for both networks
        data_by_network = {}
        stats_by_network = {}

        for network in ["preprod", "mainnet"]:
            try:
                data = load_or_query(dk, network, query)
                data_by_network[network] = data
                if dim["type"] == "numeric":
                    stats = calc_stats(data)
                    stats_by_network[network] = stats
                    all_results.append({"type": "numeric", "dim_key": dk, "network": network, **stats})
                else:
                    counts = Counter(data)
                    all_results.append({
                        "type": "categorical",
                        "dim_key": dk,
                        "network": network,
                        "count": len(data),
                        "categories": len(counts),
                        "distribution": dict(counts),
                    })
            except Exception as e:
                print(f"  ✗ Could not load {network}: {e}")
                continue

        if len(data_by_network) < 2:
            print("  ✗ Need both networks for combined plot")
            continue

        # Print stats comparison
        if dim["type"] == "numeric":
            print(f"\n{'Network':<10} {'Count':>12} {'Min':>8} {'Max':>10} {'Mean':>8} {'p50':>8} {'p90':>8} {'p99':>8} {'R²':>6} {'α':>6}")
            print("-" * 100)
            for network in ["preprod", "mainnet"]:
                s = stats_by_network[network]
                # Calculate power law fit for strategy determination
                arr = np.array(data_by_network[network], dtype=float)
                sorted_data = np.sort(arr)
                n = len(arr)
                ccdf = (n - np.arange(n)) / n * 100
                alpha, _, _, _, r_squared = fit_power_law(sorted_data, ccdf, x_min=10)
                r2_str = f"{r_squared:.2f}" if r_squared else "N/A"
                alpha_str = f"{alpha:.2f}" if alpha else "N/A"
                print(f"{network:<10} {s['count']:>12,} {s['min']:>8,} {s['max']:>10,} {s['mean']:>8.1f} {s['p50']:>8.0f} {s['p90']:>8.0f} {s['p99']:>8.0f} {r2_str:>6} {alpha_str:>6}")

            # Determine strategy and calculate buckets based on mainnet
            mainnet_data = data_by_network["mainnet"]
            mainnet_stats = stats_by_network["mainnet"]
            mainnet_arr = np.array(mainnet_data, dtype=float)
            sorted_mainnet = np.sort(mainnet_arr)
            n_mainnet = len(mainnet_arr)
            ccdf_mainnet = (n_mainnet - np.arange(n_mainnet)) / n_mainnet * 100
            _, _, _, _, r2_mainnet = fit_power_law(sorted_mainnet, ccdf_mainnet, x_min=10)

            if r2_mainnet and r2_mainnet >= 0.95:
                strategy = f"Power-of-10 (R²={r2_mainnet:.2f} ≥ 0.95)"
                buckets = calculate_power_of_10_buckets(mainnet_data, mainnet_stats, dim["unit"])
            else:
                r2_str = f"R²={r2_mainnet:.2f}" if r2_mainnet else "no fit"
                strategy = f"Percentile ({r2_str} < 0.95)"
                buckets = calculate_percentile_buckets(mainnet_data, mainnet_stats, dim["unit"])

            print(f"\n→ Strategy: {strategy}")
            print(f"\n{'Bucket':<10} {'Range':<30} {'Coverage':>10}")
            print("-" * 52)
            for b in buckets:
                name = b["name"].replace("<sup>", "^").replace("</sup>", "")
                print(f"{name:<10} {b['display']:<30} {b['coverage_pct']:>9.1f}%")
        else:
            print(f"\n{'Network':<10} {'Count':>12} {'Categories':>12}")
            print("-" * 36)
            for network in ["preprod", "mainnet"]:
                data = data_by_network[network]
                counts = Counter(data)
                print(f"{network:<10} {len(data):>12,} {len(counts):>12}")
            print(f"\n→ Strategy: Categorical sampling")

        # Generate combined plot
        question = dim.get("question")
        if dim["type"] == "numeric":
            fig = plot_distribution_combined(
                data_by_network, stats_by_network, dim["name"], dim["unit"], question
            )
        else:
            labels = dim.get("labels")
            fig = plot_categorical_combined(data_by_network, dim["name"], labels, question)

        fig.show()

        # Export
        if export:
            VAULT_SLIDES_STATIC.mkdir(parents=True, exist_ok=True)
            path = VAULT_SLIDES_STATIC / f"loadtest-dimension_{dk}_combined.png"
            # Use figure's own dimensions for export
            fig.write_image(path, scale=2)
            print(f"✓ Saved combined plot to {path}")

    return all_results


def print_summary_table(results: List[Dict]) -> None:
    """Print a summary table of all analysis results."""
    if not results:
        return

    print("\n" + "=" * 100)
    print("SUMMARY TABLE")
    print("=" * 100)

    # Separate numeric and categorical results
    numeric_results = [r for r in results if r.get("type") == "numeric"]
    categorical_results = [r for r in results if r.get("type") == "categorical"]

    if numeric_results:
        print("\nNumeric Dimensions:")
        print("-" * 100)
        print(f"{'Dimension':<20} {'Network':<8} {'Count':>12} {'Min':>10} {'Max':>10} {'Mean':>10} {'p50':>8} {'p90':>8} {'p99':>8}")
        print("-" * 100)
        for r in numeric_results:
            dim_name = r["dim_key"][:20]
            print(
                f"{dim_name:<20} {r['network']:<8} {r['count']:>12,} {r['min']:>10,} {r['max']:>10,} "
                f"{r['mean']:>10.1f} {r['p50']:>8.0f} {r['p90']:>8.0f} {r['p99']:>8.0f}"
            )

    if categorical_results:
        print("\nCategorical Dimensions:")
        print("-" * 60)
        print(f"{'Dimension':<20} {'Network':<8} {'Count':>12} {'Categories':>12}")
        print("-" * 60)
        for r in categorical_results:
            dim_name = r["dim_key"][:20]
            print(f"{dim_name:<20} {r['network']:<8} {r['count']:>12,} {r['categories']:>12}")

    print("=" * 100)


# =============================================================================
# CLI
# =============================================================================


def parse_host(host_str: str) -> tuple[str, int]:
    """Parse host:port string into (host, port) tuple."""
    if ":" in host_str:
        host, port_str = host_str.rsplit(":", 1)
        return host, int(port_str)
    return host_str, 5432  # Default PostgreSQL port


def main():
    parser = argparse.ArgumentParser(
        description="Cardano Rosetta - Dimensional Distribution Analysis",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""
Examples:
    python analyze_dimensions.py                              # Run all analyses
    python analyze_dimensions.py --dimension utxo_count       # Specific dimension
    python analyze_dimensions.py --network mainnet            # Specific network
    python analyze_dimensions.py --host 192.168.1.100:5432    # Custom DB host:port
    python analyze_dimensions.py --schema public              # Custom DB schema
    python analyze_dimensions.py --strategy power_of_10       # Force bucketing strategy
    python analyze_dimensions.py --combined                   # Overlaid preprod vs mainnet plots
    python analyze_dimensions.py --combined -d utxo_count     # Combined plot for one dimension
    python analyze_dimensions.py --list                       # List dimensions
    python analyze_dimensions.py --no-export                  # Skip image export
        """,
    )

    parser.add_argument(
        "--dimension",
        "-d",
        choices=list(DIMENSIONS.keys()),
        help="Analyze a specific dimension",
    )
    parser.add_argument(
        "--network",
        "-n",
        choices=["preprod", "mainnet"],
        help="Analyze for a specific network",
    )
    parser.add_argument(
        "--host",
        "-H",
        metavar="HOST:PORT",
        help="Database host and port (e.g., localhost:5432). Overrides default for the selected network.",
    )
    parser.add_argument(
        "--schema",
        "-S",
        help="Database schema (default: 'preprod' for preprod, 'public' for mainnet)",
    )
    parser.add_argument(
        "--strategy",
        "-s",
        choices=["power_of_10", "percentile"],
        help="Force a specific bucketing strategy (default: auto-select based on R²)",
    )
    parser.add_argument(
        "--no-export",
        action="store_true",
        help="Skip exporting images",
    )
    parser.add_argument(
        "--combined",
        "-c",
        action="store_true",
        help="Generate combined plots (preprod vs mainnet overlaid)",
    )
    parser.add_argument(
        "--list",
        "-l",
        action="store_true",
        help="List available dimensions",
    )

    args = parser.parse_args()

    if args.list:
        print("\nAvailable dimensions:")
        print("-" * 50)
        for key, dim in DIMENSIONS.items():
            print(f"  {key:20} - {dim['name']} ({dim['type']})")
        print("\nBucketing strategies:")
        print("-" * 50)
        print("  power_of_10  - Buckets at powers of 10 (best for power-law distributions)")
        print("  percentile   - Buckets at percentile boundaries (p50, p75, p90, p95, p99)")
        return

    # Apply host override if provided
    if args.host:
        host, port = parse_host(args.host)
        for network_config in DB_CONFIGS.values():
            network_config["host"] = host
            network_config["port"] = port
        print(f"Using database host: {host}:{port}")

    # Apply schema override if provided
    if args.schema:
        for network_config in DB_CONFIGS.values():
            network_config["schema"] = args.schema
        print(f"Using database schema: {args.schema}")

    networks = [args.network] if args.network else ["preprod", "mainnet"]
    export = not args.no_export
    strategy = args.strategy

    # Combined mode: generate overlaid plots for both networks
    if args.combined:
        results = generate_combined_plots(args.dimension, export)
        print_summary_table(results)
        print("\n✓ Combined plots complete")
        return

    results = []
    if args.dimension:
        for network in networks:
            try:
                result = analyze_dimension(args.dimension, network, export, strategy)
                if result:
                    results.append(result)
            except Exception as e:
                print(f"✗ Error analyzing {args.dimension} for {network}: {e}")
    else:
        results = run_all_analyses(networks, export, strategy)

    # Print summary table
    print_summary_table(results)

    print("\n✓ Analysis complete")


if __name__ == "__main__":
    main()

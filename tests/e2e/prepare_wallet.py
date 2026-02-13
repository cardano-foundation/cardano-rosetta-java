#!/usr/bin/env python3
# /// script
# dependencies = [
#     "pycardano>=0.12.0",
#     "blockfrost-python>=0.6.0",
#     "python-dotenv>=0.19.0",
#     "mnemonic>=0.20",
#     "rich>=13.0",
#     "requests>=2.28.0",
# ]
# requires-python = ">=3.11"
# ///
"""
Prepare and validate the e2e test wallet on a Cardano testnet.

Usage:
    uv run prepare_wallet.py check       # Full pre-flight check (UTXOs + on-chain state + .env)
    uv run prepare_wallet.py split       # Split UTXOs into 12+ ADA-only outputs
    uv run prepare_wallet.py mint        # Mint a native token (token bundle UTXO)
    uv run prepare_wallet.py lookup      # Look up governance values for .env
"""
import os
import re
import sys

import requests as http_requests
from dotenv import load_dotenv
from blockfrost import ApiUrls, ApiError, BlockFrostApi
from pycardano.crypto.bech32 import encode as bech32_encode
from pycardano import (
    Address,
    Asset,
    AssetName,
    BlockFrostChainContext,
    HDWallet,
    MultiAsset,
    Network,
    PaymentExtendedSigningKey,
    ScriptPubkey,
    StakeExtendedSigningKey,
    TransactionBuilder,
    TransactionOutput,
    Value,
    min_lovelace,
)
from rich.console import Console
from rich.table import Table
from rich.panel import Panel
from rich import box

console = Console()

load_dotenv()

# --- Environment ---
NETWORK = os.getenv("CARDANO_NETWORK", "preview")
BLOCKFROST_KEY = os.getenv("BLOCKFROST_PROJECT_ID") or os.getenv("BLOCKFROST_API_KEY")
MNEMONIC = os.getenv("TEST_WALLET_MNEMONIC")

# --- Constants ---
MIN_ADA_ONLY_UTXOS = 11
MIN_ADA_FOR_FEES = 5_000_000  # 5 ADA in lovelace
SPLIT_OUTPUT_LOVELACE = 25_000_000  # 25 ADA per split output
SPLIT_NUM_OUTPUTS = 12
SPLIT_FEE_HEADROOM = 2_000_000  # ~2 ADA headroom for fees
MAX_INPUTS = 20  # cap inputs to avoid exceeding max tx size
MINT_AMOUNT = 1_000_000
MINT_TOKEN_NAME = b"E2ETestToken"


# ── Shared helpers ──────────────────────────────────────────────────────────


def get_blockfrost_url(network: str) -> str:
    n = network.lower()
    if n in ("preprod", "testnet"):
        return ApiUrls.preprod.value
    if n == "preview":
        return ApiUrls.preview.value
    if n == "mainnet":
        return ApiUrls.mainnet.value
    console.print(f"[bold red]ERROR:[/] Unsupported network: {network}")
    sys.exit(1)


def resolve_network(network: str) -> Network:
    if network.lower() in ("mainnet",):
        return Network.MAINNET
    return Network.TESTNET


def is_hex(value: str) -> bool:
    return bool(re.fullmatch(r"[0-9a-fA-F]+", value))


def load_wallet():
    """Load wallet from mnemonic. Returns (payment_skey, payment_vkey, stake_vkey, address)."""
    try:
        hd = HDWallet.from_mnemonic(MNEMONIC)
    except Exception as e:
        console.print(f"[bold red]ERROR:[/] Invalid mnemonic: {e}")
        sys.exit(1)

    pay_sk = PaymentExtendedSigningKey.from_hdwallet(
        hd.derive_from_path("m/1852'/1815'/0'/0/0")
    )
    pay_vk = pay_sk.to_verification_key()

    stake_sk = StakeExtendedSigningKey.from_hdwallet(
        hd.derive_from_path("m/1852'/1815'/0'/2/0")
    )
    stake_vk = stake_sk.to_verification_key()

    addr = Address(
        payment_part=pay_vk.hash(),
        staking_part=stake_vk.hash(),
        network=resolve_network(NETWORK),
    )
    return pay_sk, pay_vk, stake_vk, addr


def get_context():
    return BlockFrostChainContext(
        project_id=BLOCKFROST_KEY,
        base_url=get_blockfrost_url(NETWORK),
        network=resolve_network(NETWORK),
    )


def get_api():
    return BlockFrostApi(
        project_id=BLOCKFROST_KEY,
        base_url=get_blockfrost_url(NETWORK),
    )


def blockfrost_get(path: str, **params):
    """Direct REST call to Blockfrost (for endpoints missing from the SDK)."""
    base = get_blockfrost_url(NETWORK)
    url = f"{base}/v0/{path.lstrip('/')}"
    r = http_requests.get(url, headers={"project_id": BLOCKFROST_KEY}, params=params, timeout=30)
    r.raise_for_status()
    return r.json()


def drep_bech32(hex_hash: str, is_script: bool = False) -> str:
    """Convert a raw 28-byte DRep hex hash to CIP-129 bech32 drep ID."""
    prefix = "23" if is_script else "22"
    return bech32_encode("drep", bytes.fromhex(prefix + hex_hash))


def is_ada_only(utxo) -> bool:
    amt = utxo.output.amount
    if isinstance(amt, int):
        return True
    if isinstance(amt, Value):
        return amt.multi_asset is None or len(amt.multi_asset) == 0
    return False


def utxo_lovelace(utxo) -> int:
    amt = utxo.output.amount
    if isinstance(amt, int):
        return amt
    if isinstance(amt, Value):
        return amt.coin
    return 0


def select_ada_utxos(all_utxos, required_lovelace: int, max_count: int = MAX_INPUTS):
    """Select ADA-only UTXOs up to required amount, capped at max_count. Largest first."""
    ada_only = [u for u in all_utxos if is_ada_only(u)]
    ada_only.sort(key=utxo_lovelace, reverse=True)

    selected = []
    total = 0
    for u in ada_only[:max_count]:
        selected.append(u)
        total += utxo_lovelace(u)
        if total >= required_lovelace:
            break
    return selected, total


def analyze_utxos(utxos):
    """Analyze Blockfrost UTXOs. Returns (ada_only_count, with_tokens_count, has_fee_utxo, total_lovelace)."""
    ada_only_count = 0
    with_tokens_count = 0
    has_fee_utxo = False
    total_lovelace = 0

    for u in utxos:
        units = [a.unit for a in u.amount]
        lovelace = sum(int(a.quantity) for a in u.amount if a.unit == "lovelace")
        total_lovelace += lovelace
        if len(units) == 1 and units[0] == "lovelace":
            ada_only_count += 1
            if lovelace >= MIN_ADA_FOR_FEES:
                has_fee_utxo = True
        else:
            with_tokens_count += 1

    return ada_only_count, with_tokens_count, has_fee_utxo, total_lovelace


# ── Commands ────────────────────────────────────────────────────────────────


def cmd_check():
    """Full pre-flight check: UTXOs + on-chain state + .env governance values."""
    _, _, stake_vk, addr = load_wallet()
    address = str(addr)
    stake_addr = str(Address(staking_part=stake_vk.hash(), network=resolve_network(NETWORK)))
    api = get_api()
    errors = []

    console.print(f"\n[bold]Pre-flight check[/] — [bold]{NETWORK}[/]\n")

    # ── Helper ──
    def row(name: str, ok: bool, detail: str):
        status = "[green]PASS[/]" if ok else "[red]FAIL[/]"
        check_table.add_row(name, status, detail)
        if not ok:
            errors.append(name)

    def warn(name: str, detail: str):
        check_table.add_row(name, "[yellow]WARN[/]", detail)

    def require_env(name: str) -> str:
        value = os.getenv(name)
        if not value:
            errors.append(name)
            check_table.add_row(name, "[red]FAIL[/]", "missing from .env")
            return ""
        return value

    # ── 1. Wallet UTXOs ──
    try:
        utxos = api.address_utxos(address, gather_pages=True)
    except ApiError as e:
        console.print(f"[bold red]FAIL:[/] Cannot fetch UTXOs: {e}")
        sys.exit(1)

    if not utxos:
        console.print(f"[bold red]FAIL:[/] No UTXOs found for {address}")
        sys.exit(1)

    ada_only_count, with_tokens_count, has_fee_utxo, total_lovelace = analyze_utxos(utxos)

    utxo_table = Table(box=box.SIMPLE_HEAVY, title="Wallet UTXOs")
    utxo_table.add_column("UTXO", style="dim")
    utxo_table.add_column("ADA", justify="right", style="cyan")
    utxo_table.add_column("Tokens", justify="right")

    for u in utxos:
        units = [a.unit for a in u.amount]
        lovelace = sum(int(a.quantity) for a in u.amount if a.unit == "lovelace")
        token_count = len(units) - 1
        if token_count == 0:
            utxo_table.add_row(f"{u.tx_hash[:12]}...#{u.tx_index}", f"{lovelace/1e6:.2f}", "")
        else:
            utxo_table.add_row(f"{u.tx_hash[:12]}...#{u.tx_index}", f"{lovelace/1e6:.2f}", f"[yellow]+{token_count}[/]")

    console.print(utxo_table)
    console.print(f"[bold]Total:[/] {total_lovelace/1e6:.2f} ADA across {len(utxos)} UTXOs\n")

    # ── 2. Checks table ──
    check_table = Table(title="Pre-flight Checks", box=box.ROUNDED, show_lines=False)
    check_table.add_column("Check", style="bold")
    check_table.add_column("Status")
    check_table.add_column("Detail", style="dim")

    row("ADA-only UTXOs", ada_only_count >= MIN_ADA_ONLY_UTXOS, f"{ada_only_count}/{MIN_ADA_ONLY_UTXOS}")
    row("Token bundle UTXO", with_tokens_count >= 1, "found" if with_tokens_count >= 1 else "missing")
    row("Fee UTXO (>= 5 ADA)", has_fee_utxo, "found" if has_fee_utxo else "missing")

    # ── 3. On-chain state: stake key ──
    try:
        acct = api.accounts(stake_addr)
        stake_registered = acct.active
        stake_pool = acct.pool_id
        if stake_registered:
            detail = f"registered, delegated to {stake_pool}" if stake_pool else "registered, not delegated"
            warn("Stake key", detail + " (tests expect clean slate)")
        else:
            row("Stake key", True, "not registered (clean)")
    except ApiError as exc:
        if getattr(exc, "status_code", None) == 404:
            row("Stake key", True, "not registered (clean)")
        else:
            warn("Stake key", f"lookup error: {exc}")

    # ── 4. On-chain state: pool from cert ──
    pool_cert = require_env("POOL_REGISTRATION_CERT")
    if pool_cert and is_hex(pool_cert) and len(pool_cert) >= 60:
        # Extract pool key hash from cert CBOR (first 581c = 28-byte hash)
        try:
            idx = pool_cert.find("581c")
            if idx >= 0:
                cert_pool_hash = pool_cert[idx + 4 : idx + 4 + 56]
                current_epoch = blockfrost_get("epochs/latest").get("epoch", 0)
                pool_bech32 = bech32_encode("pool", bytes.fromhex(cert_pool_hash))
                # Check if pool is in the retiring list
                retiring_epoch = None
                for page in range(1, 5):
                    retiring = blockfrost_get("pools/retiring", count=100, page=page)
                    if not retiring:
                        break
                    for p in retiring:
                        if p.get("pool_id") == pool_bech32:
                            retiring_epoch = p.get("epoch")
                            break
                    if retiring_epoch:
                        break

                if retiring_epoch:
                    if current_epoch >= retiring_epoch:
                        row("Pool from cert", True, f"{cert_pool_hash[:16]}... retired at epoch {retiring_epoch} (re-registrable)")
                    else:
                        warn("Pool from cert", f"{cert_pool_hash[:16]}... retiring at epoch {retiring_epoch}, current={current_epoch} (still active!)")
                else:
                    # Not in retiring list — check if it exists at all
                    try:
                        api.pool(cert_pool_hash)
                        warn("Pool from cert", f"{cert_pool_hash[:16]}... active on-chain (not retired)")
                    except ApiError as exc:
                        if getattr(exc, "status_code", None) == 404:
                            row("Pool from cert", True, "not on-chain (fresh)")
                        else:
                            row("Pool from cert", True, f"{len(pool_cert)} hex chars")
            else:
                row("POOL_REGISTRATION_CERT", True, f"{len(pool_cert)} hex chars")
        except Exception as exc:
            row("POOL_REGISTRATION_CERT", True, f"{len(pool_cert)} hex chars (lookup: {exc})")
    elif pool_cert:
        row("POOL_REGISTRATION_CERT", False, "not valid even-length hex")

    # ── 5. .env governance: stake pool ──
    pool_hash = require_env("STAKE_POOL_HASH")
    if pool_hash and is_hex(pool_hash):
        try:
            api.pool(pool_hash)
            row("STAKE_POOL_HASH", True, f"{pool_hash[:16]}... (on-chain)")
        except ApiError as exc:
            if getattr(exc, "status_code", None) == 404:
                row("STAKE_POOL_HASH", False, "not found on-chain")
            else:
                row("STAKE_POOL_HASH", False, f"lookup error: {exc}")
    elif pool_hash:
        row("STAKE_POOL_HASH", False, "not valid hex")

    # ── 6. .env governance: DReps ──
    drep_key_hash = require_env("DREP_KEY_HASH_ID")
    if drep_key_hash:
        try:
            drep_id = drep_bech32(drep_key_hash, is_script=False)
            drep_info = blockfrost_get(f"governance/dreps/{drep_id}")
            active = drep_info.get("active", False)
            expired = drep_info.get("expired", False)
            detail = f"{drep_key_hash[:16]}... active={active} expired={expired}"
            row("DREP_KEY_HASH_ID", active and not expired, detail)
        except Exception as exc:
            row("DREP_KEY_HASH_ID", False, f"not found/error: {exc}")

    drep_script_hash = require_env("DREP_SCRIPT_HASH_ID")
    if drep_script_hash:
        try:
            drep_id = drep_bech32(drep_script_hash, is_script=True)
            drep_info = blockfrost_get(f"governance/dreps/{drep_id}")
            active = drep_info.get("active", False)
            expired = drep_info.get("expired", False)
            has_script = drep_info.get("has_script", False)
            detail = f"{drep_script_hash[:16]}... active={active} expired={expired} script={has_script}"
            row("DREP_SCRIPT_HASH_ID", active and not expired and has_script, detail)
        except Exception as exc:
            row("DREP_SCRIPT_HASH_ID", False, f"not found/error: {exc}")

    # ── 7. .env governance: proposal ──
    proposal_id = require_env("POOL_GOVERNANCE_PROPOSAL_ID")
    if proposal_id and len(proposal_id) >= 66:
        try:
            tx_hash = proposal_id[:64]
            cert_index = int(proposal_id[64:], 16)
            info = blockfrost_get(f"governance/proposals/{tx_hash}/{cert_index}")

            enacted = info.get("enacted_epoch")
            dropped = info.get("dropped_epoch")
            expired = info.get("expired_epoch")
            is_open = not any([enacted, dropped, expired])
            status = "open" if is_open else f"closed(enacted={enacted} dropped={dropped} expired={expired})"
            row("POOL_GOVERNANCE_PROPOSAL_ID", is_open, f"{proposal_id[:16]}... {status}")
        except Exception as exc:
            row("POOL_GOVERNANCE_PROPOSAL_ID", False, f"not found/error: {exc}")

    vote = os.getenv("POOL_VOTE_CHOICE", "yes")
    row("POOL_VOTE_CHOICE", vote in ("yes", "no", "abstain"), vote)

    console.print(check_table)
    console.print()

    if errors:
        hints = []
        if ada_only_count < MIN_ADA_ONLY_UTXOS:
            hints.append(f"Need {MIN_ADA_ONLY_UTXOS - ada_only_count} more ADA-only UTXOs → [bold]uv run prepare_wallet.py split[/]")
        if with_tokens_count < 1:
            hints.append("Need 1 token UTXO → [bold]uv run prepare_wallet.py mint[/]")
        gov_fails = [e for e in errors if e.startswith("DREP_") or e.startswith("POOL_GOVERNANCE")]
        if gov_fails:
            hints.append("Stale governance values → [bold]uv run prepare_wallet.py lookup[/]")
        console.print(Panel(f"[bold red]{len(errors)} check(s) failed[/]" + ("\n" + "\n".join(hints) if hints else ""), border_style="red"))
        sys.exit(1)
    else:
        console.print(Panel("[bold green]All checks passed — ready for e2e tests[/]", border_style="green"))


def cmd_split():
    """Split a large UTXO into 12+ smaller ADA-only ones."""
    pay_sk, _, _, addr = load_wallet()
    context = get_context()

    try:
        all_utxos = context.utxos(addr)
    except Exception as e:
        console.print(f"[bold red]ERROR:[/] Fetching UTXOs: {e}")
        sys.exit(1)

    required = SPLIT_NUM_OUTPUTS * SPLIT_OUTPUT_LOVELACE + SPLIT_FEE_HEADROOM
    selected, total_ada = select_ada_utxos(all_utxos, required)

    if not selected:
        console.print("[bold red]ERROR:[/] No ADA-only UTXOs found to split.")
        sys.exit(1)
    if total_ada < required:
        console.print(f"[bold red]ERROR:[/] Need at least {required/1e6:.0f} ADA, have {total_ada/1e6:.2f} ADA")
        sys.exit(1)

    console.print(f"Selected {len(selected)} UTXOs with [cyan]{total_ada/1e6:.2f} ADA[/] total")
    console.print(f"Creating {SPLIT_NUM_OUTPUTS} outputs of [cyan]{SPLIT_OUTPUT_LOVELACE/1e6:.0f} ADA[/] each...")

    builder = TransactionBuilder(context)
    for utxo in selected:
        builder.add_input(utxo)
    for _ in range(SPLIT_NUM_OUTPUTS):
        builder.add_output(TransactionOutput(addr, SPLIT_OUTPUT_LOVELACE))

    try:
        tx = builder.build_and_sign(signing_keys=[pay_sk], change_address=addr)
    except Exception as e:
        console.print(f"[bold red]ERROR building transaction:[/] {e}")
        sys.exit(1)

    console.print(f"Transaction built · [dim]{len(tx.to_cbor())} bytes[/]")

    try:
        tx_hash = context.submit_tx(tx)
    except Exception as e:
        console.print(f"[bold red]ERROR submitting transaction:[/] {e}")
        sys.exit(1)

    console.print(Panel(f"TX hash: [bold]{tx_hash}[/]\n\nWait ~20s, then run: [bold]uv run prepare_wallet.py status[/]", title="Submitted", border_style="green"))


def cmd_mint():
    """Mint a native token to create a UTXO with a token bundle."""
    pay_sk, pay_vk, _, addr = load_wallet()
    context = get_context()

    try:
        all_utxos = context.utxos(addr)
    except Exception as e:
        console.print(f"[bold red]ERROR:[/] Fetching UTXOs: {e}")
        sys.exit(1)

    selected, total_ada = select_ada_utxos(all_utxos, 5_000_000, max_count=3)
    if not selected:
        console.print("[bold red]ERROR:[/] No ADA-only UTXOs found for minting.")
        sys.exit(1)

    policy_script = ScriptPubkey(pay_vk.hash())
    policy_id = policy_script.hash()
    token_name = AssetName(MINT_TOKEN_NAME)

    console.print(f"Minting [bold cyan]{MINT_AMOUNT}[/] {token_name.payload.decode()} under policy [dim]{policy_id.payload.hex()}[/]")

    builder = TransactionBuilder(context)
    for utxo in selected:
        builder.add_input(utxo)

    builder.mint = MultiAsset({policy_id: Asset({token_name: MINT_AMOUNT})})
    builder.native_scripts = [policy_script]

    multi_asset = MultiAsset({policy_id: Asset({token_name: MINT_AMOUNT})})
    provisional_output = TransactionOutput(addr, Value(0, multi_asset))
    required_lovelace = min_lovelace(context, output=provisional_output)
    builder.add_output(TransactionOutput(addr, Value(required_lovelace, multi_asset)))

    console.print(f"Token output min lovelace: [cyan]{required_lovelace/1e6:.2f} ADA[/]")

    try:
        tx = builder.build_and_sign(signing_keys=[pay_sk], change_address=addr)
    except Exception as e:
        console.print(f"[bold red]ERROR building transaction:[/] {e}")
        sys.exit(1)

    console.print(f"Transaction built · [dim]{len(tx.to_cbor())} bytes[/]")

    try:
        tx_hash = context.submit_tx(tx)
    except Exception as e:
        console.print(f"[bold red]ERROR submitting transaction:[/] {e}")
        sys.exit(1)

    console.print(Panel(f"TX hash: [bold]{tx_hash}[/]\n\nWait ~20s, then run: [bold]uv run prepare_wallet.py status[/]", title="Submitted", border_style="green"))


def cmd_lookup():
    """Look up governance values for .env using the Blockfrost REST API."""
    console.print(f"\nLooking up [bold]{NETWORK}[/] network governance data...\n")

    table = Table(title="Governance Values", box=box.ROUNDED, show_lines=True)
    table.add_column("Variable", style="bold", no_wrap=True)
    table.add_column("Value", style="cyan", overflow="fold")

    # 1. Stake pool
    try:
        pools = blockfrost_get("pools", count=1, page=1, order="asc")
        if pools:
            pool_info = blockfrost_get(f"pools/{pools[0]}")
            table.add_row("STAKE_POOL_HASH", pool_info.get("hex", pools[0]))
        else:
            table.add_row("STAKE_POOL_HASH", "[red]No pools found[/]")
    except Exception as e:
        table.add_row("STAKE_POOL_HASH", f"[red]Error: {e}[/]")

    # 2. DReps — newest first (order=desc), cap checks to avoid slow N+1
    try:
        key_hex, script_hex = None, None
        checked = 0
        max_checks = 200
        with console.status("Searching for active DReps...") as status:
            for page in range(1, 100):
                dreps = blockfrost_get("governance/dreps", count=100, page=page, order="desc")
                if not dreps:
                    break
                for d in dreps:
                    if checked >= max_checks:
                        break
                    checked += 1
                    drep_id = d.get("drep_id", "")
                    status.update(f"Checking DRep {checked}/{max_checks}...")
                    detail = blockfrost_get(f"governance/dreps/{drep_id}")
                    if not detail.get("active", False) or detail.get("expired", False):
                        continue
                    raw_hex = detail.get("hex", "")
                    has_script = detail.get("has_script", False)
                    # Strip credential type prefix (22=key, 23=script)
                    expected_prefix = "23" if has_script else "22"
                    clean_hex = raw_hex[2:] if raw_hex.startswith(expected_prefix) else raw_hex
                    if has_script and not script_hex:
                        script_hex = clean_hex
                    elif not has_script and not key_hex:
                        key_hex = clean_hex
                    if key_hex and script_hex:
                        break
                if key_hex and script_hex or checked >= max_checks:
                    break

        table.add_row("DREP_KEY_HASH_ID", key_hex or f"[red]Not found (checked {checked})[/]")
        table.add_row("DREP_SCRIPT_HASH_ID", script_hex or f"[red]Not found (checked {checked})[/]")
    except Exception as e:
        table.add_row("DREP_KEY_HASH_ID", f"[red]Error: {e}[/]")
        table.add_row("DREP_SCRIPT_HASH_ID", f"[red]Error: {e}[/]")

    # 3. Governance proposals — newest first, find an open one
    try:
        found_proposal = None
        checked = 0
        with console.status("Searching for open proposals...") as status:
            for page in range(1, 10):
                proposals = blockfrost_get("governance/proposals", count=100, page=page, order="desc")
                if not proposals:
                    break
                for p in proposals:
                    checked += 1
                    tx_hash = p.get("tx_hash", "")
                    cert_index = p.get("cert_index", 0)
                    status.update(f"Checking proposal {checked}...")
                    detail = blockfrost_get(f"governance/proposals/{tx_hash}/{cert_index}")
                    if not any([detail.get("enacted_epoch"), detail.get("dropped_epoch"), detail.get("expired_epoch")]):
                        found_proposal = f"{tx_hash}{int(cert_index):02d}"
                        break
                if found_proposal:
                    break
        if found_proposal:
            table.add_row("POOL_GOVERNANCE_PROPOSAL_ID", found_proposal)
        else:
            table.add_row("POOL_GOVERNANCE_PROPOSAL_ID", f"[red]No open proposals found (checked {checked})[/]")
    except Exception as e:
        table.add_row("POOL_GOVERNANCE_PROPOSAL_ID", f"[red]Error: {e}[/]")

    table.add_row("POOL_VOTE_CHOICE", "yes")

    console.print(table)
    console.print("\n[dim]Copy the values above into your .env file.[/]")
    console.print("[dim]POOL_REGISTRATION_CERT needs to be generated separately (pool-specific).[/]")


# ── Main ────────────────────────────────────────────────────────────────────


def cmd_help():
    """Show available commands."""
    console.print("\n[bold]prepare_wallet.py[/] — e2e test wallet manager\n")
    commands = [
        ("check", "Full pre-flight check: UTXOs + on-chain state + .env governance"),
        ("split", "Split a large UTXO into 12+ smaller ADA-only outputs"),
        ("mint", "Mint a native token to create a UTXO with a token bundle"),
        ("lookup", "Look up fresh governance values (DReps, proposals) for .env"),
        ("help", "Show this help"),
    ]
    table = Table(box=box.SIMPLE, show_header=False, padding=(0, 2))
    table.add_column(style="bold cyan")
    table.add_column(style="dim")
    for name, desc in commands:
        table.add_row(name, desc)
    console.print(table)
    console.print("\n[dim]Usage: uv run prepare_wallet.py <command>[/]")
    console.print("[dim]Aliases: status, validate → check[/]\n")


COMMANDS = {
    "check": cmd_check,
    "status": cmd_check,       # alias
    "validate": cmd_check,     # alias
    "split": cmd_split,
    "mint": cmd_mint,
    "lookup": cmd_lookup,
    "help": cmd_help,
}

if __name__ == "__main__":
    if not BLOCKFROST_KEY:
        console.print("[bold red]ERROR:[/] Set BLOCKFROST_PROJECT_ID in .env")
        sys.exit(1)
    if not MNEMONIC:
        console.print("[bold red]ERROR:[/] Set TEST_WALLET_MNEMONIC in .env")
        sys.exit(1)

    cmd = sys.argv[1] if len(sys.argv) > 1 else "check"

    if cmd not in COMMANDS:
        console.print(f"[bold red]Unknown command:[/] {cmd}")
        cmd_help()
        sys.exit(1)

    COMMANDS[cmd]()
